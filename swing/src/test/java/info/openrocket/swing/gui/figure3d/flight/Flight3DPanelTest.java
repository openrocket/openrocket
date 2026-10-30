package info.openrocket.swing.gui.figure3d.flight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Flight3DPanelTest {
	@Test
	void pausedPanelRendersOnlyWhenMarkedDirty() {
		Flight3DPanel panel = new Flight3DPanel();

		assertTrue(panel.shouldRenderOnTick());
		assertFalse(panel.shouldRenderOnTick());

		panel.requestRenderNow();
		assertTrue(panel.shouldRenderOnTick());
		assertFalse(panel.shouldRenderOnTick());
	}
}
