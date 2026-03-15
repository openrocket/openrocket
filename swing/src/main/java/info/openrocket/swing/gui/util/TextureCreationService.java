package info.openrocket.swing.gui.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.util.CoordinateIF;

/**
 * Helper that can generate blank decal textures sized to a component and optionally render
 * outline guides for fins.
 */
public class TextureCreationService {

	private static final double METERS_PER_INCH = 0.0254d;
	private static final int SAMPLING_STEPS = 128;

	public TextureGenerationResult generateTextureImage(RocketComponent component, boolean insideSurface,
														double dpi) throws TextureGenerationException {
		return generateTextureImage(component, insideSurface, dpi, true);
	}

	public TextureGenerationResult generateTextureImage(RocketComponent component, boolean insideSurface,
														double dpi, boolean drawFinOutline) throws TextureGenerationException {
		return generateTextureImage(component, insideSurface, dpi, drawFinOutline, -1f);
	}

	public TextureGenerationResult generateTextureImage(RocketComponent component, boolean insideSurface,
														double dpi, boolean drawFinOutline, float outlineWidthPx) throws TextureGenerationException {
		return generateTextureImage(component, insideSurface, dpi, drawFinOutline, outlineWidthPx, false);
	}

	public TextureGenerationResult generateTextureImage(RocketComponent component, boolean insideSurface,
														double dpi, boolean drawFinOutline, float outlineWidthPx,
														boolean mirrorVertically) throws TextureGenerationException {
		return generateTextureImage(component, insideSurface, dpi, drawFinOutline, outlineWidthPx, mirrorVertically,
				new Color(0, 0, 0, 200));
	}

	public TextureGenerationResult generateTextureImage(RocketComponent component, boolean insideSurface,
														double dpi, boolean drawFinOutline, float outlineWidthPx,
														boolean mirrorVertically, Color outlineColor) throws TextureGenerationException {
		if (dpi <= 0) {
			throw new TextureGenerationException("DPI must be larger than zero.");
		}

		if (component instanceof FinSet) {
			return generateForFinSet((FinSet) component, dpi, drawFinOutline, outlineWidthPx, mirrorVertically, outlineColor);
		}

		if (component instanceof SymmetricComponent) {
			return generateForSymmetric((SymmetricComponent) component, insideSurface, dpi);
		}

		throw new TextureGenerationException("Component type " + component.getClass().getSimpleName()
				+ " is not supported for automatic texture creation.");
	}

