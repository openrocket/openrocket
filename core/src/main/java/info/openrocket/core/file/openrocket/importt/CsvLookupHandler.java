package info.openrocket.core.file.openrocket.importt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.simulation.SimulationOptions;
import java.util.Collection;

/**
 * Handler for CSV lookup tables that can contain embedded row data.
 * Format:
 * <draglookupcsv file="path/to/file.csv">
 *   <row>Mach,AoA,Cd</row>
 *   <row>0.30,0,0.35</row>
 * </draglookupcsv>
 */
class CsvLookupHandler extends AbstractElementHandler {
	private final SimulationOptions options;
	private final Collection<String> requiredColumns;
	private final boolean isDrag;
	private Path filePath;
	private final List<String> rows = new ArrayList<>();

	public CsvLookupHandler(SimulationOptions options, Collection<String> requiredColumns, boolean isDrag) {
		this.options = options;
		this.requiredColumns = requiredColumns;
		this.isDrag = isDrag;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (element.equals("row")) {
			return PlainTextHandler.INSTANCE;
		}
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) {
		if (element.equals("row")) {
			String trimmed = content.trim();
			if (!trimmed.isEmpty()) {
				rows.add(trimmed);
			}
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) {
		// Extract file path from attributes if present
		String fileAttr = attributes != null ? attributes.get("file") : null;
		if (fileAttr != null && !fileAttr.trim().isEmpty()) {
			try {
				filePath = Path.of(fileAttr.trim());
			} catch (Exception ex) {
				warnings.add("Invalid file path in " + element + ": " + fileAttr);
			}
		}

		// Try to load from embedded rows first (preferred)
		if (!rows.isEmpty()) {
			try {
				// Default separator is comma, but we could detect it from the first row
				// For now, use comma as default
				char separator = detectSeparator(rows);
				MachAoALookup table = CsvMachAoALookup.parse(rows, requiredColumns, separator);
				// Store both the table and the original CSV rows to preserve edits
				if (isDrag) {
					options.setDragLookup(filePath, table, rows);
				} else {
					options.setStabilityLookup(filePath, table, rows);
				}
				return;
			} catch (RuntimeException ex) {
				warnings.add("Failed to parse embedded CSV data in " + element + ": " + ex.getMessage());
			}
		}

		// Fall back to loading from file if available
		if (filePath != null) {
			try {
				if (isDrag) {
					options.setDragLookupCsvPath(filePath);
				} else {
					options.setStabilityLookupCsvPath(filePath);
				}
			} catch (RuntimeException ex) {
				warnings.add("Failed to load " + element + " from file '" + filePath + "': " + ex.getMessage());
			}
		} else if (!rows.isEmpty()) {
			// If we have rows but no file path, still try to use the rows
			// but with a warning
			warnings.add(element + " has embedded data but no file reference. Data may not persist correctly.");
		}
	}

	/**
	 * Detect the separator character from the first row of CSV data.
	 * Checks for comma, semicolon, tab, or space.
	 */
	private char detectSeparator(List<String> rows) {
		if (rows.isEmpty()) {
			return ',';
		}
		String firstRow = rows.get(0);
		// Count occurrences of each common separator
		int commaCount = countOccurrences(firstRow, ',');
		int semicolonCount = countOccurrences(firstRow, ';');
		int tabCount = countOccurrences(firstRow, '\t');
		
		// Return the separator with the most occurrences
		if (commaCount >= semicolonCount && commaCount >= tabCount && commaCount > 0) {
			return ',';
		} else if (semicolonCount >= tabCount && semicolonCount > 0) {
			return ';';
		} else if (tabCount > 0) {
			return '\t';
		}
		// Default to comma
		return ',';
	}

	private int countOccurrences(String str, char ch) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ch) {
				count++;
			}
		}
		return count;
	}
}

