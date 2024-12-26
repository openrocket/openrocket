package info.openrocket.swing.gui.adaptors;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import info.openrocket.core.startup.Application;

@SuppressWarnings("serial")
public abstract class ColumnTableModel extends AbstractTableModel {
	private final Column[] columns;
	
	public ColumnTableModel(Column... columns) {
		this.columns = columns;
	}

	public void setColumnWidths(TableColumnModel model) {
		for (int i = 0; i < columns.length; i++) {
			TableColumn col = model.getColumn(i);

			if (columns[i].getExactWidth() > 0) {
				// Handle exact width columns as before
				int w = columns[i].getExactWidth();
				col.setResizable(false);
				col.setMinWidth(w);
				col.setMaxWidth(w);
				col.setPreferredWidth(w);
			}
			else if (columns[i].isAutoSize()) {
				// For auto-size columns, set minimum width but allow expansion
				col.setResizable(true);
				col.setMinWidth(columns[i].getDefaultWidth());
				col.setPreferredWidth(columns[i].getDefaultWidth());
				col.setMaxWidth(Integer.MAX_VALUE);
			}
			else {
				// Handle normal columns as before
				col.setPreferredWidth(columns[i].getDefaultWidth());
			}
		}
	}
	
	public Column getColumn(int i) {
		return columns[i];
	}
	
	@Override
	public int getColumnCount() {
		return columns.length;
	}
	
	@Override
	public String getColumnName(int col) {
		return columns[col].toString();
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		return columns[col].getColumnClass();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		if ((row < 0) || (row >= getRowCount()) ||
				(col < 0) || (col >= columns.length)) {
			Application.getExceptionHandler().handleErrorCondition("Error:  Requested illegal column/row, col=" + col + " row=" + row);
			return null;
		}
		return columns[col].getValueAt(row);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		columns[columnIndex].setValueAt(rowIndex, aValue);
	}
	
}
