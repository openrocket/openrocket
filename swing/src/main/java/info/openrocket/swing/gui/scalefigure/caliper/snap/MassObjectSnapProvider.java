package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.MassObject;
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
 * Provides snap targets for MassObject components.
 * 
 * For side/top view:
 * - If arc >= 2*radius, the vertical edges (left/right) are fully rounded -> adds points instead of lines
 * - If arc >= length, the horizontal edges (top/bottom) are fully rounded -> adds points instead of lines
 * - Otherwise, adds snap targets to the straight edges of the rounded rectangle (excluding rounded corners)
 * 
 * For back view:
 * - Adds 4 points at top, left, right, bottom of the circle
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class MassObjectSnapProvider implements ComponentSnapProvider {
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		return MassObject.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof MassObject massObject)) {
			return targets;
		}
		
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: snap to straight edges of rounded rectangle
			getSideViewSnapTargets(massObject, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: snap to circle points
			getBackViewSnapTargets(massObject, transformation, caliperMode, targets);
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 * The rounded rectangle has rounded corners with arc = Math.min(length, 2*radius) * 0.7.
	 * We only snap to the straight edges, not the rounded corners.
	 * If an edge is fully rounded (arc >= dimension), we add a point at the center of that edge instead of a line.
	 */
	private void getSideViewSnapTargets(MassObject massObject, Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		final double length = massObject.getLength();
		final double radius = massObject.getRadius();
		final double arc = Math.min(length, 2*radius) * 0.7;
		
		// Calculate local position with radial offset (matching MassComponentShapes.getShapesSide)
		final double radialDistance = massObject.getRadialPosition();
		final double radialAngleRadians = massObject.getRadialDirection();
		
		final CoordinateIF localPosition = new Coordinate(0,
														radialDistance * Math.cos(radialAngleRadians),
														radialDistance * Math.sin(radialAngleRadians));
		final CoordinateIF renderPositionIF = transformation.transform(localPosition);
		final Coordinate renderPosition = new Coordinate(renderPositionIF.getX(), 
														renderPositionIF.getY(), 
														renderPositionIF.getZ());
		
		// The rounded rectangle is drawn at:
		// x = renderPosition.getX()
		// y = renderPosition.getY() - radius
		// width = length
		// height = 2*radius
		final double x = renderPosition.getX();
		final double y = renderPosition.getY() - radius;
		
		final double halfArc = arc / 2.0;
		final double centerY = y + radius;
		
		// Top edge (horizontal)
		if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
			// Add line for the straight portion
			Coordinate topStart = new Coordinate(x + halfArc, y, renderPosition.getZ());
			Coordinate topEnd = new Coordinate(x + length - halfArc, y, renderPosition.getZ());
			targets.add(new CaliperSnapTarget(topStart, topEnd,
											 CaliperManager.CaliperMode.HORIZONTAL,
											 massObject, "Top edge"));
		}
		
		// Bottom edge (horizontal)
		if (caliperMode == CaliperManager.CaliperMode.HORIZONTAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
			// Add line for the straight portion
			Coordinate bottomStart = new Coordinate(x + halfArc, y + 2*radius, renderPosition.getZ());
			Coordinate bottomEnd = new Coordinate(x + length - halfArc, y + 2*radius, renderPosition.getZ());
			targets.add(new CaliperSnapTarget(bottomStart, bottomEnd,
											 CaliperManager.CaliperMode.HORIZONTAL,
											 massObject, "Bottom edge"));
		}
		
		// Left edge (vertical)
		if (caliperMode == CaliperManager.CaliperMode.VERTICAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
			// Add line for the straight portion
			Coordinate leftStart = new Coordinate(x, y + halfArc, renderPosition.getZ());
			Coordinate leftEnd = new Coordinate(x, y + 2*radius - halfArc, renderPosition.getZ());
			targets.add(new CaliperSnapTarget(leftStart, leftEnd,
											 CaliperManager.CaliperMode.VERTICAL,
											 massObject, "Left edge"));
		}
		
		// Right edge (vertical)
		if (caliperMode == CaliperManager.CaliperMode.VERTICAL || caliperMode == CaliperManager.CaliperMode.BOTH) {
			// Add line for the straight portion
			Coordinate rightStart = new Coordinate(x + length, y + halfArc, renderPosition.getZ());
			Coordinate rightEnd = new Coordinate(x + length, y + 2*radius - halfArc, renderPosition.getZ());
			targets.add(new CaliperSnapTarget(rightStart, rightEnd,
											 CaliperManager.CaliperMode.VERTICAL,
											 massObject, "Right edge"));
		}
	}
	
	/**
	 * Get snap targets for back view.
	 * The MassObject appears as a circle in back view.
	 * Adds 4 points at top, left, right, bottom of the circle.
	 */
	private void getBackViewSnapTargets(MassObject massObject, Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		final double radius = massObject.getRadius();
		
		// Calculate local position with radial offset (matching MassComponentShapes.getShapesBack)
		final double radialDistance = massObject.getRadialPosition();
		final double radialAngleRadians = massObject.getRadialDirection();
		
		final CoordinateIF localPosition = new Coordinate(0,
														radialDistance * Math.cos(radialAngleRadians),
														radialDistance * Math.sin(radialAngleRadians));
		final CoordinateIF renderPositionIF = transformation.transform(localPosition);
		final Coordinate renderPosition = new Coordinate(renderPositionIF.getX(), 
														renderPositionIF.getY(), 
														renderPositionIF.getZ());
		
		// In back view, the circle is drawn at:
		// center = renderPosition
		// In back view, Z is screen X, Y is screen Y
		// Add 4 points at top, left, right, bottom
		Coordinate top = new Coordinate(renderPosition.getX(), renderPosition.getY() + radius, renderPosition.getZ());
		Coordinate bottom = new Coordinate(renderPosition.getX(), renderPosition.getY() - radius, renderPosition.getZ());
		Coordinate left = new Coordinate(renderPosition.getX(), renderPosition.getY(), renderPosition.getZ() - radius);
		Coordinate right = new Coordinate(renderPosition.getX(), renderPosition.getY(), renderPosition.getZ() + radius);
		
		targets.add(new CaliperSnapTarget(top, CaliperManager.CaliperMode.BOTH, massObject, "Top"));
		targets.add(new CaliperSnapTarget(bottom, CaliperManager.CaliperMode.BOTH, massObject, "Bottom"));
		targets.add(new CaliperSnapTarget(left, CaliperManager.CaliperMode.BOTH, massObject, "Left"));
		targets.add(new CaliperSnapTarget(right, CaliperManager.CaliperMode.BOTH, massObject, "Right"));
	}
}

