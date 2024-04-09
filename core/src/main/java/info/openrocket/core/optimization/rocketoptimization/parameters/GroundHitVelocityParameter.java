package info.openrocket.core.optimization.rocketoptimization.parameters;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

/**
 * An optimization parameter that computes the speed the rocket hits the ground.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GroundHitVelocityParameter extends SimulationBasedParameter {

	private static final Translator trans = Application.getTranslator();

	@Override
	public String getName() {
		return trans.get("name");
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
