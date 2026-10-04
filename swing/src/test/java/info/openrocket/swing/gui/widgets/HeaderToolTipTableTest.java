package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.MouseEvent;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.junit.jupiter.api.Test;

public class HeaderToolTipTableTest {
	@Test
	public void testHeaderTooltipFollowsTheModelColumnWhenColumnsAreReordered() {
		HeaderToolTipTable table = new HeaderToolTipTable(
				new DefaultTableModel(new Object[0][0], new Object[] { "First", "Second" }),
				column -> column == 0 ? "First tooltip" : "Second tooltip");
		JTableHeader header = table.getTableHeader();
		header.getColumnModel().getColumn(0).setWidth(75);
		header.getColumnModel().getColumn(1).setWidth(75);

		assertEquals("First tooltip", tooltipAt(header, 10));
		header.getColumnModel().moveColumn(0, 1);
		assertEquals("Second tooltip", tooltipAt(header, 10));
	}

	private static String tooltipAt(JTableHeader header, int x) {
		MouseEvent event = new MouseEvent(header, MouseEvent.MOUSE_MOVED, 0, 0, x, 5, 0, false);
		return header.getToolTipText(event);
	}
}
