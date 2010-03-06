package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Pair;


public class FlightDataBranch {
	
	//// Time
	public static final Type TYPE_TIME = 
		new Type("Time", UnitGroup.UNITS_FLIGHT_TIME, 1);
	
	
	//// Vertical position and motion
	public static final Type TYPE_ALTITUDE = 
		new Type("Altitude", UnitGroup.UNITS_DISTANCE, 10);
	public static final Type TYPE_VELOCITY_Z = 
		new Type("Vertical velocity", UnitGroup.UNITS_VELOCITY, 11);
	public static final Type TYPE_ACCELERATION_Z = 
		new Type("Vertical acceleration", UnitGroup.UNITS_ACCELERATION, 12);

	
	//// Total motion
	public static final Type TYPE_VELOCITY_TOTAL =
		new Type("Total velocity", UnitGroup.UNITS_VELOCITY, 20);
	public static final Type TYPE_ACCELERATION_TOTAL =
		new Type("Total acceleration", UnitGroup.UNITS_ACCELERATION, 21);

	
	//// Lateral position and motion
	
	public static final Type TYPE_POSITION_X =
		new Type("Position upwind", UnitGroup.UNITS_DISTANCE, 30);
	public static final Type TYPE_POSITION_Y =
		new Type("Position parallel to wind", UnitGroup.UNITS_DISTANCE, 31);
	public static final Type TYPE_POSITION_XY =
		new Type("Lateral distance", UnitGroup.UNITS_DISTANCE, 32);
	public static final Type TYPE_POSITION_DIRECTION =
		new Type("Lateral direction", UnitGroup.UNITS_ANGLE, 33);

	public static final Type TYPE_VELOCITY_XY =
		new Type("Lateral velocity", UnitGroup.UNITS_VELOCITY, 34);
	public static final Type TYPE_ACCELERATION_XY =
		new Type("Lateral acceleration", UnitGroup.UNITS_ACCELERATION, 35);

	
	//// Angular motion
	public static final Type TYPE_AOA = new Type("Angle of attack", UnitGroup.UNITS_ANGLE, 40);
	public static final Type TYPE_ROLL_RATE = new Type("Roll rate", UnitGroup.UNITS_ROLL, 41);
	public static final Type TYPE_PITCH_RATE = new Type("Pitch rate", UnitGroup.UNITS_ROLL, 42);
	public static final Type TYPE_YAW_RATE = new Type("Yaw rate", UnitGroup.UNITS_ROLL, 43);

	
	//// Stability information
	public static final Type TYPE_MASS = 
		new Type("Mass", UnitGroup.UNITS_MASS, 50);
	public static final Type TYPE_CP_LOCATION = 
		new Type("CP location", UnitGroup.UNITS_LENGTH, 51);
	public static final Type TYPE_CG_LOCATION = 
		new Type("CG location", UnitGroup.UNITS_LENGTH, 52);
	public static final Type TYPE_STABILITY = 
		new Type("Stability margin calibers", UnitGroup.UNITS_COEFFICIENT, 53);

	
	//// Characteristic numbers
	public static final Type TYPE_MACH_NUMBER =
		new Type("Mach number", UnitGroup.UNITS_COEFFICIENT, 60);
	public static final Type TYPE_REYNOLDS_NUMBER =
		new Type("Reynolds number", UnitGroup.UNITS_COEFFICIENT, 61);
	

	//// Thrust and drag
	public static final Type TYPE_THRUST_FORCE = 
		new Type("Thrust", UnitGroup.UNITS_FORCE, 70);
	public static final Type TYPE_DRAG_FORCE = 
		new Type("Drag force", UnitGroup.UNITS_FORCE, 71);