	public void writeTexture(File file, TextureGenerationResult result) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		ImageIO.write(result.getImage(), "png", file);
	}

	private TextureGenerationResult generateForSymmetric(SymmetricComponent component, boolean insideSurface,
														 double dpi) throws TextureGenerationException {
		double length = component.getLength();
		if (length <= 0) {
			throw new TextureGenerationException(
					"Component length must be greater than zero to create a texture.");
		}

		double maxRadius = 0;
		for (int i = 0; i <= SAMPLING_STEPS; i++) {
			double x = length * i / SAMPLING_STEPS;
			double radius = insideSurface ? component.getInnerRadius(x) : component.getRadius(x);
			maxRadius = Math.max(maxRadius, radius);
		}

		if (maxRadius <= 0) {
			throw new TextureGenerationException(
					"Component radius must be greater than zero to create a texture.");
		}

		double widthMeters = 2 * Math.PI * maxRadius;
		return renderBlankImage(widthMeters, length, dpi, null);
	}

	private TextureGenerationResult generateForFinSet(FinSet finSet, double dpi, boolean drawOutline, float outlineWidthPx,
													 boolean mirrorHorizontally, Color outlineColor)
			throws TextureGenerationException {
		CoordinateIF[] points = finSet.getFinPoints();
		if (points == null || points.length < 3) {
			throw new TextureGenerationException("Unable to determine fin geometry for texture creation.");
		}

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (CoordinateIF point : points) {
			minX = Math.min(minX, point.getX());
			maxX = Math.max(maxX, point.getX());
			minY = Math.min(minY, point.getY());
			maxY = Math.max(maxY, point.getY());
		}

		double widthMeters = maxX - minX;
		double heightMeters = maxY - minY;

		if (widthMeters <= 0 || heightMeters <= 0) {
			throw new TextureGenerationException("Fin geometry has zero extent, cannot create texture.");
		}

		List<CoordinateIF> outline = new ArrayList<>(Arrays.asList(points));
		float sanitizedOutlineWidth = outlineWidthPx;
		OutlineContext outlineContext;
		if (!drawOutline || sanitizedOutlineWidth == 0f) {
			outlineContext = null;
		} else {
			outlineContext = new OutlineContext(outline, minX, maxY, sanitizedOutlineWidth,
					outlineColor == null ? new Color(0, 0, 0, 200) : outlineColor);
		}
		TextureGenerationResult result = renderBlankImage(widthMeters, heightMeters, dpi, outlineContext);
		if (mirrorHorizontally) {
			result = mirrorHorizontally(result);
		}
		return rotateResult180(result);
	}

	private TextureGenerationResult renderBlankImage(double widthMeters, double heightMeters, double dpi,
													 OutlineContext outlineContext) throws TextureGenerationException {
		double scale = dpi / METERS_PER_INCH;

		int widthPx = Math.max(1, (int) Math.round(widthMeters * scale));
		int heightPx = Math.max(1, (int) Math.round(heightMeters * scale));

		if (widthPx <= 0 || heightPx <= 0) {
			throw new TextureGenerationException("Computed image dimensions are invalid.");
		}

		BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);

		if (outlineContext != null) {
			drawOutline(image, outlineContext, scale);
		}

		return new TextureGenerationResult(image, widthMeters, heightMeters, dpi);
	}

	private TextureGenerationResult rotateResult180(TextureGenerationResult result) {
		BufferedImage source = result.getImage();
		BufferedImage rotated = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics2D g2d = rotated.createGraphics();
		try {
			g2d.rotate(Math.PI, source.getWidth() / 2.0, source.getHeight() / 2.0);
			g2d.drawImage(source, 0, 0, null);
		} finally {
			g2d.dispose();
		}
		return new TextureGenerationResult(rotated, result.getWidthMeters(), result.getHeightMeters(), result.getDpi());
	}

	private void drawOutline(BufferedImage image, OutlineContext outlineContext, double scale) {
		float requestedWidth = outlineContext.outlineWidthPx > 0
				? outlineContext.outlineWidthPx
				: (float) Math.max(1f, scale * 0.0005f);
		float inflatedWidth = Math.max(1f, requestedWidth * 2f);

		BufferedImage outlineLayer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = outlineLayer.createGraphics();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			Path2D path = new Path2D.Double();
			boolean first = true;
			for (CoordinateIF point : outlineContext.outlinePoints) {
				double x = (point.getX() - outlineContext.minX) * scale;
				double y = (outlineContext.maxY - point.getY()) * scale;
				if (first) {
					path.moveTo(x, y);
					first = false;
				} else {
					path.lineTo(x, y);
				}
			}
			path.closePath();

		g2d.setStroke(new BasicStroke(inflatedWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		g2d.setColor(outlineContext.outlineColor);
			g2d.draw(path);

			g2d.setComposite(AlphaComposite.Clear);
			g2d.fill(path);
		} finally {
			g2d.dispose();
		}

		Graphics2D base = image.createGraphics();
		try {
			base.drawImage(outlineLayer, 0, 0, null);
		} finally {
			base.dispose();
		}
	}

	private TextureGenerationResult mirrorHorizontally(TextureGenerationResult original) {
		BufferedImage src = original.getImage();
		BufferedImage mirrored = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		Graphics2D g2 = mirrored.createGraphics();
		try {
			g2.translate(src.getWidth(), 0);
			g2.scale(-1, 1);
			g2.drawImage(src, 0, 0, null);
		} finally {
			g2.dispose();
		}
		return new TextureGenerationResult(mirrored, original.getWidthMeters(), original.getHeightMeters(), original.getDpi());
	}

	private static final class OutlineContext {
		private final List<CoordinateIF> outlinePoints;
		private final double minX;
		private final double maxY;
		private final float outlineWidthPx;
		private final Color outlineColor;

		private OutlineContext(List<CoordinateIF> outlinePoints, double minX, double maxY, float outlineWidthPx, Color outlineColor) {
			this.outlinePoints = outlinePoints;
			this.minX = minX;
			this.maxY = maxY;
			this.outlineWidthPx = outlineWidthPx;
			this.outlineColor = outlineColor;
		}
	}

	public static final class TextureGenerationResult {
		private final BufferedImage image;
		private final double widthMeters;
		private final double heightMeters;
		private final double dpi;

		private TextureGenerationResult(BufferedImage image, double widthMeters, double heightMeters, double dpi) {
			this.image = image;
			this.widthMeters = widthMeters;
			this.heightMeters = heightMeters;
			this.dpi = dpi;
		}

		public BufferedImage getImage() {
			return image;
		}

		public double getWidthMeters() {
			return widthMeters;
		}

		public double getHeightMeters() {
			return heightMeters;
		}

		public double getDpi() {
			return dpi;
		}
	}

	public static class TextureGenerationException extends Exception {
		private static final long serialVersionUID = -3110562336932512500L;

		public TextureGenerationException(String message) {
			super(message);
		}
	}
}

