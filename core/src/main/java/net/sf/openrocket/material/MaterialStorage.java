package net.sf.openrocket.material;

import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.DatabaseListener;
import net.sf.openrocket.startup.Application;

/**
 * Class for storing changes to user-added materials.  The materials are stored to
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
