package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages real-time synchronization between the OpenRocket data model and the 3D scene visualization.
 * This orchestration component serves as a bridge between OpenRocket's rocket model and the 3D
 * rendering system, ensuring that changes to rocket components are immediately reflected in the
 * visual representation.
 * 
 * <p>The synchronizer operates as a ComponentChangeListener, monitoring the rocket model for
 * modifications and determining the appropriate response:</p>
 * <ul>
 *   <li><b>Appearance changes:</b> Updates visual properties (colors, textures) without rebuilding geometry</li>
 *   <li><b>Structural changes:</b> Triggers complete scene reconstruction for geometry or hierarchy modifications</li>
 *   <li><b>Resource management:</b> Properly cleans up GPU resources when objects are modified or removed</li>
 * </ul>
 * 
 * <p>This component ensures that the 3D visualization remains synchronized with the rocket design
 * as users modify components through the OpenRocket interface, providing a seamless interactive
 * experience between the design tools and the 3D preview.</p>
 * 
 * <p>The synchronizer integrates with the Scene3DOrchestrator to coordinate broader scene updates
 * and maintains efficiency by performing minimal updates when possible (appearance-only changes)
 * while falling back to full reconstruction for more complex modifications.</p>
 */
public class RocketSceneSynchronizer implements ComponentChangeListener {
	private final Scene3DOrchestrator scene3DOrchestrator;
	private final SceneView scene;
	private final Rocket rocket;
	private final ConcurrentLinkedQueue<RocketComponent> pendingAppearanceUpdates = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean appearanceQueued = new AtomicBoolean(false);
	private final AtomicBoolean rebuildQueued = new AtomicBoolean(false);
	private final AtomicBoolean refocusQueued = new AtomicBoolean(false);
	private volatile FlightConfigurationId lastSelectedConfigurationId;

	/**
	 * Constructs a new RocketSceneSynchronizer and registers it with the rocket model.
	 * The synchronizer will immediately begin monitoring the rocket for changes and
	 * coordinating updates with the 3D scene.
	 * 
	 * @param scene3DOrchestrator the orchestrator managing the overall 3D scene
	 * @param scene the 3D scene containing the visual representation
	 * @param rocket the OpenRocket model to synchronize with
	 */
	public RocketSceneSynchronizer(Scene3DOrchestrator scene3DOrchestrator, SceneView scene, Rocket rocket) {
		this.scene3DOrchestrator = scene3DOrchestrator;
		this.scene = scene;
		this.rocket = rocket;
		this.lastSelectedConfigurationId = rocket.getSelectedConfiguration().getId();
		// Register this manager as a listener to the rocket model
		this.rocket.addComponentChangeListener(this);
	}

	/**
	 * Handles component change events from the OpenRocket model.
	 * Analyzes the type of change and determines the appropriate update strategy:
	 * lightweight appearance updates for visual changes, or full scene reconstruction
	 * for structural modifications.
	 * 
	 * @param e the ComponentChangeEvent describing the modification
	 */
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (hasSelectedConfigurationChanged()) {
			queueRebuild(false);
			return;
		}

