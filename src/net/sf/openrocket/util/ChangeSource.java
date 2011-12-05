package net.sf.openrocket.util;

import java.util.EventListener;

/**
 * An interface defining an object firing ChangeEvents.  Why isn't this included in the Java API??
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ChangeSource {

	public void addChangeListener(EventListener listener);
	public void removeChangeListener(EventListener listener);
	
}
