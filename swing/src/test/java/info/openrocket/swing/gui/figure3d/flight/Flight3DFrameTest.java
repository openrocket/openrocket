package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Flight3DFrameTest extends BaseTestCase {

	@Test
	void disposeDetachesListenerFromOwnerWindow() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Headless environment cannot construct Swing windows");

		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			JFrame owner = new JFrame();
			int originalListenerCount = owner.getWindowListeners().length;
			Flight3DFrame frame = new Flight3DFrame(document, null, owner);
			try {
				assertEquals(originalListenerCount + 1, owner.getWindowListeners().length);
				frame.dispose();
				assertEquals(originalListenerCount, owner.getWindowListeners().length);
			} finally {
				frame.dispose();
				owner.dispose();
			}
		});
	}
}
