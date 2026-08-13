package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.gui.figure3d.ui.HUDPanel;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketFigure3dTest extends BaseTestCase {

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

	private static HUDPanel getHudPanel(RocketFigure3d figure) {
		try {
			Field field = RocketFigure3d.class.getDeclaredField("hudPanel");
			field.setAccessible(true);
			return (HUDPanel) field.get(figure);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not inspect the 3D HUD", e);
		}
	}
}
