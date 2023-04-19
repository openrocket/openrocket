package net.sf.openrocket.optimization.rocketoptimization.parameters;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * An optimization parameter that computes the distance where a rocket lands.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class LandingDistanceParameter extends SimulationBasedParameter {
	
	private static final Translator trans = Application.getTranslator();
	
	@Override
	public String getName() {
		return trans.get("name");
	}
	
	@Override
	protected double getResultValue(FlightData simulatedData) {
		return simulatedData.getBranch(0).getLast(FlightDataType.TYPE_POSITION_XY);
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_DISTANCE;
	}
	
}
