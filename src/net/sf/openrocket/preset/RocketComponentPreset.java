package net.sf.openrocket.preset;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A model for a preset component.
 * <p>
 * A preset component contains a component class type, manufacturer information,
 * part information, and getter methods for various properties of the component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class RocketComponentPreset {
	
	private final Class<? extends RocketComponent> componentClass;
	private final Manufacturer manufacturer;
	private final String partName;
	private final String partNo;
	private final String partDescription;
	
	
	public RocketComponentPreset(Class<? extends RocketComponent> componentClass, Manufacturer manufacturer,
			String partName, String partNo, String partDescription) {
		this.componentClass = componentClass;
		this.manufacturer = manufacturer;
		this.partName = partName;
		this.partNo = partNo;
		this.partDescription = partDescription;
	}
	
	
	/**
	 * Return the component class that this preset defines.
	 */
	public Class<? extends RocketComponent> getComponentClass() {
		return componentClass;
	}
	
	/**
	 * Return the manufacturer of this preset component.
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}
	
	/**
	 * Return the part name.  This is a short, human-readable name of the part.
	 */
	public String getPartName() {
		return partName;
	}
	
	/**
	 * Return the part number.  This is the part identifier (e.g. "BT-50").
	 */
	public String getPartNo() {
		return partNo;
	}
	
	/**
	 * Return the part description.  This is a longer description of the component.
	 */
	public String getPartDescription() {
		return partDescription;
	}
	
}
