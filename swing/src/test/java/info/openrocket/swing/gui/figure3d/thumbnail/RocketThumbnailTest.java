package info.openrocket.swing.gui.figure3d.thumbnail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.IntPredicate;

import org.junit.jupiter.api.Test;

class RocketThumbnailTest {
	private static final int SIZE = 256;

	private static final IntPredicate IS_RED = rgb -> r(rgb) > 150 && g(rgb) < 90 && b(rgb) < 90;
	private static final IntPredicate IS_BLUE = rgb -> b(rgb) > 150 && r(rgb) < 90 && g(rgb) < 90;

	@Test
	void sideViewIsTurnedWithNoseUpLeftOnAWhiteSquare() {
		BufferedImage thumbnail = RocketThumbnail.fromSideView(sideView(), SIZE);

		assertNotNull(thumbnail);
		assertEquals(SIZE, thumbnail.getWidth());
		assertEquals(SIZE, thumbnail.getHeight());
		double[] nose = centroid(thumbnail, IS_RED);
		double[] tail = centroid(thumbnail, IS_BLUE);
		assertTrue(nose[0] < SIZE / 2.0 && nose[1] < SIZE / 2.0, "nose should be in the top-left quadrant");
		assertTrue(tail[0] > SIZE / 2.0 && tail[1] > SIZE / 2.0, "tail should be in the bottom-right quadrant");
		assertEquals(Color.WHITE.getRGB(), thumbnail.getRGB(0, 0), "background should be white");
		assertEquals(Color.WHITE.getRGB(), thumbnail.getRGB(SIZE - 1, 0), "the rocket should not fill the corners");
	}

	@Test
	void rocketIsCroppedAndFittedRegardlessOfItsPositionInTheSideView() {
		// The same rocket, but in a much larger side view with lots of empty space around it.
		BufferedImage padded = new BufferedImage(1200, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = padded.createGraphics();
		g2.drawImage(sideView(), 700, 300, null);
		g2.dispose();

		BufferedImage thumbnail = RocketThumbnail.fromSideView(padded, SIZE);

		double[] nose = centroid(thumbnail, IS_RED);
		double[] tail = centroid(thumbnail, IS_BLUE);
		// Centered: the nose and tail are point-symmetric about the image center.
		assertEquals(SIZE - nose[0], tail[0], 3);
		assertEquals(SIZE - nose[1], tail[1], 3);
		// Fitted: the rocket spans most of the diagonal.
		assertTrue(tail[0] - nose[0] > SIZE * 0.5, "the rocket should fill the thumbnail");
	}

	@Test
	void emptySideViewHasNoThumbnail() {
		assertNull(RocketThumbnail.fromSideView(new BufferedImage(300, 100, BufferedImage.TYPE_INT_ARGB), SIZE));
	}

	/** A 300x30 "rocket" on a transparent background: red nose on the left, blue tail on the right. */
	private static BufferedImage sideView() {
		BufferedImage image = new BufferedImage(300, 30, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, 60, 30);
		g2.setColor(Color.GRAY);
		g2.fillRect(60, 0, 180, 30);
		g2.setColor(Color.BLUE);
		g2.fillRect(240, 0, 60, 30);
		g2.dispose();
		return image;
	}

	private static double[] centroid(BufferedImage image, IntPredicate matches) {
		double sumX = 0, sumY = 0;
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (matches.test(image.getRGB(x, y))) {
					sumX += x;
					sumY += y;
					count++;
				}
			}
		}
		assertTrue(count > 0, "expected colour not found in thumbnail");
		return new double[] { sumX / count, sumY / count };
	}

	private static int r(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	private static int g(int rgb) {
		return (rgb >> 8) & 0xFF;
	}

	private static int b(int rgb) {
		return rgb & 0xFF;
	}
}
