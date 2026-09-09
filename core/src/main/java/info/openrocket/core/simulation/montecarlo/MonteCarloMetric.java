package info.openrocket.core.simulation.montecarlo;

import info.openrocket.core.unit.UnitGroup;

/**
 * Scalar flight outputs retained for every Monte Carlo flight-data branch.
 * Values use OpenRocket's internal SI units.
 */
public enum MonteCarloMetric {
	APOGEE_ALTITUDE(UnitGroup.UNITS_DISTANCE),
	MAXIMUM_VELOCITY(UnitGroup.UNITS_VELOCITY),
	MAXIMUM_MACH(UnitGroup.UNITS_COEFFICIENT),
	MAXIMUM_ACCELERATION(UnitGroup.UNITS_ACCELERATION),
	TIME_TO_APOGEE(UnitGroup.UNITS_LONG_TIME),
	FLIGHT_TIME(UnitGroup.UNITS_LONG_TIME),
	LANDING_VELOCITY(UnitGroup.UNITS_VELOCITY);

	private final UnitGroup unitGroup;

	MonteCarloMetric(UnitGroup unitGroup) {
		this.unitGroup = unitGroup;
	}

	public UnitGroup getUnitGroup() {
		return unitGroup;
	}
}
