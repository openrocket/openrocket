package net.sf.openrocket.util;

import java.util.EventListener;
import java.util.EventObject;

public interface StateChangeListener extends EventListener {

	public void stateChanged( EventObject e );

}
