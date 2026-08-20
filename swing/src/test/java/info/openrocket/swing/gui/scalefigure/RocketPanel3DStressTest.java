package info.openrocket.swing.gui.scalefigure;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory.ComponentAppearanceRole;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketPanel3DStressTest extends BaseTestCase {

	private static final long STARTUP_TIMEOUT_MS = 12_000;
	private static final long SWITCH_TIMEOUT_MS = 4_000;
	private static final long CHURN_TIMEOUT_MS = 2_500;

	private record RenderedSurfaceColors(Vector3f primary, Vector3f secondary) {
	}

	@Test
	@Timeout(value = 90, unit = TimeUnit.SECONDS)
	void repeatedThreeDSwitchesProduceFreshFramesAfterWindowResizeAndMove() throws Exception {
		assumeMacUiEnvironment();

		FrameHarness harness = createStandaloneHarness();
		try {
			waitForShowing(harness.panel, 2_000, "RocketPanel should become visible before stress test");

			for (int i = 0; i < 6; i++) {
				final int iteration = i;
				int beforeSwap = harness.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePaint = harness.panel.getFigure3d().getCanvasPaintCallCount();

				onEdt(() -> {
					harness.frame.setSize(920 + (iteration % 3) * 80, 620 + (iteration % 2) * 60);
					harness.frame.setLocation(60 + iteration * 11, 90 + iteration * 7);
					harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
					harness.frame.validate();
					harness.frame.repaint();
				});

				awaitFresh3DFrame(harness.panel.getFigure3d(), beforeSwap, beforePaint,
						SWITCH_TIMEOUT_MS, "switch iteration " + iteration);

				beforeSwap = harness.panel.getFigure3d().getCanvasSwapCallCount();
				beforePaint = harness.panel.getFigure3d().getCanvasPaintCallCount();

				onEdt(() -> {
					harness.frame.setSize(960 + (iteration % 2) * 40, 660 + (iteration % 3) * 35);
					harness.frame.setLocation(100 + iteration * 13, 120 + iteration * 9);
					harness.frame.validate();
					harness.frame.repaint();
				});

				awaitFresh3DFrame(harness.panel.getFigure3d(), beforeSwap, beforePaint,
						CHURN_TIMEOUT_MS, "window churn iteration " + iteration);

				onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView));
				waitForEdtDrain();
			}
		} finally {
			disposeHarness(harness);
		}
	}

	@Test
	@Timeout(value = 90, unit = TimeUnit.SECONDS)
	void splitPaneDividerMovesProduceFreshFrames() throws Exception {
		assumeMacUiEnvironment();

		FrameHarness harness = createSplitPaneHarness();
		try {
			waitForShowing(harness.panel, 2_000, "Split-pane RocketPanel should become visible before stress test");

			for (int i = 0; i < 6; i++) {
				final int iteration = i;
				int beforeSwap = harness.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePaint = harness.panel.getFigure3d().getCanvasPaintCallCount();

				onEdt(() -> {
					harness.splitPane.setDividerLocation(220 + iteration * 35);
					harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
					harness.frame.validate();
					harness.frame.repaint();
				});

				awaitFresh3DFrame(harness.panel.getFigure3d(), beforeSwap, beforePaint,
						SWITCH_TIMEOUT_MS, "split-pane switch iteration " + iteration);

				beforeSwap = harness.panel.getFigure3d().getCanvasSwapCallCount();
				beforePaint = harness.panel.getFigure3d().getCanvasPaintCallCount();

				onEdt(() -> {
					harness.splitPane.setDividerLocation(520 - iteration * 30);
					harness.splitPane.revalidate();
					harness.frame.validate();
					harness.frame.repaint();
				});

				awaitFresh3DFrame(harness.panel.getFigure3d(), beforeSwap, beforePaint,
						CHURN_TIMEOUT_MS, "divider move iteration " + iteration);

				onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView));
				waitForEdtDrain();
			}
		} finally {
			disposeHarness(harness);
		}
	}

	@Test
	@Timeout(value = 90, unit = TimeUnit.SECONDS)
	void innerTubeRadialOffsetSurvivesSwitchBackToThreeD() throws Exception {
		assumeMacUiEnvironment();

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		InnerTube innerTube = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		FrameHarness harness = createStandaloneHarness(document);
		try {
			waitForShowing(harness.panel, 2_000, "RocketPanel should become visible before radial-offset test");
			onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D));
			awaitFresh3DFrame(harness.panel.getFigure3d(), 0, 0, STARTUP_TIMEOUT_MS,
					"radial-offset regression startup");

			for (int i = 0; i < 6; i++) {
				double expectedY = 0.002 + i * 0.0007;
				double expectedZ = -0.003 - i * 0.0005;
				onEdt(() -> {
					// Queue more than one scene rebuild, then hide the canvas before the
					// deferred snapshot construction runs. The scene shown on return must
					// contain the last offset, not the first queued state.
					innerTube.setRadialShift(-expectedY, -expectedZ);
					innerTube.setRadialShift(expectedY, expectedZ);
					harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView);
				});
				waitForEdtDrain();

				int beforeSwap = harness.panel.getFigure3d().getCanvasSwapCallCount();
				int beforePaint = harness.panel.getFigure3d().getCanvasPaintCallCount();
				onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D));
				awaitFresh3DFrame(harness.panel.getFigure3d(), beforeSwap, beforePaint,
						SWITCH_TIMEOUT_MS, "radial-offset switch iteration " + i);

				Matrix4f modelMatrix = findComponentModelMatrix(harness.panel.getFigure3d(), innerTube);
				assertEquals(expectedY * RenderingConstants.WORLD_SCALE, modelMatrix.m31(), 1.0e-5,
						"3D scene should use the latest inner-tube Y offset");
				assertEquals(-expectedZ * RenderingConstants.WORLD_SCALE, modelMatrix.m32(), 1.0e-5,
						"3D scene should use the latest inner-tube Z offset");
			}

			assertTrue(onEdt(harness.panel.getFigure3d()::isShowing),
					"3D card should be visible after switching back from 2D");
			BufferedImage image = harness.panel.getFigure3d().captureImage();
			assertNotNull(image, "Visible 3D view should still support frame capture after offset rebuilds");
			assertTrue(image.getWidth() > 0 && image.getHeight() > 0,
					"Captured 3D frame should have usable dimensions");
		} finally {
			disposeHarness(harness);
		}
	}

	@Test
	@Timeout(value = 45, unit = TimeUnit.SECONDS)
	void separateTubeAndFinAppearancesReachTheRenderedScene() throws Exception {
		assumeMacUiEnvironment();

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube tube = rocket.getAllChildren().stream()
				.filter(BodyTube.class::isInstance)
				.map(BodyTube.class::cast)
				.findFirst()
				.orElseThrow();
		FinSet finSet = rocket.getAllChildren().stream()
				.filter(FinSet.class::isInstance)
				.map(FinSet.class::cast)
				.findFirst()
				.orElseThrow();
		onEdt(() -> {
			tube.setAppearance(new Appearance(new ORColor(255, 0, 0), 0.3));
			tube.getInsideColorComponentHandler().setInsideAppearance(
					new Appearance(new ORColor(0, 0, 255), 0.3));
			tube.getInsideColorComponentHandler().setSeparateInsideOutside(true);
			finSet.setAppearance(new Appearance(new ORColor(0, 255, 0), 0.3));
			finSet.getInsideColorComponentHandler().setInsideAppearance(
					new Appearance(new ORColor(255, 0, 255), 0.3));
			finSet.getInsideColorComponentHandler().setSeparateInsideOutside(true);
		});

		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		FrameHarness harness = createStandaloneHarness(document);
		try {
			waitForShowing(harness.panel, 2_000, "RocketPanel should become visible before appearance test");
			onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D));
			awaitFresh3DFrame(harness.panel.getFigure3d(), 0, 0, STARTUP_TIMEOUT_MS,
					"separate surface appearance startup");

			RenderedSurfaceColors tubeColors = findRenderedSurfaceColors(harness.panel.getFigure3d(), tube);
			assertEquals(new Vector3f(1, 0, 0), tubeColors.primary(),
					"Tube outside should retain its primary appearance");
			assertEquals(new Vector3f(0, 0, 1), tubeColors.secondary(),
					"Tube inside should use its independent appearance");

			RenderedSurfaceColors finColors = findRenderedSurfaceColors(harness.panel.getFigure3d(), finSet);
			assertEquals(new Vector3f(0, 1, 0), finColors.primary(),
					"Fin left side should retain its primary appearance");
			assertEquals(new Vector3f(1, 0, 1), finColors.secondary(),
					"Fin right side should use its independent appearance");

			assertNotNull(harness.panel.getFigure3d().captureImage(),
					"Separate surface materials should still produce a capturable frame");
		} finally {
			disposeHarness(harness);
		}
	}

	@Test
	@Timeout(value = 45, unit = TimeUnit.SECONDS)
	void themeChangeDoesNotOverlayVisibleThreeDCanvas() throws Exception {
		assumeMacUiEnvironment();

		FrameHarness harness = createStandaloneHarness();
		UITheme.Theme originalTheme = GUIUtil.getUITheme();
		try {
			waitForShowing(harness.panel, 2_000,
					"RocketPanel should become visible before theme-change regression test");
			onEdt(() -> {
				harness.panel.setViewType(RocketPanel.VIEW_TYPE.Figure3D);
			});
			awaitFresh3DFrame(harness.panel.getFigure3d(), 0, 0, STARTUP_TIMEOUT_MS,
					"theme-change overlay regression startup");
			assertTrue(onEdt(harness.panel.getFigure3d()::isShowing),
					"3D view should be showing before changing theme");

			UITheme.Theme alternateTheme = UITheme.isLightTheme(originalTheme)
					? UITheme.Themes.DARK
					: UITheme.Themes.LIGHT;
			int animationLayer = JLayeredPane.DRAG_LAYER.intValue() + 1;
			int overlayCount = onEdt(() -> {
				alternateTheme.applyTheme();
				return harness.frame.getLayeredPane().getComponentsInLayer(animationLayer).length;
			});
			assertEquals(0, overlayCount,
					"Theme change must not put FlatLaf's lightweight animation layer over the native 3D canvas");
		} finally {
			try {
				onEdt(originalTheme::applyTheme);
			} finally {
				disposeHarness(harness);
			}
		}
	}

	private static void assumeMacUiEnvironment() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"3D stress test requires a live graphical environment");
		Assumptions.assumeTrue(SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS,
				"3D stress test targets the macOS AWTGLCanvas path");
	}

	private static FrameHarness createStandaloneHarness() throws Exception {
		return createStandaloneHarness(OpenRocketDocumentFactory.createNewRocket());
	}

	private static FrameHarness createStandaloneHarness(OpenRocketDocument document) throws Exception {
		AtomicReference<FrameHarness> harnessRef = new AtomicReference<>();
		onEdt(() -> {
			RocketPanel panel = new RocketPanel(document);
			panel.setPreferredSize(new Dimension(900, 600));

			JFrame frame = new JFrame("RocketPanel3DStressTest-standalone");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(panel);
			frame.pack();
			frame.setLocation(60, 90);
			frame.setVisible(true);

			harnessRef.set(new FrameHarness(frame, panel, null));
		});
		return harnessRef.get();
	}

	private static Matrix4f findComponentModelMatrix(RocketFigure3d figure3d, InnerTube component)
			throws Exception {
		long deadline = System.currentTimeMillis() + SWITCH_TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			Scene3DOrchestrator orchestrator = onEdt(figure3d::getSceneController);
			if (orchestrator == null) {
				Thread.sleep(40);
				continue;
			}

			CountDownLatch queryFinished = new CountDownLatch(1);
			AtomicReference<Matrix4f> result = new AtomicReference<>();
			orchestrator.enqueueGlTask(() -> {
				try {
					if (orchestrator.getScene() == null) {
						return;
					}
					for (SceneObject object : orchestrator.getScene().getObjects()) {
						if (object.getAppearanceSourceComponent() == component) {
							result.set(new Matrix4f(object.getModelMatrix()));
							return;
						}
					}
				} finally {
					queryFinished.countDown();
				}
			});

			long remaining = Math.max(1, deadline - System.currentTimeMillis());
			if (!queryFinished.await(remaining, TimeUnit.MILLISECONDS)) {
				break;
			}
			Matrix4f matrix = result.get();
			if (matrix != null) {
				return matrix;
			}
			Thread.sleep(40);
		}
		throw new AssertionError("No 3D scene object found for inner tube. state=" + figure3d.getCanvasDebugState());
	}

	private static RenderedSurfaceColors findRenderedSurfaceColors(
			RocketFigure3d figure3d, RocketComponent component) throws Exception {
		long deadline = System.currentTimeMillis() + SWITCH_TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			Scene3DOrchestrator orchestrator = onEdt(figure3d::getSceneController);
			if (orchestrator == null) {
				Thread.sleep(40);
				continue;
			}

			CountDownLatch queryFinished = new CountDownLatch(1);
			AtomicReference<RenderedSurfaceColors> result = new AtomicReference<>();
			orchestrator.enqueueGlTask(() -> {
				try {
					Vector3f primary = null;
					Vector3f secondary = null;
					for (SceneObject object : orchestrator.getScene().getObjects()) {
						if (object.getAppearanceSourceComponent() != component) {
							continue;
						}
						if (object.getAppearanceRole() == ComponentAppearanceRole.SECONDARY) {
							secondary = new Vector3f(object.getAppearance().getColor());
						} else if (object.getAppearanceRole() == ComponentAppearanceRole.PRIMARY) {
							primary = new Vector3f(object.getAppearance().getColor());
						}
					}
					if (primary != null && secondary != null) {
						result.set(new RenderedSurfaceColors(primary, secondary));
					}
				} finally {
					queryFinished.countDown();
				}
			});

			long remaining = Math.max(1, deadline - System.currentTimeMillis());
			if (!queryFinished.await(remaining, TimeUnit.MILLISECONDS)) {
				break;
			}
			if (result.get() != null) {
				return result.get();
			}
			Thread.sleep(40);
		}
		throw new AssertionError("No primary/secondary scene objects found for " + component.getName()
				+ ". state=" + figure3d.getCanvasDebugState());
	}

	private static FrameHarness createSplitPaneHarness() throws Exception {
		AtomicReference<FrameHarness> harnessRef = new AtomicReference<>();
		onEdt(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketPanel panel = new RocketPanel(document);
			panel.setPreferredSize(new Dimension(700, 600));

			JPanel filler = new JPanel();
			filler.add(new JLabel("Native layer resize stress"));
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, filler);
			splitPane.setResizeWeight(0.65);

			JFrame frame = new JFrame("RocketPanel3DStressTest-split");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(splitPane);
			frame.setSize(1_200, 700);
			splitPane.setDividerLocation(420);
			frame.setLocation(80, 120);
			frame.setVisible(true);

			harnessRef.set(new FrameHarness(frame, panel, splitPane));
		});
		return harnessRef.get();
	}

	private static void disposeHarness(FrameHarness harness) throws Exception {
		if (harness == null) {
			return;
		}
		onEdt(() -> {
			// Stop the shared macOS scheduler via the normal view-switch path before disposing the window.
			// Calling RocketFigure3d.cleanup() directly during teardown can race with the static render
			// scheduler thread and produce noisy executor shutdown errors in the stress harness.
			harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView);
			harness.frame.dispose();
		});
	}

	private static void waitForShowing(RocketPanel panel, long timeoutMs, String message) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (onEdt(panel::isShowing)) {
				return;
			}
			Thread.sleep(25);
		}
		assertTrue(onEdt(panel::isShowing), message);
	}

	private static void waitForEdtDrain() throws Exception {
		onEdt(() -> {
			// Drain pending Swing work queued by the render startup path.
		});
	}

	private static void awaitFresh3DFrame(RocketFigure3d figure3d, int previousSwapCount, int previousPaintCount,
			long timeoutMs, String context) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			int swapCount = figure3d.getCanvasSwapCallCount();
			if (swapCount > previousSwapCount) {
				return;
			}
			Thread.sleep(40);
		}

		int finalSwapCount = figure3d.getCanvasSwapCallCount();
		int finalPaintCount = figure3d.getCanvasPaintCallCount();
		assertTrue(finalSwapCount > previousSwapCount,
				context + " did not produce a fresh visible 3D frame. state=" + figure3d.getCanvasDebugState()
						+ ", previousSwap=" + previousSwapCount
						+ ", previousPaint=" + previousPaintCount
						+ ", finalSwap=" + finalSwapCount
						+ ", finalPaint=" + finalPaintCount);
	}

	private static void onEdt(Runnable runnable) throws Exception {
		SwingUtilities.invokeAndWait(runnable);
	}

	private static <T> T onEdt(Supplier<T> supplier) throws Exception {
		AtomicReference<T> ref = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> ref.set(supplier.get()));
		return ref.get();
	}

	private record FrameHarness(JFrame frame, RocketPanel panel, JSplitPane splitPane) {
	}
}
