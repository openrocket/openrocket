package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.startup.Application;

public abstract class StructuralComponent extends InternalComponent {

	private Material material;
	
	public StructuralComponent() {
		super();
		material = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
	}
	

	public final Material getMaterial() {
		return material;
	}
	
	public final void setMaterial(Material mat) {
		if (mat.getType() != Material.Type.BULK) {
			throw new IllegalArgumentException("Attempted to set non-bulk material "+mat);
		}
		if (mat.equals(material))
			return;
		this.material = mat;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
}
