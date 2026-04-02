package info.openrocket.swing.gui.figure3d.scene.core;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.core.math.Raycaster;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightManager;
import info.openrocket.swing.gui.figure3d.scene.events.SelectionListener;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Core scene container that manages all elements within the 3D visualization environment.
 * This class serves as the central hub for the OpenRocket 3D scene management system,
 * coordinating objects, lighting, camera, particle effects, and user interactions.
 * 
 * <p>The scene provides comprehensive management for:</p>
 * <ul>
 *   <li><b>Scene objects:</b> 3D meshes representing rocket components and other geometry</li>
 *   <li><b>Lighting system:</b> Multiple light sources with visual representations</li>
 *   <li><b>Camera management:</b> View control and projection handling</li>
 *   <li><b>Particle effects:</b> Emitters for flames, smoke, and other dynamic effects</li>
 *   <li><b>User interaction:</b> Object selection and manipulation through raycasting</li>
 *   <li><b>Visual effects:</b> Backgrounds, fog, and environmental settings</li>
 * </ul>
 * 
 * <p>The scene integrates closely with OpenRocket's rocket model, providing visual
 * representations of components while maintaining connections to the underlying
 * RocketComponent objects for interaction and manipulation.</p>
 * 
 * <p>Object selection is handled through precise 3D raycasting, supporting both
 * individual object selection and component-based group selection, enabling
 * intuitive interaction with complex rocket assemblies.</p>
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

	private Scene(Rocket rocket, Camera camera, RenderingConfiguration config) {
		this(rocket, camera, config, Light.directional().withDirection(-0.5f, -1.0f, -0.5f).build());
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
		return objects;
	}

	@Override
	public void addParticleEmitter(ParticleEmitter emitter) {
		particleEmitters.add(emitter);
	}

	@Override
	public List<ParticleEmitter> getParticleEmitters() {
		return particleEmitters;
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
	 * @param viewRight unused legacy parameter retained for call-site compatibility
	 */
	public void orbitRocket(float dx, float dy, float sensitivity, Vector3f viewRight) {
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
		return selectedObjects;
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
			return;
		}

		BoundingBox bounds = rocket.getBoundingBox();
		if (bounds == null || bounds.isEmpty()) {
			rocketRotationPivot.zero();
			return;
		}

		CoordinateIF minBounds = bounds.min.multiply(RocketMeshBuilder.WORLD_SCALE);
		CoordinateIF maxBounds = bounds.max.multiply(RocketMeshBuilder.WORLD_SCALE);
		float pivotX = (float) ((minBounds.getX() + maxBounds.getX()) / 2.0);
		rocketRotationPivot.set(
				pivotX,
				0.0f,
				0.0f);
	}
	
	/**
	 * Builder class for creating Scene instances with flexible configuration.
	 * 
	 * <p>This builder provides a fluent API for creating scenes with custom settings,
	 * including lighting, background, fog, and initial objects.</p>
	 * 
	 * <h3>Usage Example:</h3>
	 * <pre>
	 * Scene scene = Scene.builder(rocket, camera, config)
	 *     .withBackgroundColor(0.2f, 0.3f, 0.4f)
	 *     .withMainLight(-0.7f, -1.0f, -0.3f, 1.0f, 1.0f, 0.9f)
	 *     .withAdditionalLight(0.5f, 0.8f, 0.2f, 0.3f, 0.3f, 0.4f)
	 *     .withFog(true, 0.05f)
	 *     .withParticleEmitter(flameEmitter)
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private final Rocket rocket;
		private final Camera camera;
		private final RenderingConfiguration config;
		private final List<Light> lights = new ArrayList<>();
		private final List<SceneObject> initialObjects = new ArrayList<>();
		private final List<ParticleEmitter> initialEmitters = new ArrayList<>();
		private Background background = new SolidColorBackground(0.1f, 0.1f, 0.12f);
		private boolean fogEnabled = false;
		private float fogDensity = 0.07f;
		
		private Builder(Rocket rocket, Camera camera, RenderingConfiguration config) {
			this.rocket = rocket;
			this.camera = camera;
			this.config = config;
			// Add default light if none specified
			this.lights.add(Light.directional()
				.withDirection(-0.5f, -1.0f, -0.5f)
				.withColor(1, 1, 1)
				.build());
		}
		
		/**
		 * Sets the background to a solid color.
		 * @param r Red component (0.0 to 1.0)
		 * @param g Green component (0.0 to 1.0)
		 * @param b Blue component (0.0 to 1.0)
		 * @return This builder instance
		 */
		public Builder withBackgroundColor(float r, float g, float b) {
			this.background = new SolidColorBackground(r, g, b);
			return this;
		}
		
		/**
		 * Sets a custom background.
		 * @param background The background to use
		 * @return This builder instance
		 */
		public Builder withBackground(Background background) {
			this.background = background;
			return this;
		}
		
		/**
		 * Replaces the default main light with a custom one.
		 * @param dirX Light direction X component
		 * @param dirY Light direction Y component
		 * @param dirZ Light direction Z component
		 * @param colorR Light color red component
		 * @param colorG Light color green component
		 * @param colorB Light color blue component
		 * @return This builder instance
		 */
		public Builder withMainLight(float dirX, float dirY, float dirZ, float colorR, float colorG, float colorB) {
			// Replace the default light
			this.lights.clear();
			this.lights.add(Light.directional()
				.withDirection(dirX, dirY, dirZ)
				.withColor(colorR, colorG, colorB)
				.build());
			return this;
		}
		
		/**
		 * Adds an additional light to the scene.
		 * @param dirX Light direction X component
		 * @param dirY Light direction Y component
		 * @param dirZ Light direction Z component
		 * @param colorR Light color red component
		 * @param colorG Light color green component
		 * @param colorB Light color blue component
		 * @return This builder instance
		 */
		public Builder withAdditionalLight(float dirX, float dirY, float dirZ, float colorR, float colorG, float colorB) {
			this.lights.add(Light.directional()
				.withDirection(dirX, dirY, dirZ)
				.withColor(colorR, colorG, colorB)
				.build());
			return this;
		}
		
		/**
		 * Adds a light to the scene.
		 * @param light The light to add
		 * @return This builder instance
		 */
		public Builder withLight(Light light) {
			this.lights.add(light);
			return this;
		}
		
		/**
		 * Configures fog settings.
		 * @param enabled Whether fog should be enabled
		 * @param density The fog density (default: 0.07)
		 * @return This builder instance
		 */
		public Builder withFog(boolean enabled, float density) {
			this.fogEnabled = enabled;
			this.fogDensity = density;
			return this;
		}
		
		/**
		 * Enables fog with default density.
		 * @param enabled Whether fog should be enabled
		 * @return This builder instance
		 */
		public Builder withFog(boolean enabled) {
			this.fogEnabled = enabled;
			return this;
		}
		
		/**
		 * Adds an initial scene object.
		 * @param object The object to add
		 * @return This builder instance
		 */
		public Builder withObject(SceneObject object) {
			this.initialObjects.add(object);
			return this;
		}
		
		/**
		 * Adds a particle emitter to the scene.
		 * @param emitter The particle emitter to add
		 * @return This builder instance
		 */
		public Builder withParticleEmitter(ParticleEmitter emitter) {
			this.initialEmitters.add(emitter);
			return this;
		}
		
		/**
		 * Configures the scene for outdoor lighting (bright, sun-like light).
		 * @return This builder instance
		 */
		public Builder withOutdoorLighting() {
			this.lights.clear();
			this.lights.add(Light.directional()
				.withDirection(-0.3f, -1.0f, -0.4f)
				.withColor(1.0f, 0.95f, 0.8f)
				.build()); // Sun-like
			this.lights.add(Light.directional()
				.withDirection(0.2f, 0.5f, 0.3f)
				.withColor(0.3f, 0.4f, 0.6f)
				.build()); // Sky fill
			this.background = new SolidColorBackground(0.6f, 0.8f, 1.0f); // Sky blue
			return this;
		}
		
		/**
		 * Configures the scene for studio lighting (multiple controlled lights).
		 * @return This builder instance
		 */
		public Builder withStudioLighting() {
			this.lights.clear();
			this.lights.add(Light.directional()
				.withDirection(-0.7f, -0.5f, -0.5f)
				.withColor(1.0f, 1.0f, 1.0f)
				.build()); // Key light
			this.lights.add(Light.directional()
				.withDirection(0.5f, -0.3f, 0.8f)
				.withColor(0.6f, 0.6f, 0.8f)
				.build()); // Fill light
			this.lights.add(Light.directional()
				.withDirection(0.0f, 0.8f, -1.0f)
				.withColor(0.4f, 0.4f, 0.4f)
				.build()); // Rim light
			this.background = new SolidColorBackground(0.15f, 0.15f, 0.15f); // Dark gray
			return this;
		}
		
		/**
		 * Configures the scene for dramatic lighting (high contrast).
		 * @return This builder instance
		 */
		public Builder withDramaticLighting() {
			this.lights.clear();
			this.lights.add(Light.directional()
				.withDirection(-1.0f, -0.2f, -0.3f)
				.withColor(1.2f, 1.0f, 0.8f)
				.build()); // Strong side light
			this.background = new SolidColorBackground(0.05f, 0.05f, 0.1f); // Very dark
			return this;
		}
		
		/**
		 * Builds the Scene with the configured settings.
		 * @return A new Scene instance
		 */
		public Scene build() {
			// Create scene with first light (or default if none)
			Light firstLight = lights.isEmpty() ? 
				Light.directional().withDirection(-0.5f, -1.0f, -0.5f).build() : 
				lights.get(0);
							
			Scene scene = new Scene(rocket, camera, config, firstLight);
			
			// Add additional lights
			for (int i = 1; i < lights.size(); i++) {
				scene.getLightController().addLight(lights.get(i));
			}
			
			// Set background
			scene.setBackground(background);
			
			// Configure fog
			scene.setFogEnabled(fogEnabled);
			scene.setFogDensity(fogDensity);
			
			// Add initial objects
			for (SceneObject object : initialObjects) {
				scene.addObject(object);
			}
			
			// Add particle emitters
			for (ParticleEmitter emitter : initialEmitters) {
				scene.addParticleEmitter(emitter);
			}
			
			return scene;
		}
	}
	
	/**
	 * Creates a new scene builder.
	 * @param rocket The rocket model to visualize
	 * @param camera The camera for the scene
	 * @param config The rendering configuration
	 * @return A new Scene.Builder instance
	 */
	public static Builder builder(Rocket rocket, Camera camera, RenderingConfiguration config) {
		return new Builder(rocket, camera, config);
	}
}
