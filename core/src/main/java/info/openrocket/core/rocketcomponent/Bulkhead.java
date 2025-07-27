package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.startup.Application;

public class Bulkhead extends RadiusRingComponent {
	private static final Translator trans = Application.getTranslator();

	public Bulkhead() {
		setOuterRadiusAutomatic(true);
		setLength(0.002);
		super.displayOrder_side = 8; // Order for displaying the component in the 2D side view
		super.displayOrder_back = 6; // Order for displaying the component in the 2D back view
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
