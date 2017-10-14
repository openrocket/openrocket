package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.rocketcomponent.position.AnglePositionable;
import net.sf.openrocket.rocketcomponent.position.RadiusPositionable;

public interface RingInstanceable extends Instanceable, AnglePositionable, RadiusPositionable {

	@Override
	public double getAngularOffset();
	@Override
	public void setAngularOffset(final double angle);
	
	public double getInstanceAngleIncrement();
	
	public double[] getInstanceAngles();
	
	
	@Override
	public boolean getAutoRadialOffset();
	@Override
	public double getRadialOffset();
	@Override
	public void setAutoRadialOffset( final boolean auto );
	@Override
	public void setRadialOffset(final double radius);
	
}
