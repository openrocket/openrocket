package net.sf.openrocket.file;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.TextUtil;

public class CSVExport {
	
	/**
	 * Exports the specified flight data branch into a CSV file.
	 * 
	 * @param stream				the stream to write to.
	 * @param simulation			the simulation being exported.
	 * @param branch				the branch to export.
	 * @param fields				the fields to export (in appropriate order).
	 * @param units					the units of the fields.
	 * @param fieldSeparator		the field separator string.
	 * @param commentStarter		the comment starting character(s).
	 * @param simulationComments	whether to output general simulation comments.
	 * @param fieldComments			whether to output field comments.
	 * @param eventComments			whether to output comments for the flight events.
	 * @throws IOException 			if an I/O exception occurs.
	 */
	public static void exportCSV(OutputStream stream, Simulation simulation,
			FlightDataBranch branch, FlightDataType[] fields, Unit[] units,
			String fieldSeparator, String commentStarter, boolean simulationComments,
			boolean fieldComments, boolean eventComments) throws IOException {
		
		if (fields.length != units.length) {
			throw new IllegalArgumentException("fields and units lengths must be equal " +
					"(" + fields.length + " vs " + units.length + ")");
		}
		

		PrintWriter writer = null;
		try {
			
			writer = new PrintWriter(stream);
			
			// Write the initial comments
			if (simulationComments) {
				writeSimulationComments(writer, simulation, branch, fields, commentStarter);
			}
			
			if (simulationComments && fieldComments) {
				writer.println(commentStarter);
			}
			
			if (fieldComments) {
				writer.print(commentStarter + " ");
				for (int i = 0; i < fields.length; i++) {
					writer.print(fields[i].getName() + " (" + units[i].getUnit() + ")");
					if (i < fields.length - 1) {
						writer.print(fieldSeparator);
					}
				}
				writer.println();
			}
			
			writeData(writer, branch, fields, units, fieldSeparator,
					eventComments, commentStarter);
			

		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void writeData(PrintWriter writer, FlightDataBranch branch,
			FlightDataType[] fields, Unit[] units, String fieldSeparator, boolean eventComments,
			String commentStarter) {
		
		// Number of data points
		int n = branch.getLength();
		
		// Flight events in occurrance order
		List<FlightEvent> events = branch.getEvents();
		Collections.sort(events);
		int eventPosition = 0;
		
		// List of field values
		List<List<Double>> fieldValues = new ArrayList<List<Double>>();
		for (FlightDataType t : fields) {
			fieldValues.add(branch.get(t));
		}
		
		// Time variable
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		if (eventComments && time == null) {
			// If time information is not available, print events at beginning of file
			for (FlightEvent e : events) {
				printEvent(writer, e, commentStarter);
			}
			eventPosition = events.size();
		}
		

		// Loop over all data points
		for (int pos = 0; pos < n; pos++) {
			
			// Check for events to store
			if (eventComments && time != null) {
				double t = time.get(pos);
				
				while ((eventPosition < events.size()) &&
						(events.get(eventPosition).getTime() <= t)) {
					printEvent(writer, events.get(eventPosition), commentStarter);
					eventPosition++;
				}
			}
			
			// Store CSV line
			for (int i = 0; i < fields.length; i++) {
				double value = fieldValues.get(i).get(pos);
				writer.print(TextUtil.doubleToString(units[i].toUnit(value)));
				if (i < fields.length - 1) {
					writer.print(fieldSeparator);
				}
			}
			writer.println();
			
		}
		
		// Store any remaining events
		if (eventComments && time != null) {
			while (eventPosition < events.size()) {
				printEvent(writer, events.get(eventPosition), commentStarter);
				eventPosition++;
			}
		}
		
	}
	
	
	private static void printEvent(PrintWriter writer, FlightEvent e,
			String commentStarter) {
		writer.println(commentStarter + " Event " + e.getType().name() +
				" occurred at t=" + TextUtil.doubleToString(e.getTime()) + " seconds");
	}
	
	private static void writeSimulationComments(PrintWriter writer,
			Simulation simulation, FlightDataBranch branch, FlightDataType[] fields,
			String commentStarter) {
		
		String line;
		
		line = simulation.getName();
		
		FlightData data = simulation.getSimulatedData();
		
		switch (simulation.getStatus()) {
		case UPTODATE:
			line += " (Up to date)";
			break;
		
		case LOADED:
			line += " (Data loaded from a file)";
			break;
		
		case OUTDATED:
			line += " (Data is out of date)";
			break;
		
		case EXTERNAL:
			line += " (Imported data)";
			break;
		
		case NOT_SIMULATED:
			line += " (Not simulated yet)";
			break;
		}
		
		writer.println(commentStarter + " " + line);
		

		writer.println(commentStarter + " " + branch.getLength() + " data points written for "
				+ fields.length + " variables.");
		

		if (data == null) {
			writer.println(commentStarter + " No simulation data available.");
			return;
		}
		WarningSet warnings = data.getWarningSet();
		
		if (!warnings.isEmpty()) {
			writer.println(commentStarter + " Simulation warnings:");
			for (Warning w : warnings) {
				writer.println(commentStarter + "   " + w.toString());
			}
		}
	}
	
}
