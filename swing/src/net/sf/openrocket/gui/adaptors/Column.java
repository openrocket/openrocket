package net.sf.openrocket.gui.adaptors;

import java.util.Comparator;

import javax.swing.table.TableColumnModel;

public abstract class Column {
	private final String name;
	private final String toolTip;
	
	/**
	 * Create a new column with specified name.  Additionally, the {@link #getValueAt(int)}
	 * method must be implemented.
	 * 
	 * @param name	the caption of the column.
	 */
	public Column(String name) {
		this.name = name;
		this.toolTip = null;
	}
	
	/**
	 * Create a new column with specified name and toolTip.
	 * 
	 * 
	 */
	public Column(String name, String toolTip ) {
		this.name = name;
		this.toolTip = toolTip;
	}
	
	/**
	 * Return the caption of the column.
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Return the default width of the column.  This is used by the method
	 * {@link #ColumnTableModel.setColumnWidth(TableColumnModel)}.  The default width is
	 * 100, the method may be overridden to return other values relative to this value.
	 * 
	 * @return		the relative width of the column (default 100).
	 */
	public int getDefaultWidth() {
		return 100;
	}
	
	
	/**
	 * Returns the exact width of this column.  If the return value is positive,
	 * both the minimum and maximum widths of this column are set to this value
	 * 
	 * @return		the absolute exact width of the column (default 0).
	 */
	public int getExactWidth() {
		return 0;
	}
	
	
	/**
	 * Return the column type class.  This is necessary for example for numerical
	 * sorting of Value objects, showing booleans as checkboxes etc.
	 * 
	 * @return	the object class of this column, by default <code>Object.class</code>.
	 */
	public Class<?> getColumnClass() {
		return Object.class;
	}
	
	/**
	 * Return the value in this column at the specified row.
	 * 
	 * @param row	the row of the data.
	 * @return		the value at the specified position.
	 */
	public abstract Object getValueAt(int row);

	/**
	 * Set a value in the table.
	 * 
	 * Override if the cell is editable.
	 * 
	 * @param row
	 * @param value
	 */
	public void setValueAt(int row, Object value ) {
	}
	
	/**
	 * Get the Comparator to use with this column.
	 * 
	 * If null, the assumption is the default comparator will be used for this column
	 * 
	 * @return
	 */
	public Comparator getComparator() {
		return null;
	}

	public String getToolTip() {
		return toolTip;
	}
	
}
