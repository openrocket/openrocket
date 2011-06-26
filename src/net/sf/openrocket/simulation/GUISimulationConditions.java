package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.models.atmosphere.AtmosphericModel;
import net.sf.openrocket.models.atmosphere.ExtendedISAModel;
import net.sf.openrocket.models.gravity.BasicGravityModel;
import net.sf.openrocket.models.wind.PinkNoiseWindModel;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.MathUtil;

// TODO: HIGH: Move somewhere else and clean up
@Deprecated
public class GUISimulationConditions implements ChangeSource, Cloneable {
	
	public static final double MAX_LAUNCH_ROD_ANGLE = Math.PI / 3;
	
	/**
	 * The ISA standard atmosphere.
	 */
	private static final AtmosphericModel ISA_ATMOSPHERIC_MODEL = new ExtendedISAModel();
	

	private final Rocket rocket;
	private String motorID = null;
	

	/*
	 * NOTE:  When adding/modifying parameters, they must also be added to the
	 * equals and copyFrom methods!!
	 */

	// TODO: HIGH: Fetch default values from Prefs!
	
	private double launchRodLength = 1;
	
	/** Launch rod angle > 0, radians from vertical */
	private double launchRodAngle = 0;
	
	/** Launch rod direction, 0 = upwind, PI = downwind. */
	private double launchRodDirection = 0;
	

	private double windAverage = 2.0;
	private double windTurbulence = 0.1;
	
	private double launchAltitude = 0;
	private double launchLatitude = 45;
	
	private boolean useISA = true;
	private double launchTemperature = ExtendedISAModel.STANDARD_TEMPERATURE;
	private double launchPressure = ExtendedISAModel.STANDARD_PRESSURE;
	private AtmosphericModel atmosphericModel = null;
	

	private double timeStep = RK4SimulationStepper.RECOMMENDED_TIME_STEP;
	private double maximumAngle = RK4SimulationStepper.RECOMMENDED_ANGLE_STEP;
	
	private boolean calculateExtras = true;
	

	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	

	public GUISimulationConditions(Rocket rocket) {
		this.rocket = rocket;
	}
	
	

	public Rocket getRocket() {
		return rocket;
	}
	
	
	public String getMotorConfigurationID() {
		return motorID;
	}
	
