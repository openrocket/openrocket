package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;

public interface Instanceable {
		
	/** duplicate override...   especially vs shiftCoordinates... 
	// one of the two should be private
	 * 
	 * @return coordinates each instance of this component
	 */
	public Coordinate[] getLocation();
	
	// overrides a method in RocketComponent
	// not modifiable
	public boolean isCenterline();
	
	public void setInstanceCount( final int newCount );
	
	public int getInstanceCount();

	public Coordinate[] shiftCoordinates(Coordinate[] c);
	
	public String getPatternName();
	
}
