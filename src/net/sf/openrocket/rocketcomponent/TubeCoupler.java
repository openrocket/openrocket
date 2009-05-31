package net.sf.openrocket.rocketcomponent;


public class TubeCoupler extends ThicknessRingComponent {

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

	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}
}

