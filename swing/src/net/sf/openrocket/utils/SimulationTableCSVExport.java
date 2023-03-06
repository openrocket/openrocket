package net.sf.openrocket.utils;

import java.awt.Container;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.text.StringEscapeUtils;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.adaptors.ValueColumn;
import net.sf.openrocket.gui.components.CsvOptionPanel;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationTableCSVExport {
	private final OpenRocketDocument document;
	private final JTable simulationTable;
	private final ColumnTableModel simulationTableModel;
	private final HashMap<String, String> valueColumnToUnitString = new HashMap<>();

	private static final String SPACE = "SPACE";
	private static final String TAB = "TAB";

	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(SimulationTableCSVExport.class);

	public SimulationTableCSVExport (OpenRocketDocument document,
			JTable simulationTable,
			ColumnTableModel simulationTableModel			
			) {
		this.document = document;
		this.simulationTable = simulationTable;
		this.simulationTableModel = simulationTableModel;
	}

	/**
	 * To make a lookup of table header to units.  For those columns which are of type Value, the
	 * units will be added to the header...
	 */
	private void populateColumnNameToUnitsHashTable() {
		if (simulationTableModel == null) {
			return;
		}
		valueColumnToUnitString.clear(); // necessary if units changed during session
		for (int i=0; i<simulationTableModel.getColumnCount(); i++) {
			Column c = simulationTableModel.getColumn(i);
			if (c instanceof ValueColumn) {
				// only value columns seem to have units that are not zero length strings... these are
				// the ones we actually want in our lookup table.
				valueColumnToUnitString.put(c.toString(), c.getUnits().getDefaultUnit().getUnit());
			}
			
		}
	}
	/**
	 * Dump data from sim table to file for run simulations
	 * @param data The CSV data as one string block.
	 * @param CSVFile The file to dump the data to.
	 */
	private void dumpDataToFile(String data, File CSVFile) {
		BufferedWriter bufferedWriter = null;
		try {
			CSVFile.createNewFile();
			bufferedWriter = new BufferedWriter(new FileWriter(CSVFile));
			bufferedWriter.write(data);
		} catch (IOException e) {
			String msg = e.getMessage();
			JOptionPane.showMessageDialog(simulationTable.getParent(), msg);
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(simulationTable.getParent(), msg);
				}
			}
		}
	}

	/**
	 * Create the file chooser to save the CSV file.
	 * @return The file chooser.
	 */
	private JFileChooser setUpFileChooser() {
		JFileChooser fch = new JFileChooser();
		fch.setDialogTitle(trans.get("simpanel.pop.exportToCSV.save.dialog.title"));
		fch.setFileFilter(FileHelper.CSV_FILTER);
		fch.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		// Default output CSV to same name as the document's rocket name.
		String fileName = document.getRocket().getName() + ".csv";
		fch.setSelectedFile(new File(fileName));

		// Add CSV options to FileChooser
		CsvOptionPanel CSVOptions = new CsvOptionPanel(SimulationTableCSVExport.class);
		fch.setAccessory(CSVOptions);
		
		return fch;
	}

	public void performTableDataConversion() {
		Container tableParent = simulationTable.getParent();
		int modelColumnCount = simulationTableModel.getColumnCount();
		int modelRowCount = simulationTableModel.getRowCount();
		populateColumnNameToUnitsHashTable();

		// I'm pretty sure with the enablement/disablement of the menu item under the File dropdown,
		// that this would no longer be needed because if there is no sim table yet, the context menu
		// won't show up.   But I'm going to leave this in just in case....
		if (modelRowCount <= 0) {
			String msg = trans.get("simpanel.dlg.no.simulation.table.rows");
			JOptionPane.showMessageDialog(tableParent, msg);
			return;
		}

		JFileChooser fch = this.setUpFileChooser();
		int selectionStatus = fch.showSaveDialog(tableParent);
		if (selectionStatus != JFileChooser.APPROVE_OPTION) {
			log.info("User cancelled CSV export");
			return;
		}

		String fieldSep = ((CsvOptionPanel) fch.getAccessory()).getFieldSeparator();
		int precision = ((CsvOptionPanel) fch.getAccessory()).getDecimalPlaces();
		((CsvOptionPanel) fch.getAccessory()).storePreferences();

		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}

		File CSVFile = fch.getSelectedFile();
		CSVFile = FileHelper.forceExtension(CSVFile, "csv");

		String CSVSimResultString;
		// obtain the column titles for the first row of the CSV
		ArrayList<String> rowColumnElement = new ArrayList<>();
		for (int j = 1; j<modelColumnCount ; j++) {
			String colName = simulationTable.getColumnName(j);
			
			// get the unit string and append to column that it applies to.
			// columns w/o units will remain unchanged.
			if (valueColumnToUnitString.containsKey(colName)) {
				String unitString = valueColumnToUnitString.get(colName);
				colName += " (" + unitString + ")"; 
			}
			rowColumnElement.add(colName);
		}

		// ONE difference here is that we'll place any warnings at the last cell in the CSV.
		CSVSimResultString = StringUtils.join(fieldSep, rowColumnElement) + fieldSep + "Simulation Warnings";

		StringBuilder fullOutputResult = new StringBuilder(CSVSimResultString);
		
		// get relevant data and create the comma separated data from it.
		for (int i = 0; i < modelRowCount; i++) {
			// account for sorting... resulting CSV file will be in the
			// same order as shown in the table thanks to this gem.
			int idx = simulationTable.convertRowIndexToModel(i);

			int nullCnt = 0;
			rowColumnElement.clear();

			// get the simulation's warning text if any... this bypasses the need to use
			// the column 0 stuff which is kind of difficult to use!
			WarningSet ws = document.getSimulation(idx).getSimulatedWarnings();
			StringBuilder warningsText = new StringBuilder();
			for (Warning w : ws) {
				String warning = w.toString();
				if (warning != null) {
					warningsText.append(w).append(" "); // TODO - formatting.  inserting a \n does funny things so use " " for now
				}
			}

			// piece together the column data for the ith row, skipping any rows with null counts > 0!
			for (int j = 1; j < modelColumnCount ; j++) { // skip first column
				Object o = simulationTableModel.getValueAt(idx, j);
				if (o != null) {
					final String value;
					if (o instanceof Value) {
						double dvalue = ((Value) o).getValue();
						value = TextUtil.doubleToString(dvalue, precision);
					} else {
						value = o.toString();
					}
					rowColumnElement.add(StringEscapeUtils.escapeCsv(value));
				} else {
					rowColumnElement.add("");
					nullCnt++;
				}
			}
			
			// current "unstable" will have a populated sim table EXCEPT for the optimum delay column on a restart
			// after a save.  That means any row that WAS simulated will have exactly one null column in it... so we'll
			// skip row export for the case where there are MORE than one nulls.  Either way the user should run sims.
			if (nullCnt > 1) { // ignore rows that have null column fields 1 through 8... 
				continue;
			}
			
			// create the column data comma separated string for the ith row...
			CSVSimResultString = StringUtils.join(fieldSep, rowColumnElement);

			// piece together all rows into one big ginormous string, adding any warnings to the item
			fullOutputResult.append("\n").append(CSVSimResultString).append(fieldSep).append(warningsText);
		}
		
		// dump the string to the file.
		this.dumpDataToFile(fullOutputResult.toString(), CSVFile);
	}

}
