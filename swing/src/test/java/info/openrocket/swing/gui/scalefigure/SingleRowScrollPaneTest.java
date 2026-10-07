package info.openrocket.swing.gui.scalefigure;

import info.openrocket.swing.util.BaseTestCase;
import net.miginfocom.swing.MigLayout;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleRowScrollPaneTest extends BaseTestCase {

	@Test
	void narrowViewportShowsHorizontalScrollbarWithoutClippingControls() {
		JPanel footer = createFooter();
		SingleRowScrollPane scrollPane = new SingleRowScrollPane(footer, null);
		Dimension footerSize = footer.getPreferredSize();
		int scrollbarHeight = scrollPane.getHorizontalScrollBar().getPreferredSize().height;

		scrollPane.setSize(250, footerSize.height + scrollbarHeight);
		scrollPane.doLayout();

		assertTrue(scrollPane.getHorizontalScrollBar().isVisible());
		assertFalse(scrollPane.getVerticalScrollBar().isVisible());
		assertTrue(scrollPane.getViewport().getExtentSize().height >= footerSize.height,
				"The scrollbar must receive its own height instead of clipping the footer controls");
	}

	@Test
	void wideViewportDoesNotShowHorizontalScrollbar() {
		JPanel footer = createFooter();
		SingleRowScrollPane scrollPane = new SingleRowScrollPane(footer, null);
		Dimension footerSize = footer.getPreferredSize();

		scrollPane.setSize(footerSize.width + 20, footerSize.height);
		scrollPane.doLayout();

		assertFalse(scrollPane.getHorizontalScrollBar().isVisible());
	}

	private static JPanel createFooter() {
		JPanel footer = new JPanel(new MigLayout("fillx, gapy 0, ins 0"));
		footer.add(new JLabel("Click to select; shift-click to add; double-click to edit"),
				"growx, pushx");
		footer.add(new JButton("View settings"));
		footer.add(new JButton("Capture"));
		footer.add(new JCheckBox("Show warnings"));
		return footer;
	}
}
