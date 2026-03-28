package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class ShockCord extends MassObject {
	private static final Translator trans = Application.getTranslator();

	private Material material;
	private double cordLength;
	private boolean cordLengthAutomatic = true;
	private static final double AUTO_CORD_LENGTH_RATIO = 3.0;

	public ShockCord() {
		material = Application.getPreferences().getDefaultComponentMaterial(ShockCord.class, Material.Type.LINE);
		cordLength = 0.4;
		super.displayOrder_side = 12; // Order for displaying the component in the 2D side view
		super.displayOrder_back = 7; // Order for displaying the component in the 2D back view
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material m) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof ShockCord) {
				((ShockCord) listener).setMaterial(m);
			}
		}

		if (m.getType() != Material.Type.LINE)
			throw new BugException("Attempting to set non-linear material.");
		if (material.equals(m))
			return;
		this.material = m;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	@Override
	public List<Material> getAllMaterials() {
		List<Material> materials = super.getAllMaterials();
		materials = materials == null ? new ArrayList<>() : materials;
		materials.add(material);
		return materials;
	}

	public double getCordLength() {
		if (cordLengthAutomatic) {
			cordLength = getAutoCordLength();
		}
		return cordLength;
	}

	public void setCordLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof ShockCord) {
				((ShockCord) listener).setCordLength(length);
			}
		}

		length = MathUtil.max(length, 0);
		if (MathUtil.equals(length, this.cordLength) && !cordLengthAutomatic)
			return;
		this.cordLength = length;
		this.cordLengthAutomatic = false;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	private double getAutoCordLength() {
		RocketComponent root = getRoot();
		if (!(root instanceof Rocket)) {
			return cordLength;
		}
		double rocketLength = ((Rocket) root).getLength();
		return rocketLength * AUTO_CORD_LENGTH_RATIO;
	}

	public boolean isCordLengthAutomatic() {
		return cordLengthAutomatic;
	}

	public void setCordLengthAutomatic(boolean auto) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof ShockCord) {
				((ShockCord) listener).setCordLengthAutomatic(auto);
			}
		}

		if (cordLengthAutomatic == auto)
			return;
		cordLengthAutomatic = auto;
		if (cordLengthAutomatic) {
			cordLength = getAutoCordLength();
		}
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	@Override
	public double getComponentMass() {
		return material.getDensity() * getCordLength();
	}

	@Override
	public String getComponentName() {
		//// Shock cord
		return trans.get("ShockCord.ShockCord");
	}

	@Override
	public boolean allowsChildren() {
		return false;
	}

	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}

}
