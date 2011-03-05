package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;

/**
 * A set of trapezoidal fins.  The root and tip chords are perpendicular to the rocket
 * base line, while the leading and aft edges may be slanted.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class TrapezoidFinSet extends FinSet {
	public static final double MAX_SWEEP_ANGLE=(89*Math.PI/180.0);

	/*
	 *           sweep   tipChord
	 *           |    |___________
	 *           |   /            |
	 *           |  /             |
	 *           | /              |  height
	 *            /               |
	 * __________/________________|_____________
	 *                length
	 *              == rootChord
	 */

	// rootChord == length
	private double tipChord = 0;
	private double height = 0;
	private double sweep = 0;


	public TrapezoidFinSet() {
		this (3, 0.05, 0.05, 0.025, 0.05);
	}

	// TODO: HIGH:  height=0 -> CP = NaN
	public TrapezoidFinSet(int fins, double rootChord, double tipChord, double sweep,
			double height) {
		super();

		this.setFinCount(fins);
		this.length = rootChord;
		this.tipChord = tipChord;
		this.sweep = sweep;
		this.height = height;
	}


	public void setFinShape(double rootChord, double tipChord, double sweep, double height,
			double thickness) {
		if (this.length==rootChord && this.tipChord==tipChord && this.sweep==sweep &&
				this.height==height && this.thickness==thickness)
			return;
		this.length=rootChord;
		this.tipChord=tipChord;
		this.sweep=sweep;
		this.height=height;
		this.thickness=thickness;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public double getRootChord() {
		return length;
	}
	public void setRootChord(double r) {
		if (length == r)
			return;
		length = Math.max(r,0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public double getTipChord() {
		return tipChord;
	}
	public void setTipChord(double r) {
		if (tipChord == r)
			return;
		tipChord = Math.max(r,0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	/**
	 * Get the sweep length.
	 */
	public double getSweep() {
		return sweep;
	}
	/**
	 * Set the sweep length.
	 */
	public void setSweep(double r) {
		if (sweep == r)
			return;
		sweep = r;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	/**
	 * Get the sweep angle.  This is calculated from the true sweep and height, and is not
	 * stored separetely.
	 */
	public double getSweepAngle() {
		if (height == 0) {
			if (sweep > 0)
				return Math.PI/2;
			if (sweep < 0)
				return -Math.PI/2;
			return 0;
		}
		return Math.atan(sweep/height);
	}
	/**
	 * Sets the sweep by the sweep angle.  The sweep is calculated and set by this method,
	 * and the angle itself is not stored.
	 */
	public void setSweepAngle(double r) {
		if (r > MAX_SWEEP_ANGLE)
			r = MAX_SWEEP_ANGLE;
		if (r < -MAX_SWEEP_ANGLE)
			r = -MAX_SWEEP_ANGLE;
		double sweep = Math.tan(r) * height;
		if (Double.isNaN(sweep) || Double.isInfinite(sweep))
			return;
		setSweep(sweep);
	}

	public double getHeight() {
		return height;
	}
	public void setHeight(double r) {
		if (height == r)
			return;
		height = Math.max(r,0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}



	/**
	 * Returns the geometry of a trapezoidal fin.
	 */
	@Override
	public Coordinate[] getFinPoints() {
		Coordinate[] c = new Coordinate[4];

		c[0] = Coordinate.NUL;
		c[1] = new Coordinate(sweep,height);
		c[2] = new Coordinate(sweep+tipChord,height);
		c[3] = new Coordinate(length,0);

		return c;
	}

	/**
	 * Returns the span of a trapezoidal fin.
	 */
	@Override
	public double getSpan() {
		return height;
	}


	@Override
	public String getComponentName() {
		return "Trapezoidal fin set";
	}

}