		// Check for changes that only affect rendered appearance and not geometry/structure.
		// These can be handled with a lightweight update and should not reset the camera.
		if (isAppearanceOnlySceneChange(e)) {
			queueAppearanceUpdate(e.getSource());
		} else {
			queueRebuild(shouldRefocusCamera(e));
		}
	}

	private boolean isAppearanceOnlySceneChange(ComponentChangeEvent e) {
		return e.isNonFunctionalChange() || isFinishAppearanceChange(e);
	}

	private boolean isFinishAppearanceChange(ComponentChangeEvent e) {
		int finishChangeType = ComponentChangeEvent.AERODYNAMIC_CHANGE | ComponentChangeEvent.GRAPHIC_CHANGE;
		return e.getType() == finishChangeType && e.getSource() instanceof ExternalComponent;
	}

	private boolean hasSelectedConfigurationChanged() {
		FlightConfigurationId currentSelectedConfigurationId = rocket.getSelectedConfiguration().getId();
		if (currentSelectedConfigurationId.equals(lastSelectedConfigurationId)) {
			return false;
		}
		lastSelectedConfigurationId = currentSelectedConfigurationId;
		return true;
	}

	/**
	 * Detaches this synchronizer from the rocket model.
	 */
	public void dispose() {
		rocket.removeComponentChangeListener(this);
		pendingAppearanceUpdates.clear();
	}

	private void queueAppearanceUpdate(RocketComponent component) {
		if (component == null || rebuildQueued.get()) {
			return;
		}
		pendingAppearanceUpdates.add(component);
		if (appearanceQueued.compareAndSet(false, true)) {
			scene3DOrchestrator.enqueueGlTask(this::flushAppearanceUpdates);
		}
	}

	private void flushAppearanceUpdates() {
		try {
			if (rebuildQueued.get()) {
				pendingAppearanceUpdates.clear();
				return;
			}
			RocketComponent component;
			while ((component = pendingAppearanceUpdates.poll()) != null) {
				updateComponentAppearance(component);
			}
		} finally {
			appearanceQueued.set(false);
			if (!pendingAppearanceUpdates.isEmpty() && !rebuildQueued.get()) {
				if (appearanceQueued.compareAndSet(false, true)) {
					scene3DOrchestrator.enqueueGlTask(this::flushAppearanceUpdates);
				}
			}
		}
	}

	private boolean shouldRefocusCamera(ComponentChangeEvent e) {
		return e.isTreeChange() || e.isTreeChildrenChange() || e.isMassChange();
	}

	private void queueRebuild(boolean refocusCamera) {
		if (refocusCamera) {
			refocusQueued.set(true);
		}
		if (!rebuildQueued.compareAndSet(false, true)) {
			return;
		}
		scene3DOrchestrator.enqueueGlTask(() -> {
			try {
				pendingAppearanceUpdates.clear();
				rebuildRocketScene(refocusQueued.getAndSet(false));
			} finally {
				rebuildQueued.set(false);
			}
		});
	}

	/**
	 * Updates the visual appearance of all scene objects associated with a rocket component.
	 * This method handles lightweight visual updates such as color changes, texture modifications,
	 * or material property adjustments without requiring full geometry reconstruction.
	 * 
	 * <p>The method ensures proper resource management by cleaning up old appearance resources
	 * before applying new ones, preventing GPU memory leaks during visual updates.</p>
	 * 
	 * @param component the RocketComponent whose appearance should be updated
	 */
	private void updateComponentAppearance(RocketComponent component) {
		if (component == null) return;

		// Find the current appearance object to clean it up later.
		// We only need to find it once, as it's shared among all of the component's scene objects.
		Appearance3D oldAppearance = null;
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() == component) {
				oldAppearance = obj.getAppearance();
				break;
			}
		}

		// Create a new, updated engine appearance from the component's data.
		Appearance3D newAppearance = AppearanceFactory.createFrom(component);

		// Find all scene objects that represent this component and apply the new appearance.
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() == component) {
				obj.setAppearance(newAppearance);
			}
		}

		// Clean up the old, now-unused appearance object to free its texture.
		if (oldAppearance != null && oldAppearance != newAppearance) {
			oldAppearance.cleanup();
		}
	}

	/**
	 * Performs a complete rebuild of all rocket-related objects in the scene.
	 * This method is used for structural changes that cannot be handled with simple
	 * appearance updates, such as component additions, removals, or geometry modifications.
	 * 
	 * <p>The rebuild process:</p>
	 * <ol>
	 *   <li>Identifies all rocket-related scene objects</li>
	 *   <li>Removes them from the scene and cleans up their GPU resources</li>
	 *   <li>Regenerates the entire rocket mesh from the current model state</li>
	 *   <li>Adds the new objects back to the scene</li>
	 * </ol>
	 * 
	 * <p>Non-rocket objects (axes, lights, etc.) are preserved during this operation.</p>
	 */
	public void rebuildRocketScene() {
		rebuildRocketScene(true);
	}

	public void rebuildRocketScene(boolean refocusCamera) {
		lastSelectedConfigurationId = rocket.getSelectedConfiguration().getId();

		// First, create a list of all objects to be removed to avoid modification-during-iteration errors.
		List<SceneObject> objectsToRemove = new ArrayList<>();
		for (SceneObject obj : scene.getObjects()) {
			// We only remove objects that are part of the rocket.
			// Scenery like the coordinate axes will remain.
			if (obj.getRocketComponent() != null) {
				objectsToRemove.add(obj);
			}
		}

		// Now, remove and clean up the old objects.
		for (SceneObject obj : objectsToRemove) {
			scene.getObjects().remove(obj);
			obj.cleanup(); // Important: Frees up GPU resources
		}

		// Particle emitters are also rocket-derived state and must be rebuilt from scratch.
		// If not cleared here, toggling smoke/flame/sparks can leave stale emitters behind.
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (emitter instanceof FlameEmitter flameEmitter) {
				if (flameEmitter.getFlameLight() != null) {
					scene.getLightController().removeLight(flameEmitter.getFlameLight());
				}
			}
		}
		scene.getParticleEmitters().clear();

		// Finally, tell the RocketMeshBuilder to recreate the rocket objects from the current rocket state.
		RocketMeshBuilder.buildRocketMesh(scene, rocket, scene3DOrchestrator.getRenderingConfiguration());
		scene3DOrchestrator.applyRocketRotationToScene();
		if (refocusCamera) {
			scene3DOrchestrator.focusOnRocket();
		}
	}
}
