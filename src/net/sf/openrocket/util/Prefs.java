package net.sf.openrocket.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.RK4Simulator;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.unit.UnitGroup;


public class Prefs {

	/**
	 * Whether to use the debug-node instead of the normal node.
	 */
	public static final boolean DEBUG = false;
	
	/**
	 * Whether to clear all preferences at application startup.  This has an effect only
	 * if DEBUG is true.
	 */
	public static final boolean CLEARPREFS = true;
	
	/**
	 * The node name to use in the Java preferences storage.
	 */
	public static final String NODENAME = (DEBUG?"OpenRocket-debug":"OpenRocket");
	
	
	
	private static final String VERSION = "0.9.0";
	
	
	public static final String BODY_COMPONENT_INSERT_POSITION_KEY = "BodyComponentInsertPosition";
	
	
	public static final String CONFIRM_DELETE_SIMULATION = "ConfirmDeleteSimulation";

	
	/**
	 * Node to this application's preferences.
	 */
	public static final Preferences NODE;
	
	
	static {
		Preferences root = Preferences.userRoot();
		if (DEBUG && CLEARPREFS) {
			try {
				if (root.nodeExists(NODENAME)) {
					root.node(NODENAME).removeNode();
				}
			} catch (BackingStoreException e) {
				throw new RuntimeException("Unable to clear preference node",e);
			}
		}
		NODE = root.node(NODENAME);
	}
	
	
	
	
	/////////  Default component attributes
	
	private static final HashMap<Class<?>,String> DEFAULT_COLORS = 
		new HashMap<Class<?>,String>();
	static {
		DEFAULT_COLORS.put(BodyComponent.class, "0,0,240");
		DEFAULT_COLORS.put(FinSet.class, "0,0,200");
		DEFAULT_COLORS.put(LaunchLug.class, "0,0,180");
		DEFAULT_COLORS.put(InternalComponent.class, "170,0,100");
		DEFAULT_COLORS.put(MassObject.class, "0,0,0");
		DEFAULT_COLORS.put(RecoveryDevice.class, "255,0,0");
	}
	
	
	private static final HashMap<Class<?>,String> DEFAULT_LINE_STYLES = 
		new HashMap<Class<?>,String>();
	static {
		DEFAULT_LINE_STYLES.put(RocketComponent.class, LineStyle.SOLID.name());
		DEFAULT_LINE_STYLES.put(MassObject.class, LineStyle.DASHED.name());
	}
	
	
	private static final Material DEFAULT_LINE_MATERIAL = 
		Databases.findMaterial(Material.Type.LINE, "Elastic cord (round 2mm, 1/16 in)", 0.0018);
	private static final Material DEFAULT_SURFACE_MATERIAL = 
		Databases.findMaterial(Material.Type.SURFACE, "Ripstop nylon", 0.067);
	private static final Material DEFAULT_BULK_MATERIAL = 
		Databases.findMaterial(Material.Type.BULK, "Cardboard", 680);

	
	//////////////////////
	
	
	public static String getVersion() {
		return VERSION;
	}
	
	
	
	public static void storeVersion() {
		NODE.put("OpenRocketVersion", getVersion());
	}
	
	
	/**
	 * Returns a limited-range integer value from the preferences.  If the value 
	 * in the preferences is negative or greater than max, then the default value 
	 * is returned.
	 * 
	 * @param key  The preference to retrieve.
	 * @param max  Maximum allowed value for the choice.
	 * @param def  Default value.
	 * @return   The preference value.
	 */
	public static int getChoise(String key, int max, int def) {
		int v = NODE.getInt(key, def);
		if ((v<0) || (v>max))
			return def;
		return v;
	}
	
	
	/**
	 * Helper method that puts an integer choice value into the preferences.
	 * 
	 * @param key     the preference key.
	 * @param value   the value to store.
	 */
	public static void putChoise(String key, int value) {
		NODE.putInt(key, value);
		storeVersion();
	}
	
	
	
