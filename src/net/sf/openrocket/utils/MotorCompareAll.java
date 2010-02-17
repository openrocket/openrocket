package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.file.GeneralMotorLoader;
import net.sf.openrocket.file.MotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.Pair;

public class MotorCompareAll {

	/*
	 * Usage:
	 * 
	 * java MotorCompareAll  *.eng *.rse
	 */
	public static void main(String[] args) throws IOException {

		Map<String, Pair<List<Motor>, List<String>>> map =
			new HashMap<String, Pair<List<Motor>, List<String>>>();
		
		MotorLoader loader = new GeneralMotorLoader();
		
		for (String filename: args) {
			
			List<Motor> motors = loader.load(new FileInputStream(filename), filename);
			
			for (Motor m: motors) {
				String key = m.getManufacturer() + ":" + m.getDesignation();
				Pair<List<Motor>, List<String>> pair = map.get(key);
				if (pair == null) {
					pair = new Pair<List<Motor>, List<String>>
						(new ArrayList<Motor>(), new ArrayList<String>());
					map.put(key, pair);
				}
				pair.getU().add(m);
				pair.getV().add(filename);
			}
		}
		
		Collator collator = Collator.getInstance();
		
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys, collator);
		for (String basename: keys) {
			Pair<List<Motor>, List<String>> pair = map.get(basename);
			System.err.println(basename + ": " + pair.getV());
			MotorCompare.compare(pair.getU(), pair.getV());
		}
	}

}
