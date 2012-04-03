package net.sf.openrocket.preset;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.unit.UnitGroup;
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
	
	private final TypedPropertyMap properties = new TypedPropertyMap();
	
	
	// TODO - Implement clone.
	
	public enum Type {
		BODY_TUBE,
		NOSE_CONE
	}
	
	public final static TypedKey<Manufacturer> MANUFACTURER = new TypedKey<Manufacturer>("Manufacturer", Manufacturer.class);
	public final static TypedKey<String> PARTNO = new TypedKey<String>("PartNo",String.class);
	public final static TypedKey<Type> TYPE = new TypedKey<Type>("Type",Type.class);
	public final static TypedKey<Double> LENGTH = new TypedKey<Double>("Length", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> INNER_DIAMETER = new TypedKey<Double>("InnerDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Double> OUTER_DIAMETER = new TypedKey<Double>("OuterDiameter", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Material> MATERIAL = new TypedKey<Material>("Material", Material.class);
	public final static TypedKey<Finish> FINISH = new TypedKey<Finish>("Finish", Finish.class);
	public final static TypedKey<Double> THICKNESS = new TypedKey<Double>("Thickness", Double.class, UnitGroup.UNITS_LENGTH);
	public final static TypedKey<Boolean> FILLED = new TypedKey<Boolean>("Filled", Boolean.class);
	public final static TypedKey<Double> MASS = new TypedKey<Double>("Mass", Double.class, UnitGroup.UNITS_MASS);
	
	public final static Map<String, TypedKey<?>> keyMap = new HashMap<String, TypedKey<?>>();
	static {
		keyMap.put(MANUFACTURER.getName(), MANUFACTURER);
		keyMap.put(PARTNO.getName(), PARTNO);
		keyMap.put(TYPE.getName(), TYPE);
		keyMap.put(LENGTH.getName(), LENGTH);
		keyMap.put(INNER_DIAMETER.getName(), INNER_DIAMETER);
		keyMap.put(OUTER_DIAMETER.getName(), OUTER_DIAMETER);
		keyMap.put(MATERIAL.getName(), MATERIAL);
		keyMap.put(FINISH.getName(), FINISH);
		keyMap.put(THICKNESS.getName(), THICKNESS);
		keyMap.put(FILLED.getName(), FILLED);
		keyMap.put(MASS.getName(), MASS);
	}
	
	public static ComponentPreset create( TypedPropertyMap props ) throws InvalidComponentPresetException {
		
		ComponentPreset preset = new ComponentPreset();
		// First do validation.
		if ( !props.containsKey(TYPE)) {
			throw new InvalidComponentPresetException("No Type specified " + props.toString() );
		}
		
		if (!props.containsKey(MANUFACTURER)) {
			throw new InvalidComponentPresetException("No Manufacturer specified " + props.toString() );
		}

		if (!props.containsKey(PARTNO)) {
			throw new InvalidComponentPresetException("No PartNo specified " + props.toString() );
		}

		preset.properties.putAll(props);
		
		// Should check for various bits of each of the types.
		Type t = props.get(TYPE);
		switch ( t ) {
		case BODY_TUBE: {
			
			if ( !props.containsKey(LENGTH) ) {
				throw new InvalidComponentPresetException( "No Length specified for body tube preset " + props.toString());
			}
			
			BodyTube bt = new BodyTube();
			
			bt.setLength(props.get(LENGTH));
			
			// Need to verify contains 2 of OD, thickness, ID.  Compute the third.
			boolean hasOd = props.containsKey(OUTER_DIAMETER);
			boolean hasId = props.containsKey(INNER_DIAMETER);
			boolean hasThickness = props.containsKey(THICKNESS);
			
			if ( hasOd ) {
				double outerRadius = props.get(OUTER_DIAMETER)/2.0;
				double thickness = 0;
				bt.setOuterRadius( outerRadius );
				if ( hasId ) {
					thickness = outerRadius - props.get(INNER_DIAMETER)/2.0;
				} else if ( hasThickness ) {
					thickness = props.get(THICKNESS);
				} else {
					throw new InvalidComponentPresetException("Body tube preset underspecified " + props.toString());
				}
				bt.setThickness( thickness );
			} else {
				if ( ! hasId && ! hasThickness ) {
					throw new InvalidComponentPresetException("Body tube preset underspecified " + props.toString());
				}
				double innerRadius = props.get(INNER_DIAMETER)/2.0;
				double thickness = props.get(THICKNESS);
				bt.setOuterRadius(innerRadius + thickness);
				bt.setThickness(thickness);
			}

			preset.properties.put(OUTER_DIAMETER, bt.getOuterRadius() *2.0);
			preset.properties.put(INNER_DIAMETER, bt.getInnerRadius() *2.0);
			preset.properties.put(THICKNESS, bt.getThickness());
			
			// Need to translate Mass to Density.
			if ( props.containsKey(MASS) ) {
				String materialName = "TubeCustom";
				if ( props.containsKey(MATERIAL) ) {
					materialName = props.get(MATERIAL).getName();
				}
				Material m = Material.newMaterial(Material.Type.BULK, materialName, props.get(MASS)/bt.getComponentVolume(), false);
				preset.properties.put(MATERIAL, m);
			}
			
			break;
		}
		case NOSE_CONE: {
			break;
		}
		}
		
		return preset;

	}

	// Private constructor to encourage use of factory.
	private ComponentPreset() {
		
	}
	
	public boolean has(Object key) {
		return properties.containsKey(key);
	}
	
	public <T> T get(TypedKey<T> key) {
		T value = properties.get(key);
		if (value == null) {
			throw new BugException("Preset did not contain key " + key + " " + properties.toString());
		}
		return (T) value;
	}
	
	@Override
	public String toString() {
		return get(MANUFACTURER).toString() + " " + get(PARTNO);
	}
	
	
}
