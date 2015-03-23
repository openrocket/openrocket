package net.sf.openrocket.util;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Config {
	
	private LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
	
	
	public void put(String key, String value) {
		validateType(value);
		map.put(key, value);
	}
	
	public void put(String key, Number value) {
		validateType(value);
		map.put(key, clone(value));
	}
	
	public void put(String key, Boolean value) {
		validateType(value);
		map.put(key, value);
	}
	
	public void put(String key, List<?> value) {
		validateType(value);
		map.put(key, clone(value));
	}
	
	public void put(String key, Object value) {
		validateType(value);
		map.put(key, clone(value));
	}
	
	
	public Object get(String key, Object def) {
		return get(key, def, Object.class);
	}
	
	public Boolean getBoolean(String key, Boolean def) {
		return get(key, def, Boolean.class);
	}
	
	public Integer getInt(String key, Integer def) {
		Number number = get(key, null, Number.class);
		if (number == null) {
			return def;
		} else {
			return number.intValue();
		}
	}
	
	public Long getLong(String key, Long def) {
		Number number = get(key, null, Number.class);
		if (number == null) {
			return def;
		} else {
			return number.longValue();
		}
	}
	
	public Double getDouble(String key, Double def) {
		Number number = get(key, null, Number.class);
		if (number == null) {
			return def;
		} else {
			return number.doubleValue();
		}
	}
	
	public String getString(String key, String def) {
		return get(key, def, String.class);
	}
	
	public List<?> getList(String key, List<?> def) {
		return get(key, def, List.class);
	}
	
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public Set<String> keySet() {
		return Collections.unmodifiableMap(map).keySet();
	}
	
	@Override
	public Config clone() {
		Config copy = new Config();
		for (Entry<String, Object> entry : map.entrySet()) {
			copy.map.put(entry.getKey(), clone(entry.getValue()));
		}
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T get(String key, T def, Class<T> type) {
		Object value = map.get(key);
		if (type.isInstance(value)) {
			return (T) value;
		} else {
			return def;
		}
	}
	
	
	private void validateType(Object value) {
		if (value == null) {
			throw new NullPointerException("Attempting to add null value to Config object");
		} else if (value instanceof Boolean) {
			// ok
		} else if (value instanceof Number) {
			// ok
		} else if (value instanceof String) {
			// ok
		} else if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			for (Object v : list) {
				validateType(v);
			}
		} else {
			throw new IllegalArgumentException("Attempting to add value of type " + value.getClass() + " to Config object, value=" + value);
		}
	}
	
	
	private Object clone(Object value) {
		if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long ||
				value instanceof Float || value instanceof Double || value instanceof Boolean || value instanceof String) {
			// immutable
			return value;
		} else if (value instanceof Number) {
			return new BigDecimal(value.toString());
		} else if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			ArrayList<Object> copy = new ArrayList<Object>(list.size());
			for (Object o : list) {
				copy.add(clone(o));
			}
			return copy;
		} else {
			throw new IllegalStateException("Config contained value = " + value + " type = " + ((value != null) ? value.getClass() : "null"));
		}
	}
	
}
