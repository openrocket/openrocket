package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;


public abstract class FinSet extends ExternalComponent {
	
	/**
	 * Maximum allowed cant of fins.
	 */
	public static final double MAX_CANT = (15.0 * Math.PI / 180);
	
	
	public enum CrossSection {
		SQUARE("Square",   1.00),
		ROUNDED("Rounded", 0.99),
		AIRFOIL("Airfoil", 0.85);
		
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
	protected int fins = 3;
	
	/**
	 * Rotation about the x-axis by 2*PI/fins.
	 */
	protected Transformation finRotation = Transformation.rotate_x(2*Math.PI/fins);
	
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
	protected double thickness = 0;
	
	
	/**
	 * The cross-section shape of the fins.
	 */
	protected CrossSection crossSection = CrossSection.SQUARE;
	
	
	// Cached fin area & CG.  Validity of both must be checked using finArea!
	private double finArea = -1;
	private double finCGx = -1;
	private double finCGy = -1;
	
	
	/**
	 * New FinSet with given number of fins and given base rotation angle.
	 * Sets the component relative position to POSITION_RELATIVE_BOTTOM,
	 * i.e. fins are positioned at the bottom of the parent component.
	 */
	public FinSet() {
		super(RocketComponent.Position.BOTTOM);
	}

	
	
	/**
	 * Return the number of fins in the set.
	 * @return The number of fins.
	 */
	public int getFinCount() {
		return fins;
	}

