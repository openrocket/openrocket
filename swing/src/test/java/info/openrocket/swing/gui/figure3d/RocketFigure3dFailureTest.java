package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
