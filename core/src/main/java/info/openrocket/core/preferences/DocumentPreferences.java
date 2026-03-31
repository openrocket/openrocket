package info.openrocket.core.preferences;

import info.openrocket.core.database.Database;
import info.openrocket.core.material.Material;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * ORPreferences specific to an OpenRocket document (= preferences that are saved in the document file, not
 * implemented application-wise).
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DocumentPreferences implements ChangeSource, ORPreferences {
	// Map that stores all the document preferences
	private final Map<String, DocumentPreference> preferencesMap = new HashMap<>();

	/**
	 * A database of bulk materials (with bulk densities).
	 */
	private final Database<Material> BULK_MATERIAL = new Database<>();
	/**
	 * A database of surface materials (with surface densities).
	 */
	private final Database<Material> SURFACE_MATERIAL = new Database<>();
	/**
	 * A database of linear material (with length densities).
	 */
	private final Database<Material> LINE_MATERIAL = new Database<>();

	public static final String PREF_SHOW_WARNINGS = "RocketPanel.showWarnings";
	public static final String PREF_2D_BACKGROUND_COLOR = "RocketPanel.2DBackgroundColor";
	public static final String PREF_3D_BACKGROUND_COLOR = "RocketPanel.3DBackgroundColor";
	public static final String PREF_2D_TEXT_COLOR = "RocketPanel.2DTextColor";
	public static final String PREF_3D_TEXT_COLOR = "RocketPanel.3DTextColor";


	@Override
	public void addChangeListener(StateChangeListener listener) {

	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {

	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		DocumentPreference pref = preferencesMap.get(key);
		return preferencesMap.containsKey(key) ? (Boolean) pref.getValue() : defaultValue;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		preferencesMap.put(key, new DocumentPreference(value));
	}

	@Override
	public int getInt(String key, int defaultValue) {
		DocumentPreference pref = preferencesMap.get(key);
		return preferencesMap.containsKey(key) ? (Integer) pref.getValue() : defaultValue;
	}

	@Override
	public void putInt(String key, int value) {
		preferencesMap.put(key, new DocumentPreference(value));
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		DocumentPreference pref = preferencesMap.get(key);
		return preferencesMap.containsKey(key) ? (Double) pref.getValue() : defaultValue;
	}

	@Override
	public void putDouble(String key, double value) {
		preferencesMap.put(key, new DocumentPreference(value));
	}

	@Override
	public String getString(String key, String defaultValue) {
		DocumentPreference pref = preferencesMap.get(key);
		return preferencesMap.containsKey(key) ? (String) pref.getValue() : defaultValue;
	}

	@Override
	public void putString(String key, String value) {
		preferencesMap.put(key, new DocumentPreference(value));
	}

	/**
	 * Get a Color preference value. Colors are stored as comma-separated R,G,B strings (e.g., "255,128,64").
	 * @param key the preference key
	 * @param defaultValue the default color if not set (can be null)
	 * @return the color, or defaultValue if not set
	 */
	public java.awt.Color getColor(String key, java.awt.Color defaultValue) {
		DocumentPreference pref = preferencesMap.get(key);
		if (pref != null && pref.getValue() instanceof String) {
			java.awt.Color color = parseColor((String) pref.getValue());
			if (color != null) {
				return color;
			}
		}
		return defaultValue;
	}

	/**
	 * Set a Color preference value. Colors are stored as comma-separated R,G,B strings (e.g., "255,128,64").
	 * @param key the preference key
	 * @param value the color to store (null to remove the preference)
	 */
	public void putColor(String key, java.awt.Color value) {
		if (value == null) {
			preferencesMap.remove(key);
		} else {
			String colorString = stringifyColor(value);
			preferencesMap.put(key, new DocumentPreference(colorString));
		}
	}
	
	/**
	 * Helper function to convert a string representation into a java.awt.Color object.
	 * Expects format "R,G,B" where R, G, B are integers between 0 and 255.
	 * 
	 * @param color the color string (e.g., "255,128,64")
	 * @return the Color object, or null if parsing fails
	 */
	private static java.awt.Color parseColor(String color) {
		if (color == null) {
			return null;
		}

		String[] rgb = color.split(",");
		if (rgb.length == 3) {
			try {
				int red = MathUtil.clamp(Integer.parseInt(rgb[0].trim()), 0, 255);
				int green = MathUtil.clamp(Integer.parseInt(rgb[1].trim()), 0, 255);
				int blue = MathUtil.clamp(Integer.parseInt(rgb[2].trim()), 0, 255);
				return new java.awt.Color(red, green, blue);
			} catch (NumberFormatException ignore) {
			}
		}
		return null;
	}
	
	/**
	 * Helper function to convert a java.awt.Color object into a string before storing in a preference.
	 * Returns format "R,G,B" where R, G, B are integers between 0 and 255.
	 * 
	 * @param color the Color object
	 * @return the string representation (e.g., "255,128,64")
	 */
	private static String stringifyColor(java.awt.Color color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	/**
	 * Returns the map that stores all the document preferences key-value pairs.
	 * @return The document preferences map
	 */
	public Map<String, DocumentPreference> getPreferencesMap() {
		return preferencesMap;
	}

	public Database<Material> getBulkMaterials() {
		return BULK_MATERIAL;
	}

	public Database<Material> getSurfaceMaterials() {
		return SURFACE_MATERIAL;
	}

	public Database<Material> getLineMaterials() {
		return LINE_MATERIAL;
	}

	/**
	 * gets the specific database with the given type
	 * @param 	type	the desired type
	 * @return	the database of the type given
	 */
	public Database<Material> getDatabase(Material.Type type){
		return switch (type) {
			case BULK -> BULK_MATERIAL;
			case SURFACE -> SURFACE_MATERIAL;
			case LINE -> LINE_MATERIAL;
			default -> throw new IllegalArgumentException("Illegal material type: " + type);
		};
	}

	/**
	 * Returns a database with all materials.
	 * !!! Removing or adding materials to the returned database will not affect the original databases. !!!
	 * @return A database with all materials
	 */
	public Database<Material> getAllMaterials() {
		Database<Material> allMaterials = new Database<>();
		allMaterials.addAll(BULK_MATERIAL);
		allMaterials.addAll(SURFACE_MATERIAL);
		allMaterials.addAll(LINE_MATERIAL);
		return allMaterials;
	}

	public void addMaterial(Material material) {
		getDatabase(material.getType()).add(material);
	}

	public void removeMaterial(Material material) {
		getDatabase(material.getType()).remove(material);
	}

	public int getMaterialCount(Material.Type type) {
		return getDatabase(type).size();
	}

	public int getTotalMaterialCount() {
		return getMaterialCount(Material.Type.BULK) + getMaterialCount(Material.Type.SURFACE) + getMaterialCount(Material.Type.LINE);
	}

	public static class DocumentPreference {
		private final Object value;
		private final Class<?> type;

		public DocumentPreference(Object value) {
			this.value = value;
			this.type = value.getClass();
		}

		public Object getValue() {
			return value;
		}

		public Class<?> getType() {
			return type;
		}
	}
}
