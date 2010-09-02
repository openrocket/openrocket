package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.file.motor.MotorLoader;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Pair;

public class MotorCompareAll {
	
	/*
	 * Usage:
	 * 
	 * java MotorCompareAll  *.eng *.rse
	 */
	public static void main(String[] args) throws IOException {
		
		Map<String, Pair<List<ThrustCurveMotor>, List<String>>> map =
				new HashMap<String, Pair<List<ThrustCurveMotor>, List<String>>>();
		
		MotorLoader loader = new GeneralMotorLoader();
		
		for (String filename : args) {
			
			List<ThrustCurveMotor> motors = (List) loader.load(new FileInputStream(filename), filename);
			
			for (ThrustCurveMotor m : motors) {
				String key = m.getManufacturer() + ":" + m.getDesignation();
				Pair<List<ThrustCurveMotor>, List<String>> pair = map.get(key);
				if (pair == null) {
					pair = new Pair<List<ThrustCurveMotor>, List<String>>
							(new ArrayList<ThrustCurveMotor>(), new ArrayList<String>());
					map.put(key, pair);
				}
				pair.getU().add(m);
				pair.getV().add(filename);
			}
		}
		
		Collator collator = Collator.getInstance();
		
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys, collator);
		for (String basename : keys) {
			Pair<List<ThrustCurveMotor>, List<String>> pair = map.get(basename);
			System.err.println(basename + ": " + pair.getV());
			MotorCompare.compare(pair.getU(), pair.getV());
		}
	}
	
}
