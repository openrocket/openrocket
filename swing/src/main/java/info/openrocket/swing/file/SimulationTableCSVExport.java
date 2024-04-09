package info.openrocket.swing.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import info.openrocket.core.util.StringUtils;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.swing.gui.adaptors.Column;
import info.openrocket.swing.gui.adaptors.ColumnTableModel;
import info.openrocket.swing.gui.adaptors.ValueColumn;
import info.openrocket.core.unit.Value;
import info.openrocket.core.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationTableCSVExport {
	private final OpenRocketDocument document;
	private final JTable simulationTable;
	private final ColumnTableModel simulationTableModel;
	private final HashMap<String, String> valueColumnToUnitString = new HashMap<>();

	private static final Logger log = LoggerFactory.getLogger(SimulationTableCSVExport.class);

	public SimulationTableCSVExport (OpenRocketDocument document, JTable simulationTable,
									 ColumnTableModel simulationTableModel) {
		this.document = document;
		this.simulationTable = simulationTable;
		this.simulationTableModel = simulationTableModel;
	}

	/**
	 * To make a lookup of table header to units.  For those columns which are of type Value, the
	 * units will be added to the header...
	 */
	private void populateColumnNameToUnitsHashTable() {
		valueColumnToUnitString.clear(); 	// Necessary if units changed during session
		if (simulationTableModel == null) {
			return;
		}
		for (int i = 0; i < simulationTableModel.getColumnCount(); i++) {
			Column c = simulationTableModel.getColumn(i);
			if (c instanceof ValueColumn) {
				// Only value columns seem to have units that are not zero length strings... These are
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
	 * Generate the CSV data from the simulation table
	 * @param fieldSep The field separator to use in the CSV file.
	 * @param precision The number of decimal places to use in the CSV file.
	 * @param isExponentialNotation If true, use exponential notation for numbers.
	 * @param onlySelected If true, only export the selected rows in the table.
	 * @return The CSV data as one string block.
	 */
	public String generateCSVDate(String fieldSep, int precision, boolean isExponentialNotation, boolean onlySelected) {
		int modelColumnCount = simulationTableModel.getColumnCount();
		int modelRowCount = simulationTableModel.getRowCount();
		populateColumnNameToUnitsHashTable();

		String CSVSimResultString;
		// Obtain the column titles for the first row of the CSV
		ArrayList<String> rowColumnElement = new ArrayList<>();
		for (int j = 1; j < modelColumnCount ; j++) {
			String colName = simulationTable.getColumnName(j);

			// Get the unit string and append to column that it applies to. Columns w/o units will remain unchanged.
			if (valueColumnToUnitString.containsKey(colName)) {
				String unitString = valueColumnToUnitString.get(colName);
				colName += " (" + unitString + ")";
			}
			rowColumnElement.add(colName);
		}

		// ONE difference here is that we'll place any warnings at the last cell in the CSV.
		CSVSimResultString = StringUtils.join(fieldSep, rowColumnElement) + fieldSep + "Simulation Warnings";

		StringBuilder fullOutputResult = new StringBuilder(CSVSimResultString);

		// Get relevant data and create the comma separated data from it.
		int[] iterator;
		if (onlySelected) {
			iterator = simulationTable.getSelectedRows();
		} else {
			iterator = new int[modelRowCount];
			for (int i = 0; i < modelRowCount; i++) {
				iterator[i] = i;
			}
		}
		for (int i : iterator) {
			// Account for sorting... resulting CSV file will be in the same order as shown in the table thanks to this gem.
			int idx = simulationTable.convertRowIndexToModel(i);

			// Ignore empty simulation
			if (!document.getSimulation(idx).hasSummaryData()) {
				continue;
			}

			int nullCnt = 0;
			rowColumnElement.clear();

			// Get the simulation's warning text if any. This bypasses the need to use
			// the column 0 stuff which is kind of difficult to use!
			WarningSet ws = document.getSimulation(idx).getSimulatedWarnings();
			StringBuilder warningsText = new StringBuilder();
			for (Warning w : ws) {
				String warning = w.toString();
				if (warning != null) {
					warningsText.append(w).append(" "); // TODO - formatting.  inserting a \n does funny things so use " " for now
				}
			}

			// Piece together the column data for the index-row, skipping any rows with null counts > 0!
			for (int j = 1; j < modelColumnCount ; j++) { // skip first column
				Object o = simulationTableModel.getValueAt(idx, j);
				if (o != null) {
					final String valueString;
					if (o instanceof Value) {
						double value = ((Value) o).getUnitValue();
						valueString = TextUtil.doubleToString(value, precision, isExponentialNotation);
					} else {
						valueString = o.toString();
					}
					rowColumnElement.add(StringUtils.escapeCSV(valueString));
				} else {
					rowColumnElement.add("");
					nullCnt++;
				}
			}

			// If there are any null columns, need to run the simulation before we can export it
			if (nullCnt > 0) { // ignore rows that have null column fields 1 through 8...
				continue;
			}

			// Create the column data comma separated string for the ith row...
			CSVSimResultString = StringUtils.join(fieldSep, rowColumnElement);

			// Piece together all rows into one big ginormous string, adding any warnings to the item
			fullOutputResult.append("\n").append(CSVSimResultString);
			fullOutputResult.append(fieldSep).append(warningsText);
		}

		return fullOutputResult.toString();
	}

	/**
	 * Export the simulation table data to a CSV file
	 * @param file the file to save the results to
	 * @param fieldSep the CSV separator to use
	 * @param precision the decimal precision to use in the CSV file
	 * @param isExponentialNotation if true, use exponential notation for numbers
	 * @param onlySelected if true, only export the selected rows in the table
	 */
	public void export(File file, String fieldSep, int precision, boolean isExponentialNotation, boolean onlySelected) {
		if (file == null) {
			log.warn("No file selected for export");
			return;
		}

		String CSVData = generateCSVDate(fieldSep, precision, isExponentialNotation, onlySelected);
		this.dumpDataToFile(CSVData, file);
		log.info("Simulation table data exported to " + file.getAbsolutePath());
	}
}
