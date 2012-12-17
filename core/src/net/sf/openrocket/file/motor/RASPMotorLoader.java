package net.sf.openrocket.file.motor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorDigest;
import net.sf.openrocket.motor.MotorDigest.DataType;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;

public class RASPMotorLoader extends AbstractMotorLoader {
	
	public static final String CHARSET_NAME = "ISO-8859-1";
	
	public static final Charset CHARSET = Charset.forName(CHARSET_NAME);
	
	
	
	
	@Override
	protected Charset getDefaultCharset() {
		return CHARSET;
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
	@Override
	public List<Motor> load(Reader reader, String filename) throws IOException {
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
			main: while (line != null) { // Until EOF
			
				manufacturer = "";
				designation = "";
				comment = "";
				length = 0;
				diameter = 0;
				delays = new ArrayList<Double>();
				propW = 0;
				totalW = 0;
				time.clear();
				thrust.clear();
				
				// Read comment
				while (line.length() == 0 || line.charAt(0) == ';') {
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
					buf = split(pieces[3], "[-,]+");
					for (int i = 0; i < buf.length; i++) {
						if (buf[i].equalsIgnoreCase("P") ||
								buf[i].equalsIgnoreCase("plugged")) {
							delays.add(Motor.PLUGGED);
						} else if (buf[i].matches("[0-9]+")) {
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
				manufacturer = pieces[6];
				
				if (propW > totalW) {
					throw new IOException("Propellant weight exceeds total weight in " +
							"RASP file " + filename);
				}
				
				// Read the data
				for (line = in.readLine(); (line != null) && (line.length() == 0 || line.charAt(0) != ';'); line = in.readLine()) {
					
					buf = split(line);
					if (buf.length == 0) {
						continue;
					} else if (buf.length == 2) {
						
						time.add(Double.parseDouble(buf[0]));
						thrust.add(Double.parseDouble(buf[1]));
						
					} else {
						throw new IOException("Illegal file format.");
					}
				}
				
				// Comment of EOF encountered, marks the start of the next motor
				if (time.size() < 2) {
					throw new IOException("Illegal file format, too short thrust-curve.");
				}
				double[] delayArray = new double[delays.size()];
				for (int i = 0; i < delays.size(); i++) {
					delayArray[i] = delays.get(i);
				}
				motors.add(createRASPMotor(manufacturer, designation, comment,
						length, diameter, delayArray, propW, totalW, time, thrust));
			}
			
		} catch (NumberFormatException e) {
			
			throw new IOException("Illegal file format.");
			
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
		sortLists(time, thrust);
		finalizeThrustCurve(time, thrust);
		List<Double> mass = calculateMass(time, thrust, totalW, propW);
		
		double[] timeArray = new double[time.size()];
		double[] thrustArray = new double[time.size()];
		Coordinate[] cgArray = new Coordinate[time.size()];
		for (int i = 0; i < time.size(); i++) {
			timeArray[i] = time.get(i);
			thrustArray[i] = thrust.get(i);
			cgArray[i] = new Coordinate(length / 2, 0, 0, mass.get(i));
		}
		
		designation = removeDelay(designation);
		
		// Create the motor digest from data available in RASP files
		MotorDigest motorDigest = new MotorDigest();
		motorDigest.update(DataType.TIME_ARRAY, timeArray);
		motorDigest.update(DataType.MASS_SPECIFIC, totalW, totalW - propW);
		motorDigest.update(DataType.FORCE_PER_TIME, thrustArray);
		final String digest = motorDigest.getDigest();
		
		try {
			
			Manufacturer m = Manufacturer.getManufacturer(manufacturer);
			return new ThrustCurveMotor(m, designation, comment, m.getMotorType(),
					delays, diameter, length, timeArray, thrustArray, cgArray, digest);
			
		} catch (IllegalArgumentException e) {
			
			// Bad data read from file.
			throw new IOException("Illegal file format.", e);
			
		}
	}
}
