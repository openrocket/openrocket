package net.sf.openrocket.util;

import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Abstract implementation of a ChangeSource.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractChangeSource implements ChangeSource {
	private static final LogHelper log = Application.getLogger();
	
	private final List<EventListener> listeners = new ArrayList<EventListener>();
	
	private final EventObject event = new EventObject(this);
	
	
	@Override
	public final void addChangeListener(EventListener listener) {
		listeners.add(listener);
		log.verbose(1, "Adding change listeners, listener count is now " + listeners.size());
	}
	
	@Override
	public final void removeChangeListener(EventListener listener) {
		listeners.remove(listener);
		log.verbose(1, "Removing change listeners, listener count is now " + listeners.size());
	}
	
	
	/**
	 * Fire a change event to all listeners.
	 */
	protected void fireChangeEvent() {
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if ( l instanceof StateChangeListener ) {
				((StateChangeListener)l).stateChanged(event);
			}
		}
	}
}
