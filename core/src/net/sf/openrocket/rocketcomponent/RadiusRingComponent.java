package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * ???
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class RadiusRingComponent extends RingComponent implements Coaxial, LineInstanceable  {

	protected double outerRadius = 0;
	protected double innerRadius = 0;

	protected int instanceCount = 1;
	// front-front along the positive rocket axis. i.e. [1,0,0];
	protected double instanceSeparation = 0; 
   
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


	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(final double _separation){
		this.instanceSeparation = _separation;
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		if( 0 < newCount ){
			this.instanceCount = newCount;
		}
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		Coordinate[] toReturn = new Coordinate[this.getInstanceCount()];
		for ( int index=0 ; index < this.getInstanceCount(); index++){
			toReturn[index] = new Coordinate( index*this.instanceSeparation, 0, 0);
		}
		
		return toReturn;
	}

	
	@Override
	public int getInstanceCount(){
		return this.instanceCount;
	}
	
	@Override	
	public String getPatternName(){
		return (this.getInstanceCount() + "-Line");
	}
}