	public static final Type TYPE_DRAG_COEFF = 
		new Type("Drag coefficient", UnitGroup.UNITS_COEFFICIENT, 72);
	public static final Type TYPE_AXIAL_DRAG_COEFF = 
		new Type("Axial drag coefficient", UnitGroup.UNITS_COEFFICIENT, 73);

	
	////  Component drag coefficients
	public static final Type TYPE_FRICTION_DRAG_COEFF = 
		new Type("Friction drag coefficient", UnitGroup.UNITS_COEFFICIENT, 80);
	public static final Type TYPE_PRESSURE_DRAG_COEFF = 
		new Type("Pressure drag coefficient", UnitGroup.UNITS_COEFFICIENT, 81);
	public static final Type TYPE_BASE_DRAG_COEFF = 
		new Type("Base drag coefficient", UnitGroup.UNITS_COEFFICIENT, 82);


	////  Other coefficients
	public static final Type TYPE_NORMAL_FORCE_COEFF =
		new Type("Normal force coefficient", UnitGroup.UNITS_COEFFICIENT, 90);
	public static final Type TYPE_PITCH_MOMENT_COEFF =
		new Type("Pitch moment coefficient", UnitGroup.UNITS_COEFFICIENT, 91);
	public static final Type TYPE_YAW_MOMENT_COEFF =
		new Type("Yaw moment coefficient", UnitGroup.UNITS_COEFFICIENT, 92);
	public static final Type TYPE_SIDE_FORCE_COEFF =
		new Type("Side force coefficient", UnitGroup.UNITS_COEFFICIENT, 93);
	public static final Type TYPE_ROLL_MOMENT_COEFF =
		new Type("Roll moment coefficient", UnitGroup.UNITS_COEFFICIENT, 94);
	public static final Type TYPE_ROLL_FORCING_COEFF =
		new Type("Roll forcing coefficient", UnitGroup.UNITS_COEFFICIENT, 95);
	public static final Type TYPE_ROLL_DAMPING_COEFF =
		new Type("Roll damping coefficient", UnitGroup.UNITS_COEFFICIENT, 96);
	
	public static final Type TYPE_PITCH_DAMPING_MOMENT_COEFF =
		new Type("Pitch damping coefficient", UnitGroup.UNITS_COEFFICIENT, 97);
	public static final Type TYPE_YAW_DAMPING_MOMENT_COEFF =
		new Type("Yaw damping coefficient", UnitGroup.UNITS_COEFFICIENT, 98);
	
	
	////  Reference length + area
	public static final Type TYPE_REFERENCE_LENGTH = 
		new Type("Reference length", UnitGroup.UNITS_LENGTH, 100);
	public static final Type TYPE_REFERENCE_AREA = 
		new Type("Reference area", UnitGroup.UNITS_AREA, 101);
	

	////  Orientation
	public static final Type TYPE_ORIENTATION_THETA = 
		new Type("Vertical orientation (zenith)", UnitGroup.UNITS_ANGLE, 106);
	public static final Type TYPE_ORIENTATION_PHI =
		new Type("Lateral orientation (azimuth)", UnitGroup.UNITS_ANGLE, 107);
	
	
	////  Atmospheric conditions
	public static final Type TYPE_WIND_VELOCITY = new Type("Wind velocity", 
			UnitGroup.UNITS_VELOCITY, 110);
	public static final Type TYPE_AIR_TEMPERATURE = new Type("Air temperature",
			UnitGroup.UNITS_TEMPERATURE, 111);
	public static final Type TYPE_AIR_PRESSURE = new Type("Air pressure",
			UnitGroup.UNITS_PRESSURE, 112);
	public static final Type TYPE_SPEED_OF_SOUND = new Type("Speed of sound",
			UnitGroup.UNITS_VELOCITY, 113);


