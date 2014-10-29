package net.sf.openrocket.file.motor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.NullElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorDigest;
import net.sf.openrocket.motor.MotorDigest.DataType;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RockSimMotorLoader extends AbstractMotorLoader {
	
	private static final Logger log = LoggerFactory.getLogger(RockSimMotorLoader.class);
	
	public static final String CHARSET_NAME = "UTF-8";
	
	public static final Charset CHARSET = Charset.forName(CHARSET_NAME);
	
	
	/** Any delay longer than this will be interpreted as a plugged motor. */
	private static final int DELAY_LIMIT = 90;
	
	
	
	@Override
	protected Charset getDefaultCharset() {
		return CHARSET;
	}
	
	
	
	/**
	 * Load a <code>Motor</code> from a RockSim motor definition file specified by the 
	 * <code>Reader</code>. The <code>Reader</code> is responsible for using the correct 
	 * charset.
	 * <p>
	 * If automatic CG/mass calculation is used, then the CG is assumed to be located at 
	 * the center of the motor casing and the mass is calculated from the thrust curve 
	 * by assuming a constant exhaust velocity.
	 * 
	 * @param reader  the source of the file.
	 * @return		  a list of the {@link Motor} objects defined in the file.
	 * @throws IOException  if an I/O error occurs or if the file format is invalid.
	 */
	@Override
	public List<Motor> load(Reader reader, String filename) throws IOException {
		InputSource source = new InputSource(reader);
		RSEHandler handler = new RSEHandler();
		WarningSet warnings = new WarningSet();
		
		try {
			SimpleSAX.readXML(source, handler, warnings);
			return handler.getMotors();
		} catch (SAXException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	
	
	/**
	 * Initial handler for the RockSim engine files.
	 */
	private static class RSEHandler extends AbstractElementHandler {
		private final List<Motor> motors = new ArrayList<Motor>();
		
		private RSEMotorHandler motorHandler;
		
		public List<Motor> getMotors() {
			return motors;
		}
		
		@Override
		public ElementHandler openElement(String element,
				HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
			
			if (element.equals("engine-database") ||
					element.equals("engine-list")) {
				// Ignore <engine-database> and <engine-list> elements
				return this;
			}
			
			if (element.equals("version")) {
				// Ignore <version> elements completely
				return null;
			}
			
			if (element.equals("engine")) {
				motorHandler = new RSEMotorHandler(attributes);
				return motorHandler;
			}
			
			return null;
		}
		
		@Override
		public void closeElement(String element, HashMap<String, String> attributes,
				String content, WarningSet warnings) throws SAXException {
			
			if (element.equals("engine")) {
				Motor motor = motorHandler.getMotor();
				motors.add(motor);
			}
		}
	}
	
	
	/**
	 * Handler for a RockSim engine file <motor> element.
	 */
	private static class RSEMotorHandler extends AbstractElementHandler {
		
		private final String manufacturer;
		private final String designation;
		private final double[] delays;
		private final double diameter;
		private final double length;
		private final double initMass;
		private final double propMass;
		private final Motor.Type type;
		private boolean calculateMass = false;
		private boolean calculateCG = false;
		
		private String description = "";
		
		private List<Double> time;
		private List<Double> force;
		private List<Double> mass;
		private List<Double> cg;
		
		private RSEMotorDataHandler dataHandler = null;
		
		
		public RSEMotorHandler(HashMap<String, String> attributes) throws SAXException {
			String str;
			
			// Manufacturer
			str = attributes.get("mfg");
			if (str == null)
				throw new SAXException("Manufacturer missing");
			manufacturer = str;
			
			// Designation
			str = attributes.get("code");
			if (str == null)
				throw new SAXException("Designation missing");
			designation = removeDelay(str);
			
			// Delays
			ArrayList<Double> delayList = new ArrayList<Double>();
			str = attributes.get("delays");
			if (str != null) {
				String[] split = str.split(",");
				for (String delay : split) {
					try {
						
						double d = Double.parseDouble(delay);
						if (d >= DELAY_LIMIT)
							d = Motor.PLUGGED;
						delayList.add(d);
						
					} catch (NumberFormatException e) {
						if (str.equalsIgnoreCase("P") || str.equalsIgnoreCase("plugged")) {
							delayList.add(Motor.PLUGGED);
						}
					}
				}
			}
			delays = new double[delayList.size()];
			for (int i = 0; i < delayList.size(); i++) {
				delays[i] = delayList.get(i);
			}
			
			// Diameter
			str = attributes.get("dia");
			if (str == null)
				throw new SAXException("Diameter missing");
			try {
				diameter = Double.parseDouble(str) / 1000.0;
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid diameter " + str);
			}
			
			// Length
			str = attributes.get("len");
			if (str == null)
				throw new SAXException("Length missing");
			try {
				length = Double.parseDouble(str) / 1000.0;
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid length " + str);
			}
			
			// Initial mass
			str = attributes.get("initWt");
			if (str == null)
				throw new SAXException("Initial mass missing");
			try {
				initMass = Double.parseDouble(str) / 1000.0;
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid initial mass " + str);
			}
			
			// Propellant mass
			str = attributes.get("propWt");
			if (str == null)
				throw new SAXException("Propellant mass missing");
			try {
				propMass = Double.parseDouble(str) / 1000.0;
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid propellant mass " + str);
			}
			
			if (propMass > initMass) {
				throw new SAXException("Propellant weight exceeds total weight in " +
						"RockSim engine format");
			}
			
			// Motor type
			str = attributes.get("Type");
			if ("single-use".equalsIgnoreCase(str)) {
				type = Motor.Type.SINGLE;
			} else if ("hybrid".equalsIgnoreCase(str)) {
				type = Motor.Type.HYBRID;
			} else if ("reloadable".equalsIgnoreCase(str)) {
				type = Motor.Type.RELOAD;
			} else {
				type = Motor.Type.UNKNOWN;
			}
			
			// Calculate mass
			str = attributes.get("auto-calc-mass");
			if ("0".equals(str) || "false".equalsIgnoreCase(str)) {
				calculateMass = false;
			} else {
				calculateMass = true;
			}
			
			// Calculate CG
			str = attributes.get("auto-calc-cg");
			if ("0".equals(str) || "false".equalsIgnoreCase(str)) {
				calculateCG = false;
			} else {
				calculateCG = true;
			}
		}
		
		@Override
		public ElementHandler openElement(String element,
				HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
			
			if (element.equals("comments")) {
				return PlainTextHandler.INSTANCE;
			}
			
			if (element.equals("data")) {
				if (dataHandler != null) {
					throw new SAXException("Multiple data elements encountered in motor " +
							"definition");
				}
				dataHandler = new RSEMotorDataHandler();
				return dataHandler;
			}
			
			warnings.add("Unknown element '" + element + "' encountered, ignoring.");
			return null;
		}
		
		@Override
		public void closeElement(String element, HashMap<String, String> attributes,
				String content, WarningSet warnings) {
			
			if (element.equals("comments")) {
				if (description.length() > 0) {
					description = description + "\n\n" + content.trim();
				} else {
					description = content.trim();
				}
				return;
			}
			
			if (element.equals("data")) {
				time = dataHandler.getTime();
				force = dataHandler.getForce();
				mass = dataHandler.getMass();
				cg = dataHandler.getCG();
				
				sortLists(time, force, mass, cg);
				
				for (double d : mass) {
					if (Double.isNaN(d)) {
						calculateMass = true;
						break;
					}
				}
				for (double d : cg) {
					if (Double.isNaN(d)) {
						calculateCG = true;
						break;
					}
				}
				return;
			}
		}
		
		public Motor getMotor() throws SAXException {
			if (time == null || time.size() == 0)
				throw new SAXException("Illegal motor data");
			
			finalizeThrustCurve(time, force, mass, cg);
			
			final int n = time.size();
			
			if (hasIllegalValue(mass))
				calculateMass = true;
			if (hasIllegalValue(cg))
				calculateCG = true;
			
			if (calculateMass) {
				mass = calculateMass(time, force, initMass, propMass);
			}
			
			if (calculateCG) {
				for (int i = 0; i < n; i++) {
					cg.set(i, length / 2);
				}
			}
			
			double[] timeArray = toArray(time);
			double[] thrustArray = toArray(force);
			Coordinate[] cgArray = new Coordinate[n];
			for (int i = 0; i < n; i++) {
				cgArray[i] = new Coordinate(cg.get(i), 0, 0, mass.get(i));
			}
			
			
			// Create the motor digest from all data available in the file
			MotorDigest motorDigest = new MotorDigest();
			motorDigest.update(DataType.TIME_ARRAY, timeArray);
			if (!calculateMass) {
				motorDigest.update(DataType.MASS_PER_TIME, toArray(mass));
			} else {
				motorDigest.update(DataType.MASS_SPECIFIC, initMass, initMass - propMass);
			}
			if (!calculateCG) {
				motorDigest.update(DataType.CG_PER_TIME, toArray(cg));
			}
			motorDigest.update(DataType.FORCE_PER_TIME, thrustArray);
			final String digest = motorDigest.getDigest();
			
			
			try {
				Manufacturer m = Manufacturer.getManufacturer(manufacturer);
				Motor.Type t = type;
				if (t == Motor.Type.UNKNOWN) {
					t = m.getMotorType();
				} else {
					if (m.getMotorType() != Motor.Type.UNKNOWN && m.getMotorType() != t) {
						log.warn("Loaded motor type inconsistent with manufacturer," +
								" loaded type=" + t + " manufacturer=" + m +
								" manufacturer type=" + m.getMotorType() +
								" designation=" + designation);
					}
				}
				
				return new ThrustCurveMotor(m, designation, description, t,
						delays, diameter, length, timeArray, thrustArray, cgArray, digest);
			} catch (IllegalArgumentException e) {
				throw new SAXException("Illegal motor data", e);
			}
		}
	}
	
	
	/**
	 * Handler for the <data> element in a RockSim engine file motor definition.
	 */
	private static class RSEMotorDataHandler extends AbstractElementHandler {
		
		private final List<Double> time = new ArrayList<Double>();
		private final List<Double> force = new ArrayList<Double>();
		private final List<Double> mass = new ArrayList<Double>();
		private final List<Double> cg = new ArrayList<Double>();
		
		
		public List<Double> getTime() {
			return time;
		}
		
		public List<Double> getForce() {
			return force;
		}
		
		public List<Double> getMass() {
			return mass;
		}
		
		public List<Double> getCG() {
			return cg;
		}
		
		
		@Override
		public ElementHandler openElement(String element,
				HashMap<String, String> attributes, WarningSet warnings) {
			
			if (element.equals("eng-data")) {
				return NullElementHandler.INSTANCE;
			}
			
			warnings.add("Unknown element '" + element + "' encountered, ignoring.");
			return null;
		}
		
		@Override
		public void closeElement(String element, HashMap<String, String> attributes,
				String content, WarningSet warnings) throws SAXException {
			
			double t = parseDouble(attributes.get("t"));
			double f = parseDouble(attributes.get("f"));
			double m = parseDouble(attributes.get("m")) / 1000.0;
			double g = parseDouble(attributes.get("cg")) / 1000.0;
			
			if (Double.isNaN(t) || Double.isNaN(f)) {
				throw new SAXException("Illegal motor data point encountered");
			}
			
			time.add(t);
			force.add(f);
			mass.add(m);
			cg.add(g);
		}
		
		
		private double parseDouble(String str) {
			if (str == null)
				return Double.NaN;
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
	}
	
	
	
	private static boolean hasIllegalValue(List<Double> list) {
		for (Double d : list) {
			if (d == null || d.isNaN() || d.isInfinite()) {
				return true;
			}
		}
		return false;
	}
	
	private static double[] toArray(List<Double> list) {
		final int n = list.size();
		double[] array = new double[n];
		for (int i = 0; i < n; i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}
