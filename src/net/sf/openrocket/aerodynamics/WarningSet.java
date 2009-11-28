package net.sf.openrocket.aerodynamics;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.openrocket.util.BugException;

/**
 * A set that contains multiple <code>Warning</code>s.  When adding a
 * {@link Warning} to this set, the contents is checked for a warning of the
 * same type.  If one is found, then the warning left in the set is determined
 * by the method {@link #Warning.replaceBy(Warning)}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class WarningSet extends AbstractSet<Warning> implements Cloneable {

	private ArrayList<Warning> warnings = new ArrayList<Warning>();
	
	
	/**
	 * Add a <code>Warning</code> to the set.  If a warning of the same type
	 * exists in the set, the warning that is left in the set is defined by the
	 * method {@link Warning#replaceBy(Warning)}.
	 */
	@Override
	public boolean add(Warning w) {
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
	 */
	public boolean add(String s) {
		return add(Warning.fromString(s));
	}
	
	
	@Override
	public Iterator<Warning> iterator() {
		return warnings.iterator();
	}

	@Override
	public int size() {
		return warnings.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public WarningSet clone() {
		try {
			
			WarningSet newSet = (WarningSet) super.clone();
			newSet.warnings = (ArrayList<Warning>) this.warnings.clone();
			return newSet;
			
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException occurred, report bug!",e);
		}
	}
	
	
	@Override
	public String toString() {
		String s = "";
		
		for (Warning w: warnings) {
			if (s.length() > 0)
				s = s+",";
			s += w.toString();
		}
		return "WarningSet[" + s + "]";
	}
}
