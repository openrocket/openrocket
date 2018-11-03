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
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;


public class FreeformFinSet extends FinSet {
	private static final Logger log = LoggerFactory.getLogger(FreeformFinSet.class);
	private static final Translator trans = Application.getTranslator();
	
	public static final double MIN_ROOT_CHORD=0.01; // enforce this to prevent erroneous 'intersection' exceptions.
	
	private List<Coordinate> points = new ArrayList<>();
	
	public FreeformFinSet() {
		points.add(Coordinate.ZERO);
		points.add(new Coordinate(0.025, 0.05));
		points.add(new Coordinate(0.075, 0.05));
		points.add(new Coordinate(0.05, 0));
		
		this.length = 0.05;
	}

	public FreeformFinSet(Coordinate[] finpoints) {
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
			freeform = new FreeformFinSet(finpoints);
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
		List<Coordinate> copy = new ArrayList<>(this.points);
		
		this.points.remove(index);
		if( ! validate()){
			// if error, rollback.  
			this.points = copy;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	
	public int getPointCount() {
		return points.size();
	}

	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints new fin points ; replaces previous fin points
	 */
	public void setPoints(Coordinate[] newPoints) {
		if( ! Coordinate.ZERO.equals(newPoints[0])) {
			final Coordinate p0 = newPoints[0];
			newPoints = translatePoints(newPoints, p0.x, p0.y);
		}

		ArrayList<Coordinate> newList = new ArrayList<>(Arrays.asList( newPoints));
		setPoints( newList );
	}
	

	/**
	 * The first point is assumed to be at the origin.  If it isn't, it will be moved there.
	 * 
	 * @param newPoints New points to set as the exposed edges of the fin
	 */
	public void setPoints( List<Coordinate> newPoints) {
		// copy the old points, in case validation fails
		List<Coordinate> copy = new ArrayList<>(this.points);

		this.points = newPoints;
		update();

		if( ! validate()){
			// on error, reset to the old points
			this.points = copy;
		}
		
		this.length = points.get(points.size() - 1).x;
		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}
	
	private double y_body( final double x){
		return y_body( x, 0.0 );
	}
	
	private double y_body( final double x_target, final double x_ref){
		final SymmetricComponent sym = (SymmetricComponent)getParent();
		return ( sym.getRadius(x_target) - sym.getRadius( x_ref));
	}
	
	public void setPointRelToFin( final int index, final double x_request_fin, final double y_request_fin) throws IllegalFinPointException {
		final double x_finStart_body = getAxialFront(); // x @ fin start, body frame
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
	public void setPoint( final int index, final double xRequest, final double yRequest) {
		final SymmetricComponent body = (SymmetricComponent)getParent();

		final int lastPointIndex = this.points.size() - 1;
		final double xFinEnd = points.get(lastPointIndex).x;
		final double xFinStart = getAxialFront(); // x of fin start, body-frame
		final double yFinStart = body.getRadius( xFinStart); // y of fin start, body-frame
		final double xBodyStart = -xFinStart; // x-offset from fin to body; fin-frame

	    // initial guess at these values; further checks follow.
		double xAgreed = xRequest;
		double yAgreed = yRequest;

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
		final AxialMethod locationMethod = getAxialMethod();
		final double priorXOffset = getAxialOffset();
		
		if( 0 == index){
			movePoints( xAgreed);
			this.length = points.get( lastPointIndex ).x;
			
			if( AxialMethod.TOP == locationMethod){ 
				setAxialOffset( AxialMethod.TOP, priorXOffset + xAgreed );
			}else if(AxialMethod.MIDDLE == locationMethod){
				setAxialOffset( AxialMethod.MIDDLE, priorXOffset + xAgreed/2 );
			}
		}else if( lastPointIndex == index ){
			points.set(index, new Coordinate( xAgreed, yAgreed ));
			this.length = xAgreed;
			
			if( AxialMethod.MIDDLE == locationMethod){ 
				setAxialOffset( AxialMethod.MIDDLE, priorXOffset + (xAgreed - xFinEnd)/2 );
			}else if(AxialMethod.BOTTOM== locationMethod){
				setAxialOffset( AxialMethod.BOTTOM, priorXOffset + (xAgreed - xFinEnd) );
			}
		}else{
			points.set(index, new Coordinate( xAgreed, yAgreed ));			
		}
		
		// this maps the last index and the next-to-last-index to the same 'testIndex'
		int testIndex = Math.min( index, (points.size() - 2));
		if( intersects( testIndex)){
			// intersection found!  log error and abort!
			log.error(String.format("ERROR: found an intersection while setting fin point #%d to [%6.4g, %6.4g] <body frame> : ABORTING setPoint(..) !! ", index, xRequest, yRequest));
			return;
		}

		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}

	private void movePoints( final double delta_x){
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
		
		((FreeformFinSet) c).points = new ArrayList<>(this.points);
		
		return c;
	}
	
	@Override
	public void setAxialOffset( final AxialMethod newAxialMethod, final double newOffsetRequest){
		super.setAxialOffset( newAxialMethod, newOffsetRequest);

		if( null != parent ) {
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
	public void update(){
		final int lastPointIndex = this.points.size() - 1;
		this.length = points.get(lastPointIndex).x;
		
		this.setAxialOffset( this.axialMethod, this.axialOffset);
		
		clampFirstPoint();
		clampInteriorPoints();
		clampLastPoint();
		
		validateFinTab();
	}
	
	// if we translate the points, correct the first point, because it may be inconsistent
	private void clampFirstPoint(){
		double xFinStart = getAxialFront(); // x @ fin start, body frame
		final double xFinOffset = getAxialOffset();
		if( 0 > xFinStart ){
			setAxialOffset( xFinOffset - xFinStart);
		}
	}
	
	private void clampInteriorPoints(){
		if( null == this.parent ){
			// this is bad, but seems to only occur during unit tests.
			return;
		}
		final SymmetricComponent symmetricParent = (SymmetricComponent)this.getParent();
		
		final Coordinate finFront = getFinFront();
		
		// omit end points index 
		for( int index=1; index < (points.size()-1); ++index){
			final Coordinate oldPoint = this.points.get( index);
			
			final double yBody = symmetricParent.getRadius( oldPoint.x + finFront.x);
			final double yFinPoint = finFront.y+ oldPoint.y;
			
			if( yBody > yFinPoint ){
				final Coordinate newPoint = oldPoint.setY( yBody - finFront.y );
				points.set( index, newPoint);
			}
		}
	}
		
	// if we translate the points, the final point may become inconsistent
	private void clampLastPoint(){
		if( null == this.parent ){
			// this is bad, but seems to only occur during unit tests.
			return;
		}
		
		final SymmetricComponent body = (SymmetricComponent)getParent();
		// clamp the final x coord to the end of the parent body.
		final int lastPointIndex = points.size() - 1;
		final Coordinate oldPoint = points.get( lastPointIndex);
		
		final double xFinStart_body = getAxialFront(); // x @ fin start, body frame
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

		final Coordinate lastPoint = this.points.get( points.size() -1);
		if( lastPoint.x < 0){
			log.error("End point illegal: end point starts in front of start point: "+lastPoint.x);
			return false;
		}

		// the last point *is* restricted to be on the surface of its owning component:
		SymmetricComponent symBody = (SymmetricComponent)this.getParent();
		if( null != symBody ){
			final double startOffset = this.getAxialFront();
			final Coordinate finStart = new Coordinate( startOffset, symBody.getRadius(startOffset) );
			
			// campare x-values 
			final Coordinate finAtLast = lastPoint.add(finStart); 
			if( symBody.getLength() < finAtLast.x ){
				log.error("End point falls after parent body ends: ["+symBody.getName()+"].  Exception: ", new IllegalFinPointException("Fin ends after its parent body \""+symBody.getName()+"\". Ignoring."));
				log.error(String.format("    ..fin position:    (x: %12.10f   via: %s)", this.axialOffset, this.axialMethod.name()));
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

}
