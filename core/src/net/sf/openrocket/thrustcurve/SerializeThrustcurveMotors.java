package net.sf.openrocket.thrustcurve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Pair;

public class SerializeThrustcurveMotors {
	
	private static String[] manufacturers = {
			"AeroTech",
			"Alpha",
			"AMW",
			"Apogee",
			"Cesaroni",
			"Contrail",
			"Ellis",
			"Estes",
			"GR",
			"Hypertek",
			"KBA",
			"Kosdon",
			"Loki",
			"PP",
			"PML",
			"Quest",
			"RATT",
			"Roadrunner",
			"RV",
			"SkyR",
			"SCR",
			"WCH"
	};
	
	public static void main(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.out.println("Usage:  java " + SerializeThrustcurveMotors.class.getCanonicalName() + " <input-dir> <output-file>");
			System.exit(1);
		}
		
		String inputDir = args[0];
		String outputFile = args[1];
		
		final List<Motor> allMotors = new ArrayList<Motor>();
		
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
	
	public static void loadFromThrustCurve(List<Motor> allMotors) throws SAXException, MalformedURLException, IOException {
		
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
				
				final Motor.Type type;
				switch (mi.getType()) {
				case "SU":
					type = Motor.Type.SINGLE;
					break;
				case "reload":
					type = Motor.Type.RELOAD;
					break;
				case "hybrid":
					type = Motor.Type.HYBRID;
					break;
				default:
					type = Motor.Type.UNKNOWN;
					break;
				}
				
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
						
						if ("OOP".equals(mi.getAvailiability())) {
							builder.setDesignation(mi.getDesignation());
							builder.setAvailablity(false);
						} else if (mi.getDesignation().startsWith("Micro")) {
							builder.setDesignation(mi.getDesignation());
						} else {
							builder.setDesignation(mi.getCommon_name());
						}

						allMotors.add(builder.build());
					} catch (IllegalArgumentException e) {
						System.out.println("\tError in simFile " + burnFile.getSimfileId() + ":  " + e.getMessage());
						try {
							FileOutputStream out = new FileOutputStream(("simfile-" + burnFile.getSimfileId()).toString());
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
		List<MotorBurnFile> b = new ArrayList<>();
		try {
			b.addAll(ThrustCurveAPI.downloadData(motorId, "RockSim"));
		} catch (Exception ex) {
			System.out.println("\tError downloading RockSim for motorID=" + motorId);
		}
		try {
			b.addAll(ThrustCurveAPI.downloadData(motorId, "RASP"));
		} catch (Exception ex) {
			System.out.println("\tError downloading RASP for motorID=" + motorId);
		}
		return b;
	}
	
	private static void loadFromLocalMotorFiles(List<Motor> allMotors, String inputDir) throws IOException {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		FileIterator iterator = DirectoryIterator.findDirectory(inputDir, new SimpleFileFilter("", false, loader.getSupportedExtensions()));
		if (iterator == null) {
			System.out.println("Can't find " + inputDir + " directory");
			System.exit(1);
		} else {
			while (iterator.hasNext()) {
				Pair<String, InputStream> f = iterator.next();
				String fileName = f.getU();
				InputStream is = f.getV();
				
				List<ThrustCurveMotor.Builder> motors = loader.load(is, fileName);
				
				for (ThrustCurveMotor.Builder builder : motors) {
					allMotors.add(builder.build());
				}
			}
		}
		
	}
}
