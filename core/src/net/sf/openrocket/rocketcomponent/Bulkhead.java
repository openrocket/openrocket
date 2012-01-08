package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;


public class Bulkhead extends RadiusRingComponent {
	private static final Translator trans = Application.getTranslator();
	
	public Bulkhead() {
		setOuterRadiusAutomatic(true);
		setLength(0.002);
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
