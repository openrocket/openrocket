package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import org.junit.jupiter.api.Assumptions;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared live-GL harness and image comparison helpers for figure3d golden tests. */
public final class GoldenImageTestSupport {

	public static final int IMAGE_WIDTH = 512;
	public static final int IMAGE_HEIGHT = 384;
	public static final long INITIALIZATION_TIMEOUT_SECONDS = 20;
	public static final long CAPTURE_TIMEOUT_SECONDS = 20;
	private static final Path CANDIDATE_DIRECTORY = Path.of("build", "visual-regression");

	private GoldenImageTestSupport() {
	}

	/** Creates the styled rocket shared by the design-view and Photo Studio baselines. */
	public static OpenRocketDocument createStyledRocketDocument() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setSelectedConfiguration(TestRockets.TEST_FCID_0);

		for (RocketComponent component : rocket) {
			Appearance appearance;
			if (component instanceof NoseCone) {
				appearance = new Appearance(new ORColor(232, 77, 48), 0.75);
			} else if (component instanceof BodyTube) {
				appearance = new Appearance(new ORColor(42, 112, 205), 0.45);
			} else if (component instanceof FinSet) {
				appearance = new Appearance(new ORColor(244, 180, 38), 0.3);
			} else if (component instanceof LaunchLug) {
				appearance = new Appearance(new ORColor(62, 165, 101), 0.35);
			} else {
				appearance = new Appearance(new ORColor(186, 192, 204), 0.25);
			}
			component.setAppearance(appearance);
			if (component instanceof ExternalComponent externalComponent) {
				externalComponent.setFinish(ExternalComponent.Finish.NORMAL);
			}
		}

		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}

	public static RenderHarness createRenderHarness(Rocket rocket, String title,
			Consumer<Scene3DOrchestrator> initializationHook) throws Exception {
		ExecutorService renderExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "golden-image-render");
			thread.setDaemon(true);
			return thread;
		});

		try {
			return onEdt(() -> {
				GLScenePanel panel = new GLScenePanel(rocket, null);
				panel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
				panel.setInitializationHook(initializationHook);

				JFrame frame = new JFrame(title);
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

	public static void awaitInitialized(RenderHarness harness) {
		harness.requestRender();
		assertTrue(harness.panel.awaitInitialized(TimeUnit.SECONDS.toMillis(INITIALIZATION_TIMEOUT_SECONDS)),
				"OpenGL canvas did not initialize: " + harness.panel.getDebugStateSummary());
	}

	public static BufferedImage capture(RenderHarness harness, String description) throws Exception {
		CompletableFuture<BufferedImage> capture = new CompletableFuture<>();
		onEdt(() -> {
			harness.panel.requestImageCapture(false, capture::complete);
			return null;
		});
		harness.requestRender();
		try {
			BufferedImage image = capture.get(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNotNull(image, "Renderer returned a null " + description + " capture");
			return image;
		} catch (TimeoutException exception) {
			throw new AssertionError("Timed out capturing " + description + ": "
					+ harness.panel.getDebugStateSummary(), exception);
		}
	}

	public static BufferedImage normalize(BufferedImage source) {
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

	/** Writes the candidate and compares it when an approved resource is present. */
	public static Path assertMatchesApprovedGolden(Class<?> resourceOwner, String goldenResource,
			String candidateFileName, BufferedImage actual, DifferenceTolerance tolerance,
			String description) throws IOException {
		Path candidatePath = CANDIDATE_DIRECTORY.resolve(candidateFileName);
		Files.createDirectories(candidatePath.getParent());
		assertTrue(ImageIO.write(actual, "png", candidatePath.toFile()), "PNG writer should be available");
		System.out.println(description + " candidate: " + candidatePath.toAbsolutePath());

		try (InputStream stream = resourceOwner.getResourceAsStream(goldenResource)) {
			Assumptions.assumeTrue(stream != null,
					"Golden image is awaiting visual approval; inspect " + candidatePath.toAbsolutePath());
			BufferedImage expected = ImageIO.read(stream);
			assertNotNull(expected, "Approved golden must be a readable PNG");
			assertEquals(IMAGE_WIDTH, expected.getWidth(), "Golden image width");
			assertEquals(IMAGE_HEIGHT, expected.getHeight(), "Golden image height");

			ImageDifference difference = compare(expected, actual, tolerance.outlierThreshold());
			assertTrue(difference.meanAbsoluteError() <= tolerance.maximumMeanAbsoluteError(),
					() -> description + " differs from its approved golden: " + difference);
			assertTrue(difference.outlierFraction() <= tolerance.maximumOutlierFraction(),
					() -> "Too many pixels differ from the approved " + description + " golden: " + difference);
		}
		return candidatePath;
	}

	public static ImageDifference compare(BufferedImage expected, BufferedImage actual, int outlierThreshold) {
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

	public static <T> T onEdt(Callable<T> callable) throws InterruptedException, ExecutionException {
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

	public record DifferenceTolerance(int outlierThreshold, double maximumMeanAbsoluteError,
			double maximumOutlierFraction) {
	}

	public record ImageDifference(double meanAbsoluteError, double outlierFraction) {
		@Override
		public String toString() {
			return "mean absolute error=" + meanAbsoluteError + ", outlier fraction=" + outlierFraction;
		}
	}

	public static final class RenderHarness implements AutoCloseable {
		private final JFrame frame;
		private final GLScenePanel panel;
		private final ExecutorService renderExecutor;
		private final AtomicBoolean acceptingRenders = new AtomicBoolean(true);

		private RenderHarness(JFrame frame, GLScenePanel panel, ExecutorService renderExecutor) {
			this.frame = frame;
			this.panel = panel;
			this.renderExecutor = renderExecutor;
		}

		public GLScenePanel panel() {
			return panel;
		}

		public void requestRender() {
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
