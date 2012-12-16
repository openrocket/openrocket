package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.file.motor.MotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorPrinter {
	
	public static void main(String[] args) throws IOException {
		
		MotorLoader loader = new GeneralMotorLoader();
		
		System.out.println();
		for (String arg : args) {
			InputStream stream = new FileInputStream(arg);
			
			List<Motor> motors = loader.load(stream, arg);
			
			System.out.println("*** " + arg + " ***");
			System.out.println();
			for (Motor motor : motors) {
				ThrustCurveMotor m = (ThrustCurveMotor) motor;
				System.out.println("  Manufacturer:  " + m.getManufacturer());
				System.out.println("  Designation:   " + m.getDesignation());
				System.out.println("  Delays:        " +
						Arrays.toString(m.getStandardDelays()));
				System.out.printf("  Nominal time:  %.2f s\n", m.getBurnTimeEstimate());
				//				System.out.printf("  Total time:    %.2f s\n",  m.getTotalTime());
				System.out.printf("  Avg. thrust:   %.2f N\n", m.getAverageThrustEstimate());
				System.out.printf("  Max. thrust:   %.2f N\n", m.getMaxThrustEstimate());
				System.out.printf("  Total impulse: %.2f Ns\n", m.getTotalImpulseEstimate());
				System.out.println("  Diameter:      " + m.getDiameter() * 1000 + " mm");
				System.out.println("  Length:        " + m.getLength() * 1000 + " mm");
				System.out.println("  Digest:        " + m.getDigest());
				
				if (m instanceof ThrustCurveMotor) {
					ThrustCurveMotor tc = (ThrustCurveMotor) m;
					System.out.println("  Data points:   " + tc.getTimePoints().length);
					for (int i = 0; i < m.getTimePoints().length; i++) {
						double time = m.getTimePoints()[i];
						double thrust = m.getThrustPoints()[i];
						System.out.printf("    t=%.3f   F=%.3f\n", time, thrust);
					}
				}
				
				System.out.println("  Comment:");
				System.out.println(m.getDescription());
				System.out.println();
			}
		}
	}
}
