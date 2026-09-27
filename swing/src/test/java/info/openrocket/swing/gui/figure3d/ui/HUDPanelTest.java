package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

	@Test
	void refreshesFromWorkerThreadsRunOnEdt() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(
				TestRockets.makeEstesAlphaIII());
		RocketInfo rocketInfo = mock(RocketInfo.class);
		List<Boolean> refreshedOnEdt = Collections.synchronizedList(new ArrayList<>());
		doAnswer(invocation -> refreshedOnEdt.add(SwingUtilities.isEventDispatchThread()))
				.when(rocketInfo).setCurrentConfig(any());
		HUDPanel panel = new HUDPanel(document, rocketInfo);

		// The GL thread attaches the scene, and simulation workers fire document events when they finish
		Thread glThread = new Thread(() -> panel.setSceneViewController(mock(Scene3DOrchestrator.class)));
		glThread.start();
		glThread.join();
		Thread worker = new Thread(() -> document.fireDocumentChangeEvent(new DocumentChangeEvent(document)));
		worker.start();
		worker.join();
		SwingUtilities.invokeAndWait(() -> { });

		assertEquals(List.of(true, true), refreshedOnEdt);
	}
}
