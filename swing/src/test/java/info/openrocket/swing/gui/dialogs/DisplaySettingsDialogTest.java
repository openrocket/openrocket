package info.openrocket.swing.gui.dialogs;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.preferences.DocumentPreferences;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplaySettingsDialogTest extends BaseTestCase {

	@Test
	void windowCloseRevertsPreviewWithoutDirtyingSavedDocument() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Headless environment cannot construct Swing dialogs");

		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketPanel rocketPanel = new RocketPanel(document);
			DisplaySettingsDialog dialog = new DisplaySettingsDialog(null, rocketPanel);
			try {
				DocumentPreferences preferences = document.getDocumentPreferences();
				preferences.putColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, Color.BLUE);
				assertFalse(document.isSaved());

				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));

				assertNull(preferences.getColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, null));
				assertTrue(document.isSaved());
				assertFalse(dialog.isDisplayable());
			} finally {
				dialog.dispose();
				rocketPanel.getFigure3d().cleanup();
			}
		});
	}
}
