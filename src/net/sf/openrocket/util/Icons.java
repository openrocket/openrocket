package net.sf.openrocket.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.openrocket.document.Simulation;


public class Icons {

	/**
	 * Icons used for showing the status of a simulation (up to date, out of date, etc).
	 */
	public static final Map<Simulation.Status, Icon> SIMULATION_STATUS_ICON_MAP;
	static {
		HashMap<Simulation.Status, Icon> map = new HashMap<Simulation.Status, Icon>();
		map.put(Simulation.Status.NOT_SIMULATED, new ImageIcon("pix/spheres/gray-16x16.png", "Not simulated"));
		map.put(Simulation.Status.UPTODATE, new ImageIcon("pix/spheres/green-16x16.png", "Up to date"));
		map.put(Simulation.Status.LOADED, new ImageIcon("pix/spheres/yellow-16x16.png", "Loaded from file"));
		map.put(Simulation.Status.OUTDATED, new ImageIcon("pix/spheres/red-16x16.png", "Out-of-date"));
		map.put(Simulation.Status.EXTERNAL, new ImageIcon("pix/spheres/blue-16x16.png", "Imported data"));
		SIMULATION_STATUS_ICON_MAP = Collections.unmodifiableMap(map);
	}
	
	public static final Icon SIMULATION_LISTENER_OK;
	public static final Icon SIMULATION_LISTENER_ERROR;
	static {
		SIMULATION_LISTENER_OK = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.UPTODATE);
		SIMULATION_LISTENER_ERROR = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.OUTDATED);
	}


	public static final Icon FILE_NEW = new ImageIcon(ClassLoader.getSystemResource("pix/icons/document-new.png"), "New document");
	public static final Icon FILE_OPEN = new ImageIcon(ClassLoader.getSystemResource("pix/icons/document-open.png"), "Open document");
	public static final Icon FILE_SAVE = new ImageIcon(ClassLoader.getSystemResource("pix/icons/document-save.png"), "Save document");
	public static final Icon FILE_SAVE_AS = new ImageIcon(ClassLoader.getSystemResource("pix/icons/document-save-as.png"), "Save document as");
	public static final Icon FILE_CLOSE = new ImageIcon(ClassLoader.getSystemResource("pix/icons/document-close.png"), "Close document");
	public static final Icon FILE_QUIT = new ImageIcon(ClassLoader.getSystemResource("pix/icons/application-exit.png"), "Quit OpenRocket");
	
	public static final Icon EDIT_UNDO = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-undo.png"), "Undo");
	public static final Icon EDIT_REDO = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-redo.png"), "Redo");
	public static final Icon EDIT_CUT = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-cut.png"), "Cut");
	public static final Icon EDIT_COPY = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-copy.png"), "Copy");
	public static final Icon EDIT_PASTE = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-paste.png"), "Paste");
	public static final Icon EDIT_DELETE = new ImageIcon(ClassLoader.getSystemResource("pix/icons/edit-delete.png"), "Delete");

	public static final Icon ZOOM_IN = new ImageIcon(ClassLoader.getSystemResource("pix/icons/zoom-in.png"), "Zoom in");
	public static final Icon ZOOM_OUT = new ImageIcon(ClassLoader.getSystemResource("pix/icons/zoom-out.png"), "Zoom out");

	public static final Icon PREFERENCES = new ImageIcon(ClassLoader.getSystemResource("pix/icons/preferences.png"), "Preferences");

}
