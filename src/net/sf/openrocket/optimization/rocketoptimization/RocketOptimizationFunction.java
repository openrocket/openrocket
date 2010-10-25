package net.sf.openrocket.optimization.rocketoptimization;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.optimization.general.Function;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;

/**
 * A Function that optimizes a specific RocketOptimizationParameter to some goal
 * by modifying a base simulation using SimulationModifiers.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketOptimizationFunction implements Function {
	private static final LogHelper log = Application.getLogger();
	
	private static final double OUTSIDE_DOMAIN_SCALE = 1.0e200;
	
	/*
	 * NOTE:  This class must be thread-safe!!!
	 */

	private final Simulation baseSimulation;
	private final OptimizableParameter parameter;
	private final OptimizationGoal goal;
	private final SimulationDomain domain;
	private final SimulationModifier[] modifiers;
	
	private final Map<Point, Double> parameterValueCache = new ConcurrentHashMap<Point, Double>();
	private final Map<Point, Double> goalValueCache = new ConcurrentHashMap<Point, Double>();
	
	
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
		
		// Check for precomputed value
		Double d = goalValueCache.get(point);
		if (d != null && !Double.isNaN(d)) {
			log.verbose("Optimization function value at point " + point + " was found in cache: " + d);
			return d;
		}
		
		log.verbose("Computing optimization function value at point " + point);
		
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
		double distance = domain.getDistanceToDomain(simulation);
		if (distance > 0 || Double.isNaN(distance)) {
			if (Double.isNaN(distance)) {
				goalValue = Double.MAX_VALUE;
			} else {
				goalValue = (distance + 1) * OUTSIDE_DOMAIN_SCALE;
			}
			parameterValueCache.put(point, Double.NaN);
			goalValueCache.put(point, goalValue);
			log.verbose("Optimization point is outside of domain, distance=" + distance + " goal function value=" + goalValue);
			return goalValue;
		}
		

		// Compute the optimization value
		parameterValue = parameter.computeValue(simulation);
		parameterValueCache.put(point, parameterValue);
		
		goalValue = goal.getMinimizationParameter(parameterValue);
		if (Double.isNaN(goalValue)) {
			log.warn("Computed goal value was NaN, baseSimulation=" + baseSimulation + " parameter=" + parameter +
					" goal=" + goal + " modifiers=" + Arrays.toString(modifiers) + " simulation=" + simulation +
					" parameter value=" + parameterValue);
			goalValue = Double.MAX_VALUE;
		}
		goalValueCache.put(point, goalValue);
		
		log.verbose("Parameter value at point " + point + " is " + goalValue + ", goal function value=" + goalValue);
		
		return goalValue;
	}
	
	


	/**
	 * Return the parameter value at a point that has been computed.  The purpose is
	 * to allow retrieving the parameter value corresponding to the found minimum value.
	 * 
	 * @param point		the point to use.
	 * @return			the parameter value at that point, or NaN if the value at this point has not been computed.
	 */
	public double getComputedParameterValue(Point point) {
		Double value = parameterValueCache.get(point);
		if (value != null) {
			return value;
		} else {
			return Double.NaN;
		}
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
			Rocket newRocket = (Rocket) simulation.getRocket().copy();
			Simulation newSimulation = simulation.duplicateSimulation(newRocket);
			return newSimulation;
		}
	}
	
}
