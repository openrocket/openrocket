package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.input.KeyboardHandler;
import info.openrocket.swing.gui.figure3d.rendering.GLContextDiagnostics;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderer;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.events.SelectionListener;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.Figure3DPreferences;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.ViewportDimensions;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.figure3d.utils.GLDebug;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBRobustness;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.awt.GLData;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DOUBLEBUFFER;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_READ_BUFFER;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING;

/**
 * AWTGLCanvas-backed scene panel. Each canvas owns its context-local GL resources
 * and forwards AWT input to the scene orchestrator.
 */
public class GLScenePanel extends AWTGLCanvas implements HUDUpdateListener {

	private static final Logger log = LoggerFactory.getLogger(GLScenePanel.class);
	private final AtomicInteger renderCallCount = new AtomicInteger(0);
	private final AtomicInteger paintCallCount = new AtomicInteger(0);
	private final AtomicInteger swapCallCount = new AtomicInteger(0);

	private Scene3DOrchestrator scene3DOrchestrator;
	private final KeyboardHandler keyboardHandler;

	private static final double CLICK_DRAG_THRESHOLD_SQ = 5 * 5;

	private final HudOverlay hudOverlay;
	private final CountDownLatch glInitLatch = new CountDownLatch(1);
	private static final int WHEEL_INTERACTION_IDLE_MS = 150;
	private static final int RESIZE_SETTLE_IDLE_MS = 120;
	private static final byte[] STRAIGHT_SRGB_BY_PREMULTIPLIED_CHANNEL_AND_ALPHA =
			createStraightSrgbLookup();
	// Only Windows both loses contexts often enough to matter and supports
	// WGL_ARB_create_context_robustness across all three vendors, so the request is limited
	// to it: a driver that rejects the attribute would fail context creation outright, and
	// losing the 3D view entirely is worse than losing the diagnostic.
	// openrocket.figure3d.disableRobustContext turns it off if a driver does object.
	private static final boolean REQUEST_ROBUST_CONTEXT =
			SystemInfo.getPlatform() == SystemInfo.Platform.WINDOWS
					&& !Boolean.getBoolean("openrocket.figure3d.disableRobustContext");
	private static final int[] STARTUP_FRAME_RECOVERY_DELAYS_MS = {150, 400, 800, 1500, 3000, 5000};
	private final AtomicInteger startupRecoveryGeneration = new AtomicInteger(0);

	// Track dimensions to detect actual size changes
	private int lastFramebufferWidth = -1;
	private int lastFramebufferHeight = -1;

	// Camera tracking for HUD updates - volatile for thread safety between EDT and render timer
	private volatile boolean cameraIsMoving = false;
	private volatile boolean dragInteractionActive = false;
	private volatile boolean wheelInteractionActive = false;
	private final Timer wheelInteractionEndTimer;
	private final Timer resizeSettleTimer;

	// Resize coordination between EDT and render thread
	private volatile boolean resizeRequested = false;

	// Initialization guard - prevents resize/render operations before GL is fully ready
	private volatile boolean glInitialized = false;
	public volatile boolean glInitFailed = false;
	private final AtomicBoolean cleanupStarted = new AtomicBoolean(false);
	private final AtomicBoolean hostCleanupFinished = new AtomicBoolean(false);
	// Swap count when the current recovery began. Follow-up redraws stop as soon as
	// a frame completes.
	private volatile int recoveryBaselineSwapCount = 0;
	// LWJGL capabilities are thread-local; store per-canvas so we can render multiple canvases on one thread.
	private volatile GLCapabilities glCapabilities;
	// Whether this context can report driver resets, and whether one has already been seen.
	private volatile boolean robustnessAvailable = false;
	private volatile boolean graphicsResetDetected = false;

	private final Rocket rocket;
	private static final ExecutorService EXPORT_EXECUTOR;
	// Captures the AWT mouse event that triggered the most recent click-based selection update.
	private final AtomicReference<MouseEvent> pendingSelectionClickEvent = new AtomicReference<>();
	private volatile Runnable renderActivityCallback;
	private volatile Runnable renderRequestCallback;
	private volatile Runnable uiThemeListener;
	private volatile StateChangeListener graphicsPreferencesListener;
	private volatile ApplicationPreferences listenedApplicationPreferences;
	private final FrameExportQueue frameExportQueue = new FrameExportQueue();
	private volatile Consumer<Scene3DOrchestrator> initializationHook;
	private volatile boolean panModeEnabled = false;
	private final AtomicBoolean fatalRenderExceptionReported = new AtomicBoolean(false);
	private volatile Runnable graphicsResetCallback;
	private volatile Runnable glInitFailureCallback;
	private volatile Window ancestorWindow;
	private final WindowAdapter ancestorWindowListener = new WindowAdapter() {
		@Override
		public void windowIconified(WindowEvent e) {
			startupRecoveryGeneration.incrementAndGet();
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			handleWindowDeiconified();
		}

		@Override
		public void windowActivated(WindowEvent e) {
			// Some platforms restore a window without firing windowDeiconified
			// (e.g. restoring via the taskbar on Windows). A single render nudge
			// is cheap and refreshes the canvas without waiting for a user click.
			markRenderActivity();
		}
	};

	static {
		// AWT normally erases a heavyweight Canvas before repainting it after a
		// native resize. The OpenGL swap happens asynchronously, so that erase is
		// visible as a full-canvas flash before the next frame arrives. This JDK
		// property is read when the Canvas peer is created, which is still ahead of
		// us here. This also covers macOS now that lwjgl3-awt presents through its
		// native layer again.
		System.setProperty("sun.awt.noerasebackground", "true");

		// Ensure Swing popups render above the heavyweight AWTGLCanvas (notably on macOS).
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		ThreadFactory exportThreadFactory = r -> {
			Thread t = new Thread(r, "gl-export-writer");
			t.setDaemon(true);
			return t;
		};
		EXPORT_EXECUTOR = Executors.newSingleThreadExecutor(exportThreadFactory);
	}

