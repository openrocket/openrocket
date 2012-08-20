package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * A RingComponent that comes on top of another tube.  It's defined by the inner
 * radius and thickness.  The inner radius can be automatic, in which case it
 * takes the radius of the parent component.
 *  
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Sleeve extends RingComponent {
	private static final Translator trans = Application.getTranslator();
	
	protected double innerRadius = 0;
	protected double thickness = 0;
	
	
	public Sleeve() {
		super();
		setInnerRadiusAutomatic(true);
		setThickness(0.001);
		setLength(0.05);
	}
	
	
	@Override
	public double getOuterRadius() {
		return getInnerRadius() + thickness;
	}
	
	@Override
	public void setOuterRadius(double r) {
		if (MathUtil.equals(getOuterRadius(), r))
			return;
		
		innerRadius = Math.max(r - thickness, 0);
		if (thickness > r)
			thickness = r;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	@Override
	public double getInnerRadius() {
		// Implement parent inner radius automation
		if (isInnerRadiusAutomatic() && getParent() instanceof RadialParent) {
			RocketComponent parent = getParent();
			double pos1 = this.toRelative(Coordinate.NUL, parent)[0].x;
			double pos2 = this.toRelative(new Coordinate(getLength()), parent)[0].x;
			pos1 = MathUtil.clamp(pos1, 0, parent.getLength());
			pos2 = MathUtil.clamp(pos2, 0, parent.getLength());
			innerRadius = Math.max(((RadialParent) parent).getOuterRadius(pos1),
					((RadialParent) parent).getOuterRadius(pos2));
		}
		
		return innerRadius;
	}
	
	@Override
	public void setInnerRadius(double r) {
		r = Math.max(r, 0);
		if (MathUtil.equals(innerRadius, r))
			return;
		innerRadius = r;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	@Override
	public double getThickness() {
		return thickness;
	}
	
	@Override
	public void setThickness(double t) {
		t = Math.max(t, 0);
		if (MathUtil.equals(thickness, t))
			return;
		thickness = t;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	


	@Override
	public void setInnerRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}
	
	@Override
	public String getComponentName() {
		return trans.get ("Sleeve.Sleeve");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}
}
