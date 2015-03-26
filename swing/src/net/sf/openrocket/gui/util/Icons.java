package net.sf.openrocket.gui.util;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Icons {
	private static final Logger log = LoggerFactory.getLogger(Icons.class);
	private static final Translator trans = Application.getTranslator();
	
	static {
		log.debug("Starting to load icons");
	}
	
	/**
	 * Icons used for showing the status of a simulation (up to date, out of date, etc).
	 */
	public static final Map<Simulation.Status, Icon> SIMULATION_STATUS_ICON_MAP;
	static {
		HashMap<Simulation.Status, Icon> map = new HashMap<Simulation.Status, Icon>();
		map.put(Simulation.Status.NOT_SIMULATED, loadImageIcon("pix/spheres/gray-16x16.png", "Not simulated"));
		map.put(Simulation.Status.CANT_RUN, loadImageIcon("pix/spheres/yellow-16x16.png", "Can't run, no motors assigned."));
		map.put(Simulation.Status.UPTODATE, loadImageIcon("pix/spheres/green-16x16.png", "Up to date"));
		map.put(Simulation.Status.LOADED, loadImageIcon("pix/spheres/yellow-16x16.png", "Loaded from file"));
		map.put(Simulation.Status.OUTDATED, loadImageIcon("pix/spheres/red-16x16.png", "Out-of-date"));
		map.put(Simulation.Status.EXTERNAL, loadImageIcon("pix/spheres/blue-16x16.png", "Imported data"));
		SIMULATION_STATUS_ICON_MAP = Collections.unmodifiableMap(map);
	}
	
	public static final Icon SIMULATION_LISTENER_OK;
	public static final Icon SIMULATION_LISTENER_ERROR;
	static {
		SIMULATION_LISTENER_OK = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.UPTODATE);
		SIMULATION_LISTENER_ERROR = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.OUTDATED);
	}
	
	
	public static final Icon FILE_NEW = loadImageIcon("pix/icons/document-new.png", "New document");
	public static final Icon FILE_OPEN = loadImageIcon("pix/icons/document-open.png", "Open document");
	public static final Icon FILE_OPEN_EXAMPLE = loadImageIcon("pix/icons/document-open-example.png", "Open example document");
	public static final Icon FILE_SAVE = loadImageIcon("pix/icons/document-save.png", "Save document");
	public static final Icon FILE_SAVE_AS = loadImageIcon("pix/icons/document-save-as.png", "Save document as");
	public static final Icon FILE_PRINT = loadImageIcon("pix/icons/document-print.png", "Print document");
	public static final Icon FILE_CLOSE = loadImageIcon("pix/icons/document-close.png", "Close document");
	public static final Icon FILE_QUIT = loadImageIcon("pix/icons/application-exit.png", "Quit OpenRocket");
	
	public static final Icon EDIT_UNDO = loadImageIcon("pix/icons/edit-undo.png", trans.get("Icons.Undo"));
	public static final Icon EDIT_REDO = loadImageIcon("pix/icons/edit-redo.png", trans.get("Icons.Redo"));
	public static final Icon EDIT_CUT = loadImageIcon("pix/icons/edit-cut.png", "Cut");
	public static final Icon EDIT_COPY = loadImageIcon("pix/icons/edit-copy.png", "Copy");
	public static final Icon EDIT_PASTE = loadImageIcon("pix/icons/edit-paste.png", "Paste");
	public static final Icon EDIT_DELETE = loadImageIcon("pix/icons/edit-delete.png", "Delete");
	public static final Icon EDIT_SCALE = loadImageIcon("pix/icons/edit-scale.png", "Scale");
	
	public static final Icon HELP_ABOUT = loadImageIcon("pix/icons/help-about.png", "About");
	public static final Icon HELP_LICENSE = loadImageIcon("pix/icons/help-license.png", "License");
	public static final Icon HELP_BUG_REPORT = loadImageIcon("pix/icons/help-bug.png", "Bug report");
	public static final Icon HELP_DEBUG_LOG = loadImageIcon("pix/icons/help-log.png", "Debug log");
	public static final Icon HELP_TOURS = loadImageIcon("pix/icons/help-tours.png", "Guided tours");
	
	public static final Icon ZOOM_IN = loadImageIcon("pix/icons/zoom-in.png", "Zoom in");
	public static final Icon ZOOM_OUT = loadImageIcon("pix/icons/zoom-out.png", "Zoom out");
	public static final Icon ZOOM_RESET = loadImageIcon("pix/icons/zoom-reset.png", "Reset Zoom & Pan");
	
	public static final Icon PREFERENCES = loadImageIcon("pix/icons/preferences.png", "Preferences");
	
	public static final Icon DELETE = loadImageIcon("pix/icons/delete.png", "Delete");
	public static final Icon EDIT = loadImageIcon("pix/icons/pencil.png", "Edit");
	public static final Icon CONFIGURE = loadImageIcon("pix/icons/configure.png", "Configure");
	public static final Icon HELP = loadImageIcon("pix/icons/help-about.png", "Help");
	public static final Icon UP = loadImageIcon("pix/icons/up.png", "Up");
	public static final Icon DOWN = loadImageIcon("pix/icons/down.png", "Down");
	
	public static final Icon NOT_FAVORITE = loadImageIcon("pix/icons/star_silver.png", "Not favorite");
	public static final Icon FAVORITE = loadImageIcon("pix/icons/star_gold.png", "Favorite");
	
	public static final Icon CG_OVERRIDE = loadImageIcon("pix/icons/cg-override.png", "CG Override");
	public static final Icon MASS_OVERRIDE = loadImageIcon("pix/icons/mass-override.png", "Mass Override");
	
	
	static {
		log.debug("Icons loaded");
	}
	
	/**
	 * Load an ImageIcon from the specified file.  The file is obtained as a system
	 * resource from the normal classpath.  If the file cannot be loaded a bug dialog
	 * is opened and <code>null</code> is returned.
	 * 
	 * @param file	the file to load.
	 * @param name	the description of the icon.
	 * @return		the ImageIcon, or null if could not be loaded (after the user closes the dialog)
	 */
	public static ImageIcon loadImageIcon(String file, String name) {
		if (System.getProperty("openrocket.unittest") != null) {
			return new ImageIcon();
		}
		
		URL url = ClassLoader.getSystemResource(file);
		if (url == null) {
			Application.getExceptionHandler().handleErrorCondition("Image file " + file + " not found, ignoring.");
			return null;
		}
		return new ImageIcon(url, name);
	}
}
