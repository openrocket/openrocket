package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorInstance;
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
		MotorInstance m1 = motor1.getInstance();
		MotorInstance m2 = motor2.getInstance();
		
		AtmosphericConditions cond = new AtmosphericConditions();
		
		double t;
		double auto1 = 0;
		double auto2 = 0;
		double cross = 0;
		for (t = 0; t < 1000; t += 0.01) {
			m1.step(t, 0, cond);
			m2.step(t, 0, cond);
			
			double t1 = m1.getThrust();
			double t2 = m2.getThrust();
			
			if (t1 < 0 || t2 < 0) {
				throw new BugException("Negative thrust, t1=" + t1 + " t2=" + t2);
			}
			
			auto1 += t1 * t1;
			auto2 += t2 * t2;
			cross += t1 * t2;
		}
		
		double auto = Math.max(auto1, auto2);
		
		if (MathUtil.equals(auto, 0)) {
			return 1.0;
		}
		
		return cross / auto;
	}
	
	
}