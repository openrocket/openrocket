package info.openrocket.core.file;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorDigest;
import info.openrocket.core.motor.ThrustCurveMotor;
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
				if (isDigestCompatible(m, digest)) {
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

	private static boolean isDigestCompatible(Motor motor, String digest) {
		if (digest == null || motor == null) {
			return false;
		}

		String motorDigest = motor.getDigest();
		if (digest.equals(motorDigest)) {
			return true;
		}

		// Backward compatibility: historically OpenRocket has used multiple digest
		// variants. We accept older/alternate digests computed from subsets of the
		// motor data, so that older designs can be opened without spurious warnings.
		if (motor instanceof ThrustCurveMotor tcMotor) {
			double[] timePoints = tcMotor.getTimePoints();
			double[] thrustPoints = tcMotor.getThrustPoints();

			try {
				// Old RASP-style digest: TIME_ARRAY + MASS_SPECIFIC + FORCE_PER_TIME.
				MotorDigest raspStyle = new MotorDigest();
				raspStyle.update(MotorDigest.DataType.TIME_ARRAY, timePoints);
				raspStyle.update(MotorDigest.DataType.MASS_SPECIFIC, tcMotor.getInitialMass(), tcMotor.getBurnoutMass());
				raspStyle.update(MotorDigest.DataType.FORCE_PER_TIME, thrustPoints);
				if (digest.equals(raspStyle.getDigest())) {
					return true;
				}

				// Thrust-only digest: TIME_ARRAY + FORCE_PER_TIME.
				// This stays stable even if mass/CG modeling changes between database versions.
				MotorDigest thrustOnly = new MotorDigest();
				thrustOnly.update(MotorDigest.DataType.TIME_ARRAY, timePoints);
				thrustOnly.update(MotorDigest.DataType.FORCE_PER_TIME, thrustPoints);
				if (digest.equals(thrustOnly.getDigest())) {
					return true;
				}

				// Mass-per-time digest: TIME_ARRAY + MASS_PER_TIME + FORCE_PER_TIME.
				// This matches variants where CG data was not included in the digest.
				var cgPoints = tcMotor.getCGPoints();
				double[] mass = new double[cgPoints.length];
				for (int i = 0; i < cgPoints.length; i++) {
					mass[i] = cgPoints[i].getWeight();
				}
				MotorDigest massPerTime = new MotorDigest();
				massPerTime.update(MotorDigest.DataType.TIME_ARRAY, timePoints);
				massPerTime.update(MotorDigest.DataType.MASS_PER_TIME, mass);
				massPerTime.update(MotorDigest.DataType.FORCE_PER_TIME, thrustPoints);
				if (digest.equals(massPerTime.getDigest())) {
					return true;
				}

				// CG-per-time digest: TIME_ARRAY + CG_PER_TIME + FORCE_PER_TIME.
				double[] cgx = new double[cgPoints.length];
				for (int i = 0; i < cgPoints.length; i++) {
					cgx[i] = cgPoints[i].getX();
				}
				MotorDigest cgPerTime = new MotorDigest();
				cgPerTime.update(MotorDigest.DataType.TIME_ARRAY, timePoints);
				cgPerTime.update(MotorDigest.DataType.CG_PER_TIME, cgx);
				cgPerTime.update(MotorDigest.DataType.FORCE_PER_TIME, thrustPoints);
				return digest.equals(cgPerTime.getDigest());
			} catch (Exception e) {
				return false;
			}
		}

		return false;
	}

}
