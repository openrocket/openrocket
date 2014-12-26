package net.sf.openrocket.simulation.extension.impl;

import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;

public class AirStart extends AbstractSimulationExtension {
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new AirStartListener());
	}
	
	@Override
	public String getName() {
		String name;
		if (getLaunchVelocity() > 0.01) {
			name = trans.get("SimulationExtension.airstart.name.altvel");
		} else {
			name = trans.get("SimulationExtension.airstart.name.alt");
		}
		name = L10N.replace(name, "{alt}", UnitGroup.UNITS_DISTANCE.toStringUnit(getLaunchAltitude()));
		name = L10N.replace(name, "{vel}", UnitGroup.UNITS_VELOCITY.toStringUnit(getLaunchVelocity()));
		return name;
	}
	
	public double getLaunchAltitude() {
		return config.getDouble("launchAltitude", 0.0);
	}
	
	public void setLaunchAltitude(double launchAltitude) {
		config.put("launchAltitude", launchAltitude);
		fireChangeEvent();
	}
	
	public double getLaunchVelocity() {
		return config.getDouble("launchVelocity", 0.0);
	}
	
	public void setLaunchVelocity(double launchVelocity) {
		config.put("launchVelocity", launchVelocity);
		fireChangeEvent();
	}
	
	
	private class AirStartListener extends AbstractSimulationListener {
		@Override
		public void startSimulation(SimulationStatus status) throws SimulationException {
			status.setRocketPosition(new Coordinate(0, 0, getLaunchAltitude()));
			status.setRocketVelocity(status.getRocketOrientationQuaternion().rotate(new Coordinate(0, 0, getLaunchVelocity())));
		}
	}
}
