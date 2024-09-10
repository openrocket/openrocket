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

	PinkNoiseWindModel getPinkNoiseWindModel();

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

	double getLaunchTemperature();

	void setLaunchTemperature(double launchTemperature);

	double getLaunchPressure();

	void setLaunchPressure(double launchPressure);

}
