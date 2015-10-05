package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class CopyFlightConfigurationVisitor extends DepthFirstRecusiveVisitor<Void> {
	
	private final FlightConfigurationID oldConfigId;
	private final FlightConfigurationID newConfigId;
	
	public CopyFlightConfigurationVisitor(FlightConfigurationID oldConfigId, FlightConfigurationID newConfigId) {
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
