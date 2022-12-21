package net.sf.openrocket.rocketcomponent;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.ArrayList;


import net.sf.openrocket.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;

import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.AxialPositionable;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.startup.Application;

import net.sf.openrocket.rocketcomponent.Transition.Shape;

public abstract class FinSet extends ExternalComponent implements AxialPositionable, BoxBounded, RingInstanceable, InsideColorComponent {
	private static final Logger log = LoggerFactory.getLogger(FinSet.class);
	private static final Translator trans = Application.getTranslator();

	/**
	 * Maximum allowed cant of fins.
	 */
	public static final double MAX_CANT_RADIANS = (15.0 * Math.PI / 180);

	/**
	 * Maximum number of root points in the root geometry.
	 */
	private static final int MAX_ROOT_DIVISIONS = 100;
	private static final int MAX_ROOT_DIVISIONS_LOW_RES = MAX_ROOT_DIVISIONS / 5;

    public void setOverrideMass() {
    }

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
	private int finCount = 3;

	/**
	 * Rotation about the x-axis by 2*PI/fins.
	 */
	private Transformation finRotationIncrement = Transformation.IDENTITY;


	/**
	 * Rotation angle of the first fin.  Zero corresponds to the positive y-axis.
	 */
	private AngleMethod angleMethod = AngleMethod.RELATIVE;
	private double firstFinOffsetRadians = 0;
	private Transformation baseRotation = Transformation.IDENTITY;  // initially, rotate by 0 degrees.

	/**
	 * Cant angle of fins.
	 */
	private double cantRadians = 0;
	
	/* Cached value: */
	private Transformation cantRotation = null;
	
	// fixed to body surface...
	final private RadiusMethod radiusMethod = RadiusMethod.SURFACE;
		
	/**
	 * Thickness of the fins.
	 */
	protected double thickness = 0.003;

	/**
	 * The cross-section shape of the fins.
	 */
	private CrossSection crossSection = CrossSection.SQUARE;
	
	
	/*
	 * Fin tab properties.
	 */
	private static final double minimumTabArea = 1e-8;
	private double tabHeight = 0;
	private double tabLength = 0.05;
	// this is always measured from the root-lead point.
	private double tabPosition = 0.0;
	private AxialMethod tabOffsetMethod = AxialMethod.MIDDLE;
	private double tabOffset = 0.;

	/*
	 * Fin fillet properties
	 */
	private Material filletMaterial;
	private double filletRadius = 0;

	// ==== Cached Values ====
	// Fin area does not include fin tabs, CG does.

	// planform area of one side of a single fin 
	private double singlePlanformArea = Double.NaN;
	private double totalVolume = Double.NaN;
	private Coordinate centerOfMass = Coordinate.NaN;

	private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
	
