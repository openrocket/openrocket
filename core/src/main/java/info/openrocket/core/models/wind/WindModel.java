package info.openrocket.core.models.wind;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.CoordinateIF;
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
	CoordinateIF getWindVelocity(double time, double altitudeMSL, double altitudeAGL);

	/**
	 * Get the wind velocity at a given time and altitude.
	 * @param time The time in seconds since the start of the simulation.
	 * @param altitude The altitude in meters.
	 * @return The wind velocity in meters per second.
	 */
	CoordinateIF getWindVelocity(double time, double altitude);

	/**
	 * Set the seed of the model's random source, so that a simulation run with a
	 * given seed reproduces the same wind.
	 * <p>
	 * This discards any random state already generated, so it must only be called
	 * while configuring a simulation and never from within a running one: reseeding
	 * mid-flight would change the wind partway through.
	 *
	 * @param seed the seed value.
	 */
	void setSeed(int seed);

	WindModel clone();
}
