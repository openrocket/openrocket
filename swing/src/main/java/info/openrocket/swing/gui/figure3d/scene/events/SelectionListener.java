package info.openrocket.swing.gui.figure3d.scene.events;

import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;

import java.util.List;

/**
 * Listener for scene selection changes.
 */
@FunctionalInterface
public interface SelectionListener {
	void onSelectionChanged(List<SceneObject> selectedObjects);
}

