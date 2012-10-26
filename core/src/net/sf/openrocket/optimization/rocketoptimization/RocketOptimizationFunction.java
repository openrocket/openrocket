package net.sf.openrocket.optimization.rocketoptimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.Function;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.Pair;

/**
 * A Function that optimizes a specific RocketOptimizationParameter to some goal
 * by modifying a base simulation using SimulationModifiers.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketOptimizationFunction implements Function {
	private static final Logger log = LoggerFactory.getLogger(RocketOptimizationFunction.class);
	
	private static final double OUTSIDE_DOMAIN_SCALE = 1.0e200;
	
	/*
	 * NOTE:  This class must be thread-safe!!!
	 */

	private final Simulation baseSimulation;
	private final OptimizableParameter parameter;
	private final OptimizationGoal goal;
	private final SimulationDomain domain;
	private final SimulationModifier[] modifiers;
	

	private final List<RocketOptimizationListener> listeners = new ArrayList<RocketOptimizationListener>();
	
	
	/**
	 * Sole constructor.
	 * <p>
	 * The dimensionality of the resulting function is the same as the length of the
	 * modifiers array.
	 * 
	 * @param baseSimulation	the base simulation to modify
	 * @param parameter			the rocket parameter to optimize
	 * @param goal				the goal of the rocket parameter
	 * @param modifiers			the modifiers that modify the simulation
	 */
	public RocketOptimizationFunction(Simulation baseSimulation, OptimizableParameter parameter,
			OptimizationGoal goal, SimulationDomain domain, SimulationModifier... modifiers) {
		this.baseSimulation = baseSimulation;
		this.parameter = parameter;
		this.goal = goal;
		this.domain = domain;
		this.modifiers = modifiers.clone();
		if (modifiers.length == 0) {
			throw new IllegalArgumentException("No SimulationModifiers specified");
		}
	}
	
	
	@Override
	public double evaluate(Point point) throws InterruptedException, OptimizationException {
		
		/*
		 * parameterValue is the computed parameter value (e.g. altitude)
		 * goalValue is the value that needs to be minimized
		 */
		double goalValue, parameterValue;
		
		log.debug("Computing optimization function value at point " + point);
		
		// Create the new simulation based on the point
		double[] p = point.asArray();
		if (p.length != modifiers.length) {
			throw new IllegalArgumentException("Point has length " + p.length + " while function has " +
					modifiers.length + " simulation modifiers");
		}
		
		Simulation simulation = newSimulationInstance(baseSimulation);
		for (int i = 0; i < modifiers.length; i++) {
			modifiers[i].modify(simulation, p[i]);
		}
		

		// Check whether the point is within the simulation domain
		Pair<Double, Value> d = domain.getDistanceToDomain(simulation);
		double distance = d.getU();
		Value referenceValue = d.getV();
		if (distance > 0 || Double.isNaN(distance)) {
			if (Double.isNaN(distance)) {
				goalValue = Double.MAX_VALUE;
			} else {
				goalValue = (distance + 1) * OUTSIDE_DOMAIN_SCALE;
			}
			log.debug("Optimization point is outside of domain, distance=" + distance + " goal function value=" + goalValue);
			
			fireEvent(simulation, point, referenceValue, null, goalValue);
			
			return goalValue;
		}
		

		// Compute the optimization value
		parameterValue = parameter.computeValue(simulation);
		goalValue = goal.getMinimizationParameter(parameterValue);
		
		if (Double.isNaN(goalValue)) {
			log.warn("Computed goal value was NaN, baseSimulation=" + baseSimulation + " parameter=" + parameter +
					" goal=" + goal + " modifiers=" + Arrays.toString(modifiers) + " simulation=" + simulation +
					" parameter value=" + parameterValue);
			goalValue = Double.MAX_VALUE;
		}
		
		log.trace("Parameter value at point " + point + " is " + parameterValue + ", goal function value=" + goalValue);
		
		fireEvent(simulation, point, referenceValue, new Value(parameterValue, parameter.getUnitGroup().getDefaultUnit()),
				goalValue);
		
		return goalValue;
	}
	
	



	/**
	 * Returns a new deep copy of the simulation and rocket.  This methods performs
	 * synchronization on the simulation for thread protection.
	 * <p>
	 * Note:  This method is package-private for unit testing purposes.
	 * 
	 * @return	a new deep copy of the simulation and rocket
	 */
	Simulation newSimulationInstance(Simulation simulation) {
		synchronized (baseSimulation) {
			Rocket newRocket = simulation.getRocket().copyWithOriginalID();
			Simulation newSimulation = simulation.duplicateSimulation(newRocket);
			return newSimulation;
		}
	}
	
	
	/**
	 * Add a listener to this function.  The listener will be notified each time the
	 * function is successfully evaluated.
	 * <p>
	 * Note that the listener may be called from other threads and must be thread-safe!
	 * 
	 * @param listener	the listener to add.
	 */
	public void addRocketOptimizationListener(RocketOptimizationListener listener) {
		listeners.add(listener);
	}
	
	public void removeRocketOptimizationListener(RocketOptimizationListener listener) {
		listeners.remove(listener);
	}
	
	

	private void fireEvent(Simulation simulation, Point p, Value domainReference, Value parameterValue, double goalValue)
			throws OptimizationException {
		
		if (listeners.isEmpty()) {
			return;
		}
		

		Value[] values = new Value[p.dim()];
		for (int i = 0; i < values.length; i++) {
			double value = modifiers[i].getCurrentSIValue(simulation);
			UnitGroup unit = modifiers[i].getUnitGroup();
			values[i] = new Value(value, unit.getDefaultUnit());
		}
		
		for (RocketOptimizationListener l : listeners) {
			l.evaluated(p, values, domainReference, parameterValue, goalValue);
		}
	}
}
