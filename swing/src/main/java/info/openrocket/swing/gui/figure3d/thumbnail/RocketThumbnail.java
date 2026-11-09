package info.openrocket.swing.gui.figure3d.thumbnail;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Builds the thumbnail image that is stored in design files for file icons (Windows Explorer, macOS Finder) from a 3D
 * side view of the rocket.
 * <p>
 * The rocket is turned 45 degrees (nose up-left) so that it fills more of the square image, which keeps it
 * recognizable when the operating system scales the thumbnail down to icon size.
 */
public final class RocketThumbnail {
	/** Width and height of the thumbnail image, in pixels. */
	public static final int DEFAULT_SIZE = 1024;

	private static final double ANGLE = Math.PI / 4;	// Nose up-left
	private static final double MARGIN = 0.06;			// Fraction of the image left empty on each side
	private static final Color BACKGROUND = Color.WHITE;

	private RocketThumbnail() {
	}

	/**
	 * Width of the side view to render for a thumbnail of the given size: enough that the rocket is not enlarged when
	 * it is turned to fit the diagonal.
	 */
	public static int sideViewWidth(int size) {
		return (int) Math.ceil(size * 1.5);
	}

	/** Height of the side view to render for a thumbnail of the given size. */
	public static int sideViewHeight(int size) {
		return Math.max(1, size / 2);
	}

	/**
	 * Turn a side view of the rocket, with the nose to the left on a transparent background, into a thumbnail: the
	 * rocket is cropped, turned 45 degrees and fitted on a white square.
	 *
	 * @param sideView the side view
	 * @param size     width and height of the thumbnail, in pixels
	 * @return the thumbnail, or null if the side view is empty
	 */
	public static BufferedImage fromSideView(BufferedImage sideView, int size) {
		Rectangle bounds = opaqueBounds(sideView);
		if (bounds == null) {
			return null;
		}

		// A w x h rectangle turned 45 degrees is (w + h) / sqrt(2) wide and high.
		double turnedExtent = (bounds.width + bounds.height) / Math.sqrt(2);
		double scale = size * (1 - 2 * MARGIN) / turnedExtent;

		BufferedImage thumbnail = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = thumbnail.createGraphics();
		try {
			g2.setColor(BACKGROUND);
			g2.fillRect(0, 0, size, size);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(size / 2.0, size / 2.0);
			g2.rotate(ANGLE);
			g2.scale(scale, scale);
			g2.translate(-bounds.getCenterX(), -bounds.getCenterY());
			g2.drawImage(sideView, 0, 0, null);
		} finally {
			g2.dispose();
		}
		return thumbnail;
	}

	/** Bounds of the pixels that are not fully transparent, or null if there are none. */
	private static Rectangle opaqueBounds(BufferedImage image) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}
		}
		return maxX < 0 ? null : new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}
}
