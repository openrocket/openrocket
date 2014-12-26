package net.sf.openrocket.document;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class describing an entire OpenRocket document, including a rocket and
 * simulations.  The document contains:
 * <p>
 * - the rocket definition
 * - a default Configuration
 * - Simulation instances
 * - the stored file and file save information
 * - undo/redo information
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocketDocument implements ComponentChangeListener {
	private static final Logger log = LoggerFactory.getLogger(OpenRocketDocument.class);
	
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
	private ArrayList<CustomExpression> customExpressions = new ArrayList<CustomExpression>();
	
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
	
	
	private ArrayList<UndoRedoListener> undoRedoListeners = new ArrayList<UndoRedoListener>(2);
	
	private File file = null;
	private int savedID = -1;
	
	private final StorageOptions storageOptions = new StorageOptions();
	
	private final DecalRegistry decalRegistry = new DecalRegistry();
	
	private final List<DocumentChangeListener> listeners = new ArrayList<DocumentChangeListener>();
	
	OpenRocketDocument(Rocket rocket) {
		this.configuration = rocket.getDefaultConfiguration();
		this.rocket = rocket;
		init();
	}
	
	private void init() {
		clearUndo();
		
		rocket.addComponentChangeListener(this);
	}
	
	public void addCustomExpression(CustomExpression expression) {
		if (customExpressions.contains(expression)) {
			log.info(Markers.USER_MARKER, "Could not add custom expression " + expression.getName() + " to document as document alerady has a matching expression.");
		} else {
			customExpressions.add(expression);
		}
	}
	
	public void removeCustomExpression(CustomExpression expression) {
		customExpressions.remove(expression);
	}
	
	public List<CustomExpression> getCustomExpressions() {
		return customExpressions;
	}
	
	/*
	 * Returns a set of all the flight data types defined or available in any way in the rocket document
	 */
	public Set<FlightDataType> getFlightDataTypes() {
		Set<FlightDataType> allTypes = new LinkedHashSet<FlightDataType>();
		
		// built in
		Collections.addAll(allTypes, FlightDataType.ALL_TYPES);
		
		// custom expressions
		for (CustomExpression exp : customExpressions) {
			allTypes.add(exp.getType());
		}
		
		// simulation listeners
		for (Simulation sim : simulations) {
			for (SimulationExtension c : sim.getSimulationExtensions()) {
				allTypes.addAll(c.getFlightDataTypes());
			}
		}
		
		// imported data
		/// not implemented yet
		
		
		return allTypes;
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
	
	
	public Collection<DecalImage> getDecalList() {
		
		return decalRegistry.getDecalList();
		
	}
	
	public int countDecalUsage(DecalImage img) {
		int count = 0;
		
		Iterator<RocketComponent> it = rocket.iterator();
		while (it.hasNext()) {
			RocketComponent comp = it.next();
			Appearance a = comp.getAppearance();
			if (a == null) {
				continue;
			}
			Decal d = a.getTexture();
			if (d == null) {
				continue;
			}
			if (img.equals(d.getImage())) {
				count++;
			}
		}
		return count;
	}
	
	public DecalImage makeUniqueDecal(DecalImage img) {
		if (countDecalUsage(img) <= 1) {
			return img;
		}
		return decalRegistry.makeUniqueImage(img);
	}
	
	public DecalImage getDecalImage(Attachment a) {
		return decalRegistry.getDecalImage(a);
	}
	
	public List<Simulation> getSimulations() {
		return simulations.clone();
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
	
	public void removeFlightConfigurationAndSimulations(String configId) {
		if (configId == null) {
			return;
		}
		for (Simulation s : getSimulations()) {
			// Assumes modifiable collection - which it is
			if (configId.equals(s.getConfiguration().getFlightConfigurationID())) {
				removeSimulation(s);
			}
		}
		rocket.removeFlightConfigurationID(configId);
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
		
		fireUndoRedoChangeEvent();
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
		
		fireUndoRedoChangeEvent();
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
			fireUndoRedoChangeEvent();
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
		
		rocket.checkComponentStructure();
		undoHistory.get(undoPosition).checkComponentStructure();
		undoHistory.get(undoPosition).copyWithOriginalID().checkComponentStructure();
		rocket.loadFrom(undoHistory.get(undoPosition).copyWithOriginalID());
		rocket.checkComponentStructure();
	}
	
	
	/**
	 * Perform redo operation on the rocket.
	 */
	public void redo() {
		log.info("Performing redo for " + this + " undoPosition=" + undoPosition +
				" undoHistory.size=" + undoHistory.size() + " isClean=" + isCleanState());
		if (!isRedoAvailable()) {
			logUndoError("Redo not available");
			fireUndoRedoChangeEvent();
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
		log.error(error + ": this=" + this + " undoPosition=" + undoPosition +
				" undoHistory.size=" + undoHistory.size() + " isClean=" + isCleanState() +
				" nextDescription=" + nextDescription + " storedDescription=" + storedDescription,
				new Throwable());
		
		if (!undoErrorReported) {
			undoErrorReported = true;
			Application.getExceptionHandler().handleErrorCondition("Undo/Redo error: " + error);
		}
	}
	
	
	
	/**
	 * Return a copy of this document.  The rocket is copied with original ID's, the default
	 * motor configuration ID is maintained and the simulations are copied to the new rocket.
	 * No undo/redo information or file storage information is maintained.
	 * 
	 * This function is used from the Optimization routine to store alternatives of the same rocket.
	 * For now we can assume that the copy returned does not have any of the attachment factories in place.
	 * 
	 * @return	a copy of this document.
	 */
	public OpenRocketDocument copy() {
		Rocket rocketCopy = rocket.copyWithOriginalID();
		OpenRocketDocument documentCopy = OpenRocketDocumentFactory.createDocumentFromRocket(rocketCopy);
		documentCopy.getDefaultConfiguration().setFlightConfigurationID(configuration.getFlightConfigurationID());
		for (Simulation s : simulations) {
			documentCopy.addSimulation(s.duplicateSimulation(rocketCopy));
		}
		return documentCopy;
	}
	
	
	
	///////  Listeners
	
	public void addUndoRedoListener(UndoRedoListener listener) {
		undoRedoListeners.add(listener);
	}
	
	public void removeUndoRedoListener(UndoRedoListener listener) {
		undoRedoListeners.remove(listener);
	}
	
	private void fireUndoRedoChangeEvent() {
		UndoRedoListener[] array = undoRedoListeners.toArray(new UndoRedoListener[0]);
		for (UndoRedoListener l : array) {
			l.setAllValues();
		}
		
	}
	
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
	
	
	
	
}
