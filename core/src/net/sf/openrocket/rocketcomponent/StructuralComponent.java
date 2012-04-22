package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;

public abstract class StructuralComponent extends InternalComponent {

	private Material material;
	
	public StructuralComponent() {
		super();
		material = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
	}
	
	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
		if ( preset.has(ComponentPreset.MATERIAL ) ) {
			Material mat = preset.get(ComponentPreset.MATERIAL);
			if ( mat != null ) {
				this.material = mat;
			} /*
			TODO - 
			else if (c.isMassOverridden()) {
				double mass = c.getOverrideMass();
				double volume = getComponentVolume();
				double density;
				if (volume > 0.00001) {
					density = mass / volume;
				} else {
					density = 1000;
				}
				mat = Material.newMaterial(Type.BULK, mat.getName(), density, true);
				setMaterial(mat);
			}
			*/
		}
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
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
}
