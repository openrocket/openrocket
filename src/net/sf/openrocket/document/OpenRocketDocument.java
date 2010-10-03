package net.sf.openrocket.document;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.TraceException;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Icons;

/**
 * Class describing an entire OpenRocket document, including a rocket and
 * simulations.  This class also handles undo/redo operations for the rocket structure.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocketDocument implements ComponentChangeListener {
	private static final LogHelper log = Application.getLogger();
	
	/**
	 * The minimum number of undo levels that are stored.
	 */
	public static final int UNDO_LEVELS = 50;
	/**
	 * The margin of the undo levels.  After the number of undo levels exceeds 
	 * UNDO_LEVELS by this amount the undo is purged to that length.
	 */
	public static final int UNDO_MARGIN = 10;
	
	public static final String SIMULATION_NAME_PREFIX = "Simulation ";
	
	/** Whether an undo error has already been reported to the user */
	private static boolean undoErrorReported = false;
	
	private final Rocket rocket;
	private final Configuration configuration;
	
	private final ArrayList<Simulation> simulations = new ArrayList<Simulation>();
	

	/*
	 * The undo/redo variables and mechanism are documented in doc/undo-redo-flow.*
	 */

	/** 
	 * The undo history of the rocket.   Whenever a new undo position is created while the
	 * rocket is in "dirty" state, the rocket is copied here.
	 */
	private LinkedList<Rocket> undoHistory = new LinkedList<Rocket>();
	private LinkedList<String> undoDescription = new LinkedList<String>();
	
	/**
	 * The position in the undoHistory we are currently at.  If modifications have been
	 * made to the rocket, the rocket is in "dirty" state and this points to the previous
	 * "clean" state.
	 */
	private int undoPosition = -1; // Illegal position, init in constructor
	
	/**
	 * The description of the next action that modifies this rocket.
	 */
	private String nextDescription = null;
	private String storedDescription = null;
	

	private File file = null;
	private int savedID = -1;
	
	private final StorageOptions storageOptions = new StorageOptions();
	

	private final List<DocumentChangeListener> listeners =
			new ArrayList<DocumentChangeListener>();
	
	/* These must be initialized after undo history is set up. */
	private final UndoRedoAction undoAction;
	private final UndoRedoAction redoAction;
	
	
	public OpenRocketDocument(Rocket rocket) {
		this(rocket.getDefaultConfiguration());
	}
	
	
	private OpenRocketDocument(Configuration configuration) {
		this.configuration = configuration;
		this.rocket = configuration.getRocket();
		
		clearUndo();
		
		undoAction = new UndoRedoAction(UndoRedoAction.UNDO);
		redoAction = new UndoRedoAction(UndoRedoAction.REDO);
		
		rocket.addComponentChangeListener(this);
	}
	
	


	public Rocket getRocket() {
		return rocket;
	}
	
	
	public Configuration getDefaultConfiguration() {
		return configuration;
	}
	
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	
	public boolean isSaved() {
		return rocket.getModID() == savedID;
	}
	
	public void setSaved(boolean saved) {
		if (saved == false)
			this.savedID = -1;
		else
			this.savedID = rocket.getModID();
	}
	
	/**
	 * Retrieve the default storage options for this document.
	 * 
	 * @return	the storage options.
	 */
	public StorageOptions getDefaultStorageOptions() {
		return storageOptions;
	}
	
	



	@SuppressWarnings("unchecked")
	public List<Simulation> getSimulations() {
		return (ArrayList<Simulation>) simulations.clone();
	}
	
	public int getSimulationCount() {
		return simulations.size();
	}
	
	public Simulation getSimulation(int n) {
		return simulations.get(n);
	}
	
	public int getSimulationIndex(Simulation simulation) {
		return simulations.indexOf(simulation);
	}
	
	public void addSimulation(Simulation simulation) {
		simulations.add(simulation);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	public void addSimulation(Simulation simulation, int n) {
		simulations.add(n, simulation);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	public void removeSimulation(Simulation simulation) {
		simulations.remove(simulation);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	public Simulation removeSimulation(int n) {
		Simulation simulation = simulations.remove(n);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
		return simulation;
	}
	
	/**
	 * Return a unique name suitable for the next simulation.  The name begins
	 * with {@link #SIMULATION_NAME_PREFIX} and has a unique number larger than any
	 * previous simulation.
	 * 
	 * @return	the new name.
	 */
	public String getNextSimulationName() {
		// Generate unique name for the simulation
		int maxValue = 0;
		for (Simulation s : simulations) {
			String name = s.getName();
			if (name.startsWith(SIMULATION_NAME_PREFIX)) {
				try {
					maxValue = Math.max(maxValue,
							Integer.parseInt(name.substring(SIMULATION_NAME_PREFIX.length())));
				} catch (NumberFormatException ignore) {
				}
			}
		}
		return SIMULATION_NAME_PREFIX + (maxValue + 1);
	}
	
	
	/**
	 * Adds an undo point at this position.  This method should be called *before* any
	 * action that is to be undoable.  All actions after the call will be undone by a 
	 * single "Undo" action.
	 * <p>
	 * The description should be a short, descriptive string of the actions that will 
	 * follow.  This is shown to the user e.g. in the Edit-menu, for example 
	 * "Undo (Modify body tube)".  If the actions are not known (in general should not
	 * be the case!) description may be null.
	 * <p>
	 * If this method is called successively without any change events occurring between the
	 * calls, only the last call will have any effect.
	 * 
	 * @param description A short description of the following actions.
	 */
	public void addUndoPosition(String description) {
		
		if (storedDescription != null) {
			logUndoError("addUndoPosition called while storedDescription=" + storedDescription +
					" description=" + description);
		}
		
		// Check whether modifications have been done since last call
		if (isCleanState()) {
			// No modifications
			log.info("Adding undo position '" + description + "' to " + this + ", document was in clean state");
			nextDescription = description;
			return;
		}
		
		log.info("Adding undo position '" + description + "' to " + this + ", document is in unclean state");
		
		/*
		 * Modifications have been made to the rocket.  We should be at the end of the
		 * undo history, but check for consistency and try to recover.
		 */
		if (undoPosition != undoHistory.size() - 1) {
			logUndoError("undo position inconsistency");
		}
		while (undoPosition < undoHistory.size() - 1) {
			undoHistory.removeLast();
			undoDescription.removeLast();
		}
		

		// Add the current state to the undo history
		undoHistory.add(rocket.copyWithOriginalID());
		undoDescription.add(null);
		nextDescription = description;
		undoPosition++;
		

		// Maintain maximum undo size
		if (undoHistory.size() > UNDO_LEVELS + UNDO_MARGIN && undoPosition > UNDO_MARGIN) {
			for (int i = 0; i < UNDO_MARGIN; i++) {
				undoHistory.removeFirst();
				undoDescription.removeFirst();
				undoPosition--;
			}
		}
	}
	
	
	/**
	 * Start a time-limited undoable operation.  After the operation {@link #stopUndo()}
	 * must be called, which will restore the previous undo description into effect.
	 * Only one level of start-stop undo descriptions is supported, i.e. start-stop
	 * undo cannot be nested, and no other undo operations may be called between
	 * the start and stop calls.
	 * 
	 * @param description	Description of the following undoable operations.
	 */
	public void startUndo(String description) {
		if (storedDescription != null) {
			logUndoError("startUndo called while storedDescription=" + storedDescription +
					" description=" + description);
		}
		log.info("Starting time-limited undoable operation '" + description + "' for " + this);
		String store = nextDescription;
		addUndoPosition(description);
		storedDescription = store;
	}
	
	/**
	 * End the previous time-limited undoable operation.  This must be called after
	 * {@link #startUndo(String)} has been called before any other undo operations are
	 * performed.
	 */
	public void stopUndo() {
		log.info("Ending time-limited undoable operation for " + this + " nextDescription=" +
				nextDescription + "	storedDescription=" + storedDescription);
		String stored = storedDescription;
		storedDescription = null;
		addUndoPosition(stored);
	}
	
	
	public Action getUndoAction() {
		return undoAction;
	}
	
	
	public Action getRedoAction() {
		return redoAction;
	}
	
	
	/**
	 * Clear the undo history.
	 */
	public void clearUndo() {
		log.info("Clearing undo history of " + this);
		undoHistory.clear();
		undoDescription.clear();
		
		undoHistory.add(rocket.copyWithOriginalID());
		undoDescription.add(null);
		undoPosition = 0;
		
		if (undoAction != null)
			undoAction.setAllValues();
		if (redoAction != null)
			redoAction.setAllValues();
	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		
		if (!e.isUndoChange()) {
			if (undoPosition < undoHistory.size() - 1) {
				log.info("Rocket changed while in undo history, removing redo information for " + this +
						" undoPosition=" + undoPosition + " undoHistory.size=" + undoHistory.size() +
						" isClean=" + isCleanState());
			}
			// Remove any redo information if available
			while (undoPosition < undoHistory.size() - 1) {
				undoHistory.removeLast();
				undoDescription.removeLast();
			}
			
			// Set the latest description
			undoDescription.set(undoPosition, nextDescription);
		}
		
		undoAction.setAllValues();
		redoAction.setAllValues();
	}
	
	
	/**
	 * Return whether undo action is available.
	 * @return	<code>true</code> if undo can be performed
	 */
	public boolean isUndoAvailable() {
		if (undoPosition > 0)
			return true;
		
		return !isCleanState();
	}
	
	/**
	 * Return the description of what action would be undone if undo is called.
	 * @return	the description what would be undone, or <code>null</code> if description unavailable.
	 */
	public String getUndoDescription() {
		if (!isUndoAvailable())
			return null;
		
		if (isCleanState()) {
			return undoDescription.get(undoPosition - 1);
		} else {
			return undoDescription.get(undoPosition);
		}
	}
	
	
	/**
	 * Return whether redo action is available.
	 * @return	<code>true</code> if redo can be performed
	 */
	public boolean isRedoAvailable() {
		return undoPosition < undoHistory.size() - 1;
	}
	
	/**
	 * Return the description of what action would be redone if redo is called.
	 * @return	the description of what would be redone, or <code>null</code> if description unavailable.
	 */
	public String getRedoDescription() {
		if (!isRedoAvailable())
			return null;
		
		return undoDescription.get(undoPosition);
	}
	
	
	/**
	 * Perform undo operation on the rocket.
	 */
	public void undo() {
		log.info("Performing undo for " + this + " undoPosition=" + undoPosition +
				" undoHistory.size=" + undoHistory.size() + " isClean=" + isCleanState());
		if (!isUndoAvailable()) {
			logUndoError("Undo not available");
			undoAction.setAllValues();
			redoAction.setAllValues();
			return;
		}
		if (storedDescription != null) {
			logUndoError("undo() called with storedDescription=" + storedDescription);
		}
		
		// Update history position
		
		if (isCleanState()) {
			// We are in a clean state, simply move backwards in history
			undoPosition--;
		} else {
			if (undoPosition != undoHistory.size() - 1) {
				logUndoError("undo position inconsistency");
			}
			// Modifications have been made, save the state and restore previous state
			undoHistory.add(rocket.copyWithOriginalID());
			undoDescription.add(null);
		}
		
		rocket.loadFrom(undoHistory.get(undoPosition).copyWithOriginalID());
	}
	
	
	/**
	 * Perform redo operation on the rocket.
	 */
	public void redo() {
		log.info("Performing redo for " + this + " undoPosition=" + undoPosition +
				" undoHistory.size=" + undoHistory.size() + " isClean=" + isCleanState());
		if (!isRedoAvailable()) {
			logUndoError("Redo not available");
			undoAction.setAllValues();
			redoAction.setAllValues();
			return;
		}
		if (storedDescription != null) {
			logUndoError("redo() called with storedDescription=" + storedDescription);
		}
		
		undoPosition++;
		
		rocket.loadFrom(undoHistory.get(undoPosition).copyWithOriginalID());
	}
	
	
	private boolean isCleanState() {
		return rocket.getModID() == undoHistory.get(undoPosition).getModID();
	}
	
	
	/**
	 * Log a non-fatal undo/redo error or inconsistency.  Reports it to the user the first 
	 * time it occurs, but not on subsequent times.  Logs automatically the undo system state.
	 */
	private void logUndoError(String error) {
		log.error(1, error + ": this=" + this + " undoPosition=" + undoPosition +
				" undoHistory.size=" + undoHistory.size() + " isClean=" + isCleanState() +
				" nextDescription=" + nextDescription + " storedDescription=" + storedDescription,
				new TraceException());
		
		if (!undoErrorReported) {
			undoErrorReported = true;
			ExceptionHandler.handleErrorCondition("Undo/Redo error: " + error);
		}
	}
	
	///////  Listeners
	
	public void addDocumentChangeListener(DocumentChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeDocumentChangeListener(DocumentChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireDocumentChangeEvent(DocumentChangeEvent event) {
		DocumentChangeListener[] array = listeners.toArray(new DocumentChangeListener[0]);
		for (DocumentChangeListener l : array) {
			l.documentChanged(event);
		}
	}
	
	


	/**
	 * Inner class to implement undo/redo actions.
	 */
	private class UndoRedoAction extends AbstractAction {
		public static final int UNDO = 1;
		public static final int REDO = 2;
		
		private final int type;
		
		// Sole constructor
		public UndoRedoAction(int type) {
			if (type != UNDO && type != REDO) {
				throw new IllegalArgumentException("Unknown type = " + type);
			}
			this.type = type;
			setAllValues();
		}
		
		
		// Actual action to make
		public void actionPerformed(ActionEvent e) {
			switch (type) {
			case UNDO:
				log.user("Performing undo, event=" + e);
				undo();
				break;
			
			case REDO:
				log.user("Performing redo, event=" + e);
				redo();
				break;
			}
		}
		
		
		// Set all the values correctly (name and enabled/disabled status)
		public void setAllValues() {
			String name, desc;
			boolean actionEnabled;
			
			switch (type) {
			case UNDO:
				name = "Undo";
				desc = getUndoDescription();
				actionEnabled = isUndoAvailable();
				this.putValue(SMALL_ICON, Icons.EDIT_UNDO);
				break;
			
			case REDO:
				name = "Redo";
				desc = getRedoDescription();
				actionEnabled = isRedoAvailable();
				this.putValue(SMALL_ICON, Icons.EDIT_REDO);
				break;
			
			default:
				throw new BugException("illegal type=" + type);
			}
			
			if (desc != null)
				name = name + " (" + desc + ")";
			
			putValue(NAME, name);
			setEnabled(actionEnabled);
		}
	}
}
