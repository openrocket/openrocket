package info.openrocket.core.preferences;

import info.openrocket.core.database.Database;
import info.openrocket.core.material.Material;
import info.openrocket.core.util.ChangeSource;
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
