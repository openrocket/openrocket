package net.sf.openrocket.simulation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.StringUtil;

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
	private static final Logger log = LoggerFactory.getLogger(FlightDataType.class);
	
	/** Priority of custom-created variables */
	private static final int DEFAULT_PRIORITY = 999;
	
	/** List of existing types.  MUST BE DEFINED BEFORE ANY TYPES!! */
	/** NOTE: The String key here is now the symbol */
	private static final Map<String, FlightDataType> EXISTING_TYPES = new HashMap<String, FlightDataType>();
	
	
	//// Time
	public static final FlightDataType TYPE_TIME = newType(trans.get("FlightDataType.TYPE_TIME"), "t", UnitGroup.UNITS_FLIGHT_TIME, 1);
	
	//// Vertical position and motion
	//// Altitude
	public static final FlightDataType TYPE_ALTITUDE = newType(trans.get("FlightDataType.TYPE_ALTITUDE"), "h", UnitGroup.UNITS_DISTANCE, 10);
	//// Vertical velocity
	public static final FlightDataType TYPE_VELOCITY_Z = newType(trans.get("FlightDataType.TYPE_VELOCITY_Z"), "Vz", UnitGroup.UNITS_VELOCITY, 11);
	//// Vertical acceleration
	public static final FlightDataType TYPE_ACCELERATION_Z = newType(trans.get("FlightDataType.TYPE_ACCELERATION_Z"), "Az", UnitGroup.UNITS_ACCELERATION, 12);
	
	
	//// Total motion
	//// Total velocity
	public static final FlightDataType TYPE_VELOCITY_TOTAL = newType(trans.get("FlightDataType.TYPE_VELOCITY_TOTAL"), "Vt", UnitGroup.UNITS_VELOCITY, 20);
	//// Total acceleration
	public static final FlightDataType TYPE_ACCELERATION_TOTAL = newType(trans.get("FlightDataType.TYPE_ACCELERATION_TOTAL"), "At", UnitGroup.UNITS_ACCELERATION, 21);
	
	
	//// Lateral position and motion
	//// Position upwind
	public static final FlightDataType TYPE_POSITION_X = newType(trans.get("FlightDataType.TYPE_POSITION_X"), "Px", UnitGroup.UNITS_DISTANCE, 30);
	//// Position parallel to wind
	public static final FlightDataType TYPE_POSITION_Y = newType(trans.get("FlightDataType.TYPE_POSITION_Y"), "Py", UnitGroup.UNITS_DISTANCE, 31);
	//// Lateral distance
	public static final FlightDataType TYPE_POSITION_XY = newType(trans.get("FlightDataType.TYPE_POSITION_XY"), "Pl", UnitGroup.UNITS_DISTANCE, 32);
	//// Lateral direction
	public static final FlightDataType TYPE_POSITION_DIRECTION = newType(trans.get("FlightDataType.TYPE_POSITION_DIRECTION"), "\u03b8l", UnitGroup.UNITS_ANGLE, 33);
	//// Lateral velocity
	public static final FlightDataType TYPE_VELOCITY_XY = newType(trans.get("FlightDataType.TYPE_VELOCITY_XY"), "Vl", UnitGroup.UNITS_VELOCITY, 34);
	//// Lateral acceleration
	public static final FlightDataType TYPE_ACCELERATION_XY = newType(trans.get("FlightDataType.TYPE_ACCELERATION_XY"), "Al", UnitGroup.UNITS_ACCELERATION, 35);
	//// Latitude
	public static final FlightDataType TYPE_LATITUDE = newType(trans.get("FlightDataType.TYPE_LATITUDE"), "\u03c6", UnitGroup.UNITS_ANGLE, 36);
	//// Longitude
	public static final FlightDataType TYPE_LONGITUDE = newType(trans.get("FlightDataType.TYPE_LONGITUDE"), "\u03bb", UnitGroup.UNITS_ANGLE, 37);
	
	//// Gravity
	public static final FlightDataType TYPE_GRAVITY = newType(trans.get("FlightDataType.TYPE_GRAVITY"), "g", UnitGroup.UNITS_ACCELERATION, 38);
	
	//// Angular motion
	//// Angle of attack
	public static final FlightDataType TYPE_AOA = newType(trans.get("FlightDataType.TYPE_AOA"), "\u03b1", UnitGroup.UNITS_ANGLE, 40);
	//// Roll rate
	public static final FlightDataType TYPE_ROLL_RATE = newType(trans.get("FlightDataType.TYPE_ROLL_RATE"), "d\u03a6", UnitGroup.UNITS_ROLL, 41);
	//// Pitch rate
	public static final FlightDataType TYPE_PITCH_RATE = newType(trans.get("FlightDataType.TYPE_PITCH_RATE"), "d\u03b8", UnitGroup.UNITS_ROLL, 42);
	//// Yaw rate
	public static final FlightDataType TYPE_YAW_RATE = newType(trans.get("FlightDataType.TYPE_YAW_RATE"), "d\u03a8", UnitGroup.UNITS_ROLL, 43);
	
	
	//// Stability information
	//// Mass
	public static final FlightDataType TYPE_MASS = newType(trans.get("FlightDataType.TYPE_MASS"), "m", UnitGroup.UNITS_MASS, 50);
	//// Propellant mass
	public static final FlightDataType TYPE_PROPELLANT_MASS = newType(trans.get("FlightDataType.TYPE_PROPELLANT_MASS"), "mp", UnitGroup.UNITS_MASS, 51);
	//// Longitudinal moment of inertia
	public static final FlightDataType TYPE_LONGITUDINAL_INERTIA = newType(trans.get("FlightDataType.TYPE_LONGITUDINAL_INERTIA"), "Il", UnitGroup.UNITS_INERTIA, 52);
	//// Rotational moment of inertia
	public static final FlightDataType TYPE_ROTATIONAL_INERTIA = newType(trans.get("FlightDataType.TYPE_ROTATIONAL_INERTIA"), "Ir", UnitGroup.UNITS_INERTIA, 53);
	//// CP location
	public static final FlightDataType TYPE_CP_LOCATION = newType(trans.get("FlightDataType.TYPE_CP_LOCATION"), "Cp", UnitGroup.UNITS_LENGTH, 54);
	//// CG location
	public static final FlightDataType TYPE_CG_LOCATION = newType(trans.get("FlightDataType.TYPE_CG_LOCATION"), "Cg", UnitGroup.UNITS_LENGTH, 55);
	//// Stability margin calibers
	public static final FlightDataType TYPE_STABILITY = newType(trans.get("FlightDataType.TYPE_STABILITY"), "S", UnitGroup.UNITS_COEFFICIENT, 56);
	
	
	//// Characteristic numbers
	//// Mach number
	public static final FlightDataType TYPE_MACH_NUMBER = newType(trans.get("FlightDataType.TYPE_MACH_NUMBER"), "M", UnitGroup.UNITS_COEFFICIENT, 60);
	//// Reynolds number
	public static final FlightDataType TYPE_REYNOLDS_NUMBER = newType(trans.get("FlightDataType.TYPE_REYNOLDS_NUMBER"), "R", UnitGroup.UNITS_COEFFICIENT, 61);
	
	
	//// Thrust and drag
	//// Thrust
	public static final FlightDataType TYPE_THRUST_FORCE = newType(trans.get("FlightDataType.TYPE_THRUST_FORCE"), "Ft", UnitGroup.UNITS_FORCE, 70);
	//// Drag force
	public static final FlightDataType TYPE_DRAG_FORCE = newType(trans.get("FlightDataType.TYPE_DRAG_FORCE"), "Fd", UnitGroup.UNITS_FORCE, 71);
	//// Drag coefficient
	public static final FlightDataType TYPE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_DRAG_COEFF"), "Cd", UnitGroup.UNITS_COEFFICIENT, 72);
	//// Axial drag coefficient
	public static final FlightDataType TYPE_AXIAL_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_AXIAL_DRAG_COEFF"), "Cda", UnitGroup.UNITS_COEFFICIENT, 73);
	
	
	////  Component drag coefficients
	//// Friction drag coefficient
	public static final FlightDataType TYPE_FRICTION_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_FRICTION_DRAG_COEFF"), "Cdf", UnitGroup.UNITS_COEFFICIENT, 80);
	//// Pressure drag coefficient
	public static final FlightDataType TYPE_PRESSURE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_PRESSURE_DRAG_COEFF"), "Cdp", UnitGroup.UNITS_COEFFICIENT, 81);
	//// Base drag coefficient
	public static final FlightDataType TYPE_BASE_DRAG_COEFF = newType(trans.get("FlightDataType.TYPE_BASE_DRAG_COEFF"), "Cdb", UnitGroup.UNITS_COEFFICIENT, 82);
	
	
	////  Other coefficients
	//// Normal force coefficient
	public static final FlightDataType TYPE_NORMAL_FORCE_COEFF = newType(trans.get("FlightDataType.TYPE_NORMAL_FORCE_COEFF"), "Cn", UnitGroup.UNITS_COEFFICIENT, 90);
	//// Pitch moment coefficient
	public static final FlightDataType TYPE_PITCH_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_PITCH_MOMENT_COEFF"), "C\u03b8", UnitGroup.UNITS_COEFFICIENT, 91);
	//// Yaw moment coefficient
	public static final FlightDataType TYPE_YAW_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_YAW_MOMENT_COEFF"), "C\u03c4\u03a8", UnitGroup.UNITS_COEFFICIENT, 92);
	//// Side force coefficient
	public static final FlightDataType TYPE_SIDE_FORCE_COEFF = newType(trans.get("FlightDataType.TYPE_SIDE_FORCE_COEFF"), "C\u03c4s", UnitGroup.UNITS_COEFFICIENT, 93);
	//// Roll moment coefficient
	public static final FlightDataType TYPE_ROLL_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_MOMENT_COEFF"), "C\u03c4\u03a6", UnitGroup.UNITS_COEFFICIENT, 94);
	//// Roll forcing coefficient
	public static final FlightDataType TYPE_ROLL_FORCING_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_FORCING_COEFF"), "Cf\u03a6", UnitGroup.UNITS_COEFFICIENT, 95);
	//// Roll damping coefficient
	public static final FlightDataType TYPE_ROLL_DAMPING_COEFF = newType(trans.get("FlightDataType.TYPE_ROLL_DAMPING_COEFF"), "C\u03b6\u03a6", UnitGroup.UNITS_COEFFICIENT, 96);
	
	//// Pitch damping coefficient
	public static final FlightDataType TYPE_PITCH_DAMPING_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF"), "C\u03b6\u03b8", UnitGroup.UNITS_COEFFICIENT, 97);
	//// Yaw damping coefficient
	public static final FlightDataType TYPE_YAW_DAMPING_MOMENT_COEFF = newType(trans.get("FlightDataType.TYPE_YAW_DAMPING_MOMENT_COEFF"), "C\u03b6\u03a8", UnitGroup.UNITS_COEFFICIENT, 98);
	
	//// Coriolis acceleration
	public static final FlightDataType TYPE_CORIOLIS_ACCELERATION = newType(trans.get("FlightDataType.TYPE_CORIOLIS_ACCELERATION"), "Ac", UnitGroup.UNITS_ACCELERATION, 99);
	
	
	////  Reference length + area
	//// Reference length
	public static final FlightDataType TYPE_REFERENCE_LENGTH = newType(trans.get("FlightDataType.TYPE_REFERENCE_LENGTH"), "Lr", UnitGroup.UNITS_LENGTH, 100);
	//// Reference area
	public static final FlightDataType TYPE_REFERENCE_AREA = newType(trans.get("FlightDataType.TYPE_REFERENCE_AREA"), "Ar", UnitGroup.UNITS_AREA, 101);
	
	
	////  Orientation
	//// Vertical orientation (zenith)
	public static final FlightDataType TYPE_ORIENTATION_THETA = newType(trans.get("FlightDataType.TYPE_ORIENTATION_THETA"), "\u0398", UnitGroup.UNITS_ANGLE, 106);
	//// Lateral orientation (azimuth)
	public static final FlightDataType TYPE_ORIENTATION_PHI = newType(trans.get("FlightDataType.TYPE_ORIENTATION_PHI"), "\u03a6", UnitGroup.UNITS_ANGLE, 107);
	
	
	////  Atmospheric conditions
	//// Wind velocity
	public static final FlightDataType TYPE_WIND_VELOCITY = newType(trans.get("FlightDataType.TYPE_WIND_VELOCITY"), "Vw", UnitGroup.UNITS_VELOCITY, 110);
	//// Air temperature
	public static final FlightDataType TYPE_AIR_TEMPERATURE = newType(trans.get("FlightDataType.TYPE_AIR_TEMPERATURE"), "T", UnitGroup.UNITS_TEMPERATURE, 111);
	//// Air pressure
	public static final FlightDataType TYPE_AIR_PRESSURE = newType(trans.get("FlightDataType.TYPE_AIR_PRESSURE"), "P", UnitGroup.UNITS_PRESSURE, 112);
	//// Speed of sound
	public static final FlightDataType TYPE_SPEED_OF_SOUND = newType(trans.get("FlightDataType.TYPE_SPEED_OF_SOUND"), "Vs", UnitGroup.UNITS_VELOCITY, 113);
	
	////  Simulation information
	//// Simulation time step
	public static final FlightDataType TYPE_TIME_STEP = newType(trans.get("FlightDataType.TYPE_TIME_STEP"), "dt", UnitGroup.UNITS_TIME_STEP, 200);
	//// Computation time
	public static final FlightDataType TYPE_COMPUTATION_TIME = newType(trans.get("FlightDataType.TYPE_COMPUTATION_TIME"), "tc", UnitGroup.UNITS_SHORT_TIME, 201);	
	
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
		TYPE_GRAVITY,
		TYPE_AOA,
		TYPE_ROLL_RATE,
		TYPE_PITCH_RATE,
		TYPE_YAW_RATE,
		TYPE_MASS,
		TYPE_PROPELLANT_MASS,
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
	 * Return a {@link FlightDataType} with a given string description, symbol and unitgroup.
	 * This returns an existing data type if the symbol matches that of an existing type. 
	 * 
	 * If the symbol matches but the unit and description information differ, then the old stored datatype
	 * is erased and the updated version based on the given parametes is returned.
	 * The only exception is if the description or unitgroup are undefined (null or empty string). In this case 
	 * we just get these parameters from the existing type when making the new one.
	 * 
	 * @param s		the string description of the type.
	 * @param u		the unit group the new type should belong to if a new group is created.
	 * @return		a data type.
	 */
	public static synchronized FlightDataType getType(String s, String symbol, UnitGroup u) {

		// if symbol is null : try finding by name
		// if unit is null : don't do anything to the unit if found, just return datatype if found and generate an error and an empty unit otherwise
		int oldPriority = DEFAULT_PRIORITY;
		
		//FlightDataType type = findFromSymbol(symbol);
		FlightDataType type = EXISTING_TYPES.get(symbol);
		
		if (type != null) {
			// found it from symbol
			
			// if name was not given (empty string), can use the one we found
			if ( s == null || StringUtil.isEmpty(s)){
				s = type.getName();
			}
			if ( u == null ){
				u = type.getUnitGroup();
			}
			
			// if something has changed, then we need to remove the old one
			// otherwise, just return what we found
			if ( !u.equals(type.getUnitGroup()) )
			   {
				oldPriority = type.priority;
				EXISTING_TYPES.remove(type);
				log.info("Unitgroup of type "+type.getName() + 
						 ", has changed from "+type.getUnitGroup().toString() + 
						 " to "+u.toString() +
						 ". Removing old version.");
			}
			else if (!s.equals(type.getName())) {
				oldPriority = type.priority;
				EXISTING_TYPES.remove(type);
				log.info("Name of type "+type.getName()+", has changed to "+s+". Removing old version.");
			}
			else{
				return type;
			}
		}
		
		if (u == null){
			u = UnitGroup.UNITS_NONE;
			log.error("Made a new flightdatatype, but did not know what units to use.");
		}
		
		// make a new one
		type = newType(s, symbol, u, oldPriority);
		return type;	
	}
	
	/*
	 * Get the flightdatatype from existing types based on the symbol.
	 */
	/*
	private static FlightDataType findFromSymbol(String symbol){
		for (FlightDataType t : EXISTING_TYPES.values()){
			if (t.getSymbol().equals(symbol)){
				return t;
			}
		}
		return null;
	}
	*/
	
	/**
	 * Used while initializing the class.
	 */
	private static synchronized FlightDataType newType(String s, String symbol, UnitGroup u, int priority) {
		FlightDataType type = new FlightDataType(s, symbol, u, priority);
		//EXISTING_TYPES.put(s.toLowerCase(Locale.ENGLISH), type);
		EXISTING_TYPES.put(symbol, type);
		return type;
	}
	
	
	private final String name;
	private final String symbol;
	private final UnitGroup units;
	private final int priority;
	private final int hashCode;
	
	
	private FlightDataType(String typeName, String symbol, UnitGroup units, int priority) {
		if (typeName == null)
			throw new IllegalArgumentException("typeName is null");
		if (units == null)
			throw new IllegalArgumentException("units is null");
		this.name = typeName;
		this.symbol = symbol;
		this.units = units;
		this.priority = priority;
		this.hashCode = this.name.toLowerCase(Locale.ENGLISH).hashCode();
	}
	
	/*
	public void setPriority(int p){
		this.priority = p;
	}
	*/
	
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
