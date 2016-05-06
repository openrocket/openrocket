package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;


public class FreeformFinSet extends FinSet {
	private static final Logger log = LoggerFactory.getLogger(FreeformFinSet.class);
	private static final Translator trans = Application.getTranslator();
	
	private ArrayList<Coordinate> points = new ArrayList<Coordinate>();
	
	public FreeformFinSet() {
		points.add(Coordinate.NUL);
		points.add(new Coordinate(0.025, 0.05));
		points.add(new Coordinate(0.075, 0.05));
		points.add(new Coordinate(0.05, 0));
		
		this.length = 0.05;
	}
	
	
	public FreeformFinSet(Coordinate[] finpoints) throws IllegalFinPointException {
		setPoints(finpoints);
	}
	
	/*
	public FreeformFinSet(FinSet finset) {
		Coordinate[] finpoints = finset.getFinPoints();
		this.copyFrom(finset);

		points.clear();
		for (Coordinate c: finpoints) {
			points.add(c);
		}
		this.length = points.get(points.size()-1).x - points.get(0).x;
	}
	*/
	
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
		log.info("Converting " + finset.getComponentName() + " into freeform fin set");
		final RocketComponent root = finset.getRoot();
		FreeformFinSet freeform;
		List<RocketComponent> toInvalidate = Collections.emptyList();
		
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
			Coordinate[] finpoints = finset.getFinPoints();
			try {
				freeform = new FreeformFinSet(finpoints);
			} catch (IllegalFinPointException e) {
				throw new BugException("Illegal fin points when converting existing fin to " +
						"freeform fin, fin=" + finset + " points=" + Arrays.toString(finpoints),
						e);
			}
			
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
	 */
	public void addPoint(int index) {
		double x0, y0, x1, y1;
		
		x0 = points.get(index - 1).x;
		y0 = points.get(index - 1).y;
		x1 = points.get(index).x;
		y1 = points.get(index).y;
		
		points.add(index, new Coordinate((x0 + x1) / 2, (y0 + y1) / 2));
		// adding a point within the segment affects neither mass nor aerodynamics
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	/** 
	 * Returns the x dimesion of the fin-body base.  It is not a euclidean distance, but simply the x difference.
	 * 
	 * Base length may be larger OR smaller than the overall length, i.e. this.length
	 * 
	 * @return
	 */
	public double getBaseLength(){
		return (points.get(points.size()-1).x - points.get(0).x);
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
		
		@SuppressWarnings("unchecked")
		ArrayList<Coordinate> copy = (ArrayList<Coordinate>)this.points.clone();
		
		this.points.remove(index);
		if( ! validate()){
			// if error, rollback.  
			this.points = copy;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public int getPointCount() {
		return points.size();
	}

	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints
	 * @throws IllegalFinPointException
	 */
	public void setPoints(Coordinate[] newPoints) throws IllegalFinPointException {
		Coordinate p0 = newPoints[0];
		newPoints = translatePoints( newPoints, p0.x, p0.y);
		
		ArrayList<Coordinate> newList = new ArrayList<Coordinate>();
		newList.addAll( Arrays.asList( newPoints));
		setPoints( newList );
	}
	

	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints
	 * @throws IllegalFinPointException
	 */
	// WARNING:  this is the Openrocket custom ArrayList instance.   not the standard one... 
	public void setPoints( ArrayList<Coordinate> newPoints) throws IllegalFinPointException {
		@SuppressWarnings("unchecked")
		ArrayList<Coordinate> copy = (ArrayList<Coordinate>)this.points.clone();
		
		this.points = newPoints;
		if( ! validate()){
			this.points = copy;
		}
		
		this.length = points.get(points.size() - 1).x;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	/**
	 * Set the point at position <code>i</code> to coordinates (x,y).
	 * <p>
	 * Note that this method enforces basic fin shape restrictions (non-negative y,
	 * first and last point locations) silently, but throws an
	 * <code>IllegalFinPointException</code> if the point causes fin segments to
	 * intersect.
	 * <p>
	 * Moving of the first point in the X-axis is allowed, but this actually moves
	 * all of the other points the corresponding distance back.
	 *
	 * @param index	the point index to modify.
	 * @param x		the x-coordinate.
	 * @param y		the y-coordinate.
	 * @throws IllegalFinPointException	if the specified fin point would cause intersecting
	 * 									segments
	 */
	public void setPoint( final int index, double x, double y) throws IllegalFinPointException {
		// x,y start out in parent-space; so first, translate (x,y) into fin-space
		
		final SymmetricComponent sym = (Transition)getParent();
		final double x_fin = asPositionValue(Position.TOP); // x @ fin start, parent frame
		final double r_fin = sym.getRadius(x_fin); // radius of body @ fin start
		final double r_ref = sym.getForeRadius(); // reference radius of body (front)
		final double r_new = sym.getRadius(x); // radius of body @ point
		final double y_fin = r_fin - r_ref;  // y @ fin start, parent frame
		
		
		// ^^^^ parent-body-space corodinates ^^^^
		x -= x_fin;
		y -= y_fin;
		// vvvv we are now in fin-space coordinates vvvv

		final double y_body = r_new - r_fin;		
		if (y < y_body){
			y = y_body;
		}
		
		double x0, y0, x1, y1;
		
		System.err.println("  attempting to set last fin point to: "+x+", "+y);
		if (index == 0) {
			// Restrict first point to be in front of last point.
			x = Math.min(x, points.get(points.size() - 1).x);
			
			x0 = Double.NaN;
			y0 = Double.NaN;
			x1 = points.get(1).x;
			y1 = points.get(1).y;
		} else if (index == points.size() - 1) {
			// Restrict last point to be behind first point
			x = Math.max(points.get(0).x, x);
			
			// constrain x to connect back to the body before it ends
			x = Math.min(x, (sym.length - x_fin));
			y = sym.getRadius(x + x_fin) - r_fin; 

			x0 = points.get(index - 1).x;
			y0 = points.get(index - 1).y;
			x1 = Double.NaN;
			y1 = Double.NaN;
		} else {
			x0 = points.get(index - 1).x;
			y0 = points.get(index - 1).y;
			x1 = points.get(index + 1).x;
			y1 = points.get(index + 1).y;
		}
		
		// Check for intersecting
		double px0, py0, px1, py1;
		px0 = 0;
		py0 = 0;
		for (int i = 1; i < points.size(); i++) {
			px1 = points.get(i).x;
			py1 = points.get(i).y;
			
			if (i != index - 1 && i != index && i != index + 1) {
				if (intersects(x0, y0, x, y, px0, py0, px1, py1)) {
					throw new IllegalFinPointException("segments intersect");
				}
			}
			if (i != index && i != index + 1 && i != index + 2) {
				if (intersects(x, y, x1, y1, px0, py0, px1, py1)) {
					throw new IllegalFinPointException("segments intersect");
				}
			}
			
			px0 = px1;
			py0 = py1;
		}
		
		// if moving first point, translate entire fin to match
		if ( 0 == index ) {
			//System.out.println("Set point zero to x:" + x);
			for (int i = 1; i < points.size(); i++) {
				Coordinate c = points.get(i);
				points.set(i, c.setX(c.x - x));
			}
		} else {
			points.set(index, new Coordinate(x, y));
		}
		
		// set fin length
		if (index == 0 || index == points.size() - 1) {
			this.length = points.get(points.size() - 1).x;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	private boolean intersects(double ax0, double ay0, double ax1, double ay1,
			double bx0, double by0, double bx1, double by1) {
		
		double d = ((by1 - by0) * (ax1 - ax0) - (bx1 - bx0) * (ay1 - ay0));
		
		double ua = ((bx1 - bx0) * (ay0 - by0) - (by1 - by0) * (ax0 - bx0)) / d;
		double ub = ((ax1 - ax0) * (ay0 - by0) - (ay1 - ay0) * (ax0 - bx0)) / d;
		
		return (ua >= 0) && (ua <= 1) && (ub >= 0) && (ub <= 1);
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
	
	@SuppressWarnings("unchecked")
	@Override
	protected RocketComponent copyWithOriginalID() {
		RocketComponent c = super.copyWithOriginalID();
		
		((FreeformFinSet) c).points = (ArrayList<Coordinate>)this.points.clone();
		
		return c;
	}
	
	private boolean validate() {
		final Coordinate firstPoint = this.points.get(0);
		if (firstPoint.x != 0 || firstPoint.y != 0 ){
			log.error("Start point illegal --  not located at (0,0): "+firstPoint);
			return false;
		}
		final int lastIndex = this.points.size() - 1;
		final Coordinate lastPoint = this.points.get(0);
		if( lastPoint.x < 0){
			log.error("End point illegal: end point starts in front of start point: "+lastPoint.x);
			return false;
		}
		// the last point *is* restricted to be on the surface of its owning component:
		SymmetricComponent symBody = (SymmetricComponent)this.getParent();
		if( null != symBody ){
			// ^^^  fin frame ^^^
			// vvv body frame vvv 
			final double startOffset = this.asPositionValue( Position.TOP );
			final Coordinate finStart = this.position.setY( symBody.getRadius(startOffset) );
			
			// campare x-values 
			final Coordinate finAtLast = lastPoint.add(finStart); 
			if( symBody.getLength() < finAtLast.x ){
				log.error("End point falls after parent body ends: ["+symBody.getName()+"].  Exception: ", new IllegalFinPointException("Fin ends after its parent body \""+symBody.getName()+"\". Ignoring."));
				log.error(String.format("    ..fin end@         (%6.2g,%6.2g)", finAtLast.x, finAtLast.y));
				return false;
			}
			
			// compare the y-values
			final Coordinate bodyAtLast = finAtLast.setY( symBody.getRadius( finAtLast.x ) ); 
			if( 0.0001 < Math.abs( finAtLast.y - bodyAtLast.y) ){
				log.error("End point does not touch its parent body ["+symBody.getName()+"].  exception: ", new IllegalFinPointException("End point does not touch its parent body. illegal.")); 
				log.error(String.format("    ..fin end@         (%6.2g,%6.2g)", finAtLast.x, finAtLast.y));
				return false;
			}
		}

		final ArrayList<Coordinate> pts = this.points;
		for (int i = 0; i < lastIndex; i++) {
			for (int j = i + 2; j < lastIndex; j++) {
				if (intersects(pts.get(i).x, pts.get(i).y, pts.get(i + 1).x, pts.get(i + 1).y,
						pts.get(j).x, pts.get(j).y, pts.get(j + 1).x, pts.get(j + 1).y)) {
					log.error("segments intersect: indices: "+i+", "+j);
					return false;
				}
			}
			if (pts.get(i).z != 0) {
				log.error("z-coordinate not zero");
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public StringBuilder toDebugDetail(){
		StringBuilder buf = new StringBuilder( ">> ====== " + this.getComponentName() + " ======\n" );
		
		buf.append("    points:[\n");
		for( Coordinate pi : this.points ){
			buf.append(String.format("        (%8.4g, %8.4g),\n", pi.x, pi.y));
		}
		buf.append("    ]");
		
		return buf;
	}
	
	
}
