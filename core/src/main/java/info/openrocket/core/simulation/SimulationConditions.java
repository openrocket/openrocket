package info.openrocket.core.simulation;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.models.atmosphere.AtmosphericModel;
import info.openrocket.core.models.gravity.GravityModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.WorldCoordinate;

/**
 * A holder class for the simulation conditions.  These include conditions that do not change
 * during the flight of a rocket, for example launch rod parameters, atmospheric models,
 * aerodynamic calculators etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationConditions implements Monitorable, Cloneable {
	
	private Simulation simulation; // The parent simulation 
	
	private double launchRodLength = 1;
	
	/** Launch rod angle >= 0, radians from vertical */
	private double launchRodAngle = 0;
	
	/** Launch rod direction, 0 = north */
	private double launchRodDirection = 0;
	
	// Launch site location (lat, lon, alt)
	private WorldCoordinate launchSite = new WorldCoordinate(0, 0, 0);
	
	// Launch location in simulation coordinates (normally always 0, air-start would override this)
	private Coordinate launchPosition = Coordinate.NUL;

	private Coordinate launchVelocity = Coordinate.NUL;

	private GeodeticComputationStrategy geodeticComputation = GeodeticComputationStrategy.SPHERICAL;

	private WindModel windModel;
	private AtmosphericModel atmosphericModel;
	private GravityModel gravityModel;

	private AerodynamicCalculator aerodynamicCalculator;
	private MassCalculator massCalculator;

	private double timeStep = RK4SimulationStepper.RECOMMENDED_TIME_STEP;
	private double maxSimulationTime = RK4SimulationStepper.RECOMMENDED_MAX_TIME;
	private double maximumAngleStep = RK4SimulationStepper.RECOMMENDED_ANGLE_STEP;


	private List<SimulationListener> simulationListeners = new ArrayList<>();

	private int randomSeed = 0;

	private ModID modID = ModID.INVALID;
	private ModID modIDadd = ModID.INVALID;

	public AerodynamicCalculator getAerodynamicCalculator() {
		return aerodynamicCalculator;
	}

	public void setAerodynamicCalculator(AerodynamicCalculator aerodynamicCalculator) {
		if (this.aerodynamicCalculator != null)
			this.modIDadd = new ModID();
		this.aerodynamicCalculator = aerodynamicCalculator;
	}

	public MassCalculator getMassCalculator() {
		return massCalculator;
	}

	public void setMassCalculator(MassCalculator massCalculator) {
		if (this.massCalculator != null)
			this.modIDadd = new ModID();
		this.massCalculator = massCalculator;
	}

	public Rocket getRocket() {
		return simulation.getRocket();
	}

	public FlightConfigurationId getMotorConfigurationID() {
		return simulation.getId();
	}

	public FlightConfigurationId getFlightConfigurationID() {
		return simulation.getId();
	}

	public double getLaunchRodLength() {
		return launchRodLength;
	}

	public void setLaunchRodLength(double launchRodLength) {
		this.launchRodLength = launchRodLength;
		this.modID = new ModID();
	}

	public double getLaunchRodAngle() {
		return launchRodAngle;
	}

	public void setLaunchRodAngle(double launchRodAngle) {
		this.launchRodAngle = launchRodAngle;
		this.modID = new ModID();
	}

	public double getLaunchRodDirection() {
		return launchRodDirection;
	}

	public void setLaunchRodDirection(double launchRodDirection) {
		this.launchRodDirection = launchRodDirection;
		this.modID = new ModID();
	}

	public WorldCoordinate getLaunchSite() {
		return this.launchSite;
	}

	public void setLaunchSite(WorldCoordinate site) {
		if (this.launchSite.equals(site))
			return;
		this.launchSite = site;
		this.modID = new ModID();
	}

	public Coordinate getLaunchPosition() {
		return launchPosition;
	}

	public void setLaunchPosition(Coordinate launchPosition) {
		if (this.launchPosition.equals(launchPosition))
			return;
		this.launchPosition = launchPosition;
		this.modID = new ModID();
	}

	public Coordinate getLaunchVelocity() {
		return launchVelocity;
	}

	public void setLaunchVelocity(Coordinate launchVelocity) {
		if (this.launchVelocity.equals(launchVelocity))
			return;
		this.launchVelocity = launchVelocity;
		this.modID = new ModID();
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
		this.modID = new ModID();
	}

	public WindModel getWindModel() {
		return windModel;
	}

	public void setWindModel(WindModel windModel) {
		if (this.windModel != null)
			this.modIDadd = new ModID();
		this.windModel = windModel;
	}

	public AtmosphericModel getAtmosphericModel() {
		return atmosphericModel;
	}

	public void setAtmosphericModel(AtmosphericModel atmosphericModel) {
		if (this.atmosphericModel != null)
			this.modIDadd = new ModID();
		this.atmosphericModel = atmosphericModel;
	}

	public GravityModel getGravityModel() {
		return gravityModel;
	}

	public void setGravityModel(GravityModel gravityModel) {
		this.modID = new ModID();
		this.gravityModel = gravityModel;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
		this.modID = new ModID();
	}

	public double getMaxSimulationTime() {
		return maxSimulationTime;
	}

	public void setMaxSimulationTime(double maxSimulationTime) {
		this.maxSimulationTime = maxSimulationTime;
		this.modID = new ModID();
	}

	public double getMaximumAngleStep() {
		return maximumAngleStep;
	}

	public void setMaximumAngleStep(double maximumAngle) {
		this.maximumAngleStep = maximumAngle;
		this.modID = new ModID();
	}

	public int getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
		this.modID = new ModID();
	}

	public void setSimulation(Simulation sim) {
		this.simulation = sim;
	}

	public Simulation getSimulation() {
		return this.simulation;
	}

	// TODO: HIGH: Make cleaner
	public List<SimulationListener> getSimulationListenerList() {
		return simulationListeners;
	}

	@Override
	public ModID getModID() {
		return modID;
	}

	@Override
	public SimulationConditions clone() {
		try {
			// TODO: HIGH: Deep clone models
			SimulationConditions clone = (SimulationConditions) super.clone();
			clone.simulationListeners = new ArrayList<>(this.simulationListeners.size());
			for (SimulationListener listener : this.simulationListeners) {
				clone.simulationListeners.add(listener.clone());
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}

}