	public static String getString(String key, String def) {
		return NODE.get(key, def);
	}
	
	public static void putString(String key, String value) {
		NODE.put(key, value);
		storeVersion();
	}
	

	
	
	//////////////////
	
	public static File getDefaultDirectory() {
		String file = NODE.get("defaultDirectory", null);
		if (file == null)
			return null;
		return new File(file);
	}
	
	public static void setDefaultDirectory(File dir) {
		String d;
		if (dir == null) {
			d = null;
		} else {
			d = dir.getAbsolutePath();
		}
		NODE.put("defaultDirectory", d);
		storeVersion();
	}
	
	
	
	public static Color getDefaultColor(Class<? extends RocketComponent> c) {
		String color = get("componentColors", c, DEFAULT_COLORS);
		if (color == null)
			return Color.BLACK;

		String[] rgb = color.split(",");
		if (rgb.length==3) {
			try {
				int red = MathUtil.clamp(Integer.parseInt(rgb[0]),0,255);
				int green = MathUtil.clamp(Integer.parseInt(rgb[1]),0,255);
				int blue = MathUtil.clamp(Integer.parseInt(rgb[2]),0,255);
				return new Color(red,green,blue);
			} catch (NumberFormatException ignore) { }
		}

		return Color.BLACK;
	}
	
	public static void setDefaultColor(Class<? extends RocketComponent> c, Color color) {
		if (color==null)
			return;
		String string = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		set("componentColors", c, string);
	}
	
	public static Color getMotorBorderColor() {
		// TODO: MEDIUM:  Motor color (settable?)
		return new Color(0,0,0,200);
	}

	
	public static Color getMotorFillColor() {
		// TODO: MEDIUM:  Motor fill color (settable?)
		return new Color(0,0,0,100);
	}
	
	
	public static LineStyle getDefaultLineStyle(Class<? extends RocketComponent> c) {
		String value = get("componentStyle", c, DEFAULT_LINE_STYLES);
		try {
			return LineStyle.valueOf(value);
		} catch (Exception e) {
			return LineStyle.SOLID;
		}
	}
	
	public static void setDefaultLineStyle(Class<? extends RocketComponent> c,
			LineStyle style) {
		if (style == null)
			return;
		set("componentStyle", c, style.name());
	}
	

	/**
	 * Return the DPI setting of the monitor.  This is either the setting provided
	 * by the system or a user-specified DPI setting.
	 * 
	 * @return    the DPI setting to use.
	 */
	public static double getDPI() {
		int dpi = NODE.getInt("DPI", 0);  // Tenths of a dpi
		
		if (dpi < 10) {
			dpi = Toolkit.getDefaultToolkit().getScreenResolution()*10;
		}
		if (dpi < 10)
			dpi = 960;
		
		return ((double)dpi)/10.0;
	}
	
	
	public static double getDefaultMach() {
		// TODO: HIGH: implement custom default mach number
		return 0.3;
	}
	
	
	
	
	public static Material getDefaultComponentMaterial(
			Class<? extends RocketComponent> componentClass,
			Material.Type type) {
		
		String material = get("componentMaterials", componentClass, null);
		if (material != null) {
			try {
				Material m = Material.fromStorableString(material);
				if (m.getType() == type)
					return m;
			} catch (IllegalArgumentException ignore) { }
		}
		
		switch (type) {
		case LINE:
			return DEFAULT_LINE_MATERIAL;
		case SURFACE:
			return DEFAULT_SURFACE_MATERIAL;
		case BULK:
			return DEFAULT_BULK_MATERIAL;
		}
		throw new IllegalArgumentException("Unknown material type: "+type);
	}
	
	public static void setDefaultComponentMaterial(
			Class<? extends RocketComponent> componentClass, Material material) {
		
		set("componentMaterials", componentClass, 
				material==null ? null : material.toStorableString());
	}
	
	
	public static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	
	
