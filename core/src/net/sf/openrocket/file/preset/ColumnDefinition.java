package net.sf.openrocket.file.preset;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;

public interface ColumnDefinition {

	public void setProperty( ComponentPreset preset, String value );
	
	public static class Manufactuer implements ColumnDefinition {
		@Override
		public void setProperty( ComponentPreset preset, String value ) {
			preset.setManufacturer(value);
		}
	}
	public static class PartNumber implements ColumnDefinition {
		@Override
		public void setProperty( ComponentPreset preset, String value ) {
			preset.setPartNo(value);
		}
	}
	public static class Type implements ColumnDefinition {
		@Override
		public void setProperty( ComponentPreset preset, String value ) {
			ComponentPreset.Type t = ComponentPreset.Type.valueOf(value);
			if ( t == null ) {
				throw new RuntimeException("Invalid ComponentPreset Type: " + value);
			}
			preset.setType(t);
		}
	}
	public static class Parameter implements ColumnDefinition {
		TypedKey key;
		public Parameter( TypedKey key ) {
			this.key = key;
		}
		@Override
		public void setProperty( ComponentPreset preset, String value ) {
			Object o = key.parseFromString(value);
			preset.put(key, o);
		}
	}
	
	
	
}
