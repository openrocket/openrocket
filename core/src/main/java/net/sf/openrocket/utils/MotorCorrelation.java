package net.sf.openrocket.utils;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MathUtil;

public class MotorCorrelation {
	
	/**
	 * Return a measure of motor similarity.  The measure is a value between 0.0 and 1.0.
	 * The larger the value, the more similar the motor thrust curves are, for value 1.0 they
	 * are identical.
	 * <p>
	 * This method takes into account the thrust curve shape, average thrust, burn time and
	 * total impulse of the motor.  The similarity is the minimum of all of these.
	 * 
	 * @param motor1	the first motor
	 * @param motor2	the second motor
	 * @return			the similarity of the two motors
	 */
	public static double similarity(Motor motor1, Motor motor2) {
		double d;
		
		d = crossCorrelation(motor1, motor2);
		d = Math.min(d, diff(motor1.getAverageThrustEstimate(), motor2.getAverageThrustEstimate()));
		d = Math.min(d, 2 * diff(motor1.getBurnTimeEstimate(), motor2.getBurnTimeEstimate()));
		d = Math.min(d, diff(motor1.getTotalImpulseEstimate(), motor2.getTotalImpulseEstimate()));
		
		return d;
	}
	
	
	private static double diff(double a, double b) {
		double min = Math.min(a, b);
		double max = Math.max(a, b);
		
		if (MathUtil.equals(max, 0))
			return 1.0;
		return min / max;
	}
	
	
	/**
	 * Compute the cross-correlation of the thrust curves of the two motors.  The result is
	 * a double between 0 and 1 (inclusive).  The closer the return value is to one the more
	 * similar the thrust curves are.
	 * 
	 * @param motor1	the first motor.
	 * @param motor2	the second motor.
	 * @return			the scaled cross-correlation of the two thrust curves.
	 */
	public static double crossCorrelation(Motor motor1, Motor motor2) {		
		double t;
		double auto1 = 0;
		double auto2 = 0;
		double cross = 0;
		for (t = 0; t < 1000; t += 0.01) {
			
			double thrust1 = motor1.getThrust( t); 
			double thrust2 = motor2.getThrust( t);
			
			if ( thrust1 < 0 || thrust2 < 0) {
				throw new BugException("Negative thrust, t1=" + thrust1 + " t2=" + thrust2);
			}
			
			auto1 += thrust1 * thrust1;
			auto2 += thrust2 * thrust2;
			cross += thrust1 * thrust2;
		}
		
		double auto = Math.max(auto1, auto2);
		
		if (MathUtil.equals(auto, 0)) {
			return 1.0;
		}
		
		return cross / auto;
	}
	
}