package net.sf.openrocket.util;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list of listeners of a specific type.  This class contains various utility,
 * safety and debugging methods for handling listeners.
 * <p>
 * Note that unlike normal listener implementations, this list does NOT allow the 
 * exact same listener (equality using ==) twice.  While adding a listener twice to
 * a event source would in principle be valid, in practice it's most likely a bug.
 * For example the Swing implementation Sun JRE contains such bugs.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @param <T>	the type of the listeners.
 */
public class ListenerList<T> implements Invalidatable, Iterable<T> {
	private static final Logger log = LoggerFactory.getLogger(ListenerList.class);
	
	private final ArrayList<ListenerData<T>> listeners = new ArrayList<ListenerData<T>>();
	private final Throwable instantiationLocation;
	
	private Throwable invalidated = null;
	
	
	/**
	 * Sole contructor.
	 */
	public ListenerList() {
		this.instantiationLocation = new Throwable();
	}
	
	/**
	 * Adds the specified listener to this list.  The listener is not added if it
	 * already is in the list (checked by the equality operator ==).  This method throws
	 * a BugException if {@link #invalidate()} has been called.
	 * 
	 * @param listener	the listener to add.
	 * @return			whether the listeners was actually added to the list.
	 * @throws BugException		if this listener list has been invalidated.
	 */
	public boolean addListener(T listener) {
		checkState(true);
		
		ListenerData<T> data = new ListenerData<T>(listener);
		if (listeners.contains(data)) {
			log.warn("Attempting to add duplicate listener " + listener);
			return false;
		}
		listeners.add(data);
		return true;
	}
	
	
	/**
	 * Remove the specified listener from the list.  The listener is removed based on the
	 * quality operator ==, not by the equals() method.
	 * 
	 * @param listener	the listener to remove.
	 * @return			whether the listener was actually removed.
	 */
	public boolean removeListener(T listener) {
		checkState(false);
		
		Iterator<ListenerData<T>> iterator = listeners.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().listener == listener) {
				iterator.remove();
				log.trace("Removing listener " + listener);
				return true;
			}
		}
		log.info("Attempting to remove non-existant listener " + listener);
		return false;
	}
	
	
	/**
	 * Return the number of listeners in this list.
	 */
	public int getListenerCount() {
		return listeners.size();
	}
	
	
	/**
	 * Return an iterator that iterates of the listeners.  This iterator is backed by
	 * a copy of the iterator list, so {@link #addListener(Object)} and {@link #removeListener(Object)}
	 * may be called while iterating the list without effect on the iteration.  The returned
	 * iterator does not support the {@link Iterator#remove()} method.
	 */
	@Override
	public Iterator<T> iterator() {
		checkState(false);
		return new ListenerDataIterator();
	}
	
	/**
	 * Return the instantiation location of this listener list.
	 * @return	the location where this listener list was instantiated.
	 */
	public Throwable getInstantiationLocation() {
		return instantiationLocation;
	}
	
	
	/**
	 * Invalidate this listener list.  Invalidation removes all listeners from the list.
	 * After invalidation {@link #addListener(Object)} will throw an exception, the other
	 * methods produce a warning log message.
	 */
	@Override
	public void invalidate() {
		this.invalidated = new Throwable("Invalidation occurred at this point");
		if (!listeners.isEmpty()) {
			log.info("Invalidating " + this + " while still having listeners " + listeners);
		}
		listeners.clear();
	}
	
	
	public boolean isInvalidated() {
		return this.invalidated != null;
	}
	
	
	private void checkState(boolean error) {
		if (this.invalidated != null) {
			if (error) {
				throw new BugException(this + ": this ListenerList has been invalidated", invalidated);
			} else {
				log.warn(this + ": this ListenerList has been invalidated",
						new Throwable("ListenerList was attempted to be used here", invalidated));
			}
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ListenerList[");
		
		if (this.invalidated != null) {
			sb.append("INVALIDATED]");
			return sb.toString();
		}
		
		if (listeners.isEmpty()) {
			sb.append("empty");
		} else {
			boolean first = true;
			for (ListenerData<T> l : listeners) {
				if (!first) {
					sb.append("; ");
				}
				first = false;
				sb.append(l);
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	/**
	 * A class containing data about a listener.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 * @param <T>	the listener type
	 */
	public static class ListenerData<T> {
		private final T listener;
		private final long addTimestamp;
		private final Throwable addLocation;
		private long accessTimestamp;
		
		/**
		 * Sole constructor.
		 */
		private ListenerData(T listener) {
			if (listener == null) {
				throw new NullPointerException("listener is null");
			}
			this.listener = listener;
			this.addTimestamp = System.currentTimeMillis();
			this.accessTimestamp = this.addTimestamp;
			this.addLocation = new Throwable("Listener " + listener + " add position");
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ListenerData))
				return false;
			ListenerData<?> other = (ListenerData<?>) obj;
			return this.listener == other.listener;
		}
		
		@Override
		public int hashCode() {
			return listener.hashCode();
		}
		
		/**
		 * Return the listener.
		 */
		public T getListener() {
			return listener;
		}
		
		/**
		 * Return the millisecond timestamp when this listener was added to the
		 * listener list.
		 */
		public long getAddTimestamp() {
			return addTimestamp;
		}
		
		/**
		 * Return the location where this listener was added to the listener list.
		 */
		public Throwable getAddLocation() {
			return addLocation;
		}
		
		/**
		 * Return the millisecond timestamp when this listener was last accessed through
		 * the listener list iterator.
		 */
		public long getAccessTimestamp() {
			return accessTimestamp;
		}
	}
	
	
	private class ListenerDataIterator implements Iterator<T> {
		private final Iterator<ListenerData<T>> iterator = listeners.clone().iterator();
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public T next() {
			ListenerData<T> data = iterator.next();
			data.accessTimestamp = System.currentTimeMillis();
			return data.listener;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");
		}
	}
	
}
