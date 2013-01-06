package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.file.motor.MotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorDigester {
	
	public static void main(String[] args) {
		final MotorLoader loader = new GeneralMotorLoader();
		final boolean printFileNames;
		
		if (args.length == 0) {
			System.err.println("Usage:  MotorDigester <files>");
			printFileNames = false;
			System.exit(1);
		} else if (args.length == 1) {
			printFileNames = false;
		} else {
			printFileNames = true;
		}
		

		for (String file : args) {
			
			List<Motor> motors = null;
			try {
				InputStream stream = new FileInputStream(file);
				motors = loader.load(stream, file);
				stream.close();
			} catch (IOException e) {
				System.err.println("ERROR: " + e.getMessage());
				e.printStackTrace();
				continue;
			}
			
			for (Motor m : motors) {
				if (!(m instanceof ThrustCurveMotor)) {
					System.err.println(file + ": Not ThrustCurveMotor: " + m);
					continue;
				}
				
				String digest = ((ThrustCurveMotor) m).getDigest();
				if (printFileNames) {
					System.out.print(file + ": ");
				}
				System.out.println(digest);
			}
		}
		
	}
	
}
