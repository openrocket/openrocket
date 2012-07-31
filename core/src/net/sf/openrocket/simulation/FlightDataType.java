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
	public static final FlightDataType TYPE_TIME = newType("TYPE_TIME", "t", UnitGroup.UNITS_FLIGHT_TIME, 1);
	
	//// Vertical position and motion
	//// Altitude
	public static final FlightDataType TYPE_ALTITUDE = newType("TYPE_ALTITUDE", "h", UnitGroup.UNITS_DISTANCE, 10);
	//// Vertical velocity
	public static final FlightDataType TYPE_VELOCITY_Z = newType("TYPE_VELOCITY_Z", "Vz", UnitGroup.UNITS_VELOCITY, 11);
	//// Vertical acceleration
	public static final FlightDataType TYPE_ACCELERATION_Z = newType("TYPE_ACCELERATION_Z", "Az", UnitGroup.UNITS_ACCELERATION, 12);
	
	
	//// Total motion
	//// Total velocity
	public static final FlightDataType TYPE_VELOCITY_TOTAL = newType("TYPE_VELOCITY_TOTAL", "Vt", UnitGroup.UNITS_VELOCITY, 20);
	//// Total acceleration
	public static final FlightDataType TYPE_ACCELERATION_TOTAL = newType("TYPE_ACCELERATION_TOTAL", "At", UnitGroup.UNITS_ACCELERATION, 21);
	
	
	//// Lateral position and motion
	//// Position upwind
	public static final FlightDataType TYPE_POSITION_X = newType("TYPE_POSITION_X", "Px", UnitGroup.UNITS_DISTANCE, 30);
	//// Position parallel to wind
	public static final FlightDataType TYPE_POSITION_Y = newType("TYPE_POSITION_Y", "Py", UnitGroup.UNITS_DISTANCE, 31);
	//// Lateral distance
	public static final FlightDataType TYPE_POSITION_XY = newType("TYPE_POSITION_XY", "Pl", UnitGroup.UNITS_DISTANCE, 32);
	//// Lateral direction
	public static final FlightDataType TYPE_POSITION_DIRECTION = newType("TYPE_POSITION_DIRECTION", "\u03b8l", UnitGroup.UNITS_ANGLE, 33);
	//// Lateral velocity
	public static final FlightDataType TYPE_VELOCITY_XY = newType("TYPE_VELOCITY_XY", "Vl", UnitGroup.UNITS_VELOCITY, 34);
	//// Lateral acceleration
	public static final FlightDataType TYPE_ACCELERATION_XY = newType("TYPE_ACCELERATION_XY", "Al", UnitGroup.UNITS_ACCELERATION, 35);
	//// Latitude
	public static final FlightDataType TYPE_LATITUDE = newType("TYPE_LATITUDE", "\u03c6", UnitGroup.UNITS_ANGLE, 36);
	//// Longitude
	public static final FlightDataType TYPE_LONGITUDE = newType("TYPE_LONGITUDE", "\u03bb", UnitGroup.UNITS_ANGLE, 37);
	
	//// Angular motion
	//// Angle of attack
	public static final FlightDataType TYPE_AOA = newType("TYPE_AOA", "\u03b1", UnitGroup.UNITS_ANGLE, 40);
	//// Roll rate
	public static final FlightDataType TYPE_ROLL_RATE = newType("TYPE_ROLL_RATE", "d\u03a6", UnitGroup.UNITS_ROLL, 41);
	//// Pitch rate
	public static final FlightDataType TYPE_PITCH_RATE = newType("TYPE_PITCH_RATE", "d\u03b8", UnitGroup.UNITS_ROLL, 42);
	//// Yaw rate
	public static final FlightDataType TYPE_YAW_RATE = newType("TYPE_YAW_RATE", "d\u03a8", UnitGroup.UNITS_ROLL, 43);
	
	
	//// Stability information
	//// Mass
	public static final FlightDataType TYPE_MASS = newType("TYPE_MASS", "m", UnitGroup.UNITS_MASS, 50);
	//// Longitudinal moment of inertia
	public static final FlightDataType TYPE_LONGITUDINAL_INERTIA = newType("TYPE_LONGITUDINAL_INERTIA", "Il", UnitGroup.UNITS_INERTIA, 51);
	//// Rotational moment of inertia
	public static final FlightDataType TYPE_ROTATIONAL_INERTIA = newType("TYPE_ROTATIONAL_INERTIA", "Ir", UnitGroup.UNITS_INERTIA, 52);
	//// CP location
	public static final FlightDataType TYPE_CP_LOCATION = newType("TYPE_CP_LOCATION", "Cp", UnitGroup.UNITS_LENGTH, 53);
	//// CG location
	public static final FlightDataType TYPE_CG_LOCATION = newType("TYPE_CG_LOCATION", "Cg", UnitGroup.UNITS_LENGTH, 54);
	//// Stability margin calibers
	public static final FlightDataType TYPE_STABILITY = newType("TYPE_STABILITY", "S", UnitGroup.UNITS_COEFFICIENT, 55);
	
	
	//// Characteristic numbers
	//// Mach number
	public static final FlightDataType TYPE_MACH_NUMBER = newType("TYPE_MACH_NUMBER", "M", UnitGroup.UNITS_COEFFICIENT, 60);
	//// Reynolds number
	public static final FlightDataType TYPE_REYNOLDS_NUMBER = newType("TYPE_REYNOLDS_NUMBER", "R", UnitGroup.UNITS_COEFFICIENT, 61);
	
	
	//// Thrust and drag
	//// Thrust
	public static final FlightDataType TYPE_THRUST_FORCE = newType("TYPE_THRUST_FORCE", "Ft", UnitGroup.UNITS_FORCE, 70);
	//// Drag force
	public static final FlightDataType TYPE_DRAG_FORCE = newType("TYPE_DRAG_FORCE", "Fd", UnitGroup.UNITS_FORCE, 71);
	//// Drag coefficient
	public static final FlightDataType TYPE_DRAG_COEFF = newType("TYPE_DRAG_COEFF", "Cd", UnitGroup.UNITS_COEFFICIENT, 72);
	//// Axial drag coefficient
	public static final FlightDataType TYPE_AXIAL_DRAG_COEFF = newType("TYPE_AXIAL_DRAG_COEFF", "Cda", UnitGroup.UNITS_COEFFICIENT, 73);
	
	
	////  Component drag coefficients
	//// Friction drag coefficient
	public static final FlightDataType TYPE_FRICTION_DRAG_COEFF = newType("TYPE_FRICTION_DRAG_COEFF", "Cdf", UnitGroup.UNITS_COEFFICIENT, 80);
	//// Pressure drag coefficient
	public static final FlightDataType TYPE_PRESSURE_DRAG_COEFF = newType("TYPE_PRESSURE_DRAG_COEFF", "Cdp", UnitGroup.UNITS_COEFFICIENT, 81);
	//// Base drag coefficient
	public static final FlightDataType TYPE_BASE_DRAG_COEFF = newType("TYPE_BASE_DRAG_COEFF", "Cdb", UnitGroup.UNITS_COEFFICIENT, 82);
	
	
	////  Other coefficients
	//// Normal force coefficient
	public static final FlightDataType TYPE_NORMAL_FORCE_COEFF = newType("TYPE_NORMAL_FORCE_COEFF", "Cn", UnitGroup.UNITS_COEFFICIENT, 90);
	//// Pitch moment coefficient
	public static final FlightDataType TYPE_PITCH_MOMENT_COEFF = newType("TYPE_PITCH_MOMENT_COEFF", "C\u03b8", UnitGroup.UNITS_COEFFICIENT, 91);
	//// Yaw moment coefficient
	public static final FlightDataType TYPE_YAW_MOMENT_COEFF = newType("TYPE_YAW_MOMENT_COEFF", "C\u03c4\u03a8", UnitGroup.UNITS_COEFFICIENT, 92);
	//// Side force coefficient
	public static final FlightDataType TYPE_SIDE_FORCE_COEFF = newType("TYPE_SIDE_FORCE_COEFF", "C\u03c4s", UnitGroup.UNITS_COEFFICIENT, 93);
	//// Roll moment coefficient
	public static final FlightDataType TYPE_ROLL_MOMENT_COEFF = newType("TYPE_ROLL_MOMENT_COEFF", "C\u03c4\u03a6", UnitGroup.UNITS_COEFFICIENT, 94);
	//// Roll forcing coefficient
	public static final FlightDataType TYPE_ROLL_FORCING_COEFF = newType("TYPE_ROLL_FORCING_COEFF", "Cf\u03a6", UnitGroup.UNITS_COEFFICIENT, 95);
	//// Roll damping coefficient
	public static final FlightDataType TYPE_ROLL_DAMPING_COEFF = newType("TYPE_ROLL_DAMPING_COEFF", "C\u03b6\u03a6", UnitGroup.UNITS_COEFFICIENT, 96);
	
	//// Pitch damping coefficient
	public static final FlightDataType TYPE_PITCH_DAMPING_MOMENT_COEFF = newType("TYPE_PITCH_DAMPING_MOMENT_COEFF", "C\u03b6\u03b8", UnitGroup.UNITS_COEFFICIENT, 97);
	//// Yaw damping coefficient
	public static final FlightDataType TYPE_YAW_DAMPING_MOMENT_COEFF = newType("TYPE_YAW_DAMPING_MOMENT_COEFF", "C\u03b6\u03a8", UnitGroup.UNITS_COEFFICIENT, 98);
	
	//// Coriolis acceleration
	public static final FlightDataType TYPE_CORIOLIS_ACCELERATION = newType("TYPE_CORIOLIS_ACCELERATION", "Ac", UnitGroup.UNITS_ACCELERATION, 99);
	
	
	////  Reference length + area
	//// Reference length
	public static final FlightDataType TYPE_REFERENCE_LENGTH = newType("TYPE_REFERENCE_LENGTH", "Lr", UnitGroup.UNITS_LENGTH, 100);
	//// Reference area
	public static final FlightDataType TYPE_REFERENCE_AREA = newType("TYPE_REFERENCE_AREA", "Ar", UnitGroup.UNITS_AREA, 101);
	
	
	////  Orientation
	//// Vertical orientation (zenith)
	public static final FlightDataType TYPE_ORIENTATION_THETA = newType("TYPE_ORIENTATION_THETA", "\u0398", UnitGroup.UNITS_ANGLE, 106);
	//// Lateral orientation (azimuth)
	public static final FlightDataType TYPE_ORIENTATION_PHI = newType("TYPE_ORIENTATION_PHI", "\u03a6", UnitGroup.UNITS_ANGLE, 107);
	
	
	////  Atmospheric conditions
	//// Wind velocity
	public static final FlightDataType TYPE_WIND_VELOCITY = newType("TYPE_WIND_VELOCITY", "Vw", UnitGroup.UNITS_VELOCITY, 110);
	//// Air temperature
	public static final FlightDataType TYPE_AIR_TEMPERATURE = newType("TYPE_AIR_TEMPERATURE", "T", UnitGroup.UNITS_TEMPERATURE, 111);
	//// Air pressure
	public static final FlightDataType TYPE_AIR_PRESSURE = newType("TYPE_AIR_PRESSURE", "p", UnitGroup.UNITS_PRESSURE, 112);
	//// Speed of sound
	public static final FlightDataType TYPE_SPEED_OF_SOUND = newType("TYPE_SPEED_OF_SOUND", "Vs", UnitGroup.UNITS_VELOCITY, 113);
	
	////  Simulation information
	//// Simulation time step
	public static final FlightDataType TYPE_TIME_STEP = newType("TYPE_TIME_STEP", "dt", UnitGroup.UNITS_TIME_STEP, 200);
	//// Computation time
	public static final FlightDataType TYPE_COMPUTATION_TIME = newType("TYPE_COMPUTATION_TIME", "tc", UnitGroup.UNITS_SHORT_TIME, 201);	
	
	// An array of all the built in types
	public static final FlightDataType[] ALL_TYPES = { 
		TYPE_TIME,
		TYPE_ALTITUDE ,
		TYPE_VELOCITY_Z ,
		TYPE_ACCELERATION_Z, 
		TYPE_VELOCITY_TOTAL, 
		TYPE_ACCELERATION_TOTAL, 
		TYPE_POSITION_X, 
		TYPE_POSITION_Y,  
		TYPE_POSITION_XY, 
		TYPE_POSITION_DIRECTION, 
		TYPE_VELOCITY_XY, 
		TYPE_ACCELERATION_XY, 
		TYPE_LATITUDE, 
		TYPE_LONGITUDE, 
		TYPE_AOA,
		TYPE_ROLL_RATE,
		TYPE_PITCH_RATE,
		TYPE_YAW_RATE,
		TYPE_MASS,
		TYPE_LONGITUDINAL_INERTIA,
		TYPE_ROTATIONAL_INERTIA,
		TYPE_CP_LOCATION,
		TYPE_CG_LOCATION,
		TYPE_STABILITY,
		TYPE_MACH_NUMBER,
		TYPE_REYNOLDS_NUMBER,
		TYPE_THRUST_FORCE,
		TYPE_DRAG_FORCE,
		TYPE_DRAG_COEFF,
		TYPE_AXIAL_DRAG_COEFF,
		TYPE_FRICTION_DRAG_COEFF,
		TYPE_PRESSURE_DRAG_COEFF,
		TYPE_BASE_DRAG_COEFF,
		TYPE_NORMAL_FORCE_COEFF,
		TYPE_PITCH_MOMENT_COEFF,
		TYPE_YAW_MOMENT_COEFF,
		TYPE_SIDE_FORCE_COEFF,
		TYPE_ROLL_MOMENT_COEFF,
		TYPE_ROLL_FORCING_COEFF,
		TYPE_ROLL_DAMPING_COEFF,
		TYPE_PITCH_DAMPING_MOMENT_COEFF,
		TYPE_YAW_DAMPING_MOMENT_COEFF,
		TYPE_CORIOLIS_ACCELERATION,
		TYPE_REFERENCE_LENGTH,
		TYPE_REFERENCE_AREA,
		TYPE_ORIENTATION_THETA,
		TYPE_ORIENTATION_PHI,
		TYPE_WIND_VELOCITY,
		TYPE_AIR_TEMPERATURE,
		TYPE_AIR_PRESSURE,
		TYPE_SPEED_OF_SOUND,
		TYPE_TIME_STEP,
		TYPE_COMPUTATION_TIME
		};
	
	/**
	 * Return a {@link FlightDataType} based on a string description.  This returns known data types
	 * if possible, or a new type otherwise.
	 * 
	 * @param s		the string description of the type.
	 * @param u		the unit group the new type should belong to if a new group is created.
	 * @return		a data type.
	 */
	public static synchronized FlightDataType getType(String s, String symbol, UnitGroup u) {
		// modified to include the unit
		FlightDataType type = EXISTING_TYPES.get(s.toLowerCase(Locale.ENGLISH));
		
		// added this for backward compatibility. Will update type if symbol undefined
		//if (type != null && type.getSymbol() != symbol){
		//	EXISTING_TYPES.remove(type);
		//	type = null;
		//}
		
		if (type != null) {
			return type;
		}
		type = newType("UserDefined." + s, s, symbol, u, DEFAULT_PRIORITY);
		return type;
	}
	
	/**
	 * Used while initializing the class.
	 */

	private static FlightDataType newType( String key , String symbol, UnitGroup u, int priority ) {
		String name = trans.get("FlightDataType." + key );
		return newType( key, name, symbol, u, priority );
	}
	
	private static synchronized FlightDataType newType(String key, String s, String symbol, UnitGroup u, int priority) {
		FlightDataType type = new FlightDataType(key, s, symbol, u, priority);
		EXISTING_TYPES.put(s.toLowerCase(Locale.ENGLISH), type);
		return type;
	}
	
	private final String key;
	private final String name;
	private final String symbol;
	private final UnitGroup units;
	private final int priority;
	private final int hashCode;
	
	
	private FlightDataType(String key, String typeName, String symbol, UnitGroup units, int priority) {
		this.key = key;
		if (typeName == null)
			throw new IllegalArgumentException("typeName is null");
		if (units == null)
			throw new IllegalArgumentException("units is null");
		this.name = typeName;
		this.symbol = symbol;
		this.units = units;
		this.priority = priority;
		this.hashCode = this.key.hashCode();
	}
	
	/*
	public void setPriority(int p){
		this.priority = p;
	}
	*/
	
	public String getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSymbol(){
		return symbol;
	}
	
	public UnitGroup getUnitGroup() {
		return units;
	}
	
	@Override
	public String toString() {
		return name; //+" ("+symbol+") "+units.getDefaultUnit().toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FlightDataType))
			return false;
		return this.hashCode == other.hashCode();
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