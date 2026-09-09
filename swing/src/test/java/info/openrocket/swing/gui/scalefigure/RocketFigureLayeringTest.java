package info.openrocket.swing.gui.scalefigure;

import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figureelements.FigureElement;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RocketFigureLayeringTest extends BaseTestCase {

	@Test
	void screenSpaceInformationPaintsAfterRocketRelativeOverlays() {
		List<String> paintOrder = new ArrayList<>();
		RocketFigure figure = new RocketFigure(TestRockets.makeEstesAlphaIII());
		figure.setSize(800, 300);
		figure.addRelativeTopExtra(new RecordingElement("relative-overlay", paintOrder));
		figure.addAbsoluteExtra(new RecordingElement("screen-space-information", paintOrder));
		BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			figure.paint(graphics);
		} finally {
			graphics.dispose();
		}

		assertEquals(List.of("relative-overlay", "screen-space-information"), paintOrder);
	}

	/** Records when an overlay is painted so the layer contract can be tested without pixel matching. */
	private record RecordingElement(String name, List<String> paintOrder) implements FigureElement {
		@Override
		public void paint(Graphics2D graphics, double scale) {
			paintOrder.add(name);
		}

		@Override
		public void paint(Graphics2D graphics, double scale, Rectangle visible) {
			paintOrder.add(name);
		}
	}
}
