package net.sf.openrocket.util;

import java.util.SortedSet;
import java.util.TreeSet;

public class PrintProperties {

	public static void main(String[] args) {

		// Sort the keys
		SortedSet<String> keys = new TreeSet<String>();
		for (Object key: System.getProperties().keySet()) {
			keys.add((String)key);
		}
		
		for (String key: keys) {
			System.out.println(key + "=" + System.getProperty((String)key));
		}
		
	}

}
