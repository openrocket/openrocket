package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SupportsFlightConfiguration;

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
		
		if ( visitable instanceof SupportsFlightConfiguration ) {
			((SupportsFlightConfiguration)visitable).cloneFlightConfiguration(oldConfigId, newConfigId);
		}
	}
	
	
}
