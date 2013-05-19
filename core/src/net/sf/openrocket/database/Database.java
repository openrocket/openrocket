package net.sf.openrocket.database;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.openrocket.database.DatabaseListener;



/**
 * A database set.  This class functions as a <code>Set</code> that contains items
 * of a specific type.  Additionally, the items can be accessed via an index number.
 * The elements are always kept in their natural order.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Database<T extends Comparable<T>> extends AbstractSet<T> {
	
	protected final List<T> list = new ArrayList<T>();
	private final ArrayList<DatabaseListener<T>> listeners = new ArrayList<DatabaseListener<T>>();
	
	
	@Override
	public Iterator<T> iterator() {
		return new DBIterator();
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean add(T element) {
		int index;
		
		index = Collections.binarySearch(list, element);
		if (index >= 0) {
			// List might contain the element
			if (list.contains(element)) {
				return false;
			}
		} else {
			index = -(index + 1);
		}
		list.add(index, element);
		fireAddEvent(element);
		return true;
	}
	
	
	/**
	 * Get the element with the specified index.
	 * @param index	the index to retrieve.
	 * @return		the element at the index.
	 */
	public T get(int index) {
		return list.get(index);
	}
	
	/**
	 * Return the index of the given <code>Motor</code>, or -1 if not in the database.
	 * 
	 * @param m   the motor
	 * @return	  the index of the motor
	 */
	public int indexOf(T m) {
		return list.indexOf(m);
	}
	
	
	public void addDatabaseListener(DatabaseListener<T> listener) {
		listeners.add(listener);
	}
	
	public void removeChangeListener(DatabaseListener<T> listener) {
		listeners.remove(listener);
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected void fireAddEvent(T element) {
		Object[] array = listeners.toArray();
		for (Object l : array) {
			((DatabaseListener<T>) l).elementAdded(element, this);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fireRemoveEvent(T element) {
		Object[] array = listeners.toArray();
		for (Object l : array) {
			((DatabaseListener<T>) l).elementRemoved(element, this);
		}
	}
	
	
	
	
	
	/**
	 * Iterator class implementation that fires changes if remove() is called.
	 */
	private class DBIterator implements Iterator<T> {
		private Iterator<T> iterator = list.iterator();
		private T current = null;
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public T next() {
			current = iterator.next();
			return current;
		}
		
		@Override
		public void remove() {
			iterator.remove();
			fireRemoveEvent(current);
		}
	}
}
