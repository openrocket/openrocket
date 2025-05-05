package info.openrocket.core.thrustcurve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import java.util.concurrent.*;
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
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);

		try {
			List<CompletableFuture<List<Motor>>> futureMotorLists = new ArrayList<>();

			for (String manufacturer : manufacturers) {
				System.out.println("Motors for : " + manufacturer);

				SearchRequest searchRequest = new SearchRequest();
				searchRequest.setManufacturer(manufacturer);
				SearchResponse res = ThrustCurveAPI.doSearch(searchRequest);

				for (TCMotor tcMotor : res.getResults()) {
					if (tcMotor.getData_files() == null || tcMotor.getData_files() == 0) {
						continue;
					}
					CompletableFuture<List<Motor>> future = fetchMotorsCompletables(tcMotor, executor);
					futureMotorLists.add(future);
				}
			}

			for (CompletableFuture<List<Motor>> futureList : futureMotorLists) {
				try {
					allMotors.addAll(futureList.get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}

		} finally {
			executor.shutdown();
		}
	}

	private static CompletableFuture<List<Motor>> fetchMotorsCompletables(TCMotor tcMotor, ExecutorService executor) {
		System.out.println(formatMotorMessage(tcMotor));
		Motor.Type type = getType(tcMotor);

		return CompletableFuture.supplyAsync(() -> fetchMotors(tcMotor, type), executor);
	}

	private static List<Motor> fetchMotors(TCMotor tcMotor, Motor.Type type) {
		List<Motor> motors = new ArrayList<>();
		try {
			List<MotorBurnFile> motorBurnFiles = getThrustCurvesForMotorId(tcMotor.getMotor_id());
			for (MotorBurnFile burnFile : motorBurnFiles) {
				try {
					ThrustCurveMotor.Builder builder = initThrustCurveMotorBuilder(tcMotor, burnFile, type);
					if (builder != null) {
						motors.add(builder.build());
					}
				} catch (IllegalArgumentException e) {
					System.out.println("\tError in simFile " + burnFile.getSimfileId() + ": " + e.getMessage());
					writeBadFile(burnFile);
				}
			}
			System.out.println("\t curves: " + motors.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return motors;
	}

	private static void writeBadFile(MotorBurnFile burnFile) {
		try (FileOutputStream out = new FileOutputStream("simfile-" + burnFile.getSimfileId())) {
			out.write(burnFile.getContents().getBytes());
		} catch (IOException e) {
			System.out.println("Unable to write bad file: " + e.getMessage());
		}
	}



	private static Motor.Type getType(TCMotor tcMotor) {
		final Motor.Type type = switch (tcMotor.getType()) {
			case "SU" -> Motor.Type.SINGLE;
			case "reload" -> Motor.Type.RELOAD;
			case "hybrid" -> Motor.Type.HYBRID;
			default -> Motor.Type.UNKNOWN;
		};
		return type;
	}

	private static StringBuilder formatMotorMessage(TCMotor tcMotor) {
		StringBuilder message = new StringBuilder();
		message.append(tcMotor.getManufacturer_abbr());
		message.append(" ");
		message.append(tcMotor.getCommon_name());
		message.append(" ");
		message.append(tcMotor.getMotor_id());
		return message;
	}
	private static ThrustCurveMotor.Builder initThrustCurveMotorBuilder(TCMotor tcMotor, MotorBurnFile burnFile, Motor.Type type) {
		ThrustCurveMotor.Builder builder = burnFile.getThrustCurveMotor();
		if (builder == null) {
			return null;
		}
		if (tcMotor.getTot_mass_g() != null) {
			builder.setInitialMass(tcMotor.getTot_mass_g() / 1000.0);
		}

		builder.setCaseInfo(tcMotor.getCase_info());
		builder.setPropellantInfo(tcMotor.getProp_info());
		builder.setDiameter(tcMotor.getDiameter() / 1000.0);
		builder.setLength(tcMotor.getLength() / 1000.0);
		builder.setMotorType(type);

		builder.setCommonName(tcMotor.getCommon_name());
		builder.setDesignation(tcMotor.getDesignation());

		if ("OOP".equals(tcMotor.getAvailability())) {
			builder.setAvailability(false);
		}
		return builder;

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
