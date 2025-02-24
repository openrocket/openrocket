package info.openrocket.core.thrustcurve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import info.openrocket.core.file.iterator.DirectoryIterator;
import info.openrocket.core.file.iterator.FileIterator;
import info.openrocket.core.file.motor.GeneralMotorLoader;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Pair;

public class SerializeThrustcurveMotors {

	private static final String[] manufacturers = {
		"AeroTech",
		"Alpha",
		"AMW",
		"Apogee",
		"Cesaroni",
		"Contrail",
		"Ellis",
		"Estes",
		"Gorilla",
		"Hypertek",
		"KBA",
		"Kosdon",
		"Loki",
		"TSP",
		"PP",
		"PML",
		"Quest",
		"RATT",
		"Klima",
		"Roadrunner",
		"RV",
		"SkyR",
		"SCR",
		"WCH"
	};

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage:  java " + SerializeThrustcurveMotors.class.getCanonicalName()
					+ " <input-dir> <output-file>");
			System.exit(1);
		}

		String inputDir = args[0];
		String outputFile = args[1];

		final List<Motor> allMotors = new ArrayList<>();

		loadFromLocalMotorFiles(allMotors, inputDir);

		loadFromThrustCurve(allMotors);

		File outFile = new File(outputFile);

		FileOutputStream ofs = new FileOutputStream(outFile);
		final ObjectOutputStream oos = new ObjectOutputStream(ofs);

		oos.writeObject(allMotors);

		oos.flush();
		ofs.flush();
		ofs.close();

	}

	public static void loadFromThrustCurve(List<Motor> allMotors) throws SAXException, IOException {

		SearchRequest searchRequest = new SearchRequest();
		for (String m : manufacturers) {
			searchRequest.setManufacturer(m);
			System.out.println("Motors for : " + m);

			SearchResponse res = ThrustCurveAPI.doSearch(searchRequest);

			for (TCMotor mi : res.getResults()) {
				StringBuilder message = new StringBuilder();
				message.append(mi.getManufacturer_abbr());
				message.append(" ");
				message.append(mi.getCommon_name());
				message.append(" ");
				message.append(mi.getMotor_id());

				if (mi.getData_files() == null || mi.getData_files().intValue() == 0) {
					continue;
				}

				final Motor.Type type = switch (mi.getType()) {
					case "SU" -> Motor.Type.SINGLE;
					case "reload" -> Motor.Type.RELOAD;
					case "hybrid" -> Motor.Type.HYBRID;
					default -> Motor.Type.UNKNOWN;
				};

				System.out.println(message);

				List<MotorBurnFile> b = getThrustCurvesForMotorId(mi.getMotor_id());
				for (MotorBurnFile burnFile : b) {
					try {
						ThrustCurveMotor.Builder builder = burnFile.getThrustCurveMotor();
						if (builder == null) {
							continue;
						}
						if (mi.getTot_mass_g() != null) {
							builder.setInitialMass(mi.getTot_mass_g() / 1000.0);
						}
						if (mi.getProp_mass_g() != null) {
							// builder.setPropellantMass(mi.getProp_mass_g() / 1000.0);
						}

						builder.setCaseInfo(mi.getCase_info());
						builder.setPropellantInfo(mi.getProp_info());
						builder.setDiameter(mi.getDiameter() / 1000.0);
						builder.setLength(mi.getLength() / 1000.0);
						builder.setMotorType(type);

						builder.setCommonName(mi.getCommon_name());
						builder.setDesignation(mi.getDesignation());

						if ("OOP".equals(mi.getAvailability())) {
							builder.setAvailability(false);
						}

						allMotors.add(builder.build());
					} catch (IllegalArgumentException e) {
						System.out.println("\tError in simFile " + burnFile.getSimfileId() + ":  " + e.getMessage());
						try {
							FileOutputStream out = new FileOutputStream(
									("simfile-" + burnFile.getSimfileId()).toString());
							out.write(burnFile.getContents().getBytes());
							out.close();
						} catch (IOException i) {
							System.out.println("unable to write bad file:  " + i.getMessage());
						}
					}

				}

				System.out.println("\t curves: " + b.size());

			}
		}
	}

	private static List<MotorBurnFile> getThrustCurvesForMotorId(int motorId) {
		String[] formats = new String[] { "RASP", "RockSim" };
		List<MotorBurnFile> b = new ArrayList<>();
		for (String format : formats) {
			try {
				List<MotorBurnFile> motorData = ThrustCurveAPI.downloadData(motorId, format);
				if (motorData != null) {
					b.addAll(motorData);
				}
			} catch (Exception ex) {
				System.out.println(
						"\tError downloading " + format + " for motorID=" + motorId + ": " + ex.getLocalizedMessage());
			}
		}
		return b;
	}

	private static void loadFromLocalMotorFiles(List<Motor> allMotors, String inputDir) throws IOException {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		FileIterator iterator = DirectoryIterator.findDirectory(inputDir,
				new SimpleFileFilter("", false, loader.getSupportedExtensions()));
		if (iterator == null) {
			System.out.println("Can't find " + inputDir + " directory");
			System.exit(1);
		} else {
			while (iterator.hasNext()) {
				Pair<File, InputStream> f = iterator.next();
				String fileName = f.getU().getName();
				InputStream is = f.getV();

				List<ThrustCurveMotor.Builder> motors = loader.load(is, fileName);

				for (ThrustCurveMotor.Builder builder : motors) {
					allMotors.add(builder.build());
				}
			}
		}

	}
}
