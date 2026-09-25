package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.ui.HUDPanel;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RocketFigure3dTest extends BaseTestCase {
	private static final long EDT_CALLBACK_TIMEOUT_SECONDS = 5;
	private static final long NON_BLOCKING_TIMEOUT_SECONDS = 2;

	@Test
	void hidingCaretsKeepsHudTextVisibleForImageCapture() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketFigure3d figure = new RocketFigure3d(document);
			try {
				figure.setDrawCarets(false);

				assertTrue(getHudPanel(figure).isVisible(),
						"The caret toggle must not hide the HUD text composited into captured images");
			} finally {
				figure.cleanup();
			}
		});
	}

	@Test
	void deliversZoomChangeNotificationsOnTheEventDispatchThread() throws Exception {
		AtomicReference<RocketFigure3d> figureReference = new AtomicReference<>();
		CountDownLatch notified = new CountDownLatch(1);
		AtomicBoolean notifiedOnEdt = new AtomicBoolean();

		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketFigure3d figure = new RocketFigure3d(document);
			figure.addChangeListener(event -> {
				notifiedOnEdt.set(SwingUtilities.isEventDispatchThread());
				notified.countDown();
			});
			figureReference.set(figure);
		});

		try {
			invokeFireChangeEvent(figureReference.get());
			assertTrue(notified.await(EDT_CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS),
					"Zoom change notification should reach the EDT");
			assertTrue(notifiedOnEdt.get(), "Zoom change listeners may update Swing components");
		} finally {
			SwingUtilities.invokeAndWait(figureReference.get()::cleanup);
		}
	}

	@Test
	void schedulingContextResetRecoveryDoesNotWaitForTheEventDispatchThread() throws Exception {
		AtomicReference<RocketFigure3d> figureReference = new AtomicReference<>();
		AtomicReference<GLScenePanel> panelReference = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			figureReference.set(new RocketFigure3d(document));
			panelReference.set(new GLScenePanel(document.getRocket(), null));
		});

		RocketFigure3d figure = figureReference.get();
		GLScenePanel panel = panelReference.get();
		figure.requestContextResetRebuild(panel);

		CountDownLatch edtBlocked = new CountDownLatch(1);
		CountDownLatch releaseEdt = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> {
			edtBlocked.countDown();
			try {
				releaseEdt.await(EDT_CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		assertTrue(edtBlocked.await(EDT_CALLBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS),
				"EDT blocker should start");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Boolean> recoveryScheduled = executor.submit(() -> figure.processPendingContextResetRebuild(panel));
		try {
			assertTrue(recoveryScheduled.get(NON_BLOCKING_TIMEOUT_SECONDS, TimeUnit.SECONDS),
					"Context reset should be accepted without synchronously entering the EDT");
		} catch (TimeoutException e) {
			fail("Render thread waited for the EDT while scheduling context reset recovery", e);
		} finally {
			releaseEdt.countDown();
			executor.shutdownNow();
			SwingUtilities.invokeAndWait(() -> {
				panel.cleanup();
				figure.cleanup();
			});
		}
	}

	private static HUDPanel getHudPanel(RocketFigure3d figure) {
		try {
			Field field = RocketFigure3d.class.getDeclaredField("hudPanel");
			field.setAccessible(true);
			return (HUDPanel) field.get(figure);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not inspect the 3D HUD", e);
		}
	}

	private static void invokeFireChangeEvent(RocketFigure3d figure) {
		try {
			Method method = RocketFigure3d.class.getDeclaredMethod("fireChangeEvent");
			method.setAccessible(true);
			method.invoke(figure);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not fire a 3D zoom change event", e);
		}
	}
}
