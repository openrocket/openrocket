package net.sf.openrocket.gui.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.main.componenttree.ComponentTreeModel;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class DocumentSelectionModel {

	private static final Simulation[] NO_SIMULATION = new Simulation[0];

	private final ComponentTreeSelectionListener componentTreeSelectionListener =
		new ComponentTreeSelectionListener();
	private final SimulationListSelectionListener simulationListSelectionListener =
		new SimulationListSelectionListener();

	
	private final OpenRocketDocument document;
	
	private RocketComponent componentSelection = null;
	private Simulation[] simulationSelection = NO_SIMULATION;

	private TreeSelectionModel componentTreeSelectionModel = null; 
	private ListSelectionModel simulationListSelectionModel = null;

	private final List<DocumentSelectionListener> listeners =
		new ArrayList<DocumentSelectionListener>();

	
	
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
	 * if no rocket component is selected.
	 * 
	 * @return	the currently selected rocket component, or <code>null</code>.
	 */
	public RocketComponent getSelectedComponent() {
		return componentSelection;
	}
	
	public void setSelectedComponent(RocketComponent component) {
		componentSelection = component;
		clearSimulationSelection();
	
		TreePath path = ComponentTreeModel.makeTreePath(component);
		componentTreeSelectionModel.setSelectionPath(path);
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
		if (componentSelection == null)
			return;
		
		componentSelection = null;
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
			TreePath path = componentTreeSelectionModel.getSelectionPath();
			if (path == null) {
				componentSelection = null;
				fireDocumentSelection(DocumentSelectionListener.COMPONENT_SELECTION_CHANGE);
				return;
			}
			
			componentSelection = (RocketComponent)path.getLastPathComponent();
			
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
			
			ArrayList<Simulation> list = new ArrayList<Simulation>();
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
