package net.sf.openrocket.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;

/**
 * A database containing ThrustCurveMotorSet objects and allowing adding a motor
 * to the database.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class ThrustCurveMotorSetDatabase implements MotorDatabase {
	
	private static final LogHelper logger = Application.getLogger();
	
	protected List<ThrustCurveMotorSet> motorSets;
	
	private volatile boolean startedLoading = false;
	private volatile boolean endedLoading = false;
	private final boolean asynchronous;
	
	/** Set to true the first time {@link #blockUntilLoaded()} is called. */
	protected volatile boolean inUse = false;
	
	/**
	 * Sole constructor.
	 * 
	 * @param asynchronous	whether to load motors asynchronously in a background thread.
	 */
	public ThrustCurveMotorSetDatabase(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
	
	
	/* (non-Javadoc)
	 * @see net.sf.openrocket.database.ThrustCurveMotorSetDatabaseI#getMotorSets()
	 */
	public List<ThrustCurveMotorSet> getMotorSets() {
		blockUntilLoaded();
		return motorSets;
	}
	
	

	/* (non-Javadoc)
	 * @see net.sf.openrocket.database.ThrustCurveMotorSetDatabaseI#findMotors(net.sf.openrocket.motor.Motor.Type, java.lang.String, java.lang.String, double, double)
	 */
	@Override
	public List<ThrustCurveMotor> findMotors(Motor.Type type, String manufacturer, String designation,
			double diameter, double length) {
		blockUntilLoaded();
		ArrayList<ThrustCurveMotor> results = new ArrayList<ThrustCurveMotor>();
		
		for (ThrustCurveMotorSet set : motorSets) {
			for (ThrustCurveMotor m : set.getMotors()) {
				boolean match = true;
				if (type != null && type != set.getType())
					match = false;
				else if (manufacturer != null && !m.getManufacturer().matches(manufacturer))
					match = false;
				else if (designation != null && !designation.equalsIgnoreCase(m.getDesignation()))
					match = false;
				else if (!Double.isNaN(diameter) && (Math.abs(diameter - m.getDiameter()) > 0.0015))
					match = false;
				else if (!Double.isNaN(length) && (Math.abs(length - m.getLength()) > 0.0015))
					match = false;
				
				if (match)
					results.add(m);
			}
		}
		
		return results;
	}
	
	
	/**
	 * Add a motor to the database.  If a matching ThrustCurveMototSet is found, 
	 * the motor is added to that set, otherwise a new set is created and added to the
	 * database.
	 * 
	 * @param motor		the motor to add
	 */
	protected void addMotor(ThrustCurveMotor motor) {
		// Iterate from last to first, as this is most likely to hit early when loading files
		for (int i = motorSets.size() - 1; i >= 0; i--) {
			ThrustCurveMotorSet set = motorSets.get(i);
			if (set.matches(motor)) {
				set.addMotor(motor);
				return;
			}
		}
		
		ThrustCurveMotorSet newSet = new ThrustCurveMotorSet();
		newSet.addMotor(motor);
		motorSets.add(newSet);
	}
	
	



	/**
	 * Start loading the motors.  If asynchronous 
	 * 
	 * @throws  IllegalStateException	if this method has already been called.
	 */
	public void startLoading() {
		if (startedLoading) {
			throw new IllegalStateException("Already called startLoading");
		}
		startedLoading = true;
		if (asynchronous) {
			new LoadingThread().start();
		} else {
			performMotorLoading();
		}
	}
	
	
	/**
	 * Return whether loading the database has ended.
	 * 
	 * @return	whether background loading has ended.
	 */
	public boolean isLoaded() {
		return endedLoading;
	}
	
	
	/**
	 * Mark that this database is in use or a place is waiting for the database to 
	 * become loaded.  This can be used in conjunction with {@link #isLoaded()} to load
	 * the database without blocking.
	 */
	public void setInUse() {
		inUse = true;
	}
	
	
	/**
	 * Block the current thread until loading of the motors has been completed.
	 * 
	 * @throws IllegalStateException	if startLoading() has not been called.
	 */
	public void blockUntilLoaded() {
		inUse = true;
		if (!startedLoading) {
			throw new IllegalStateException("startLoading() has not been called");
		}
		if (!endedLoading) {
			synchronized (this) {
				while (!endedLoading) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						logger.warn("InterruptedException occurred, ignoring", e);
					}
				}
			}
		}
	}
	
	
	/**
	 * Used for loading the motor database.  This method will be called in a background
	 * thread to load the motors asynchronously.  This method should call 
	 * {@link #addMotor(ThrustCurveMotor)} to add the motors to the database.
	 */
	protected abstract void loadMotors();
	
	

	/**
	 * Creates the motor list, calls {@link #loadMotors()}, sorts the list and marks
	 * the motors as loaded.  This method is called either synchronously or from the
	 * background thread.
	 */
	private void performMotorLoading() {
		motorSets = new ArrayList<ThrustCurveMotorSet>();
		try {
			loadMotors();
		} catch (Exception e) {
			logger.error("Loading motors failed", e);
		}
		Collections.sort(motorSets);
		motorSets = Collections.unmodifiableList(motorSets);
		synchronized (ThrustCurveMotorSetDatabase.this) {
			endedLoading = true;
			ThrustCurveMotorSetDatabase.this.notifyAll();
		}
	}
	
	
	/**
	 * Background thread for loading the motors.  This creates the motor list,
	 * calls loadMotors(), sorts the database, makes it unmodifiable, and finally
	 * marks the database as loaded and notifies any blocked threads.
	 */
	private class LoadingThread extends Thread {
		@Override
		public void run() {
			performMotorLoading();
		}
	}
	
}
