package info.openrocket.core.simulation;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Random;

import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.preferences.ApplicationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.models.atmosphere.AtmosphericModel;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.gravity.GravityModel;
import info.openrocket.core.models.gravity.WGSGravityModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.util.WorldCoordinate;

/**
 * A class holding simulation options in basic parameter form and which functions
 * as a ChangeSource.  A SimulationConditions instance is generated from this class
 * using {@link #toSimulationConditions()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationOptions implements ChangeSource, Cloneable, SimulationOptionsInterface {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SimulationOptions.class);

	public static final double MAX_LAUNCH_ROD_ANGLE = Math.PI / 3;

	/**
	 * The ISA standard atmosphere.
	 */
	private static final AtmosphericModel ISA_ATMOSPHERIC_MODEL = new ExtendedISAModel();

	protected final ApplicationPreferences preferences = Application.getPreferences();

	/*
	 * NOTE:  When adding/modifying parameters, they must also be added to the
	 * equals and copyFrom methods!!
	 */

	private double launchRodLength = preferences.getDouble(ApplicationPreferences.LAUNCH_ROD_LENGTH, 1);
	private boolean launchIntoWind = preferences.getBoolean(ApplicationPreferences.LAUNCH_INTO_WIND, true);
	private double launchRodAngle = preferences.getDouble(ApplicationPreferences.LAUNCH_ROD_ANGLE, 0);
	private double launchRodDirection = preferences.getDouble(ApplicationPreferences.LAUNCH_ROD_DIRECTION, Math.PI / 2);

	/*
	 * SimulationOptions maintains the launch site parameters as separate double values,
	 * and converts them into a WorldCoordinate when converting to SimulationConditions.
	 */
	
	private double launchAltitude = preferences.getLaunchAltitude();
	private double launchLatitude = preferences.getLaunchLatitude();
	private double launchLongitude = preferences.getLaunchLongitude();
	private GeodeticComputationStrategy geodeticComputation = GeodeticComputationStrategy.SPHERICAL;
	
	private boolean useISA = preferences.isISAAtmosphere();
	private double launchTemperature = preferences.getLaunchTemperature();	// In Kelvin
	private double launchPressure = preferences.getLaunchPressure();		// In Pascal
	
	private double timeStep = preferences.getTimeStep();
	private double maxSimulationTime = preferences.getMaxSimulationTime();
	private double maximumAngle = RK4SimulationStepper.RECOMMENDED_ANGLE_STEP;
	
	private int randomSeed = new Random().nextInt();

	private List<EventListener> listeners = new ArrayList<>();

	private WindModelType windModelType = WindModelType.AVERAGE;
	private PinkNoiseWindModel averageWindModel;
	private MultiLevelPinkNoiseWindModel multiLevelPinkNoiseWindModel;

	public SimulationOptions() {
		averageWindModel = new PinkNoiseWindModel(randomSeed);
		averageWindModel.addChangeListener(e -> fireChangeEvent());
		multiLevelPinkNoiseWindModel = new MultiLevelPinkNoiseWindModel();
		multiLevelPinkNoiseWindModel.addChangeListener(e -> fireChangeEvent());
	}

	public double getLaunchRodLength() {
		return launchRodLength;
	}

	public void setLaunchRodLength(double launchRodLength) {
		if (MathUtil.equals(this.launchRodLength, launchRodLength))
			return;
		this.launchRodLength = launchRodLength;
		fireChangeEvent();
	}

	public boolean getLaunchIntoWind() {
		return launchIntoWind;
	}

	public void setLaunchIntoWind(boolean i) {
		if (launchIntoWind == i)
			return;
		launchIntoWind = i;
		fireChangeEvent();
	}

	public double getLaunchRodAngle() {
		return launchRodAngle;
	}

	public void setLaunchRodAngle(double launchRodAngle) {
		launchRodAngle = MathUtil.clamp(launchRodAngle, -MAX_LAUNCH_ROD_ANGLE, MAX_LAUNCH_ROD_ANGLE);
		if (MathUtil.equals(this.launchRodAngle, launchRodAngle))
			return;
		this.launchRodAngle = launchRodAngle;
		fireChangeEvent();
	}

	public double getLaunchRodDirection() {
		if (launchIntoWind) {
			double windDirection;
			if (windModelType == WindModelType.AVERAGE) {
				windDirection = averageWindModel.getDirection();
			} else {
				windDirection = multiLevelPinkNoiseWindModel.getWindDirection(0, launchAltitude);
			}
			this.setLaunchRodDirection(windDirection);
		}
		return launchRodDirection;
	}

	public void setLaunchRodDirection(double launchRodDirection) {
		launchRodDirection = MathUtil.reduce2Pi(launchRodDirection);
		if (MathUtil.equals(this.launchRodDirection, launchRodDirection))
			return;
		this.launchRodDirection = launchRodDirection;
		fireChangeEvent();
	}

	public WindModelType getWindModelType() {
		return windModelType;
	}

	public void setWindModelType(WindModelType windModelType) {
		if (this.windModelType != windModelType) {
			this.windModelType = windModelType;
			fireChangeEvent();
		}
	}

	public WindModel getWindModel() {
		if (windModelType == WindModelType.AVERAGE) {
			return averageWindModel;
		} else if (windModelType == WindModelType.MULTI_LEVEL) {
			return multiLevelPinkNoiseWindModel;
		} else {
			throw new IllegalArgumentException("Unknown wind model type: " + windModelType);
		}
	}

	public PinkNoiseWindModel getAverageWindModel() {
		return averageWindModel;
	}

	public MultiLevelPinkNoiseWindModel getMultiLevelWindModel() {
		return multiLevelPinkNoiseWindModel;
	}

	// Deprecated wind methods to keep compatibility with old plugins (e.g. the original multi-level wind code)
	@Deprecated
	public double getWindSpeedAverage() {
		setWindModelType(WindModelType.AVERAGE);
		return averageWindModel.getAverage();
	}

	@Deprecated
	public void setWindSpeedAverage(double windAverage) {
		setWindModelType(WindModelType.AVERAGE);
		averageWindModel.setAverage(windAverage);
	}

	@Deprecated
	public double getWindSpeedDeviation() {
		setWindModelType(WindModelType.AVERAGE);
		return averageWindModel.getStandardDeviation();
	}

	@Deprecated
	public void setWindSpeedDeviation(double windDeviation) {
		setWindModelType(WindModelType.AVERAGE);
		averageWindModel.setStandardDeviation(windDeviation);
	}

	@Deprecated
	public double getWindTurbulenceIntensity() {
		setWindModelType(WindModelType.AVERAGE);
		return averageWindModel.getTurbulenceIntensity();
	}

	@Deprecated
	public void setWindTurbulenceIntensity(double intensity) {
		setWindModelType(WindModelType.AVERAGE);
		averageWindModel.setTurbulenceIntensity(intensity);
	}

	@Deprecated
	public double getWindDirection() {
		setWindModelType(WindModelType.AVERAGE);
		return averageWindModel.getDirection();
	}

	@Deprecated
	public void setWindDirection(double direction) {
		setWindModelType(WindModelType.AVERAGE);
		averageWindModel.setDirection(direction);
	}

	public double getLaunchAltitude() {
		return launchAltitude;
	}

	public void setLaunchAltitude(double altitude) {
		if (MathUtil.equals(this.launchAltitude, altitude))
			return;
		this.launchAltitude = MathUtil.min(altitude, ExtendedISAModel.getMaximumAllowedAltitude());

		// Update the launch temperature and pressure if using ISA
		if (useISA) {
			setLaunchTemperature(ISA_ATMOSPHERIC_MODEL.getConditions(getLaunchAltitude()).getTemperature());
			setLaunchPressure(ISA_ATMOSPHERIC_MODEL.getConditions(getLaunchAltitude()).getPressure());
		}

		fireChangeEvent();
	}

	public double getLaunchLatitude() {
		return launchLatitude;
	}

	public void setLaunchLatitude(double launchLatitude) {
		launchLatitude = MathUtil.clamp(launchLatitude, -90, 90);
		if (MathUtil.equals(this.launchLatitude, launchLatitude))
			return;
		this.launchLatitude = launchLatitude;
		fireChangeEvent();
	}

	public double getLaunchLongitude() {
		return launchLongitude;
	}

	public void setLaunchLongitude(double launchLongitude) {
		launchLongitude = MathUtil.clamp(launchLongitude, -180, 180);
		if (MathUtil.equals(this.launchLongitude, launchLongitude))
			return;
		this.launchLongitude = launchLongitude;
		fireChangeEvent();
	}

	public GeodeticComputationStrategy getGeodeticComputation() {
		return geodeticComputation;
	}

	public void setGeodeticComputation(GeodeticComputationStrategy geodeticComputation) {
		if (this.geodeticComputation == geodeticComputation)
			return;
		if (geodeticComputation == null) {
			throw new IllegalArgumentException("strategy cannot be null");
		}
		this.geodeticComputation = geodeticComputation;
		fireChangeEvent();
	}

	public boolean isISAAtmosphere() {
		return useISA;
	}

	public void setISAAtmosphere(boolean isa) {
		if (isa == useISA)
			return;
		useISA = isa;

		// Update the launch temperature and pressure
		if (isa) {
			setLaunchTemperature(ISA_ATMOSPHERIC_MODEL.getConditions(getLaunchAltitude()).getTemperature());
			setLaunchPressure(ISA_ATMOSPHERIC_MODEL.getConditions(getLaunchAltitude()).getPressure());
		}

		fireChangeEvent();
	}

	public double getLaunchTemperature() {
		return launchTemperature;
	}

	public void setLaunchTemperature(double launchTemperature) {
		if (MathUtil.equals(this.launchTemperature, launchTemperature))
			return;
		this.launchTemperature = launchTemperature;
		fireChangeEvent();
	}

	public double getLaunchPressure() {
		return launchPressure;
	}

	public void setLaunchPressure(double launchPressure) {
		if (MathUtil.equals(this.launchPressure, launchPressure))
			return;
		this.launchPressure = launchPressure;
		fireChangeEvent();
	}

	/**
	 * Returns an atmospheric model corresponding to the launch conditions. The
	 * atmospheric models may be shared between different calls.
	 *
	 * @return an AtmosphericModel object.
	 */
	private AtmosphericModel getAtmosphericModel() {
		if (useISA) {
			return ISA_ATMOSPHERIC_MODEL;
		}
		return new ExtendedISAModel(getLaunchAltitude(), launchTemperature, launchPressure);
	}

	public double getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(double timeStep) {
		if (MathUtil.equals(this.timeStep, timeStep))
			return;
		this.timeStep = timeStep;
		fireChangeEvent();
	}

	public double getMaxSimulationTime() {
		return maxSimulationTime;
	}

	public void setMaxSimulationTime(double maxSimulationTime) {
		if (MathUtil.equals(this.maxSimulationTime, maxSimulationTime))
			return;
		this.maxSimulationTime = maxSimulationTime;
		fireChangeEvent();
	}

	public double getMaximumStepAngle() {
		return maximumAngle;
	}

	public void setMaximumStepAngle(double maximumAngle) {
		maximumAngle = MathUtil.clamp(maximumAngle, 1 * Math.PI / 180, 20 * Math.PI / 180);
		if (MathUtil.equals(this.maximumAngle, maximumAngle))
			return;
		this.maximumAngle = maximumAngle;
		fireChangeEvent();
	}

	public int getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(int randomSeed) {
		if (this.randomSeed == randomSeed) {
			return;
		}
		this.randomSeed = randomSeed;
		/*
		 * This does not fire an event since we don't want to invalidate simulation
		 * results
		 * due to changing the seed value. This needs to be revisited if the user is
		 * ever
		 * allowed to select the seed value.
		 */
		// fireChangeEvent();
	}

	/**
	 * Randomize the random seed value.
	 */
	public void randomizeSeed() {
		this.randomSeed = new Random().nextInt();
		// fireChangeEvent();
	}

	@Override
	public SimulationOptions clone() {
		try {
			SimulationOptions copy = (SimulationOptions) super.clone();

			// Deep clone the wind models
			copy.averageWindModel = this.averageWindModel.clone();
			copy.multiLevelPinkNoiseWindModel = this.multiLevelPinkNoiseWindModel.clone();

			copy.windModelType = this.windModelType;

			// Create a new list for listeners
			copy.listeners = new ArrayList<>();

			return copy;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}

	public void copyConditionsFrom(SimulationOptions src) {
		// Be a little smart about triggering the change event.
		// only do it if one of the "important" (user specified) parameters has really
		// changed.
		boolean isChanged = false;

		if (this.windModelType != src.windModelType) {
			isChanged = true;
			this.windModelType = src.windModelType;
		}
		if (!this.averageWindModel.equals(src.averageWindModel)) {
			isChanged = true;
			this.averageWindModel.loadFrom(src.averageWindModel);
		}
		if (!this.multiLevelPinkNoiseWindModel.equals(src.multiLevelPinkNoiseWindModel)) {
			isChanged = true;
			this.multiLevelPinkNoiseWindModel.loadFrom(src.multiLevelPinkNoiseWindModel);
		}

		if (this.launchAltitude != src.launchAltitude) {
			isChanged = true;
			this.launchAltitude = src.launchAltitude;
		}
		if (this.launchLatitude != src.launchLatitude) {
			isChanged = true;
			this.launchLatitude = src.launchLatitude;
		}
		if (this.launchLongitude != src.launchLongitude) {
			isChanged = true;
			this.launchLongitude = src.launchLongitude;
		}
		if (this.launchRodAngle != src.launchRodAngle) {
			isChanged = true;
			this.launchRodAngle = src.launchRodAngle;
		}
		if (this.launchRodDirection != src.launchRodDirection) {
			isChanged = true;
			this.launchRodDirection = src.launchRodDirection;
		}
		if (this.launchRodLength != src.launchRodLength) {
			isChanged = true;
			this.launchRodLength = src.launchRodLength;
		}
		if (this.launchIntoWind != src.launchIntoWind) {
			isChanged = true;
			this.launchIntoWind = src.launchIntoWind;
		}
		if (this.useISA != src.useISA) {
			isChanged = true;
			this.useISA = src.useISA;
		}
		if (this.launchTemperature != src.launchTemperature) {
			isChanged = true;
			this.launchTemperature = src.launchTemperature;
		}
		if (this.launchPressure != src.launchPressure) {
			isChanged = true;
			this.launchPressure = src.launchPressure;
		}
		if (this.maximumAngle != src.maximumAngle) {
			isChanged = true;
			this.maximumAngle = src.maximumAngle;
		}

		if (this.timeStep != src.timeStep) {
			isChanged = true;
			this.timeStep = src.timeStep;
		}
		if (this.maxSimulationTime != src.maxSimulationTime) {
			isChanged = true;
			this.maxSimulationTime = src.maxSimulationTime;
		}
		if (this.geodeticComputation != src.geodeticComputation) {
			isChanged = true;
			this.geodeticComputation = src.geodeticComputation;
		}

		if (isChanged) {
			// Only copy the randomSeed if something else has changed.
			// Honestly, I don't really see a need for that.
			this.randomSeed = src.randomSeed;
			fireChangeEvent();
		}
	}

	/**
	 * Compares whether the two simulation conditions are equal. The two are
	 * considered
	 * equal if the rocket, motor id and all variables are equal.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SimulationOptions))
			return false;
		SimulationOptions o = (SimulationOptions) other;
		return (MathUtil.equals(this.launchAltitude, o.launchAltitude) &&
				MathUtil.equals(this.launchLatitude, o.launchLatitude) &&
				MathUtil.equals(this.launchLongitude, o.launchLongitude) &&
				MathUtil.equals(this.launchPressure, o.launchPressure) &&
				MathUtil.equals(this.launchRodAngle, o.launchRodAngle) &&
				MathUtil.equals(this.launchRodDirection, o.launchRodDirection) &&
				MathUtil.equals(this.launchRodLength, o.launchRodLength) &&
				MathUtil.equals(this.launchTemperature, o.launchTemperature) &&
				MathUtil.equals(this.maximumAngle, o.maximumAngle) &&
				MathUtil.equals(this.timeStep, o.timeStep) &&
				MathUtil.equals(this.maxSimulationTime, o.maxSimulationTime)) &&
				this.windModelType == o.windModelType &&
				this.averageWindModel.equals(o.averageWindModel) &&
				this.multiLevelPinkNoiseWindModel.equals(o.multiLevelPinkNoiseWindModel);
	}

	/**
	 * Hashcode method compatible with {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	public List<EventListener> getChangeListeners() {
		return listeners;
	}

	private final EventObject event = new EventObject(this);

	private void fireChangeEvent() {

		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}

	// TODO: HIGH: Clean up
	public SimulationConditions toSimulationConditions() {
		SimulationConditions conditions = new SimulationConditions();

		conditions.setLaunchRodLength(getLaunchRodLength());
		conditions.setLaunchRodAngle(getLaunchRodAngle());
		conditions.setLaunchRodDirection(getLaunchRodDirection());
		conditions.setLaunchSite(new WorldCoordinate(getLaunchLatitude(), getLaunchLongitude(), getLaunchAltitude()));
		conditions.setGeodeticComputation(getGeodeticComputation());
		conditions.setRandomSeed(randomSeed);

		WindModel windModel = getWindModel().clone();
		conditions.setWindModel(windModel);
		conditions.setAtmosphericModel(getAtmosphericModel());
		GravityModel gravityModel = new WGSGravityModel();
		conditions.setGravityModel(gravityModel);

		conditions.setAerodynamicCalculator(new BarrowmanCalculator());
		conditions.setMassCalculator(new MassCalculator());

		conditions.setTimeStep(getTimeStep());
		conditions.setMaxSimulationTime(getMaxSimulationTime());
		conditions.setMaximumAngleStep(getMaximumStepAngle());

		return conditions;
	}

	public String toString() {
		return "SimulationOptions [\n"
				.concat("    AtmosphericModel: " + getAtmosphericModel().toString() + "\n")
				.concat(String.format("    launchRodLength:  %f\n", launchRodLength))
				.concat(String.format("    launchIntoWind: %b\n", launchIntoWind))
				.concat(String.format("    launchRodAngle:  %f\n", launchRodAngle))
				.concat(String.format("    launchRodDirection:  %f\n", launchRodDirection))
				.concat(String.format("    windModelType: %s\n", windModelType))
				.concat(String.format("    pinkNoiseWindModel: %s\n", averageWindModel))
				.concat(String.format("    multiLevelPinkNoiseWindModel: %s\n", multiLevelPinkNoiseWindModel))
				.concat(String.format("    launchAltitude:  %f\n", launchAltitude))
				.concat(String.format("    launchLatitude:  %f\n", launchLatitude))
				.concat(String.format("    launchLongitude:  %f\n", launchLongitude))
				.concat("    geodeticComputation:  " + geodeticComputation.toString() + "\n")
				.concat(String.format("    useISA:  %b\n", useISA))
				.concat(String.format("    launchTemperature:  %f\n", launchTemperature))
				.concat(String.format("    launchPressure:  %f\n", launchPressure))
				.concat(String.format("    timeStep:  %f\n", timeStep))
				.concat(String.format("    maxTime:  %f\n", maxSimulationTime))
				.concat(String.format("    maximumAngle:  %f\n", maximumAngle))
				.concat("]\n");
	}

}
