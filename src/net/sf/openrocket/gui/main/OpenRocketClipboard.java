package net.sf.openrocket.gui.main;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public final class OpenRocketClipboard {

	private static RocketComponent clipboardComponent = null;
	private static Simulation[] clipboardSimulations = null;
	
	private static List<ClipboardListener> listeners = new ArrayList<ClipboardListener>();
	
	private OpenRocketClipboard() {
		// Disallow instantiation
	}
	
	
	/**
	 * Return the <code>RocketComponent</code> contained in the clipboard, or
	 * <code>null</code>.  The component is returned verbatim, and must be copied
	 * before attaching to any rocket design!
	 * 
	 * @return	the rocket component contained in the clipboard, or <code>null</code>
	 * 			if the clipboard does not currently contain a rocket component.
	 */
	public static RocketComponent getClipboardComponent() {
		return clipboardComponent;
	}
	
	
	public static void setClipboard(RocketComponent component) {
		clipboardComponent = component;
		clipboardSimulations = null;
		fireClipboardChanged();
	}
	
	
	public static Simulation[] getClipboardSimulations() {
		if (clipboardSimulations == null || clipboardSimulations.length == 0)
			return null;
		return clipboardSimulations.clone();
	}
	
	public static void setClipboard(Simulation[] simulations) {
		clipboardSimulations = simulations.clone();
		clipboardComponent = null;
		fireClipboardChanged();
	}
	
	
	
	public static void addClipboardListener(ClipboardListener listener) {
		listeners.add(listener);
	}
	
	public static void removeClipboardListener(ClipboardListener listener) {
		listeners.remove(listener);
	}
	
	private static void fireClipboardChanged() {
		ClipboardListener[] array = listeners.toArray(new ClipboardListener[0]);
		for (ClipboardListener l: array) {
			l.clipboardChanged();
		}
	}
	
}
