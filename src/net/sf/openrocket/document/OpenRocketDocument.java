package net.sf.openrocket.document;
//TODO: LOW: move class somewhere else?

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
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.Icons;


public class OpenRocketDocument implements ComponentChangeListener {
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
	
	private final Rocket rocket;
	private final Configuration configuration;

	private final ArrayList<Simulation> simulations = new ArrayList<Simulation>();
	
	
	private int undoPosition = -1;  // Illegal position, init in constructor
	private LinkedList<Rocket> undoHistory = new LinkedList<Rocket>();
	private LinkedList<String> undoDescription = new LinkedList<String>();
	
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
		return (ArrayList<Simulation>)simulations.clone();
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
		for (Simulation s: simulations) {
			String name = s.getName();
			if (name.startsWith(SIMULATION_NAME_PREFIX)) {
				try {
					maxValue = Math.max(maxValue, 
							Integer.parseInt(name.substring(SIMULATION_NAME_PREFIX.length())));
				} catch (NumberFormatException ignore) { }
			}
		}
		return SIMULATION_NAME_PREFIX + (maxValue+1);
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

		// Check whether modifications have been done since last call
		if (isCleanState()) {
			// No modifications
			nextDescription = description;
			return;
		}

		
		/*
		 * Modifications have been made to the rocket.  We should be at the end of the
		 * undo history, but check for consistency.
		 */
		assert(undoPosition == undoHistory.size()-1): "Undo inconsistency, report bug!";
		while (undoPosition < undoHistory.size()-1) {
			undoHistory.removeLast();
			undoDescription.removeLast();
		}
		
		
		// Add the current state to the undo history
		undoHistory.add(rocket.copy());
		undoDescription.add(description);
		nextDescription = description;
		undoPosition++;
		
		
		// Maintain maximum undo size
		if (undoHistory.size() > UNDO_LEVELS + UNDO_MARGIN) {
			for (int i=0; i < UNDO_MARGIN+1; i++) {
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
		storedDescription = nextDescription;
		addUndoPosition(description);
	}
	
	/**
	 * End the previous time-limited undoable operation.  This must be called after
	 * {@link #startUndo(String)} has been called before any other undo operations are
	 * performed.
	 */
	public void stopUndo() {
		addUndoPosition(storedDescription);
		storedDescription = null;
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
		undoHistory.clear();
		undoDescription.clear();
		
		undoHistory.add(rocket.copy());
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
			// Remove any redo information if available
			while (undoPosition < undoHistory.size()-1) {
				undoHistory.removeLast();
				undoDescription.removeLast();
			}
			
			// Set the latest description
			undoDescription.set(undoPosition, nextDescription);
		}
		
		undoAction.setAllValues();
		redoAction.setAllValues();
	}

	
	public boolean isUndoAvailable() {
		if (undoPosition > 0)
			return true;
		
		return !isCleanState();
	}
	
	public String getUndoDescription() {
		if (!isUndoAvailable())
			return null;
		
		if (isCleanState()) {
			return undoDescription.get(undoPosition-1);
		} else {
			return undoDescription.get(undoPosition);
		}
	}

	
	public boolean isRedoAvailable() {
		return undoPosition < undoHistory.size()-1;
	}
	
	public String getRedoDescription() {
		if (!isRedoAvailable())
			return null;
		
		return undoDescription.get(undoPosition);
	}
	
	
	
	public void undo() {
		if (!isUndoAvailable()) {
			throw new IllegalStateException("Undo not available.");
		}

		// Update history position
		
		if (isCleanState()) {
			// We are in a clean state, simply move backwards in history
			undoPosition--;
		} else {
			// Modifications have been made, save the state and restore previous state
			undoHistory.add(rocket.copy());
			undoDescription.add(null);
		}
		
		rocket.loadFrom(undoHistory.get(undoPosition).copy());
	}
	
	
	public void redo() {
		if (!isRedoAvailable()) {
			throw new IllegalStateException("Redo not available.");
		}
		
		undoPosition++;
		
		rocket.loadFrom(undoHistory.get(undoPosition).copy());
	}
	
	
	private boolean isCleanState() {
		return rocket.getModID() == undoHistory.get(undoPosition).getModID();
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
		for (DocumentChangeListener l: array) {
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
				throw new IllegalArgumentException("Unknown type = "+type);
			}
			this.type = type;
			setAllValues();
		}

		
		// Actual action to make
		public void actionPerformed(ActionEvent e) {
			switch (type) {
			case UNDO:
				undo();
				break;
				
			case REDO:
				redo();
				break;
			}
		}

		
		// Set all the values correctly (name and enabled/disabled status)
		public void setAllValues() {
			String name,desc;
			boolean enabled;
			
			switch (type) {
			case UNDO:
				name = "Undo";
				desc = getUndoDescription();
				enabled = isUndoAvailable();
				this.putValue(SMALL_ICON, Icons.EDIT_UNDO);
				break;
				
			case REDO:
				name = "Redo";
				desc = getRedoDescription();
				enabled = isRedoAvailable();
				this.putValue(SMALL_ICON, Icons.EDIT_REDO);
				break;
				
			default:
				throw new RuntimeException("EEEK!");
			}
			
			if (desc != null)
				name = name + " ("+desc+")";
			
			putValue(NAME, name);
			setEnabled(enabled);
		}
	}
}
