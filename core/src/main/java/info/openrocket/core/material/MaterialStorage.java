package info.openrocket.core.material;

import info.openrocket.core.database.Database;
import info.openrocket.core.database.DatabaseListener;
import info.openrocket.core.startup.Application;

/**
 * Class for storing changes to user-added materials. The materials are stored
 * to
 * the OpenRocket preferences.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MaterialStorage implements DatabaseListener<Material> {

	@Override
	public void elementAdded(Material material, Database<Material> source) {
		if (material.isUserDefined()) {
			Application.getPreferences().addUserMaterial(material);
		}
	}

	@Override
	public void elementRemoved(Material material, Database<Material> source) {
		Application.getPreferences().removeUserMaterial(material);
	}

}
