package info.openrocket.core.rocketcomponent;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

/**
 * A set of trapezoidal fins. The root and tip chords are perpendicular to the
 * rocket
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
		this(3, 0.05, 0.05, 0.025, 0.03);
	}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setFinShape(rootChord, tipChord, sweep, height, thickness);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setRootChord(r);
			}
		}

		if (length == r)
			return;
		length = Math.max(r, 0);
		updateTabPosition();

		fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
	}

	public double getTipChord() {
		return tipChord;
	}

	public void setTipChord(double r) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setTipChord(r);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setSweep(r);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setSweepAngle(r);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof TrapezoidFinSet) {
				((TrapezoidFinSet) listener).setHeight(r);
			}
		}

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
		List<Coordinate> points = new ArrayList<>(4);

		points.add(Coordinate.NUL);
		points.add(new Coordinate(sweep, height));
		if (tipChord > 0.0001) {
			points.add(new Coordinate(sweep + tipChord, height));
		}
		points.add(new Coordinate(MathUtil.max(length, 0.0001), 0));

		Coordinate[] finPoints = points.toArray(new Coordinate[0]);

		// Set the start and end fin points the same as the root points (necessary for canted fins)
		final Coordinate[] rootPoints = getRootPoints();
		if (rootPoints.length > 1) {
			finPoints[0] = finPoints[0].setX(rootPoints[0].x).setY(rootPoints[0].y);
			finPoints[finPoints.length - 1] = finPoints[finPoints.length - 1].setX(rootPoints[rootPoints.length - 1].x).setY(rootPoints[rootPoints.length - 1].y);
		}

		return finPoints;
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
