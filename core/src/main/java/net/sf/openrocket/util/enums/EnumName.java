package net.sf.openrocket.util.enums;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.util.BugException;

public class EnumName<E extends Enum<E>> {
	
	private static final EnumConversion NAME = new EnumConversion() {
		@Override
		public String convert(Enum<?> e) {
			return e.name();
		}
	};
	
	
	private final Class<E> type;
	private final Map<E, String> map;
	private final Map<String, E> reverse;
	
	public EnumName(Class<E> type) {
		this(type, NAME);
	}
	
	public EnumName(Class<E> type, EnumConversion conversion) {
		this.type = type;
		map = new EnumMap<E, String>(type);
		reverse = new HashMap<String, E>();
		
		E[] keys = type.getEnumConstants();
		if (keys == null) {
			throw new IllegalArgumentException("Type " + type + " is not of enum type");
		}
		for (E key : keys) {
			String value = conversion.convert(key);
			if (reverse.containsKey(value)) {
				throw new BugException("Two enum constants were converted to have the name value: " + reverse.get(value)
						+ " and " + key + " both convert to '" + value + "'");
			}
			map.put(key, value);
			reverse.put(value, key);
			
		}
	}
	
	
	
	public String getName(E key) {
		String name = map.get(key);
		if (name == null) {
			throw new IllegalArgumentException("No name found for enum " + key + " from map of type " + type);
		}
		return name;
	}
	
	public E getEnum(String name) {
		return reverse.get(name);
	}
	
	
}
