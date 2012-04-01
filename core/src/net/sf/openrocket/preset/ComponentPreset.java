package net.sf.openrocket.preset;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.util.BugException;


/**
 * A model for a preset component.
 * <p>
 * A preset component contains a component class type, manufacturer information,
 * part information, and a method that returns a prototype of the preset component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ComponentPreset {
	
	private final Map<TypedKey<?>, Object> properties = new HashMap<TypedKey<?>, Object>();
	
	
	// TODO - Implement clone.
	// Implement "freezing" so the object cannot be modified.
	
	public enum Type {
		BODY_TUBE,
		NOSE_CONE
	}
	
	public final static TypedKey<Double> LENGTH = new TypedKey<Double>("Length", Double.class);
	public final static TypedKey<Double> INNER_DIAMETER = new TypedKey<Double>("InnerDiameter", Double.class);
	public final static TypedKey<Double> OUTER_DIAMETER = new TypedKey<Double>("OuterDiameter", Double.class);
	public final static TypedKey<Material> MATERIAL = new TypedKey<Material>("Material", Material.class);
	public final static TypedKey<Finish> FINISH = new TypedKey<Finish>("Finish", Finish.class);
	public final static TypedKey<Double> THICKNESS = new TypedKey<Double>("Thickness", Double.class);
	public final static TypedKey<Boolean> FILLED = new TypedKey<Boolean>("Filled", Boolean.class);
	public final static TypedKey<Double> MASS = new TypedKey<Double>("Mass", Double.class);
	
	public final static Map<String, TypedKey<?>> keyMap = new HashMap<String, TypedKey<?>>();
	static {
		keyMap.put(LENGTH.getName(), LENGTH);
		keyMap.put(INNER_DIAMETER.getName(), INNER_DIAMETER);
		keyMap.put(OUTER_DIAMETER.getName(), OUTER_DIAMETER);
		keyMap.put(MATERIAL.getName(), MATERIAL);
		keyMap.put(FINISH.getName(), FINISH);
		keyMap.put(THICKNESS.getName(), THICKNESS);
		keyMap.put(FILLED.getName(), FILLED);
		keyMap.put(MASS.getName(), MASS);
	}
	
	private String manufacturer;
	private String partNo;
	private String partDescription;
	private Type type;
	
	
	public String getManufacturer() {
		return manufacturer;
	}
	
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	public String getPartNo() {
		return partNo;
	}
	
	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	
	public String getPartDescription() {
		return partDescription;
	}
	
	public void setPartDescription(String partDescription) {
		this.partDescription = partDescription;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	
	
	public boolean has(Object key) {
		return properties.containsKey(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(TypedKey<T> key) {
		Object value = properties.get(key);
		if (value == null) {
			throw new BugException("Preset of type " + type + " did not contain key " + key + " mfg=" + manufacturer + " partNo=" + partNo);
		}
		return (T) value;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T put(TypedKey<T> key, T value) {
		return (T) properties.put(key, value);
	}
	
	
	
	@Override
	public String toString() {
		return partNo;
	}
	
}
