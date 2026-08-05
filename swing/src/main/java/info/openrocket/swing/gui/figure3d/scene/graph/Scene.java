package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.math.Raycaster;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightManager;
import info.openrocket.swing.gui.figure3d.scene.events.SelectionListener;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Mutable scene containing rocket objects, camera, lights, particles, and selection state.
 */
public class Scene implements SceneView {

	private final List<SceneObject> objects;
	private final List<ParticleEmitter> particleEmitters;
	private Camera camera;
	private final LightManager lightManager;
	private Background background;
	private final List<SceneObject> selectedObjects = new ArrayList<>();
	private final List<SelectionListener> selectionListeners = new ArrayList<>();
	private boolean fogEnabled = false;
	private float fogDensity = 0.07f;
	private final RenderingConfiguration config;

	private final Rocket rocket;
	private final Matrix4f rocketRotationMatrix = new Matrix4f();
	private final Matrix4f scratchRocketTransform = new Matrix4f();
	private final Matrix4f scratchRotationMatrix = new Matrix4f();
	private final Matrix4f scratchModelMatrix = new Matrix4f();
	private final Matrix4f scratchPreviousRocketRotation = new Matrix4f();
	private final Matrix4f scratchInverseRotationMatrix = new Matrix4f();
	private final Vector3f rocketRotationPivot = new Vector3f();
	private final Vector3f overriddenRocketRotationPivot = new Vector3f();
	private boolean rocketRotationPivotOverridden = false;
	private float rocketDragYaw = 0.0f;
	private float rocketDragRoll = 0.0f;

	private Scene(Rocket rocket, Camera camera, RenderingConfiguration config, Light light) {
		this.rocket = rocket;
		this.camera = camera;
		this.objects = new ArrayList<>();
		this.particleEmitters = new ArrayList<>();
		this.lightManager = new LightManager(this);
		this.lightManager.setVisualizersVisible(config.getVisualEffects().areLightVisualizersVisible());
		this.lightManager.addLight(light);
		this.background = new SolidColorBackground(0.1f, 0.1f, 0.12f);
		this.config = config;
	}

	public Scene(Rocket rocket, Camera camera, RenderingConfiguration config) {
		this(rocket, camera, config, Light.directional(-0.5f, -1.0f, -0.5f));
	}

	/**
	 * Gets the rocket model that this scene visualizes.
	 * 
	 * @return the OpenRocket Rocket instance associated with this scene
	 */
	public Rocket getRocket() {
		return rocket;
	}

	@Override
	public void addObject(SceneObject object) {
		objects.add(object);
	}

	@Override
	public void removeObject(SceneObject object) {
		objects.remove(object);
	}

	@Override
	public void removeObjectsIf(Predicate<SceneObject> predicate) {
		objects.removeIf(predicate);
	}

	@Override
	public void clearObjects() {
		objects.clear();
	}

	/**
	 * Adds multiple scene objects to the 3D environment in a single operation.
	 * All objects will be rendered and made available for interaction.
	 * 
	 * @param objects the list of SceneObjects to add to the scene
	 * @throws IllegalArgumentException if the objects list is null or empty
	 */
	public void addObjects(List<SceneObject> objects) {
		if (objects == null || objects.isEmpty()) {
			throw new IllegalArgumentException("Objects list cannot be null or empty");
		}
		this.objects.addAll(objects);
	}

	@Override
	public List<SceneObject> getObjects() {
		return Collections.unmodifiableList(objects);
	}

	@Override
	public void addParticleEmitter(ParticleEmitter emitter) {
		particleEmitters.add(emitter);
	}

	@Override
	public List<ParticleEmitter> getParticleEmitters() {
		return Collections.unmodifiableList(particleEmitters);
	}

	@Override
	public void clearParticleEmitters() {
		particleEmitters.clear();
	}

	@Override
	public void updateParticles(float deltaTime) {
		for (ParticleEmitter emitter : particleEmitters) {
			emitter.update(deltaTime);
		}
	}

	/**
	 * Applies drag-based rocket rotation while keeping the camera and lights fixed.
	 * Horizontal drag changes yaw around the vertical axis; vertical drag changes
	 * rotation around the horizontal axis, matching the legacy design-view controls.
	 *
	 * @param dx horizontal drag delta
	 * @param dy vertical drag delta
	 */
	public void orbitRocket(float dx, float dy, float sensitivity) {
		if (dx == 0.0f && dy == 0.0f) {
			return;
		}

		rocketDragYaw += dx * sensitivity;

		float normalizedYaw = (float) MathUtil.reduce2Pi(rocketDragYaw);
		float adjustedDy = dy;
		if (normalizedYaw > ((float) Math.PI / 2.0f) && normalizedYaw < ((float) (3.0 * Math.PI / 2.0))) {
			adjustedDy = -adjustedDy;
		}
		rocketDragRoll += adjustedDy * sensitivity;
		applyRocketRotation();
	}

