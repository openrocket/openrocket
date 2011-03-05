package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.*;


/**
 * Base for all rocket components.  This is the "starting point" for all rocket trees.
 * It provides the actual implementations of several methods defined in RocketComponent
 * (eg. the rocket listener lists) and the methods defined in RocketComponent call these.
 * It also defines some other methods that concern the whole rocket, and helper methods
 * that keep information about the program state.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class Rocket extends RocketComponent {
	private static final LogHelper log = Application.getLogger();

	public static final double DEFAULT_REFERENCE_LENGTH = 0.01;


	/**
	 * List of component change listeners.
	 */
	private EventListenerList listenerList = new EventListenerList();

	/**
	 * When freezeList != null, events are not dispatched but stored in the list.
	 * When the structure is thawed, a single combined event will be fired.
	 */
	private List<ComponentChangeEvent> freezeList = null;


	private int modID;
	private int massModID;
	private int aeroModID;
	private int treeModID;
	private int functionalModID;


	private ReferenceType refType = ReferenceType.MAXIMUM; // Set in constructor
	private double customReferenceLength = DEFAULT_REFERENCE_LENGTH;


	// The default configuration used in dialogs
	private final Configuration defaultConfiguration;


	private String designer = "";
	private String revision = "";


	// Motor configuration list
	private ArrayList<String> motorConfigurationIDs = new ArrayList<String>();
	private HashMap<String, String> motorConfigurationNames = new HashMap<String, String>();
	{
		motorConfigurationIDs.add(null);
	}


	// Does the rocket have a perfect finish (a notable amount of laminar flow)
	private boolean perfectFinish = false;



	/////////////  Constructor  /////////////

	public Rocket() {
		super(RocketComponent.Position.AFTER);
		modID = UniqueID.next();
		massModID = modID;
		aeroModID = modID;
		treeModID = modID;
		functionalModID = modID;
		defaultConfiguration = new Configuration(this);
	}



	public String getDesigner() {
		checkState();
		return designer;
	}

	public void setDesigner(String s) {
		if (s == null)
			s = "";
		designer = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}


	public String getRevision() {
		checkState();
		return revision;
	}

	public void setRevision(String s) {
		if (s == null)
			s = "";
		revision = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}




	/**
	 * Return the number of stages in this rocket.
	 *
	 * @return   the number of stages in this rocket.
	 */
	public int getStageCount() {
		checkState();
		return this.getChildCount();
	}


	/**
	 * Return the non-negative modification ID of this rocket.  The ID is changed
	 * every time any change occurs in the rocket.  This can be used to check
	 * whether it is necessary to void cached data in cases where listeners can not
	 * or should not be used.
	 * <p>
	 * Three other modification IDs are also available, {@link #getMassModID()},
	 * {@link #getAerodynamicModID()} {@link #getTreeModID()}, which change every time
	 * a mass change, aerodynamic change, or tree change occur.  Even though the values
	 * of the different modification ID's may be equal, they should be treated totally
	 * separate.
	 * <p>
	 * Note that undo events restore the modification IDs that were in use at the
	 * corresponding undo level.  Subsequent modifications, however, produce modIDs
	 * distinct from those already used.
	 *
	 * @return   a unique ID number for this modification state.
	 */
	public int getModID() {
		return modID;
	}

	/**
	 * Return the non-negative mass modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this mass-modification state.
	 */
	public int getMassModID() {
		return massModID;
	}

	/**
	 * Return the non-negative aerodynamic modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this aerodynamic-modification state.
	 */
	public int getAerodynamicModID() {
		return aeroModID;
	}

	/**
	 * Return the non-negative tree modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this tree-modification state.
	 */
	public int getTreeModID() {
		return treeModID;
	}

	/**
	 * Return the non-negative functional modificationID of this rocket.
	 * This changes every time a functional change occurs.
	 *
	 * @return	a unique ID number for this functional modification state.
	 */
	public int getFunctionalModID() {
		return functionalModID;
	}




	public ReferenceType getReferenceType() {
		checkState();
		return refType;
	}

	public void setReferenceType(ReferenceType type) {
		if (refType == type)
			return;
		refType = type;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}


	public double getCustomReferenceLength() {
		checkState();
		return customReferenceLength;
	}

	public void setCustomReferenceLength(double length) {
		if (MathUtil.equals(customReferenceLength, length))
			return;

		this.customReferenceLength = Math.max(length, 0.001);

		if (refType == ReferenceType.CUSTOM) {
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		}
	}





	/**
	 * Set whether the rocket has a perfect finish.  This will affect whether the
	 * boundary layer is assumed to be fully turbulent or not.
	 *
	 * @param perfectFinish		whether the finish is perfect.
	 */
	public void setPerfectFinish(boolean perfectFinish) {
		if (this.perfectFinish == perfectFinish)
			return;
		this.perfectFinish = perfectFinish;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}



	/**
	 * Get whether the rocket has a perfect finish.
	 *
	 * @return the perfectFinish
	 */
	public boolean isPerfectFinish() {
		return perfectFinish;
	}





	/**
	 * Make a deep copy of the Rocket structure.  This method is exposed as public to allow
	 * for undo/redo system functionality.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Rocket copyWithOriginalID() {
		Rocket copy = (Rocket) super.copyWithOriginalID();
		copy.motorConfigurationIDs = this.motorConfigurationIDs.clone();
		copy.motorConfigurationNames =
				(HashMap<String, String>) this.motorConfigurationNames.clone();
		copy.resetListeners();

		return copy;
	}

	/**
	 * Load the rocket structure from the source.  The method loads the fields of this
	 * Rocket object and copies the references to siblings from the <code>source</code>.
	 * The object <code>source</code> should not be used after this call, as it is in
	 * an illegal state!
	 * <p>
	 * This method is meant to be used in conjunction with undo/redo functionality,
	 * and therefore fires an UNDO_EVENT, masked with all applicable mass/aerodynamic/tree
	 * changes.
	 */
	@SuppressWarnings("unchecked")
	public void loadFrom(Rocket r) {

		// Store list of components to invalidate after event has been fired
		List<RocketComponent> toInvalidate = this.copyFrom(r);

		int type = ComponentChangeEvent.UNDO_CHANGE | ComponentChangeEvent.NONFUNCTIONAL_CHANGE;
		if (this.massModID != r.massModID)
			type |= ComponentChangeEvent.MASS_CHANGE;
		if (this.aeroModID != r.aeroModID)
			type |= ComponentChangeEvent.AERODYNAMIC_CHANGE;
		// Loading a rocket is always a tree change since the component objects change
		type |= ComponentChangeEvent.TREE_CHANGE;

		this.modID = r.modID;
		this.massModID = r.massModID;
		this.aeroModID = r.aeroModID;
		this.treeModID = r.treeModID;
		this.functionalModID = r.functionalModID;
		this.refType = r.refType;
		this.customReferenceLength = r.customReferenceLength;

		this.motorConfigurationIDs = r.motorConfigurationIDs.clone();
		this.motorConfigurationNames =
				(HashMap<String, String>) r.motorConfigurationNames.clone();
		this.perfectFinish = r.perfectFinish;

		String id = defaultConfiguration.getMotorConfigurationID();
		if (!this.motorConfigurationIDs.contains(id))
			defaultConfiguration.setMotorConfigurationID(null);

		this.checkComponentStructure();

		fireComponentChangeEvent(type);

		// Invalidate obsolete components after event
		for (RocketComponent c : toInvalidate) {
			c.invalidate();
		}
	}




	///////  Implement the ComponentChangeListener lists

	/**
	 * Creates a new EventListenerList for this component.  This is necessary when cloning
	 * the structure.
	 */
	public void resetListeners() {
		//		System.out.println("RESETTING LISTENER LIST of Rocket "+this);
		listenerList = new EventListenerList();
	}


	public void printListeners() {
		System.out.println("" + this + " has " + listenerList.getListenerCount() + " listeners:");
		Object[] list = listenerList.getListenerList();
		for (int i = 1; i < list.length; i += 2)
			System.out.println("  " + ((i + 1) / 2) + ": " + list[i]);
	}

	@Override
	public void addComponentChangeListener(ComponentChangeListener l) {
		checkState();
		listenerList.add(ComponentChangeListener.class, l);
		log.verbose("Added ComponentChangeListener " + l + ", current number of listeners is " +
				listenerList.getListenerCount());
	}

	@Override
	public void removeComponentChangeListener(ComponentChangeListener l) {
		listenerList.remove(ComponentChangeListener.class, l);
		log.verbose("Removed ComponentChangeListener " + l + ", current number of listeners is " +
				listenerList.getListenerCount());
	}


	@Override
	public void addChangeListener(ChangeListener l) {
		checkState();
		listenerList.add(ChangeListener.class, l);
		log.verbose("Added ChangeListener " + l + ", current number of listeners is " +
				listenerList.getListenerCount());
	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
		log.verbose("Removed ChangeListener " + l + ", current number of listeners is " +
				listenerList.getListenerCount());
	}


	@Override
	protected void fireComponentChangeEvent(ComponentChangeEvent e) {
		mutex.lock("fireComponentChangeEvent");
		try {
			checkState();

			// Update modification ID's only for normal (not undo/redo) events
			if (!e.isUndoChange()) {
				modID = UniqueID.next();
				if (e.isMassChange())
					massModID = modID;
				if (e.isAerodynamicChange())
					aeroModID = modID;
				if (e.isTreeChange())
					treeModID = modID;
				if (e.getType() != ComponentChangeEvent.NONFUNCTIONAL_CHANGE)
					functionalModID = modID;
			}

			// Check whether frozen
			if (freezeList != null) {
				log.debug("Rocket is in frozen state, adding event " + e + " info freeze list");
				freezeList.add(e);
				return;
			}

			log.debug("Firing rocket change event " + e);

			// Notify all components first
			Iterator<RocketComponent> iterator = this.iterator(true);
			while (iterator.hasNext()) {
				iterator.next().componentChanged(e);
			}

			// Notify all listeners
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ComponentChangeListener.class) {
					((ComponentChangeListener) listeners[i + 1]).componentChanged(e);
				} else if (listeners[i] == ChangeListener.class) {
					((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		} finally {
			mutex.unlock("fireComponentChangeEvent");
		}
	}


	/**
	 * Freezes the rocket structure from firing any events.  This may be performed to
	 * combine several actions on the structure into a single large action.
	 * <code>thaw()</code> must always be called afterwards.
	 *
	 * NOTE:  Always use a try/finally to ensure <code>thaw()</code> is called:
	 * <pre>
	 *     Rocket r = c.getRocket();
	 *     try {
	 *         r.freeze();
	 *         // do stuff
	 *     } finally {
	 *         r.thaw();
	 *     }
	 * </pre>
	 *
	 * @see #thaw()
	 */
	public void freeze() {
		checkState();
		if (freezeList == null) {
			freezeList = new LinkedList<ComponentChangeEvent>();
			log.debug("Freezing Rocket");
		} else {
			ExceptionHandler.handleErrorCondition("Attempting to freeze Rocket when it is already frozen, " +
					"freezeList=" + freezeList);
		}
	}

	/**
	 * Thaws a frozen rocket structure and fires a combination of the events fired during
	 * the freeze.  The event type is a combination of those fired and the source is the
	 * last component to have been an event source.
	 *
	 * @see #freeze()
	 */
	public void thaw() {
		checkState();
		if (freezeList == null) {
			ExceptionHandler.handleErrorCondition("Attempting to thaw Rocket when it is not frozen");
			return;
		}
		if (freezeList.size() == 0) {
			log.warn("Thawing rocket with no changes made");
			freezeList = null;
			return;
		}

		log.debug("Thawing rocket, freezeList=" + freezeList);

		int type = 0;
		Object c = null;
		for (ComponentChangeEvent e : freezeList) {
			type = type | e.getType();
			c = e.getSource();
		}
		freezeList = null;

		fireComponentChangeEvent(new ComponentChangeEvent((RocketComponent) c, type));
	}




	////////  Motor configurations  ////////


	/**
	 * Return the default configuration.  This should be used in the user interface
	 * to ensure a consistent rocket configuration between dialogs.  It should NOT
	 * be used in simulations not relating to the UI.
	 *
	 * @return   the default {@link Configuration}.
	 */
	public Configuration getDefaultConfiguration() {
		checkState();
		return defaultConfiguration;
	}


	/**
	 * Return an array of the motor configuration IDs.  This array is guaranteed
	 * to contain the <code>null</code> ID as the first element.
	 *
	 * @return  an array of the motor configuration IDs.
	 */
	public String[] getMotorConfigurationIDs() {
		checkState();
		return motorConfigurationIDs.toArray(new String[0]);
	}

	/**
	 * Add a new motor configuration ID to the motor configurations.  The new ID
	 * is returned.
	 *
	 * @return  the new motor configuration ID.
	 */
	public String newMotorConfigurationID() {
		checkState();
		String id = UUID.randomUUID().toString();
		motorConfigurationIDs.add(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		return id;
	}

	/**
	 * Add a specified motor configuration ID to the motor configurations.
	 *
	 * @param id	the motor configuration ID.
	 * @return		true if successful, false if the ID was already used.
	 */
	public boolean addMotorConfigurationID(String id) {
		checkState();
		if (id == null || motorConfigurationIDs.contains(id))
			return false;
		motorConfigurationIDs.add(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		return true;
	}

	/**
	 * Remove a motor configuration ID from the configuration IDs.  The <code>null</code>
	 * ID cannot be removed, and an attempt to remove it will be silently ignored.
	 *
	 * @param id   the motor configuration ID to remove
	 */
	public void removeMotorConfigurationID(String id) {
		checkState();
		if (id == null)
			return;
		motorConfigurationIDs.remove(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}


	/**
	 * Check whether <code>id</code> is a valid motor configuration ID.
	 *
	 * @param id	the configuration ID.
	 * @return		whether a motor configuration with that ID exists.
	 */
	public boolean isMotorConfigurationID(String id) {
		checkState();
		return motorConfigurationIDs.contains(id);
	}



	/**
	 * Check whether the given motor configuration ID has motors defined for it.
	 *
	 * @param id	the motor configuration ID (may be invalid).
	 * @return		whether any motors are defined for it.
	 */
	public boolean hasMotors(String id) {
		checkState();
		if (id == null)
			return false;

		Iterator<RocketComponent> iterator = this.iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();

			if (c instanceof MotorMount) {
				MotorMount mount = (MotorMount) c;
				if (!mount.isMotorMount())
					continue;
				if (mount.getMotor(id) != null) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * Return the user-set name of the motor configuration.  If no name has been set,
	 * returns an empty string (not null).
	 *
	 * @param id   the motor configuration id
	 * @return	   the configuration name
	 */
	public String getMotorConfigurationName(String id) {
		checkState();
		if (!isMotorConfigurationID(id))
			return "";
		String s = motorConfigurationNames.get(id);
		if (s == null)
			return "";
		return s;
	}


	/**
	 * Set the name of the motor configuration.  A name can be unset by passing
	 * <code>null</code> or an empty string.
	 *
	 * @param id	the motor configuration id
	 * @param name	the name for the motor configuration
	 */
	public void setMotorConfigurationName(String id, String name) {
		checkState();
		motorConfigurationNames.put(id, name);
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}


	/**
	 * Return either the motor configuration name (if set) or its description.
	 *
	 * @param id  the motor configuration ID.
	 * @return    a textual representation of the configuration
	 */
	public String getMotorConfigurationNameOrDescription(String id) {
		checkState();
		String name;

		name = getMotorConfigurationName(id);
		if (name != null && !name.equals(""))
			return name;

		return getMotorConfigurationDescription(id);
	}

	/**
	 * Return a description for the motor configuration, generated from the motor
	 * designations of the components.
	 *
	 * @param id  the motor configuration ID.
	 * @return    a textual representation of the configuration
	 */
	@SuppressWarnings("null")
	public String getMotorConfigurationDescription(String id) {
		checkState();
		String name;
		int motorCount = 0;

		// Generate the description

		// First iterate over each stage and store the designations of each motor
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> currentList = null;

		Iterator<RocketComponent> iterator = this.iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();

			if (c instanceof Stage) {

				currentList = new ArrayList<String>();
				list.add(currentList);

			} else if (c instanceof MotorMount) {

				MotorMount mount = (MotorMount) c;
				Motor motor = mount.getMotor(id);

				if (mount.isMotorMount() && motor != null) {
					String designation = motor.getDesignation(mount.getMotorDelay(id));

					for (int i = 0; i < mount.getMotorCount(); i++) {
						currentList.add(designation);
						motorCount++;
					}
				}

			}
		}

		if (motorCount == 0) {
			return "[No motors]";
		}

		// Change multiple occurrences of a motor to n x motor
		List<String> stages = new ArrayList<String>();

		for (List<String> stage : list) {
			String stageName = "";
			String previous = null;
			int count = 0;

			Collections.sort(stage);
			for (String current : stage) {
				if (current.equals(previous)) {

					count++;

				} else {

					if (previous != null) {
						String s = "";
						if (count > 1) {
							s = "" + count + Chars.TIMES + previous;
						} else {
							s = previous;
						}

						if (stageName.equals(""))
							stageName = s;
						else
							stageName = stageName + "," + s;
					}

					previous = current;
					count = 1;

				}
			}
			if (previous != null) {
				String s = "";
				if (count > 1) {
					s = "" + count + Chars.TIMES + previous;
				} else {
					s = previous;
				}

				if (stageName.equals(""))
					stageName = s;
				else
					stageName = stageName + "," + s;
			}

			stages.add(stageName);
		}

		name = "[";
		for (int i = 0; i < stages.size(); i++) {
			String s = stages.get(i);
			if (s.equals(""))
				s = "None";
			if (i == 0)
				name = name + s;
			else
				name = name + "; " + s;
		}
		name += "]";
		return name;
	}



	////////  Obligatory component information


	@Override
	public String getComponentName() {
		return "Rocket";
	}

	@Override
	public Coordinate getComponentCG() {
		return new Coordinate(0, 0, 0, 0);
	}

	@Override
	public double getComponentMass() {
		return 0;
	}

	@Override
	public double getLongitudinalUnitInertia() {
		return 0;
	}

	@Override
	public double getRotationalUnitInertia() {
		return 0;
	}

	@Override
	public Collection<Coordinate> getComponentBounds() {
		return Collections.emptyList();
	}

	@Override
	public boolean isAerodynamic() {
		return false;
	}

	@Override
	public boolean isMassive() {
		return false;
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	/**
	 * Allows only <code>Stage</code> components to be added to the type Rocket.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return (Stage.class.isAssignableFrom(type));
	}

}
