package info.openrocket.swing.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import info.openrocket.core.database.Databases;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.swing.gui.theme.UITheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InternalComponent;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.simulation.SimulationOptionsInterface;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.RK4SimulationStepper;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.BuildProperties;

import info.openrocket.swing.communication.AssetHandler.UpdatePlatform;


public class SwingPreferences extends info.openrocket.core.startup.Preferences implements SimulationOptionsInterface {
	private static final Logger log = LoggerFactory.getLogger(SwingPreferences.class);


	public static final String NODE_WINDOWS = "windows";
	public static final String NODE_TABLES = "tables";
	private static final String UI_FONT_SIZE = "UIFontSize";
	public static final String UPDATE_PLATFORM = "UpdatePlatform";
	
	private static final List<Locale> SUPPORTED_LOCALES;
	static {
		List<Locale> list = new ArrayList<Locale>();
		for (String lang : new String[] { "en", "ar", "de", "es", "fr", "it", "nl", "ru", "cs", "pl", "ja", "pt", "tr" }) {
			list.add(new Locale(lang));
		}
		list.add(new Locale("zh", "CN"));
		list.add(new Locale("uk", "UA"));
		SUPPORTED_LOCALES = Collections.unmodifiableList(list);
	}

	private final HashMap<Class<?>, String> DEFAULT_COLORS = new HashMap<>();
	
	
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
	
