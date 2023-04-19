package net.sf.openrocket.optimization.rocketoptimization.parameters;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.simulation.listeners.system.ApogeeEndListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * An optimization parameter that computes the maximum velocity during a simulated flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MaximumVelocityParameter extends SimulationBasedParameter {
	
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
		return simulatedData.getBranch(0).getMaximum(FlightDataType.TYPE_VELOCITY_TOTAL);
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_VELOCITY;
	}
	
}
