package info.openrocket.core.simulation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import info.openrocket.core.util.Groupable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Chars;
import info.openrocket.core.util.StringUtils;

/**
 * A class defining a storable simulation variable type. This class defined
 * numerous ready
 * types, and allows also creating new types with any name. When retrieving
 * types based on
 * a name, you should use {@link #getType(String, String, UnitGroup)} to return
 * the default unit type,
 * or a new type if the name does not currently exist.
 * <p>
 * Each type has a type name (description), a unit group and a priority. The
 * type is identified
 * purely by its name case-insensitively. The unit group provides the units for
 * the type.
 * The priority is used to order the types. The pre-existing types are defined
 * specific priority
 * numbers, and other types have a default priority number that is after all
 * other types.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightDataType implements Comparable<FlightDataType>, Groupable<FlightDataTypeGroup>, DataType {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(FlightDataType.class);

	/** Priority of custom-created variables */
	private static final int DEFAULT_PRIORITY = 999;

	/** List of existing types keyed by symbol. MUST BE DEFINED BEFORE ANY TYPES!! */
	private static final Map<String, FlightDataType> EXISTING_TYPES = new HashMap<>();

	/** List of built-in types keyed by save key, for fast .ork deserialization. MUST BE DEFINED BEFORE ANY TYPES!! */
	private static final Map<String, FlightDataType> SAVE_KEY_TYPES = new HashMap<>();

	//// Time
	public static final FlightDataType TYPE_TIME = newType("time", trans.get("FlightDataType.TYPE_TIME"), "t",
			UnitGroup.UNITS_LONG_TIME,
			FlightDataTypeGroup.TIME, 0);

	//// Position and motion
	//// Altitude
	public static final FlightDataType TYPE_ALTITUDE = newType("altitude", trans.get("FlightDataType.TYPE_ALTITUDE"), "h",
			UnitGroup.UNITS_DISTANCE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 0);
	//// Altitude above sea level
	public static final FlightDataType TYPE_ALTITUDE_ABOVE_SEA = newType("altitude_above_sea",
			trans.get("FlightDataType.TYPE_ALTITUDE_ABOVE_SEA"),
			"ha", UnitGroup.UNITS_DISTANCE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 1);
	//// Vertical velocity
	public static final FlightDataType TYPE_VELOCITY_Z = newType("velocity_z",
			trans.get("FlightDataType.TYPE_VELOCITY_Z"), "Vz",
			UnitGroup.UNITS_VELOCITY,
			FlightDataTypeGroup.POSITION_AND_MOTION, 2);
	//// Total velocity
	public static final FlightDataType TYPE_VELOCITY_TOTAL = newType("velocity_total",
			trans.get("FlightDataType.TYPE_VELOCITY_TOTAL"),
			"Vt", UnitGroup.UNITS_VELOCITY,
			FlightDataTypeGroup.POSITION_AND_MOTION, 3);
	//// Vertical acceleration
	public static final FlightDataType TYPE_ACCELERATION_Z = newType("acceleration_z",
			trans.get("FlightDataType.TYPE_ACCELERATION_Z"),
			"Az", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 4);
	//// X acceleration
	public static final FlightDataType TYPE_ACCELERATION_X = newType("acceleration_x",
			trans.get("FlightDataType.TYPE_ACCELERATION_X"),
			"Ax", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 5);
	//// Y acceleration
	public static final FlightDataType TYPE_ACCELERATION_Y = newType("acceleration_y",
			trans.get("FlightDataType.TYPE_ACCELERATION_Y"),
			"Ay", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 6);
	//// X body acceleration
	public static final FlightDataType TYPE_ACCELERATION_BODYX = newType("acceleration_bodyx",
			trans.get("FlightDataType.TYPE_ACCELERATION_BODYX"),
			"Abx", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 7);
	//// Y body acceleration
	public static final FlightDataType TYPE_ACCELERATION_BODYY = newType("acceleration_bodyy",
			trans.get("FlightDataType.TYPE_ACCELERATION_BODYY"),
			"Aby", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 8);
	//// Z body acceleration
	public static final FlightDataType TYPE_ACCELERATION_BODYZ = newType("acceleration_bodyz",
			trans.get("FlightDataType.TYPE_ACCELERATION_BODYZ"),
			"Abz", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 9);
	//// Total acceleration
	public static final FlightDataType TYPE_ACCELERATION_TOTAL = newType("acceleration_total",
			trans.get("FlightDataType.TYPE_ACCELERATION_TOTAL"), "At", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 10);

	//// Lateral position and motion
	//// Position East of launch
	public static final FlightDataType TYPE_POSITION_X = newType("position_x",
			trans.get("FlightDataType.TYPE_POSITION_X"), "Px",
			UnitGroup.UNITS_DISTANCE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 11);
	//// Position North of launch
	public static final FlightDataType TYPE_POSITION_Y = newType("position_y",
			trans.get("FlightDataType.TYPE_POSITION_Y"), "Py",
			UnitGroup.UNITS_DISTANCE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 12);
	//// Lateral distance
	public static final FlightDataType TYPE_POSITION_XY = newType("position_xy",
			trans.get("FlightDataType.TYPE_POSITION_XY"), "Pl",
			UnitGroup.UNITS_DISTANCE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 13);
	//// Lateral direction
	public static final FlightDataType TYPE_POSITION_DIRECTION = newType("position_direction",
			trans.get("FlightDataType.TYPE_POSITION_DIRECTION"), "\u03b8l", UnitGroup.UNITS_ANGLE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 14);
	//// Lateral velocity
	public static final FlightDataType TYPE_VELOCITY_XY = newType("velocity_xy",
			trans.get("FlightDataType.TYPE_VELOCITY_XY"), "Vl",
			UnitGroup.UNITS_VELOCITY,
			FlightDataTypeGroup.POSITION_AND_MOTION, 15);
	//// Lateral acceleration
	public static final FlightDataType TYPE_ACCELERATION_XY = newType("acceleration_xy",
			trans.get("FlightDataType.TYPE_ACCELERATION_XY"),
			"Al", UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.POSITION_AND_MOTION, 16);
	//// Latitude
	public static final FlightDataType TYPE_LATITUDE = newType("latitude",
			trans.get("FlightDataType.TYPE_LATITUDE"), "\u03c6",
			UnitGroup.UNITS_LATITUDE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 17);
	//// Longitude
	public static final FlightDataType TYPE_LONGITUDE = newType("longitude",
			trans.get("FlightDataType.TYPE_LONGITUDE"), "\u03bb",
			UnitGroup.UNITS_LONGITUDE,
			FlightDataTypeGroup.POSITION_AND_MOTION, 18);

	// Orientation
	//// Angle of attack
	public static final FlightDataType TYPE_AOA = newType("aoa", trans.get("FlightDataType.TYPE_AOA"), "\u03b1",
			UnitGroup.UNITS_ANGLE,
			FlightDataTypeGroup.ORIENTATION, 0);
	//// Roll rate
	public static final FlightDataType TYPE_ROLL_RATE = newType("roll_rate",
			trans.get("FlightDataType.TYPE_ROLL_RATE"), "d\u03a6",
			UnitGroup.UNITS_ROLL,
			FlightDataTypeGroup.ORIENTATION, 1);
	//// Pitch rate
	public static final FlightDataType TYPE_PITCH_RATE = newType("pitch_rate",
			trans.get("FlightDataType.TYPE_PITCH_RATE"), "d\u03b8",
			UnitGroup.UNITS_ROLL,
			FlightDataTypeGroup.ORIENTATION, 2);
	//// Yaw rate
	public static final FlightDataType TYPE_YAW_RATE = newType("yaw_rate",
			trans.get("FlightDataType.TYPE_YAW_RATE"), "d\u03a8",
			UnitGroup.UNITS_ROLL,
			FlightDataTypeGroup.ORIENTATION, 3);
	//// Vertical orientation (zenith)
	public static final FlightDataType TYPE_ORIENTATION_THETA = newType("orientation_theta",
			trans.get("FlightDataType.TYPE_ORIENTATION_THETA"), "\u0398", UnitGroup.UNITS_ANGLE,
			FlightDataTypeGroup.ORIENTATION, 4);
	//// Lateral orientation (azimuth)
	public static final FlightDataType TYPE_ORIENTATION_PHI = newType("orientation_phi",
			trans.get("FlightDataType.TYPE_ORIENTATION_PHI"),
			"\u03a6", UnitGroup.UNITS_ANGLE,
			FlightDataTypeGroup.ORIENTATION, 5);
	// Mass and inertia
	//// Mass
	public static final FlightDataType TYPE_MASS = newType("mass", trans.get("FlightDataType.TYPE_MASS"), "m",
			UnitGroup.UNITS_MASS,
			FlightDataTypeGroup.MASS_AND_INERTIA, 0);
	//// Motor mass
	public static final FlightDataType TYPE_MOTOR_MASS = newType("motor_mass",
			trans.get("FlightDataType.TYPE_MOTOR_MASS"), "mp",
			UnitGroup.UNITS_MASS,
			FlightDataTypeGroup.MASS_AND_INERTIA, 1);
	//// Longitudinal moment of inertia
	public static final FlightDataType TYPE_LONGITUDINAL_INERTIA = newType("longitudinal_inertia",
			trans.get("FlightDataType.TYPE_LONGITUDINAL_INERTIA"), "Il", UnitGroup.UNITS_INERTIA,
			FlightDataTypeGroup.MASS_AND_INERTIA, 2);
	//// Rotational moment of inertia
	public static final FlightDataType TYPE_ROTATIONAL_INERTIA = newType("rotational_inertia",
			trans.get("FlightDataType.TYPE_ROTATIONAL_INERTIA"), "Ir", UnitGroup.UNITS_INERTIA,
			FlightDataTypeGroup.MASS_AND_INERTIA, 3);
	//// Gravity
	public static final FlightDataType TYPE_GRAVITY = newType("gravity",
			trans.get("FlightDataType.TYPE_GRAVITY"), "g",
			UnitGroup.UNITS_ACCELERATION,
			FlightDataTypeGroup.MASS_AND_INERTIA, 4);

	// Stability
	//// CP location
	public static final FlightDataType TYPE_CP_LOCATION = newType("cp_location",
			trans.get("FlightDataType.TYPE_CP_LOCATION"), "Cp",
			UnitGroup.UNITS_LENGTH,
			FlightDataTypeGroup.STABILITY, 0);
	//// CG location
	public static final FlightDataType TYPE_CG_LOCATION = newType("cg_location",
			trans.get("FlightDataType.TYPE_CG_LOCATION"), "Cg",
			UnitGroup.UNITS_LENGTH,
			FlightDataTypeGroup.STABILITY, 1);
	//// Stability margin calibers
	public static final FlightDataType TYPE_STABILITY = newType("stability",
			trans.get("FlightDataType.TYPE_STABILITY"), "S",
			UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.STABILITY, 2);
	//// Damping ratio
	public static final FlightDataType TYPE_DAMPING_RATIO = newType("damping_ratio",
			trans.get("FlightDataType.TYPE_DAMPING_RATIO"), "\u03b6", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.STABILITY, 3);
	//// Natural frequency
	public static final FlightDataType TYPE_NATURAL_FREQUENCY = newType("natural_frequency",
			trans.get("FlightDataType.TYPE_NATURAL_FREQUENCY"), "\u03c9n", UnitGroup.UNITS_ROLL,
			FlightDataTypeGroup.STABILITY, 4);

	// Characteristic numbers
	//// Mach number
	public static final FlightDataType TYPE_MACH_NUMBER = newType("mach_number",
			trans.get("FlightDataType.TYPE_MACH_NUMBER"), "M",
			UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.CHARACTERISTIC_NUMBERS, 0);
	//// Reynolds number
	public static final FlightDataType TYPE_REYNOLDS_NUMBER = newType("reynolds_number",
			trans.get("FlightDataType.TYPE_REYNOLDS_NUMBER"),
			"R", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.CHARACTERISTIC_NUMBERS, 1);

	// Thrust and drag
	//// Thrust
	public static final FlightDataType TYPE_THRUST_FORCE = newType("thrust_force",
			trans.get("FlightDataType.TYPE_THRUST_FORCE"), "Ft",
			UnitGroup.UNITS_FORCE,
			FlightDataTypeGroup.THRUST_AND_DRAG, 0);
	//// Thrust-to-weight ratio
	public static final FlightDataType TYPE_THRUST_WEIGHT_RATIO = newType("thrust_weight_ratio",
			trans.get("FlightDataType.TYPE_THRUST_WEIGHT_RATIO"), "Twr", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 1);
	//// Drag force
	public static final FlightDataType TYPE_DRAG_FORCE = newType("drag_force",
			trans.get("FlightDataType.TYPE_DRAG_FORCE"), "Fd",
			UnitGroup.UNITS_FORCE,
			FlightDataTypeGroup.THRUST_AND_DRAG, 2);
	//// Drag coefficient
	public static final FlightDataType TYPE_DRAG_COEFF = newType("drag_coeff",
			trans.get("FlightDataType.TYPE_DRAG_COEFF"), "Cd",
			UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 3);
	//// Friction drag coefficient
	public static final FlightDataType TYPE_FRICTION_DRAG_COEFF = newType("friction_drag_coeff",
			trans.get("FlightDataType.TYPE_FRICTION_DRAG_COEFF"), "Cdf", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 4);
	//// Pressure drag coefficient
	public static final FlightDataType TYPE_PRESSURE_DRAG_COEFF = newType("pressure_drag_coeff",
			trans.get("FlightDataType.TYPE_PRESSURE_DRAG_COEFF"), "Cdp", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 5);
	//// Base drag coefficient
	public static final FlightDataType TYPE_BASE_DRAG_COEFF = newType("base_drag_coeff",
			trans.get("FlightDataType.TYPE_BASE_DRAG_COEFF"),
			"Cdb", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 6);
	//// Axial drag coefficient
	public static final FlightDataType TYPE_AXIAL_DRAG_COEFF = newType("axial_drag_coeff",
			trans.get("FlightDataType.TYPE_AXIAL_DRAG_COEFF"), "Cda", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.THRUST_AND_DRAG, 7);

	// Coefficients
	//// Normal force coefficient
	public static final FlightDataType TYPE_NORMAL_FORCE_COEFF = newType("normal_force_coeff",
			trans.get("FlightDataType.TYPE_NORMAL_FORCE_COEFF"), "Cn", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 0);
	//// CP weight (CNa)
	public static final FlightDataType TYPE_CNA = newType("cna",
			trans.get("FlightDataType.TYPE_CNA"), "CN" + Chars.ALPHA, UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 1);
	//// Pitch moment coefficient
	public static final FlightDataType TYPE_PITCH_MOMENT_COEFF = newType("pitch_moment_coeff",
			trans.get("FlightDataType.TYPE_PITCH_MOMENT_COEFF"), "C\u03b8", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 2);
	//// Yaw moment coefficient
	public static final FlightDataType TYPE_YAW_MOMENT_COEFF = newType("yaw_moment_coeff",
			trans.get("FlightDataType.TYPE_YAW_MOMENT_COEFF"), "C\u03c4\u03a8", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 3);
	//// Side force coefficient
	public static final FlightDataType TYPE_SIDE_FORCE_COEFF = newType("side_force_coeff",
			trans.get("FlightDataType.TYPE_SIDE_FORCE_COEFF"), "C\u03c4s", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 4);
	//// Roll moment coefficient
	public static final FlightDataType TYPE_ROLL_MOMENT_COEFF = newType("roll_moment_coeff",
			trans.get("FlightDataType.TYPE_ROLL_MOMENT_COEFF"), "C\u03c4\u03a6", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 5);
	//// Roll forcing coefficient
	public static final FlightDataType TYPE_ROLL_FORCING_COEFF = newType("roll_forcing_coeff",
			trans.get("FlightDataType.TYPE_ROLL_FORCING_COEFF"), "Cf\u03a6", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 6);
	//// Roll damping coefficient
	public static final FlightDataType TYPE_ROLL_DAMPING_COEFF = newType("roll_damping_coeff",
			trans.get("FlightDataType.TYPE_ROLL_DAMPING_COEFF"), "C\u03b6\u03a6", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 7);
	//// Pitch damping coefficient
	public static final FlightDataType TYPE_PITCH_DAMPING_MOMENT_COEFF = newType("pitch_damping_moment_coeff",
			trans.get("FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF"), "C\u03b6\u03b8", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 8);
	//// Yaw damping coefficient
	public static final FlightDataType TYPE_YAW_DAMPING_MOMENT_COEFF = newType("yaw_damping_moment_coeff",
			trans.get("FlightDataType.TYPE_YAW_DAMPING_MOMENT_COEFF"), "C\u03b6\u03a8", UnitGroup.UNITS_COEFFICIENT,
			FlightDataTypeGroup.COEFFICIENTS, 9);
	//// Damping moment coefficient
	public static final FlightDataType TYPE_DAMPING_MOMENT_COEFF = newType("damping_moment_coeff",
			trans.get("FlightDataType.TYPE_DAMPING_MOMENT_COEFF"), "Cdm", UnitGroup.UNITS_ANGULAR_MOMENTUM,
			FlightDataTypeGroup.COEFFICIENTS, 10);
	//// Damping moment coefficient (aerodynamic part)
	public static final FlightDataType TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC = newType(
			"damping_moment_coeff_aerodynamic",
			trans.get("FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC"), "Cdm_aero",
			UnitGroup.UNITS_ANGULAR_MOMENTUM,
			FlightDataTypeGroup.COEFFICIENTS, 11);
	//// Damping moment coefficient (propulsive part)
	public static final FlightDataType TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE = newType(
			"damping_moment_coeff_propulsive",
			trans.get("FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE"), "Cdm_prop",
			UnitGroup.UNITS_ANGULAR_MOMENTUM,
			FlightDataTypeGroup.COEFFICIENTS, 12);
	//// Corrective moment coefficient
	public static final FlightDataType TYPE_CORRECTIVE_MOMENT_COEFF = newType("corrective_moment_coeff",
			trans.get("FlightDataType.TYPE_CORRECTIVE_MOMENT_COEFF"), "Ccm", UnitGroup.UNITS_MOMENT,
			FlightDataTypeGroup.COEFFICIENTS, 13);

	//// Coriolis acceleration
	public static final FlightDataType TYPE_CORIOLIS_ACCELERATION = newType("coriolis_acceleration",
			trans.get("FlightDataType.TYPE_CORIOLIS_ACCELERATION"), "Ac", UnitGroup.UNITS_ACCELERATION, 99);

	// Reference values
	//// Reference length
	public static final FlightDataType TYPE_REFERENCE_LENGTH = newType("reference_length",
			trans.get("FlightDataType.TYPE_REFERENCE_LENGTH"), "Lr", UnitGroup.UNITS_LENGTH,
			FlightDataTypeGroup.REFERENCE_VALUES, 0);
	//// Reference area
	public static final FlightDataType TYPE_REFERENCE_AREA = newType("reference_area",
			trans.get("FlightDataType.TYPE_REFERENCE_AREA"),
			"Ar", UnitGroup.UNITS_AREA,
			FlightDataTypeGroup.REFERENCE_VALUES, 1);

	// Atmospheric conditions
	//// Wind velocity
	public static final FlightDataType TYPE_WIND_VELOCITY = newType("wind_velocity",
			trans.get("FlightDataType.TYPE_WIND_VELOCITY"),
			"Vw", UnitGroup.UNITS_VELOCITY,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 0);
	//// Wind direction
	public static final FlightDataType TYPE_WIND_DIRECTION = newType("wind_direction",
			trans.get("FlightDataType.TYPE_WIND_DIRECTION"),
			"\u03b8w", UnitGroup.UNITS_ANGLE,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 1);
	//// Air temperature
	public static final FlightDataType TYPE_AIR_TEMPERATURE = newType("air_temperature",
			trans.get("FlightDataType.TYPE_AIR_TEMPERATURE"),
			"T", UnitGroup.UNITS_TEMPERATURE,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 2);
	//// Air pressure
	public static final FlightDataType TYPE_AIR_PRESSURE = newType("air_pressure",
			trans.get("FlightDataType.TYPE_AIR_PRESSURE"), "P",
			UnitGroup.UNITS_PRESSURE,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 3);
	//// Air density
	public static final FlightDataType TYPE_AIR_DENSITY = newType("air_density",
			trans.get("FlightDataType.TYPE_AIR_DENSITY"), "\u03C1",
			UnitGroup.UNITS_DENSITY_BULK,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 4);
	//// Speed of sound
	public static final FlightDataType TYPE_SPEED_OF_SOUND = newType("speed_of_sound",
			trans.get("FlightDataType.TYPE_SPEED_OF_SOUND"),
			"Vs", UnitGroup.UNITS_VELOCITY,
			FlightDataTypeGroup.ATMOSPHERIC_CONDITIONS, 5);

	// Simulation information
	//// Simulation time step
	public static final FlightDataType TYPE_TIME_STEP = newType("time_step",
			trans.get("FlightDataType.TYPE_TIME_STEP"), "dt",
			UnitGroup.UNITS_TIME_STEP,
			FlightDataTypeGroup.SIMULATION_INFORMATION, 0);
	//// Computation time
	public static final FlightDataType TYPE_COMPUTATION_TIME = newType("computation_time",
			trans.get("FlightDataType.TYPE_COMPUTATION_TIME"), "tc", UnitGroup.UNITS_SHORT_TIME,
			FlightDataTypeGroup.SIMULATION_INFORMATION, 1);

	// An array of all the built in types
	public static final FlightDataType[] ALL_TYPES = {
			TYPE_TIME,
			TYPE_ALTITUDE,
			TYPE_ALTITUDE_ABOVE_SEA,
			TYPE_VELOCITY_Z,
			TYPE_ACCELERATION_Z,
			TYPE_ACCELERATION_BODYZ,
			TYPE_VELOCITY_TOTAL,
			TYPE_ACCELERATION_TOTAL,
			TYPE_POSITION_X,
			TYPE_ACCELERATION_X,
			TYPE_ACCELERATION_BODYX,
			TYPE_POSITION_Y,
			TYPE_ACCELERATION_Y,
			TYPE_ACCELERATION_BODYY,
			TYPE_POSITION_XY,
			TYPE_POSITION_DIRECTION,
			TYPE_VELOCITY_XY,
			TYPE_ACCELERATION_XY,
			TYPE_LATITUDE,
			TYPE_LONGITUDE,
			TYPE_GRAVITY,
			TYPE_AOA,
			TYPE_ROLL_RATE,
			TYPE_PITCH_RATE,
			TYPE_YAW_RATE,
			TYPE_MASS,
			TYPE_MOTOR_MASS,
			TYPE_LONGITUDINAL_INERTIA,
			TYPE_ROTATIONAL_INERTIA,
			TYPE_CP_LOCATION,
			TYPE_CG_LOCATION,
			TYPE_STABILITY,
			TYPE_MACH_NUMBER,
			TYPE_REYNOLDS_NUMBER,
			TYPE_THRUST_FORCE,
			TYPE_THRUST_WEIGHT_RATIO,
			TYPE_DRAG_FORCE,
			TYPE_DRAG_COEFF,
			TYPE_AXIAL_DRAG_COEFF,
			TYPE_FRICTION_DRAG_COEFF,
			TYPE_PRESSURE_DRAG_COEFF,
			TYPE_BASE_DRAG_COEFF,
			TYPE_NORMAL_FORCE_COEFF,
			TYPE_CNA,
			TYPE_PITCH_MOMENT_COEFF,
			TYPE_YAW_MOMENT_COEFF,
			TYPE_SIDE_FORCE_COEFF,
			TYPE_ROLL_MOMENT_COEFF,
			TYPE_ROLL_FORCING_COEFF,
			TYPE_ROLL_DAMPING_COEFF,
			TYPE_PITCH_DAMPING_MOMENT_COEFF,
			TYPE_YAW_DAMPING_MOMENT_COEFF,
			TYPE_DAMPING_MOMENT_COEFF,
			TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC,
			TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE,
			TYPE_CORRECTIVE_MOMENT_COEFF,
			TYPE_DAMPING_RATIO,
			TYPE_NATURAL_FREQUENCY,
			TYPE_CORIOLIS_ACCELERATION,
			TYPE_REFERENCE_LENGTH,
			TYPE_REFERENCE_AREA,
			TYPE_ORIENTATION_THETA,
			TYPE_ORIENTATION_PHI,
			TYPE_WIND_VELOCITY,
			TYPE_WIND_DIRECTION,
			TYPE_AIR_TEMPERATURE,
			TYPE_AIR_PRESSURE,
			TYPE_AIR_DENSITY,
			TYPE_SPEED_OF_SOUND,
			TYPE_TIME_STEP,
			TYPE_COMPUTATION_TIME
	};

	/**
	 * Return a {@link FlightDataType} with a given string description, symbol and
	 * unitgroup.
	 * This returns an existing data type if the symbol matches that of an existing
	 * type.
	 *
	 * If the symbol matches but the unit and description information differ, then
	 * the old stored datatype
	 * is erased and the updated version based on the given parameters is returned.
	 * The only exception is if the description or unitgroup are undefined (null or
	 * empty string). In this case
	 * we just get these parameters from the existing type when making the new one.
	 *
	 * @param s the string description of the type.
	 * @param u the unit group the new type should belong to if a new group is
	 *          created.
	 * @return a data type.
	 */
	public static synchronized FlightDataType getType(String s, String symbol, UnitGroup u) {

		// if symbol is null : try finding by name
		// if unit is null : don't do anything to the unit if found, just return
		// datatype if found and generate an error and an empty unit otherwise
		int oldPriority = DEFAULT_PRIORITY;

		// FlightDataType type = findFromSymbol(symbol);
		FlightDataType type = EXISTING_TYPES.get(symbol);

		if (type != null) {
			// found it from symbol

			// if name was not given (empty string), can use the one we found
			if (s == null || StringUtils.isEmpty(s)) {
				s = type.getName();
			}
			if (u == null) {
				u = type.getUnitGroup();
			}

			// if something has changed, then we need to remove the old one
			// otherwise, just return what we found
			if (!u.equals(type.getUnitGroup())) {
				oldPriority = type.priority;
				EXISTING_TYPES.remove(symbol);
				log.info("Unitgroup of type " + type.getName() +
						", has changed from " + type.getUnitGroup().toString() +
						" to " + u.toString() +
						". Removing old version.");
			} else if (!s.equals(type.getName())) {
				oldPriority = type.priority;
				EXISTING_TYPES.remove(symbol);
				log.info("Name of type " + type.getName() + ", has changed to " + s + ". Removing old version.");
			} else {
				return type;
			}
		}

		if (u == null) {
			u = UnitGroup.UNITS_NONE;
			log.error("Made a new flightdatatype, but did not know what units to use.");
		}

		// make a new one (no save key — this is a custom/dynamic type)
		type = newType(s, symbol, u, oldPriority);
		return type;
	}

	/**
	 * Return the built-in {@link FlightDataType} with the given save key, or {@code null} if not found.
	 * Save keys are stable, language-independent identifiers used for .ork serialization.
	 */
	public static FlightDataType getTypeBySaveKey(String saveKey) {
		return SAVE_KEY_TYPES.get(saveKey);
	}

	/*
	 * Get the flightdatatype from existing types based on the symbol.
	 */
	/*
	 * private static FlightDataType findFromSymbol(String symbol){
	 * for (FlightDataType t : EXISTING_TYPES.values()){
	 * if (t.getSymbol().equals(symbol)){
	 * return t;
	 * }
	 * }
	 * return null;
	 * }
	 */

	/**
	 * Used while initializing the class. Creates a built-in type with a stable save key.
	 *
	 * @param saveKey  stable, language-independent key used for .ork serialization.
	 * @param s        the display name of the type.
	 * @param symbol   the mathematical symbol of the type.
	 * @param u        the unit group of the type.
	 * @param group    the group of the type.
	 * @param priority the priority of the type within the group.
	 */
	private static synchronized FlightDataType newType(String saveKey, String s, String symbol, UnitGroup u,
			FlightDataTypeGroup group, int priority) {
		FlightDataType type = new FlightDataType(s, saveKey, symbol, u, group, priority);
		EXISTING_TYPES.put(symbol, type);
		SAVE_KEY_TYPES.put(saveKey, type);
		return type;
	}

	private static synchronized FlightDataType newType(String saveKey, String s, String symbol, UnitGroup u,
			int priority) {
		FlightDataType type = new FlightDataType(s, saveKey, symbol, u, FlightDataTypeGroup.CUSTOM, priority);
		EXISTING_TYPES.put(symbol, type);
		SAVE_KEY_TYPES.put(saveKey, type);
		return type;
	}

	/** Used by {@link #getType} to create custom/dynamic types with no stable save key. */
	private static synchronized FlightDataType newType(String s, String symbol, UnitGroup u, int priority) {
		FlightDataType type = new FlightDataType(s, null, symbol, u, FlightDataTypeGroup.CUSTOM, priority);
		EXISTING_TYPES.put(symbol, type);
		return type;
	}

	private final String name;
	private final String saveKey;
	private final String symbol;
	private final UnitGroup units;
	private final FlightDataTypeGroup group;
	private final int priority;
	private final int hashCode;

	private FlightDataType(String typeName, String saveKey, String symbol, UnitGroup units, FlightDataTypeGroup group,
			int priority) {
		if (typeName == null)
			throw new IllegalArgumentException("typeName is null");
		if (units == null)
			throw new IllegalArgumentException("units is null");
		this.name = typeName;
		this.saveKey = saveKey;
		this.symbol = symbol;
		this.units = units;
		this.group = group;
		this.priority = priority;
		this.hashCode = this.name.toLowerCase(Locale.ENGLISH).hashCode();
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the stable, language-independent key used to identify this type in .ork files.
	 * For built-in types this is a fixed string (e.g. {@code "TYPE_DRAG_COEFF"}) that never
	 * changes regardless of locale or display-name renames.
	 * For custom/dynamic types (user expressions) that have no fixed save key, this falls back
	 * to {@link #getName()} to preserve the existing serialisation behaviour.
	 */
	public String getSaveKey() {
		return saveKey != null ? saveKey : name;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public UnitGroup getUnitGroup() {
		return units;
	}

	@Override
	public FlightDataTypeGroup getGroup() {
		return group;
	}

	public int getGroupPriority() {
		return group.getPriority();
	}

	@Override
	public String toString() {
		return name; // +" ("+symbol+") "+units.getDefaultUnit().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FlightDataType))
			return false;
		return this.name.compareToIgnoreCase(((FlightDataType)o).name) == 0;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public int compareTo(FlightDataType o) {
		final int groupCompare = this.getGroup().compareTo(o.getGroup());
		if (groupCompare != 0) {
			return groupCompare;
		}

		return this.priority - o.priority;
	}
}
