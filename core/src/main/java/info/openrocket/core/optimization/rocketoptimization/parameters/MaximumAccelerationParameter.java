package info.openrocket.core.optimization.rocketoptimization.parameters;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.simulation.listeners.system.ApogeeEndListener;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

/**
 * An optimization parameter that computes the maximum acceleration during a
 * simulated flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MaximumAccelerationParameter extends SimulationBasedParameter {

	private static final Translator trans = Application.getTranslator();

	@Override
	public String getName() {
		return trans.get("name");
	}

	@Override
	protected SimulationListener[] getSimulationListeners() {
		return new SimulationListener[] { new ApogeeEndListener() };
	}

	@Override
	protected double getResultValue(FlightData simulatedData) {
		return simulatedData.getBranch(0).getMaximum(FlightDataType.TYPE_ACCELERATION_TOTAL);
	}

	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_ACCELERATION;
	}

}
