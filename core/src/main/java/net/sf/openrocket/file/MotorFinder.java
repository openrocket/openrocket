package net.sf.openrocket.file;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.Motor;

/**
 * Interface for finding the appropriate motor for one defined in an ORK file.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface MotorFinder {
	
	/**
	 * Return the motor to inject to the rocket design when loading a file.
	 * This may also return dysfunctional motor placeholders that will later be
	 * replaced with the appropriate motor.
	 * <p>
	 * Any parameter that is null/NaN should be ignored when finding the motors.
	 * The method can also add appropriate warnings to the provided warning set
	 * in case there are problems selecting the motor.
	 * 
	 * @return	the motor to use in the design, or <code>null</code> for no motor.
	 */
	public Motor findMotor(Motor.Type type, String manufacturer, String designation, double diameter,
			double length, String digest, WarningSet warnings);
	
	
}
