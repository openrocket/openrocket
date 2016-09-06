package net.sf.openrocket.rocketcomponent;

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
import net.sf.openrocket.util.Mass;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;


public abstract class FinSet extends ExternalComponent {
	private static final Translator trans = Application.getTranslator();
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
	private static final double minimumTabArea = 0.001;
	private double tabHeight = 0;
	private double tabLength = 0.05;
	private double tabShift = 0;
	private Position tabRelativePosition = Position.TOP;
	
	/*
	 * Fin fillet properties
	 */
	protected Material filletMaterial = null;
	protected double filletRadius = 0;
	protected double filletCenterY = 0;
	
	public boolean debug;
	// wetted area 
	private double wettedArea=Double.NaN;
	// these coordinates are weighted by AREA: simple component-total area (incl. tabs)
	private Mass centroid = new Mass();
	// cache some sort of measure of restoring moment? especially for fins? 
	private double restoringMoment=Double.NaN;
	
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
	 * Set the height from the fin's base at the point where the tab is located from -- if the tab is located via Position.BOTTOM, then the back edge will be 
	 * <code>height</code> deep, and the bottom edge of the tab will be parallel to the stage centerline.
	 *  
	 * @param height how deep the fin tab should project from the fin root.
	 * 
	 */
	public void setTabHeight(double height) {
		height = MathUtil.max(height, 0);
		if (MathUtil.equals(this.tabHeight, height))
			return;
		this.tabHeight = height;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getTabLength() {
		return tabLength;
	}
	
	public void setTabLength(double length) {
		length = MathUtil.max(length, 0);
		if (MathUtil.equals(this.tabLength, length))
			return;
		this.tabLength = length;
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
	
	
	///////////  Calculation methods  //////////
	/**
	 * Return the area of a *single* fin exposed to the airflow (i.e. external area) 
	 * N.B. counts only one side of each fin,  
	 * https://en.wikipedia.org/wiki/Wetted_area
	 * 
	 * @return returns the one-sided air-exposed area of a single fin  
	 */
	public double getFinWettedArea() {
		if( Double.isNaN( this.wettedArea)){
			updatePhysicalProperties();
		}
		return wettedArea;
	}
	
	public double getFinTotalArea() {
		if( this.centroid.isNaN() ){
			updatePhysicalProperties();
		}
		return centroid.w;
	}
	
	@Override
	public double getComponentMass() {
		return getComponentVolume()*material.getDensity();
	}
	
//	public double getSingleFinMass() {
//		return getComponentVolume() * material.getDensity();
//	}
	
	public double getFilletMass() {
		return getFilletVolume() * filletMaterial.getDensity();
	}
	
	@Override
	public double getComponentVolume() {
		if( this.centroid.isNaN() ){
			this.updatePhysicalProperties();
		}
		// this is for the fins alone, fillets are taken care of separately.
		return finCount * (this.centroid.w) * thickness *
				crossSection.getRelativeVolume();
	}
	
	/**
	 * Return the unweighted CG of a single fin.  The X-coordinate is relative to
	 * the root chord leading edge and the Y-coordinate to the fin root chord.
	 * 
	 * @return  the unweighted CG coordinate of a single fin. 
	 */	
	@Override
	public Coordinate getComponentCG() {
		if( this.centroid.isNaN() ){
			//?? not sure how to organize these..sure.
			//calculateWettedAread
			updatePhysicalProperties();
		}
		
		double mass = getComponentMass();
		
		//Coordinate[] rootPoints = getRootPoints();
		// TODO: this are probably inaccurate on anything but <code>BodyTube</code> fin bases.  rework to calculate the CoM of a line
		// >> piecewise segment average (?)
		double filletMass = getFilletMass();
		double filletCenter = length / 2;
		
		// TODO: this are probably inaccurate on anything but <code>BodyTube</code> fin bases.  rework.
		double newFinCMx = (filletCenter * filletMass + this.centroid.x * mass) / (filletMass + mass);
		
		// FilletRadius/5 is a good estimate for where the vertical centroid of the fillet
		// is.  Finding the actual position is very involved and won't make a huge difference.
		double newFinCMy = (filletRadius / 5 * filletMass + this.centroid.y * mass) / (filletMass + mass);
		
		if (finCount == 1) {
			return baseRotation.transform(
					new Coordinate(newFinCMx, newFinCMy + getBodyRadius(), 0, (filletMass + mass)));
		} else {
			return new Coordinate(newFinCMx, 0, 0, (filletMass + mass));
		}
	}
	
	public double getFilletVolume() {
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
		 * 
		 */
		double btRadius = 1000.0; // assume a really big body tube if we can't get the radius,
		RocketComponent c = this.getParent();
		if (BodyTube.class.isInstance(c)) {
			btRadius = ((BodyTube) c).getOuterRadius();
		}
		double totalRad = filletRadius + btRadius;
		double innerAngle = Math.asin(filletRadius / totalRad);
		double outerAngle = Math.acos(filletRadius / totalRad);
		
		double outerArea = Math.tan(outerAngle) * filletRadius * filletRadius / 2;
		double filletVolume = length * (outerArea
				- outerAngle * filletRadius * filletRadius / 2
				- innerAngle * btRadius * btRadius / 2);
		return 2 * filletVolume;
	}
	
	public void updatePhysicalProperties(){
		Mass wettedCentroid = calculateWettedAreaCentroid();
		Mass tabCentroid = calculateTabCentroid();
		
		this.wettedArea = wettedCentroid.w;

		this.centroid = wettedCentroid.add( tabCentroid);
//		if(debug){
//			System.err.println(String.format( "    >> wetted: (%6.4g, %6.4g, %6.4g // %6.4g )", wettedCentroid.x, wettedCentroid.y, wettedCentroid.z, wettedCentroid.w ));
//			System.err.println(String.format( "    >> tab: (%6.4g, %6.4g, %6.4g // %6.4g )", tabCentroid.x, tabCentroid.y, tabCentroid.z, tabCentroid.w ));
//			System.err.println(String.format( "    << finTotal: (%6.4g, %6.4g, %6.4g // %6.4g )", centroid.x, centroid.y, centroid.z, centroid.w ));
//			System.err.println( "");
//		}
		
		// this.restoringMoment
 	}
	
	/**
	 * \brief  calculate the area between the points and the x-axis, and returns the centroid + area. 
	 *  
	 * @param points define a piece-wise line bounding the area.    
	 * @return  centroid of the area, additionaly the area is stored as the weight
	 */
	public static Mass calculateCurveIntegral( Coordinate[] points ){
		Mass centroidSum = new Mass(0);
		
		Coordinate prev= points[0];
		for( int index = 1; index < points.length; index++){
			Coordinate cur = points[index];
			final double delta_x = (cur.x - prev.x);
			final double y_avg = (cur.y + prev.y)/2;
			
			// calculate marginal area
			double area_increment = delta_x*y_avg;
					
			// calculate centroid increment 
			double common = (0.333333333/(cur.y+prev.y));
			double x_ctr = common*(prev.x*(2*prev.y+cur.y) + cur.x*(2*cur.y+prev.y));
			double y_ctr =  common*( cur.y*prev.y + Math.pow( cur.y, 2) + Math.pow( prev.y, 2));
			
			Mass centroid_increment = new Mass( x_ctr, y_ctr, 0, area_increment);
			centroidSum = centroidSum.add( centroid_increment );
			
//			{
//				System.err.println(String.format("       for increment: from:[%2d](%4.2g, %4.2g)   to:[%2d](%4.2g, %4.2g)", index-1, prev.x, prev.y, index, cur.x, cur.y));
//				System.err.println(String.format("  	    	    delta_x= %6.4g  y_avg=%6.4g  A_incr=%6.4g     x= %6.4g y=%6.4g, A=%6.4g", 
//											     delta_x, y_avg, area_increment, centroid_increment.x, centroid_increment.y, centroid_increment.w));
//				System.err.println(String.format( "                 sum = (%6.4g, %6.4g, %6.4g // %6.4g )", centroidSum.x, centroidSum.y, centroidSum.z, centroidSum.w ));
//			}
			
			prev=cur;
		}
		
		return centroidSum;
	}
	
	public static Mass calculateCurveCoM( Coordinate[] points){
		// NYI! 
		
		//	Coordinate[] rootPoints = getRootPoints();
			// TODO: this are probably inaccurate on anything but <code>BodyTube</code> fin bases.  rework to calculate the CoM of a line
			// >> piecewise segment average (?)
	
		return null;
	}
			
	
	/* 
	 * The coordinate contains an x,y coordinate of the centroid, and the raw alread is stored in the weight field.
	 */
	
	private Mass calculateWettedAreaCentroid(){
		if(( null == getParent()) || ( getParent() instanceof BodyTube )){
			// optimization: this is the quickest for most common fins: 
			return calculateCurveIntegral( getFinPoints_fromFin() );
		}else {
			final double yFinFront = getBodyRadius();
			
			final Coordinate[] finPoints = translatePoints( getFinPoints(), 0.0, yFinFront );
			Coordinate[] upperCurve = finPoints;
			final Mass upperCentroid = calculateCurveIntegral( upperCurve );

			Coordinate[] bodyCurve = getBodyPoints();
			Coordinate[] lowerCurve = bodyCurve;
			final Mass lowerCentroid = calculateCurveIntegral( lowerCurve );
			
			Mass totalCentroid = upperCentroid.subtract( lowerCentroid );
			
			// move centroid to be relative to fin start.
			totalCentroid = totalCentroid.move( 0.0, yFinFront, 0);
			
			return totalCentroid;
		}
	}
	
	private Mass calculateTabCentroid(){
		RocketComponent comp = getParent();
		if( (null == comp) 
				|| (!( comp instanceof SymmetricComponent))
				|| isTabTrivial() ){

			// if null or invalid type:
			return new Mass(0);
		}

		// relto: fin
		final double xTabFront_fin = getTabFrontEdge();
		final double xTabBack_fin = getTabTrailingEdge();
		
		final double xFinFront_body = getTop();
		final double yFinFront = getBodyRadius();
		final double xTabFront_body = xFinFront_body + xTabFront_fin;
		final double xTabBack_body = xFinFront_body + xTabBack_fin;
				
		// always returns x coordinates relTo fin front:
		Coordinate[] upperCurve = getBodyPoints( xTabFront_body, xTabBack_body );
		// subtract off tab location
		upperCurve = translatePoints( upperCurve, -xFinFront_body, 0.0);
		
		double rTabInner = getTabReferenceRadius() - tabHeight; 
		Coordinate[] lowerCurve = new Coordinate[]{ new Coordinate( xTabFront_fin, rTabInner),
													new Coordinate( xTabBack_fin, rTabInner)};

		Mass upperCentroid = calculateCurveIntegral( upperCurve );
		Mass lowerCentroid = calculateCurveIntegral( lowerCurve );
		
		Mass tabCentroid = upperCentroid.subtract( lowerCentroid );

		// move centroid to be relative to fin start.
		tabCentroid = tabCentroid.move( 0.0, yFinFront, 0);
		return tabCentroid;
	}	
	
	private double getTabReferenceRadius() {
		double xTabOffset = Position.getTop( tabShift, tabRelativePosition, length, tabLength );
		double xTabReference = getTop() + xTabOffset; 
		return ((SymmetricComponent) getParent()).getRadius( xTabReference ); 
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
		
		double radius = getBodyRadius();
		
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
		
		double radius = getBodyRadius();
		
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
			this.wettedArea=Double.NaN;
			this.centroid.reset(Double.NaN);
			this.restoringMoment = Double.NaN;
			cantRotation = null;
		}
		super.componentChanged(e);
	}
	
	
	/**
	 * Return the radius of the BodyComponent the fin set is situated on.  Currently
	 * only supports SymmetricComponents and returns the radius at the starting point of the
	 * root chord.
	 *  
	 * @return  radius of the underlying BodyComponent or 0 if none exists.
	 */
	public double getBodyRadius() {
		RocketComponent s;
		
		s = this.getParent();
		while (s != null) {
			if (s instanceof SymmetricComponent) {
				double xFinFront = getTop();
				return ((SymmetricComponent) s).getRadius(xFinFront);
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
	 * Return a list of coordinates defining the geometry of a single fin tab. 
	 * The coordinates are the XY-coordinates of points defining the 
	 * shape of a single fin, where the origin is the leading root edge, and the height
	 * (aka 'depth') is the radial distance inwards from the leading root edge.  
	 * 
	 * 
	 * The tab coordinates will have a negative y value.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public Coordinate[] getTabPoints() {
		
		if (MathUtil.equals(getTabHeight(), 0) ||
				MathUtil.equals(getTabLength(), 0))
			return new Coordinate[0];
		
		final int pointCount = 4;
		Coordinate[] points = new Coordinate[pointCount];
		
		final SymmetricComponent symmetricParent = (SymmetricComponent)this.getParent();
		final double xFinFront = asPositionValue(Position.TOP);
		final double yFinFront = symmetricParent.getRadius( xFinFront );
		
		final double xTabFront = getTabFrontEdge();
		final double yTabFront = symmetricParent.getRadius( xFinFront + xTabFront ) - yFinFront;
		final double xTabTrail = getTabTrailingEdge();
		final double yTabTrail = symmetricParent.getRadius( xFinFront + xTabTrail ) - yFinFront;
		final double yTabBottom = -getTabHeight();
		
		points[0] = new Coordinate(xTabFront, yTabFront);
		points[1] = new Coordinate(xTabFront, yTabBottom );
		points[2] = new Coordinate(xTabTrail, yTabBottom );
		points[3] = new Coordinate(xTabTrail, yTabTrail);
		
		return points;
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
		final double yOffset = -getBodyRadius();
		
		return translatePoints( getBodyPoints( xFinStart, xFinEnd), xOffset, yOffset);
	}

	public Coordinate[] getBodyPoints( final double xStart, final double xEnd ) {
		if( null == parent){
			return null;
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

	// for debugging.  You can safely delete this method (+ overload)
	public static String getPointDescr( final Coordinate[] points, final String name){
		return getPointDescr( points, name, "");
	}
	public static String getPointDescr( final Coordinate[] points, final String name, final String indent){
		StringBuilder buf = new StringBuilder(); 
		
		buf.append( (indent+"    >> "+name+": length: "+points.length+"\n"));
		int index =0;
		for( Coordinate c : points ){
			buf.append( String.format( indent+"      ....[%2d] (%6.4g, %6.4g)\n", index, c.x, c.y));
			index++;
		}
		return buf.toString();
	}

}
