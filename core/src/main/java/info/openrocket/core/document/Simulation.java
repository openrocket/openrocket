package info.openrocket.core.document;

import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import info.openrocket.core.simulation.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.BasicEventSimulationEngine;
import info.openrocket.core.simulation.DefaultSimulationOptionFactory;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.RK4SimulationStepper;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationEngine;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.SimulationStepper;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.SafetyMutex;
import info.openrocket.core.util.StateChangeListener;

/**
 * A class defining a simulation, its conditions and simulated data.
 * <p>
 * This class is not thread-safe and enforces single-threaded access with a
 * SafetyMutex.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Simulation implements ChangeSource, Cloneable {
	private static final Logger log = LoggerFactory.getLogger(Simulation.class);
	
	public static enum Status {
		/** Up-to-date */
		UPTODATE,
		
		/** Loaded from file, status probably up-to-date */
		LOADED,
		
		/** Data outdated */
		OUTDATED,
		
		/** Imported external data */
		EXTERNAL,
		
		/** Not yet simulated */
		NOT_SIMULATED,
		
		/** Can't be simulated, NO_MOTORS **/
		CANT_RUN
	}
	
	private final RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	private SafetyMutex mutex = SafetyMutex.newInstance();

	private final OpenRocketDocument document;
	private final Rocket rocket;
	FlightConfigurationId configId = FlightConfigurationId.ERROR_FCID;
	
	private String name = "";
	
	private Status status;
	
	/** The conditions to use */
	// TODO: HIGH: Change to use actual conditions class??
	private SimulationOptions options = new SimulationOptions();
	
	private ArrayList<SimulationExtension> simulationExtensions = new ArrayList<SimulationExtension>();
	
	
	private final Class<? extends SimulationEngine> simulationEngineClass = BasicEventSimulationEngine.class;
	private Class<? extends SimulationStepper> simulationStepperClass = RK4SimulationStepper.class;
	private Class<? extends AerodynamicCalculator> aerodynamicCalculatorClass = BarrowmanCalculator.class;
	@SuppressWarnings("unused")
	private final Class<? extends MassCalculator> massCalculatorClass = MassCalculator.class;
	
	/** Listeners for this object */
	private List<EventListener> listeners = new ArrayList<EventListener>();
	
	
	/** The conditions actually used in the previous simulation, or null */
	private SimulationOptions simulatedConditions = null;
	private String simulatedConfigurationDescription = null;
	private FlightData simulatedData = null;
	private int simulatedConfigurationID = -1;

	/**
	 * Create a new simulation for the rocket. Parent document should also be provided.
	 * The initial motor configuration is taken from the default rocket configuration.
	 *
	 * @param rocket the rocket associated with the simulation.
	 */
	public Simulation(OpenRocketDocument document, Rocket rocket) {
		this.document = document;
		this.rocket = rocket;
		this.status = Status.NOT_SIMULATED;

		DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
		options.copyConditionsFrom(f.getDefault());

		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
		setFlightConfigurationId(fcid);

		options.addChangeListener(new ConditionListener());
		addChangeListener(document);
	}

	/**
	 * Create a new simulation for the rocket. Parent document should also be provided.
	 * The initial motor configuration is taken from the default rocket configuration.
	 *
	 * @param rocket the rocket associated with the simulation.
	 */
	public Simulation(Rocket rocket) {
		this(null, rocket);
	}

	public Simulation(OpenRocketDocument document, Rocket rocket, Status status, String name, SimulationOptions options,
			List<SimulationExtension> extensions, FlightData data) {

		if (rocket == null)
			throw new IllegalArgumentException("rocket cannot be null");
		if (status == null)
			throw new IllegalArgumentException("status cannot be null");
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");
		if (options == null)
			throw new IllegalArgumentException("options cannot be null");

		this.rocket = rocket;
		this.name = name;
		this.status = status;
		this.simulatedConditions = options.clone();
		this.simulatedData = data;
		this.document = document;
		addChangeListener(this.document);

		this.options = options;
		this.options.addChangeListener(new ConditionListener());

		final FlightConfiguration config = rocket.getSelectedConfiguration();
		this.setFlightConfigurationId(config.getFlightConfigurationID());
		this.simulatedConfigurationID = config.getModID();

		this.simulationExtensions.addAll(extensions);
	}

	public FlightConfiguration getActiveConfiguration() {
		mutex.verify();
		return rocket.getFlightConfiguration(this.configId);
	}

	/**
	 * Return the rocket associated with this simulation.
	 *
	 * @return the rocket.
	 */
	public Rocket getRocket() {
		mutex.verify();
		return rocket;
	}

	public FlightConfigurationId getFlightConfigurationId() {
		return this.configId;
	}

	public FlightConfigurationId getId() {
		return this.getFlightConfigurationId();
	}

	/**
	 * Set the motor configuration ID.  If this id does not yet exist, it will be created.
	 * 
	 * @param fcid the configuration to set.
	 */
	public void setFlightConfigurationId(FlightConfigurationId fcid) {
		if (null == fcid) {
			throw new NullPointerException("Attempted to set a null Config id in simulation options. Not allowed!");
		} else if (fcid.hasError()) {
			throw new IllegalArgumentException("Attempted to set the configuration to an error id. Not Allowed!");
		} else if (!rocket.containsFlightConfigurationID(fcid)) {
			rocket.createFlightConfiguration(fcid);
		}

		if (fcid.equals(this.configId)) {
			return;
		}
		
		this.configId = fcid;
		fireChangeEvent();
	}

	/**
	 * Applies the simulation options to the simulation.
	 *
	 * @param options the simulation options to apply.
	 */
	public void copySimulationOptionsFrom(SimulationOptions options) {
		this.options.copyConditionsFrom(options);
	}
	
