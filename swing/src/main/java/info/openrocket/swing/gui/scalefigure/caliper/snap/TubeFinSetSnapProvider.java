package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides snap targets for TubeFinSet components.
 * 
 * TubeFinSet has a special geometry: each tube fin is positioned at a radius from the body center,
 * and the component's local coordinate system has the tube fin center at (0, outerRadius, 0)
 * relative to the component origin (which is at the body surface).
 * 
 * For side/top view:
 * - Vertical caliper mode: left edge (x = component start), right edge (x = component end)
 * - Horizontal caliper mode: top edge (y = +outerRadius + outerRadius), bottom edge (y = +outerRadius - outerRadius)
 * 
 * For back view:
 * - All four cardinal points on the outer circle (left, right, top, bottom)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class TubeFinSetSnapProvider implements ComponentSnapProvider {
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		return TubeFinSet.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof TubeFinSet finSet)) {
			return targets;
		}

		double length = component.getLength();
		double outerRadius = finSet.getOuterRadius();
		double innerRadius = finSet.getInnerRadius();
		
		// TubeFinSet local coordinate system: tube fin center is at (0, outerRadius, 0)
		// relative to component origin (body surface)
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: tube fin appears as a rectangle
			getSideViewSnapTargets(component, length, outerRadius, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: tube fin appears as a circle
			getBackViewSnapTargets(component, outerRadius, innerRadius, transformation, caliperMode, targets);
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 * The tube fin center is at (0, outerRadius, 0) in local coordinates.
	 * The rendering code transforms (0, outerRadius, 0) to get the center,
	 * then draws a rectangle at (center.getX(), center.getY()-outerRadius, length, 2*outerRadius).
	 */
	private void getSideViewSnapTargets(RocketComponent component, double length, double radius,
									   Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		// Transform the tube fin center (same as TubeFinSetShapes does)
		CoordinateIF centerIF = transformation.transform(new Coordinate(0, radius, 0));
		Coordinate center = new Coordinate(centerIF.getX(), centerIF.getY(), centerIF.getZ());
		
		// Calculate the four corners of the rectangle
		// Rectangle is drawn at (center.getX(), center.getY()-radius, length, 2*radius)
		// So:
		// - Left edge: x = center.getX(), from y = center.getY() - radius to y = center.getY() + radius
		// - Right edge: x = center.getX() + length, from y = center.getY() - radius to y = center.getY() + radius
		// - Top edge: from x = center.getX() to x = center.getX() + length, at y = center.getY() + radius
		// - Bottom edge: from x = center.getX() to x = center.getX() + length, at y = center.getY() - radius
		
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
	 * The tube fin center is at (0, outerRadius, 0) in local coordinates.
	 */
	private void getBackViewSnapTargets(RocketComponent component, double outerRadius, double innerRadius,
									  Transformation transformation,
									  CaliperManager.CaliperMode caliperMode,
									  List<CaliperSnapTarget> targets) {
		// In back view, the tube fin appears as a circle centered at the tube fin center
		// The transformation gives us the absolute position of the component origin
		// We need to offset by (0, outerRadius, 0) to get the tube fin center
		CoordinateIF tubeFinCenterIF = transformation.transform(new Coordinate(0, outerRadius, 0));
		Coordinate tubeFinCenter = new Coordinate(tubeFinCenterIF.getX(), tubeFinCenterIF.getY(), tubeFinCenterIF.getZ());
		
		// Snap to left and right points on outer circle
		Coordinate outerLeft = new Coordinate(tubeFinCenter.getX(), tubeFinCenter.getY(), tubeFinCenter.getZ() - outerRadius);
		Coordinate outerRight = new Coordinate(tubeFinCenter.getX(), tubeFinCenter.getY(), tubeFinCenter.getZ() + outerRadius);
		targets.add(new CaliperSnapTarget(outerLeft, CaliperManager.CaliperMode.BOTH, component, "Outer circle left"));
		targets.add(new CaliperSnapTarget(outerRight, CaliperManager.CaliperMode.BOTH, component, "Outer circle right"));

		// Snap to top and bottom points on outer circle
		Coordinate outerTop = new Coordinate(tubeFinCenter.getX(), tubeFinCenter.getY() + outerRadius, tubeFinCenter.getZ());
		Coordinate outerBottom = new Coordinate(tubeFinCenter.getX(), tubeFinCenter.getY() - outerRadius, tubeFinCenter.getZ());
		targets.add(new CaliperSnapTarget(outerTop, CaliperManager.CaliperMode.BOTH, component, "Outer circle top"));
		targets.add(new CaliperSnapTarget(outerBottom, CaliperManager.CaliperMode.BOTH, component, "Outer circle bottom"));
	}
}