	public GLScenePanel(Rocket rocket, HUDPanel hudPanel) {
		super(createGLData());
		// Keep the heavyweight peer's fallback fill aligned with the initial GL
		// scene. RocketFigure3d updates this when a document-specific color is used.
		setBackground(GUIUtil.getUITheme().getBackgroundColor());

		this.rocket = rocket;
		this.hudOverlay = hudPanel != null ? new HudOverlay(hudPanel, this) : null;
		this.keyboardHandler = new KeyboardHandler();
		this.wheelInteractionEndTimer = createWheelInteractionEndTimer();
		this.resizeSettleTimer = createResizeSettleTimer();

		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setIgnoreRepaint(true);

		installHierarchyListener();
		installAncestorBoundsRenderListener();
		installResizeListener();
	}

	/** Ends the zoom gesture once the wheel has been idle long enough. */
	private Timer createWheelInteractionEndTimer() {
		Timer timer = new Timer(WHEEL_INTERACTION_IDLE_MS, e -> {
			wheelInteractionActive = false;
			updateCameraInteractionMode();
			markHudForUpdate();
			// Render one full-quality frame after the wheel has been idle long enough
			// to consider the zoom gesture complete.
			markRenderActivity();
		});
		timer.setRepeats(false);
		return timer;
	}

	/**
	 * Forces one redraw after a resize has settled.
	 *
	 * <p>A resize changes the size of the native drawable underneath the render thread:
	 * a frame that was already in flight gets presented at the old size, and the part
	 * of the canvas that the resize newly exposed keeps whatever the driver happened
	 * to leave there until a correctly sized frame is swapped in. This guarantees that
	 * frame, even when the render tick that observed the resize ran while the canvas
	 * was between layouts and therefore produced nothing.</p>
	 */
	private Timer createResizeSettleTimer() {
		Timer timer = new Timer(RESIZE_SETTLE_IDLE_MS, e -> {
			markHudForUpdate();
			requestRenderNow();
		});
		timer.setRepeats(false);
		return timer;
	}

	private void installHierarchyListener() {
		addHierarchyListener(e -> {
			long changeFlags = e.getChangeFlags();
			if ((changeFlags & (HierarchyEvent.PARENT_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED
					| HierarchyEvent.SHOWING_CHANGED)) != 0) {
				updateAncestorWindowListener();
			}
			if ((changeFlags & HierarchyEvent.SHOWING_CHANGED) != 0) {
				handleShowingChanged();
			}
		});
	}