	/**
	 * New FinSet with given number of fins and given base rotation angle.
	 * Sets the component relative position to POSITION_RELATIVE_BOTTOM,
	 * i.e. fins are positioned at the bottom of the parent component.
	 */
	public FinSet() {
		super( AxialMethod.BOTTOM);
		this.filletMaterial = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
		super.displayOrder_side = 4;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 4;		// Order for displaying the component in the 2D back view
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setFinCount(n);
			}
		}

		if (finCount == n)
			return;
		if (n < 1)
			n = 1;
		if (n > 8)
			n = 8;
		finCount = n;

		finRotationIncrement = Transformation.rotate_x(2 * Math.PI / finCount);

		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public Transformation getFinRotationTransformation() {
		return finRotationIncrement;
	}

	@Override
	public double getBoundingRadius(){
		return 0.;
	}

	/**
	 * Gets the base rotation amount of the first fin.
	 * @return The base rotation amount.
	 */
	public double getBaseRotation() {
		return getAngleOffset();
	}
	
	/**
	 * Sets the base rotation amount of the first fin.
	 * @param r The base rotation in radians
	 */
	public void setBaseRotation(double r) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setBaseRotation(r);
			}
		}

		setAngleOffset(r);
	}
	
	/**
	 * @return angle current cant angle, in radians 
	 */
	public double getCantAngle() {
		return cantRadians;
	}
	
	/**
	 * 
	 * @param newCantRadians -- new cant angle, in radians
	 */
	public void setCantAngle(final double newCantRadians) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setCantAngle(newCantRadians);
			}
		}

		final double clampedCant = MathUtil.clamp(newCantRadians, -MAX_CANT_RADIANS, MAX_CANT_RADIANS);
		if (MathUtil.equals(clampedCant, this.cantRadians))
			return;
		this.cantRadians = clampedCant;

		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}

	public Transformation getCantRotation() {
		if( null == cantRotation ) {
			if (MathUtil.equals(this.cantRadians, 0)) {
				cantRotation = Transformation.IDENTITY;
			} else {
				cantRotation = Transformation.rotate_y(cantRadians);
			}
		}
		return cantRotation;
	}
	
	public double getThickness() {
		return thickness;
	}
	
	public void setThickness(double r) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setThickness(r);
			}
		}

		if (thickness == r)
			return;
		thickness = Math.max(r, 0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public CrossSection getCrossSection() {
		return crossSection;
	}
	
	public void setCrossSection(CrossSection cs) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setCrossSection(cs);
			}
		}

		if (crossSection == cs)
			return;
		crossSection = cs;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public double getTabHeight() {
		return tabHeight;
	}
	
	/**
	 * Set the height from the fin's base at the reference point --
	 * i.e. where the tab is located from.  If the tab is located via
	 * BOTTOM, then the back edge will be  <code>height</code> deep,
	 * and the bottom edge of the tab will be parallel to the stage
	 * centerline.  If the tab is located via TOP, the the front edge
	 * will have corresponding height/depth.
	 * If the tab is located via MIDDLE, the tab's midpoint is used.
	 * 
	 * Note this function also does bounds checking, and will not set
	 * a tab height that passes through it's parent's midpoint.
	 *  
	 * @param newTabHeight how deep the fin tab should project
	 * from the fin root, at the reference point
	 * 
	 */
	public void setTabHeight(final double newTabHeight) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setTabHeight(newTabHeight);
			}
		}

		if (MathUtil.equals(this.tabHeight, MathUtil.max(newTabHeight, 0))){
			return;
		}
		
		tabHeight = newTabHeight;
		double maxTabHeight = getMaxTabHeight();
		this.tabHeight = Math.min(this.tabHeight,  maxTabHeight);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getTabLength() {
		return tabLength;
	}

	/**
	 * set tab length
	 */
	public void setTabLength(final double lengthRequest) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setTabLength(lengthRequest);
			}
		}

		if (MathUtil.equals(tabLength, MathUtil.max(lengthRequest, 0))) {
			return;
		}
		
		tabLength = lengthRequest;
		
		updateTabPosition();
		
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	public void updateTabPosition(){
		this.tabPosition = this.tabOffsetMethod.getAsPosition(tabOffset, tabLength, length);
	}
	
	/** 
	 * internally, set the internal offset and optionally validate tab
	 * 
	 * @param offsetRequest new requested tab offset
	 */
	public void setTabOffset( final double offsetRequest) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setTabOffset(offsetRequest);
			}
		}

		tabOffset = offsetRequest;
		updateTabPosition();
		
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public AxialMethod getTabOffsetMethod() {
		return tabOffsetMethod;
	}

	/**
	 * the tab's positioning method variable does not change the internal representation --
	 * it is merely a lens through which other modules may view the tab's position.
     */
	public void setTabOffsetMethod(final AxialMethod newPositionMethod) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setTabOffsetMethod(newPositionMethod);
			}
		}

		this.tabOffsetMethod = newPositionMethod;
		this.tabOffset = this.tabOffsetMethod.getAsOffset(tabPosition, tabLength, length);
		
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	/**
	 * Return the tab front edge position from the front of the fin.
	 */
	public double getTabFrontEdge() {
		return tabPosition;
	}

	public double getTabOffset(AxialMethod method){
		return method.getAsOffset(tabPosition, tabLength, length);
	}

	public double getTabOffset(){
		return getTabOffset(this.tabOffsetMethod);
	}

	/**
	 * Return the tab trailing edge position *from the front of the fin*.
	 */
	public double getTabTrailingEdge() {
		return tabPosition + tabLength;
	}
	
	public void validateFinTabPosition() {
		//check front bounds:
		if (tabPosition < 0) {
			this.tabPosition = 0;
		}

		//check tail bounds:
		if (this.length < tabPosition) {
			this.tabPosition = length;
		}
	}
	
	public void validateFinTabLength() {
		//System.err.println(String.format("    >> Fin Tab Length: %.6f @ %.6f", tabLength, tabOffset));
		
		final double xTabBack = getTabTrailingEdge();
		if (this.length < xTabBack) {
			this.tabLength -= (xTabBack - this.length);
		}
		
		tabLength = Math.max(0, tabLength);
		
		//System.err.println(String.format("    << Fin Tab Length: %.6f @ %.6f", tabLength, tabOffset));
	}

	/**
	 * Calculates the maximum height that the fin tabs can be, depending on the parent shape.
	 * @return maximum tab height value
	 */
	public double getMaxTabHeight() {
		Double radiusFront = getParentFrontRadius();
		Double radiusTrailing = getParentTrailingRadius();
		if (radiusFront != null && radiusTrailing != null) {
			return MathUtil.min(radiusFront, radiusTrailing);
		}
		return Double.MAX_VALUE;
	}

	/**
	 * Returns the radius of the parent at the front of the fin, or null if no parent is present.
	 * @param parent the fin's parent component
	 */
	public Double getParentFrontRadius(RocketComponent parent) {
		if (parent instanceof SymmetricComponent) {
			final Coordinate finFront = this.getFinFront();

			// pulls the parent-body radius at the fin-tab reference point.
			final double xLead = this.getTabFrontEdge();

			final SymmetricComponent sym = (SymmetricComponent) parent;
			return sym.getRadius(finFront.x + xLead);
		}
		return null;
	}

	/**
	 * Returns the radius of the parent at the front of the fin, or null if no parent is present.
	 */
	public Double getParentFrontRadius() {
		return getParentFrontRadius(getParent());
	}

	/**
	 * Returns the radius of the parent at the trailing edge of the fin, or null if no parent is present.
	 * @param parent the fin's parent component
	 */
	public Double getParentTrailingRadius(RocketComponent parent) {
		if (parent instanceof SymmetricComponent) {
			final Coordinate finFront = this.getFinFront();

			// pulls the parent-body radius at the fin-tab reference point.
			final double xTrail = this.getTabTrailingEdge();

			final SymmetricComponent sym = (SymmetricComponent) parent;
			return sym.getRadius(finFront.x + xTrail);
		}
		return null;
	}

	/**
	 * Returns the radius of the parent at the trailing edge of the fin, or null if no parent is present.
	 */
	public Double getParentTrailingRadius() {
		return getParentTrailingRadius(getParent());
	}
	
	///////////  Calculation methods  //////////
	/**
	 * Return the area of a *single* fin exposed to the airflow (i.e. external area) 
	 * N.B. counts only one side of each fin,  
	 * https://en.wikipedia.org/wiki/Wetted_area
	 * 
	 * @return returns the one-sided air-exposed area of a single fin  
	 */
	public double getPlanformArea() {
		if( Double.isNaN(singlePlanformArea) ){
			calculateCM();
		}
		return this.singlePlanformArea;
	}
	
	@Override
	public double getComponentMass() {
		if(this.centerOfMass.isNaN()){
			calculateCM();
		}
		return this.centerOfMass.weight;
	}
	
	@Override
	public double getComponentVolume() {
        if(Double.isNaN(this.totalVolume)){
			calculateCM();
        }

        return totalVolume;
	}
	
	/**
	 * Return the center-of-mass of a single fin.  The X-coordinate is relative to
	 * the root chord leading edge and the Y-coordinate to the fin root chord.
	 * 
	 * @return  the Center-of-Mass coordinate of a single fin. 
	 */	
	@Override
	public Coordinate getComponentCG() {
		if( centerOfMass.isNaN() ){
			calculateCM();
		}
		
		return centerOfMass;
	}

	private static Coordinate calculateFilletCrossSection(final double filletRadius, final double bodyRadius){
		final double hypotenuse = filletRadius + bodyRadius;
		final double innerArcAngle = Math.asin(filletRadius / hypotenuse);
		final double outerArcAngle = Math.acos(filletRadius / hypotenuse);

		final double triangleArea = Math.tan(outerArcAngle) * filletRadius * filletRadius / 2;
		double crossSectionArea = (triangleArea
				- outerArcAngle * filletRadius * filletRadius / 2
				- innerArcAngle * bodyRadius * bodyRadius / 2);

		if(Double.isNaN(crossSectionArea)) {
			crossSectionArea = 0.;
		}else {
			// each fin has a fillet on each side
			crossSectionArea *= 2;
		}

		// heuristic, relTo the body center
		double yCentroid = bodyRadius + filletRadius /5;

		return new Coordinate(0,yCentroid,0,crossSectionArea);
	}

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
	protected Coordinate calculateFilletVolumeCentroid() {
		if((null == this.parent) || (!SymmetricComponent.class.isAssignableFrom(this.parent.getClass()))){
			return Coordinate.ZERO;
		}
		Coordinate[] mountPoints = this.getRootPoints();
//		if( null == mountPoints ){
//			return Coordinate.ZERO;
//		}

		final SymmetricComponent sym = (SymmetricComponent) this.parent;

		final Coordinate finLead = getFinFront();
		final double xFinEnd = finLead.x + getLength();
		final Coordinate[] rootPoints = getMountPoints( finLead.x, xFinEnd, -finLead.x, -finLead.y);
		if (0 == rootPoints.length) {
			return Coordinate.ZERO;
		}
		
		Coordinate filletVolumeCentroid = Coordinate.ZERO;

		Coordinate prev = mountPoints[0];
		for (int index = 1; index < mountPoints.length; index++) {
			final Coordinate cur = mountPoints[index];

			// cross section at mid-segment
			final double xAvg = (prev.x + cur.x) / 2;
			final double bodyRadius = sym.getRadius(xAvg);
			final Coordinate segmentCrossSection = calculateFilletCrossSection(this.filletRadius, bodyRadius).setX(xAvg);
			
//			final double xCentroid = xAvg;
//			final double yCentroid = segmentCrossSection.y; ///< heuristic, not exact
			final double segmentLength = Point2D.Double.distance(prev.x, prev.y, cur.x, cur.y);
			final double segmentVolume = segmentLength * segmentCrossSection.weight;

			final Coordinate segmentCentroid = segmentCrossSection.setWeight(segmentVolume);

			filletVolumeCentroid = filletVolumeCentroid.add(segmentCentroid);

			prev = cur;
		}

		if (finCount == 1) {
			Transformation rotation = Transformation.rotate_x( getAngleOffset());
			return rotation.transform(filletVolumeCentroid);
		}else{
			return filletVolumeCentroid.setY(0.);
		}
	}

    /**
	 * \brief  calculate the area-under-the-curve (i.e. the integral) in the form of a centroid + area 
	 *  
	 * @param points define a piece-wise line bounding the area.    
	 * @return  x,y,z => centroid of the area; weight => magnitude of the area
	 */
	protected static Coordinate calculateCurveIntegral( final Coordinate[] points ){
		Coordinate centroidSum = new Coordinate(0);

		if( 0 == points.length ){
			return centroidSum;
		}

		Coordinate prev= points[0];
		for( int index = 1; index < points.length; index++){
			Coordinate cur = points[index];

			// calculate marginal area
			final double delta_x = (cur.x - prev.x);
			final double y_avg = (cur.y + prev.y)*0.5;	// TODO: MEDIUM: what if one of the points is below the x-axis? (can produce negative area)
			double area_increment = delta_x*y_avg;
			if( MathUtil.equals( 0, area_increment)){
				prev = cur;
				// zero area increment: ignore and continue;
				continue;
			}

			// calculate centroid increment
			final double common = 1/(3*(cur.y+prev.y));
			final double x_ctr = common*(  prev.x*(2*prev.y+cur.y) + cur.x*(2*cur.y+prev.y));
			final double y_ctr = common*( cur.y*prev.y + Math.pow( cur.y, 2) + Math.pow( prev.y, 2));

			Coordinate centroid_increment = new Coordinate( x_ctr, y_ctr, 0, area_increment);
			centroidSum = centroidSum.average( centroid_increment );

            prev=cur;
		}

		// Negative weight => make positive. TODO: This is NOT a correct solution, but at least it won't throw an exception...
		if (centroidSum.weight < 0) {
			centroidSum = new Coordinate(centroidSum.x, -centroidSum.y, centroidSum.z, Math.abs(centroidSum.weight));
		}
		
		return centroidSum;
	}

	/**
	 *  calculates the planform area-centroid of a single fin's tab:
	 */
	private Coordinate calculateTabCentroid(){
		RocketComponent comp = getParent();

		if( !( comp instanceof SymmetricComponent) || isTabTrivial() ){
			// if null or invalid type:
			return Coordinate.ZERO;
		}
		// relto: fin
		final double xTabFront_fin = getTabFrontEdge();
		final double xTabTrail_fin = getTabTrailingEdge();

		final Coordinate finFront = getFinFront();
		final double xFinFront_body = finFront.x;
		final double xTabFront_body = xFinFront_body + xTabFront_fin;
		final double xTabTrail_body = xFinFront_body + xTabTrail_fin;
				
		// get body points, relTo fin front / centerline);
		final Coordinate[] upperCurve = getMountPoints( xTabFront_body, xTabTrail_body, -xFinFront_body, 0);
		final Coordinate[] lowerCurve = translateToCenterline( getTabPoints());
		final Coordinate[] tabPoints = combineCurves( upperCurve, lowerCurve);

		return calculateCurveIntegral( tabPoints );
	}
	
	private Coordinate[] translateToCenterline( final Coordinate[] fromRoot) {
		Coordinate finRoot = this.getFinFront();
		
		// locate relative to fin/body centerline
		return FinSet.translatePoints( fromRoot, 0.0d, finRoot.y);
	}

	/**
	 * The coordinate contains an x,y coordinate of the centroid, relative to the parent-body-centerline
	 * The weight contains the area of the fin.
	 *
	 * @return area centroid coordinates (weight is the area)
	 */
	private Coordinate calculateSinglePlanformCentroid(){
		final Coordinate finLead = getFinFront();
		final double xFinTrail = finLead.x+getLength();

		final Coordinate[] upperCurve = translatePoints(getFinPoints(), 0, finLead.y);
		final Coordinate[] lowerCurve = getMountPoints( finLead.x, xFinTrail, -finLead.x, 0);
		final Coordinate[] totalCurve = combineCurves( upperCurve, lowerCurve);

		final Coordinate planformCentroid = calculateCurveIntegral( totalCurve );

		// return as a position relative to fin-root
		return planformCentroid;
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
	private Coordinate[] combineCurves( final Coordinate[] c1, final Coordinate[] c2){
		Coordinate[] combined = new Coordinate[ c1.length + c2.length];

		// copy the first array to the start of the return array...
		System.arraycopy(c1, 0, combined, 0, c1.length);

		Coordinate[] revCurve = reverse( c2);
		int writeIndex = c1.length; // start directly after previous array
		int writeCount = revCurve.length;
		System.arraycopy(revCurve, 0, combined, writeIndex, writeCount);
			
		return combined;
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
	 * Return an approximation of the longitudinal unitary moment of inertia
     *
	 * The process is the following:
	 * 
	 * 1. Approximate a fin with a rectangular thin plate
	 * 
	 * 2. The unitary moment of inertia of one fin is taken as the average
	 *    of the unitary moments of inertia through its center perpendicular
	 *    to the plane (Izz/M), and through its center parallel to the plane (Iyy/M)
	 *    
	 * 3. If there are multiple fins, the inertia is shifted to the center of the
	 *    FinSet using the Parallel Axis Theorem
	 */
	@Override
	public double getLongitudinalUnitInertia() {
		if(Double.isNaN(this.singlePlanformArea)){
			calculateCM();
		}

		// Approximate fin with a rectangular fin
		// w2 and h2 are squares of the fin width and height
		double w = getLength();
		double h = getSpan();
		double w2, h2;

		// If either h or w is 0, we punt and treat the fin as square
		if (MathUtil.equals(h * w, 0)) {
			w2 = singlePlanformArea;
			h2 = singlePlanformArea;
		} else {
			w2 = w * singlePlanformArea / h;
			h2 = h * singlePlanformArea / w;
		}

		// Iyy = h * w^3 / 12, so Iyy/M = w^2 / 12
		// Izz = h * w * (h^2 + w^2) / 12, so Izz/M = (h^2 + w^2) / 12
		// (Iyy / M + Izz / M) / 2 = (h^2 + 2 * w^2)/24
		final double inertia = (h2 + 2 * w2) / 24;

		
		if (finCount == 1)
			return inertia;

		// Move axis to center of FinSet.  We need to apply the Parallel Axis Theorem
		// to Izz, but not to Iyy (as the displacement as we move to the new axis
		// is along Y).  Since our moment of inertia is the average of Iyy and Izz,
		// this is accomplished by just weighting the transformation from the theorem
		// by 1/2
		return inertia + MathUtil.pow2(MathUtil.safeSqrt(h2) / 2 + getBodyRadius()) / 2;
	}
	
	
	/*
	 * Return an approximation of the rotational unitary inertia of the fin set.
	 * The process is the following:
	 * 
	 * 1. Approximate the fin with a rectangular thin plate
	 *
	 * 2. calculate the unitary rotational inertia (Ixx/M) of the
	 *    rectangular approximation, about the center of the approximated fin.
	 *    
	 * 2. If there are multiple fins, shift the inertia axis to the center
	 *    of the Finset.
	 */
	@Override
	public double getRotationalUnitInertia() {
		if(Double.isNaN(this.singlePlanformArea)){
			calculateCM();
		}

		// Approximate fin with a rectangular fin
		// h2 is square of fin height
		double w = getLength();
		double h = getSpan();
		double h2;
		
		// If either h or w is 0, punt and treat it as a square fin
		if (MathUtil.equals(w * h, 0)) {
			h2 = singlePlanformArea;
		} else {
			h2 = h * singlePlanformArea / w;
		}

		// Ixx = w * h^3 / 12, so Ixx / M = h^2 / 12
		final double inertia = h2 / 12;

		if (finCount == 1)
			return inertia;

		// Move axis to center of FinSet using Parallel Axis Theorem
		return inertia + MathUtil.pow2(MathUtil.safeSqrt(h2) / 2 + getBodyRadius());
	}
	

	public BoundingBox getInstanceBoundingBox(){
		final BoundingBox singleFinBounds = new BoundingBox();

		singleFinBounds.update(getFinPoints());

		singleFinBounds.update(new Coordinate( 0, 0, -this.thickness/2));
		singleFinBounds.update(new Coordinate( 0, 0,  this.thickness/2));

		return singleFinBounds;
	}
	
	/**
	 * Adds bounding coordinates to the given set.  The body tube will fit within the
	 * convex hull of the points.
	 *
	 * Currently the points are simply a rectangular box around the body tube.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<>(8);
		
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
		if (e.isAerodynamicChange() || e.isMassChange()) {
			this.singlePlanformArea = Double.NaN;
			this.centerOfMass = Coordinate.NaN;
			this.totalVolume = Double.NaN;
			this.cantRotation = null;
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
		return getFinFront().y;
	}

	public Coordinate getFinFront() {
		final double xFinFront = this.getAxialFront();
		final SymmetricComponent symmetricParent = (SymmetricComponent)this.getParent();
		if( null == symmetricParent){
			return new Coordinate( 0, 0);
		}else{
			final double yFinFront = symmetricParent.getRadius( xFinFront );
			return new Coordinate(xFinFront, yFinFront);
		}
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
	
	public boolean isTabTrivial(){
		return ( FinSet.minimumTabArea > (getTabLength()*getTabHeight()));
	}
		
	public boolean isRootStraight( ){
        if( getParent() instanceof Transition){
			return ((Transition) getParent()).getType() == Shape.CONICAL;
        }
        
        // by default, assume a flat base
        return true;
    }

	/**
	 * Return a copied list of the given input, translated by the delta
	 *
	 * @return  List of XY-coordinates.
	 */
	protected static Coordinate[] translatePoints( final Coordinate[] inp, final double x_delta , final double y_delta){
		Coordinate[] returnPoints = new Coordinate[inp.length];
		for( int index=0; index < inp.length; ++index){
			final double new_x = inp[index].x + x_delta;
			final double new_y = inp[index].y + y_delta;
			returnPoints[index] = new Coordinate(new_x, new_y);
		}
		return returnPoints;
	}

	/**
	 * Return a copied list of the given input, translated by the delta
	 *
	 * @return  List of XY-coordinates.
	 */
	protected static ArrayList<Coordinate> translatePoints( final ArrayList<Coordinate> inp, final Coordinate delta){
		final ArrayList<Coordinate> returnPoints = new ArrayList<>();
		returnPoints.ensureCapacity(inp.size());

		for( Coordinate c: inp ){
			returnPoints.add(c.add(delta));
		}

		return returnPoints;
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

	/**
	 * used to get body points for the profile design view
	 *
	 * @return points representing the fin-root points, relative to ( x: fin-front, y: centerline ) i.e. relto: fin Component reference point
	 */
	public Coordinate[] getRootPoints(final int maximumBodyDivisionCount) {
		if( null == parent){
			return new Coordinate[]{Coordinate.ZERO};
		}

		final Coordinate finLead = getFinFront();
		final double xFinEnd = finLead.x + getLength();

		return getMountPoints( finLead.x, xFinEnd, -finLead.x, -finLead.y, maximumBodyDivisionCount);
	}

	/**
	 * used to get body points for the profile design view
	 *
	 * @return points representing the fin-root points, relative to ( x: fin-front, y: centerline ) i.e. relto: fin Component reference point
	 */
	public Coordinate[] getRootPoints(){
		return getRootPoints(MAX_ROOT_DIVISIONS);
	}

	/**
	 * use this for calculating physical properties, and routine drawing
	 *
	 * @return points representing the fin-root points, relative to ( x: fin-front, y: centerline ) i.e. relto: fin Component reference point
	 */
	public Coordinate[] getMountPoints() {
		if( null == parent){
			return null;
		}

		return getMountPoints(0., parent.getLength(), 0,0);
	}

	/**
	 * used to get calculate body profile points:
	 *
	 * @param xStart - xStart, in Mount-frame
	 * @param xEnd - xEnd, in Mount-frame
	 * @param xOffset - x-Offset to apply to returned points
	 * @param yOffset - y-Offset to apply to returned points
	 *
	 * @return points representing the mount's points
	 */
	private Coordinate[] getMountPoints(final double xStart, final double xEnd, final double xOffset, final double yOffset,
										final int maximumBodyDivisionCount) {
		if (parent == null) {
			return new Coordinate[]{Coordinate.ZERO};
		}

		// for a simple body, one increment is perfectly accurate.
		int divisionCount = 1;
		final SymmetricComponent body = (SymmetricComponent) getParent();
		final double intervalLength = xEnd - xStart;

		// for anything more complicated, increase the count:
		if ((body instanceof Transition) && (((Transition)body).getType() != Shape.CONICAL)) {
			// the maximum precision to enforce when calculating the areas of fins (especially on curved parent bodies)
			final double xWidth = 0.0025; // width (in meters) of each individual iteration
			divisionCount = (int) Math.ceil(intervalLength / xWidth);

			// When creating body curves, don't create more than this many divisions. -- only relevant on very large components
			// a too high division count will cause the 3D render to have invisible faces because it can't deal with the geometry.
			divisionCount = Math.min(maximumBodyDivisionCount, divisionCount);
		}

		// Recalculate the x step increment, now with the (rounded) division count.
		double xIncrement = intervalLength / divisionCount;

		// Create the points: step through the radius of the parent
		double xCur = xStart;
		List<Coordinate> points = new ArrayList<>();
		for (int index = 0; index < divisionCount+1; index++) {
			double yCur = body.getRadius(xCur);
			points.add(new Coordinate(xCur, yCur));

			xCur += xIncrement;
		}

		/*
		If the front fin point is outside the parent's bounds, and the last point is still within the parent's bounds,
		then we need to add an extra root point at the front of the parent. Same goes for the last point, but vice versa.
		This ensures that fins are drawn correctly on transitions and nose cones (see GitHub issue #1021 for more info).
		 */
		// Front fin point is outside the parent's bounds and last point is still within the parent's bounds
		if (xStart < 0 && xEnd > 0) {
			points.add(1, new Coordinate(0, points.get(0).y));
		}
		// End fin point is outside the parent's bounds and first point is still within the parent's bounds
		if (xEnd > parent.length && xStart < parent.length) {
			final double x = parent.length;
			final double y = points.get(points.size() - 1).y;
			points.add(points.size() - 1, new Coordinate(x, y));
		}

		Coordinate[] rootPoints = points.toArray(new Coordinate[0]);

		// correct last point, if beyond a rounding error from body's end.
		final int lastIndex = rootPoints.length - 1;
		if (Math.abs(rootPoints[lastIndex].x - body.getLength()) < MathUtil.EPSILON) {
			rootPoints[lastIndex] = rootPoints[lastIndex].setX(body.getLength()).setY(body.getAftRadius());
		}

		// translate the points if needed
		if ((Math.abs(xOffset) + Math.abs(yOffset)) > MathUtil.EPSILON) {
			rootPoints = translatePoints(rootPoints, xOffset, yOffset);
		}

		return rootPoints;
	}

	private Coordinate[] getMountPoints(final double xStart, final double xEnd, final double xOffset, final double yOffset) {
		return getMountPoints(xStart, xEnd, xOffset, yOffset, MAX_ROOT_DIVISIONS);
	}

	/**
	 * Return a list of coordinates defining the geometry of a single fin, including the parent's body points .
	 */
	public Coordinate[] getFinPointsWithRoot() {
		return combineCurves(getFinPoints(), getRootPoints());
	}

	/**
	 * Return a list of coordinates defining the geometry of a single fin, including the parent's body points .
	 *
	 * This low res version is for 3D rendering, as a too high resolution would cause clipping and invisible fin faces.
	 * This should at one point be solved by rendering the fin faces using triangulation, instead of how it's currently implemented.
	 */
	public Coordinate[] getFinPointsWithLowResRoot() {
		return combineCurves(getFinPoints(), getRootPoints(MAX_ROOT_DIVISIONS_LOW_RES));
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

		final double xTabFront = getTabFrontEdge();
		final double xTabTrail = getTabTrailingEdge();

		List<Coordinate> rootPoints = new ArrayList<>();
		for (Coordinate point : getRootPoints()) {
			if (point.x > xTabFront && point.x < xTabTrail) {
				rootPoints.add(point);
			}
		}

		return generateTabPointsWithRoot(rootPoints);
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
	 * This low res version is for 3D rendering, as a too high resolution would cause clipping and invisible fin faces.
	 * This should at one point be solved by rendering the fin faces using triangulation, instead of how it's currently implemented.
	 *
	 * @return  List of XY-coordinates.
	 */
	public Coordinate[] getTabPointsLowRes() {
		if (MathUtil.equals(getTabHeight(), 0) ||
				MathUtil.equals(getTabLength(), 0)){
			return new Coordinate[]{};
		}

		final double xTabFront = getTabFrontEdge();
		final double xTabTrail = getTabTrailingEdge();

		List<Coordinate> rootPoints = new ArrayList<>();
		for (Coordinate point : getRootPoints(MAX_ROOT_DIVISIONS_LOW_RES)) {
			if (point.x > xTabFront && point.x < xTabTrail) {
				rootPoints.add(point);
			}
		}

		return generateTabPointsWithRoot(rootPoints);
	}

	private Coordinate[] generateTabPointsWithRoot(List<Coordinate> rootPoints) {
		final double xTabFront = getTabFrontEdge();
		final double xTabTrail = getTabTrailingEdge();

		Coordinate[] tabPoints = new Coordinate[4];
		final Coordinate finFront = this.getFinFront();

		final SymmetricComponent body = (SymmetricComponent)this.getParent();

		// // limit the new heights to be no greater than the current body radius.
		double yTabFront = Double.NaN;
		double yTabTrail = Double.NaN;
		double yTabBottom = Double.NaN;
		if( null != body ){
			yTabFront = body.getRadius( finFront.x + xTabFront) - finFront.y;
			yTabTrail = body.getRadius( finFront.x + xTabTrail) - finFront.y;
			yTabBottom = MathUtil.min(yTabFront, yTabTrail) - tabHeight;
		}

		tabPoints[0] = new Coordinate(xTabFront, yTabFront);
		tabPoints[1] = new Coordinate(xTabFront, yTabBottom );
		tabPoints[2] = new Coordinate(xTabTrail, yTabBottom );
		tabPoints[3] = new Coordinate(xTabTrail, yTabTrail);
		rootPoints.add(0, new Coordinate(xTabFront, yTabFront));

		return combineCurves(tabPoints, rootPoints.toArray(new Coordinate[0]));
	}
	
	@Override
	public double getAngleOffset() {
		return firstFinOffsetRadians;
	}

	@Override
	public void setAngleOffset(final double angleRadians) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setAngleOffset(angleRadians);
			}
		}

		final double reducedAngle = MathUtil.reducePi(angleRadians);
		if (MathUtil.equals(reducedAngle, firstFinOffsetRadians))
			return;
		firstFinOffsetRadians = reducedAngle;

		if (MathUtil.equals(this.firstFinOffsetRadians, 0)) {
			baseRotation = Transformation.IDENTITY;
		} else {
			baseRotation = Transformation.rotate_x(firstFinOffsetRadians);
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	@Override
	public double getInstanceAngleIncrement(){
		return ( 2*Math.PI / getFinCount());
	}
	
	@Override
	public double[] getInstanceAngles() {
		final double angleIncrementRadians = getInstanceAngleIncrement();
		
		double[] result = new double[ getFinCount()]; 
		for( int finNumber=0; finNumber < getFinCount(); ++finNumber ){
			result[finNumber] = MathUtil.reduce2Pi( firstFinOffsetRadians + angleIncrementRadians*finNumber);
		}
		
		return result;
	}
	 
	@Override
	public AngleMethod getAngleMethod() {
		return this.angleMethod;
	}

	@Override
	public void setAngleMethod(AngleMethod newAngleMethod ) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setAngleMethod(newAngleMethod);
			}
		}

		mutex.verify();
		this.angleMethod = newAngleMethod;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	@Override
	public RadiusMethod getRadiusMethod() {
		return this.radiusMethod;
	}

	@Override
	public void setRadiusMethod(RadiusMethod newRadiusMethod) {
		// no-op.  Fins are inherently set to RadiusMethod.SURFACE @ 0.0 
	}

	@Override
	public void setRadius( final RadiusMethod newMethod, final double newRadius_m ) {
		// no-op.  Fins are inherently set to RadiusMethod.SURFACE @ 0.0 
	}
	
	@Override
	public void setRadiusOffset(double radius) {
		// no-op.  Fins are inherently set to RadiusMethod.SURFACE @ 0.0 
	}
	

	@Override
	public void setInstanceCount(int newCount) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setInstanceCount(newCount);
			}
		}

		setFinCount(newCount);
	}

	@Override
	public int getInstanceCount() {
		return getFinCount();
	}
	
	@Override
	public String getPatternName() {
		return (this.getInstanceCount() + "-fin-ring");
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
		this.finRotationIncrement = src.finRotationIncrement;
		this.firstFinOffsetRadians = src.firstFinOffsetRadians;
		this.cantRadians = src.cantRadians;
		this.cantRotation = src.cantRotation;
		this.thickness = src.thickness;
		this.crossSection = src.crossSection;
		this.tabHeight = src.tabHeight;
		this.tabLength = src.tabLength;
		this.tabOffsetMethod = src.tabOffsetMethod;
		this.tabPosition = src.tabPosition;
		
		return super.copyFrom(c);
	}
	
	/*
	 * Handle fin fillet mass properties	
	 */
	public Material getFilletMaterial() {
		return filletMaterial;
	}
	
	public void setFilletMaterial(Material mat) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setFilletMaterial(mat);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof FinSet) {
				((FinSet) listener).setFilletRadius(r);
			}
		}

		if (MathUtil.equals(filletRadius, r))
			return;
		filletRadius = r;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	// for debugging.  You can safely delete this method
	public static String getPointDescr( final Coordinate[] points, final String name, final String indent){
		return getPointDescr(Arrays.asList(points), name, indent);
	}

	// for debugging.  You can safely delete this method
	public static String getPointDescr( final List<Coordinate> points, final String name, final String indent){
		StringBuilder buf = new StringBuilder();

		buf.append(String.format("%s    >> %s: %d points\n", indent, name, points.size()));
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

		buf.append( getPointDescr( this.getFinPoints(), "Fin Points", ""));

		if (null != parent) {
			buf.append( getPointDescr( this.getMountPoints(0, parent.getLength(), 0, 0), "Body Points", ""));
			buf.append( getPointDescr( this.getRootPoints(), "Root Points", ""));
		}

		if( ! this.isTabTrivial() ) {
			buf.append(String.format("    TabLength: %6.4f TabHeight: %6.4f @ %6.4f    via: %s\n", tabLength, tabHeight, tabPosition, this.tabOffsetMethod));
			buf.append(getPointDescr(this.getTabPoints(), "Tab Points", ""));
		}
		return buf;
	}

	private void calculateCM(){
		final Coordinate wettedCentroid = calculateSinglePlanformCentroid();
		this.singlePlanformArea = wettedCentroid.weight;
		final double wettedVolume = wettedCentroid.weight * thickness * crossSection.getRelativeVolume();
		final double finBulkMass = wettedVolume * material.getDensity();
		final Coordinate wettedCM = wettedCentroid.setWeight(finBulkMass);

		final Coordinate tabCentroid = calculateTabCentroid();
		final double tabVolume = tabCentroid.weight * thickness;
		final double tabMass = tabVolume * material.getDensity();
		final Coordinate tabCM = tabCentroid.setWeight(tabMass);
		
		Coordinate filletCentroid = calculateFilletVolumeCentroid();
		double filletVolume = filletCentroid.weight;
		double filletMass = filletVolume * filletMaterial.getDensity();
		final Coordinate filletCM = filletCentroid.setWeight(filletMass);

		this.totalVolume = (wettedVolume + tabVolume + filletVolume) * finCount;

		final double eachFinMass = finBulkMass + tabMass + filletMass;
		final Coordinate eachFinCenterOfMass = wettedCM.average(tabCM).average(filletCM).setWeight(eachFinMass);
		
		// ^^ per fin
		// vv per component

		// set y coordinate: rotate around parent, if single fin; otherwise multiple fins will average out to zero
		if (finCount == 1) {
			this.centerOfMass = baseRotation.transform( eachFinCenterOfMass );
		} else {
			this.centerOfMass = eachFinCenterOfMass.setY(0.).setWeight( eachFinMass * this.finCount);
		}
	}
	
	// ============= Instanceable Interface Methods ===============
	@Override
	public Coordinate[] getInstanceOffsets(){
		checkState();

		final double bodyRadius = this.getBodyRadius();
		
		// already includes the base rotation
		final double[] angles = getInstanceAngles();

		final Transformation localCantRotation = new Transformation(length / 2, 0, 0)
													.applyTransformation(getCantRotation())
													.applyTransformation(new Transformation(-length / 2, 0, 0));
		
		Coordinate[] toReturn = new Coordinate[finCount];
		for (int instanceNumber = 0; instanceNumber < finCount; instanceNumber++) {
			final Coordinate raw = new Coordinate( 0, bodyRadius, 0);
			final Coordinate canted = localCantRotation.transform(raw);
			final Coordinate rotated = Transformation.rotate_x(angles[instanceNumber]).transform(canted);
			toReturn[instanceNumber] = rotated;
		}
		
		return toReturn;
	}

	@Override
	public InsideColorComponentHandler getInsideColorComponentHandler() {
		return this.insideColorComponentHandler;
	}

	@Override
	public void setInsideColorComponentHandler(InsideColorComponentHandler handler) {
		this.insideColorComponentHandler = handler;
	}
}
