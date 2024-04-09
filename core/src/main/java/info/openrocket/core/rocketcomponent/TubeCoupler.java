package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.startup.Application;

public class TubeCoupler extends ThicknessRingComponent implements RadialParent {
	private static final Translator trans = Application.getTranslator();

	public TubeCoupler() {
		setOuterRadiusAutomatic(true);
		setThickness(0.002);
		setLength(0.06);
		super.displayOrder_side = 6; // Order for displaying the component in the 2D side view
		super.displayOrder_back = 13; // Order for displaying the component in the 2D back view
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
