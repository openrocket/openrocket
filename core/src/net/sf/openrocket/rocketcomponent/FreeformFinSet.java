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
	
	private static final double SNAP_SMALLER_THAN = 1e-6;
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
		if (!validate()) {
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
		// move to zero, if applicable
		if( ! Coordinate.ZERO.equals(newPoints[0])) {
			final Coordinate p0 = newPoints[0];
			newPoints = translatePoints( newPoints, -p0.x, -p0.y);
		}
		
		setPoints(new ArrayList<>(Arrays.asList(newPoints)));
	}
	
	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints New points to set as the exposed edges of the fin
	 */
	public void setPoints( ArrayList<Coordinate> newPoints) {
		// copy the old points, in case validation fails
		ArrayList<Coordinate> copy = new ArrayList<>(this.points);
		this.points = newPoints;
		this.length = newPoints.get(newPoints.size() -1).x;		
		
		update();
		
		//StackTraceElement[] stacktrack = Thread.currentThread().getStackTrace();
		if("Canard fins, mounted to transition".equals(this.getName())) {
			log.error(String.format("starting to set %d points @ %s", newPoints.size(), this.getName()), new NullPointerException());
			System.err.println( toDebugDetail());
		}
		
		if( ! validate()){
			// on error, reset to the old points
			this.points = copy;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	private double y_body(final double x) {
		return y_body(x, 0.0);
	}
	
	private double y_body(final double x_target, final double x_ref) {
		final SymmetricComponent sym = (SymmetricComponent) getParent();
		return (sym.getRadius(x_target) - sym.getRadius(x_ref));
	}
	
	public void setPointRelToFin(final int index, final double x_request_fin, final double y_request_fin) throws IllegalFinPointException {
		final double x_finStart_body = getAxialFront(); // x @ fin start, body frame
		final double y_finStart_body = y_body(x_finStart_body);
		
		setPoint(index, x_request_fin + x_finStart_body, y_request_fin + y_finStart_body);
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

		if(null != this.getParent()) {
			if (0 == index) {
				clampFirstPoint(new Coordinate(xRequest, yRequest));
			} else if ((this.points.size() - 1) == index) {
				Coordinate priorPoint = points.get(index);				
				points.set(index, new Coordinate(xRequest, yRequest));
				clampLastPoint(priorPoint);
			} else {
				// interior points can never change the 
				points.set(index, new Coordinate(xRequest, yRequest));
				clampInteriorPoint(index);
			}
		}
		
		// this maps the last index and the next-to-last-index to the same 'testIndex'
		int testIndex = Math.min(index, (points.size() - 2));
		if (intersects(testIndex)) {
			// intersection found!  log error and abort!
			log.error(String.format("ERROR: found an intersection while setting fin point #%d to [%6.4g, %6.4g] <body frame> : ABORTING setPoint(..) !! ", index, xRequest, yRequest));
			return;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	private void movePoints(final double delta_x, final double delta_y) {
		// skip 0th index -- it's the local origin and is always (0,0) 
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
	public void setAxialOffset(final AxialMethod newAxialMethod, final double newOffsetRequest) {
		super.setAxialOffset(newAxialMethod, newOffsetRequest);
		
		if (null != parent) {
			// if the new position would cause fin overhang, only allow movement up to the end of the parent component.
			// N.B. if you want a fin to overhang, add & adjust interior points.
			final double backOverhang = getAxialOffset(AxialMethod.BOTTOM);
			if (0 < backOverhang) {
				final double newOffset = newOffsetRequest - backOverhang;
				super.setAxialOffset(newAxialMethod, newOffset);
			}
			final double frontOverhang = getAxialFront();
			if (0 > frontOverhang) {
				final double newOffset = newOffsetRequest - frontOverhang;
				super.setAxialOffset(newAxialMethod, newOffset);
			}
		}
	}
	
	@Override
	public void update() {
		this.setAxialOffset(this.axialMethod, this.axialOffset);
		
		if(null != this.getParent()) {
			clampFirstPoint(points.get(0));
			for(int i=1; i < points.size()-1; i++) {
				clampInteriorPoint(i);
			}
			
			clampLastPoint(null);

			validateFinTab();
		}
	}
	
	private void clampFirstPoint(final Coordinate newPoint) {
		final SymmetricComponent body = (SymmetricComponent) getParent();
		
		final Coordinate finFront = getFinFront();
		final double xFinFront = finFront.x; // x of fin start, body-frame
		final double yFinFront = finFront.y; // y of fin start, body-frame
		final double xBodyStart = -getAxialFront(); // x-offset from start-to-start; fin-frame
		
		double xDelta;
		double yDelta;

	    if(IGNORE_SMALLER_THAN > Math.abs(newPoint.x)){
	    	return;
	    }else if (xBodyStart > newPoint.x) {
			// attempt to place point in front of the start of the body
			
			// delta for new zeroth point
			xDelta = xBodyStart;
			yDelta = body.getForeRadius() - yFinFront;			
			points.set(0, newPoint);
			points.add(0, Coordinate.ZERO);
			movePoints(-xDelta, -yDelta);
            
			//System.err.println(String.format(".... @[0]//A: delta= %f, %f", xDelta, yDelta));

		}else if (xFinFront > body.getLength()) {
			final double xNew = body.getLength();
			final double yNew = yFinFront - body.getAftRadius(); 
			points.set(0, points.set(0, new Coordinate(xNew, yNew)));
			
			xDelta = xNew - xFinFront;
			yDelta = yNew - yFinFront;
			movePoints(-xDelta, -yDelta);
			//System.err.println(String.format(".... @[0]//B: delta= %f, %f", xDelta, yDelta));
			
		}else {
			// distance to move the entire fin by:
			xDelta = newPoint.x;
			yDelta = body.getRadius(xFinFront + xDelta) - yFinFront;
			movePoints(-xDelta, -yDelta);

			//System.err.println(String.format(".... @[0]//C: delta= %f, %f", xDelta, yDelta));
		}
	    
	    final int lastIndex = points.size()-1;
	    this.length = points.get(lastIndex).x;
	    
		if (AxialMethod.TOP == getAxialMethod()) {
			setAxialOffset(AxialMethod.TOP, getAxialOffset() + xDelta);
		} else if (AxialMethod.MIDDLE == getAxialMethod()) {
			setAxialOffset(AxialMethod.MIDDLE, getAxialOffset() + xDelta / 2);
		}
	}
	
	private void clampInteriorPoint(final int index) {
		final SymmetricComponent sym = (SymmetricComponent) this.getParent();

		final double xPrior = points.get(index).x;
		final double yPrior = points.get(index).y; 
		
		final Coordinate finFront = getFinFront();
		final double xFinFront = finFront.x; // x of fin start, body-frame
		final double yFinFront = finFront.y; // y of fin start, body-frame
		
		final double yBody = sym.getRadius(xPrior + xFinFront) - yFinFront;
		
		// ensure that an interior point is outside of its mounting body:
		if (yBody > yPrior) {
			points.set(index, points.get(index).setY(yBody));
		}
	}
	
	private void clampLastPoint(final Coordinate prior) {
		final SymmetricComponent body = (SymmetricComponent) getParent();
		
		final double xFinStart = getAxialFront(); // x of fin start, body-frame
		final double yFinStart = body.getRadius(xFinStart); // y of fin start, body-frame
		
		final double xBodyStart = -getAxialFront(); // x-offset from start-to-start; fin-frame
		final double xBodyEnd = xBodyStart + body.getLength(); /// x-offset from start-to-body; fin-frame
		 
		int lastIndex = points.size() - 1;
		final Coordinate cur = points.get(lastIndex);
		
		double xDelta=0;

		if (xBodyEnd < cur.x) {
			if(SNAP_SMALLER_THAN > Math.abs(xBodyEnd - cur.x)){
				points.set( lastIndex, new Coordinate(xBodyEnd, body.getAftRadius() - yFinStart));
			}else {
				// the last point is placed after the end of the mount-body
				points.add(new Coordinate(xBodyEnd, body.getAftRadius() - yFinStart));
			}
			
			if(null != prior) {
				xDelta = xBodyEnd - prior.x;
			}else{
				xDelta = xBodyEnd - cur.x;
			}
			//System.err.println(String.format(".... @[-1]//A: delta= %f", xDelta));
			
		}else if (cur.x < 0) {
			// the last point is positioned ahead of the first point.
			points.set(lastIndex, Coordinate.ZERO);
			
			xDelta = cur.x;
			
			//System.err.println(String.format(".... @[-1]//B: delta= %f", xDelta));

		} else {
			if(null != prior) {
				xDelta = cur.x - prior.x;
			}
			double yBody = body.getRadius(xFinStart + cur.x) - yFinStart;
			if(IGNORE_SMALLER_THAN < Math.abs(yBody - cur.y)) {
				// for the first and last points: set y-value to *exactly* match parent body:
				points.set(lastIndex, new Coordinate(cur.x, yBody));
			
			}
	    	
			
			//System.err.println(String.format(".... @[-1]//C: delta = %f", xDelta));
		}
		
		if(IGNORE_SMALLER_THAN < Math.abs(xDelta)) {
			lastIndex = points.size()-1;
			this.length = points.get(lastIndex).x;
		
			if (AxialMethod.MIDDLE == getAxialMethod()) {
				setAxialOffset(AxialMethod.MIDDLE, getAxialOffset() + xDelta / 2);
			} else if (AxialMethod.BOTTOM == getAxialMethod()) {
				setAxialOffset(AxialMethod.BOTTOM, getAxialOffset() + xDelta);
			}
		}
	}
	
	private boolean validate() {
		final Coordinate firstPoint = this.points.get(0);
		if (firstPoint.x != 0 || firstPoint.y != 0) {
			log.error("Start point illegal --  not located at (0,0): " + firstPoint + " (" + getName() + ")");
			return false;
		}
		
		final Coordinate lastPoint = this.points.get(points.size() - 1);
		if (lastPoint.x < 0) {
			log.error("End point illegal: end point starts in front of start point: " + lastPoint.x);
			return false;
		}
		
		// the last point *is* restricted to be on the surface of its owning component:
		SymmetricComponent symBody = (SymmetricComponent) this.getParent();
		if (null != symBody) {
			final double startOffset = this.getAxialFront();
			final Coordinate finStart = new Coordinate(startOffset, symBody.getRadius(startOffset));
			
			// campare x-values 
			final Coordinate finAtLast = lastPoint.add(finStart);
			if (symBody.getLength() < finAtLast.x) {
				log.error("End point falls after parent body ends: [" + symBody.getName() + "].  Exception: ",
						new IllegalFinPointException("Fin ends after its parent body \"" + symBody.getName() + "\". Ignoring."));
				log.error(String.format("    ..fin position:    (x: %12.10f   via: %s)", this.axialOffset, this.axialMethod.name()));
				log.error(String.format("    ..Body Length: %12.10f  finLength: %12.10f", symBody.getLength(), this.getLength()));
				log.error(String.format("    ..fin endpoint:    (x: %12.10f, y: %12.10f)", finAtLast.x, finAtLast.y));
				return false;
			}
			
			// compare the y-values
			final Coordinate bodyAtLast = finAtLast.setY(symBody.getRadius(finAtLast.x));
			if (0.0001 < Math.abs(finAtLast.y - bodyAtLast.y)) {
				String numbers = String.format("finStart=(%6.2g,%6.2g) // fin_end=(%6.2g,%6.2g) // body=(%6.2g,%6.2g)", finStart.x, finStart.y, finAtLast.x, finAtLast.y, bodyAtLast.x, bodyAtLast.y);
				log.error("End point does not touch its parent body [" + symBody.getName() + "].  exception: ",
						new IllegalFinPointException("End point does not touch its parent body! Expected: " + numbers));
				log.error("    .." + numbers);
				return false;
			}
		}
		
		if (intersects()) {
			log.error("found intersection in finset points!");
			return false;
		}
		
		final int lastIndex = points.size() - 1;
		final List<Coordinate> pts = this.points;
		for (int i = 0; i < lastIndex; i++) {
			if (pts.get(i).z != 0) {
				log.error("z-coordinate not zero");
				return false;
			}
		}
		
		return true;
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
			throw new IndexOutOfBoundsException("request validate of non-existent fin edge segment: " + targetIndex + "/" + points.size());
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
				log.error(String.format("Found intersection at %d-%d and %d-%d", targetIndex, targetIndex+1, comparisonIndex, comparisonIndex+1)); 
				log.error(String.format("                   between (%g, %g) => (%g, %g)", pt1.x, pt1.y, pt2.x, pt2.y)); 
				log.error(String.format("                       and (%g, %g) => (%g, %g)", pc1.x, pc1.y, pc2.x, pc2.y));
				return true;
			}
		}
		return false;
	}
	
}
