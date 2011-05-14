package net.sf.openrocket.util;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Abstract implementation of a ChangeSource.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractChangeSource implements ChangeSource {
	private static final LogHelper log = Application.getLogger();
	
	private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	private final ChangeEvent event = new ChangeEvent(this);
	
	
	@Override
	public final void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
		log.verbose(1, "Adding change listeners, listener count is now " + listeners.size());
	}
	
	@Override
	public final void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
		log.verbose(1, "Removing change listeners, listener count is now " + listeners.size());
	}
	
	
	/**
	 * Fire a change event to all listeners.
	 */
	protected void fireChangeEvent() {
		ChangeListener[] array = listeners.toArray(new ChangeListener[0]);
		
		for (ChangeListener l : array) {
			l.stateChanged(event);
		}
	}
}
