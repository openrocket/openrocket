package info.openrocket.core.file.rasaero;

import info.openrocket.core.file.motor.GeneralMotorLoader;
import info.openrocket.core.file.motor.RASPMotorLoader;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.file.motor.AbstractMotorLoader;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;

import java.io.IOException;
import java.io.InputStream;
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
     * Loads all original RASAero motors.
     * 
     * @param warnings The warning set to add import warnings to.
     * @return the loaded motors
     * @throws RuntimeException If the RASAero motors file could not be found.
     */
    public static List<ThrustCurveMotor> loadAllRASAeroMotors(WarningSet warnings) throws RuntimeException {
        List<ThrustCurveMotor> RASAeroMotors = new ArrayList<>();

        RASPMotorLoader loader = new RASPMotorLoader();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String fileName = "RASAero_Motors.eng";
        InputStream is = classloader.getResourceAsStream("datafiles/thrustcurves/RASAero/" + fileName);
        if (is == null) {
            throw new RuntimeException("Could not find " + fileName);
        }
        try {
            List<ThrustCurveMotor.Builder> motors = loader.load(is, fileName, false);
            for (ThrustCurveMotor.Builder builder : motors) {
                RASAeroMotors.add(builder.build());
            }
        } catch (IOException e) {
            warnings.add("Error during motor loading: " + e.getMessage());
        }

        return RASAeroMotors;
    }

    /**
     * Loads the OpenRocket motors database.
     */
    private static void loadAllMotors(WarningSet warnings) {
        allMotors = new ArrayList<>();
        List<ThrustCurveMotorSet> database = Application.getThrustCurveMotorSetDatabase().getMotorSets();
        for (ThrustCurveMotorSet set : database) {
            allMotors.addAll(set.getMotors());
        }
        // allMotors.addAll(loadAllRASAeroMotors(warnings));
    }

}
