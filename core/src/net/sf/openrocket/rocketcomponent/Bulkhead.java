package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;


public class Bulkhead extends RadiusRingComponent {
	private static final Translator trans = Application.getTranslator();
	
	public Bulkhead() {
		setOuterRadiusAutomatic(true);
		setLength(0.002);
	}
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.BULK_HEAD;
	}

	@Override
	public double getInnerRadius() {
		return 0;
	}
	
	@Override
	public void setInnerRadius(double r) {
		// No-op
	}
	
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
		clearPreset();
	}
	
	@Override
	public String getComponentName() {
		//// Bulkhead
		return trans.get("Bulkhead.Bulkhead");
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
