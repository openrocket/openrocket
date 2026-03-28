package info.openrocket.core.file.rasaero;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.file.motor.AbstractMotorLoader;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;

import java.util.ArrayList;
import java.util.List;

public abstract class RASAeroMotorsLoader {
    private static List<ThrustCurveMotor> allMotors = null;

    /**
     * Returns a RASAero motor from the motor string of its RASAero file.
     * 
     * @param motorString The motor string of the RASAero file, e.g. "1/4A2 (AP)".
     * @param warnings    The warning set to add import warnings to.
     * @return The motor, or null if not found.
     */
    public static ThrustCurveMotor getMotorFromRASAero(String motorString, WarningSet warnings) {
        if (motorString == null) {
            return null;
        }
        if (allMotors == null) {
            loadAllMotors(warnings);
        }
        /*
         * RASAero file motor strings are formatted as "<motorName>  (<manufacturer>)"
         */
        String[] split = motorString.split("\\s{2}");
        if (split.length != 2) {
            return null;
        }
        String motorName = AbstractMotorLoader.removeDelay(split[0]);
        String manufacturer = split[1].replaceAll("^\\(|\\)$", ""); // Remove beginning and ending parenthesis
        for (ThrustCurveMotor motor : allMotors) {
            if (motorName.equals(motor.getDesignation()) && motor.getManufacturer().matches(manufacturer)) {
                return motor;
            }
        }
        warnings.add("Could not find motor '" + motorString
                + "' in the OpenRocket motors database. Please add it manually.");
        return null;
    }

    /**
     * Call this method when you don't need the RASAero motors anymore to free
     * memory.
     */
    public static void clearAllMotors() {
        if (allMotors != null) {
            allMotors.clear();
            allMotors = null;
        }
    }

    // Not currently used for importing, because it causes some compatibility issues
    // when e.g. wanting to open the RASAero motor
    // in the motor selection table (because it is not present there).
    // It's probably also better to load OR-native motors.
    // But I'll leave this in, in case it's needed in the future.
    /**
     * Loads all motors available for RASAero export.
     * <p>
     * Historically this loaded motors from a bundled {@code RASAero_Motors.eng} file. That file is no longer shipped;
     * these motors are now part of the normal OpenRocket motor database.
     * 
     * @param warnings The warning set to add import warnings to.
     * @return the loaded motors
     */
    public static List<ThrustCurveMotor> loadAllRASAeroMotors(WarningSet warnings) {
        return loadMotorsFromOpenRocketDatabase(warnings);
    }

    /**
     * Loads the OpenRocket motors database.
     */
    private static void loadAllMotors(WarningSet warnings) {
        allMotors = loadMotorsFromOpenRocketDatabase(warnings);
    }

    private static List<ThrustCurveMotor> loadMotorsFromOpenRocketDatabase(WarningSet warnings) {
        List<ThrustCurveMotor> motors = new ArrayList<>();
        try {
            List<ThrustCurveMotorSet> database = Application.getThrustCurveMotorSetDatabase().getMotorSets();
            for (ThrustCurveMotorSet set : database) {
                motors.addAll(set.getMotors());
            }
        } catch (Exception e) {
            if (warnings != null) {
                warnings.add("Unable to load motors from the OpenRocket motor database: " + e.getMessage());
            }
        }
        return motors;
    }

}
