package net.sf.openrocket.rocketcomponent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;


public class FreeformFinSet extends FinSet {
	private static final Logger log = LoggerFactory.getLogger(FreeformFinSet.class);
	private static final Translator trans = Application.getTranslator();

	// this class uses certain features of 'ArrayList' which are not implemented in other 'List' implementations.
	private ArrayList<Coordinate> points = new ArrayList<>();
	
	private static final double SNAP_SMALLER_THAN = 5e-3;
	private static final double IGNORE_SMALLER_THAN = 1e-12;
	
	public FreeformFinSet() {
		points.add(Coordinate.ZERO);
		points.add(new Coordinate(0.025, 0.05));
		points.add(new Coordinate(0.075, 0.05));
		points.add(new Coordinate(0.05, 0));
		
		this.length = 0.05;
	}
	
	/**
	 * Convert an existing fin set into a freeform fin set.  The specified
	 * fin set is taken out of the rocket tree (if any) and the new component
	 * inserted in its stead.
	 * <p>
	 * The specified fin set should not be used after the call!
	 *
	 * @param finset	the fin set to convert.
	 * @return			the new freeform fin set.
	 */
	public static FreeformFinSet convertFinSet(FinSet finset) {
		final RocketComponent root = finset.getRoot();
		FreeformFinSet freeform;
		List<RocketComponent> toInvalidate = new ArrayList<>();

		try {
			if (root instanceof Rocket) {
				((Rocket) root).freeze();
			}
			
			// Get fin set position and remove fin set
			final RocketComponent parent = finset.getParent();
			final int position;
			if (parent != null) {
				position = parent.getChildPosition(finset);
				parent.removeChild(position);
			} else {
				position = -1;
			}
			
			// Create the freeform fin set
			Coordinate[] finPoints = finset.getFinPoints();
			freeform = new FreeformFinSet();
			freeform.setPoints(finPoints);
			freeform.setAxialOffset(finset.getAxialMethod(), finset.getAxialOffset());
			
			// Copy component attributes
			toInvalidate = freeform.copyFrom(finset);
			
			// Set name
			final String componentTypeName = finset.getComponentName();
			final String name = freeform.getName();
			
			if (name.startsWith(componentTypeName)) {
				freeform.setName(freeform.getComponentName() +
						name.substring(componentTypeName.length()));
			}
			
			freeform.setAppearance(finset.getAppearance());
			
			// Add freeform fin set to parent
			if (parent != null) {
				parent.addChild(freeform, position);
			}
			
		} finally {
			if (root instanceof Rocket) {
				((Rocket) root).thaw();
			}
			// Invalidate components after events have been fired
			for (RocketComponent c : toInvalidate) {
				c.invalidate();
			}
		}
		return freeform;
	}
	
