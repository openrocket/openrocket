package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.geometry.RocketSceneSnapshot;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraControls;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketSceneSynchronizerTest extends BaseTestCase {

	@Test
	void appearanceOnlyMultiEditRefreshesConfigListeners() {
		RocketComponent primary = mock(RocketComponent.class);
		RocketComponent listener = mock(RocketComponent.class);
		when(primary.getConfigListeners()).thenReturn(List.of(listener));
		when(listener.getConfigListeners()).thenReturn(Collections.emptyList());
		when(primary.getAppearance()).thenReturn(new Appearance(new ORColor(0, 0, 255), 0.3));
		when(listener.getAppearance()).thenReturn(new Appearance(new ORColor(0, 128, 255), 0.3));

		SceneObject primaryObject = mock(SceneObject.class);
		Appearance3D primaryAppearance = new Appearance3D();
		when(primaryObject.getRocketComponent()).thenReturn(primary);
		when(primaryObject.getAppearanceSourceComponent()).thenReturn(primary);
		when(primaryObject.getAppearance()).thenReturn(primaryAppearance);

		SceneObject listenerObject = mock(SceneObject.class);
		Appearance3D listenerAppearance = new Appearance3D();
		when(listenerObject.getRocketComponent()).thenReturn(listener);
		when(listenerObject.getAppearanceSourceComponent()).thenReturn(listener);
		when(listenerObject.getAppearance()).thenReturn(listenerAppearance);

		SceneView scene = mock(SceneView.class);
		when(scene.getObjects()).thenReturn(List.of(primaryObject, listenerObject));

		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(orchestrator).enqueueGlTask(any(Runnable.class));

		Rocket rocket = new Rocket();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket);
		synchronizer.componentChanged(new ComponentChangeEvent(primary, ComponentChangeEvent.NONFUNCTIONAL_CHANGE));

		verify(primaryObject).setAppearance(primaryAppearance);
		verify(listenerObject).setAppearance(listenerAppearance);
	}

	@Test
	void mountAppearanceEditDoesNotReplaceSelectionGroupedMotorAppearance() {
		RocketComponent mount = mock(RocketComponent.class);
		when(mount.getConfigListeners()).thenReturn(Collections.emptyList());
		when(mount.getAppearance()).thenReturn(new Appearance(new ORColor(0, 0, 255), 0.3));

		SceneObject mountObject = mock(SceneObject.class);
		Appearance3D mountAppearance = new Appearance3D();
		when(mountObject.getRocketComponent()).thenReturn(mount);
		when(mountObject.getAppearanceSourceComponent()).thenReturn(mount);
		when(mountObject.getAppearance()).thenReturn(mountAppearance);

		SceneObject motorObject = mock(SceneObject.class);
		Appearance3D motorAppearance = new Appearance3D();
		// Motors retain the mount key for selection but have an independent appearance.
		when(motorObject.getRocketComponent()).thenReturn(mount);
		when(motorObject.getAppearanceSourceComponent()).thenReturn(null);
		when(motorObject.getAppearance()).thenReturn(motorAppearance);

		SceneView scene = mock(SceneView.class);
		when(scene.getObjects()).thenReturn(List.of(mountObject, motorObject));

		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(orchestrator).enqueueGlTask(any(Runnable.class));

		Rocket rocket = new Rocket();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket);
		synchronizer.componentChanged(new ComponentChangeEvent(mount, ComponentChangeEvent.NONFUNCTIONAL_CHANGE));

		verify(mountObject).setAppearance(mountAppearance);
		verify(motorObject, never()).setAppearance(any(Appearance3D.class));
	}

	@Test
	void undoLeavesTheCameraWhereTheUserPutIt() throws Exception {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = rebuildingSynchronizer(orchestrator, emptyScene(), rocket);

		// The event Rocket.loadFrom() fires for both undo and redo.
		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.UNDO_CHANGE
				| ComponentChangeEvent.NONFUNCTIONAL_CHANGE | ComponentChangeEvent.TREE_CHANGE));
		drainEdt();

		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		verify(orchestrator, never()).focusOnRocket();
		verify(orchestrator, never()).refitOnRocketBoundsChange();
	}

	@Test
	void zoomFittedRebuildReframesWithoutRestoringTheDefaultView() throws Exception {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = rebuildingSynchronizer(orchestrator, emptyScene(), rocket);

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE));
		drainEdt();

		verify(orchestrator).refitOnRocketBoundsChange();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
	}

	@Test
	void componentAdditionAndDeletionEventsKeepManualZoomAndView() throws Exception {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(false);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = rebuildingSynchronizer(orchestrator, emptyScene(), rocket);
		ComponentChangeEvent addRemoveEvent = new ComponentChangeEvent(rocket,
				ComponentChangeEvent.TREE_CHANGE | ComponentChangeEvent.AEROMASS_CHANGE);

		synchronizer.componentChanged(addRemoveEvent);
		synchronizer.componentChanged(addRemoveEvent);
		drainEdt();

		verify(orchestrator, never()).refitOnRocketBoundsChange();
		verify(orchestrator).applyRocketRotationToScene();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		synchronizer.dispose();
	}

	@Test
	void componentAdditionAndDeletionEventsRefitAtHundredPercentWithoutResettingView() throws Exception {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = rebuildingSynchronizer(orchestrator, emptyScene(), rocket);
		ComponentChangeEvent addRemoveEvent = new ComponentChangeEvent(rocket,
				ComponentChangeEvent.TREE_CHANGE | ComponentChangeEvent.AEROMASS_CHANGE);

		synchronizer.componentChanged(addRemoveEvent);
		synchronizer.componentChanged(addRemoveEvent);
		drainEdt();

		verify(orchestrator).refitOnRocketBoundsChange();
		verify(orchestrator).applyRocketRotationToScene();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		synchronizer.dispose();
	}

	@Test
	void treeMetadataChangeDoesNotMoveCamera() throws Exception {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = rebuildingSynchronizer(orchestrator, emptyScene(), rocket);

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.TREE_CHANGE));
		drainEdt();

		verify(orchestrator, never()).refitOnRocketBoundsChange();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		synchronizer.dispose();
	}

	private static SceneView emptyScene() {
		SceneView scene = mock(SceneView.class);
		when(scene.getObjects()).thenReturn(new ArrayList<>());
		when(scene.getSelectedObjects()).thenReturn(new ArrayList<>());
		when(scene.getParticleEmitters()).thenReturn(new ArrayList<>());
		return scene;
	}

	private static Scene3DOrchestrator rebuildingOrchestrator(CameraControls camera) {
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		when(orchestrator.getRenderingConfiguration()).thenReturn(new RenderingConfiguration());
		when(orchestrator.getCameraController()).thenReturn(camera);
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(orchestrator).enqueueGlTask(any(Runnable.class));
		return orchestrator;
	}

	private static RocketSceneSynchronizer rebuildingSynchronizer(
			Scene3DOrchestrator orchestrator, SceneView scene, Rocket rocket) {
		return new RocketSceneSynchronizer(orchestrator, scene, rocket,
				() -> RocketMeshBuilder.buildSnapshot(rocket, orchestrator.getRenderingConfiguration()),
				(snapshot, configuration) -> mock(RocketMeshBuilder.PreparedSnapshot.class));
	}

	@Test
	void rebuildEventsCoalesceSnapshotConstructionAndGlWork() throws Exception {
		SceneView scene = mock(SceneView.class);
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		RenderingConfiguration configuration = new RenderingConfiguration();
		when(orchestrator.getRenderingConfiguration()).thenReturn(configuration);
		when(orchestrator.getCameraController()).thenReturn(mock(CameraControls.class));
		when(scene.getObjects()).thenReturn(new ArrayList<>());
		when(scene.getSelectedObjects()).thenReturn(new ArrayList<>());
		when(scene.getParticleEmitters()).thenReturn(new ArrayList<>());

		Rocket rocket = new Rocket();
		RocketSceneSnapshot latestSnapshot = mock(RocketSceneSnapshot.class);
		RocketMeshBuilder.PreparedSnapshot prepared = mock(RocketMeshBuilder.PreparedSnapshot.class);
		AtomicInteger buildCount = new AtomicInteger();
		AtomicReference<RocketSceneSnapshot> modelState = new AtomicReference<>();
		List<RocketSceneSnapshot> preparedSnapshots = new ArrayList<>();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket, () -> {
			buildCount.incrementAndGet();
			return modelState.get();
		}, (snapshot, ignored) -> {
			preparedSnapshots.add(snapshot);
			return prepared;
		});

		ComponentChangeEvent event = new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE);
		modelState.set(mock(RocketSceneSnapshot.class));
		synchronizer.componentChanged(event);
		modelState.set(latestSnapshot);
		synchronizer.componentChanged(event);
		drainEdt();

		ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(orchestrator, times(1)).enqueueGlTask(taskCaptor.capture());
		taskCaptor.getValue().run();

		assertEquals(1, buildCount.get());
		assertEquals(List.of(latestSnapshot), preparedSnapshots);
		verify(prepared).commitTo(scene);
	}

	@Test
	void rebuildReportedDuringSnapshotConstructionSchedulesTheNewestState() throws Exception {
		SceneView scene = emptyScene();
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		RenderingConfiguration configuration = new RenderingConfiguration();
		when(orchestrator.getRenderingConfiguration()).thenReturn(configuration);
		when(orchestrator.getCameraController()).thenReturn(mock(CameraControls.class));

		Rocket rocket = new Rocket();
		RocketSceneSnapshot firstSnapshot = mock(RocketSceneSnapshot.class);
		RocketSceneSnapshot latestSnapshot = mock(RocketSceneSnapshot.class);
		RocketMeshBuilder.PreparedSnapshot prepared = mock(RocketMeshBuilder.PreparedSnapshot.class);
		AtomicInteger buildCount = new AtomicInteger();
		AtomicReference<RocketSceneSynchronizer> synchronizerReference = new AtomicReference<>();
		List<RocketSceneSnapshot> preparedSnapshots = new ArrayList<>();
		ComponentChangeEvent event = new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket, () -> {
			if (buildCount.getAndIncrement() == 0) {
				synchronizerReference.get().componentChanged(event);
				return firstSnapshot;
			}
			return latestSnapshot;
		}, (snapshot, ignored) -> {
			preparedSnapshots.add(snapshot);
			return prepared;
		});
		synchronizerReference.set(synchronizer);

		synchronizer.componentChanged(event);
		drainEdt();
		drainEdt();

		ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(orchestrator).enqueueGlTask(taskCaptor.capture());
		taskCaptor.getValue().run();

		assertEquals(2, buildCount.get());
		assertEquals(List.of(latestSnapshot), preparedSnapshots);
	}

	@Test
	void disposeCancelsADeferredSnapshotBuild() throws Exception {
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		Rocket rocket = new Rocket();
		AtomicInteger buildCount = new AtomicInteger();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket, () -> {
			buildCount.incrementAndGet();
			return mock(RocketSceneSnapshot.class);
		});

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE));
		synchronizer.dispose();
		drainEdt();

		assertEquals(0, buildCount.get());
		verify(orchestrator, never()).enqueueGlTask(any(Runnable.class));
	}

	@Test
	void failedPreparationLeavesTheExistingSceneIntact() throws Exception {
		RocketComponent component = mock(RocketComponent.class);
		SceneObject existingObject = mock(SceneObject.class);
		when(existingObject.getRocketComponent()).thenReturn(component);
		SceneView scene = mock(SceneView.class);
		when(scene.getObjects()).thenReturn(List.of(existingObject));
		when(scene.getSelectedObjects()).thenReturn(new ArrayList<>());
		when(scene.getParticleEmitters()).thenReturn(new ArrayList<>());

		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		RenderingConfiguration configuration = new RenderingConfiguration();
		when(orchestrator.getRenderingConfiguration()).thenReturn(configuration);
		Rocket rocket = new Rocket();
		RocketSceneSnapshot snapshot = mock(RocketSceneSnapshot.class);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(
				orchestrator, scene, rocket, () -> snapshot,
				(ignoredSnapshot, ignoredConfiguration) -> {
					throw new IllegalStateException("upload failed");
				});

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE));
		drainEdt();
		ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(orchestrator).enqueueGlTask(taskCaptor.capture());

		assertThrows(IllegalStateException.class, () -> taskCaptor.getValue().run());
		verify(scene, never()).removeObject(existingObject);
		verify(existingObject, never()).cleanup();
	}

	private static void drainEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			// Wait for previously queued snapshot construction.
		});
	}
}
