package info.openrocket.core.optimization.services;

import java.util.Collection;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.optimization.rocketoptimization.SimulationModifier;

/**
 * A service for generating simulation modifiers.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationModifierService {

	/**
	 * Return all available simulation modifiers for this document.
	 * 
	 * @param document the design document
	 * @return a collection of the rocket optimization parameters.
	 */
	public Collection<SimulationModifier> getModifiers(OpenRocketDocument document);

}
