package net.sf.openrocket.preset;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A model for a preset component.
 * <p>
 * A preset component contains a component class type, manufacturer information,
 * part information, and a method that returns a prototype of the preset component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class ComponentPreset {
	
	private final Manufacturer manufacturer;
	private final String partNo;
	private final String partDescription;
	private final RocketComponent prototype;
	
	
	public ComponentPreset(Manufacturer manufacturer, String partNo, String partDescription,
			RocketComponent prototype) {
		this.manufacturer = manufacturer;
		this.partNo = partNo;
		this.partDescription = partDescription;
		this.prototype = prototype.copy();
		
		if (prototype.getParent() != null) {
			throw new IllegalArgumentException("Prototype component cannot have a parent");
		}
		if (prototype.getChildCount() > 0) {
			throw new IllegalArgumentException("Prototype component cannot have children");
		}
	}
	
	
	/**
	 * Return the component class that this preset defines.
	 */
	public Class<? extends RocketComponent> getComponentClass() {
		return prototype.getClass();
	}
	
	/**
	 * Return the manufacturer of this preset component.
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
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
	
	/**
	 * Return a prototype component.  This component may be modified freely.
	 */
	public RocketComponent getPrototype() {
		return prototype.copy();
	}
	
}