	public static Point getWindowPosition(Class<?> c) {
		int x, y;
		String pref = NODE.node("windows").get("position." + c.getCanonicalName(), null);
		
		if (pref == null)
			return null;
		
		if (pref.indexOf(',')<0)
			return null;
		
		try {
			x = Integer.parseInt(pref.substring(0,pref.indexOf(',')));
			y = Integer.parseInt(pref.substring(pref.indexOf(',')+1));
		} catch (NumberFormatException e) {
			return null;
		}
		return new Point(x,y);
	}
	
	public static void setWindowPosition(Class<?> c, Point p) {
		NODE.node("windows").put("position." + c.getCanonicalName(), "" + p.x + "," + p.y);
		storeVersion();
	}
	
	
	

	public static Dimension getWindowSize(Class<?> c) {
		int x, y;
		String pref = NODE.node("windows").get("size." + c.getCanonicalName(), null);
		
		if (pref == null)
			return null;
		
		if (pref.indexOf(',')<0)
			return null;
		
		try {
			x = Integer.parseInt(pref.substring(0,pref.indexOf(',')));
			y = Integer.parseInt(pref.substring(pref.indexOf(',')+1));
		} catch (NumberFormatException e) {
			return null;
		}
		return new Dimension(x,y);
	}
	
	public static void setWindowSize(Class<?> c, Dimension d) {
		NODE.node("windows").put("size." + c.getCanonicalName(), "" + d.width + "," + d.height);
		storeVersion();
	}
	
	
	////  Background flight data computation
	
	public static boolean computeFlightInBackground() {
		return NODE.getBoolean("backgroundFlight", true);
	}
	
	public static Simulation getBackgroundSimulation(Rocket rocket) {
		Simulation s = new Simulation(rocket);
		SimulationConditions cond = s.getConditions();
		
		cond.setTimeStep(RK4Simulator.RECOMMENDED_TIME_STEP*2);
		cond.setWindSpeedAverage(1.0);
		cond.setWindSpeedDeviation(0.1);
		cond.setLaunchRodLength(5);
		return s;
	}
	
	
	
	
	/////////  Default unit storage
	
	public static void loadDefaultUnits() {
		Preferences prefs = NODE.node("units");
		try {
			
			for (String key: prefs.keys()) {
				UnitGroup group = UnitGroup.UNITS.get(key);
				if (group == null)
					continue;
				
				group.setDefaultUnit(prefs.get(key, null));
			}
			
		} catch (BackingStoreException e) {
			System.err.println("BackingStoreException:");
			e.printStackTrace();
		}
	}
	
	public static void storeDefaultUnits() {
		Preferences prefs = NODE.node("units");
		
		for (String key: UnitGroup.UNITS.keySet()) {
			UnitGroup group = UnitGroup.UNITS.get(key);
			if (group == null || group.getUnitCount() < 2)
				continue;
			
			prefs.put(key, group.getDefaultUnit().getUnit());
		}
	}
	
	
	
	////  Helper methods
	
	private static String get(String directory, 
			Class<? extends RocketComponent> componentClass,
			Map<Class<?>, String> defaultMap) {

		// Search preferences
		Class<?> c = componentClass;
		Preferences prefs = NODE.node(directory);
		while (c!=null && RocketComponent.class.isAssignableFrom(c)) {
			String value = prefs.get(c.getSimpleName(), null);
			if (value != null)
				return value;
			c = c.getSuperclass();
		}
		
		if (defaultMap == null)
			return null;

		// Search defaults
		c = componentClass;
		while (RocketComponent.class.isAssignableFrom(c)) {
			String value = defaultMap.get(c);
			if (value != null)
				return value;
			c = c.getSuperclass();
		}
		
		return null;
	}


	private static void set(String directory, Class<? extends RocketComponent> componentClass,
			String value) {
		Preferences prefs = NODE.node(directory);
		if (value == null)
			prefs.remove(componentClass.getSimpleName());
		else
			prefs.put(componentClass.getSimpleName(), value);
		storeVersion();
	}

}
