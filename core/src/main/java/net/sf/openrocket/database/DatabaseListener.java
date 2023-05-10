package net.sf.openrocket.database;

/**
 * interface defining listeners for database
 *
 * @param <T>	type stored in the database
 */
public interface DatabaseListener<T extends Comparable<T>> {

	/**
	 * action for when elements are added
	 * @param element	the element added
	 * @param source	the database of which the element was added
	 */
	public void elementAdded(T element, Database<T> source);
	
	/**
	 * action for when elements are removed
	 * @param element	the removed element
	 * @param source	the database on which the element was removed
	 */
	public void elementRemoved(T element, Database<T> source);
	
}
