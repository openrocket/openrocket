package info.openrocket.core.preferences;

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
