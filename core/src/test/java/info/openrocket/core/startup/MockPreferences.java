package info.openrocket.core.startup;

import java.util.Set;
import java.util.prefs.BackingStoreException;

import com.google.inject.Singleton;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.util.BugException;

@Singleton
public class MockPreferences extends Preferences {

	private final String NODENAME = "OpenRocket-test-mock";
	private final java.util.prefs.Preferences NODE;

	public MockPreferences() {
		java.util.prefs.Preferences root = java.util.prefs.Preferences.userRoot();
		try {
			if (root.nodeExists(NODENAME)) {
				root.node(NODENAME).removeNode();
			}
		} catch (BackingStoreException e) {
			throw new BugException("Unable to clear preference node", e);
		}
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
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void putString(String directory, String key, String value) {
		throw new UnsupportedOperationException("Not yet implemented");
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
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Set<Material> getUserMaterials() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void removeUserMaterial(Material m) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void setComponentFavorite(ComponentPreset preset, Type type, boolean favorite) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Set<String> getComponentFavorites(Type type) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
