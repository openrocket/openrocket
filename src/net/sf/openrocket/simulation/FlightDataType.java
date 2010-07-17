package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.unit.UnitGroup;

/**
 * A class defining a storable simulation variable type.  This class defined numerous ready
 * types, and allows also creating new types with any name.  When retrieving types based on
 * a name, you should use {@link #getType(String, UnitGroup)} to return the default unit type,
 * or a new type if the name does not currently exist.
 * <p>
 * Each type has a type name (description) and a unit group.  The type is identified purely by its name.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightDataType implements Comparable<FlightDataType> {
	
	/** Priority of custom-created variables */
	private static final int DEFAULT_PRIORITY = 999;
	
	/** List of existing types.  MUST BE DEFINED BEFORE ANY TYPES!! */
	private static final List<FlightDataType> EXISTING_TYPES = new ArrayList<FlightDataType>();
	


	//// Time
	public static final FlightDataType TYPE_TIME =
			newType("Time", UnitGroup.UNITS_FLIGHT_TIME, 1);
	

	//// Vertical position and motion
	public static final FlightDataType TYPE_ALTITUDE =
			newType("Altitude", UnitGroup.UNITS_DISTANCE, 10);
	public static final FlightDataType TYPE_VELOCITY_Z =
			newType("Vertical velocity", UnitGroup.UNITS_VELOCITY, 11);
	public static final FlightDataType TYPE_ACCELERATION_Z =
			newType("Vertical acceleration", UnitGroup.UNITS_ACCELERATION, 12);
	

	//// Total motion
	public static final FlightDataType TYPE_VELOCITY_TOTAL =
			newType("Total velocity", UnitGroup.UNITS_VELOCITY, 20);
	public static final FlightDataType TYPE_ACCELERATION_TOTAL =
			newType("Total acceleration", UnitGroup.UNITS_ACCELERATION, 21);
	

	//// Lateral position and motion
	
	public static final FlightDataType TYPE_POSITION_X =
			newType("Position upwind", UnitGroup.UNITS_DISTANCE, 30);
	public static final FlightDataType TYPE_POSITION_Y =
			newType("Position parallel to wind", UnitGroup.UNITS_DISTANCE, 31);
	public static final FlightDataType TYPE_POSITION_XY =
			newType("Lateral distance", UnitGroup.UNITS_DISTANCE, 32);
	public static final FlightDataType TYPE_POSITION_DIRECTION =
			newType("Lateral direction", UnitGroup.UNITS_ANGLE, 33);
	
	public static final FlightDataType TYPE_VELOCITY_XY =
			newType("Lateral velocity", UnitGroup.UNITS_VELOCITY, 34);
	public static final FlightDataType TYPE_ACCELERATION_XY =
			newType("Lateral acceleration", UnitGroup.UNITS_ACCELERATION, 35);
	

	//// Angular motion
	public static final FlightDataType TYPE_AOA = newType("Angle of attack", UnitGroup.UNITS_ANGLE, 40);
	public static final FlightDataType TYPE_ROLL_RATE = newType("Roll rate", UnitGroup.UNITS_ROLL, 41);
	public static final FlightDataType TYPE_PITCH_RATE = newType("Pitch rate", UnitGroup.UNITS_ROLL, 42);
	public static final FlightDataType TYPE_YAW_RATE = newType("Yaw rate", UnitGroup.UNITS_ROLL, 43);
	

	//// Stability information
	public static final FlightDataType TYPE_MASS =
			newType("Mass", UnitGroup.UNITS_MASS, 50);
	public static final FlightDataType TYPE_CP_LOCATION =
			newType("CP location", UnitGroup.UNITS_LENGTH, 51);
	public static final FlightDataType TYPE_CG_LOCATION =
			newType("CG location", UnitGroup.UNITS_LENGTH, 52);
	public static final FlightDataType TYPE_STABILITY =
			newType("Stability margin calibers", UnitGroup.UNITS_COEFFICIENT, 53);
	// TODO: HIGH: Add moment of inertia
	

	//// Characteristic numbers
	public static final FlightDataType TYPE_MACH_NUMBER =
			newType("Mach number", UnitGroup.UNITS_COEFFICIENT, 60);
	public static final FlightDataType TYPE_REYNOLDS_NUMBER =
			newType("Reynolds number", UnitGroup.UNITS_COEFFICIENT, 61);
	

	//// Thrust and drag
	public static final FlightDataType TYPE_THRUST_FORCE =
			newType("Thrust", UnitGroup.UNITS_FORCE, 70);
	public static final FlightDataType TYPE_DRAG_FORCE =
			newType("Drag force", UnitGroup.UNITS_FORCE, 71);
	
	public static final FlightDataType TYPE_DRAG_COEFF =
			newType("Drag coefficient", UnitGroup.UNITS_COEFFICIENT, 72);
	public static final FlightDataType TYPE_AXIAL_DRAG_COEFF =
			newType("Axial drag coefficient", UnitGroup.UNITS_COEFFICIENT, 73);
	

	////  Component drag coefficients
	public static final FlightDataType TYPE_FRICTION_DRAG_COEFF =
			newType("Friction drag coefficient", UnitGroup.UNITS_COEFFICIENT, 80);
	public static final FlightDataType TYPE_PRESSURE_DRAG_COEFF =
			newType("Pressure drag coefficient", UnitGroup.UNITS_COEFFICIENT, 81);
	public static final FlightDataType TYPE_BASE_DRAG_COEFF =
			newType("Base drag coefficient", UnitGroup.UNITS_COEFFICIENT, 82);
	

	////  Other coefficients
	public static final FlightDataType TYPE_NORMAL_FORCE_COEFF =
			newType("Normal force coefficient", UnitGroup.UNITS_COEFFICIENT, 90);
	public static final FlightDataType TYPE_PITCH_MOMENT_COEFF =
			newType("Pitch moment coefficient", UnitGroup.UNITS_COEFFICIENT, 91);
	public static final FlightDataType TYPE_YAW_MOMENT_COEFF =
			newType("Yaw moment coefficient", UnitGroup.UNITS_COEFFICIENT, 92);
	public static final FlightDataType TYPE_SIDE_FORCE_COEFF =
			newType("Side force coefficient", UnitGroup.UNITS_COEFFICIENT, 93);
	public static final FlightDataType TYPE_ROLL_MOMENT_COEFF =
			newType("Roll moment coefficient", UnitGroup.UNITS_COEFFICIENT, 94);
	public static final FlightDataType TYPE_ROLL_FORCING_COEFF =
			newType("Roll forcing coefficient", UnitGroup.UNITS_COEFFICIENT, 95);
	public static final FlightDataType TYPE_ROLL_DAMPING_COEFF =
			newType("Roll damping coefficient", UnitGroup.UNITS_COEFFICIENT, 96);
	
	public static final FlightDataType TYPE_PITCH_DAMPING_MOMENT_COEFF =
			newType("Pitch damping coefficient", UnitGroup.UNITS_COEFFICIENT, 97);
	public static final FlightDataType TYPE_YAW_DAMPING_MOMENT_COEFF =
			newType("Yaw damping coefficient", UnitGroup.UNITS_COEFFICIENT, 98);
	

	////  Reference length + area
	public static final FlightDataType TYPE_REFERENCE_LENGTH =
			newType("Reference length", UnitGroup.UNITS_LENGTH, 100);
	public static final FlightDataType TYPE_REFERENCE_AREA =
			newType("Reference area", UnitGroup.UNITS_AREA, 101);
	

	////  Orientation
	public static final FlightDataType TYPE_ORIENTATION_THETA =
			newType("Vertical orientation (zenith)", UnitGroup.UNITS_ANGLE, 106);
	public static final FlightDataType TYPE_ORIENTATION_PHI =
			newType("Lateral orientation (azimuth)", UnitGroup.UNITS_ANGLE, 107);
	

	////  Atmospheric conditions
	public static final FlightDataType TYPE_WIND_VELOCITY = newType("Wind velocity",
			UnitGroup.UNITS_VELOCITY, 110);
	public static final FlightDataType TYPE_AIR_TEMPERATURE = newType("Air temperature",
			UnitGroup.UNITS_TEMPERATURE, 111);
	public static final FlightDataType TYPE_AIR_PRESSURE = newType("Air pressure",
			UnitGroup.UNITS_PRESSURE, 112);
	public static final FlightDataType TYPE_SPEED_OF_SOUND = newType("Speed of sound",
			UnitGroup.UNITS_VELOCITY, 113);
	

	////  Simulation information
	public static final FlightDataType TYPE_TIME_STEP = newType("Simulation time step",
			UnitGroup.UNITS_TIME_STEP, 200);
	public static final FlightDataType TYPE_COMPUTATION_TIME = newType("Computation time",
			UnitGroup.UNITS_SHORT_TIME, 201);
	
	

	/**
	 * Return a {@link FlightDataType} based on a string description.  This returns known data types
	 * if possible, or a new type otherwise.
	 * 
	 * @param s		the string description of the type.
	 * @param u		the unit group the new type should belong to if a new group is created.
	 * @return		a data type.
	 */
	public static synchronized FlightDataType getType(String s, UnitGroup u) {
		for (FlightDataType t : EXISTING_TYPES) {
			if (t.getName().equalsIgnoreCase(s))
				return t;
		}
		FlightDataType type = new FlightDataType(s, u, DEFAULT_PRIORITY);
		EXISTING_TYPES.add(type);
		return type;
	}
	
	/**
	 * Used while initializing the class.
	 */
	private static synchronized FlightDataType newType(String s, UnitGroup u, int priority) {
		FlightDataType type = new FlightDataType(s, u, priority);
		EXISTING_TYPES.add(type);
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
		this.hashCode = this.name.toLowerCase().hashCode();
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
		return this.name.compareTo(o.name);
	}
}