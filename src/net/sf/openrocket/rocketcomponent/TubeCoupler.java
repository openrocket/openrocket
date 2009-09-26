package net.sf.openrocket.rocketcomponent;


public class TubeCoupler extends ThicknessRingComponent implements RadialParent {

	public TubeCoupler() {
		setOuterRadiusAutomatic(true);
		setThickness(0.002);
		setLength(0.06);
	}
	
	
	// Make setter visible
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}

	
	@Override
	public String getComponentName() {
		return "Tube coupler";
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

