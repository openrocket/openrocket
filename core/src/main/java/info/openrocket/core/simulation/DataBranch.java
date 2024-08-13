package info.openrocket.core.simulation;

import info.openrocket.core.util.Monitorable;

import java.util.List;

/**
 * A branch of data / collection of data points for a specific type of data.
 * @param <T> the type of data in this branch
 */
public interface DataBranch<T extends DataType> extends Monitorable {
	/**
	 * Return an array of values for the specified variable type.
	 *
	 * @param type	the variable type.
	 * @return		a list of the variable values, or <code>null</code> if
	 * 				the variable type hasn't been added to this branch.
	 */
	List<Double> get(T type);

	/**
	 * Return the value of the specified type at the specified index.
	 * @param type the variable type
	 * @param index the data index of the value
	 * @return the value at the specified index
	 */
	Double getByIndex(T type, int index);

	/**
	 * Return the last value of the specified type in the branch, or NaN if the type is
	 * unavailable.
	 *
	 * @param type	the parameter type.
	 * @return		the last value in this branch, or NaN.
	 */
	double getLast(T type);

	/**
	 * Return the minimum value of the specified type in the branch, or NaN if the type
	 * is unavailable.
	 *
	 * @param type	the parameter type.
	 * @return		the minimum value in this branch, or NaN.
	 */
	double getMinimum(T type);

	/**
	 * Return the maximum value of the specified type in the branch, or NaN if the type
	 * is unavailable.
	 *
	 * @param type	the parameter type.
	 * @return		the maximum value in this branch, or NaN.
	 */
	double getMaximum(T type);

	/**
	 * Return the number of data points in this branch.
	 */
	int getLength();

	/**
	 * Return the variable types included in this branch.  The types are sorted in their
	 * natural order.
	 */
	T[] getTypes();

	/**
	 * Return the branch name.
	 */
	String getName();
}
