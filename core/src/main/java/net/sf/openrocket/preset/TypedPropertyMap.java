package net.sf.openrocket.preset;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TypedPropertyMap implements Cloneable {
	
	private final Map<TypedKey<?>, Object> delegate;
	
	public TypedPropertyMap() {
		delegate = new LinkedHashMap<TypedKey<?>, Object>();
	}
	
	public int size() {
		return delegate.size();
	}
	
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(TypedKey<T> key) {
		return (T) delegate.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T put(TypedKey<T> key, T value) {
		return (T) delegate.put(key, value);
	}
	
	public Object remove(Object key) {
		return delegate.remove(key);
	}
	
	public void putAll(TypedPropertyMap other) {
		if (other == null) {
			return;
		}
		delegate.putAll(other.delegate);
	}
	
	public void clear() {
		delegate.clear();
	}
	
	public Set<TypedKey<?>> keySet() {
		return delegate.keySet();
	}
	
	public Collection<Object> values() {
		return delegate.values();
	}
	
	public Set<Entry<TypedKey<?>, Object>> entrySet() {
		return delegate.entrySet();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TypedPropertyMap: { ");
		for( Map.Entry<TypedKey<?>, Object> e : delegate.entrySet() ) {
			sb.append(e.getKey()).append(" => ").append(String.valueOf(e.getValue()));
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	protected TypedPropertyMap clone() throws CloneNotSupportedException {
		TypedPropertyMap clone = new TypedPropertyMap();
		clone.putAll(this);
		return clone;
	}
	
}
