package net.sf.openrocket.rocketcomponent;


public class EngineBlock extends ThicknessRingComponent {
	
	public EngineBlock() {
		super();
		setOuterRadiusAutomatic(true);
		setThickness(0.005);
		setLength(0.005);
	}
	
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}
	
	@Override
	public String getComponentName() {
		return "Engine block";
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
