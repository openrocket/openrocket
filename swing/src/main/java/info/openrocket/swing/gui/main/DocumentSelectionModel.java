package info.openrocket.swing.gui.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import info.openrocket.swing.gui.main.componenttree.ComponentTreeModel;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class DocumentSelectionModel {

	private static final Simulation[] NO_SIMULATION = new Simulation[0];

	private final ComponentTreeSelectionListener componentTreeSelectionListener =
		new ComponentTreeSelectionListener();
	private final SimulationListSelectionListener simulationListSelectionListener =
		new SimulationListSelectionListener();

	
	private final OpenRocketDocument document;
	
	private final List<RocketComponent> componentSelection = new LinkedList<>();
	private Simulation[] simulationSelection = NO_SIMULATION;

	private TreeSelectionModel componentTreeSelectionModel = null; 
	private ListSelectionModel simulationListSelectionModel = null;

	private final List<DocumentSelectionListener> listeners =
			new ArrayList<>();

	
	
	public DocumentSelectionModel(OpenRocketDocument document) {
		this.document = document;
	}
	



	/**
	 * Return the currently selected simulations.  Returns an empty array if none
	 * are selected.
	 * 
	 * @return	an array of the currently selected simulations, may be of zero length.
	 */
	public Simulation[] getSelectedSimulations() {
		return Arrays.copyOf(simulationSelection, simulationSelection.length);
	}
	
	public void setSelectedSimulations(Simulation[] sims) {
		simulationSelection = sims;
		clearComponentSelection();

		simulationListSelectionModel.clearSelection();
		for (Simulation s: sims) {
			int index = document.getSimulationIndex(s);
			if (index >= 0) {
				simulationListSelectionModel.addSelectionInterval(index, index);
			}
		}
	}

	/**
	 * Return the currently selected rocket component.  Returns <code>null</code>
	 * if no rocket component is selected. If there is more than one component selected,
	 * the first selected component is returned.
	 * 
	 * @return	the currently selected rocket component, or <code>null</code>.
	 */
	public RocketComponent getSelectedComponent() {
		if (componentSelection.size() == 0) return null;
		return componentSelection.get(0);
	}

	/**
	 * Return the currently selected rocket components.  Returns <code>null</code>
	 * if no rocket component is selected.
	 *
	 * @return	the currently selected rocket components, or <code>null</code>.
	 */
	public List<RocketComponent> getSelectedComponents() {
		if (componentSelection.size() == 0) return Collections.emptyList();
		return componentSelection;
	}
	
	public void setSelectedComponent(RocketComponent component) {
		componentSelection.clear();
		componentSelection.add(component);
		clearSimulationSelection();
	
		TreePath path = ComponentTreeModel.makeTreePath(component);
		componentTreeSelectionModel.setSelectionPath(path);
	}

	public void setSelectedComponents(List<RocketComponent> components) {
		componentSelection.clear();
		componentSelection.addAll(components);
		clearSimulationSelection();

		List<TreePath> paths = ComponentTreeModel.makeTreePaths(components);
		componentTreeSelectionModel.setSelectionPaths(paths.toArray(new TreePath[0]));
	}

	

	public void attachComponentTreeSelectionModel(TreeSelectionModel model) {
		if (componentTreeSelectionModel != null)
			componentTreeSelectionModel.removeTreeSelectionListener(
					componentTreeSelectionListener);

		componentTreeSelectionModel = model;
		if (model != null)
			model.addTreeSelectionListener(componentTreeSelectionListener);
		clearComponentSelection();
	}
	
	
	
	public void attachSimulationListSelectionModel(ListSelectionModel model) {
		if (simulationListSelectionModel != null)
			simulationListSelectionModel.removeListSelectionListener(
					simulationListSelectionListener);
		
		simulationListSelectionModel = model;
		if (model != null)
			model.addListSelectionListener(simulationListSelectionListener);
		clearSimulationSelection();
	}

	
	
	public void clearSimulationSelection() {
		if (simulationSelection.length == 0)
			return;
		
		simulationSelection = NO_SIMULATION;
		if (simulationListSelectionModel != null)
			simulationListSelectionModel.clearSelection();
		
		fireDocumentSelection(DocumentSelectionListener.SIMULATION_SELECTION_CHANGE);
	}
	
	
	public void clearComponentSelection() {
		if (componentSelection.size() == 0)
			return;
		
		componentSelection.clear();
		if (componentTreeSelectionModel != null)
			componentTreeSelectionModel.clearSelection();
		
		fireDocumentSelection(DocumentSelectionListener.COMPONENT_SELECTION_CHANGE);
	}


	
	public void addDocumentSelectionListener(DocumentSelectionListener l) {
		listeners.add(l);
	}
	
	public void removeDocumentSelectionListener(DocumentSelectionListener l) {
		listeners.remove(l);
	}
	
	protected void fireDocumentSelection(int type) {
		DocumentSelectionListener[] array = 
			listeners.toArray(new DocumentSelectionListener[0]);
		
		for (DocumentSelectionListener l: array) {
			l.valueChanged(type);
		}
	}
	
	
	
	private class ComponentTreeSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath[] paths = componentTreeSelectionModel.getSelectionPaths();
			if (paths == null || paths.length == 0) {
				componentSelection.clear();
				fireDocumentSelection(DocumentSelectionListener.COMPONENT_SELECTION_CHANGE);
				return;
			}

			componentSelection.clear();
			for (TreePath path : paths) {
				componentSelection.add((RocketComponent)path.getLastPathComponent());
			}
			
			clearSimulationSelection();
			fireDocumentSelection(DocumentSelectionListener.COMPONENT_SELECTION_CHANGE);
		}

	}
	
	private class SimulationListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int min = simulationListSelectionModel.getMinSelectionIndex();
			int max = simulationListSelectionModel.getMaxSelectionIndex();
			if (min < 0 || max < 0) {
				simulationSelection = NO_SIMULATION;
				fireDocumentSelection(DocumentSelectionListener.SIMULATION_SELECTION_CHANGE);
				return;
			}
			
			ArrayList<Simulation> list = new ArrayList<>();
			for (int i = min; i <= max; i++) {
				if (simulationListSelectionModel.isSelectedIndex(i) && 
						(i < document.getSimulationCount())) {
					list.add(document.getSimulation(i));
				}
			}
			simulationSelection = list.toArray(NO_SIMULATION);
			
			clearComponentSelection();
			fireDocumentSelection(DocumentSelectionListener.SIMULATION_SELECTION_CHANGE);
		}
		
	}

}
