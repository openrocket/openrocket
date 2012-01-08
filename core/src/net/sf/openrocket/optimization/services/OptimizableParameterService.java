package net.sf.openrocket.optimization.services;

import java.util.Collection;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;

/**
 * A service for generating rocket optimization parameters.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface OptimizableParameterService {
	
	/**
	 * Return all available rocket optimization parameters for this document.
	 * These should be new instances unless the parameter implementation is stateless.
	 * 
	 * @param document	the design document
	 * @return			a collection of the rocket optimization parameters.
	 */
	public Collection<OptimizableParameter> getParameters(OpenRocketDocument document);
	

}