	/**
	 * Requests a frame when layout changes move or resize an ancestor without
	 * delivering a component event to this heavyweight canvas. lwjgl3-awt updates
	 * the native layer bounds when the next context is activated; OpenRocket still
	 * needs to schedule that activation because its renderer is demand-driven.
	 */
	private void installAncestorBoundsRenderListener() {
		addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsAdapter() {
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				handleAncestorBoundsChanged();
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				handleAncestorBoundsChanged();
			}
		});
	}

	private void handleAncestorBoundsChanged() {
		if (!isDisplayable() || !isShowing()) {
			return;
		}
		resizeRequested = true;
		resizeSettleTimer.restart();
		markHudForUpdate();
		requestRenderNow();
	}

	private void installResizeListener() {
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (!isDisplayable()) {
					return;
				}
				// Re-arm on every event, including the ones that carry no new size, so the
				// settle redraw always happens after the *last* resize rather than after the
				// last one that changed the canvas dimensions.
				resizeSettleTimer.restart();
				markRenderActivity();
				resizeRequested = true;
				markHudForUpdate();
			}
		});
	}

	private void reportFatalRenderException(Throwable throwable) {
		if (!fatalRenderExceptionReported.compareAndSet(false, true)) {
			return;
		}

		Throwable reportable = throwable instanceof BugException
				? throwable
				: new BugException("3D view failed while initializing or rendering. Panel state: "
						+ getDebugStateSummary(), throwable);

		if (Application.getExceptionHandler() != null) {
			Application.getExceptionHandler().handleErrorCondition(reportable);
		} else if (reportable instanceof RuntimeException runtimeException) {
			throw runtimeException;
		} else {
			throw new RuntimeException(reportable);
		}
	}

	/**
	 * Registers a callback invoked when input, resize, or queued scene work makes the canvas stale.
	 * Demand-driven hosts use it to schedule a fresh frame.
	 */
	public void setRenderActivityCallback(Runnable callback) {
		this.renderActivityCallback = callback;
	}

	public void setRenderRequestCallback(Runnable callback) {
		this.renderRequestCallback = callback;
	}

	// Track camera interaction independently of the renderer preference so the
	// configured quality policy can decide whether any effects are suspended.
	private void setDragInteractionActive(boolean active) {
		dragInteractionActive = active;
		updateCameraInteractionMode();
	}

	private void beginWheelInteraction() {
		wheelInteractionActive = true;
		updateCameraInteractionMode();
		wheelInteractionEndTimer.restart();
	}

	private void updateCameraInteractionMode() {
		boolean moving = dragInteractionActive || wheelInteractionActive;
		cameraIsMoving = moving;
		Scene3DOrchestrator orchestrator = scene3DOrchestrator;
		if (orchestrator != null) {
			GLRenderer renderer = orchestrator.getRenderer();
			if (renderer != null) {
				renderer.setInteractionMode(moving);
			}
		}
	}

	public void setGraphicsResetCallback(Runnable callback) {
		this.graphicsResetCallback = callback;
	}

	public void setGlInitFailureCallback(Runnable callback) {
		this.glInitFailureCallback = callback;
	}

	/**
	 * Checks whether the driver has reset the graphics context underneath us, which on Windows
	 * happens when the GPU watchdog fires, when the display driver is replaced, and on some
	 * sleep/resume paths. Every GL call after a reset has undefined behaviour and typically
	 * takes the process down without a Java-level trace, so the first thing a frame does is
	 * ask. A reset is reported once and then handed to the canvas-rebuild recovery that
	 * already exists for a dead context.
	 *
	 * @return {@code true} if the context is gone and this frame must not touch GL
	 */
	private boolean hasGraphicsContextBeenReset() {
		if (!robustnessAvailable || graphicsResetDetected) {
			return graphicsResetDetected;
		}

		int status = ARBRobustness.glGetGraphicsResetStatusARB();
		if (status == org.lwjgl.opengl.GL11.GL_NO_ERROR) {
			return false;
		}

		graphicsResetDetected = true;
		glInitialized = false;
		log.error("The graphics driver reset the OpenGL context (status 0x{}). This is usually the "
						+ "GPU watchdog recovering from a hung or overlong draw, a display driver update, "
						+ "or a resume from sleep. Rebuilding the 3D canvas. Panel state: {}",
				Integer.toHexString(status), getDebugStateSummary());

		Runnable callback = graphicsResetCallback;
		if (callback != null) {
			callback.run();
		}
		return true;
	}

	/** True once the GL scene exists and the canvas can still be drawn into. */
	boolean isSceneReady() {
		return scene3DOrchestrator != null && isDisplayable();
	}

	void markRenderActivity() {
		Runnable callback = renderActivityCallback;
		if (callback != null) {
			callback.run();
		}
	}

	/**
	 * Schedules the frame which uploads a HUD image rasterised asynchronously by Swing.
	 */
	void requestHudUpload() {
		requestRenderNow();
	}

	/**
	 * Asks the owner to render a frame as soon as possible, rather than waiting for the
	 * next scheduler tick to notice that the canvas is dirty.
	 */
	private void requestRenderNow() {
		Runnable callback = renderRequestCallback;
		if (callback != null) {
			callback.run();
		} else {
			markRenderActivity();
		}
	}

	private void beginVisibleFrameRecovery() {
		recoveryBaselineSwapCount = swapCallCount.get();
		markHudForUpdate();
		markRenderActivity();
		revalidate();
		repaint();
		scheduleStartupFrameRecoverySequence();
	}

	private void handleWindowDeiconified() {
		if (!isDisplayable()) {
			return;
		}
		beginVisibleFrameRecovery();
	}

	private void updateAncestorWindowListener() {
		Window window = SwingUtilities.getWindowAncestor(this);
		if (window == ancestorWindow) {
			return;
		}
		if (ancestorWindow != null) {
			ancestorWindow.removeWindowListener(ancestorWindowListener);
		}
		ancestorWindow = window;
		if (ancestorWindow != null) {
			ancestorWindow.addWindowListener(ancestorWindowListener);
		}
	}

	private void detachAncestorWindowListener() {
		if (ancestorWindow == null) {
			return;
		}
		ancestorWindow.removeWindowListener(ancestorWindowListener);
		ancestorWindow = null;
	}

	private void handleShowingChanged() {
		if (!isShowing()) {
			startupRecoveryGeneration.incrementAndGet();
			return;
		}

		beginVisibleFrameRecovery();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		updateAncestorWindowListener();
		recoveryBaselineSwapCount = swapCallCount.get();
		scheduleStartupFrameRecoverySequence();
	}

	@Override
	public void removeNotify() {
		detachAncestorWindowListener();
		beginCleanup();
		try {
			super.removeNotify();
		} finally {
			finishCleanup();
		}
	}

	private void scheduleStartupFrameRecoverySequence() {
		int generation = startupRecoveryGeneration.incrementAndGet();
		for (int delayMs : STARTUP_FRAME_RECOVERY_DELAYS_MS) {
			Timer timer = new Timer(delayMs, e -> runStartupFrameRecovery(generation, delayMs));
			timer.setRepeats(false);
			timer.start();
		}
	}

	private void runStartupFrameRecovery(int generation, int delayMs) {
		if (generation != startupRecoveryGeneration.get() || glInitFailed
				|| swapCallCount.get() > recoveryBaselineSwapCount) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		markRenderActivity();
		Container parent = getParent();
		if (parent != null) {
			parent.revalidate();
			parent.repaint();
		}
		revalidate();
		repaint();

		// The background render scheduler is already driving this canvas on all platforms.
		// Forcing a direct EDT render here would race with the background thread and can
		// crash inside JAWT surface acquisition while the heavyweight peer is still settling.
		// Instead, re-mark the view dirty so the shared scheduler performs the retry.

		if (delayMs == STARTUP_FRAME_RECOVERY_DELAYS_MS[STARTUP_FRAME_RECOVERY_DELAYS_MS.length - 1]
				&& swapCallCount.get() == recoveryBaselineSwapCount) {
			log.warn("No completed 3D frame after startup recovery: {}", getDebugStateSummary());
		}
	}

	private void addInputListeners() {
		if (scene3DOrchestrator == null) return;

		CanvasMouseHandler mouseAdapter = new CanvasMouseHandler();
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);

		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				markRenderActivity();
				keyboardHandler.handleKeyEvent(e.getKeyCode(), 1);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				markRenderActivity();
				keyboardHandler.handleKeyEvent(e.getKeyCode(), 0);
			}
		});
	}

	/**
	 * Turns mouse events into scene interaction: orbit, pan, light drag, zoom and
	 * selection.
	 *
	 * <p>Every handler re-reads the orchestrator and gives up when it has gone. The
	 * canvas keeps receiving AWT events while it is being torn down or rebuilt, after
	 * cleanup() has released it.</p>
	 */
	private class CanvasMouseHandler extends MouseAdapter {
		private Point pressPoint;
		private Point lastPoint;
		private boolean isDragging;
		private int activeDragButton = MouseEvent.NOBUTTON;

		@Override
		public void mousePressed(MouseEvent e) {
			// The canvas keeps receiving AWT events during teardown/rebuild,
			// after cleanup() has released the orchestrator.
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			if (isTrackedDragButton(e)) {
				// Track modifier state for multi-selection.
				InputState inputState = orchestrator.getInputHandler().getInputState();
				inputState.isShiftPressed = e.isShiftDown() || e.isMetaDown();
				// Use Swing's built-in click count to detect double-clicks on left button.
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					inputState.doubleClickPoint.set(e.getPoint());
				}
				pressPoint = e.getPoint();
				lastPoint = e.getPoint();
				isDragging = false;
				activeDragButton = e.getButton();
				setDragInteractionActive(true);
			}
			markRenderActivity();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			if (isTrackedDragButton(e)) {
				InputState inputState = orchestrator.getInputHandler().getInputState();
				// We check for !isDragging to differentiate a click from a drag-release.
				// A double-click will also fire this event for the second click.
				if (!isDragging && pressPoint != null &&
						(SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e))) {
					// Right-click popup selection mirrors the 2D view and does not use
					// shift/meta as a multi-select modifier.
					inputState.isShiftPressed = SwingUtilities.isLeftMouseButton(e) &&
							(e.isShiftDown() || e.isMetaDown());
					inputState.clickPoint.set(pressPoint);
					pendingSelectionClickEvent.set(e);
				}
				pressPoint = null;
				isDragging = false;
				activeDragButton = MouseEvent.NOBUTTON;
				inputState.isLightDragging = false;
				inputState.isPanning = false;
				setDragInteractionActive(false);
			}
			// Mark dirty after leaving interaction mode so the release frame is
			// guaranteed to use the user's full quality settings.
			markRenderActivity();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			if (isTrackedDragButton(activeDragButton) && pressPoint != null) {
				InputState inputState = orchestrator.getInputHandler().getInputState();
				if (!isDragging && pressPoint.distanceSq(e.getPoint()) > CLICK_DRAG_THRESHOLD_SQ) {
					isDragging = true;
					inputState.dragJustStarted = true;
				}

				if (isDragging) {
					updateDragMode(inputState, e);

					// Always update the drag delta
					float deltaX = e.getX() - lastPoint.x;
					float deltaY = e.getY() - lastPoint.y;
					inputState.addDrag(deltaX, deltaY);
				}
				lastPoint = e.getPoint();
			}
			markRenderActivity();
		}

		private void updateDragMode(InputState inputState, MouseEvent e) {
			boolean isRightDrag = activeDragButton == MouseEvent.BUTTON3;
			boolean isMiddleDrag = activeDragButton == MouseEvent.BUTTON2;
			boolean isAltDown = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;
			boolean isCtrlDown = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
			inputState.isLightDragging = isRightDrag || isAltDown;
			inputState.isPanning = !inputState.isLightDragging && (panModeEnabled || isCtrlDown || isMiddleDrag);
		}

		private boolean isTrackedDragButton(MouseEvent e) {
			return SwingUtilities.isLeftMouseButton(e)
					|| SwingUtilities.isMiddleMouseButton(e)
					|| SwingUtilities.isRightMouseButton(e);
		}

		private boolean isTrackedDragButton(int button) {
			return button == MouseEvent.BUTTON1
					|| button == MouseEvent.BUTTON2
					|| button == MouseEvent.BUTTON3;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			orchestrator.getInputHandler().getInputState().addScroll(e.getWheelRotation() * -1.0f, e.getX(), e.getY());
			beginWheelInteraction();
			markHudForUpdate(); // Mark HUD for update on zoom
			markRenderActivity();
		}
	}

	/**
	 * Binds the keyboard shortcuts the 3D view offers.
	 *
	 * <p>Only view-level actions belong here. These used to be installed from the demo
	 * scene factory, which also bound keys that edited the open design: 'G' gave the nose
	 * cone a random length and 'C' a random colour, both reachable in the normal design
	 * view whenever the canvas had focus.</p>
	 */
	private void addViewKeyBindings() {
		keyboardHandler.addSinglePressAction(KeyEvent.VK_F, scene3DOrchestrator::focusOnRocket);
		keyboardHandler.addSinglePressAction(KeyEvent.VK_E, () -> {
			// Shift exports with a transparent background.
			frameExportQueue.requestFileExport(keyboardHandler.isKeyPressed(KeyEvent.VK_SHIFT));
			requestRenderNow();
		});
	}

	static GLData createGLData() {
		GLData data = new GLData();
		data.majorVersion = 3;
		data.minorVersion = 1;
		data.versionPolicy = GLData.VersionPolicy.AT_LEAST;
		// OpenGL 3.1 predates core/compatibility profile selection. The renderer's
		// vertex attributes and fragment output are bound before shader linking, so
		// it does not need GLSL 3.30's explicit-location syntax. Keeping this profile
		// unspecified also permits Mesa's desktop OpenGL 3.1 context on Raspberry Pi.
		data.profile = null;
		data.doubleBuffer = true;
		// The macOS backend rejects debug-context attributes. GLDebug can still install
		// a KHR_debug/ARB_debug_output callback when the resulting context supports it.
		data.debug = GLDebug.isDebugRequested()
				&& SystemInfo.getPlatform() != SystemInfo.Platform.MAC_OS;
		// Default-framebuffer MSAA is disabled — see getRequestedSampleCount().
		data.samples = getRequestedSampleCount();
		// Do not request an sRGB default framebuffer: the macOS backend rejects that
		// pixel-format attribute, and presentation is already gamma-corrected by the renderer.
		// Disable the swap interval to avoid vsync stalls when multiple canvases share a thread.
		// On Linux, an explicit zero makes GLX swap-control support mandatory; leaving it
		// unspecified also permits valid contexts that only expose SGI swap control (or none).
		data.swapInterval = SystemInfo.getPlatform() == SystemInfo.Platform.UNIX ? null : 0;
		if (REQUEST_ROBUST_CONTEXT) {
			// Windows resets the graphics context when the driver's watchdog fires, when the
			// display driver is updated, and on some sleep/resume paths. Without robustness
			// there is no way to notice: every later GL call goes into a dead context and the
			// process dies with nothing in the log. With it, glGetGraphicsResetStatus reports
			// the reset and the canvas can be rebuilt instead.
			data.robustness = true;
			data.loseContextOnReset = true;
		}
		return data;
	}

	private static int getRequestedSampleCount() {
		String override = System.getProperty("openrocket.figure3d.samples");
		if (override != null) {
			try {
				return Math.max(0, Integer.parseInt(override.trim()));
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}
		// The main scene now handles anti-aliasing inside the off-screen pipeline. Keep the
		// heavyweight AWT default framebuffer single-sampled to avoid WGL multisample context
		// creation failures on Windows; the renderer resolves its own MSAA target before present.
		// Requesting samples > 0 causes hard context-creation failures on Windows when
		// WGL_ARB_multisample / WGL_EXT_multisample is unavailable (some drivers, VMs).
		return 0;
	}

	/** Returns the native framebuffer dimensions maintained by lwjgl3-awt. */
	int[] getCurrentFramebufferSize() {
		return new int[]{Math.max(1, getFramebufferWidth()), Math.max(1, getFramebufferHeight())};
	}

	@Override
	protected void beforeRender() {
		super.beforeRender();
		// When multiple AWTGLCanvas instances are rendered from the same thread (e.g. on macOS),
		// the active OpenGL context changes between calls. LWJGL requires updating the
		// thread-local capabilities to match the current context.
		GLCapabilities caps = glCapabilities;
		if (caps != null) {
			GL.setCapabilities(caps);
		}
	}

	@Override
	protected void afterRender() {
		try {
			super.afterRender();
		} finally {
			// Avoid leaving stale capabilities set when no context is current.
			GL.setCapabilities(null);
		}
	}

	@Override
	public void initGL() {
		try {
			configureGlState();
			log.debug("OpenGL context {}", GLContextDiagnostics.record(data, effective));

			int windowWidth = Math.max(1, getWidth());
			int windowHeight = Math.max(1, getHeight());
			int[] framebufferSize = getCurrentFramebufferSize();
			int framebufferWidth = framebufferSize[0];
			int framebufferHeight = framebufferSize[1];

			createScene(windowWidth, windowHeight, framebufferWidth, framebufferHeight);
			if (hudOverlay != null) {
				lastFramebufferWidth = framebufferWidth;
				lastFramebufferHeight = framebufferHeight;
				hudOverlay.initResources();
			}

			addInputListeners();
			addViewKeyBindings();

			resizeRequested = false;

			// Mark initialization complete - allows resize/render operations to proceed
			glInitialized = true;
			glInitLatch.countDown();
		} catch (Exception e) {
			glInitFailed = true;
			glInitLatch.countDown();
			frameExportQueue.failPendingCaptures();
			throw new RuntimeException("Failed to initialize renderer", e);
		}
	}

	/** Reads back what the context supports and sets the state the renderer assumes. */
	private void configureGlState() {
		glCapabilities = GL.createCapabilities();
		robustnessAvailable = glCapabilities.GL_ARB_robustness;
		graphicsResetDetected = false;
		if (REQUEST_ROBUST_CONTEXT && !robustnessAvailable) {
			log.info("Robust context requested but ARB_robustness is unavailable; "
					+ "a driver reset will not be reported.");
		}
		GLDebug.enableIfRequested("AWT-canvas");
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glEnable(GL_FRAMEBUFFER_SRGB);
	}

	/** Builds the orchestrator and its scene, and hooks it up to the preferences and the HUD. */
	private void createScene(int windowWidth, int windowHeight,
			int framebufferWidth, int framebufferHeight) throws Exception {
		scene3DOrchestrator = Scene3DOrchestrator.create(
				rocket, windowWidth, windowHeight, framebufferWidth, framebufferHeight);
		scene3DOrchestrator.setGlTaskQueuedCallback(this::markRenderActivity);

		// Create the scene mesh through the orchestrator so context-owned textures
		// use this canvas's decal cache rather than the process-wide fallback cache.
		scene3DOrchestrator.rebuildRocketScene(false);
		applyThemeBackground(scene3DOrchestrator.getScene());
		installThemeListener();
		installGraphicsPreferencesListener();
		scene3DOrchestrator.focusOnRocket();

		Consumer<Scene3DOrchestrator> initHook = initializationHook;
		if (initHook != null) {
			initHook.accept(scene3DOrchestrator);
		}
		if (hudOverlay != null) {
			hudOverlay.getPanel().setSceneViewController(this.scene3DOrchestrator);
			hudOverlay.getPanel().setGLScenePanel(this);
		}
	}

	@Override
	public void render() {
		if (glInitFailed) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
		renderCallCount.incrementAndGet();
		int paintBefore = paintCallCount.get();
		int swapBefore = swapCallCount.get();
		try {
			// Delegate to AWTGLCanvas to make the context current and handle buffer swapping.
			super.render();
			int paintAfter = paintCallCount.get();
			int swapAfter = swapCallCount.get();
			if (paintAfter == paintBefore && swapAfter == swapBefore) {
				SwingUtilities.invokeLater(() -> {
					revalidate();
					repaint();
				});
			}
		} catch (Throwable t) {
			glInitFailed = true;
			String msg = t.getMessage();
			if (!glInitialized) {
				log.error("3D view disabled: OpenGL context could not be initialized. Cause: {}", msg, t);
				Runnable failureCallback = glInitFailureCallback;
				if (failureCallback != null) {
					failureCallback.run();
				}
			} else {
				log.error("3D rendering failed: {}: {}", t.getClass().getSimpleName(), msg, t);
				reportFatalRenderException(t);
			}
		}
	}

	@Override
	public void paintGL() {
		if (!glInitialized || !isDisplayable()) {
			return;
		}

		boolean shouldSwap = false;
		try {
			if (scene3DOrchestrator == null) {
				return;
			}

			if (hasGraphicsContextBeenReset()) {
				return;
			}

			handlePendingResize();

			GLRenderer renderer = scene3DOrchestrator.getRenderer();
			Scene sceneView = scene3DOrchestrator.getScene();

			if (renderer == null || sceneView == null) {
				return;
			}
			paintCallCount.incrementAndGet();

			// --- 3D Scene Rendering & Export Logic ---
			handleKeyboardEvents();
			int[] fbSize = getCurrentFramebufferSize();
			glViewport(0, 0, fbSize[0], fbSize[1]);
			scene3DOrchestrator.update();

			if (cameraIsMoving) {
				markHudForUpdate();
			}

			// Check for an export request before the main render. The export frame is
			// captured off-screen only; the normal display frame below is what gets
			// presented, so a transparent-background export never flashes on screen.
			if (!frameExportQueue.isEmpty()) {
				handlePendingExports(sceneView, renderer);
			}

			// --- Main Display Rendering --
			renderer.render(sceneView, true);
			renderer.presentResolvedToCurrentFramebuffer();

			if (hudOverlay != null) {
				hudOverlay.refresh(cameraIsMoving);
				hudOverlay.uploadIfReady();
				hudOverlay.draw();
				// HUD drawing binds textures directly; reset cached state before next frame.
				renderer.resetTextureState();
			}
			shouldSwap = true;
		} catch (Exception ex) {
			log.error("Error during paintGL", ex);
		} finally {
			if (shouldSwap) {
				swapBuffers();
				swapCallCount.incrementAndGet();
			}
		}
	}

	private void handlePendingExports(Scene sceneView, GLRenderer renderer) {
		FrameExportQueue.Request request;
		while ((request = frameExportQueue.poll()) != null) {
			try {
				handleExport(sceneView, renderer, request);
			} catch (RuntimeException | Error e) {
				frameExportQueue.failPendingCaptures();
				throw e;
			}
		}
	}

	private void handleExport(Scene sceneView, GLRenderer renderer, FrameExportQueue.Request request) {
		BufferedImage capturedImage = null;
		try {
			renderer.render(sceneView, !request.isTransparent());

			int exportWidth = renderer.getRenderWidth();
			int exportHeight = renderer.getRenderHeight();
			ByteBuffer exportBuffer = captureResolvedFramebuffer(renderer, exportWidth, exportHeight);
			if (exportBuffer == null) {
				log.warn("Export skipped: no framebuffer data available");
				return;
			}

			if (request.isImageCapture()) {
				capturedImage = bufferToImage(exportBuffer, exportWidth, exportHeight);
				return;
			}

			String filePath = "export_" + System.currentTimeMillis() + ".png";
			EXPORT_EXECUTOR.submit(() -> writePng(exportBuffer, exportWidth, exportHeight, filePath));
		} finally {
			if (request.isImageCapture()) {
				request.complete(capturedImage);
			}
		}
	}

	private void handlePendingResize() {
		if (!glInitialized || scene3DOrchestrator == null) {
			return;
		}

		int windowWidth = Math.max(1, getWidth());
		int windowHeight = Math.max(1, getHeight());
		int[] framebufferSize = getCurrentFramebufferSize();
		int framebufferWidth = framebufferSize[0];
		int framebufferHeight = framebufferSize[1];
		ViewportDimensions viewport = scene3DOrchestrator.getViewport();

		if (viewport.getWindowWidth() == windowWidth
				&& viewport.getWindowHeight() == windowHeight
				&& viewport.getFramebufferWidth() == framebufferWidth
				&& viewport.getFramebufferHeight() == framebufferHeight) {
			resizeRequested = false;
			return;
		}

		glViewport(0, 0, framebufferWidth, framebufferHeight);
		scene3DOrchestrator.resize(windowWidth, windowHeight, framebufferWidth, framebufferHeight);

		if (hudOverlay != null) {
			if (framebufferWidth != lastFramebufferWidth || framebufferHeight != lastFramebufferHeight) {
				hudOverlay.initTexture();
				lastFramebufferWidth = framebufferWidth;
				lastFramebufferHeight = framebufferHeight;
				hudOverlay.markForUpdate();
				hudOverlay.requestRepaint(true);
			} else {
				// AWT's logical bounds can advance before lwjgl3-awt reports the
				// corresponding native framebuffer. Do not stretch the old HUD across
				// that transient size; the EDT repaint will restore it immediately.
				hudOverlay.invalidateForResize();
			}
		}

		resizeRequested = false;
	}

	private ByteBuffer captureResolvedFramebuffer(GLRenderer renderer, int width, int height) {
		if (width <= 0 || height <= 0) {
			return null;
		}

		int framebufferId = renderer.getResolvedFramebufferId();
		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int previousReadBuffer = glGetInteger(GL_READ_BUFFER);

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		glReadBuffer(framebufferId == 0 ? getDefaultFramebufferColorBuffer() : GL_COLOR_ATTACHMENT0);

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
		glReadBuffer(previousReadBuffer);

		buffer.rewind();
		return buffer;
	}

	private int getDefaultFramebufferColorBuffer() {
		return glGetInteger(GL_DOUBLEBUFFER) != 0 ? GL_BACK : GL_FRONT;
	}

	static BufferedImage bufferToImage(ByteBuffer buffer, int width, int height) {
		buffer.rewind();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = (x + (width * y)) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				if (a == 0) {
					// Fully transparent PNG pixels must not retain an invisible matte.
					r = 0;
					g = 0;
					b = 0;
				} else if (a < 0xFF) {
					// Rendering and MSAA resolve operate on premultiplied linear RGB in an
					// sRGB framebuffer. PNG stores straight-alpha sRGB, so decode before
					// unpremultiplying and encode again afterwards.
					r = unpremultiplySrgbChannel(r, a);
					g = unpremultiplySrgbChannel(g, a);
					b = unpremultiplySrgbChannel(b, a);
				}
				image.setRGB(x, height - 1 - y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return image;
	}

	private static int unpremultiplySrgbChannel(int channel, int alpha) {
		return STRAIGHT_SRGB_BY_PREMULTIPLIED_CHANNEL_AND_ALPHA[(alpha << 8) | channel] & 0xFF;
	}

	private static byte[] createStraightSrgbLookup() {
		byte[] lookup = new byte[256 * 256];
		for (int alpha = 1; alpha < 0xFF; alpha++) {
			float normalizedAlpha = alpha / 255.0f;
			for (int channel = 0; channel < 256; channel++) {
				float premultipliedLinear = ColorUtils.srgbChannelToLinear(channel / 255.0f);
				float straightLinear = Math.min(1.0f, premultipliedLinear / normalizedAlpha);
				int straightSrgb = Math.round(ColorUtils.linearChannelToSrgb(straightLinear) * 255.0f);
				lookup[(alpha << 8) | channel] = (byte) straightSrgb;
			}
		}
		for (int channel = 0; channel < 256; channel++) {
			lookup[(0xFF << 8) | channel] = (byte) channel;
		}
		return lookup;
	}

	private void writePng(ByteBuffer buffer, int width, int height, String filePath) {
		try {
			BufferedImage image = bufferToImage(buffer, width, height);
			ImageIO.write(image, "png", new File(filePath));
			log.info("Successfully exported view to {}", filePath);
		} catch (IOException e) {
			log.error("Failed to write PNG file: {}", e.getMessage());
		}
	}

	public void handleKeyboardEvents() {
		if (keyboardHandler != null) {
			keyboardHandler.handleQueuedEvents();
		}
	}

	/**
	 * Waits for the GL context to finish initialization.
	 *
	 * @param timeoutMs how long to wait in milliseconds
	 * @return true if initialized before timeout, false otherwise
	 */
	public boolean awaitInitialized(long timeoutMs) {
		try {
			boolean completed = glInitLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
			return completed && glInitialized && !glInitFailed;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	@Override
	public void markHudForUpdate() {
		if (hudOverlay != null) {
			hudOverlay.markForUpdate();
		}
	}

	public void requestImageCapture(boolean transparent, Consumer<BufferedImage> callback) {
		if (callback == null) {
			return;
		}
		Consumer<BufferedImage> edtCompletion = image -> SwingUtilities.invokeLater(() -> callback.accept(image));
		if (cleanupStarted.get() || glInitFailed) {
			edtCompletion.accept(null);
			return;
		}
		frameExportQueue.requestImageCapture(transparent, edtCompletion);
		requestRenderNow();
	}

	public void setInitializationHook(Consumer<Scene3DOrchestrator> initializationHook) {
		this.initializationHook = initializationHook;
	}

	public void setPanModeEnabled(boolean enabled) {
		panModeEnabled = enabled;
		setCursor(enabled
				? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
				: Cursor.getDefaultCursor());
	}

	public void addSceneSelectionListener(SelectionListener listener) {
		if (scene3DOrchestrator == null) {
			return;
		}
		scene3DOrchestrator.enqueueGlTask(() -> {
			if (scene3DOrchestrator.getScene() != null) {
				scene3DOrchestrator.getScene().addSelectionListener(listener);
			}
		});
	}

	/**
	 * Returns and clears the most recent click event that triggered a selection change.
	 * If the selection changed programmatically (e.g., via setSelection), this returns null.
	 */
	public MouseEvent consumePendingSelectionClickEvent() {
		return pendingSelectionClickEvent.getAndSet(null);
	}

	public Scene3DOrchestrator getScene3DOrchestrator() {
		return scene3DOrchestrator;
	}

	public void cleanup() {
		beginCleanup();
		try {
			// disposeCanvas() invokes disposeGL() with the context current, and is
			// idempotent when AWT removal has already disposed the native context.
			disposeCanvas();
		} catch (RuntimeException e) {
			log.warn("Error disposing 3D canvas: {}", e.getMessage(), e);
		} finally {
			finishCleanup();
		}
	}

	private void beginCleanup() {
		if (!cleanupStarted.compareAndSet(false, true)) {
			return;
		}

		// Prevent any further rendering/resize operations
		wheelInteractionEndTimer.stop();
		resizeSettleTimer.stop();
		dragInteractionActive = false;
		wheelInteractionActive = false;
		updateCameraInteractionMode();
		glInitialized = false;
		frameExportQueue.failPendingCaptures();
		startupRecoveryGeneration.incrementAndGet();
		if (hudOverlay != null) {
			hudOverlay.resetPendingWork();
		}
		uninstallThemeListener();
		uninstallGraphicsPreferencesListener();
		if (scene3DOrchestrator != null) {
			scene3DOrchestrator.shutdown();
		}
	}

	@Override
	protected void disposeGL() {
		GLCapabilities capabilities = glCapabilities;
		if (capabilities == null) {
			return;
		}

		GLCapabilities previousCapabilities = null;
		try {
			try {
				previousCapabilities = GL.getCapabilities();
			} catch (IllegalStateException ignored) {
				// No capabilities were installed on the disposal thread.
			}
			GL.setCapabilities(capabilities);
			cleanupGLResources();
		} finally {
			GL.setCapabilities(previousCapabilities);
		}
	}

	private void finishCleanup() {
		if (!hostCleanupFinished.compareAndSet(false, true)) {
			return;
		}

		// Host memory does not require a current GL context.
		if (hudOverlay != null) {
			hudOverlay.cleanupHostResources();
		}
		scene3DOrchestrator = null;
		glCapabilities = null;

		GpuResourceTracker.logLiveResources("Swing canvas cleanup (other canvases may still be active)", false);
	}

	/**
	 * Releases GL-side resources from {@link #disposeGL()}, while lwjgl3-awt has
	 * made this canvas's context current and serialized disposal with rendering.
	 */
	private void cleanupGLResources() {
		if (scene3DOrchestrator != null) {
			try {
				Scene scene = scene3DOrchestrator.getScene();
				GLRenderer renderer = scene3DOrchestrator.getRenderer();
				if (scene != null) {
					scene.cleanup();
				}
				if (renderer != null) {
					renderer.cleanup();
				}
				scene3DOrchestrator.getDecalTextureCache().cleanup();
			} catch (Exception e) {
				log.warn("Error cleaning 3D resources: {}", e.getMessage());
			}
		}
		if (hudOverlay != null) {
			hudOverlay.cleanupGlResources();
		}
	}

	public int getRenderCallCount() {
		return renderCallCount.get();
	}

	public int getPaintCallCount() {
		return paintCallCount.get();
	}

	public int getSwapCallCount() {
		return swapCallCount.get();
	}

	public boolean hasCompletedFrame() {
		return swapCallCount.get() > 0;
	}

	/**
	 * Whether a resize has been observed that no rendered frame has picked up yet.
	 */
	public boolean hasPendingResize() {
		return resizeRequested;
	}

	public String getDebugStateSummary() {
		return "displayable=" + isDisplayable()
				+ ", showing=" + isShowing()
				+ ", visible=" + isVisible()
				+ ", size=" + getWidth() + "x" + getHeight()
				+ ", framebuffer=" + getFramebufferWidth() + "x" + getFramebufferHeight()
				+ ", glInitialized=" + glInitialized
				+ ", glInitFailed=" + glInitFailed
				+ ", renderCalls=" + renderCallCount.get()
				+ ", paintCalls=" + paintCallCount.get()
				+ ", swapCalls=" + swapCallCount.get();
	}

	private void applyThemeBackground(Scene scene) {
		if (scene == null) {
			return;
		}
		Color color = GUIUtil.getUITheme().getBackgroundColor();
		float srgbR = color.getRed() / 255.0f;
		float srgbG = color.getGreen() / 255.0f;
		float srgbB = color.getBlue() / 255.0f;
		float alpha = color.getAlpha() / 255.0f;
		Vector4f linear = ColorUtils.srgbToLinear(new Vector4f(srgbR, srgbG, srgbB, alpha));
		scene.setBackground(new SolidColorBackground(linear.x, linear.y, linear.z, linear.w));
	}
	private void installThemeListener() {
		if (uiThemeListener != null) {
			return;
		}
		uiThemeListener = () -> SwingUtilities.invokeLater(() -> {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			orchestrator.enqueueGlTask(() -> applyThemeBackground(orchestrator.getScene()));
			markHudForUpdate();
			revalidate();
			repaint();
			markRenderActivity();
			Runnable requestCallback = renderRequestCallback;
			if (requestCallback != null) {
				requestCallback.run();
			}
		});
		UITheme.Theme.addUIThemeChangeListener(uiThemeListener);
	}

	private void uninstallThemeListener() {
		if (uiThemeListener != null) {
			UITheme.Theme.removeUIThemeChangeListener(uiThemeListener);
			uiThemeListener = null;
		}
	}

	private void installGraphicsPreferencesListener() {
		if (graphicsPreferencesListener != null) {
			return;
		}
		ApplicationPreferences preferences = Application.getPreferences();
		if (preferences == null) {
			return;
		}
		StateChangeListener listener = event -> applyLiveGraphicsPreferences(preferences);
		listenedApplicationPreferences = preferences;
		graphicsPreferencesListener = listener;
		preferences.addChangeListener(listener);
	}

	private void applyLiveGraphicsPreferences(ApplicationPreferences preferences) {
		Scene3DOrchestrator orchestrator = scene3DOrchestrator;
		if (orchestrator == null) {
			return;
		}
		boolean reduceEffects = Figure3DPreferences.shouldReduceEffectsDuringInteraction(preferences);
		boolean msaaEnabled = Figure3DPreferences.isMSAAEnabled(preferences);
		GraphicsQualitySettings quality = orchestrator.getRenderingConfiguration().getQuality();
		if (quality.shouldReduceEffectsDuringInteraction() == reduceEffects
				&& quality.isMSAAEnabled() == msaaEnabled) {
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			if (orchestrator != scene3DOrchestrator) {
				return;
			}
			GraphicsQualitySettings currentQuality = orchestrator.getRenderingConfiguration().getQuality();
			boolean reduceEffectsChanged =
					currentQuality.shouldReduceEffectsDuringInteraction() != reduceEffects;
			boolean msaaChanged = currentQuality.isMSAAEnabled() != msaaEnabled;
			if (!reduceEffectsChanged && !msaaChanged) {
				return;
			}
			currentQuality.setReduceEffectsDuringInteraction(reduceEffects);
			currentQuality.setMSAAEnabled(msaaEnabled);
			orchestrator.getRenderingConfiguration().notifyListeners();
		});
		markRenderActivity();
	}

	private void uninstallGraphicsPreferencesListener() {
		StateChangeListener listener = graphicsPreferencesListener;
		ApplicationPreferences preferences = listenedApplicationPreferences;
		if (listener != null && preferences != null) {
			preferences.removeChangeListener(listener);
		}
		graphicsPreferencesListener = null;
		listenedApplicationPreferences = null;
	}

}
