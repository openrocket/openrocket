package info.openrocket.swing.gui.figureelements;

import info.openrocket.swing.gui.scalefigure.caliper.snap.CaliperSnapTarget;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.core.util.Coordinate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Figure element for highlighting snap targets when hovering in snap mode.
 * Draws highlighted lines or points to show where caliper lines can snap.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SnapTargetHighlight implements FigureElement {

	private static final float LINE_WIDTH = 6.0f;
	private static final float POINT_RADIUS = 6.0f;

	private static Color highlightColor;
	private static Color highlightBorderColor;

	static {
		initColors();
		UITheme.Theme.addUIThemeChangeListener(SnapTargetHighlight::updateColors);
	}

	private final CaliperSnapTarget target;
	private final RocketPanel.VIEW_TYPE viewType;

	/**
	 * Create a highlight for a snap target.
	 *
	 * @param target the snap target to highlight
	 * @param viewType the current view type (needed for coordinate mapping)
	 */
	public SnapTargetHighlight(CaliperSnapTarget target, RocketPanel.VIEW_TYPE viewType) {
		this.target = target;
		this.viewType = viewType;
	}

	private static void initColors() {
		updateColors();
	}

	public static void updateColors() {
		highlightColor = GUIUtil.getUITheme().getCaliperSnapHighlightColor();
		highlightBorderColor = new Color(highlightColor.getRed(), highlightColor.getGreen(),
				highlightColor.getBlue(), 255);
	}

	@Override
	public void paint(Graphics2D g2, double scale) {
		paint(g2, scale, null);
	}

	@Override
	public void paint(Graphics2D g2, double scale, Rectangle visible) {
		if (target == null) {
			return;
		}

		Graphics2D g2Screen = (Graphics2D) g2.create();
		try {
			AffineTransform transform = g2.getTransform();
			
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				// Draw a highlighted point
				Coordinate pos = target.getPosition();
				// In back view, screen X maps to model Z, screen Y maps to model Y
				// In side/top view, screen X maps to model X, screen Y maps to model Y
				Point2D.Double modelPoint;
				if (viewType == RocketPanel.VIEW_TYPE.BackView) {
					modelPoint = new Point2D.Double(pos.getZ(), pos.getY());
				} else {
					modelPoint = new Point2D.Double(pos.getX(), pos.getY());
				}
				Point2D.Double screenPoint = new Point2D.Double();
				transform.transform(modelPoint, screenPoint);

				if (Double.isNaN(screenPoint.x) || Double.isInfinite(screenPoint.x) ||
						Double.isNaN(screenPoint.y) || Double.isInfinite(screenPoint.y)) {
					return;
				}

				g2Screen.setTransform(new AffineTransform());
				g2Screen.setColor(highlightColor);
				g2Screen.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
				// Draw a circle for the point
				double radius = POINT_RADIUS;
				Ellipse2D.Double circle = new Ellipse2D.Double(
						screenPoint.x - radius, screenPoint.y - radius,
						2 * radius, 2 * radius);
				g2Screen.fill(circle);
				
				// Draw a border
				g2Screen.setColor(highlightBorderColor);
				g2Screen.draw(circle);
			} else {
				// Draw a highlighted line
				Coordinate start = target.getLineStart();
				Coordinate end = target.getLineEnd();
				
				// In back view, screen X maps to model Z, screen Y maps to model Y
				// In side/top view, screen X maps to model X, screen Y maps to model Y
				Point2D.Double startModel;
				Point2D.Double endModel;
				if (viewType == RocketPanel.VIEW_TYPE.BackView) {
					startModel = new Point2D.Double(start.getZ(), start.getY());
					endModel = new Point2D.Double(end.getZ(), end.getY());
				} else {
					startModel = new Point2D.Double(start.getX(), start.getY());
					endModel = new Point2D.Double(end.getX(), end.getY());
				}
				Point2D.Double startScreen = new Point2D.Double();
				Point2D.Double endScreen = new Point2D.Double();
				transform.transform(startModel, startScreen);
				transform.transform(endModel, endScreen);

				if (Double.isNaN(startScreen.x) || Double.isInfinite(startScreen.x) ||
						Double.isNaN(endScreen.x) || Double.isInfinite(endScreen.x)) {
					return;
				}

				g2Screen.setTransform(new AffineTransform());
				g2Screen.setColor(highlightColor);
				g2Screen.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
				// Draw the line
				Line2D.Double line = new Line2D.Double(startScreen.x, startScreen.y, endScreen.x, endScreen.y);
				g2Screen.draw(line);
			}
		} finally {
			g2Screen.dispose();
		}
	}
}

