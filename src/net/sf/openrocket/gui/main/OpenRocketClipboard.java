package net.sf.openrocket.gui.main;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class OpenRocketClipboard {

	private static Object clipboard = null;
	
	private OpenRocketClipboard() {
		// Disallow instantiation
	}
	
	
	/**
	 * Return the <code>RocketComponent</code> contained in the clipboard, or
	 * <code>null</code>.
	 * 
	 * @return	the rocket component contained in the clipboard, or <code>null</code>
	 * 			if the clipboard does not currently contain a rocket component.
	 */
	public static RocketComponent getComponent() {
		if (clipboard instanceof RocketComponent) {
			return (RocketComponent) clipboard;
		}
		return null;
	}
	
	
	public static Simulation[] getSimulations() {
		return null; // TODO
	}
	
	
}
