package net.sf.openrocket.optimization.rocketoptimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifierService;

public class DefaultSimulationModifierService implements SimulationModifierService {
	
	@Override
	public Collection<SimulationModifier> getModifiers(OpenRocketDocument document) {
		// TODO: Should this really be OpenRocketDocument instead of Simulation?
		List<SimulationModifier> list = new ArrayList<SimulationModifier>();
		
		// TODO: implement
		

		return null;
	}
	
}
