package net.sf.openrocket.rocketcomponent;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.UniqueID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static final Logger log = LoggerFactory.getLogger(Rocket.class);
	private static final Translator trans = Application.getTranslator();
	
	public static final String DEFAULT_NAME = "[{motors}]";
	public static final double DEFAULT_REFERENCE_LENGTH = 0.01;
	
	
	/**
	 * List of component change listeners.
	 */
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
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
	
	
	// Flight configuration list
	private ArrayList<String> flightConfigurationIDs = new ArrayList<String>();
	private HashMap<String, String> flightConfigurationNames = new HashMap<String, String>();
	{
		flightConfigurationIDs.add(null);
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
		copy.flightConfigurationIDs = this.flightConfigurationIDs.clone();
		copy.flightConfigurationNames =
				(HashMap<String, String>) this.flightConfigurationNames.clone();
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
		
		this.flightConfigurationIDs = r.flightConfigurationIDs.clone();
		this.flightConfigurationNames =
				(HashMap<String, String>) r.flightConfigurationNames.clone();
		this.perfectFinish = r.perfectFinish;
		
		String id = defaultConfiguration.getFlightConfigurationID();
		if (!this.flightConfigurationIDs.contains(id))
			defaultConfiguration.setFlightConfigurationID(null);
		
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
		listenerList = new ArrayList<EventListener>();
	}
	
	
	public void printListeners() {
		System.out.println("" + this + " has " + listenerList.size() + " listeners:");
		int i = 0;
		for (EventListener l : listenerList) {
			System.out.println("  " + (i) + ": " + l);
			i++;
		}
	}
	
	@Override
	public void addComponentChangeListener(ComponentChangeListener l) {
		checkState();
		listenerList.add(l);
		log.trace("Added ComponentChangeListener " + l + ", current number of listeners is " +
				listenerList.size());
	}
	
	@Override
	public void removeComponentChangeListener(ComponentChangeListener l) {
		listenerList.remove(l);
		log.trace("Removed ComponentChangeListener " + l + ", current number of listeners is " +
				listenerList.size());
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
			// Copy the list before iterating to prevent concurrent modification exceptions.
			EventListener[] list = listenerList.toArray(new EventListener[0]);
			for (EventListener l : list) {
				if (l instanceof ComponentChangeListener) {
					((ComponentChangeListener) l).componentChanged(e);
				} else if (l instanceof StateChangeListener) {
					((StateChangeListener) l).stateChanged(e);
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
			Application.getExceptionHandler().handleErrorCondition("Attempting to freeze Rocket when it is already frozen, " +
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
			Application.getExceptionHandler().handleErrorCondition("Attempting to thaw Rocket when it is not frozen");
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
	 * Return an array of the flight configuration IDs.  This array is guaranteed
	 * to contain the <code>null</code> ID as the first element.
	 *
	 * @return  an array of the flight configuration IDs.
	 */
	public String[] getFlightConfigurationIDs() {
		checkState();
		return flightConfigurationIDs.toArray(new String[0]);
	}
	
	/**
	 * Add a new flight configuration ID to the flight configurations.  The new ID
	 * is returned.
	 *
	 * @return  the new flight configuration ID.
	 */
	public String newFlightConfigurationID() {
		checkState();
		String id = UUID.randomUUID().toString();
		flightConfigurationIDs.add(id);
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
		if (id == null || flightConfigurationIDs.contains(id))
			return false;
		flightConfigurationIDs.add(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		return true;
	}
	
	/**
	 * Remove a flight configuration ID from the configuration IDs.  The <code>null</code>
	 * ID cannot be removed, and an attempt to remove it will be silently ignored.
	 *
	 * @param id   the flight configuration ID to remove
	 */
	public void removeFlightConfigurationID(String id) {
		checkState();
		if (id == null)
			return;
		// Get current configuration:
		String currentId = getDefaultConfiguration().getFlightConfigurationID();
		// If we're removing the current configuration, we need to switch to a different one first.
		if (currentId != null && currentId.equals(id)) {
			getDefaultConfiguration().setFlightConfigurationID(null);
		}
		flightConfigurationIDs.remove(id);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
	
	/**
	 * Check whether <code>id</code> is a valid motor configuration ID.
	 *
	 * @param id	the configuration ID.
	 * @return		whether a motor configuration with that ID exists.
	 */
	public boolean isFlightConfigurationID(String id) {
		checkState();
		return flightConfigurationIDs.contains(id);
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
				if (mount.getMotorConfiguration().get(id).getMotor() != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Return the user-set name of the flight configuration.  If no name has been set,
	 * returns the default name ({@link #DEFAULT_NAME}).
	 *
	 * @param id   the flight configuration id
	 * @return	   the configuration name
	 */
	public String getFlightConfigurationName(String id) {
		checkState();
		if (!isFlightConfigurationID(id))
			return DEFAULT_NAME;
		String s = flightConfigurationNames.get(id);
		if (s == null)
			return DEFAULT_NAME;
		return s;
	}
	
	
	/**
	 * Set the name of the flight configuration.  A name can be unset by passing
	 * <code>null</code> or an empty string.
	 *
	 * @param id	the flight configuration id
	 * @param name	the name for the flight configuration
	 */
	public void setFlightConfigurationName(String id, String name) {
		checkState();
		if (name == null || name.equals("") || DEFAULT_NAME.equals(name)) {
			flightConfigurationNames.remove(id);
		} else {
			flightConfigurationNames.put(id, name);
		}
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	
	
	////////  Obligatory component information
	
	
	@Override
	public String getComponentName() {
		//// Rocket
		return trans.get("Rocket.compname.Rocket");
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
