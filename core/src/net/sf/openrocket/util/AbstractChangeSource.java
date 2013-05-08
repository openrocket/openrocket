package net.sf.openrocket.util;

import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a ChangeSource.  Can either be extended
 * or used as a helper object.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class AbstractChangeSource implements ChangeSource {
	private static final Logger log = LoggerFactory.getLogger(AbstractChangeSource.class);
	
	private final List<EventListener> listeners = new ArrayList<EventListener>();
	
	
	@Override
	public final void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
		log.trace("Adding change listeners, listener count is now " + listeners.size());
	}
	
	@Override
	public final void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
		log.trace("Removing change listeners, listener count is now " + listeners.size());
	}
	
	public void fireChangeEvent(Object source) {
		EventObject event = new EventObject(source);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
		
	}
	
	/**
	 * Fire a change event to all listeners.
	 */
	protected void fireChangeEvent() {
		fireChangeEvent(this);
	}
}
