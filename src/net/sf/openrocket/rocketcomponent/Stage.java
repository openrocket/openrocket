package net.sf.openrocket.rocketcomponent;

public class Stage extends ComponentAssembly {
	
	@Override
	public String getComponentName() {
		return "Stage";
	}
	
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Check whether the given type can be added to this component.  A Stage allows 
	 * only BodyComponents to be added.
	 * 
	 * @param type  The RocketComponent class type to add.
	 * @return      Whether such a component can be added.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return BodyComponent.class.isAssignableFrom(type);
	}
	
}
