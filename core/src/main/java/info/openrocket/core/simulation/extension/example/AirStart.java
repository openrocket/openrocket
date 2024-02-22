package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.l10n.L10N;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;

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

	@Override
	public String getDescription() {
		return "Start simulation with a configurable altitude and velocity";
	}

	public double getLaunchAltitude() {
		return config.getDouble("launchAltitude", 100.0);
	}

	public void setLaunchAltitude(double launchAltitude) {
		config.put("launchAltitude", launchAltitude);
		fireChangeEvent();
	}

	public double getLaunchVelocity() {
		return config.getDouble("launchVelocity", 50.0);
	}

	public void setLaunchVelocity(double launchVelocity) {
		config.put("launchVelocity", launchVelocity);
		fireChangeEvent();
	}

	private class AirStartListener extends AbstractSimulationListener {
		@Override
		public void startSimulation(SimulationStatus status) throws SimulationException {
			status.setRocketPosition(new Coordinate(0, 0, getLaunchAltitude()));
			status.setRocketVelocity(
					status.getRocketOrientationQuaternion().rotate(new Coordinate(0, 0, getLaunchVelocity())));
		}
	}
}
