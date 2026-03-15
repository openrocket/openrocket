package info.openrocket.core.aerodynamics;

import java.util.Map;

import info.openrocket.core.rocketcomponent.RocketComponent;

/**
 * Container for stability force data broken down by component and assembly.
 */
public class StabilityForceBreakdown {

	private final Map<RocketComponent, AerodynamicForces> componentForces;
	private final Map<RocketComponent, AerodynamicForces> assemblyForces;

	public StabilityForceBreakdown(Map<RocketComponent, AerodynamicForces> componentForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces) {
		this.componentForces = componentForces;
		this.assemblyForces = assemblyForces;
	}

	public Map<RocketComponent, AerodynamicForces> getComponentForces() {
		return componentForces;
	}

	public Map<RocketComponent, AerodynamicForces> getAssemblyForces() {
		return assemblyForces;
	}
}