	/**
	 * Add a fin point between indices <code>index-1</code> and <code>index</code>.
	 * The point is placed at the midpoint of the current segment.
	 *
	 * @param index   the fin point before which to add the new point.
	 * @param location the target location to create the new point at
	 */
	public void addPoint(int index, Point2D.Double location) {
		// new method: add new point at closest point
		points.add(index, new Coordinate(location.x, location.y));
		
		// adding a point within the segment affects neither mass nor aerodynamics
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	/**
	 * Remove the fin point with the given index.  The first and last fin points
	 * cannot be removed, and will cause an <code>IllegalFinPointException</code>
	 * if attempted.
	 *
	 * @param index   the fin point index to remove
	 * @throws IllegalFinPointException if removing would result in invalid fin planform
	 */
	public void removePoint(int index) throws IllegalFinPointException {
		if (index == 0 || index == points.size() - 1) {
			throw new IllegalFinPointException("cannot remove first or last point");
		}
		
		// copy the old list in case the operation fails
		ArrayList<Coordinate> copy = new ArrayList<>(this.points);
		
		this.points.remove(index);
		if (intersects()) {
			// if error, rollback.  
			this.points = copy;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	
	public int getPointCount() {
		return points.size();
	}
	
	/** maintained just for backwards compatibility:
	 */
	public void setPoints(Coordinate[] newPoints) {
		setPoints(new ArrayList<>(Arrays.asList(newPoints)));
	}
	
	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints New points to set as the exposed edges of the fin
	 */
	public void setPoints( ArrayList<Coordinate> newPoints) {

		final Coordinate delta = newPoints.get(0).multiply(-1);
		if( IGNORE_SMALLER_THAN < delta.length2()){
			newPoints = translatePoints( newPoints, delta);
		}

		// copy the old points, in case validation fails
		final ArrayList<Coordinate> pointsCopy = new ArrayList<>(this.points);
		final double lengthCopy = this.length;

		this.points = newPoints;

		update();

		if( intersects()){
			// on error, reset to the old points
			this.points = pointsCopy;
			this.length = lengthCopy;
		}

		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}

	/**
	 * Set the point at position <code>i</code> to coordinates (x,y).
	 * <p>
	 * Note that this method silently enforces basic fin shape restrictions 
	 *     - points may not be within the parent body.
	 *     - first point occurs before last (and vice versa) 
	 *     - first and last points must be on the parent body
	 *     - non-self-intersecting fin shape (aborts set on invalid fin point)
	 * </p><p>
	 * NOTE: the fin-point axes differ from rocket axes:
	 *    +x within the fin points foreward; +x for the rocket points aft
	 * </p><p>
	 * Moving of the first point in the X-axis is allowed, but this actually moves
	 * all of the other points the corresponding distance back, relative to the first.
	 * That is, moving the first point should not change how the rest of the
	 * points are positioned *relative to the fin-mount*.
	 *
	 * @param index	the point index to modify.
	 * @param xRequest the x-coordinate.
	 * @param yRequest the y-coordinate.
	 */
	public void setPoint(final int index, final double xRequest, final double yRequest) {
		final Coordinate revertPoint = points.get(index);
		if(null != this.getParent()) {
			final Coordinate prior =  points.get(index);
			points.set(index, new Coordinate(xRequest, yRequest));

			if((points.size() - 1) == index){
				clampLastPoint(xRequest-prior.x);
			}
		}

		update();

		if (intersects()) {
			// intersection found!  log error and abort!
			points.set(index, revertPoint);
			return;
		}

		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	private void movePoints(final double delta_x, final double delta_y) {
		// zero-out 0th index -- it's the local origin and is always (0,0)
		points.set(0, Coordinate.ZERO);

		for (int index = 1; index < points.size(); ++index) {
			final Coordinate oldPoint = this.points.get(index);
			final Coordinate newPoint = oldPoint.add(delta_x, delta_y, 0.0f);
			points.set(index, newPoint);
		}
	}

	@Override
	public Coordinate[] getFinPoints() {
		return points.toArray(new Coordinate[0]);
	}

	@Override
	public double getSpan() {
		double max = 0;
		for (Coordinate c : points) {
			if (c.y > max)
				max = c.y;
		}
		return max;
	}
	
	@Override
	public String getComponentName() {
		//// Freeform fin set
		return trans.get("FreeformFinSet.FreeformFinSet");
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		RocketComponent c = super.copyWithOriginalID();
		
		((FreeformFinSet) c).points = new ArrayList<>(this.points);
		
		return c;
	}

	@Override
	public void update() {
		this.length = points.get(points.size() -1).x - points.get(0).x;
		this.setAxialOffset(this.axialMethod, this.axialOffset);

		if(null != this.getParent()) {
			clampFirstPoint();

			for(int i=1; i < points.size()-1; i++) {
				clampInteriorPoint(i);
			}
			
			clampLastPoint();

			validateFinTab();
		}
	}

	private void clampFirstPoint() {
		final SymmetricComponent body = (SymmetricComponent) getParent();

		final Coordinate finFront = getFinFront();
		final double xFinFront = finFront.x; // x of fin start, body-frame
		final double yFinFront = finFront.y; // y of fin start, body-frame

		final Coordinate p0 = points.get(0);
		
		if( ! Coordinate.ZERO.equals(p0)){
			double xDelta = p0.x;
			double xTrail = points.get(points.size() - 1).x;
			if(xDelta > xTrail){
				xDelta = xTrail;
			}
			double yDelta = body.getRadius(xFinFront + xDelta) - yFinFront;

			movePoints(-xDelta, -yDelta);

			if(AxialMethod.TOP == getAxialMethod()) {
				this.axialOffset = axialOffset + xDelta;
				this.position = this.position.add(xDelta, 0, 0);
            } else if (AxialMethod.MIDDLE == getAxialMethod()) {
				this.axialOffset = axialOffset + xDelta / 2;
			}
		}

	    final int lastIndex = points.size()-1;
	    this.length = points.get(lastIndex).x;

	}

	private void clampInteriorPoint(final int index) {
		final SymmetricComponent sym = (SymmetricComponent) this.getParent();

		final Coordinate finFront = getFinFront();
		final double xFinFront = finFront.x; // x of fin start, body-frame
		final double yFinFront = finFront.y; // y of fin start, body-frame

		final double xBodyFront = -xFinFront;
		final double xBodyBack = xBodyFront + sym.getLength();

		final double xPrior = points.get(index).x;
		final double yPrior = points.get(index).y;

		if((xBodyFront <= xPrior ) && ( xPrior <= xBodyBack )) {
			final double yBody = sym.getRadius(xPrior + xFinFront) - yFinFront;

			// ensure that an interior point is outside of its mounting body:
			if (yBody > yPrior) {
				points.set(index, points.get(index).setY(yBody));
			}
		}

	}
	
	private void clampLastPoint() {
		clampLastPoint(0);
	}
			
	private void clampLastPoint(final double xDelta) {
		final SymmetricComponent body = (SymmetricComponent) getParent();

		final Coordinate finFront = getFinFront();
		final double xFinStart = finFront.x; // x of fin start, body-frame
		final double yFinStart = finFront.y; // y of fin start, body-frame

		int lastIndex = points.size() - 1;
		final Coordinate last = points.get(lastIndex);
		
		double yBody = body.getRadius(xFinStart + last.x) - yFinStart;
		double yDelta = yBody - last.y;
		if( IGNORE_SMALLER_THAN < Math.abs(yDelta)){
			// i.e. if it delta is close enough above OR is inside the body.  In either case, snap it to the body.

			// => set y-value to *exactly* match parent body:
			points.set(lastIndex, new Coordinate(last.x, yBody));
		}

		if( IGNORE_SMALLER_THAN < Math.abs(xDelta)) {
			this.length = points.get(lastIndex).x;
			if (AxialMethod.MIDDLE == getAxialMethod()) {
				this.axialOffset = axialOffset + xDelta/2;
			} else if (AxialMethod.BOTTOM == getAxialMethod()) {
				this.axialOffset = axialOffset + xDelta;
			}
		}
	}
	
	/** 
	 * Check if *any* of the fin-point line segments intersects with another.
	 * 
	 * @return  true if an intersection is found
	 */
	public boolean intersects() {
		for (int index = 0; index < (this.points.size() - 1); ++index) {
			if (intersects(index)) {
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * Check if the line segment from targetIndex to targetIndex+1 intersects with any other part of the fin.
	 * 
	 * @return  true if an intersection was found
	 */
	private boolean intersects(final int targetIndex) {
		if ((points.size() - 2) < targetIndex) {
			log.error("request validation of non-existent fin edge segment: " + targetIndex + "/" + points.size());
			// throw new IndexOutOfBoundsException("request validate of non-existent fin edge segment: " + targetIndex + "/" + points.size());
		}

		// (pre-check the indices above.)
		final Point2D.Double pt1 = new Point2D.Double(points.get(targetIndex).x, points.get(targetIndex).y);
		final Point2D.Double pt2 = new Point2D.Double(points.get(targetIndex + 1).x, points.get(targetIndex + 1).y);
		final Line2D.Double targetLine = new Line2D.Double(pt1, pt2);
		
		for (int comparisonIndex = targetIndex+1; comparisonIndex < (points.size() - 1); ++comparisonIndex) {
			if (2 > Math.abs(targetIndex - comparisonIndex)) {
				// a line segment will trivially not intersect with itself
				// nor can adjacent line segments intersect with each other, because they share a common endpoint.
				continue;
			}
			final Point2D.Double pc1 = new Point2D.Double(points.get(comparisonIndex).x, points.get(comparisonIndex).y); // p1 
			final Point2D.Double pc2 = new Point2D.Double(points.get(comparisonIndex + 1).x, points.get(comparisonIndex + 1).y); // p2
		
			// special case for when the first and last points are co-located.
			if((0==targetIndex)&&(points.size()==comparisonIndex+2)&&(IGNORE_SMALLER_THAN > Math.abs(pt1.distance(pc2)))){
				continue;
			}
			
			final Line2D.Double comparisonLine = new Line2D.Double(pc1, pc2);
			if (targetLine.intersectsLine(comparisonLine)) {
				log.warn(String.format("Found intersection at %d-%d and %d-%d", targetIndex, targetIndex+1, comparisonIndex, comparisonIndex+1));
				log.warn(String.format("                   between (%g, %g) => (%g, %g)", pt1.x, pt1.y, pt2.x, pt2.y));
				log.warn(String.format("                       and (%g, %g) => (%g, %g)", pc1.x, pc1.y, pc2.x, pc2.y));
				return true;
			}
		}
		return false;
	}
	
}
