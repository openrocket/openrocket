package net.sf.openrocket.rocketcomponent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
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
	
	public static final double MIN_ROOT_CHORD=0.01; // enforce this to prevent erroneous 'intersection' exceptions.
	
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
				throw new BugException("Illegal fin points when converting existing fin to freeform fin, fin=" 
						+finset + " points=" + Arrays.toString(finpoints), e);
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
		
		fireComponentChangeEvent(ComponentChangeEvent.AERO_MASS_CHANGE);
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
	public void setPoints(Coordinate[] newPoints) {
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
	public void setPoints( ArrayList<Coordinate> newPoints) {
		
		// copy the old points, in case validation fails
		@SuppressWarnings("unchecked")
		ArrayList<Coordinate> copy = (ArrayList<Coordinate>)this.points.clone();

		this.points = newPoints;
		update();
		
		if( ! validate()){
			// on error, reset to the old points
			this.points = copy;
		}
		
		this.length = points.get(points.size() - 1).x;
		fireComponentChangeEvent(ComponentChangeEvent.AERO_MASS_CHANGE);
	}
	
	private double y_body( final double x){
		return y_body( x, 0.0 );
	}
	
	private double y_body( final double x_target, final double x_ref){
		final SymmetricComponent sym = (SymmetricComponent)getParent();
		return ( sym.getRadius(x_target) - sym.getRadius( x_ref));
	}
	
	public void setPointRelToFin( final int index, final double x_request_fin, final double y_request_fin) throws IllegalFinPointException {
		final double x_finStart_body = asPositionValue(Position.TOP); // x @ fin start, body frame
		final double y_finStart_body = y_body( x_finStart_body);
		
		setPoint( index, x_request_fin + x_finStart_body , y_request_fin + y_finStart_body);
	}
	
	/**
	 * Set the point at position <code>i</code> to coordinates (x,y).
	 * <p>
	 * Note that this method silently enforces basic fin shape restrictions 
	 *     - points may not be within the parent body.
	 *     - first point occurs before last (and vice versa) 
	 *     - first and last points must be on the parent body
	 *     - non-self-intersecting fin shape (aborts set on invalid fin point)
	 *     
	 * 
	 * <p>
	 * Moving of the first point in the X-axis is allowed, but this actually moves
	 * all of the other points the corresponding distance back.
	 *
	 * @param index	the point index to modify.
	 * @param x		the x-coordinate.
	 * @param y		the y-coordinate.
	 */
	public void setPoint( final int index, final double x_request, final double y_request) {
		final int lastPointIndex = this.points.size() - 1;
		final SymmetricComponent body = (SymmetricComponent)getParent();
		final double xFinStart = asPositionValue(Position.TOP); // x of fin start, body-frame
		final double xFinEnd = points.get(lastPointIndex).x;
		final double yFinStart = body.getRadius( xFinStart); // y of fin start, body-frame
		final double xBodyStart = -xFinStart; // x-offset from fin to body; fin-frame

	    // initial guess at these values.  Further checks take place below....
		double xAgreed = x_request;
		double yAgreed = y_request;

		// clamp x coordinates:
		// within bounds, and consistent with the rest of the fin (at this time).
		if(  0 == index ) {
			// restrict the first point to be between the parent's start, and the last fin point
			xAgreed = Math.max( xBodyStart, Math.min( xAgreed, xFinEnd - MIN_ROOT_CHORD ));
		}else if( lastPointIndex == index ){
			// restrict the last point to be between the first fin point, and the parent's end length.
			xAgreed = Math.max( MIN_ROOT_CHORD, Math.min( xAgreed, xBodyStart + body.getLength()));
		}
		
		// adjust y-value to be consistent with body
		final double yBody_atPoint= body.getRadius( xFinStart + xAgreed) - yFinStart;
		if (index == 0 || index == lastPointIndex) {
			// for the first and last points: set y-value to *exactly* match parent body:
			yAgreed = yBody_atPoint;
		}else{
			// for all other points, merely insist that the point is outside the body...
			yAgreed = Math.max( yAgreed, yBody_atPoint);
		}
		
		// if moving either begin or end points, we'll probably have to update the position, as well.
		final Position locationMethod = getRelativePositionMethod();
		final double priorXOffset = getAxialOffset();
		
		if( 0 == index){
			movePoints( xAgreed);
			this.length = points.get( points.size() -1 ).x;
			
			if( Position.TOP == locationMethod){ 
				setAxialOffset( Position.TOP, priorXOffset + xAgreed );
			}else if(Position.MIDDLE == locationMethod){
				setAxialOffset( Position.MIDDLE, priorXOffset + xAgreed/2 );
			}
		}else if( lastPointIndex == index ){
			points.set(index, new Coordinate( xAgreed, yAgreed ));
			this.length = xAgreed;
			
			if( Position.MIDDLE == locationMethod){ 
				setAxialOffset( Position.MIDDLE, priorXOffset + (xAgreed - xFinEnd)/2 );
			}else if(Position.BOTTOM== locationMethod){
				setAxialOffset( Position.BOTTOM, priorXOffset + (xAgreed - xFinEnd) );
			}
		}else{
			points.set(index, new Coordinate( xAgreed, yAgreed ));			
		}
		
		// this maps the last index and the next-to-last-index to the same 'testIndex'
		int testIndex = Math.min( index, (points.size() - 2));
		if( intersects( testIndex)){
			// intersection found!  log error and abort!
			log.error(String.format("ERROR: found an intersection while setting fin point #%d to [%6.4g, %6.4g] <body frame> : ABORTING setPoint(..) !! ", index, x_request, y_request));
			return;
		}

		fireComponentChangeEvent(ComponentChangeEvent.AERO_MASS_CHANGE);
	}

	final private void movePoints( final double delta_x){
		// skip 0th index -- it's the local origin and is always (0,0) 
		for( int index=1; index < points.size(); ++index){
			final Coordinate oldPoint = this.points.get( index);
			final Coordinate newPoint = oldPoint.sub( delta_x,  0.0f,  0.0f);
			points.set( index, newPoint);
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
	
	@SuppressWarnings("unchecked")
	@Override
	protected RocketComponent copyWithOriginalID() {
		RocketComponent c = super.copyWithOriginalID();
		
		((FreeformFinSet) c).points = (ArrayList<Coordinate>)this.points.clone();
		
		return c;
	}
	
	@Override
	public void setAxialOffset( final Position newPositionMethod, final double newOffsetRequest){
		super.setAxialOffset( newPositionMethod, newOffsetRequest);

		double newOffset = newOffsetRequest;
		
		// if the new position would cause fin overhang, only allow movement up to the end of the parent component.
		// N.B. if you want a fin to overhang, add & adjust interior points.
		final double backOverhang = asPositionValue(Position.BOTTOM);
		if( 0 <  backOverhang ){
			
			newOffset -= backOverhang;
			super.setAxialOffset( newPositionMethod, newOffset );
		}
		final double frontOverhang = asPositionValue(Position.TOP);
		if( 0 > frontOverhang){
			
			newOffset -= frontOverhang;
			super.setAxialOffset( newPositionMethod, newOffset );
		}
	}
	
	@Override
	public void update(){
		final int lastPointIndex = this.points.size() - 1;
		this.length = points.get(lastPointIndex).x;
		
		this.setAxialOffset( this.relativePosition, this.x_offset);
		
		clampFirstPoint();
		clampInteriorPoints();
		clampLastPoint();
		
		validateFinTab();
	}
	
	// if we translate the points, correct the first point, because it may be inconsistent
	public void clampFirstPoint(){
		double xFinStart = asPositionValue(Position.TOP); // x @ fin start, body frame
		final double xFinOffset = getAxialOffset();
		if( 0 > xFinStart ){
			setAxialOffset( xFinOffset - xFinStart);
			xFinStart = asPositionValue(Position.TOP);
		}
	}
	
	public void clampInteriorPoints(){
		final double yFinStart = getBodyRadius( 0.0f );
		
		// omit end points index 
		for( int index=1; index < (points.size()-1); ++index){
			final Coordinate oldPoint = this.points.get( index);
			
			final double yBody = getBodyRadius( oldPoint.x);
			final double yFinPoint = yFinStart + oldPoint.y;
			
			if( yBody > yFinPoint ){
				final Coordinate newPoint = oldPoint.setY( yBody - yFinStart );
				points.set( index, newPoint);
			}
		}
	}
		
	// if we translate the points, the final point may become inconsistent
	public void clampLastPoint(){
		if( null == this.parent ){
			// this is bad, but seems to only occur during unit tests.
			return;
		}
		
		final SymmetricComponent body = (SymmetricComponent)getParent();
		// clamp the final x coord to the end of the parent body.
		final int lastPointIndex = points.size() - 1;
		final Coordinate oldPoint = points.get( lastPointIndex);
		
		final double xFinStart_body = asPositionValue(Position.TOP); // x @ fin start, body frame
		final double xBodyEnd_fin = body.getLength() - xFinStart_body;
		
		double x_clamped = Math.min( oldPoint.x, xBodyEnd_fin); 
		double y_clamped = body.getRadius( x_clamped+xFinStart_body) - body.getRadius( xFinStart_body); 
		
		
 		points.set( lastPointIndex, new Coordinate( x_clamped, y_clamped, 0));
	}
	
	private boolean validate() {
		final Coordinate firstPoint = this.points.get(0);
		if (firstPoint.x != 0 || firstPoint.y != 0 ){
			log.error("Start point illegal --  not located at (0,0): "+firstPoint+ " ("+ getName()+")");
			return false;
		}
		final int lastIndex = this.points.size() - 1;
		final Coordinate lastPoint = this.points.get( points.size() -1);
		if( lastPoint.x < 0){
			log.error("End point illegal: end point starts in front of start point: "+lastPoint.x);
			return false;
		}
		
		// the last point *is* restricted to be on the surface of its owning component:
		SymmetricComponent symBody = (SymmetricComponent)this.getParent();
		if( null != symBody ){
			final double startOffset = getTop();
			final Coordinate finStart = new Coordinate( startOffset, symBody.getRadius(startOffset) );
			
			// campare x-values 
			final Coordinate finAtLast = lastPoint.add(finStart); 
			if( symBody.getLength() < finAtLast.x ){
				log.error("End point falls after parent body ends: ["+symBody.getName()+"].  Exception: ", new IllegalFinPointException("Fin ends after its parent body \""+symBody.getName()+"\". Ignoring."));
				log.error(String.format("    ..fin position:    (x: %12.10f   via: %s)", this.x_offset, this.relativePosition.name()));
				log.error(String.format("    ..Body Length: %12.10f  finLength: %12.10f", symBody.getLength(), this.getLength()));
				log.error(String.format("    ..fin endpoint:    (x: %12.10f, y: %12.10f)", finAtLast.x, finAtLast.y));
				return false;
			}
			
			// compare the y-values
			final Coordinate bodyAtLast = finAtLast.setY( symBody.getRadius( finAtLast.x ) ); 
			if( 0.0001 < Math.abs( finAtLast.y - bodyAtLast.y) ){
				String numbers = String.format( "finStart=(%6.2g,%6.2g) // fin_end=(%6.2g,%6.2g) // body=(%6.2g,%6.2g)", finStart.x, finStart.y, finAtLast.x, finAtLast.y, bodyAtLast.x, bodyAtLast.y );
				log.error("End point does not touch its parent body ["+symBody.getName()+"].  exception: ", new IllegalFinPointException("End point does not touch its parent body! Expected: "+numbers));
				log.error("    .."+numbers);
				return false;
			}
		}

		if( intersects()){
			log.error("found intersection in finset points!");
			return false;
		}
		
		final ArrayList<Coordinate> pts = this.points;
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
	public boolean intersects( ){
		for( int index=0; index < (this.points.size()-1); ++index ){
			if( intersects( index )){
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * Check if the line segment from targetIndex to targetIndex+1 intersects with any other part of the fin.
	 * 
	 * 
	 * 
	 * @return  true if an intersection was found
	 */
	private boolean intersects( final int targetIndex){
		if( (points.size()-2) < targetIndex ){
			throw new IndexOutOfBoundsException("request validate of non-existent fin edge segment: "+ targetIndex + "/"+points.size());
		}
		
		// (pre-check the indices above.)
		Point2D.Double p1 = new Point2D.Double( points.get(targetIndex).x, points.get(targetIndex).y);
		Point2D.Double p2 = new Point2D.Double( points.get(targetIndex+1).x, points.get(targetIndex+1).y);
		Line2D.Double targetLine = new Line2D.Double( p1, p2);
		
		for (int comparisonIndex = 0; comparisonIndex < (points.size()-1); ++comparisonIndex ) {
			if( 2 > Math.abs( targetIndex - comparisonIndex) ){
				// a line segment will trivially not intersect with itself
				// nor can adjacent line segments intersect with each other, because they share a common endpoint.
				continue; 
			}
			
			Line2D.Double comparisonLine = new Line2D.Double( points.get(comparisonIndex).x, points.get(comparisonIndex).y, // p1 
															  points.get(comparisonIndex+1).x, points.get(comparisonIndex+1).y); // p2
			
			if ( targetLine.intersectsLine( comparisonLine ) ) {
				return true;
			}
		}
		return false;
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
