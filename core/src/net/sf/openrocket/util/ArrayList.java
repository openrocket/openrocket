package net.sf.openrocket.util;

import java.util.Collection;

/**
 * An implementation of an ArrayList with a type-safe {@link #clone()} method.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ArrayList<E> extends java.util.ArrayList<E> {
	
	public ArrayList() {
		super();
	}
	
	public ArrayList(Collection<? extends E> c) {
		super(c);
	}
	
	public ArrayList(int initialCapacity) {
		super(initialCapacity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<E> clone() {
		return (ArrayList<E>) super.clone();
	}
	
}
