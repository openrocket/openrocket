package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InsideColorComponent;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory.ComponentAppearanceRole;
import info.openrocket.swing.gui.figure3d.materials.TextureTransform;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
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

	/**
	 * Regression test for #3489: separate-side fin appearances must land on the same physical
	 * faces as 24.12 (port/top vs starboard/bottom) and mirror right-side U as -u so saved
	 * sticker decals stay inside [0, 1] instead of being pushed off the fin.
	 */
	@Test
	void separateFinSidesAndRightStickerTransformsMatchLegacyOrientation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube bodyTube = rocket.getAllChildren().stream()
				.filter(BodyTube.class::isInstance)
				.map(BodyTube.class::cast)
				.findFirst()
				.orElseThrow();
		bodyTube.getChildren().stream()
				.filter(FinSet.class::isInstance)
				.toList()
				.forEach(bodyTube::removeChild);

		TrapezoidFinSet verticalTail = new TrapezoidFinSet();
		verticalTail.setFinCount(1);
		verticalTail.setBaseRotation(0.0);
		verticalTail.setRootChord(0.15);
		verticalTail.setTipChord(0.08);
		verticalTail.setHeight(0.12);
		verticalTail.setThickness(0.004);
		verticalTail.setAppearance(PRIMARY_APPEARANCE);
		verticalTail.getInsideColorComponentHandler().setInsideAppearance(SECONDARY_APPEARANCE);
		verticalTail.getInsideColorComponentHandler().setSeparateInsideOutside(true);
		bodyTube.addChild(verticalTail);

		TrapezoidFinSet starboardWing = new TrapezoidFinSet();
		starboardWing.setFinCount(1);
		starboardWing.setBaseRotation(Math.toRadians(90.0));
		starboardWing.setRootChord(0.28);
		starboardWing.setTipChord(0.10);
		starboardWing.setHeight(0.22);
		starboardWing.setThickness(0.004);
		starboardWing.setAppearance(PRIMARY_APPEARANCE);
		starboardWing.getInsideColorComponentHandler().setInsideAppearance(SECONDARY_APPEARANCE);
		starboardWing.getInsideColorComponentHandler().setSeparateInsideOutside(true);
		bodyTube.addChild(starboardWing);

		TrapezoidFinSet portWing = new TrapezoidFinSet();
		portWing.setFinCount(1);
		portWing.setBaseRotation(Math.toRadians(-90.0));
		portWing.setRootChord(0.28);
		portWing.setTipChord(0.10);
		portWing.setHeight(0.22);
		portWing.setThickness(0.004);
		portWing.setAppearance(PRIMARY_APPEARANCE);
		portWing.getInsideColorComponentHandler().setInsideAppearance(SECONDARY_APPEARANCE);
		portWing.getInsideColorComponentHandler().setSeparateInsideOutside(true);
		bodyTube.addChild(portWing);

		RocketSceneSnapshot snapshot = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());

		// Vertical stabilizer (rot=0): primary (left/port) faces +Z_world, secondary (right/starboard) faces -Z_world.
		RocketSceneSnapshot.ComponentInstance tailPrimary = findPart(snapshot, verticalTail, ComponentAppearanceRole.PRIMARY);
		RocketSceneSnapshot.ComponentInstance tailSecondary = findPart(snapshot, verticalTail, ComponentAppearanceRole.SECONDARY);
		assertTrue(firstSurfaceNormalWorld(tailPrimary, RenderingConstants.SURFACE_ID_OUTSIDE).z > 0.99f,
				"Vertical fin primary appearance should face port (+Z_world)");
		assertTrue(firstSurfaceNormalWorld(tailSecondary, RenderingConstants.SURFACE_ID_RIGHT).z < -0.99f,
				"Vertical fin secondary appearance should face starboard (-Z_world)");

		// Starboard wing (rot=+90 deg): primary is top (+Y_world), secondary is bottom (-Y_world).
		RocketSceneSnapshot.ComponentInstance stbdPrimary = findPart(snapshot, starboardWing, ComponentAppearanceRole.PRIMARY);
		RocketSceneSnapshot.ComponentInstance stbdSecondary = findPart(snapshot, starboardWing, ComponentAppearanceRole.SECONDARY);
		assertTrue(firstSurfaceNormalWorld(stbdPrimary, RenderingConstants.SURFACE_ID_OUTSIDE).y > 0.99f,
				"Starboard wing primary appearance should face top (+Y_world)");
		assertTrue(firstSurfaceNormalWorld(stbdSecondary, RenderingConstants.SURFACE_ID_RIGHT).y < -0.99f,
				"Starboard wing secondary appearance should face bottom (-Y_world)");

		// Port wing (rot=-90 deg): primary is bottom (-Y_world), secondary is top (+Y_world).
		RocketSceneSnapshot.ComponentInstance portPrimary = findPart(snapshot, portWing, ComponentAppearanceRole.PRIMARY);
		RocketSceneSnapshot.ComponentInstance portSecondary = findPart(snapshot, portWing, ComponentAppearanceRole.SECONDARY);
		assertTrue(firstSurfaceNormalWorld(portPrimary, RenderingConstants.SURFACE_ID_OUTSIDE).y < -0.99f,
				"Port wing primary appearance should face bottom (-Y_world)");
		assertTrue(firstSurfaceNormalWorld(portSecondary, RenderingConstants.SURFACE_ID_RIGHT).y > 0.99f,
				"Port wing secondary appearance should face top (+Y_world)");

		// A right-side sticker saved in 24.12 (positive offset.x because right-side raw U spans [-1, 0])
		// must transform a mid-fin point into the visible [0, 1] sticker square.
		TextureTransform tailRightTransform = new TextureTransform();
		tailRightTransform.setScaleFromTop(false);
		tailRightTransform.rotation = (float) Math.PI;
		tailRightTransform.offset.set(0.40f, -0.80f);
		tailRightTransform.scale.set(2.7777778f, 1.3333334f);
		Matrix4f tailRightMatrix = tailRightTransform.getTransformMatrix(new Matrix4f());

		// At s = 0.58 (midpoint of [0.40, 0.76]), right-side raw u = -0.58; at t = 0.425 (midpoint of [0.05, 0.80]), v = 0.425.
		Vector4f tailSample = tailRightMatrix.transform(new Vector4f(-0.58f, 0.425f, 0.0f, 1.0f));
		assertEquals(0.5f, tailSample.x, 1.0e-3f, "Right-side sticker center should land at u=0.5");
		assertEquals(0.5f, tailSample.y, 1.0e-3f, "Right-side sticker center should land at v=0.5");
	}

	@Test
	void cantedFinModelMatricesMatchTheFinSetGeometry() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet finSet = rocket.getAllChildren().stream()
				.filter(TrapezoidFinSet.class::isInstance)
				.map(TrapezoidFinSet.class::cast)
				.findFirst()
				.orElseThrow();
		finSet.setCantAngle(Math.toRadians(10));

		RocketSceneSnapshot snapshot = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		List<RocketSceneSnapshot.ComponentInstance> finInstances = snapshot.getComponentInstances().stream()
				.filter(instance -> instance.component() == finSet)
				.toList();
		List<InstanceContext> contexts = rocket.getSelectedConfiguration().getActiveInstances().get(finSet);
		assertEquals(contexts.size(), finInstances.size());

		double halfThickness = finSet.getThickness() / 2;
		List<CoordinateIF> finPoints = List.of(
				new Coordinate(0, 0, 0),
				new Coordinate(finSet.getLength(), 0, halfThickness),
				new Coordinate(finSet.getLength() / 2, finSet.getSpan(), -halfThickness));
		for (int i = 0; i < contexts.size(); i++) {
			Transformation expectedTransform = contexts.get(i).transform
					.applyTransformation(finSet.getCantRotation());
			Matrix4f modelMatrix = finInstances.get(i).modelMatrix();
			for (CoordinateIF point : finPoints) {
				CoordinateIF expected = expectedTransform.transform(point);
				// The engine frame is OpenRocket's frame with Z flipped
				Vector3f actual = modelMatrix.transformPosition(
						new Vector3f((float) point.getX(), (float) point.getY(), (float) -point.getZ()));
				float scale = RenderingConstants.WORLD_SCALE;
				assertEquals(expected.getX() * scale, actual.x, 1.0e-5f);
				assertEquals(expected.getY() * scale, actual.y, 1.0e-5f);
				assertEquals(-expected.getZ() * scale, actual.z, 1.0e-5f);
			}
		}
	}

	@Test
	void tubeFinInstancesSitTangentOutsideParentBodyTube() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube bodyTube = rocket.getAllChildren().stream()
				.filter(BodyTube.class::isInstance)
				.map(BodyTube.class::cast)
				.findFirst()
				.orElseThrow();
		double bodyRadius = 0.033401;
		bodyTube.setOuterRadius(bodyRadius);

		TubeFinSet tubeFinSet = new TubeFinSet();
		tubeFinSet.setFinCount(6);
		tubeFinSet.setOuterRadiusAutomatic(false);
		tubeFinSet.setOuterRadius(bodyRadius);
		tubeFinSet.setThickness(0.000889);
		tubeFinSet.setLength(0.1397);
		tubeFinSet.setBaseRotation(Math.toRadians(20));
		bodyTube.addChild(tubeFinSet);

		RocketSceneSnapshot snapshot = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		List<RocketSceneSnapshot.ComponentInstance> tubeFinInstances = snapshot.getComponentInstances().stream()
				.filter(instance -> instance.component() == tubeFinSet)
				.toList();
		List<InstanceContext> contexts = rocket.getSelectedConfiguration().getActiveInstances().get(tubeFinSet);
		assertEquals(6, contexts.size());
		assertEquals(contexts.size(), tubeFinInstances.size());

		float scale = RenderingConstants.WORLD_SCALE;
		double expectedCenterRadial = (bodyRadius + tubeFinSet.getOuterRadius()) * scale;
		double expectedMinRadial = bodyRadius * scale;
		double expectedMaxRadial = (bodyRadius + 2.0 * tubeFinSet.getOuterRadius()) * scale;

		for (int i = 0; i < contexts.size(); i++) {
			RocketSceneSnapshot.ComponentInstance instance = tubeFinInstances.get(i);
			Matrix4f modelMatrix = instance.modelMatrix();

			CoordinateIF expectedCenter = contexts.get(i).transform
					.transform(new Coordinate(tubeFinSet.getLength() / 2.0, tubeFinSet.getOuterRadius(), 0));
			Vector3f actualCenter = modelMatrix.transformPosition(new Vector3f(0, 0, 0));
			assertEquals(expectedCenter.getX() * scale, actualCenter.x, 1.0e-5f);
			assertEquals(expectedCenter.getY() * scale, actualCenter.y, 1.0e-5f);
			assertEquals(-expectedCenter.getZ() * scale, actualCenter.z, 1.0e-5f);
			assertEquals(expectedCenterRadial, Math.hypot(actualCenter.y, actualCenter.z), 1.0e-5);

			double minRadial = Double.POSITIVE_INFINITY;
			double maxRadial = Double.NEGATIVE_INFINITY;
			for (Vertex vertex : instance.mesh().getVertices()) {
				Vector3f worldPos = modelMatrix.transformPosition(new Vector3f(vertex.position));
				double radial = Math.hypot(worldPos.y, worldPos.z);
				minRadial = Math.min(minRadial, radial);
				maxRadial = Math.max(maxRadial, radial);
			}
			assertEquals(expectedMinRadial, minRadial, 1.0e-3,
					"Tube fin must sit tangent to the outside of the parent body tube, not inside it");
			assertEquals(expectedMaxRadial, maxRadial, 1.0e-3);
		}
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
		boolean sawNonZeroU = false;
		for (int i = 0; i < primary.getVertices().size(); i++) {
			Vertex primaryVertex = primary.getVertices().get(i);
			if (primaryVertex.surfaceID == RenderingConstants.SURFACE_ID_RIGHT) {
				assertEquals(-primaryVertex.texCoords.x, secondary.getVertices().get(i).texCoords.x, 1.0e-6f,
						"The independently styled right fin face should negate U to match 24.12 decal offsets");
				if (primaryVertex.texCoords.x > 0.01f) {
					sawNonZeroU = true;
				}
			}
		}
		assertTrue(sawNonZeroU, "Fin mesh has no right-side vertex with non-zero U");
	}

	private static Vector3f firstSurfaceNormalWorld(RocketSceneSnapshot.ComponentInstance instance, int surfaceId) {
		for (int index : instance.mesh().getIndices()) {
			Vertex vertex = instance.mesh().getVertices().get(index);
			if (vertex.surfaceID == surfaceId) {
				return instance.modelMatrix().transformDirection(new Vector3f(vertex.normal)).normalize();
			}
		}
		throw new AssertionError("No vertex with surface ID " + surfaceId + " referenced by mesh");
	}
}
