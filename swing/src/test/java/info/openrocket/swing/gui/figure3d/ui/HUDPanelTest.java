package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HUDPanelTest extends BaseTestCase {

	@Test
	void changingWarningVisibilityInvalidatesHud() {
		RocketInfo rocketInfo = mock(RocketInfo.class);
		HUDUpdateListener updateListener = mock(HUDUpdateListener.class);
		HUDPanel panel = new HUDPanel(mock(Rocket.class), rocketInfo);
		panel.setSceneViewController(null);
		panel.setGLScenePanel(updateListener);

		assertFalse(panel.needsRepaint());
		panel.setShowWarnings(false);

		verify(rocketInfo).setShowWarnings(false);
		verify(updateListener).markHudForUpdate();
		assertTrue(panel.needsRepaint());
	}
}
