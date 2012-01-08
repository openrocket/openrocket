package net.sf.openrocket.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.TestRockets;

public final class OptimizationServiceHelper {
	
	private OptimizationServiceHelper() {
		// Prevent instantiation
	}
	
	/**
	 * Return the simulation modifiers for an OpenRocketDocument.  This queries the
	 * getModifiers() method from all available services and returns a collection of all
	 * the modifiers.
	 * 
	 * @param document	the OpenRocketDocument.
	 * @return			a collection of all simulation modifiers applicable to the document.
	 */
	public static Collection<SimulationModifier> getSimulationModifiers(OpenRocketDocument document) {
		List<SimulationModifier> list = new ArrayList<SimulationModifier>();
		
		ServiceLoader<SimulationModifierService> loader = ServiceLoader.load(SimulationModifierService.class);
		for (SimulationModifierService service : loader) {
			list.addAll(service.getModifiers(document));
		}
		
		return list;
	}
	
	

	/**
	 * Return the optimization parameters for an OpenRocketDocument.  This queries the
	 * getParameters() method from all available services and returns a collection of all
	 * the modifiers.
	 * 
	 * @param document	the OpenRocketDocument.
	 * @return			a collection of all optimization parameters applicable to the document.
	 */
	public static Collection<OptimizableParameter> getOptimizableParameters(OpenRocketDocument document) {
		List<OptimizableParameter> list = new ArrayList<OptimizableParameter>();
		
		ServiceLoader<OptimizableParameterService> loader = ServiceLoader.load(OptimizableParameterService.class);
		for (OptimizableParameterService service : loader) {
			list.addAll(service.getParameters(document));
		}
		
		return list;
	}
	
	
	public static void main(String[] args) {
		Rocket r = TestRockets.makeBigBlue();
		OpenRocketDocument document = new OpenRocketDocument(r);
		System.out.println("Simulation modifiers: " + getSimulationModifiers(document));
		System.out.println("Optimization parameters: " + getOptimizableParameters(document));
	}
}
