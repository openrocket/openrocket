package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InsideColorComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory.ComponentAppearanceRole;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketMeshBuilderTest extends BaseTestCase {
	private static final Appearance PRIMARY_APPEARANCE = new Appearance(new ORColor(220, 30, 20), 0.2);
	private static final Appearance SECONDARY_APPEARANCE = new Appearance(new ORColor(20, 60, 220), 0.7);

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
	void separateTubeAppearancePartitionsOuterInnerAndEdgeSurfaces() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube tube = rocket.getAllChildren().stream()
				.filter(BodyTube.class::isInstance)
				.map(BodyTube.class::cast)
				.findFirst()
				.orElseThrow();
		tube.setAppearance(PRIMARY_APPEARANCE);
		tube.getInsideColorComponentHandler().setInsideAppearance(SECONDARY_APPEARANCE);
		tube.getInsideColorComponentHandler().setSeparateInsideOutside(true);

		RocketSceneSnapshot outsideEdges = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		RocketSceneSnapshot.ComponentInstance primary = findPart(outsideEdges, tube, ComponentAppearanceRole.PRIMARY);
		RocketSceneSnapshot.ComponentInstance secondary = findPart(
				outsideEdges, tube, ComponentAppearanceRole.SECONDARY);

		assertSame(PRIMARY_APPEARANCE, primary.appearance().appearance());
		assertSame(SECONDARY_APPEARANCE, secondary.appearance().appearance());
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_OUTSIDE,
				RenderingConstants.SURFACE_ID_FORE, RenderingConstants.SURFACE_ID_AFT),
				referencedSurfaceIds(primary.mesh()));
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_INSIDE), referencedSurfaceIds(secondary.mesh()));

		tube.getInsideColorComponentHandler().setEdgesSameAsInside(true);
		RocketSceneSnapshot insideEdges = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		primary = findPart(insideEdges, tube, ComponentAppearanceRole.PRIMARY);
		secondary = findPart(insideEdges, tube, ComponentAppearanceRole.SECONDARY);
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_OUTSIDE), referencedSurfaceIds(primary.mesh()));
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_INSIDE,
				RenderingConstants.SURFACE_ID_FORE, RenderingConstants.SURFACE_ID_AFT),
				referencedSurfaceIds(secondary.mesh()));
	}

	@Test
	void separateFinAppearancePartitionsLeftRightAndEdgeSurfaces() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		FinSet finSet = rocket.getAllChildren().stream()
				.filter(FinSet.class::isInstance)
				.map(FinSet.class::cast)
				.findFirst()
				.orElseThrow();
		finSet.setAppearance(PRIMARY_APPEARANCE);
		finSet.getInsideColorComponentHandler().setInsideAppearance(SECONDARY_APPEARANCE);
		finSet.getInsideColorComponentHandler().setSeparateInsideOutside(true);

		RocketSceneSnapshot outsideEdges = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		RocketSceneSnapshot.ComponentInstance primary = findPart(
				outsideEdges, finSet, ComponentAppearanceRole.PRIMARY);
		RocketSceneSnapshot.ComponentInstance secondary = findPart(
				outsideEdges, finSet, ComponentAppearanceRole.SECONDARY);

		assertSame(PRIMARY_APPEARANCE, primary.appearance().appearance());
		assertSame(SECONDARY_APPEARANCE, secondary.appearance().appearance());
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_OUTSIDE, RenderingConstants.SURFACE_ID_EDGE),
				referencedSurfaceIds(primary.mesh()));
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_RIGHT), referencedSurfaceIds(secondary.mesh()));
		assertRightSideTextureCoordinatesAreMirrored(primary.mesh(), secondary.mesh());

		finSet.getInsideColorComponentHandler().setEdgesSameAsInside(true);
		RocketSceneSnapshot insideEdges = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		primary = findPart(insideEdges, finSet, ComponentAppearanceRole.PRIMARY);
		secondary = findPart(insideEdges, finSet, ComponentAppearanceRole.SECONDARY);
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_OUTSIDE), referencedSurfaceIds(primary.mesh()));
		assertEquals(Set.of(RenderingConstants.SURFACE_ID_RIGHT, RenderingConstants.SURFACE_ID_EDGE),
				referencedSurfaceIds(secondary.mesh()));
	}

	@Test
	void allInsideCapableComponentMeshesRespectAppearanceBoundaries() {
		List<Rocket> rockets = List.of(
				TestRockets.makeEstesAlphaIII(),
				new TestRockets("separate-surface-appearance").makeTestRocket());
		for (Rocket rocket : rockets) {
			List<InsideColorComponent> components = rocket.getAllChildren().stream()
					.filter(InsideColorComponent.class::isInstance)
					.map(InsideColorComponent.class::cast)
					.toList();
			for (InsideColorComponent component : components) {
				component.getInsideColorComponentHandler().setSeparateInsideOutside(true);
				component.getInsideColorComponentHandler().setEdgesSameAsInside(false);
			}
			assertFalse(RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration())
					.getComponentInstances().isEmpty());

			for (InsideColorComponent component : components) {
				component.getInsideColorComponentHandler().setEdgesSameAsInside(true);
			}
			assertFalse(RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration())
					.getComponentInstances().isEmpty());
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

	private static RocketSceneSnapshot.ComponentInstance findPart(RocketSceneSnapshot snapshot,
			RocketComponent component, ComponentAppearanceRole role) {
		return snapshot.getComponentInstances().stream()
				.filter(instance -> instance.component() == component && instance.appearanceRole() == role)
				.findFirst()
				.orElseThrow();
	}

	private static Set<Integer> referencedSurfaceIds(Mesh mesh) {
		Set<Integer> surfaceIds = new HashSet<>();
		for (int index : mesh.getIndices()) {
			surfaceIds.add(mesh.getVertices().get(index).surfaceID);
		}
		return surfaceIds;
	}

	private static void assertRightSideTextureCoordinatesAreMirrored(Mesh primary, Mesh secondary) {
		for (int i = 0; i < primary.getVertices().size(); i++) {
			Vertex primaryVertex = primary.getVertices().get(i);
			if (primaryVertex.surfaceID == RenderingConstants.SURFACE_ID_RIGHT) {
				assertEquals(1.0f - primaryVertex.texCoords.x, secondary.getVertices().get(i).texCoords.x, 1.0e-6f,
						"The independently styled right fin face should preserve readable decal orientation");
				return;
			}
		}
		throw new AssertionError("Fin mesh has no right-side vertex");
	}
}
