package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
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
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static info.openrocket.swing.gui.figure3d.constants.RenderingConstants.SURFACE_ID_OUTSIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live-GL regression coverage for intersecting translucent geometry and mixed opaque /
 * translucent texture coverage.
 */
class WeightedBlendedTransparencyGoldenTest extends BaseTestCase {

	private static final int IMAGE_WIDTH = 512;
	private static final int IMAGE_HEIGHT = 384;
	private static final long INITIALIZATION_TIMEOUT_SECONDS = 20;
	private static final long CAPTURE_TIMEOUT_SECONDS = 20;
	private static final String GOLDEN_RESOURCE = "/figure3d/weighted-blended-transparency.png";
	private static final Path CANDIDATE_PATH = Path.of(
			"build", "visual-regression", "weighted-blended-transparency-actual.png");

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void intersectingTransparencyIsOrderIndependentAndMatchesApprovedGolden() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Weighted transparency golden test requires a live graphical environment");

		try (RenderHarness harness = createHarness(WeightedBlendedTransparencyGoldenTest::configureScene)) {
			harness.requestRender();
			assertTrue(harness.panel.awaitInitialized(TimeUnit.SECONDS.toMillis(INITIALIZATION_TIMEOUT_SECONDS)),
					"OpenGL canvas did not initialize: " + harness.panel.getDebugStateSummary());

			BufferedImage firstOrder = normalize(capture(harness));
			reverseSceneObjects(harness);
			BufferedImage reversedOrder = normalize(capture(harness));

			ImageDifference orderDifference = compare(firstOrder, reversedOrder, 2);
			assertTrue(orderDifference.meanAbsoluteError() <= 0.10,
					() -> "Transparency changed with scene order: " + orderDifference);
			assertTrue(orderDifference.outlierFraction() <= 0.001,
					() -> "Too many transparency pixels changed with scene order: " + orderDifference);

			writeCandidate(firstOrder);
			compareWithApprovedGolden(firstOrder);
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
			harness.requestRender();
			assertTrue(harness.panel.awaitInitialized(TimeUnit.SECONDS.toMillis(INITIALIZATION_TIMEOUT_SECONDS)),
					"OpenGL canvas did not initialize: " + harness.panel.getDebugStateSummary());

			BufferedImage opacityIndependent = normalize(capture(harness));
			Appearance3D appearance = texturedAppearance.get();
			assertNotNull(appearance, "Test texture should be created during GL initialization");
			harness.panel.getScene3DOrchestrator().enqueueGlTask(() -> appearance.setOpacity(1.0f));
			BufferedImage fullyOpaqueComponent = normalize(capture(harness));

			ImageDifference difference = compare(fullyOpaqueComponent, opacityIndependent, 2);
			assertTrue(difference.meanAbsoluteError() <= 0.05,
					() -> "Opaque texture changed with component opacity: " + difference);
			assertTrue(difference.outlierFraction() <= 0.001,
					() -> "Too many opaque texture pixels changed with component opacity: " + difference);
		}
	}

	private static RenderHarness createHarness(Consumer<Scene3DOrchestrator> initializationHook)
			throws Exception {
		ExecutorService renderExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "weighted-transparency-golden-render");
			thread.setDaemon(true);
			return thread;
		});

		try {
			return onEdt(() -> {
				OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
				GLScenePanel panel = new GLScenePanel(document.getRocket(), null);
				panel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
				panel.setInitializationHook(initializationHook);

				JFrame frame = new JFrame("Weighted transparency visual regression");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setResizable(false);
				frame.add(panel);
				frame.pack();
				frame.setLocationByPlatform(true);

				RenderHarness harness = new RenderHarness(frame, panel, renderExecutor);
				panel.setRenderActivityCallback(harness::requestRender);
				panel.setRenderRequestCallback(harness::requestRender);
				frame.setVisible(true);
				return harness;
			});
		} catch (Exception exception) {
			renderExecutor.shutdownNow();
			throw exception;
		}
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
		camera.setPerspectiveProjection();
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

	private static BufferedImage capture(RenderHarness harness)
			throws Exception {
		CompletableFuture<BufferedImage> capture = new CompletableFuture<>();
		onEdt(() -> {
			harness.panel.requestImageCapture(false, capture::complete);
			return null;
		});
		harness.requestRender();
		try {
			BufferedImage image = capture.get(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNotNull(image, "Renderer returned a null capture");
			return image;
		} catch (TimeoutException exception) {
			throw new AssertionError("Timed out capturing weighted transparency frame: "
					+ harness.panel.getDebugStateSummary(), exception);
		}
	}

	private static void reverseSceneObjects(RenderHarness harness) {
		Scene3DOrchestrator orchestrator = harness.panel.getScene3DOrchestrator();
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

	private static BufferedImage normalize(BufferedImage source) {
		assertTrue(source.getWidth() > 0 && source.getHeight() > 0, "Captured image must not be empty");
		float aspect = (float) source.getWidth() / source.getHeight();
		assertEquals((float) IMAGE_WIDTH / IMAGE_HEIGHT, aspect, 0.01f,
				"Framebuffer aspect ratio should match the fixed test canvas");

		BufferedImage normalized = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < IMAGE_HEIGHT; y++) {
			int sourceY = Math.min(source.getHeight() - 1,
					(int) ((y + 0.5) * source.getHeight() / IMAGE_HEIGHT));
			for (int x = 0; x < IMAGE_WIDTH; x++) {
				int sourceX = Math.min(source.getWidth() - 1,
						(int) ((x + 0.5) * source.getWidth() / IMAGE_WIDTH));
				normalized.setRGB(x, y, source.getRGB(sourceX, sourceY));
			}
		}
		return normalized;
	}

	private static void writeCandidate(BufferedImage image) throws IOException {
		Files.createDirectories(CANDIDATE_PATH.getParent());
		assertTrue(ImageIO.write(image, "png", CANDIDATE_PATH.toFile()), "PNG writer should be available");
		System.out.println("Weighted transparency candidate: " + CANDIDATE_PATH.toAbsolutePath());
	}

	private static void compareWithApprovedGolden(BufferedImage actual) throws IOException {
		try (InputStream stream = WeightedBlendedTransparencyGoldenTest.class
				.getResourceAsStream(GOLDEN_RESOURCE)) {
			Assumptions.assumeTrue(stream != null,
					"Golden image is awaiting visual approval; inspect " + CANDIDATE_PATH.toAbsolutePath());
			BufferedImage expected = ImageIO.read(stream);
			assertNotNull(expected, "Approved transparency golden must be a readable PNG");
			assertEquals(IMAGE_WIDTH, expected.getWidth(), "Golden image width");
			assertEquals(IMAGE_HEIGHT, expected.getHeight(), "Golden image height");

			ImageDifference difference = compare(expected, actual, 10);
			assertTrue(difference.meanAbsoluteError() <= 1.25,
					() -> "Weighted transparency differs from its approved golden: " + difference);
			assertTrue(difference.outlierFraction() <= 0.01,
					() -> "Too many pixels differ from the approved golden: " + difference);
		}
	}

	private static ImageDifference compare(BufferedImage expected, BufferedImage actual, int outlierThreshold) {
		assertEquals(expected.getWidth(), actual.getWidth(), "Compared image widths");
		assertEquals(expected.getHeight(), actual.getHeight(), "Compared image heights");
		long absoluteError = 0;
		long outlierPixels = 0;
		long pixelCount = (long) expected.getWidth() * expected.getHeight();

		for (int y = 0; y < expected.getHeight(); y++) {
			for (int x = 0; x < expected.getWidth(); x++) {
				int expectedArgb = expected.getRGB(x, y);
				int actualArgb = actual.getRGB(x, y);
				int largestChannelDifference = 0;
				for (int shift = 0; shift <= 24; shift += 8) {
					int channelDifference = Math.abs(
							((expectedArgb >>> shift) & 0xff) - ((actualArgb >>> shift) & 0xff));
					absoluteError += channelDifference;
					largestChannelDifference = Math.max(largestChannelDifference, channelDifference);
				}
				if (largestChannelDifference > outlierThreshold) {
					outlierPixels++;
				}
			}
		}

		return new ImageDifference(
				absoluteError / (double) (pixelCount * 4),
				outlierPixels / (double) pixelCount);
	}

	private static <T> T onEdt(Callable<T> callable)
			throws InterruptedException, ExecutionException {
		if (SwingUtilities.isEventDispatchThread()) {
			try {
				return callable.call();
			} catch (Exception exception) {
				throw new ExecutionException(exception);
			}
		}
		FutureTask<T> task = new FutureTask<>(callable);
		SwingUtilities.invokeLater(task);
		return task.get();
	}

	private record ImageDifference(double meanAbsoluteError, double outlierFraction) {
		@Override
		public String toString() {
			return "mean absolute error=" + meanAbsoluteError
					+ ", outlier fraction=" + outlierFraction;
		}
	}

	private static final class RenderHarness implements AutoCloseable {
		private final JFrame frame;
		private final GLScenePanel panel;
		private final ExecutorService renderExecutor;
		private final AtomicBoolean acceptingRenders = new AtomicBoolean(true);

		private RenderHarness(JFrame frame, GLScenePanel panel, ExecutorService renderExecutor) {
			this.frame = frame;
			this.panel = panel;
			this.renderExecutor = renderExecutor;
		}

		private void requestRender() {
			if (!acceptingRenders.get()) {
				return;
			}
			try {
				renderExecutor.execute(panel::render);
			} catch (RejectedExecutionException ignored) {
				// Cleanup won the race with a delayed Swing recovery callback.
			}
		}

		@Override
		public void close() throws Exception {
			acceptingRenders.set(false);
			onEdt(() -> {
				panel.setRenderActivityCallback(null);
				panel.setRenderRequestCallback(null);
				return null;
			});

			Future<?> cleanup = renderExecutor.submit(panel::cleanup);
			try {
				cleanup.get(15, TimeUnit.SECONDS);
			} finally {
				renderExecutor.shutdownNow();
				renderExecutor.awaitTermination(5, TimeUnit.SECONDS);
				onEdt(() -> {
					frame.dispose();
					return null;
				});
			}
		}
	}
}
