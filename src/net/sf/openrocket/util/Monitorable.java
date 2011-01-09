package net.sf.openrocket.util;

/**
 * Interface describing objects whose state changes can be monitored based on
 * a modification ID number.  If a specific object has the same modification ID
 * at two different points in time, the object state is guaranteed to be the same.
 * This does not necessarily hold between two different instances of an object type.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Monitorable {
	
	/**
	 * Return a modification ID unique to the current state of this object and contained objects.
	 * The general contract is that if a specific object has the same modification ID at two moments
	 * in time, then the state of the object has not changed in between.  Additionally the
	 * modification ID value must be monotonically increasing.  This value can be used as a monitor
	 * to whether an object has been changed between two points of time.
	 * <p>
	 * Implementations may optionally fulfill the stronger requirement that any two objects of the same
	 * type that have the same modification ID will be equal, though for complex objects guaranteeing
	 * this may be impractical.
	 * <p>
	 * Objects that contain only primitive types or immutable objects can implement this method by
	 * increasing a modification counter or retrieving a new unique ID every time a value is set.
	 * <p>
	 * Objects that contain other objects with a mutable state may for example return the sum of the
	 * object's own modification ID, a modification ID counter (initially zero) and the modification IDs
	 * of the contained objects.  When a mutable object is set, the modification counter is increased by
	 * the modification ID of the current object in order to preserve monotonicity.
	 * <p>
	 * If an object does not have any fields, this method can simply return zero.
	 * <p>
	 * Cloned objects may or may not have the same modification ID as the original object.
	 * 
	 * @return		a modification ID value for this object.
	 * @see			UniqueID#next()
	 */
	public int getModID();
	
}
