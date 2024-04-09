package info.openrocket.swing.gui.util;

import info.openrocket.swing.gui.dialogs.preset.XTableColumnModel;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Utility class for storing and loading the following table UI preferences:
 * - column width
 * - column order
 * - column visibility
 */
public class TableUIPreferences {

	private static final String TABLE_COLUMN_WIDTH_PREFIX = ".cw.";
	private static final String TABLE_COLUMN_ORDER_PREFIX = ".co.";
	private static final String TABLE_COLUMN_VISIBILITY_PREFIX = ".cv.";

	public static void storeTableUIPreferences(JTable table, String tableName, Preferences preferences) {
		// Store column widths
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			int width = column.getWidth();
			preferences.putInt(tableName + TABLE_COLUMN_WIDTH_PREFIX + column.getIdentifier(), width);
		}

		// Store column order
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			preferences.putInt(tableName + TABLE_COLUMN_ORDER_PREFIX + column.getIdentifier(), i);
		}

		// Store column visibility
		if (table.getColumnModel() instanceof XTableColumnModel customModel) {
			Enumeration<TableColumn> columns = customModel.getAllColumns();
			while (columns.hasMoreElements()) {
				TableColumn column = columns.nextElement();
				boolean isVisible = customModel.isColumnVisible(column);
				preferences.putBoolean(tableName + TABLE_COLUMN_VISIBILITY_PREFIX + column.getIdentifier(), isVisible);
			}
		}
	}

	public static void loadTableUIPreferences(JTable table, String tableName, Preferences preferences) {
		// Ensure all columns are visible
		Enumeration<TableColumn> allColumns = table.getColumnModel().getColumns();
		List<TableColumn> removedColumns = new ArrayList<>();
		while (allColumns.hasMoreElements()) {
			TableColumn column = allColumns.nextElement();
			if (table.convertColumnIndexToView(column.getModelIndex()) == -1) {
				removedColumns.add(column);
			}
		}
		for (TableColumn col : removedColumns) {
			table.addColumn(col);
		}

		// Get all columns from the table's column model and restore visibility
		if (table.getColumnModel() instanceof XTableColumnModel customModel) {
			Enumeration<TableColumn> columns = customModel.getAllColumns();  // Use getAllColumns to get all columns, including invisible ones
			while (columns.hasMoreElements()) {
				TableColumn column = columns.nextElement();
				String identifier = column.getIdentifier().toString();
				// Default to true if the preference is not found
				boolean isVisible = preferences.getBoolean(tableName + TABLE_COLUMN_VISIBILITY_PREFIX + identifier, true);
				customModel.setColumnVisible(column, isVisible);
			}
		}

		// Now, restore column order
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			int storedOrder = preferences.getInt(tableName + TABLE_COLUMN_ORDER_PREFIX + column.getIdentifier(), i);
			if (storedOrder != i && storedOrder < table.getColumnCount()) {
				table.moveColumn(table.convertColumnIndexToView(column.getModelIndex()), storedOrder);
			}
		}

		// Check if any column width is missing from preferences
		boolean computeOptimalWidths = false;
		for (int i = 0; i < table.getColumnCount() && !computeOptimalWidths; i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			if (preferences.get(tableName + TABLE_COLUMN_WIDTH_PREFIX + column.getIdentifier(), null) == null) {
				computeOptimalWidths = true;
			}
		}

		// If any column width is missing, compute optimal widths for all columns
		int[] optimalWidths = null;
		if (computeOptimalWidths) {
			optimalWidths = GUIUtil.computeOptimalColumnWidths(table, 20, true);
		}

		// Restore column widths
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			int defaultWidth = (optimalWidths != null) ? optimalWidths[i] : column.getWidth();
			int width = preferences.getInt(tableName + TABLE_COLUMN_WIDTH_PREFIX + column.getIdentifier(), defaultWidth);
			column.setPreferredWidth(width);
		}
	}
}