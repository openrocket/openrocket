package net.sf.openrocket.gui.main;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.gui.components.CsvOptionPanel;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.widgets.SaveFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.Simulation.Status;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTable;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.adaptors.ColumnTableRowSorter;
import net.sf.openrocket.gui.adaptors.ValueColumn;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.simulation.SimulationEditDialog;
import net.sf.openrocket.gui.simulation.SimulationRunDialog;
import net.sf.openrocket.gui.simulation.SimulationWarningDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.widgets.IconButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.AlphanumComparator;
import net.sf.openrocket.file.SimulationTableCSVExport;
import net.sf.openrocket.utils.TableRowTraversalPolicy;

@SuppressWarnings("serial")
public class SimulationPanel extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(SimulationPanel.class);
	private static final Translator trans = Application.getTranslator();


	private static final Color WARNING_COLOR = Color.RED;
	private static final String WARNING_TEXT = "\uFF01"; // Fullwidth exclamation mark

	private static final Color OK_COLOR = new Color(60, 150, 0);
	private static final String OK_TEXT = "\u2714"; // Heavy check mark


	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);


	private final OpenRocketDocument document;

	private final ColumnTableModel simulationTableModel;
	private final JTable simulationTable;

	private final JButton editButton;
	private final JButton runButton;
	private final JButton deleteButton;
	private final JButton plotButton;
	private final JButton simTableExportButton;
	private final JPopupMenu pm;

	private final SimulationAction editSimulationAction;
	private final SimulationAction runSimulationAction;
	private final SimulationAction plotSimulationAction;
	private final SimulationAction duplicateSimulationAction;
	private final SimulationAction deleteSimulationAction;
	private final SimulationAction simTableExportAction;
	private final SimulationAction selectedSimsExportAction;

	private int[] previousSelection = null;
	private JMenuItem exportSimTableToCSVMenuItem;

	public SimulationPanel(OpenRocketDocument doc) {
		super(new MigLayout("fill", "[grow][][][][][][grow]"));

		this.document = doc;


		// Simulation actions
		SimulationAction newSimulationAction = new NewSimulationAction();
		editSimulationAction = new EditSimulationAction();
		runSimulationAction = new RunSimulationAction();
		plotSimulationAction = new PlotSimulationAction();
		duplicateSimulationAction = new DuplicateSimulationAction();
		deleteSimulationAction = new DeleteSimulationAction();
		simTableExportAction = new ExportSimulationTableAsCSVAction();
		selectedSimsExportAction = new ExportSelectedSimulationsAsCSVAction();

		////////  The simulation action buttons ////////

		//// New simulation button
		JButton newButton = new IconButton();
		RocketActions.tieActionToButton(newButton, newSimulationAction, trans.get("simpanel.but.newsimulation"));
		newButton.setToolTipText(trans.get("simpanel.but.ttip.newsimulation"));
		this.add(newButton, "skip 1, gapright para");

		//// Edit simulation button
		editButton = new IconButton();
		RocketActions.tieActionToButton(editButton, editSimulationAction, trans.get("simpanel.but.editsimulation"));
		editButton.setToolTipText(trans.get("simpanel.but.ttip.editsim"));
		this.add(editButton, "gapright para");

		//// Run simulations
		runButton = new IconButton();
		RocketActions.tieActionToButton(runButton, runSimulationAction, trans.get("simpanel.but.runsimulations"));
		runButton.setToolTipText(trans.get("simpanel.but.ttip.runsimu"));
		this.add(runButton, "gapright para");
		
		//// Delete simulations button
		deleteButton = new IconButton();
		RocketActions.tieActionToButton(deleteButton, deleteSimulationAction, trans.get("simpanel.but.deletesimulations"));
		deleteButton.setToolTipText(trans.get("simpanel.but.ttip.deletesim"));
		this.add(deleteButton, "gapright para");

		//// Plot / export button
		plotButton = new IconButton();
		RocketActions.tieActionToButton(plotButton, plotSimulationAction, trans.get("simpanel.but.plotexport"));
		this.add(plotButton, "wrap para");

		//// Run then Dump simulations
		simTableExportButton = new IconButton();
		RocketActions.tieActionToButton(simTableExportButton, simTableExportAction, trans.get("simpanel.but.runsimulations"));


		////////  The simulation table
		simulationTableModel = new SimulationTableModel();

		simulationTable = new ColumnTable(simulationTableModel) {
			private static final long serialVersionUID = -5799340181229735630L;
		};
		ColumnTableRowSorter simulationTableSorter = new ColumnTableRowSorter(simulationTableModel);
		simulationTable.setRowSorter(simulationTableSorter);
		simulationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		simulationTable.setDefaultRenderer(Object.class, new JLabelRenderer());
		simulationTableModel.setColumnWidths(simulationTable.getColumnModel());
		simulationTable.setFillsViewportHeight(true);

		// Context menu
		pm = new JPopupMenu();
		pm.add(editSimulationAction);
		pm.add(duplicateSimulationAction);
		pm.add(deleteSimulationAction);
		pm.addSeparator();
		pm.add(runSimulationAction);
		pm.add(plotSimulationAction);
		pm.add(selectedSimsExportAction);

		// The normal left/right and tab/shift-tab key action traverses each cell/column of the table instead of going to the next row.
		TableRowTraversalPolicy.setTableRowTraversalPolicy(simulationTable);

		// Mouse listener to act on double-clicks
		simulationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = simulationTable.getSelectedRow();
				int row = simulationTable.rowAtPoint(e.getPoint());
				int column = simulationTable.columnAtPoint(e.getPoint());

				// Clear the table selection when clicked outside the table rows.
				if (row == -1 || column == -1 || selectedRow == -1) {
					simulationTable.clearSelection();
					return;
				}

				if (e.getButton() == MouseEvent.BUTTON1) {
					// Edit the simulation or plot/export
					if (e.getClickCount() == 2) {
						int selected = simulationTable.convertRowIndexToModel(selectedRow);
						if (column == 0) {
							SimulationWarningDialog.showWarningDialog(SimulationPanel.this, document.getSimulations().get(selected));
						} else {
							simulationTable.clearSelection();
							simulationTable.addRowSelectionInterval(selectedRow, selectedRow);

							openDialog(document.getSimulations().get(selected));
						}
					}
				}
				// Show context menu
				else if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					// Select new row
					if (!simulationTable.isRowSelected(row)) {
						if (row >= 0 && row < simulationTable.getRowCount()) {
							simulationTable.setRowSelectionInterval(row, row);
						} else {
							return;
						}
					}

                    doPopup(e);
				}
			}
		});

		simulationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			private int previousSelectedRow = -1;
			private int previousSelectedRowCount = 0;
			public void valueChanged(ListSelectionEvent event) {
				if ((simulationTable.getSelectedRow() != previousSelectedRow) ||
						(simulationTable.getSelectedRowCount() != previousSelectedRowCount)) {
					updateButtonStates();
					previousSelectedRow = simulationTable.getSelectedRow();
					previousSelectedRowCount = simulationTable.getSelectedRowCount();
				}
			}
		});

		document.addDocumentChangeListener(new DocumentChangeListener() {
			@Override
			public void documentChanged(DocumentChangeEvent event) {
				if (!(event instanceof SimulationChangeEvent))
					return;
				fireMaintainSelection();
			}
		});




		// Fire table change event when the rocket changes
		document.getRocket().addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				fireMaintainSelection();
			}
		});


		JScrollPane scrollpane = new JScrollPane(simulationTable);
		this.add(scrollpane, "spanx, grow, wrap rel");

		updateButtonStates();
	}

	public void updatePreviousSelection() {
		this.previousSelection = simulationTable.getSelectedRows();
	}

	private void newSimulation() {
		Simulation sim = new Simulation(document, document.getRocket());
		sim.setName(document.getNextSimulationName());

		int n = document.getSimulationCount();
		document.addSimulation(sim);
		simulationTableModel.fireTableDataChanged();
		simulationTable.clearSelection();
		simulationTable.addRowSelectionInterval(n, n);
		updatePreviousSelection();

		openDialog(false, true, sim);
	}

	private void plotSimulation() {
		int selected = simulationTable.getSelectedRow();
		if (selected < 0) {
			return;
		}
		selected = simulationTable.convertRowIndexToModel(selected);
		simulationTable.clearSelection();
		simulationTable.addRowSelectionInterval(selected, selected);


		Simulation sim = document.getSimulations().get(selected);

		if (!sim.hasSimulationData()) {
			new SimulationRunDialog(SwingUtilities.getWindowAncestor(
					SimulationPanel.this), document, sim).setVisible(true);
		}

		fireMaintainSelection();
		takeTheSpotlight();

		openDialog(true, sim);
	}

	private void deleteSimulation() {
		int[] selection = simulationTable.getSelectedRows();
		if (selection.length == 0) {
			return;
		}
		// Verify deletion
		boolean verify = Application.getPreferences().getBoolean(Preferences.CONFIRM_DELETE_SIMULATION, true);
		if (verify) {

			JPanel panel = new JPanel(new MigLayout());
			//// Do not ask me again
			JCheckBox dontAsk = new JCheckBox(trans.get("simpanel.checkbox.donotask"));
			panel.add(dontAsk, "wrap");
			//// You can change the default operation in the preferences.
			panel.add(new StyledLabel(trans.get("simpanel.lbl.defpref"), -2));

			int ret = JOptionPane.showConfirmDialog(SimulationPanel.this,
					new Object[] {
					//// Delete the selected simulations?
					trans.get("simpanel.dlg.lbl.DeleteSim1"),
					//// <html><i>This operation cannot be undone.</i>
					trans.get("simpanel.dlg.lbl.DeleteSim2"),
					"",
					panel },
					//// Delete simulations
					trans.get("simpanel.dlg.lbl.DeleteSim3"),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (ret != JOptionPane.OK_OPTION)
				return;

			if (dontAsk.isSelected()) {
				Application.getPreferences().putBoolean(Preferences.CONFIRM_DELETE_SIMULATION, false);
			}
		}

		// Delete simulations
		for (int i = 0; i < selection.length; i++) {
			selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
		}
		Arrays.sort(selection);
		for (int i = selection.length - 1; i >= 0; i--) {
			document.removeSimulation(selection[i]);
		}
		simulationTableModel.fireTableDataChanged();
		updatePreviousSelection();
		takeTheSpotlight();
	}

	private void runSimulation() {
		Simulation[] sims = getSelectedSimulations();
		if (sims == null) return;

		long t = System.currentTimeMillis();
		new SimulationRunDialog(SwingUtilities.getWindowAncestor(
				SimulationPanel.this), document, sims).setVisible(true);
		log.info("Running simulations took " + (System.currentTimeMillis() - t) + " ms");
		fireMaintainSelection();
		takeTheSpotlight();
	}

	public void editSimulation() {
		Simulation[] sims = getSelectedSimulations();
		if (sims == null) return;

		openDialog(false, sims);
	}

	private void exportSimulationsToCSV(boolean onlySelected) {
		Container tableParent = simulationTable.getParent();
		int rowCount = simulationTableModel.getRowCount();

		// I'm pretty sure with the enablement/disablement of the menu item under the File dropdown,
		// that this would no longer be needed because if there is no sim table yet, the context menu
		// won't show up.   But I'm going to leave this in just in case....
		if (rowCount <= 0) {
			log.info("No simulation table rows to export");
			JOptionPane.showMessageDialog(tableParent, trans.get("simpanel.dlg.no.simulation.table.rows"));
			return;
		}

		JFileChooser chooser = setUpSimExportCSVFileChooser();
		int selectionStatus = chooser.showSaveDialog(tableParent);
		if (selectionStatus != JFileChooser.APPROVE_OPTION) {
			log.debug("User cancelled CSV export");
			return;
		}

		// Fetch the info from the file chooser
		File CSVFile = chooser.getSelectedFile();
		CSVFile = FileHelper.forceExtension(CSVFile, "csv");
		if (!FileHelper.confirmWrite(CSVFile, SimulationPanel.this)) {
			log.debug("User cancelled CSV export overwrite");
			return;
		}

		String separator = ((CsvOptionPanel) chooser.getAccessory()).getFieldSeparator();
		int precision = ((CsvOptionPanel) chooser.getAccessory()).getDecimalPlaces();
		boolean isExponentialNotation = ((CsvOptionPanel) chooser.getAccessory()).isExponentialNotation();
		((CsvOptionPanel) chooser.getAccessory()).storePreferences();

		// Handle some special separator options from CsvOptionPanel
		if (separator.equals(trans.get("CsvOptionPanel.separator.space"))) {
			separator = " ";
		} else if (separator.equals(trans.get("CsvOptionPanel.separator.tab"))) {
			separator = "\t";
		}

		SimulationTableCSVExport exporter = new SimulationTableCSVExport(document, simulationTable, simulationTableModel);
		exporter.export(CSVFile, separator, precision, isExponentialNotation, onlySelected);
	}

	/**
	 * Create the file chooser to save the CSV file.
	 * @return The file chooser.
	 */
	private JFileChooser setUpSimExportCSVFileChooser() {
		JFileChooser chooser = new SaveFileChooser();
		chooser.setDialogTitle(trans.get("simpanel.pop.exportToCSV.save.dialog.title"));
		chooser.setFileFilter(FileHelper.CSV_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setAcceptAllFileFilterUsed(false);

		// Default output CSV to same name as the document's rocket name.
		String fileName = document.getRocket().getName() + ".csv";
		chooser.setSelectedFile(new File(fileName));

		// Add CSV options to FileChooser
		CsvOptionPanel CSVOptions = new CsvOptionPanel(SimulationTableCSVExport.class);
		chooser.setAccessory(CSVOptions);

		// TODO: update this dynamically instead of hard-coded values
		// The macOS file chooser has an issue where it does not update its size when the accessory is added.
		if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
			Dimension currentSize = chooser.getPreferredSize();
			Dimension newSize = new Dimension((int) (1.5 * currentSize.width), (int) (1.3 * currentSize.height));
			chooser.setPreferredSize(newSize);
		}

		return chooser;
	}

	private Simulation[] getSelectedSimulations() {
		int[] selection = simulationTable.getSelectedRows();
		if (selection.length == 0) {
			return null;
		}

		Simulation[] sims = new Simulation[selection.length];
		for (int i = 0; i < selection.length; i++) {
			selection[i] = simulationTable.convertRowIndexToModel(selection[i]);
			sims[i] = document.getSimulation(selection[i]);
		}
		return sims;
	}


	private void copySimulationAction() {
		int numCols=simulationTable.getColumnCount();
		int numRows=simulationTable.getSelectedRowCount();
		int[] rowsSelected=simulationTable.getSelectedRows();

		if (numRows!=rowsSelected[rowsSelected.length-1]-rowsSelected[0]+1 || numRows!=rowsSelected.length) {

			JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuilder excelStr =new StringBuilder();
		for (int k = 1; k < numCols; k++) {
			excelStr.append(simulationTable.getColumnName(k));
			if (k < numCols-1) {
				excelStr.append("\t");
			}
		}
		excelStr.append("\n");
		for (int i = 0; i < numRows; i++) {
			for (int j = 1; j < numCols; j++) {
				excelStr.append(simulationTable.getValueAt(rowsSelected[i], j));
				if (j < numCols-1) {
					excelStr.append("\t");
				}
			}
			excelStr.append("\n");
		}

		StringSelection sel = new StringSelection(excelStr.toString());

		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(sel, sel);
	}

	private void duplicateSimulation() {
		Simulation[] sims = getSelectedSimulations();
		if (sims == null) return;

		for (Simulation s: sims) {
			Simulation copy = s.duplicateSimulation(document.getRocket());
			String name = copy.getName();
			if (name.matches(OpenRocketDocument.SIMULATION_NAME_PREFIX + "[0-9]+ *")) {
				copy.setName(document.getNextSimulationName());
			}
			document.addSimulation(copy);
		}
		simulationTable.getSelectionModel().setSelectionInterval(simulationTable.getRowCount()-sims.length,simulationTable.getRowCount()-1);

	}

	/**
	 * Return the action for exporting the simulation table data to a CSV file.
	 * @return the action for exporting the simulation table data to a CSV file.
	 */
	public AbstractAction getExportSimulationTableAsCSVAction() {
		return simTableExportAction;
	}

	protected void doPopup(MouseEvent e) {
        pm.show(e.getComponent(), e.getX(), e.getY());
    }
	
	private void updateButtonStates() {
		editSimulationAction.updateEnabledState();
		deleteSimulationAction.updateEnabledState();
		runSimulationAction.updateEnabledState();
		plotSimulationAction.updateEnabledState();
		duplicateSimulationAction.updateEnabledState();
		simTableExportAction.updateEnabledState();
		selectedSimsExportAction.updateEnabledState();
	}

	/// when the simulation tab is selected this run outdated simulated if appropriate.
	public void activating(){
		if( ((Preferences) Application.getPreferences()).getAutoRunSimulations()){
			int nSims = simulationTable.getRowCount();
			int outdated = 0;
			if (nSims == 0) {
				return;
			}
			
			for (int i = 0; i < nSims; i++) {
				Simulation.Status s = document.getSimulation(simulationTable.convertRowIndexToModel(i)).getStatus();
				if((s==Simulation.Status.NOT_SIMULATED) ||
						(s==Simulation.Status.OUTDATED)){
					outdated++;
				}
			}
			if(outdated>0){
				Simulation[] sims = new Simulation[outdated];
				
				int index=0;
				for (int i = 0; i < nSims; i++) {
					int t = simulationTable.convertRowIndexToModel(i);
					Simulation s = document.getSimulation(t);
					if((s.getStatus()==Status.NOT_SIMULATED)||(s.getStatus()==Status.OUTDATED)){
						sims[index] = s;
						index++;
					}
				}
	
				long t = System.currentTimeMillis();
				new SimulationRunDialog(SwingUtilities.getWindowAncestor(
						SimulationPanel.this), document, sims).setVisible(true);
				log.info("Running simulations took " + (System.currentTimeMillis() - t) + " ms");
				fireMaintainSelection();
			}
		}
	}

	public ListSelectionModel getSimulationListSelectionModel() {
		return simulationTable.getSelectionModel();
	}

	private void openDialog(boolean plotMode, boolean isNewSimulation, final Simulation... sims) {
		SimulationEditDialog d = new SimulationEditDialog(SwingUtilities.getWindowAncestor(this), document, isNewSimulation, sims);
		if (plotMode) {
			d.setPlotMode();
		}
		d.setVisible(true);
		fireMaintainSelection();
		takeTheSpotlight();
	}

	private void openDialog(boolean plotMode, final Simulation... sims) {
		openDialog(plotMode, false, sims);
	}

	private void openDialog(final Simulation sim) {
		boolean plotMode = false;
		if (sim.hasSimulationData() && Simulation.isStatusUpToDate(sim.getStatus())) {
			plotMode = true;
		}
		openDialog(plotMode, sim);
	}

	private void fireMaintainSelection() {
		int[] selection = simulationTable.getSelectedRows();
		simulationTableModel.fireTableDataChanged();
		for (int row : selection) {
			if (row >= simulationTableModel.getRowCount())
				break;
			simulationTable.addRowSelectionInterval(row, row);
		}
	}

	private abstract static class SimulationAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public abstract void updateEnabledState();
	}

	class NewSimulationAction extends SimulationAction {
		public NewSimulationAction() {
			putValue(NAME, trans.get("simpanel.but.newsimulation"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_N);
			this.putValue(SMALL_ICON, Icons.FILE_NEW);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			newSimulation();
		}

		@Override
		public void updateEnabledState() {
			setEnabled(true);
		}
	}

	class EditSimulationAction extends SimulationAction {
		public EditSimulationAction() {
			putValue(NAME, trans.get("simpanel.pop.edit"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
			this.putValue(ACCELERATOR_KEY, RocketActions.EDIT_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_EDIT);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			editSimulation();
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class RunSimulationAction extends SimulationAction {
		public RunSimulationAction() {
			putValue(NAME, trans.get("simpanel.pop.run"));
			putValue(SMALL_ICON, Icons.SIM_RUN);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runSimulation();
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class DeleteSimulationAction extends SimulationAction {
		public DeleteSimulationAction() {
			putValue(NAME, trans.get("simpanel.pop.delete"));
			putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			putValue(SMALL_ICON, Icons.EDIT_DELETE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			deleteSimulation();
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	/**
	 * Export the entire simulation table as a CSV file.
	 */
	class ExportSimulationTableAsCSVAction extends SimulationAction {

		public ExportSimulationTableAsCSVAction() {
			putValue(NAME, trans.get("simpanel.pop.exportSimTableToCSV"));
			putValue(SMALL_ICON, Icons.SIM_TABLE_EXPORT);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			exportSimulationsToCSV(false);
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTableModel != null && simulationTableModel.getRowCount() > 0);
		}
		
	}

	/**
	 * Export only the selected simulations as a CSV file.
	 */
	class ExportSelectedSimulationsAsCSVAction extends SimulationAction {

		public ExportSelectedSimulationsAsCSVAction() {
			putValue(NAME, trans.get("simpanel.pop.exportSelectedSimsToCSV"));
			putValue(SMALL_ICON, Icons.SIM_TABLE_EXPORT);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			exportSimulationsToCSV(true);
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTableModel != null && simulationTableModel.getRowCount() > 0 &&
					simulationTable.getSelectedRowCount() > 0);
		}

	}

	class PlotSimulationAction extends SimulationAction {
		public PlotSimulationAction() {
			putValue(NAME, trans.get("simpanel.pop.plot"));
			putValue(SMALL_ICON, Icons.SIM_PLOT);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			plotSimulation();
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() == 1);
		}
	}

	class DuplicateSimulationAction extends SimulationAction {
        public DuplicateSimulationAction() {
            putValue(NAME, trans.get("simpanel.pop.duplicate"));
			putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			putValue(ACCELERATOR_KEY, RocketActions.DUPLICATE_KEY_STROKE);
			putValue(SMALL_ICON, Icons.EDIT_DUPLICATE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
			duplicateSimulation();
        }

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}
	
	public static class CellTransferable implements Transferable {

        public static final DataFlavor CELL_DATA_FLAVOR = new DataFlavor(Object.class, "application/x-cell-value");

        private Object cellValue;

        public CellTransferable(Object cellValue) {
            this.cellValue = cellValue;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{CELL_DATA_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return CELL_DATA_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return cellValue;
        }

    }
	
	private class JLabelRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (row < 0 || row >= document.getSimulationCount())
				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);

			row = table.getRowSorter().convertRowIndexToModel(row);

			// A JLabel is self-contained and has set its own tool tip
			if (value instanceof JLabel) {
				JLabel label = (JLabel) value;
				if (isSelected)
					label.setBackground(table.getSelectionBackground());
				else
					label.setBackground(table.getBackground());
				label.setOpaque(true);

				label.setToolTipText(getSimulationToolTip(document.getSimulation(row)));
				return label;
			}

			Component component = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			if (component instanceof JComponent) {
				((JComponent) component).setToolTipText(getSimulationToolTip(
						document.getSimulation(row)));
			}
			return component;
		}

		private String getSimulationToolTip(Simulation sim) {
			String tip;
			FlightData data = sim.getSimulatedData();

			tip = "<html><b>" + sim.getName() + "</b><br>";
			switch (sim.getStatus()) {
			case CANT_RUN:
				tip += trans.get("simpanel.ttip.noData")+"<br>";
				break;
			case LOADED:
				tip += trans.get("simpanel.ttip.loaded") + "<br>";
				break;
			case UPTODATE:
				tip += trans.get("simpanel.ttip.uptodate") + "<br>";
				break;

			case OUTDATED:
				tip += trans.get("simpanel.ttip.outdated") + "<br>";
				break;

			case EXTERNAL:
				tip += trans.get("simpanel.ttip.external") + "<br>";
				return tip;

			case NOT_SIMULATED:
				tip += trans.get("simpanel.ttip.notSimulated");
				return tip;
			}

			if (data == null) {
				tip += trans.get("simpanel.ttip.noData");
				return tip;
			}
			WarningSet warnings = data.getWarningSet();

			if (warnings.isEmpty()) {
				tip += trans.get("simpanel.ttip.noWarnings");
				return tip;
			}

			tip += trans.get("simpanel.ttip.warnings");
			for (Warning w : warnings) {
				tip += "<br>" + w.toString();
			}

			return tip;
		}
	}

	private class SimulationTableModel extends ColumnTableModel {
		private static final long serialVersionUID = 8686456963492628476L;

		public SimulationTableModel() {
			super(
					////  Status and warning column
					new Column("") {
						private JLabel label = null;

						@Override
						public Object getValueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							// Initialize the label
							if (label == null) {
								label = new StyledLabel(2f);
								label.setIconTextGap(1);
								//							label.setFont(label.getFont().deriveFont(Font.BOLD));
							}

							// Set simulation status icon
							Simulation.Status status = document.getSimulation(row).getStatus();
							label.setIcon(Icons.SIMULATION_STATUS_ICON_MAP.get(status));


							// Set warning marker
							if (status == Simulation.Status.NOT_SIMULATED ||
									status == Simulation.Status.EXTERNAL) {

								label.setText("");

							} else {

								WarningSet w = document.getSimulation(row).getSimulatedWarnings();
								if (w == null) {
									label.setText("");
								} else if (w.isEmpty()) {
									label.setForeground(OK_COLOR);
									label.setText(OK_TEXT);
								} else {
									label.setForeground(WARNING_COLOR);
									label.setText(WARNING_TEXT);
								}
							}

							return label;
						}

						@Override
						public int getExactWidth() {
							return 36;
						}

						@Override
						public Class<?> getColumnClass() {
							return JLabel.class;
						}
					},

					//// Simulation name
					//// Name
					new Column(trans.get("simpanel.col.Name")) {
						@Override
						public Object getValueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;
							return document.getSimulation(row).getName();
						}

						@Override
						public int getDefaultWidth() {
							return 125;
						}

						@Override
						public Comparator<String> getComparator() {
							return new AlphanumComparator();
						}
					},

					//// Simulation configuration
					new Column(trans.get("simpanel.col.Configuration")) {
						@Override
						public Object getValueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount()) {
								return null;
							}

							Rocket rkt = document.getRocket();
							FlightConfigurationId fcid = document.getSimulation(row).getId();
							return descriptor.format(rkt, fcid);
						}

						@Override
						public int getDefaultWidth() {
							return 125;
						}
					},

					//// Launch rod velocity
					new ValueColumn(trans.get("simpanel.col.Velocityoffrod"), UnitGroup.UNITS_VELOCITY) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getLaunchRodVelocity();
						}

					},

					//// Apogee
					new ValueColumn(trans.get("simpanel.col.Apogee"), UnitGroup.UNITS_DISTANCE) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getMaxAltitude();
						}
					},

					//// Velocity at deployment
					new ValueColumn(trans.get("simpanel.col.Velocityatdeploy"), UnitGroup.UNITS_VELOCITY) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getDeploymentVelocity();
						}
					},

					//// Deployment Time from Apogee
					new ValueColumn(trans.get("simpanel.col.OptimumCoastTime"),
							trans.get("simpanel.col.OptimumCoastTime.ttip"), UnitGroup.UNITS_SHORT_TIME) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null || data.getBranchCount() == 0)
								return null;

							double val = data.getBranch(0).getOptimumDelay();
							if (Double.isNaN(val)) {
								return null;
							}
							return val;
						}
					},

					//// Maximum velocity
					new ValueColumn(trans.get("simpanel.col.Maxvelocity"), UnitGroup.UNITS_VELOCITY) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getMaxVelocity();
						}
					},

					//// Maximum acceleration
					new ValueColumn(trans.get("simpanel.col.Maxacceleration"), UnitGroup.UNITS_ACCELERATION) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getMaxAcceleration();
						}
					},

					//// Time to apogee
					new ValueColumn(trans.get("simpanel.col.Timetoapogee"), UnitGroup.UNITS_FLIGHT_TIME) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getTimeToApogee();
						}
					},

					//// Flight time
					new ValueColumn(trans.get("simpanel.col.Flighttime"), UnitGroup.UNITS_FLIGHT_TIME) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getFlightTime();
						}
					},

					//// Ground hit velocity
					new ValueColumn(trans.get("simpanel.col.Groundhitvelocity"), UnitGroup.UNITS_VELOCITY) {
						@Override
						public Double valueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							FlightData data = document.getSimulation(row).getSimulatedData();
							if (data == null)
								return null;

							return data.getGroundHitVelocity();
						}
					}
			);
		}

		@Override
		public int getRowCount() {
			return document.getSimulationCount();
		}
	}

	/**
	 * Focus on the simulation table and maintain the previous row selection(s).
	 */
	public void takeTheSpotlight() {
		simulationTable.requestFocusInWindow();
		if (simulationTable.getRowCount() == 0 || simulationTable.getSelectedRows().length > 0) {
			return;
		}
		if (previousSelection == null || previousSelection.length == 0) {
			simulationTable.setRowSelectionInterval(0, 0);
		} else {
			simulationTable.clearSelection();
			for (int row : previousSelection) {
				if (row < 0 || row >= simulationTable.getRowCount()) {
					continue;
				}
				simulationTable.addRowSelectionInterval(row, row);
			}
		}
	}
}
