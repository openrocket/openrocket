package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.DifferenceTolerance;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.RenderHarness;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Vector4f;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Golden images for the three display modes offered by the design view. */
@Tag("requires-live-opengl")
class Figure3DRenderModesGoldenTest extends BaseTestCase {

	private static final DifferenceTolerance TOLERANCE = new DifferenceTolerance(12, 1.75, 0.02);

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void figureModeWithoutMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.XRAY, false,
				"/figure3d/design-figure-msaa-disabled.png",
				"design-figure-msaa-disabled-actual.png", "3D Figure without MSAA");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void figureModeWithMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.XRAY, true,
				"/figure3d/design-figure-msaa-enabled.png",
				"design-figure-msaa-enabled-actual.png", "3D Figure with MSAA");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void unfinishedModeWithoutMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.UNFINISHED, false,
				"/figure3d/design-unfinished-msaa-disabled.png",
				"design-unfinished-msaa-disabled-actual.png", "3D Unfinished without MSAA");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void unfinishedModeWithMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.UNFINISHED, true,
				"/figure3d/design-unfinished-msaa-enabled.png",
				"design-unfinished-msaa-enabled-actual.png", "3D Unfinished with MSAA");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void finishedModeWithoutMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.FINISHED, false,
				"/figure3d/design-finished-msaa-disabled.png",
				"design-finished-msaa-disabled-actual.png", "3D Finished without MSAA");
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void finishedModeWithMsaaMatchesApprovedGolden() throws Exception {
		renderMode(DisplaySettings.RenderMode.FINISHED, true,
				"/figure3d/design-finished-msaa-enabled.png",
				"design-finished-msaa-enabled-actual.png", "3D Finished with MSAA");
	}

	private static void renderMode(DisplaySettings.RenderMode mode, boolean msaaEnabled, String goldenResource,
			String candidateFileName, String description) throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				description + " golden test requires a live graphical environment");
		OpenRocketDocument document = GoldenImageTestSupport.createStyledRocketDocument();

		try (RenderHarness harness = GoldenImageTestSupport.createRenderHarness(
				document.getRocket(), description + " visual regression",
				orchestrator -> configureScene(orchestrator, mode, msaaEnabled))) {
			GoldenImageTestSupport.awaitInitialized(harness);
			assertSceneSampleCount(harness, msaaEnabled ? 2 : 0);
			BufferedImage image = GoldenImageTestSupport.normalize(
					GoldenImageTestSupport.capture(harness, description));
			GoldenImageTestSupport.assertMatchesApprovedGolden(Figure3DRenderModesGoldenTest.class,
					goldenResource, candidateFileName, image, TOLERANCE, description);
		}
	}

	private static void configureScene(Scene3DOrchestrator orchestrator, DisplaySettings.RenderMode mode,
			boolean msaaEnabled) {
		RenderingConfiguration config = orchestrator.getRenderingConfiguration();
		GraphicsQualitySettings quality = config.getQuality();
		quality.setQuality(GraphicsQualitySettings.RenderQuality.MEDIUM);
		quality.setMSAAEnabled(msaaEnabled);
		quality.setFXAAEnabled(false);
		quality.setRoughnessBumpEnabled(false);
		quality.setShadowsEnabled(false);
		quality.setAmbientOcclusionEnabled(false);
		quality.setBackfaceCullingEnabled(true);

		VisualEffectsSettings effects = config.getVisualEffects();
		effects.setParticleEffectsEnabled(false);
		effects.setMotionBlurEnabled(false);
		effects.setOriginAxesVisible(false);
		effects.setLightVisualizersVisible(false);
		effects.setCameraPointOfInterestVisible(false);
		effects.setCaretsVisible(false);
		effects.setAmbientLightFactor(0.16f);

		config.getDisplay().setMode(mode);
		config.getDisplay().setRenderInternalSurfaces(true);
		config.notifyListeners();

		SceneView scene = orchestrator.getScene();
		Vector4f background = ColorUtils.srgbToLinear(new Vector4f(0.065f, 0.075f, 0.105f, 1.0f));
		scene.setBackground(new SolidColorBackground(background.x, background.y, background.z, background.w));
		scene.setSelection(List.of());

		Light light = scene.getLightController().getLights().get(0);
		light.setDirection(-0.45f, -0.8f, -0.55f);
		light.setColor(1.0f, 0.96f, 0.9f);

		Camera camera = scene.getCamera();
		camera.setAngleX(0.32f);
		camera.setAngleY(0.18f);
		camera.update();
		orchestrator.focusOnRocket();
	}

	private static void assertSceneSampleCount(RenderHarness harness, int expectedSamples) {
		RealisticRenderer renderer = assertInstanceOf(RealisticRenderer.class,
				harness.panel().getScene3DOrchestrator().getRenderer());
		assertEquals(expectedSamples, renderer.getSceneSampleCount(),
				"The golden must exercise the requested MSAA path");
	}
}
