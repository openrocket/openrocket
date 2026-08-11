package info.openrocket.swing.gui.figure3d.photo;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.DifferenceTolerance;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Golden image for the real Photo Studio settings and rendering path. */
@Tag("requires-live-opengl")
@Timeout(value = 90, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PhotoStudioGoldenTest extends BaseTestCase {

	private static final DifferenceTolerance PHOTO_TOLERANCE = new DifferenceTolerance(18, 3.0, 0.04);
	private static final DifferenceTolerance EFFECT_TOLERANCE = new DifferenceTolerance(22, 4.0, 0.06);

	@Test
	void configuredPhotoStudioMatchesApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio.png", "photo-studio-actual.png",
				"Photo Studio", settings -> { }, PHOTO_TOLERANCE);
	}

	@Test
	void alternateLightingMatchesApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio-alternate-light.png",
				"photo-studio-alternate-light-actual.png", "Photo Studio alternate lighting", settings -> {
					settings.setLight(-0.22, 0.78);
					settings.setLightStrength(1.35);
					settings.setSunlight(new ORColor(182, 215, 255));
					settings.setAmbiance(0.26);
				}, PHOTO_TOLERANCE);
	}

	@Test
	void smokeMatchesApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio-smoke.png", "photo-studio-smoke-actual.png",
				"Photo Studio smoke", settings -> {
					frameExhaust(settings);
					settings.setSmoke(true);
					settings.setSmokeColor(new ORColor(185, 192, 205));
					settings.setSmokeOpacity(0.78);
					settings.setExhaustScale(0.30);
				}, EFFECT_TOLERANCE);
	}

	@Test
	void flameMatchesApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio-flame.png", "photo-studio-flame-actual.png",
				"Photo Studio flame", settings -> {
					frameExhaust(settings);
					settings.setFlame(true);
					settings.setFlameColor(new ORColor(255, 112, 38));
					settings.setExhaustScale(0.60);
					settings.setFlameAspectRatio(1.25);
				}, EFFECT_TOLERANCE);
	}

	@Test
	void sparksMatchApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio-sparks.png", "photo-studio-sparks-actual.png",
				"Photo Studio sparks", settings -> {
					frameExhaust(settings);
					settings.setSparks(true);
					settings.setExhaustScale(0.32);
					settings.setSparkConcentration(1.35);
					settings.setSparkWeight(0.2);
				}, EFFECT_TOLERANCE);
	}

	@Test
	void motionBlurMatchesApprovedGolden() throws Exception {
		renderPhotoStudio("/figure3d/photo-studio-motion-blur.png",
				"photo-studio-motion-blur-actual.png", "Photo Studio motion blur", settings -> {
					settings.setMotionBlurred(true);
					settings.setMotionBlurAmount(9.0);
				}, EFFECT_TOLERANCE);
	}

	@Test
	void transparentSolidBackgroundCapturePreservesAlpha() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Photo Studio transparency test requires a live graphical environment");
		OpenRocketDocument document = GoldenImageTestSupport.createStyledRocketDocument();
		PhotoSettings settings = createBaseSettings();
		settings.setBackgroundType(PhotoSettings.BackgroundType.SOLID_COLOR);
		settings.setSkyColorOpacity(0.0);
		PhotoHarness harness = createHarness(document, settings);
		try {
			BufferedImage image = capture(harness.panel);
			int cornerAlpha = image.getRGB(0, 0) >>> 24;
			assertEquals(0, cornerAlpha, "Transparent exports must not contain the preview checkerboard");
		} finally {
			dispose(harness);
		}
	}

	private static void renderPhotoStudio(String goldenResource, String candidateFileName,
			String description, Consumer<PhotoSettings> settingsCustomizer,
			DifferenceTolerance tolerance) throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				description + " golden test requires a live graphical environment");
		OpenRocketDocument document = GoldenImageTestSupport.createStyledRocketDocument();
		PhotoSettings settings = createBaseSettings();
		settingsCustomizer.accept(settings);
		PhotoHarness harness = createHarness(document, settings);
		try {
			BufferedImage image = GoldenImageTestSupport.normalize(capture(harness.panel));
			GoldenImageTestSupport.assertMatchesApprovedGolden(PhotoStudioGoldenTest.class,
					goldenResource, candidateFileName, image, tolerance, description);
		} finally {
			dispose(harness);
		}
	}

	private static PhotoSettings createBaseSettings() {
		PhotoSettings settings = new PhotoSettings();
		settings.setBackgroundType(PhotoSettings.BackgroundType.GRADIENT);
		settings.setGradientTopColor(new ORColor(81, 126, 193));
		settings.setGradientBottomColor(new ORColor(28, 36, 54));
		settings.setView(0.12, -0.18, 0.48, 0.72);
		settings.setPitch(0.22);
		settings.setYaw(0.34);
		settings.setRoll(0.40);
		settings.setAdvance(0.0);
		settings.setLight(0.58, -0.72);
		settings.setLightStrength(1.1);
		settings.setSunlight(new ORColor(255, 242, 220));
		settings.setAmbiance(0.18);
		settings.setMotionBlurred(false);
		settings.setFlame(false);
		settings.setSmoke(false);
		settings.setSparks(false);
		return settings;
	}

	private static void frameExhaust(PhotoSettings settings) {
		settings.setView(0.12, -0.18, 0.78, 0.72);
		settings.setAdvance(0.10);
	}

	private static PhotoHarness createHarness(OpenRocketDocument document, PhotoSettings settings) throws Exception {
		return GoldenImageTestSupport.onEdt(() -> {
			PhotoPanel panel = new PhotoPanel(document, settings);
			panel.setPreferredSize(new Dimension(
					GoldenImageTestSupport.IMAGE_WIDTH, GoldenImageTestSupport.IMAGE_HEIGHT));

			JFrame frame = new JFrame("Photo Studio visual regression");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setResizable(false);
			frame.setContentPane(panel);
			frame.pack();
			frame.setLocationByPlatform(true);
			frame.setVisible(true);
			panel.setDoc(document);
			return new PhotoHarness(frame, panel);
		});
	}

	private static BufferedImage capture(PhotoPanel panel) throws Exception {
		CompletableFuture<BufferedImage> capture = new CompletableFuture<>();
		GoldenImageTestSupport.onEdt(() -> {
			panel.addImageCallback(capture::complete);
			return null;
		});
		try {
			BufferedImage image = capture.get(
					GoldenImageTestSupport.CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNotNull(image, "Photo Studio returned a null capture");
			return image;
		} catch (TimeoutException exception) {
			throw new AssertionError("Timed out capturing Photo Studio", exception);
		}
	}

	private static void dispose(PhotoHarness harness) throws Exception {
		GoldenImageTestSupport.onEdt(() -> {
			harness.panel.clearDoc();
			harness.frame.dispose();
			return null;
		});
	}

	private record PhotoHarness(JFrame frame, PhotoPanel panel) {
	}
}
