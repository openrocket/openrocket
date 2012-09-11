package net.sf.openrocket.aerodynamics.barrowman;

import static net.sf.openrocket.models.atmosphere.AtmosphericConditions.GAMMA;
import static net.sf.openrocket.util.MathUtil.pow2;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LinearInterpolator;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PolyInterpolator;



/**
 * Calculates the aerodynamic properties of a <code>SymmetricComponent</code>.
 * <p>
 * CP and CNa are calculated by the Barrowman method extended to account for body lift
 * by the method presented by Galejs.  Supersonic CNa and CP are assumed to be the
 * same as the subsonic values.
 * 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SymmetricComponentCalc extends RocketComponentCalc {
	
	public static final double BODY_LIFT_K = 1.1;
	
	private final double length;
	private final double foreRadius, aftRadius;
	private final double fineness;
	private final Transition.Shape shape;
	private final double param;
	private final double frontalArea;
	private final double fullVolume;
	private final double planformArea, planformCenter;
	private final double sinphi;
	
	public SymmetricComponentCalc(RocketComponent c) {
		super(c);
		if (!(c instanceof SymmetricComponent)) {
			throw new IllegalArgumentException("Illegal component type " + c);
		}
		SymmetricComponent component = (SymmetricComponent) c;
		

		length = component.getLength();
		foreRadius = component.getForeRadius();
		aftRadius = component.getAftRadius();
		
		fineness = length / (2 * Math.abs(aftRadius - foreRadius));
		fullVolume = component.getFullVolume();
		planformArea = component.getComponentPlanformArea();
		planformCenter = component.getComponentPlanformCenter();
		
		if (component instanceof BodyTube) {
			shape = null;
			param = 0;
			frontalArea = 0;
			sinphi = 0;
		} else if (component instanceof Transition) {
			shape = ((Transition) component).getType();
			param = ((Transition) component).getShapeParameter();
			frontalArea = Math.abs(Math.PI * (foreRadius * foreRadius - aftRadius * aftRadius));
			
			double r = component.getRadius(0.99 * length);
			sinphi = (aftRadius - r) / MathUtil.hypot(aftRadius - r, 0.01 * length);
		} else {
			throw new UnsupportedOperationException("Unknown component type " +
					component.getComponentName());
		}
	}
	
	
	private boolean isTube = false;
	private double cnaCache = Double.NaN;
	private double cpCache = Double.NaN;
	
	
	/**
	 * Calculates the non-axial forces produced by the fins (normal and side forces,
	 * pitch, yaw and roll moments, CP position, CNa).
	 * <p> 
	 * This method uses the Barrowman method for CP and CNa calculation and the 
	 * extension presented by Galejs for the effect of body lift.
	 * <p>
	 * The CP and CNa at supersonic speeds are assumed to be the same as those at
	 * subsonic speeds.
	 */
	@Override
	public void calculateNonaxialForces(FlightConditions conditions,
			AerodynamicForces forces, WarningSet warnings) {
		
		// Pre-calculate and store the results
		if (Double.isNaN(cnaCache)) {
			final double r0 = foreRadius;
			final double r1 = aftRadius;
			
			if (MathUtil.equals(r0, r1)) {
				isTube = true;
				cnaCache = 0;
			} else {
				isTube = false;
				
				final double A0 = Math.PI * pow2(r0);
				final double A1 = Math.PI * pow2(r1);
				
				cnaCache = 2 * (A1 - A0);
				//				System.out.println("cnaCache = " + cnaCache);
				cpCache = (length * A1 - fullVolume) / (A1 - A0);
			}
		}
		
		Coordinate cp;
		
		// If fore == aft, only body lift is encountered
		if (isTube) {
			cp = getLiftCP(conditions, warnings);
		} else {
			cp = new Coordinate(cpCache, 0, 0, cnaCache * conditions.getSincAOA() /
					conditions.getRefArea()).average(getLiftCP(conditions, warnings));
		}
		
		forces.setCP(cp);
		forces.setCNa(cp.weight);
		forces.setCN(forces.getCNa() * conditions.getAOA());
		forces.setCm(forces.getCN() * cp.x / conditions.getRefLength());
		forces.setCroll(0);
		forces.setCrollDamp(0);
		forces.setCrollForce(0);
		forces.setCside(0);
		forces.setCyaw(0);
		

		// Add warning on supersonic flight
		if (conditions.getMach() > 1.1) {
			warnings.add(Warning.SUPERSONIC);
		}
		
	}
	
	

	/**
	 * Calculate the body lift effect according to Galejs.
	 */
	protected Coordinate getLiftCP(FlightConditions conditions, WarningSet warnings) {
		
		/*
		 * Without this extra multiplier the rocket may become unstable at apogee
		 * when turning around, and begin oscillating horizontally.  During the flight
		 * of the rocket this has no effect.  It is effective only when AOA > 45 deg
		 * and the velocity is less than 15 m/s.
		 * 
		 * TODO: MEDIUM:  This causes an anomaly to the flight results with the CP jumping at apogee
		 */
		double mul = 1;
		if ((conditions.getMach() < 0.05) && (conditions.getAOA() > Math.PI / 4)) {
			mul = pow2(conditions.getMach() / 0.05);
		}
		
		return new Coordinate(planformCenter, 0, 0, mul * BODY_LIFT_K * planformArea / conditions.getRefArea() *
				conditions.getSinAOA() * conditions.getSincAOA()); // sin(aoa)^2 / aoa
	}
	
	

	private LinearInterpolator interpolator = null;
	
	@Override
	public double calculatePressureDragForce(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {
		
		// Check for simple cases first
		if (MathUtil.equals(foreRadius, aftRadius))
			return 0;
		
		if (length < 0.001) {
			if (foreRadius < aftRadius) {
				return stagnationCD * frontalArea / conditions.getRefArea();
			} else {
				return baseCD * frontalArea / conditions.getRefArea();
			}
		}
		

		// Boattail drag computed directly from base drag
		if (aftRadius < foreRadius) {
			if (fineness >= 3)
				return 0;
			double cd = baseCD * frontalArea / conditions.getRefArea();
			if (fineness <= 1)
				return cd;
			return cd * (3 - fineness) / 2;
		}
		

		// All nose cones and shoulders from pre-calculated and interpolating 
		if (interpolator == null) {
			calculateNoseInterpolator();
		}
		
		return interpolator.getValue(conditions.getMach()) * frontalArea / conditions.getRefArea();
	}
	
	

	/* 
	 * Experimental values of pressure drag for different nose cone shapes with a fineness
	 * ratio of 3.  The data is taken from 'Collection of Zero-Lift Drag Data on Bodies
	 * of Revolution from Free-Flight Investigations', NASA TR-R-100, NTRS 19630004995,
	 * page 16.
	 * 
	 * This data is extrapolated for other fineness ratios.
	 */

	private static final LinearInterpolator ellipsoidInterpolator = new LinearInterpolator(
			new double[] { 1.2, 1.25, 1.3, 1.4, 1.6, 2.0, 2.4 },
			new double[] { 0.110, 0.128, 0.140, 0.148, 0.152, 0.159, 0.162 /* constant */}
			);
	private static final LinearInterpolator x14Interpolator = new LinearInterpolator(
			new double[] { 1.2, 1.3, 1.4, 1.6, 1.8, 2.2, 2.6, 3.0, 3.6 },
			new double[] { 0.140, 0.156, 0.169, 0.192, 0.206, 0.227, 0.241, 0.249, 0.252 }
			);
	private static final LinearInterpolator x12Interpolator = new LinearInterpolator(
			new double[] { 0.925, 0.95, 1.0, 1.05, 1.1, 1.2, 1.3, 1.7, 2.0 },
			new double[] { 0, 0.014, 0.050, 0.060, 0.059, 0.081, 0.084, 0.085, 0.078 }
			);
	private static final LinearInterpolator x34Interpolator = new LinearInterpolator(
			new double[] { 0.8, 0.9, 1.0, 1.06, 1.2, 1.4, 1.6, 2.0, 2.8, 3.4 },
			new double[] { 0, 0.015, 0.078, 0.121, 0.110, 0.098, 0.090, 0.084, 0.078, 0.074 }
			);
	private static final LinearInterpolator vonKarmanInterpolator = new LinearInterpolator(
			new double[] { 0.9, 0.95, 1.0, 1.05, 1.1, 1.2, 1.4, 1.6, 2.0, 3.0 },
			new double[] { 0, 0.010, 0.027, 0.055, 0.070, 0.081, 0.095, 0.097, 0.091, 0.083 }
			);
	private static final LinearInterpolator lvHaackInterpolator = new LinearInterpolator(
			new double[] { 0.9, 0.95, 1.0, 1.05, 1.1, 1.2, 1.4, 1.6, 2.0 },
			new double[] { 0, 0.010, 0.024, 0.066, 0.084, 0.100, 0.114, 0.117, 0.113 }
			);
	private static final LinearInterpolator parabolicInterpolator = new LinearInterpolator(
			new double[] { 0.95, 0.975, 1.0, 1.05, 1.1, 1.2, 1.4, 1.7 },
			new double[] { 0, 0.016, 0.041, 0.092, 0.109, 0.119, 0.113, 0.108 }
			);
	private static final LinearInterpolator parabolic12Interpolator = new LinearInterpolator(
			new double[] { 0.8, 0.9, 0.95, 1.0, 1.05, 1.1, 1.3, 1.5, 1.8 },
			new double[] { 0, 0.016, 0.042, 0.100, 0.126, 0.125, 0.100, 0.090, 0.088 }
			);
	private static final LinearInterpolator parabolic34Interpolator = new LinearInterpolator(
			new double[] { 0.9, 0.95, 1.0, 1.05, 1.1, 1.2, 1.4, 1.7 },
			new double[] { 0, 0.023, 0.073, 0.098, 0.107, 0.106, 0.089, 0.082 }
			);
	private static final LinearInterpolator bluntInterpolator = new LinearInterpolator();
	static {
		for (double m = 0; m < 3; m += 0.05)
			bluntInterpolator.addPoint(m, BarrowmanCalculator.calculateStagnationCD(m));
	}
	
	/**
	 * Calculate the LinearInterpolator 'interpolator'.  After this call, if can be used
	 * to get the pressure drag coefficient at any Mach number.
	 * 
	 * First, the transonic/supersonic region is computed.  For conical and ogive shapes
	 * this is calculated directly.  For other shapes, the values for fineness-ratio 3
	 * transitions are taken from the experimental values stored above (for parameterized
	 * shapes the values are interpolated between the parameter values).  These are then
	 * extrapolated to the current fineness ratio.
	 * 
	 * Finally, if the first data points in the interpolator are not zero, the subsonic
	 * region is interpolated in the form   Cd = a*M^b + Cd(M=0).
	 */
	@SuppressWarnings("null")
	private void calculateNoseInterpolator() {
		LinearInterpolator int1 = null, int2 = null;
		double p = 0;
		
		interpolator = new LinearInterpolator();
		

		/*
		 * Take into account nose cone shape.  Conical and ogive generate the interpolator
		 * directly.  Others store a interpolator for fineness ratio 3 into int1, or
		 * for parameterized shapes store the bounding fineness ratio 3 interpolators into
		 * int1 and int2 and set 0 <= p <= 1 according to the bounds. 
		 */
		switch (shape) {
		case CONICAL:
			interpolator = calculateOgiveNoseInterpolator(0, sinphi); // param==0 -> conical
			break;
		
		case OGIVE:
			interpolator = calculateOgiveNoseInterpolator(param, sinphi);
			break;
		
		case ELLIPSOID:
			int1 = ellipsoidInterpolator;
			break;
		
		case POWER:
			if (param <= 0.25) {
				int1 = bluntInterpolator;
				int2 = x14Interpolator;
				p = param * 4;
			} else if (param <= 0.5) {
				int1 = x14Interpolator;
				int2 = x12Interpolator;
				p = (param - 0.25) * 4;
			} else if (param <= 0.75) {
				int1 = x12Interpolator;
				int2 = x34Interpolator;
				p = (param - 0.5) * 4;
			} else {
				int1 = x34Interpolator;
				int2 = calculateOgiveNoseInterpolator(0, 1 / MathUtil.safeSqrt(1 + 4 * pow2(fineness)));
				p = (param - 0.75) * 4;
			}
			break;
		
		case PARABOLIC:
			if (param <= 0.5) {
				int1 = calculateOgiveNoseInterpolator(0, 1 / MathUtil.safeSqrt(1 + 4 * pow2(fineness)));
				int2 = parabolic12Interpolator;
				p = param * 2;
			} else if (param <= 0.75) {
				int1 = parabolic12Interpolator;
				int2 = parabolic34Interpolator;
				p = (param - 0.5) * 4;
			} else {
				int1 = parabolic34Interpolator;
				int2 = parabolicInterpolator;
				p = (param - 0.75) * 4;
			}
			break;
		
		case HAACK:
			int1 = vonKarmanInterpolator;
			int2 = lvHaackInterpolator;
			p = param * 3;
			break;
		
		default:
			throw new UnsupportedOperationException("Unknown transition shape: " + shape);
		}
		
		if (p < 0 || p > 1.00001) {
			throw new BugException("Inconsistent parameter value p=" + p + " shape=" + shape);
		}
		

		// Check for parameterized shape and interpolate if necessary
		if (int2 != null) {
			LinearInterpolator int3 = new LinearInterpolator();
			for (double m : int1.getXPoints()) {
				int3.addPoint(m, p * int2.getValue(m) + (1 - p) * int1.getValue(m));
			}
			for (double m : int2.getXPoints()) {
				int3.addPoint(m, p * int2.getValue(m) + (1 - p) * int1.getValue(m));
			}
			int1 = int3;
		}
		
		// Extrapolate for fineness ratio if necessary
		if (int1 != null) {
			double log4 = Math.log(fineness + 1) / Math.log(4);
			for (double m : int1.getXPoints()) {
				double stag = bluntInterpolator.getValue(m);
				interpolator.addPoint(m, stag * Math.pow(int1.getValue(m) / stag, log4));
			}
		}
		

		/*
		 * Now the transonic/supersonic region is ok.  We still need to interpolate
		 * the subsonic region, if the values are non-zero.
		 */

		double min = interpolator.getXPoints()[0];
		double minValue = interpolator.getValue(min);
		if (minValue < 0.001) {
			// No interpolation necessary
			return;
		}
		
		double cdMach0 = 0.8 * pow2(sinphi);
		double minDeriv = (interpolator.getValue(min + 0.01) - minValue) / 0.01;
		
		// These should not occur, but might cause havoc for the interpolation
		if ((cdMach0 >= minValue - 0.01) || (minDeriv <= 0.01)) {
			return;
		}
		
		// Cd = a*M^b + cdMach0
		double a = minValue - cdMach0;
		double b = minDeriv / a;
		
		for (double m = 0; m < minValue; m += 0.05) {
			interpolator.addPoint(m, a * Math.pow(m, b) + cdMach0);
		}
	}
	
	
	private static final PolyInterpolator conicalPolyInterpolator =
			new PolyInterpolator(new double[] { 1.0, 1.3 }, new double[] { 1.0, 1.3 });
	
	private static LinearInterpolator calculateOgiveNoseInterpolator(double param,
			double sinphi) {
		LinearInterpolator interpolator = new LinearInterpolator();
		
		// In the range M = 1 ... 1.3 use polynomial approximation
		double cdMach1 = 2.1 * pow2(sinphi) + 0.6019 * sinphi;
		
		double[] poly = conicalPolyInterpolator.interpolator(
				1.0 * sinphi, cdMach1,
				4 / (GAMMA + 1) * (1 - 0.5 * cdMach1), -1.1341 * sinphi
				);
		
		// Shape parameter multiplier
		double mul = 0.72 * pow2(param - 0.5) + 0.82;
		
		for (double m = 1; m < 1.3001; m += 0.02) {
			interpolator.addPoint(m, mul * PolyInterpolator.eval(m, poly));
		}
		
		// Above M = 1.3 use direct formula
		for (double m = 1.32; m < 4; m += 0.02) {
			interpolator.addPoint(m, mul * (2.1 * pow2(sinphi) + 0.5 * sinphi / MathUtil.safeSqrt(m * m - 1)));
		}
		
		return interpolator;
	}
	


}
