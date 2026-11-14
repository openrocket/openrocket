package info.openrocket.swing.gui.figure3d.thumbnail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class RocketThumbnailTest {
	/** Size of the render; the thumbnail is half as large. */
	private static final int N = 400;

	private static final int GREY = 0xC0C0C0;
	private static final int OUTLINE = 0x4D4D4D;	// Grey, darkened by 60%

	@Test
	void renderIsAveragedDownOntoWhiteWithAnOutlinedSilhouette() {
		BufferedImage color = layer();
		BufferedImage ids = layer();
		fill(color, new Color(GREY), 100, 300);
		fill(ids, new Color(24, 8, 8), 100, 300);

		BufferedImage thumbnail = RocketThumbnail.compose(color, ids, new float[N * N]);

		assertNotNull(thumbnail);
		assertEquals(N / 2, thumbnail.getWidth());
		assertEquals(N / 2, thumbnail.getHeight());
		assertEquals(0xFFFFFF, rgb(thumbnail, 10, 100), "background should be white");
		assertEquals(GREY, rgb(thumbnail, 100, 100), "the inside of a part keeps its colour");
		assertEquals(OUTLINE, rgb(thumbnail, 50, 100), "the silhouette should be outlined");
		assertEquals(OUTLINE, rgb(thumbnail, 149, 100), "the silhouette should be outlined");
	}

	@Test
	void partsOfTheSameColorAreOutlinedWhereTheyMeet() {
		BufferedImage color = layer();
		BufferedImage ids = layer();
		fill(color, new Color(GREY), 100, 300);
		fill(ids, new Color(24, 8, 8), 100, 200);
		fill(ids, new Color(40, 8, 8), 200, 300);

		BufferedImage thumbnail = RocketThumbnail.compose(color, ids, new float[N * N]);

		// Only one of the two parts gets the line: the one with the lower id.
		assertEquals(OUTLINE, rgb(thumbnail, 99, 100), "parts should be outlined where they meet");
		assertEquals(GREY, rgb(thumbnail, 101, 100), "the line should be on one part only");
		assertEquals(GREY, rgb(thumbnail, 75, 100));
	}

	@Test
	void whereOnePartIsInFrontTheLineIsOnThatPart() {
		BufferedImage color = layer();
		BufferedImage ids = layer();
		fill(color, new Color(GREY), 100, 300);
		fill(ids, new Color(24, 8, 8), 100, 200);
		fill(ids, new Color(40, 8, 8), 200, 300);
		// The right part, with the higher id, is far in front of the left one.
		float[] depth = new float[N * N];
		for (int y = 0; y < N; y++) {
			for (int x = 0; x < 200; x++) {
				depth[y * N + x] = 100;
			}
		}

		BufferedImage thumbnail = RocketThumbnail.compose(color, ids, depth);

		assertEquals(OUTLINE, rgb(thumbnail, 100, 100), "the part in front should get the line");
		assertEquals(GREY, rgb(thumbnail, 99, 100), "the part behind should not get the line");
	}

	@Test
	void emptyRenderHasNoThumbnail() {
		assertNull(RocketThumbnail.compose(layer(), layer(), new float[N * N]));
	}

	private static BufferedImage layer() {
		return new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
	}

	/** Fills a horizontal band in the middle of the layer between two x coordinates. */
	private static void fill(BufferedImage image, Color color, int fromX, int toX) {
		Graphics2D g2 = image.createGraphics();
		g2.setColor(color);
		g2.fillRect(fromX, 150, toX - fromX, 100);
		g2.dispose();
	}

	private static int rgb(BufferedImage image, int x, int y) {
		return image.getRGB(x, y) & 0xFFFFFF;
	}
}
