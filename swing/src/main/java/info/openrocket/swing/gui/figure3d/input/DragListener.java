package info.openrocket.swing.gui.figure3d.input;

import org.joml.Vector3f;

/**
 * A functional interface for handling drag events on a SceneObject.
 */
@FunctionalInterface
public interface DragListener {
	/**
	 * Called when a draggable object is being dragged.
	 * @param newPosition The new position of the object in world space, based on the drag.
	 */
	void onDrag(Vector3f newPosition);
}