//	/**
//	 * Return a newly created Configuration for this simulation.  The configuration
//	 * has the motor ID set and all stages active.
//	 *
//	 * @return	a newly created Configuration of the launch conditions.
//	 */
//	public FlightConfiguration getConfiguration() {
//		mutex.verify();
//		FlightConfiguration c = rocket.getDefaultConfiguration().clone();
//		c.setFlightConfigurationID(options.getConfigID());
//		c.setAllStages();
//		return c;
//	}
//	
//	
//	/**
//	 * Return a newly created Configuration for this simulation.  The configuration
//	 * has the motor ID set and all stages active.
//	 *
//	 * @return	a newly created Configuration of the launch conditions.
//	 */
//	public FlightConfiguration setConfiguration( final FlightConfiguration fc ) {
//		mutex.verify();
//		//FlightConfiguration c = rocket.getDefaultConfiguration().clone();
//		//c.setFlightConfigurationID(options.getConfigID());
//		//c.setAllStages();
//		//return c;
//	}
	
	/**
	 * Returns the simulation options attached to this simulation.  The options
	 * may be modified freely, and the status of the simulation will change to reflect
	 * the changes.
	 *
	 * @return the simulation conditions.
	 */
	public SimulationOptions getOptions() {
		mutex.verify();
		return options;
	}
	
	
	/**
	 * Get the list of simulation extensions.  The returned list is the one used by
	 * this object; changes to it will reflect changes in the simulation.
	 *
	 * @return	the actual list of simulation extensions.
	 */
	public List<SimulationExtension> getSimulationExtensions() {
		mutex.verify();
		return simulationExtensions;
	}

	/**
	 * Applies the simulation extensions to the simulation.
	 *
	 * @param extensions the simulation extensions to apply.
	 */
	public void copyExtensionsFrom(List<SimulationExtension> extensions) {
		if (extensions == null) {
			return;
		}
		this.simulationExtensions.clear();
		this.simulationExtensions.addAll(extensions);
	}
	
	
	/**
	 * Return the user-defined name of the simulation.
	 *
	 * @return	the name for the simulation.
	 */
	public String getName() {
		mutex.verify();
		return name;
	}
	
	/**
	 * Set the user-defined name of the simulation.  Setting the name to
	 * null yields an empty name.
	 *
	 * @param name	the name of the simulation.
	 */
	public void setName(String name) {
		mutex.lock("setName");
		try {
			if (this.name.equals(name))
				return;
			
			if (name == null)
				this.name = "";
			else
				this.name = name;
			
			fireChangeEvent();
		} finally {
			mutex.unlock("setName");
		}
	}
	
	
	/**
	 * Returns the status of this simulation.  This method examines whether the
	 * simulation has been outdated and returns {@link Status#OUTDATED} accordingly.
	 *
	 * @return the status
	 * @see Status
	 */
	public Status getStatus() {
		mutex.verify();
		final FlightConfiguration config = rocket.getFlightConfiguration(this.getId()).clone();

		if (isStatusUpToDate(status)) {
			if (config.getModID() != simulatedConfigurationID || !options.equals(simulatedConditions)) {
				status = Status.OUTDATED;
			}
		}

		// if the id hasn't been set yet, skip.
		if (getId().hasError()) {
			log.warn(" simulationOptions lacks a valid id. Skipping.");
			status = Status.CANT_RUN;
			return status;
		}

		// Make sure this simulation has motors.
		if (!config.hasMotors()) {
			status = Status.CANT_RUN;
		}

		return status;
	}

	/**
	 * Determines whether the simulation has errors
	 */
	public boolean hasErrors() {
		FlightData data = getSimulatedData();
		for (int branchNo = 0; branchNo < data.getBranchCount(); branchNo++) {
			if (data.getBranch(branchNo).getFirstEvent(FlightEvent.Type.SIM_ABORT) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether the specified branch in the simulation has errors
	 *
	 * @param branch the branch to check for errors
	 */
	public boolean hasErrors(int branch) {
		FlightData data = getSimulatedData();
		return data.getBranch(branch).getFirstEvent(FlightEvent.Type.SIM_ABORT) != null;
	}

	/**
	 * Returns true if the status indicates that the simulation data is up-to-date.
	 * @param status status of the simulation to check for if its data is up-to-date
	 */
	public static boolean isStatusUpToDate(Status status) {
		return status == Status.UPTODATE || status == Status.LOADED || status == Status.EXTERNAL;
	}

	/**
	 * Syncs the modID with its flight configuration.
	 */
	public void syncModID() {
		this.simulatedConfigurationID = getActiveConfiguration().getModID();
		fireChangeEvent();
	}
	
	
	/**
	 * Simulate the flight.
	 *
	 * @param additionalListeners	additional simulation listeners (those defined by the simulation are used in any case)
	 * @throws SimulationException	if a problem occurs during simulation
	 */
	public void simulate(SimulationListener... additionalListeners)
			throws SimulationException {
		mutex.lock("simulate");
		SimulationEngine simulator = null;
		simulatedData = null;
		try {
			
			if (this.status == Status.EXTERNAL) {
				throw new SimulationException("Cannot simulate imported simulation.");
			}
			
			try {
				simulator = simulationEngineClass.getConstructor().newInstance();
			} catch (InstantiationException e) {
				throw new IllegalStateException("Cannot instantiate simulator.", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot access simulator instance?! BUG!", e);
			} catch (InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

			SimulationConditions simulationConditions = options.toSimulationConditions();
			simulationConditions.setSimulation(this);
			for (SimulationListener l : additionalListeners) {
				simulationConditions.getSimulationListenerList().add(l);
			}
			
			for (SimulationExtension extension : simulationExtensions) {
				extension.initialize(simulationConditions);
			}
			
			long t1, t2;
			log.debug("Simulation: calling simulator");
			t1 = System.currentTimeMillis();
			simulator.simulate(simulationConditions);
			t2 = System.currentTimeMillis();
			log.debug("Simulation: returning from simulator, simulation took " + (t2 - t1) + "ms");

		} catch (SimulationException e) {
			throw e;
		} finally {
			// Set simulated info after simulation
			simulatedConditions = options.clone();
			simulatedConfigurationDescription = descriptor.format(this.rocket, getId());
			simulatedConfigurationID = getActiveConfiguration().getModID();
			if (simulator != null) {
				simulatedData = simulator.getFlightData();
			}
			
			status = Status.UPTODATE;
			fireChangeEvent();

			mutex.unlock("simulate");
		}
	}
	
	
	/**
	 * Return the conditions used in the previous simulation, or <code>null</code>
	 * if this simulation has not been run.
	 *
	 * @return	the conditions used in the previous simulation, or <code>null</code>.
	 */
	public SimulationOptions getSimulatedConditions() {
		mutex.verify();
		return simulatedConditions;
	}
	
	/**
	 * Return the warnings generated in the previous simulation, or
	 * <code>null</code> if this simulation has not been run.  This is the same
	 * warning set as contained in the <code>FlightData</code> object.
	 *
	 * @return	the warnings during the previous simulation, or <code>null</code>.
	 * @see		FlightData#getWarningSet()
	 */
	public WarningSet getSimulatedWarnings() {
		mutex.verify();
		if (simulatedData == null)
			return null;
		return simulatedData.getWarningSet();
	}
	
	
	/**
	 * Return a string describing the motor configuration of the previous simulation,
	 * or <code>null</code> if this simulation has not been run.
	 *
	 * @return	a description of the motor configuration of the previous simulation, or
	 * 			<code>null</code>.
	 */
	public String getSimulatedConfigurationDescription() {
		mutex.verify();
		return simulatedConfigurationDescription;
	}
	
	/**
	 * Return the flight data of the previous simulation, or <code>null</code> if
	 * this simulation has not been run.
	 *
	 * @return	the flight data of the previous simulation, or <code>null</code>.
	 */
	public FlightData getSimulatedData() {
		mutex.verify();
		return simulatedData;
	}
	
	/**
	 * Return true if this simulation contains plottable flight data.
	 * 
	 * @return true if this simulation contains plottable flight data.
	 */
	public boolean hasSimulationData() {
		FlightData data = getSimulatedData();
		if (data == null) {
			return false;
		}
		if (data.getBranchCount() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Return true if this simulation contains summary flight data.
	 *
	 * @return true if this simulation contains summary flight data.
	 */
	public boolean hasSummaryData() {
		FlightData data = getSimulatedData();
		return data != null;
	}
	
	/**
	 * Returns a copy of this simulation suitable for cut/copy/paste operations.
	 * The rocket refers to the same instance as the original simulation.
	 * This excludes any simulated data.
	 *
	 * @return	a copy of this simulation and its conditions.
	 */
	public Simulation copy() {
		mutex.lock("copy");
		try {
			
			Simulation copy = (Simulation) super.clone();
			
			copy.mutex = SafetyMutex.newInstance();
			copy.status = Status.NOT_SIMULATED;
			copy.options = this.options.clone();
			copy.simulationExtensions = new ArrayList<SimulationExtension>();
			for (SimulationExtension c : this.simulationExtensions) {
				copy.simulationExtensions.add(c.clone());
			}
			copy.listeners = new ArrayList<EventListener>();
			copy.simulatedConditions = null;
			copy.simulatedConfigurationDescription = null;
			copy.simulatedData = null;
			copy.simulatedConfigurationID = -1;
			
			return copy;
			
		} catch (CloneNotSupportedException e) {
			throw new BugException("Clone not supported, BUG", e);
		} finally {
			mutex.unlock("copy");
		}
	}

	public Simulation clone() {
		mutex.lock("clone");
		try {
			Simulation clone = (Simulation) super.clone();

			clone.mutex = SafetyMutex.newInstance();
			clone.name = this.name;
			clone.configId = this.configId;
			clone.simulatedConfigurationDescription = this.simulatedConfigurationDescription;
			clone.simulatedConfigurationID = this.simulatedConfigurationID;
			clone.options = this.options.clone();
			clone.listeners = new ArrayList<>();
			if (this.simulatedConditions != null) {
				clone.simulatedConditions = this.simulatedConditions.clone();
			} else {
				clone.simulatedConditions = null;
			}
			clone.simulationExtensions = new ArrayList<>();
			for (SimulationExtension c : this.simulationExtensions) {
				clone.simulationExtensions.add(c.clone());
			}
			clone.status = this.status;
			clone.simulatedData = this.simulatedData != null ? this.simulatedData.clone() : this.simulatedData;
			clone.simulationStepperClass = this.simulationStepperClass;
			clone.aerodynamicCalculatorClass = this.aerodynamicCalculatorClass;

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException("Clone not supported, BUG", e);
		} finally {
			mutex.unlock("clone");
		}
	}

	/**
	 * Load the data from the specified simulation into this simulation.
	 * @param simulation the simulation to load from.
	 */
	public void loadFrom(Simulation simulation) {
		mutex.lock("loadFrom");
		try {
			this.name = simulation.name;
			this.configId = simulation.configId;
			this.simulatedConfigurationDescription = simulation.simulatedConfigurationDescription;
			this.simulatedConfigurationID = simulation.simulatedConfigurationID;
			this.options.copyConditionsFrom(simulation.options);
			if (simulation.simulatedConditions == null) {
				this.simulatedConditions = null;
			} else {
				this.simulatedConditions.copyConditionsFrom(simulation.simulatedConditions);
			}
			copyExtensionsFrom(simulation.getSimulationExtensions());
			this.status = simulation.status;
			this.simulatedData = simulation.simulatedData;
			this.simulationStepperClass = simulation.simulationStepperClass;
			this.aerodynamicCalculatorClass = simulation.aerodynamicCalculatorClass;
		} finally {
			mutex.unlock("loadFrom");
		}
	}
	
	
	/**
	 * Create a duplicate of this simulation with the specified rocket.  The new
	 * simulation is in non-simulated state.
	 * This methods performs
	 * synchronization on the simulation for thread protection.
	 * <p>
	 * Note:  This method is package-private for unit testing purposes.
	 *
	 * @param newRocket		the rocket for the new simulation.
	 * @return	a new deep copy of the simulation and rocket with the same conditions and properties.
	 */
	public Simulation duplicateSimulation(Rocket newRocket) {
		mutex.lock("duplicateSimulation");
		try {
			final Simulation newSim = new Simulation(this.document, newRocket);
			newSim.name = this.name;
			newSim.configId = this.configId;
			newSim.options.copyConditionsFrom(this.options);
			newSim.simulatedConfigurationDescription = this.simulatedConfigurationDescription;
			for (SimulationExtension c : this.simulationExtensions) {
				newSim.simulationExtensions.add(c.clone());
			}
			newSim.simulationStepperClass = this.simulationStepperClass;
			newSim.aerodynamicCalculatorClass = this.aerodynamicCalculatorClass;
			
			return newSim;
		} finally {
			mutex.unlock("duplicateSimulation");
		}
	}
	
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		mutex.verify();
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		mutex.verify();
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
		EventObject e = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] ls = listeners.toArray(new EventListener[0]);
		for (EventListener l : ls) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(e);
			}
		}
	}
	
	
	
	
	private class ConditionListener implements StateChangeListener {
		@Override
		public void stateChanged(EventObject e) {
			fireChangeEvent();
		}
	}
	
}
