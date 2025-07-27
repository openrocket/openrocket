package info.openrocket.swing.gui.util;

import java.awt.Window;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.componentanalysis.CAParameters;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.dialogs.SwingWorkerDialog;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.CSVExport;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.TextUtil;

public class SaveCSVWorker extends SwingWorker<Void, Void> {

	private static final int BYTES_PER_FIELD_PER_POINT = 7;

	private final File file;
	private final String fieldSeparator;
	private final int decimalPlaces;
	private final boolean isExponentialNotation;
	private final String commentStarter;

	// Simulation-specific fields
	private Simulation simulation;
	private FlightDataBranch flightDataBranch;
	private FlightDataType[] flightDataFields;
	private Unit[] flightDataUnits;
	private boolean simulationComments;
	private boolean fieldComments;
	private boolean eventComments;

	// CA-specific fields
	private CAParameters caParameters;
	private CADataBranch caDataBranch;
	private CADomainDataType caDomainDataType;
	private CADataType[] caDataFields;
	private Map<CADataType, List<RocketComponent>> caComponents;
	private Unit[] caUnits;
	private boolean analysisComments;
	private boolean fieldDescriptions;

	private boolean isCAData;

	// Constructor for simulation data
	public SaveCSVWorker(File file, Simulation simulation, FlightDataBranch branch,
						 FlightDataType[] fields, Unit[] units, String fieldSeparator, int decimalPlaces,
						 boolean isExponentialNotation, String commentStarter, boolean simulationComments,
						 boolean fieldComments, boolean eventComments) {
		this.file = file;
		this.simulation = simulation;
		this.flightDataBranch = branch;
		this.flightDataFields = fields;
		this.flightDataUnits = units;
		this.fieldSeparator = fieldSeparator;
		this.decimalPlaces = decimalPlaces;
		this.isExponentialNotation = isExponentialNotation;
		this.commentStarter = commentStarter;
		this.simulationComments = simulationComments;
		this.fieldComments = fieldComments;
		this.eventComments = eventComments;
		this.isCAData = false;
	}

	// Constructor for CA data
	public SaveCSVWorker(File file, CAParameters parameters, CADataBranch branch,
						 CADomainDataType domainDataType, CADataType[] fields,
						 Map<CADataType, List<RocketComponent>> components, Unit[] units,
						 String fieldSeparator, int decimalPlaces, boolean isExponentialNotation,
						 String commentStarter, boolean analysisComments, boolean fieldDescriptions) {
		this.file = file;
		this.caParameters = parameters;
		this.caDataBranch = branch;
		this.caDomainDataType = domainDataType;
		this.caDataFields = fields;
		this.caComponents = components;
		this.caUnits = units;
		this.fieldSeparator = fieldSeparator;
		this.decimalPlaces = decimalPlaces;
		this.isExponentialNotation = isExponentialNotation;
		this.commentStarter = commentStarter;
		this.analysisComments = analysisComments;
		this.fieldDescriptions = fieldDescriptions;
		this.isCAData = true;
	}

	@Override
	protected Void doInBackground() throws Exception {
		int estimate = BYTES_PER_FIELD_PER_POINT * (isCAData ? caDataFields.length : flightDataFields.length) *
				(isCAData ? caDataBranch.getLength() : flightDataBranch.getLength());
		estimate = Math.max(estimate, 1000);

		try (ProgressOutputStream os = new ProgressOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)),
				estimate, this) {
			@Override
			protected void setProgress(int progress) {
				SaveCSVWorker.this.setProgress(progress);
			}
		}) {
			if (isCAData) {
				CSVExport.exportCSV(os, caParameters, caDataBranch, caDomainDataType, caDataFields, caComponents, caUnits,
						fieldSeparator, decimalPlaces, isExponentialNotation, analysisComments, fieldDescriptions, commentStarter);
			} else {
				CSVExport.exportCSV(os, simulation, flightDataBranch, flightDataFields, flightDataUnits, fieldSeparator,
						decimalPlaces, isExponentialNotation, commentStarter, simulationComments, fieldComments, eventComments);
			}
		} catch (Exception e) {
			Application.getExceptionHandler().handleErrorCondition("Error writing file", e);
		}
		return null;
	}

	public static boolean exportSimulationData(File file, Simulation simulation, FlightDataBranch branch,
								 FlightDataType[] fields, Unit[] units, String fieldSeparator,
								 String commentStarter, boolean simulationComments,
								 boolean fieldComments, boolean eventComments, Window parent) {
		return exportSimulationData(file, simulation, branch, fields, units, fieldSeparator, TextUtil.DEFAULT_DECIMAL_PLACES, true,
				commentStarter, simulationComments, fieldComments, eventComments, parent);
	}

	/**
	 * Exports a CSV file using a progress dialog if necessary.
	 *
	 * @return	<code>true</code> if the save was successful, <code>false</code> otherwise.
	 */
	public static boolean exportSimulationData(File file, Simulation simulation, FlightDataBranch branch,
								 FlightDataType[] fields, Unit[] units, String fieldSeparator, int decimalPlaces,
								 boolean isExponentialNotation, String commentStarter, boolean simulationComments,
								 boolean fieldComments, boolean eventComments, Window parent) {


		SaveCSVWorker worker = new SaveCSVWorker(file, simulation, branch, fields, units,
				fieldSeparator, decimalPlaces, isExponentialNotation, commentStarter, simulationComments,
				fieldComments, eventComments);

		if (!SwingWorkerDialog.runWorker(parent, "Exporting flight data",
				"Writing " + file.getName() + "...", worker)) {

			// User cancelled the save
			file.delete();
			return false;
		}

		try {
			worker.get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();

			if (cause instanceof IOException) {
				JOptionPane.showMessageDialog(parent, new String[] {
						"An I/O error occurred while saving:",
						e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				throw new BugException("Unknown error when saving file", e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		return true;
	}

	// New export method for CA data
	public static boolean exportCAData(File file, CAParameters parameters, CADataBranch branch,
									   CADomainDataType domainDataType, CADataType[] fields,
									   Map<CADataType, List<RocketComponent>> components, Unit[] units,
									   String fieldSeparator, int decimalPlaces, boolean isExponentialNotation,
									   String commentStarter, boolean analysisComments, boolean fieldDescriptions,
									   Window parent) {
		SaveCSVWorker worker = new SaveCSVWorker(file, parameters, branch, domainDataType, fields, components, units,
				fieldSeparator, decimalPlaces, isExponentialNotation, commentStarter, analysisComments, fieldDescriptions);

		if (!SwingWorkerDialog.runWorker(parent, "Exporting component analysis data",
				"Writing " + file.getName() + "...", worker)) {
			// User cancelled the save
			file.delete();
			return false;
		}

		try {
			worker.get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();

			if (cause instanceof IOException) {
				JOptionPane.showMessageDialog(parent, new String[] {
						"An I/O error occurred while saving:",
						e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				throw new BugException("Unknown error when saving file", e);
			}

		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}

		return true;
	}
}