package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.geometry.RocketSceneSnapshot;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Synchronizes rocket-model changes into the GL scene, updating appearances in
 * place and rebuilding geometry for structural changes.
 */
public class RocketSceneSynchronizer implements ComponentChangeListener {
	private final Scene3DOrchestrator scene3DOrchestrator;
	private final SceneView scene;
	private final Rocket rocket;
	private final Supplier<RocketSceneSnapshot> snapshotBuilder;
	private final BiFunction<RocketSceneSnapshot, RenderingConfiguration, RocketMeshBuilder.PreparedSnapshot>
			snapshotPreparer;
	private final ConcurrentLinkedQueue<PendingAppearanceUpdate> pendingAppearanceUpdates = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean appearanceQueued = new AtomicBoolean(false);
	private final AtomicBoolean snapshotBuildQueued = new AtomicBoolean(false);
	private final AtomicBoolean rebuildRequested = new AtomicBoolean(false);
	private final AtomicBoolean disposed = new AtomicBoolean(false);
	// Holds the latest rocket snapshot waiting to be applied on the GL thread. A
	// snapshot built while GL work is pending replaces the earlier value.
	private final AtomicReference<RocketSceneSnapshot> pendingSnapshot = new AtomicReference<>();
	private final AtomicReference<CameraUpdateBehavior> pendingCameraUpdateBehavior =
			new AtomicReference<>(CameraUpdateBehavior.NONE);
	private volatile FlightConfigurationId lastSelectedConfigurationId;

	private record PendingAppearanceUpdate(
			RocketComponent component,
			AppearanceFactory.ComponentAppearanceSnapshot appearance) {
	}

	private enum CameraUpdateBehavior {
		NONE,
		REFIT_IF_FIT;

		private static CameraUpdateBehavior merge(CameraUpdateBehavior current, CameraUpdateBehavior requested) {
			return current.ordinal() >= requested.ordinal() ? current : requested;
		}
	}

	/**
	 * @param scene3DOrchestrator the orchestrator managing the overall 3D scene
	 * @param scene the 3D scene containing the visual representation
	 * @param rocket the OpenRocket model to synchronize with
	 */
	public RocketSceneSynchronizer(Scene3DOrchestrator scene3DOrchestrator, SceneView scene, Rocket rocket) {
		this(scene3DOrchestrator, scene, rocket,
				() -> RocketMeshBuilder.buildSnapshot(rocket, scene3DOrchestrator.getRenderingConfiguration()),
				RocketMeshBuilder::prepareSnapshot);
	}

	RocketSceneSynchronizer(Scene3DOrchestrator scene3DOrchestrator, SceneView scene, Rocket rocket,
			Supplier<RocketSceneSnapshot> snapshotBuilder) {
		this(scene3DOrchestrator, scene, rocket, snapshotBuilder, RocketMeshBuilder::prepareSnapshot);
	}

	RocketSceneSynchronizer(Scene3DOrchestrator scene3DOrchestrator, SceneView scene, Rocket rocket,
			Supplier<RocketSceneSnapshot> snapshotBuilder,
			BiFunction<RocketSceneSnapshot, RenderingConfiguration, RocketMeshBuilder.PreparedSnapshot> snapshotPreparer) {
		this.scene3DOrchestrator = scene3DOrchestrator;
		this.scene = scene;
		this.rocket = rocket;
		this.snapshotBuilder = snapshotBuilder;
		this.snapshotPreparer = snapshotPreparer;
		this.lastSelectedConfigurationId = rocket.getSelectedConfiguration().getId();
		this.rocket.addComponentChangeListener(this);
	}

	/** Chooses an appearance-only update or a full rebuild for a model change. */
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		CameraUpdateBehavior requestedCameraUpdate = determineCameraUpdateBehavior(e);
		if (hasSelectedConfigurationChanged()) {
			queueRebuild(requestedCameraUpdate);
			return;
		}

