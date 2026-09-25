package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketFigure3dFailureTest extends BaseTestCase {

	@Test
	void replacesTheBlankDesignViewWithAnOpenGLFailureMessage() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketFigure3d figure = new RocketFigure3d(document);
			try {
				figure.showGLInitFailureUI(null);

				assertEquals("3d-unavailable", figure.getCanvasDebugState());
				assertEquals(1, figure.getComponentCount());
				List<String> labels = labelTexts(figure);
				assertTrue(labels.contains("[RocketFigure3d.glInitFailed.title]"));
				assertTrue(labels.contains("[RocketFigure3d.glInitFailed.detail]"));
			} finally {
				figure.cleanup();
			}
		});
	}

	@Test
	void switchingBackToThreeDReplacesACanvasThatFailedWhileRendering() throws Exception {
		AtomicReference<RocketFigure3d> figureReference = new AtomicReference<>();
		AtomicReference<GLScenePanel> failedPanelReference = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketFigure3d figure = new RocketFigure3d(document);
			GLScenePanel failedPanel = new GLScenePanel(document.getRocket(), null);
			setField(failedPanel, "glInitFailed", true);
			setField(failedPanel, "fatalRenderFailure", true);
			setField(figure, "glScenePanel", failedPanel);
			figure.add(failedPanel, BorderLayout.CENTER);
			figureReference.set(figure);
			failedPanelReference.set(failedPanel);

			figure.startRendering();
		});

		// startRendering() performs canvas lifecycle work in a deferred EDT task.
		SwingUtilities.invokeAndWait(() -> {
		});

		SwingUtilities.invokeAndWait(() -> {
			RocketFigure3d figure = figureReference.get();
			try {
				GLScenePanel replacement = getCanvas(figure);
				assertNotSame(failedPanelReference.get(), replacement,
						"Returning to 3D should discard a canvas that failed after initialization");
				assertFalse(replacement.hasFatalRenderFailure(),
						"Replacement canvas should begin with a clean render-failure state");
			} finally {
				figure.cleanup();
			}
		});
	}

	private static List<String> labelTexts(Container parent) {
		List<String> labels = new ArrayList<>();
		for (Component child : parent.getComponents()) {
			if (child instanceof JLabel label) {
				labels.add(label.getText());
			}
			if (child instanceof Container container) {
				labels.addAll(labelTexts(container));
			}
		}
		return labels;
	}

	private static GLScenePanel getCanvas(RocketFigure3d figure) {
		return (GLScenePanel) getField(figure, "glScenePanel");
	}

	private static Object getField(Object target, String name) {
		try {
			Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not inspect field " + name, e);
		}
	}

	private static void setField(Object target, String name, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not set field " + name, e);
		}
	}
}
