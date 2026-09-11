package info.openrocket.swing.gui.figure3d.photo;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoFrameTest extends BaseTestCase {

	@Test
	void disposeDetachesListenerFromOwnerWindow() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Headless environment cannot construct Swing windows");

		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			JFrame owner = new JFrame();
			int originalListenerCount = owner.getWindowListeners().length;
			PhotoFrame frame = new PhotoFrame(document, owner);
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
