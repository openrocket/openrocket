package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.DifferenceTolerance;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.ImageDifference;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.RenderHarness;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static info.openrocket.swing.gui.figure3d.constants.RenderingConstants.SURFACE_ID_OUTSIDE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live-GL regression coverage for intersecting translucent geometry and mixed opaque /
 * translucent texture coverage.
 */
class WeightedBlendedTransparencyGoldenTest extends BaseTestCase {

	private static final String GOLDEN_RESOURCE = "/figure3d/weighted-blended-transparency.png";
	private static final DifferenceTolerance GOLDEN_TOLERANCE = new DifferenceTolerance(10, 1.25, 0.01);

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void intersectingTransparencyIsOrderIndependentAndMatchesApprovedGolden() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Weighted transparency golden test requires a live graphical environment");

		try (RenderHarness harness = createHarness(WeightedBlendedTransparencyGoldenTest::configureScene)) {
			GoldenImageTestSupport.awaitInitialized(harness);

			BufferedImage firstOrder = GoldenImageTestSupport.normalize(
					GoldenImageTestSupport.capture(harness, "weighted transparency frame"));
			reverseSceneObjects(harness);
			BufferedImage reversedOrder = GoldenImageTestSupport.normalize(
					GoldenImageTestSupport.capture(harness, "reordered weighted transparency frame"));

			ImageDifference orderDifference = GoldenImageTestSupport.compare(firstOrder, reversedOrder, 2);
			assertTrue(orderDifference.meanAbsoluteError() <= 0.10,
					() -> "Transparency changed with scene order: " + orderDifference);
			assertTrue(orderDifference.outlierFraction() <= 0.001,
					() -> "Too many transparency pixels changed with scene order: " + orderDifference);

			GoldenImageTestSupport.assertMatchesApprovedGolden(WeightedBlendedTransparencyGoldenTest.class,
					GOLDEN_RESOURCE, "weighted-blended-transparency-actual.png", firstOrder,
					GOLDEN_TOLERANCE, "Weighted transparency");
		}
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void opaqueTextureCoverageIgnoresComponentOpacity() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Texture opacity regression test requires a live graphical environment");
		AtomicReference<Appearance3D> texturedAppearance = new AtomicReference<>();

