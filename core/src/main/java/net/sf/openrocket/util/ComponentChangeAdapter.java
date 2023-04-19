package net.sf.openrocket.util;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;

/**
 * An adapter class which allows a StateChangeListener to act as
 * a ComponentChangeListener.
 */
public class ComponentChangeAdapter implements ComponentChangeListener {
	
	private final StateChangeListener listener;
	
	public ComponentChangeAdapter(StateChangeListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		listener.stateChanged(e);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentChangeAdapter other = (ComponentChangeAdapter) obj;
		return this.listener.equals(other.listener);
	}
	
	
	@Override
	public int hashCode() {
		return listener.hashCode();
	}
	
	
}
