package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DesignPanelTest {

	@Test
	public void narrowTreeKeepsDefaultDividerLocation() {
		assertEquals(400, DesignPanel.getInitialDividerLocation(1000, 250));
	}

	@Test
	public void wideTreeWidensDividerToFitComponentNames() {
		assertEquals(620, DesignPanel.getInitialDividerLocation(1000, 620));
	}

	@Test
	public void veryWideTreeLeavesRoomForComponentButtons() {
		assertEquals(700, DesignPanel.getInitialDividerLocation(1000, 2000));
	}
}
