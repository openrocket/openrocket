package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;


/**
 * This interface is used to signal that the implementing interface contains multiple instances of its components. 
 * (Note: not all implementations replicate their children, but that is design intention.)
 * 
 * @author teyrana ( Daniel Williams, equipoise@gmail.com  )
 *
 */

public interface MultipleComponent {
	
	
	public int getInstanceCount();
	
	// location of each instance relative to the component
	// center-to-center vectors
	public Coordinate[] getInstanceOffsets();
	
	
}
