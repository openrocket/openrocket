package net.sf.openrocket.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.gui.print.PrintSettings;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.GUISimulationConditions;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


public class Prefs {
	private static final LogHelper log = Application.getLogger();
	
	private static final String SPLIT_CHARACTER = "|";
	

	private static final List<Locale> SUPPORTED_LOCALES;
	static {
		List<Locale> list = new ArrayList<Locale>();
		for (String lang : new String[] { "en", "de", "es", "fr" }) {
			list.add(new Locale(lang));
		}
		SUPPORTED_LOCALES = Collections.unmodifiableList(list);
	}
	

	/**
	 * Whether to use the debug-node instead of the normal node.
	 */
	private static final boolean DEBUG;
	static {
		DEBUG = (System.getProperty("openrocket.debug.prefs") != null);
	}
	
	/**
	 * Whether to clear all preferences at application startup.  This has an effect only
	 * if DEBUG is true.
	 */
	private static final boolean CLEARPREFS = true;
	
	/**
	 * The node name to use in the Java preferences storage.
	 */
	private static final String NODENAME = (DEBUG ? "OpenRocket-debug" : "OpenRocket");
	
	
	/*
	 * Load property file only when necessary.
	 */
	private static class BuildPropertyHolder {
		
		public static final Properties PROPERTIES;
		public static final String BUILD_VERSION;
		public static final String BUILD_SOURCE;
		public static final boolean DEFAULT_CHECK_UPDATES;
		
		static {
			try {
				InputStream is = ClassLoader.getSystemResourceAsStream("build.properties");
				if (is == null) {
					throw new MissingResourceException(
							"build.properties not found, distribution built wrong" +
									"   classpath:" + System.getProperty("java.class.path"),
							"build.properties", "build.version");
				}
				
				PROPERTIES = new Properties();
				PROPERTIES.load(is);
				is.close();
				
				String version = PROPERTIES.getProperty("build.version");
				if (version == null) {
					throw new MissingResourceException(
							"build.version not found in property file",
							"build.properties", "build.version");
				}
				BUILD_VERSION = version.trim();
				
				BUILD_SOURCE = PROPERTIES.getProperty("build.source");
				if (BUILD_SOURCE == null) {
					throw new MissingResourceException(
							"build.source not found in property file",
							"build.properties", "build.source");
				}
				
				String value = PROPERTIES.getProperty("build.checkupdates");
				if (value != null)
					DEFAULT_CHECK_UPDATES = Boolean.parseBoolean(value);
				else
					DEFAULT_CHECK_UPDATES = true;
				
			} catch (IOException e) {
				throw new MissingResourceException(
						"Error reading build.properties",
						"build.properties", "build.version");
			}
		}
	}
	
	public static final String BODY_COMPONENT_INSERT_POSITION_KEY = "BodyComponentInsertPosition";
	
	public static final String USER_THRUST_CURVES_KEY = "UserThrustCurves";
	
	public static final String CONFIRM_DELETE_SIMULATION = "ConfirmDeleteSimulation";
	
	// Preferences related to data export
	public static final String EXPORT_FIELD_SEPARATOR = "ExportFieldSeparator";
	public static final String EXPORT_SIMULATION_COMMENT = "ExportSimulationComment";
	public static final String EXPORT_FIELD_NAME_COMMENT = "ExportFieldDescriptionComment";
	public static final String EXPORT_EVENT_COMMENTS = "ExportEventComments";
	public static final String EXPORT_COMMENT_CHARACTER = "ExportCommentCharacter";
	
	public static final String PLOT_SHOW_POINTS = "ShowPlotPoints";
	
	private static final String CHECK_UPDATES = "CheckUpdates";
	public static final String LAST_UPDATE = "LastUpdateVersion";
	
	public static final String MOTOR_DIAMETER_FILTER = "MotorDiameterMatch";
	public static final String MOTOR_HIDE_SIMILAR = "MotorHideSimilar";
	

	// Node names
	public static final String PREFERRED_THRUST_CURVE_MOTOR_NODE = "preferredThrustCurveMotors";
	

	/**
	 * Node to this application's preferences.
	 * @deprecated  Use the static methods instead.
	 */
	@Deprecated
	public static final Preferences NODE;
	private static final Preferences PREFNODE;
	

	// Clear the preferences if debug mode and clearprefs is defined
	static {
		Preferences root = Preferences.userRoot();
		if (DEBUG && CLEARPREFS) {
			try {
				if (root.nodeExists(NODENAME)) {
					root.node(NODENAME).removeNode();
				}
			} catch (BackingStoreException e) {
				throw new BugException("Unable to clear preference node", e);
			}
		}
		PREFNODE = root.node(NODENAME);
		NODE = PREFNODE;
	}
	



