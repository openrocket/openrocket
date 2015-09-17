package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.StateChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class defining a rocket configuration, including which stages are active.
 * 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Configuration implements Cloneable, ChangeSource, ComponentChangeListener, Monitorable {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	protected Rocket rocket;
	protected String flightConfigurationId = null;
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
	protected class StageFlags {
		public boolean active = true;
		public int prev = -1;
		public AxialStage stage = null;
		
		public StageFlags(AxialStage _stage, int _prev, boolean _active) {
			this.stage = _stage;
			this.prev = _prev;
			this.active = _active;
		}
	}
	
	/* Cached data */
	protected HashMap<Integer, StageFlags> stageMap = new HashMap<Integer, StageFlags>();
	
	private int boundsModID = -1;
	private ArrayList<Coordinate> cachedBounds = new ArrayList<Coordinate>();
	private double cachedLength = -1;
	
	private int refLengthModID = -1;
	private double cachedRefLength = -1;
	
	private int modID = 0;
	
	
	/**
	 * Create a new configuration with the specified <code>Rocket</code> with 
	 * <code>null</code> motor configuration.
	 * 
	 * @param rocket  the rocket
	 */
	public Configuration(Rocket rocket) {
		this.rocket = rocket;
		updateStageMap();
		rocket.addComponentChangeListener(this);
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void clearAllStages() {
		this.setAllStages(false);
	}
	
	public void setAllStages() {
		this.setAllStages(true);
	}
	
	public void setAllStages(final boolean _value) {
		for (StageFlags cur : stageMap.values()) {
			cur.active = _value;
		}
		fireChangeEvent();
	}
	
	/** 
	 * This method flags a stage inactive.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to inactivate
	 */
	public void clearOnlyStage(final int stageNumber) {
		setStage(stageNumber, false);
	}
	
	/** 
	 * This method flags a stage active.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		setStage(stageNumber, true);
	}
	
	/** 
	 * This method flags the specified stage as requested.  Other stages are unaffected.
	 * 
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 */
	public void setStage(final int stageNumber, final boolean _active) {
		if ((0 <= stageNumber) && (stageMap.containsKey(stageNumber))) {
			log.error("debug: setting stage " + stageNumber + " to " + _active);
			stageMap.get(stageNumber).active = _active;
			fireChangeEvent();
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}
	
	
	public void toggleStage(final int stageNumber) {
		if ((0 <= stageNumber) && (stageMap.containsKey(stageNumber))) {
			StageFlags flags = stageMap.get(stageNumber);
			flags.active = !flags.active;
			//log.error("debug: toggling stage " + stageNumber + " to " + !flags.active + "    " + this.toDebug());
			fireChangeEvent();
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}
	
	/**
	 * Check whether the stage is active.
	 */
	public boolean isStageActive(final AxialStage stage) {
		return this.isStageActive(stage.getStageNumber());
	}
	
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stageNumber) {
		if (stageNumber >= this.rocket.getStageCount()) {
			return false;
		}
		return stageMap.get(stageNumber).active;
	}
	
	public Collection<RocketComponent> getActiveComponents() {
		Queue<RocketComponent> toProcess = new ArrayDeque<RocketComponent>(this.rocket.getChildren());
		ArrayList<RocketComponent> toReturn = new ArrayList<RocketComponent>();
		
		while (!toProcess.isEmpty()) {
			RocketComponent comp = toProcess.poll();
			
			if (comp instanceof AxialStage) {
				if (!isStageActive(comp.getStageNumber())) {
					continue;
				}
			}
			
			toReturn.add(comp);
			for (RocketComponent child : comp.getChildren()) {
				toProcess.offer(child);
			}
		}
		
		return toReturn;
	}
	
	public List<MotorInstance> getActiveMotors(final MotorInstanceConfiguration mic) {
		ArrayList<MotorInstance> toReturn = new ArrayList<MotorInstance>();
		for (MotorInstance inst : mic.getAllMotors()) {
			MotorMount mount = inst.getMount();
			if (mount instanceof RocketComponent) {
				RocketComponent comp = (RocketComponent) mount;
				if (this.isStageActive(comp.getStage().getStageNumber())) {
					toReturn.add(inst);
				}
			}
		}
		
		return toReturn;
	}
	
	public List<AxialStage> getActiveStages() {
		List<AxialStage> activeStages = new ArrayList<AxialStage>();
		
		for (StageFlags flags : this.stageMap.values()) {
			activeStages.add(flags.stage);
		}
		
		return activeStages;
	}
	
	public int getActiveStageCount() {
		int activeCount = 0;
		for (StageFlags cur : this.stageMap.values()) {
			if (cur.active) {
				activeCount++;
			}
		}
		return activeCount;
	}
	
	/** 
	 * Retrieve the bottom-most active stage.
	 * @return 
	 */
	public AxialStage getBottomStage() {
		AxialStage bottomStage = null;
		for (StageFlags curFlags : this.stageMap.values()) {
			if (curFlags.active) {
				bottomStage = curFlags.stage;
			}
		}
		return bottomStage;
	}
	
	public int getStageCount() {
		return stageMap.size();
	}
	
	
	/**
	 * Return the reference length associated with the current configuration.  The 
	 * reference length type is retrieved from the <code>Rocket</code>.
	 * 
	 * @return  the reference length for this configuration.
	 */
	public double getReferenceLength() {
		if (rocket.getModID() != refLengthModID) {
			refLengthModID = rocket.getModID();
			cachedRefLength = rocket.getReferenceType().getReferenceLength(this);
		}
		return cachedRefLength;
	}
	
	
	public double getReferenceArea() {
		return Math.PI * MathUtil.pow2(getReferenceLength() / 2);
	}
	
	
	public String getFlightConfigurationID() {
		return flightConfigurationId;
	}
	
	public void setFlightConfigurationID(String id) {
		if ((flightConfigurationId == null && id == null) ||
				(id != null && id.equals(flightConfigurationId)))
			return;
		
		flightConfigurationId = id;
		fireChangeEvent();
	}
	
	
	
	/**
	 * Removes the listener connection to the rocket and listeners of this object.
	 * This configuration may not be used after a call to this method!
	 */
	public void release() {
		rocket.removeComponentChangeListener(this);
		listenerList = new ArrayList<EventListener>();
		rocket = null;
	}
	
	////////////////  Listeners  ////////////////
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listenerList.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listenerList.remove(listener);
	}
	
	protected void fireChangeEvent() {
		EventObject e = new EventObject(this);
		
		this.modID++;
		boundsModID = -1;
		refLengthModID = -1;
		
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] listeners = listenerList.toArray(new EventListener[0]);
		for (EventListener l : listeners) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(e);
			}
		}
		
		updateStageMap();
	}
	
	private void updateStageMap() {
		if (this.rocket.getStageCount() == this.stageMap.size()) {
			// no changes needed
			return;
		}
		
		this.stageMap.clear();
		for (AxialStage curStage : this.rocket.getStageList()) {
			int prevStageNum = curStage.getStageNumber() - 1;
			if (curStage.getParent() instanceof AxialStage) {
				prevStageNum = curStage.getParent().getStageNumber();
			}
			StageFlags flagsToAdd = new StageFlags(curStage, prevStageNum, true);
			this.stageMap.put(curStage.getStageNumber(), flagsToAdd);
		}
	}
	
	// DEBUG / DEVEL
	public String toDebug() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("["));
		for (StageFlags flags : this.stageMap.values()) {
			buf.append(String.format(" %d", (flags.active ? 1 : 0)));
		}
		buf.append("]\n");
		return buf.toString();
	}
	
	// DEBUG / DEVEL
	public String toDebugDetail() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping stage config: \n"));
		for (StageFlags flags : this.stageMap.values()) {
			AxialStage curStage = flags.stage;
			buf.append(String.format("    [%d]: %24s: %b\n", curStage.getStageNumber(), curStage.getName(), flags.active));
		}
		buf.append("\n\n");
		return buf.toString();
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		fireChangeEvent();
	}
	
	
	///////////////  Helper methods  ///////////////
	
	/**
	 * Return whether a component is in the currently active stages.
	 */
	public boolean isComponentActive(final RocketComponent c) {
		int stage = c.getStageNumber();
		return isStageActive(stage);
	}
	
	
	/**
	 * Return the bounds of the current configuration.  The bounds are cached.
	 * 
	 * @return	a <code>Collection</code> containing coordinates bounding the rocket.
	 */
	public Collection<Coordinate> getBounds() {
		if (rocket.getModID() != boundsModID) {
			boundsModID = rocket.getModID();
			cachedBounds.clear();
			
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (RocketComponent component : this.getActiveComponents()) {
				System.err.println("..bounds checking component: " + component.getName());
				for (Coordinate coord : component.getComponentBounds()) {
					cachedBounds.add(coord);
					if (coord.x < minX)
						minX = coord.x;
					if (coord.x > maxX)
						maxX = coord.x;
				}
			}
			
			if (Double.isInfinite(minX) || Double.isInfinite(maxX)) {
				cachedLength = 0;
			} else {
				cachedLength = maxX - minX;
			}
		}
		return cachedBounds.clone();
	}
	
	
	/**
	 * Returns the length of the rocket configuration, from the foremost bound X-coordinate
	 * to the aft-most X-coordinate.  The value is cached.
	 * 
	 * @return	the length of the rocket in the X-direction.
	 */
	public double getLength() {
		if (rocket.getModID() != boundsModID)
			getBounds(); // Calculates the length
			
		return cachedLength;
	}
	
	
	
	
	/**
	 * Perform a deep-clone.  The object references are also cloned and no
	 * listeners are listening on the cloned object.  The rocket instance remains the same.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Configuration clone() {
		try {
			Configuration config = (Configuration) super.clone();
			config.listenerList = new ArrayList<EventListener>();
			config.stageMap = (HashMap<Integer, StageFlags>) this.stageMap.clone();
			config.cachedBounds = new ArrayList<Coordinate>();
			config.boundsModID = -1;
			config.refLengthModID = -1;
			rocket.addComponentChangeListener(config);
			return config;
		} catch (CloneNotSupportedException e) {
			throw new BugException("clone not supported!", e);
		}
	}
	
	
	@Override
	public int getModID() {
		return modID + rocket.getModID();
	}
	
	
}
