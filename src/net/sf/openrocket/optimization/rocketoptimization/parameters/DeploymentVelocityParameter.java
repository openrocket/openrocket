package net.sf.openrocket.optimization.rocketoptimization.parameters;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.simulation.listeners.system.RecoveryDeviceDeploymentEndListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * An optimization parameter that computes the total flight time.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DeploymentVelocityParameter extends SimulationBasedParameter {
	
	private static final Translator trans = Application.getTranslator();
	
	@Override
	public String getName() {
		return trans.get("name");
	}
	
	@Override
	protected SimulationListener[] getSimulationListeners() {
		return new SimulationListener[] { new RecoveryDeviceDeploymentEndListener() };
	}
	
	@Override
	protected double getResultValue(FlightData simulatedData) {
		return simulatedData.getBranch(0).getLast(FlightDataType.TYPE_VELOCITY_TOTAL);
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_VELOCITY;
	}
	
}
