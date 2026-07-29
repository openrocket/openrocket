package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraControls;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
		when(primaryObject.getAppearance()).thenReturn(primaryAppearance);

		SceneObject listenerObject = mock(SceneObject.class);
		Appearance3D listenerAppearance = new Appearance3D();
		when(listenerObject.getRocketComponent()).thenReturn(listener);
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
	}

	@Test
	void zoomFittedRebuildReframesWithoutRestoringTheDefaultView() {
		Rocket rocket = new Rocket();
		CameraControls camera = mock(CameraControls.class);
		when(camera.isZoomFitting()).thenReturn(true);
		Scene3DOrchestrator orchestrator = rebuildingOrchestrator(camera);
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, emptyScene(), rocket);

		synchronizer.componentChanged(new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE));

		verify(orchestrator).focusOnRocket();
		verify(orchestrator, never()).resetViewAndFocusOnRocket();
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
		when(orchestrator.getRenderingConfiguration()).thenReturn(RenderingConfiguration.builder().build());
		when(orchestrator.getCameraController()).thenReturn(camera);
		doAnswer(invocation -> {
			Runnable task = invocation.getArgument(0);
			task.run();
			return null;
		}).when(orchestrator).enqueueGlTask(any(Runnable.class));
		return orchestrator;
	}

	@Test
	void rebuildEventsCoalesceBeforeBuildingQueuedSnapshots() {
		SceneView scene = mock(SceneView.class);
		Scene3DOrchestrator orchestrator = mock(Scene3DOrchestrator.class);
		when(orchestrator.getRenderingConfiguration()).thenReturn(RenderingConfiguration.builder().build());

		Rocket rocket = new Rocket();
		RocketSceneSynchronizer synchronizer = new RocketSceneSynchronizer(orchestrator, scene, rocket);

		ComponentChangeEvent event = new ComponentChangeEvent(rocket, ComponentChangeEvent.MASS_CHANGE);
		synchronizer.componentChanged(event);
		synchronizer.componentChanged(event);

		verify(orchestrator, times(1)).enqueueGlTask(any(Runnable.class));
	}
}
