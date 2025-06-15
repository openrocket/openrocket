package info.openrocket.core.startup.providers;

import java.util.Set;
import java.util.HashSet;

import info.openrocket.core.material.Material;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;

import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Provider for ApplicationPreferences in core-only applications.
 * This provides a basic implementation suitable for non-GUI applications.
 */
public class CoreApplicationPreferencesProvider implements Provider<ApplicationPreferences> {
	
	@Override
	public ApplicationPreferences get() {
		return new CoreApplicationPreferences();
	}
	
	/**
	 * Basic implementation of ApplicationPreferences for core-only usage.
	 */
	@Singleton
	public static class CoreApplicationPreferences extends ApplicationPreferences {
		
		private final String NODENAME = "OpenRocket-core";
		private final java.util.prefs.Preferences NODE;
		
		public CoreApplicationPreferences() {
			java.util.prefs.Preferences root = java.util.prefs.Preferences.userRoot();
			NODE = root.node(NODENAME);
		}
		
		@Override
		public boolean getBoolean(String key, boolean def) {
			return NODE.getBoolean(key, def);
		}
		
		@Override
		public void putBoolean(String key, boolean value) {
			NODE.putBoolean(key, value);
		}
		
		@Override
		public int getInt(String key, int def) {
			return NODE.getInt(key, def);
		}
		
		@Override
		public void putInt(String key, int value) {
			NODE.putInt(key, value);
		}
		
		@Override
		public double getDouble(String key, double def) {
			return NODE.getDouble(key, def);
		}
		
		@Override
		public void putDouble(String key, double value) {
			NODE.putDouble(key, value);
		}
		
		@Override
		public String getString(String key, String def) {
			return NODE.get(key, def);
		}
		
		@Override
		public void putString(String key, String value) {
			NODE.put(key, value);
		}
		
		@Override
		public String getString(String directory, String key, String def) {
			return NODE.node(directory).get(key, def);
		}
		
		@Override
		public void putString(String directory, String key, String value) {
			NODE.node(directory).put(key, value);
		}
		
		@Override
		public java.util.prefs.Preferences getNode(String nodeName) {
			return NODE.node(nodeName);
		}
		
		@Override
		public java.util.prefs.Preferences getPreferences() {
			return NODE;
		}
		
		@Override
		public void addUserMaterial(Material m) {
			// Basic implementation - could be enhanced if needed
		}
		
		@Override
		public Set<Material> getUserMaterials() {
			// Basic implementation - return empty set
			return new HashSet<>();
		}
		
		@Override
		public void removeUserMaterial(Material m) {
			// Basic implementation - no-op
		}
		
		@Override
		public void setComponentFavorite(ComponentPreset preset, Type type, boolean favorite) {
			// Basic implementation - could be enhanced if needed
		}
		
		@Override
		public Set<String> getComponentFavorites(Type type) {
			// Basic implementation - return empty set
			return new HashSet<>();
		}
	}
}