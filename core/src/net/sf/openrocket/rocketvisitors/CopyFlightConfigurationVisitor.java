package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class CopyFlightConfigurationVisitor extends DepthFirstRecusiveVisitor<Void> {
	
	private final String oldConfigId;
	private final String newConfigId;
	
	public CopyFlightConfigurationVisitor(String oldConfigId, String newConfigId) {
		super();
		this.oldConfigId = oldConfigId;
		this.newConfigId = newConfigId;
	}
	
	@Override
	public void doAction(RocketComponent visitable) {
		
		if (visitable instanceof FlightConfigurableComponent) {
			((FlightConfigurableComponent) visitable).cloneFlightConfiguration(oldConfigId, newConfigId);
		}
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
}
