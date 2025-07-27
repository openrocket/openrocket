package info.openrocket.core.database.motor;

import java.util.List;

import info.openrocket.core.motor.Motor;

public interface MotorDatabase {

	/**
	 * Return all motors in the database matching a search criteria. Any search
	 * criteria that
	 * is null or NaN is ignored.
	 * 
	 * @param type         the motor type, or null.
	 * @param manufacturer the manufacturer, or null.
	 * @param designation  the designation, or null.
	 * @param diameter     the diameter, or NaN.
	 * @param length       the length, or NaN.
	 * @return a list of all the matching motors.
	 */
	public List<? extends Motor> findMotors(String digest, Motor.Type type,
			String manufacturer, String designation, double diameter,
			double length);
}
