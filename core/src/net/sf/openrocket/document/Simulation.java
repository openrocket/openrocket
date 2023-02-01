package net.sf.openrocket.document;

import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
import net.sf.openrocket.simulation.DefaultSimulationOptionFactory;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationEngine;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.SimulationStepper;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.SafetyMutex;
import net.sf.openrocket.util.StateChangeListener;

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
	 * @param rocket	the rocket associated with the simulation.
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
	 * @param rocket	the rocket associated with the simulation.
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

		this.document = document;
		this.rocket = rocket;

		if (status == Status.UPTODATE) {
			this.status = Status.LOADED;
		} else if (data == null) {
			this.status = Status.NOT_SIMULATED;
		} else {
			this.status = status;
		}
		
		this.name = name;
		
		this.options = options;

		final FlightConfiguration config = rocket.getSelectedConfiguration();
		this.setFlightConfigurationId(config.getFlightConfigurationID());
				
		options.addChangeListener(new ConditionListener());
		addChangeListener(document);
		
		if (extensions != null) {
			this.simulationExtensions.addAll(extensions);
		}
		
		
		if (data != null && this.status != Status.NOT_SIMULATED) {
			simulatedData = data;
			if (this.status == Status.LOADED) {
				simulatedConditions = options.clone();
				simulatedConfigurationID = config.getModID();
			}
		}
		
	}

	public FlightConfiguration getActiveConfiguration() {
		mutex.verify();
		return rocket.getFlightConfiguration(this.configId);
	}

	/**
	 * Return the rocket associated with this simulation.
	 *
	 * @return	the rocket.
	 */
	public Rocket getRocket() {
		mutex.verify();
		return rocket;
	}

	public FlightConfigurationId getFlightConfigurationId(){
		return this.configId;
	}
	public FlightConfigurationId getId(){
		return this.getFlightConfigurationId();
	}
	
	/**
	 * Set the motor configuration ID.  If this id does not yet exist, it will be created.
	 * 
	 * @param fcid	the configuration to set.
	 */
	public void setFlightConfigurationId(FlightConfigurationId fcid) {
		if ( null == fcid ){
			throw new NullPointerException("Attempted to set a null Config id in simulation options. Not allowed!");
		}else if ( fcid.hasError() ){
			throw new IllegalArgumentException("Attempted to set the configuration to an error id. Not Allowed!");
		}else if (!rocket.containsFlightConfigurationID(fcid)){
			rocket.createFlightConfiguration(fcid);
		}
		
		if( fcid.equals(this.configId)){
			return;
		}
		
		this.configId = fcid;
		fireChangeEvent();
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
		if ( getId().hasError() ){
			log.warn(" simulationOptions lacks a valid id. Skipping.");
			status = Status.CANT_RUN;
			return status;
		}
				
		//Make sure this simulation has motors.
		if ( ! config.hasMotors() ){
			status = Status.CANT_RUN;
		}
		
		return status;
	}

	/**
	 * Returns true is the status indicates that the simulation data is up-to-date.
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
		try {
			
			if (this.status == Status.EXTERNAL) {
				throw new SimulationException("Cannot simulate imported simulation.");
			}
			
			SimulationEngine simulator;
			
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
			simulatedData = simulator.simulate(simulationConditions);
			t2 = System.currentTimeMillis();
			log.debug("Simulation: returning from simulator, simulation took " + (t2 - t1) + "ms");

		} catch (SimulationException e) {
			simulatedData = e.getFlightData();
			throw e;
		} finally {
			// Set simulated info after simulation
			simulatedConditions = options.clone();
			simulatedConfigurationDescription = descriptor.format( this.rocket, getId());
			simulatedConfigurationID = getActiveConfiguration().getModID();
			
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
	 * @return
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
			newSim.options.copyFrom(this.options);
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
		
		private Status oldStatus = null;
		
		@Override
		public void stateChanged(EventObject e) {
			if (getStatus() != oldStatus) {
				oldStatus = getStatus();
				fireChangeEvent();
			}
		}
	}
	
}
