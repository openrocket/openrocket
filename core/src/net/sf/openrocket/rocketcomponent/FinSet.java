package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;


public abstract class FinSet extends ExternalComponent {
	private static final Translator trans = Application.getTranslator();
	
	
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
	
	public enum TabRelativePosition {
		//// Root chord leading edge
		FRONT(trans.get("FinSet.TabRelativePosition.FRONT")),
		//// Root chord midpoint
		CENTER(trans.get("FinSet.TabRelativePosition.CENTER")),
		//// Root chord trailing edge
		END(trans.get("FinSet.TabRelativePosition.END"));
		
		private final String name;
		
		TabRelativePosition(String name) {
			this.name = name;
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
	protected Transformation finRotation = Transformation.rotate_x(2 * Math.PI / fins);
	
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
	private double tabHeight = 0;
	private double tabLength = 0.05;
	private double tabShift = 0;
	private TabRelativePosition tabRelativePosition = TabRelativePosition.CENTER;
	
	/*
	 * Fin fillet properties
	 */
	
	protected Material filletMaterial = null;
	protected double filletRadius = 0;
	protected double filletCenterY = 0;
	
	// Cached fin area & CG.  Validity of both must be checked using finArea!
	// Fin area does not include fin tabs, CG does.
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
		this.filletMaterial = Application.getPreferences().getDefaultComponentMaterial(this.getClass(), Material.Type.BULK);
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
		finRotation = Transformation.rotate_x(2 * Math.PI / fins);
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
	public void setRelativePosition(RocketComponent.Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public void setPositionValue(double value) {
		super.setPositionValue(value);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	
	public double getTabHeight() {
		return tabHeight;
	}
	
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
	
	public void setTabShift(double shift) {
		this.tabShift = shift;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public TabRelativePosition getTabRelativePosition() {
		return tabRelativePosition;
	}
	
	public void setTabRelativePosition(TabRelativePosition position) {
		if (this.tabRelativePosition == position)
			return;
		
		
		double front = getTabFrontEdge();
		switch (position) {
		case FRONT:
			this.tabShift = front;
			break;
		
		case CENTER:
			this.tabShift = front + tabLength / 2 - getLength() / 2;
			break;
		
		case END:
			this.tabShift = front + tabLength - getLength();
			break;
		
		default:
			throw new IllegalArgumentException("position=" + position);
		}
		this.tabRelativePosition = position;
		
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	/**
	 * Return the tab front edge position from the front of the fin.
	 */
	public double getTabFrontEdge() {
		switch (this.tabRelativePosition) {
		case FRONT:
			return tabShift;
			
		case CENTER:
			return getLength() / 2 - tabLength / 2 + tabShift;
			
		case END:
			return getLength() - tabLength + tabShift;
			
		default:
			throw new IllegalStateException("tabRelativePosition=" + tabRelativePosition);
		}
	}
	
	/**
	 * Return the tab trailing edge position *from the front of the fin*.
	 */
	public double getTabTrailingEdge() {
		switch (this.tabRelativePosition) {
		case FRONT:
			return tabLength + tabShift;
		case CENTER:
			return getLength() / 2 + tabLength / 2 + tabShift;
			
		case END:
			return getLength() + tabShift;
			
		default:
			throw new IllegalStateException("tabRelativePosition=" + tabRelativePosition);
		}
	}
	
	
	
	
	///////////  Calculation methods  ///////////
	
	/**
	 * Return the area of one side of one fin.  This does NOT include the area of
	 * the fin tab.
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
		
		return new Coordinate(finCGx, finCGy, 0);
	}
	
	
	@Override
	public double getComponentMass() {
		return getFilletMass() + getFinMass();
	}
	
	public double getFinMass() {
		return getComponentVolume() * material.getDensity();
	}
	
	public double getFilletMass() {
		return getFilletVolume() * filletMaterial.getDensity();
	}
	
	
	@Override
	public double getComponentVolume() {
		// this is for the fins alone, fillets are taken care of separately.
		return fins * (getFinArea() + tabHeight * tabLength) * thickness *
				crossSection.getRelativeVolume();
	}
	
	
	
	@Override
	public Coordinate getComponentCG() {
		if (finArea < 0)
			calculateAreaCG();
		
		double mass = getFinMass();
		double filletMass = getFilletMass();
		double filletCenter = length / 2;
		
		double newCGx = (filletCenter * filletMass + finCGx * mass) / (filletMass + mass);
		
		// FilletRadius/5 is a good estimate for where the vertical centroid of the fillet
		// is.  Finding the actual position is very involved and won't make a huge difference.
		double newCGy = (filletRadius / 5 * filletMass + finCGy * mass) / (filletMass + mass);
		
		if (fins == 1) {
			return baseRotation.transform(
					new Coordinate(finCGx, finCGy + getBodyRadius(), 0, (filletMass + mass)));
		} else {
			return new Coordinate(finCGx, 0, 0, (filletMass + mass));
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
	
	private void calculateAreaCG() {
		Coordinate[] points = this.getFinPoints();
		finArea = 0;
		finCGx = 0;
		finCGy = 0;
		
		for (int i = 0; i < points.length - 1; i++) {
			final double x0 = points[i].x;
			final double x1 = points[i + 1].x;
			final double y0 = points[i].y;
			final double y1 = points[i + 1].y;
			
			double da = (y0 + y1) * (x1 - x0) / 2;
			finArea += da;
			if (Math.abs(y0 + y1) < 0.00001) {
				finCGx += (x0 + x1) / 2 * da;
				finCGy += y0 / 2 * da;
			} else {
				finCGx += (x0 * (2 * y0 + y1) + x1 * (y0 + 2 * y1)) / (3 * (y0 + y1)) * da;
				finCGy += (y1 + y0 * y0 / (y0 + y1)) / 3 * da;
			}
		}
		
		if (finArea < 0)
			finArea = 0;
		
		// Add effect of fin tabs to CG
		double tabArea = tabLength * tabHeight;
		if (!MathUtil.equals(tabArea, 0)) {
			
			double x = (getTabFrontEdge() + getTabTrailingEdge()) / 2;
			double y = -this.tabHeight / 2;
			
			finCGx += x * tabArea;
			finCGy += y * tabArea;
			
		}
		
		if ((finArea + tabArea) > 0) {
			finCGx /= (finArea + tabArea);
			finCGy /= (finArea + tabArea);
		} else {
			finCGx = (points[0].x + points[points.length - 1].x) / 2;
			finCGy = 0;
		}
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
		double area = getFinArea();
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
		
		if (fins == 1)
			return inertia;
		
		double radius = getBodyRadius();
		
		return fins * (inertia + MathUtil.pow2(MathUtil.safeSqrt(h2) + radius));
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
		
		if (MathUtil.equals(w * h, 0)) {
			h = MathUtil.safeSqrt(area);
		} else {
			h = MathUtil.safeSqrt(h * area / w);
		}
		
		if (fins == 1)
			return h * h / 12;
		
		double radius = getBodyRadius();
		
		return fins * (h * h / 12 + MathUtil.pow2(h / 2 + radius));
	}
	
	
	/**
	 * Adds the fin set's bounds to the collection.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		List<Coordinate> bounds = new ArrayList<Coordinate>();
		double r = getBodyRadius();
		
		for (Coordinate point : getFinPoints()) {
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
		
		c = new Coordinate(x, y, thickness / 2);
		c = baseRotation.transform(c);
		set.add(c);
		for (i = 1; i < fins; i++) {
			c = finRotation.transform(c);
			set.add(c);
		}
		
		c = new Coordinate(x, y, -thickness / 2);
		c = baseRotation.transform(c);
		set.add(c);
		for (i = 1; i < fins; i++) {
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
		while (s != null) {
			if (s instanceof SymmetricComponent) {
				double x = this.toRelative(new Coordinate(0, 0, 0), s)[0].x;
				return ((SymmetricComponent) s).getRadius(x);
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
	 * All Z-coordinates must be zero, and the last coordinate must have Y=0.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public abstract Coordinate[] getFinPoints();
	
	
	/**
	 * Return a list of coordinates defining the geometry of a single fin, including a
	 * possible fin tab.  The coordinates are the XY-coordinates of points defining the 
	 * shape of a single fin, where the origin is the leading root edge.  This implementation
	 * calls {@link #getFinPoints()} and adds the necessary points for the fin tab.
	 * The tab coordinates will have a negative y value.
	 * 
	 * @return  List of XY-coordinates.
	 */
	public Coordinate[] getFinPointsWithTab() {
		Coordinate[] points = getFinPoints();
		
		if (MathUtil.equals(getTabHeight(), 0) ||
				MathUtil.equals(getTabLength(), 0))
			return points;
		
		double x1 = getTabFrontEdge();
		double x2 = getTabTrailingEdge();
		double y = -getTabHeight();
		
		boolean add1 = x1 != points[0].x;
		boolean add2 = x2 != points[points.length - 1].x;
		
		int n = points.length;
		points = ArrayUtils.copyOf(points, points.length + 2 + (add1 ? 1 : 0) + (add2 ? 1 : 0));
		
		if (add2)
			points[n++] = new Coordinate(x2, 0);
		points[n++] = new Coordinate(x2, y);
		points[n++] = new Coordinate(x1, y);
		if (add1)
			points[n++] = new Coordinate(x1, 0);
		
		return points;
	}
	
	
	
	/**
	 * Get the span of a single fin.  That is, the length from the root to the tip of the fin.
	 * @return  Span of a single fin.
	 */
	public abstract double getSpan();
	
	
	@Override
	protected List<RocketComponent> copyFrom(RocketComponent c) {
		FinSet src = (FinSet) c;
		this.fins = src.fins;
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
	
}
