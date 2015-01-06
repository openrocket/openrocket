package net.sf.openrocket.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.BuildProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwingPreferences extends net.sf.openrocket.startup.Preferences {
	private static final Logger log = LoggerFactory.getLogger(SwingPreferences.class);
	
	private static final String SPLIT_CHARACTER = "|";
	
	
	private static final List<Locale> SUPPORTED_LOCALES;
	static {
		List<Locale> list = new ArrayList<Locale>();
		for (String lang : new String[] { "en", "de", "es", "fr", "it", "ru", "cs", "pl", "ja", "pt", "tr" }) {
			list.add(new Locale(lang));
		}
		list.add(new Locale("zh", "CN"));
		list.add(new Locale("uk", "UA"));
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
	
	private final Preferences PREFNODE;
	
	
	public SwingPreferences() {
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
	}
	
	
	
	
	//////////////////////
	
	
	
	/**
	 * Store the current OpenRocket version into the preferences to allow for preferences migration.
	 */
	private void storeVersion() {
		PREFNODE.put("OpenRocketVersion", BuildProperties.getVersion());
	}
	
	/**
	 * Return a string preference.
	 * 
	 * @param key	the preference key.
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	@Override
	public String getString(String key, String def) {
		return PREFNODE.get(key, def);
	}
	
	@Override
	public String getString(String directory, String key, String defaultValue) {
		Preferences p = PREFNODE.node(directory);
		return p.get(key, defaultValue);
	}
	
	/**
	 * Set a string preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set, or <code>null</code> to remove the key
	 */
	@Override
	public void putString(String key, String value) {
		if (value == null) {
			PREFNODE.remove(key);
		} else {
			PREFNODE.put(key, value);
		}
		storeVersion();
	}
	
	@Override
	public void putString(String directory, String key, String value) {
		Preferences p = PREFNODE.node(directory);
		if (value == null) {
			p.remove(key);
		} else {
			p.put(key, value);
		}
		storeVersion();
	}
	
	/**
	 * Return a boolean preference.
	 * 
	 * @param key	the preference key
	 * @param def	the default if no preference is stored
	 * @return		the preference value
	 */
	@Override
	public boolean getBoolean(String key, boolean def) {
		return PREFNODE.getBoolean(key, def);
	}
	
	/**
	 * Set a boolean preference.
	 * 
	 * @param key		the preference key
	 * @param value		the value to set
	 */
	@Override
	public void putBoolean(String key, boolean value) {
		PREFNODE.putBoolean(key, value);
		storeVersion();
	}
	
	@Override
	public int getInt(String key, int defaultValue) {
		return PREFNODE.getInt(key, defaultValue);
	}
	
	@Override
	public void putInt(String key, int value) {
		PREFNODE.putInt(key, value);
		storeVersion();
	}
	
	@Override
	public double getDouble(String key, double defaultValue) {
		return PREFNODE.getDouble(key, defaultValue);
	}
	
	@Override
	public void putDouble(String key, double value) {
		PREFNODE.putDouble(key, value);
		storeVersion();
	}
	
	
	
	/**
	 * Return a preferences object for the specified node name.
	 * 
	 * @param nodeName	the node name
	 * @return			the preferences object for that node
	 */
	@Override
	public Preferences getNode(String nodeName) {
		return PREFNODE.node(nodeName);
	}
	
	
	//////////////////
	
	
	public static List<Locale> getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}
	
	public File getDefaultDirectory() {
		String file = getString("defaultDirectory", null);
		if (file == null)
			return null;
		return new File(file);
	}
	
	public void setDefaultDirectory(File dir) {
		String d;
		if (dir == null) {
			d = null;
		} else {
			d = dir.getAbsolutePath();
		}
		putString("defaultDirectory", d);
		storeVersion();
	}
	
	public File getDefaultUserComponentDirectory() {
		
		File compdir = new File(SystemInfo.getUserApplicationDirectory(), "Components");
		
		if (!compdir.isDirectory()) {
			compdir.mkdirs();
		}
		
		if (!compdir.isDirectory()) {
			return null;
		}
		if (!compdir.canRead()) {
			return null;
		}
		return compdir;
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
	public List<File> getUserThrustCurveFiles() {
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
	
	public File getDefaultUserThrustCurveFile() {
		File appdir = SystemInfo.getUserApplicationDirectory();
		File tcdir = new File(appdir, "ThrustCurves");
		return tcdir;
	}
	
	
	/**
	 * Set the list of files/directories to be loaded as custom thrust curves.
	 * 
	 * @param files		the files to load, or <code>null</code> to reset to default value.
	 */
	public void setUserThrustCurveFiles(List<File> files) {
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
	
	public Color getMotorBorderColor() {
		// TODO: MEDIUM:  Motor color (settable?)
		return new Color(0, 0, 0, 200);
	}
	
	
	public Color getMotorFillColor() {
		// TODO: MEDIUM:  Motor fill color (settable?)
		return new Color(0, 0, 0, 100);
	}
	
	
	
	public static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	
	
	public Point getWindowPosition(Class<?> c) {
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
	
	public void setWindowPosition(Class<?> c, Point p) {
		PREFNODE.node("windows").put("position." + c.getCanonicalName(), "" + p.x + "," + p.y);
		storeVersion();
	}
	
	
	
	
	public Dimension getWindowSize(Class<?> c) {
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
	
	
	public boolean isWindowMaximized(Class<?> c) {
		String pref = PREFNODE.node("windows").get("size." + c.getCanonicalName(), null);
		return "max".equals(pref);
	}
	
	public void setWindowSize(Class<?> c, Dimension d) {
		PREFNODE.node("windows").put("size." + c.getCanonicalName(), "" + d.width + "," + d.height);
		storeVersion();
	}
	
	public void setWindowMaximized(Class<?> c) {
		PREFNODE.node("windows").put("size." + c.getCanonicalName(), "max");
		storeVersion();
	}
	
	/**
	 * this class returns a java.awt.Color object for the specified key.
	 * you can pass (java.awt.Color) null to the second argument to
	 * disambiguate
	 */
	public Color getColor(String key, Color defaultValue) {
		net.sf.openrocket.util.Color c = super.getColor(key, (net.sf.openrocket.util.Color) null);
		if (c == null) {
			return defaultValue;
		}
		return ColorConversion.toAwtColor(c);
	}
	
	/**
	 * 
	 */
	public void putColor(String key, Color value) {
		net.sf.openrocket.util.Color c = ColorConversion.fromAwtColor(value);
		super.putColor(key, c);
	}
	
	////  Printing
	
	
	////  Background flight data computation
	
	public boolean computeFlightInBackground() {
		return PREFNODE.getBoolean("backgroundFlight", true);
	}
	
	public void setComputeFlightInBackground(boolean b) {
		PREFNODE.putBoolean("backgroundFlight", b);
	}
	
	public Simulation getBackgroundSimulation(Rocket rocket) {
		Simulation s = new Simulation(rocket);
		SimulationOptions cond = s.getOptions();
		
		cond.setTimeStep(RK4SimulationStepper.RECOMMENDED_TIME_STEP * 2);
		cond.setWindSpeedAverage(1.0);
		cond.setWindSpeedDeviation(0.1);
		cond.setLaunchRodLength(5);
		return s;
	}
	
	
	
	/////////  Export variables
	
	public boolean isExportSelected(FlightDataType type) {
		Preferences prefs = PREFNODE.node("exports");
		return prefs.getBoolean(type.getName(), false);
	}
	
	public void setExportSelected(FlightDataType type, boolean selected) {
		Preferences prefs = PREFNODE.node("exports");
		prefs.putBoolean(type.getName(), selected);
	}
	
	
	
	/////////  Default unit storage
	
	public void loadDefaultUnits() {
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
			Application.getExceptionHandler().handleErrorCondition(e);
		}
	}
	
	public void storeDefaultUnits() {
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
	@Override
	public void addUserMaterial(Material m) {
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
	@Override
	public void removeUserMaterial(Material m) {
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
	@Override
	public Set<Material> getUserMaterials() {
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
	
	////  Preset Component Favorites
	
	@Override
	public void setComponentFavorite(ComponentPreset preset, ComponentPreset.Type type, boolean favorite) {
		Preferences prefs = PREFNODE.node("favoritePresets").node(type.name());
		if (favorite) {
			prefs.putBoolean(preset.preferenceKey(), true);
		} else {
			prefs.remove(preset.preferenceKey());
		}
	}
	
	@Override
	public Set<String> getComponentFavorites(ComponentPreset.Type type) {
		Preferences prefs = PREFNODE.node("favoritePresets").node(type.name());
		Set<String> collection = new HashSet<String>();
		try {
			collection.addAll(Arrays.asList(prefs.keys()));
		} catch (BackingStoreException bex) {
			
		}
		return collection;
	}
	
	////  Decal Editor Setting
	private final static String DECAL_EDITOR_PREFERNCE_NODE = "decalEditorPreference";
	private final static String DECAL_EDITOR_USE_SYSTEM_DEFAULT = "<SYSTEM>";
	
	public void clearDecalEditorPreference() {
		putString(DECAL_EDITOR_PREFERNCE_NODE, null);
	}
	
	public void setDecalEditorPreference(boolean useSystem, String commandLine) {
		if (useSystem) {
			putString(DECAL_EDITOR_PREFERNCE_NODE, DECAL_EDITOR_USE_SYSTEM_DEFAULT);
		} else if (commandLine != null) {
			putString(DECAL_EDITOR_PREFERNCE_NODE, commandLine);
		} else {
			clearDecalEditorPreference();
		}
	}
	
	public boolean isDecalEditorPreferenceSet() {
		String s = getString(DECAL_EDITOR_PREFERNCE_NODE, null);
		return s != null;
	}
	
	public boolean isDecalEditorPreferenceSystem() {
		String s = getString(DECAL_EDITOR_PREFERNCE_NODE, null);
		return DECAL_EDITOR_USE_SYSTEM_DEFAULT.equals(s);
	}
	
	public String getDecalEditorCommandLine() {
		return getString(DECAL_EDITOR_PREFERNCE_NODE, null);
	}
	
	public List<Manufacturer> getExcludedMotorManufacturers() {
		Preferences prefs = PREFNODE.node("excludedMotorManufacturers");
		List<Manufacturer> collection = new ArrayList<Manufacturer>();
		try {
			String[] manuShortNames = prefs.keys();
			for (String s : manuShortNames) {
				Manufacturer m = Manufacturer.getManufacturer(s);
				if (m != null) {
					collection.add(m);
				}
			}
		} catch (BackingStoreException e) {
		}
		
		return collection;
		
	}
	
	public void setExcludedMotorManufacturers(Collection<Manufacturer> manus) {
		Preferences prefs = PREFNODE.node("excludedMotorManufacturers");
		try {
			for (String s : prefs.keys()) {
				prefs.remove(s);
			}
		} catch (BackingStoreException e) {
		}
		for (Manufacturer m : manus) {
			prefs.putBoolean(m.getSimpleName(), true);
		}
	}
}
