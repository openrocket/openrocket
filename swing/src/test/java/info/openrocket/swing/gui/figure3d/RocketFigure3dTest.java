package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests theme-related refresh behavior of the 3D rocket figure.
 */
class RocketFigure3dTest extends BaseTestCase {

	@Test
	void refreshesCanvasAfterCurrentThemeUpdateCompletes() throws Exception {
		String originalDisable3D = System.getProperty("openrocket.3d.disable");
		System.setProperty("openrocket.3d.disable", "true");
		try {
			OpenRocketDocument document = mock(OpenRocketDocument.class);
			when(document.getRocket()).thenReturn(mock(Rocket.class));
			TrackingRocketFigure3d figure = new TrackingRocketFigure3d(document);
			figure.startTrackingRepaints();

			SwingUtilities.invokeAndWait(() -> {
				figure.refreshAfterThemeChange();
				assertEquals(0, figure.getTrackedRepaintCount(),
						"The canvas repaint must wait until the look-and-feel update has finished");
			});

			SwingUtilities.invokeAndWait(() -> {
				// Drain the event queue so the deferred repaint can run.
			});
			assertEquals(1, figure.getTrackedRepaintCount());
		} finally {
			if (originalDisable3D == null) {
				System.clearProperty("openrocket.3d.disable");
			} else {
				System.setProperty("openrocket.3d.disable", originalDisable3D);
			}
		}
	}

	/**
	 * Records repaints requested after construction without initializing OpenGL.
	 */
	private static class TrackingRocketFigure3d extends RocketFigure3d {
		private boolean trackRepaints;
		private int trackedRepaintCount;

		TrackingRocketFigure3d(OpenRocketDocument document) {
			super(document);
		}

		void startTrackingRepaints() {
			trackRepaints = true;
		}

		int getTrackedRepaintCount() {
			return trackedRepaintCount;
		}

		@Override
		public void repaint() {
			if (trackRepaints) {
				trackedRepaintCount++;
			}
			super.repaint();
		}
	}
}
