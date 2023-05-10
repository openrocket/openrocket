package net.sf.openrocket.util;


/**
 * An interface defining an object firing ChangeEvents.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ChangeSource {
	
	public void addChangeListener(StateChangeListener listener);
	
	public void removeChangeListener(StateChangeListener listener);
	
}
