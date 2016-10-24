package net.sf.openrocket.document;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.document.events.SimulationChangeEvent;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;

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
	
	/**
	 * main constructor, enable events in the rocket 
	 * and initializes the document 
	 * @param rocket	the rocket to be used in the document
	 */
	OpenRocketDocument(Rocket rocket) {
		this.rocket = rocket;
		rocket.enableEvents();
		init();
	}
	
	/**
	 * initializes the document, clearing the undo cache and
	 * setting itself as a listener for changes in the rocket
	 */
	private void init() {
		clearUndo();
		rocket.addComponentChangeListener(this);
	}
	
	/**
	 * adds a customExpression into the list
	 * @param expression	the expression to be added
	 */
	public void addCustomExpression(CustomExpression expression) {
		if (customExpressions.contains(expression)) {
			log.info(Markers.USER_MARKER, "Could not add custom expression " + expression.getName() + " to document as document alerady has a matching expression.");
		} 
		customExpressions.add(expression);
	}
	
	/**
	 * remves
	 * @param expression
	 */
	public void removeCustomExpression(CustomExpression expression) {
		customExpressions.remove(expression);
	}
	
	//TODO:LOW:this leaves the object custom expression exposed, is it supposed to be like that?
	/**
	 * 
	 * @return
	 */
	public List<CustomExpression> getCustomExpressions() {
		return customExpressions;
	}
	
	/**
	 * @returns a set of all the flight data types defined or available in any way in the rocket document
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
	
	/**
	 * gets the rocket in the document
	 * @return	the rocket in the document
	 */
	public Rocket getRocket() {
		return rocket;
	}
	
	/**
	 * returns the selected configuration from the rocket
	 * @return	selected configuration from the rocket
	 */
	public FlightConfiguration getSelectedConfiguration() {
		return rocket.getSelectedConfiguration();
	}
	
	/**
	 * returns the File handler object for the document
	 * @return	the File handler object for the document
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * set the file handler object for the document
	 * @param file	the new file handler object
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * returns if the current rocket is saved
	 * @return	if the current rocket is saved
	 */
	public boolean isSaved() {
		return rocket.getModID() == savedID;
	}
	
	/**
	 * sets the current rocket as saved, and none if false is given
	 * @param saved	if the current rocket or none will be set to save
	 */
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
	
	
	/**
	 * returns the decal list used in the document
	 * @return	the decal list registered in the document
	 */
	public Collection<DecalImage> getDecalList() {
		
		return decalRegistry.getDecalList();
		
	}
	
	/**
	 * returns the number of times the given decal was used
	 * @param img	the decal to be counted
	 * @return		the number of times 
	 */
	public int countDecalUsage(DecalImage img) {
		int count = 0;
		
		Iterator<RocketComponent> it = rocket.iterator();
		while (it.hasNext()) {
			if(hasDecal(it.next(),img))
				count++;
		}
		return count;
	}
	
	//TODO: LOW: move this method to rocketComponent, Appearance and decal
	//I see 3 layers of object accessed, seems unsafe
	/**
	 * checks if a rocket component has the given decalImage
	 * @param comp	the RocketComponent to be searched
	 * @param img	the DecalImage to be checked
	 * @return	if the comp has img
	 */
	private boolean hasDecal(RocketComponent comp, DecalImage img){
		Appearance a = comp.getAppearance();
		if(a == null)
			return false;
		Decal d = a.getTexture();
		if(d == null)
			return false;
		if(img.equals(d.getImage()))
			return true;
		return false;
	}
	
	/**
	 * gets a unique identification for the given decal
	 * @param img	the decal to be made unique
	 * @return		the new unique decal
	 */
	public DecalImage makeUniqueDecal(DecalImage img) {
		if (countDecalUsage(img) <= 1) {
			return img;
		}
		return decalRegistry.makeUniqueImage(img);
	}
	
	/**
	 * gets the decal image from an attachment
	 * @param a	the attachment
	 * @return	the image from the attachment
	 */
	public DecalImage getDecalImage(Attachment a) {
		return decalRegistry.getDecalImage(a);
	}
	
	/**
	 * gets a list of simulations in the document
	 * @return	the simulations in the document
	 */
	public List<Simulation> getSimulations() {
		return simulations.clone();
	}
	
	/**
	 * gets the number of simulations in the document
	 * @return	the number of simulations in the document
	 */
	public int getSimulationCount() {
		return simulations.size();
	}
	
	/**
	 * the the Nth simulation from the document
	 * @param n	simulation index
	 * @return	the Nth simulation from the document, null if there's none
	 */
	public Simulation getSimulation(int n) {
		return simulations.get(n);
	}
	
	/**
	 * gets the index of the given simulation
	 * @param simulation	the simulation being searched
	 * @return				the index of the simulation in the document
	 */
	public int getSimulationIndex(Simulation simulation) {
		return simulations.indexOf(simulation);
	}
	
	/**
	 * adds simulation into the document
	 * fires document change event
	 * @param simulation	the simulation to be added
	 */
	public void addSimulation(Simulation simulation) {
		simulations.add(simulation);
		FlightConfigurationId simId = simulation.getId();
		if( !rocket.containsFlightConfigurationID( simId )){
			rocket.createFlightConfiguration(simId);
		}
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	/**
	 * adds the simulation to the Nth index, overwriting if there is already one
	 * fires change document event
	 * @param simulation	the simulation to be added
	 * @param n				the index to be added
	 */
	public void addSimulation(Simulation simulation, int n) {
		simulations.add(n, simulation);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	/**
	 * removes the specific simulation from the list
	 * @param simulation	the simulation to be removed
	 */
	public void removeSimulation(Simulation simulation) {
		simulations.remove(simulation);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
	}
	
	/**
	 * removes the Nth simulation from the document
	 * fires document change event
	 * @param n	the Nth simulation
	 * @return	the removed simulation
	 */
	public Simulation removeSimulation(int n) {
		Simulation simulation = simulations.remove(n);
		fireDocumentChangeEvent(new SimulationChangeEvent(simulation));
		return simulation;
	}
	
	/**
	 * removes the flight configuration and simulation with the specific id
	 * @param configId
	 */
	public void removeFlightConfigurationAndSimulations(FlightConfigurationId configId) {
		if (configId == null) {
			return;
		}
		removeSimulations(configId);
		rocket.removeFlightConfiguration(configId);
	}

	/**
	 * removes all simulations with the specific configId
	 * @param configId	the Flight Configuration Id that dictates which simulations shoul be removed
	 */
	private void removeSimulations(FlightConfigurationId configId) {
		for (Simulation s : getSimulations()) {
			// Assumes modifiable collection - which it is
			if (configId.equals(s.getId())) {
				removeSimulation(s);
			}
		}
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
		
		checkDescription(description);
		
		// Check whether modifications have been done since last call
		if(isCheckNoModification(description))
			return;
		log.info("Adding undo position '" + description + "' to " + this + ", document is in unclean state");
		checkUndoPositionConsistency();
		addStateToUndoHistory(description);
		
		maintainMaximumUndoSize();
	}

	/**
	 * 
	 */
	private void maintainMaximumUndoSize() {
		if (undoHistory.size() > UNDO_LEVELS + UNDO_MARGIN && undoPosition > UNDO_MARGIN) {
			for (int i = 0; i < UNDO_MARGIN; i++) {
				undoHistory.removeFirst();
				undoDescription.removeFirst();
				undoPosition--;
			}
		}
	}

	/**
	 * @param description
	 */
	private void addStateToUndoHistory(String description) {
		// Add the current state to the undo history
		undoHistory.add(rocket.copyWithOriginalID());
		undoDescription.add(null);
		nextDescription = description;
		undoPosition++;
	}

	/**
	 * checks if there was or not modification, and logs
	 * 
	 * @param description	the description to be used in the log
	 * @return	if there was or not modification
	 */
	private boolean isCheckNoModification(String description){
		if (isCleanState()) {
			// No modifications
			log.info("Adding undo position '" + description + "' to " + this + ", document was in clean state");
			nextDescription = description;
			return true;
		}
		return false;
	}
	
	/**
	 * checks if the document already has a stored undo description
	 * logs if it has
	 * 
	 * @param description	undo description to be logged
	 */
	private void checkDescription(String description) {
		if (storedDescription != null) {
			logUndoError("addUndoPosition called while storedDescription=" + storedDescription +
					" description=" + description);
		}
	}

	/**
	 * If modifications have been made to the rocket.  We should be at the end of the
	 * undo history, but check for consistency and try to recover.
	 */
	private void checkUndoPositionConsistency() {
		if (undoPosition != undoHistory.size() - 1) {
			logUndoError("undo position inconsistency");
		}
		removeRedoInfo();
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
		//log.info("Clearing undo history of " + this);
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
			removeRedoInfo();
			setLatestDescription();
		}
		
		fireUndoRedoChangeEvent();
	}

	/**
	 * Sets the latest description
	 */
	private void setLatestDescription() {
		undoDescription.set(undoPosition, nextDescription);
	}

	/**
	 * Removes any redo information if available
	 */
	private void removeRedoInfo() {
		while (undoPosition < undoHistory.size() - 1) {
			undoHistory.removeLast();
			undoDescription.removeLast();
		}
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
	
	public String toSimulationDetail(){
		StringBuilder str = new StringBuilder();
		str.append(">> Dumping simulation list:\n");
		int simNum = 0; 
		for( Simulation s : this.simulations ){
			str.append(String.format("    [%d] %s (%s) \n", simNum, s.getName(), s.getId().toShortKey() ));
			simNum++;
		}
		
		return str.toString();
	}
	
	
	
}
