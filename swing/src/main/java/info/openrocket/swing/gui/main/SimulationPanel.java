package info.openrocket.swing.gui.main;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
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
import java.io.Serial;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.logging.Message;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.Simulation.Status;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.core.document.events.DocumentChangeListener;
import info.openrocket.core.document.events.SimulationChangeEvent;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.AlphanumComparator;

import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.simulation.SimulationConfigDialog;
import info.openrocket.swing.gui.util.ColorConversion;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.adaptors.Column;
import info.openrocket.swing.gui.adaptors.ColumnTable;
import info.openrocket.swing.gui.adaptors.ColumnTableModel;
import info.openrocket.swing.gui.adaptors.ColumnTableRowSorter;
import info.openrocket.swing.gui.adaptors.ValueColumn;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.simulation.SimulationRunDialog;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.widgets.IconButton;
import info.openrocket.swing.file.SimulationTableCSVExport;
import info.openrocket.swing.utils.TableRowTraversalPolicy;

import static info.openrocket.swing.gui.main.BasicFrame.SHORTCUT_KEY;

@SuppressWarnings("serial")
public class SimulationPanel extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(SimulationPanel.class);
	private static final Translator trans = Application.getTranslator();


	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);


	private final Window parent;
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
	private final SimulationAction cutSimulationAction;
	private final SimulationAction copySimulationAction;
	private final SimulationAction pasteSimulationAction;
	private final SimulationAction runSimulationAction;
	private final SimulationAction plotSimulationAction;
	private final SimulationAction duplicateSimulationAction;
	private final SimulationAction deleteSimulationAction;
	private final SimulationAction simTableExportAction;
	private final SimulationAction selectedSimsExportAction;

	private int[] previousSelection = null;


	private static Color dimTextColor;
	private static Color warningColor;
	private static Color errorColor;
	private static Color informationColor;

	static {
		initColors();
	}

	public SimulationPanel(Window parent, OpenRocketDocument doc) {
		super(new MigLayout("fill", "[grow][][][][][][grow]"));

		this.parent = parent;
		this.document = doc;


		// Simulation actions
		SimulationAction newSimulationAction = new NewSimulationAction();
		editSimulationAction = new EditSimulationAction();
		cutSimulationAction = new CutSimulationAction();
		copySimulationAction = new CopySimulationAction();
		pasteSimulationAction = new PasteSimulationAction();
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
			@Serial
			private static final long serialVersionUID = -5799340181229735630L;
		};
		ColumnTableRowSorter simulationTableSorter = new ColumnTableRowSorter(simulationTableModel);
		simulationTable.setRowSorter(simulationTableSorter);
		simulationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		simulationTable.setDefaultRenderer(Object.class, new JLabelRenderer());
		simulationTable.setDefaultRenderer(WarningsBox.class, new WarningsBoxRenderer());
		simulationTableModel.setColumnWidths(simulationTable.getColumnModel());
		simulationTable.setFillsViewportHeight(true);

		// Unregister the default actions that would otherwise conflict with RocketActions and their acceleration keys
		InputMap im = simulationTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, SHORTCUT_KEY), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, SHORTCUT_KEY), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, SHORTCUT_KEY), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, SHORTCUT_KEY), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, SHORTCUT_KEY), "none");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, SHORTCUT_KEY), "none");

		// Context menu
		pm = new JPopupMenu();
		pm.add(editSimulationAction);
		pm.add(cutSimulationAction);
		pm.add(copySimulationAction);
		pm.add(pasteSimulationAction);
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
					if (e.getClickCount() == 1) {
						// Rerun the simulation
						if (column == 0) {
							int selected = simulationTable.convertRowIndexToModel(selectedRow);
							Simulation sim = document.getSimulations().get(selected);
							Status status = sim.getStatus();

							if (status == Status.NOT_SIMULATED || status == Status.OUTDATED) {
								runSimulation();
							}
						}
					} else if (e.getClickCount() == 2) {
						int selected = simulationTable.convertRowIndexToModel(selectedRow);
						// Show the warnings for the simulation
						if (column == 1) {
							SimulationConfigDialog dialog = new SimulationConfigDialog(parent, document, false,
									document.getSimulations().get(selected));
							dialog.switchToWarningsTab();
							dialog.setVisible(true);
						}
						// Edit the simulation or plot/export
						else if (column > 1) {
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
					updateActions();
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

		updateActions();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationPanel::updateColors);
	}

	private static void updateColors() {
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
		warningColor = GUIUtil.getUITheme().getWarningColor();
		errorColor = GUIUtil.getUITheme().getErrorColor();
		informationColor = GUIUtil.getUITheme().getInformationColor();
	}

	/**
	 * Returns the action used for editing selected simulations.
	 * @return the action used for editing selected simulations.
	 */
	public SimulationAction getEditSimulationAction() {
		return editSimulationAction;
	}

	/**
	 * Returns the action used for cutting selected simulations.
	 * @return the action used for cutting selected simulations.
	 */
	public SimulationAction getCutSimulationAction() {
		return cutSimulationAction;
	}

	/**
	 * Returns the action used for copying selected simulations.
	 * @return the action used for copying selected simulations.
	 */
	public SimulationAction getCopySimulationAction() {
		return copySimulationAction;
	}

	/**
	 * Returns the action used for pasting simulations.
	 * @return the action used for pasting simulations.
	 */
	public SimulationAction getPasteSimulationAction() {
		return pasteSimulationAction;
	}

	/**
	 * Returns the action used for duplicating selected simulations.
	 * @return the action used for duplicating selected simulations.
	 */
	public SimulationAction getDuplicateSimulationAction() {
		return duplicateSimulationAction;
	}

	/**
	 * Returns the action used for deleting selected simulations.
	 * @return the action used for deleting selected simulations.
	 */
	public SimulationAction getDeleteSimulationAction() {
		return deleteSimulationAction;
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

	private void deleteSimulations(Simulation[] sims) {
		if (sims == null || sims.length == 0) {
			return;
		}
		// Verify deletion
		if (!verifyDeleteSimulation()) {
			return;
		}

		// Delete simulations
		for (Simulation sim : sims) {
			document.removeSimulation(sim);
		}

		simulationTableModel.fireTableDataChanged();
		updatePreviousSelection();
		takeTheSpotlight();
	}

	private void deleteSimulations() {
		deleteSimulations(getSelectedSimulations());
	}

	private boolean verifyDeleteSimulation() {
		boolean verify = Application.getPreferences().getConfirmSimDeletion();
		if (!verify) {
			return true;
		}

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

		if (dontAsk.isSelected()) {
			Application.getPreferences().setConfirmSimDeletion(false);
		}

		return ret == JOptionPane.OK_OPTION;
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

		CsvOptionPanel csvOptions = (CsvOptionPanel) chooser.getAccessory();
		String separator = csvOptions.getFieldSeparator();
		int precision = csvOptions.getDecimalPlaces();
		boolean isExponentialNotation = csvOptions.isExponentialNotation();
		csvOptions.storePreferences();

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
		if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS && UITheme.isLightTheme(GUIUtil.getUITheme())) {
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

	/**
	 * Full simulation copying
	 */
	public void copySimulationsAction() {
		Simulation[] sims = getSelectedSimulations();

		if (sims == null || sims.length == 0)
			return;

		Simulation[] simsCopy = new Simulation[sims.length];
		for (int i=0; i < sims.length; i++) {
			simsCopy[i] = sims[i].copy();
		}

		OpenRocketClipboard.setClipboard(simsCopy);
		copySimulationValues();
	}

	/**
	 * Only copy the simulation table values to the clipboard. (not actual Simulation copying)
	 */
	public void copySimulationValues() {
		int numCols = simulationTable.getColumnCount();
		int numRows = simulationTable.getSelectedRowCount();
		int[] rowsSelected = simulationTable.getSelectedRows();

		if (numRows != rowsSelected.length) {
			JOptionPane.showMessageDialog(this, trans.get("simpanel.msg.invalidCopySelection"),
					trans.get("simpanel.msg.invalidCopySelection"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuilder valuesStr = new StringBuilder();

		// Copy the column names
		valuesStr.append(trans.get("simpanel.col.Status")).append("\t");
		for (int i = 1; i < numCols; i++) {
			valuesStr.append(simulationTable.getColumnName(i));
			if (i < numCols-1) {
				valuesStr.append("\t");
			}
		}
		valuesStr.append("\n");

		// Copy the values
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				Object value = simulationTable.getValueAt(rowsSelected[i], j);
				valuesStr.append(value == null ? "" : value.toString());
				if (j < numCols-1) {
					valuesStr.append("\t");
				}
			}
			valuesStr.append("\n");
		}

		StringSelection sel = new StringSelection(valuesStr.toString());

		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(sel, sel);
	}

	private void pasteSimulationsAction() {
		Simulation[] sims = OpenRocketClipboard.getClipboardSimulations();
		if (sims == null || sims.length == 0) {
			return;
		}

		duplicateSimulation(sims, simulationTable.getSelectedRow() + 1);
	}

	/**
	 * Duplicates the provided simulations at a certain index and selects them in the simulation table.
	 * @param sims The simulations to duplicate
	 * @param index The index to insert the simulations at (e.g. '1' to insert after the first simulation)
	 */
	public void duplicateSimulation(Simulation[] sims, int index) {
		if (sims == null || sims.length == 0) return;

		// TODO: the undoing doesn't do anything...
		if (sims.length == 1) {
			document.addUndoPosition("Duplicate " + sims[0].getName());
		} else {
			document.addUndoPosition("Duplicate simulations");
		}

		index = index >= 0 ? index : document.getSimulationCount();
		int newIndex = index;
		for (Simulation s: sims) {
			Simulation copy = s.duplicateSimulation(document.getRocket());
			String name = copy.getName();
			if (name.matches(OpenRocketDocument.SIMULATION_NAME_PREFIX + "[0-9]+ *")) {
				copy.setName(document.getNextSimulationName());
			}
			document.addSimulation(copy, newIndex);
			newIndex++;		// Ensure simulations are in the correct order
		}
		simulationTableModel.fireTableDataChanged();
		simulationTable.getSelectionModel().setSelectionInterval(index, newIndex-1);
	}

	/**
	 * Duplicates the provided simulations to the end and selects them in the simulation table.
	 * @param sims The simulations to duplicate.
	 */
	public void duplicateSimulation(Simulation[] sims) {
		duplicateSimulation(sims, document.getSimulationCount());
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

	/**
	 * Add a listener to the simulation table selection model.
	 * @param listener the listener to add.
	 */
	public void addSimulationTableListSelectionListener(ListSelectionListener listener) {
		simulationTable.getSelectionModel().addListSelectionListener(listener);
	}

	public void updateActions() {
		editSimulationAction.updateEnabledState();
		cutSimulationAction.updateEnabledState();
		copySimulationAction.updateEnabledState();
		pasteSimulationAction.updateEnabledState();
		duplicateSimulationAction.updateEnabledState();
		deleteSimulationAction.updateEnabledState();
		runSimulationAction.updateEnabledState();
		plotSimulationAction.updateEnabledState();
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

	private static String getSimulationStatusToolTip(Simulation sim, boolean includeSimName) {
		StringBuilder tip;
		FlightData data = sim.getSimulatedData();

		tip = new StringBuilder("<html>");
		if (includeSimName) {
			tip.append("<b>").append(sim.getName()).append("</b><br>");
		}

		if (data == null) {
			tip.append(trans.get("simpanel.ttip.noData"));
			return tip.toString();
		}

		switch (sim.getStatus()) {
			case CANT_RUN:
				tip.append(trans.get("simpanel.ttip.noData")).append("<br>");
				break;
			case LOADED:
				tip.append(trans.get("simpanel.ttip.loaded")).append("<br>");
				break;
			case UPTODATE:
				tip.append(trans.get("simpanel.ttip.uptodate")).append("<br>");
				break;

			case OUTDATED:
				tip.append(trans.get("simpanel.ttip.outdated")).append("<br>");
				break;

			case EXTERNAL:
				tip.append(trans.get("simpanel.ttip.external")).append("<br>");
				return tip.toString();

			case NOT_SIMULATED:
				tip.append(trans.get("simpanel.ttip.notSimulated"));
				return tip.toString();
		}

		for (int b = 0; b < data.getBranchCount(); b++) {
			FlightEvent abortEvent = data.getBranch(b).getFirstEvent(FlightEvent.Type.SIM_ABORT);
			if (abortEvent != null) {
				tip.append("<font color=\"red\"><i><b>").append(trans.get("simpanel.ttip.simAbort")).append(":</b></i> ").append((abortEvent.getData()).toString()).append("</font><br />");
			}
		}

		return tip.toString();
	}

	private static String getSimulationWarningsToolTip(Simulation sim, boolean includeSimName) {
		StringBuilder tip;
		FlightData data = sim.getSimulatedData();

		tip = new StringBuilder("<html>");
		if (includeSimName) {
			tip.append("<b>").append(sim.getName()).append("</b>");
		}

		if (data == null) {
			tip.append("<br>").append(trans.get("simpanel.ttip.noData"));
			return tip.toString();
		}

		WarningSet warnings = data.getWarningSet();
		if (warnings.isEmpty()) {
			tip.append("<br>").append(ColorConversion.formatHTMLColor(dimTextColor, trans.get("simpanel.ttip.noWarnings")));
			return tip.toString();
		}

		List<Warning> criticalWarnings = warnings.getCriticalWarnings();
		List<Warning> normalWarnings = warnings.getNormalWarnings();
		List<Warning> informativeWarnings = warnings.getInformativeWarnings();

		// Critical warnings
		if (!criticalWarnings.isEmpty()) {
			tip.append("<br><b>")
					.append(ColorConversion.formatHTMLColor(errorColor, trans.get("simpanel.ttip.criticalWarnings")))
					.append("</b>");
			for (Message m : criticalWarnings) {
				tip.append("<br>").append(m.toString());
			}
		}

		// Warnings
		if (!normalWarnings.isEmpty()) {
			tip.append("<br><b>")
					.append(ColorConversion.formatHTMLColor(warningColor, trans.get("simpanel.ttip.normalWarnings")))
					.append("</b>");
			for (Message m : normalWarnings) {
				tip.append("<br>").append(m.toString());
			}
		}

		// Informative warnings
		if (!informativeWarnings.isEmpty()) {
			tip.append("<br><b>")
					.append(ColorConversion.formatHTMLColor(informationColor, trans.get("simpanel.ttip.informativeWarnings")))
					.append("</b>");
			for (Message m : informativeWarnings) {
				tip.append("<br>").append(m.toString());
			}
		}


		return tip.toString();
	}

	private String getSimulationStatusToolTip(Simulation sim) {
		return getSimulationStatusToolTip(sim, true);
	}

	private void openDialog(boolean plotMode, boolean isNewSimulation, final Simulation... sims) {
		SimulationConfigDialog d = new SimulationConfigDialog(SwingUtilities.getWindowAncestor(this), document, isNewSimulation, sims);
		if (plotMode) {
			d.switchToPlotTab();
		} else {
			d.switchToSettingsTab();
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

	public abstract static class SimulationAction extends AbstractAction {
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
			this.putValue(NAME, trans.get("simpanel.pop.edit"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.edit.ttip"));
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
			this.setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class CutSimulationAction extends SimulationAction {
		public CutSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.cut"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.cut.ttip"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_X);
			this.putValue(ACCELERATOR_KEY, RocketActions.CUT_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_CUT);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Simulation[] sims = getSelectedSimulations();

			if (sims == null || sims.length == 0)
				return;

			copySimulationsAction();
			deleteSimulations(sims);
		}

		@Override
		public void updateEnabledState() {
			setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class CopySimulationAction extends SimulationAction {
		public CopySimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.copy"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.copy.ttip"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_C);
			this.putValue(ACCELERATOR_KEY, RocketActions.COPY_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_COPY);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			copySimulationsAction();
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class PasteSimulationAction extends SimulationAction {
		public PasteSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.paste"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.paste.ttip"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_V);
			this.putValue(ACCELERATOR_KEY, RocketActions.PASTE_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_PASTE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			pasteSimulationsAction();
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled((simulationTable.getSelectedRowCount() > 0) &&
					(OpenRocketClipboard.getClipboardSimulations() != null) && (OpenRocketClipboard.getClipboardSimulations().length > 0));
		}
	}

	class DuplicateSimulationAction extends SimulationAction {
		public DuplicateSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.duplicate"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.duplicate.ttip"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
			this.putValue(ACCELERATOR_KEY, RocketActions.DUPLICATE_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_DUPLICATE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			duplicateSimulation(getSelectedSimulations());
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class DeleteSimulationAction extends SimulationAction {
		public DeleteSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.delete"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.delete.ttip"));
			this.putValue(MNEMONIC_KEY, KeyEvent.VK_DELETE);
			this.putValue(ACCELERATOR_KEY, RocketActions.DELETE_KEY_STROKE);
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			deleteSimulations();
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}


	class RunSimulationAction extends SimulationAction {
		public RunSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.run"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.run.ttip"));
			this.putValue(SMALL_ICON, Icons.SIM_RUN);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runSimulation();
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTable.getSelectedRowCount() > 0);
		}
	}

	class PlotSimulationAction extends SimulationAction {
		public PlotSimulationAction() {
			this.putValue(NAME, trans.get("simpanel.pop.plot"));
			this.putValue(SHORT_DESCRIPTION, trans.get("simpanel.pop.plot.ttip"));
			this.putValue(SMALL_ICON, Icons.SIM_PLOT);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			plotSimulation();
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTable.getSelectedRowCount() == 1);
		}
	}

	/**
	 * Export the entire simulation table as a CSV file.
	 */
	class ExportSimulationTableAsCSVAction extends SimulationAction {

		public ExportSimulationTableAsCSVAction() {
			this.putValue(NAME, trans.get("simpanel.pop.exportSimTableToCSV"));
			this.putValue(SMALL_ICON, Icons.SIM_TABLE_EXPORT);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			exportSimulationsToCSV(false);
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTableModel != null && simulationTableModel.getRowCount() > 0);
		}
		
	}

	/**
	 * Export only the selected simulations as a CSV file.
	 */
	class ExportSelectedSimulationsAsCSVAction extends SimulationAction {

		public ExportSelectedSimulationsAsCSVAction() {
			this.putValue(NAME, trans.get("simpanel.pop.exportSelectedSimsToCSV"));
			this.putValue(SMALL_ICON, Icons.SIM_TABLE_EXPORT);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			exportSimulationsToCSV(true);
		}

		@Override
		public void updateEnabledState() {
			this.setEnabled(simulationTableModel != null && simulationTableModel.getRowCount() > 0 &&
					simulationTable.getSelectedRowCount() > 0);
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

				label.setToolTipText(getSimulationStatusToolTip(document.getSimulation(row)));
				return label;
			}

			Component component = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			if (component instanceof JComponent) {
				((JComponent) component).setToolTipText(getSimulationStatusToolTip(
						document.getSimulation(row)));
			}
			return component;
		}
	}

	private class StatusLabel extends StyledLabel {
		private Simulation simulation;

		public StatusLabel(Simulation simulation, float size) {
			super(size);
			this.simulation = simulation;
		}

		public void replaceSimulation(Simulation simulation) {
			this.simulation = simulation;
		}

		@Override
		public String toString() {
			String text = getSimulationStatusToolTip(simulation, false);
			return text.replace("<br>", "-").replaceAll("<[^>]*>","");
		}
	}

	private static class WarningsBox extends Box {
		private Simulation simulation;

		public WarningsBox(Simulation simulation) {
			super(BoxLayout.X_AXIS);		// Horizontal box
			setOpaque(false);
			this.simulation = simulation;
			updateContent();
		}

		public void replaceSimulation(Simulation simulation) {
			this.simulation = simulation;
			updateContent();
		}

		private void updateContent() {
			removeAll(); // Clear existing content before update
			setToolTipText("");

			if (simulation == null) {
				revalidate();
				return;
			}

			// Update the tooltip text
			String ttip = getSimulationWarningsToolTip(simulation, true);
			setToolTipText(ttip);

			WarningSet warnings = simulation.getSimulatedWarnings();

			if (warnings == null || warnings.isEmpty()) {
				revalidate();
				return;
			}

			int nrOfCriticalWarnings = warnings.getNrOfCriticalWarnings();
			int nrOfNormalWarnings = warnings.getNrOfNormalWarnings();
			int nrOfInfoWarnings = warnings.getNrOfInformativeWarnings();

			if (nrOfCriticalWarnings > 0) {
				add(new JLabel(nrOfCriticalWarnings + " "));
				add(new JLabel(Icons.WARNING_HIGH));
			}

			if (nrOfCriticalWarnings > 0 && nrOfNormalWarnings > 0) {
				add(new JLabel(", "));
			}

			if (nrOfNormalWarnings > 0) {
				add(new JLabel(nrOfNormalWarnings + " "));
				add(new JLabel(Icons.WARNING_NORMAL));
			}

			if ((nrOfCriticalWarnings > 0 || nrOfNormalWarnings > 0) && nrOfInfoWarnings > 0) {
				add(new JLabel(", "));
			}

			if (nrOfInfoWarnings > 0) {
				add(new JLabel(nrOfInfoWarnings + " "));
				add(new JLabel(Icons.WARNING_LOW));
			}

			revalidate(); // Notify layout manager of changes
		}
	}

	public static class WarningsBoxRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof WarningsBox box) {
				// Wrap the box in a panel with BorderLayout to allow alignment
				JPanel panel = new JPanel(new BorderLayout());
				panel.setToolTipText(box.getToolTipText());
				panel.add(box, BorderLayout.EAST); 	// Align to the right within the panel
				panel.setOpaque(true);
				if (isSelected) {
					panel.setBackground(table.getSelectionBackground());
					updateBoxColors(box, table.getSelectionForeground());
				} else {
					panel.setBackground(table.getBackground());
					updateBoxColors(box, table.getForeground());
				}

				return panel;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		private void updateBoxColors(WarningsBox box, Color foreground) {
			// Set the foreground for the box and its child components
			box.setForeground(foreground); // Assuming this sets the box's own foreground
			for (Component comp : box.getComponents()) {
				if (comp instanceof JLabel) {
					comp.setForeground(foreground);
				}
			}
		}
	}

	private class SimulationTableModel extends ColumnTableModel {
		private static final long serialVersionUID = 8686456963492628476L;

		public SimulationTableModel() {
			super(
					////  Status column
					new Column("") {
						private StatusLabel label = null;

						@Override
						public Object getValueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							Simulation simulation = document.getSimulation(row);

							// Initialize the label
							if (label == null) {
								label = new StatusLabel(simulation, 2f);
								label.setIconTextGap(1);
								//							label.setFont(label.getFont().deriveFont(Font.BOLD));
							} else {
								label.replaceSimulation(simulation);
							}

							// Set simulation status icon
							Simulation.Status status = simulation.getStatus();
							label.setIcon(Icons.SIMULATION_STATUS_ICON_MAP.get(status));

							return label;
						}

						@Override
						public int getExactWidth() {
							return 26;
						}

						@Override
						public Class<?> getColumnClass() {
							return JLabel.class;
						}
					},

					//// Warnings column
					new Column(trans.get("simpanel.col.Warnings")) {
						private WarningsBox box = null;

						@Override
						public Object getValueAt(int row) {
							if (row < 0 || row >= document.getSimulationCount())
								return null;

							Simulation simulation = document.getSimulation(row);

							// Initialize the box
							if (box == null) {
								box = new WarningsBox(simulation);
							} else {
								box.replaceSimulation(simulation);
							}

							return box;
						}

						@Override
						public int getDefaultWidth() {
							return 70;
						}

						@Override
						public Class<?> getColumnClass() {
							return WarningsBox.class;
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
							if (data == null)
								return null;

							return data.getOptimumDelay();
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				simulationTable.requestFocusInWindow();
				if (simulationTable.getRowCount() == 0 || simulationTable.getSelectedRows().length > 0) {
					return;
				}
				if (previousSelection == null || previousSelection.length == 0) {
					simulationTable.getSelectionModel().setSelectionInterval(0, 0);
				} else {
					simulationTable.clearSelection();
					for (int row : previousSelection) {
						if (row < 0 || row >= simulationTable.getRowCount()) {
							continue;
						}
						simulationTable.addRowSelectionInterval(row, row);
					}
				}
				updateActions();
			}
		});
	}
}
