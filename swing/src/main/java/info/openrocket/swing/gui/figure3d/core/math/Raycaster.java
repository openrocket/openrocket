package info.openrocket.swing.gui.figure3d.core.math;

import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import org.joml.Vector3f;

import java.util.List;

/**
 * Interface for raycasting from 2D screen space into 3D scene objects.
 */
public interface Raycaster {
    /**
     * Update the internal ray based on mouse position and camera.
     */
    void update(float mouseX, float mouseY, int viewportWidth, int viewportHeight, Camera camera);

    /**
     * Return the closest intersected object from a list or null.
     */
    SceneObject getIntersectedObject(List<SceneObject> objects);

    /**
     * Get current ray origin in world space.
     */
    Vector3f getRayOrigin();

    /**
     * Get current ray direction (normalized).
     */
    Vector3f getRayDirection();
}

