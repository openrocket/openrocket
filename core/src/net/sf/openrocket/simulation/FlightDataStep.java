package net.sf.openrocket.simulation;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Mutable;

public class FlightDataStep {
	/**
	 * A single step of flight data. One step of data is extracted from a FlightDataBranch
	 * object and stored as <key,value> linked hash map in the variable values.
	 * 
	 * @author nubjub <nubjub@gmail.com>
	 */
	private int iteration = -1;
	private int modID = -1;
	private final Map<FlightDataType, Double> values =
			new LinkedHashMap<FlightDataType, Double>();
	
	private Mutable mutable = new Mutable();
	
	/**
	 * Simple constructor, the branch must not be null.
	 * @param branch
	 */
	public FlightDataStep(FlightDataBranch branch) {
		this(branch, getLastStep(branch));
	}
	
	/**
	 * Will create a FlightDataStep from the specified iteration in the
	 * specified FlightDataBranch
	 * 
	 * @param branch	This should be an existing FlightDataBranch object
	 * @param step		The iteration within the FlightDataBranch to extract
	 */
	public FlightDataStep(FlightDataBranch branch, int step) {
		if (branch == null) {
			throw new IllegalArgumentException("Flight data branch parameter cannot be null.");
		}
		if (branch.getLength() < step) {
			throw new IllegalArgumentException("Step does not exist in Flight data branch");
		}
		
		iteration = step;
		modID = branch.getModID();
		
		if (step > 0) {
			step = step - 1;
			FlightDataType[] types = branch.getTypes();
			for (FlightDataType t : types) {
				ArrayList<Double> list = new ArrayList<Double>(branch.get(t));
				Double v = new Double(list.get(step));
				//TODO: what is the optimal value to set a NAN to?
				if (v.isNaN()) {
					v = (double) -1;
				}
				values.put(t, v);
			}
		}
	}
	
	/**
	 * Will create a FlightDataStep from the last iteration in the
	 * specified FlightDataBranch
	 * 
	 * @param branch	This 
	 * @return
	 */
	private static int getLastStep(FlightDataBranch branch) {
		if (branch == null) {
			throw new IllegalArgumentException("branch parameter cannot be null.");
		}
		return branch.getLength();
	}
	
	/**
	 * Get an array of all the keys within the data step
	 * @return
	 */
	public FlightDataType[] getTypes() {
		FlightDataType[] array = values.keySet().toArray(new FlightDataType[0]);
		Arrays.sort(array);
		return array;
	}
	
	/**
	 * Get a count of all the keys in the data step
	 * @return		int
	 */
	public int getTypesCount() {
		return values.size();
	}
	
	/**
	 * Return one value corresponding to the key type, from within the data step
	 * @param type	
	 * @return		double
	 */
	public double get(FlightDataType type) {
		if (values.containsKey(type)) {
			return values.get(type);
		}
		return -1;
	}
	
	/**
	 * Get the iteration of the FlightDataBranch which this data step corresponds to.
	 * @return
	 */
	public int getIteration() {
		return iteration;
	}
	
	/**
	 * Unique modification index
	 * @return
	 */
	//TODO: Not really sure how this works.
	public int getModID() {
		return modID;
	}
	
	/**
	 * Make this FlightDataBranch immutable.  Any calls to the set methods that would
	 * modify this object will after this call throw an <code>IllegalStateException</code>.
	 * 
	 * NOTE: This isn't in use currently.
	 */
	public void immute() {
		mutable.immute();
	}
	
	/**
	 * Return whether this branch is still mutable.
	 * 
	 * NOTE: This isn't in use currently.
	 */
	public boolean isMutable() {
		return mutable.isMutable();
	}
	
}