	/////////  Default component attributes
	
	private static final HashMap<Class<?>, String> DEFAULT_COLORS =
			new HashMap<Class<?>, String>();
	static {
		DEFAULT_COLORS.put(BodyComponent.class, "0,0,240");
		DEFAULT_COLORS.put(FinSet.class, "0,0,200");
		DEFAULT_COLORS.put(LaunchLug.class, "0,0,180");
		DEFAULT_COLORS.put(InternalComponent.class, "170,0,100");
		DEFAULT_COLORS.put(MassObject.class, "0,0,0");
		DEFAULT_COLORS.put(RecoveryDevice.class, "255,0,0");
	}
	

	private static final HashMap<Class<?>, String> DEFAULT_LINE_STYLES =
			new HashMap<Class<?>, String>();
	static {
		DEFAULT_LINE_STYLES.put(RocketComponent.class, LineStyle.SOLID.name());
		DEFAULT_LINE_STYLES.put(MassObject.class, LineStyle.DASHED.name());
	}
	
	
	/*
	 * Within a holder class so they will load only when needed.
	 */
	private static class DefaultMaterialHolder {
		private static final Translator trans = Application.getTranslator();
		
		//// Elastic cord (round 2mm, 1/16 in)
		private static final Material DEFAULT_LINE_MATERIAL =
				Databases.findMaterial(Material.Type.LINE, trans.get("Databases.materials.Elasticcordround2mm"),
						0.0018, false);
		//// Ripstop nylon
		private static final Material DEFAULT_SURFACE_MATERIAL =
				Databases.findMaterial(Material.Type.SURFACE, trans.get("Databases.materials.Ripstopnylon"), 0.067, false);
		//// Cardboard
		private static final Material DEFAULT_BULK_MATERIAL =
				Databases.findMaterial(Material.Type.BULK, trans.get("Databases.materials.Cardboard"), 680, false);
	}
	
	//////////////////////
	

	/**
	 * Return the OpenRocket version number.
	 */
	public static String getVersion() {
		return BuildPropertyHolder.BUILD_VERSION;
	}
	
	
	/**
	 * Return the OpenRocket build source (e.g. "default" or "Debian")
	 */
	public static String getBuildSource() {
		return BuildPropertyHolder.BUILD_SOURCE;
	}
	
	
	/**
	 * Return the OpenRocket unique ID.
	 * 
	 * @return	a random ID string that stays constant between OpenRocket executions
	 */
	public static String getUniqueID() {
		String id = PREFNODE.get("id", null);
		if (id == null) {
			id = UniqueID.uuid();
			PREFNODE.put("id", id);
		}
		return id;
	}
	
	

	/**
	 * Store the current OpenRocket version into the preferences to allow for preferences migration.
	 */
	private static void storeVersion() {
		PREFNODE.put("OpenRocketVersion", getVersion());
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
		int v = PREFNODE.getInt(key, def);
		if ((v < 0) || (v > max))
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
		PREFNODE.putInt(key, value);
		storeVersion();
	}
	
	
	/**
	 * Return a string preference.
	 * 
	 * @param key	the preference key.
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	public static String getString(String key, String def) {
		return PREFNODE.get(key, def);
	}
	
	/**
	 * Set a string preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set, or <code>null</code> to remove the key
	 */
	public static void putString(String key, String value) {
		if (value == null) {
			PREFNODE.remove(key);
			return;
		}
		PREFNODE.put(key, value);
		storeVersion();
	}
	
	
	/**
	 * Retrieve an enum value from the user preferences.
	 * 
	 * @param <T>	the enum type
	 * @param key	the key
	 * @param def	the default value, cannot be null
	 * @return		the value in the preferences, or the default value
	 */
	public static <T extends Enum<T>> T getEnum(String key, T def) {
		if (def == null) {
			throw new BugException("Default value cannot be null");
		}
		
		String value = getString(key, null);
		if (value == null) {
			return def;
		}
		
		try {
			return Enum.valueOf(def.getDeclaringClass(), value);
		} catch (IllegalArgumentException e) {
			return def;
		}
	}
	
