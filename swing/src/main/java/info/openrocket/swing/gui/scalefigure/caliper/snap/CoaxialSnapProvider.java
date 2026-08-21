package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.Coaxial;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides snap targets for Coaxial components.
 * 
 * For side/top view:
 * - Vertical caliper mode: left edge (x = component start), right edge (x = component end)
 * - Horizontal caliper mode: top edge (y = +radius), bottom edge (y = -radius)
 * 
 * For back view:
 * - Vertical caliper mode: left/right points on outer and inner circles (z = ±radius)
 * - Horizontal caliper mode: top/bottom points on outer and inner circles (y = ±radius)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CoaxialSnapProvider implements ComponentSnapProvider {
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		// Coaxial is an interface, not a class, so we return RocketComponent.class
		// The actual Coaxial check is performed in getSnapTargets()
		return RocketComponent.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof Coaxial coaxial)) {
			return targets;
		}

		double length = component.getLength();
		double outerRadius = coaxial.getOuterRadius();
		double innerRadius = coaxial.getInnerRadius();
		
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: coaxial component appears as a rectangle
			getSideViewSnapTargets(component, length, outerRadius, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: coaxial component appears as circles
			getBackViewSnapTargets(component, outerRadius, innerRadius, transformation, caliperMode, targets);
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 * The rendering code transforms the origin, then draws a rectangle at (center.getX(), center.getY()-radius, length, 2*radius).
	 * We need to match this exactly.
	 */
	private void getSideViewSnapTargets(RocketComponent component, double length, double radius,
									   Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		// Match the rendering code: transform the origin to get the center
		CoordinateIF centerIF = transformation.transform(Coordinate.ZERO);
		Coordinate center = new Coordinate(centerIF.getX(), centerIF.getY(), centerIF.getZ());
		
		// The rendering draws a rectangle at (center.getX(), center.getY()-radius, length, 2*radius)
		// So the edges are:
		// - Left edge: x = center.getX(), from y = center.getY()-radius to y = center.getY()+radius
		// - Right edge: x = center.getX()+length, from y = center.getY()-radius to y = center.getY()+radius
		// - Top edge: from x = center.getX() to x = center.getX()+length, at y = center.getY()+radius
		// - Bottom edge: from x = center.getX() to x = center.getX()+length, at y = center.getY()-radius
		
		Coordinate leftBottom = new Coordinate(center.getX(), center.getY() - radius, center.getZ());
		Coordinate leftTop = new Coordinate(center.getX(), center.getY() + radius, center.getZ());
		Coordinate rightBottom = new Coordinate(center.getX() + length, center.getY() - radius, center.getZ());
		Coordinate rightTop = new Coordinate(center.getX() + length, center.getY() + radius, center.getZ());
		
		if (caliperMode == CaliperManager.CaliperMode.VERTICAL) {
			// Vertical caliper mode: snap to left and right edges (vertical lines)
			// Left edge: from leftBottom to leftTop
			targets.add(new CaliperSnapTarget(leftBottom, leftTop,
					CaliperManager.CaliperMode.VERTICAL, component, "Left edge"));
			// Right edge: from rightBottom to rightTop
			targets.add(new CaliperSnapTarget(rightBottom, rightTop,
					CaliperManager.CaliperMode.VERTICAL, component, "Right edge"));
		} else {
			// Horizontal caliper mode: snap to top and bottom edges (horizontal lines)
			// Top edge: from leftTop to rightTop
			targets.add(new CaliperSnapTarget(leftTop, rightTop,
					CaliperManager.CaliperMode.HORIZONTAL, component, "Top edge"));
			// Bottom edge: from leftBottom to rightBottom
			targets.add(new CaliperSnapTarget(leftBottom, rightBottom,
					CaliperManager.CaliperMode.HORIZONTAL, component, "Bottom edge"));
		}
	}
	
	/**
	 * Get snap targets for back view.
	 */
	private void getBackViewSnapTargets(RocketComponent component, double outerRadius, double innerRadius,
									  Transformation transformation,
									  CaliperManager.CaliperMode caliperMode,
									  List<CaliperSnapTarget> targets) {
		// In back view, the coaxial component appears as circles centered at the component origin
		// The transformation gives us the absolute position
		CoordinateIF centerIF = transformation.transform(Coordinate.ZERO);
		Coordinate center = new Coordinate(centerIF.getX(), centerIF.getY(), centerIF.getZ());
		
		// Snap to left and right points on circles
		// Outer circle: left point (z = -outerRadius), right point (z = +outerRadius)
		Coordinate outerLeft = new Coordinate(center.getX(), center.getY(), center.getZ() - outerRadius);
		Coordinate outerRight = new Coordinate(center.getX(), center.getY(), center.getZ() + outerRadius);
		targets.add(new CaliperSnapTarget(outerLeft, CaliperManager.CaliperMode.BOTH, component, "Outer circle left"));
		targets.add(new CaliperSnapTarget(outerRight, CaliperManager.CaliperMode.BOTH, component, "Outer circle right"));

		// Inner circle - currently not used because the inner circle is not drawn in back view
		/*if (innerRadius > 0 && innerRadius < outerRadius) {
			Coordinate innerLeft = new Coordinate(center.getX(), center.getY(), center.getZ() - innerRadius);
			Coordinate innerRight = new Coordinate(center.getX(), center.getY(), center.getZ() + innerRadius);
			targets.add(new CaliperSnapTarget(innerLeft, CaliperManager.CaliperMode.BOTH, tube, "Inner circle left"));
			targets.add(new CaliperSnapTarget(innerRight, CaliperManager.CaliperMode.BOTH, tube, "Inner circle right"));
		}*/

		// Snap to top and bottom points on circles
		// Outer circle: top point (y = +outerRadius), bottom point (y = -outerRadius)
		Coordinate outerTop = new Coordinate(center.getX(), center.getY() + outerRadius, center.getZ());
		Coordinate outerBottom = new Coordinate(center.getX(), center.getY() - outerRadius, center.getZ());
		targets.add(new CaliperSnapTarget(outerTop, CaliperManager.CaliperMode.BOTH, component, "Outer circle top"));
		targets.add(new CaliperSnapTarget(outerBottom, CaliperManager.CaliperMode.BOTH, component, "Outer circle bottom"));

		// Inner circle - currently not used because the inner circle is not drawn in back view
		/*if (innerRadius > 0 && innerRadius < outerRadius) {
			Coordinate innerTop = new Coordinate(center.getX(), center.getY() + innerRadius, center.getZ());
			Coordinate innerBottom = new Coordinate(center.getX(), center.getY() - innerRadius, center.getZ());
			targets.add(new CaliperSnapTarget(innerTop, CaliperManager.CaliperMode.BOTH, tube, "Inner circle top"));
			targets.add(new CaliperSnapTarget(innerBottom, CaliperManager.CaliperMode.BOTH, tube, "Inner circle bottom"));
		}*/
	}
}

