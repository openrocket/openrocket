package net.sf.openrocket.file;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.Motor.Type;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;

public class DatabaseMotorFinderWithMissingMotors extends DatabaseMotorFinder
implements MotorFinder {

	/**
	 * This implementation returns a ThrustCurveMotorPlaceholder.
	 */
	@Override
	protected Motor handleMissingMotor(Type type, String manufacturer, String designation, double diameter, double length, String digest, WarningSet warnings) {
		Motor motor = new ThrustCurveMotorPlaceholder(type,
				manufacturer,
				designation,
				diameter,
				length,
				digest,
				/* delay */ Double.NaN,
				/*launchMass*/ Double.NaN,
				/*emptyMass*/ Double.NaN);
		return motor;
	}
}
