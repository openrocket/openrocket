package net.sf.openrocket.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.MotorLoaderHelper;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.Pair;

public class SerializeMotors {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		Application.setPreferences( new SwingPreferences() );

		File outFile = new File("resources/datafiles/thrustcurves","system.ser");

		FileOutputStream ofs = new FileOutputStream(outFile);
		final ObjectOutputStream oos = new ObjectOutputStream(ofs);

		final List<Motor> allMotors = new ArrayList<Motor>();

		ThrustCurveMotorSetDatabase motorDB = new ThrustCurveMotorSetDatabase(false) {

			@Override
			protected void loadMotors() {

				FileIterator iterator = DirectoryIterator.findDirectory("resources-src/datafiles/thrustcurves", new SimpleFileFilter("", false, "eng", "rse"));

				if ( iterator == null ) {
					throw new RuntimeException("Can't find resources-src/thrustcurves directory");
				}
				while( iterator.hasNext() ) {
					Pair<String,InputStream> f = iterator.next();
					String fileName = f.getU();
					InputStream is = f.getV();

					List<Motor> motors = MotorLoaderHelper.load(is, fileName);

					allMotors.addAll(motors);
				}
			}

		};

		motorDB.startLoading();

		oos.writeObject(allMotors);

		ofs.flush();
		ofs.close();
	}

}
