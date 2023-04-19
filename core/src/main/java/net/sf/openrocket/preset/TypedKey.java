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
		TypedKey<?> other = (TypedKey<?>) obj;
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
