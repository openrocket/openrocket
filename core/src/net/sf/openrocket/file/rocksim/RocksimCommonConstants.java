package net.sf.openrocket.file.rocksim;

/**
 */
public class RocksimCommonConstants {

    public static final String SHAPE_CODE = "ShapeCode";
    public static final String CONSTRUCTION_TYPE = "ConstructionType";
    public static final String WALL_THICKNESS = "WallThickness";
    public static final String SHAPE_PARAMETER = "ShapeParameter";
    public static final String ATTACHED_PARTS = "AttachedParts";
    public static final String SUBASSEMBLY = "SubAssembly";
    public static final String BODY_TUBE = "BodyTube";
    public static final String FIN_SET = "FinSet";
    public static final String CUSTOM_FIN_SET = "CustomFinSet";
    public static final String RING = "Ring";
    public static final String STREAMER = "Streamer";
    public static final String PARACHUTE = "Parachute";
    public static final String MASS_OBJECT = "MassObject";
    public static final String KNOWN_MASS = "KnownMass";
    public static final String DENSITY = "Density";
    public static final String MATERIAL = "Material";
    public static final String NAME = "Name";
    public static final String KNOWN_CG = "KnownCG";
    public static final String USE_KNOWN_CG = "UseKnownCG";
    public static final String XB = "Xb";
    public static final String CALC_MASS = "CalcMass";
    public static final String CALC_CG = "CalcCG";
    public static final String DENSITY_TYPE = "DensityType";
    public static final String RADIAL_LOC = "RadialLoc";
    public static final String RADIAL_ANGLE = "RadialAngle";
    public static final String LOCATION_MODE = "LocationMode";
    public static final String FINISH_CODE = "FinishCode";
    public static final String SERIAL_NUMBER = "SerialNo";
    public static final String LEN = "Len";
    public static final String OD = "OD";
    public static final String ID = "ID";
    public static final String IS_MOTOR_MOUNT = "IsMotorMount";
    public static final String MOTOR_DIA = "MotorDia";
    public static final String ENGINE_OVERHANG = "EngineOverhang";
    public static final String IS_INSIDE_TUBE = "IsInsideTube";
    public static final String LAUNCH_LUG = "LaunchLug";
    public static final String USAGE_CODE = "UsageCode";
    public static final String AUTO_SIZE = "AutoSize";
    public static final String POINT_LIST = "PointList";
    public static final String FIN_COUNT = "FinCount";
    public static final String ROOT_CHORD = "RootChord";
    public static final String TIP_CHORD = "TipChord";
    public static final String SEMI_SPAN = "SemiSpan";
    public static final String SWEEP_DISTANCE = "SweepDistance";
    public static final String THICKNESS = "Thickness";
    public static final String TIP_SHAPE_CODE = "TipShapeCode";
    public static final String TAB_LENGTH = "TabLength";
    public static final String TAB_DEPTH = "TabDepth";
    public static final String TAB_OFFSET = "TabOffset";
    public static final String SWEEP_MODE = "SweepMode";
    public static final String CANT_ANGLE = "CantAngle";
    public static final String TYPE_CODE = "TypeCode";
    public static final String NOSE_CONE = "NoseCone";
    public static final String BASE_DIA = "BaseDia";
    public static final String SHOULDER_LEN = "ShoulderLen";
    public static final String SHOULDER_OD = "ShoulderOD";
    public static final String DIAMETER = "Dia";
    public static final String SPILL_HOLE_DIA = "SpillHoleDia";
    public static final String SHROUD_LINE_COUNT = "ShroudLineCount";
    public static final String SHROUD_LINE_LEN = "ShroudLineLen";
    public static final String CHUTE_COUNT = "ChuteCount";
    public static final String SHROUD_LINE_MASS_PER_MM = "ShroudLineMassPerMM";
    public static final String SHROUD_LINE_MATERIAL = "ShroudLineMaterial";
    public static final String DRAG_COEFFICIENT = "DragCoefficient";
    public static final String ROCKET_DESIGN = "RocketDesign";
    public static final String TRANSITION = "Transition";
    public static final String WIDTH = "Width";
    public static final String FRONT_SHOULDER_LEN = "FrontShoulderLen";
    public static final String REAR_SHOULDER_LEN = "RearShoulderLen";
    public static final String FRONT_SHOULDER_DIA = "FrontShoulderDia";
    public static final String REAR_SHOULDER_DIA = "RearShoulderDia";
    public static final String FRONT_DIA = "FrontDia";
    public static final String REAR_DIA = "RearDia";
    public static final String ROCK_SIM_DOCUMENT = "RockSimDocument";
    public static final String FILE_VERSION = "FileVersion";
    public static final String DESIGN_INFORMATION = "DesignInformation";
    public static final String TUBE_FIN_SET = "TubeFinSet";
    public static final String RING_TAIL = "RingTail";
    public static final String EXTERNAL_POD = "ExternalPod";
    public static final String TEXTURE = "Texture";
    public static final String TUBE_COUNT = "TubeCount";
    public static final String MAX_TUBES_ALLOWED = "MaxTubesAllowed";

    /**
     * Length conversion.  Rocksim is in millimeters, OpenRocket in meters.
     */
    public static final int ROCKSIM_TO_OPENROCKET_LENGTH = 1000;
    /**
     * Mass conversion.  Rocksim is in grams, OpenRocket in kilograms.
     */
    public static final int ROCKSIM_TO_OPENROCKET_MASS = 1000;
    /**
     * Bulk Density conversion.  Rocksim is in kilograms/cubic meter, OpenRocket in kilograms/cubic meter.
     */
    public static final int ROCKSIM_TO_OPENROCKET_BULK_DENSITY = 1;
    /**
     * Surface Density conversion.  Rocksim is in grams/sq centimeter, OpenRocket in kilograms/sq meter.  1000/(100*100) = 1/10
     */
    public static final double ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY = 1/10d;
    /**
     * Line Density conversion.  Rocksim is in kilograms/meter, OpenRocket in kilograms/meter.
     */
    public static final int ROCKSIM_TO_OPENROCKET_LINE_DENSITY = 1;
    /**
     * Radius conversion.  Rocksim is always in diameters, OpenRocket mostly in radius.
     */
    public static final int ROCKSIM_TO_OPENROCKET_RADIUS = 2 * ROCKSIM_TO_OPENROCKET_LENGTH;
}
