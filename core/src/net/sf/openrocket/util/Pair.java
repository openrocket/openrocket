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
	
	
	/**
	 * Compare both components of the Pair to another object.
	 * The pair is equal iff both items are equal (or null).
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object other) {
		if (!(other instanceof Pair))
			return false;
		Object otherU = ((Pair<U,V>) other).getU();
		Object otherV = ((Pair<U,V>) other).getV();
		
		if (otherU == null) {
			if (this.u != null)
				return false;
		} else {
			if (!otherU.equals(this.u))
				return false;
		}

		if (otherV == null) {
			if (this.v != null)
				return false;
		} else {
			if (!otherV.equals(this.v))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return ((u != null) ? u.hashCode() : 0) + ((v != null) ? v.hashCode() : 0);
	}
	
	
	@Override
	public String toString() {
		return "[" + u + ";" + v + "]";
	}
	
}
