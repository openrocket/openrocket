package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

	@Test
	void rocketChangeRefreshesOncePerDocumentChange() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(
				TestRockets.makeEstesAlphaIII());
		RocketInfo rocketInfo = mock(RocketInfo.class);
		AtomicInteger refreshes = countRefreshes(rocketInfo);
		HUDPanel panel = new HUDPanel(document, rocketInfo);
		SwingUtilities.invokeAndWait(() -> panel.setSceneViewController(mock(Scene3DOrchestrator.class)));
		refreshes.set(0);
		AtomicInteger documentChanges = new AtomicInteger();
		document.addDocumentChangeListener(event -> documentChanges.incrementAndGet());

		BodyTube bodyTube = findBodyTube(document.getRocket());
		SwingUtilities.invokeAndWait(() -> bodyTube.setLength(bodyTube.getLength() * 1.1));

		assertTrue(documentChanges.get() > 0);
		assertEquals(documentChanges.get(), refreshes.get());
	}

	@Test
	void hiddenHudRefreshesOnlyWhenShownAgain() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(
				TestRockets.makeEstesAlphaIII());
		RocketInfo rocketInfo = mock(RocketInfo.class);
		AtomicInteger refreshes = countRefreshes(rocketInfo);
		HUDPanel panel = new HUDPanel(document, rocketInfo);
		SwingUtilities.invokeAndWait(() -> panel.setSceneViewController(mock(Scene3DOrchestrator.class)));
		refreshes.set(0);

		BodyTube bodyTube = findBodyTube(document.getRocket());
		SwingUtilities.invokeAndWait(() -> {
			panel.setActive(false);
			bodyTube.setLength(bodyTube.getLength() * 1.1);
			bodyTube.setLength(bodyTube.getLength() * 1.1);
		});
		assertEquals(0, refreshes.get());

		SwingUtilities.invokeAndWait(() -> panel.setActive(true));
		assertEquals(1, refreshes.get());
		assertTrue(panel.needsRepaint());

		// Showing the HUD again without intermediate changes needs no refresh
		SwingUtilities.invokeAndWait(() -> {
			panel.setActive(false);
			panel.setActive(true);
		});
		assertEquals(1, refreshes.get());
	}

	private static AtomicInteger countRefreshes(RocketInfo rocketInfo) {
		AtomicInteger refreshes = new AtomicInteger();
		doAnswer(invocation -> refreshes.incrementAndGet()).when(rocketInfo).setCurrentConfig(any());
		return refreshes;
	}

	private static BodyTube findBodyTube(Rocket rocket) {
		for (RocketComponent component : rocket) {
			if (component instanceof BodyTube bodyTube) {
				return bodyTube;
			}
		}
		assertNotNull(null, "Test rocket has no body tube");
		return null;
	}
}
