package net.sf.openrocket.startup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.UniqueID;

public abstract class Preferences {
	
	/*
	 * Well known string keys to preferences.
	 * There are other strings out there in the source as well.
	 */
	public static final String BODY_COMPONENT_INSERT_POSITION_KEY = "BodyComponentInsertPosition";
	public static final String USER_THRUST_CURVES_KEY = "UserThrustCurves";
	public static final String CONFIRM_DELETE_SIMULATION = "ConfirmDeleteSimulation";
	// Preferences related to data export
	public static final String EXPORT_FIELD_SEPARATOR = "ExportFieldSeparator";
	public static final String EXPORT_SIMULATION_COMMENT = "ExportSimulationComment";
	public static final String EXPORT_FIELD_NAME_COMMENT = "ExportFieldDescriptionComment";
	public static final String EXPORT_EVENT_COMMENTS = "ExportEventComments";
	public static final String EXPORT_COMMENT_CHARACTER = "ExportCommentCharacter";
	public static final String USER_LOCAL = "locale";
	
	public static final String PLOT_SHOW_POINTS = "ShowPlotPoints";
	
	private static final String CHECK_UPDATES = "CheckUpdates";
	public static final String LAST_UPDATE = "LastUpdateVersion";
	
	public static final String MOTOR_DIAMETER_FILTER = "MotorDiameterMatch";
	public static final String MOTOR_HIDE_SIMILAR = "MotorHideSimilar";
	
	// Node names
	public static final String PREFERRED_THRUST_CURVE_MOTOR_NODE = "preferredThrustCurveMotors";
	private static final String AUTO_OPEN_LAST_DESIGN = "AUTO_OPEN_LAST_DESIGN";
	
	//Preferences related to 3D graphics
	public static final String OPENGL_ENABLED = "OpenGL_Is_Enabled";
	public static final String OPENGL_ENABLE_AA = "OpenGL_Antialiasing_Is_Enabled";
	public static final String OPENGL_USE_FBO = "OpenGL_Use_FBO";
	
	/*
	 * ******************************************************************************************
	 *
	 * Abstract methods which must be implemented by any derived class.
	 */
	public abstract boolean getBoolean(String key, boolean defaultValue);
	
	public abstract void putBoolean(String key, boolean value);
	
	public abstract int getInt(String key, int defaultValue);
	
	public abstract void putInt(String key, int value);
	
	public abstract double getDouble(String key, double defaultValue);
	
	public abstract void putDouble(String key, double value);
	
	public abstract String getString(String key, String defaultValue);
	
	public abstract void putString(String key, String value);
	
	/**
	 * Directory represents a way to collect multiple keys together.  Implementors may
	 * choose to concatenate the directory with the key using some special character.
	 * @param directory
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public abstract String getString(String directory, String key, String defaultValue);
	
	public abstract void putString(String directory, String key, String value);
	
	/*
	 * ******************************************************************************************
	 */
	public final boolean getCheckUpdates() {
		return this.getBoolean(CHECK_UPDATES, BuildProperties.getDefaultCheckUpdates());
	}
	
	public final void setCheckUpdates(boolean check) {
		this.putBoolean(CHECK_UPDATES, check);
	}
	
	public final double getDefaultMach() {
		// TODO: HIGH: implement custom default mach number
		return 0.3;
	}
	
	/**
	 * Enable/Disable the auto-opening of the last edited design file on startup.
	 */
	public final void setAutoOpenLastDesignOnStartup(boolean enabled) {
		this.putBoolean(AUTO_OPEN_LAST_DESIGN, enabled);
	}
	
	/**
	 * Answer if the auto-opening of the last edited design file on startup is enabled.
	 *
	 * @return true if the application should automatically open the last edited design file on startup.
	 */
	public final boolean isAutoOpenLastDesignOnStartupEnabled() {
		return this.getBoolean(AUTO_OPEN_LAST_DESIGN, false);
	}
	
