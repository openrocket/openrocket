package net.sf.openrocket.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A Set that additionally implements the Monitorable interface.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MonitorableSet<E> extends HashSet<E> implements Monitorable {

	private int modID;
	
	@Override
	public boolean add(E e) {
		modID++;
		return super.add(e);
	};
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		modID++;
		return super.addAll(c);
	}
	
	@Override
	public void clear() {
		modID++;
		super.clear();
	}
	
	@Override
	public Iterator<E> iterator() {
		return new MonitorableIterator<E>(super.iterator());
	}
	
	@Override
	public boolean remove(Object o) {
		modID++;
		return super.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		modID++;
		return super.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		modID++;
		return super.retainAll(c);
	}
	
	
	@Override
	public int getModID() {
		return modID;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MonitorableSet<E> clone() {
		return (MonitorableSet<E>) super.clone();
	}
	
	private class MonitorableIterator<F> implements Iterator<F> {
		private final Iterator<F> iterator;
		
		public MonitorableIterator(Iterator<F> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public F next() {
			return iterator.next();
		}

		@Override
		public void remove() {
			iterator.remove();
			modID++;
		}
	}
}
