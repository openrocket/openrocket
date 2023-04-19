package net.sf.openrocket.aerodynamics.barrowman;

import static java.lang.Math.pow;
import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.Arrays;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.Warning.Other;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LinearInterpolator;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;
import net.sf.openrocket.util.Transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preliminary computation of tube fin aerodynamics.
 *
 */
public class TubeFinSetCalc extends TubeCalc {
	private final static Logger log = LoggerFactory.getLogger(TubeFinSetCalc.class);
	
	private static final double STALL_ANGLE = (20 * Math.PI / 180);
	private final double[] poly = new double[6];
	
	// parameters straight from configuration; we'll be grabbing them once
	// so code is a bit shorter elsewhere
	private final double bodyRadius;
	private final double chord;
	private final double innerRadius;
	private final double outerRadius;
	private final int tubeCount;
	private final double baseRotation;
	// at present tubes are only allowed a cant angle of 0
	private final double cantAngle;
	
	// values we can precompute once
	private final double ar;
	private final double intersticeArea;
	private final double wettedArea;
	private final double cnaconst;
		
	protected final WarningSet geometryWarnings = new WarningSet();
	
	public TubeFinSetCalc(RocketComponent component) {
		super(component);
		if (!(component instanceof TubeFinSet)) {
			throw new IllegalArgumentException("Illegal component type " + component);
		}
		
		final TubeFinSet tubes = (TubeFinSet) component;

		if (tubes.getTubeSeparation() > MathUtil.EPSILON) {
			geometryWarnings.add(Warning.TUBE_SEPARATION);
		} else if (tubes.getTubeSeparation() < -MathUtil.EPSILON) {
			geometryWarnings.add(Warning.TUBE_OVERLAP);
		}

		bodyRadius = tubes.getBodyRadius();
		chord = tubes.getLength();
		innerRadius = tubes.getInnerRadius();
		outerRadius = tubes.getOuterRadius();
		tubeCount = tubes.getFinCount();
		baseRotation = tubes.getBaseRotation();
		// at present, tube cant angle can only be 0
		cantAngle = 0;
		// cantAngle = tubes.getCantAngle();
		
		// precompute geometry.  This will be the geometry of a single tube, since BarrowmanCalculator
		// iterates across them.  Doesn't consider interference between them; that should only be relevant for
		// fins that are either separated or overlapping.

		// aspect ratio.
		ar = 2 * innerRadius / chord;
		
		// wetted area for friction drag calculation.  We don't consider the inner surface of the tube;
		// that affects the pressure drop through the tube and so (indirecctly) affects the pressure drag.
			
		// Area of the outer surface of tubes.  Since roughly half
		// of the area is "masked" by the interstices between the tubes and the
		// body tube, only consider the other half of the area (so only multiplying by pi instead of 2*pi)
		final double outerArea = chord * Math.PI * outerRadius;
			
		// Surface area of the portion of the body tube masked by the tube fins, per tube
		final BodyTube parent = (BodyTube) tubes.getParent();
		final double maskedArea = chord * 2.0 * Math.PI * bodyRadius / tubeCount;
		
		wettedArea = outerArea - maskedArea;
		log.debug("wetted area of tube fins " + wettedArea);

		// frontal area of interstices between tubes for pressure drag calculation.
		// We'll treat them as a closed blunt object.

		// area of disk passing through tube fin centers
		final double tubeDiskArea = Math.PI * MathUtil.pow2(bodyRadius + outerRadius);

		// half of combined area of tube fin exteriors.  Deliberately using the outer radius here since we
		// calculate pressure drag from the tube walls in TubeCalc
		final double tubeOuterArea = tubeCount * Math.PI * MathUtil.pow2(outerRadius) / 2.0;

		// body tube area
		final double bodyTubeArea = Math.PI * MathUtil.pow2(bodyRadius);

		// area of an interstice
		intersticeArea = (tubeDiskArea - tubeOuterArea - bodyTubeArea) / tubeCount;

		// Precompute most of CNa.  Equation comes from Ribner, "The ring airfoil in nonaxial
		// flow", Journal of the Aeronautical Sciences 14(9) pp 529-530 (1947) equation (5).
		// As stated in techdoc.pdf, it's normalized by (1/2) rho v^2 (see section 3.1.1)
		final double arprime = 2 * ar / Math.PI;
		cnaconst = 2 * (arprime / (1 + arprime)) * Math.PI * Math.PI * innerRadius * chord;
		log.debug("ar " + ar + ", cnaconst " + cnaconst);
	}
	
