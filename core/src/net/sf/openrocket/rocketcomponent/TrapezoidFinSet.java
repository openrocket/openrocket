package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * A set of trapezoidal fins.  The root and tip chords are perpendicular to the rocket
 * base line, while the leading and aft edges may be slanted.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class TrapezoidFinSet extends FinSet {
	private static final Translator trans = Application.getTranslator();
	
	public static final double MAX_SWEEP_ANGLE = (89 * Math.PI / 180.0);
	
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
		this(1, 0.05, 0.05, 0.025, 0.03);
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
		if (this.length == rootChord && this.tipChord == tipChord && this.sweep == sweep &&
				this.height == height && this.thickness == thickness)
			return;
		this.length = rootChord;
		this.tipChord = tipChord;
		this.sweep = sweep;
		this.height = height;
		this.thickness = thickness;
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public double getRootChord() {
		return length;
	}
	
	public void setRootChord(double r) {
		if (length == r)
			return;
		length = Math.max(r, 0);
		validateFinTab();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public double getTipChord() {
		return tipChord;
	}
	
	public void setTipChord(double r) {
		if (tipChord == r)
			return;
		tipChord = Math.max(r, 0);
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
	 * stored separately.
	 */
	public double getSweepAngle() {
		if (height == 0) {
			if (sweep > 0)
				return Math.PI / 2;
			if (sweep < 0)
				return -Math.PI / 2;
			return 0;
		}
		return Math.atan(sweep / height);
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
		double mySweep = Math.tan(r) * height;
		if (Double.isNaN(mySweep) || Double.isInfinite(mySweep))
			return;
		setSweep(mySweep);
	}
	
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double r) {
		if (height == r)
			return;
		height = Math.max(r, 0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	
	/**
	 * Returns the geometry of a trapezoidal fin.
	 */
	@Override
	public Coordinate[] getFinPoints() {
		List<Coordinate> list = new ArrayList<Coordinate>(4);
		
		list.add(Coordinate.NUL);
		list.add(new Coordinate(sweep, height));
		if (tipChord > 0.0001) {
			list.add(new Coordinate(sweep + tipChord, height));
		}
		list.add(new Coordinate(MathUtil.max(length, 0.0001), 0));
		
		return list.toArray(new Coordinate[list.size()]);
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
		//// Trapezoidal fin set
		return trans.get("TrapezoidFinSet.TrapezoidFinSet");
	}
	
}
