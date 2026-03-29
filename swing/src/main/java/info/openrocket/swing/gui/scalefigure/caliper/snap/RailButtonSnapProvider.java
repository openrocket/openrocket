package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides snap targets for RailButton components.
 * 
 * For side/top view:
 * - Adds vertical line snap targets for the left and right edges of each section (base, inner, flange)
 * - Adds point snap targets for the circles (ellipses) at the top and bottom of each section
 * - Accounts for rotation (both view rotation and button angle offset)
 * 
 * For back view:
 * - Adds snap targets for the edges of all rectangles (base, inner, flange)
 * - Accounts for rotation
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RailButtonSnapProvider implements ComponentSnapProvider {
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		return RailButton.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof RailButton railButton)) {
			return targets;
		}
		
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: vertical lines and circle points
			getSideViewSnapTargets(railButton, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: rectangle edges
			getBackViewSnapTargets(railButton, transformation, caliperMode, targets);
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 * Adds vertical lines for the left/right edges of each section, and points for the circles.
	 */
	private void getSideViewSnapTargets(RailButton railButton, Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		final double baseHeight = railButton.getBaseHeight();
		final double innerHeight = railButton.getInnerHeight();
		final double flangeHeight = railButton.getFlangeHeight();
		
		final double outerDiameter = railButton.getOuterDiameter();
		final double outerRadius = outerDiameter / 2;
		final double innerDiameter = railButton.getInnerDiameter();
		final double innerRadius = innerDiameter / 2;
		
		// Instance absolute location
		final CoordinateIF locIF = transformation.transform(Coordinate.ZERO);
		final Coordinate loc = new Coordinate(locIF.getX(), locIF.getY(), locIF.getZ());
		
		// Calculate view rotation and angle offset (matching RailButtonShapes.getShapesSide)
		final CoordinateIF unitOrientationIF = transformation.transform(new Coordinate(0, 1, 0));
		final double view_rotation_rad = -Math.atan2(unitOrientationIF.getY(), unitOrientationIF.getZ()) + Math.PI / 2;
		final double angle_offset_rad = railButton.getAngleOffset();
		final double sinr = Math.abs(Math.sin(angle_offset_rad + view_rotation_rad));
		final double cosr = Math.cos(angle_offset_rad + view_rotation_rad);
		
		final double baseHeightcos = baseHeight * cosr;
		final double innerHeightcos = innerHeight * cosr;
		final double flangeHeightcos = flangeHeight * cosr;
		
		// Base section
		if (baseHeight > 0) {
			final double centerY = loc.getY();
			final double topY = centerY + baseHeightcos;
			final double bottomY = centerY;
			
			// Vertical lines (left and right edges)
			if (caliperMode == CaliperManager.CaliperMode.VERTICAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
				// Left edge
				Coordinate leftBottom = new Coordinate(loc.getX() - outerRadius, bottomY, loc.getZ());
				Coordinate leftTop = new Coordinate(loc.getX() - outerRadius, topY, loc.getZ());
				targets.add(new CaliperSnapTarget(leftBottom, leftTop, 
												 CaliperManager.CaliperMode.VERTICAL, 
												 railButton, "Base left edge"));
				
				// Right edge
				Coordinate rightBottom = new Coordinate(loc.getX() + outerRadius, bottomY, loc.getZ());
				Coordinate rightTop = new Coordinate(loc.getX() + outerRadius, topY, loc.getZ());
				targets.add(new CaliperSnapTarget(rightBottom, rightTop, 
												 CaliperManager.CaliperMode.VERTICAL, 
												 railButton, "Base right edge"));
			}
			
			// Circle points (top and bottom ellipses) - add points at top, left, right, bottom
			// Top ellipse: top, left, right, bottom points
			Coordinate topEllipseTop = new Coordinate(loc.getX(), topY - outerRadius * sinr, loc.getZ());
			Coordinate topEllipseLeft = new Coordinate(loc.getX() - outerRadius, topY, loc.getZ());
			Coordinate topEllipseRight = new Coordinate(loc.getX() + outerRadius, topY, loc.getZ());
			Coordinate topEllipseBottom = new Coordinate(loc.getX(), topY + outerRadius * sinr, loc.getZ());
			targets.add(new CaliperSnapTarget(topEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Base top ellipse top"));
			targets.add(new CaliperSnapTarget(topEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Base top ellipse left"));
			targets.add(new CaliperSnapTarget(topEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Base top ellipse right"));
			targets.add(new CaliperSnapTarget(topEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Base top ellipse bottom"));
			
			// Bottom ellipse: top, left, right, bottom points
			Coordinate bottomEllipseTop = new Coordinate(loc.getX(), bottomY - outerRadius * sinr, loc.getZ());
			Coordinate bottomEllipseLeft = new Coordinate(loc.getX() - outerRadius, bottomY, loc.getZ());
			Coordinate bottomEllipseRight = new Coordinate(loc.getX() + outerRadius, bottomY, loc.getZ());
			Coordinate bottomEllipseBottom = new Coordinate(loc.getX(), bottomY + outerRadius * sinr, loc.getZ());
			targets.add(new CaliperSnapTarget(bottomEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Base bottom ellipse top"));
			targets.add(new CaliperSnapTarget(bottomEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Base bottom ellipse left"));
			targets.add(new CaliperSnapTarget(bottomEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Base bottom ellipse right"));
			targets.add(new CaliperSnapTarget(bottomEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Base bottom ellipse bottom"));
		}
		
		// Inner section
		final double innerCenterY = loc.getY() + baseHeightcos;
		final double innerTopY = innerCenterY + innerHeightcos;
		final double innerBottomY = innerCenterY;
		
		// Vertical lines (left and right edges)
		if (caliperMode == CaliperManager.CaliperMode.VERTICAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
			// Left edge
			Coordinate innerLeftBottom = new Coordinate(loc.getX() - innerRadius, innerBottomY, loc.getZ());
			Coordinate innerLeftTop = new Coordinate(loc.getX() - innerRadius, innerTopY, loc.getZ());
			targets.add(new CaliperSnapTarget(innerLeftBottom, innerLeftTop, 
											 CaliperManager.CaliperMode.VERTICAL, 
											 railButton, "Inner left edge"));
			
			// Right edge
			Coordinate innerRightBottom = new Coordinate(loc.getX() + innerRadius, innerBottomY, loc.getZ());
			Coordinate innerRightTop = new Coordinate(loc.getX() + innerRadius, innerTopY, loc.getZ());
			targets.add(new CaliperSnapTarget(innerRightBottom, innerRightTop, 
											 CaliperManager.CaliperMode.VERTICAL, 
											 railButton, "Inner right edge"));
		}
		
		// Circle points (top and bottom ellipses) - add points at top, left, right, bottom
		// Top ellipse: top, left, right, bottom points
		Coordinate innerTopEllipseTop = new Coordinate(loc.getX(), innerTopY - innerRadius * sinr, loc.getZ());
		Coordinate innerTopEllipseLeft = new Coordinate(loc.getX() - innerRadius, innerTopY, loc.getZ());
		Coordinate innerTopEllipseRight = new Coordinate(loc.getX() + innerRadius, innerTopY, loc.getZ());
		Coordinate innerTopEllipseBottom = new Coordinate(loc.getX(), innerTopY + innerRadius * sinr, loc.getZ());
		targets.add(new CaliperSnapTarget(innerTopEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Inner top ellipse top"));
		targets.add(new CaliperSnapTarget(innerTopEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Inner top ellipse left"));
		targets.add(new CaliperSnapTarget(innerTopEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Inner top ellipse right"));
		targets.add(new CaliperSnapTarget(innerTopEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Inner top ellipse bottom"));
		
		// Bottom ellipse: top, left, right, bottom points
		Coordinate innerBottomEllipseTop = new Coordinate(loc.getX(), innerBottomY - innerRadius * sinr, loc.getZ());
		Coordinate innerBottomEllipseLeft = new Coordinate(loc.getX() - innerRadius, innerBottomY, loc.getZ());
		Coordinate innerBottomEllipseRight = new Coordinate(loc.getX() + innerRadius, innerBottomY, loc.getZ());
		Coordinate innerBottomEllipseBottom = new Coordinate(loc.getX(), innerBottomY + innerRadius * sinr, loc.getZ());
		targets.add(new CaliperSnapTarget(innerBottomEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Inner bottom ellipse top"));
		targets.add(new CaliperSnapTarget(innerBottomEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Inner bottom ellipse left"));
		targets.add(new CaliperSnapTarget(innerBottomEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Inner bottom ellipse right"));
		targets.add(new CaliperSnapTarget(innerBottomEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Inner bottom ellipse bottom"));
		
		// Flange section
		if (flangeHeight > 0) {
			final double flangeCenterY = loc.getY() + baseHeightcos + innerHeightcos;
			final double flangeTopY = flangeCenterY + flangeHeightcos;
			final double flangeBottomY = flangeCenterY;
			
			// Vertical lines (left and right edges)
			if (caliperMode == CaliperManager.CaliperMode.VERTICAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
				// Left edge
				Coordinate flangeLeftBottom = new Coordinate(loc.getX() - outerRadius, flangeBottomY, loc.getZ());
				Coordinate flangeLeftTop = new Coordinate(loc.getX() - outerRadius, flangeTopY, loc.getZ());
				targets.add(new CaliperSnapTarget(flangeLeftBottom, flangeLeftTop, 
												 CaliperManager.CaliperMode.VERTICAL, 
												 railButton, "Flange left edge"));
				
				// Right edge
				Coordinate flangeRightBottom = new Coordinate(loc.getX() + outerRadius, flangeBottomY, loc.getZ());
				Coordinate flangeRightTop = new Coordinate(loc.getX() + outerRadius, flangeTopY, loc.getZ());
				targets.add(new CaliperSnapTarget(flangeRightBottom, flangeRightTop, 
												 CaliperManager.CaliperMode.VERTICAL, 
												 railButton, "Flange right edge"));
			}
			
			// Circle points (top and bottom ellipses) - add points at top, left, right, bottom
			// Top ellipse: top, left, right, bottom points
			Coordinate flangeTopEllipseTop = new Coordinate(loc.getX(), flangeTopY - outerRadius * sinr, loc.getZ());
			Coordinate flangeTopEllipseLeft = new Coordinate(loc.getX() - outerRadius, flangeTopY, loc.getZ());
			Coordinate flangeTopEllipseRight = new Coordinate(loc.getX() + outerRadius, flangeTopY, loc.getZ());
			Coordinate flangeTopEllipseBottom = new Coordinate(loc.getX(), flangeTopY + outerRadius * sinr, loc.getZ());
			targets.add(new CaliperSnapTarget(flangeTopEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Flange top ellipse top"));
			targets.add(new CaliperSnapTarget(flangeTopEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Flange top ellipse left"));
			targets.add(new CaliperSnapTarget(flangeTopEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Flange top ellipse right"));
			targets.add(new CaliperSnapTarget(flangeTopEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Flange top ellipse bottom"));
			
			// Bottom ellipse: top, left, right, bottom points
			Coordinate flangeBottomEllipseTop = new Coordinate(loc.getX(), flangeBottomY - outerRadius * sinr, loc.getZ());
			Coordinate flangeBottomEllipseLeft = new Coordinate(loc.getX() - outerRadius, flangeBottomY, loc.getZ());
			Coordinate flangeBottomEllipseRight = new Coordinate(loc.getX() + outerRadius, flangeBottomY, loc.getZ());
			Coordinate flangeBottomEllipseBottom = new Coordinate(loc.getX(), flangeBottomY + outerRadius * sinr, loc.getZ());
			targets.add(new CaliperSnapTarget(flangeBottomEllipseTop, CaliperManager.CaliperMode.BOTH, railButton, "Flange bottom ellipse top"));
			targets.add(new CaliperSnapTarget(flangeBottomEllipseLeft, CaliperManager.CaliperMode.BOTH, railButton, "Flange bottom ellipse left"));
			targets.add(new CaliperSnapTarget(flangeBottomEllipseRight, CaliperManager.CaliperMode.BOTH, railButton, "Flange bottom ellipse right"));
			targets.add(new CaliperSnapTarget(flangeBottomEllipseBottom, CaliperManager.CaliperMode.BOTH, railButton, "Flange bottom ellipse bottom"));
		}
	}
	
	/**
	 * Get snap targets for back view.
	 * Adds snap targets for the edges of all rectangles (base, inner, flange).
	 */
	private void getBackViewSnapTargets(RailButton railButton, Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		final double baseHeight = railButton.getBaseHeight();
		final double innerHeight = railButton.getInnerHeight();
		final double flangeHeight = railButton.getFlangeHeight();
		
		final double outerDiameter = railButton.getOuterDiameter();
		final double outerRadius = outerDiameter / 2;
		final double innerDiameter = railButton.getInnerDiameter();
		final double innerRadius = innerDiameter / 2;
		
		// Instance absolute location
		final CoordinateIF locIF = transformation.transform(Coordinate.ZERO);
		final Coordinate loc = new Coordinate(locIF.getX(), locIF.getY(), locIF.getZ());
		
		// Calculate view rotation and angle offset (matching RailButtonShapes.getShapesBack)
		final CoordinateIF unitOrientationIF = transformation.transform(new Coordinate(0, 1, 0));
		final double view_rotation_rad = -Math.atan2(unitOrientationIF.getY(), unitOrientationIF.getZ()) + Math.PI / 2;
		final double angle_offset_rad = railButton.getAngleOffset();
		final double combined_angle_rad = angle_offset_rad + view_rotation_rad;
		
		final double sinr = Math.sin(combined_angle_rad);
		final double cosr = Math.cos(combined_angle_rad);
		
		// Base rectangle
		if (baseHeight > 0) {
			addRotatedRectangleSnapTargets(loc.getZ(), loc.getY(), outerRadius, baseHeight, combined_angle_rad,
										  railButton, caliperMode, targets, "Base");
		}
		
		// Inner rectangle
		final double delta_r = baseHeight;
		final double delta_y = delta_r * cosr;
		final double delta_z = delta_r * sinr;
		addRotatedRectangleSnapTargets(loc.getZ() + delta_z, loc.getY() + delta_y, innerRadius, innerHeight, combined_angle_rad,
									  railButton, caliperMode, targets, "Inner");
		
		// Flange rectangle
		if (flangeHeight > 0) {
			final double flange_delta_r = baseHeight + innerHeight;
			final double flange_delta_y = flange_delta_r * cosr;
			final double flange_delta_z = flange_delta_r * sinr;
			addRotatedRectangleSnapTargets(loc.getZ() + flange_delta_z, loc.getY() + flange_delta_y, outerRadius, flangeHeight, combined_angle_rad,
										  railButton, caliperMode, targets, "Flange");
		}
	}
	
	/**
	 * Add snap targets for a rotated rectangle.
	 * The rectangle is defined by center (x, y), radius (half-width), height, and rotation angle.
	 * In back view, Z is screen X, Y is screen Y.
	 */
	private void addRotatedRectangleSnapTargets(double centerZ, double centerY, double radius, double height,
												double angle_rad, RailButton railButton,
												CaliperManager.CaliperMode caliperMode,
												List<CaliperSnapTarget> targets, String prefix) {
		final double sinr = Math.sin(angle_rad);
		final double cosr = Math.cos(angle_rad);
		
		// Calculate the four corners of the rotated rectangle (matching getRotatedRectangle)
		// In back view, Z is screen X, Y is screen Y, X is 0 (looking along X axis)
		// Corner 1: (x-radius*cosr, y+radius*sinr) where x=centerZ, y=centerY
		Coordinate corner1 = new Coordinate(0, centerY + radius * sinr, centerZ - radius * cosr);
		
		// Corner 2: (x-radius*cosr+height*sinr, y+radius*sinr+height*cosr)
		Coordinate corner2 = new Coordinate(0, centerY + radius * sinr + height * cosr, centerZ - radius * cosr + height * sinr);
		
		// Corner 3: (x+radius*cosr+height*sinr, y-radius*sinr+height*cosr)
		Coordinate corner3 = new Coordinate(0, centerY - radius * sinr + height * cosr, centerZ + radius * cosr + height * sinr);
		
		// Corner 4: (x+radius*cosr, y-radius*sinr)
		Coordinate corner4 = new Coordinate(0, centerY - radius * sinr, centerZ + radius * cosr);
		
		// Add snap targets for the four corners (points)
		targets.add(new CaliperSnapTarget(corner1, CaliperManager.CaliperMode.BOTH, railButton, prefix + " corner 1"));
		targets.add(new CaliperSnapTarget(corner2, CaliperManager.CaliperMode.BOTH, railButton, prefix + " corner 2"));
		targets.add(new CaliperSnapTarget(corner3, CaliperManager.CaliperMode.BOTH, railButton, prefix + " corner 3"));
		targets.add(new CaliperSnapTarget(corner4, CaliperManager.CaliperMode.BOTH, railButton, prefix + " corner 4"));
	}
}

