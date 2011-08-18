package net.sf.openrocket.preset;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class ExternalComponentPreset extends RocketComponentPreset {
	
	private final double mass;
	private final String materialName;
	
	public ExternalComponentPreset(Class<? extends RocketComponent> componentClass, Manufacturer manufacturer, String partName,
			String partNo, String partDescription, double mass, String materialName) {
		super(componentClass, manufacturer, partName, partNo, partDescription);
		
		this.materialName = materialName;
		this.mass = mass;
	}
	
	
	public String getMaterialName() {
		return materialName;
	}
	
	
	public double getMass() {
		return mass;
	}
	
}
