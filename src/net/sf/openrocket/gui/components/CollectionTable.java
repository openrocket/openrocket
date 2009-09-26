package net.sf.openrocket.gui.components;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


/*
 * TODO: LOW:  This is currently unused.
 */
public abstract class CollectionTable<T> extends JTable {

	private final String[] columnNames;
	private CollectionTableModel model;
	
	
	protected CollectionTable(String[] columnNames) {
		this.columnNames = columnNames.clone();
	}
	
	
	protected void initializeTable() {
		model = new CollectionTableModel();
		this.setModel(model);
	}
	
	
	/**
	 * Retrieve the object for the specified row number.
	 * 
	 * @param row	the row number being queried.
	 * @return		the object at that row.
	 */
	protected abstract T getModelObjectAt(int row);
	
	protected abstract int getModelRowCount();
	
	
	
	protected abstract Object getViewForModelObject(T object, int column);
	
	protected Class<?> getViewColumnClass(int column) {
		return Object.class;
	}
	
	
	
	private class CollectionTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return getViewColumnClass(column);
		}


		@Override
		public int getRowCount() {
			return getModelRowCount();
		}

		@Override
		public Object getValueAt(int row, int column) {
			T value = getModelObjectAt(row);
			return getViewForModelObject(value, column);
		}
	}
}
