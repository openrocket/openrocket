package net.sf.openrocket.util;

/**
 * An object that can be invalidated (in some sense of the word).  After calling the
 * invalidate method the object should not be used any more and it may enforce
 * disusage for certain methods.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Invalidatable {
	
	/**
	 * Invalidate this object.
	 */
	public void invalidate();
	
}