	/*
	 * Calculates the non-axial forces produced by the fins (normal and side forces,
	 * pitch, yaw and roll moments, CP position, CNa).
	 */
	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
										AerodynamicForces forces, WarningSet warnings) {

		if (outerRadius < 0.001) {
			forces.setCm(0);
			forces.setCN(0);
			forces.setCNa(0);
			forces.setCP(Coordinate.NUL);
			forces.setCroll(0);
			forces.setCrollDamp(0);
			forces.setCrollForce(0);
			forces.setCside(0);
			forces.setCyaw(0);
			return;
		}
		
		// Calculate CNa
		log.debug("body radius " + bodyRadius + ", ref area " + conditions.getRefArea());
		final double cna = cnaconst / conditions.getRefArea();
		
		// Calculate CP position
		double x = calculateCPPos(conditions) * chord;
		// log.debug("CP position " + x);
		
		// Roll forces
		// This isn't really tested, since the cant angle is required to be 0.
		forces.setCrollForce((bodyRadius + outerRadius) * cna * cantAngle /
							 conditions.getRefLength());
		
		if (conditions.getAOA() > STALL_ANGLE) {
			//			log.debug("Tube stalling in roll");
			forces.setCrollForce(forces.getCrollForce() *
								 MathUtil.clamp(1 - (conditions.getAOA() - STALL_ANGLE) / (STALL_ANGLE / 2), 0, 1));
		}

		forces.setCrollDamp((bodyRadius + outerRadius) * conditions.getRollRate()/conditions.getVelocity() * cna / conditions.getRefLength());
		
		forces.setCroll(forces.getCrollForce() - forces.getCrollDamp());
		
		forces.setCNa(cna);
		forces.setCN(cna * MathUtil.min(conditions.getAOA(), STALL_ANGLE));
		forces.setCP(new Coordinate(x, 0, 0, cna));
		forces.setCm(forces.getCN() * x / conditions.getRefLength());
		
		/*
		 * TODO: HIGH:  Compute actual side force and yaw moment.
		 * This is not currently performed because it produces strange results for
		 * stable rockets that have two fins in the front part of the fuselage,
		 * where the rocket flies at an ever-increasing angle of attack.  This may
		 * be due to incorrect computation of pitch/yaw damping moments.
		 */
		//		if (fins == 1 || fins == 2) {
		//			forces.Cside = fins * cna1 * Math.cos(theta-angle) * Math.sin(theta-angle);
		//			forces.Cyaw = fins * forces.Cside * x / conditions.getRefLength();
		//		} else {
		//			forces.Cside = 0;
		//			forces.Cyaw = 0;
		//		}
		forces.setCside(0);
		forces.setCyaw(0);

		log.debug(forces.toString());
	}
	
	/**
	 * Return the relative position of the CP along the mean aerodynamic chord.
	 * Below mach 0.5 it is at the quarter chord, above mach 2 calculated using an
	 * empirical formula, between these two using an interpolation polynomial.
	 * 
	 * @param cond   Mach speed used
	 * @return		 CP position along the MAC
	 */
	private double calculateCPPos(FlightConditions cond) {
		double m = cond.getMach();
		//		log.debug("m = {} ", m);
		if (m <= 0.5) {
			// At subsonic speeds CP at quarter chord
			return 0.25;
		}
		if (m >= 2) {
			// At supersonic speeds use empirical formula
			double beta = cond.getBeta();
			return (ar * beta - 0.67) / (2 * ar * beta - 1);
		}
		
		// In between use interpolation polynomial
		double x = 1.0;
		double val = 0;
		
		for (int i = 0; i < poly.length; i++) {
			val += poly[i] * x;
			x *= m;
		}
		//		log.debug("val = {}", val);
		return val;
	}
	
	/**
	 * Calculate CP position interpolation polynomial coefficients from the
	 * fin geometry.  This is a fifth order polynomial that satisfies
	 * 
	 * p(0.5)=0.25
	 * p'(0.5)=0
	 * p(2) = f(2)
	 * p'(2) = f'(2)
	 * p''(2) = 0
	 * p'''(2) = 0
	 * 
	 * where f(M) = (ar*sqrt(M^2-1) - 0.67) / (2*ar*sqrt(M^2-1) - 1).
	 * 
	 * The values were calculated analytically in Mathematica.  The coefficients
	 * are used as poly[0] + poly[1]*x + poly[2]*x^2 + ...
	 */
	private void calculatePoly() {
		double denom = pow2(1 - 3.4641 * ar); // common denominator
		
		poly[5] = (-1.58025 * (-0.728769 + ar) * (-0.192105 + ar)) / denom;
		poly[4] = (12.8395 * (-0.725688 + ar) * (-0.19292 + ar)) / denom;
		poly[3] = (-39.5062 * (-0.72074 + ar) * (-0.194245 + ar)) / denom;
		poly[2] = (55.3086 * (-0.711482 + ar) * (-0.196772 + ar)) / denom;
		poly[1] = (-31.6049 * (-0.705375 + ar) * (-0.198476 + ar)) / denom;
		poly[0] = (9.16049 * (-0.588838 + ar) * (-0.20624 + ar)) / denom;
	}

	@Override
	public double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings) {
		warnings.addAll(geometryWarnings);

		final double frictionCD =  componentCf * wettedArea / conditions.getRefArea();
		
		return frictionCD;
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
					  double stagnationCD, double baseCD, WarningSet warnings) {
		
	    warnings.addAll(geometryWarnings);
		final double cd = super.calculatePressureCD(conditions, stagnationCD, baseCD, warnings) +
			(stagnationCD + baseCD) * intersticeArea / conditions.getRefArea();
	    
	    return cd;
		}
	
	private static int calculateInterferenceFinCount(TubeFinSet component) {
		RocketComponent parent = component.getParent();
		if (parent == null) {
			throw new IllegalStateException("fin set without parent component");
		}
		
		return 3 * component.getFinCount();
	}
}
