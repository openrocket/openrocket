package info.openrocket.core.file;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorDigest;
import info.openrocket.core.motor.Motor.Type;
import info.openrocket.core.startup.Application;

/**
 * A MotorFinder implementation that searches the thrust curve motor database
 * for a motor.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DatabaseMotorFinder implements MotorFinder {
	private static final Logger log = LoggerFactory.getLogger(DatabaseMotorFinder.class);

	/**
	 * Do something when a missing motor is found.
	 * 
	 * This implementation adds a Warning.MissingMotor to the warning set and
	 * returns null.
	 * 
	 * Override this function to change the behavior.
	 * 
	 * @return The Motor which will be put in the Rocket.
	 */
	protected Motor handleMissingMotor(Type type, String manufacturer, String designation, double diameter,
			double length, String digest, WarningSet warnings) {
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
	public Motor findMotor(Type type, String manufacturer, String designation, double diameter, double length,
			String digest, WarningSet warnings) {

		log.debug("type " + type + ", manufacturer " + manufacturer + ", designation " + designation + ", diameter "
				+ diameter + ", length " + length + ", digest " + digest + ", warnings " + warnings);

		if (designation == null) {
			warnings.add(Warning.fromString("No motor specified, ignoring."));
			return null;
		}

		List<? extends Motor> motors;

		motors = Application.getMotorSetDatabase().findMotors(digest, type, manufacturer, designation, diameter,
				length);

		// No motors
		if (motors.size() == 0) {
			return handleMissingMotor(type, manufacturer, designation, diameter, length, digest, warnings);
		}

		// One motor
		if (motors.size() == 1) {
			Motor m = motors.get(0);

			log.debug("motor is " + m.getDesignation());

			return m;
		}

		// Multiple motors, check digest for which one to use
		if (digest != null) {

			// Prefer a motor with a compatible digest (historical digests included).
			for (Motor m : motors) {
				if (MotorDigest.isDigestCompatible(m, digest)) {
					return m;
				}
			}

			// Fall back to an exact designation match if possible (e.g. prefer "B6" over "B6-0").
			for (Motor m : motors) {
				if (m.getDesignation() != null && m.getDesignation().equalsIgnoreCase(designation)) {
					return m;
				}
			}

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
