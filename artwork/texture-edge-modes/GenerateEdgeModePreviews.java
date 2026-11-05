import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Generates the texture edge mode example images shown when hovering the edge mode options in the
 * appearance panel. Run from the repository root:
 *
 *   java artwork/texture-edge-modes/GenerateEdgeModePreviews.java
 *
 * Writes {repeat,mirror,clamp,sticker}.png and their @2x versions to
 * swing/src/main/resources/pix/edgemodes/.
 */
public class GenerateEdgeModePreviews {
	private static final String OUTPUT_DIR = "swing/src/main/resources/pix/edgemodes";

	// Sizes at 1x; the @2x images double every dimension
	private static final int WIDTH = 240;
	private static final int HEIGHT = 140;
	private static final int TILE = 80;
	/** Position of the texture's own [0,1] UV square within the example surface. */
	private static final int TILE_X = (WIDTH - TILE) / 2;
	private static final int TILE_Y = (HEIGHT - TILE) / 2;
	/** The example texture is a GRID x GRID UV test grid of labelled cells. */
	private static final int GRID = 5;

	/**
	 * Pastel column colours, red to violet. The labels carry the information, so telling these
	 * apart is not needed to read the image; their lightness also differs between neighbours.
	 */
	private static final Color[] COLUMN_COLORS = {
			new Color(255, 155, 155), new Color(255, 236, 140), new Color(150, 225, 150),
			new Color(130, 170, 255), new Color(195, 150, 240)};

	/** Border marking the texture's own position. */
	private static final Color BORDER_COLOR = new Color(230, 0, 0);

	/** Component paint, shown outside a sticker. Neutral so it cannot be mistaken for texture. */
	private static final Color PAINT = new Color(200, 200, 200);

	private enum Mode { REPEAT, MIRROR, CLAMP, STICKER }

	public static void main(String[] args) throws IOException {
		new File(OUTPUT_DIR).mkdirs();
		for (Mode mode : Mode.values()) {
			String name = mode.name().toLowerCase();
			ImageIO.write(render(mode, 1), "png", new File(OUTPUT_DIR, name + ".png"));
			ImageIO.write(render(mode, 2), "png", new File(OUTPUT_DIR, name + "@2x.png"));
		}
	}

	private static BufferedImage render(Mode mode, int scale) {
		int tile = TILE * scale;
		BufferedImage texture = createTexture(tile);
		BufferedImage image = new BufferedImage(WIDTH * scale, HEIGHT * scale, BufferedImage.TYPE_INT_RGB);
		int tileX = TILE_X * scale;
		int tileY = TILE_Y * scale;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int u = x - tileX;
				int v = y - tileY;
				if (mode == Mode.STICKER && (u < 0 || u >= tile || v < 0 || v >= tile)) {
					image.setRGB(x, y, PAINT.getRGB());
				} else {
					image.setRGB(x, y, texture.getRGB(wrap(u, tile, mode), wrap(v, tile, mode)));
				}
			}
		}

		// Frame the texture's own position, so what the mode adds around it is clear. The red
		// border is edged in black so it stays distinct from the pastel cells, including red ones.
		Graphics2D g = image.createGraphics();
		int border = 2 * scale;
		g.setColor(Color.BLACK);
		g.fillRect(tileX - border - scale, tileY - border - scale, tile + 2 * (border + scale), border + 2 * scale);
		g.fillRect(tileX - border - scale, tileY + tile - scale, tile + 2 * (border + scale), border + 2 * scale);
		g.fillRect(tileX - border - scale, tileY - border - scale, border + 2 * scale, tile + 2 * (border + scale));
		g.fillRect(tileX + tile - scale, tileY - border - scale, border + 2 * scale, tile + 2 * (border + scale));
		g.setColor(BORDER_COLOR);
		g.fillRect(tileX - border, tileY - border, tile + 2 * border, border);
		g.fillRect(tileX - border, tileY + tile, tile + 2 * border, border);
		g.fillRect(tileX - border, tileY - border, border, tile + 2 * border);
		g.fillRect(tileX + tile, tileY - border, border, tile + 2 * border);
		g.dispose();
		return image;
	}

	private static int wrap(int t, int size, Mode mode) {
		return switch (mode) {
			case REPEAT -> Math.floorMod(t, size);
			case MIRROR -> {
				int period = Math.floorMod(t, 2 * size);
				yield period < size ? period : 2 * size - 1 - period;
			}
			case CLAMP, STICKER -> Math.min(Math.max(t, 0), size - 1);
		};
	}

	/**
	 * A UV test grid: pastel columns, with the cells of each column numbered 1 to GRID from the
	 * top. The numbers show the orientation regardless of colour: mirrored copies show reversed
	 * digits and reversed counting order, clamped edges show as streaks of the edge cells. Lines
	 * are kept off the outer border so clamping streaks the cell colours rather than a border line.
	 */
	private static BufferedImage createTexture(int size) {
		BufferedImage texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = texture.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		float cell = (float) size / GRID;

		for (int col = 0; col < GRID; col++) {
			g.setColor(COLUMN_COLORS[col]);
			g.fill(new Rectangle2D.Float(col * cell, 0, cell, size));
		}

		float scale = size / (float) TILE;
		g.setColor(new Color(0, 0, 0, 150));
		g.setStroke(new BasicStroke(0.8f * scale));
		for (int i = 1; i < GRID; i++) {
			float pos = i * cell;
			g.draw(new Line2D.Float(pos, 0, pos, size));
			g.draw(new Line2D.Float(0, pos, size, pos));
		}

		Font font = new Font(Font.SANS_SERIF, Font.BOLD, Math.round(cell * 0.62f));
		g.setStroke(new BasicStroke(1.4f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int row = 0; row < GRID; row++) {
			for (int col = 0; col < GRID; col++) {
				drawLabel(g, font, Integer.toString(row + 1), (col + 0.5f) * cell, (row + 0.5f) * cell);
			}
		}
		g.dispose();
		return texture;
	}

	/** Draws white text with a black outline, centred on (centerX, centerY). */
	private static void drawLabel(Graphics2D g, Font font, String text, float centerX, float centerY) {
		TextLayout layout = new TextLayout(text, font, g.getFontRenderContext());
		Rectangle2D bounds = layout.getBounds();
		AffineTransform position = AffineTransform.getTranslateInstance(
				centerX - bounds.getCenterX(), centerY - bounds.getCenterY());
		Shape outline = layout.getOutline(position);
		g.setColor(Color.BLACK);
		g.draw(outline);
		g.setColor(Color.WHITE);
		g.fill(outline);
	}
}
