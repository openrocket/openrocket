package info.openrocket.swing.gui.figure3d.photo;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.motor.GeneralMotorLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Lake;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Meadow;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Miramar;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Mountains;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Orbit;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Storm;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoStudio3DStressTest extends BaseTestCase {

	private static final long FRAME_TIMEOUT_MS = 4_000;
	private static final long CAPTURE_TIMEOUT_MS = 5_000;
	private static final long CAMERA_SNAPSHOT_TIMEOUT_MS = 2_000;
	private static final long EDT_CALL_TIMEOUT_MS = 2_500;
	private static final long WATCHDOG_PERIOD_SECONDS = 5;
	private static final Sky[] TEST_SKIES = {
			Mountains.instance,
			Meadow.instance,
			Miramar.instance,
			Lake.instance,
			Orbit.instance,
			Storm.instance
	};

	@BeforeAll
	static void installMotorDatabaseFixture() {
		Module applicationModule = new ServicesForTesting();
		Module debugTranslator = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};
		Module pluginModule = new PluginModule();
		Module motorDbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(MotorDatabase.class).toProvider(new TestMotorDbProvider());
			}
		};
		Injector injector = Guice.createInjector(
				Modules.override(applicationModule).with(debugTranslator),
				pluginModule,
				motorDbOverrides);
		Application.setInjector(injector);
	}

	@Test
	@Timeout(value = 90, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void photoStudioRepeatedMutationsResizesAndCapturesProduceFreshFrames() throws Exception {
		assumeMacUiEnvironment();
		AtomicReference<String> phase = new AtomicReference<>("create-photo-harness");
		AtomicReference<PhotoHarness> harnessRef = new AtomicReference<>();

		PhotoHarness harness = null;
		try (DiagnosticWatchdog ignored = startWatchdog(
				"photoStudioRepeatedMutationsResizesAndCapturesProduceFreshFrames",
				phase::get,
				() -> describePhotoHarness(harnessRef.get()))) {
			harness = createPhotoHarness("PhotoStudio3DStressTest-photo-only");
			harnessRef.set(harness);
			final PhotoHarness currentHarness = harness;
			phase.set("await-photo-showing");
			try {
				waitForShowing(currentHarness.panel, 2_000, "Photo Studio panel should become visible before stress test");
				phase.set("await-initial-photo-frame");
				awaitFreshPhotoFrame(currentHarness.panel, null, -1, -1, FRAME_TIMEOUT_MS, "initial Photo Studio frame");

				for (int i = 0; i < 6; i++) {
					GLScenePanel beforeCanvas = currentPhotoCanvas(currentHarness.panel);
					int beforeSwap = beforeCanvas != null ? beforeCanvas.getSwapCallCount() : -1;
					int beforePaint = beforeCanvas != null ? beforeCanvas.getPaintCallCount() : -1;

					final int iteration = i;
					phase.set("mutate-photo-settings-" + iteration);
					onEdt(() -> {
						mutatePhotoSettings(currentHarness.settings, iteration);
						currentHarness.frame.setSize(960 + (iteration % 3) * 70, 680 + (iteration % 2) * 60);
						currentHarness.frame.setLocation(90 + iteration * 15, 90 + iteration * 11);
						currentHarness.frame.validate();
						currentHarness.frame.repaint();
					});

					phase.set("await-photo-frame-" + iteration);
					awaitFreshPhotoFrame(currentHarness.panel, beforeCanvas, beforeSwap, beforePaint,
							FRAME_TIMEOUT_MS, "Photo Studio iteration " + iteration);

					phase.set("capture-photo-image-" + iteration);
					BufferedImage image = capturePhotoImage(currentHarness.panel);
					assertNotNull(image, "Photo Studio capture should complete during stress iteration " + iteration);
					assertTrue(image.getWidth() > 0 && image.getHeight() > 0,
							"Photo Studio capture should produce a non-empty image");
				}
				phase.set("completed");
			} finally {
				disposePhotoHarness(harness);
			}
		}
	}

	@Test
	@Timeout(value = 120, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void repeatedPhotoStudioOpenCloseCyclesDoNotBreakDesignViewRendering() throws Exception {
		assumeMacUiEnvironment();
		AtomicReference<String> phase = new AtomicReference<>("create-design-harness");
		AtomicReference<DesignHarness> designHarnessRef = new AtomicReference<>();

		DesignHarness designHarness = null;
		try (DiagnosticWatchdog ignored = startWatchdog(
				"repeatedPhotoStudioOpenCloseCyclesDoNotBreakDesignViewRendering",
				phase::get,
				() -> describeDesignHarness(designHarnessRef.get()))) {
			designHarness = createDesignHarness();
			designHarnessRef.set(designHarness);
			final DesignHarness currentDesignHarness = designHarness;
			phase.set("await-design-showing");
			try {
				waitForShowing(currentDesignHarness.panel, 2_000, "Design view panel should become visible before stress test");
				phase.set("enable-design-3d");
				onEdt(() -> currentDesignHarness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D));
				phase.set("await-initial-design-frame");
				awaitFresh3DFrame(currentDesignHarness.panel.getFigure3d(), -1, -1, FRAME_TIMEOUT_MS,
						"initial design-view 3D frame");

				for (int i = 0; i < 5; i++) {
					phase.set("create-photo-cycle-" + i);
					PhotoHarness photoHarness = createPhotoHarness("PhotoStudio3DStressTest-cycle-" + i);
					try {
						phase.set("await-photo-cycle-showing-" + i);
						waitForShowing(photoHarness.panel, 2_000,
								"Photo Studio panel should become visible during open/close cycle " + i);
						phase.set("await-photo-cycle-initial-frame-" + i);
						awaitFreshPhotoFrame(photoHarness.panel, null, -1, -1, FRAME_TIMEOUT_MS,
								"initial Photo Studio frame for cycle " + i);

					GLScenePanel beforePhotoCanvas = currentPhotoCanvas(photoHarness.panel);
					int beforePhotoSwap = beforePhotoCanvas != null ? beforePhotoCanvas.getSwapCallCount() : -1;
					int beforePhotoPaint = beforePhotoCanvas != null ? beforePhotoCanvas.getPaintCallCount() : -1;
						int beforeDesignSwap = currentDesignHarness.panel.getFigure3d().getCanvasSwapCallCount();
						int beforeDesignPaint = currentDesignHarness.panel.getFigure3d().getCanvasPaintCallCount();
					final int iteration = i;

					phase.set("mutate-photo-and-design-cycle-" + iteration);
					onEdt(() -> {
						mutatePhotoSettings(photoHarness.settings, iteration);
						photoHarness.frame.setSize(980 + (iteration % 2) * 50, 720 + (iteration % 3) * 35);
						photoHarness.frame.setLocation(220 + iteration * 18, 120 + iteration * 12);
						photoHarness.frame.validate();
						photoHarness.frame.repaint();

						currentDesignHarness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView);
						currentDesignHarness.frame.setSize(920 + (iteration % 2) * 65, 640 + (iteration % 3) * 45);
						currentDesignHarness.frame.setLocation(50 + iteration * 12, 60 + iteration * 9);
						currentDesignHarness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
						currentDesignHarness.frame.validate();
						currentDesignHarness.frame.repaint();
					});

					phase.set("await-photo-cycle-frame-" + iteration);
					awaitFreshPhotoFrame(photoHarness.panel, beforePhotoCanvas, beforePhotoSwap, beforePhotoPaint,
							FRAME_TIMEOUT_MS, "Photo Studio frame for cycle " + iteration);
					phase.set("await-design-cycle-frame-" + iteration);
					awaitFresh3DFrame(currentDesignHarness.panel.getFigure3d(), beforeDesignSwap, beforeDesignPaint,
							FRAME_TIMEOUT_MS, "design-view frame while Photo Studio is open for cycle " + iteration);

					phase.set("capture-photo-cycle-" + iteration);
					BufferedImage photoImage = capturePhotoImage(photoHarness.panel);
					assertNotNull(photoImage, "Photo Studio should capture during cycle " + iteration);
					phase.set("capture-design-cycle-" + iteration);
					BufferedImage designImage = currentDesignHarness.panel.getFigure3d().captureImage();
					assertNotNull(designImage, "Design view should capture during cycle " + iteration);
				} finally {
					disposePhotoHarness(photoHarness);
				}

					int beforeSwap = currentDesignHarness.panel.getFigure3d().getCanvasSwapCallCount();
					int beforePaint = currentDesignHarness.panel.getFigure3d().getCanvasPaintCallCount();
					final int iteration = i;
					phase.set("recover-design-after-cycle-" + iteration);
					onEdt(() -> {
						currentDesignHarness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView);
						currentDesignHarness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
						currentDesignHarness.frame.setSize(940 + (iteration % 3) * 40, 660 + (iteration % 2) * 55);
						currentDesignHarness.frame.validate();
						currentDesignHarness.frame.repaint();
					});

					awaitFresh3DFrame(currentDesignHarness.panel.getFigure3d(), beforeSwap, beforePaint,
							FRAME_TIMEOUT_MS, "design-view recovery after Photo Studio cycle " + iteration);
				}
				phase.set("completed");
			} finally {
				disposeDesignHarness(designHarness);
			}
		}
	}

	@Test
	@Timeout(value = 150, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void concurrentDesignAndPhotoWindowsStayResponsiveUnderResizeAndInputBursts() throws Exception {
		assumeMacUiEnvironment();
		AtomicReference<String> phase = new AtomicReference<>("create-concurrent-harnesses");
		AtomicReference<DesignHarness> designARef = new AtomicReference<>();
		AtomicReference<DesignHarness> designBRef = new AtomicReference<>();
		AtomicReference<PhotoHarness> photoRef = new AtomicReference<>();

		DesignHarness designA = null;
		DesignHarness designB = null;
		PhotoHarness photo = null;
		try (DiagnosticWatchdog ignored = startWatchdog(
				"concurrentDesignAndPhotoWindowsStayResponsiveUnderResizeAndInputBursts",
				phase::get,
				() -> describeDesignHarness(designARef.get()),
				() -> describeDesignHarness(designBRef.get()),
				() -> describePhotoHarness(photoRef.get()))) {
			designA = createDesignHarness();
			designB = createDesignHarness();
			photo = createPhotoHarness("PhotoStudio3DStressTest-concurrent-photo");
			designARef.set(designA);
			designBRef.set(designB);
			photoRef.set(photo);
			final DesignHarness currentDesignA = designA;
			final DesignHarness currentDesignB = designB;
			final PhotoHarness currentPhoto = photo;
			try {
				phase.set("await-concurrent-showing");
				waitForShowing(currentDesignA.panel, 2_000, "Primary design view should be visible");
				waitForShowing(currentDesignB.panel, 2_000, "Secondary design view should be visible");
				waitForShowing(currentPhoto.panel, 2_000, "Photo Studio should be visible");

			phase.set("enable-concurrent-3d");
			onEdt(() -> {
				currentDesignA.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
				currentDesignB.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
				currentDesignB.panel.getFigure3d().setPanModeEnabled(true);
			});

			phase.set("await-concurrent-initial-frames");
			awaitFresh3DFrame(currentDesignA.panel.getFigure3d(), -1, -1, FRAME_TIMEOUT_MS, "initial design A frame");
			awaitFresh3DFrame(currentDesignB.panel.getFigure3d(), -1, -1, FRAME_TIMEOUT_MS, "initial design B frame");
			awaitFreshPhotoFrame(currentPhoto.panel, null, -1, -1, FRAME_TIMEOUT_MS, "initial concurrent Photo Studio frame");

			for (int i = 0; i < 4; i++) {
				GLScenePanel designCanvasA = awaitReadyDesignCanvas(currentDesignA.panel.getFigure3d(), FRAME_TIMEOUT_MS,
						"design A canvas iteration " + i);
				GLScenePanel designCanvasB = awaitReadyDesignCanvas(currentDesignB.panel.getFigure3d(), FRAME_TIMEOUT_MS,
						"design B canvas iteration " + i);
				GLScenePanel photoCanvas = awaitReadyPhotoCanvas(currentPhoto.panel, FRAME_TIMEOUT_MS,
						"Photo Studio canvas iteration " + i);

				CameraSnapshot designABefore = snapshotDesignCamera(currentDesignA.panel.getFigure3d());
				CameraSnapshot designBBefore = snapshotDesignCamera(currentDesignB.panel.getFigure3d());
				CameraSnapshot photoBefore = snapshotCamera(photoCanvas);

				int beforeSwapA = currentDesignA.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePaintA = currentDesignA.panel.getFigure3d().getCanvasPaintCallCount();
				int beforeSwapB = currentDesignB.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePaintB = currentDesignB.panel.getFigure3d().getCanvasPaintCallCount();
				int beforePhotoSwap = photoCanvas.getSwapCallCount();
				int beforePhotoPaint = photoCanvas.getPaintCallCount();
				final int iteration = i;

				phase.set("resize-and-mutate-concurrent-" + iteration);
				onEdt(() -> {
					currentDesignA.frame.setSize(920 + (iteration % 2) * 75, 660 + iteration * 18);
					currentDesignA.frame.setLocation(60 + iteration * 15, 70 + iteration * 9);
					currentDesignB.frame.setSize(910 + (iteration % 3) * 55, 640 + iteration * 22);
					currentDesignB.frame.setLocation(480 + iteration * 13, 80 + iteration * 11);
					currentPhoto.frame.setSize(980 + (iteration % 2) * 60, 720 + iteration * 20);
					currentPhoto.frame.setLocation(180 + iteration * 17, 160 + iteration * 10);
					mutatePhotoSettings(currentPhoto.settings, iteration + 1);
				});

				phase.set("dispatch-input-concurrent-" + iteration);
				dragCanvas(designCanvasA, centerX(designCanvasA), centerY(designCanvasA),
						centerX(designCanvasA) + 40, centerY(designCanvasA) + 30, MouseEvent.BUTTON1, 0);
				scrollCanvas(designCanvasA, centerX(designCanvasA), centerY(designCanvasA), -1);

				dragCanvas(designCanvasB, centerX(designCanvasB), centerY(designCanvasB),
						centerX(designCanvasB) + 35, centerY(designCanvasB) - 28, MouseEvent.BUTTON1, 0);
				scrollCanvas(designCanvasB, centerX(designCanvasB), centerY(designCanvasB), 1);

				dragCanvas(photoCanvas, centerX(photoCanvas), centerY(photoCanvas),
						centerX(photoCanvas) - 32, centerY(photoCanvas) + 24, MouseEvent.BUTTON1, 0);
				scrollCanvas(photoCanvas, centerX(photoCanvas), centerY(photoCanvas), -1);

				phase.set("await-concurrent-frames-" + iteration);
				awaitFresh3DFrame(currentDesignA.panel.getFigure3d(), beforeSwapA, beforePaintA,
						FRAME_TIMEOUT_MS, "design A concurrent frame iteration " + iteration);
				awaitFresh3DFrame(currentDesignB.panel.getFigure3d(), beforeSwapB, beforePaintB,
						FRAME_TIMEOUT_MS, "design B concurrent frame iteration " + iteration);
				awaitFreshPhotoFrame(currentPhoto.panel, photoCanvas, beforePhotoSwap, beforePhotoPaint,
						FRAME_TIMEOUT_MS, "Photo Studio concurrent frame iteration " + iteration);

				phase.set("assert-camera-reactions-concurrent-" + iteration);
					awaitDesignCameraChange(currentDesignA.panel.getFigure3d(), designABefore, FRAME_TIMEOUT_MS,
							"design A camera should react to input iteration " + iteration);
					awaitDesignCameraChange(currentDesignB.panel.getFigure3d(), designBBefore, FRAME_TIMEOUT_MS,
							"design B camera should react to input iteration " + iteration);
					awaitCameraChange(photoCanvas, photoBefore, FRAME_TIMEOUT_MS,
							"Photo Studio camera should react to input iteration " + iteration);
			}
				phase.set("completed");
			} finally {
				disposePhotoHarness(photo);
				disposeDesignHarness(designB);
				disposeDesignHarness(designA);
			}
		}
	}

	@Test
	@Timeout(value = 120, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void designViewMouseDragScrollPanAndClickUpdateLiveSceneState() throws Exception {
		assumeMacUiEnvironment();
		AtomicReference<String> phase = new AtomicReference<>("create-design-input-harness");
		AtomicReference<DesignHarness> harnessRef = new AtomicReference<>();

		DesignHarness harness = null;
		try (DiagnosticWatchdog ignored = startWatchdog(
				"designViewMouseDragScrollPanAndClickUpdateLiveSceneState",
				phase::get,
				() -> describeDesignHarness(harnessRef.get()))) {
			harness = createDesignHarness();
			harnessRef.set(harness);
			final DesignHarness currentHarness = harness;
			try {
				phase.set("await-design-input-showing");
				waitForShowing(currentHarness.panel, 2_000, "Design view should become visible before input test");
				phase.set("enable-design-input-3d");
				onEdt(() -> currentHarness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D));
				phase.set("await-design-input-initial-frame");
				awaitFresh3DFrame(currentHarness.panel.getFigure3d(), -1, -1, FRAME_TIMEOUT_MS, "initial design-view frame");

				phase.set("find-design-canvas");
				GLScenePanel canvas = awaitReadyDesignCanvas(currentHarness.panel.getFigure3d(), FRAME_TIMEOUT_MS,
						"design input test canvas");
				phase.set("drag-render");
				int beforeDragSwap = currentHarness.panel.getFigure3d().getCanvasSwapCallCount();
				int beforeDragPaint = currentHarness.panel.getFigure3d().getCanvasPaintCallCount();
				dragCanvas(canvas, centerX(canvas), centerY(canvas),
						centerX(canvas) + 45, centerY(canvas) + 34, MouseEvent.BUTTON1, 0);
				awaitFresh3DFrame(currentHarness.panel.getFigure3d(), beforeDragSwap, beforeDragPaint,
						FRAME_TIMEOUT_MS, "left-drag input should trigger fresh design-view rendering");

			phase.set("scroll-zoom");
			CameraSnapshot zoomBefore = snapshotCamera(canvas);
			scrollCanvas(canvas, centerX(canvas), centerY(canvas), -1);
				awaitCameraDistanceChange(canvas, zoomBefore.distance(), FRAME_TIMEOUT_MS,
					"scroll should change the design-view zoom distance");

				phase.set("enable-pan-mode");
				onEdt(() -> currentHarness.panel.getFigure3d().setPanModeEnabled(true));
				phase.set("pan-drag");
				int beforePanSwap = currentHarness.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePanPaint = currentHarness.panel.getFigure3d().getCanvasPaintCallCount();
				dragCanvas(canvas, centerX(canvas), centerY(canvas),
						centerX(canvas) + 38, centerY(canvas) - 22, MouseEvent.BUTTON1, 0);
				awaitFresh3DFrame(currentHarness.panel.getFigure3d(), beforePanSwap, beforePanPaint,
						FRAME_TIMEOUT_MS, "pan drag should trigger fresh design-view rendering");

				phase.set("register-selection-listener");
				AtomicReference<RocketComponent[]> selectedComponents = new AtomicReference<>();
				onEdt(() -> currentHarness.panel.getFigure3d().addComponentSelectionListener((components, event) -> {
					if (components != null && components.length > 0) {
						selectedComponents.set(components);
					}
				}));
				phase.set("click-until-selection");
				clickUntilSelection(canvas, currentHarness.panel.getFigure3d(), selectedComponents, FRAME_TIMEOUT_MS);
				phase.set("completed");
			} finally {
				disposeDesignHarness(harness);
			}
		}
	}

	private static void mutatePhotoSettings(PhotoSettings settings, int iteration) {
		settings.setAdjusting(true);
		settings.setBackgroundType(PhotoSettings.BackgroundType.values()[iteration % PhotoSettings.BackgroundType.values().length]);
		settings.setSky(TEST_SKIES[iteration % TEST_SKIES.length]);
		settings.setView(
				-0.35 + iteration * 0.18,
				0.55 + iteration * 0.47,
				0.38 + iteration * 0.08,
				0.85 + iteration * 0.09);
		settings.setPitch(1.65 + iteration * 0.14);
		settings.setYaw(iteration * 0.52);
		settings.setRoll(0.45 + iteration * 0.39);
		settings.setAdvance(-0.04 + iteration * 0.02);
		settings.setLightAlt(-0.25 + iteration * 0.15);
		settings.setLightAz(-1.1 + iteration * 0.45);
		settings.setLightStrength(0.55 + iteration * 0.18);
		settings.setAmbiance(0.12 + iteration * 0.08);
		settings.setMotionBlurred((iteration % 2) == 0);
		settings.setMotionBlurAmount(2.0 + iteration * 1.7);
		settings.setFlame((iteration % 2) == 0);
		settings.setSmoke(true);
		settings.setSparks((iteration % 3) != 0);
		settings.setExhaustScale(0.8 + iteration * 0.45);
		settings.setFlameAspectRatio(0.85 + iteration * 0.28);
		settings.setSparkConcentration(Math.min(0.95, 0.20 + iteration * 0.12));
		settings.setSparkWeight(Math.min(0.95, 0.10 + iteration * 0.16));
		settings.setAdjusting(false);
	}

	private static void assumeMacUiEnvironment() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"3D stress test requires a live graphical environment");
		Assumptions.assumeTrue(SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS,
				"3D stress test targets the macOS AWTGLCanvas path");
	}

	private static PhotoHarness createPhotoHarness(String frameTitle) throws Exception {
		AtomicReference<PhotoHarness> harnessRef = new AtomicReference<>();
		onEdt("create Photo Studio harness", () -> {
			OpenRocketDocument document = loadMotorizedDocumentUnchecked();
			PhotoSettings settings = new PhotoSettings();
			PhotoPanel panel = new PhotoPanel(document, settings);
			panel.setPreferredSize(new Dimension(960, 700));

			JFrame frame = new JFrame(frameTitle);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(panel);
			frame.pack();
			frame.setLocationByPlatform(true);
			frame.setVisible(true);

			panel.setDoc(document);
			harnessRef.set(new PhotoHarness(frame, panel, settings));
		});
		return harnessRef.get();
	}

	private static DesignHarness createDesignHarness() throws Exception {
		AtomicReference<DesignHarness> harnessRef = new AtomicReference<>();
		onEdt("create design-view harness", () -> {
			OpenRocketDocument document = loadMotorizedDocumentUnchecked();
			RocketPanel panel = new RocketPanel(document);
			panel.setPreferredSize(new Dimension(900, 620));

			JFrame frame = new JFrame("PhotoStudio3DStressTest-design");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(panel);
			frame.pack();
			frame.setLocation(60, 60);
			frame.setVisible(true);

			harnessRef.set(new DesignHarness(frame, panel));
		});
		return harnessRef.get();
	}

	private static void disposePhotoHarness(PhotoHarness harness) throws Exception {
		if (harness == null) {
			return;
		}
		onEdt("dispose Photo Studio harness", () -> {
			harness.panel.clearDoc();
			harness.frame.dispose();
		});
	}

	private static void disposeDesignHarness(DesignHarness harness) throws Exception {
		if (harness == null) {
			return;
		}
		onEdt("dispose design-view harness", () -> {
			harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView);
			harness.frame.dispose();
		});
	}

	private static void waitForShowing(java.awt.Component component, long timeoutMs, String message) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (onEdt("check showing state for " + component.getClass().getSimpleName(), component::isShowing)) {
				return;
			}
			Thread.sleep(25);
		}
		assertTrue(onEdt("re-check showing state for " + component.getClass().getSimpleName(), component::isShowing), message);
	}

	private static void awaitFreshPhotoFrame(PhotoPanel panel, GLScenePanel previousCanvas, int previousSwapCount,
			int previousPaintCount, long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			GLScenePanel currentCanvas = currentPhotoCanvas(panel);
			if (currentCanvas != null) {
				int swapCount = currentCanvas.getSwapCallCount();
				int paintCount = currentCanvas.getPaintCallCount();
				boolean freshFrame = currentCanvas != previousCanvas
						? currentCanvas.hasCompletedFrame()
						: swapCount > previousSwapCount || paintCount > previousPaintCount;
				if (freshFrame && !currentCanvas.isPeerMispositionedForDebug()) {
					return;
				}
			}
			Thread.sleep(40);
		}

		GLScenePanel finalCanvas = currentPhotoCanvas(panel);
		assertNotNull(finalCanvas, context + " lost its GL canvas");
		int finalSwapCount = finalCanvas.getSwapCallCount();
		int finalPaintCount = finalCanvas.getPaintCallCount();
		boolean freshFrame = finalCanvas != previousCanvas
				? finalCanvas.hasCompletedFrame()
				: finalSwapCount > previousSwapCount || finalPaintCount > previousPaintCount;
		assertTrue(freshFrame && !finalCanvas.isPeerMispositionedForDebug(),
				context + " did not produce a fresh visible Photo Studio frame. state="
						+ finalCanvas.getDebugStateSummary()
						+ ", previousSwap=" + previousSwapCount
						+ ", previousPaint=" + previousPaintCount
						+ ", finalSwap=" + finalSwapCount
						+ ", finalPaint=" + finalPaintCount);
	}

	private static void awaitFresh3DFrame(RocketFigure3d figure3d, int previousSwapCount, int previousPaintCount,
			long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			int swapCount = figure3d.getCanvasSwapCallCount();
			int paintCount = figure3d.getCanvasPaintCallCount();
			if ((swapCount > previousSwapCount || paintCount > previousPaintCount)
					&& !figure3d.isCanvasPeerMispositioned()) {
				return;
			}
			Thread.sleep(40);
		}

		int finalSwapCount = figure3d.getCanvasSwapCallCount();
		int finalPaintCount = figure3d.getCanvasPaintCallCount();
		assertTrue((finalSwapCount > previousSwapCount || finalPaintCount > previousPaintCount)
						&& !figure3d.isCanvasPeerMispositioned(),
				context + " did not produce a fresh visible 3D frame. state=" + figure3d.getCanvasDebugState()
						+ ", previousSwap=" + previousSwapCount
						+ ", previousPaint=" + previousPaintCount
						+ ", finalSwap=" + finalSwapCount
						+ ", finalPaint=" + finalPaintCount);
	}

	private static GLScenePanel awaitCurrentPhotoCanvas(PhotoPanel panel, long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			GLScenePanel canvas = currentPhotoCanvas(panel);
			if (canvas != null) {
				return canvas;
			}
			Thread.sleep(25);
		}
		GLScenePanel canvas = currentPhotoCanvas(panel);
		assertNotNull(canvas, context + " was not created");
		return canvas;
	}

	private static GLScenePanel awaitReadyPhotoCanvas(PhotoPanel panel, long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			GLScenePanel canvas = currentPhotoCanvas(panel);
			if (isCanvasReady(canvas)) {
				return canvas;
			}
			Thread.sleep(25);
		}
		GLScenePanel canvas = currentPhotoCanvas(panel);
		assertTrue(isCanvasReady(canvas), context + " was created but not initialized");
		return canvas;
	}

	private static GLScenePanel currentPhotoCanvas(PhotoPanel panel) throws Exception {
		return onEdt("locate Photo Studio GL canvas", () -> {
			for (Component component : panel.getComponents()) {
				if (component instanceof GLScenePanel canvas) {
					return canvas;
				}
			}
			return null;
		});
	}

	private static GLScenePanel awaitCurrentDesignCanvas(RocketFigure3d figure3d, long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			GLScenePanel canvas = currentDesignCanvas(figure3d);
			if (canvas != null) {
				return canvas;
			}
			Thread.sleep(25);
		}
		GLScenePanel canvas = currentDesignCanvas(figure3d);
		assertNotNull(canvas, context + " was not created");
		return canvas;
	}

	private static GLScenePanel awaitReadyDesignCanvas(RocketFigure3d figure3d, long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			GLScenePanel canvas = currentDesignCanvas(figure3d);
			if (isCanvasReady(canvas)) {
				return canvas;
			}
			Thread.sleep(25);
		}
		GLScenePanel canvas = currentDesignCanvas(figure3d);
		assertTrue(isCanvasReady(canvas), context + " was created but not initialized");
		return canvas;
	}

	private static GLScenePanel currentDesignCanvas(RocketFigure3d figure3d) throws Exception {
		return onEdt("locate design-view GL canvas", () -> findCanvasInHierarchy(figure3d));
	}

	private static GLScenePanel findCanvasInHierarchy(Component component) {
		if (component instanceof GLScenePanel canvas) {
			return canvas;
		}
		if (component instanceof JComponent container) {
			for (Component child : container.getComponents()) {
				GLScenePanel canvas = findCanvasInHierarchy(child);
				if (canvas != null) {
					return canvas;
				}
			}
		}
		return null;
	}

	private static boolean isCanvasReady(GLScenePanel canvas) {
		return canvas != null
				&& !canvas.glInitFailed
				&& canvas.awaitInitialized(0)
				&& canvas.getScene3DOrchestrator() != null;
	}

	private static CameraSnapshot snapshotCamera(GLScenePanel canvas) throws Exception {
		long deadline = System.currentTimeMillis() + CAMERA_SNAPSHOT_TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			CameraSnapshot snapshot = onEdt("snapshot GL camera state", () -> {
				Scene3DOrchestrator orchestrator = canvas.getScene3DOrchestrator();
				if (orchestrator == null) {
					return null;
				}
				Camera camera = orchestrator.getCameraController().getCamera();
				if (camera == null) {
					return null;
				}
				Vector3f center = camera.getCenterOfInterest();
				return new CameraSnapshot(
						camera.getAngleX(),
						camera.getAngleY(),
						camera.getDistance(),
						center.x,
						center.y,
						center.z);
			});
			if (snapshot != null) {
				return snapshot;
			}
			if (canvas.glInitFailed) {
				break;
			}
			Thread.sleep(25);
		}

		Scene3DOrchestrator orchestrator = onEdt("inspect GL camera snapshot failure", canvas::getScene3DOrchestrator);
		assertNotNull(orchestrator,
				"3D orchestrator should exist for camera snapshot. state=" + canvas.getDebugStateSummary());
		Camera camera = onEdt("inspect camera snapshot failure", () -> orchestrator.getCameraController().getCamera());
		assertNotNull(camera, "Camera should exist for camera snapshot. state=" + canvas.getDebugStateSummary());
		return onEdt("read camera state after snapshot failure", () -> {
			Vector3f center = camera.getCenterOfInterest();
			return new CameraSnapshot(
					camera.getAngleX(),
					camera.getAngleY(),
					camera.getDistance(),
					center.x,
					center.y,
					center.z);
		});
	}

	private static CameraSnapshot snapshotDesignCamera(RocketFigure3d figure3d) throws Exception {
		GLScenePanel canvas = awaitReadyDesignCanvas(figure3d, CAMERA_SNAPSHOT_TIMEOUT_MS,
				"design camera snapshot canvas");
		return snapshotCamera(canvas);
	}

	private static void awaitCameraChange(GLScenePanel canvas, CameraSnapshot previous, long timeoutMs, String context)
			throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			CameraSnapshot current = snapshotCamera(canvas);
			if (!current.approximatelyEquals(previous)) {
				return;
			}
			Thread.sleep(40);
		}
		CameraSnapshot current = snapshotCamera(canvas);
			assertTrue(!current.approximatelyEquals(previous),
					context + ". previous=" + previous + ", current=" + current);
	}

	private static void awaitDesignCameraChange(RocketFigure3d figure3d, CameraSnapshot previous, long timeoutMs,
			String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			CameraSnapshot current = snapshotDesignCamera(figure3d);
			if (!current.approximatelyEquals(previous)) {
				return;
			}
			Thread.sleep(40);
		}
		CameraSnapshot current = snapshotDesignCamera(figure3d);
		assertTrue(!current.approximatelyEquals(previous),
				context + ". previous=" + previous + ", current=" + current);
	}

	private static void awaitCameraDistanceChange(GLScenePanel canvas, float previousDistance, long timeoutMs, String context)
			throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			CameraSnapshot current = snapshotCamera(canvas);
			if (Math.abs(current.distance() - previousDistance) > 0.001f) {
				return;
			}
			Thread.sleep(40);
		}
		CameraSnapshot current = snapshotCamera(canvas);
		assertTrue(Math.abs(current.distance() - previousDistance) > 0.001f,
				context + ". previousDistance=" + previousDistance + ", current=" + current);
	}

	private static void awaitCameraCenterChange(GLScenePanel canvas, CameraSnapshot previous, long timeoutMs, String context)
			throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			CameraSnapshot current = snapshotCamera(canvas);
			if (current.centerChangedFrom(previous)) {
				return;
			}
			Thread.sleep(40);
		}
		CameraSnapshot current = snapshotCamera(canvas);
		assertTrue(current.centerChangedFrom(previous),
				context + ". previous=" + previous + ", current=" + current);
	}

	private static void clickUntilSelection(GLScenePanel canvas, RocketFigure3d figure3d,
			AtomicReference<RocketComponent[]> selectedComponents, long timeoutMs) throws Exception {
		int[][] offsets = {
				{0, 0},
				{-24, 0},
				{24, 0},
				{0, -18},
				{0, 18},
				{-18, -18},
				{18, 18}
		};
		long deadline = System.currentTimeMillis() + timeoutMs;
		int centerX = centerX(canvas);
		int centerY = centerY(canvas);
		while (System.currentTimeMillis() < deadline) {
			for (int[] offset : offsets) {
				clickCanvas(canvas, centerX + offset[0], centerY + offset[1], MouseEvent.BUTTON1, 0);
				if (hasSelection(figure3d) || hasSelectedComponents(selectedComponents)) {
					return;
				}
				Thread.sleep(80);
			}
		}
		assertTrue(hasSelection(figure3d) || hasSelectedComponents(selectedComponents),
				"Click input should select at least one rocket component in the 3D design view");
	}

	private static boolean hasSelection(RocketFigure3d figure3d) throws Exception {
		return onEdt("inspect 3D selection state", () -> {
			Scene3DOrchestrator orchestrator = figure3d.getSceneController();
			if (orchestrator == null) {
				return false;
			}
			SceneView scene = orchestrator.getScene();
			return scene != null && !scene.getSelectedObjects().isEmpty();
		});
	}

	private static boolean hasSelectedComponents(AtomicReference<RocketComponent[]> selectedComponents) {
		RocketComponent[] components = selectedComponents.get();
		return components != null && components.length > 0;
	}

	private static void clickCanvas(GLScenePanel canvas, int x, int y, int button, int modifiersEx) throws Exception {
		long when = System.currentTimeMillis();
		dispatchMouseEvent(canvas, MouseEvent.MOUSE_PRESSED, x, y, button, modifiersEx, when);
		dispatchMouseEvent(canvas, MouseEvent.MOUSE_RELEASED, x, y, button, modifiersEx, when + 10);
		dispatchMouseEvent(canvas, MouseEvent.MOUSE_CLICKED, x, y, button, modifiersEx, when + 20);
	}

	private static void dragCanvas(GLScenePanel canvas, int startX, int startY, int endX, int endY,
			int button, int modifiersEx) throws Exception {
		long when = System.currentTimeMillis();
		dispatchMouseEvent(canvas, MouseEvent.MOUSE_PRESSED, startX, startY, button, modifiersEx, when);
		int dragModifiers = modifiersEx | buttonMask(button);
		int steps = 4;
		for (int step = 1; step <= steps; step++) {
			int x = startX + (endX - startX) * step / steps;
			int y = startY + (endY - startY) * step / steps;
			dispatchMouseEvent(canvas, MouseEvent.MOUSE_DRAGGED, x, y, button, dragModifiers, when + 10L * step);
		}
		dispatchMouseEvent(canvas, MouseEvent.MOUSE_RELEASED, endX, endY, button, modifiersEx, when + 70);
	}

	private static void scrollCanvas(GLScenePanel canvas, int x, int y, int wheelRotation) throws Exception {
		onEdt("dispatch mouse wheel event", () -> {
			long when = System.currentTimeMillis();
			MouseWheelEvent event = new MouseWheelEvent(
					canvas,
					MouseEvent.MOUSE_WHEEL,
					when,
					0,
					x,
					y,
					x,
					y,
					0,
					false,
					MouseWheelEvent.WHEEL_UNIT_SCROLL,
					1,
					wheelRotation);
			canvas.dispatchEvent(event);
		});
	}

	private static void dispatchMouseEvent(GLScenePanel canvas, int id, int x, int y, int button, int modifiersEx,
			long when) throws Exception {
		onEdt("dispatch mouse event " + id, () -> {
			MouseEvent event = new MouseEvent(
					canvas,
					id,
					when,
					modifiersEx,
					x,
					y,
					x,
					y,
					1,
					false,
					button);
			canvas.dispatchEvent(event);
		});
	}

	private static int centerX(Component component) {
		return Math.max(1, component.getWidth() / 2);
	}

	private static int centerY(Component component) {
		return Math.max(1, component.getHeight() / 2);
	}

	private static int buttonMask(int button) {
		return switch (button) {
			case MouseEvent.BUTTON1 -> InputEvent.BUTTON1_DOWN_MASK;
			case MouseEvent.BUTTON2 -> InputEvent.BUTTON2_DOWN_MASK;
			case MouseEvent.BUTTON3 -> InputEvent.BUTTON3_DOWN_MASK;
			default -> 0;
		};
	}

	private static BufferedImage capturePhotoImage(PhotoPanel panel) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<BufferedImage> result = new AtomicReference<>();
		onEdt("register Photo Studio image callback", () -> panel.addImageCallback(image -> {
			result.set(image);
			latch.countDown();
		}));
		assertTrue(latch.await(CAPTURE_TIMEOUT_MS, TimeUnit.MILLISECONDS),
				"Timed out capturing Photo Studio image");
		return result.get();
	}

	private static OpenRocketDocument loadMotorizedDocumentUnchecked() {
		try {
			return loadMotorizedDocument();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to load simplerocket.ork test fixture", e);
		}
	}

	private static OpenRocketDocument loadMotorizedDocument() throws Exception {
		try (InputStream stream = Objects.requireNonNull(
				PhotoStudio3DStressTest.class.getResourceAsStream("/simplerocket.ork"),
				"simplerocket.ork test resource is missing")) {
			return new GeneralRocketLoader(new File("simplerocket.ork")).load(stream, "simplerocket.ork");
		}
	}

	private static void onEdt(Runnable runnable) throws Exception {
		onEdt(describeEdtCaller(), () -> {
			runnable.run();
			return null;
		});
	}

	private static <T> T onEdt(Supplier<T> supplier) throws Exception {
		return onEdt(describeEdtCaller(), supplier);
	}

	private static void onEdt(String context, Runnable runnable) throws Exception {
		onEdt(context, () -> {
			runnable.run();
			return null;
		});
	}

	private static <T> T onEdt(String context, Supplier<T> supplier) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			return supplier.get();
		}

		FutureTask<T> task = new FutureTask<>(supplier::get);
		SwingUtilities.invokeLater(task);
		try {
			return task.get(EDT_CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			String header = "EDT call timed out after " + EDT_CALL_TIMEOUT_MS + "ms while " + context;
			printThreadDump(header);
			throw new AssertionError(header, e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception exception) {
				throw exception;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new RuntimeException(cause);
		}
	}

	private static String describeEdtCaller() {
		String helperClassName = PhotoStudio3DStressTest.class.getName();
		for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (helperClassName.equals(element.getClassName()) && !"onEdt".equals(element.getMethodName())) {
				return element.getMethodName() + ":" + element.getLineNumber();
			}
		}
		return "unknown call site";
	}

	private record PhotoHarness(JFrame frame, PhotoPanel panel, PhotoSettings settings) {
	}

	private record DesignHarness(JFrame frame, RocketPanel panel) {
	}

	@SafeVarargs
	private static DiagnosticWatchdog startWatchdog(String testName, Supplier<String> phaseSupplier,
			Supplier<String>... stateSuppliers) {
		return new DiagnosticWatchdog(testName, phaseSupplier, stateSuppliers);
	}

	private static String describePhotoHarness(PhotoHarness harness) {
		if (harness == null) {
			return "photoHarness=null";
		}
		String frameState = describeComponent(harness.frame);
		String panelState = describeComponent(harness.panel);
		GLScenePanel canvas = findCanvasInHierarchy(harness.panel);
		String canvasState = canvas != null ? canvas.getDebugStateSummary() : "canvas=null";
		return "photoFrame={" + frameState + "}, photoPanel={" + panelState + "}, photoCanvas={" + canvasState + "}";
	}

	private static String describeDesignHarness(DesignHarness harness) {
		if (harness == null) {
			return "designHarness=null";
		}
		return "designFrame={" + describeComponent(harness.frame)
				+ "}, designPanel={" + describeComponent(harness.panel)
				+ "}, figure3d={" + harness.panel.getFigure3d().getCanvasDebugState() + "}";
	}

	private static String describeComponent(Component component) {
		if (component == null) {
			return "component=null";
		}
		return "showing=" + component.isShowing()
				+ ", visible=" + component.isVisible()
				+ ", displayable=" + component.isDisplayable()
				+ ", size=" + component.getWidth() + "x" + component.getHeight()
				+ ", location=" + component.getX() + "," + component.getY();
	}

	private static void printThreadDump(String header) {
		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(true, true);
		System.err.println("[3D-STRESS-WATCHDOG] " + header);
		for (ThreadInfo threadInfo : threadInfos) {
			if (threadInfo == null) {
				continue;
			}
			System.err.println('"' + threadInfo.getThreadName() + "\" id=" + threadInfo.getThreadId()
					+ " state=" + threadInfo.getThreadState());
			if (threadInfo.getLockName() != null) {
				System.err.println("    waiting on " + threadInfo.getLockName());
			}
			if (threadInfo.getLockOwnerName() != null) {
				System.err.println("    owned by " + threadInfo.getLockOwnerName()
						+ " id=" + threadInfo.getLockOwnerId());
			}
			for (MonitorInfo monitorInfo : threadInfo.getLockedMonitors()) {
				System.err.println("    locked monitor " + monitorInfo);
			}
			for (LockInfo lockInfo : threadInfo.getLockedSynchronizers()) {
				System.err.println("    locked synchronizer " + lockInfo);
			}
			for (StackTraceElement element : threadInfo.getStackTrace()) {
				System.err.println("        at " + element);
			}
		}
	}

	private record CameraSnapshot(float angleX, float angleY, float distance,
			float centerX, float centerY, float centerZ) {
		private boolean approximatelyEquals(CameraSnapshot other) {
			return Math.abs(angleX - other.angleX) <= 0.001f
					&& Math.abs(angleY - other.angleY) <= 0.001f
					&& Math.abs(distance - other.distance) <= 0.001f
					&& Math.abs(centerX - other.centerX) <= 0.001f
					&& Math.abs(centerY - other.centerY) <= 0.001f
					&& Math.abs(centerZ - other.centerZ) <= 0.001f;
		}

		private boolean centerChangedFrom(CameraSnapshot other) {
			return Math.abs(centerX - other.centerX) > 0.001f
					|| Math.abs(centerY - other.centerY) > 0.001f
					|| Math.abs(centerZ - other.centerZ) > 0.001f;
		}
	}

	private static final class DiagnosticWatchdog implements AutoCloseable {
		private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "photo-stress-watchdog");
			thread.setDaemon(true);
			return thread;
		});

		@SafeVarargs
		private DiagnosticWatchdog(String testName, Supplier<String> phaseSupplier, Supplier<String>... stateSuppliers) {
			executor.scheduleAtFixedRate(() -> {
				StringBuilder builder = new StringBuilder();
				builder.append("test=").append(testName)
						.append(", phase=").append(safeGet(phaseSupplier));
				for (Supplier<String> supplier : stateSuppliers) {
					builder.append(System.lineSeparator()).append("    ").append(safeGet(supplier));
				}
				printThreadDump(builder.toString());
			}, WATCHDOG_PERIOD_SECONDS, WATCHDOG_PERIOD_SECONDS, TimeUnit.SECONDS);
		}

		private static String safeGet(Supplier<String> supplier) {
			try {
				return supplier.get();
			} catch (Throwable t) {
				return "state-supplier-failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
			}
		}

		@Override
		public void close() {
			executor.shutdownNow();
		}
	}

	private static final class TestMotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {
		private final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();

		private TestMotorDbProvider() {
			db.addMotor(readMotor());
		}

		@Override
		public ThrustCurveMotorSetDatabase get() {
			return db;
		}
	}

	private static ThrustCurveMotor readMotor() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		try (InputStream stream = Objects.requireNonNull(
				PhotoStudio3DStressTest.class.getResourceAsStream("/Estes_A8.rse"),
				"Estes_A8.rse test resource is missing")) {
			for (ThrustCurveMotor.Builder builder : loader.load(stream, "Estes_A8.rse")) {
				return builder.build();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load Estes_A8.rse motor fixture", e);
		}
		throw new IllegalStateException("Estes_A8.rse did not contain a motor");
	}
}
