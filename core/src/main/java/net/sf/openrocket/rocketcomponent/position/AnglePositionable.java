package net.sf.openrocket.rocketcomponent.position;

public interface AnglePositionable {

	/**
	 * @return angle to the first element, in radians
	 */
	public double getAngleOffset();
	
	/**
	 * @param new offset angle, in radians
	 */
	public void setAngleOffset(final double angle);
	
	public AngleMethod getAngleMethod( );
	public void setAngleMethod( final AngleMethod newMethod );	
}
