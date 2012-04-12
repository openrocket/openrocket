package net.sf.openrocket.preset;

import org.jfree.util.StringUtils;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.unit.UnitGroup;

public class TypedKey<T> {

	private final String name;
	private final Class<T> type;
	private final UnitGroup unitGroup;
	
	public TypedKey(String name, Class<T> type) {
		this(name, type, null);
	}
	
	public TypedKey(String name, Class<T> type, UnitGroup unitGroup) {
		this.name = name;
		this.type = type;
		this.unitGroup = unitGroup;
	}

	@Override
	public String toString() {
		return "TypedKey [name=" + name + "]";
	}

	public String getName() {
		return name;
	}

	public Class<T> getType() {
		return type;
	}

	public UnitGroup getUnitGroup() {
		return unitGroup;
	}

	public Object parseFromString( String value ) {
		if ( type.equals(Manufacturer.class)) {
			Manufacturer m = Manufacturer.getManufacturer(value);
			return m;
		}
		if ( type.equals(ComponentPreset.Type.class) ) {
			ComponentPreset.Type t = ComponentPreset.Type.valueOf(value);
			return t;
		}
		if ( type.equals(Boolean.class) ) {
			return Boolean.parseBoolean(value);
		}
		if ( type.isAssignableFrom(Double.class) ) {
			return Double.parseDouble(value);
		}
		if ( type.equals(String.class ) ) {
			return value;
		}
		if ( type.equals(Finish.class) ) {
			return Finish.valueOf(value);
		}
		if ( type.equals(Material.class) ) {
			// FIXME - cannot parse Materials just yet.  Need a way to do it without worrying about locale.
			return null;
			/*
			String translated_value = Application.getTranslator().get(value);
			Material material;
			material = Databases.findMaterial(Material.Type.BULK, translated_value);
			if ( material != null ) {
				return material;
			}
			material = Databases.findMaterial(Material.Type.LINE, translated_value);
			if ( material != null ) {
				return material;
			}
			material = Databases.findMaterial(Material.Type.SURFACE, translated_value);
			if ( material != null ) {
				return material;
			}
			throw new IllegalArgumentException("Invalid material " + value + " in component preset.");
			*/
		}
		if ( type.equals(Shape.class) ) {
			//FIXME - ignore case!
			if ( "ogive".equalsIgnoreCase(value) ) {
				return Shape.OGIVE;
			}
			if ( "cone".equalsIgnoreCase(value) ) {
				return Shape.CONICAL;
			}
			if ( "elliptical".equalsIgnoreCase(value) ) {
				return Shape.ELLIPSOID;
			}
			if ( "parabolic".equalsIgnoreCase(value) ) {
				return Shape.PARABOLIC;
			}
			if ( "sears-haack".equalsIgnoreCase(value) ) {
				return Shape.HAACK;
			}
			if ( "power-series".equalsIgnoreCase(value) ) {
				return Shape.POWER;
			}
			throw new IllegalArgumentException("Invalid shape " + value + " in component preset.");
		}
		throw new IllegalArgumentException("Inavlid type " + type.getName() + " for component preset parameter " + name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedKey other = (TypedKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
