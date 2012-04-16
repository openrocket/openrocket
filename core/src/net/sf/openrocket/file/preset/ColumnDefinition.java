package net.sf.openrocket.file.preset;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.startup.Application;

public class ColumnDefinition<T> {
	TypedKey<T> key;
	public ColumnDefinition( TypedKey<T> key ) {
		this.key = key;
	}
	public void setProperty( TypedPropertyMap preset, String value ) {
		T o = (T) parseFromString(key.getType(), value);
		if ( o != null ) {
			preset.put(key, o);
		}
	}

	private static Object parseFromString( Class<?> type, String value ) {
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
			if ( "balsa".equalsIgnoreCase(value) ) {
				String translated_value = Application.getTranslator().get("Databases.materials.Balsa");
				return getMaterialFor(translated_value);
			}
			if ( "paper".equalsIgnoreCase(value) ) {
				String translated_value = Application.getTranslator().get("Databases.materials.Paperoffice");
				return getMaterialFor(translated_value);
			}
			throw new IllegalArgumentException("Invalid material " + value + " in component preset.");
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
		throw new IllegalArgumentException("Invalid type " + type.getName() + " for component preset parameter." );
	}
	
	private static Material getMaterialFor( String translatedName ) {
		Material material;
		material = Databases.findMaterial(Material.Type.BULK, translatedName);
		if ( material != null ) {
			return material;
		}
		material = Databases.findMaterial(Material.Type.LINE, translatedName);
		if ( material != null ) {
			return material;
		}
		material = Databases.findMaterial(Material.Type.SURFACE, translatedName);
		if ( material != null ) {
			return material;
		}
		throw new IllegalArgumentException("Invalid Material: " + translatedName );
	}
}
