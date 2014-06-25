package net.sf.openrocket.gui.adaptors;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

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
	
}
