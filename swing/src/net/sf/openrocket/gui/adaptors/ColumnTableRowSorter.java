package net.sf.openrocket.gui.adaptors;

import java.util.Comparator;

import javax.swing.table.TableRowSorter;

public class ColumnTableRowSorter extends TableRowSorter {

	private final ColumnTableModel columnTableModel;
	
	public ColumnTableRowSorter(ColumnTableModel model) {
		super(model);
		this.columnTableModel = model;
	}

	@Override
	public Comparator getComparator(int column) {
		Comparator c = columnTableModel.getColumn(column).getComparator();
		return (c!= null) ? c : super.getComparator(column);
	}

	/*
	 * We have to override this function because the base class (TableRowSorter) calls
	 * super.getComparator( int ) to determine if string comparison is required.
	 * Since the super class is DefaultRowSorter, it determines incorrectly that we are defining
	 * our own sorter. 
	 * 
	 * (non-Javadoc)
	 * @see javax.swing.table.TableRowSorter#useToString(int)
	 */
	@Override
	protected boolean useToString(int column) {
		Comparator c = columnTableModel.getColumn(column).getComparator();
		return ( c != null ) ? false : super.useToString(column);
	}

}
