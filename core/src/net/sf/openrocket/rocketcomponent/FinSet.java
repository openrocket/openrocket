package net.sf.openrocket.rocketcomponent;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PointWeight;
import net.sf.openrocket.util.Transformation;


public abstract class FinSet extends ExternalComponent implements RingInstanceable {
	private static final Translator trans = Application.getTranslator();
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FinSet.class);
	
	
	
	/**
	 * Maximum allowed cant of fins.
	 */
	public static final double MAX_CANT = (15.0 * Math.PI / 180);
	
	
	public enum CrossSection {
		//// Square
		SQUARE(trans.get("FinSet.CrossSection.SQUARE"), 1.00),
		//// Rounded
		ROUNDED(trans.get("FinSet.CrossSection.ROUNDED"), 0.99),
		//// Airfoil
		AIRFOIL(trans.get("FinSet.CrossSection.AIRFOIL"), 0.85);
		
		private final String name;
		private final double volume;
		
		CrossSection(String name, double volume) {
			this.name = name;
			this.volume = volume;
		}
		
		public double getRelativeVolume() {
			return volume;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Number of fins.
	 */
	protected int finCount = 3;
	
	/**
	 * Rotation about the x-axis by 2*PI/fins.
	 */
	protected Transformation finRotation = Transformation.rotate_x(2 * Math.PI / finCount);
	
	/**
	 * Rotation angle of the first fin.  Zero corresponds to the positive y-axis.
	 */
	protected double rotation = 0;
	
	/**
	 * Rotation about the x-axis by angle this.rotation.
	 */
	protected Transformation baseRotation = Transformation.rotate_x(rotation);
	
	
	/**
	 * Cant angle of fins.
	 */
	protected double cantAngle = 0;
	
	/* Cached value: */
	private Transformation cantRotation = null;
	
	
	/**
	 * Thickness of the fins.
	 */
	protected double thickness = 0.003;

	
	
	/**
	 * The cross-section shape of the fins.
	 */
	protected CrossSection crossSection = CrossSection.SQUARE;
	
	
	/*
	 * Fin tab properties.
	 */
	private static final double minimumTabArea = 1e-8;
	private double tabHeight = 0;
	private double tabLength = 0.05;
	private double tabShift = 0;
	private Position tabRelativePosition = Position.MIDDLE;
	
	/*
	 * Fin fillet properties
	 */
	protected Material filletMaterial = null;
	protected double filletRadius = 0;
	protected double filletCenterY = 0;
	
	// wetted area 
	private PointWeight wettedArea = new PointWeight();
	// these coordinates are weighted by AREA: simple component-total area (incl. tabs)
	private double volume = Double.NaN;
	private PointWeight mass = new PointWeight(); 
	
	/**
	 * New FinSet with given number of fins and given base rotation angle.
	 * Sets the component relative position to POSITION_RELATIVE_BOTTOM,
	 * i.e. fins are positioned at the bottom of the parent component.
	 */
	public FinSet() {
		super( Position.BOTTOM);
		this.filletMaterial = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
	}
	
	@Override
	public boolean isAfter(){ 
		return false; 
	}
	
	/**
	 * Return the number of fins in the set.
	 * @return The number of fins.
	 */
	public int getFinCount() {
		return finCount;
	}
	
	/**
	 * Sets the number of fins in the set.
	 * @param n The number of fins, greater of equal to one.
	 */
	public void setFinCount(int n) {
		if (finCount == n)
			return;
		if (n < 1)
			n = 1;
		if (n > 8)
			n = 8;
		finCount = n;
		finRotation = Transformation.rotate_x(2 * Math.PI / finCount);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public Transformation getFinRotationTransformation() {
		return finRotation;
	}
	
	/**
	 * Gets the base rotation amount of the first fin.
	 * @return The base rotation amount.
	 */
	public double getBaseRotation() {
		return rotation;
	}
	
	/**
	 * Sets the base rotation amount of the first fin.
	 * @param r The base rotation amount.
	 */
	public void setBaseRotation(double r) {
		r = MathUtil.reduce180(r);
		if (MathUtil.equals(r, rotation))
			return;
		rotation = r;
		baseRotation = Transformation.rotate_x(rotation);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public Transformation getBaseRotationTransformation() {
		return baseRotation;
	}
	
	public double getCantAngle() {
		return cantAngle;
	}
	
	public void setCantAngle(double cant) {
		cant = MathUtil.clamp(cant, -MAX_CANT, MAX_CANT);
		if (MathUtil.equals(cant, cantAngle))
			return;
		this.cantAngle = cant;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public Transformation getCantRotation() {
		if (cantRotation == null) {
			if (MathUtil.equals(cantAngle, 0)) {
				cantRotation = Transformation.IDENTITY;
			} else {
				Transformation t = new Transformation(-length / 2, 0, 0);
				t = Transformation.rotate_y(cantAngle).applyTransformation(t);
				t = new Transformation(length / 2, 0, 0).applyTransformation(t);
				cantRotation = t;
			}
		}
		return cantRotation;
	}
	
	public double getThickness() {
		return thickness;
	}
	
	public void setThickness(double r) {
		if (thickness == r)
			return;
		thickness = Math.max(r, 0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public CrossSection getCrossSection() {
		return crossSection;
	}
	
	public void setCrossSection(CrossSection cs) {
		if (crossSection == cs)
			return;
		crossSection = cs;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public void setRelativePosition( Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public double getTabHeight() {
		return tabHeight;
	}
	
	/**
	 * Set the height from the fin's base at the reference point -- i.e. where the tab is located from.  If the tab is located via BOTTOM, then the back edge will be 
	 * <code>height</code> deep, and the bottom edge of the tab will be parallel to the stage centerline.  If the tab is located via TOP, the the front edge will have corresponding height/depth. 
	 * If the tab is located via MIDDLE, the tab's midpoint is used.
	 * 
	 * Note this function also does bounds checking, and will not set a tab height that passes through it's parent's midpoint.  
	 *  
	 * @param newHeightRequest how deep the fin tab should project from the fin root, at the reference point 
	 * 
	 */
	public void setTabHeight(double newHeightRequest) {
		newHeightRequest = MathUtil.max(newHeightRequest, 0);
		if (MathUtil.equals(this.tabHeight, newHeightRequest)){
			return;
		}
		
		this.tabHeight = newHeightRequest;
		
		validateFinTab();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getTabLength() {
		return tabLength;
	}
	
	public void setTabLength(double length) {
		length = MathUtil.max(length, 0);
		if (MathUtil.equals(this.tabLength, length))
			return;
		tabLength = length;
		validateFinTab();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getTabShift() {
		return tabShift;
	}
	
	/** 
	 * internally, set the internal  
	 * 
	 * @param newShift
	 */
	public void setTabShift( final double newShift) {
		this.tabShift = newShift;
		
		validateFinTab();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public Position getTabPositionMethod() {
		return tabRelativePosition;
	}
	
	public void setTabPositionMethod( final Position newPositionMethod) {
		final double oldFront = getTabFrontEdge();
		
		this.tabRelativePosition = newPositionMethod;
		this.tabShift = Position.getShift( newPositionMethod, oldFront, this.length, this.tabLength);
		
		fireComponentChangeEvent( ComponentChangeEvent.MASS_CHANGE);
	}
	
	/**
	 * Return the tab front edge position from the front of the fin.
	 */
	public double getTabFrontEdge() {
		return Position.getTop( this.tabShift, this.tabRelativePosition, this.length, this.tabLength );
	}
	
	/**
	 * Return the tab trailing edge position *from the front of the fin*.
	 */
	public double getTabTrailingEdge() {
		return (getTabFrontEdge() + tabLength);
	}
	
	/*
	 * This is the point which defines the tab position -- and where the tab height is measure from.
	 * note: this is a permutation of its usual role... here we just set the tab length to zero...
	 *  
	 */
	public double getTabReferenceOffset(){ 
	    return Position.getTop( this.tabShift, this.tabRelativePosition, this.length, 0 );
	}

	
	public void validateFinTab(){
		// check tab length 
		if( this.tabLength > this.length ){
			this.tabLength = this.length;
		}
		
		//check front bounds:
	    final double xTabFront = getTabFrontEdge();
		if( 0 > xTabFront){
			this.tabShift -= xTabFront;
	    }
		
		//check tail bounds:
	    final double xTabBack = getTabTrailingEdge();
		if( this.length < xTabBack ){
	        this.tabShift -= (xTabBack - this.length);
		}
		
		// check tab height 
		if( null != getParent() ){
			// pulls the parent-body radius at the fin-tab reference point.
			final double xRef = this.getTabReferenceOffset();
			final double bodyRadius = this.getBodyRadius( xRef);
			
			// limit the new heights to be no greater than the current body radius.
			this.tabHeight = Math.min( this.tabHeight,  bodyRadius );
		}
	}
	
	///////////  Calculation methods  //////////
	/**
	 * Return the area of a *single* fin exposed to the airflow (i.e. external area) 
	 * N.B. counts only one side of each fin,  
	 * https://en.wikipedia.org/wiki/Wetted_area
	 * 
	 * @return returns the one-sided air-exposed area of a single fin  
	 */
	public double getFinWettedArea() {
		if( wettedArea.isNaN()){
			updatePhysicalProperties();
		}
		return wettedArea.w;
	}
	
	@Override
	public int getInstanceCount(){
		return getFinCount();
	}
	
	@Override
	public double getComponentMass() {
		return getComponentVolume()*material.getDensity();
	}
	
	@Override
	public double getComponentVolume() {
		if( Double.isNaN( this.volume)){
			this.updatePhysicalProperties();
		}
	
		// this is for the fins alone, fillets are taken care of separately.
		return finCount * this.volume;
	}
	
	/**
	 * Return the unweighted CG of a single fin.  The X-coordinate is relative to
	 * the root chord leading edge and the Y-coordinate to the fin root chord.
	 * 
	 * @return  the unweighted CG coordinate of a single fin. 
	 */	
	@Override
	public Coordinate getComponentCG() {
		if( this.mass.isNaN() ){
			updatePhysicalProperties();
		}
		
		return this.mass.toCoordinate();
	}
	
	public PointWeight calculateFilletVolumeCentroid(){
		PointWeight filletVolume = PointWeight.empty();

		Coordinate[] bodyPoints = this.getBodyPoints();
		if( 0 == bodyPoints.length ){
			return filletVolume;
		}
		if ( ! SymmetricComponent.class.isInstance( this.parent )) {
			return filletVolume;
		}
		
		
		Coordinate prev = bodyPoints[0]; 
		for( int index = 1; index < bodyPoints.length; index++){
			Coordinate cur = bodyPoints[index];
			
			final double xAvg= (prev.x + cur.x)/2;
			final double yAvg = (prev.y + cur.y)/2;
			
			// cross section at mid-segment
			final PointWeight segmentCrossSection = calculateFilletCrossSection( yAvg, this.filletRadius);
			
			final double xCentroid= xAvg;
			final double yCentroid = segmentCrossSection.y; ///< heuristic, not exact
			final double segmentLength = Point2D.Double.distance(prev.x, prev.y, cur.x, cur.y);
			final double segmentVolume = segmentLength * segmentCrossSection.w;
			
			PointWeight segmentCentroid = new PointWeight( xCentroid, yCentroid, 0, segmentVolume);
			filletVolume = filletVolume.add( segmentCentroid );
			
			prev=cur;
		}
		
		return filletVolume;
	}
	
	
	public static PointWeight calculateFilletCrossSection( final double bodyRadius, final double filletRadius ){
		/*
		 * Here is how the volume of the fillet is found.  It assumes a circular concave 
		 * fillet tangent to the fin and the body tube. 
		 * 
		 * 1. Form a triangle with vertices at the BT center, the tangent point between 
		 *    the fillet and the fin, and the center of the fillet radius.
		 * 2. The line between the center of the BT and the center of the fillet radius 
		 *    will pass through the tangent point between the fillet and the BT.
		 * 3. Find the area of the triangle, then subtract the portion of the BT and 
		 *    fillet that is in that triangle. (angle/2PI * pi*r^2= angle/2 * r^2)
		 * 4. Multiply the remaining area by the length.
		 * 5. Return twice that since there is a fillet on each side of the fin.
		 */
		double hypotenuse = filletRadius + bodyRadius;
		double innerArcAngle = Math.asin(filletRadius / hypotenuse);
		double outerArcAngle = Math.acos(filletRadius / hypotenuse);

		double triangleArea = Math.tan(outerArcAngle) * filletRadius * filletRadius / 2;
		double crossSectionArea = (triangleArea 
									- outerArcAngle * filletRadius * filletRadius / 2
									- innerArcAngle * bodyRadius * bodyRadius / 2);
		
		// each fin has a fillet on each side
		crossSectionArea *= 2;
		
		// heuristic, relTo the body center
		double yCentroid = bodyRadius + filletRadius /5;

		return new PointWeight( 0, yCentroid, 0, crossSectionArea);
	}
	



/**
	 * \brief  calculate the area-under-the-curve (i.e. the integral) in the form of a centroid + area 
	 *  
	 * @param points define a piece-wise line bounding the area.    
	 * @return  centroid of the area, additionaly the area is stored as the weight
	 */
	public static PointWeight calculateCurveIntegral( final Coordinate[] points ){
		PointWeight centroidSum = new PointWeight(0);
		
		if( 0 == points.length ){
			return centroidSum;
		}
		
		Coordinate prev= points[0];
		for( int index = 1; index < points.length; index++){
			Coordinate cur = points[index];
			
			final double delta_x = (cur.x - prev.x);
			final double y_avg = (cur.y + prev.y)/2;
			
			// calculate marginal area
			double area_increment = delta_x*y_avg;
			if( MathUtil.equals( 0, area_increment)){
				prev = cur;
				// zero area slice: ignore and continue;
				continue;
			}
			
			// calculate centroid increment 
			double common = (0.333333333/(cur.y+prev.y));
			double x_ctr = common*(  prev.x*(2*prev.y+cur.y) + cur.x*(2*cur.y+prev.y));
			double y_ctr = common*( cur.y*prev.y + Math.pow( cur.y, 2) + Math.pow( prev.y, 2));
		
			PointWeight centroid_increment = new PointWeight( x_ctr, y_ctr, 0, area_increment);
			centroidSum = centroidSum.add( centroid_increment );
			
			prev=cur;
		}
		
		return centroidSum;
	}

	
	private PointWeight calculateTabCentroid(){
		RocketComponent comp = getParent();

		if( (null == comp) 
				|| (!( comp instanceof SymmetricComponent))
				|| isTabTrivial() ){
			// if null or invalid type:
			return new PointWeight(0);
		}
		// relto: fin
		final double xTabFront_fin = getTabFrontEdge();
		final double xTabTrail_fin = getTabTrailingEdge();
		
		// cast AND assert:
		final double xFinFront_body = getTop();
		final double xTabFront_body = xFinFront_body + xTabFront_fin;
		final double xTabTrail_body = xFinFront_body + xTabTrail_fin;
				
		// always returns x coordinates relTo fin front:
		Coordinate[] upperCurve = getBodyPoints( xTabFront_body, xTabTrail_body );
		// locate relative to fin/body centerline
		upperCurve = translatePoints( upperCurve, -xFinFront_body, 0.0);
		
		Coordinate[] lowerCurve = translateToCenterline( getTabPoints());
		
		final Coordinate[] tabPoints = combineCurves( upperCurve, lowerCurve);
				
		final PointWeight totalCentroid = calculateCurveIntegral( tabPoints );
        
		return totalCentroid;
	}
	
	private Coordinate[] translateToCenterline( final Coordinate[] fromRoot) {
		Coordinate finRoot = this.getFinFront();
		
		// locate relative to fin/body centerline
		final Coordinate[] fromCenterline = FinSet.translatePoints( fromRoot, 0.0d, finRoot.y);
						
		return fromCenterline;
	}

	/* 
	 * The coordinate contains an x,y coordinate of the centroid, and the raw alread is stored in the weight field.
	 */
	private PointWeight calculateWettedAreaCentroid(){
		final double yFinFront = getBodyRadius(0);
		
		final Coordinate[] upperCurve = translatePoints( getFinPoints(), 0.0, yFinFront ); 
		final Coordinate[] lowerCurve = getBodyPoints();
		final Coordinate[] totalCurve = combineCurves( upperCurve, lowerCurve); 

		final PointWeight wettedAreaCentroid = calculateCurveIntegral( totalCurve );
		
		return wettedAreaCentroid;
	}

	/** 
	 * copies the supplied areas into a third array, such that the first curve is copied forward, and the second is copied in reverse.
	 * 
	 *   The motivation is to use the two sets of forward points to produce a single close curve, suitable for an integration operation 
	 * 
	 * @param c1 forward curve
	 * @param c2  backward curve
	 * @return combined curve
	 */
	public Coordinate[] combineCurves( final Coordinate[] c1, final Coordinate[] c2){
		Coordinate[] returnCurve = new Coordinate[ c1.length + c2.length ];
		
		// copy the first array to the start of the return array...
		System.arraycopy(c1, 0, returnCurve, 0, c1.length);

		Coordinate[] revCurve = reverse( c2);
		int readIndex = 0; // to eliminate duplicate points
		int writeIndex = c1.length; // start directly after previous array
		int writeCount = revCurve.length; // write all-but-first
		System.arraycopy(revCurve, readIndex, returnCurve, writeIndex, writeCount);
			
		return returnCurve;
	}
	
	
	// simply return a reversed copy of the source array
	public Coordinate[] reverse( Coordinate[] source){
		Coordinate[] reverse = new Coordinate[ source.length ];
		
		int readIndex = 0;
		int writeIndex = source.length-1;
		while( readIndex < source.length ){
			reverse[writeIndex] = source[readIndex];
			++readIndex;
			--writeIndex;
		}	
		return reverse;
	}

	/*
	 * Return an approximation of the longitudinal unitary inertia of the fin set.
	 * The process is the following:
	 * 
	 * 1. Approximate the fin with a rectangular fin
	 * 
	 * 2. The inertia of one fin is taken as the average of the moments of inertia
	 *    through its center perpendicular to the plane, and the inertia through
	 *    its center parallel to the plane
	 *    
	 * 3. If there are multiple fins, the inertia is shifted to the center of the fin
	 *    set and multiplied by the number of fins.
	 */
	@Override
	public double getLongitudinalUnitInertia() {
		double area = getFinWettedArea();
		if (MathUtil.equals(area, 0))
			return 0;
		
		// Approximate fin with a rectangular fin
		// w2 and h2 are squares of the fin width and height
		double w = getLength();
		double h = getSpan();
		double w2, h2;
		
		if (MathUtil.equals(w * h, 0)) {
			w2 = area;
			h2 = area;
		} else {
			w2 = w * area / h;
			h2 = h * area / w;
		}
		
		double inertia = (h2 + 2 * w2) / 24;
		
		if (finCount == 1)
			return inertia;
		
		double radius = getBodyRadius(0);
		
		return finCount * (inertia + MathUtil.pow2(MathUtil.safeSqrt(h2) + radius));
	}
	
	
	/*
	 * Return an approximation of the rotational unitary inertia of the fin set.
	 * The process is the following:
	 * 
	 * 1. Approximate the fin with a rectangular fin and calculate the inertia of the
	 *    rectangular approximate
	 *    
	 * 2. If there are multiple fins, shift the inertia center to the fin set center
	 *    and multiply with the number of fins.
	 */
	@Override
	public double getRotationalUnitInertia() {
		double area = getFinWettedArea();
		if (MathUtil.equals(area, 0))
			return 0;
		
		// Approximate fin with a rectangular fin
		double w = getLength();
		double h = getSpan();
		
		if (MathUtil.equals(w * h, 0)) {
			h = MathUtil.safeSqrt(area);
		} else {
			h = MathUtil.safeSqrt(h * area / w);
		}
		
		if (finCount == 1)
			return h * h / 12;
		
		double radius = getBodyRadius(0);
		
		return finCount * (h * h / 12 + MathUtil.pow2(h / 2 + radius));
	}
	
	
	
	/**
	 * Adds bounding coordinates to the given set.  The body tube will fit within the
	 * convex hull of the points.
	 *
	 * Currently the points are simply a rectangular box around the body tube.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<Coordinate>(8);
		
		// should simply return this component's bounds in this component's body frame.
		
		double x_min = Double.MAX_VALUE;
		double x_max = Double.MIN_VALUE;
		double r_max = 0.0;
		
		for (Coordinate point : getFinPoints()) {
			double hypot = MathUtil.hypot(point.y, point.z);
			double x_cur = point.x;
			if (x_min > x_cur) {
				x_min = x_cur;
			}
			if (x_max < x_cur) {
				x_max = x_cur;
			}
			if (r_max < hypot) {
				r_max = hypot;
			}
		}
		
		Coordinate location = this.getLocations()[0];
		x_max += location.x;
		
		if( parent instanceof SymmetricComponent){
			r_max += ((SymmetricComponent)parent).getRadius(0);
		}
		
		addBoundingBox(bounds, x_min, x_max, r_max);
		return bounds;
	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isAerodynamicChange()
			|| e.isMassChange()) {
			this.wettedArea.reset( Double.NaN);
			this.volume = Double.NaN;
			this.mass.reset(Double.NaN);
			cantRotation = null;
		}
		super.componentChanged(e);
	}
	
	
	/**
	 * Return the radius of the BodyComponent the fin set is situated on.  Currently
	 * only supports SymmetricComponents and returns the radius at xOffset + the 
	 * starting point of the root chord
	 *  
	 * @return  radius of the underlying BodyComponent or 0 if none exists.
	 */
	public double getBodyRadius( final double xOffset) {
		RocketComponent s;
		
		s = this.getParent();
		while (s != null) {
			if (s instanceof SymmetricComponent) {
				double xFinFront = getTop();
				double at_x = xFinFront + xOffset;
				return ((SymmetricComponent) s).getRadius( at_x );
			}
			s = s.getParent();
		}
		return 0;
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	/**
	 * Allows nothing to be attached to a FinSet.
	 * 
	 * @return <code>false</code>
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}

	/**
	 * Return a list of coordinates defining the geometry of a single fin.  
	 * The coordinates are the XY-coordinates of points defining the shape of a single fin,
	 * where the origin is the leading root edge.  Therefore, the first point must be (0,0,0).
	 * All Z-coordinates must be zero.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public abstract Coordinate[] getFinPoints();
	
	
	public Coordinate[] getFinPoints_fromFin(){
		return getFinPoints();
	}
	
	public boolean isTabTrivial(){
		return ( FinSet.minimumTabArea > (getTabLength()*getTabHeight()));
	}
		
	public boolean isRootStraight( ){
        if( getParent() instanceof Transition){
            if( ((Transition)getParent()).getType() == Transition.Shape.CONICAL ){
                return true;
            }else{
                return false;
            }
        }
        
        // by default, assume a flat base
        return true;
    }
	
	final public static Coordinate[] translatePoints( final Coordinate[] inp, final double x_delta , final double y_delta){
		Coordinate[] returnPoints = new Coordinate[inp.length];
		for( int index=0; index < inp.length; ++index){
			final double new_x = inp[index].x + x_delta;
			final double new_y = inp[index].y + y_delta;
			returnPoints[index] = new Coordinate(new_x, new_y);
		}
		return returnPoints;
	}
	
	
	/**
	 * Return a list of X,Y coordinates defining the geometry of a single fin tab. 
	 * The origin is the leading root edge, and the tab height (or 'depth') is 
	 * the radial distance inwards from the reference point, depending on positioning method: 
	 *      if via TOP:    tab front edge
	 *      if via MIDDLE: tab middle
	 *      if via BOTTOM: tab trailing edge 
	 *
	 * The tab coordinates will generally have negative y values.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public Coordinate[] getTabPoints() {
		
		if (MathUtil.equals(getTabHeight(), 0) ||
				MathUtil.equals(getTabLength(), 0)){
			return new Coordinate[]{};
		}
	
		final int pointCount = 4;
		Coordinate[] points = new Coordinate[pointCount];
		final Coordinate finFront = this.getFinFront();
		
		final SymmetricComponent body = (SymmetricComponent)this.getParent();
		
		final double xTabFront = getTabFrontEdge();
		final double xTabTrail = getTabTrailingEdge();
		
		final double xTabReference = finFront.x + getTabReferenceOffset();
		
		double yTabFront = 0;
		double yTabTrail = 0;
		double yTabBottom = -tabHeight;
		if( null != body ){
			yTabFront = body.getRadius( finFront.x + xTabFront ) - finFront.y;
			yTabTrail = body.getRadius( finFront.x + xTabTrail ) - finFront.y;
			yTabBottom = body.getRadius( xTabReference ) - tabHeight - finFront.y;
		}
		
		points[0] = new Coordinate(xTabFront, yTabFront);
		points[1] = new Coordinate(xTabFront, yTabBottom );
		points[2] = new Coordinate(xTabTrail, yTabBottom );
		points[3] = new Coordinate(xTabTrail, yTabTrail);

	    return points;
	}

	public Coordinate getFinFront() {
		final double xFinFront = asPositionValue(Position.TOP);
		final SymmetricComponent symmetricParent = (SymmetricComponent)this.getParent();
		if( null == symmetricParent){
			return new Coordinate( 0, 0);
		}else{
			final double yFinFront = symmetricParent.getRadius( xFinFront );
			return new Coordinate(xFinFront, yFinFront);
		}
	}


	/* 
	 * yes, this may over-count points between the fin and fin tabs, 
	 * but the minor performance hit is not worth the code complexity of dealing with.
	 */
	public Coordinate[] getFinPointsWithTab() {
		final Coordinate[] finPoints = getFinPoints();
		final Coordinate[] tabPoints = getTabPoints();
		
		Coordinate[] combinedPoints = Arrays.copyOf(finPoints, finPoints.length + tabPoints.length);
		System.arraycopy(tabPoints, 0, combinedPoints, finPoints.length, tabPoints.length);
		return combinedPoints;
	}
	

	/**
	 * Get the span of a single fin.  That is, the length from the root to the tip of the fin.
	 * @return  Span of a single fin.
	 */
	public abstract double getSpan();
	
	
	@Override
	protected List<RocketComponent> copyFrom(RocketComponent c) {
		FinSet src = (FinSet) c;
		this.finCount = src.finCount;
		this.finRotation = src.finRotation;
		this.rotation = src.rotation;
		this.baseRotation = src.baseRotation;
		this.cantAngle = src.cantAngle;
		this.cantRotation = src.cantRotation;
		this.thickness = src.thickness;
		this.crossSection = src.crossSection;
		this.tabHeight = src.tabHeight;
		this.tabLength = src.tabLength;
		this.tabRelativePosition = src.tabRelativePosition;
		this.tabShift = src.tabShift;
		
		return super.copyFrom(c);
	}
	
	/*
	 * Handle fin fillet mass properties	
	 */
	public Material getFilletMaterial() {
		return filletMaterial;
	}
	
	public void setFilletMaterial(Material mat) {
		if (mat.getType() != Material.Type.BULK) {
			throw new IllegalArgumentException("ExternalComponent requires a bulk material" +
					" type=" + mat.getType());
		}
		
		if (filletMaterial.equals(mat))
			return;
		filletMaterial = mat;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public double getFilletRadius() {
		return filletRadius;
	}
	
	public void setFilletRadius(double r) {
		if (MathUtil.equals(filletRadius, r))
			return;
		filletRadius = r;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	/**
	 * use this for calculating physical properties, and routine drawing
	 * 
	 * @return points representing the fin-root points, relative to ( x: fin-front, y: centerline ) i.e. relto: fin Component reference point
	 */
	public Coordinate[] getBodyPoints() {
		final double xFinStart = getTop();
		final double xFinEnd = xFinStart+getLength();
		final double xOffset = -xFinStart;
		final double yOffset = 0.0d;
		
		return translatePoints( getBodyPoints( xFinStart, xFinEnd), xOffset, yOffset);
	}

	/**
	 * used to get body points for the profile design view
	 * 
	 * @return points representing the fin-root points, relative to ( x: fin-front, y: fin-root-radius ) 
	 */
	public Coordinate[] getRootPoints(){
		final double xFinStart = getTop();
		final double xFinEnd = xFinStart+getLength();
		final double xOffset = -xFinStart;
		final double yOffset = -getBodyRadius(0.0);
		
		return translatePoints( getBodyPoints( xFinStart, xFinEnd), xOffset, yOffset);
	}

	public Coordinate[] getBodyPoints( final double xStart, final double xEnd ) {
		if( null == parent){
			return new Coordinate[]{};
		}
		
		// for a simple bodies, one increment is perfectly accurate.
		int divisionCount = 1;
		// cast-assert
		final SymmetricComponent body = (SymmetricComponent) getParent();

		// for anything more complicated, increase the count: 
		if( ( body instanceof Transition) && ( ((Transition)body).getType() != Shape.CONICAL )){
			
			// the maximum precision to enforce when calculating the areas of fins ( especially on curved parent bodies)
			final double calculationPrecision = 0.005;
			divisionCount = (int)Math.ceil(  (xEnd - xStart) / calculationPrecision);
			
			// When creating body curves, don't create more than this many divisions. -- only relevant on very large components
			final int maximumBodyDivisionCount = 100;
			divisionCount = Math.min( maximumBodyDivisionCount, divisionCount);
		}
		
		final double intervalLength = xEnd - xStart;
		double increment = (intervalLength)/divisionCount;
		
				
		double xCur = xStart;
		Coordinate[] points = new Coordinate[divisionCount+1];
		for( int index = 0; index < points.length; index++){
			double yCur = body.getRadius( xCur );
			points[index]=new Coordinate( xCur, yCur);
			
			xCur += increment;
		}
		return points;
	}

	// for debugging.  You can safely delete this method
	public static String getPointDescr( final Coordinate[] points, final String name, final String indent){
		StringBuilder buf = new StringBuilder(); 
		
		buf.append( (indent+"    >> "+name+": "+points.length+" points\n"));
		int index =0;
		for( Coordinate c : points ){
			buf.append( String.format( indent+"      ....[%2d] (%6.4g, %6.4g)\n", index, c.x, c.y));
			index++;
		}
		return buf.toString();
	}

	@Override
	public StringBuilder toDebugDetail(){
		StringBuilder buf = super.toDebugDetail();
		
		buf.append(String.format("    TabLength: %6.4f TabHeight: %6.4f @ %6.4f at %s\n", tabLength, tabHeight, tabShift, tabRelativePosition ));
		buf.append( getPointDescr( this.getFinPoints(), "FinPoints", ""));
		buf.append( getPointDescr( this.getTabPoints(), "Tab Points", ""));
		return buf;
	}
	
	
	public void updatePhysicalProperties(){
		this.wettedArea = calculateWettedAreaCentroid();
		
		PointWeight tabArea = calculateTabCentroid();

		PointWeight wettedVolume = wettedArea.copy().scaleWeight( thickness * crossSection.getRelativeVolume());
		PointWeight tabVolume = tabArea.copy().scaleWeight( thickness);
		PointWeight bulkVolume = wettedVolume.add( tabVolume );
		this.volume = bulkVolume.w;
		
		PointWeight finMaterialMass = bulkVolume.copy().scaleWeight( material.getDensity());
		
		PointWeight filletVolume = calculateFilletVolumeCentroid();
		PointWeight filletMass = filletVolume.copy().scaleWeight( filletMaterial.getDensity());

		this.mass = finMaterialMass.add( filletMass );
		
		// set y coordinate: rotate around parent, if single fin; otherwise multiple fins will average out to zero
		if (finCount == 1) {
			this.mass.y = baseRotation.transform( new Coordinate( mass.x, mass.y, 0, 0)).y;
		} else {
			this.mass.y = 0;
		}
		
	}
	
	// ============= Instanceable Interface Methods ===============

	@Override
	public Coordinate[] getInstanceOffsets(){
		checkState();
		
		final double xOffset = getTop();
		final double radius = getBodyRadius(0.0f);
		final Transformation localCantRotation = getCantRotation();
		
		Coordinate[] toReturn = new Coordinate[this.finCount];
		Coordinate prevFin = baseRotation.transform( localCantRotation.transform( new Coordinate( 0, radius, 0, 0)));
		toReturn[0] = prevFin.setX( xOffset);
		
		for (int instanceNumber = 1; instanceNumber < this.finCount; instanceNumber++) {
			Coordinate curFin = finRotation.transform( prevFin ).setX(xOffset);
			
			toReturn[instanceNumber] = curFin;
			
			prevFin = curFin;
	 	}
	 		
		return toReturn;
	}

	@Override
	public void setInstanceCount( final int newCount ){
		setFinCount( newCount);
	}
	
	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-fin-ring");
	}
	
	
	// ============= RingInstanceable Interface Methods ===============


	@Override
	public double getAngularOffset(){
		return this.rotation;
	}

	@Override
	public double getRadialOffset(){
		return getBodyRadius(0.0f);
	}
	
	@Override
	public boolean getAutoRadialOffset(){
		return true;
	}
	
	@Override
	public void setAngularOffset(final double angle){
		this.setBaseRotation( angle);
	}
	
	@Override
	public void setRadialOffset(final double radius){
		// no-op.  This is value is purely automatic.
	}
	
}
