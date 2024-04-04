package info.openrocket.core.aerodynamics.barrowman;

import static info.openrocket.core.util.MathUtil.pow2;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Transformation;

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

	private final TubeFinSet tubes;

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
		
		tubes = (TubeFinSet) component;

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

		// precompute geometry. This will be the geometry of a single tube, since
		// BarrowmanCalculator
		// iterates across them. Doesn't consider interference between them; that should
		// only be relevant for
		// fins that are either separated or overlapping.

		// aspect ratio.
		ar = 2 * innerRadius / chord;


		// Some trigonometry...
		// We need a triangle with the following three sides:
		// d is from the center of the body tube to a tangent point on the tube fin
		// outerRadius is from the center of the tube fin to the tangent point.  Note that
		//     d and outerRadius are at right angles
		// bodyRadius + outerRadius is from the center of the body tube to the center of the tube fin.
		//     This is the hypotenuse of the right triangle.

		// Find length of d
		final double d = Math.sqrt(MathUtil.pow2(bodyRadius + outerRadius) - MathUtil.pow2(outerRadius));

		// Area of diamond formed by mirroring triangle on its hypotenuse (same area as rectangle
		// formed by d and outerarea, but it *isn't* that rectangle)
		double a = d * outerRadius;

		// angle between outerRadius and bodyRadius+outerRadius
		final double theta1 = Math.acos(outerRadius/(outerRadius + bodyRadius));

		// area of arc from tube fin, doubled to get both halves of diamond
		final double a1 = MathUtil.pow2(outerRadius) * theta1;

		// angle between bodyRadius+outerRadius and d
		final double theta2 = Math.PI/2.0 - theta1;

		// area of arc from body tube.  Doubled so we have area to remove from diamond
		final double a2 = MathUtil.pow2(bodyRadius) * theta2;

		// area of interstice for one tube fin
		intersticeArea = (a - a1 - a2);

		// for comparison, what's the area of a tube fin?
		double tubeArea = MathUtil.pow2(outerRadius) * Math.PI;

		// wetted area for friction drag calculation.  We don't consider the inner surface of the tube;
		// that affects the pressure drop through the tube and so (indirecctly) affects the pressure drag.

		// Area of the outer surface of a tube, not including portion masked by interstice
		final double outerArea = chord * 2.0 * (Math.PI - theta1) * outerRadius;

		// Surface area of the portion of the body tube masked by the tube fin.  We'll subtract it from
		// the tube fin area rather than go in and change the body tube surface area calculation. If tube
		// fin and body tube roughness aren't the same this will result in an inaccuracy.
		final double maskedArea = chord * 2.0 * theta2 * bodyRadius;
		
		wettedArea = outerArea - maskedArea;

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
			// log.debug("Tube stalling in roll");
			forces.setCrollForce(forces.getCrollForce() *
					MathUtil.clamp(1 - (conditions.getAOA() - STALL_ANGLE) / (STALL_ANGLE / 2), 0, 1));
		}

		forces.setCrollDamp((bodyRadius + outerRadius) * conditions.getRollRate() / conditions.getVelocity() * cna
				/ conditions.getRefLength());

		forces.setCroll(forces.getCrollForce() - forces.getCrollDamp());

		forces.setCNa(cna);
		forces.setCN(cna * MathUtil.min(conditions.getAOA(), STALL_ANGLE));
		forces.setCP(new Coordinate(x, 0, 0, cna));
		forces.setCm(forces.getCN() * x / conditions.getRefLength());

		/*
		 * TODO: HIGH: Compute actual side force and yaw moment.
		 * This is not currently performed because it produces strange results for
		 * stable rockets that have two fins in the front part of the fuselage,
		 * where the rocket flies at an ever-increasing angle of attack. This may
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
	 * @param cond Mach speed used
	 * @return CP position along the MAC
	 */
	private double calculateCPPos(FlightConditions cond) {
		double m = cond.getMach();
		// log.debug("m = {} ", m);
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
		// log.debug("val = {}", val);
		return val;
	}

	/**
	 * Calculate CP position interpolation polynomial coefficients from the
	 * fin geometry. This is a fifth order polynomial that satisfies
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
	 * The values were calculated analytically in Mathematica. The coefficients
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
		final double frictionCD = componentCf * wettedArea / conditions.getRefArea();

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
}
