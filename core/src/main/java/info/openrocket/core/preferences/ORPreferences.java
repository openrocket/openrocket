package info.openrocket.core.preferences;

public interface ORPreferences {
	boolean getBoolean(String key, boolean defaultValue);

	void putBoolean(String key, boolean value);

	int getInt(String key, int defaultValue);

	void putInt(String key, int value);

	double getDouble(String key, double defaultValue);

	void putDouble(String key, double value);

	String getString(String key, String defaultValue);

	void putString(String key, String value);
}
