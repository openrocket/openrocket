package net.sf.openrocket.optimization.rocketoptimization.parameters;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationCalculationException;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.simulation.listeners.system.InterruptListener;

/**
 * An abstract optimization parameter that simulates a rocket flight and obtains
 * a value from the simulation result.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class SimulationBasedParameter implements OptimizableParameter {
	
	private static final Logger log = LoggerFactory.getLogger(SimulationBasedParameter.class);
	
	@Override
	public double computeValue(Simulation simulation) throws OptimizationException, InterruptedException {
		try {
			log.debug("Running simulation for " + getName());
			
			SimulationListener[] listeners = getSimulationListeners();
			listeners = Arrays.copyOf(listeners, listeners.length + 1);
			listeners[listeners.length - 1] = new InterruptListener();
			simulation.simulate(listeners);
			
			double value = getResultValue(simulation.getSimulatedData());
			log.debug("Parameter '" + getName() + " was " + value);
			return value;
		} catch (MotorIgnitionException e) {
			// A problem with motor ignition will cause optimization to fail
			throw new OptimizationException(e);
		} catch (SimulationLaunchException e) {
			// Other launch exceptions result in illegal value
			return Double.NaN;
		} catch (SimulationCalculationException e) {
			// Calculation errors result in illegal value
			return Double.NaN;
		} catch (SimulationCancelledException e) {
			// Simulation cancellation stops the optimization
			throw (InterruptedException) new InterruptedException("Optimization was interrupted").initCause(e);
		} catch (SimulationException e) {
			// Other exceptions fail
			throw new OptimizationException(e);
		}
	}
	
	
	/**
	 * Return the optimization parameter from the simulation flight data.
	 * 
	 * @param simulatedData		the simulated data.
	 * @return					the optimization parameter.
	 */
	protected abstract double getResultValue(FlightData simulatedData);
	
	/**
	 * Return an array of simulation listeners to provide to the simulation.
	 * This may include a listener that stops the simulation after the necessary value
	 * has been computed.
	 * <p>
	 * This array should NOT contain InterruptListener, it will be added implicitly.
	 * 
	 * @return	an array of simulation listeners to include.
	 */
	protected SimulationListener[] getSimulationListeners() {
		return new SimulationListener[0];
	}
	
}
