package net.sf.openrocket.gui.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public final class OpenRocketClipboard {

	private static final List<RocketComponent> clipboardComponents = new LinkedList<>();
	private static Simulation[] clipboardSimulations = null;
	
	private static final List<ClipboardListener> listeners = new ArrayList<ClipboardListener>();
	
	private OpenRocketClipboard() {
		// Disallow instantiation
	}
	
	
	/**
	 * Return the <code>RocketComponent</code> contained in the clipboard, or
	 * <code>null</code>.  The component is returned verbatim, and must be copied
	 * before attaching to any rocket design!
	 * 
	 * @return	the rocket component contained in the clipboard, or <code>null</code>
	 * 			if the clipboard does not currently contain a rocket component.
	 */
	public static List<RocketComponent> getClipboardComponents() {
		return clipboardComponents;
	}
	
	
	public static void setClipboard(List<RocketComponent> components) {
		clipboardComponents.clear();
		components = RocketActions.copyComponentsMaintainParent(components);
		if (components != null) {
			clipboardComponents.addAll(components);
		}
		filterClipboardComponents(new LinkedList<>(clipboardComponents));
		clipboardSimulations = null;
		fireClipboardChanged();
	}

	/**
	 * Filters out children from clipboardComponents and the children's list of the components, based on the selection
	 * (see {@link #filterClipboardComponent} to see how the actual filtering is done.
	 * @param components components to be filtered
	 */
	private static void filterClipboardComponents(List<RocketComponent> components) {
		if (components == null || components.size() == 0) return;
		List<RocketComponent> checkedComponents = new ArrayList<>();
		for (RocketComponent component : components) {
			// Make sure a parent component is processed before the child components
			RocketComponent temp = component;
			while (temp.getParent() != null && clipboardComponents.contains(temp.getParent())
					&& !checkedComponents.contains(temp.getParent())) {
				filterClipboardComponent(temp.getParent());
				checkedComponents.add(temp.getParent());
				temp = temp.getParent();
			}
			filterClipboardComponent(component);
			checkedComponents.add(component);
		}
	}

	/**
	 * Iteratively (over all children of component and their children), filter clipboard components according to
	 * the following rules:
	 * 	- If all children of component are selected, then remove those children from clipboardComponents, but keep them
	 * 	  as children of component
	 * 	- If one of the children of component is selected, but some are not, then remove the unselected children as child
	 * 	  from component, but keep the selected children as children of component + remove the selected children from
	 * 	  clipboardComponents
	 * 	- If no children are selected, then no children are removed from component + no children are removed from
	 * 	  clipboardComponents
	 * @param component component to filter
	 */
	private static void filterClipboardComponent(RocketComponent component) {
		if (component == null) return;

		boolean allChildrenSelected = clipboardComponents.containsAll(component.getChildren());
		boolean someChildrenSelected = false;
		for (RocketComponent child : component.getChildren()) {
			if (clipboardComponents.contains(child)) {
				someChildrenSelected = true;
				break;
			}
		}

		if (allChildrenSelected) {
			for (RocketComponent child : component.getChildren()) {
				clipboardComponents.remove(child);
				filterClipboardComponents(child.getChildren());
			}
			return;
		}

		if (someChildrenSelected) {
			for (RocketComponent child : component.getChildren()) {
				if (!clipboardComponents.contains(child)) {
					component.removeChild(child);
				} else {
					clipboardComponents.remove(child);
					filterClipboardComponents(child.getChildren());
				}
			}
		} else {
			for (RocketComponent child : component.getChildren()) {
				filterClipboardComponents(child.getChildren());
			}
		}
	}
	
	
	public static Simulation[] getClipboardSimulations() {
		if (clipboardSimulations == null || clipboardSimulations.length == 0)
			return null;
		return clipboardSimulations.clone();
	}
	
	public static void setClipboard(Simulation[] simulations) {
		clipboardSimulations = simulations.clone();
		clipboardComponents.clear();
		fireClipboardChanged();
	}
	
	
	
	public static void addClipboardListener(ClipboardListener listener) {
		listeners.add(listener);
	}
	
	public static void removeClipboardListener(ClipboardListener listener) {
		listeners.remove(listener);
	}
	
	private static void fireClipboardChanged() {
		ClipboardListener[] array = listeners.toArray(new ClipboardListener[0]);
		for (ClipboardListener l: array) {
			l.clipboardChanged();
		}
	}
	
}
