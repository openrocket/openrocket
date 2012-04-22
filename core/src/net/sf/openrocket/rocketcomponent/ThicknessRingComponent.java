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
public abstract class ThicknessRingComponent extends RingComponent {

	protected double outerRadius = 0;
	protected double thickness = 0;
	
	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if ( preset.has(ComponentPreset.OUTER_DIAMETER) )  {
			this.outerRadiusAutomatic = false;
			this.innerRadiusAutomatic = false;
			double outerDiameter = preset.get(ComponentPreset.OUTER_DIAMETER);
			this.outerRadius = outerDiameter/2.0;
			if ( preset.has(ComponentPreset.INNER_DIAMETER) ) {
				double innerDiameter = preset.get(ComponentPreset.INNER_DIAMETER);
				this.thickness = (outerDiameter-innerDiameter) / 2.0;
			}
		}
		super.loadFromPreset(preset);

		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);

	}

	@Override
	public double getOuterRadius() {
		if (isOuterRadiusAutomatic() && getParent() instanceof RadialParent) {
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

		if (thickness > outerRadius)
			thickness = outerRadius;
		
		clearPreset();
		
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	

	@Override
	public double getThickness() {
		return Math.min(thickness, getOuterRadius());
	}
	@Override
	public void setThickness(double thickness) {
		double outer = getOuterRadius();
		
		thickness = MathUtil.clamp(thickness, 0, outer);
		if (MathUtil.equals(getThickness(), thickness))
			return;
		
		this.thickness = thickness;
		
		clearPreset();

		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	
	@Override
	public double getInnerRadius() {
		return Math.max(getOuterRadius()-thickness, 0);
	}
	@Override
	public void setInnerRadius(double r) {
		r = Math.max(r,0);
		setThickness(getOuterRadius() - r);
	}
	
	
}