	////  Simulation information
	public static final Type TYPE_TIME_STEP = new Type("Simulation time step",
			UnitGroup.UNITS_TIME_STEP, 200);
	public static final Type TYPE_COMPUTATION_TIME = new Type("Computation time",
			UnitGroup.UNITS_SHORT_TIME, 201);

	
	/**
	 * Array of known data types for String -> Type conversion.
	 */
	private static final Type[] TYPES = {
		TYPE_TIME, 
		TYPE_ALTITUDE, TYPE_VELOCITY_Z, TYPE_ACCELERATION_Z,
		TYPE_VELOCITY_TOTAL, TYPE_ACCELERATION_TOTAL,
		TYPE_POSITION_X, TYPE_POSITION_Y, TYPE_POSITION_XY, TYPE_POSITION_DIRECTION,
		TYPE_VELOCITY_XY, TYPE_ACCELERATION_XY,
		TYPE_AOA, TYPE_ROLL_RATE, TYPE_PITCH_RATE, TYPE_YAW_RATE,
		TYPE_MASS, TYPE_CP_LOCATION, TYPE_CG_LOCATION, TYPE_STABILITY,
		TYPE_MACH_NUMBER, TYPE_REYNOLDS_NUMBER,
		TYPE_THRUST_FORCE, TYPE_DRAG_FORCE,
		TYPE_DRAG_COEFF, TYPE_AXIAL_DRAG_COEFF,
		TYPE_FRICTION_DRAG_COEFF, TYPE_PRESSURE_DRAG_COEFF, TYPE_BASE_DRAG_COEFF,
		TYPE_NORMAL_FORCE_COEFF, TYPE_PITCH_MOMENT_COEFF, TYPE_YAW_MOMENT_COEFF, TYPE_SIDE_FORCE_COEFF,
		TYPE_ROLL_MOMENT_COEFF, TYPE_ROLL_FORCING_COEFF, TYPE_ROLL_DAMPING_COEFF,
		TYPE_PITCH_DAMPING_MOMENT_COEFF, TYPE_YAW_DAMPING_MOMENT_COEFF,
		TYPE_REFERENCE_LENGTH, TYPE_REFERENCE_AREA, 
		TYPE_ORIENTATION_THETA, TYPE_ORIENTATION_PHI,
		TYPE_WIND_VELOCITY, TYPE_AIR_TEMPERATURE, TYPE_AIR_PRESSURE, TYPE_SPEED_OF_SOUND,
		TYPE_TIME_STEP, TYPE_COMPUTATION_TIME
	};
	
	/**
	 * Return a {@link Type} based on a string description.  This returns known data types
	 * if possible, or a new type otherwise.
	 * 
	 * @param s		the string description of the type.
	 * @param u		the unit group the new type should belong to if a new group is created.
	 * @return		a data type.
	 */
	public static Type getType(String s, UnitGroup u) {
		for (Type t: TYPES) {
			if (t.getName().equalsIgnoreCase(s))
				return t;
		}
		return new Type(s, u);
	}
	
	
	
	public static class Type implements Comparable<Type> {
		private final String name;
		private final UnitGroup units;
		private final int priority;
		private final int hashCode;
		
		private Type(String typeName, UnitGroup units) {
			this(typeName, units, 999);
		}
		
		public Type(String typeName, UnitGroup units, int priority) {
			if (typeName == null)
				throw new IllegalArgumentException("typeName is null");
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
			if (!(other instanceof Type))
				return false;
			return this.name.equalsIgnoreCase(((Type)other).name);
		}
		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public int compareTo(Type o) {
			return this.priority - o.priority;
		}
	}
	

	
	
	/** The name of this flight data branch. */
	private final String branchName;
	
	private final Map<Type, ArrayList<Double>> values = 
		new LinkedHashMap<Type, ArrayList<Double>>();
	
	private final Map<Type, Double> maxValues = new HashMap<Type, Double>();
	private final Map<Type, Double> minValues = new HashMap<Type, Double>();
	
	
	private final ArrayList<Pair<Double,FlightEvent>> events =
		new ArrayList<Pair<Double,FlightEvent>>();
	
