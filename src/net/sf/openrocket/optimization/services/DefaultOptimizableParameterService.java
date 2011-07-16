package net.sf.openrocket.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.MaximumAltitudeParameter;

/**
 * Default implementation for optimization parameter service.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DefaultOptimizableParameterService implements OptimizableParameterService {
	
	@Override
	public Collection<OptimizableParameter> getParameters(OpenRocketDocument document) {
		List<OptimizableParameter> list = new ArrayList<OptimizableParameter>();
		
		list.add(new MaximumAltitudeParameter());
		
		return list;
	}
	
}
