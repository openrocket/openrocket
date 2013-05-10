package net.sf.openrocket.util;


/**
 * A utility class helping an object to be made immutable after a certain point of time.
 * An object should contain an instance of Mutable and an immute() method which calls
 * {@link #immute()}.  Additionally, every method that changes the state of the object
 * should call {@link #check()} before modification.
 * <p>
 * This class also provides a stack trace of the position where the object was made
 * immutable to help in debugging modifications of immuted objects.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Mutable implements Cloneable {
	
	private Throwable immuteTrace = null;
	
	/**
	 * Mark the object immutable.  Once the object has been called the object
	 * cannot be made mutable again.  Repeated calls to this method have no effect.
	 */
	public void immute() {
		if (immuteTrace == null) {
			immuteTrace = new Throwable();
		}
	}
	
	/**
	 * Check that the object is still mutable, and throw an exception if it is not.
	 * <p>
	 * The thrown exception will contain a trace of the position where the object was made
	 * immutable to help in debugging.
	 * 
	 * @throws	IllegalStateException	if {@link #immute()} has been called for this object.
	 */
	public void check() {
		if (immuteTrace != null) {
			throw new IllegalStateException("Object has been made immutable at "
					+ immuteTrace.getMessage(), immuteTrace);
		}
	}
	
	/**
	 * Check whether this object is still mutable.
	 * 
	 * @return	whether this object is still mutable.
	 */
	public boolean isMutable() {
		return immuteTrace == null;
	}
	
	
	/**
	 * Return a new Mutable instance with the same state as the current object.
	 */
	@Override
	public Mutable clone() {
		try {
			return (Mutable) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException", e);
		}
	}
}
