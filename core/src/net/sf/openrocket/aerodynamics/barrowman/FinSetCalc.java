package net.sf.openrocket.aerodynamics.barrowman;

import static java.lang.Math.pow;
import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.Arrays;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LinearInterpolator;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FinSetCalc extends RocketComponentCalc {
	
	private final static Logger logger = LoggerFactory.getLogger(FinSetCalc.class);
	
	private static final double STALL_ANGLE = (20 * Math.PI / 180);
	
	/** Number of divisions in the fin chords. */
	protected static final int DIVISIONS = 48;
	
	protected double macLength = Double.NaN; // MAC length
	protected double macLead = Double.NaN; // MAC leading edge position
	protected double macSpan = Double.NaN; // MAC spanwise position
	protected double finArea = Double.NaN; // Fin area
	protected double ar = Double.NaN; // Fin aspect ratio
	protected double span = Double.NaN; // Fin span
	protected double cosGamma = Double.NaN; // Cosine of midchord sweep angle
	protected double cosGammaLead = Double.NaN; // Cosine of leading edge sweep angle
	protected double rollSum = Double.NaN; // Roll damping sum term
	
	protected int interferenceFinCount = -1; // No. of fins in interference
	
	protected double[] chordLead = new double[DIVISIONS];
	protected double[] chordTrail = new double[DIVISIONS];
	protected double[] chordLength = new double[DIVISIONS];
	
	protected final WarningSet geometryWarnings = new WarningSet();
	
	private double[] poly = new double[6];
	
	private final double thickness;
	private final double bodyRadius;
	private final int finCount;
	private final double baseRotation;
	private final double cantAngle;
	private final FinSet.CrossSection crossSection;
	
	public FinSetCalc(RocketComponent component) {
		super(component);
		if (!(component instanceof FinSet)) {
			throw new IllegalArgumentException("Illegal component type " + component);
		}
		
		FinSet fin = (FinSet) component;
		thickness = fin.getThickness();
		bodyRadius = fin.getBodyRadius();
		finCount = fin.getFinCount();
		baseRotation = fin.getBaseRotation();
		cantAngle = fin.getCantAngle();
		span = fin.getSpan();
		finArea = fin.getFinArea();
		crossSection = fin.getCrossSection();
		
		calculateFinGeometry(fin);
		calculatePoly();
		calculateInterferenceFinCount(fin);
	}
	
	/*
	 * Calculates the non-axial forces produced by the fins (normal and side forces,
	 * pitch, yaw and roll moments, CP position, CNa).
	 */
	@Override
	public void calculateNonaxialForces(FlightConditions conditions,
			AerodynamicForces forces, WarningSet warnings) {
		
		if (span < 0.001) {
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
		
		// Add warnings  (radius/2 == diameter/4)
		if (thickness > bodyRadius / 2) {
			warnings.add(Warning.THICK_FIN);
		}
		warnings.addAll(geometryWarnings);
		
		//////// Calculate CNa.  /////////
		
		// One fin without interference (both sub- and supersonic):
		double cna1 = calculateFinCNa1(conditions);
		
		//		logger.debug("Component cna1 = {}", cna1);
		
		// Multiple fins with fin-fin interference
		double cna;
		double theta = conditions.getTheta();
		double angle = baseRotation;
		
		// Compute basic CNa without interference effects
		if (finCount == 1 || finCount == 2) {
			// Basic CNa from geometry
			double mul = 0;
			for (int i = 0; i < finCount; i++) {
				mul += MathUtil.pow2(Math.sin(theta - angle));
				angle += 2 * Math.PI / finCount;
			}
			cna = cna1 * mul;
		} else {
			// Basic CNa assuming full efficiency
			cna = cna1 * finCount / 2.0;
		}
		
		//		logger.debug("Component cna = {}", cna);
		
		// Take into account fin-fin interference effects
		switch (interferenceFinCount) {
		case 1:
		case 2:
		case 3:
		case 4:
			// No interference effect
			break;
		
		case 5:
			cna *= 0.948;
			break;
		
		case 6:
			cna *= 0.913;
			break;
		
		case 7:
			cna *= 0.854;
			break;
		
		case 8:
			cna *= 0.81;
			break;
		
		default:
			// Assume 75% efficiency
			cna *= 0.75;
			warnings.add(Warning.PARALLEL_FINS);
			break;
		}
		
		/*
		 * Used in 0.9.5 and earlier.  Takes into account rotation angle for three
		 * and four fins, does not take into account interference from other fin sets.
		 * 
		switch (fins) {
		case 1:
		case 2:
			// from geometry
			double mul = 0;
			for (int i=0; i < fins; i++) {
				mul += MathUtil.pow2(Math.sin(theta - angle));
				angle += 2 * Math.PI / fins;
			}
			cna = cna1*mul;
			break;
			
		case 3:
			// multiplier 1.5, sinusoidal reduction of 15%
			cna = cna1 * 1.5 * (1 - 0.15*pow2(Math.cos(1.5 * (theta-angle))));
			break;
			
		case 4:
			// multiplier 2.0, sinusoidal reduction of 6%
			cna = cna1 * 2.0 * (1 - 0.06*pow2(Math.sin(2 * (theta-angle))));
			break;
			
		case 5:
			cna = 2.37 * cna1;
			break;
			
		case 6:
			cna = 2.74 * cna1;
			break;
			
		case 7:
			cna = 2.99 * cna1;
			break;
			
		case 8:
			cna = 3.24 * cna1;
			break;
			
		default:
			// Assume N/2 * 3/4 efficiency for more fins
			cna = cna1 * fins * 3.0/8.0;
			break;
		}
		*/
		
		// Body-fin interference effect
		double r = bodyRadius;
		double tau = r / (span + r);
		if (Double.isNaN(tau) || Double.isInfinite(tau))
			tau = 0;
		cna *= 1 + tau; // Classical Barrowman
		//		cna *= pow2(1 + tau);	// Barrowman thesis (too optimistic??)
		//		logger.debug("Component cna = {}", cna);
		
		// TODO: LOW: check for fin tip mach cone interference
		// (Barrowman thesis pdf-page 40)
		
		// TODO: LOW: fin-fin mach cone effect, MIL-HDBK page 5-25
		
		// Calculate CP position
		double x = macLead + calculateCPPos(conditions) * macLength;
		//		logger.debug("Component macLead = {}", macLead);
		//		logger.debug("Component macLength = {}", macLength);
		//		logger.debug("Component x = {}", x);
		
		
		// Calculate roll forces, reduce forcing above stall angle
		
		// Without body-fin interference effect:
		//		forces.CrollForce = fins * (macSpan+r) * cna1 * component.getCantAngle() / 
		//			conditions.getRefLength();
		// With body-fin interference effect:
		forces.setCrollForce(finCount * (macSpan + r) * cna1 * (1 + tau) * cantAngle / conditions.getRefLength());
		
		if (conditions.getAOA() > STALL_ANGLE) {
			//			System.out.println("Fin stalling in roll");
			forces.setCrollForce(forces.getCrollForce() * MathUtil.clamp(
					1 - (conditions.getAOA() - STALL_ANGLE) / (STALL_ANGLE / 2), 0, 1));
		}
		forces.setCrollDamp(calculateDampingMoment(conditions));
		forces.setCroll(forces.getCrollForce() - forces.getCrollDamp());
		
		//		System.out.printf(component.getName() + ":  roll rate:%.3f  force:%.3f  damp:%.3f  " +
		//				"total:%.3f\n",
		//				conditions.getRollRate(), forces.CrollForce, forces.CrollDamp, forces.Croll);
		
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
		
	}
	
	/**
	 * Returns the MAC length of the fin.  This is required in the friction drag
	 * computation.
	 * 
	 * @return  the MAC length of the fin.
	 */
	public double getMACLength() {
		return macLength;
	}
	
	public double getMidchordPos() {
		return macLead + 0.5 * macLength;
	}
	
	/**
	 * Pre-calculates the fin geometry values.
	 */
	protected void calculateFinGeometry(FinSet component) {
		
		span = component.getSpan();
		finArea = component.getFinArea();
		ar = 2 * pow2(span) / finArea;
		
		Coordinate[] points = component.getFinPoints();
		
		// Check for jagged edges
		geometryWarnings.clear();
		boolean down = false;
		for (int i = 1; i < points.length; i++) {
			if ((points[i].y > points[i - 1].y + 0.001) && down) {
				geometryWarnings.add(Warning.JAGGED_EDGED_FIN);
				break;
			}
			if (points[i].y < points[i - 1].y - 0.001) {
				down = true;
			}
		}
		
		// Calculate the chord lead and trail positions and length
		
		Arrays.fill(chordLead, Double.POSITIVE_INFINITY);
		Arrays.fill(chordTrail, Double.NEGATIVE_INFINITY);
		Arrays.fill(chordLength, 0);
		
		for (int point = 1; point < points.length; point++) {
			double x1 = points[point - 1].x;
			double y1 = points[point - 1].y;
			double x2 = points[point].x;
			double y2 = points[point].y;
			
			// Don't use the default EPSILON since it is too small
			// and causes too much numerical instability in the computation of x below
			if (MathUtil.equals(y1, y2, 0.001))
				continue;
			
			int i1 = (int) (y1 * 1.0001 / span * (DIVISIONS - 1));
			int i2 = (int) (y2 * 1.0001 / span * (DIVISIONS - 1));
			i1 = MathUtil.clamp(i1, 0, DIVISIONS - 1);
			i2 = MathUtil.clamp(i2, 0, DIVISIONS - 1);
			if (i1 > i2) {
				int tmp = i2;
				i2 = i1;
				i1 = tmp;
			}
			
			for (int i = i1; i <= i2; i++) {
				// Intersection point (x,y)
				double y = i * span / (DIVISIONS - 1);
				double x = (y - y2) / (y1 - y2) * x1 + (y1 - y) / (y1 - y2) * x2;
				if (x < chordLead[i])
					chordLead[i] = x;
				if (x > chordTrail[i])
					chordTrail[i] = x;
				
				// TODO: LOW:  If fin point exactly on chord line, might be counted twice:
				if (y1 < y2) {
					chordLength[i] -= x;
				} else {
					chordLength[i] += x;
				}
			}
		}
		
		// Check and correct any inconsistencies
		for (int i = 0; i < DIVISIONS; i++) {
			if (Double.isInfinite(chordLead[i]) || Double.isInfinite(chordTrail[i]) ||
					Double.isNaN(chordLead[i]) || Double.isNaN(chordTrail[i])) {
				chordLead[i] = 0;
				chordTrail[i] = 0;
			}
			if (chordLength[i] < 0 || Double.isNaN(chordLength[i])) {
				chordLength[i] = 0;
			}
			if (chordLength[i] > chordTrail[i] - chordLead[i]) {
				chordLength[i] = chordTrail[i] - chordLead[i];
			}
		}
		
		/* Calculate fin properties:
		 * 
		 * macLength // MAC length
		 * macLead   // MAC leading edge position
		 * macSpan   // MAC spanwise position
		 * ar        // Fin aspect ratio (already set)
		 * span      // Fin span (already set)
		 */
		macLength = 0;
		macLead = 0;
		macSpan = 0;
		cosGamma = 0;
		cosGammaLead = 0;
		rollSum = 0;
		double area = 0;
		double radius = component.getBodyRadius();
		
		final double dy = span / (DIVISIONS - 1);
		for (int i = 0; i < DIVISIONS; i++) {
			double length = chordTrail[i] - chordLead[i];
			double y = i * dy;
			
			macLength += length * length;
			logger.debug("macLength = {}, length = {}, i = {}", macLength, length, i);
			macSpan += y * length;
			macLead += chordLead[i] * length;
			area += length;
			rollSum += chordLength[i] * pow2(radius + y);
			
			if (i > 0) {
				double dx = (chordTrail[i] + chordLead[i]) / 2 - (chordTrail[i - 1] + chordLead[i - 1]) / 2;
				cosGamma += dy / MathUtil.hypot(dx, dy);
				
				dx = chordLead[i] - chordLead[i - 1];
				cosGammaLead += dy / MathUtil.hypot(dx, dy);
			}
		}
		
		macLength *= dy;
		logger.debug("macLength = {}", macLength);
		macSpan *= dy;
		macLead *= dy;
		area *= dy;
		rollSum *= dy;
		
		macLength /= area;
		macSpan /= area;
		macLead /= area;
		cosGamma /= (DIVISIONS - 1);
		cosGammaLead /= (DIVISIONS - 1);
	}
	
	///////////////  CNa1 calculation  ////////////////
	
	private static final double CNA_SUBSONIC = 0.9;
	private static final double CNA_SUPERSONIC = 1.5;
	private static final double CNA_SUPERSONIC_B = pow(pow2(CNA_SUPERSONIC) - 1, 1.5);
	private static final double GAMMA = 1.4;
	private static final LinearInterpolator K1, K2, K3;
	private static final PolyInterpolator cnaInterpolator = new PolyInterpolator(
			new double[] { CNA_SUBSONIC, CNA_SUPERSONIC },
			new double[] { CNA_SUBSONIC, CNA_SUPERSONIC },
			new double[] { CNA_SUBSONIC }
			);
	/* Pre-calculate the values for K1, K2 and K3 */
	static {
		// Up to Mach 5
		int n = (int) ((5.0 - CNA_SUPERSONIC) * 10);
		double[] x = new double[n];
		double[] k1 = new double[n];
		double[] k2 = new double[n];
		double[] k3 = new double[n];
		for (int i = 0; i < n; i++) {
			double M = CNA_SUPERSONIC + i * 0.1;
			double beta = MathUtil.safeSqrt(M * M - 1);
			x[i] = M;
			k1[i] = 2.0 / beta;
			k2[i] = ((GAMMA + 1) * pow(M, 4) - 4 * pow2(beta)) / (4 * pow(beta, 4));
			k3[i] = ((GAMMA + 1) * pow(M, 8) + (2 * pow2(GAMMA) - 7 * GAMMA - 5) * pow(M, 6) +
					10 * (GAMMA + 1) * pow(M, 4) + 8) / (6 * pow(beta, 7));
		}
		K1 = new LinearInterpolator(x, k1);
		K2 = new LinearInterpolator(x, k2);
		K3 = new LinearInterpolator(x, k3);
		
		//		System.out.println("K1[m="+CNA_SUPERSONIC+"] = "+k1[0]);
		//		System.out.println("K2[m="+CNA_SUPERSONIC+"] = "+k2[0]);
		//		System.out.println("K3[m="+CNA_SUPERSONIC+"] = "+k3[0]);
	}
	
	protected double calculateFinCNa1(FlightConditions conditions) {
		double mach = conditions.getMach();
		double ref = conditions.getRefArea();
		double alpha = MathUtil.min(conditions.getAOA(),
				Math.PI - conditions.getAOA(), STALL_ANGLE);
		
		// Subsonic case
		if (mach <= CNA_SUBSONIC) {
			return 2 * Math.PI * pow2(span) / (1 + MathUtil.safeSqrt(1 + (1 - pow2(mach)) *
					pow2(pow2(span) / (finArea * cosGamma)))) / ref;
		}
		
		// Supersonic case
		if (mach >= CNA_SUPERSONIC) {
			return finArea * (K1.getValue(mach) + K2.getValue(mach) * alpha +
					K3.getValue(mach) * pow2(alpha)) / ref;
		}
		
		// Transonic case, interpolate
		double subV, superV;
		double subD, superD;
		
		double sq = MathUtil.safeSqrt(1 + (1 - pow2(CNA_SUBSONIC)) * pow2(span * span / (finArea * cosGamma)));
		subV = 2 * Math.PI * pow2(span) / ref / (1 + sq);
		subD = 2 * mach * Math.PI * pow(span, 6) / (pow2(finArea * cosGamma) * ref *
				sq * pow2(1 + sq));
		
		superV = finArea * (K1.getValue(CNA_SUPERSONIC) + K2.getValue(CNA_SUPERSONIC) * alpha +
				K3.getValue(CNA_SUPERSONIC) * pow2(alpha)) / ref;
		superD = -finArea / ref * 2 * CNA_SUPERSONIC / CNA_SUPERSONIC_B;
		
		//		System.out.println("subV="+subV+" superV="+superV+" subD="+subD+" superD="+superD);
		
		return cnaInterpolator.interpolate(mach, subV, superV, subD, superD, 0);
	}
	
	private double calculateDampingMoment(FlightConditions conditions) {
		double rollRate = conditions.getRollRate();
		
		if (Math.abs(rollRate) < 0.1)
			return 0;
		
		double mach = conditions.getMach();
		double absRate = Math.abs(rollRate);
		
		/*
		 * At low speeds and relatively large roll rates (i.e. near apogee) the
		 * fin tips rotate well above stall angle.  In this case sum the chords
		 * separately.
		 */
		if (absRate * (bodyRadius + span) / conditions.getVelocity() > 15 * Math.PI / 180) {
			double sum = 0;
			for (int i = 0; i < DIVISIONS; i++) {
				double dist = bodyRadius + span * i / DIVISIONS;
				double aoa = Math.min(absRate * dist / conditions.getVelocity(), 15 * Math.PI / 180);
				sum += chordLength[i] * dist * aoa;
			}
			sum = sum * (span / DIVISIONS) * 2 * Math.PI / conditions.getBeta() /
					(conditions.getRefArea() * conditions.getRefLength());
			
			//			System.out.println("SPECIAL: " + 
			//					(MathUtil.sign(rollRate) *component.getFinCount() * sum));
			return MathUtil.sign(rollRate) * finCount * sum;
		}
		
		if (mach <= CNA_SUBSONIC) {
			//			System.out.println("BASIC:   "+
			//					(component.getFinCount() * 2*Math.PI * rollRate * rollSum / 
			//			(conditions.getRefArea() * conditions.getRefLength() * 
			//					conditions.getVelocity() * conditions.getBeta())));
			
			return finCount * 2 * Math.PI * rollRate * rollSum /
					(conditions.getRefArea() * conditions.getRefLength() *
							conditions.getVelocity() * conditions.getBeta());
		}
		if (mach >= CNA_SUPERSONIC) {
			
			double vel = conditions.getVelocity();
			double k1 = K1.getValue(mach);
			double k2 = K2.getValue(mach);
			double k3 = K3.getValue(mach);
			
			double sum = 0;
			
			for (int i = 0; i < DIVISIONS; i++) {
				double y = i * span / (DIVISIONS - 1);
				double angle = rollRate * (bodyRadius + y) / vel;
				
				sum += (k1 * angle + k2 * angle * angle + k3 * angle * angle * angle)
						* chordLength[i] * (bodyRadius + y);
			}
			
			return finCount * sum * span / (DIVISIONS - 1) /
					(conditions.getRefArea() * conditions.getRefLength());
		}
		
		// Transonic, do linear interpolation
		
		FlightConditions cond = conditions.clone();
		cond.setMach(CNA_SUBSONIC - 0.01);
		double subsonic = calculateDampingMoment(cond);
		cond.setMach(CNA_SUPERSONIC + 0.01);
		double supersonic = calculateDampingMoment(cond);
		
		return subsonic * (CNA_SUPERSONIC - mach) / (CNA_SUPERSONIC - CNA_SUBSONIC) +
				supersonic * (mach - CNA_SUBSONIC) / (CNA_SUPERSONIC - CNA_SUBSONIC);
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
		//		logger.debug("m = {} ", m);
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
		//		logger.debug("val = {}", val);
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
	
	
	//	@SuppressWarnings("null")
	//	public static void main(String arg[]) {
	//		Rocket rocket = TestRocket.makeRocket();
	//		FinSet finset = null;
	//		
	//		Iterator<RocketComponent> iter = rocket.deepIterator();
	//		while (iter.hasNext()) {
	//			RocketComponent c = iter.next();
	//			if (c instanceof FinSet) {
	//				finset = (FinSet)c;
	//				break;
	//			}
	//		}
	//		
	//		((TrapezoidFinSet)finset).setHeight(0.10);
	//		((TrapezoidFinSet)finset).setRootChord(0.10);
	//		((TrapezoidFinSet)finset).setTipChord(0.10);
	//		((TrapezoidFinSet)finset).setSweep(0.0);
	//
	//		
	//		FinSetCalc calc = new FinSetCalc(finset);
	//		
	//		calc.calculateFinGeometry();
	//		FlightConditions cond = new FlightConditions(new Configuration(rocket));
	//		for (double m=0; m < 3; m+=0.05) {
	//			cond.setMach(m);
	//			cond.setAOA(0.0*Math.PI/180);
	//			double cna = calc.calculateFinCNa1(cond);
	//			System.out.printf("%5.2f "+cna+"\n", m);
	//		}
	//		
	//	}
	
	@Override
	public double calculatePressureDragForce(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {
		
		double mach = conditions.getMach();
		double drag = 0;
		
		// Pressure fore-drag
		if (crossSection == FinSet.CrossSection.AIRFOIL ||
				crossSection == FinSet.CrossSection.ROUNDED) {
			
			// Round leading edge
			if (mach < 0.9) {
				drag = Math.pow(1 - pow2(mach), -0.417) - 1;
			} else if (mach < 1) {
				drag = 1 - 1.785 * (mach - 0.9);
			} else {
				drag = 1.214 - 0.502 / pow2(mach) + 0.1095 / pow2(pow2(mach));
			}
			
		} else if (crossSection == FinSet.CrossSection.SQUARE) {
			drag = stagnationCD;
		} else {
			throw new UnsupportedOperationException("Unsupported fin profile: " + crossSection);
		}
		
		// Slanted leading edge
		drag *= pow2(cosGammaLead);
		
		// Trailing edge drag
		if (crossSection == FinSet.CrossSection.SQUARE) {
			drag += baseCD;
		} else if (crossSection == FinSet.CrossSection.ROUNDED) {
			drag += baseCD / 2;
		}
		// Airfoil assumed to have zero base drag
		
		// Scale to correct reference area
		drag *= finCount * span * thickness / conditions.getRefArea();
		
		return drag;
	}
	
	private void calculateInterferenceFinCount(FinSet component) {
		RocketComponent parent = component.getParent();
		if (parent == null) {
			throw new IllegalStateException("fin set without parent component");
		}
		
		double lead = component.toRelative(Coordinate.NUL, parent)[0].x;
		double trail = component.toRelative(new Coordinate(component.getLength()),
				parent)[0].x;
		
		/*
		 * The counting fails if the fin root chord is very small, in that case assume
		 * no other fin interference than this fin set.
		 */
		if (trail - lead < 0.007) {
			interferenceFinCount = finCount;
		} else {
			interferenceFinCount = 0;
			for (RocketComponent c : parent.getChildren()) {
				if (c instanceof FinSet) {
					double finLead = c.toRelative(Coordinate.NUL, parent)[0].x;
					double finTrail = c.toRelative(new Coordinate(c.getLength()), parent)[0].x;
					
					// Compute overlap of the fins
					
					if ((finLead < trail - 0.005) && (finTrail > lead + 0.005)) {
						interferenceFinCount += ((FinSet) c).getFinCount();
					}
				}
			}
		}
		if (interferenceFinCount < component.getFinCount()) {
			throw new BugException("Counted " + interferenceFinCount + " parallel fins, " +
					"when component itself has " + component.getFinCount() +
					", fin points=" + Arrays.toString(component.getFinPoints()));
		}
	}
	
}
