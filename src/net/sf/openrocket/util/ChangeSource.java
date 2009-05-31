package net.sf.openrocket.util;

import javax.swing.event.ChangeListener;

/**
 * An interface defining an object firing ChangeEvents.  Why isn't this included in the Java API??
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ChangeSource {

	public void addChangeListener(ChangeListener listener);
	public void removeChangeListener(ChangeListener listener);
	
}
