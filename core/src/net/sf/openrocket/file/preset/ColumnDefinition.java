package net.sf.openrocket.file.preset;

import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;

public class ColumnDefinition<T> {
	TypedKey<T> key;
	public ColumnDefinition( TypedKey<T> key ) {
		this.key = key;
	}
	public void setProperty( TypedPropertyMap preset, String value ) {
		T o = (T) key.parseFromString(value);
		preset.put(key, o);
	}
}
