package net.sf.openrocket.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.rocketcomponent.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;


public class MotorLoader implements Loader<Motor> {
	
	/** The charset used when reading RASP files. */
	public static final String RASP_CHARSET = "ISO-8859-1";

	
	
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		return loadMotor(stream, filename);
	}
	
	
	/**
	 * Load <code>Motor</code> objects from the specified <code>InputStream</code>.
	 * The file type is detected based on the filename extension. 
	 * 
	 * @param stream	the stream from which to read the file.
	 * @param filename	the file name, by which the format is detected.
	 * @return			a list of <code>Motor</code> objects defined in the file.
	 * @throws IOException  if an I/O exception occurs, the file format is unknown
	 * 						or illegal.
	 */
	public static List<Motor> loadMotor(InputStream stream, String filename) throws IOException {
		if (filename == null) {
			throw new IOException("Unknown file type.");
		}
		
		String ext = "";
		int point = filename.lastIndexOf('.');
		
		if (point > 0)
			ext = filename.substring(point+1);
		
		if (ext.equalsIgnoreCase("eng")) {
			return loadRASP(stream);
		}
		
		throw new IOException("Unknown file type.");
	}
	
	
	
	
	//////////////  RASP file format  //////////////
	
	
	/** Manufacturer codes to expand in RASP files */
	private static final Map<String,String> manufacturerCodes =
		new HashMap<String,String>();
	static {
		manufacturerCodes.put("A", "AeroTech");
		manufacturerCodes.put("AT", "AeroTech");
		manufacturerCodes.put("AT-RMS", "AeroTech");
		manufacturerCodes.put("AT/RCS", "AeroTech");
		manufacturerCodes.put("AERO", "AeroTech");
		manufacturerCodes.put("AEROT", "AeroTech");
		manufacturerCodes.put("ISP", "AeroTech");
		manufacturerCodes.put("AEROTECH", "AeroTech");
		manufacturerCodes.put("AEROTECH/APOGEE", "AeroTech");
		manufacturerCodes.put("AMW", "Animal Motor Works");
		manufacturerCodes.put("AW", "Animal Motor Works");
		manufacturerCodes.put("ANIMAL", "Animal Motor Works");
		manufacturerCodes.put("AP", "Apogee");
		manufacturerCodes.put("APOG", "Apogee");
		manufacturerCodes.put("P", "Apogee");
		manufacturerCodes.put("CES", "Cesaroni");
		manufacturerCodes.put("CTI", "Cesaroni");
		manufacturerCodes.put("CS", "Cesaroni");
		manufacturerCodes.put("CSR", "Cesaroni");
		manufacturerCodes.put("PRO38", "Cesaroni");
		manufacturerCodes.put("CR", "Contrail Rocket");
		manufacturerCodes.put("CONTR", "Contrail Rocket");
		manufacturerCodes.put("E", "Estes");
		manufacturerCodes.put("ES", "Estes");
		manufacturerCodes.put("EM", "Ellis Mountain");
		manufacturerCodes.put("ELLIS", "Ellis Mountain");
		manufacturerCodes.put("GR", "Gorilla Rocket Motors");
		manufacturerCodes.put("GORILLA", "Gorilla Rocket Motors");
		manufacturerCodes.put("H", "HyperTEK");
		manufacturerCodes.put("HT", "HyperTEK");
		manufacturerCodes.put("HYPER", "HyperTEK");
		manufacturerCodes.put("HYPERTEK", "HyperTEK");
		manufacturerCodes.put("K", "Kosdon by AeroTech");
		manufacturerCodes.put("KBA", "Kosdon by AeroTech");
		manufacturerCodes.put("K/AT", "Kosdon by AeroTech");
		manufacturerCodes.put("KOSDON", "Kosdon by AeroTech");
		manufacturerCodes.put("KOSDON/AT", "Kosdon by AeroTech");
		manufacturerCodes.put("KOSDON-BY-AEROTECH", "Kosdon by AeroTech");
		manufacturerCodes.put("LOKI", "Loki Research");
		manufacturerCodes.put("LR", "Loki Research");
		manufacturerCodes.put("PM", "Public Missiles");
		manufacturerCodes.put("PML", "Public Missiles");
		manufacturerCodes.put("PP", "Propulsion Polymers");
		manufacturerCodes.put("PROP", "Propulsion Polymers");
		manufacturerCodes.put("PROPULSION", "Propulsion Polymers");
		manufacturerCodes.put("PROPULSION-POLYMERS", "Propulsion Polymers");
		manufacturerCodes.put("Q", "Quest");
		manufacturerCodes.put("QU", "Quest");
		manufacturerCodes.put("RATT", "RATT Works");
		manufacturerCodes.put("RT", "RATT Works");
		manufacturerCodes.put("RTW", "RATT Works");
		manufacturerCodes.put("RR", "Roadrunner Rocketry");
		manufacturerCodes.put("ROADRUNNER", "Roadrunner Rocketry");
		manufacturerCodes.put("RV", "Rocketvision");
		manufacturerCodes.put("SR", "Sky Ripper Systems");
		manufacturerCodes.put("SRS", "Sky Ripper Systems");
		manufacturerCodes.put("SKYR", "Sky Ripper Systems");
		manufacturerCodes.put("SKYRIPPER", "Sky Ripper Systems");
		manufacturerCodes.put("WCH", "West Coast Hybrids");
		manufacturerCodes.put("WCR", "West Coast Hybrids");
		
		manufacturerCodes.put("SF", "WECO Feuerwerk");  // Previously Sachsen Feuerwerks
		manufacturerCodes.put("WECO", "WECO Feuerwerk");
		
	}
	
	/**
	 * A helper method to load a <code>Motor</code> from a RASP file, read from the
	 * specified <code>InputStream</code>.  The charset used is defined in 
	 * {@link #RASP_CHARSET}.
	 * 
	 * @param stream	the InputStream to read.
	 * @return			the <code>Motor</code> object. 
	 * @throws IOException  if an I/O error occurs or if the file format is illegal.
	 * @see #loadRASP(Reader)
	 */
	public static List<Motor> loadRASP(InputStream stream) throws IOException {
		return loadRASP(new InputStreamReader(stream, RASP_CHARSET));
	}
	
	
	
	/**
	 * Load a <code>Motor</code> from a RASP file specified by the <code>Reader</code>.
	 * The <code>Reader</code> is responsible for using the correct charset.
	 * <p>
	 * The CG is assumed to be located at the center of the motor casing and the mass
	 * is calculated from the thrust curve by assuming a constant exhaust velocity.
	 * 
	 * @param reader  the source of the file.
	 * @return		  a list of the {@link Motor} objects defined in the file.
	 * @throws IOException  if an I/O error occurs or if the file format is illegal.
	 */
	public static List<Motor> loadRASP(Reader reader) throws IOException {
		List<Motor> motors = new ArrayList<Motor>();
		BufferedReader in = new BufferedReader(reader);

		String manufacturer = "";
		String designation = "";
		String comment = "";
		
		double length = 0;
		double diameter = 0;
		ArrayList<Double> delays = null;
		
		List<Double> time = new ArrayList<Double>();
		List<Double> thrust = new ArrayList<Double>();
		
		double propW = 0;
		double totalW = 0;
		
		try {
			String line;
			String[] pieces, buf;

			line = in.readLine();
			main: while (line != null) {   // Until EOF

				manufacturer = "";
				designation = "";
				comment = "";
				length = 0;
				diameter = 0;
				delays = new ArrayList<Double>();
				propW = 0;
				totalW = 0;
				time.clear();
				thrust .clear();
			
				// Read comment
				while (line.length()==0 || line.charAt(0)==';') {
					if (line.length() > 0) {
						comment += line.substring(1).trim() + "\n";
					}
					line = in.readLine();
					if (line == null)
						break main;
				}
				comment = comment.trim();
				
				// Parse header line, example:
				// F32 24 124 5-10-15-P .0377 .0695 RV
				// desig diam len delays prop.w tot.w manufacturer
				pieces = split(line);
				if (pieces.length != 7) {
					throw new IOException("Illegal file format.");
				}
				
				designation = pieces[0];
				diameter = Double.parseDouble(pieces[1]) / 1000.0;
				length = Double.parseDouble(pieces[2]) / 1000.0;
				
				if (pieces[3].equalsIgnoreCase("None")) {

				} else {
					buf = split(pieces[3],"[-,]+");
					for (int i=0; i < buf.length; i++) {
						if (buf[i].equalsIgnoreCase("P")) {
							delays.add(Motor.PLUGGED);
						} else {
							// Many RASP files have "100" as an only delay
							double d = Double.parseDouble(buf[i]);
							if (d < 99)
								delays.add(d);
						}
					}
					Collections.sort(delays);
				}
				
				propW = Double.parseDouble(pieces[4]);
				totalW = Double.parseDouble(pieces[5]);
				if (manufacturerCodes.containsKey(pieces[6].toUpperCase())) {
					manufacturer = manufacturerCodes.get(pieces[6].toUpperCase());
				} else {
					manufacturer = pieces[6].replace('_', ' ');
				}
				
				// Read the data
				for (line = in.readLine(); 
					 (line != null) && (line.length()==0 || line.charAt(0) != ';');
					 line = in.readLine()) {
					
					buf = split(line);
					if (buf.length == 0) {
						continue;
					} else if (buf.length == 2) {
						
						time.add(Double.parseDouble(buf[0]));
						thrust .add(Double.parseDouble(buf[1]));
						
					} else {
						throw new IOException("Illegal file format.");
					}
				}
				
				// Comment of EOF encountered, marks the start of the next motor
				if (time.size() < 2) {
					throw new IOException("Illegal file format, too short thrust-curve.");
				}
				double[] delayArray = new double[delays.size()];
				for (int i=0; i<delays.size(); i++) {
					delayArray[i] = delays.get(i);
				}
				motors.add(createRASPMotor(manufacturer, designation, comment,
						length, diameter, delayArray, propW, totalW, time, thrust));
			}
			
		} catch (NumberFormatException e) {
			
			throw new IOException("Illegal file format.");
			
		} finally {
			
			in.close();
			
		}
		
		return motors;
	}
	
	
	/**
	 * Create a motor from RASP file data.
	 * @throws IOException  if the data is illegal for a thrust curve
	 */
	private static Motor createRASPMotor(String manufacturer, String designation,
			String comment, double length, double diameter, double[] delays,
			double propW, double totalW, List<Double> time, List<Double> thrust) 
			throws IOException {
		
		// Add zero time/thrust if necessary
		if (time.get(0) > 0) {
			time.add(0, 0.0);
			thrust.add(0, 0.0);
		}
		
		List<Double> mass = calculateMass(time,thrust,totalW,propW);
		
		double[] timeArray = new double[time.size()];
		double[] thrustArray = new double[time.size()];
		Coordinate[] cgArray = new Coordinate[time.size()];
		for (int i=0; i < time.size(); i++) {
			timeArray[i] = time.get(i);
			thrustArray[i] = thrust.get(i);
			cgArray[i] = new Coordinate(length/2,0,0,mass.get(i));
		}
		
		try {
			
			return new ThrustCurveMotor(manufacturer, designation, comment, Motor.Type.UNKNOWN,
					delays, diameter, length, timeArray, thrustArray, cgArray);
			
		} catch (IllegalArgumentException e) {
			
			// Bad data read from file.
			throw new IOException("Illegal file format.", e);
			
		}
	}
	
	
	
	/**
	 * Calculate the mass of a motor at distinct points in time based on the
	 * initial total mass, propellant weight and thrust.
	 * <p>
	 * This calculation assumes that the velocity of the exhaust remains constant
	 * during the burning.  This derives from the mass-flow and thrust relation
	 * <pre>F = m' * v</pre>
	 *  
	 * @param time    list of time points
	 * @param thrust  thrust at the discrete times
	 * @param total   total weight of the motor
	 * @param prop    propellant amount consumed during burning
	 * @return		  a list of the mass at the specified time points
	 */
	private static List<Double> calculateMass(List<Double> time, List<Double> thrust,
			double total, double prop) {
		List<Double> mass = new ArrayList<Double>();
		List<Double> deltam = new ArrayList<Double>();

		double t0, f0;
		double totalMassChange = 0;
		double scale;

		// First calculate mass change between points
		t0 = time.get(0);
		f0 = thrust.get(0);
		for (int i=1; i < time.size(); i++) {
			double t1 = time.get(i);
			double f1 = thrust.get(i);
			
			double dm = 0.5*(f0+f1)*(t1-t0);
			deltam.add(dm);
			totalMassChange += dm;
		}
		
		// Scale mass change and calculate mass
		mass.add(total);
		scale = prop / totalMassChange;
		for (double dm: deltam) {
			total -= dm*scale;
			mass.add(total);
		}
		
		return mass;
	}
	
	
	/**
	 * Tokenizes a string using whitespace as the delimiter.
	 */
	private static String[] split(String str) {
		return split(str,"\\s+");
	}
	
	/**
	 * Tokenizes a string using the given delimiter.
	 */
	private static String[] split(String str, String delim) {
		String[] pieces = str.split(delim);
		if (pieces.length==0 || !pieces[0].equals(""))
			return pieces;
		return Arrays.copyOfRange(pieces, 1, pieces.length);
	}
	
	
	
	
	
	/**
	 * For testing purposes.
	 */
	public static void main(String[] args) throws IOException {
		List<Motor> motors;
		
		if (args.length != 1) {
			System.out.println("Run with one argument, the RAPS file.");
			System.exit(1);
		}
		
		motors = loadRASP(new FileInputStream(new File(args[0])));
		
		for (Motor motor: motors) {
			double time = motor.getTotalTime();

			System.out.println("Motor " + motor);
			System.out.println("Manufacturer:    "+motor.getManufacturer());
			System.out.println("Designation:     "+motor.getDesignation());
			System.out.println("Type:            "+motor.getMotorType().getName());
			System.out.printf( "Length:          %.1f mm\n",motor.getLength()*1000);
			System.out.printf( "Diameter:        %.1f mm\n",motor.getDiameter()*1000);
			System.out.println("Comment:\n" + motor.getDescription());

			System.out.printf( "Total burn time: %.2f s\n", time);
			System.out.printf( "Avg. burn time:  %.2f s\n", motor.getAverageTime());
			System.out.printf( "Avg. thrust:     %.2f N\n", motor.getAverageThrust());
			System.out.printf( "Max. thrust:     %.2f N\n", motor.getMaxThrust());
			System.out.printf( "Total impulse:   %.2f Ns\n", motor.getTotalImpulse());
			System.out.println("Delay times:     " + 
					Arrays.toString(motor.getStandardDelays()));
			System.out.println("");
			
			final double COUNT = 20;
			for (int i=0; i <= COUNT; i++) {
				double t = time * i/COUNT;
				System.out.printf("t=%.2fs F=%.2fN m=%.4fkg\n",
						t, motor.getThrust(t), motor.getMass(t));
			}
			System.out.println("");
		}
		
	}
}
