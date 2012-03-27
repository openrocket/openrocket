package net.sf.openrocket.simulation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * A class defining a storable simulation variable type.  This class defined numerous ready
 * types, and allows also creating new types with any name.  When retrieving types based on
 * a name, you should use {@link #getType(String, UnitGroup)} to return the default unit type,
 * or a new type if the name does not currently exist.
 * <p>
 * Each type has a type name (description), a unit group and a priority.  The type is identified
 * purely by its name case-insensitively.  The unit group provides the units for the type.
 * The priority is used to order the types.  The pre-existing types are defined specific priority
 * numbers, and other types have a default priority number that is after all other types.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightDataType implements Comparable<FlightDataType> {
	private static final Translator trans = Application.getTranslator();
	
	/** Priority of custom-created variables */
	private static final int DEFAULT_PRIORITY = 999;
	
	/** List of existing types.  MUST BE DEFINED BEFORE ANY TYPES!! */
	private static final Map<String, FlightDataType> EXISTING_TYPES = new HashMap<String, FlightDataType>();
	
	
	
	//// Time
	public static final FlightDataType TYPE_TIME = newType(trans.get("FlightDataType.TYPE_TIME"), UnitGroup.UNITS_FLIGHT_TIME, 1);
	
	
	//// Vertical position and motion
	//// Altitude
	public static final FlightDataType TYPE_ALTITUDE = newType(trans.get("FlightDataType.TYPE_ALTITUDE"), UnitGroup.UNITS_DISTANCE, 10);
	//// Vertical velocity
	public static final FlightDataType TYPE_VELOCITY_Z = newType(trans.get("FlightDataType.TYPE_VELOCITY_Z"), UnitGroup.UNITS_VELOCITY, 11);
	//// Vertical acceleration
	public static final FlightDataType TYPE_ACCELERATION_Z = newType(trans.get("FlightDataType.TYPE_ACCELERATION_Z"), UnitGroup.UNITS_ACCELERATION, 12);
	
	
	//// Total motion
	//// Total velocity
	public static final FlightDataType TYPE_VELOCITY_TOTAL = newType(trans.get("FlightDataType.TYPE_VELOCITY_TOTAL"), UnitGroup.UNITS_VELOCITY, 20);
	//// Total acceleration
	public static final FlightDataType TYPE_ACCELERATION_TOTAL = newType(trans.get("FlightDataType.TYPE_ACCELERATION_TOTAL"), UnitGroup.UNITS_ACCELERATION, 21);
	
	
	//// Lateral position and motion
	//// Position upwind
	public static final FlightDataType TYPE_POSITION_X = newType(trans.get("FlightDataType.TYPE_POSITION_X"), UnitGroup.UNITS_DISTANCE, 30);
	//// Position parallel to wind
	public static final FlightDataType TYPE_POSITION_Y = newType(trans.get("FlightDataType.TYPE_POSITION_Y"), UnitGroup.UNITS_DISTANCE, 31);
	//// Lateral distance
	public static final FlightDataType TYPE_POSITION_XY = newType(trans.get("FlightDataType.TYPE_POSITION_XY"), UnitGroup.UNITS_DISTANCE, 32);
	//// Lateral direction
	public static final FlightDataType TYPE_POSITION_DIRECTION = newType(trans.get("FlightDataType.TYPE_POSITION_DIRECTION"), UnitGroup.UNITS_ANGLE, 33);
	//// Lateral velocity
	public static final FlightDataType TYPE_VELOCITY_XY = newType(trans.get("FlightDataType.TYPE_VELOCITY_XY"), UnitGroup.UNITS_VELOCITY, 34);
	//// Lateral acceleration
	public static final FlightDataType TYPE_ACCELERATION_XY = newType(trans.get("FlightDataType.TYPE_ACCELERATION_XY"), UnitGroup.UNITS_ACCELERATION, 35);
	//// Latitude
	public static final FlightDataType TYPE_LATITUDE = newType(trans.get("FlightDataType.TYPE_LATITUDE"), UnitGroup.UNITS_ANGLE, 36);
	//// Longitude
	public static final FlightDataType TYPE_LONGITUDE = newType(trans.get("FlightDataType.TYPE_LONGITUDE"), UnitGroup.UNITS_ANGLE, 37);
	
	//// Angular motion
	//// Angle of attack
	public static final FlightDataType TYPE_AOA = newType(trans.get("FlightDataType.TYPE_AOA"), UnitGroup.UNITS_ANGLE, 40);
	//// Roll rate
	public static final FlightDataType TYPE_ROLL_RATE = newType(trans.get("FlightDataType.TYPE_ROLL_RATE"), UnitGroup.UNITS_ROLL, 41);
	//// Pitch rate
	public static final FlightDataType TYPE_PITCH_RATE = newType(trans.get("FlightDataType.TYPE_PITCH_RATE"), UnitGroup.UNITS_ROLL, 42);
	//// Yaw rate
	public static final FlightDataType TYPE_YAW_RATE = newType(trans.get("FlightDataType.TYPE_YAW_RATE"), UnitGroup.UNITS_ROLL, 43);
	
	
	//// Stability information
	//// Mass
	public static final FlightDataType TYPE_MASS = newType(trans.get("FlightDataType.TYPE_MASS"), UnitGroup.UNITS_MASS, 50);
	//// Longitudinal moment of inertia
	public static final FlightDataType TYPE_LONGITUDINAL_INERTIA = newType(trans.get("FlightDataType.TYPE_LONGITUDINAL_INERTIA"), UnitGroup.UNITS_INERTIA, 51);
	//// Rotational moment of inertia
	public static final FlightDataType TYPE_ROTATIONAL_INERTIA = newType(trans.get("FlightDataType.TYPE_ROTATIONAL_INERTIA"), UnitGroup.UNITS_INERTIA, 52);
	//// CP location
	public static final FlightDataType TYPE_CP_LOCATION = newType(trans.get("FlightDataType.TYPE_CP_LOCATION"), UnitGroup.UNITS_LENGTH, 53);
	//// CG location
	public static final FlightDataType TYPE_CG_LOCATION = newType(trans.get("FlightDataType.TYPE_CG_LOCATION"), UnitGroup.UNITS_LENGTH, 54);
	//// Stability margin calibers
	public static final FlightDataType TYPE_STABILITY = newType(trans.get("FlightDataType.TYPE_STABILITY"), UnitGroup.UNITS_COEFFICIENT, 55);
	
	
	//// Characteristic numbers
	//// Mach number
	public static final FlightDataType TYPE_MACH_NUMBER = newType(trans.get("FlightDataType.TYPE_MACH_NUMBER"), UnitGroup.UNITS_COEFFICIENT, 60);
	//// Reynolds number
	public static final FlightDataType TYPE_REYNOLDS_NUMBER = newType(trans.get("FlightDataType.TYPE_REYNOLDS_NUMBER"), UnitGroup.UNITS_COEFFICIENT, 61);
	
	
	//// Thrust and drag
	//// Thrust
	public static final FlightDataType TYPE_THRUST_FORCE = newType(trans.get("FlightDataType.TYPE_THRUST_FORCE"), UnitGroup.UNITS_FORCE, 70);
	//// Drag force
	public static final FlightDataType TYPE_DRAG_FORCE = newType(trans.get("FlightDataType.TYPE_DRAG_FORCE"), UnitGroup.UNITS_FORCE, 71);
	//// Drag coefficient
	public static final FlightDataType TYPE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_DRAG_COEFF"), UnitGroup.UNITS_COEFFICIENT, 72);
	//// Axial drag coefficient
	public static final FlightDataType TYPE_AXIAL_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_AXIAL_DRAG_COEFF"), UnitGroup.UNITS_COEFFICIENT, 73);
	
	
	////  Component drag coefficients
	//// Friction drag coefficient
	public static final FlightDataType TYPE_FRICTION_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_FRICTION_DRAG_COEFF"), UnitGroup.UNITS_COEFFICIENT, 80);
	//// Pressure drag coefficient
	public static final FlightDataType TYPE_PRESSURE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_PRESSURE_DRAG_COEFF"), UnitGroup.UNITS_COEFFICIENT, 81);
	//// Base drag coefficient
	public static final FlightDataType TYPE_BASE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_BASE_DRAG_COEFF"), UnitGroup.UNITS_COEFFICIENT, 82);
	
	
	////  Other coefficients
	//// Normal force coefficient
	public static final FlightDataType TYPE_NORMAL_FORCE_COEFF = newType(trans.get("FlightDataType.TYPE_NORMAL_FORCE_COEFF"), UnitGroup.UNITS_COEFFICIENT, 90);
	//// Pitch moment coefficient
	public static final FlightDataType TYPE_PITCH_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_PITCH_MOMENT_COEFF"), UnitGroup.UNITS_COEFFICIENT, 91);
	//// Yaw moment coefficient
	public static final FlightDataType TYPE_YAW_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_YAW_MOMENT_COEFF"), UnitGroup.UNITS_COEFFICIENT, 92);
	//// Side force coefficient
	public static final FlightDataType TYPE_SIDE_FORCE_COEFF = newType(trans.get("FlightDataType.TYPE_SIDE_FORCE_COEFF"), UnitGroup.UNITS_COEFFICIENT, 93);
	//// Roll moment coefficient
	public static final FlightDataType TYPE_ROLL_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_MOMENT_COEFF"), UnitGroup.UNITS_COEFFICIENT, 94);
	//// Roll forcing coefficient
	public static final FlightDataType TYPE_ROLL_FORCING_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_FORCING_COEFF"), UnitGroup.UNITS_COEFFICIENT, 95);
	//// Roll damping coefficient
	public static final FlightDataType TYPE_ROLL_DAMPING_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_DAMPING_COEFF"), UnitGroup.UNITS_COEFFICIENT, 96);
	
	//// Pitch damping coefficient
	public static final FlightDataType TYPE_PITCH_DAMPING_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF"), UnitGroup.UNITS_COEFFICIENT, 97);
	//// Yaw damping coefficient
	public static final FlightDataType TYPE_YAW_DAMPING_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_YAW_DAMPING_MOMENT_COEFF"), UnitGroup.UNITS_COEFFICIENT, 98);
	
	//// Coriolis acceleration
	public static final FlightDataType TYPE_CORIOLIS_ACCELERATION = newType(trans.get("FlightDataType.TYPE_CORIOLIS_ACCELERATION"), UnitGroup.UNITS_ACCELERATION, 99);
	
	
	////  Reference length + area
	//// Reference length
	public static final FlightDataType TYPE_REFERENCE_LENGTH = newType(trans.get("FlightDataType.TYPE_REFERENCE_LENGTH"), UnitGroup.UNITS_LENGTH, 100);
	//// Reference area
	public static final FlightDataType TYPE_REFERENCE_AREA = newType(trans.get("FlightDataType.TYPE_REFERENCE_AREA"), UnitGroup.UNITS_AREA, 101);
	
	
	////  Orientation
	//// Vertical orientation (zenith)
	public static final FlightDataType TYPE_ORIENTATION_THETA = newType(trans.get("FlightDataType.TYPE_ORIENTATION_THETA"), UnitGroup.UNITS_ANGLE, 106);
	//// Lateral orientation (azimuth)
	public static final FlightDataType TYPE_ORIENTATION_PHI = newType(trans.get("FlightDataType.TYPE_ORIENTATION_PHI"), UnitGroup.UNITS_ANGLE, 107);
	
	
	////  Atmospheric conditions
	//// Wind velocity
	public static final FlightDataType TYPE_WIND_VELOCITY = newType(trans.get("FlightDataType.TYPE_WIND_VELOCITY"), UnitGroup.UNITS_VELOCITY, 110);
	//// Air temperature
	public static final FlightDataType TYPE_AIR_TEMPERATURE = newType(trans.get("FlightDataType.TYPE_AIR_TEMPERATURE"), UnitGroup.UNITS_TEMPERATURE, 111);
	//// Air pressure
	public static final FlightDataType TYPE_AIR_PRESSURE = newType(trans.get("FlightDataType.TYPE_AIR_PRESSURE"), UnitGroup.UNITS_PRESSURE, 112);
	//// Speed of sound
	public static final FlightDataType TYPE_SPEED_OF_SOUND = newType(trans.get("FlightDataType.TYPE_SPEED_OF_SOUND"), UnitGroup.UNITS_VELOCITY, 113);
	
	////  Simulation information
	//// Simulation time step
	public static final FlightDataType TYPE_TIME_STEP = newType(trans.get("FlightDataType.TYPE_TIME_STEP"), UnitGroup.UNITS_TIME_STEP, 200);
	//// Computation time
	public static final FlightDataType TYPE_COMPUTATION_TIME = newType(trans.get("FlightDataType.TYPE_COMPUTATION_TIME"), UnitGroup.UNITS_SHORT_TIME, 201);
	
	
	
	/**
	 * Return a {@link FlightDataType} based on a string description.  This returns known data types
	 * if possible, or a new type otherwise.
	 * 
	 * @param s		the string description of the type.
	 * @param u		the unit group the new type should belong to if a new group is created.
	 * @return		a data type.
	 */
	public static synchronized FlightDataType getType(String s, UnitGroup u) {
		FlightDataType type = EXISTING_TYPES.get(s.toLowerCase(Locale.ENGLISH));
		if (type != null) {
			return type;
		}
		type = newType(s, u, DEFAULT_PRIORITY);
		return type;
	}
	
	/**
	 * Used while initializing the class.
	 */
	private static synchronized FlightDataType newType(String s, UnitGroup u, int priority) {
		FlightDataType type = new FlightDataType(s, u, priority);
		EXISTING_TYPES.put(s.toLowerCase(Locale.ENGLISH), type);
		return type;
	}
	
	
	private final String name;
	private final UnitGroup units;
	private final int priority;
	private final int hashCode;
	
	
	private FlightDataType(String typeName, UnitGroup units, int priority) {
		if (typeName == null)
			throw new IllegalArgumentException("typeName is null");
		if (units == null)
			throw new IllegalArgumentException("units is null");
		this.name = typeName;
		this.units = units;
		this.priority = priority;
		this.hashCode = this.name.toLowerCase(Locale.ENGLISH).hashCode();
	}
	
	
	
	
	public String getName() {
		return name;
	}
	
	public UnitGroup getUnitGroup() {
		return units;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FlightDataType))
			return false;
		return this.name.equalsIgnoreCase(((FlightDataType) other).name);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public int compareTo(FlightDataType o) {
		if (this.priority != o.priority)
			return this.priority - o.priority;
		return this.name.compareToIgnoreCase(o.name);
	}
}