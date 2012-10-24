package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.FlightConfigurable;

public class CopyFlightConfigurationVisitor extends DepthFirstRecusiveVisitor {

	private final String oldConfigId;
	private final String newConfigId;
	
	public CopyFlightConfigurationVisitor(String oldConfigId, String newConfigId) {
		super();
		this.oldConfigId = oldConfigId;
		this.newConfigId = newConfigId;
	}

	@Override
	public void doAction(RocketComponent visitable) {
		
		if ( visitable instanceof FlightConfigurable ) {
			((FlightConfigurable)visitable).cloneFlightConfiguration(oldConfigId, newConfigId);
		}
	}
	
	
}
