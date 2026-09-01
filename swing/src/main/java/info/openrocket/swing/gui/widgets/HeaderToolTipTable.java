package info.openrocket.swing.gui.widgets;

import java.awt.event.MouseEvent;
import java.util.function.IntFunction;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/** A table whose column-header tooltips are supplied by model-column index. */
@SuppressWarnings("serial")
public class HeaderToolTipTable extends JTable {
	private final IntFunction<String> headerToolTipProvider;

	public HeaderToolTipTable(TableModel model, IntFunction<String> headerToolTipProvider) {
		super(model);
		this.headerToolTipProvider = headerToolTipProvider;
	}

	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			@Override
			public String getToolTipText(MouseEvent event) {
				int viewColumn = columnModel.getColumnIndexAtX(event.getX());
				if (viewColumn < 0 || headerToolTipProvider == null) {
					return null;
				}
				int modelColumn = columnModel.getColumn(viewColumn).getModelIndex();
				return headerToolTipProvider.apply(modelColumn);
			}
		};
	}
}
