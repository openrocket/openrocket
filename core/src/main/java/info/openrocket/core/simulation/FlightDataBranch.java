package info.openrocket.core.simulation;

import java.util.List;
import java.util.Map;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.ModID;

/**
 * A single branch of flight data.  The data is ordered based on some variable, typically time.
 * It also contains flight events that have occurred during simulation.
 * <p>
 * After instantiating a FlightDataBranch data and new variable types can be added to the branch.
 * A new data point (a value for each variable defined) is created using {@link #addPoint()} after
 * which the value for each variable type can be set using {@link #setValue(FlightDataType, double)}.
 * Each variable type does NOT have to be set, unset values will default to NaN.  New variable types
 * not defined in the constructor can be added using {@link #setValue(FlightDataType, double)}, they
 * will be created and all previous values will be set to NaN.
 * <p>
 * After populating a FlightDataBranch object it can be made immutable by calling {@link #immute()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightDataBranch extends DataBranch<FlightDataType> {
	private double timeToOptimumAltitude = Double.NaN;
	private double optimumAltitude = Double.NaN;
	private double separationTime = Double.NaN;
	private final ArrayList<FlightEvent> events = new ArrayList<>();
	
	/**
	 * Sole constructor.  Defines the name of the FlightDataBranch and at least one variable type.
	 * 
	 * @param name		the name of this FlightDataBranch.
	 * @param types		data types to include (must include at least one type).
	 */
	public FlightDataBranch(String name, FlightDataType... types) {
		super(name, types);
	}

	/**
	 * Make a flight data branch with all data points copied from its parent.  Intended for use
	 * when creating a new branch upon stage separation, so the data at separation is present
	 * in both branches (and if the new branch has an immediate exception, it can be plotted)
	 *
	 * @param name		the name of the new branch.
	 * @param srcComponent		the component that is the source of the new branch.
	 * @param parent			the parent branch to copy data from.
	 */
	public FlightDataBranch(String name, RocketComponent srcComponent, FlightDataBranch parent) {
		super(name);

		// Copy all the values from the parent
		copyValuesFromBranch(parent, srcComponent);
	}
	
	/**
	 * Makes an 'empty' flight data branch which has no data but all built in data types are defined.
	 */
	public FlightDataBranch() {
		super("Empty branch");
		for (FlightDataType type : FlightDataType.ALL_TYPES) {
			this.setValue(type, Double.NaN);
		}
		this.immute();
	}


	/**
	 * Clears all the current values in the branch and copies the values from the given branch.
	 * @param srcBranch 	the branch to copy values from
	 * @param srcComponent 	the component that is the source of this branch (used for copying events)
	 */
	private void copyValuesFromBranch(FlightDataBranch srcBranch, RocketComponent srcComponent) {
		this.values.clear();

		// Need to have at least one type to set up values
		values.put(FlightDataType.TYPE_TIME, new ArrayList<>());
		minValues.put(FlightDataType.TYPE_TIME, Double.NaN);
		maxValues.put(FlightDataType.TYPE_TIME, Double.NaN);

		if (srcBranch == null) {
			return;
		}

		// Copy flight data
		for (int i = 0; i < srcBranch.getLength(); i++) {
			this.addPoint();
			for (FlightDataType type : srcBranch.getTypes()) {
				this.setValue(type, srcBranch.getByIndex(type, i));
			}
		}

		// Copy flight events belonging to this branch
		List<FlightEvent> sustainerEvents = srcBranch.getEvents();
		for (FlightEvent event : sustainerEvents) {
			// Stage separation is already added elsewhere, so don't copy it over (otherwise you have a duplicate)
			if (event.getType() == FlightEvent.Type.STAGE_SEPARATION) {
				continue;
			}
			RocketComponent srcEventComponent = event.getSource();
			// Ignore null events
			if (srcComponent == null || srcEventComponent == null) {
				continue;
			}
			// Ignore events from other stages. Important for when the current stage has a booster stage; we don't want to copy over the booster events.
			if (getStageForComponent(srcComponent) != getStageForComponent(srcEventComponent)) {
				continue;
			}
			if (srcComponent == srcEventComponent || srcComponent.containsChild(srcEventComponent)) {
				events.add(event);
			}
		}
	}

	/**
	 * A safer method for checking the stage of a component (that shouldn't throw exceptions when calling on stages/rockets)
	 * @param component 	the component to get the stage of
	 * @return the stage of the component, or null if the component is a rocket
	 */
	private AxialStage getStageForComponent(RocketComponent component) {
		if (component instanceof AxialStage) {
			return (AxialStage) component;
		} else if (component instanceof Rocket) {
			return null;
		} else {
			return component.getStage();
		}
	}
	
	
	/**
	 * @return the timeToOptimumAltitude
	 */
	public double getTimeToOptimumAltitude() {
		return timeToOptimumAltitude;
	}
	
	/**
	 * @param timeToOptimumAltitude the timeToOptimumAltitude to set
	 */
	public void setTimeToOptimumAltitude(double timeToOptimumAltitude) {
		this.timeToOptimumAltitude = timeToOptimumAltitude;
	}
	
	/**
	 * @return the optimumAltitude
	 */
	public double getOptimumAltitude() {
		return optimumAltitude;
	}
	
	/**
	 * @param optimumAltitude the optimumAltitude to set
	 */
	public void setOptimumAltitude(double optimumAltitude) {
		this.optimumAltitude = optimumAltitude;
	}
	
	public double getOptimumDelay() {
		
		if (Double.isNaN(timeToOptimumAltitude)) {
			return Double.NaN;
		}
		// TODO - we really want the first burnout of this stage.  which
		// could be computed as the first burnout after the last stage separation event.
		// however, that's not quite so concise
		FlightEvent e = getLastEvent(FlightEvent.Type.BURNOUT);
		if (e != null) {
			return timeToOptimumAltitude - e.getTime();
		}
		
		return Double.NaN;
	}
	
	/**
	 * Add a flight event to this branch.
	 * 
	 * @param event		the event to add.
	 * @throws IllegalStateException	if this branch has been made immutable.
	 */
	public void addEvent(FlightEvent event) {
		mutable.check();
		events.add(event);
		if (event.getType() == FlightEvent.Type.STAGE_SEPARATION) {
			separationTime = event.getTime();
		}
		modID = new ModID();
	}
	
	
	/**
	 * Return the list of events.
	 * 
	 * @return	the list of events during the flight.
	 */
	public List<FlightEvent> getEvents() {
		return events.clone();
	}
	
	/**
	 * Return the first event of the given type.
	 * @param type
	 * @return
	 */
	public FlightEvent getFirstEvent(FlightEvent.Type type) {
		for (FlightEvent e : events) {
			if (e.getType() == type) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * Return the last event of the given type.
	 * @param type
	 * @return
	 */
	public FlightEvent getLastEvent(FlightEvent.Type type) {
		FlightEvent retval = null;
		for (FlightEvent e : events) {
			if (e.getType() == type) {
				retval = e;
			}
		}
		return retval;
	}

	/**
	 * Return the time of the stage separation event.
	 * @return the time of the stage separation event, or NaN if no separation event has occurred.
	 */
	public double getSeparationTime() {
		return separationTime;
	}

	/**
	 * Return the data index corresponding to the given time.
	 * @param time the time to search for
	 * @return the data index corresponding to the given time, or -1 if the time is not found.
	 */
	public int getDataIndexOfTime(double time) {
		if (Double.isNaN(time)) {
			return -1;
		}
		List<Double> times = get(FlightDataType.TYPE_TIME);
		if (times == null) {
			return -1;
		}
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i) >= time) {
				return i;
			}
		}
		return -1;
	}

	public FlightDataBranch clone() {
		FlightDataType[] types = getTypes();
		FlightDataBranch clone = new FlightDataBranch(name, types);
		for (Map.Entry<FlightDataType, ArrayList<Double>> entry : values.entrySet()) {
			clone.values.put(entry.getKey(), entry.getValue().clone());
		}
		clone.minValues.putAll(minValues);
		clone.maxValues.putAll(maxValues);
		clone.events.addAll(events);
		clone.timeToOptimumAltitude = timeToOptimumAltitude;
		clone.optimumAltitude = optimumAltitude;
		clone.modID = modID;
		return clone;
	}
	
}
