package net.sf.openrocket.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * A class that performs certain memory-management operations for debugging purposes.
 * For example, complex objects that are being discarded and that should be garbage-collectable
 * (such as dialog windows) should be registered for monitoring by calling
 * {@link #collectable(Object)}.  This will allow monitoring whether the object really is
 * garbage-collected or whether it is retained in memory due to a memory leak.
 * Only complex objects should be registered due to the overhead of the monitoring.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MemoryManagement {
	private static final LogHelper log = Application.getLogger();
	
	/** Purge cleared references every this many calls to {@link #collectable(Object)} */
	private static final int PURGE_CALL_COUNT = 100;
	

	/**
	 * Storage of the objects.  This is basically a mapping from the objects (using weak references)
	 * to 
	 */
	private static List<MemoryData> objects = new LinkedList<MemoryData>();
	private static int callCount = 0;
	
	
	private MemoryManagement() {
	}
	
	
	/**
	 * Mark an object that should be garbage-collectable by the GC.  This class will monitor
	 * whether the object actually gets garbage-collected or not by holding a weak reference
	 * to the object.
	 * 
	 * @param o		the object to monitor.
	 */
	public static synchronized void collectable(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("object is null");
		}
		log.debug("Adding object into collectable list: " + o);
		objects.add(new MemoryData(o));
		callCount++;
		if (callCount % PURGE_CALL_COUNT == 0) {
			purge();
		}
	}
	
	
	/**
	 * Return the number of times {@link #collectable(Object)} has been called.
	 * @return	the number of times {@link #collectable(Object)} has been called.
	 */
	public static synchronized int getCallCount() {
		return callCount;
	}
	
	
	/**
	 * Return a list of MemoryData objects corresponding to the objects that have been
	 * registered by {@link #collectable(Object)} and have not been garbage-collected properly.
	 * This method first calls <code>System.gc()</code> multiple times to attempt to
	 * force any remaining garbage collection.
	 * 
	 * @return	a list of MemoryData objects for objects that have not yet been garbage-collected.
	 */
	public static synchronized ArrayList<MemoryData> getRemainingObjects() {
		for (int i = 0; i < 5; i++) {
			System.runFinalization();
			System.gc();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		purge();
		return new ArrayList<MemoryData>(objects);
	}
	
	

	/**
	 * Purge all cleared references from the object list.
	 */
	private static void purge() {
		int origCount = objects.size();
		Iterator<MemoryData> iterator = objects.iterator();
		while (iterator.hasNext()) {
			MemoryData data = iterator.next();
			if (data.getReference().get() == null) {
				iterator.remove();
			}
		}
		log.debug(objects.size() + " of " + origCount + " objects remaining in discarded objects list after purge.");
	}
	
	
	/**
	 * A value object class containing data of a discarded object reference.
	 */
	public static final class MemoryData {
		private final WeakReference<Object> reference;
		private final long registrationTime;
		
		private MemoryData(Object object) {
			this.reference = new WeakReference<Object>(object);
			this.registrationTime = System.currentTimeMillis();
		}
		
		/**
		 * Return the weak reference to the discarded object.
		 */
		public WeakReference<Object> getReference() {
			return reference;
		}
		
		/**
		 * Return the time when the object was discarded.
		 * @return	a millisecond timestamp of when the object was discarded.
		 */
		public long getRegistrationTime() {
			return registrationTime;
		}
	}
	
}
