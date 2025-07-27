package info.openrocket.core.models.atmosphere;

import info.openrocket.core.util.Monitorable;

/**
 * Defines the base interface for atmospheric models in OpenRocket.
 * An atmospheric model is responsible for providing atmospheric conditions
 * (temperature, pressure, and derived properties) at any given altitude.
 */
public interface AtmosphericModel extends Monitorable {

	/**
	 * Returns the atmospheric conditions at the specified altitude.
	 *
	 * @param altitude The altitude in meters above sea level (or launch site)
	 * @return AtmosphericConditions object containing the conditions at that altitude
	 */
	AtmosphericConditions getConditions(double altitude);

}