	/**
	 * Clears the persisted drag rotation and reapplies the identity transform.
	 */
	public void resetRocketRotation() {
		rocketDragYaw = 0.0f;
		rocketDragRoll = 0.0f;
		applyRocketRotation();
	}

	/**
	 * Reapplies the persisted rocket drag rotation to freshly rebuilt rocket objects.
	 */
	public void applyRocketRotationToRocketObjects() {
		Matrix4f rocketTransform = getRocketTransformMatrix();
		for (SceneObject object : objects) {
			if (object.getRocketComponent() == null) {
				continue;
			}
			scratchModelMatrix.set(rocketTransform).mul(object.getModelMatrix());
			object.getModelMatrix().set(scratchModelMatrix);
		}
	}

	@Override
	public Vector3f transformRocketPoint(Vector3f point, Vector3f destination) {
		if (destination == null) {
			destination = new Vector3f();
		}
		return getRocketTransformMatrix().transformPosition(point, destination);
	}

	/**
	 * Overrides the pivot used for interactive rocket drag rotation.
	 */
	public void setRocketRotationPivotOverride(float x, float y, float z) {
		overriddenRocketRotationPivot.set(x, y, z);
		rocketRotationPivotOverridden = true;
	}

	/**
	 * Clears any custom rocket drag-rotation pivot override.
	 */
	public void clearRocketRotationPivotOverride() {
		rocketRotationPivotOverridden = false;
	}

	/**
	 * Copies the current rocket drag transform into the provided matrix.
	 */
	public Matrix4f getRocketRotationTransform(Matrix4f destination) {
		if (destination == null) {
			destination = new Matrix4f();
		}
		return getRocketTransformMatrix(rocketRotationMatrix, destination);
	}

	@Override
	public List<SceneObject> getSelectedObjects() {
		return Collections.unmodifiableList(selectedObjects);
	}


