package net.sf.openrocket.rocketcomponent;


public class Bulkhead extends RadiusRingComponent {
	
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
		return "Bulkhead";
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
