package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides snap targets for FinSet components.
 * 
 * For FreeformFinSet: snaps to all fin points
 * For EllipticalFinSet: snaps to start point, end point, and top of ellipse
 * For TrapezoidFinSet: snaps to fin points, and if cant is 0, also root/tip chord lines for horizontal caliper
 * 
 * For back view: only creates snap targets if cant angle is 0, then creates rectangle using span and thickness
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class FinSetSnapProvider implements ComponentSnapProvider {
	
	@Override
	public Class<? extends RocketComponent> getComponentClass() {
		return FinSet.class;
	}
	
	@Override
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		List<CaliperSnapTarget> targets = new ArrayList<>();
		
		if (!(component instanceof FinSet finSet)) {
			return targets;
		}
		
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			// Side/Top view: snap to fin points
			getSideViewSnapTargets(finSet, transformation, caliperMode, targets);
		} else if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: only if cant is 0, create rectangle snap targets
			if (MathUtil.equals(finSet.getCantAngle(), 0)) {
				getBackViewSnapTargets(finSet, transformation, caliperMode, targets);
			}
		}
		
		return targets;
	}
	
	/**
	 * Get snap targets for side/top view.
	 * The fin points are transformed with the cant rotation, matching the rendering code.
	 */
	private void getSideViewSnapTargets(FinSet finSet, Transformation transformation,
									   CaliperManager.CaliperMode caliperMode,
									   List<CaliperSnapTarget> targets) {
		// Apply cant rotation to the transformation (matching FinSetShapes.getShapesSide)
		Transformation cantRotation = finSet.getCantRotation();
		Transformation compositeTransform = transformation.applyTransformation(cantRotation);
		
		// Get fin points and transform them
		CoordinateIF[] finPoints = finSet.getFinPoints();
		CoordinateIF[] transformedPoints = compositeTransform.transform(finPoints);
		
		if (finSet instanceof FreeformFinSet) {
			// FreeformFinSet: snap to all fin points
			for (int i = 0; i < transformedPoints.length; i++) {
				Coordinate point = new Coordinate(transformedPoints[i].getX(), 
												  transformedPoints[i].getY(), 
												  transformedPoints[i].getZ());
				targets.add(new CaliperSnapTarget(point, CaliperManager.CaliperMode.BOTH, 
												  finSet, "Fin point " + i));
			}
		} else if (finSet instanceof EllipticalFinSet) {
			// EllipticalFinSet: snap to start point, end point, and top of ellipse
			if (transformedPoints.length > 0) {
				// Start point (first point)
				Coordinate start = new Coordinate(transformedPoints[0].getX(), 
												 transformedPoints[0].getY(), 
												 transformedPoints[0].getZ());
				targets.add(new CaliperSnapTarget(start, CaliperManager.CaliperMode.BOTH, 
												 finSet, "Fin start"));
				
				// End point (last point)
				Coordinate end = new Coordinate(transformedPoints[transformedPoints.length - 1].getX(), 
												transformedPoints[transformedPoints.length - 1].getY(), 
												transformedPoints[transformedPoints.length - 1].getZ());
				targets.add(new CaliperSnapTarget(end, CaliperManager.CaliperMode.BOTH, 
												 finSet, "Fin end"));
				
				// Top of ellipse (point with maximum Y)
				double maxY = transformedPoints[0].getY();
				int maxIndex = 0;
				for (int i = 1; i < transformedPoints.length; i++) {
					if (transformedPoints[i].getY() > maxY) {
						maxY = transformedPoints[i].getY();
						maxIndex = i;
					}
				}
				Coordinate top = new Coordinate(transformedPoints[maxIndex].getX(), 
												transformedPoints[maxIndex].getY(), 
												transformedPoints[maxIndex].getZ());
				targets.add(new CaliperSnapTarget(top, CaliperManager.CaliperMode.BOTH, 
												 finSet, "Fin top"));
			}
		} else if (finSet instanceof TrapezoidFinSet) {
			// TrapezoidFinSet: snap to fin points
			for (int i = 0; i < transformedPoints.length; i++) {
				Coordinate point = new Coordinate(transformedPoints[i].getX(), 
												 transformedPoints[i].getY(), 
												 transformedPoints[i].getZ());
				targets.add(new CaliperSnapTarget(point, CaliperManager.CaliperMode.BOTH, 
												 finSet, "Fin point " + i));
			}
			
			// If cant is 0 and horizontal caliper mode, also snap to root chord and tip chord
			if (MathUtil.equals(finSet.getCantAngle(), 0) && 
				caliperMode == CaliperManager.CaliperMode.HORIZONTAL) {
				
				// Root chord: from first point to last point (at y=0)
				if (transformedPoints.length >= 2) {
					Coordinate rootStart = new Coordinate(transformedPoints[0].getX(), 
														 transformedPoints[0].getY(), 
														 transformedPoints[0].getZ());
					Coordinate rootEnd = new Coordinate(transformedPoints[transformedPoints.length - 1].getX(), 
													   transformedPoints[transformedPoints.length - 1].getY(), 
													   transformedPoints[transformedPoints.length - 1].getZ());
					targets.add(new CaliperSnapTarget(rootStart, rootEnd, 
													 CaliperManager.CaliperMode.HORIZONTAL, 
													 finSet, "Root chord"));
				}
				
				// Tip chord: from second point to third point (at y=height)
				// TrapezoidFinSet.getFinPoints() returns: [0,0], [sweep,height], [sweep+tipChord,height], [length,0]
				if (transformedPoints.length >= 4) {
					Coordinate tipStart = new Coordinate(transformedPoints[1].getX(), 
														transformedPoints[1].getY(), 
														transformedPoints[1].getZ());
					Coordinate tipEnd = new Coordinate(transformedPoints[2].getX(), 
													  transformedPoints[2].getY(), 
													  transformedPoints[2].getZ());
					targets.add(new CaliperSnapTarget(tipStart, tipEnd, 
													 CaliperManager.CaliperMode.HORIZONTAL, 
													 finSet, "Tip chord"));
				}
			}
		} else {
			// Generic FinSet: snap to all fin points
			for (int i = 0; i < transformedPoints.length; i++) {
				Coordinate point = new Coordinate(transformedPoints[i].getX(), 
												  transformedPoints[i].getY(), 
												  transformedPoints[i].getZ());
				targets.add(new CaliperSnapTarget(point, CaliperManager.CaliperMode.BOTH, 
												 finSet, "Fin point " + i));
			}
		}
	}
	
	/**
	 * Get snap targets for back view.
	 * Only called when cant angle is 0.
	 * Creates rectangle snap targets using span and thickness.
	 * Matches the rendering code in FinSetShapes.uncantedShapesBack.
	 */
	private void getBackViewSnapTargets(FinSet finSet, Transformation transformation,
										CaliperManager.CaliperMode caliperMode,
										List<CaliperSnapTarget> targets) {
		double span = finSet.getSpan();
		double thickness = finSet.getThickness();
		
		// Get the fin points to find the minimum Y offset (matching FinSetShapes.uncantedShapesBack)
		CoordinateIF[] finPoints = finSet.getFinPoints();
		double yOffset = Double.MAX_VALUE;
		for (CoordinateIF point : finPoints) {
			yOffset = Math.min(yOffset, point.getY());
		}
		
		// Apply Y offset transformation (matching rendering code)
		Transformation translateOffsetY = new Transformation(0, yOffset, 0);
		Transformation compositeTransform = transformation.applyTransformation(translateOffsetY);
		
		// Create rectangle corners in local coordinates (matching FinSetShapes.uncantedShapesBack)
		// The rectangle corners are:
		// c[0] = (0, 0, -thickness/2)
		// c[1] = (0, 0, thickness/2)
		// c[2] = (0, span, thickness/2)
		// c[3] = (0, span, -thickness/2)
		// In back view rendering, Z is screen X, Y is screen Y
		CoordinateIF bottomLeftIF = compositeTransform.transform(new Coordinate(0, 0, -thickness / 2));
		CoordinateIF bottomRightIF = compositeTransform.transform(new Coordinate(0, 0, thickness / 2));
		CoordinateIF topRightIF = compositeTransform.transform(new Coordinate(0, span, thickness / 2));
		CoordinateIF topLeftIF = compositeTransform.transform(new Coordinate(0, span, -thickness / 2));
		
		Coordinate bottomLeft = new Coordinate(bottomLeftIF.getX(), bottomLeftIF.getY(), bottomLeftIF.getZ());
		Coordinate bottomRight = new Coordinate(bottomRightIF.getX(), bottomRightIF.getY(), bottomRightIF.getZ());
		Coordinate topRight = new Coordinate(topRightIF.getX(), topRightIF.getY(), topRightIF.getZ());
		Coordinate topLeft = new Coordinate(topLeftIF.getX(), topLeftIF.getY(), topLeftIF.getZ());
		
		// Add snap targets for all four corners (BOTH mode for back view)
		targets.add(new CaliperSnapTarget(bottomLeft, CaliperManager.CaliperMode.BOTH, finSet, "Bottom left"));
		targets.add(new CaliperSnapTarget(bottomRight, CaliperManager.CaliperMode.BOTH, finSet, "Bottom right"));
		targets.add(new CaliperSnapTarget(topRight, CaliperManager.CaliperMode.BOTH, finSet, "Top right"));
		targets.add(new CaliperSnapTarget(topLeft, CaliperManager.CaliperMode.BOTH, finSet, "Top left"));
	}
}

