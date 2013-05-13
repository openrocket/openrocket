package net.sf.openrocket.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.Pair;

public class SerializeMotors {
	
	public static void main(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.out.println("Usage:  java " + SerializeMotors.class.getCanonicalName() + " <input-dir> <output-file>");
			System.exit(1);
		}
		
		String inputDir = args[0];
		String outputFile = args[1];
		
		//Application.setPreferences(new SwingPreferences());
		
		File outFile = new File(outputFile);
		
		FileOutputStream ofs = new FileOutputStream(outFile);
		final ObjectOutputStream oos = new ObjectOutputStream(ofs);
		
		final List<Motor> allMotors = new ArrayList<Motor>();
		
		
		GeneralMotorLoader loader = new GeneralMotorLoader();
		FileIterator iterator = DirectoryIterator.findDirectory(inputDir, new SimpleFileFilter("", false, loader.getSupportedExtensions()));
		if (iterator == null) {
			System.out.println("Can't find resources-src/thrustcurves directory");
			System.exit(1);
		} else {
			while (iterator.hasNext()) {
				Pair<String, InputStream> f = iterator.next();
				String fileName = f.getU();
				InputStream is = f.getV();
				
				List<Motor> motors = loader.load(is, fileName);
				
				allMotors.addAll(motors);
			}
		}
		
		oos.writeObject(allMotors);
		
		oos.flush();
		ofs.flush();
		ofs.close();
	}
	
}
