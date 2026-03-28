package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides snap targets for Transition components (cones, ogives, etc.).
 * 
 * For side/top view:
 * - Vertical caliper mode: 
 *   - Fore end edge (x = 0): line from y=-foreRadius to y=+foreRadius, or point if radius is 0
 *   - Aft end edge (x = length): line from y=-aftRadius to y=+aftRadius, or point if radius is 0
 *   - Fore shoulder: horizontal line at y=±foreShoulderRadius from x=-foreShoulderLength to x=0
 *   - Aft shoulder: horizontal line at y=±aftShoulderRadius from x=length to x=length+aftShoulderLength
 * - Horizontal caliper mode:
 *   - Fore shoulder: horizontal line at y=±foreShoulderRadius from x=-foreShoulderLength to x=0
 *   - Aft shoulder: horizontal line at y=±aftShoulderRadius from x=length to x=length+aftShoulderLength
 * 
 * For back view:
 * - Vertical caliper mode: left/right points on outer circle at fore and aft ends (z = ±radius)
 * - Horizontal caliper mode: top/bottom points on outer circle at fore and aft ends (y = ±radius)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class TransitionSnapProvider implements ComponentSnapProvider {
	
	private static final double MINFEATURE = 0.001; // Minimum feature size to consider
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		return Transition.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof Transition transition)) {
			return targets;
		}

		double length = transition.getLength();
		double foreRadius = transition.getForeRadius();
		double aftRadius = transition.getAftRadius();
		
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: transition appears as a shape with edges and shoulders
			getSideViewSnapTargets(transition, length, foreRadius, aftRadius, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: transition appears as circles at fore and aft ends
			getBackViewSnapTargets(transition, foreRadius, aftRadius, transformation, caliperMode, targets);
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 */
	private void getSideViewSnapTargets(Transition transition, double length, double foreRadius, double aftRadius,
									   Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		
		if (caliperMode == CaliperManager.CaliperMode.VERTICAL) {
			// Vertical caliper mode: snap to fore and aft end edges (vertical lines or points)
			
			// Fore end (x = 0)
			if (foreRadius > MINFEATURE) {
				// Draw vertical line from y=-foreRadius to y=+foreRadius at x=0
				CoordinateIF foreBottomIF = transformation.transform(new Coordinate(0, -foreRadius, 0));
				CoordinateIF foreTopIF = transformation.transform(new Coordinate(0, foreRadius, 0));
				Coordinate foreBottom = new Coordinate(foreBottomIF.getX(), foreBottomIF.getY(), foreBottomIF.getZ());
				Coordinate foreTop = new Coordinate(foreTopIF.getX(), foreTopIF.getY(), foreTopIF.getZ());
				targets.add(new CaliperSnapTarget(foreBottom, foreTop,
						CaliperManager.CaliperMode.VERTICAL, transition, "Fore end"));
			} else {
				// Radius is 0 or very small, draw a point
				CoordinateIF foreCenterIF = transformation.transform(new Coordinate(0, 0, 0));
				Coordinate foreCenter = new Coordinate(foreCenterIF.getX(), foreCenterIF.getY(), foreCenterIF.getZ());
				targets.add(new CaliperSnapTarget(foreCenter, CaliperManager.CaliperMode.VERTICAL, transition, "Fore end"));
			}
			
			// Aft end (x = length)
			if (aftRadius > MINFEATURE) {
				// Draw vertical line from y=-aftRadius to y=+aftRadius at x=length
				CoordinateIF aftBottomIF = transformation.transform(new Coordinate(length, -aftRadius, 0));
				CoordinateIF aftTopIF = transformation.transform(new Coordinate(length, aftRadius, 0));
				Coordinate aftBottom = new Coordinate(aftBottomIF.getX(), aftBottomIF.getY(), aftBottomIF.getZ());
				Coordinate aftTop = new Coordinate(aftTopIF.getX(), aftTopIF.getY(), aftTopIF.getZ());
				targets.add(new CaliperSnapTarget(aftBottom, aftTop,
						CaliperManager.CaliperMode.VERTICAL, transition, "Aft end"));
			} else {
				// Radius is 0 or very small, draw a point
				CoordinateIF aftCenterIF = transformation.transform(new Coordinate(length, 0, 0));
				Coordinate aftCenter = new Coordinate(aftCenterIF.getX(), aftCenterIF.getY(), aftCenterIF.getZ());
				targets.add(new CaliperSnapTarget(aftCenter, CaliperManager.CaliperMode.VERTICAL, transition, "Aft end"));
			}
		} else {
			// Horizontal caliper mode: snap to top and bottom corners of fore and aft ends
			
			// Fore end corners (x = 0)
			if (foreRadius > MINFEATURE) {
				// Top corner: (0, foreRadius)
				CoordinateIF foreTopCornerIF = transformation.transform(new Coordinate(0, foreRadius, 0));
				Coordinate foreTopCorner = new Coordinate(foreTopCornerIF.getX(), foreTopCornerIF.getY(), foreTopCornerIF.getZ());
				targets.add(new CaliperSnapTarget(foreTopCorner, CaliperManager.CaliperMode.HORIZONTAL, transition, "Fore end top"));
				
				// Bottom corner: (0, -foreRadius)
				CoordinateIF foreBottomCornerIF = transformation.transform(new Coordinate(0, -foreRadius, 0));
				Coordinate foreBottomCorner = new Coordinate(foreBottomCornerIF.getX(), foreBottomCornerIF.getY(), foreBottomCornerIF.getZ());
				targets.add(new CaliperSnapTarget(foreBottomCorner, CaliperManager.CaliperMode.HORIZONTAL, transition, "Fore end bottom"));
			} else {
				// Radius is 0 or very small, draw a point at center
				CoordinateIF foreCenterIF = transformation.transform(new Coordinate(0, 0, 0));
				Coordinate foreCenter = new Coordinate(foreCenterIF.getX(), foreCenterIF.getY(), foreCenterIF.getZ());
				targets.add(new CaliperSnapTarget(foreCenter, CaliperManager.CaliperMode.HORIZONTAL, transition, "Fore end"));
			}
			
			// Aft end corners (x = length)
			if (aftRadius > MINFEATURE) {
				// Top corner: (length, aftRadius)
				CoordinateIF aftTopCornerIF = transformation.transform(new Coordinate(length, aftRadius, 0));
				Coordinate aftTopCorner = new Coordinate(aftTopCornerIF.getX(), aftTopCornerIF.getY(), aftTopCornerIF.getZ());
				targets.add(new CaliperSnapTarget(aftTopCorner, CaliperManager.CaliperMode.HORIZONTAL, transition, "Aft end top"));
				
				// Bottom corner: (length, -aftRadius)
				CoordinateIF aftBottomCornerIF = transformation.transform(new Coordinate(length, -aftRadius, 0));
				Coordinate aftBottomCorner = new Coordinate(aftBottomCornerIF.getX(), aftBottomCornerIF.getY(), aftBottomCornerIF.getZ());
				targets.add(new CaliperSnapTarget(aftBottomCorner, CaliperManager.CaliperMode.HORIZONTAL, transition, "Aft end bottom"));
			} else {
				// Radius is 0 or very small, draw a point at center
				CoordinateIF aftCenterIF = transformation.transform(new Coordinate(length, 0, 0));
				Coordinate aftCenter = new Coordinate(aftCenterIF.getX(), aftCenterIF.getY(), aftCenterIF.getZ());
				targets.add(new CaliperSnapTarget(aftCenter, CaliperManager.CaliperMode.HORIZONTAL, transition, "Aft end"));
			}
		}
		
		// Shoulders: rectangles with edges that can be snapped to
		// Vertical calipers can snap to left/right edges (vertical lines)
		// Horizontal calipers can snap to top/bottom edges (horizontal lines)
		
		// Fore shoulder
		double foreShoulderLength = transition.getForeShoulderLength();
		double foreShoulderRadius = transition.getForeShoulderRadius();
		if (foreShoulderLength > MINFEATURE && foreShoulderRadius > MINFEATURE) {
			// Fore shoulder extends from x=-foreShoulderLength to x=0
			// It's a rectangle: x from -foreShoulderLength to 0, y from -foreShoulderRadius to +foreShoulderRadius
			
			// Left edge (vertical line at x=-foreShoulderLength) - for vertical calipers
			if (caliperMode == CaliperManager.CaliperMode.VERTICAL) {
				CoordinateIF foreShoulderLeftBottomIF = transformation.transform(new Coordinate(-foreShoulderLength, -foreShoulderRadius, 0));
				CoordinateIF foreShoulderLeftTopIF = transformation.transform(new Coordinate(-foreShoulderLength, foreShoulderRadius, 0));
				Coordinate foreShoulderLeftBottom = new Coordinate(foreShoulderLeftBottomIF.getX(), foreShoulderLeftBottomIF.getY(), foreShoulderLeftBottomIF.getZ());
				Coordinate foreShoulderLeftTop = new Coordinate(foreShoulderLeftTopIF.getX(), foreShoulderLeftTopIF.getY(), foreShoulderLeftTopIF.getZ());
				targets.add(new CaliperSnapTarget(foreShoulderLeftBottom, foreShoulderLeftTop,
						CaliperManager.CaliperMode.VERTICAL, transition, "Fore shoulder left"));
			}
			
			// Right edge (vertical line at x=0) - already handled as fore end edge above for vertical calipers
			
			// Top edge (horizontal line at y=+foreShoulderRadius) - for horizontal calipers
			if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL) {
				CoordinateIF foreShoulderTopStartIF = transformation.transform(new Coordinate(-foreShoulderLength, foreShoulderRadius, 0));
				CoordinateIF foreShoulderTopEndIF = transformation.transform(new Coordinate(0, foreShoulderRadius, 0));
				Coordinate foreShoulderTopStart = new Coordinate(foreShoulderTopStartIF.getX(), foreShoulderTopStartIF.getY(), foreShoulderTopStartIF.getZ());
				Coordinate foreShoulderTopEnd = new Coordinate(foreShoulderTopEndIF.getX(), foreShoulderTopEndIF.getY(), foreShoulderTopEndIF.getZ());
				targets.add(new CaliperSnapTarget(foreShoulderTopStart, foreShoulderTopEnd,
						CaliperManager.CaliperMode.HORIZONTAL, transition, "Fore shoulder top"));
			}
			
			// Bottom edge (horizontal line at y=-foreShoulderRadius) - for horizontal calipers
			if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL) {
				CoordinateIF foreShoulderBottomStartIF = transformation.transform(new Coordinate(-foreShoulderLength, -foreShoulderRadius, 0));
				CoordinateIF foreShoulderBottomEndIF = transformation.transform(new Coordinate(0, -foreShoulderRadius, 0));
				Coordinate foreShoulderBottomStart = new Coordinate(foreShoulderBottomStartIF.getX(), foreShoulderBottomStartIF.getY(), foreShoulderBottomStartIF.getZ());
				Coordinate foreShoulderBottomEnd = new Coordinate(foreShoulderBottomEndIF.getX(), foreShoulderBottomEndIF.getY(), foreShoulderBottomEndIF.getZ());
				targets.add(new CaliperSnapTarget(foreShoulderBottomStart, foreShoulderBottomEnd,
						CaliperManager.CaliperMode.HORIZONTAL, transition, "Fore shoulder bottom"));
			}
		}
		
		// Aft shoulder
		double aftShoulderLength = transition.getAftShoulderLength();
		double aftShoulderRadius = transition.getAftShoulderRadius();
		if (aftShoulderLength > MINFEATURE && aftShoulderRadius > MINFEATURE) {
			// Aft shoulder extends from x=length to x=length+aftShoulderLength
			// It's a rectangle: x from length to length+aftShoulderLength, y from -aftShoulderRadius to +aftShoulderRadius
			
			// Left edge (vertical line at x=length) - already handled as aft end edge above for vertical calipers
			
			// Right edge (vertical line at x=length+aftShoulderLength) - for vertical calipers
			if (caliperMode == CaliperManager.CaliperMode.VERTICAL) {
				CoordinateIF aftShoulderRightBottomIF = transformation.transform(new Coordinate(length + aftShoulderLength, -aftShoulderRadius, 0));
				CoordinateIF aftShoulderRightTopIF = transformation.transform(new Coordinate(length + aftShoulderLength, aftShoulderRadius, 0));
				Coordinate aftShoulderRightBottom = new Coordinate(aftShoulderRightBottomIF.getX(), aftShoulderRightBottomIF.getY(), aftShoulderRightBottomIF.getZ());
				Coordinate aftShoulderRightTop = new Coordinate(aftShoulderRightTopIF.getX(), aftShoulderRightTopIF.getY(), aftShoulderRightTopIF.getZ());
				targets.add(new CaliperSnapTarget(aftShoulderRightBottom, aftShoulderRightTop,
						CaliperManager.CaliperMode.VERTICAL, transition, "Aft shoulder right"));
			}
			
			// Top edge (horizontal line at y=+aftShoulderRadius) - for horizontal calipers
			if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL) {
				CoordinateIF aftShoulderTopStartIF = transformation.transform(new Coordinate(length, aftShoulderRadius, 0));
				CoordinateIF aftShoulderTopEndIF = transformation.transform(new Coordinate(length + aftShoulderLength, aftShoulderRadius, 0));
				Coordinate aftShoulderTopStart = new Coordinate(aftShoulderTopStartIF.getX(), aftShoulderTopStartIF.getY(), aftShoulderTopStartIF.getZ());
				Coordinate aftShoulderTopEnd = new Coordinate(aftShoulderTopEndIF.getX(), aftShoulderTopEndIF.getY(), aftShoulderTopEndIF.getZ());
				targets.add(new CaliperSnapTarget(aftShoulderTopStart, aftShoulderTopEnd,
						CaliperManager.CaliperMode.HORIZONTAL, transition, "Aft shoulder top"));
			}
			
			// Bottom edge (horizontal line at y=-aftShoulderRadius) - for horizontal calipers
			if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL) {
				CoordinateIF aftShoulderBottomStartIF = transformation.transform(new Coordinate(length, -aftShoulderRadius, 0));
				CoordinateIF aftShoulderBottomEndIF = transformation.transform(new Coordinate(length + aftShoulderLength, -aftShoulderRadius, 0));
				Coordinate aftShoulderBottomStart = new Coordinate(aftShoulderBottomStartIF.getX(), aftShoulderBottomStartIF.getY(), aftShoulderBottomStartIF.getZ());
				Coordinate aftShoulderBottomEnd = new Coordinate(aftShoulderBottomEndIF.getX(), aftShoulderBottomEndIF.getY(), aftShoulderBottomEndIF.getZ());
				targets.add(new CaliperSnapTarget(aftShoulderBottomStart, aftShoulderBottomEnd,
						CaliperManager.CaliperMode.HORIZONTAL, transition, "Aft shoulder bottom"));
			}
		}
	}
	
	/**
	 * Get snap targets for back view.
	 */
	private void getBackViewSnapTargets(Transition transition, double foreRadius, double aftRadius,
									  Transformation transformation,
									  CaliperManager.CaliperMode caliperMode,
									  List<CaliperSnapTarget> targets) {
		// In back view, the transition appears as circles at fore and aft ends
		// We need to get the position of the fore and aft ends
		
		// Transform the fore end center (x=0, y=0, z=0 in component coordinates)
		CoordinateIF foreCenterIF = transformation.transform(Coordinate.ZERO);
		Coordinate foreCenter = new Coordinate(foreCenterIF.getX(), foreCenterIF.getY(), foreCenterIF.getZ());
		
		// Transform the aft end center (x=length, y=0, z=0 in component coordinates)
		CoordinateIF aftCenterIF = transformation.transform(new Coordinate(transition.getLength(), 0, 0));
		Coordinate aftCenter = new Coordinate(aftCenterIF.getX(), aftCenterIF.getY(), aftCenterIF.getZ());
		
		//// Snap to left and right points on outer circles
		// Fore end outer circle points
		if (foreRadius > MINFEATURE) {
			Coordinate foreLeft = new Coordinate(foreCenter.getX(), foreCenter.getY(), foreCenter.getZ() - foreRadius);
			Coordinate foreRight = new Coordinate(foreCenter.getX(), foreCenter.getY(), foreCenter.getZ() + foreRadius);
			targets.add(new CaliperSnapTarget(foreLeft, CaliperManager.CaliperMode.BOTH, transition, "Fore outer left"));
			targets.add(new CaliperSnapTarget(foreRight, CaliperManager.CaliperMode.BOTH, transition, "Fore outer right"));
		}

		// Aft end outer circle points
		if (aftRadius > MINFEATURE) {
			Coordinate aftLeft = new Coordinate(aftCenter.getX(), aftCenter.getY(), aftCenter.getZ() - aftRadius);
			Coordinate aftRight = new Coordinate(aftCenter.getX(), aftCenter.getY(), aftCenter.getZ() + aftRadius);
			targets.add(new CaliperSnapTarget(aftLeft, CaliperManager.CaliperMode.BOTH, transition, "Aft outer left"));
			targets.add(new CaliperSnapTarget(aftRight, CaliperManager.CaliperMode.BOTH, transition, "Aft outer right"));
		}

		//// Snap to top and bottom points on outer circles
		// Fore end outer circle points
		if (foreRadius > MINFEATURE) {
			Coordinate foreTop = new Coordinate(foreCenter.getX(), foreCenter.getY() + foreRadius, foreCenter.getZ());
			Coordinate foreBottom = new Coordinate(foreCenter.getX(), foreCenter.getY() - foreRadius, foreCenter.getZ());
			targets.add(new CaliperSnapTarget(foreTop, CaliperManager.CaliperMode.BOTH, transition, "Fore outer top"));
			targets.add(new CaliperSnapTarget(foreBottom, CaliperManager.CaliperMode.BOTH, transition, "Fore outer bottom"));
		}

		// Aft end outer circle points
		if (aftRadius > MINFEATURE) {
			Coordinate aftTop = new Coordinate(aftCenter.getX(), aftCenter.getY() + aftRadius, aftCenter.getZ());
			Coordinate aftBottom = new Coordinate(aftCenter.getX(), aftCenter.getY() - aftRadius, aftCenter.getZ());
			targets.add(new CaliperSnapTarget(aftTop, CaliperManager.CaliperMode.BOTH, transition, "Aft outer top"));
			targets.add(new CaliperSnapTarget(aftBottom, CaliperManager.CaliperMode.BOTH, transition, "Aft outer bottom"));
		}
	}
}

