package net.sf.openrocket.rocketcomponent.position;

public interface RadiusPositionable {
	public boolean getAutoRadialOffset();	
	public double getRadialOffset();
	public void setAutoRadialOffset( final boolean auto );
	public void setRadialOffset(final double radius);
}
