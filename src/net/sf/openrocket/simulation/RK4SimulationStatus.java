package net.sf.openrocket.simulation;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Quaternion;

public class RK4SimulationStatus extends SimulationStatus {
	public Quaternion orientation;
	public Coordinate rotation;
	
	public Coordinate launchRodDirection;
	
	
	/**
	 * Provides a copy of the simulation status.  The orientation quaternion is
	 * cloned as well, so changing it does not affect other simulation status objects.
	 */
	@Override
	public RK4SimulationStatus clone() {
		RK4SimulationStatus copy = (RK4SimulationStatus) super.clone();
		copy.orientation = this.orientation.clone();
		return copy;
	}
}
