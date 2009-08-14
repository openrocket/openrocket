package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * A class of motors specified by a fixed thrust curve.  This is the most
 * accurate for solid rocket motors.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ThrustCurveMotor extends Motor {
	
	public static final double MAX_THRUST = 10e6;

	private final double[] time;
	private final double[] thrust;
	private final Coordinate[] cg;
	
	private final double totalTime;
	private final double maxThrust;
	

	/**
	 * Sole constructor.  Sets all the properties of the motor.
	 * 
	 * @param manufacturer  the manufacturer of the motor.
	 * @param designation   the designation of the motor.
	 * @param description   extra description of the motor.
	 * @param diameter      diameter of the motor.
	 * @param length        length of the motor.
	 * @param time          the time points for the thrust curve.
	 * @param thrust        thrust at the time points.
	 * @param cg            cg at the time points.
	 */
	public ThrustCurveMotor(String manufacturer, String designation, String description, 
			Motor.Type type, double[] delays, double diameter, double length,
			double[] time, double[] thrust, Coordinate[] cg) {
		super(manufacturer, designation, description, type, delays, diameter, length);

		double max = -1;

		// Check argument validity
		if ((time.length != thrust.length) || (time.length != cg.length)) {
			throw new IllegalArgumentException("Array lengths do not match, " +
					"time:" + time.length + " thrust:" + thrust.length +
					" cg:" + cg.length);
		}
		if (time.length < 2) {
			throw new IllegalArgumentException("Too short thrust-curve, length=" + 
					time.length);
		}
		for (int i=0; i < time.length-1; i++) {
			if (time[i+1] < time[i]) {
				throw new IllegalArgumentException("Time goes backwards, " +
						"time[" + i + "]=" + time[i] + " " +
						"time[" + (i+1) + "]=" + time[i+1]);
			}
		}
		if (!MathUtil.equals(time[0], 0)) {
			throw new IllegalArgumentException("Curve starts at time " + time[0]);
		}
		if (!MathUtil.equals(thrust[0], 0)) {
			throw new IllegalArgumentException("Curve starts at thrust " + thrust[0]);
		}
		if (!MathUtil.equals(thrust[thrust.length-1], 0)) {
			throw new IllegalArgumentException("Curve ends at thrust " + 
					thrust[thrust.length-1]);
		}
		for (double t: thrust) {
			if (t < 0) {
				throw new IllegalArgumentException("Negative thrust.");
			}
			if (t > MAX_THRUST || Double.isNaN(t)) {
				throw new IllegalArgumentException("Invalid thrust " + t);
			}
			if (t > max)
				max = t;
		}
		for (Coordinate c: cg) {
			if (c.isNaN()) {
				throw new IllegalArgumentException("Invalid CG " + c);
			}
			if (c.x < 0 || c.x > length) {
				throw new IllegalArgumentException("Invalid CG position " + c.x);
			}
			if (c.weight < 0) {
				throw new IllegalArgumentException("Negative mass " + c.weight);
			}
		}

		this.maxThrust = max;
		this.time = time.clone();
		this.thrust = thrust.clone();
		this.cg = cg.clone();
		this.totalTime = time[time.length-1];
	}


	@Override
	public double getTotalTime() {
		return totalTime;
	}

	@Override
	public double getMaxThrust() {
		return maxThrust;
	}
	
	@Override
	public double getThrust(double t) {
		if ((t < 0) || (t > totalTime))
			return 0;

		for (int i=0; i < time.length-1; i++) {
			if ((t >= time[i]) && (t <= time[i+1])) {
				double delta = time[i+1] - time[i];
				if (delta < 0.0001) {
					return thrust[i];
				}
				t = t - time[i];
				return thrust[i] * (1 - t/delta) + thrust[i+1] * (t/delta);
			}
		}
		assert false : "Should not be reached.";
		return 0;
	}


	@Override
	public Coordinate getCG(double t) {
		if (t <= 0)
			return cg[0];
		if (t >= totalTime)
			return cg[cg.length-1];
		
		for (int i=0; i < time.length-1; i++) {
			if ((t >= time[i]) && (t <= time[i+1])) {
				double delta = time[i+1] - time[i];
				t = t - time[i];
				return cg[i].multiply(1 - t/delta).add(cg[i+1].multiply(t/delta));
			}
		}
		assert false : "Should not be reached.";
		return cg[cg.length-1];
	}

	
	public double[] getTimePoints() {
		return time.clone();
	}
	
	public double[] getThrustPoints() {
		return thrust.clone();
	}
	
	public Coordinate[] getCGPoints() {
		return cg.clone();
	}
	
}
