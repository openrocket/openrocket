package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * An inner component that consists of a hollow cylindrical component.  This can be
 * an inner tube, tube coupler, centering ring, bulkhead etc.
 *
 * The properties include the inner and outer radii, length and radial position.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class RadiusRingComponent extends RingComponent implements Coaxial {

	protected double outerRadius = 0;
	protected double innerRadius = 0;

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
		if ( preset.has(ComponentPreset.OUTER_DIAMETER)) {
			this.outerRadius = preset.get(ComponentPreset.OUTER_DIAMETER) / 2.0;
			this.outerRadiusAutomatic = false;
		}
		this.innerRadiusAutomatic = false;
		if ( preset.has(ComponentPreset.INNER_DIAMETER)) {
			this.innerRadius = preset.get(ComponentPreset.INNER_DIAMETER) / 2.0;
		}

		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);

	}

	@Override
	public double getOuterRadius() {
		if (outerRadiusAutomatic && getParent() instanceof RadialParent) {
			RocketComponent parent = getParent();
			double pos1 = this.toRelative(Coordinate.NUL, parent)[0].x;
			double pos2 = this.toRelative(new Coordinate(getLength()), parent)[0].x;
			pos1 = MathUtil.clamp(pos1, 0, parent.getLength());
			pos2 = MathUtil.clamp(pos2, 0, parent.getLength());
			outerRadius = Math.min(((RadialParent)parent).getInnerRadius(pos1),
					((RadialParent)parent).getInnerRadius(pos2));
		}

		return outerRadius;
	}

	@Override
	public void setOuterRadius(double r) {
		r = Math.max(r,0);
		if (MathUtil.equals(outerRadius, r) && !isOuterRadiusAutomatic())
			return;

		outerRadius = r;
		outerRadiusAutomatic = false;
		if (getInnerRadius() > r) {
			innerRadius = r;
			innerRadiusAutomatic = false;
		}

		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}


	@Override
	public double getInnerRadius() {
		return innerRadius;
	}
	@Override
	public void setInnerRadius(double r) {
		r = Math.max(r,0);
		if (MathUtil.equals(innerRadius, r))
			return;

		innerRadius = r;
		innerRadiusAutomatic = false;
		if (getOuterRadius() < r) {
			outerRadius = r;
			outerRadiusAutomatic = false;
		}

		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}


	@Override
	public double getThickness() {
		return Math.max(getOuterRadius() - getInnerRadius(), 0);
	}
	@Override
	public void setThickness(double thickness) {
		double outer = getOuterRadius();

		thickness = MathUtil.clamp(thickness, 0, outer);
		setInnerRadius(outer - thickness);
	}

}