	private Preferences PREFNODE;
	
	
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
		fillDefaultComponentColors();
	}

	private void fillDefaultComponentColors() {
		DEFAULT_COLORS.put(BodyComponent.class, getUIThemeAsTheme().getDefaultBodyComponentColor());
		DEFAULT_COLORS.put(TubeFinSet.class, getUIThemeAsTheme().getDefaultTubeFinSetColor());
		DEFAULT_COLORS.put(FinSet.class, getUIThemeAsTheme().getDefaultFinSetColor());
		DEFAULT_COLORS.put(LaunchLug.class, getUIThemeAsTheme().getDefaultLaunchLugColor());
		DEFAULT_COLORS.put(RailButton.class, getUIThemeAsTheme().getDefaultRailButtonColor());
		DEFAULT_COLORS.put(InternalComponent.class, getUIThemeAsTheme().getDefaultInternalComponentColor());
		DEFAULT_COLORS.put(MassObject.class, getUIThemeAsTheme().getDefaultMassObjectColor());
		DEFAULT_COLORS.put(RecoveryDevice.class, getUIThemeAsTheme().getDefaultRecoveryDeviceColor());
		DEFAULT_COLORS.put(PodSet.class, getUIThemeAsTheme().getDefaultPodSetColor());
		DEFAULT_COLORS.put(ParallelStage.class, getUIThemeAsTheme().getDefaultParallelStageColor());
	}

	public String getNodename() {
		return NODENAME;
	}
	
	
	//////////////////////

	@Override
	public Preferences getPreferences() {
		return PREFNODE;
	}

	/**
	 * Returns the preference node responsible for saving UI window information (position, size...)
	 * @return the preference node for window information
	 */
	public Preferences getWindowsPreferences() {
		return PREFNODE.node(NODE_WINDOWS);
	}

	/**
	 * Returns the preference node responsible for saving table information (column widths, order...)
	 * @return the preference node for table information
	 */
	public Preferences getTablePreferences() {
		return PREFNODE.node(NODE_TABLES);
	}
	
	public void clearPreferences() {
		try {
			Preferences root = Preferences.userRoot();
			if (root.nodeExists(NODENAME)) {
				root.node(NODENAME).removeNode();
			}
			PREFNODE = root.node(NODENAME);
			UnitGroup.resetDefaultUnits();
			storeDefaultUnits();
			log.info("Cleared preferences");
		} catch (BackingStoreException e) {
			throw new BugException("Unable to clear preference node", e);
		}
	}
	
	/**
	 * Store the current OpenRocket version into the preferences to allow for preferences migration.
	 */
	private void storeVersion() {
		PREFNODE.put("OpenRocketVersion", BuildProperties.getVersion());
	}

	/**
	 * Checks if a certain key exists in the node
	 * @param node node to check the keys of.
	 * @param key key to check
	 * @return true if the key is stored in the preferences, false otherwise
	 */
	private boolean keyExists(Preferences node, String key) {
		try {
			return Arrays.asList(node.keys()).contains(key);
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return false;
		}
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
		if (!keyExists(PREFNODE, key) && key != null && def != null) {
			PREFNODE.put(key, def);
			try {
				PREFNODE.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		return PREFNODE.get(key, def);
	}
	
	@Override
	public String getString(String directory, String key, String defaultValue) {
		Preferences p = PREFNODE.node(directory);
		if (!keyExists(p, key) && key != null && defaultValue != null) {
			p.put(key, defaultValue);
			try {
				p.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
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
		// Check if the key exists
		if (!keyExists(PREFNODE, key) && key != null) {
			// Save the default value
			PREFNODE.putBoolean(key, def);
			try {
				PREFNODE.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
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
		if (!keyExists(PREFNODE, key) && key != null) {
			PREFNODE.putInt(key, defaultValue);
			try {
				PREFNODE.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		return PREFNODE.getInt(key, defaultValue);
	}
	
	@Override
	public void putInt(String key, int value) {
		PREFNODE.putInt(key, value);
		storeVersion();
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		if (!keyExists(PREFNODE, key) && key != null) {
			PREFNODE.putDouble(key, defaultValue);
			try {
				PREFNODE.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
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

	/**
	 * Get the current theme used for the UI.
	 * @return the current theme
	 */
	@Override
	public Object getUITheme() {
		return getUIThemeAsTheme();
	}

	private UITheme.Theme getUIThemeAsTheme() {
		String themeName = getString(info.openrocket.core.startup.Preferences.UI_THEME, UITheme.Themes.LIGHT.name());
		if (themeName == null) return UITheme.Themes.LIGHT;		// Default theme
		try {
			return UITheme.Themes.valueOf(themeName);
		} catch (IllegalArgumentException e) {
			return UITheme.Themes.LIGHT;
		}
	}

	/**
	 * Set the theme used for the UI.
	 * @param theme the theme to set
	 */
	@Override
	public void setUITheme(Object theme) {
		if (!(theme instanceof UITheme.Theme)) return;
		putString(info.openrocket.core.startup.Preferences.UI_THEME, ((UITheme.Theme) theme).name());
		storeVersion();
	}

	/**
	 * Get the current font size used for the UI.
	 * @return the current font size
	 */
	public int getUIFontSize() {
		return getInt(UI_FONT_SIZE, getDefaultFontSize());
	}

	public final float getRocketInfoFontSize() {
		return (float) ((getUIFontSize() - 2) + 3 * Application.getPreferences().getChoice(info.openrocket.core.startup.Preferences.ROCKET_INFO_FONT_SIZE, 2, 0));
	}

	private static int getDefaultFontSize() {
		javax.swing.UIDefaults uiDefaults = javax.swing.UIManager.getDefaults();
		Object value = uiDefaults.get("defaultFont");
		if (value instanceof javax.swing.plaf.FontUIResource fontUIResource) {
			return fontUIResource.getSize();
		} else {
			return 12;
		}
	}

	/**
	 * Set the font size used for the UI.
	 * @param size the font size to set
	 */
	public void setUIFontSize(int size) {
		putInt(UI_FONT_SIZE, size);
		storeVersion();
	}

	public ORColor getDefaultColor(Class<? extends RocketComponent> c) {
		String color = get("componentColors", c, DEFAULT_COLORS);
		if (color == null)
			return ORColor.fromAWTColor(getUIThemeAsTheme().getTextColor());

		ORColor clr = parseColor(color);
		if (clr != null) {
			return clr;
		} else {
			return ORColor.fromAWTColor(getUIThemeAsTheme().getTextColor());
		}
	}

	public final void setDefaultColor(Class<? extends RocketComponent> c, ORColor color) {
		if (color == null)
			return;
		putString("componentColors", c.getSimpleName(), stringifyColor(color));
	}
	
	public File getDefaultDirectory() {
		String file = getString(info.openrocket.core.startup.Preferences.DEFAULT_DIRECTORY, null);
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
		putString(info.openrocket.core.startup.Preferences.DEFAULT_DIRECTORY, d);
		storeVersion();
	}

	/**
	 * Set the operating system that the software updater will use to redirect you to an installer download link.
	 * @param platform the operating system to use
	 */
	public void setUpdatePlatform(UpdatePlatform platform) {
		if (platform == null) return;
		putString(UPDATE_PLATFORM, platform.name());
	}

	/**
	 * Get the operating system that will be selected when asking for a software update.
	 * E.g. "Windows" will cause the software updater to default to letting you download a Windows installer.
	 * @return the operating system that is used
	 */
	public UpdatePlatform getUpdatePlatform() {
		String p = getString(UPDATE_PLATFORM, SystemInfo.getPlatform().name());
		if (p == null) return null;
		return UpdatePlatform.valueOf(p);
	}
	
	public static int getMaxThreadCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	
	
	public Point getWindowPosition(Class<?> c) {
		int x, y;
		String pref = getWindowsPreferences().get("position." + c.getCanonicalName(), null);
		
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

		// If position was on a screen that is not available anymore
		Point p = new Point(x, y);
		if (!isPointOnScreen(p)) {
			return null;
		}

		return p;
	}
	
	public void setWindowPosition(Class<?> c, Point p) {
		getWindowsPreferences().put("position." + c.getCanonicalName(), "" + p.x + "," + p.y);
		storeVersion();
	}

	/**
	 * Checks whether the point is present on any of the current monitor screens.
	 * Can return false if point was e.g. referenced on a secondary monitor that doesn't exist anymore.
	 * @param p point to check
	 * @return true if point is present on any of the current screens, false otherwise
	 */
	private boolean isPointOnScreen(Point p) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();

		for (GraphicsDevice screen : screens) {
			GraphicsConfiguration gc = screen.getDefaultConfiguration();
			Rectangle bounds = gc.getBounds();
			if (bounds.contains(p)) {
				return true;
			}
		}
		return false;
	}

	
	
	public Dimension getWindowSize(Class<?> c) {
		int x, y;
		String pref = getWindowsPreferences().get("size." + c.getCanonicalName(), null);
		
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
		String pref = getWindowsPreferences().get("size." + c.getCanonicalName(), null);
		return "max".equals(pref);
	}
	
	public void setWindowSize(Class<?> c, Dimension d) {
		getWindowsPreferences().put("size." + c.getCanonicalName(), "" + d.width + "," + d.height);
		storeVersion();
	}
	
	public void setWindowMaximized(Class<?> c) {
		getWindowsPreferences().put("size." + c.getCanonicalName(), "max");
		storeVersion();
	}

	public Integer getTableColumnWidth(String keyName, int columnIdx) {
		String pref = getTablePreferences().get(
				"cw." + keyName + "." + columnIdx, null);
		if (pref == null)
			return null;


		try {
			return Integer.parseInt(pref);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Integer getTableColumnWidth(Class<?> c, int columnIdx) {
		return getTableColumnWidth(c.getCanonicalName(), columnIdx);
	}

	public void setTableColumnWidth(String keyName, int columnIdx, Integer width) {
		getTablePreferences().put(
				"cw." + keyName + "." + columnIdx, width.toString());
		storeVersion();
	}

	public void setTableColumnWidth(Class<?> c, int columnIdx, Integer width) {
		setTableColumnWidth(c.getCanonicalName(), columnIdx, width);
	}
	
	/**
	 * this class returns a java.awt.ORColor object for the specified key.
	 * you can pass (java.awt.ORColor) null to the second argument to
	 * disambiguate
	 */
	public Color getColor(String key, Color defaultValue) {
		ORColor c = super.getColor(key, (ORColor) null);
		if (c == null) {
			return defaultValue;
		}
		return ColorConversion.toAwtColor(c);
	}
	
	/**
	 * 
	 */
	public void putColor(String key, Color value) {
		ORColor c = ColorConversion.fromAwtColor(value);
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

	/**
	 * Loads the default units from the preferences.
	 */
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

	/**
	 * Stores the standard default units in the preferences.
	 */
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
	public void loadDefaultComponentMaterials() {
		setDefaultComponentMaterial(FinSet.class, Databases.findMaterial(Material.Type.BULK, "Balsa"));
		setDefaultComponentMaterial(NoseCone.class, Databases.findMaterial(Material.Type.BULK, "Polystyrene"));
	}
	
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
