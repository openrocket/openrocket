package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;

public interface Instanceable {
		
	/**
	 * Note: <code> this.getLocation().length == this.getInstanceCount() </code> should ALWAYS be true.  If getInstanceCount() returns anything besides 1,
	 *       this function should be override as well.  
	 * 
	 * Note: This is function has a concrete implementation in RocketComponent.java ... it is included here only as a reminder.
	 * 
	 * @return coordinates of each instance of this component -- specifically the front center of each instance in global coordinates
	 */
	public Coordinate[] getLocations();
	
	/**
	 * Returns vector coordinates of each instance of this component relative to this component's reference point (typically front center)
	 * 
	 * Note: <code> this.getOffsets().length == this.getInstanceCount() </code> should ALWAYS be true.  
	 * If getInstanceCount() returns anything besides 1 this function should be overridden as well.  
	 * 
	 * 
	 * @return coordinates location of each instance relative to <b>this</b> component's reference point.
	 */
	public Coordinate[] getInstanceOffsets();
	
	/** 
	 * How many instances of this component are represented.  This should generally be editable.
	 * @param newCount  number of instances to set
	 */
	public void setInstanceCount( final int newCount );
	
	/** 
	 * How many instances of this component are represented.  This should generally be editable.
	 * 
	 * @return number of instances this component currently represent. 
	 */
	public int getInstanceCount();

	/** 
	 * Get a human-readable name for this instance arrangement.
	 * Note: the same instance count may have different pattern names   
	 * 
	 * @return pattern name
	 */
	public String getPatternName();
	
}
