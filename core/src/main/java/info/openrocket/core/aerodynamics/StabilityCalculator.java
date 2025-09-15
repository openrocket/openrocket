package info.openrocket.core.aerodynamics;

import java.util.Map;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;

/**
 * Calculator responsible for the non-axial (stability) aerodynamic behaviour of a rocket.
 */
public interface StabilityCalculator {

	/**
	 * @return an independent instance of this stability calculator implementation.
	 */
	StabilityCalculator newInstance();

	/**
	 * Determine whether the current conditions are close to stall.
	 *
	 * @return positive value when not stalled, negative value when stalled
	 */
	double getStallMargin();

	/**
	 * Calculate the centre of pressure for the supplied configuration and conditions.
	 */
	Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions, WarningSet warnings);

	/**
	 * Calculate the non-axial force coefficients for the entire rocket.
	 */
	AerodynamicForces calculateNonAxialForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings);

	/**
	 * Produce per-component and per-assembly non-axial force information.
	 *
	 * @return a breakdown of the calculated forces
	 */
	StabilityForceBreakdown getForceAnalysis(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings);

	/**
	 * Apply pitch and yaw damping moments to the supplied force object.
	 */
	void calculateDampingMoments(FlightConfiguration configuration, FlightConditions conditions,
			AerodynamicForces total);

	/**
	 * Perform geometric checks for the supplied component tree.
	 */
	void checkGeometry(FlightConfiguration configuration, RocketComponent component, WarningSet warnings);

	/**
	 * Clear any cached data held by the calculator.
	 */
	void voidAerodynamicCache();
}