	/**
	 * Sets the number of fins in the set.
	 * @param n The number of fins, greater of equal to one.
	 */
	public void setFinCount(int n) {
		if (fins == n)
			return;
		if (n < 1)
			n = 1;
		if (n > 8)
			n = 8;
		fins = n;
		finRotation = Transformation.rotate_x(2*Math.PI/fins);
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
			if (MathUtil.equals(cantAngle,0)) {
				cantRotation = Transformation.IDENTITY;
			} else {
				Transformation t = new Transformation(-length/2,0,0);
				t = Transformation.rotate_y(cantAngle).applyTransformation(t);
				t = new Transformation(length/2,0,0).applyTransformation(t);
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
		thickness = Math.max(r,0);
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
	public void setRelativePosition(RocketComponent.Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	
	@Override
	public void setPositionValue(double value) {
		super.setPositionValue(value);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	

	
	
	
	///////////  Calculation methods  ///////////
	
	/**
	 * Return the area of one side of one fin.
	 * 
	 * @return   the area of one side of one fin.
	 */
	public double getFinArea() {
		if (finArea < 0)
			calculateAreaCG();
		
		return finArea;
	}
	
	/**
	 * Return the unweighted CG of a single fin.  The X-coordinate is relative to
	 * the root chord trailing edge and the Y-coordinate to the fin root chord.
	 * 
	 * @return  the unweighted CG coordinate of a single fin. 
	 */
	public Coordinate getFinCG() {
		if (finArea < 0)
			calculateAreaCG();
		
		return new Coordinate(finCGx,finCGy,0);
	}
	
	

	@Override
	public double getComponentVolume() {
		return fins * getFinArea() * thickness * crossSection.getRelativeVolume();
	}
	

	@Override
	public Coordinate getComponentCG() {
		if (finArea < 0)
			calculateAreaCG();
		
		double mass = getComponentMass();  // safe
		
		if (fins == 1) {
			return baseRotation.transform(
					new Coordinate(finCGx,finCGy + getBodyRadius(), 0, mass));
		} else {
			return new Coordinate(finCGx, 0, 0, mass);
		}
	}

	
	private void calculateAreaCG() {
		Coordinate[] points = this.getFinPoints();
		finArea = 0;
		finCGx = 0;
		finCGy = 0;
		
		for (int i=0; i < points.length-1; i++) {
			final double x0 = points[i].x;
			final double x1 = points[i+1].x;
			final double y0 = points[i].y;
			final double y1 = points[i+1].y;
			
			double da = (y0+y1)*(x1-x0) / 2;
			finArea += da;
			if (Math.abs(y0-y1) < 0.00001) {
				finCGx += (x0+x1)/2 * da;
				finCGy += y0/2 * da;
			} else {
				finCGx += (x0*(2*y0 + y1) + x1*(y0 + 2*y1)) / (3*(y0 + y1)) * da;
				finCGy += (y1 + y0*y0/(y0 + y1))/3 * da;
			}
		}
		
		if (finArea < 0)
			finArea = 0;
		
		if (finArea > 0) {
			finCGx /= finArea;
			finCGy /= finArea;
		} else {
			finCGx = (points[0].x + points[points.length-1].x)/2;
			finCGy = 0;
		}
	}
	
	
	/*
	 * Return an approximation of the longitudal unitary inertia of the fin set.
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
	public double getLongitudalUnitInertia() {
		double area = getFinArea();
		if (MathUtil.equals(area, 0))
			return 0;
		
		// Approximate fin with a rectangular fin
		// w2 and h2 are squares of the fin width and height
		double w = getLength();
		double h = getSpan();
		double w2,h2;
		
		if (MathUtil.equals(w*h,0)) {
			w2 = area;
			h2 = area;
		} else {
			w2 = w*area/h;
			h2 = h*area/w;
		}
		
		double inertia = (h2 + 2*w2)/24;
		
		if (fins == 1)
			return inertia;
		
		double radius = getBodyRadius();

		return fins * (inertia + MathUtil.pow2(Math.sqrt(h2) + radius));
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
		double area = getFinArea();
		if (MathUtil.equals(area, 0))
			return 0;
		
		// Approximate fin with a rectangular fin
		double w = getLength();
		double h = getSpan();
		
		if (MathUtil.equals(w*h,0)) {
			h = Math.sqrt(area);
		} else {
			h = Math.sqrt(h*area/w);
		}
		
		if (fins == 1)
			return h*h / 12;
		
		double radius = getBodyRadius();
		
		return fins * (h*h/12 + MathUtil.pow2(h/2 + radius));
	}
	
	
	/**
	 * Adds the fin set's bounds to the collection.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		List<Coordinate> bounds = new ArrayList<Coordinate>();
		double r = getBodyRadius();
		
		for (Coordinate point: getFinPoints()) {
			addFinBound(bounds, point.x, point.y + r);
		}
		
		return bounds;
	}

	
	/**
	 * Adds the 2d-coordinate bound (x,y) to the collection for both z-components and for
	 * all fin rotations.
	 */
	private void addFinBound(Collection<Coordinate> set, double x, double y) {
		Coordinate c;
		int i;
		
		c = new Coordinate(x,y,thickness/2);
		c = baseRotation.transform(c);
		set.add(c);
		for (i=1; i<fins; i++) {
			c = finRotation.transform(c);
			set.add(c);
		}
		
		c = new Coordinate(x,y,-thickness/2);
		c = baseRotation.transform(c);
		set.add(c);
		for (i=1; i<fins; i++) {
			c = finRotation.transform(c);
			set.add(c);
		}
	}

	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isAerodynamicChange()) {
			finArea = -1;
			cantRotation = null;
		}
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
		while (s!=null) {
			if (s instanceof SymmetricComponent) {
				double x = this.toRelative(new Coordinate(0,0,0), s)[0].x;
				return ((SymmetricComponent)s).getRadius(x);
			}
			s = s.getParent();
		}
		return 0;
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
	 * All Z-coordinates must be zero, and the last coordinate must have Y=0.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public abstract Coordinate[] getFinPoints();
	
	/**
	 * Get the span of a single fin.  That is, the length from the root to the tip of the fin.
	 * @return  Span of a single fin.
	 */
	public abstract double getSpan();
	
	
	@Override
	protected void copyFrom(RocketComponent c) {
		super.copyFrom(c);
		
		FinSet src = (FinSet)c;
		this.fins = src.fins;
		this.finRotation = src.finRotation;
		this.rotation = src.rotation;
		this.baseRotation = src.baseRotation;
		this.cantAngle = src.cantAngle;
		this.cantRotation = src.cantRotation;
		this.thickness = src.thickness;
		this.crossSection = src.crossSection;
	}
}
