package net.sf.openrocket.aerodynamics;

import java.util.AbstractSet;
import java.util.Iterator;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.Mutable;

/**
 * A set that contains multiple <code>Warning</code>s.  When adding a
 * {@link Warning} to this set, the contents is checked for a warning of the
 * same type.  If one is found, then the warning left in the set is determined
 * by the method {@link Warning#replaceBy(Warning)}.
 * <p>
 * A WarningSet can be made immutable by calling {@link #immute()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class WarningSet extends AbstractSet<Warning> implements Cloneable, Monitorable {
	
	private ArrayList<Warning> warnings = new ArrayList<Warning>();
	
	private Mutable mutable = new Mutable();
	
	private int modID = 0;
	
	/**
	 * Add a <code>Warning</code> to the set.  If a warning of the same type
	 * exists in the set, the warning that is left in the set is defined by the
	 * method {@link Warning#replaceBy(Warning)}.
	 * 
	 * @throws IllegalStateException	if this warning set has been made immutable.
	 */
	@Override
	public boolean add(Warning w) {
		mutable.check();
		
		modID++;
		int index = warnings.indexOf(w);
		
		if (index < 0) {
			warnings.add(w);
			return false;
		}
		
		Warning old = warnings.get(index);
		if (old.replaceBy(w)) {
			warnings.set(index, w);
		}
		
		return true;
	}
	
	/**
	 * Add a <code>Warning</code> with the specified text to the set.  The Warning object
	 * is created using the {@link Warning#fromString(String)} method.  If a warning of the
	 * same type exists in the set, the warning that is left in the set is defined by the
	 * method {@link Warning#replaceBy(Warning)}.
	 * 
	 * @param s		the warning text.
	 * @throws IllegalStateException	if this warning set has been made immutable.
	 */
	public boolean add(String s) {
		mutable.check();
		return add(Warning.fromString(s));
	}
	
	
	@Override
	public Iterator<Warning> iterator() {
		final Iterator<Warning> iterator = warnings.iterator();
		return new Iterator<Warning>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			
			@Override
			public Warning next() {
				return iterator.next();
			}
			
			@Override
			public void remove() {
				mutable.check();
				iterator.remove();
			}
			
		};
	}
	
	@Override
	public int size() {
		return warnings.size();
	}
	
	
	public void immute() {
		mutable.immute();
	}
	
	
	@Override
	public WarningSet clone() {
		try {
			
			WarningSet newSet = (WarningSet) super.clone();
			newSet.warnings = this.warnings.clone();
			newSet.mutable = this.mutable.clone();
			return newSet;
			
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException occurred, report bug!", e);
		}
	}
	
	
	@Override
	public String toString() {
		String s = "";
		
		for (Warning w : warnings) {
			if (s.length() > 0)
				s = s + ",";
			s += w.toString();
		}
		return "WarningSet[" + s + "]";
	}
	
	@Override
	public int getModID() {
		return modID;
	}
}
