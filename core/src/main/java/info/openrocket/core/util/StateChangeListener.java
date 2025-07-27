package info.openrocket.core.util;

import java.util.EventListener;
import java.util.EventObject;

public interface StateChangeListener extends EventListener {

	public void stateChanged(EventObject e);

}