	@Override
	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		if (camera == null) {
			throw new IllegalArgumentException("Camera cannot be null");
		}
		this.camera = camera;
	}

	public LightController getLightController() {
		return lightManager;
	}

	@Override
	public Background getBackground() {
		return background;
	}

	@Override
	public void setBackground(Background background) {
		if (background == null) {
			throw new IllegalArgumentException("Background cannot be null");
		}
		if (this.background == background) {
			return;
		}
		if (this.background != null) {
			this.background.cleanup();
		}
		this.background = background;
	}

	@Override
	public boolean isFogEnabled() {
		return fogEnabled;
	}

	public void setFogEnabled(boolean fogEnabled) {
		this.fogEnabled = fogEnabled;
	}

	@Override
	public float getFogDensity() {
		return fogDensity;
	}

	public void setFogDensity(float fogDensity) {
		this.fogDensity = fogDensity;
	}

	@Override
	public void updateSelection(Raycaster raycaster, boolean isMultiSelect) {
		boolean selectionChanged = false;
		// Find the new object directly under the cursor
		SceneObject intersectedObject = raycaster.getIntersectedObject(objects);

		if (intersectedObject != null) {
			RocketComponent component = intersectedObject.getRocketComponent();
			if (component != null) {
				// Check if this component is already selected
				boolean componentAlreadySelected = selectedObjects.stream()
					.anyMatch(obj -> obj.getRocketComponent() == component);

				if (isMultiSelect && componentAlreadySelected) {
					// Deselect this component (toggle off)
					for (int i = selectedObjects.size() - 1; i >= 0; i--) {
						SceneObject obj = selectedObjects.get(i);
						if (obj.getRocketComponent() == component) {
							obj.setSelected(false);
							selectedObjects.remove(i);
							selectionChanged = true;
						}
					}
				} else {
					// Select this component
					if (!isMultiSelect) {
						// Clear previous selections if not multi-selecting
						for (SceneObject obj : selectedObjects) {
							obj.setSelected(false);
						}
						selectedObjects.clear();
						selectionChanged = true;
					}

					// Add all objects belonging to this component
					for (SceneObject obj : objects) {
						if (obj.getRocketComponent() == component && !selectedObjects.contains(obj)) {
							obj.setSelected(true);
							selectedObjects.add(obj);
							selectionChanged = true;
						}
					}
				}
			} else {
				// It's not a component part (e.g., a light visualizer)
				boolean objectAlreadySelected = selectedObjects.contains(intersectedObject);

				if (isMultiSelect && objectAlreadySelected) {
					// Deselect this object (toggle off)
					intersectedObject.setSelected(false);
					selectedObjects.remove(intersectedObject);
					selectionChanged = true;
				} else {
					// Select this object
					if (!isMultiSelect) {
						// Clear previous selections if not multi-selecting
						for (SceneObject obj : selectedObjects) {
							obj.setSelected(false);
						}
						selectedObjects.clear();
						selectionChanged = true;
					}

					if (!selectedObjects.contains(intersectedObject)) {
						intersectedObject.setSelected(true);
						selectedObjects.add(intersectedObject);
						selectionChanged = true;
					}
				}
			}
		} else if (!isMultiSelect) {
			// If no object was clicked and not multi-selecting, clear all selections
			for (SceneObject obj : selectedObjects) {
				obj.setSelected(false);
			}
			selectedObjects.clear();
			selectionChanged = true;
		}

		if (selectionChanged) {
			notifySelectionChanged();
		}
	}

	@Override
	public void setSelection(List<SceneObject> newSelection) {
		// Clear current selections
		for (SceneObject obj : selectedObjects) {
			obj.setSelected(false);
		}
		selectedObjects.clear();

		if (newSelection != null) {
			for (SceneObject obj : newSelection) {
				if (obj != null) {
					obj.setSelected(true);
					selectedObjects.add(obj);
				}
			}
		}
		notifySelectionChanged();
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		if (listener != null) selectionListeners.add(listener);
	}

	@Override
	public void removeSelectionListener(SelectionListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public void cleanup() {
		objects.forEach(SceneObject::cleanup);
		if (background != null) {
			background.cleanup();
		}
	}

	private void notifySelectionChanged() {
		for (SelectionListener l : selectionListeners) {
			l.onSelectionChanged(new ArrayList<>(selectedObjects));
		}
	}

	private void applyRocketRotation() {
		scratchPreviousRocketRotation.set(rocketRotationMatrix);
		rocketRotationMatrix.identity()
				.rotateY(rocketDragYaw)
				.rotateX(rocketDragRoll);

		scratchInverseRotationMatrix.set(scratchPreviousRocketRotation).invert();
		scratchRotationMatrix.set(rocketRotationMatrix).mul(scratchInverseRotationMatrix);

		Matrix4f incrementalTransform = getRocketTransformMatrix(scratchRotationMatrix, scratchRocketTransform);
		for (SceneObject object : objects) {
			if (object.getRocketComponent() == null) {
				continue;
			}
			scratchModelMatrix.set(incrementalTransform).mul(object.getModelMatrix());
			object.getModelMatrix().set(scratchModelMatrix);
		}
	}

	private Matrix4f getRocketTransformMatrix() {
		return getRocketTransformMatrix(rocketRotationMatrix, scratchRocketTransform);
	}

	private Matrix4f getRocketTransformMatrix(Matrix4f rotationMatrix, Matrix4f destination) {
		updateRocketRotationPivot();
		return destination.identity()
				.translate(rocketRotationPivot)
				.mul(rotationMatrix)
				.translate(-rocketRotationPivot.x, -rocketRotationPivot.y, -rocketRotationPivot.z);
	}

	private void updateRocketRotationPivot() {
		if (rocketRotationPivotOverridden) {
			rocketRotationPivot.set(overriddenRocketRotationPivot);
		}
		// rocketRotationPivot is updated explicitly via updateRocketPivotFromCamera()
		// at drag-start and after panning, so we do NOT read the camera here every frame.
		// Reading it dynamically caused the caret to jump when centerOfInterest changed
		// mid-rotation because the model matrices were built with the old pivot.
	}

	/**
	 * Captures the camera's current centerOfInterest.x as the new rocket rotation pivot.
	 * This is safe only when the rocket has no accumulated drag rotation (yaw == 0, roll == 0),
	 * because changing the pivot with a non-identity rotation would physically move the rocket
	 * to a different world position (the pivot encodes where the rotation is centered).
	 * When R is identity, all encodings are equivalent so the pivot can be updated freely.
	 */
	public void updateRocketPivotFromCamera() {
		if (rocketRotationPivotOverridden) return;
		// Guard: only update when there is no accumulated rotation.
		// T(P_new) * I * T(-P_new) == I for any P, so changing the pivot with R=I is always safe.
		// With R != I the two encodings differ, causing a visible jump.
		if (Math.abs(rocketDragYaw) > 1e-4f || Math.abs(rocketDragRoll) > 1e-4f) return;

		rocketRotationPivot.x = camera.getCenterOfInterest().x;
	}
}