	/**
	 * Store an enum value to the user preferences.
	 * 
	 * @param key		the key
	 * @param value		the value to store, or null to remove the value
	 */
	public static void putEnum(String key, Enum<?> value) {
		if (value == null) {
			putString(key, null);
		} else {
			putString(key, value.name());
		}
	}
	
	
	/**
	 * Return a boolean preference.
	 * 
	 * @param key	the preference key
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	public static boolean getBoolean(String key, boolean def) {
		return PREFNODE.getBoolean(key, def);
	}
	
	/**
	 * Set a boolean preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set
	 */
	public static void putBoolean(String key, boolean value) {
		PREFNODE.putBoolean(key, value);
		storeVersion();
	}
	
	
	/**
	 * Return a preferences object for the specified node name.
	 * 
	 * @param nodeName	the node name
	 * @return			the preferences object for that node
	 */
	public static Preferences getNode(String nodeName) {
		return PREFNODE.node(nodeName);
	}
	
	
	//////////////////
	

	public static List<Locale> getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}
	
	public static Locale getUserLocale() {
		String locale = getString("locale", null);
		return L10N.toLocale(locale);
	}
	
	public static void setUserLocale(Locale l) {
		if (l == null) {
			putString("locale", null);
		} else {
			putString("locale", l.toString());
		}
	}
	
	

	public static boolean getCheckUpdates() {
		return PREFNODE.getBoolean(CHECK_UPDATES, BuildPropertyHolder.DEFAULT_CHECK_UPDATES);
	}
	
	public static void setCheckUpdates(boolean check) {
		PREFNODE.putBoolean(CHECK_UPDATES, check);
		storeVersion();
	}
	
	public static File getDefaultDirectory() {
		String file = PREFNODE.get("defaultDirectory", null);
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
		PREFNODE.put("defaultDirectory", d);
		storeVersion();
	}
	
	
	/**
	 * Return a list of files/directories to be loaded as custom thrust curves.
	 * <p>
	 * If this property has not been set, the directory "ThrustCurves" in the user
	 * application directory will be used.  The directory will be created if it does not
	 * exist.
	 * 
	 * @return	a list of files to load as thrust curves.
	 */
	public static List<File> getUserThrustCurveFiles() {
		List<File> list = new ArrayList<File>();
		
		String files = getString(USER_THRUST_CURVES_KEY, null);
		if (files == null) {
			// Default to application directory
			File tcdir = getDefaultUserThrustCurveFile();
			if (!tcdir.isDirectory()) {
				tcdir.mkdirs();
			}
			list.add(tcdir);
		} else {
			for (String file : files.split("\\" + SPLIT_CHARACTER)) {
				file = file.trim();
				if (file.length() > 0) {
					list.add(new File(file));
				}
			}
		}
		
		return list;
	}
	
	public static File getDefaultUserThrustCurveFile() {
		File appdir = SystemInfo.getUserApplicationDirectory();
		File tcdir = new File(appdir, "ThrustCurves");
		return tcdir;
	}
	
	
	/**
	 * Set the list of files/directories to be loaded as custom thrust curves.
	 * 
	 * @param files		the files to load, or <code>null</code> to reset to default value.
	 */
	public static void setUserThrustCurveFiles(List<File> files) {
		if (files == null) {
			putString(USER_THRUST_CURVES_KEY, null);
			return;
		}
		
		String str = "";
		
		for (File file : files) {
			if (str.length() > 0) {
				str += SPLIT_CHARACTER;
			}
			str += file.getAbsolutePath();
		}
		putString(USER_THRUST_CURVES_KEY, str);
	}
	
	



	public static Color getDefaultColor(Class<? extends RocketComponent> c) {
		String color = get("componentColors", c, DEFAULT_COLORS);
		if (color == null)
			return Color.BLACK;
		
		Color clr = parseColor(color);
		if (clr != null) {
			return clr;
		} else {
			return Color.BLACK;
		}
	}
	
	public static void setDefaultColor(Class<? extends RocketComponent> c, Color color) {
		if (color == null)
			return;
		set("componentColors", c, stringifyColor(color));
	}
	
	
	private static Color parseColor(String color) {
		if (color == null) {
			return null;
		}
		
		String[] rgb = color.split(",");
		if (rgb.length == 3) {
			try {
				int red = MathUtil.clamp(Integer.parseInt(rgb[0]), 0, 255);
				int green = MathUtil.clamp(Integer.parseInt(rgb[1]), 0, 255);
				int blue = MathUtil.clamp(Integer.parseInt(rgb[2]), 0, 255);
				return new Color(red, green, blue);
			} catch (NumberFormatException ignore) {
			}
		}
		return null;
	}
	
	
	private static String stringifyColor(Color color) {
		String string = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		return string;
	}
	
	

	public static Color getMotorBorderColor() {
		// TODO: MEDIUM:  Motor color (settable?)
		return new Color(0, 0, 0, 200);
	}
	
	
	public static Color getMotorFillColor() {
		// TODO: MEDIUM:  Motor fill color (settable?)
		return new Color(0, 0, 0, 100);
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
		int dpi = PREFNODE.getInt("DPI", 0); // Tenths of a dpi
		
		if (dpi < 10) {
			dpi = Toolkit.getDefaultToolkit().getScreenResolution() * 10;
		}
		if (dpi < 10)
			dpi = 960;
		
		return (dpi) / 10.0;
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
				Material m = Material.fromStorableString(material, false);
				if (m.getType() == type)
					return m;
			} catch (IllegalArgumentException ignore) {
			}
		}
		
		switch (type) {
		case LINE:
			return DefaultMaterialHolder.DEFAULT_LINE_MATERIAL;
		case SURFACE:
			return DefaultMaterialHolder.DEFAULT_SURFACE_MATERIAL;
		case BULK:
			return DefaultMaterialHolder.DEFAULT_BULK_MATERIAL;
		}
		throw new IllegalArgumentException("Unknown material type: " + type);
	}
	
	public static void setDefaultComponentMaterial(
			Class<? extends RocketComponent> componentClass, Material material) {
		
		set("componentMaterials", componentClass,
				material == null ? null : material.toStorableString());
	}
	
	
	public static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	
	/**
	 * Return whether to use additional safety code checks.
	 */
	public static boolean useSafetyChecks() {
		// Currently default to false unless openrocket.debug.safetycheck is defined
		String s = System.getProperty("openrocket.debug.safetycheck");
		if (s != null && !(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off"))) {
			return true;
		}
		return false;
	}
	
	
	public static Point getWindowPosition(Class<?> c) {
		int x, y;
		String pref = PREFNODE.node("windows").get("position." + c.getCanonicalName(), null);
		
		if (pref == null)
			return null;
		
		if (pref.indexOf(',') < 0)
			return null;
		
		try {
			x = Integer.parseInt(pref.substring(0, pref.indexOf(',')));
			y = Integer.parseInt(pref.substring(pref.indexOf(',') + 1));
		} catch (NumberFormatException e) {
			return null;
		}
		return new Point(x, y);
	}
	
	public static void setWindowPosition(Class<?> c, Point p) {
		PREFNODE.node("windows").put("position." + c.getCanonicalName(), "" + p.x + "," + p.y);
		storeVersion();
	}
	
	


	public static Dimension getWindowSize(Class<?> c) {
		int x, y;
		String pref = PREFNODE.node("windows").get("size." + c.getCanonicalName(), null);
		
		if (pref == null)
			return null;
		
		if (pref.indexOf(',') < 0)
			return null;
		
		try {
			x = Integer.parseInt(pref.substring(0, pref.indexOf(',')));
			y = Integer.parseInt(pref.substring(pref.indexOf(',') + 1));
		} catch (NumberFormatException e) {
			return null;
		}
		return new Dimension(x, y);
	}
	
	public static void setWindowSize(Class<?> c, Dimension d) {
		PREFNODE.node("windows").put("size." + c.getCanonicalName(), "" + d.width + "," + d.height);
		storeVersion();
	}
	
	
	////  Printing
	
	public static PrintSettings getPrintSettings() {
		PrintSettings settings = new PrintSettings();
		Color c;
		
		c = parseColor(getString("print.template.fillColor", null));
		if (c != null) {
			settings.setTemplateFillColor(c);
		}
		
		c = parseColor(getString("print.template.borderColor", null));
		if (c != null) {
			settings.setTemplateBorderColor(c);
		}
		
		settings.setPaperSize(getEnum("print.paper.size", settings.getPaperSize()));
		settings.setPaperOrientation(getEnum("print.paper.orientation", settings.getPaperOrientation()));
		
		return settings;
	}
	
	public static void setPrintSettings(PrintSettings settings) {
		putString("print.template.fillColor", stringifyColor(settings.getTemplateFillColor()));
		putString("print.template.borderColor", stringifyColor(settings.getTemplateBorderColor()));
		putEnum("print.paper.size", settings.getPaperSize());
		putEnum("print.paper.orientation", settings.getPaperOrientation());
	}
	
	////  Background flight data computation
	
	public static boolean computeFlightInBackground() {
		return PREFNODE.getBoolean("backgroundFlight", true);
	}
	
	public static Simulation getBackgroundSimulation(Rocket rocket) {
		Simulation s = new Simulation(rocket);
		GUISimulationConditions cond = s.getConditions();
		
		cond.setTimeStep(RK4SimulationStepper.RECOMMENDED_TIME_STEP * 2);
		cond.setWindSpeedAverage(1.0);
		cond.setWindSpeedDeviation(0.1);
		cond.setLaunchRodLength(5);
		return s;
	}
	
	

	/////////  Export variables
	
	public static boolean isExportSelected(FlightDataType type) {
		Preferences prefs = PREFNODE.node("exports");
		return prefs.getBoolean(type.getName(), false);
	}
	
	public static void setExportSelected(FlightDataType type, boolean selected) {
		Preferences prefs = PREFNODE.node("exports");
		prefs.putBoolean(type.getName(), selected);
	}
	
	

	/////////  Default unit storage
	
	public static void loadDefaultUnits() {
		Preferences prefs = PREFNODE.node("units");
		try {
			
			for (String key : prefs.keys()) {
				UnitGroup group = UnitGroup.UNITS.get(key);
				if (group == null)
					continue;
				
				try {
					group.setDefaultUnit(prefs.get(key, null));
				} catch (IllegalArgumentException ignore) {
				}
			}
			
		} catch (BackingStoreException e) {
			ExceptionHandler.handleErrorCondition(e);
		}
	}
	
	public static void storeDefaultUnits() {
		Preferences prefs = PREFNODE.node("units");
		
		for (String key : UnitGroup.UNITS.keySet()) {
			UnitGroup group = UnitGroup.UNITS.get(key);
			if (group == null || group.getUnitCount() < 2)
				continue;
			
			prefs.put(key, group.getDefaultUnit().getUnit());
		}
	}
	
	

	////  Material storage
	

	/**
	 * Add a user-defined material to the preferences.  The preferences are
	 * first checked for an existing material matching the provided one using
	 * {@link Material#equals(Object)}.
	 * 
	 * @param m		the material to add.
	 */
	public static void addUserMaterial(Material m) {
		Preferences prefs = PREFNODE.node("userMaterials");
		

		// Check whether material already exists
		if (getUserMaterials().contains(m)) {
			return;
		}
		
		// Add material using next free key (key is not used when loading)
		String mat = m.toStorableString();
		for (int i = 0;; i++) {
			String key = "material" + i;
			if (prefs.get(key, null) == null) {
				prefs.put(key, mat);
				return;
			}
		}
	}
	
	
	/**
	 * Remove a user-defined material from the preferences.  The matching is performed
	 * using {@link Material#equals(Object)}.
	 * 
	 * @param m		the material to remove.
	 */
	public static void removeUserMaterial(Material m) {
		Preferences prefs = PREFNODE.node("userMaterials");
		
		try {
			
			// Iterate through materials and remove all keys with a matching material
			for (String key : prefs.keys()) {
				String value = prefs.get(key, null);
				try {
					
					Material existing = Material.fromStorableString(value, true);
					if (existing.equals(m)) {
						prefs.remove(key);
					}
					
				} catch (IllegalArgumentException ignore) {
				}
				
			}
			
		} catch (BackingStoreException e) {
			throw new IllegalStateException("Cannot read preferences!", e);
		}
	}
	
	
	/**
	 * Return a set of all user-defined materials in the preferences.  The materials
	 * are created marked as user-defined.
	 * 
	 * @return	a set of all user-defined materials.
	 */
	public static Set<Material> getUserMaterials() {
		Preferences prefs = PREFNODE.node("userMaterials");
		
		HashSet<Material> materials = new HashSet<Material>();
		try {
			
			for (String key : prefs.keys()) {
				String value = prefs.get(key, null);
				try {
					
					Material m = Material.fromStorableString(value, true);
					materials.add(m);
					
				} catch (IllegalArgumentException e) {
					log.warn("Illegal material string " + value);
				}
				
			}
			
		} catch (BackingStoreException e) {
			throw new IllegalStateException("Cannot read preferences!", e);
		}
		
		return materials;
	}
	
	
	////  Helper methods
	
	private static String get(String directory,
			Class<? extends RocketComponent> componentClass,
			Map<Class<?>, String> defaultMap) {
		
		// Search preferences
		Class<?> c = componentClass;
		Preferences prefs = PREFNODE.node(directory);
		while (c != null && RocketComponent.class.isAssignableFrom(c)) {
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
		Preferences prefs = PREFNODE.node(directory);
		if (value == null)
			prefs.remove(componentClass.getSimpleName());
		else
			prefs.put(componentClass.getSimpleName(), value);
		storeVersion();
	}
	
}
