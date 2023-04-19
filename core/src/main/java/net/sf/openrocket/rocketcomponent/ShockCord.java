package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MathUtil;

public class ShockCord extends MassObject {
	private static final Translator trans = Application.getTranslator();

	private Material material;
	private double cordLength;
	
	public ShockCord() {
		material = Application.getPreferences().getDefaultComponentMaterial(ShockCord.class, Material.Type.LINE);
		cordLength = 0.4;
		super.displayOrder_side = 12;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 7;		// Order for displaying the component in the 2D back view
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
	
	
	public double getCordLength() {
		return cordLength;
	}
	
	public void setCordLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof ShockCord) {
				((ShockCord) listener).setCordLength(length);
			}
		}

		length = MathUtil.max(length, 0);
		if (MathUtil.equals(length, this.length))
			return;
		this.cordLength = length;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	

	@Override
	public double getComponentMass() {
		return material.getDensity() * cordLength;
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
