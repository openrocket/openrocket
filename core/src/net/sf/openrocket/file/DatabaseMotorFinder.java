package net.sf.openrocket.file;

import java.util.List;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.Motor.Type;
import net.sf.openrocket.startup.Application;

/**
 * A MotorFinder implementation that searches the thrust curve motor database
 * for a motor.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DatabaseMotorFinder implements MotorFinder {
	
	/**
	 * Do something when a missing motor is found.
	 * 
	 * This implementation adds a Warning.MissingMotor to the warning set and returns null.
	 * 
	 * Override this function to change the behavior.
	 * 
	 * @return The Motor which will be put in the Rocket.
	 */
	protected Motor handleMissingMotor(Type type, String manufacturer, String designation, double diameter, double length, String digest, WarningSet warnings) {
		Warning.MissingMotor mmw = new Warning.MissingMotor();
		mmw.setDesignation(designation);
		mmw.setDigest(digest);
		mmw.setDiameter(diameter);
		mmw.setLength(length);
		mmw.setManufacturer(manufacturer);
		mmw.setType(type);
		warnings.add(mmw);
		return null;
	}
	
	@Override
	public Motor findMotor(Type type, String manufacturer, String designation, double diameter, double length, String digest, WarningSet warnings) {
		
		if (designation == null) {
			warnings.add(Warning.fromString("No motor specified, ignoring."));
			return null;
		}
		
		List<? extends Motor> motors = Application.getMotorSetDatabase().findMotors(type, manufacturer, designation, diameter, length);
		
		// No motors
		if (motors.size() == 0) {
			return handleMissingMotor(type, manufacturer, designation, diameter, length, digest, warnings);
		}		
		
		// One motor
		if (motors.size() == 1) {
			Motor m = motors.get(0);
			if (digest != null && !digest.equals(m.getDigest())) {
				String str = "Motor with designation '" + designation + "'";
				if (manufacturer != null)
					str += " for manufacturer '" + manufacturer + "'";
				str += " has differing thrust curve than the original.";
				warnings.add(str);
			}
			return m;
		}
		
		// Multiple motors, check digest for which one to use
		if (digest != null) {
			
			// Check for motor with correct digest
			for (Motor m : motors) {
				if (digest.equals(m.getDigest())) {
					return m;
				}
			}
			String str = "Motor with designation '" + designation + "'";
			if (manufacturer != null)
				str += " for manufacturer '" + manufacturer + "'";
			str += " has differing thrust curve than the original.";
			warnings.add(str);
			
		} else {
			
			String str = "Multiple motors with designation '" + designation + "'";
			if (manufacturer != null)
				str += " for manufacturer '" + manufacturer + "'";
			str += " found, one chosen arbitrarily.";
			warnings.add(str);
			
		}
		return motors.get(0);
	}
	
}
