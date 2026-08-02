package info.openrocket.swing.gui.scalefigure;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketPanel3DStressTest extends BaseTestCase {

	private static final long SWITCH_TIMEOUT_MS = 4_000;
	private static final long CHURN_TIMEOUT_MS = 2_500;

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
	void splitPaneDividerMovesDoNotLeaveThreeDPeerMispositioned() throws Exception {
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

				assertFalse(harness.panel.getFigure3d().isCanvasPeerMispositioned(),
						"3D peer should not remain mispositioned after divider move: "
								+ harness.panel.getFigure3d().getCanvasDebugState());

				onEdt(() -> harness.panel.setViewType(RocketPanel.VIEW_TYPE.SideView));
				waitForEdtDrain();
			}
		} finally {
			disposeHarness(harness);
		}
	}

	private static void assumeMacUiEnvironment() {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"3D stress test requires a live graphical environment");
		Assumptions.assumeTrue(SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS,
				"3D stress test targets the macOS AWTGLCanvas path");
	}

	private static FrameHarness createStandaloneHarness() throws Exception {
		AtomicReference<FrameHarness> harnessRef = new AtomicReference<>();
		onEdt(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
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

	private static FrameHarness createSplitPaneHarness() throws Exception {
		AtomicReference<FrameHarness> harnessRef = new AtomicReference<>();
		onEdt(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			RocketPanel panel = new RocketPanel(document);
			panel.setPreferredSize(new Dimension(700, 600));

			JPanel filler = new JPanel();
			filler.add(new JLabel("Peer sync stress"));
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
			if (swapCount > previousSwapCount && !figure3d.isCanvasPeerMispositioned()) {
				return;
			}
			Thread.sleep(40);
		}

		int finalSwapCount = figure3d.getCanvasSwapCallCount();
		int finalPaintCount = figure3d.getCanvasPaintCallCount();
		assertTrue(finalSwapCount > previousSwapCount && !figure3d.isCanvasPeerMispositioned(),
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