	private boolean mutable = true;
	
	
	public FlightDataBranch(String name, Type... types) {
		if (types.length == 0) {
			throw new IllegalArgumentException("Must specify at least one data type.");
		}
		
		this.branchName = name;

		for (Type t: types) {
			if (values.containsKey(t)) {
				throw new IllegalArgumentException("Value type "+t+" specified multiple " +
				"times in constructor.");
			}
			
			values.put(t, new ArrayList<Double>());
			minValues.put(t, Double.NaN);
			maxValues.put(t, Double.NaN);
		}
	}
	
	
	public String getBranchName() {
		return branchName;
	}
	
	public Type[] getTypes() {
		Type[] array = values.keySet().toArray(new Type[0]);
		Arrays.sort(array);
		return array;
	}
	
	public int getLength() {
		for (Type t: values.keySet()) {
			return values.get(t).size();
		}
		return 0;
	}
	
	
	
	public void addPoint() {
		if (!mutable)
			throw new IllegalStateException("FlightDataBranch has been made immutable.");
		for (Type t: values.keySet()) {
			values.get(t).add(Double.NaN);
		}
	}
	
	public void setValue(Type type, double value) {
		if (!mutable)
			throw new IllegalStateException("FlightDataBranch has been made immutable.");
		ArrayList<Double> list = values.get(type);
		if (list == null) {
			
			list = new ArrayList<Double>();
			int n = getLength();
			for (int i=0; i < n; i++) {
				list.add(Double.NaN);
			}
			values.put(type, list);
			minValues.put(type, value);
			maxValues.put(type, value);
			
		}
		list.set(list.size()-1, value);
		double min = minValues.get(type);
		double max = maxValues.get(type);
		
		if (Double.isNaN(min) || (value < min)) {
			minValues.put(type, value);
		}
		if (Double.isNaN(max) || (value > max)) {
			maxValues.put(type, value);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Double> get(Type type) {
		ArrayList<Double> list = values.get(type);
		if (list==null)
			return null;
		return (List<Double>)list.clone();
	}
	
	
	public double get(Type type, int index) {
		ArrayList<Double> list = values.get(type);
		if (list==null)
			return Double.NaN;
		return list.get(index);
	}
	

	/**
	 * Return the last value of the specified type in the branch, or NaN if the type is
	 * unavailable.
	 * 
	 * @param type	the parameter type.
	 * @return		the last value in this branch, or NaN.
	 */
	public double getLast(Type type) {
		ArrayList<Double> list = values.get(type);
		if (list==null || list.isEmpty())
			return Double.NaN;
		return list.get(list.size()-1);
	}

	/**
	 * Return the minimum value of the specified type in the branch, or NaN if the type
	 * is unavailable.
	 * 
	 * @param type	the parameter type.
	 * @return		the minimum value in this branch, or NaN.
	 */
	public double getMinimum(Type type) {
		Double v = minValues.get(type);
		if (v==null)
			return Double.NaN;
		return v;
	}
	
	/**
	 * Return the maximum value of the specified type in the branch, or NaN if the type
	 * is unavailable.
	 * 
	 * @param type	the parameter type.
	 * @return		the maximum value in this branch, or NaN.
	 */
	public double getMaximum(Type type) {
		Double v = maxValues.get(type);
		if (v==null)
			return Double.NaN;
		return v;
	}
	
	
	public void addEvent(double time, FlightEvent event) {
		if (!mutable)
			throw new IllegalStateException("FlightDataBranch has been made immutable.");
		events.add(new Pair<Double,FlightEvent>(time,event));
	}
	
	
	/**
	 * Return the list of events.  The list is a list of (time, event) pairs.
	 * 
	 * @return	the list of events during the flight.
	 */
	@SuppressWarnings("unchecked")
	public List<Pair<Double, FlightEvent>> getEvents() {
		return (List<Pair<Double, FlightEvent>>) events.clone();
	}

	
	/**
	 * Make this FlightDataBranch immutable.  Any calls to the set methods that would
	 * modify this object will after this call throw an <code>IllegalStateException</code>.
	 */
	public void immute() {
		mutable = false;
	}
	
	public boolean isMutable() {
		return mutable;
	}
	
	

	
}
