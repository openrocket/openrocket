package info.openrocket.swing.gui.adaptors;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class ColumnTable extends JTable {

	public ColumnTable( ColumnTableModel model ) {
		super(model);
	}

	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader( columnModel ) {
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				if ( index < 0 ) {
					return null;
				}
				int realIndex = columnModel.getColumn(index).getModelIndex();
				tip = ((ColumnTableModel) getModel()).getColumn(realIndex).getToolTip();
				return tip;
			}
		};
	}

	/**
	 * Get the maximum width needed by the column.
	 * @param colIndex the index of the column
	 * @return the maximum width needed by the column
	 */
	private int getMaxColumnWidth(int colIndex) {
		int maxWidth = 0;
		ColumnTableModel columnModel = (ColumnTableModel) getModel();
		Column column = columnModel.getColumn(colIndex);

		// Check header width if needed
		if (column.includeHeaderInAutoSize()) {
			TableColumn tableColumn = getColumnModel().getColumn(colIndex);
			TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
			if (headerRenderer == null) {
				headerRenderer = getTableHeader().getDefaultRenderer();
			}
			Component headerComp = headerRenderer.getTableCellRendererComponent(
					this, tableColumn.getHeaderValue(), false, false, 0, colIndex);
			maxWidth = headerComp.getPreferredSize().width;
		}

		// Check each row
		for (int row = 0; row < getRowCount(); row++) {
			TableCellRenderer renderer = getCellRenderer(row, colIndex);
			Component comp = prepareRenderer(renderer, row, colIndex);
			maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
		}

		return maxWidth;
	}

	/**
	 * Set the preferred width of each column to the maximum width needed by the
	 * column.
	 */
	public void setupAutoSizeColumns() {
		ColumnTableModel columnModel = (ColumnTableModel) getModel();

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			Column col = columnModel.getColumn(i);
			if (col.isAutoSize()) {
				TableColumn tableColumn = getColumnModel().getColumn(i);

				// Get max width needed
				int maxWidth = getMaxColumnWidth(i);

				// Add some padding
				maxWidth += 10;

				// Set the preferred width
				tableColumn.setPreferredWidth(maxWidth);
			}
		}
	}
	
}
