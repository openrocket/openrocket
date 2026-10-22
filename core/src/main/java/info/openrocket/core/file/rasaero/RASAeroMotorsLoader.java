package info.openrocket.core.file.rasaero;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.file.motor.AbstractMotorLoader;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class RASAeroMotorsLoader {
    private static final String RASAERO_MOTOR_FILE = "rasp.eng";
    private static final String RASAERO_MOTOR_RESOURCE =
            "datafiles/thrustcurves/RASAero/" + RASAERO_MOTOR_FILE;

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
        String normalizedMotorString = motorString.trim();
        if (normalizedMotorString.isEmpty()) {
            return null;
        }
        if (allMotors == null) {
            loadAllMotors(warnings);
        }
        /*
         * RASAero file motor strings are formatted as "<motorName>  (<manufacturer>)"
         */
        String[] split = normalizedMotorString.split("\\s{2,}", 2);
        if (split.length == 2) {
            String motorName = AbstractMotorLoader.removeDelay(split[0].trim());
            String manufacturer = split[1].trim().replaceAll("^\\(|\\)$", "");
            for (ThrustCurveMotor motor : allMotors) {
                if (motorName.equals(motor.getDesignation()) && motor.getManufacturer().matches(manufacturer)) {
                    return motor;
                }
            }
            ThrustCurveMotor motor = findCommonNameMatch(split[0].trim(), manufacturer, warnings);
            if (motor != null) {
                return motor;
            }
        }
        if (warnings != null) {
            warnings.add("Could not find motor '" + normalizedMotorString
                    + "' in the OpenRocket motors database. Stage mass and CG overrides were skipped because "
                    + "RASAero values include motor mass. Add the motor and set the stage's dry mass and CG "
                    + "manually.");
        }
        return null;
    }

    /**
     * Finds an OpenRocket motor whose designation differs from RASAero's only in a propellant or impulse suffix
     * or prefix (for example K707-BG for RASAero's K707), as the export does. The case dimensions must match those of
     * RASAero's catalog entry, and only a unique match is returned.
     */
    private static ThrustCurveMotor findCommonNameMatch(String motorName, String manufacturer,
            WarningSet warnings) {
        String commonName = RASAeroCommonConstants.commonMotorDesignation(motorName);
        List<ThrustCurveMotor> candidates = new ArrayList<>();
        for (ThrustCurveMotor motor : allMotors) {
            if (motor.getManufacturer().matches(manufacturer)
                    && commonName.equals(RASAeroCommonConstants.commonMotorDesignation(motor.getDesignation()))) {
                candidates.add(motor);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        RASAeroMotor catalogMotor = null;
        for (RASAeroMotor rasaeroMotor : loadAllRASAeroMotors(warnings)) {
            if (rasaeroMotor.getDesignation().equals(motorName)
                    && rasaeroMotor.getManufacturer().equalsIgnoreCase(manufacturer)) {
                catalogMotor = rasaeroMotor;
            }
        }
        List<ThrustCurveMotor> dimensionMatches = new ArrayList<>();
        for (ThrustCurveMotor candidate : candidates) {
            if (catalogMotor != null && RASAeroCommonConstants.motorDimensionsMatch(candidate, catalogMotor)) {
                dimensionMatches.add(candidate);
            }
        }
        return dimensionMatches.size() == 1 ? dimensionMatches.get(0) : null;
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

    /**
     * Loads the motor catalog distributed with RASAero II for export validation.
     * <p>
     * The RASAero designation is retained exactly, including delays such as
     * {@code -P}, because that is the spelling RASAero expects in a CDX1 file.
     * 
     * @param warnings the warning set to add loading warnings to
     * @return the motors available in RASAero's {@code rasp.eng}
     */
    public static List<RASAeroMotor> loadAllRASAeroMotors(WarningSet warnings) {
        List<RASAeroMotor> motors = new ArrayList<>();

        try (InputStream stream = openRASAeroMotorResource()) {
            if (stream == null) {
                addWarning(warnings, "Unable to load RASAero motor catalog '" + RASAERO_MOTOR_FILE + "'.");
                return motors;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.ISO_8859_1))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    RASAeroMotor motor = parseMotorHeader(line);
                    if (motor != null) {
                        motors.add(motor);
                    }
                }
            }
        } catch (IOException e) {
            addWarning(warnings, "Unable to load RASAero motor catalog '" + RASAERO_MOTOR_FILE
                    + "': " + e.getMessage());
            return motors;
        }

        if (motors.isEmpty()) {
            addWarning(warnings, "RASAero motor catalog '" + RASAERO_MOTOR_FILE + "' contains no motors.");
        }

        return motors;
    }

    private static InputStream openRASAeroMotorResource() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = contextClassLoader != null
                ? contextClassLoader.getResourceAsStream(RASAERO_MOTOR_RESOURCE)
                : null;
        if (stream == null) {
            stream = RASAeroMotorsLoader.class.getResourceAsStream("/" + RASAERO_MOTOR_RESOURCE);
        }
        return stream;
    }

    /**
     * Parses only RASP motor header records. Export validation needs the exact
     * name, manufacturer, and case dimensions, not the thrust-curve samples.
     */
    private static RASAeroMotor parseMotorHeader(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty() || trimmedLine.startsWith(";")) {
            return null;
        }

        String[] fields = trimmedLine.split("\\s+");
        if (fields.length != 7) {
            return null;
        }

        try {
            double diameter = Double.parseDouble(fields[1]) / 1000.0;
            double length = Double.parseDouble(fields[2]) / 1000.0;
            double propellantMass = Double.parseDouble(fields[4]);
            double totalMass = Double.parseDouble(fields[5]);
            if (diameter <= 0 || length <= 0 || propellantMass < 0 || totalMass < propellantMass) {
                return null;
            }
            return new RASAeroMotor(fields[0], fields[6], diameter, length, fields[3]);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private static void addWarning(WarningSet warnings, String message) {
        if (warnings != null) {
            warnings.add(message);
        }
    }

    /**
     * A motor entry from RASAero's bundled {@code rasp.eng} catalog.
     */
    public static final class RASAeroMotor {
        private final String designation;
        private final String manufacturer;
        private final double diameter;
        private final double length;
        private final String delays;

        private RASAeroMotor(String designation, String manufacturer, double diameter, double length,
                String delays) {
            this.designation = designation;
            this.manufacturer = manufacturer;
            this.diameter = diameter;
            this.length = length;
            this.delays = delays;
        }

        public String getDesignation() {
            return designation;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public double getDiameter() {
            return diameter;
        }

        public double getLength() {
            return length;
        }

        /**
         * Returns whether the catalog lists an ejection delay for this motor.
         *
         * @param delay the ejection delay in seconds
         * @return true if the delay is one of the motor's ejection delays
         */
        public boolean hasDelay(double delay) {
            for (String listedDelay : delays.split("-")) {
                try {
                    if (Math.abs(Double.parseDouble(listedDelay) - delay) < 0.01) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // Not a numeric delay
                }
            }
            return false;
        }
    }

}
