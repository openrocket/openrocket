package net.sf.openrocket.rocketcomponent;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.StateChangeListener;


/**
 * A class defining a rocket configuration, including motors and which stages are active.
 * 
 * TODO: HIGH: Remove motor ignition times from this class.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Configuration implements Cloneable, ChangeSource, ComponentChangeListener,
		Iterable<RocketComponent>, Monitorable {
	
	private Rocket rocket;
	private BitSet stages = new BitSet();
	
	private String flightConfigurationId = null;
	
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
	
	/* Cached data */
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
		setAllStages();
		rocket.addComponentChangeListener(this);
	}
	
	
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void setAllStages() {
		stages.clear();
		stages.set(0, rocket.getStageCount());
		fireChangeEvent();
	}
	
	
	/**
	 * Set all stages up to and including the given stage number.  For example,
	 * <code>setToStage(0)</code> will set only the first stage active.
	 * 
	 * @param stage		the stage number.
	 */
	public void setToStage(int stage) {
		stages.clear();
		stages.set(0, stage + 1, true);
		//		stages.set(stage+1, rocket.getStageCount(), false);
		fireChangeEvent();
	}
	
	public void setOnlyStage(int stage) {
		stages.clear();
		stages.set(stage, stage + 1, true);
		fireChangeEvent();
	}
	
	/**
	 * Check whether the up-most stage of the rocket is in this configuration.
	 * 
	 * @return	<code>true</code> if the first stage is active in this configuration.
	 */
	public boolean isHead() {
		return isStageActive(0);
	}
	
	
	
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stage) {
		if (stage >= rocket.getStageCount())
			return false;
		return stages.get(stage);
	}
	
	public int getStageCount() {
		return rocket.getStageCount();
	}
	
	public int getActiveStageCount() {
		int count = 0;
		int s = rocket.getStageCount();
		
		for (int i = 0; i < s; i++) {
			if (stages.get(i))
				count++;
		}
		return count;
	}
	
	public int[] getActiveStages() {
		int stageCount = rocket.getStageCount();
		List<Integer> active = new ArrayList<Integer>();
		int[] ret;
		
		for (int i = 0; i < stageCount; i++) {
			if (stages.get(i)) {
				active.add(i);
			}
		}
		
		ret = new int[active.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = active.get(i);
		}
		
		return ret;
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
	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		fireChangeEvent();
	}
	
	
	///////////////  Helper methods  ///////////////
	
	/**
	 * Return whether this configuration has any motors defined to it.
	 * 
	 * @return  true if this configuration has active motor mounts with motors defined to them.
	 */
	public boolean hasMotors() {
		for (RocketComponent c : this) {
			if (c instanceof MotorMount) {
				MotorMount mount = (MotorMount) c;
				if (!mount.isMotorMount())
					continue;
				if (mount.getMotor(this.flightConfigurationId) != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	
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
	 * @return	a <code>Collection</code> containing coordinates bouding the rocket.
	 */
	public Collection<Coordinate> getBounds() {
		if (rocket.getModID() != boundsModID) {
			boundsModID = rocket.getModID();
			cachedBounds.clear();
			
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (RocketComponent component : this) {
				for (Coordinate c : component.getComponentBounds()) {
					for (Coordinate coord : component.toAbsolute(c)) {
						cachedBounds.add(coord);
						if (coord.x < minX)
							minX = coord.x;
						if (coord.x > maxX)
							maxX = coord.x;
					}
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
	 * Return an iterator that iterates over the currently active components.
	 * The <code>Rocket</code> and <code>Stage</code> components are not returned,
	 * but instead all components that are within currently active stages.
	 */
	@Override
	public Iterator<RocketComponent> iterator() {
		return new ConfigurationIterator();
	}
	
	
	/**
	 * Return an iterator that iterates over all <code>MotorMount</code>s within the
	 * current configuration that have an active motor.
	 * 
	 * @return  an iterator over active motor mounts.
	 */
	public Iterator<MotorMount> motorIterator() {
		return new MotorIterator();
	}
	
	
	/**
	 * Perform a deep-clone.  The object references are also cloned and no
	 * listeners are listening on the cloned object.  The rocket instance remains the same.
	 */
	@Override
	public Configuration clone() {
		try {
			Configuration config = (Configuration) super.clone();
			config.listenerList = new ArrayList<EventListener>();
			config.stages = (BitSet) this.stages.clone();
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
	
	
	/**
	 * A class that iterates over all currently active components.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private class ConfigurationIterator implements Iterator<RocketComponent> {
		Iterator<Iterator<RocketComponent>> iterators;
		Iterator<RocketComponent> current = null;
		
		public ConfigurationIterator() {
			List<Iterator<RocketComponent>> list = new ArrayList<Iterator<RocketComponent>>();
			
			for (RocketComponent stage : rocket.getChildren()) {
				if (isComponentActive(stage)) {
					list.add(stage.iterator(false));
				}
			}
			
			// Get iterators and initialize current
			iterators = list.iterator();
			if (iterators.hasNext()) {
				current = iterators.next();
			} else {
				List<RocketComponent> l = Collections.emptyList();
				current = l.iterator();
			}
		}
		
		
		@Override
		public boolean hasNext() {
			if (!current.hasNext())
				getNextIterator();
			
			return current.hasNext();
		}
		
		@Override
		public RocketComponent next() {
			if (!current.hasNext())
				getNextIterator();
			
			return current.next();
		}
		
		/**
		 * Get the next iterator that has items.  If such an iterator does
		 * not exist, current is left to an empty iterator.
		 */
		private void getNextIterator() {
			while ((!current.hasNext()) && iterators.hasNext()) {
				current = iterators.next();
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove unsupported");
		}
	}
	
	private class MotorIterator implements Iterator<MotorMount> {
		private final Iterator<RocketComponent> iterator;
		private MotorMount next = null;
		
		public MotorIterator() {
			this.iterator = iterator();
		}
		
		@Override
		public boolean hasNext() {
			getNext();
			return (next != null);
		}
		
		@Override
		public MotorMount next() {
			getNext();
			if (next == null) {
				throw new NoSuchElementException("iterator called for too long");
			}
			
			MotorMount ret = next;
			next = null;
			return ret;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove unsupported");
		}
		
		private void getNext() {
			if (next != null)
				return;
			while (iterator.hasNext()) {
				RocketComponent c = iterator.next();
				if (c instanceof MotorMount) {
					MotorMount mount = (MotorMount) c;
					if (mount.isMotorMount() && mount.getMotor(flightConfigurationId) != null) {
						next = mount;
						return;
					}
				}
			}
		}
	}
	
}
