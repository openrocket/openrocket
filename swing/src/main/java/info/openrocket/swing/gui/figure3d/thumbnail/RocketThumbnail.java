package info.openrocket.swing.gui.figure3d.thumbnail;

import java.awt.image.BufferedImage;

/**
 * Builds the thumbnail image that is stored in design files for file icons (Windows Explorer, macOS Finder) from an
 * unlit render of the 3D view.
 * <p>
 * The rocket is drawn at 45 degrees (nose up-left) so that it fills more of the square image, and each part gets a
 * dark outline. Both keep it recognizable when the operating system scales the thumbnail down to icon size.
 */
public final class RocketThumbnail {
	/** Width and height of the thumbnail image, in pixels. */
	public static final int DEFAULT_SIZE = 1024;
	/** The render is this many times larger than the thumbnail along each axis, and averaged down. */
	public static final int SUPERSAMPLING = 2;
	/** Rotation of the rocket in the image, in radians: nose up-left. */
	public static final float ROLL = (float) (-Math.PI / 4);
	/** Fraction of the image left empty on each side. */
	public static final float MARGIN = 0.06f;

	private static final int BACKGROUND = 0xFFFFFF;
	private static final double OUTLINE_WIDTH = 1 / 170.0;	// Fraction of the image size
	private static final double OUTLINE_DARKENING = 0.6;
	/** Pixels with at least this alpha belong to the rocket. */
	private static final int OPAQUE_ALPHA = 128;

	private static final int[] DX = { 1, -1, 0, 0, 1, 1, -1, -1 };
	private static final int[] DY = { 0, 0, 1, -1, 1, -1, 1, -1 };

	private RocketThumbnail() {
	}

	/**
	 * Combine the layers of a render into the thumbnail: outline the parts, put the rocket on a white background and
	 * average the supersampled render down.
	 *
	 * @param color     the rocket without lighting on a transparent background, {@link #SUPERSAMPLING} times the size
	 *                  of the thumbnail
	 * @param objectIds the same view with each object opaque and in a flat colour of its own
	 * @param depth     distance from the camera of each pixel of {@code objectIds}, top row first, in pixels
	 * @return the thumbnail, or null if the render is empty
	 */
	public static BufferedImage compose(BufferedImage color, BufferedImage objectIds, float[] depth) {
		int n = color.getWidth();
		if (color.getHeight() != n || objectIds.getWidth() != n || objectIds.getHeight() != n
				|| depth.length != n * n || n % SUPERSAMPLING != 0) {
			throw new IllegalArgumentException("The layers must be square, of the same size and supersampled");
		}
		int[] colors = color.getRGB(0, 0, n, n, null, 0, n);
		int[] ids = objectIds.getRGB(0, 0, n, n, null, 0, n);

		boolean empty = true;
		for (int id : ids) {
			if (isRocket(id)) {
				empty = false;
				break;
			}
		}
		if (empty) {
			return null;
		}

		drawOutlines(colors, ids, depth, n);
		return downsample(colors, n);
	}

	/**
	 * Darken the pixels of each part near the silhouette of the rocket and near other parts behind it. Where two
	 * parts meet at about the same depth, only one of them gets the line.
	 */
	private static void drawOutlines(int[] colors, int[] ids, float[] depth, int n) {
		int outlineWidth = Math.max(2, (int) Math.round(n * OUTLINE_WIDTH));
		int[] distances = { outlineWidth / 2, outlineWidth };
		// Depth change that a smooth surface can have over the outline width
		float tolerance = 3f * outlineWidth;

		boolean[] outline = new boolean[n * n];
		for (int y = 0; y < n; y++) {
			for (int x = 0; x < n; x++) {
				int o = y * n + x;
				if (!isRocket(ids[o])) {
					continue;
				}
				int id = ids[o] & 0xFFFFFF;
				search:
				for (int d = 0; d < DX.length; d++) {
					for (int distance : distances) {
						int qx = x + DX[d] * distance, qy = y + DY[d] * distance;
						if (qx < 0 || qy < 0 || qx >= n || qy >= n) {
							outline[o] = true;
							break search;
						}
						int q = qy * n + qx;
						if (!isRocket(ids[q])) {
							outline[o] = true;
							break search;
						}
						int other = ids[q] & 0xFFFFFF;
						if (other == id) {
							continue;
						}
						float behind = depth[q] - depth[o];
						if (behind > tolerance || (Math.abs(behind) <= tolerance && other > id)) {
							outline[o] = true;
							break search;
						}
					}
				}
			}
		}
		for (int o = 0; o < outline.length; o++) {
			if (outline[o]) {
				int c = colors[o];
				double f = 1 - OUTLINE_DARKENING;
				// The line is opaque, also where the part is translucent.
				colors[o] = 0xFF000000 | ((int) Math.round(((c >> 16) & 0xFF) * f) << 16)
						| ((int) Math.round(((c >> 8) & 0xFF) * f) << 8) | (int) Math.round((c & 0xFF) * f);
			}
		}
	}

	/** Put the render on the background and average each block of supersamples into one pixel. */
	private static BufferedImage downsample(int[] colors, int n) {
		int size = n / SUPERSAMPLING;
		int samples = SUPERSAMPLING * SUPERSAMPLING;
		int[] pixels = new int[size * size];
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int r = 0, g = 0, b = 0;
				for (int sy = 0; sy < SUPERSAMPLING; sy++) {
					int offset = (y * SUPERSAMPLING + sy) * n + x * SUPERSAMPLING;
					for (int sx = 0; sx < SUPERSAMPLING; sx++) {
						int c = colors[offset + sx];
						int alpha = c >>> 24;
						r += over((c >> 16) & 0xFF, (BACKGROUND >> 16) & 0xFF, alpha);
						g += over((c >> 8) & 0xFF, (BACKGROUND >> 8) & 0xFF, alpha);
						b += over(c & 0xFF, BACKGROUND & 0xFF, alpha);
					}
				}
				pixels[y * size + x] = ((r / samples) << 16) | ((g / samples) << 8) | (b / samples);
			}
		}
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, size, size, pixels, 0, size);
		return image;
	}

	private static int over(int value, int background, int alpha) {
		return (value * alpha + background * (255 - alpha)) / 255;
	}

	private static boolean isRocket(int argb) {
		return (argb >>> 24) >= OPAQUE_ALPHA;
	}
}
