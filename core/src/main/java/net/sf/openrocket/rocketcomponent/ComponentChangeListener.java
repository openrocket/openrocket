package net.sf.openrocket.rocketcomponent;

import java.util.EventListener;

public interface ComponentChangeListener extends EventListener {

	public void componentChanged(ComponentChangeEvent e);
	
}
