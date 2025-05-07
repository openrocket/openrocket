package info.openrocket.swing.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
	 * Generate CSV data from the simulation table
	 *
	 * @param fieldSep The field separator to use in the CSV file
	 * @param precision The number of decimal places for numeric values
	 * @param isExponentialNotation Whether to use exponential notation for numbers
	 * @param onlySelected Whether to export only the selected rows
	 * @return The CSV data as a single string
	 */
	public String generateCSVData(String fieldSep, int precision,
								  boolean isExponentialNotation, boolean onlySelected) {

		// Initialize and populate column name to units mapping
		populateColumnNameToUnitsHashTable();

		// Build the header row
		StringBuilder csvOutput = new StringBuilder();
		List<String> headerColumns = buildHeaderRow();
		csvOutput.append(String.join(fieldSep, headerColumns));

		// Get row indices to process (either selected rows or all rows)
		int[] rowsToProcess = getRowsToProcess(onlySelected);

		// Process each row and append to output
		String warningDelimiter = fieldSep.equals("|") ? " ; " : " | ";

		for (int viewRowIndex : rowsToProcess) {
			// Convert view row index to model row index (handles sorting)
			int modelRowIndex = simulationTable.convertRowIndexToModel(viewRowIndex);

			// Skip simulations without summary data
			if (!document.getSimulation(modelRowIndex).hasSummaryData()) {
				continue;
			}

			// Generate row data
			List<String> rowData = buildRowData(modelRowIndex, precision,
					isExponentialNotation, warningDelimiter);

			// Skip rows with missing data
			if (rowData.isEmpty()) {
				continue;
			}

			// Append row to output
			csvOutput.append("\n").append(String.join(fieldSep, rowData));
		}

		return csvOutput.toString();
	}

	/**
	 * Builds the header row for the CSV file
	 *
	 * @return List of column headers with units
	 */
	private List<String> buildHeaderRow() {
		List<String> headers = new ArrayList<>();

		// Add columns starting from index 1 (skipping first column)
		for (int j = 1; j < simulationTableModel.getColumnCount(); j++) {
			String colName = simulationTable.getColumnName(j);

			// Append units to column names where applicable
			if (valueColumnToUnitString.containsKey(colName)) {
				String unitString = valueColumnToUnitString.get(colName);
				colName += " (" + unitString + ")";
			}
			headers.add(colName);
		}

		return headers;
	}

	/**
	 * Gets the row indices to process based on selection preference
	 *
	 * @param onlySelected Whether to export only selected rows
	 * @return Array of row indices to process
	 */
	private int[] getRowsToProcess(boolean onlySelected) {
		if (onlySelected) {
			return simulationTable.getSelectedRows();
		} else {
			int rowCount = simulationTableModel.getRowCount();
			int[] allRows = new int[rowCount];
			for (int i = 0; i < rowCount; i++) {
				allRows[i] = i;
			}
			return allRows;
		}
	}

	/**
	 * Builds a row of data for the CSV file
	 *
	 * @param modelRowIndex Index of the row in the model
	 * @param precision Decimal precision
	 * @param isExponentialNotation Whether to use exponential notation
	 * @param warningDelimiter Delimiter for warning messages
	 * @return List of column values, or empty list if row has missing data
	 */
	private List<String> buildRowData(int modelRowIndex, int precision,
									  boolean isExponentialNotation, String warningDelimiter) {

		List<String> rowData = new ArrayList<>();

		// Add warnings text
		String warningsText = formatWarningsText(modelRowIndex, warningDelimiter);
		rowData.add(warningsText);

		// Process each column (starting from column 2, skipping warnings and name)
		int nullCount = 0;
		for (int j = 2; j < simulationTableModel.getColumnCount(); j++) {
			Object cellValue = simulationTableModel.getValueAt(modelRowIndex, j);

			if (cellValue != null) {
				String formattedValue = formatCellValue(cellValue, precision, isExponentialNotation);
				rowData.add(StringUtils.escapeCSV(formattedValue));
			} else {
				rowData.add("");
				nullCount++;
			}
		}

		// Return empty list if any required data is missing
		return nullCount > 0 ? Collections.emptyList() : rowData;
	}

	/**
	 * Formats warning text for a simulation
	 *
	 * @param modelRowIndex Index of the row in the model
	 * @param warningDelimiter Delimiter for warnings
	 * @return Formatted warning text
	 */
	private String formatWarningsText(int modelRowIndex, String warningDelimiter) {
		WarningSet warnings = document.getSimulation(modelRowIndex).getSimulatedWarnings();
		if (warnings.isEmpty()) {
			return "";
		}

		StringBuilder warningsText = new StringBuilder();
		int warningIndex = 0;

		for (Warning warning : warnings) {
			if (warning != null) {
				String escapedWarning = StringUtils.escapeCSV(warning.toString());
				warningsText.append(escapedWarning);

				if (warningIndex < warnings.size() - 1) {
					warningsText.append(warningDelimiter);
				}
			}
			warningIndex++;
		}

		return warningsText.toString();
	}

	/**
	 * Formats a cell value based on its type
	 *
	 * @param cellValue The value to format
	 * @param precision Decimal precision
	 * @param isExponentialNotation Whether to use exponential notation
	 * @return Formatted value as string
	 */
	private String formatCellValue(Object cellValue, int precision, boolean isExponentialNotation) {
		if (cellValue instanceof Value) {
			double value = ((Value) cellValue).getUnitValue();
			return TextUtil.doubleToString(value, precision, isExponentialNotation);
		} else {
			return cellValue.toString();
		}
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

		String CSVData = generateCSVData(fieldSep, precision, isExponentialNotation, onlySelected);
		this.dumpDataToFile(CSVData, file);
		log.info("Simulation table data exported to " + file.getAbsolutePath());
	}
}
