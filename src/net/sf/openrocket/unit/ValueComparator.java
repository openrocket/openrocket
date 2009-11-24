package net.sf.openrocket.unit;

import java.util.Comparator;

public class ValueComparator implements Comparator<Value> {

	public static final ValueComparator INSTANCE = new ValueComparator();
	
	@Override
	public int compare(Value o1, Value o2) {
		return o1.compareTo(o2);
	}

}
