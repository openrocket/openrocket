package net.sf.openrocket.rocketcomponent;

public interface RingInstanceable extends Instanceable {

	public double getAngularOffset();

	public double getInstanceAngle( final int instanceNumber);
	
	public double getRadialOffset();
	
	public boolean getAutoRadialOffset();
	
	public void setAngularOffset(final double angle);
	
	public void setRadialOffset(final double radius);
	
}
