package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.file.motor.MotorLoader;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorCheck {
	
	// Warn if less that this many points
	public static final int WARN_POINTS = 6;
	
	
	public static void main(String[] args) {
		MotorLoader loader = new GeneralMotorLoader();
		
		// Load files
		for (String file : args) {
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
			Manufacturer mfg = Manufacturer.getManufacturer(base);
			
			if (motors != null) {
				if (motors.size() == 0) {
					System.out.println("ERROR: File contained no motors");
					ok = false;
				} else {
					for (Motor motor : motors) {
						ThrustCurveMotor m = (ThrustCurveMotor) motor;
						double sum = 0;
						sum += m.getAverageThrustEstimate();
						sum += m.getBurnTimeEstimate();
						sum += m.getTotalImpulseEstimate();
						//						sum += m.getTotalTime();
						sum += m.getDiameter();
						sum += m.getLength();
						sum += m.getEmptyCG().weight;
						sum += m.getEmptyCG().x;
						sum += m.getLaunchCG().weight;
						sum += m.getLaunchCG().x;
						sum += m.getMaxThrustEstimate();
						if (Double.isInfinite(sum) || Double.isNaN(sum)) {
							System.out.println("ERROR: Invalid motor values");
							ok = false;
						}
						
						if (m.getManufacturer() != mfg) {
							System.out.println("ERROR: Inconsistent manufacturer " +
									m.getManufacturer() + " (file name indicates " + mfg
									+ ")");
							ok = false;
						}
						
						int points = ((ThrustCurveMotor) m).getTimePoints().length;
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
