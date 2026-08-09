package info.openrocket.swing.gui.figureelements;

import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests screen-space positioning of vertical caliper handles.
 */
class CaliperLineTest extends BaseTestCase {

	@Test
	void handleTracksTopOfShiftedViewport() {
		CaliperLine line = new CaliperLine(200.0);
		AffineTransform transform = new AffineTransform();
		Rectangle initialViewport = new Rectangle(0, 0, 800, 600);
		Rectangle shiftedViewport = new Rectangle(0, 125, 800, 600);

		Rectangle2D.Double initialBounds = line.getHandleBounds(transform, initialViewport);
		Rectangle2D.Double shiftedBounds = line.getHandleBounds(transform, shiftedViewport);

		assertEquals(initialBounds.x, shiftedBounds.x, 0.0,
				"Vertical scrolling must not change the handle's X position");
		assertEquals(shiftedViewport.y - initialViewport.y, shiftedBounds.y - initialBounds.y, 0.0,
				"The handle must remain anchored to the viewport's top edge");
	}

	@Test
	void paintsHandleInsideShiftedViewport() {
		int caliperX = 200;
		CaliperLine line = new CaliperLine(caliperX);
		Rectangle shiftedViewport = new Rectangle(0, 125, 400, 200);
		BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setClip(shiftedViewport);
			line.paint(graphics, 1.0, shiftedViewport);
		} finally {
			graphics.dispose();
		}

		boolean handlePainted = false;
		for (int y = shiftedViewport.y; y < shiftedViewport.y + 60 && !handlePainted; y++) {
			for (int x = caliperX - 30; x <= caliperX + 30; x++) {
				// Ignore the narrow vertical line and look for pixels belonging to the diamond handle.
				if (Math.abs(x - caliperX) > 4 && (image.getRGB(x, y) >>> 24) != 0) {
					handlePainted = true;
					break;
				}
			}
		}

		assertTrue(handlePainted, "The handle must be painted within the shifted viewport clip");
	}
}