		// Check for changes that only affect rendered appearance and not geometry/structure.
		// These can be handled with a lightweight update and should not reset the camera.
		if (isAppearanceOnlySceneChange(e)) {
			queueAppearanceUpdates(e.getSource());
		} else {
			queueRebuild(requestedCameraUpdate);
		}
	}

	private boolean isAppearanceOnlySceneChange(ComponentChangeEvent e) {
		// Visibility toggles are emitted as pure GRAPHIC_CHANGE events and require a
		// full rebuild so hidden components are removed from the scene graph.
		return (e.isNonFunctionalChange() && e.getType() != ComponentChangeEvent.GRAPHIC_CHANGE)
				|| isFinishAppearanceChange(e);
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
		if (!disposed.compareAndSet(false, true)) {
			return;
		}
		rocket.removeComponentChangeListener(this);
		rebuildRequested.set(false);
		pendingAppearanceUpdates.clear();
		pendingSnapshot.set(null);
		pendingCameraUpdateBehavior.set(CameraUpdateBehavior.NONE);
	}

	private void queueAppearanceUpdates(RocketComponent component) {
		if (component == null) {
			return;
		}

		queueAppearanceUpdate(component);

		// Multi-component edits only emit a change event from the primary component.
		// The additional selected components sit in configListeners with bypassed
		// component events, so their scene objects must be refreshed explicitly.
		for (RocketComponent listener : component.getConfigListeners()) {
			queueAppearanceUpdate(listener);
		}
	}

	private void queueAppearanceUpdate(RocketComponent component) {
		if (component == null || disposed.get() || pendingSnapshot.get() != null) {
			return;
		}
		pendingAppearanceUpdates.add(new PendingAppearanceUpdate(
				component, AppearanceFactory.captureComponentAppearance(component)));
		if (appearanceQueued.compareAndSet(false, true)) {
			scene3DOrchestrator.enqueueGlTask(this::flushAppearanceUpdates);
		}
	}

	private void flushAppearanceUpdates() {
		try {
			if (disposed.get() || pendingSnapshot.get() != null) {
				pendingAppearanceUpdates.clear();
				return;
			}
			PendingAppearanceUpdate update;
			while ((update = pendingAppearanceUpdates.poll()) != null) {
				updateComponentAppearance(update);
			}
		} finally {
			appearanceQueued.set(false);
			if (!pendingAppearanceUpdates.isEmpty() && pendingSnapshot.get() == null) {
				if (appearanceQueued.compareAndSet(false, true)) {
					scene3DOrchestrator.enqueueGlTask(this::flushAppearanceUpdates);
				}
			}
		}
	}

	private CameraUpdateBehavior determineCameraUpdateBehavior(ComponentChangeEvent e) {
		// Undo/redo replaces the whole component tree (Rocket.loadFrom always reports a
		// tree change), but it restores a design the user was just looking at. Moving the
		// camera there would mean that merely cancelling a configuration dialog throws
		// away the current view, so the camera is left exactly where it is.
		if (e.isUndoChange()) {
			return CameraUpdateBehavior.NONE;
		}
		if (e.isTreeChange()) {
			// Add/remove events include the affected mass/aerodynamic flags. Pure tree
			// events, such as renaming a stage, do not change the rendered bounds.
			return e.isMassChange() || e.isAerodynamicChange()
					? CameraUpdateBehavior.REFIT_IF_FIT : CameraUpdateBehavior.NONE;
		}
		if (e.isTreeChildrenChange() || e.isMassChange()) {
			return CameraUpdateBehavior.REFIT_IF_FIT;
		}
		return CameraUpdateBehavior.NONE;
	}

	private void queueRebuild(CameraUpdateBehavior requestedCameraUpdate) {
		if (disposed.get()) {
			return;
		}
		pendingCameraUpdateBehavior.accumulateAndGet(requestedCameraUpdate, CameraUpdateBehavior::merge);
		rebuildRequested.set(true);
		queueSnapshotBuild();
	}

	private void queueSnapshotBuild() {
		if (disposed.get() || !snapshotBuildQueued.compareAndSet(false, true)) {
			return;
		}
		// Model events are normally delivered on the EDT. Deferring until the current
		// event-queue burst drains turns slider and spinner edits into one tessellation.
		SwingUtilities.invokeLater(this::buildLatestSnapshot);
	}

	private void buildLatestSnapshot() {
		try {
			if (disposed.get()) {
				return;
			}
			rebuildRequested.set(false);
			RocketSceneSnapshot snapshot = snapshotBuilder.get();
			if (disposed.get()) {
				return;
			}

			RocketSceneSnapshot previous = pendingSnapshot.getAndSet(snapshot);
			if (disposed.get()) {
				pendingSnapshot.compareAndSet(snapshot, null);
				return;
			}
			if (previous == null) {
				scene3DOrchestrator.enqueueGlTask(this::flushRebuild);
			}
		} finally {
			snapshotBuildQueued.set(false);
			// A non-EDT model source can report another event while tessellation is
			// running. Schedule another pass so that change is not lost.
			if (rebuildRequested.get() && !disposed.get()) {
				queueSnapshotBuild();
			}
		}
	}

	private void flushRebuild() {
		pendingAppearanceUpdates.clear();
		RocketSceneSnapshot snapshot = pendingSnapshot.getAndSet(null);
		if (snapshot == null || disposed.get()) {
			return;
		}
		applyRebuildSnapshot(snapshot, pendingCameraUpdateBehavior.getAndSet(CameraUpdateBehavior.NONE));
	}

	/**
	 * Updates the visual appearance of all scene objects associated with a rocket component.
	 * This method handles lightweight visual updates such as color changes, texture modifications,
	 * or material property adjustments without requiring full geometry reconstruction.
	 *
	 * <p>The method ensures proper resource management by cleaning up old appearance resources
	 * before applying new ones, preventing GPU memory leaks during visual updates.</p>
	 *
	 * @param update component identity and immutable appearance state captured with the model event
	 */
	private void updateComponentAppearance(PendingAppearanceUpdate update) {
		AppearanceFactory.withDecalTextureCache(scene3DOrchestrator.getDecalTextureCache(),
				() -> updateComponentAppearanceWithCache(update));
	}

	private void updateComponentAppearanceWithCache(PendingAppearanceUpdate update) {
		RocketComponent component = update.component();

		// A component's instances share one appearance, so update that object in place when possible.
		Appearance3D oldAppearance = null;
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getAppearanceSourceComponent() == component) {
				oldAppearance = obj.getAppearance();
				break;
			}
		}

		// Update the existing appearance in place when possible so slider-driven
		// changes do not reload and upload the same texture every time.
		Appearance3D newAppearance = oldAppearance;
		if (newAppearance == null) {
			newAppearance = AppearanceFactory.createFrom(update.appearance());
		} else {
			AppearanceFactory.updateFrom(newAppearance, update.appearance());
		}

		// Apply a newly created appearance to every instance sourced from this component.
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getAppearanceSourceComponent() == component) {
				obj.setAppearance(newAppearance);
			}
		}
	}

	/**
	 * Performs a complete rebuild of all rocket-related objects in the scene.
	 * This method is used for structural changes that cannot be handled with simple
	 * appearance updates, such as component additions, removals, or geometry modifications.
	 *
	 * <p>The rebuild process:</p>
	 * <ol>
	 *   <li>Prepares all replacement GPU resources</li>
	 *   <li>Removes the old rocket and origin-axis objects</li>
	 *   <li>Commits the prepared objects and particle emitters</li>
	 * </ol>
	 *
	 * <p>Other scene infrastructure, such as light visualizers, is preserved.</p>
	 *
	 * @param refocusCamera whether a zoom-fitted camera should be reframed after the rebuild
	 */
	public void rebuildRocketScene(boolean refocusCamera) {
		rebuildRocketScene(refocusCamera ? CameraUpdateBehavior.REFIT_IF_FIT : CameraUpdateBehavior.NONE);
	}

	// Synchronous rebuild on the calling thread, used by external one-shot triggers
	// (Photo Studio settings, display dialogs, init) that arrange to be on the GL thread
	// and depend on the scene being fully rebuilt before this returns. The snapshot is
	// just an intermediate data structure here — it does not change threading semantics.
	private void rebuildRocketScene(CameraUpdateBehavior cameraUpdateBehavior) {
		RocketSceneSnapshot snapshot = snapshotBuilder.get();
		applyRebuildSnapshot(snapshot, cameraUpdateBehavior);
	}

	private void applyRebuildSnapshot(RocketSceneSnapshot snapshot, CameraUpdateBehavior cameraUpdateBehavior) {
		RocketMeshBuilder.PreparedSnapshot prepared = AppearanceFactory.withDecalTextureCache(
				scene3DOrchestrator.getDecalTextureCache(),
				() -> snapshotPreparer.apply(snapshot, scene3DOrchestrator.getRenderingConfiguration()));
		try {
			commitPreparedSnapshot(snapshot, cameraUpdateBehavior, prepared);
		} finally {
			prepared.cleanupIfUncommitted();
		}
	}

	private void commitPreparedSnapshot(RocketSceneSnapshot snapshot, CameraUpdateBehavior cameraUpdateBehavior,
			RocketMeshBuilder.PreparedSnapshot prepared) {
		lastSelectedConfigurationId = snapshot.getFlightConfigurationId();
		boolean hadSelection = !scene.getSelectedObjects().isEmpty();
		Set<RocketComponent> selectedRocketComponents = captureSelectedRocketComponents();
		List<SceneObject> persistentSelection = capturePersistentSelection();

		// First, create a list of all objects to be removed to avoid modification-during-iteration errors.
		List<SceneObject> objectsToRemove = new ArrayList<>();
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() != null || obj.isOriginAxis()) {
				objectsToRemove.add(obj);
			}
		}

		// Now, remove and clean up the old objects.
		for (SceneObject obj : objectsToRemove) {
			scene.removeObject(obj);
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
		scene.clearParticleEmitters();

		prepared.commitTo(scene);
		scene3DOrchestrator.applyRocketRotationToScene();
		restoreSelectionAfterRebuild(hadSelection, selectedRocketComponents, persistentSelection);
		if (cameraUpdateBehavior == CameraUpdateBehavior.REFIT_IF_FIT
				&& scene3DOrchestrator.getCameraController().isZoomFitting()) {
			// Recompute framing without changing the camera angles or persisted rocket rotation.
			scene3DOrchestrator.refitOnRocketBoundsChange();
		}
	}

	private Set<RocketComponent> captureSelectedRocketComponents() {
		Set<RocketComponent> selectedComponents = new LinkedHashSet<>();
		for (SceneObject selectedObject : scene.getSelectedObjects()) {
			RocketComponent component = selectedObject.getRocketComponent();
			if (component != null) {
				selectedComponents.add(component);
			}
		}
		return selectedComponents;
	}

	private List<SceneObject> capturePersistentSelection() {
		List<SceneObject> persistentSelection = new ArrayList<>();
		for (SceneObject selectedObject : scene.getSelectedObjects()) {
			if (selectedObject.getRocketComponent() == null && scene.getObjects().contains(selectedObject)) {
				persistentSelection.add(selectedObject);
			}
		}
		return persistentSelection;
	}

	private void restoreSelectionAfterRebuild(boolean hadSelection, Set<RocketComponent> selectedRocketComponents,
			List<SceneObject> persistentSelection) {
		if (!hadSelection && selectedRocketComponents.isEmpty() && persistentSelection.isEmpty()) {
			return;
		}

		List<SceneObject> restoredSelection = new ArrayList<>(persistentSelection);
		if (!selectedRocketComponents.isEmpty()) {
			for (SceneObject sceneObject : scene.getObjects()) {
				RocketComponent component = sceneObject.getRocketComponent();
				if (component != null && selectedRocketComponents.contains(component)) {
					restoredSelection.add(sceneObject);
				}
			}
		}
		scene.setSelection(restoredSelection);
	}
}
