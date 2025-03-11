package info.openrocket.core.file.openrocket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.openrocket.core.file.openrocket.savers.PhotoStudioSaver;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.material.Material;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.preferences.DocumentPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.RocketSaver;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.customexpression.CustomExpression;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.BuildProperties;
import info.openrocket.core.util.Config;
import info.openrocket.core.util.Reflection;
import info.openrocket.core.util.TextUtil;

public class OpenRocketSaver extends RocketSaver {
	private static final Logger log = LoggerFactory.getLogger(OpenRocketSaver.class);
	
	/**
	 * Divisor used in converting an integer version to the point-represented version.
	 * The integer version divided by this value is the major version and the remainder is
	 * the minor version.  For example 101 corresponds to file version "1.1".
	 */
	public static final int FILE_VERSION_DIVISOR = 100;
	
	
	private static final String OPENROCKET_CHARSET = "UTF-8";

	private static final String METHOD_PACKAGE = "info.openrocket.core.file.openrocket.savers";
	private static final String METHOD_SUFFIX = "Saver";
	public static final String INDENT = "  ";
	
	
	// Estimated storage used by different portions
	// These have been hand-estimated from saved files
	private static final int BYTES_PER_COMPONENT_COMPRESSED = 80;
	private static final int BYTES_PER_SIMULATION_COMPRESSED = 100;
	private static final int BYTES_PER_DATAPOINT_COMPRESSED = 100;
	
	
	private int indent;
	private Writer dest;
	
	@Override
	public void save(OutputStream output, OpenRocketDocument document, StorageOptions options, WarningSet warnings, ErrorSet errors) throws IOException {
		
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
			saveSimulation(s, options.getSaveSimulationData());
		}
		indent--;
		writeln("</simulations>");

		// Save PhotoSettings
		savePhotoSettings(document.getPhotoSettings());

