package info.openrocket.swing.gui.figure3d.scene.core;

import info.openrocket.swing.gui.figure3d.math.Raycaster;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.events.SelectionListener;
import org.joml.Vector3f;

import java.util.List;

/**
 * Read-only view of the scene for rendering and UI consumers.
 */
public interface SceneView {
	/**
	 * Gets the camera associated with this scene.
	 *
	 * @return the Camera instance used for viewing this scene
	 */
	Camera getCamera();

	/**
	 * Adds a scene object to the 3D environment.
	 * The object will be rendered and made available for interaction.
	 *
	 * @param object the SceneObject to add to the scene
	 */
	void addObject(SceneObject object);

	/**
	 * Gets all scene objects currently in the 3D environment.
	 *
	 * @return an unmodifiable view of all SceneObjects in the scene
	 */
	List<SceneObject> getObjects();

	/**
	 * Gets all currently selected objects in the scene.
	 * @return a list of selected SceneObjects
	 */
	List<SceneObject> getSelectedObjects();

	/**
	 * Updates the selection state based on a raycast intersection.
	 * If the intersected object is part of a RocketComponent, all objects
	 * belonging to that same component will be selected as a group.
	 * @param raycaster The raycaster to use for intersection testing.
	 * @param isMultiSelect Whether to add to existing selection (true) or replace it (false).
	 */
	void updateSelection(Raycaster raycaster, boolean isMultiSelect);

	/**
	 * Programmatically set the current selection to the given components.
	 * @param components components to select (null or empty clears selection)
	 */
	void setSelection(List<SceneObject> components);

	void addSelectionListener(SelectionListener listener);

	void removeSelectionListener(SelectionListener listener);

	/**
	 * Gets all particle emitters currently active in the scene.
	 *
	 * @return a list of all ParticleEmitters in the scene
	 */
	List<ParticleEmitter> getParticleEmitters();

	/**
	 * Gets the background settings for the scene.
	 * @return the Background instance defining the scene's background
	 */
	Background getBackground();

	/**
	 * Sets the background for the scene.
	 * @param background the Background instance to set
	 */
	void setBackground(Background background);

	/**
	 * Checks if fog effects are enabled in the scene.
	 * @return true if fog is enabled, false otherwise
	 */
	boolean isFogEnabled();

	/**
	 * Gets the density of the fog effect in the scene.
	 * @return the fog density value
	 */
	float getFogDensity();

	/**
	 * Gets the light controller for managing scene lighting.
	 * @return the LightController instance
	 */
	LightController getLightController();

	/* --------------------------------------------------------------
	 * PARTICLE SYSTEM
	 * -------------------------------------------------------------
	 */

	/**
	 * Adds a particle emitter to the scene for dynamic effects.
	 * Particle emitters can create effects like flames, smoke, sparks, etc.
	 *
	 * @param emitter the ParticleEmitter to add to the scene
	 */
	void addParticleEmitter(ParticleEmitter emitter);

	/**
	 * Updates all particle emitters in the scene.
	 * This method should be called each frame to animate particle effects.
	 *
	 * @param deltaTime the time elapsed since the last update in seconds
	 */
	void updateParticles(float deltaTime);

	/**
	 * Updates the rocket rotation pivot to match the camera's current centerOfInterest,
	 * adjusting model matrices so the rocket's visual position stays the same.
	 * Should be called at drag-start (rotation) and after horizontal pan.
	 */
	default void updateRocketPivotFromCamera() {}

	/**
	 * Transforms a point from rocket-local world space through the current rocket drag rotation.
	 *
	 * @param point the point to transform
	 * @param destination the destination vector, or null to allocate one
	 * @return the transformed point
	 */
	default Vector3f transformRocketPoint(Vector3f point, Vector3f destination) {
		if (destination == null) {
			destination = new Vector3f();
		}
		return destination.set(point);
	}

	/**
	 * Cleans up all resources associated with the scene.
	 * This method should be called when the scene is no longer needed
	 * to ensure proper disposal of GPU resources and memory.
	 */
	void cleanup();
}
