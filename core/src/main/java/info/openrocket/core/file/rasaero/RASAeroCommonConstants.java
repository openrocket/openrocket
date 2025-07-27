package info.openrocket.core.file.rasaero;

import info.openrocket.core.file.motor.AbstractMotorLoader;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.MathUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List of constants used in RASAero files + helper functions to read parameters from it.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RASAeroCommonConstants {
    // File settings
    public static final String FILE_EXTENSION = "CDX1";

    // General settings
    public static final String RASAERO_DOCUMENT = "RASAeroDocument";
    public static final String FILE_VERSION = "FileVersion";
    public static final String ROCKET_DESIGN = "RocketDesign";

    // RASAeroDocument settings
    public static final String MACH_ALT = "MachAlt";

    // Base part settings
    public static final String PART_TYPE = "PartType";
    public static final String LENGTH = "Length";
    public static final String DIAMETER = "Diameter";
    public static final String LOCATION = "Location";
    public static final String COLOR = "Color";
    public static final String SURFACE_FINISH = "Surface";
    public static final String COMMENTS = "Comments";

    // Components
    public static final String NOSE_CONE = "NoseCone";
    public static final String BODY_TUBE = "BodyTube";
    public static final String TRANSITION = "Transition";
    public static final String FIN = "Fin";
    public static final String BOOSTER = "Booster";
    public static final String FIN_CAN = "FinCan";
    public static final String BOATTAIL = "BoatTail";

    // Body tube settings
    public static final String OVERHANG = "Overhang";

    // Nose cone settings
    public static final String SHAPE = "Shape";
    public static final String POWER_LAW = "PowerLaw";
    public static final String BLUNT_RADIUS = "BluntRadius";
    private static final Map<String, Transition.Shape> RASAeroNoseConeShapeMap = new HashMap<>();

    //// Nose cone shapes
    private static final String SHAPE_CONICAL = "Conical";
    private static final String SHAPE_TANGENT_OGIVE = "Tangent Ogive";
    private static final String SHAPE_VON_KARMAN_OGIVE = "Von Karman Ogive";
    private static final String SHAPE_POWER_LAW = "Power Law";
    private static final String SHAPE_LVHAACK = "LV-Haack";
    private static final String SHAPE_PARABOLIC = "Parabolic";
    private static final String SHAPE_ELLIPTICAL = "Elliptical";

    // Transition settings
    public static final String REAR_DIAMETER = "RearDiameter";

    // Fin settings
    public static final String FIN_COUNT = "Count";
    public static final String FIN_CHORD = "Chord";
    public static final String FIN_SPAN = "Span";
    public static final String FIN_SWEEP_DISTANCE = "SweepDistance";
    public static final String FIN_TIP_CHORD = "TipChord";
    public static final String FIN_THICKNESS = "Thickness";
    public static final String FIN_LE_RADIUS = "LERadius";
    public static final String FIN_AIRFOIL_SECTION = "AirfoilSection";
    public static final String FIN_FX1 = "FX1";
    public static final String FIN_FX3 = "FX3";
    public static final String AIRFOIL_SECTION = "AirfoilSection";
    //// LERadius, FX1 and FX3 not used
    public static final String CROSS_SECTION_SQUARE = "Square";
    public static final String CROSS_SECTION_ROUNDED = "Rounded";
    public static final String CROSS_SECTION_SUBSONIC_NACA = "Subsonic NACA";

    // Launch lug settings
    public static final String LAUNCH_LUG_DIAMETER = "LaunchLugDiameter";
    public static final String LAUNCH_LUG_LENGTH = "LaunchLugLength";

    // Rail guide settings
    public static final String RAIL_GUIDE_DIAMETER = "RailGuideDiameter";
    public static final String RAIL_GUIDE_HEIGHT = "RailGuideHeight";

    // Launch shoe settings
    public static final String LAUNCH_SHOE_AREA = "LaunchShoeArea";

    // Fin can settings
    public static final String INSIDE_DIAMETER = "InsideDiameter";
    public static final String SHOULDER_LENGTH = "ShoulderLength";

    // Surface finishes
    public static final String FINISH_SMOOTH = "Smooth (Zero Roughness)";
    public static final String FINISH_POLISHED = "Polished";
    public static final String FINISH_SHEET_METAL = "Sheet Metal";
    public static final String FINISH_SMOOTH_PAINT = "Smooth Paint";
    public static final String FINISH_CAMOUFLAGE = "Camouflage Paint";
    public static final String FINISH_ROUGH_CAMOUFLAGE = "Rough Camouflage Paint";
    public static final String FINISH_GALVANIZED = "Galvanized Metal";
    public static final String FINISH_CAST_IRON = "Cast Iron (Very Rough)";

    // Booster settings
    public static final String BOATTAIL_LENGTH = "BoattailLength";
    public static final String BOATTAIL_REAR_DIAMETER = "BoattailRearDiameter";
    public static final String BOATTAIL_OFFSET = "BoattailOffset";
    public static final String NOZZLE_EXIT_DIAMETER = "NozzleExitDiameter";

    // RocketDesign settings
    public static final String CD = "CD";
    public static final String MODIFIED_BARROWMAN = "ModifiedBarrowman";
    public static final String TURBULENCE = "Turbulence";
    public static final String SUSTAINER_NOZZLE = "SustainerNozzle";
    public static final String BOOSTER1_NOZZLE = "Booster1Nozzle";
    public static final String BOOSTER2_NOZZLE = "Booster2Nozzle";
    public static final String USE_BOOSTER1 = "UseBooster1";
    public static final String USE_BOOSTER2 = "UseBooster2";

    // Launch site settings
    public static final String LAUNCH_SITE = "LaunchSite";
    public static final String LAUNCH_ALTITUDE = "Altitude";
    public static final String LAUNCH_PRESSURE = "Pressure";
    public static final String LAUNCH_ROD_ANGLE = "RodAngle";
    public static final String LAUNCH_ROD_LENGTH = "RodLength";
    public static final String LAUNCH_TEMPERATURE = "Temperature";
    public static final String LAUNCH_WIND_SPEED = "WindSpeed";

    // Recovery settings
    public static final String RECOVERY = "Recovery";
    public static final String RECOVERY_ALTITUDE = "Altitude";
    public static final String RECOVERY_DEVICE_TYPE = "DeviceType";
    public static final String RECOVERY_EVENT = "Event";
    public static final String RECOVERY_SIZE = "Size";
    public static final String RECOVERY_EVENT_TYPE = "EventType";
    public static final String RECOVERY_CD = "CD";

    // Deployment settings
    public static final String DEPLOYMENT_NONE = "None";
    public static final String DEPLOYMENT_APOGEE = "Apogee";
    public static final String DEPLOYMENT_ALTITUDE = "Altitude";

    // Simulation settings
    public static final String SIMULATION_LIST = "SimulationList";
    public static final String SIMULATION = "Simulation";
    public static final String SUSTAINER_ENGINE = "SustainerEngine";
    public static final String SUSTAINER_LAUNCH_WT = "SustainerLaunchWt";
    public static final String SUSTAINER_NOZZLE_DIAMETER = "SustainerNozzleDiameter";
    public static final String SUSTAINER_CG = "SustainerCG";
    public static final String SUSTAINER_IGNITION_DELAY = "SustainerIgnitionDelay";
    public static final String BOOSTER1_ENGINE = "Booster1Engine";
    public static final String BOOSTER1_SEPARATION_DELAY = "Booster1SeparationDelay";       // Delay after booster burnout to separate
    public static final String BOOSTER1_IGNITION_DELAY = "Booster1IgnitionDelay";
    public static final String BOOSTER1_LAUNCH_WT = "Booster1LaunchWt";
    public static final String BOOSTER1_NOZZLE_DIAMETER = "Booster1NozzleDiameter";
    public static final String BOOSTER1_CG = "Booster1CG";
    public static final String INCLUDE_BOOSTER1 = "IncludeBooster1";
    public static final String BOOSTER2_ENGINE = "Booster2Engine";
    public static final String BOOSTER2_SEPARATION_DELAY = "Booster2Delay";       // Delay after booster burnout to separate
    public static final String BOOSTER2_LAUNCH_WT = "Booster2LaunchWt";
    public static final String BOOSTER2_NOZZLE_DIAMETER = "Booster2NozzleDiameter";
    public static final String BOOSTER2_CG = "Booster2CG";
    public static final String INCLUDE_BOOSTER2 = "IncludeBooster2";
    public static final String FLIGHT_TIME = "FlightTime";
    public static final String TIME_TO_APOGEE = "TimetoApogee";
    public static final String MAX_ALTITUDE = "MaxAltitude";
    public static final String MAX_VELOCITY = "MaxVelocity";
    public static final String OPTIMUM_WT = "OptimumWt";
    public static final String OPTIMUM_MAX_ALT = "OptimumMaxAlt";


    /**
     * Length conversion from OpenRocket units to RASAero units.  RASAero is in inches, OpenRocket in meters.
     */
    public static final double OPENROCKET_TO_RASAERO_LENGTH = 39.37;
    /**
     * Altitude conversion from OpenRocket units to RASAero units.  RASAero is in feet, OpenRocket in meters.
     */
    public static final double OPENROCKET_TO_RASAERO_ALTITUDE = 3.28084;
    /**
     * Speed conversion from OpenRocket units to RASAero units.  RASAero is in mph, OpenRocket in m/s.
     */
    public static final double OPENROCKET_TO_RASAERO_SPEED = 2.23694;
    /**
     * Pressure conversion from OpenRocket units to RASAero units.  RASAero is in in-hg, OpenRocket in Pa.
     */
    public static final double OPENROCKET_TO_RASAERO_PRESSURE = 0.000295301;
    /**
     * Angle conversion from OpenRocket units to RASAero units.  RASAero is in degrees, OpenRocket in rad.
     */
    public static final double OPENROCKET_TO_RASAERO_ANGLE = 180 / Math.PI;
    /**
     * Weight conversion from OpenRocket units to RASAero units.  RASAero is in pounds (lb), OpenRocket in kilograms (kg).
     */
    public static final double OPENROCKET_TO_RASAERO_WEIGHT = 2.20462262;
    /**
     * Temperature conversion from OpenRocket units to RASAero units.  RASAero is in Fahrenheit, OpenRocket in Kelvin.
     */
    public static final double RASAERO_TO_OPENROCKET_TEMPERATURE(Double input) {
        return (input + 459.67) * 5.0 / 9.0;
    }
    public static final double OPENROCKET_TO_RASAERO_TEMPERATURE(Double input) {
        return input * 9.0 / 5.0 - 459.67;
    }

    static {
        RASAeroNoseConeShapeMap.put(SHAPE_CONICAL, Transition.Shape.CONICAL);
        RASAeroNoseConeShapeMap.put(SHAPE_TANGENT_OGIVE, Transition.Shape.OGIVE);       // = Ogive with shape parameter = 1
        RASAeroNoseConeShapeMap.put(SHAPE_VON_KARMAN_OGIVE, Transition.Shape.HAACK);    // = Haack series with shape parameter = 0
        RASAeroNoseConeShapeMap.put(SHAPE_POWER_LAW, Transition.Shape.POWER);
        RASAeroNoseConeShapeMap.put(SHAPE_LVHAACK, Transition.Shape.HAACK);            // = Haack series with shape parameter = 1/3
        RASAeroNoseConeShapeMap.put(SHAPE_PARABOLIC, Transition.Shape.POWER);           // = Power law with shape parameter = 1/2
        RASAeroNoseConeShapeMap.put(SHAPE_ELLIPTICAL, Transition.Shape.ELLIPSOID);
    }

    private static final Logger log = LoggerFactory.getLogger(RASAeroCommonConstants.class);

    /**
     * Returns the OpenRocket nose cone shape from the RASAero shape string.
     * @param shape The RASAero shape string.
     * @return The OpenRocket nose cone shape.
     */
    public static Transition.Shape RASAERO_TO_OPENROCKET_SHAPE(String shape) {
        return RASAeroNoseConeShapeMap.get(shape);
    }

    /**
     * Returns the OpenRocket nose cone shape from the RASAero shape string.
     * @param shape The RASAero shape string.
     * @return The OpenRocket nose cone shape object.
     */
    public static NoseConeShapeSettings OPENROCKET_TO_RASAERO_SHAPE(Transition.Shape shape, double shapeParameter)
            throws RASAeroExportException {
        if (shape.equals(Transition.Shape.CONICAL)) {
            return new NoseConeShapeSettings(SHAPE_CONICAL);
        } else if (shape.equals(Transition.Shape.OGIVE) && MathUtil.equals(shapeParameter, 1)) {
            return new NoseConeShapeSettings(SHAPE_TANGENT_OGIVE);
        } else if (shape.equals(Transition.Shape.HAACK) && MathUtil.equals(shapeParameter, 0)) {
            return new NoseConeShapeSettings(SHAPE_VON_KARMAN_OGIVE);
        } else if (shape.equals(Transition.Shape.POWER) && MathUtil.equals(shapeParameter, 0.5, 0.01)) {
            return new NoseConeShapeSettings(SHAPE_PARABOLIC);
        } else if (shape.equals(Transition.Shape.POWER)) {
            return new NoseConeShapeSettings(SHAPE_POWER_LAW, shapeParameter);
        } else if (shape.equals(Transition.Shape.HAACK) && MathUtil.equals(shapeParameter, 0.33, 0.01)) {
            return new NoseConeShapeSettings(SHAPE_LVHAACK);
        } else if (shape.equals(Transition.Shape.ELLIPSOID)) {
            return new NoseConeShapeSettings(SHAPE_ELLIPTICAL);
        }

        // Special cases
        else if (shape.equals(Transition.Shape.OGIVE)) {
            throw new RASAeroExportException(
                    String.format("RASAero only supports Ogive nose cones with shape parameter 1, not %.2f", shapeParameter));
        } else if (shape.equals(Transition.Shape.HAACK)) {
            throw new RASAeroExportException(
                    String.format("RASAero only supports Haack nose cones with shape parameter 0 or 0.33, not %.2f", shapeParameter));
        } else if (shape.equals(Transition.Shape.PARABOLIC)) {
            throw new RASAeroExportException("RASAero does not support Parabolic nose cones");
        }

        throw new RASAeroExportException(
                String.format("Invalid shape and shape parameter combination: %s, %.2f", shape.getName(), shapeParameter));
    }

    /**
     * RASAero has a slightly different way of specifying shapes compared to OpenRocket. For instance
     * RASAero has an "LV-Haack" shape, which is the same as the OpenRocket "Haack" shape with a shape
     * parameter of 1/3.
     * This method returns the shape parameter for the RASAero shape to get the correct OpenRocket nose cone shape.
     * @param shape The RASAero shape string.
     * @return The shape parameter for the OpenRocket nose cone.
     */
    public static double RASAERO_TO_OPENROCKET_SHAPE_PARAMETER(String shape) {
        if (SHAPE_CONICAL.equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit
        } else if (SHAPE_TANGENT_OGIVE.equals(shape)) {
            return 1.0;
        } else if (SHAPE_VON_KARMAN_OGIVE.equals(shape)) {
            return 0.0;
        } else if (SHAPE_POWER_LAW.equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit (will be overwritten later)
        } else if (SHAPE_LVHAACK.equals(shape)) {
            return 0.33;
        } else if (SHAPE_PARABOLIC.equals(shape)) {
            return 0.5;
        } else if (SHAPE_ELLIPTICAL.equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit
        } else {
            return 0.0;
        }
    }

    public static FinSet.CrossSection RASAERO_TO_OPENROCKET_FIN_CROSSSECTION(String crossSection, WarningSet warnings) {
        if (CROSS_SECTION_SQUARE.equals(crossSection)) {
            return FinSet.CrossSection.SQUARE;
        } else if (CROSS_SECTION_ROUNDED.equals(crossSection)) {
            return FinSet.CrossSection.ROUNDED;
        } else if (CROSS_SECTION_SUBSONIC_NACA.equals(crossSection)) {
            return FinSet.CrossSection.AIRFOIL;
        } else {
            String msg = "Unknown fin cross section: " + crossSection + ", defaulting to Airfoil.";
            warnings.add(msg);
            log.debug(msg);
            return FinSet.CrossSection.AIRFOIL;
        }
    }

    public static String OPENROCKET_TO_RASAERO_FIN_CROSSSECTION(FinSet.CrossSection crossSection, WarningSet warnings) {
        if (FinSet.CrossSection.SQUARE.equals(crossSection)) {
            return CROSS_SECTION_SQUARE;
        } else if (FinSet.CrossSection.ROUNDED.equals(crossSection)) {
            return CROSS_SECTION_ROUNDED;
        } else if (FinSet.CrossSection.AIRFOIL.equals(crossSection)) {
            return CROSS_SECTION_SUBSONIC_NACA;
        } else {
            String msg = "Unknown fin cross section: " + crossSection + ".";
            warnings.add(msg);
            log.warn(msg);
            return null;
        }
    }

    public static ExternalComponent.Finish RASAERO_TO_OPENROCKET_SURFACE(String surfaceFinish, WarningSet warnings) {
        // NOTE: the RASAero surface finishes are not really the same as the OpenRocket surface finishes. There are some
        // approximations here.
        if (FINISH_SMOOTH.equals(surfaceFinish)) {
            return ExternalComponent.Finish.MIRROR;
        } else if (FINISH_POLISHED.equals(surfaceFinish)) {
            return ExternalComponent.Finish.FINISHPOLISHED;
        } else if (FINISH_SHEET_METAL.equals(surfaceFinish)) {
            return ExternalComponent.Finish.OPTIMUM;
        } else if (FINISH_SMOOTH_PAINT.equals(surfaceFinish)) {
            return ExternalComponent.Finish.OPTIMUM;
        } else if (FINISH_CAMOUFLAGE.equals(surfaceFinish)) {
            return ExternalComponent.Finish.SMOOTH;
        } else if (FINISH_ROUGH_CAMOUFLAGE.equals(surfaceFinish)) {
            return ExternalComponent.Finish.NORMAL;
        } else if (FINISH_GALVANIZED.equals(surfaceFinish)) {
            return ExternalComponent.Finish.UNFINISHED;
        } else if (FINISH_CAST_IRON.equals(surfaceFinish)) {
            return ExternalComponent.Finish.ROUGHUNFINISHED;
        } else {
            String msg = "Unknown surface finish: " + surfaceFinish + ", defaulting to Regular Paint.";
            warnings.add(msg);
            log.debug(msg);
            return ExternalComponent.Finish.NORMAL;
        }
    }

    public static String OPENROCKET_TO_RASAERO_SURFACE(ExternalComponent.Finish finish, WarningSet warnings) {
        if (finish.equals(ExternalComponent.Finish.MIRROR)) {
            return FINISH_SMOOTH;
        } else if (finish.equals(ExternalComponent.Finish.FINISHPOLISHED)) {
            return FINISH_POLISHED;
        } else if (finish.equals(ExternalComponent.Finish.OPTIMUM)) {
            return FINISH_SHEET_METAL;
        } else if (finish.equals(ExternalComponent.Finish.SMOOTH)) {
            return FINISH_CAMOUFLAGE;
        } else if (finish.equals(ExternalComponent.Finish.NORMAL)) {
            return FINISH_ROUGH_CAMOUFLAGE;
        } else if (finish.equals(ExternalComponent.Finish.UNFINISHED)) {
            return FINISH_GALVANIZED;
        } else if (finish.equals(ExternalComponent.Finish.ROUGHUNFINISHED)) {
            return FINISH_CAST_IRON;
        } else {
            String msg = "Unknown surface finish: " + finish + ", defaulting to Smooth.";
            warnings.add(msg);
            log.debug(msg);
            return FINISH_SMOOTH;
        }
    }

    /**
     * Format an OpenRocket motor as a RASAero motor.
     * @param RASAeroMotors list of available RASAero motors
     * @param ORMotor OpenRocket motor
     * @return a RASAero String representation of a motor
     */
    public static String OPENROCKET_TO_RASAERO_MOTOR(List<ThrustCurveMotor> RASAeroMotors, Motor ORMotor,
                                                     WarningSet warnings) {
        if (!(ORMotor instanceof ThrustCurveMotor)) {
            log.debug("RASAero motor not found: not a thrust curve motor");
            return null;
        }

        for (ThrustCurveMotor RASAeroMotor : RASAeroMotors) {
            String RASAeroDesignation = AbstractMotorLoader.removeDelay(RASAeroMotor.getDesignation());
            if (ORMotor.getDesignation().equals(RASAeroDesignation) &&
                    ((ThrustCurveMotor) ORMotor).getManufacturer().matches(RASAeroMotor.getManufacturer().getDisplayName())) {
                String motorName = RASAeroMotor.getDesignation();
                log.debug(String.format("RASAero RASAeroMotor found: %s", motorName));
                return motorName + "  (" + OPENROCKET_TO_RASAERO_MANUFACTURER(RASAeroMotor.getManufacturer()) + ")";
            }
        }

        String msg = String.format("Could not find RASAero motor for '%s'", ORMotor.getDesignation());
        warnings.add(msg);
        log.debug(msg);
        return null;
    }

    public static String OPENROCKET_TO_RASAERO_MANUFACTURER(Manufacturer manufacturer) {
        if (manufacturer.matches("AeroTech")) {
            return "AT";
        } else if (manufacturer.matches("Estes")) {
            return "ES";
        } else if (manufacturer.matches("Apogee")) {
            return "AP";
        } else if (manufacturer.matches("Quest")) {
            return "QU";
        } else if (manufacturer.matches("Cesaroni")) {
            return "CTI";
        } else if (manufacturer.matches("NoThrust")) {
            return "NoThrust";
        } else if (manufacturer.matches("Ellis Mountain")) {
            return "EM";
        } else if (manufacturer.matches("Contrail")) {
            return "Contrail";
        } else if (manufacturer.matches("Rocketvision")) {
            return "RV";
        } else if (manufacturer.matches("Roadrunner Rocketry")) {
            return "RR";
        } else if (manufacturer.matches("Sky Ripper Systems")) {
            return "SRS";
        } else if (manufacturer.matches("Loki Research")) {
            return "LR";
        } else if (manufacturer.matches("Public Missiles, Ltd.")) {
            return "PML";
        } else if (manufacturer.matches("Kosdon by AeroTech")) {
            return "KBA";
        } else if (manufacturer.matches("Gorilla Rocket Motors")) {
            return "GM";
        } else if (manufacturer.matches("RATT Works")) {
            return "RTW";
        } else if (manufacturer.matches("HyperTEK")) {
            return "HT";
        } else if (manufacturer.matches("Animal Motor Works")) {
            return "AMW";
        } else if (manufacturer.matches("Loki")) {
            return "CT";
        } else if (manufacturer.matches("AMW ProX")) {
            return "AMW/ProX";
        } else if (manufacturer.matches("Loki Research EX")) {
            return "LR-EX";
        } else if (manufacturer.matches("Derek Deville DEAP EX")) {
            return "DEAP-EX";
        } else if (manufacturer.matches("Historical")) {
            return "Hist";
        }
        return manufacturer.getSimpleName();
    }

    public static DeploymentConfiguration.DeployEvent RASAERO_TO_OPENROCKET_DEPLOY_EVENT(String deployEvent, WarningSet warnings) {
        if (DEPLOYMENT_NONE.equals(deployEvent)) {
            return DeploymentConfiguration.DeployEvent.NEVER;
        } else if (DEPLOYMENT_APOGEE.equals(deployEvent)) {
            return DeploymentConfiguration.DeployEvent.APOGEE;
        } else if (DEPLOYMENT_ALTITUDE.equals(deployEvent)) {
            return DeploymentConfiguration.DeployEvent.ALTITUDE;
        }
        warnings.add("Unknown deployment event: " + deployEvent + ", defaulting to apogee.");
        return DeploymentConfiguration.DeployEvent.APOGEE;
    }

    public static String OPENROCKET_TO_RASAERO_COLOR(ORColor color) {
        if (color != null) {
            if (color.equals(ORColor.BLACK)) {
                return "Black";     // Currently the only officially supported color by RASAero
            }
        }
        return "Blue";          // But we can also apply our own color hehe
    }

    /**
     * Class containing the RASAero nose cone shape and shape parameter settings
     */
    public static class NoseConeShapeSettings {
        private final String shape;
        private final Double shapeParameter;

        public NoseConeShapeSettings(String shape, double shapeParameter) {
            this.shape = shape;
            this.shapeParameter = shapeParameter;
        }

        public NoseConeShapeSettings(String shape) {
            this.shape = shape;
            this.shapeParameter = null;
        }

        public String getShape() {
            return shape;
        }

        public Double getShapeParameter() {
            return shapeParameter;
        }
    }
}
