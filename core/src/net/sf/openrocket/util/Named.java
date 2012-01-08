package net.sf.openrocket.util;

import java.text.Collator;

/**
 * An object holder that provides a custom toString return value.
 * <p>
 * The class supports sorting by the name.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @param <T>	the holder type
 */
public class Named<T> implements Comparable<Named<T>> {
	
	private final T object;
	private final String name;
	
	private Collator collator = null;
	
	/**
	 * Sole constructor.
	 * 
	 * @param object	the held object
	 * @param name		the value to return by toString().
	 */
	public Named(T object, String name) {
		this.object = object;
		this.name = name;
	}
	
	
	/**
	 * Get the held object.
	 * 
	 * @return	the object.
	 */
	public T get() {
		return object;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	
	@Override
	public int compareTo(Named<T> other) {
		if (collator == null) {
			collator = Collator.getInstance();
		}
		
		return collator.compare(this.toString(), other.toString());
	}
	
}
