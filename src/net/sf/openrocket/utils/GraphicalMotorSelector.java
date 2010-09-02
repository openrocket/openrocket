package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Pair;

public class GraphicalMotorSelector {
	
	public static void main(String[] args) throws IOException {
		
		if (args.length == 0) {
			System.err.println("MotorPlot <files>");
			System.exit(1);
		}
		
		// Load files
		Map<String, List<Pair<String, ThrustCurveMotor>>> map =
				new LinkedHashMap<String, List<Pair<String, ThrustCurveMotor>>>();
		
		GeneralMotorLoader loader = new GeneralMotorLoader();
		for (String file : args) {
			
			for (Motor motor : loader.load(new FileInputStream(file), file)) {
				ThrustCurveMotor m = (ThrustCurveMotor) motor;
				System.out.println("Loaded " + m + " from file " + file);
				
				Pair<String, ThrustCurveMotor> pair = new Pair<String, ThrustCurveMotor>(file, m);
				String key = m.getManufacturer() + ":" + m.getDesignation();
				
				List<Pair<String, ThrustCurveMotor>> list = map.get(key);
				if (list == null) {
					list = new ArrayList<Pair<String, ThrustCurveMotor>>();
					map.put(key, list);
				}
				
				list.add(pair);
			}
		}
		

		// Go through different motors
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			List<Pair<String, ThrustCurveMotor>> list = map.get(key);
			

			// Select best one of identical motors
			List<String> filenames = new ArrayList<String>();
			List<ThrustCurveMotor> motors = new ArrayList<ThrustCurveMotor>();
			for (Pair<String, ThrustCurveMotor> pair : list) {
				String file = pair.getU();
				ThrustCurveMotor m = pair.getV();
				
				int index = indexOf(motors, m);
				if (index >= 0) {
					// Replace previous if this has more delays, a known type or longer comment
					ThrustCurveMotor m2 = motors.get(index);
					if (m.getStandardDelays().length > m2.getStandardDelays().length ||
							(m2.getMotorType() == Motor.Type.UNKNOWN &&
							m.getMotorType() != Motor.Type.UNKNOWN) ||
							(m.getDescription().trim().length() >
							m2.getDescription().trim().length())) {
						
						filenames.set(index, file);
						motors.set(index, m);
						
					}
				} else {
					filenames.add(file);
					motors.add(m);
				}
			}
			
			if (filenames.size() == 0) {
				
				System.out.println("ERROR selecting from " + list);
				System.exit(1);
				
			} else if (filenames.size() == 1) {
				
				select(filenames.get(0), list, false);
				
			} else {
				
				System.out.println("Choosing from " + filenames +
						" (" + count + "/" + map.size() + ")");
				MotorPlot plot = new MotorPlot(filenames, motors);
				plot.setVisible(true);
				plot.dispose();
				int n = plot.getSelected();
				if (n < 0) {
					System.out.println("NONE SELECTED from " + filenames);
				} else {
					select(filenames.get(n), list, true);
				}
				
			}
			
		}
		
	}
	
	private static void select(String selected, List<Pair<String, ThrustCurveMotor>> list, boolean manual) {
		System.out.print("SELECT " + selected + " ");
		if (manual) {
			System.out.println("(manual)");
		} else if (list.size() == 1) {
			System.out.println("(only)");
		} else {
			System.out.println("(identical)");
		}
		
		for (Pair<String, ThrustCurveMotor> pair : list) {
			String file = pair.getU();
			if (!file.equals(selected)) {
				System.out.println("IGNORE " + file);
			}
		}
	}
	
	
	private static int indexOf(List<ThrustCurveMotor> motors, ThrustCurveMotor motor) {
		for (int i = 0; i < motors.size(); i++) {
			ThrustCurveMotor m = motors.get(i);
			// TODO: Similar?
			if (m.equals(motor)) {
				if (m.getStandardDelays().length == 0 || motor.getStandardDelays().length == 0 ||
						Arrays.equals(m.getStandardDelays(), motor.getStandardDelays())) {
					return i;
				}
			}
		}
		return -1;
	}
}
