package net.sf.openrocket.util;

import java.util.Comparator;

public class NumericComparator implements Comparator<Object> {
	
	public static final NumericComparator INSTANCE = new NumericComparator();
	
	@Override
	public int compare(Object o1, Object o2) {
		double v1 = getValue(o1);
		double v2 = getValue(o2);
		
		if (Double.isNaN(v1) || Double.isNaN(v2)) {
			String s1 = o1.toString();
			String s2 = o2.toString();
			return s1.compareTo(s2);
		}
		
		return Double.compare(v1, v2);
	}
	
	private double getValue(Object o) {
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		String s = o.toString();
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
	
}
