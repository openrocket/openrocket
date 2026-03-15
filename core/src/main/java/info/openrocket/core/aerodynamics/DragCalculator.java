package info.openrocket.core.aerodynamics;

import java.util.Map;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;

/**
 * Calculator responsible for determining the axial drag contributions of a rocket.
 */
public interface DragCalculator {

	/**
	 * @return an independent instance of this drag calculator implementation.
	 */
	DragCalculator newInstance();

	/**
	 * Calculate drag contributions for the supplied configuration.
	 *
	 * @param configuration the rocket configuration being analysed
	 * @param conditions    the current flight conditions
	 * @param componentForces per-component force data, may be {@code null}
	 * @param assemblyForces per-assembly force data, may be {@code null}
	 * @param totalForces   the overall forces object for the rocket
	 * @param warnings      warning sink, may be {@code null}
	 */
	void calculateDrag(FlightConfiguration configuration,
			FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> componentForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces,
			AerodynamicForces totalForces,
			WarningSet warnings);

	/**
	 * Convert a drag coefficient to an axial drag coefficient for the supplied conditions.
	 */
	double toAxialDrag(FlightConditions conditions, double cd);

	/**
	 * Clear any cached data held by the calculator.
	 */
	void voidAerodynamicCache();
}
