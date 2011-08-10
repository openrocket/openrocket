package net.sf.openrocket.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.DeploymentVelocityParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.GroundHitVelocityParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.LandingDistanceParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.MaximumAccelerationParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.MaximumAltitudeParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.MaximumVelocityParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.StabilityParameter;
import net.sf.openrocket.optimization.rocketoptimization.parameters.TotalFlightTimeParameter;

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
		list.add(new MaximumVelocityParameter());
		list.add(new MaximumAccelerationParameter());
		list.add(new StabilityParameter(false));
		list.add(new StabilityParameter(true));
		list.add(new GroundHitVelocityParameter());
		list.add(new LandingDistanceParameter());
		list.add(new TotalFlightTimeParameter());
		list.add(new DeploymentVelocityParameter());
		
		return list;
	}
	
}
