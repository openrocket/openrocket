package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.GeneralMotorLoader;
import net.sf.openrocket.file.MotorLoader;
import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.rocketcomponent.ThrustCurveMotor;

public class MotorCheck {

	// Warn if less that this many points
	public static final int WARN_POINTS = 6;
	

	public static void main(String[] args) {
		MotorLoader loader = new GeneralMotorLoader();

		// Load files
		for (String file: args) {
			System.out.print("Checking " + file + "... ");
			System.out.flush();
			
			boolean ok = true;
			
			List<Motor> motors = null;
			try {
				InputStream stream = new FileInputStream(file);
				motors = loader.load(stream, file);
				stream.close();
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
				e.printStackTrace(System.out);
				ok = false;
			}
			
			String base = file.split("_")[0];
			String mfg = MotorLoader.convertManufacturer(base);
			
			if (motors != null) {
				if (motors.size() == 0) {
					System.out.println("ERROR: File contained no motors");
					ok = false;
				} else {
					for (Motor m: motors) {
						double sum = 0;
						sum += m.getAverageThrust();
						sum += m.getAverageTime();
						sum += m.getTotalImpulse();
						sum += m.getTotalTime();
						sum += m.getDiameter();
						sum += m.getLength();
						sum += m.getMass(0);
						sum += m.getMass(Double.POSITIVE_INFINITY);
						sum += m.getCG(0).x;
						sum += m.getCG(0).weight;
						sum += m.getMaxThrust();
						if (Double.isInfinite(sum) || Double.isNaN(sum)) {
							System.out.println("ERROR: Invalid motor values");
							ok = false;
						}
						
						if (!m.getManufacturer().equals(mfg)) {
							System.out.println("ERROR: Inconsistent manufacturer " + 
									m.getManufacturer() + " (file name indicates " + mfg 
									+ ")");
							ok = false;
						}
						
						int points = ((ThrustCurveMotor)m).getTimePoints().length;
						if (points < WARN_POINTS) {
							System.out.println("WARNING: Only " + points + " data points");
							ok = false;
						}
					}
				}
			}
			
			if (ok) {
				System.out.println("OK");
			}
		}
	}
}
