package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * List of constants used in RASAero files + helper functions to read parameters from it.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RASAeroCommonConstants {
    // General settings
    public static final String RASAERO_DOCUMENT = "RASAeroDocument";
    public static final String FILE_VERSION = "FileVersion";
    public static final String ROCKET_DESIGN = "RocketDesign";

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

    // Nose cone settings
    public static final String SHAPE = "Shape";
    public static final String POWER_LAW = "PowerLaw";
    //public static final String BLUNT_RADIUS = "BluntRadius";
    private static final Map<String, Transition.Shape> RASAeroNoseConeShapeMap = new HashMap<>();

    // Transition settings
    public static final String REAR_DIAMETER = "RearDiameter";

    // Fin settings
    public static final String FIN_COUNT = "Count";
    public static final String FIN_CHORD = "Chord";
    public static final String FIN_SPAN = "Span";
    public static final String FIN_SWEEP_DISTANCE = "SweepDistance";
    public static final String FIN_TIP_CHORD = "TipChord";
    public static final String FIN_THICKNESS = "Thickness";
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
    public static final String BOAT_TAIL_LENGTH = "BoattailLength";
    public static final String BOAT_TAIL_REAR_DIAMETER = "BoattailRearDiameter";

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
     * Length conversion.  RASAero is in inches, OpenRocket in meters.
     */
    public static final double RASAERO_TO_OPENROCKET_LENGTH = 39.37;
    /**
     * Altitude conversion.  RASAero is in feet, OpenRocket in meters.
     */
    public static final double RASAERO_TO_OPENROCKET_ALTITUDE = 3.28084;
    /**
     * Speed conversion.  RASAero is in mph, OpenRocket in m/s.
     */
    public static final double RASAERO_TO_OPENROCKET_SPEED = 2.23694;
    /**
     * Pressure conversion.  RASAero is in in-hg, OpenRocket in Pa.
     */
    public static final double RASAERO_TO_OPENROCKET_PRESSURE = 0.000295301;
    /**
     * Angle conversion.  RASAero is in degrees, OpenRocket in rad.
     */
    public static final double RASAERO_TO_OPENROCKET_ANGLE = 180 / Math.PI;
    /**
     * Weight conversion from OpenRocket units to RASAero units.  RASAero is in pounds (lb), OpenRocket in kilograms (kg).
     */
    public static final double OPENROCKET_TO_RASAERO_WEIGHT = 2.20462262;
    /**
     * Temperature conversion.  RASAero is in Fahrenheit, OpenRocket in Kelvin.
     */
    public static final double RASAERO_TO_OPENROCKET_TEMPERATURE(Double input) {
        return (input + 459.67) * 5.0 / 9.0;
    }
    public static final double OPENROCKET_TO_RASAERO_TEMPERATURE(Double input) {
        return input * 9.0 / 5.0 - 459.67;
    }

    static {
        RASAeroNoseConeShapeMap.put("Conical", Transition.Shape.CONICAL);
        RASAeroNoseConeShapeMap.put("Tangent Ogive", Transition.Shape.OGIVE);       // = Ogive with shape parameter = 1
        RASAeroNoseConeShapeMap.put("Von Karman Ogive", Transition.Shape.HAACK);    // = Haack series with shape parameter = 0
        RASAeroNoseConeShapeMap.put("Power Law", Transition.Shape.POWER);
        RASAeroNoseConeShapeMap.put("LV-Haack", Transition.Shape.HAACK);            // = Haack series with shape parameter = 1/3
        RASAeroNoseConeShapeMap.put("Parabolic", Transition.Shape.POWER);           // = Power law with shape parameter = 1/2
        RASAeroNoseConeShapeMap.put("Elliptical", Transition.Shape.ELLIPSOID);
    }

    /**
     * Returns the OpenRocket nose cone shape from the RASAero shape string.
     * @param shape The RASAero shape string.
     * @return The OpenRocket nose cone shape.
     */
    public static Transition.Shape getNoseConeShapeFromRASAero(String shape) {
        return RASAeroNoseConeShapeMap.get(shape);
    }

    /**
     * RASAero has a slightly different way of specifying shapes compared to OpenRocket. For instance
     * RASAero has an "LV-Haack" shape, which is the same as the OpenRocket "Haack" shape with a shape
     * parameter of 1/3.
     * This method returns the shape parameter for the RASAero shape to get the correct OpenRocket nose cone shape.
     * @param shape The RASAero shape string.
     * @return The shape parameter for the OpenRocket nose cone.
     */
    public static double getNoseConeShapeParameterFromRASAeroShape(String shape) {
        if ("Conical".equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit
        } else if ("Tangent Ogive".equals(shape)) {
            return 1.0;
        } else if ("Von Karman Ogive".equals(shape)) {
            return 0.0;
        } else if ("Power Law".equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit (will be overwritten later)
        } else if ("LV-Haack".equals(shape)) {
            return 0.33;
        } else if ("Parabolic".equals(shape)) {
            return 0.5;
        } else if ("Elliptical".equals(shape)) {
            return 0.0;     // Not really needed, but just to be explicit
        } else {
            return 0.0;
        }
    }

    public static FinSet.CrossSection getFinCrossSectionFromRASAero(String crossSection, WarningSet warnings) {
        if (CROSS_SECTION_SQUARE.equals(crossSection)) {
            return FinSet.CrossSection.SQUARE;
        } else if (CROSS_SECTION_ROUNDED.equals(crossSection)) {
            return FinSet.CrossSection.ROUNDED;
        } else if (CROSS_SECTION_SUBSONIC_NACA.equals(crossSection)) {
            return FinSet.CrossSection.AIRFOIL;
        } else {
            warnings.add("Unknown fin cross section: " + crossSection + ", defaulting to Airfoil.");
            return FinSet.CrossSection.AIRFOIL;
        }
    }

    public static ExternalComponent.Finish getSurfaceFinishFromRASAero(String surfaceFinish, WarningSet warnings) {
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
            warnings.add("Unknown surface finish: " + surfaceFinish + ", defaulting to Regular Paint.");
            return ExternalComponent.Finish.NORMAL;
        }
    }

    public static DeploymentConfiguration.DeployEvent getDeployEventFromRASAero(String deployEvent, WarningSet warnings) {
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
}