		// Save document preferences
		saveDocumentPreferences(document.getDocumentPreferences());
		
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
		if (options.getSaveSimulationData()) {
			for (Simulation s : doc.getSimulations()) {
				FlightData data = s.getSimulatedData();
				if (data != null) {
					for (int i = 0; i < data.getBranchCount(); i++) {
						pointCount += countFlightDataBranchPoints(data.getBranch(i));
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
		 */
		return FILE_VERSION_DIVISOR + 10;
		
	}
	
	
	/**
	 * Finds a getElements method somewhere in the *saver class hierarchy corresponding to the given component.
	 */
	private static Reflection.Method findGetElementsMethod(RocketComponent component) {
		String currentclassname;
		Class<?> currentclass;
		String saverclassname;
		Class<?> saverClass;
		
		Reflection.Method mtr = null; // method-to-return
		
		currentclass = component.getClass();
		while ((currentclass != null) && (currentclass != Object.class)) {
			currentclassname = currentclass.getSimpleName();
			saverclassname = METHOD_PACKAGE + "." + currentclassname + METHOD_SUFFIX;
			
			try {
				saverClass = Class.forName(saverclassname);
				
				// if class exists
				java.lang.reflect.Method m = saverClass.getMethod("getElements", RocketComponent.class);
				mtr = new Reflection.Method(m);
				
				return mtr;
			} catch (Exception ignore) {
			}
			
			currentclass = currentclass.getSuperclass();
		}
		
		// if( null == mtr ){
		throw new BugException("Unable to find saving class for component " +
				METHOD_PACKAGE + "." + component.getClass().getSimpleName() + " ... " + METHOD_SUFFIX);
	}
	
	@SuppressWarnings("unchecked")
	private void saveComponent(RocketComponent component) throws IOException {
		log.debug("Saving component " + component.getComponentName());
		
		Reflection.Method m = findGetElementsMethod(component);
		
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
	
	
	private void saveSimulation(Simulation simulation, boolean saveSimulationData) throws IOException {
		SimulationOptions cond = simulation.getOptions();

		Simulation.Status simStatus;
		simStatus = saveSimulationData ? simulation.getStatus() : Simulation.Status.NOT_SIMULATED;

		writeln("<simulation status=\"" + enumToXMLName(simStatus) + "\">");
		indent++;
		
		writeln("<name>" + TextUtil.escapeXML(simulation.getName()) + "</name>");
		// TODO: MEDIUM: Other simulators/calculators
		
		writeln("<simulator>RK4Simulator</simulator>");
		writeln("<calculator>BarrowmanCalculator</calculator>");
		
		writeln("<conditions>");
		indent++;
		
		writeElement("configid", simulation.getId().key);
		writeElement("launchrodlength", cond.getLaunchRodLength());
		writeElement("launchrodangle", cond.getLaunchRodAngle() * 180.0 / Math.PI);
		writeElement("launchroddirection", cond.getLaunchRodDirection() * 360.0 / (2.0 * Math.PI));

		// TODO: remove once support for OR 23.09 and prior is dropped
		writeElement("windaverage", cond.getAverageWindModel().getAverage());
		writeElement("windturbulence", cond.getAverageWindModel().getTurbulenceIntensity());
		writeElement("winddirection", cond.getAverageWindModel().getDirection());

		writeln("<wind model=\"average\">");
		indent++;
		writeElement("speed", cond.getAverageWindModel().getAverage());
		writeElement("direction", cond.getAverageWindModel().getDirection());
		writeElement("standarddeviation", cond.getAverageWindModel().getStandardDeviation());
		indent--;
		writeln("</wind>");

		if (!cond.getMultiLevelWindModel().getLevels().isEmpty()) {
			WindModel.AltitudeReference altitudeRef = cond.getMultiLevelWindModel().getAltitudeReference();
			String altitudeRefString = enumToXMLName(altitudeRef);
			writeln("<wind model=\"multilevel\" altituderef=\"" + altitudeRefString + "\">");
			indent++;
			for (MultiLevelPinkNoiseWindModel.LevelWindModel level : cond.getMultiLevelWindModel().getLevels()) {
				writeln("<windlevel altitude=\"" + level.getAltitude() + "\" speed=\"" + level.getSpeed() +
						"\" direction=\"" + level.getDirection() + "\" standarddeviation=\"" + level.getStandardDeviation() +
						"\"/>");
			}
			indent--;
			writeln("</wind>");
		}

		writeElement("windmodeltype", cond.getWindModelType().toStringValue());

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
		writeElement("maxtime", cond.getMaxSimulationTime());
		
		indent--;
		writeln("</conditions>");
		
		for (SimulationExtension extension : simulation.getSimulationExtensions()) {
			Config config = extension.getConfig();
			writeln("<extension extensionid=\"" + TextUtil.escapeXML(extension.getId()) + "\">");
			indent++;
			if (config != null) {
				for (String key : config.keySet()) {
					Object value = config.get(key, null);
					writeEntry("entry", key, value, false);
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
			if (!Double.isNaN(data.getOptimumDelay()))
				str += " optimumdelay=\"" + TextUtil.doubleToString(data.getOptimumDelay()) + "\"";
			str += ">";
			writeln(str);
			indent++;
			
			for (Warning w : data.getWarningSet()) {
				writeln("<warning type=\"" + w.getClass().getSimpleName() + "\">");
				indent++; 

				writeElement("id", w.getID().toString());
				writeElement("description", w.getMessageDescription());
				writeElement("priority", w.getPriority());

				if (null != w.getSources()) {
					for (RocketComponent c : w.getSources()) {
						writeElement("source", c.getID());
					}
				}

				// Data for specific warning types
				if (w instanceof Warning.LargeAOA) {
					writeElement("parameter", ((Warning.LargeAOA) w).getAOA());
				}

				if (w instanceof Warning.HighSpeedDeployment) {
					writeElement("parameter", ((Warning.HighSpeedDeployment) w).getSpeed());
				}

				// We write the whole string content for backwards compatibility with old versions
				writeln(TextUtil.escapeXML(w.toString()));

				indent--;
				writeln("</warning>");
			}
			
			// Check whether to store data
			if ((simulation.getStatus() == Simulation.Status.EXTERNAL) || // Always store external data
				saveSimulationData) {
				for (int i = 0; i < data.getBranchCount(); i++) {
					FlightDataBranch branch = data.getBranch(i);
					saveFlightDataBranch(branch);
				}
			}
			
			indent--;
			writeln("</flightdata>");
		}
		
		indent--;
		writeln("</simulation>");
		
	}

	private void savePhotoSettings(Map<String, String> p) throws IOException {
		log.debug("Saving Photo Settings");

		writeln("<photostudio>");
		indent++;

		for (String s : PhotoStudioSaver.getElements(p))
			writeln(s);

		indent--;
		writeln("</photostudio>");
	}

	private void saveDocumentPreferences(DocumentPreferences docPrefs) throws IOException {
		log.debug("Saving Document Preferences");

		writeln("<docprefs>");
		indent++;

		// Normal preferences
		Map<String, DocumentPreferences.DocumentPreference> prefs = docPrefs.getPreferencesMap();
		for (Map.Entry<String, DocumentPreferences.DocumentPreference> entry : prefs.entrySet()) {
			DocumentPreferences.DocumentPreference pref = entry.getValue();
			writeEntry("pref", entry.getKey(), pref.getValue(), true);
		}

		// Document materials
		if (docPrefs.getTotalMaterialCount() > 0) {
			writeln("<docmaterials>");
			indent++;
			for (Material m : docPrefs.getAllMaterials()) {
				writeln("<material>" + m.toStorableString() + "</material>");
			}
			indent--;
			writeln("</docmaterials>");
		}

		indent--;
		writeln("</docprefs>");
	}

	/**
	 * Write an entry element, which has a key and type attribute, and a value, to the output.
	 * For example: <entry key="key" type="string">value</entry>
	 * @param tagName The tag name (e.g. 'entry')
	 * @param key The key attribute value
	 * @param value The value to store
	 * @param saveNumbersWithExplicitType If true, numbers will be stored with an explicit type attribute ('integer' or 'double'),
	 *                                    if false, save simply as 'number'
	 * @throws IOException
	 */
	private void writeEntry(String tagName, String key, Object value, boolean saveNumbersWithExplicitType) throws IOException {
		if (value == null) {
			return;
		}
		String keyAttr;

		if (key != null) {
			keyAttr = "key=\"" + key + "\" ";
		} else {
			keyAttr = "";
		}

		final String openTag = "<" + tagName + " ";
		final String closeTag = "</" + tagName + ">";
		if (value instanceof Boolean) {
			writeln(openTag + keyAttr + "type=\"boolean\">" + value + closeTag);
		} else if (value instanceof Number) {
			if (saveNumbersWithExplicitType) {
				if (value instanceof Integer) {
					writeln(openTag + keyAttr + "type=\"integer\">" + value + closeTag);
				} else if (value instanceof Double) {
					writeln(openTag + keyAttr + "type=\"double\">" + value + closeTag);
				} else {
					writeln(openTag + keyAttr + "type=\"number\">" + value + closeTag);
				}
			} else {
				writeln(openTag + keyAttr + "type=\"number\">" + value + closeTag);
			}
		} else if (value instanceof String) {
			writeln(openTag + keyAttr + "type=\"string\">" + TextUtil.escapeXML(value) + closeTag);
		} else if (value instanceof List<?> list) {
			// Nested element
			writeln(openTag + keyAttr + "type=\"list\">");
			indent++;
			for (Object o : list) {
				writeEntry(tagName, null, o, saveNumbersWithExplicitType);
			}
			indent--;
			writeln(closeTag);
		} else {
			// Unknown type
			log.error("Unknown configuration value type {}  value={}", value.getClass(), value);
		}
	}
	
	private void saveFlightDataBranch(FlightDataBranch branch)
			throws IOException {
		
		if (branch == null)
			return;
		
		// Retrieve the types from the branch
		FlightDataType[] types = branch.getTypes();
		
		if (types.length == 0)
			return;
		
		// Retrieve the data from the branch
		List<List<Double>> data = new ArrayList<>(types.length);
		for (FlightDataType type : types) {
			data.add(branch.get(type));
		}
		
		// Build the <databranch> tag
		StringBuilder sb = new StringBuilder();
		sb.append("<databranch name=\"");
		sb.append(TextUtil.escapeXML(branch.getName()));
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
			String eventStr = "<event time=\"" + TextUtil.doubleToString(event.getTime())
					+ "\" type=\"" + enumToXMLName(event.getType()) + "\"";
			
			if (event.getSource() != null) {
				eventStr += " source=\"" + TextUtil.escapeXML(event.getSource().getID()) + "\"";
			}

			if (event.getType() == FlightEvent.Type.SIM_WARN) {
				eventStr += " warnid=\"" + TextUtil.escapeXML(((Warning) event.getData()).getID()) + "\"";
			}
			
			if (event.getType() == FlightEvent.Type.SIM_ABORT) {
				eventStr += " cause=\"" + enumToXMLName(((SimulationAbort)(event.getData())).getCause()) + "\"";
			}

			eventStr += "/>";
			writeln(eventStr);
		}
		
		// Write the data
		int length = branch.getLength();
		for (int i = 0; i < length; i++) {
			writeDataPointString(data, i, sb);
		}
		
		indent--;
		writeln("</databranch>");
	}
	
	/* TODO: LOW: This is largely duplicated from above! */
	private int countFlightDataBranchPoints(FlightDataBranch branch) {
		int count = 0;
		
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
		
		// Count the data
		count += branch.getLength();
		
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
		writeln("<" + element + ">" + TextUtil.escapeXML(content) + "</" + element + ">");
	}
	
	private void writeln(String str) throws IOException {
		if (str.length() == 0) {
			dest.write("\n");
			return;
		}
		String s = INDENT.repeat(Math.max(0, indent)) + str + "\n";
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
