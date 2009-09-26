package net.sf.openrocket.util;

/**
 * Storage for a pair of objects.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @param <U>	the first object type.
 * @param <V>	the second object type.
 */
public class Pair<U,V> {

	private final U u;
	private final V v;
	
	
	public Pair(U u, V v) {
		this.u = u;
		this.v = v;
	}
	
	public U getU() {
		return u;
	}
	
	public V getV() {
		return v;
	}
	
}
