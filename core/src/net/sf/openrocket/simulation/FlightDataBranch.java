package net.sf.openrocket.simulation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.Mutable;

/**
 * A single branch of flight data.  The data is ordered based on some variable, typically time.
 * It also contains flight events that have occurred during simulation.
 * <p>
 * After instantiating a FlightDataBranch data and new variable types can be added to the branch.
 * A new data point (a value for each variable defined) is created using {@link #addPoint()} after
 * which the value for each variable type can be set using {@link #setValue(FlightDataType, double)}.
 * Each variable type does NOT have to be set, unset values will default to NaN.  New variable types
 * not defined in the constructor can be added using {@link #setValue(FlightDataType, double)}, they
 * will be created and all previous values will be set to NaN.
 * <p>
 * After populating a FlightDataBranch object it can be made immutable by calling {@link #immute()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightDataBranch implements Monitorable {
	
	/** The name of this flight data branch. */
	private final String branchName;
	
	private final Map<FlightDataType, ArrayList<Double>> values =
			new LinkedHashMap<FlightDataType, ArrayList<Double>>();
	
	private final Map<FlightDataType, Double> maxValues = new HashMap<FlightDataType, Double>();
	private final Map<FlightDataType, Double> minValues = new HashMap<FlightDataType, Double>();
	
	/**
	 * time for the rocket to reach apogee if the flight had been no recovery deployment
	 */
	private double timeToOptimumAltitude = Double.NaN;
	/**
	 * Altitude the rocket would reach if there had been no recovery deployment.
	 */
	private double optimumAltitude = Double.NaN;
	
	private final ArrayList<FlightEvent> events = new ArrayList<FlightEvent>();
	
	private Mutable mutable = new Mutable();
	
	private int modID = 0;
	
	/**
	 * Sole constructor.  Defines the name of the FlightDataBranch and at least one variable type.
	 * 
	 * @param name		the name of this FlightDataBranch.
	 * @param types		data types to include (must include at least one type).
	 */
	public FlightDataBranch(String name, FlightDataType... types) {
		if (types.length == 0) {
			throw new IllegalArgumentException("Must specify at least one data type.");
		}
		
		this.branchName = name;
		
		for (FlightDataType t : types) {
			if (values.containsKey(t)) {
				throw new IllegalArgumentException("Value type " + t + " specified multiple " +
						"times in constructor.");
			}
			
			values.put(t, new ArrayList<Double>());
			minValues.put(t, Double.NaN);
			maxValues.put(t, Double.NaN);
		}
	}
	
	/**
	 * Makes an 'empty' flight data branch which has no data but all built in data types are defined.
	 */
	public FlightDataBranch() {
		branchName = "Empty branch";
		for (FlightDataType type : FlightDataType.ALL_TYPES) {
			this.setValue(type, Double.NaN);
		}
		this.immute();
	}
	
	/**
	 * Adds a new point into the data branch.  The value for all types is set to NaN by default.
	 * 
	 * @throws IllegalStateException	if this object has been made immutable.
	 */
	public void addPoint() {
		mutable.check();
		
		for (FlightDataType t : values.keySet()) {
			values.get(t).add(Double.NaN);
		}
		modID++;
	}
	
	
	/**
	 * Set the value for a specific data type at the latest point.  New variable types can be
	 * added to the FlightDataBranch transparently.
	 * 
	 * @param type		the variable to set.
	 * @param value		the value to set.
	 * @throws IllegalStateException	if this object has been made immutable.
	 */
	public void setValue(FlightDataType type, double value) {
		mutable.check();
		
		ArrayList<Double> list = values.get(type);
		
		if (list == null) {
			list = new ArrayList<Double>();
			int n = getLength();
			for (int i = 0; i < n; i++) {
				list.add(Double.NaN);
			}
			values.put(type, list);
			minValues.put(type, value);
			maxValues.put(type, value);
		}
		
		if (list.size() > 0) {
			list.set(list.size() - 1, value);
		}
		
		double min = minValues.get(type);
		double max = maxValues.get(type);
		
		if (Double.isNaN(min) || (value < min)) {
			minValues.put(type, value);
		}
		if (Double.isNaN(max) || (value > max)) {
			maxValues.put(type, value);
		}
		modID++;
	}
	
	
	/**
	 * Return the branch name.
	 */
	public String getBranchName() {
		return branchName;
	}
	
	/**
	 * Return the variable types included in this branch.  The types are sorted in their
	 * natural order.
	 */
	public FlightDataType[] getTypes() {
		FlightDataType[] array = values.keySet().toArray(new FlightDataType[0]);
		Arrays.sort(array);
		return array;
	}
	
	/**
	 * Return the number of data points in this branch.
	 */
	public int getLength() {
		for (FlightDataType t : values.keySet()) {
			return values.get(t).size();
		}
		return 0;
	}
	
	/**
	 * Return an array of values for the specified variable type.
	 * 
	 * @param type	the variable type.
	 * @return		a list of the variable values, or <code>null</code> if
	 * 				the variable type hasn't been added to this branch.
	 */
	public List<Double> get(FlightDataType type) {
		ArrayList<Double> list = values.get(type);
		if (list == null)
			return null;
		return list.clone();
	}
	
	/**
	 * Return the last value of the specified type in the branch, or NaN if the type is
	 * unavailable.
	 * 
	 * @param type	the parameter type.
	 * @return		the last value in this branch, or NaN.
	 */
	public double getLast(FlightDataType type) {
		ArrayList<Double> list = values.get(type);
		if (list == null || list.isEmpty())
			return Double.NaN;
		return list.get(list.size() - 1);
	}
	
	/**
	 * Return the minimum value of the specified type in the branch, or NaN if the type
	 * is unavailable.
	 * 
	 * @param type	the parameter type.
	 * @return		the minimum value in this branch, or NaN.
	 */
	public double getMinimum(FlightDataType type) {
		Double v = minValues.get(type);
		if (v == null)
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
	public double getMaximum(FlightDataType type) {
		Double v = maxValues.get(type);
		if (v == null)
			return Double.NaN;
		return v;
	}
	
	
	/**
	 * @return the timeToOptimumAltitude
	 */
	public double getTimeToOptimumAltitude() {
		return timeToOptimumAltitude;
	}
	
	/**
	 * @param timeToOptimumAltitude the timeToOptimumAltitude to set
	 */
	public void setTimeToOptimumAltitude(double timeToOptimumAltitude) {
		this.timeToOptimumAltitude = timeToOptimumAltitude;
	}
	
	/**
	 * @return the optimumAltitude
	 */
	public double getOptimumAltitude() {
		return optimumAltitude;
	}
	
	/**
	 * @param optimumAltitude the optimumAltitude to set
	 */
	public void setOptimumAltitude(double optimumAltitude) {
		this.optimumAltitude = optimumAltitude;
	}
	
	public double getOptimumDelay() {
		
		if (Double.isNaN(timeToOptimumAltitude)) {
			return Double.NaN;
		}
		// TODO - we really want the first burnout of this stage.  which
		// could be computed as the first burnout after the last stage separation event.
		// however, that's not quite so concise
		FlightEvent e = getLastEvent(FlightEvent.Type.BURNOUT);
		if (e != null) {
			return timeToOptimumAltitude - e.getTime();
		}
		
		return Double.NaN;
	}
	
	/**
	 * Add a flight event to this branch.
	 * 
	 * @param event		the event to add.
	 * @throws IllegalStateException	if this branch has been made immutable.
	 */
	public void addEvent(FlightEvent event) {
		mutable.check();
		events.add(event.resetSourceAndData());
		modID++;
	}
	
	
	/**
	 * Return the list of events.
	 * 
	 * @return	the list of events during the flight.
	 */
	public List<FlightEvent> getEvents() {
		return events.clone();
	}
	
	/**
	 * Return the first event of the given type.
	 * @param type
	 * @return
	 */
	public FlightEvent getFirstEvent(FlightEvent.Type type) {
		for (FlightEvent e : events) {
			if (e.getType() == type) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * Return the last event of the given type.
	 * @param type
	 * @return
	 */
	public FlightEvent getLastEvent(FlightEvent.Type type) {
		FlightEvent retval = null;
		for (FlightEvent e : events) {
			if (e.getType() == type) {
				retval = e;
			}
		}
		return retval;
	}
	
	/**
	 * Make this FlightDataBranch immutable.  Any calls to the set methods that would
	 * modify this object will after this call throw an <code>IllegalStateException</code>.
	 */
	public void immute() {
		mutable.immute();
	}
	
	
	/**
	 * Return whether this branch is still mutable.
	 */
	public boolean isMutable() {
		return mutable.isMutable();
	}
	
	
	@Override
	public int getModID() {
		return modID;
	}
	
}
