package net.sf.openrocket.simulation.extension;

import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.util.ArrayList;

/**
 * A map containing simulation extension configuration.  This map can
 * store values of type int, long, float, double, boolean, String,
 * List and SimulationExtensionConfiguration.
 */
public final class SimulationExtensionConfiguration extends HashMap<String, Object> {
	
	private SimulationExtension extension;
	
	
	public SimulationExtension getExtension() {
		return extension;
	}
	
	public void setExtension(SimulationExtension extension) {
		this.extension = extension;
	}
	
	
	@Override
	public Object put(String key, Object value) {
		Class<?> c = value.getClass();
		if (c != Long.class && c != Integer.class &&
				c != Double.class && c != Float.class &&
				c != Boolean.class &&
				!(value instanceof SimulationExtensionConfiguration) &&
				!(value instanceof List)) {
			throw new UnsupportedOperationException("Invalid configuration parameter type: " + c + "  key=" + key + "  value=" + value);
		}
		return super.put(key, value);
	}
	
	
	public long getLong(String key, long def) {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).longValue();
		} else {
			return def;
		}
	}
	
	public int getInt(String key, int def) {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).intValue();
		} else {
			return def;
		}
	}
	
	public double getDouble(String key, double def) {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else {
			return def;
		}
	}
	
	public float getFloat(String key, float def) {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).floatValue();
		} else {
			return def;
		}
	}
	
	public boolean getBoolean(String key, boolean def) {
		Object o = get(key);
		if (o instanceof Boolean) {
			return (Boolean) o;
		} else {
			return def;
		}
	}
	
	public String getString(String key, String def) {
		Object o = get(key);
		if (o instanceof String) {
			return (String) o;
		} else {
			return def;
		}
	}
	
	
	/**
	 * Deep-clone this object.

	 */
	@Override
	public SimulationExtensionConfiguration clone() {
		SimulationExtensionConfiguration copy = new SimulationExtensionConfiguration();
		copy.extension = this.extension;
		for (String key : this.keySet()) {
			Object value = this.get(key);
			if (value instanceof SimulationExtensionConfiguration) {
				copy.put(key, ((SimulationExtensionConfiguration) value).clone());
			} else if (value instanceof List) {
				copy.put(key, cloneList((List<?>) value));
			} else {
				copy.put(key, value);
			}
		}
		return copy;
	}
	
	private Object cloneList(List<?> original) {
		ArrayList<Object> list = new ArrayList<Object>();
		for (Object value : original) {
			if (value instanceof SimulationExtensionConfiguration) {
				list.add(((SimulationExtensionConfiguration) value).clone());
			} else if (value instanceof List) {
				list.add(cloneList((List<?>) value));
			} else {
				list.add(value);
			}
		}
		return list;
	}
	
	
	
}