	/**
	 * Return the OpenRocket unique ID.
	 *
	 * @return	a random ID string that stays constant between OpenRocket executions
	 */
	public final String getUniqueID() {
		String id = this.getString("id", null);
		if (id == null) {
			id = UniqueID.uuid();
			this.putString("id", id);
		}
		return id;
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
	public final int getChoice(String key, int max, int def) {
		int v = this.getInt(key, def);
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
	public final void putChoice(String key, int value) {
		this.putInt(key, value);
	}
	
	/**
	 * Retrieve an enum value from the user preferences.
	 *
	 * @param <T>	the enum type
	 * @param key	the key
	 * @param def	the default value, cannot be null
	 * @return		the value in the preferences, or the default value
	 */
	public final <T extends Enum<T>> T getEnum(String key, T def) {
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
	public final void putEnum(String key, Enum<?> value) {
		if (value == null) {
			putString(key, null);
		} else {
			putString(key, value.name());
		}
	}
	
	public Color getDefaultColor(Class<? extends RocketComponent> c) {
		String color = get("componentColors", c, StaticFieldHolder.DEFAULT_COLORS);
		if (color == null)
			return Color.BLACK;
		
		Color clr = parseColor(color);
		if (clr != null) {
			return clr;
		} else {
			return Color.BLACK;
		}
	}
	
	public final void setDefaultColor(Class<? extends RocketComponent> c, Color color) {
		if (color == null)
			return;
		putString("componentColors", c.getSimpleName(), stringifyColor(color));
	}
	
	
	/**
	 * Retrieve a Line style for the given component.
	 * @param c
	 * @return
	 */
	public final LineStyle getDefaultLineStyle(Class<? extends RocketComponent> c) {
		String value = get("componentStyle", c, StaticFieldHolder.DEFAULT_LINE_STYLES);
		try {
			return LineStyle.valueOf(value);
		} catch (Exception e) {
			return LineStyle.SOLID;
		}
	}
	
	/**
	 * Set a default line style for the given component.
	 * @param c
	 * @param style
	 */
	public final void setDefaultLineStyle(Class<? extends RocketComponent> c,
			LineStyle style) {
		if (style == null)
			return;
		putString("componentStyle", c.getSimpleName(), style.name());
	}
	
	/**
	 * Get the default material type for the given component.
	 * @param componentClass
	 * @param type the Material.Type to return.
	 * @return
	 */
	public Material getDefaultComponentMaterial(
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
			return StaticFieldHolder.DEFAULT_LINE_MATERIAL;
		case SURFACE:
			return StaticFieldHolder.DEFAULT_SURFACE_MATERIAL;
		case BULK:
			return StaticFieldHolder.DEFAULT_BULK_MATERIAL;
		}
		throw new IllegalArgumentException("Unknown material type: " + type);
	}
	
	/**
	 * Set the default material for a component type.
	 * @param componentClass
	 * @param material
	 */
	public void setDefaultComponentMaterial(
			Class<? extends RocketComponent> componentClass, Material material) {
		
		putString("componentMaterials", componentClass.getSimpleName(),
				material == null ? null : material.toStorableString());
	}
	
	/**
	 * get a net.sf.openrocket.util.Color object for the given key.
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public final Color getColor(String key, Color defaultValue) {
		Color c = parseColor(getString(key, null));
		if (c == null) {
			return defaultValue;
		}
		return c;
	}
	
	/**
	 * set a net.sf.openrocket.util.Color preference value for the given key.
	 * @param key
	 * @param value
	 */
	public final void putColor(String key, Color value) {
		putString(key, stringifyColor(value));
	}
	
	/**
	 * Helper function to convert a string representation into a net.sf.openrocket.util.Color object.
	 * @param color
	 * @return
	 */
	protected static Color parseColor(String color) {
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
	
	/**
	 * Helper function to convert a net.sf.openrocket.util.Color object into a
	 * String before storing in a preference.
	 * @param color
	 * @return
	 */
	protected static String stringifyColor(Color color) {
		String string = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		return string;
	}
	
	/**
	 * Special helper function which allows for a map of default values.
	 *
	 * First getString(directory,componentClass.getSimpleName(), null) is invoked,
	 * if the returned value is null, the defaultMap is consulted for a value.
	 *
	 * @param directory
	 * @param componentClass
	 * @param defaultMap
	 * @return
	 */
	protected String get(String directory,
			Class<? extends RocketComponent> componentClass,
			Map<Class<?>, String> defaultMap) {
		
		// Search preferences
		Class<?> c = componentClass;
		while (c != null && RocketComponent.class.isAssignableFrom(c)) {
			String value = this.getString(directory, c.getSimpleName(), null);
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
	
	public abstract void addUserMaterial(Material m);
	
	public abstract Set<Material> getUserMaterials();
	
	public abstract void removeUserMaterial(Material m);
	
	public abstract void setComponentFavorite(ComponentPreset preset, ComponentPreset.Type type, boolean favorite);
	
	public abstract Set<String> getComponentFavorites(ComponentPreset.Type type);
	
	/*
	 * Within a holder class so they will load only when needed.
	 */
	private static class StaticFieldHolder {
		private static final Material DEFAULT_LINE_MATERIAL = Databases.findMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)");
		private static final Material DEFAULT_SURFACE_MATERIAL = Databases.findMaterial(Material.Type.SURFACE, "Ripstop nylon");
		private static final Material DEFAULT_BULK_MATERIAL = Databases.findMaterial(Material.Type.BULK, "Cardboard");
		/*
		 * Map of default line styles
		 */
		
		private static final HashMap<Class<?>, String> DEFAULT_LINE_STYLES = new HashMap<Class<?>, String>();
		static {
			DEFAULT_LINE_STYLES.put(RocketComponent.class, LineStyle.SOLID.name());
			DEFAULT_LINE_STYLES.put(MassObject.class, LineStyle.DASHED.name());
		}
		private static final HashMap<Class<?>, String> DEFAULT_COLORS = new HashMap<Class<?>, String>();
		static {
			DEFAULT_COLORS.put(BodyComponent.class, "0,0,240");
			DEFAULT_COLORS.put(FinSet.class, "0,0,200");
			DEFAULT_COLORS.put(LaunchLug.class, "0,0,180");
			DEFAULT_COLORS.put(InternalComponent.class, "170,0,100");
			DEFAULT_COLORS.put(MassObject.class, "0,0,0");
			DEFAULT_COLORS.put(RecoveryDevice.class, "255,0,0");
		}
	}
	
}
