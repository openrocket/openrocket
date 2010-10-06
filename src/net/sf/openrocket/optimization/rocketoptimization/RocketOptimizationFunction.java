package net.sf.openrocket.optimization.rocketoptimization;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.optimization.general.Function;
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
	
	/*
	 * NOTE:  This class must be thread-safe!!!
	 */

	private final Simulation baseSimulation;
	private final RocketOptimizationParameter parameter;
	private final OptimizationGoal goal;
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
	public RocketOptimizationFunction(Simulation baseSimulation, RocketOptimizationParameter parameter,
			OptimizationGoal goal, SimulationModifier... modifiers) {
		this.baseSimulation = baseSimulation;
		this.parameter = parameter;
		this.goal = goal;
		this.modifiers = modifiers.clone();
		if (modifiers.length == 0) {
			throw new IllegalArgumentException("No SimulationModifiers specified");
		}
	}
	
	
	@Override
	public double evaluate(Point point) throws InterruptedException {
		
		// Check for precomputed value
		double value = preComputed(point);
		if (!Double.isNaN(value)) {
			return value;
		}
		
		// Create the new simulation based on the point
		double[] p = point.asArray();
		if (p.length != modifiers.length) {
			throw new IllegalArgumentException("Point has length " + p.length + " while function has " +
					modifiers.length + " simulation modifiers");
		}
		Simulation simulation = newSimulationInstance();
		for (int i = 0; i < modifiers.length; i++) {
			modifiers[i].modify(simulation, p[i]);
		}
		
		// Compute the optimization value
		value = parameter.computeValue(simulation);
		parameterValueCache.put(point, value);
		
		value = goal.getMinimizationParameter(value);
		if (Double.isNaN(value)) {
			log.warn("Computed value was NaN, baseSimulation=" + baseSimulation + " parameter=" + parameter +
					" goal=" + goal + " modifiers=" + Arrays.toString(modifiers) + " simulation=" + simulation);
			value = Double.MAX_VALUE;
		}
		goalValueCache.put(point, value);
		
		return value;
	}
	
	@Override
	public double preComputed(Point point) {
		Double value = goalValueCache.get(point);
		if (value != null) {
			return value;
		}
		
		// TODO: : is in domain?
		return 0;
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
	 * 
	 * @return
	 */
	private Simulation newSimulationInstance() {
		synchronized (baseSimulation) {
			Rocket newRocket = (Rocket) baseSimulation.getRocket().copy();
			Simulation newSimulation = baseSimulation.duplicateSimulation(newRocket);
			return newSimulation;
		}
	}
	
}
