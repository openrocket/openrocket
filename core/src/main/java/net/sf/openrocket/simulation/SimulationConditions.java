package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.models.atmosphere.AtmosphericModel;
import net.sf.openrocket.models.gravity.GravityModel;
import net.sf.openrocket.models.wind.WindModel;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.GeodeticComputationStrategy;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.WorldCoordinate;

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
	private double maximumAngleStep = RK4SimulationStepper.RECOMMENDED_ANGLE_STEP;
	
	/* Whether to calculate additional data or only primary simulation figures */
	private boolean calculateExtras = true;
	
	
	private List<SimulationListener> simulationListeners = new ArrayList<SimulationListener>();
	
	
	private int randomSeed = 0;
	
	private int modID = 0;
	private int modIDadd = 0;
	
	
	
	
	public AerodynamicCalculator getAerodynamicCalculator() {
		return aerodynamicCalculator;
	}
	
	
	public void setAerodynamicCalculator(AerodynamicCalculator aerodynamicCalculator) {
		if (this.aerodynamicCalculator != null)
			this.modIDadd += this.aerodynamicCalculator.getModID();
		this.modID++;
		this.aerodynamicCalculator = aerodynamicCalculator;
	}
	
	public MassCalculator getMassCalculator() {
		return massCalculator;
	}
	
	
	public void setMassCalculator(MassCalculator massCalculator) {
		if (this.massCalculator != null)
			this.modIDadd += this.massCalculator.getModID();
		this.modID++;
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
		this.modID++;
	}
	
	
	public double getLaunchRodAngle() {
		return launchRodAngle;
	}
	
	
	public void setLaunchRodAngle(double launchRodAngle) {
		this.launchRodAngle = launchRodAngle;
		this.modID++;
	}
	
	
	public double getLaunchRodDirection() {
		return launchRodDirection;
	}
	
	
	public void setLaunchRodDirection(double launchRodDirection) {
		this.launchRodDirection = launchRodDirection;
		this.modID++;
	}
	
	
	public WorldCoordinate getLaunchSite() {
		return this.launchSite;
	}
	
	public void setLaunchSite(WorldCoordinate site) {
		if (this.launchSite.equals(site))
			return;
		this.launchSite = site;
		this.modID++;
	}
	
	
	public Coordinate getLaunchPosition() {
		return launchPosition;
	}
	
	public void setLaunchPosition(Coordinate launchPosition) {
		if (this.launchPosition.equals(launchPosition))
			return;
		this.launchPosition = launchPosition;
		this.modID++;
	}
	
	public Coordinate getLaunchVelocity() {
		return launchVelocity;
	}
	
	public void setLaunchVelocity(Coordinate launchVelocity) {
		if (this.launchVelocity.equals(launchVelocity))
			return;
		this.launchVelocity = launchVelocity;
		this.modID++;
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
		this.modID++;
	}
	
	
	public WindModel getWindModel() {
		return windModel;
	}
	
	
	public void setWindModel(WindModel windModel) {
		if (this.windModel != null)
			this.modIDadd += this.windModel.getModID();
		this.modID++;
		this.windModel = windModel;
	}
	
	
	public AtmosphericModel getAtmosphericModel() {
		return atmosphericModel;
	}
	
	
	public void setAtmosphericModel(AtmosphericModel atmosphericModel) {
		if (this.atmosphericModel != null)
			this.modIDadd += this.atmosphericModel.getModID();
		this.modID++;
		this.atmosphericModel = atmosphericModel;
	}
	
	
	public GravityModel getGravityModel() {
		return gravityModel;
	}
	
	
	public void setGravityModel(GravityModel gravityModel) {
		//if (this.gravityModel != null)
		//	this.modIDadd += this.gravityModel.getModID();
		this.modID++;
		this.gravityModel = gravityModel;
	}
	
	
	public double getTimeStep() {
		return timeStep;
	}
	
	
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
		this.modID++;
	}
	
	
	public double getMaximumAngleStep() {
		return maximumAngleStep;
	}
	
	
	public void setMaximumAngleStep(double maximumAngle) {
		this.maximumAngleStep = maximumAngle;
		this.modID++;
	}
	
	
	public boolean isCalculateExtras() {
		return calculateExtras;
	}
	
	
	public void setCalculateExtras(boolean calculateExtras) {
		this.calculateExtras = calculateExtras;
		this.modID++;
	}
	
	
	
	public int getRandomSeed() {
		return randomSeed;
	}
	
	
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
		this.modID++;
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
	public int getModID() {
		//return (modID + modIDadd + rocket.getModID() + windModel.getModID() + atmosphericModel.getModID() +
		//		gravityModel.getModID() + aerodynamicCalculator.getModID() + massCalculator.getModID());
		return (modID + modIDadd + simulation.getRocket().getModID() + windModel.getModID() + atmosphericModel.getModID() +
				aerodynamicCalculator.getModID() + massCalculator.getModID());
	}
	
	
	@Override
	public SimulationConditions clone() {
		try {
			// TODO: HIGH: Deep clone models
			SimulationConditions clone = (SimulationConditions) super.clone();
			clone.simulationListeners = new ArrayList<SimulationListener>(this.simulationListeners.size());
			for (SimulationListener listener : this.simulationListeners) {
				clone.simulationListeners.add(listener.clone());
			}
			
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}
	
}
