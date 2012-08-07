package net.sf.openrocket.android;

import java.util.Collections;
import java.util.Set;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;

public class PreferencesAdapter extends net.sf.openrocket.startup.Preferences {

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getInt(String key, int defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void putInt(String key, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getDouble(String key, double defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void putDouble(String key, double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getString(String key, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putString(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getString(String directory, String key, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putString(String directory, String key, String value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.sf.openrocket.startup.Preferences#addUserMaterial(net.sf.openrocket.material.Material)
	 */
	@Override
	public void addUserMaterial(Material m) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.openrocket.startup.Preferences#getUserMaterials()
	 */
	@Override
	public Set<Material> getUserMaterials() {
		return Collections.<Material>emptySet();
	}

	/* (non-Javadoc)
	 * @see net.sf.openrocket.startup.Preferences#removeUserMaterial(net.sf.openrocket.material.Material)
	 */
	@Override
	public void removeUserMaterial(Material m) {
	}

	@Override
	public void setComponentFavorite(ComponentPreset preset, Type type,	boolean favorite) {
	}

	@Override
	public Set<String> getComponentFavorites(Type type) {
		return Collections.<String>emptySet();
	}

}
