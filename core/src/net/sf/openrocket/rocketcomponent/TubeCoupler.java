package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;


public class TubeCoupler extends ThicknessRingComponent implements RadialParent {
	private static final Translator trans = Application.getTranslator();

	public TubeCoupler() {
		setOuterRadiusAutomatic(true);
		setThickness(0.002);
		setLength(0.06);
	}
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.TUBE_COUPLER;
	}


	// Make setter visible
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}
	
	
	@Override
	public String getComponentName() {
		//// Tube coupler
		return trans.get("TubeCoupler.TubeCoupler");
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Allow all InternalComponents to be added to this component.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return InternalComponent.class.isAssignableFrom(type);
	}
	
	
	@Override
	public double getInnerRadius(double x) {
		return getInnerRadius();
	}
	
	
	@Override
	public double getOuterRadius(double x) {
		return getOuterRadius();
	}
}