	/**
	 * Set the motor configuration ID.  This must be a valid motor configuration ID of
	 * the rocket, otherwise the configuration is set to <code>null</code>.
	 * 
	 * @param id	the configuration to set.
	 */
	public void setMotorConfigurationID(String id) {
		if (id != null)
			id = id.intern();
		if (!rocket.isMotorConfigurationID(id))
			id = null;
		if (id == motorID)
			return;
		motorID = id;
		fireChangeEvent();
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
	
	
	public double getLaunchRodAngle() {
		return launchRodAngle;
	}
	
	public void setLaunchRodAngle(double launchRodAngle) {
		launchRodAngle = MathUtil.clamp(launchRodAngle, 0, MAX_LAUNCH_ROD_ANGLE);
		if (MathUtil.equals(this.launchRodAngle, launchRodAngle))
			return;
		this.launchRodAngle = launchRodAngle;
		fireChangeEvent();
	}
	
	
	public double getLaunchRodDirection() {
		return launchRodDirection;
	}
	
	public void setLaunchRodDirection(double launchRodDirection) {
		launchRodDirection = MathUtil.reduce180(launchRodDirection);
		if (MathUtil.equals(this.launchRodDirection, launchRodDirection))
			return;
		this.launchRodDirection = launchRodDirection;
		fireChangeEvent();
	}
	
	

	public double getWindSpeedAverage() {
		return windAverage;
	}
	
	public void setWindSpeedAverage(double windAverage) {
		if (MathUtil.equals(this.windAverage, windAverage))
			return;
		this.windAverage = MathUtil.max(windAverage, 0);
		fireChangeEvent();
	}
	
	
	public double getWindSpeedDeviation() {
		return windAverage * windTurbulence;
	}
	
	public void setWindSpeedDeviation(double windDeviation) {
		if (windAverage < 0.1) {
			windAverage = 0.1;
		}
		setWindTurbulenceIntensity(windDeviation / windAverage);
	}
	
	
	/**
	 * Return the wind turbulence intensity (standard deviation / average).
	 * 
	 * @return  the turbulence intensity
	 */
	public double getWindTurbulenceIntensity() {
		return windTurbulence;
	}
	
	/**
	 * Set the wind standard deviation to match the given turbulence intensity.
	 * 
	 * @param intensity   the turbulence intensity
	 */
	public void setWindTurbulenceIntensity(double intensity) {
		// Does not check equality so that setWindSpeedDeviation can be sure of event firing
		this.windTurbulence = intensity;
		fireChangeEvent();
	}
	
	



	public double getLaunchAltitude() {
		return launchAltitude;
	}
	
	public void setLaunchAltitude(double altitude) {
		if (MathUtil.equals(this.launchAltitude, altitude))
			return;
		this.launchAltitude = altitude;
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
	
	



	public boolean isISAAtmosphere() {
		return useISA;
	}
	
	public void setISAAtmosphere(boolean isa) {
		if (isa == useISA)
			return;
		useISA = isa;
		fireChangeEvent();
	}
	
	
	public double getLaunchTemperature() {
		return launchTemperature;
	}
	
	

	public void setLaunchTemperature(double launchTemperature) {
		if (MathUtil.equals(this.launchTemperature, launchTemperature))
			return;
		this.launchTemperature = launchTemperature;
		this.atmosphericModel = null;
		fireChangeEvent();
	}
	
	

	public double getLaunchPressure() {
		return launchPressure;
	}
	
	

	public void setLaunchPressure(double launchPressure) {
		if (MathUtil.equals(this.launchPressure, launchPressure))
			return;
		this.launchPressure = launchPressure;
		this.atmosphericModel = null;
		fireChangeEvent();
	}
	
	
	/**
	 * Returns an atmospheric model corresponding to the launch conditions.  The
	 * atmospheric models may be shared between different calls.
	 * 
	 * @return	an AtmosphericModel object.
	 */
	public AtmosphericModel getAtmosphericModel() {
		if (useISA) {
			return ISA_ATMOSPHERIC_MODEL;
		}
		if (atmosphericModel == null) {
			atmosphericModel = new ExtendedISAModel(launchAltitude,
					launchTemperature, launchPressure);
		}
		return atmosphericModel;
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
	
	

	public boolean getCalculateExtras() {
		return calculateExtras;
	}
	
	

	public void setCalculateExtras(boolean calculateExtras) {
		if (this.calculateExtras == calculateExtras)
			return;
		this.calculateExtras = calculateExtras;
		fireChangeEvent();
	}
	
	

	@Override
	public GUISimulationConditions clone() {
		try {
			GUISimulationConditions copy = (GUISimulationConditions) super.clone();
			copy.listeners = new ArrayList<ChangeListener>();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}
	
	
	public void copyFrom(GUISimulationConditions src) {
		
		if (this.rocket == src.rocket) {
			
			this.motorID = src.motorID;
			
		} else {
			
			if (src.rocket.hasMotors(src.motorID)) {
				// Try to find a matching motor ID
				String motorDesc = src.rocket.getMotorConfigurationDescription(src.motorID);
				String matchID = null;
				
				for (String id : this.rocket.getMotorConfigurationIDs()) {
					if (motorDesc.equals(this.rocket.getMotorConfigurationDescription(id))) {
						matchID = id;
						break;
					}
				}
				
				this.motorID = matchID;
			} else {
				this.motorID = null;
			}
		}
		
		this.launchAltitude = src.launchAltitude;
		this.launchLatitude = src.launchLatitude;
		this.launchPressure = src.launchPressure;
		this.launchRodAngle = src.launchRodAngle;
		this.launchRodDirection = src.launchRodDirection;
		this.launchRodLength = src.launchRodLength;
		this.launchTemperature = src.launchTemperature;
		this.maximumAngle = src.maximumAngle;
		this.timeStep = src.timeStep;
		this.windAverage = src.windAverage;
		this.windTurbulence = src.windTurbulence;
		this.calculateExtras = src.calculateExtras;
		
		fireChangeEvent();
	}
	
	

	/**
	 * Compares whether the two simulation conditions are equal.  The two are considered
	 * equal if the rocket, motor id and all variables are equal.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GUISimulationConditions))
			return false;
		GUISimulationConditions o = (GUISimulationConditions) other;
		return ((this.rocket == o.rocket) &&
				this.motorID == o.motorID &&
				MathUtil.equals(this.launchAltitude, o.launchAltitude) &&
				MathUtil.equals(this.launchLatitude, o.launchLatitude) &&
				MathUtil.equals(this.launchPressure, o.launchPressure) &&
				MathUtil.equals(this.launchRodAngle, o.launchRodAngle) &&
				MathUtil.equals(this.launchRodDirection, o.launchRodDirection) &&
				MathUtil.equals(this.launchRodLength, o.launchRodLength) &&
				MathUtil.equals(this.launchTemperature, o.launchTemperature) &&
				MathUtil.equals(this.maximumAngle, o.maximumAngle) &&
				MathUtil.equals(this.timeStep, o.timeStep) &&
				MathUtil.equals(this.windAverage, o.windAverage) &&
				MathUtil.equals(this.windTurbulence, o.windTurbulence) && this.calculateExtras == o.calculateExtras);
	}
	
	/**
	 * Hashcode method compatible with {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		if (motorID == null)
			return rocket.hashCode();
		return rocket.hashCode() + motorID.hashCode();
	}
	
	@Override
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}
	
	private final ChangeEvent event = new ChangeEvent(this);
	
	private void fireChangeEvent() {
		ChangeListener[] array = listeners.toArray(new ChangeListener[0]);
		
		for (int i = array.length - 1; i >= 0; i--) {
			array[i].stateChanged(event);
		}
	}
	
	
	// TODO: HIGH: Clean up
	@Deprecated
	public SimulationConditions toSimulationConditions() {
		SimulationConditions conditions = new SimulationConditions();
		
		conditions.setRocket((Rocket) getRocket().copy());
		conditions.setMotorConfigurationID(getMotorConfigurationID());
		conditions.setLaunchRodLength(getLaunchRodLength());
		conditions.setLaunchRodAngle(getLaunchRodAngle());
		conditions.setLaunchRodDirection(getLaunchRodDirection());
		conditions.setLaunchAltitude(getLaunchAltitude());
		conditions.setLaunchLatitude(getLaunchLatitude());
		
		PinkNoiseWindModel windModel = new PinkNoiseWindModel();
		windModel.setAverage(getWindSpeedAverage());
		windModel.setStandardDeviation(getWindSpeedDeviation());
		conditions.setWindModel(windModel);
		
		conditions.setAtmosphericModel(getAtmosphericModel());
		
		BasicGravityModel gravityModel = new BasicGravityModel(getLaunchLatitude());
		conditions.setGravityModel(gravityModel);
		
		conditions.setAerodynamicCalculator(new BarrowmanCalculator());
		conditions.setMassCalculator(new BasicMassCalculator());
		
		conditions.setTimeStep(getTimeStep());
		conditions.setMaximumAngleStep(getMaximumStepAngle());
		
		conditions.setCalculateExtras(getCalculateExtras());
		
		return conditions;
	}
	
}
