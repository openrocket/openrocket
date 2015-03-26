package net.sf.openrocket.file.openrocket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Config;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.TextUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenRocketSaver extends RocketSaver {
	private static final Logger log = LoggerFactory.getLogger(OpenRocketSaver.class);
	
	/**
	 * Divisor used in converting an integer version to the point-represented version.
	 * The integer version divided by this value is the major version and the remainder is
	 * the minor version.  For example 101 corresponds to file version "1.1".
	 */
	public static final int FILE_VERSION_DIVISOR = 100;
	
	
	private static final String OPENROCKET_CHARSET = "UTF-8";
	
	private static final String METHOD_PACKAGE = "net.sf.openrocket.file.openrocket.savers";
	private static final String METHOD_SUFFIX = "Saver";
	
	
	// Estimated storage used by different portions
	// These have been hand-estimated from saved files
	private static final int BYTES_PER_COMPONENT_COMPRESSED = 80;
	private static final int BYTES_PER_SIMULATION_COMPRESSED = 100;
	private static final int BYTES_PER_DATAPOINT_COMPRESSED = 100;
	
	
	private int indent;
	private Writer dest;
	
	@Override
	public void save(OutputStream output, OpenRocketDocument document, StorageOptions options) throws IOException {
		
		log.info("Saving .ork file");
		
		dest = new BufferedWriter(new OutputStreamWriter(output, OPENROCKET_CHARSET));
		
		// Select file version number
		final int fileVersion = calculateNecessaryFileVersion(document, options);
		final String fileVersionString =
				(fileVersion / FILE_VERSION_DIVISOR) + "." + (fileVersion % FILE_VERSION_DIVISOR);
		log.debug("Storing file version " + fileVersionString);
		
		
		this.indent = 0;
		
		
		writeln("<?xml version='1.0' encoding='utf-8'?>");
		writeln("<openrocket version=\"" + fileVersionString + "\" creator=\"OpenRocket "
				+ BuildProperties.getVersion() + "\">");
		indent++;
		
		// Recursively save the rocket structure
		saveComponent(document.getRocket());
		
		writeln("");
		
		// Save custom expressions;
		saveCustomDatatypes(document);
		
		// Save all simulations
		writeln("<simulations>");
		indent++;
		boolean first = true;
		for (Simulation s : document.getSimulations()) {
			if (!first)
				writeln("");
			first = false;
			saveSimulation(s, options.getSimulationTimeSkip());
		}
		indent--;
		writeln("</simulations>");
		
		indent--;
		writeln("</openrocket>");
		
		log.debug("Writing complete, flushing buffers");
		dest.flush();
	}
	
	/*
	 * Save all the custom expressions
	 */
	private void saveCustomDatatypes(OpenRocketDocument doc) throws IOException {
		
		if (doc.getCustomExpressions().isEmpty())
			return;
		
		writeln("<datatypes>");
		indent++;
		
		for (CustomExpression exp : doc.getCustomExpressions()) {
			saveCustomExpressionDatatype(exp);
		}
		
		indent--;
		writeln("</datatypes>");
		writeln("");
	}
	
	/*
	 * Save one custom expression datatype
	 */
	private void saveCustomExpressionDatatype(CustomExpression exp) throws IOException {
		// Write out custom expression
		
		writeln("<type source=\"customexpression\">");
		indent++;
		writeln("<name>" + exp.getName() + "</name>");
		writeln("<symbol>" + exp.getSymbol() + "</symbol>");
		writeln("<unit unittype=\"auto\">" + exp.getUnit() + "</unit>"); // auto unit type means it will be determined from string
		writeln("<expression>" + exp.getExpressionString() + "</expression>");
		indent--;
		writeln("</type>");
	}
	
	@Override
	public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
		
		long size = 0;
		
		// TODO - estimate decals
		
		// Size per component
		int componentCount = 0;
		Rocket rocket = doc.getRocket();
		Iterator<RocketComponent> iterator = rocket.iterator(true);
		while (iterator.hasNext()) {
			iterator.next();
			componentCount++;
		}
		
		size += componentCount * BYTES_PER_COMPONENT_COMPRESSED;
		
		
		// Size per simulation
		size += doc.getSimulationCount() * BYTES_PER_SIMULATION_COMPRESSED;
		
		
		// Size per flight data point
		int pointCount = 0;
		double timeSkip = options.getSimulationTimeSkip();
		if (timeSkip != StorageOptions.SIMULATION_DATA_NONE) {
			for (Simulation s : doc.getSimulations()) {
				FlightData data = s.getSimulatedData();
				if (data != null) {
					for (int i = 0; i < data.getBranchCount(); i++) {
						pointCount += countFlightDataBranchPoints(data.getBranch(i), timeSkip);
					}
				}
			}
		}
		
		size += pointCount * BYTES_PER_DATAPOINT_COMPRESSED;
		
		return size;
	}
	
	/**
	 * Public test accessor method for calculateNecessaryFileVersion, used by unit tests.
	 * 
	 * @param document	the document to output.
	 * @param opts		the storage options.
	 * @return			the integer file version to use.
	 */
	public int testAccessor_calculateNecessaryFileVersion(OpenRocketDocument document, StorageOptions opts) {
		// TODO: should check for test context here and fail if not running junit
		return calculateNecessaryFileVersion(document, opts);
	}
	
	/**
	 * Determine which file version is required in order to store all the features of the
	 * current design.  By default the oldest version that supports all the necessary features
	 * will be used.
	 * 
	 * @param document	the document to output.
	 * @param opts		the storage options.
	 * @return			the integer file version to use.
	 */
	private int calculateNecessaryFileVersion(OpenRocketDocument document, StorageOptions opts) {
		/*
		 * NOTE:  Remember to update the supported versions in DocumentConfig as well!
		 * 
		 * File version 1.7 is required for:
		 *  - simulation extensions
		 *  - saving tube fins.
		 * 
		 * File version 1.6 is required for:
		 *  - saving files using appearances and textures, flight configurations.
		 *  
		 * File version 1.5 is requires for:
		 *  - saving designs using ComponentPrests
		 *  - recovery device deployment on lower stage separation
		 *  - custom expressions
		 *  
		 * File version 1.4 is required for:
		 *  - saving simulation data
		 *  - saving motor data
		 * 
		 * File version 1.1 is required for:
		 *  - fin tabs
		 *  - components attached to tube coupler
		 * 
		 * Otherwise use version 1.0.
		 */
		
		/////////////////
		// Version 1.7 // 
		/////////////////
		for (Simulation sim : document.getSimulations()) {
			if (!sim.getSimulationExtensions().isEmpty()) {
				return FILE_VERSION_DIVISOR + 7;
			}
		}
		
		// Search the rocket for any TubeFinSet objects (version 1.7)
		for (RocketComponent c : document.getRocket()) {
			if (c instanceof TubeFinSet) {
				return FILE_VERSION_DIVISOR + 7;
			}
		}
		
		
		/////////////////
		// Version 1.6 // 
		/////////////////
		
		// Search the rocket for any Appearances or non-motor flight configurations (version 1.6)
		for (RocketComponent c : document.getRocket()) {
			if (c.getAppearance() != null) {
				return FILE_VERSION_DIVISOR + 6;
			}
			if (c instanceof FlightConfigurableComponent) {
				if (c instanceof MotorMount) {
					MotorMount mmt = (MotorMount) c;
					if (mmt.getIgnitionConfiguration().size() > 0) {
						return FILE_VERSION_DIVISOR + 6;
					}
				}
				if (c instanceof RecoveryDevice) {
					RecoveryDevice recovery = (RecoveryDevice) c;
					if (recovery.getDeploymentConfiguration().size() > 0) {
						return FILE_VERSION_DIVISOR + 6;
					}
				}
				if (c instanceof Stage) {
					Stage stage = (Stage) c;
					if (stage.getStageSeparationConfiguration().size() > 0) {
						return FILE_VERSION_DIVISOR + 6;
					}
				}
			}
		}
		
		/////////////////
		// Version 1.5 // 
		/////////////////
		
		// Search the rocket for any ComponentPresets (version 1.5)
		for (RocketComponent c : document.getRocket()) {
			if (c.getPresetComponent() != null) {
				return FILE_VERSION_DIVISOR + 5;
			}
		}
		
		// Search for recovery device deployment type LOWER_STAGE_SEPARATION (version 1.5)
		for (RocketComponent c : document.getRocket()) {
			if (c instanceof RecoveryDevice) {
				if (((RecoveryDevice) c).getDeploymentConfiguration().getDefault().getDeployEvent() == DeployEvent.LOWER_STAGE_SEPARATION) {
					return FILE_VERSION_DIVISOR + 5;
				}
			}
		}
		
		// Check for custom expressions (version 1.5)
		if (!document.getCustomExpressions().isEmpty()) {
			return FILE_VERSION_DIVISOR + 5;
		}
		
		/////////////////
		// Version 1.4 // 
		/////////////////
		
		// Check if design has simulations defined (version 1.4)
		if (document.getSimulationCount() > 0) {
			return FILE_VERSION_DIVISOR + 4;
		}
		
		// Check for motor definitions (version 1.4)
		for (RocketComponent c : document.getRocket()) {
			if (!(c instanceof MotorMount))
				continue;
			
			MotorMount mount = (MotorMount) c;
			for (String id : document.getRocket().getFlightConfigurationIDs()) {
				if (mount.getMotor(id) != null) {
					return FILE_VERSION_DIVISOR + 4;
				}
			}
		}
		
		/////////////////
		// Version 1.3 // 
		/////////////////
		
		// no version 1.3 file type exists
		
		/////////////////
		// Version 1.2 // 
		/////////////////
		
		// no version 1.2 file type exists
		
		/////////////////
		// Version 1.1 // 
		/////////////////
		
		// Check for fin tabs or tube coupler children (version 1.1)
		for (RocketComponent c : document.getRocket()) {
			// Check for fin tabs
			if (c instanceof FinSet) {
				FinSet fin = (FinSet) c;
				if (!MathUtil.equals(fin.getTabHeight(), 0) &&
						!MathUtil.equals(fin.getTabLength(), 0)) {
					return FILE_VERSION_DIVISOR + 1;
				}
			}
			
			// Check for components attached to tube coupler
			if (c instanceof TubeCoupler) {
				if (c.getChildCount() > 0) {
					return FILE_VERSION_DIVISOR + 1;
				}
			}
		}
		
		/////////////////
		// Version 1.0 // 
		/////////////////
		
		// Default (version 1.0)
		return FILE_VERSION_DIVISOR + 0;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void saveComponent(RocketComponent component) throws IOException {
		
		log.debug("Saving component " + component.getComponentName());
		
		Reflection.Method m = Reflection.findMethod(METHOD_PACKAGE, component, METHOD_SUFFIX,
				"getElements", RocketComponent.class);
		if (m == null) {
			throw new BugException("Unable to find saving class for component " +
					component.getComponentName());
		}
		
		// Get the strings to save
		List<String> list = (List<String>) m.invokeStatic(component);
		int length = list.size();
		
		if (length == 0) // Nothing to do
			return;
		
		if (length < 2) {
			throw new RuntimeException("BUG, component data length less than two lines.");
		}
		
		// Open element
		writeln(list.get(0));
		indent++;
		
		// Write parameters
		for (int i = 1; i < length - 1; i++) {
			writeln(list.get(i));
		}
		
		// Recursively write subcomponents
		if (component.getChildCount() > 0) {
			writeln("");
			writeln("<subcomponents>");
			indent++;
			boolean emptyline = false;
			for (RocketComponent subcomponent : component.getChildren()) {
				if (emptyline)
					writeln("");
				emptyline = true;
				saveComponent(subcomponent);
			}
			indent--;
			writeln("</subcomponents>");
		}
		
		// Close element
		indent--;
		writeln(list.get(length - 1));
	}
	
	
	private void saveSimulation(Simulation simulation, double timeSkip) throws IOException {
		SimulationOptions cond = simulation.getOptions();
		
		writeln("<simulation status=\"" + enumToXMLName(simulation.getStatus()) + "\">");
		indent++;
		
		writeln("<name>" + TextUtil.escapeXML(simulation.getName()) + "</name>");
		// TODO: MEDIUM: Other simulators/calculators
		
		writeln("<simulator>RK4Simulator</simulator>");
		writeln("<calculator>BarrowmanCalculator</calculator>");
		
		writeln("<conditions>");
		indent++;
		
		writeElement("configid", cond.getMotorConfigurationID());
		writeElement("launchrodlength", cond.getLaunchRodLength());
		writeElement("launchrodangle", cond.getLaunchRodAngle() * 180.0 / Math.PI);
		writeElement("launchroddirection", cond.getLaunchRodDirection() * 360.0 / (2.0 * Math.PI));
		writeElement("windaverage", cond.getWindSpeedAverage());
		writeElement("windturbulence", cond.getWindTurbulenceIntensity());
		writeElement("launchaltitude", cond.getLaunchAltitude());
		writeElement("launchlatitude", cond.getLaunchLatitude());
		writeElement("launchlongitude", cond.getLaunchLongitude());
		writeElement("geodeticmethod", cond.getGeodeticComputation().name().toLowerCase(Locale.ENGLISH));
		
		if (cond.isISAAtmosphere()) {
			writeln("<atmosphere model=\"isa\"/>");
		} else {
			writeln("<atmosphere model=\"extendedisa\">");
			indent++;
			writeElement("basetemperature", cond.getLaunchTemperature());
			writeElement("basepressure", cond.getLaunchPressure());
			indent--;
			writeln("</atmosphere>");
		}
		
		writeElement("timestep", cond.getTimeStep());
		
		indent--;
		writeln("</conditions>");
		
		for (SimulationExtension extension : simulation.getSimulationExtensions()) {
			Config config = extension.getConfig();
			writeln("<extension extensionid=\"" + TextUtil.escapeXML(extension.getId()) + "\">");
			indent++;
			if (config != null) {
				for (String key : config.keySet()) {
					Object value = config.get(key, null);
					writeEntry(key, value);
				}
			}
			indent--;
			writeln("</extension>");
		}
		
		// Write basic simulation data
		
		FlightData data = simulation.getSimulatedData();
		if (data != null) {
			String str = "<flightdata";
			if (!Double.isNaN(data.getMaxAltitude()))
				str += " maxaltitude=\"" + TextUtil.doubleToString(data.getMaxAltitude()) + "\"";
			if (!Double.isNaN(data.getMaxVelocity()))
				str += " maxvelocity=\"" + TextUtil.doubleToString(data.getMaxVelocity()) + "\"";
			if (!Double.isNaN(data.getMaxAcceleration()))
				str += " maxacceleration=\"" + TextUtil.doubleToString(data.getMaxAcceleration()) + "\"";
			if (!Double.isNaN(data.getMaxMachNumber()))
				str += " maxmach=\"" + TextUtil.doubleToString(data.getMaxMachNumber()) + "\"";
			if (!Double.isNaN(data.getTimeToApogee()))
				str += " timetoapogee=\"" + TextUtil.doubleToString(data.getTimeToApogee()) + "\"";
			if (!Double.isNaN(data.getFlightTime()))
				str += " flighttime=\"" + TextUtil.doubleToString(data.getFlightTime()) + "\"";
			if (!Double.isNaN(data.getGroundHitVelocity()))
				str += " groundhitvelocity=\"" + TextUtil.doubleToString(data.getGroundHitVelocity()) + "\"";
			if (!Double.isNaN(data.getLaunchRodVelocity()))
				str += " launchrodvelocity=\"" + TextUtil.doubleToString(data.getLaunchRodVelocity()) + "\"";
			if (!Double.isNaN(data.getDeploymentVelocity()))
				str += " deploymentvelocity=\"" + TextUtil.doubleToString(data.getDeploymentVelocity()) + "\"";
			str += ">";
			writeln(str);
			indent++;
			
			for (Warning w : data.getWarningSet()) {
				writeElement("warning", TextUtil.escapeXML(w.toString()));
			}
			
			// Check whether to store data
			if (simulation.getStatus() == Simulation.Status.EXTERNAL) // Always store external data
				timeSkip = 0;
			
			if (timeSkip != StorageOptions.SIMULATION_DATA_NONE) {
				for (int i = 0; i < data.getBranchCount(); i++) {
					FlightDataBranch branch = data.getBranch(i);
					saveFlightDataBranch(branch, timeSkip);
				}
			}
			
			indent--;
			writeln("</flightdata>");
		}
		
		indent--;
		writeln("</simulation>");
		
	}
	
	
	private void writeEntry(String key, Object value) throws IOException {
		if (value == null) {
			return;
		}
		String keyAttr;
		
		if (key != null) {
			keyAttr = "key=\"" + key + "\" ";
		} else {
			keyAttr = "";
		}
		
		if (value instanceof Boolean) {
			writeln("<entry " + keyAttr + "type=\"boolean\">" + value + "</entry>");
		} else if (value instanceof Number) {
			writeln("<entry " + keyAttr + "type=\"number\">" + value + "</entry>");
		} else if (value instanceof String) {
			writeln("<entry " + keyAttr + "type=\"string\">" + TextUtil.escapeXML((String) value) + "</entry>");
		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			writeln("<entry " + keyAttr + "type=\"list\">");
			indent++;
			for (Object o : list) {
				writeEntry(null, o);
			}
			indent--;
			writeln("</entry>");
		} else {
			// Unknown type
			log.error("Unknown configuration value type " + value.getClass() + "  value=" + value);
		}
	}
	
	private void saveFlightDataBranch(FlightDataBranch branch, double timeSkip)
			throws IOException {
		double previousTime = -100000;
		
		if (branch == null)
			return;
		
		// Retrieve the types from the branch
		FlightDataType[] types = branch.getTypes();
		
		if (types.length == 0)
			return;
		
		// Retrieve the data from the branch
		List<List<Double>> data = new ArrayList<List<Double>>(types.length);
		for (int i = 0; i < types.length; i++) {
			data.add(branch.get(types[i]));
		}
		List<Double> timeData = branch.get(FlightDataType.TYPE_TIME);
		
		// Build the <databranch> tag
		StringBuilder sb = new StringBuilder();
		sb.append("<databranch name=\"");
		sb.append(TextUtil.escapeXML(branch.getBranchName()));
		sb.append("\" ");
		
		// Kevins version where typekeys are used
		/*
		sb.append("\" typekeys=\"");
		for (int i = 0; i < types.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(escapeXML(types[i].getKey()));
		}
		*/
		
		if (!Double.isNaN(branch.getOptimumAltitude())) {
			sb.append("optimumAltitude=\"");
			sb.append(branch.getOptimumAltitude());
			sb.append("\" ");
		}
		
		if (!Double.isNaN(branch.getTimeToOptimumAltitude())) {
			sb.append("timeToOptimumAltitude=\"");
			sb.append(branch.getTimeToOptimumAltitude());
			sb.append("\" ");
		}
		
		sb.append("types=\"");
		for (int i = 0; i < types.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(TextUtil.escapeXML(types[i].getName()));
		}
		sb.append("\">");
		writeln(sb.toString());
		indent++;
		
		// Write events
		for (FlightEvent event : branch.getEvents()) {
			writeln("<event time=\"" + TextUtil.doubleToString(event.getTime())
					+ "\" type=\"" + enumToXMLName(event.getType()) + "\"/>");
		}
		
		// Write the data
		int length = branch.getLength();
		if (length > 0) {
			writeDataPointString(data, 0, sb);
			previousTime = timeData.get(0);
		}
		
		for (int i = 1; i < length - 1; i++) {
			if (timeData != null) {
				if (Math.abs(timeData.get(i) - previousTime - timeSkip) < Math.abs(timeData.get(i + 1) - previousTime - timeSkip)) {
					writeDataPointString(data, i, sb);
					previousTime = timeData.get(i);
				}
			} else {
				// If time data is not available, write all points
				writeDataPointString(data, i, sb);
			}
		}
		
		if (length > 1) {
			writeDataPointString(data, length - 1, sb);
		}
		
		indent--;
		writeln("</databranch>");
	}
	
	/* TODO: LOW: This is largely duplicated from above! */
	private int countFlightDataBranchPoints(FlightDataBranch branch, double timeSkip) {
		int count = 0;
		
		double previousTime = -100000;
		
		if (branch == null)
			return 0;
		
		// Retrieve the types from the branch
		FlightDataType[] types = branch.getTypes();
		
		if (types.length == 0)
			return 0;
		
		List<Double> timeData = branch.get(FlightDataType.TYPE_TIME);
		if (timeData == null) {
			// If time data not available, store all points
			return branch.getLength();
		}
		
		// Write the data
		int length = branch.getLength();
		if (length > 0) {
			count++;
			previousTime = timeData.get(0);
		}
		
		for (int i = 1; i < length - 1; i++) {
			if (Math.abs(timeData.get(i) - previousTime - timeSkip) < Math.abs(timeData.get(i + 1) - previousTime - timeSkip)) {
				count++;
				previousTime = timeData.get(i);
			}
		}
		
		if (length > 1) {
			count++;
		}
		
		return count;
	}
	
	
	
	private void writeDataPointString(List<List<Double>> data, int index, StringBuilder sb)
			throws IOException {
		sb.setLength(0);
		sb.append("<datapoint>");
		for (int j = 0; j < data.size(); j++) {
			if (j > 0)
				sb.append(",");
			sb.append(TextUtil.doubleToString(data.get(j).get(index)));
		}
		sb.append("</datapoint>");
		writeln(sb.toString());
	}
	
	
	
	private void writeElement(String element, Object content) throws IOException {
		if (content == null)
			content = "";
		writeln("<" + element + ">" + content + "</" + element + ">");
	}
	
	
	
	private void writeln(String str) throws IOException {
		if (str.length() == 0) {
			dest.write("\n");
			return;
		}
		String s = "";
		for (int i = 0; i < indent; i++)
			s = s + "  ";
		s = s + str + "\n";
		dest.write(s);
	}
	
	
	
	
	/**
	 * Return the XML equivalent of an enum name.
	 * 
	 * @param e		the enum to save.
	 * @return		the corresponding XML name.
	 */
	public static String enumToXMLName(Enum<?> e) {
		return e.name().toLowerCase(Locale.ENGLISH).replace("_", "");
	}
	
}
