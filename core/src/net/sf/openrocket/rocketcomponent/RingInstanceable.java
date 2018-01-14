package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.rocketcomponent.position.AnglePositionable;
import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusPositionable;

public interface RingInstanceable extends Instanceable, AnglePositionable, RadiusPositionable {

	@Override
	public double getAngleOffset();
	@Override
	public void setAngleOffset( final double angle);
	@Override
	public AngleMethod getAngleMethod();
	@Override
	public void setAngleMethod( final AngleMethod method );
	
	public double getInstanceAngleIncrement();
	
	public double[] getInstanceAngles();
	
	
	@Override
	public double getRadiusOffset();
	@Override
	public void setRadiusOffset( final double radius);
	@Override
	public RadiusMethod getRadiusMethod();
	@Override
	public void setRadiusMethod( final RadiusMethod method );
	
}
