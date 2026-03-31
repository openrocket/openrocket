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
 * A horizontal line element for the caliper measurement tool.
 * Draws a horizontal line with a draggable handle on the left side.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class HorizontalCaliperLine implements FigureElement {

	// Base sizes at 96 DPI - will be scaled based on actual DPI
	private static final float BASE_DIAMOND_HALF_WIDTH = 22.0f;   // Half-width of the diamond handle (horizontal)
	private static final float BASE_DIAMOND_HALF_HEIGHT = 14.0f;  // Half-height of the diamond handle (vertical)
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

	private double y;  // Y position in model coordinates
	private boolean isHovered = false;          // Whether the mouse is hovering over this line
	private boolean isIndicatorHovered = false; // Whether the mouse is hovering over the out-of-view indicator
	private boolean isSnapMode = false;  // Whether we're in snap mode (affects transparency)
	private String handleLabel = "";
	private HorizontalCaliperLine siblingLine = null;  // The other caliper line, used to avoid indicator overlap
	private String dragPositionLabel = null; // Non-null while dragging; shown as a tooltip below the handle

	private static Color lineColor;
	private static Color handleColor;
	private static Color handleHoverColor;
	private static Color handleTextColor;
	private static Color handleBorderColor;
	private static Color textColor;

	static {
		initColors();
		updateSizes();
	}

	/**
	 * Create a new HorizontalCaliperLine at the specified y coordinate.
	 *
	 * @param y the y position in model coordinates (meters)
	 */
	public HorizontalCaliperLine(double y) {
		this.y = y;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(HorizontalCaliperLine::updateColors);
	}

	public static void updateColors() {
		lineColor = GUIUtil.getUITheme().getCaliperColor();
		textColor = GUIUtil.getUITheme().getTextColor();

		handleColor = new Color(
				Math.min(255, lineColor.getRed() + 50),
				Math.min(255, lineColor.getGreen() + 50),
				Math.min(255, lineColor.getBlue() + 50),
				lineColor.getAlpha()
		);
		handleHoverColor = ColorConversion.brightenColor(handleColor, 50);

		double lum = 0.2126 * handleColor.getRed() + 0.7152 * handleColor.getGreen()
				+ 0.0722 * handleColor.getBlue();
		handleTextColor = lum >= 128 ? Color.BLACK : Color.WHITE;

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
	 * Set the y position of the caliper line in model coordinates.
	 *
	 * @param y the y position in meters
	 */
	public void setY(double y) {
		this.y = y;
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
	 * Get the y position of the caliper line in model coordinates.
	 *
	 * @return the y position in meters
	 */
	public double getY() {
		return y;
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

	public void setSiblingLine(HorizontalCaliperLine sibling) {
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
			Point2D.Double modelPoint = new Point2D.Double(0, y);
			Point2D.Double screenPoint = new Point2D.Double();
			transform.transform(modelPoint, screenPoint);

			if (Double.isNaN(screenPoint.y) || Double.isInfinite(screenPoint.y)) {
				return;
			}

			double siblingYScreen = siblingLine != null ? siblingLine.getScreenY(transform) : Double.NaN;

			// Reset transform to draw in screen coordinates
			g2Screen.setTransform(new AffineTransform());
			
			// Get the actual visible bounds in screen coordinates (after transform reset)
			// Use clip bounds if available, otherwise fall back to visible parameter
			Rectangle screenVisible = visible;
			if (g2Screen.getClipBounds() != null) {
				screenVisible = g2Screen.getClipBounds();
			}
			
			// Use the visible rectangle's left X coordinate to position the handle correctly
			// This ensures the handle stays at the left of the visible area after window resize
			double handleX_screen = screenVisible != null ? screenVisible.x : 0.0;
			double handleY_screen = screenPoint.y;
			
			// Draw horizontal line covering the full visible area width
			// Use the visible rectangle to determine the line bounds
			double lineLeft = screenVisible != null ? screenVisible.x : 0.0;
			double lineRight = screenVisible != null ? (screenVisible.x + screenVisible.width) : 20000;
			Line2D.Double screenLine = new Line2D.Double(lineLeft, handleY_screen, lineRight, handleY_screen);
			float lineWidth = isHovered ? LINE_WIDTH_HOVER : LINE_WIDTH_NORMAL;

			int alpha = isSnapMode ? 128 : 255;
			Color drawColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), alpha);

			g2Screen.setColor(drawColor);
			g2Screen.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2Screen.draw(screenLine);

			// Draw marker handle as one continuous path: elongated rectangle on left, triangle to the right
			// Create a single manifold path for the entire marker shape
			Path2D.Double marker = new Path2D.Double();

			double diamondCenterX = handleX_screen + DIAMOND_HALF_WIDTH;

			// Diamond shape: left tip at left edge, right tip pointing toward the line
			marker.moveTo(handleX_screen, handleY_screen);                              // Left tip
			marker.lineTo(diamondCenterX, handleY_screen - DIAMOND_HALF_HEIGHT);        // Top point
			marker.lineTo(handleX_screen + DIAMOND_HALF_WIDTH * 2, handleY_screen);     // Right tip
			marker.lineTo(diamondCenterX, handleY_screen + DIAMOND_HALF_HEIGHT);        // Bottom point
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
				// Position text horizontally centered within diamond
				double textX = diamondCenterX - textWidth / 2.0;
				double textY = handleY_screen + textHeight / 4.0;
				g2Screen.setColor(handleTextColor);
				g2Screen.drawString(handleLabel, (float) textX, (float) textY);
			}
			
			// Draw drag position tooltip below the handle
			if (dragPositionLabel != null) {
				drawDragTooltip(g2Screen, handleX_screen + DIAMOND_HALF_WIDTH, handleY_screen + DIAMOND_HALF_HEIGHT);
			}

			// Draw out-of-view indicator if the caliper line is outside the visible area
			if (screenVisible != null) {
				// Both handleY_screen and screenVisible are now in screen coordinates (after transform reset)
				// Add a small margin to avoid flickering when the line is right at the edge
				double margin = 5.0;
				boolean isOutOfView = (handleY_screen < screenVisible.y - margin) || 
				                      (handleY_screen > screenVisible.y + screenVisible.height + margin);
				if (isOutOfView) {
					drawOutOfViewIndicator(g2Screen, handleY_screen, screenVisible, siblingYScreen);
				}
			}
		} finally {
			g2Screen.dispose();
		}
	}
	
	/**
	 * Get the screen Y position of the caliper line.
	 * This is a helper method to calculate the screen position from the model position.
	 *
	 * @param transform the transform from model to screen coordinates
	 * @return the screen Y position, or Double.NaN if invalid
	 */
	public double getScreenY(AffineTransform transform) {
		Point2D.Double modelPoint = new Point2D.Double(0, y);
		Point2D.Double screenPoint = new Point2D.Double();
		transform.transform(modelPoint, screenPoint);
		return screenPoint.y;
	}
	
	/**
	 * Get the bounds of the handle in screen coordinates for hit testing.
	 *
	 * @param transform the transform from model to screen coordinates
	 * @param visible the visible viewport rectangle (for determining handle X position)
	 * @return the handle bounds in screen coordinates, or null if invalid
	 */
	public Rectangle2D.Double getHandleBounds(AffineTransform transform, Rectangle visible) {
		Point2D.Double modelPoint = new Point2D.Double(0, y);
		Point2D.Double screenPoint = new Point2D.Double();
		transform.transform(modelPoint, screenPoint);
		
		if (Double.isNaN(screenPoint.y) || Double.isInfinite(screenPoint.y)) {
			return null;
		}
		
		// Use the visible rectangle's left X coordinate to position the handle correctly
		double handleX_screen = visible != null ? visible.x : 0.0;
		double handleY_screen = screenPoint.y;
		
		// Return bounds that encompass the diamond handle
		double minX = handleX_screen;
		double maxX = handleX_screen + DIAMOND_HALF_WIDTH * 2;
		double minY = handleY_screen - DIAMOND_HALF_HEIGHT;
		double maxY = handleY_screen + DIAMOND_HALF_HEIGHT;

		// Add some padding for easier clicking
		double padding = DIAMOND_HALF_HEIGHT * 0.3;
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
		
		// Get the screen Y position of the line
		double lineY = getScreenY(transform);
		if (Double.isNaN(lineY) || Double.isInfinite(lineY)) {
			return false;
		}
		
		// Check if point is within tolerance of the line's Y position
		// Use a DPI-scaled tolerance (base 10 pixels at 96 DPI)
		double dpi = GUIUtil.getDPI();
		double tolerance = (10.0 * dpi) / BASE_DPI;
		double distance = Math.abs(screenY - lineY);
		
		if (distance > tolerance) {
			return false;
		}
		
		// Also check that the point is within the visible area horizontally
		// (we don't want to drag if clicking way outside the viewport)
		return screenX >= visibleRect.x && screenX <= visibleRect.x + visibleRect.width;
	}
	
	/**
	 * Get the bounds of the out-of-view indicator in screen coordinates.
	 *
	 * @param caliperYScreen the Y position of the caliper line in screen coordinates
	 * @param visible the visible viewport rectangle
	 * @return the bounds of the indicator, or null if not out of view
	 */
	/**
	 * Draw a small tooltip below the handle showing the current drag position.
	 *
	 * @param g2    graphics context in screen coordinates
	 * @param cx    horizontal center of the handle diamond
	 * @param topY  Y coordinate of the handle's bottom tip
	 */
	private void drawDragTooltip(Graphics2D g2, double cx, double topY) {
		Font font = HANDLE_LABEL_FONT.deriveFont(Font.PLAIN, HANDLE_LABEL_FONT.getSize2D() * 1.5f);
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
	 * Compute the horizontal center position for the out-of-view indicator.
	 * When both calipers are off the same edge, caliper "1" sits at 1/3 width
	 * and caliper "2" at 2/3 width so they don't overlap.
	 */
	private double computeIndicatorX(boolean isTop, Rectangle visible, double siblingYScreen) {
		double margin = 5.0;
		boolean siblingOnSameSide = Double.isFinite(siblingYScreen) && (
				(isTop && siblingYScreen < visible.y - margin) ||
				(!isTop && siblingYScreen > visible.y + visible.height + margin));
		if (siblingOnSameSide) {
			return visible.x + visible.width * ("1".equals(handleLabel) ? 1.0 / 3.0 : 2.0 / 3.0);
		}
		return visible.x + visible.width / 2.0;
	}

	public Rectangle2D.Double getIndicatorBounds(double caliperYScreen, Rectangle visible, double siblingYScreen) {
		if (visible == null) {
			return null;
		}

		double margin = 5.0;
		boolean isOutOfView = (caliperYScreen < visible.y - margin) ||
		                      (caliperYScreen > visible.y + visible.height + margin);
		if (!isOutOfView) {
			return null;
		}

		// Determine which edge to draw the arrow on
		boolean isTop = caliperYScreen < visible.y;
		double arrowY = isTop ? visible.y : visible.y + visible.height;
		double arrowX = computeIndicatorX(isTop, visible, siblingYScreen);
		
		// Calculate bounds: arrow area + label area
		double minX, maxX, minY, maxY;
		minX = arrowX - ARROW_SIZE / 2.0 - 10; // Add padding
		maxX = arrowX + ARROW_SIZE / 2.0 + 10;
		if (handleLabel != null && !handleLabel.isEmpty()) {
			// Account for label width (approximate)
			minX -= 30;
			maxX += 30;
		}
		
		if (isTop) {
			minY = arrowY;
			maxY = arrowY + ARROW_SIZE + LABEL_OFFSET;
			if (handleLabel != null && !handleLabel.isEmpty()) {
				// Add label height
				maxY += 30; // Approximate label height
			}
		} else {
			maxY = arrowY;
			minY = arrowY - ARROW_SIZE - LABEL_OFFSET;
			if (handleLabel != null && !handleLabel.isEmpty()) {
				// Subtract label height
				minY -= 30; // Approximate label height
			}
		}
		
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}
	
	/**
	 * Draw an arrow indicator at the edge of the viewport pointing toward the caliper line.
	 *
	 * @param g2Screen the graphics context in screen coordinates
	 * @param caliperY the Y position of the caliper line in screen coordinates
	 * @param visible the visible viewport rectangle
	 */
	private void drawOutOfViewIndicator(Graphics2D g2Screen, double caliperY, Rectangle visible, double siblingYScreen) {
		Color baseColor = isIndicatorHovered ? ColorConversion.brightenColor(lineColor, 60) : lineColor;
		Color indicatorColor = isSnapMode
				? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 128)
				: baseColor;
		g2Screen.setColor(indicatorColor);
		g2Screen.setStroke(new BasicStroke(ARROW_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		// Determine which edge to draw the arrow on
		boolean isTop = caliperY < visible.y;
		double arrowY = isTop ? visible.y : visible.y + visible.height;

		// Position arrow horizontally — offset from center when both calipers are on the same edge
		double arrowX = computeIndicatorX(isTop, visible, siblingYScreen);

		// Draw arrow pointing toward the caliper line
		Path2D.Double arrow = new Path2D.Double();
		if (isTop) {
			arrow.moveTo(arrowX, arrowY);
			arrow.lineTo(arrowX - ARROW_SIZE / 2, arrowY + ARROW_SIZE);
			arrow.lineTo(arrowX + ARROW_SIZE / 2, arrowY + ARROW_SIZE);
			arrow.closePath();
		} else {
			arrow.moveTo(arrowX, arrowY);
			arrow.lineTo(arrowX - ARROW_SIZE / 2, arrowY - ARROW_SIZE);
			arrow.lineTo(arrowX + ARROW_SIZE / 2, arrowY - ARROW_SIZE);
			arrow.closePath();
		}

		g2Screen.fill(arrow);
		g2Screen.draw(arrow);

		// Draw caliper number label and "click" hint beside the arrow
		FontRenderContext frc = g2Screen.getFontRenderContext();
		if (handleLabel != null && !handleLabel.isEmpty()) {
			g2Screen.setFont(INDICATOR_LABEL_FONT);
			Rectangle2D textBounds = INDICATOR_LABEL_FONT.getStringBounds(handleLabel, frc);
			double textWidth = textBounds.getWidth();
			double textHeight = textBounds.getHeight();
			double labelX = arrowX - textWidth / 2.0;
			double labelY = isTop
					? arrowY + ARROW_SIZE + LABEL_OFFSET + textHeight
					: arrowY - ARROW_SIZE - LABEL_OFFSET;
			g2Screen.setColor(indicatorColor);
			g2Screen.drawString(handleLabel, (float) labelX, (float) labelY);
		}

		// Always draw a small "click to return" hint beside the arrow
		Font hintFont = INDICATOR_LABEL_FONT.deriveFont(Font.PLAIN, INDICATOR_LABEL_FONT.getSize2D());
		g2Screen.setFont(hintFont);
		String hint = "click";
		Rectangle2D hintBounds = hintFont.getStringBounds(hint, frc);
		double hintWidth = hintBounds.getWidth();
		double hintX = arrowX - hintWidth / 2.0;
		double hintY = isTop
				? arrowY + ARROW_SIZE + LABEL_OFFSET + hintBounds.getHeight()
				: arrowY - ARROW_SIZE - LABEL_OFFSET;
		// Offset below/above the number label
		if (handleLabel != null && !handleLabel.isEmpty()) {
			g2Screen.setFont(INDICATOR_LABEL_FONT);
			Rectangle2D labelBounds = INDICATOR_LABEL_FONT.getStringBounds(handleLabel, frc);
			g2Screen.setFont(hintFont);
			hintY = isTop
					? hintY + labelBounds.getHeight() + 2
					: hintY - labelBounds.getHeight() - 2;
		}
		Color hintColor = new Color(indicatorColor.getRed(), indicatorColor.getGreen(),
				indicatorColor.getBlue(), isSnapMode ? 100 : 180);
		g2Screen.setColor(hintColor);
		g2Screen.drawString(hint, (float) hintX, (float) hintY);
	}
}

