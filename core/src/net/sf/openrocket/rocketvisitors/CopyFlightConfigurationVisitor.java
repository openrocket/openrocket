package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class CopyFlightConfigurationVisitor extends DepthFirstRecusiveVisitor<Void> {
	
	private final FlightConfigurationId oldConfigId;
	private final FlightConfigurationId newConfigId;
	
	public CopyFlightConfigurationVisitor(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
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
