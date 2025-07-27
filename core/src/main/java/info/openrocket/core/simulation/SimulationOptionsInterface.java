package info.openrocket.core.simulation;

import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.GeodeticComputationStrategy;

public interface SimulationOptionsInterface extends ChangeSource {
	double getLaunchRodLength();

	void setLaunchRodLength(double launchRodLength);

	boolean getLaunchIntoWind();

	void setLaunchIntoWind(boolean i);

	double getLaunchRodAngle();

	void setLaunchRodAngle(double launchRodAngle);

	double getLaunchRodDirection();

	void setLaunchRodDirection(double launchRodDirection);

	PinkNoiseWindModel getAverageWindModel();

	double getLaunchAltitude();

	void setLaunchAltitude(double altitude);

	double getLaunchLatitude();

	void setLaunchLatitude(double launchLatitude);

	double getLaunchLongitude();

	void setLaunchLongitude(double launchLongitude);

	GeodeticComputationStrategy getGeodeticComputation();

	void setGeodeticComputation(GeodeticComputationStrategy geodeticComputation);

	boolean isISAAtmosphere();

	void setISAAtmosphere(boolean isa);

	/**
	 * Get the launch temperature in Kelvin at the launch site.
	 * @return the launch temperature in Kelvin
	 */
	double getLaunchTemperature();

	/**
	 * Set the launch temperature in Kelvin at the launch site.
	 * @param launchTemperature the launch temperature in Kelvin
	 */
	void setLaunchTemperature(double launchTemperature);

	/**
	 * Get the launch pressure in Pascals at the launch site.
	 * @return the launch pressure in Pascals
	 */
	double getLaunchPressure();

	/**
	 * Set the launch pressure in Pascals at the launch site.
	 * @param launchPressure the launch pressure in Pascals
	 */
	void setLaunchPressure(double launchPressure);

}
