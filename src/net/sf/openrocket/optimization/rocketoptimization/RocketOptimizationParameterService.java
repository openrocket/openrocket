package net.sf.openrocket.optimization.rocketoptimization;

import java.util.Collection;

import net.sf.openrocket.document.OpenRocketDocument;

/**
 * A service for generating rocket optimization parameters.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface RocketOptimizationParameterService {
	
	/**
	 * Return all available rocket optimization parameters for this document.
	 * These should be new instances unless the parameter implementation is stateless.
	 * 
	 * @param document	the design document
	 * @return			a collection of the rocket optimization parameters.
	 */
	public Collection<RocketOptimizationParameter> getParameters(OpenRocketDocument document);
	
}
