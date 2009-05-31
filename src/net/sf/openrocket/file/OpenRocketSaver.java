package net.sf.openrocket.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.Prefs;
import net.sf.openrocket.util.Reflection;

public class OpenRocketSaver extends RocketSaver {
	
	/* Remember to update OpenRocketLoader as well! */
	public static final String FILE_VERSION = "1.0";
	
	private static final String OPENROCKET_CHARSET = "UTF-8";
	
	private static final String METHOD_PACKAGE = "net.sf.openrocket.file.openrocket";
	private static final String METHOD_SUFFIX = "Saver";
	
	private int indent;
	private Writer dest;
	
	@Override
	public void save(OutputStream output, OpenRocketDocument document, StorageOptions options)
	throws IOException {
		
		if (options.isCompressionEnabled()) {
			output = new GZIPOutputStream(output);
		}
		
		dest = new BufferedWriter(new OutputStreamWriter(output, OPENROCKET_CHARSET)); 
		
		
		this.indent = 0;
		
		System.out.println("Writing...");
		
		writeln("<?xml version='1.0' encoding='utf-8'?>");
		writeln("<openrocket version=\""+FILE_VERSION+"\" creator=\"OpenRocket "
				+Prefs.getVersion()+ "\">");
		indent++;
		
		// Recursively save the rocket structure
		saveComponent(document.getRocket());
		
		writeln("");
		
		// Save all simulations
		writeln("<simulations>");
		indent++;
		boolean first = true;
		for (Simulation s: document.getSimulations()) {
			if (!first)
				writeln("");
			first = false;
			saveSimulation(s, options.getSimulationTimeSkip());
		}
		indent--;
		writeln("</simulations>");
		
		indent--;
		writeln("</openrocket>");
		
		dest.flush();
		if (output instanceof GZIPOutputStream)
			((GZIPOutputStream)output).finish();
	}
	

	
	@SuppressWarnings("unchecked")
	private void saveComponent(RocketComponent component) throws IOException {
		
		Reflection.Method m = Reflection.findMethod(METHOD_PACKAGE, component, METHOD_SUFFIX,
				"getElements", RocketComponent.class);
		if (m==null) {
			throw new RuntimeException("Unable to find saving class for component "+
					component.getComponentName());
		}

		// Get the strings to save
		List<String> list = (List<String>) m.invokeStatic(component);
		int length = list.size();
		
		if (length == 0)  // Nothing to do
			return;

		if (length < 2) {
			throw new RuntimeException("BUG, component data length less than two lines.");
		}
		
		// Open element
		writeln(list.get(0));
		indent++;
		
		// Write parameters
		for (int i=1; i<length-1; i++) {
			writeln(list.get(i));
		}
		
		// Recursively write subcomponents
		if (component.getChildCount() > 0) {
			writeln("");
			writeln("<subcomponents>");
			indent++;
			boolean emptyline = false;
			for (RocketComponent subcomponent: component) {
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
		writeln(list.get(length-1));
	}

	
	
	private void saveSimulation(Simulation simulation, double timeSkip) throws IOException {
		SimulationConditions cond = simulation.getConditions();
		
		writeln("<simulation status=\"" + enumToXMLName(simulation.getStatus()) +"\">");
		indent++;
		
		writeln("<name>" + escapeXML(simulation.getName()) + "</name>");
		// TODO: MEDIUM: Other simulators/calculators
		writeln("<simulator>RK4Simulator</simulator>");
		writeln("<calculator>BarrowmanCalculator</calculator>");
		writeln("<conditions>");
		indent++;
		
		writeElement("configid", cond.getMotorConfigurationID());
		writeElement("launchrodlength", cond.getLaunchRodLength());
		writeElement("launchrodangle", cond.getLaunchRodAngle() * 180.0/Math.PI); 
		writeElement("launchroddirection", cond.getLaunchRodDirection() * 180.0/Math.PI);
		writeElement("windaverage", cond.getWindSpeedAverage());
		writeElement("windturbulence", cond.getWindTurbulenceIntensity());
		writeElement("launchaltitude", cond.getLaunchAltitude());
		writeElement("launchlatitude", cond.getLaunchLatitude());
		
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
		
		
		for (String s: simulation.getSimulationListeners()) {
			writeElement("listener", escapeXML(s));
		}
		
		
		// Write basic simulation data
		
		FlightData data = simulation.getSimulatedData();
		if (data != null) {
			String str = "<flightdata";
			if (!Double.isNaN(data.getMaxAltitude()))
				str += " maxaltitude=\"" + doubleToString(data.getMaxAltitude()) + "\"";
			if (!Double.isNaN(data.getMaxVelocity()))
				str += " maxvelocity=\"" + doubleToString(data.getMaxVelocity()) + "\"";
			if (!Double.isNaN(data.getMaxAcceleration()))
				str += " maxacceleration=\"" + doubleToString(data.getMaxAcceleration()) + "\"";
			if (!Double.isNaN(data.getMaxMachNumber()))
				str += " maxmach=\"" + doubleToString(data.getMaxMachNumber()) + "\"";
			if (!Double.isNaN(data.getTimeToApogee()))
				str += " timetoapogee=\"" + doubleToString(data.getTimeToApogee()) + "\"";
			if (!Double.isNaN(data.getFlightTime()))
				str += " flighttime=\"" + doubleToString(data.getFlightTime()) + "\"";
			if (!Double.isNaN(data.getGroundHitVelocity()))
				str += " groundhitvelocity=\"" + doubleToString(data.getGroundHitVelocity()) + "\"";
			str += ">";
			writeln(str);
			indent++;
			
			for (Warning w: data.getWarningSet()) {
				writeElement("warning", escapeXML(w.toString()));
			}
			
			// Check whether to store data
			if (simulation.getStatus() == Simulation.Status.EXTERNAL) // Always store external data
				timeSkip = 0;
			
			if (timeSkip != StorageOptions.SIMULATION_DATA_NONE) {
				for (int i=0; i<data.getBranchCount(); i++) {
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
	
	
	
	private void saveFlightDataBranch(FlightDataBranch branch, double timeSkip) throws IOException {
		double previousTime = -100;
		
		if (branch == null)
			return;
		
		// Retrieve the types from the branch
		FlightDataBranch.Type[] types = branch.getTypes();
		
		if (types.length == 0)
			return;
		
		// Retrieve the data from the branch
		List<List<Double>> data = new ArrayList<List<Double>>(types.length);
		for (int i=0; i<types.length; i++) {
			data.add(branch.get(types[i]));
		}
		List<Double> timeData = branch.get(FlightDataBranch.TYPE_TIME);
		if (timeData == null) {
			// TODO: MEDIUM: External data may not have time data
			throw new IllegalArgumentException("Data did not contain time data");
		}
		
		// Build the <databranch> tag
		StringBuilder sb = new StringBuilder();
		sb.append("<databranch name=\"");
		sb.append(escapeXML(branch.getBranchName()));
		sb.append("\" types=\"");
		for (int i=0; i<types.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(escapeXML(types[i].getName()));
		}
		sb.append("\">");
		writeln(sb.toString());
		indent++;
		
		// Write events
		for (Pair<Double,FlightEvent> p: branch.getEvents()) {
			writeln("<event time=\"" + doubleToString(p.getU())
					+ "\" type=\"" + enumToXMLName(p.getV().getType()) + "\"/>");
		}
		
		// Write the data
		int length = branch.getLength();
		if (length > 0) {
			writeDataPointString(data, 0, sb);
			previousTime = timeData.get(0);
		}
		
		for (int i=1; i < length-1; i++) {
			if (Math.abs(timeData.get(i) - previousTime - timeSkip) < 
					Math.abs(timeData.get(i+1) - previousTime - timeSkip)) {
				writeDataPointString(data, i, sb);
				previousTime = timeData.get(i);
			}
		}
		
		if (length > 1) {
			writeDataPointString(data, length-1, sb);
		}
		
		indent--;
		writeln("</databranch>");
	}
	
	private void writeDataPointString(List<List<Double>> data, int index, StringBuilder sb)
	throws IOException {
		sb.setLength(0);
		sb.append("<datapoint>");
		for (int j=0; j < data.size(); j++) {
			if (j > 0)
				sb.append(",");
			sb.append(doubleToString(data.get(j).get(index)));
		}
		sb.append("</datapoint>");
		writeln(sb.toString());
	}
	
	
	
	private void writeElement(String element, Object content) throws IOException {
		if (content == null)
			content = "";
		writeln("<"+element+">"+content+"</"+element+">");
	}


	
	private void writeln(String str) throws IOException {
		if (str.length() == 0) {
			dest.write("\n");
			return;
		}
		String s="";
		for (int i=0; i<indent; i++)
			s=s+"  ";
		s = s+str+"\n";
		dest.write(s);
	}
	
	
	/**
	 * Return a string of the double value with suitable precision.
	 * The string is the shortest representation of the value including the
	 * required precision.
	 * 
	 * @param d		the value to present.
	 * @return		a representation with suitable precision.
	 */
	public static final String doubleToString(double d) {
		
		// Check for special cases
		if (MathUtil.equals(d, 0))
			return "0";
		
		if (Double.isNaN(d))
			return "NaN";
		
		if (Double.isInfinite(d)) {
			if (d < 0)
				return "-Inf";
			else
				return "Inf";
		}
		
		
		double abs = Math.abs(d);
		
		if (abs < 0.001) {
			// Compact exponential notation
			int exp = 0;
			
			while (abs < 1.0) {
				abs *= 10;
				exp++;
			}
			
			String sign = (d < 0) ? "-" : "";
			return sign + String.format((Locale)null, "%.4fe-%d", abs, exp);
		}
		if (abs < 0.01)
			return String.format((Locale)null, "%.7f", d);
		if (abs < 0.1)
			return String.format((Locale)null, "%.6f", d);
		if (abs < 1)
			return String.format((Locale)null, "%.5f", d);
		if (abs < 10)
			return String.format((Locale)null, "%.4f", d);
		if (abs < 100)
			return String.format((Locale)null, "%.3f", d);
		if (abs < 1000)
			return String.format((Locale)null, "%.2f", d);
		if (abs < 10000)
			return String.format((Locale)null, "%.1f", d);
		if (abs < 100000000.0)
			return String.format((Locale)null, "%.0f", d);
			
		// Compact exponential notation
		int exp = 0;
		while (abs >= 10.0) {
			abs /= 10;
			exp++;
		}
		
		String sign = (d < 0) ? "-" : "";
		return sign + String.format((Locale)null, "%.4fe%d", abs, exp);
	}
	
	
	
	public static void main(String[] arg) {
		double d = -0.000000123456789123;
		
		
		for (int i=0; i< 20; i++) {
			String str = doubleToString(d);
			System.out.println(str + "   ->   " + Double.parseDouble(str));
			d *= 10;
		}
		
		
		System.out.println("Value: "+ Double.parseDouble("1.2345e9"));
		
	}

	
	/**
	 * Return the XML equivalent of an enum name.
	 * 
	 * @param e		the enum to save.
	 * @return		the corresponding XML name.
	 */
	public static String enumToXMLName(Enum<?> e) {
		return e.name().toLowerCase().replace("_", "");
	}
	
}
