package info.openrocket.swing.gui.figureelements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests screen-space positioning of horizontal caliper handles and labels.
 */
class HorizontalCaliperLineTest extends BaseTestCase {

	@Test
	void handleTracksLeftEdgeOfShiftedViewport() {
		HorizontalCaliperLine line = new HorizontalCaliperLine(200.0);
		AffineTransform transform = new AffineTransform();
		Rectangle initialViewport = new Rectangle(0, 0, 800, 600);
		Rectangle shiftedViewport = new Rectangle(125, 0, 800, 600);

		Rectangle2D.Double initialBounds = line.getHandleBounds(transform, initialViewport);
		Rectangle2D.Double shiftedBounds = line.getHandleBounds(transform, shiftedViewport);

		assertEquals(shiftedViewport.x - initialViewport.x, shiftedBounds.x - initialBounds.x, 0.0,
				"The handle must remain anchored to the viewport's left edge");
		assertEquals(initialBounds.y, shiftedBounds.y, 0.0,
				"Horizontal scrolling must not change the handle's Y position");
	}

	@Test
	void dragTooltipStaysInsideLeftEdgeOfViewport() {
		HorizontalCaliperLine line = new HorizontalCaliperLine(100.0);
		line.setDragPositionLabel("454 in");
		Rectangle viewport = new Rectangle(75, 0, 250, 400);
		BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			Rectangle2D.Double bounds = line.getDragTooltipBounds(graphics, viewport.x + 10, 100, viewport);

			assertTrue(bounds.getMinX() >= viewport.getMinX(),
					"The tooltip must not extend beyond the viewport's left edge");
			assertTrue(bounds.getMaxX() <= viewport.getMaxX(),
					"The tooltip must not extend beyond the viewport's right edge");
		} finally {
			graphics.dispose();
		}
	}
}
