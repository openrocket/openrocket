package info.openrocket.swing.gui.simulation;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;

import info.openrocket.swing.gui.widgets.CSVExportPanel;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.plot.Util;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SaveCSVWorker;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

public class SimulationExportPanel extends CSVExportPanel<FlightDataType> {
	
	private static final long serialVersionUID = 3423905472892675964L;
	private static final Translator trans = Application.getTranslator();
	
	private static final int OPTION_SIMULATION_COMMENTS = 0;
	private static final int OPTION_FIELD_DESCRIPTIONS = 1;
	private static final int OPTION_FLIGHT_EVENTS = 2;

	private final Simulation simulation;
	private FlightDataBranch branch;
	
	
	private SimulationExportPanel(Simulation simulation, FlightDataBranch branch, FlightDataType[] types,
								  boolean[] selected, CsvOptionPanel csvOptions, Component... extraComponents) {
		super(types, selected, csvOptions, false, extraComponents);
		this.simulation = simulation;
		this.branch = branch;
	}

	public static SimulationExportPanel create(Simulation simulation) {
		final FlightData data = simulation.getSimulatedData();

		// Check that data exists
		if (data == null || data.getBranchCount() == 0 ||
				data.getBranch(0).getTypes().length == 0) {
			throw new IllegalArgumentException("No data for panel");
		}

		// Create the data model
		FlightDataBranch branch = data.getBranch(0);
		FlightDataType[] types = branch.getTypes();

		// Get the selected types from the preferences
		final boolean[] selected = new boolean[types.length];
		for (int i = 0; i < types.length; i++) {
			selected[i] = ((SwingPreferences) Application.getPreferences()).isFlightDataTypeExportSelected(types[i]);
		}

		CsvOptionPanel csvOptions = new CsvOptionPanel(SimulationExportPanel.class,
				trans.get("SimExpPan.checkbox.Includesimudesc"),
				trans.get("SimExpPan.checkbox.ttip.Includesimudesc"),
				trans.get("SimExpPan.checkbox.Includefielddesc"),
				trans.get("SimExpPan.checkbox.ttip.Includefielddesc"),
				trans.get("SimExpPan.checkbox.Incflightevents"),
				trans.get("SimExpPan.checkbox.ttip.Incflightevents"));

		//// Add series selection box
		ArrayList<String> stages = new ArrayList<>(Util.generateSeriesLabels(simulation.getSimulatedData().getBranches()));
		if (stages.size() > 1) {
			final JComboBox<String> stageSelection = new JComboBox<>(stages.toArray(new String[0]));

			// Only show the combo box if there are at least 2 entries (ie, "Main", and one other one
			JPanel stagePanel = new JPanel(new MigLayout("fill"));
			stagePanel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Stage")));
			stagePanel.add(stageSelection, "growx");

			SimulationExportPanel panel = new SimulationExportPanel(simulation, branch, types, selected, csvOptions, stagePanel);

			stageSelection.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					int selectedStage = stageSelection.getSelectedIndex();
					panel.branch = data.getBranch(selectedStage);
				}

			});
			return panel;
		}

		return new SimulationExportPanel(simulation, branch, types, selected, csvOptions);
	}

	@Override
	public boolean doExport() {
		JFileChooser chooser = new SaveFileChooser();
		chooser.setFileFilter(FileHelper.CSV_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;
		
		File file = chooser.getSelectedFile();
		if (file == null)
			return false;
		
		file = FileHelper.forceExtension(file, "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}
		
		
		String commentChar = csvOptions.getCommentCharacter();
		String fieldSep = csvOptions.getFieldSeparator();
		int decimalPlaces = csvOptions.getDecimalPlaces();
		boolean isExponentialNotation = csvOptions.isExponentialNotation();
		boolean simulationComment = csvOptions.getSelectionOption(OPTION_SIMULATION_COMMENTS);
		boolean fieldComment = csvOptions.getSelectionOption(OPTION_FIELD_DESCRIPTIONS);
		boolean eventComment = csvOptions.getSelectionOption(OPTION_FLIGHT_EVENTS);
		csvOptions.storePreferences();
		
		// Store preferences and export
		int n = 0;
		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		for (int i = 0; i < selected.length; i++) {
			((SwingPreferences) Application.getPreferences()).setFlightDataTypeExportSelected(types[i], selected[i]);
			if (selected[i])
				n++;
		}
		
		
		FlightDataType[] fieldTypes = new FlightDataType[n];
		Unit[] fieldUnits = new Unit[n];
		int pos = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				fieldTypes[pos] = types[i];
				fieldUnits[pos] = units[i];
				pos++;
			}
		}
		
		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}


		SaveCSVWorker.exportSimulationData(file, simulation, branch, fieldTypes, fieldUnits, fieldSep, decimalPlaces,
				isExponentialNotation, commentChar, simulationComment, fieldComment, eventComment,
				SwingUtilities.getWindowAncestor(this));
		
		return true;
	}

	@Override
	protected String getDisplayName(FlightDataType type) {
		return type.getName();
	}
}
