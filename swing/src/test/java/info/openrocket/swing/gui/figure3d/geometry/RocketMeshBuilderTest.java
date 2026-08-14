package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketMeshBuilderTest extends BaseTestCase {

	@Test
	void snapshotReusesMeshesAcrossRepeatedComponentAndMotorInstances() {
		Rocket rocket = TestRockets.makeClusterPods();
		RocketSceneSnapshot snapshot = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());

		Map<RocketComponent, List<RocketSceneSnapshot.ComponentInstance>> componentsBySource =
				snapshot.getComponentInstances().stream()
						.collect(Collectors.groupingBy(RocketSceneSnapshot.ComponentInstance::component));
		List<RocketSceneSnapshot.ComponentInstance> repeatedComponent = componentsBySource.values().stream()
				.filter(instances -> instances.size() > 1)
				.findFirst()
				.orElseThrow();
		Mesh componentMesh = repeatedComponent.get(0).mesh();
		assertTrue(repeatedComponent.stream().allMatch(instance -> instance.mesh() == componentMesh));

		Map<RocketComponent, List<RocketSceneSnapshot.MotorInstance>> motorsByMount =
				snapshot.getMotorInstances().stream()
						.collect(Collectors.groupingBy(
								RocketSceneSnapshot.MotorInstance::mountForSelectionGrouping));
		List<RocketSceneSnapshot.MotorInstance> repeatedMotors = motorsByMount.values().stream()
				.filter(instances -> instances.size() > 1)
				.findFirst()
				.orElseThrow();
		Mesh motorMesh = repeatedMotors.get(0).motorMesh();
		for (RocketSceneSnapshot.MotorInstance motor : repeatedMotors) {
			assertSame(motorMesh, motor.motorMesh());
		}
	}

	@Test
	void rebuildingOriginAxesRemovesOnlyTaggedAxesAndCleansTheirResources() {
		SceneObject axis = mock(SceneObject.class);
		when(axis.isOriginAxis()).thenReturn(true);
		SceneObject otherOverlay = mock(SceneObject.class);
		Scene scene = mock(Scene.class);
		when(scene.getObjects()).thenReturn(List.of(axis, otherOverlay));
		RenderingConfiguration configuration = new RenderingConfiguration();
		configuration.getVisualEffects().setOriginAxesVisible(false);

		RocketMeshBuilder.rebuildOriginAxes(scene, configuration, true, true);

		verify(scene).removeObject(axis);
		verify(axis).cleanup();
		verify(scene, never()).removeObject(otherOverlay);
		verify(otherOverlay, never()).cleanup();
	}
}
