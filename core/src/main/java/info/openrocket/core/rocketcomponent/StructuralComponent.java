package info.openrocket.core.rocketcomponent;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.startup.Application;

import java.util.ArrayList;
import java.util.List;

public abstract class StructuralComponent extends InternalComponent {

	private Material material;

	public StructuralComponent() {
		super();
		material = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
	}

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
		if (preset.has(ComponentPreset.MATERIAL)) {
			Material mat = preset.get(ComponentPreset.MATERIAL);
			if (mat != null) {
				this.material = mat;
				if (material.isDocumentMaterial() && getRoot() instanceof Rocket rocket && rocket.getDocument() != null) {
					rocket.getDocument().getDocumentPreferences().addMaterial(mat);
				}
			} /*
				 * TODO -
				 * else if (c.isMassOverridden()) {
				 * double mass = c.getOverrideMass();
				 * double volume = getComponentVolume();
				 * double density;
				 * if (volume > 0.00001) {
				 * density = mass / volume;
				 * } else {
				 * density = 1000;
				 * }
				 * mat = Material.newMaterial(Type.BULK, mat.getName(), density, true);
				 * setMaterial(mat);
				 * }
				 */
		}
	}

	public final Material getMaterial() {
		return material;
	}

	public final void setMaterial(Material mat) {
		if (mat.getType() != Material.Type.BULK) {
			throw new IllegalArgumentException("Attempted to set non-bulk material " + mat);
		}

		for (RocketComponent listener : configListeners) {
			if (listener instanceof StructuralComponent) {
				((StructuralComponent) listener).setMaterial(mat);
			}
		}

		if (mat.equals(material))
			return;
		this.material = mat;
		if (material.isDocumentMaterial() && getRoot() instanceof Rocket rocket && rocket.getDocument() != null) {
			rocket.getDocument().getDocumentPreferences().addMaterial(mat);
		}
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	@Override
	public List<Material> getAllMaterials() {
		List<Material> materials = super.getAllMaterials();
		materials = materials == null ? new ArrayList<>() : materials;
		materials.add(material);
		return materials;
	}
}
