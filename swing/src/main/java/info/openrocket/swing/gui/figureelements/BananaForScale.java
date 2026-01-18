package info.openrocket.swing.gui.figureelements;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.DomElement;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import info.openrocket.swing.gui.scalefigure.AbstractScaleFigure;
import info.openrocket.swing.gui.theme.UITheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.net.URL;

public class BananaForScale implements FigureElement {
	private static final Logger log = LoggerFactory.getLogger(BananaForScale.class);

	private static final double BANANA_LENGTH_M = 0.20; // Standard Cavendish
	private static final int MARGIN_PX = 15;

	// Cache the parsed SVG so we don't reload it every frame
	private static SVGDocument cachedBananaSvg;
	private static boolean loadAttempted = false;

	private final AbstractScaleFigure figure;

	public BananaForScale(AbstractScaleFigure figure) {
		this.figure = figure;
	}

	/**
	 * Lazy loads the SVG resource.
	 */
	private SVGDocument getBananaSVG() {
		if (!loadAttempted && cachedBananaSvg == null) {
			loadAttempted = true;
			try {
				// Ensure you have a banana.svg in your resources folder at this path
				URL url = getClass().getResource("/pix/other/banana.svg");
				if (url != null) {
					// 1. Get the OR Color and convert to Hex (#RRGGBB)
					Color orYellow = UITheme.getColor("OR.colors.yellow", new Color(255, 225, 50));
					String hexColor = String.format("#%02x%02x%02x",
							orYellow.getRed(), orYellow.getGreen(), orYellow.getBlue());

					// 2. Load with the processor
					SVGLoader loader = new SVGLoader();
					cachedBananaSvg = loader.load(url, LoaderContext.builder()
							.preProcessor((root) -> {
								// Start the recursion manually
								applyColorRecursively(root, "banana", hexColor);
							})
							.build()
					);
				} else {
					log.debug("BananaForScale: Could not find banana.svg resource.");
				}
			} catch (Exception e) {
				log.debug(e.toString());
			}
		}
		return cachedBananaSvg;
	}

	@Override
	public void paint(Graphics2D g2, double scale) {
		// Only intended for absolute overlays.
	}

	@Override
	public void paint(Graphics2D g2, double ignoredScale, Rectangle visible) {
		if (figure == null || visible == null) {
			return;
		}

		double pxPerM = figure.getAbsoluteScale();
		if (Double.isNaN(pxPerM) || Double.isInfinite(pxPerM) || pxPerM <= 0) {
			return;
		}

		SVGDocument svg = getBananaSVG();
		if (svg == null) {
			return;
		}

		// Save original state
		AffineTransform oldTransform = g2.getTransform();
		RenderingHints oldHints = g2.getRenderingHints();

		try {
			// Enable Anti-aliasing for best SVG rendering results
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			// --- Calculation ---

			// 1. Determine Target Size in Pixels
			double targetWidthPx = BANANA_LENGTH_M * pxPerM;

			// 2. Get SVG Intrinsic Size
			// Note: size() returns float dimensions defined in the SVG file header
			float svgWidth = svg.size().width;
			float svgHeight = svg.size().height;

			if (svgWidth == 0 || svgHeight == 0) return;

			// 3. Calculate Scale Factor to map SVG Width -> Target Pixel Width
			double scaleFactor = targetWidthPx / svgWidth;

			// 4. Calculate final dimensions for positioning
			double renderWidth = svgWidth * scaleFactor;
			double renderHeight = svgHeight * scaleFactor;

			// 5. Calculate Position (Bottom Center of visible area)
			double cx = visible.getCenterX();
			double cy = visible.getMaxY() - MARGIN_PX;

			double xPos = cx - (renderWidth / 2.0);
			double yPos = cy - renderHeight;

			// --- Rendering ---
			g2.setColor(Color.RED);

			// Move to position
			g2.translate(xPos, yPos);

			// Scale the coordinate system so the SVG draws at the desired size
			g2.scale(scaleFactor, scaleFactor);

			// Render the SVG
			svg.render(null, g2);

		} finally {
			// Restore original state
			g2.setTransform(oldTransform);
			g2.setRenderingHints(oldHints);
		}
	}

	/**
	 * Recursively walks the SVG tree to find the element with the given ID.
	 */
	private void applyColorRecursively(DomElement element, String targetId, String hexColor) {
		// 1. Check current element
		String id = element.attribute("id");
		if (targetId.equals(id)) {
			element.setAttribute("fill", hexColor);
		}

		// 2. Walk children
		for (DomElement child : element.children()) {
			applyColorRecursively(child, targetId, hexColor);
		}
	}
}