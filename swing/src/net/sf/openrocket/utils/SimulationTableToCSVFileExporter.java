package net.sf.openrocket.utils;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.adaptors.ValueColumn;
import net.sf.openrocket.gui.components.CsvOptionPanel;
import net.sf.openrocket.gui.dialogs.optimization.GeneralOptimizationDialog;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;

public class SimulationTableToCSVFileExporter {
	private final OpenRocketDocument document;
	private final JTable simulationTable;
	private final ColumnTableModel simulationTableModel;
	private final HashMap<String, String> valueColumnToUnitString = new HashMap<>();

	private static final Translator trans = Application.getTranslator();
	private static final String SPACE = "SPACE";
	private static final String TAB = "TAB";

	public SimulationTableToCSVFileExporter (OpenRocketDocument document,
			JTable simulationTable,
			ColumnTableModel simulationTableModel			
			) 
	{
		this.document = document;
		this.simulationTable = simulationTable;
		this.simulationTableModel = simulationTableModel;
	}

	/**
	 * Means by which the CSV export will clean up units on the values and
	 * describe them on the header fields instead.
	 */
	private void populateColumnNameToUnitsHashTable() {
		if (null == simulationTableModel) {
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
	 * @param data The csv data as one string block.
	 * @param csvFile The file to dump the data to.
	 */
	private void dumpDataToFile(String data, File csvFile) {
		BufferedWriter bufferedWriter = null;
		try {
			csvFile.createNewFile();
			bufferedWriter = new BufferedWriter(new FileWriter(csvFile));
			bufferedWriter.write(data);
		} catch (FileNotFoundException e) {
			String msg = e.getMessage();
			JOptionPane.showMessageDialog(simulationTable.getParent(), msg);
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

	private JFileChooser setUpFileChooser() {
		JFileChooser fch = new JFileChooser();
		String saveDialogTitle = trans.get("simpanel.pop.export_to_csv.save.dialog.title");
		String saveButtonText = trans.get("simpanel.pop.export_to_csv.save.button.text");
		fch.setApproveButtonText(saveButtonText);
		fch.setDialogTitle(saveDialogTitle);
		fch.setApproveButtonToolTipText(saveDialogTitle);

		fch.setFileFilter(FileHelper.CSV_FILTER);

		// default output csv to same name as the document's rocket name.
		String documentFileName = document.getRocket().getName();
		documentFileName += ".csv";
		fch.setSelectedFile(new File(documentFileName));
		CsvOptionPanel csvOptions = new CsvOptionPanel(SimulationTableToCSVFileExporter.class /*,
				trans.get("GeneralOptimizationDialog.export.header"), trans.get("GeneralOptimizationDialog.export.header.ttip")*/);

		fch.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		fch.setAccessory(csvOptions);
		
		return fch;
	}

	public void performTableDataConversion() {
		// one down side is that to change this means you have to export and save at least ONCE!
		// there is no entry in the preferences to set this a-priori and it is default to a comma
		String fieldSep = Application.getPreferences().getString(Preferences.EXPORT_FIELD_SEPARATOR, ",");

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
		if (selectionStatus == JFileChooser.CANCEL_OPTION || selectionStatus == JFileChooser.ERROR_OPTION) {
			return;  // cancel or error... nothing to do here
		}

		fieldSep = ((CsvOptionPanel) fch.getAccessory()).getFieldSeparator();
		((CsvOptionPanel) fch.getAccessory()).storePreferences();

		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}

		File csvFile = fch.getSelectedFile();

		String csvSimResultString = "";
		// obtain the column titles for the first row of the csv
		ArrayList<String> rowColumnElement = new ArrayList<>();
		for (int j=1; j<modelColumnCount ; j++) {
			String colName = simulationTable.getColumnName(j);
			String unitString = valueColumnToUnitString.get(colName);
			if (unitString != null) {
				colName += " (" + unitString + ")";
			}
			rowColumnElement.add(colName);
		}

		// ONE difference here is that we'll place any warnings at the last cell in the csv.
		csvSimResultString = StringUtils.join(rowColumnElement,fieldSep) + fieldSep + "Simulation Warnings";

		String fullOutputResult = csvSimResultString;
		
		// get relevant data and create the comma separated data from it.
		for (int i1 = 0; i1 < modelRowCount; i1++) {
			// account for sorting... resulting csv file will be in the
			// same order as shown in the table thanks to this gem.
			int i = simulationTable.convertRowIndexToModel(i1);

			int nullCnt = 0;
			rowColumnElement.clear();

			// get the simulation's warning text if any... this bypasses the need to use
			// the column 0 stuff which is kind of difficult to use!
			WarningSet ws = document.getSimulation(i).getSimulatedWarnings();
			String warningsText = "";
			for (Warning w : ws) {
				String warning = w.toString();
				if (warning != null) {
					warningsText += w + " "; // TODO - formatting.  inserting a \n does funny things so use " " for now
				}
			}

			// piece together the column data for the ith row, skipping any rows with null counts > 0!
			for (int j=1; j<modelColumnCount ; j++) { // skip first column
				String colName = simulationTable.getColumnName(j);
				String unitString = valueColumnToUnitString.get(colName); // unit string MAY be null!

				Object o = simulationTableModel.getValueAt(i, j);
				if (o != null) {
					String value = o.toString();
					if (unitString != null) {
						value = value.replace(" " + unitString, "");
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
			csvSimResultString = StringUtils.join(rowColumnElement, fieldSep);

			// piece together all rows into one big ginourmous string, adding any warnings to the item
			fullOutputResult += "\n" + csvSimResultString + fieldSep + warningsText;
		}
		
		// dump the string to the file.
		this.dumpDataToFile(fullOutputResult, csvFile);
		return;
	}

}
