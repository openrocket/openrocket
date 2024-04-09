package info.openrocket.core.optimization.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.optimization.rocketoptimization.OptimizableParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.DeploymentVelocityParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.GroundHitVelocityParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.LandingDistanceParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.MaximumAccelerationParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.MaximumAltitudeParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.MaximumVelocityParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.StabilityParameter;
import info.openrocket.core.optimization.rocketoptimization.parameters.TotalFlightTimeParameter;

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
