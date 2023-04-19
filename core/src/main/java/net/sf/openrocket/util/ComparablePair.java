package net.sf.openrocket.util;

/**
 * Sortable storage of a pair of objects.  A list of these objects can be sorted according
 * to the first object.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @param <U>	the first object type, according to which comparisons are performed.
 * @param <V>	the second object type.
 */
public class ComparablePair<U extends Comparable<U>, V> extends Pair<U, V> 
	implements Comparable<ComparablePair<U, V>>{

	public ComparablePair(U u, V v) {
		super(u, v);
	}
	
	
	/**
	 * Compares the first objects.  If either of the objects is <code>null</code> this
	 * method throws <code>NullPointerException</code>.
	 */
	@Override
	public int compareTo(ComparablePair<U, V> other) {
		return this.getU().compareTo(other.getU());
	}

}
