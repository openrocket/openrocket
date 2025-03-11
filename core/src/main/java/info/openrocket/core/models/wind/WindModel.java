package info.openrocket.core.models.wind;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Monitorable;

public interface WindModel extends Monitorable, Cloneable, ChangeSource {
	/**
	 * The reference altitude that is used for wind-altitude relations in the wind model.
	 */
	enum AltitudeReference {
		MSL,		// Mean Sea Level
		AGL			// Above Ground Level (launch site)
	}


	/**
	 * Get the wind velocity at a given time and altitude.
	 * The wind model decides whether to use MSL or AGL altitude.
	 * @param time The time in seconds since the start of the simulation.
	 * @param altitudeMSL The altitude above mean sea level in meters.
	 * @param altitudeAGL The altitude above ground level in meters.
	 * @return The wind velocity in meters per second.
	 */
	Coordinate getWindVelocity(double time, double altitudeMSL, double altitudeAGL);

	/**
	 * Get the wind velocity at a given time and altitude.
	 * @param time The time in seconds since the start of the simulation.
	 * @param altitude The altitude in meters.
	 * @return The wind velocity in meters per second.
	 */
	Coordinate getWindVelocity(double time, double altitude);

	WindModel clone();
}
