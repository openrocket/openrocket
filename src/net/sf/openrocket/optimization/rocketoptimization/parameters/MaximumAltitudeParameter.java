package net.sf.openrocket.optimization.rocketoptimization.parameters;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.system.ApogeeEndListener;

/**
 * An optimization parameter that computes the maximum altitude of a simulated flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MaximumAltitudeParameter implements OptimizableParameter {
	
	@Override
	public String getName() {
		return "Maximum altitude";
	}
	
	@Override
	public double computeValue(Simulation simulation) throws OptimizationException {
		try {
			System.out.println("Running simulation");
			simulation.simulate(new ApogeeEndListener());
			System.out.println("Maximum altitude was " + simulation.getSimulatedData().getBranch(0).getMaximum(FlightDataType.TYPE_ALTITUDE));
			return simulation.getSimulatedData().getBranch(0).getMaximum(FlightDataType.TYPE_ALTITUDE);
		} catch (SimulationException e) {
			throw new OptimizationException(e);
		}
	}
	
}
