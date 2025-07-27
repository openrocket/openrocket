package info.openrocket.core.simulation;

import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.Mutable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A branch of data / collection of data points for a specific type of data.
 * @param <T> the type of data in this branch
 */
public abstract class DataBranch<T extends DataType> implements Monitorable {
	protected final String name;
	protected final Map<T, ArrayList<Double>> values = new LinkedHashMap<>();
	protected final Map<T, Double> maxValues = new HashMap<>();
	protected final Map<T, Double> minValues = new HashMap<>();

	protected final Mutable mutable = new Mutable();
	protected ModID modID = ModID.INVALID;

	/**
	 * Sole constructor.  Defines the name of the DataBranch and at least one variable type.
	 *
	 * @param name		the name of this DataBranch.
	 * @param types		data types to include (must include at least one type).
	 */
	@SafeVarargs
	public DataBranch(String name, T... types) {
		if (types.length == 0) {
			throw new IllegalArgumentException("Must specify at least one data type.");
		}

		this.name = name;

		for (T t : types) {
			addType(t);
		}
	}

	public DataBranch(String name) {
		this.name = name;
	}

	public void addType(T type) {
		if (values.containsKey(type)) {
			throw new IllegalArgumentException("Value type " + type + " already exists.");
		}

		values.put(type, new ArrayList<>());
		minValues.put(type, Double.NaN);
		maxValues.put(type, Double.NaN);
	}

	/**
	 * Adds a new point into the data branch.  The value for all types is set to NaN by default.
	 *
	 * @throws IllegalStateException	if this object has been made immutable.
	 */
	public void addPoint() {
		mutable.check();
		for (Map.Entry<T, ArrayList<Double>> entry : values.entrySet()) {
			sanityCheckValues(entry.getKey(), Double.NaN);
			entry.getValue().add(Double.NaN);
		}
		modID = new ModID();
	}

	private void sanityCheckValues(T type, Double value) {
		ArrayList<Double> list = values.get(type);

		if (list == null) {
			list = new info.openrocket.core.util.ArrayList<>();
			int n = getLength();
			for (int i = 0; i < n; i++) {
				list.add(Double.NaN);
			}
			values.put(type, list);
			minValues.put(type, value);
			maxValues.put(type, value);
		}
	}

	/**
	 * Set the value for a specific data type at the latest point.  New variable types can be
	 * added to the FlightDataBranch transparently.
	 *
	 * @param type		the variable to set.
	 * @param value		the value to set.
	 * @throws IllegalStateException	if this object has been made immutable.
	 */
	public void setValue(T type, double value) {
		mutable.check();

		ArrayList<Double> list = values.computeIfAbsent(type, k -> {
			ArrayList<Double> newList = new ArrayList<>();
			int n = getLength();
			for (int i = 0; i < n; i++) {
				newList.add(Double.NaN);
			}
			minValues.put(k, Double.NaN);
			maxValues.put(k, Double.NaN);
			return newList;
		});

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
		modID = new ModID();
	}


	/**
	 * Return an array of values for the specified variable type.
	 *
	 * @param type	the variable type.
	 * @return		a list of the variable values, or <code>null</code> if
	 * 				the variable type hasn't been added to this branch.
	 */
	public List<Double> get(T type) {
		ArrayList<Double> list = values.get(type);
		if (list == null)
			return null;
		return list.clone();
	}

	/**
	 * Return the value of the specified type at the specified index.
	 * @param type the variable type
	 * @param index the data index of the value
	 * @return the value at the specified index
	 */
	public Double getByIndex(T type, int index) {
		if (index < 0 || index >= getLength()) {
			throw new IllegalArgumentException("Index out of bounds");
		}
		ArrayList<Double> list = values.get(type);
		if (list == null) {
			return null;
		}
		return list.get(index);
	}

	/**
	 * Return the last value of the specified type in the branch, or NaN if the type is
	 * unavailable.
	 *
	 * @param type	the parameter type.
	 * @return		the last value in this branch, or NaN.
	 */
	public double getLast(T type) {
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
	public double getMinimum(T type) {
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
	public double getMaximum(T type) {
		Double v = maxValues.get(type);
		if (v == null)
			return Double.NaN;
		return v;
	}

	/**
	 * Return the number of data points in this branch.
	 */
	public int getLength() {
		for (ArrayList<Double> doubles : values.values()) {
			return doubles.size();
		}
		return 0;
	}

	/**
	 * Return the variable types included in this branch.  The types are sorted in their
	 * natural order.
	 */
	@SuppressWarnings("unchecked")
	public T[] getTypes() {
		Set<T> keySet = values.keySet();
		T[] array = (T[]) Array.newInstance(keySet.iterator().next().getClass(), keySet.size());
		keySet.toArray(array);
		Arrays.sort(array);
		return array;
	}

	/**
	 * Return the branch name.
	 */
	public String getName() {
		return name;
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

	public ModID getModID() {
		return modID;
	}
}
