package info.openrocket.core.rocketcomponent;

import java.util.EventListener;

public interface ComponentChangeListener extends EventListener {

	public void componentChanged(ComponentChangeEvent e);

}
