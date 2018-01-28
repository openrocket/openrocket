package net.sf.openrocket.rocketcomponent.position;

public interface AnglePositionable {

	public double getAngleOffset();
	
	public void setAngleOffset(final double angle);
	
	public AngleMethod getAngleMethod( );
	public void setAngleMethod( final AngleMethod newMethod );	
}
