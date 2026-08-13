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
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
	void undoLeavesTheCameraWhereTheUserPutIt() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);

		// The event Rocket.loadFrom() fires for both undo and redo.
		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.UNDO_CHANGE
				| ComponentChangeEvent.NONFUNCTIONAL_CHANGE | ComponentChangeEvent.TREE_CHANGE));

		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		verify(orchestrator, never()).focusOnRocket();
		verify(orchestrator, never()).refitOnRocketBoundsChange();
	}

	@Test
	void zoomFittedRebuildReframesWithoutRestoringTheDefaultView() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE));

		verify(orchestrator).refitOnRocketBoundsChange();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
	}

	@Test
	void componentAdditionAndDeletionEventsKeepManualZoomAndView() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(false);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);
		ComponentChangeEvent addRemoveEvent = new ComponentChangeEvent(rocket,
				ComponentChangeEvent.TREE_CHANGE | ComponentChangeEvent.AEROMASS_CHANGE);

		synchronizer.componentChanged(addRemoveEvent);
		synchronizer.componentChanged(addRemoveEvent);

		verify(orchestrator, never()).refitOnRocketBoundsChange();
		verify(orchestrator, times(2)).applyRocketRotationToScene();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		synchronizer.dispose();
	}

	@Test
	void componentAdditionAndDeletionEventsRefitAtHundredPercentWithoutResettingView() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);
		ComponentChangeEvent addRemoveEvent = new ComponentChangeEvent(rocket,
				ComponentChangeEvent.TREE_CHANGE | ComponentChangeEvent.AEROMASS_CHANGE);

		synchronizer.componentChanged(addRemoveEvent);
		synchronizer.componentChanged(addRemoveEvent);

		verify(orchestrator, times(2)).refitOnRocketBoundsChange();
		verify(orchestrator, times(2)).applyRocketRotationToScene();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
		synchronizer.dispose();
	}

	@Test
	void treeMetadataChangeDoesNotMoveCamera() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.TREE_CHANGE));

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

	@Test
	void rebuildEventsCoalesceGlWorkButApplyTheLatestSnapshot() {
		SceneView scene = mock(SceneView.class);
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		RenderingConfiguration configuration = new RenderingConfiguration();
		when(orchestrator.getRenderingConfiguration()).thenReturn(configuration);
		when(orchestrator.getCameraController()).thenReturn(mock(CameraControls.class));
		when(scene.getObjects()).thenReturn(new ArrayList<>());
		when(scene.getSelectedObjects()).thenReturn(new ArrayList<>());
		when(scene.getParticleEmitters()).thenReturn(new ArrayList<>());

		Rocket rocket = new Rocket();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket);
		RocketSceneSnapshot firstSnapshot = mock(RocketSceneSnapshot.class);
		RocketSceneSnapshot latestSnapshot = mock(RocketSceneSnapshot.class);

		try (MockedStatic<RocketMeshBuilder> meshBuilder = mockStatic(RocketMeshBuilder.class)) {
			meshBuilder.when(() -> RocketMeshBuilder.buildSnapshot(rocket, configuration))
					.thenReturn(firstSnapshot, latestSnapshot);

			ComponentChangeEvent event = new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE);
			synchronizer.componentChanged(event);
			synchronizer.componentChanged(event);

			ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
			verify(orchestrator, times(1)).enqueueGlTask(taskCaptor.capture());
			taskCaptor.getValue().run();

			meshBuilder.verify(() -> RocketMeshBuilder.applySnapshot(scene, latestSnapshot, configuration));
			meshBuilder.verify(() -> RocketMeshBuilder.applySnapshot(scene, firstSnapshot, configuration), never());
		}
	}
}
