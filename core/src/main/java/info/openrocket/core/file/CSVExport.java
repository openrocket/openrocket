package info.openrocket.core.file;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.componentanalysis.CAParameters;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StringUtils;
import info.openrocket.core.util.TextUtil;

public class CSVExport {

	/**
	 * Exports the specified flight data branch into a CSV file.
	 *
	 * @param stream                the stream to write to.
	 * @param simulation            the simulation being exported.
	 * @param branch                the branch to export.
	 * @param fields                the fields to export (in appropriate order).
	 * @param units                 the units of the fields.
	 * @param fieldSeparator        the field separator string.
	 * @param decimalPlaces         the number of decimal places to use.
	 * @param isExponentialNotation whether to use exponential notation.
	 * @param commentStarter        the comment starting character(s).
	 * @param simulationComments    whether to output general simulation comments.
	 * @param fieldComments         whether to output field comments.
	 * @param eventComments         whether to output comments for the flight
	 *                              events.
	 * @throws IOException if an I/O exception occurs.
	 */
	public static void exportCSV(OutputStream stream, Simulation simulation,
			FlightDataBranch branch, FlightDataType[] fields, Unit[] units,
			String fieldSeparator, int decimalPlaces, boolean isExponentialNotation,
			String commentStarter, boolean simulationComments, boolean fieldComments,
			boolean eventComments) throws IOException {

		if (fields.length != units.length) {
			throw new IllegalArgumentException("fields and units lengths must be equal " +
					"(" + fields.length + " vs " + units.length + ")");
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(stream, false, StandardCharsets.UTF_8);

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

			writeData(writer, branch, fields, units, fieldSeparator, decimalPlaces, isExponentialNotation,
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

	public static void exportCSV(OutputStream stream, CAParameters parameters, CADataBranch branch,
								 CADomainDataType domainDataType, CADataType[] fields,
								 Map<CADataType, List<RocketComponent>> components, Unit[] units,
								 String fieldSeparator, int decimalPlaces, boolean isExponentialNotation,
								 boolean analysisComments, boolean fieldDescriptions, String commentStarter) throws IOException {
		if (fields.length != units.length) {
			throw new IllegalArgumentException("fields and units lengths must be equal " +
					"(" + fields.length + " vs " + units.length + ")");
		}
		if (fields.length != components.size()) {
			throw new IllegalArgumentException("fields and components lengths must be equal " +
					"(" + fields.length + " vs " + components.size() + ")");
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(stream, false, StandardCharsets.UTF_8);

			// Component analysis comments
			if (analysisComments) {
				writeComponentAnalysisComments(writer, parameters, branch, domainDataType, fields, components,
						fieldSeparator, commentStarter);
			}

			// Field names
			if (fieldDescriptions) {
				writer.print(prependComment(commentStarter, StringUtils.removeHTMLTags(domainDataType.getName())));
				writer.print(fieldSeparator);
				for (int i = 0; i < fields.length; i++) {
					for (int j = 0; j < components.get(fields[i]).size(); j++) {
						writer.print(StringUtils.removeHTMLTags(fields[i].getName()) +
								" (" + components.get(fields[i]).get(j).getName() + ") (" +
								units[i].getUnit() + ")");
						if (i < fields.length - 1) {
							writer.print(fieldSeparator);
						} else if (j < components.get(fields[i]).size() - 1) {
							writer.print(fieldSeparator);
						}
					}
				}
				writer.println();
			}

			writeData(writer, branch, domainDataType, fields, components, units, fieldSeparator, decimalPlaces,
					isExponentialNotation);

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

	private static void writeData(PrintWriter writer, FlightDataBranch branch, FlightDataType[] fields, Unit[] units,
								  String fieldSeparator, int decimalPlaces, boolean isExponentialNotation,
								  boolean eventComments, String commentStarter) {
		// Time variable
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);

		// Number of data points
		int n = time != null ? time.size() : branch.getLength();

		// Flight events in occurrence order
		List<FlightEvent> events = branch.getEvents();
		Collections.sort(events);
		int eventPosition = 0;

		// List of field values
		List<List<Double>> fieldValues = new ArrayList<>();
		for (FlightDataType t : fields) {
			List<Double> values = branch.get(t);
			fieldValues.add(values);
		}

		// If time information is not available, print events at beginning of file
		if (eventComments && time == null) {
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
				writer.print(TextUtil.doubleToString(units[i].toUnit(value), decimalPlaces, isExponentialNotation));

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

	private static void writeData(PrintWriter writer, CADataBranch branch, CADomainDataType domainDataType,
								  CADataType[] fields, Map<CADataType, List<RocketComponent>> components, Unit[] units,
								  String fieldSeparator, int decimalPlaces, boolean isExponentialNotation) {
		List<Double> domainValues = branch.get(domainDataType);

		int n = domainValues != null ? domainValues.size() : branch.getLength();

		// List of field values
		List<List<Double>> fieldValues = new ArrayList<>();
		for (int i = 0; i < fields.length; i++) {
			Unit unit = units[i];
			for (RocketComponent c : components.get(fields[i])) {
				List<Double> values = branch.get(fields[i], c);

				// Convert the values to the correct unit
				values.replaceAll(unit::toUnit);

				fieldValues.add(values);
			}
		}

		// Loop over all data points
		for (int pos = 0; pos < n; pos++) {
			// Store domain type
			if (domainValues != null) {
				writer.print(TextUtil.doubleToString(domainValues.get(pos), decimalPlaces, isExponentialNotation));
				writer.print(fieldSeparator);
			}

			// Store CSV line
			for (int i = 0; i < fieldValues.size(); i++) {
				double value = fieldValues.get(i).get(pos);
				writer.print(TextUtil.doubleToString(value, decimalPlaces, isExponentialNotation));

				if (i < fieldValues.size() - 1) {
					writer.print(fieldSeparator);
				}
			}
			writer.println();
		}
	}

	private static void printEvent(PrintWriter writer, FlightEvent e,
			String commentStarter) {
		writer.print(prependComment(commentStarter, "Event " + e.getType().name() +
				" occurred at t=" + TextUtil.doubleToString(e.getTime()) + " seconds"));
		if (e.getType() == FlightEvent.Type.SIM_WARN) {
			writer.print(": " + (Warning) e.getData());
		}
		writer.println();
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
				line += " (Out of date)";
				break;

			case EXTERNAL:
				line += " (Imported data)";
				break;

			case NOT_SIMULATED:
				line += " (Not simulated yet)";
				break;
		}

		writer.println(prependComment(commentStarter,line));

		writer.println(prependComment(commentStarter, branch.getLength() + " data points written for "
				+ fields.length + " variables."));

		if (data == null) {
			writer.println(prependComment(commentStarter, "No simulation data available."));
			return;
		}
		WarningSet warnings = data.getWarningSet();

		if (!warnings.isEmpty()) {
			writer.println(prependComment(commentStarter,"Simulation warnings:"));
			for (Warning w : warnings) {
				writer.println(prependComment(commentStarter, "  " + w.toString()));
			}
		}
	}

	private static void writeComponentAnalysisComments(PrintWriter writer, CAParameters parameters, CADataBranch branch,
													   CADomainDataType domainDataType, CADataType[] fields,
													   Map<CADataType, List<RocketComponent>> components,
													   String fieldSeparator, String commentStarter) {
		StringBuilder line = new StringBuilder(prependComment(commentStarter, "Parameters:")).append(fieldSeparator);

		if (domainDataType != CADomainDataType.WIND_DIRECTION) {
			line.append("Wind direction:").append(fieldSeparator);
			Unit unit = parameters.getThetaUnit();
			if (unit != null) {
				line.append(unit.toStringUnit(parameters.getTheta())).append(fieldSeparator);
			} else {
				line.append(parameters.getTheta()).append(fieldSeparator);
			}
		}
		if (domainDataType != CADomainDataType.AOA) {
			line.append("Angle of attack:").append(fieldSeparator);
			Unit unit = parameters.getAOAUnit();
			if (unit != null) {
				line.append(unit.toStringUnit(parameters.getAOA())).append(fieldSeparator);
			} else {
				line.append(parameters.getAOA()).append(fieldSeparator);
			}
		}
		if (domainDataType != CADomainDataType.MACH) {
			line.append("Mach:").append(fieldSeparator);
			Unit unit = parameters.getMachUnit();
			if (unit != null) {
				line.append(unit.toStringUnit(parameters.getMach())).append(fieldSeparator);
			} else {
				line.append(parameters.getMach()).append(fieldSeparator);
			}
		}
		if (domainDataType != CADomainDataType.ROLL_RATE) {
			line.append("Roll rate:").append(fieldSeparator);
			Unit unit = parameters.getRollRateUnit();
			if (unit != null) {
				line.append(unit.toStringUnit(parameters.getRollRate())).append(fieldSeparator);
			} else {
				line.append(parameters.getRollRate()).append(fieldSeparator);
			}
		}

		line.append("Active stages:").append(fieldSeparator);
		List<AxialStage> activeStages = parameters.getSelectedConfiguration().getActiveStages();
		for (AxialStage stage: activeStages) {
			line.append(stage.getName()).append(fieldSeparator);
		}

		line.append("Flight configuration:").append(fieldSeparator);
		line.append(parameters.getSelectedConfiguration().getName());

		writer.println(line);

		int nrOfVariables = 0;
		for (CADataType t : fields) {
			nrOfVariables += components.get(t).size();
		}

		writer.println(prependComment(commentStarter, branch.getLength() + " data points written for "
				+ nrOfVariables + " variables."));
	}

	private static String prependComment(String commentStarter, String comment) {
		return commentStarter + " " + comment;
	}
}
