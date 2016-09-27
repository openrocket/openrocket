package net.sf.openrocket.preset;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nameHashCode();
		result = prime * result + typeHashCode();
		return result;
	}
	
	private int typeHashCode() {
		return (type == null) ? 0 : type.hashCode();
	}
	
	private int nameHashCode() {
		return (name == null) ? 0 : name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedKey<?> other = (TypedKey<?>) obj;
		return hasSameName(other) && hasSameType(other);
	}
	
	private boolean hasSameName(TypedKey<?> other) {
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (name.equals(other.name)) {
			return true;
		}
		return false;
	}
	
	
	private boolean hasSameType(TypedKey<?> other) {
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (type.equals(other.type)) {
			return true;
		}
		return false;
	}
	
}
