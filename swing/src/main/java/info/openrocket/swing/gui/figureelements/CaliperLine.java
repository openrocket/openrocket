package info.openrocket.swing.gui.figureelements;

import info.openrocket.swing.gui.util.ColorConversion;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A vertical line element for the caliper measurement tool.
 * Draws a vertical line with a draggable handle at the top.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CaliperLine implements FigureElement {

	// Base sizes at 96 DPI - will be scaled based on actual DPI
	private static final float BASE_DIAMOND_HALF_WIDTH = 14.0f;   // Half-width of the diamond handle
	private static final float BASE_DIAMOND_HALF_HEIGHT = 22.0f;  // Half-height of the diamond handle
	private static final float BASE_LINE_WIDTH_NORMAL = 2.0f;  // Normal line thickness
	private static final float BASE_LINE_WIDTH_HOVER = 4.0f;  // Thicker line when hovering
	private static final float BASE_HANDLE_LABEL_FONT_SIZE = 16.0f;  // Base font size
	private static final float BASE_INDICATOR_LABEL_FONT_SIZE = 16.0f;  // Base font size for indicator (reduced from 24)
	
	// Out-of-view indicator
	private static final float BASE_ARROW_SIZE = 20.0f;  // Size of the arrow indicator
	private static final float BASE_ARROW_STROKE_WIDTH = 3.0f;  // Thickness of arrow lines
	private static final float BASE_LABEL_OFFSET = 8.0f;  // Distance from arrow to label
	
	private static final double BASE_DPI = 96.0;  // Base DPI for scaling
	
	// DPI-scaled values (calculated once)
	private static float DIAMOND_HALF_WIDTH;
	private static float DIAMOND_HALF_HEIGHT;
	private static float LINE_WIDTH_NORMAL;
	private static float LINE_WIDTH_HOVER;
	private static Font HANDLE_LABEL_FONT;
	private static Font INDICATOR_LABEL_FONT;
	private static float ARROW_SIZE;
	private static float ARROW_STROKE_WIDTH;
	private static float LABEL_OFFSET;

	private double x;  // X position in model coordinates
	private boolean isHovered = false;          // Whether the mouse is hovering over this line
	private boolean isIndicatorHovered = false; // Whether the mouse is hovering over the out-of-view indicator
	private boolean isSnapMode = false;  // Whether we're in snap mode (affects transparency)
	private String handleLabel = "";
	private CaliperLine siblingLine = null;  // The other caliper line, used to avoid indicator overlap
	private String dragPositionLabel = null; // Non-null while dragging; shown as a tooltip below the handle

	private static Color lineColor;
	private static Color handleColor;
	private static Color handleHoverColor;  // Brighter handle color when hovered
	private static Color handleTextColor;   // Contrast color for text inside the diamond
	private static Color handleBorderColor; // High-contrast border around the diamond
	private static Color textColor;

	static {
		initColors();
		updateSizes();
	}

	/**
	 * Create a new CaliperLine at the specified x coordinate.
	 *
	 * @param x the x position in model coordinates (meters)
	 */
	public CaliperLine(double x) {
		this.x = x;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(CaliperLine::updateColors);
	}

	public static void updateColors() {
		lineColor = GUIUtil.getUITheme().getCaliperColor();
		textColor = GUIUtil.getUITheme().getTextColor();

		// Handle fill: slightly brighter than the line color
		handleColor = new Color(
				Math.min(255, lineColor.getRed() + 50),
				Math.min(255, lineColor.getGreen() + 50),
				Math.min(255, lineColor.getBlue() + 50),
				lineColor.getAlpha()
		);
		handleHoverColor = ColorConversion.brightenColor(handleColor, 50);

		// Handle text: black or white chosen by relative luminance of the fill
		double lum = 0.2126 * handleColor.getRed() + 0.7152 * handleColor.getGreen()
				+ 0.0722 * handleColor.getBlue();
		handleTextColor = lum >= 128 ? Color.BLACK : Color.WHITE;

		// Diamond border: contrasting against the fill (dark on bright fill, light on dark fill)
		handleBorderColor = lum >= 128
				? new Color(0, 0, 0, 180)
				: new Color(255, 255, 255, 180);
	}
	
	/**
	 * Update all sizes based on current DPI.
	 */
	private static void updateSizes() {
		double dpi = GUIUtil.getDPI();
		double scale = dpi / BASE_DPI;
		
		DIAMOND_HALF_WIDTH = (float) (BASE_DIAMOND_HALF_WIDTH * scale);
		DIAMOND_HALF_HEIGHT = (float) (BASE_DIAMOND_HALF_HEIGHT * scale);
		LINE_WIDTH_NORMAL = (float) (BASE_LINE_WIDTH_NORMAL * scale);
		LINE_WIDTH_HOVER = (float) (BASE_LINE_WIDTH_HOVER * scale);
		ARROW_SIZE = (float) (BASE_ARROW_SIZE * scale);
		ARROW_STROKE_WIDTH = (float) (BASE_ARROW_STROKE_WIDTH * scale);
		LABEL_OFFSET = (float) (BASE_LABEL_OFFSET * scale);
		
		// Update fonts
		float handleFontSize = (float) (BASE_HANDLE_LABEL_FONT_SIZE * scale);
		float indicatorFontSize = (float) (BASE_INDICATOR_LABEL_FONT_SIZE * scale);
		Font baseFont = new Font(Font.SANS_SERIF, Font.BOLD, 12); // Base font
		HANDLE_LABEL_FONT = baseFont.deriveFont(Font.BOLD, handleFontSize);
		INDICATOR_LABEL_FONT = baseFont.deriveFont(Font.BOLD, indicatorFontSize);
	}

	/**
	 * Set the x position of the caliper line in model coordinates.
	 *
	 * @param x the x position in meters
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set whether this line is in snap mode (affects transparency).
	 *
	 * @param snapMode true if in snap mode
	 */
	public void setSnapMode(boolean snapMode) {
		this.isSnapMode = snapMode;
	}

	/**
	 * Get the x position of the caliper line in model coordinates.
	 *
	 * @return the x position in meters
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set whether the mouse is hovering over this caliper line.
	 *
	 * @param hovered true if hovering, false otherwise
	 */
	public void setHovered(boolean hovered) {
		this.isHovered = hovered;
	}

	/**
	 * Check if the mouse is hovering over this caliper line.
	 *
	 * @return true if hovering, false otherwise
	 */
	public boolean isHovered() {
		return isHovered;
	}

	public void setIndicatorHovered(boolean hovered) {
		this.isIndicatorHovered = hovered;
	}

	public boolean isIndicatorHovered() {
		return isIndicatorHovered;
	}

	/**
	 * Set the label displayed inside the handle (e.g., "1" or "2").
	 *
	 * @param label the label text
	 */
	public void setHandleLabel(String label) {
		this.handleLabel = label != null ? label : "";
	}

	public void setSiblingLine(CaliperLine sibling) {
		this.siblingLine = sibling;
	}

	public void setDragPositionLabel(String label) {
		this.dragPositionLabel = label;
	}

	@Override
	public void paint(Graphics2D g2, double scale) {
		paint(g2, scale, null);
	}

	@Override
	public void paint(Graphics2D g2, double scale, Rectangle visible) {
		// Use a graphics copy for screen-space drawing to avoid altering the main context
		Graphics2D g2Screen = (Graphics2D) g2.create();
		try {
			// Calculate screen coordinates for the handle
			AffineTransform transform = g2.getTransform();
			Point2D.Double modelPoint = new Point2D.Double(x, 0);
			Point2D.Double screenPoint = new Point2D.Double();
			transform.transform(modelPoint, screenPoint);

			if (Double.isNaN(screenPoint.x) || Double.isInfinite(screenPoint.x)) {
				return;
			}

			double handleX_screen = screenPoint.x;
			double siblingXScreen = siblingLine != null ? siblingLine.getScreenX(transform) : Double.NaN;
			double handleY_screen = 0.0;  // Start at the very top

			// Reset transform to draw in screen coordinates
			g2Screen.setTransform(new AffineTransform());
			
			// Get the actual visible bounds in screen coordinates (after transform reset)
			// Use clip bounds if available, otherwise fall back to visible parameter
			Rectangle screenVisible = visible;
			if (g2Screen.getClipBounds() != null) {
				screenVisible = g2Screen.getClipBounds();
			}
			
			// Draw vertical line covering the full viewport height
			// We draw in screen coordinates, from Y=0 to a very large number (e.g. 20000)
			// The clip will ensure it doesn't draw outside the viewport
			Line2D.Double screenLine = new Line2D.Double(handleX_screen, 0, handleX_screen, 20000);
			float lineWidth = isHovered ? LINE_WIDTH_HOVER : LINE_WIDTH_NORMAL;

			// Apply 50% transparency in snap mode
			int alpha = isSnapMode ? 128 : 255;
			Color drawColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), alpha);

			g2Screen.setColor(drawColor);
			g2Screen.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2Screen.draw(screenLine);

			// Draw marker handle as one continuous path: elongated rectangle at top, inverted triangle below
			// Create a single manifold path for the entire marker shape
			Path2D.Double marker = new Path2D.Double();

			double diamondCenterY = handleY_screen + DIAMOND_HALF_HEIGHT;

			// Diamond shape: top tip at y=0, bottom tip pointing down toward the line
			marker.moveTo(handleX_screen, handleY_screen);                             // Top tip
			marker.lineTo(handleX_screen + DIAMOND_HALF_WIDTH, diamondCenterY);       // Right point
			marker.lineTo(handleX_screen, handleY_screen + DIAMOND_HALF_HEIGHT * 2);  // Bottom tip
			marker.lineTo(handleX_screen - DIAMOND_HALF_WIDTH, diamondCenterY);       // Left point
			marker.closePath();

			// Fill the entire marker shape
			Color baseHandleColor = isHovered ? handleHoverColor : handleColor;
			Color drawHandleColor = isSnapMode
					? new Color(baseHandleColor.getRed(), baseHandleColor.getGreen(), baseHandleColor.getBlue(), 128)
					: baseHandleColor;
			g2Screen.setColor(drawHandleColor);
			g2Screen.fill(marker);

			// Draw high-contrast border around the diamond
			Color border = new Color(handleBorderColor.getRed(), handleBorderColor.getGreen(),
					handleBorderColor.getBlue(), isSnapMode ? handleBorderColor.getAlpha() / 2 : handleBorderColor.getAlpha());
			g2Screen.setColor(border);
			g2Screen.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2Screen.draw(marker);

			// Draw label (1 or 2) inside the handle rectangle if provided
			if (handleLabel != null && !handleLabel.isEmpty()) {
				g2Screen.setFont(HANDLE_LABEL_FONT);
				FontRenderContext frc = g2Screen.getFontRenderContext();
				Rectangle2D textBounds = HANDLE_LABEL_FONT.getStringBounds(handleLabel, frc);
				double textWidth = textBounds.getWidth();
				double textHeight = textBounds.getHeight();
				double textX = handleX_screen - textWidth / 2.0;
				// Position text vertically centered within diamond
				double textY = diamondCenterY + textHeight / 4.0;
				g2Screen.setColor(handleTextColor);
				g2Screen.drawString(handleLabel, (float) textX, (float) textY);
			}
			
			// Draw drag position tooltip below the handle
			if (dragPositionLabel != null) {
				drawDragTooltip(g2Screen, handleX_screen, handleY_screen + DIAMOND_HALF_HEIGHT * 2);
			}

			// Draw out-of-view indicator if the caliper line is outside the visible area
			if (screenVisible != null) {
				// Both handleX_screen and screenVisible are now in screen coordinates (after transform reset)
				// Add a small margin to avoid flickering when the line is right at the edge
				double margin = 5.0;
				boolean isOutOfView = (handleX_screen < screenVisible.x - margin) || 
				                       (handleX_screen > screenVisible.x + screenVisible.width + margin);
				if (isOutOfView) {
					drawOutOfViewIndicator(g2Screen, handleX_screen, screenVisible, siblingXScreen);
				}
			}
		} finally {
			g2Screen.dispose();
		}
	}
	
	/**
	 * Get the screen X position of the caliper line.
	 * This is a helper method to calculate the screen position from the model position.
	 *
	 * @param transform the transform from model to screen coordinates
	 * @return the screen X position, or Double.NaN if invalid
	 */
	public double getScreenX(AffineTransform transform) {
		Point2D.Double modelPoint = new Point2D.Double(x, 0);
		Point2D.Double screenPoint = new Point2D.Double();
		transform.transform(modelPoint, screenPoint);
		return screenPoint.x;
	}
	
	/**
	 * Get the bounds of the handle in screen coordinates for hit testing.
	 *
	 * @param transform the transform from model to screen coordinates
	 * @return the handle bounds in screen coordinates, or null if invalid
	 */
	public Rectangle2D.Double getHandleBounds(AffineTransform transform) {
		Point2D.Double modelPoint = new Point2D.Double(x, 0);
		Point2D.Double screenPoint = new Point2D.Double();
		transform.transform(modelPoint, screenPoint);
		
		if (Double.isNaN(screenPoint.x) || Double.isInfinite(screenPoint.x)) {
			return null;
		}
		
		double handleX_screen = screenPoint.x;
		double handleY_screen = 0.0;  // Start at the very top

		// Return bounds that encompass the diamond handle
		double minX = handleX_screen - DIAMOND_HALF_WIDTH;
		double maxX = handleX_screen + DIAMOND_HALF_WIDTH;
		double minY = handleY_screen;
		double maxY = handleY_screen + DIAMOND_HALF_HEIGHT * 2;

		// Add some padding for easier clicking
		double padding = DIAMOND_HALF_WIDTH * 0.3;
		return new Rectangle2D.Double(
			minX - padding,
			minY - padding,
			(maxX - minX) + 2 * padding,
			(maxY - minY) + 2 * padding
		);
	}
	
	/**
	 * Check if a screen point is near the caliper line (within hit tolerance).
	 * This allows dragging anywhere on the line, not just the handle.
	 *
	 * @param screenX the screen X coordinate
	 * @param screenY the screen Y coordinate
	 * @param transform the transform from model to screen coordinates
	 * @param visibleRect the visible viewport rectangle
	 * @return true if the point is near the line
	 */
	public boolean isPointNearLine(double screenX, double screenY, AffineTransform transform, Rectangle visibleRect) {
		if (visibleRect == null) {
			return false;
		}
		
		// Get the screen X position of the line
		double lineX = getScreenX(transform);
		if (Double.isNaN(lineX) || Double.isInfinite(lineX)) {
			return false;
		}
		
		// Check if point is within tolerance of the line's X position
		// Use a DPI-scaled tolerance (base 10 pixels at 96 DPI)
		double dpi = GUIUtil.getDPI();
		double tolerance = (10.0 * dpi) / BASE_DPI;
		double distance = Math.abs(screenX - lineX);
		
		if (distance > tolerance) {
			return false;
		}
		
		// Also check that the point is within the visible area vertically
		// (we don't want to drag if clicking way outside the viewport)
		return screenY >= visibleRect.y && screenY <= visibleRect.y + visibleRect.height;
	}
	
	/**
	 * Get the bounds of the out-of-view indicator in screen coordinates.
	 *
	 * @param caliperXScreen the X position of the caliper line in screen coordinates
	 * @param visible the visible viewport rectangle
	 * @return the bounds of the indicator, or null if not out of view
	 */
	/**
	 * Draw a small tooltip below the handle showing the current drag position.
	 *
	 * @param g2    graphics context in screen coordinates
	 * @param cx    horizontal center of the handle
	 * @param topY  Y coordinate of the handle's bottom tip
	 */
	private void drawDragTooltip(Graphics2D g2, double cx, double topY) {
		Font font = HANDLE_LABEL_FONT.deriveFont(Font.PLAIN, HANDLE_LABEL_FONT.getSize2D() * 1.2f);
		g2.setFont(font);
		FontRenderContext frc = g2.getFontRenderContext();
		Rectangle2D textBounds = font.getStringBounds(dragPositionLabel, frc);
		double tw = textBounds.getWidth();
		double th = textBounds.getHeight();
		double pad = 4.0;
		double boxW = tw + pad * 2;
		double boxH = th + pad;
		double boxX = cx - boxW / 2.0;
		double boxY = topY + 5.0;

		g2.setColor(handleColor);
		g2.fill(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 6, 6));
		g2.setStroke(new BasicStroke(1.0f));
		g2.setColor(new Color(handleBorderColor.getRed(), handleBorderColor.getGreen(),
				handleBorderColor.getBlue(), 120));
		g2.draw(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 6, 6));
		g2.setColor(handleTextColor);
		g2.drawString(dragPositionLabel, (float) (boxX + pad), (float) (boxY + th * 0.8));
	}

	/**
	 * Compute the vertical center position for the out-of-view indicator.
	 * When both calipers are off the same edge, caliper "1" sits at 1/3 height
	 * and caliper "2" at 2/3 height so they don't overlap.
	 */
	private double computeIndicatorY(boolean isLeft, Rectangle visible, double siblingXScreen) {
		double margin = 5.0;
		boolean siblingOnSameSide = Double.isFinite(siblingXScreen) && (
				(isLeft && siblingXScreen < visible.x - margin) ||
				(!isLeft && siblingXScreen > visible.x + visible.width + margin));
		if (siblingOnSameSide) {
			return visible.y + visible.height * ("1".equals(handleLabel) ? 1.0 / 3.0 : 2.0 / 3.0);
		}
		return visible.y + visible.height / 2.0;
	}

	public Rectangle2D.Double getIndicatorBounds(double caliperXScreen, Rectangle visible, double siblingXScreen) {
		if (visible == null) {
			return null;
		}

		double margin = 5.0;
		boolean isOutOfView = (caliperXScreen < visible.x - margin) ||
		                       (caliperXScreen > visible.x + visible.width + margin);
		if (!isOutOfView) {
			return null;
		}

		// Determine which edge to draw the arrow on
		boolean isLeft = caliperXScreen < visible.x;
		double arrowX = isLeft ? visible.x : visible.x + visible.width;
		double arrowY = computeIndicatorY(isLeft, visible, siblingXScreen);
		
		// Calculate bounds: arrow area + label area
		double minX, maxX, minY, maxY;
		if (isLeft) {
			minX = arrowX;
			maxX = arrowX + ARROW_SIZE + LABEL_OFFSET;
			if (handleLabel != null && !handleLabel.isEmpty()) {
				// Add label width (approximate)
				maxX += 30; // Approximate label width
			}
		} else {
			maxX = arrowX;
			minX = arrowX - ARROW_SIZE - LABEL_OFFSET;
			if (handleLabel != null && !handleLabel.isEmpty()) {
				// Subtract label width (approximate)
				minX -= 30; // Approximate label width
			}
		}
		
		minY = arrowY - ARROW_SIZE / 2.0 - 10; // Add some padding
		maxY = arrowY + ARROW_SIZE / 2.0 + 10;
		if (handleLabel != null && !handleLabel.isEmpty()) {
			// Account for label height
			maxY += 30; // Approximate label height
		}
		
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}
	
	/**
	 * Draw an arrow indicator at the edge of the viewport pointing toward the caliper line.
	 *
	 * @param g2Screen the graphics context in screen coordinates
	 * @param caliperX the X position of the caliper line in screen coordinates
	 * @param visible the visible viewport rectangle
	 */
	private void drawOutOfViewIndicator(Graphics2D g2Screen, double caliperX, Rectangle visible, double siblingXScreen) {
		Color baseColor = isIndicatorHovered ? ColorConversion.brightenColor(lineColor, 60) : lineColor;
		Color indicatorColor = isSnapMode
				? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 128)
				: baseColor;
		g2Screen.setColor(indicatorColor);
		g2Screen.setStroke(new BasicStroke(ARROW_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		// Determine which edge to draw the arrow on
		boolean isLeft = caliperX < visible.x;
		double arrowX = isLeft ? visible.x : visible.x + visible.width;

		// Position arrow vertically — offset from center when both calipers are on the same edge
		double arrowY = computeIndicatorY(isLeft, visible, siblingXScreen);

		// Draw arrow pointing toward the caliper line
		Path2D.Double arrow = new Path2D.Double();
		if (isLeft) {
			arrow.moveTo(arrowX, arrowY);
			arrow.lineTo(arrowX + ARROW_SIZE, arrowY - ARROW_SIZE / 2);
			arrow.lineTo(arrowX + ARROW_SIZE, arrowY + ARROW_SIZE / 2);
			arrow.closePath();
		} else {
			arrow.moveTo(arrowX, arrowY);
			arrow.lineTo(arrowX - ARROW_SIZE, arrowY - ARROW_SIZE / 2);
			arrow.lineTo(arrowX - ARROW_SIZE, arrowY + ARROW_SIZE / 2);
			arrow.closePath();
		}

		g2Screen.fill(arrow);
		g2Screen.draw(arrow);

		// Draw caliper number label and "click" hint next to the arrow
		FontRenderContext frc = g2Screen.getFontRenderContext();
		double labelX = 0, labelY = arrowY;
		if (handleLabel != null && !handleLabel.isEmpty()) {
			g2Screen.setFont(INDICATOR_LABEL_FONT);
			Rectangle2D textBounds = INDICATOR_LABEL_FONT.getStringBounds(handleLabel, frc);
			double textWidth = textBounds.getWidth();
			double textHeight = textBounds.getHeight();
			labelX = isLeft
					? arrowX + ARROW_SIZE + LABEL_OFFSET
					: arrowX - ARROW_SIZE - LABEL_OFFSET - textWidth;
			labelY = arrowY + textHeight / 4.0;
			g2Screen.setColor(indicatorColor);
			g2Screen.drawString(handleLabel, (float) labelX, (float) labelY);
		}

		// "click" hint: aligned with the number label so it stays within the viewport
		Font hintFont = INDICATOR_LABEL_FONT.deriveFont(Font.PLAIN, INDICATOR_LABEL_FONT.getSize2D());
		g2Screen.setFont(hintFont);
		String hint = "click";
		Rectangle2D hintBounds = hintFont.getStringBounds(hint, frc);
		double hintWidth = hintBounds.getWidth();
		double hintX = isLeft
				? arrowX + ARROW_SIZE + LABEL_OFFSET
				: arrowX - ARROW_SIZE - LABEL_OFFSET - hintWidth;
		double hintY = labelY + hintBounds.getHeight();
		Color hintColor = new Color(indicatorColor.getRed(), indicatorColor.getGreen(),
				indicatorColor.getBlue(), isSnapMode ? 100 : 180);
		g2Screen.setColor(hintColor);
		g2Screen.drawString(hint, (float) hintX, (float) hintY);
	}
}

