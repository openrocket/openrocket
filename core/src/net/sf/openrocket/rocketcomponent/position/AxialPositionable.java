package net.sf.openrocket.rocketcomponent.position;

import net.sf.openrocket.rocketcomponent.RocketComponent.Position;

public interface AxialPositionable {

	public double getAxialOffset();
	
	public void setAxialOffset(final double newAxialOffset);
	
	public Position getAxialPositionMethod( );
	
	public void setAxialPositionMethod( Position newMethod );
}