		try (RenderHarness harness = createHarness(
				orchestrator -> configureOpacityIndependentTextureScene(orchestrator, texturedAppearance))) {
			GoldenImageTestSupport.awaitInitialized(harness);

			BufferedImage opacityIndependent = GoldenImageTestSupport.normalize(
					GoldenImageTestSupport.capture(harness, "opacity-independent texture frame"));
			Appearance3D appearance = texturedAppearance.get();
			assertNotNull(appearance, "Test texture should be created during GL initialization");
			harness.panel().getScene3DOrchestrator().enqueueGlTask(() -> appearance.setOpacity(1.0f));
			BufferedImage fullyOpaqueComponent = GoldenImageTestSupport.normalize(
					GoldenImageTestSupport.capture(harness, "fully opaque texture frame"));

			ImageDifference difference = GoldenImageTestSupport.compare(
					fullyOpaqueComponent, opacityIndependent, 2);
			assertTrue(difference.meanAbsoluteError() <= 0.05,
					() -> "Opaque texture changed with component opacity: " + difference);
			assertTrue(difference.outlierFraction() <= 0.001,
					() -> "Too many opaque texture pixels changed with component opacity: " + difference);
		}
	}

	private static RenderHarness createHarness(Consumer<Scene3DOrchestrator> initializationHook)
			throws Exception {
		return GoldenImageTestSupport.createRenderHarness(
				OpenRocketDocumentFactory.createNewRocket().getRocket(),
				"Weighted transparency visual regression", initializationHook);
	}

	private static void configureScene(Scene3DOrchestrator orchestrator) {
		configureDeterministicRendering(orchestrator);
		SceneView scene = resetScene(orchestrator);

		Vector4f background = ColorUtils.srgbToLinear(new Vector4f(0.055f, 0.065f, 0.09f, 1.0f));
		scene.setBackground(new SolidColorBackground(background.x, background.y, background.z, background.w));
		configureCamera(scene);

		// A dark opaque card makes the blend colors easy to inspect and exercises
		// depth-testing translucent fragments against already-resolved opaque depth.
		scene.addObject(createQuad(
				new Vector3f(-1.62f, -1.12f, -0.90f),
				new Vector3f(1.62f, -1.12f, -0.90f),
				new Vector3f(1.62f, 1.12f, -0.90f),
				new Vector3f(-1.62f, 1.12f, -0.90f),
				appearance(0.12f, 0.145f, 0.19f, 1.0f)));

		// These two planes exchange front/back order at x=0. A single object-level
		// sort cannot render both halves correctly.
		scene.addObject(createQuad(
				new Vector3f(-1.38f, -0.86f, 0.58f),
				new Vector3f(1.38f, -0.86f, -0.58f),
				new Vector3f(1.38f, 0.86f, -0.58f),
				new Vector3f(-1.38f, 0.86f, 0.58f),
				appearance(0.96f, 0.16f, 0.12f, 0.58f)));
		scene.addObject(createQuad(
				new Vector3f(-1.38f, -0.66f, -0.58f),
				new Vector3f(1.38f, -0.66f, 0.58f),
				new Vector3f(1.38f, 0.66f, 0.58f),
				new Vector3f(-1.38f, 0.66f, -0.58f),
				appearance(0.12f, 0.34f, 0.98f, 0.58f)));

		// An opaque foreground strip verifies that the shared resolved depth attachment
		// rejects both transparency accumulation passes identically.
		scene.addObject(createQuad(
				new Vector3f(-0.085f, -0.97f, 0.82f),
				new Vector3f(0.085f, -0.97f, 0.82f),
				new Vector3f(0.085f, 0.97f, 0.82f),
				new Vector3f(-0.085f, 0.97f, 0.82f),
				appearance(0.86f, 0.89f, 0.94f, 1.0f)));
	}

	private static void configureOpacityIndependentTextureScene(Scene3DOrchestrator orchestrator,
			AtomicReference<Appearance3D> texturedAppearance) {
		configureDeterministicRendering(orchestrator);
		GraphicsQualitySettings quality = orchestrator.getRenderingConfiguration().getQuality();
		quality.setQuality(GraphicsQualitySettings.RenderQuality.MEDIUM);
		quality.setMSAAEnabled(true);
		orchestrator.getRenderingConfiguration().notifyListeners();
		SceneView scene = resetScene(orchestrator);

		Vector4f background = ColorUtils.srgbToLinear(new Vector4f(0.025f, 0.03f, 0.045f, 1.0f));
		scene.setBackground(new SolidColorBackground(background.x, background.y, background.z, background.w));
		configureCamera(scene);

		scene.addObject(createQuad(
				new Vector3f(-1.62f, -1.12f, -0.80f),
				new Vector3f(1.62f, -1.12f, -0.80f),
				new Vector3f(1.62f, 1.12f, -0.80f),
				new Vector3f(-1.62f, 1.12f, -0.80f),
				appearance(0.08f, 0.10f, 0.14f, 1.0f)));
		// This translucent layer must be completely hidden by opaque texture coverage.
		scene.addObject(createQuad(
				new Vector3f(-1.42f, -0.92f, -0.20f),
				new Vector3f(1.42f, -0.92f, -0.20f),
				new Vector3f(1.42f, 0.92f, -0.20f),
				new Vector3f(-1.42f, 0.92f, -0.20f),
				appearance(0.05f, 0.95f, 0.18f, 0.72f)));

		Appearance3D textureAppearance = new Appearance3D(
				new Vector3f(0.75f, 0.12f, 0.08f), new Texture(64, 64),
				Appearance3D.RenderStyle.TEXTURED);
		textureAppearance.setUnlit(true);
		textureAppearance.setShine(0.0f);
		textureAppearance.setOpacity(0.08f);
		textureAppearance.setOpacityAffectsTexture(false);
		texturedAppearance.set(textureAppearance);
		scene.addObject(createQuad(
				new Vector3f(-1.25f, -0.78f, 0.45f),
				new Vector3f(1.25f, -0.78f, 0.45f),
				new Vector3f(1.25f, 0.78f, 0.45f),
				new Vector3f(-1.25f, 0.78f, 0.45f),
				textureAppearance));
	}

	private static void configureDeterministicRendering(Scene3DOrchestrator orchestrator) {
		RenderingConfiguration config = orchestrator.getRenderingConfiguration();
		GraphicsQualitySettings quality = config.getQuality();
		quality.setQuality(GraphicsQualitySettings.RenderQuality.LOW);
		quality.setMSAAEnabled(false);
		quality.setFXAAEnabled(false);
		quality.setRoughnessBumpEnabled(false);
		quality.setShadowsEnabled(false);
		quality.setAmbientOcclusionEnabled(false);
		quality.setBackfaceCullingEnabled(false);

		VisualEffectsSettings effects = config.getVisualEffects();
		effects.setParticleEffectsEnabled(false);
		effects.setMotionBlurEnabled(false);
		effects.setOriginAxesVisible(false);
		effects.setLightVisualizersVisible(false);
		effects.setCameraPointOfInterestVisible(false);
		effects.setCaretsVisible(false);
		config.getDisplay().setMode(DisplaySettings.RenderMode.FINISHED);
		config.notifyListeners();
	}

	private static SceneView resetScene(Scene3DOrchestrator orchestrator) {
		SceneView scene = orchestrator.getScene();
		for (SceneObject object : scene.getObjects()) {
			object.cleanup();
		}
		scene.clearObjects();
		return scene;
	}

	private static void configureCamera(SceneView scene) {
		Camera camera = scene.getCamera();
		camera.setZoomLimits(0.25f, 20.0f);
		camera.setDistance(5.0f);
		camera.setFieldOfView(Math.toRadians(36.0));
		camera.setAngleX(0.0f);
		camera.setAngleY(0.0f);
		camera.setCenterOfInterest(new Vector3f());
		camera.update();
	}

	private static SceneObject createQuad(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3,
										  Appearance3D appearance) {
		Vector3f normal = new Vector3f(p1).sub(p0).cross(new Vector3f(p2).sub(p0)).normalize();
		List<Vertex> vertices = List.of(
				new Vertex(p0, new Vector3f(normal), new Vector2f(0.0f, 0.0f), SURFACE_ID_OUTSIDE),
				new Vertex(p1, new Vector3f(normal), new Vector2f(1.0f, 0.0f), SURFACE_ID_OUTSIDE),
				new Vertex(p2, new Vector3f(normal), new Vector2f(1.0f, 1.0f), SURFACE_ID_OUTSIDE),
				new Vertex(p3, new Vector3f(normal), new Vector2f(0.0f, 1.0f), SURFACE_ID_OUTSIDE));
		IntList indices = new IntList(6);
		indices.addTriangle(0, 1, 2);
		indices.addTriangle(0, 2, 3);
		return new SceneObject(new Mesh(vertices, indices), new Vector3f(), appearance);
	}

	private static Appearance3D appearance(float red, float green, float blue, float opacity) {
		Appearance3D appearance = new Appearance3D(new Vector3f(red, green, blue));
		appearance.setUnlit(true);
		appearance.setShine(0.0f);
		appearance.setOpacity(opacity);
		return appearance;
	}

	private static void reverseSceneObjects(RenderHarness harness) {
		Scene3DOrchestrator orchestrator = harness.panel().getScene3DOrchestrator();
		assertNotNull(orchestrator, "Scene should exist after GL initialization");
		orchestrator.enqueueGlTask(() -> {
			List<SceneObject> reversed = new ArrayList<>(orchestrator.getScene().getObjects());
			Collections.reverse(reversed);
			orchestrator.getScene().clearObjects();
			for (SceneObject object : reversed) {
				orchestrator.getScene().addObject(object);
			}
		});
	}

}
