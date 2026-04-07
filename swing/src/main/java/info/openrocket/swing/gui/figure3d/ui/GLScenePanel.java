package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.DemoFactory;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.input.KeyboardHandler;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.Renderer;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.events.SelectionListener;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.ViewportDimensions;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.figure3d.utils.GLDebug;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_READ_BUFFER;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING;

/**
 * AWTGLCanvas-backed scene panel for Swing integration.
 *
 * <p>This panel supports multiple simultaneous windows by ensuring each window
 * creates its own GL resources (no shared texture pools).</p>
 *
 * <p>Note: There are two input paths in this codebase:</p>
 * <ul>
 *   <li>AWT path (this class): registers Swing listeners and writes into the shared InputState via the orchestrator.</li>
 *   <li>GLFW path: {@code MouseInputHandler} with GLFW callbacks.</li>
 * </ul>
 * <p>Both paths converge on the same SceneInputProcessor so the interaction model is consistent.</p>
 */
public class GLScenePanel extends AWTGLCanvas implements HUDUpdateListener {

	private static final Logger log = LoggerFactory.getLogger(GLScenePanel.class);
	private static final boolean NEEDS_PEER_BOUNDS_SYNC_WORKAROUND = SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS;
	private final AtomicInteger renderCallCount = new AtomicInteger(0);
	private final AtomicInteger paintCallCount = new AtomicInteger(0);
	private final AtomicInteger swapCallCount = new AtomicInteger(0);

	private Scene3DOrchestrator scene3DOrchestrator;
	private final KeyboardHandler keyboardHandler;

	private static final double CLICK_DRAG_THRESHOLD_SQ = 5 * 5;

	private final HUDPanel hudPanel;
	private final boolean hudEnabled;
	private final Object hudLock = new Object();
	private BufferedImage hudImage;
	private Texture hudTexture;
	private Shader hudShader;
	private int hudVao;
	private int hudVbo; // Store VBO reference for cleanup
	private ByteBuffer hudImageBuffer;
	private IntBuffer hudIntBuffer; // Direct IntBuffer view for efficiency
	private Graphics2D hudGraphics; // Reusable Graphics2D context
	private final CountDownLatch glInitLatch = new CountDownLatch(1);
	private final AtomicBoolean hudPaintScheduled = new AtomicBoolean(false);
	private final AtomicBoolean hudBufferReady = new AtomicBoolean(false);
	private volatile long lastHudPaintTimeMs = 0;
	private static final long MIN_HUD_PAINT_INTERVAL_MS = 33; // ~30 FPS max for EDT HUD painting
	private final AtomicBoolean peerBoundsSyncQueued = new AtomicBoolean(false);
	private final AtomicBoolean peerBoundsSyncInProgress = new AtomicBoolean(false);
	private final AtomicInteger peerBoundsSyncAttempts = new AtomicInteger(0);
	private static final int MAX_PEER_BOUNDS_SYNC_ATTEMPTS = 20;
	private static final int PEER_BOUNDS_SYNC_RETRY_DELAY_MS = 50;
	private static final int[] STARTUP_FRAME_RECOVERY_DELAYS_MS = {150, 400, 800, 1500, 3000, 5000};
	private final AtomicInteger startupRecoveryGeneration = new AtomicInteger(0);

	// Track dimensions to detect actual size changes
	private int lastFramebufferWidth = -1;
	private int lastFramebufferHeight = -1;
	private volatile boolean hudNeedsUpdate = true;

	// Camera tracking for HUD updates - volatile for thread safety between EDT and render timer
	private volatile boolean cameraIsMoving = false;

	// Resize coordination between EDT and render thread
	private volatile boolean resizeRequested = false;
	private volatile int pendingWinWidth;
	private volatile int pendingWinHeight;
	private volatile int pendingFbWidth;
	private volatile int pendingFbHeight;

	// Initialization guard - prevents resize/render operations before GL is fully ready
	private volatile boolean glInitialized = false;
	public volatile boolean glInitFailed = false;
	private static final Semaphore INIT_SEMAPHORE = new Semaphore(1, true);
	private static final ReentrantLock RENDER_LOCK = new ReentrantLock(true);
	// LWJGL capabilities are thread-local; store per-canvas so we can render multiple canvases on one thread.
	private volatile GLCapabilities glCapabilities;

	private final Rocket rocket;
	private final boolean peerBoundsSyncEnabled;
	private static final ExecutorService EXPORT_EXECUTOR;
	// Captures the AWT mouse event that triggered the most recent click-based selection update.
	private final AtomicReference<MouseEvent> pendingSelectionClickEvent = new AtomicReference<>();
	private volatile Runnable renderActivityCallback;
	private volatile Runnable uiThemeListener;
	private final AtomicReference<ImageCaptureRequest> imageCaptureRequest = new AtomicReference<>();
	private volatile Consumer<Scene3DOrchestrator> initializationHook;
	private volatile boolean panModeEnabled = false;

	private static final class ImageCaptureRequest {
		private final boolean transparent;
		private final Consumer<BufferedImage> callback;

		private ImageCaptureRequest(boolean transparent, Consumer<BufferedImage> callback) {
			this.transparent = transparent;
			this.callback = callback;
		}
	}

	static {
		// LWJGL 3.3.4+ auto-detects Wayland and tries to initialise EGL, which conflicts
		// with lwjgl3-awt's PlatformLinuxGLCanvas (X11/GLX). Forcing "native" here prevents
		// that auto-switch and keeps the GLX path active. On Wayland systems, XWayland must
		// be running; on X11-only or other platforms this is a no-op.
		Configuration.OPENGL_CONTEXT_API.set("native");

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
		this(rocket, hudPanel, true);
	}

	public GLScenePanel(Rocket rocket, HUDPanel hudPanel, boolean enablePeerBoundsSync) {
		super(createGLData());

		this.rocket = rocket;
		this.hudPanel = hudPanel;
		this.hudEnabled = hudPanel != null;
		this.peerBoundsSyncEnabled = NEEDS_PEER_BOUNDS_SYNC_WORKAROUND && enablePeerBoundsSync;
		this.keyboardHandler = new KeyboardHandler();
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setIgnoreRepaint(true);

		if (peerBoundsSyncEnabled) {
			// CardLayout/JSplitPane switches can change the on-screen position of heavyweight
			// components without changing their local bounds. On macOS this can leave the
			// native peer at an incorrect location until the next real resize. Force a peer
			// bounds sync when the canvas becomes showing.
				addHierarchyListener(e -> {
					if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
						peerBoundsSyncAttempts.set(0);
						if (isShowing()) {
							requestPeerBoundsSync();
							scheduleStartupFrameRecoverySequence();
						}
					}
				});
			// If an ancestor moves (e.g., JSplitPane divider/layout), Swing won't necessarily resize this
			// canvas, but the native peer still needs an updated on-screen location.
			addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsAdapter() {
				@Override
				public void ancestorMoved(HierarchyEvent e) {
					if (!isShowing()) {
						return;
					}
					peerBoundsSyncAttempts.set(0);
					requestPeerBoundsSync();
				}

				@Override
				public void ancestorResized(HierarchyEvent e) {
					if (!isShowing()) {
						return;
					}
					peerBoundsSyncAttempts.set(0);
					requestPeerBoundsSync();
				}
			});
		}

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (!isDisplayable()) {
					return;
				}
				markRenderActivity();
				int width = Math.max(1, getWidth());
				int height = Math.max(1, getHeight());
				pendingWinWidth = width;
				pendingWinHeight = height;
				int[] fbSize = computeFramebufferSize(width, height);
				pendingFbWidth = fbSize[0];
				pendingFbHeight = fbSize[1];
				resizeRequested = true;
				hudNeedsUpdate = true;
			}
		});
	}

	/**
	 * Registers a callback that is invoked when the user interacts with the canvas (mouse/keyboard/resize).
	 * Used by the macOS render scheduler to prioritize recently active 3D views.
	 */
	public void setRenderActivityCallback(Runnable callback) {
		this.renderActivityCallback = callback;
	}

	private void markRenderActivity() {
		Runnable callback = renderActivityCallback;
		if (callback != null) {
			callback.run();
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();
		if (peerBoundsSyncEnabled) {
			peerBoundsSyncAttempts.set(0);
			schedulePeerBoundsSyncRetry(0);
			schedulePeerBoundsSyncRetry(50);
			schedulePeerBoundsSyncRetry(200);
			schedulePeerBoundsSyncRetry(500);
			schedulePeerBoundsSyncRetry(1000);
		}
		scheduleStartupFrameRecoverySequence();
	}

	/**
	 * Explicitly retries the macOS native-peer bounds sync workaround.
	 * Useful when embedding in layouts where SHOWING_CHANGED is unreliable.
	 */
	public void requestPeerBoundsSyncNow() {
		if (!peerBoundsSyncEnabled) {
			return;
		}
		peerBoundsSyncAttempts.set(0);
		requestPeerBoundsSync();
		schedulePeerBoundsSyncRetry(50);
		schedulePeerBoundsSyncRetry(200);
		schedulePeerBoundsSyncRetry(500);
		schedulePeerBoundsSyncRetry(1000);
	}

	private void requestPeerBoundsSync() {
		if (!peerBoundsSyncEnabled) {
			return;
		}
		if (!isDisplayable() || !isShowing()) {
			peerBoundsSyncAttempts.set(0);
			return;
		}
		if (!peerBoundsSyncQueued.compareAndSet(false, true)) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			peerBoundsSyncQueued.set(false);
			if (!isDisplayable() || !isShowing()) {
				peerBoundsSyncAttempts.set(0);
				return;
			}
			int attempt = peerBoundsSyncAttempts.incrementAndGet();
			if (attempt > MAX_PEER_BOUNDS_SYNC_ATTEMPTS) {
				peerBoundsSyncAttempts.set(0);
				return;
			}
			syncPeerBounds();
			if (isPeerMispositioned()) {
				schedulePeerBoundsSyncRetry(PEER_BOUNDS_SYNC_RETRY_DELAY_MS);
			} else {
				peerBoundsSyncAttempts.set(0);
			}
		});
	}

	private void schedulePeerBoundsSyncRetry(int delayMs) {
		Timer timer = new Timer(delayMs, e -> requestPeerBoundsSync());
		timer.setRepeats(false);
		timer.start();
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
		if (generation != startupRecoveryGeneration.get() || glInitFailed || hasCompletedFrame()) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		requestPeerBoundsSyncNow();
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
		// Layout/peer-state repair above is sufficient: RocketFigure3d.renderFrame() re-marks
		// itself dirty on every early return until hasCompletedFrame() is true, so the
		// background thread will retry on the very next scheduler tick.

		if (delayMs == STARTUP_FRAME_RECOVERY_DELAYS_MS[STARTUP_FRAME_RECOVERY_DELAYS_MS.length - 1]
				&& !hasCompletedFrame()) {
			log.warn("No completed 3D frame after startup recovery: {}", getDebugStateSummary());
		}
	}

	private void syncPeerBounds() {
		if (!isDisplayable() || !isShowing()) {
			return;
		}
		if (!peerBoundsSyncInProgress.compareAndSet(false, true)) {
			return;
		}
		try {
			int x = getX();
			int y = getY();
			int width = getWidth();
			int height = getHeight();
			if (width <= 0 || height <= 0) {
				return;
			}
			// Nudge the peer size by 1px and back to force native bounds recomputation.
			// On macOS, a real resize often "snaps" the canvas back to the correct location.
			setBounds(x, y, width, height + 1);
			setBounds(x, y, width, height);
			Container parent = getParent();
			if (parent != null) {
				parent.validate();
			}
		} finally {
			peerBoundsSyncInProgress.set(false);
		}
	}

	private boolean isPeerMispositioned() {
		Point expected = computeExpectedLocationOnScreen();
		Point actual = null;
		try {
			if (isShowing()) {
				actual = getLocationOnScreen();
			}
		} catch (Exception ignored) {
			// Ignore and treat as mispositioned to allow retries.
		}
		if (expected == null || actual == null) {
			return true;
		}
		int dx = expected.x - actual.x;
		int dy = expected.y - actual.y;
		return Math.abs(dx) > 2 || Math.abs(dy) > 2;
	}

	private Point computeExpectedLocationOnScreen() {
		Container parent = getParent();
		if (parent == null || !parent.isShowing()) {
			return null;
		}
		Point parentOnScreen;
		try {
			parentOnScreen = parent.getLocationOnScreen();
		} catch (Exception ignored) {
			return null;
		}
		Point local = getLocation();
		return new Point(parentOnScreen.x + local.x, parentOnScreen.y + local.y);
	}

	private void addInputListeners() {
		if (scene3DOrchestrator == null) return;

		MouseAdapter mouseAdapter = new MouseAdapter() {
			private Point pressPoint;
			private Point lastPoint;
			private boolean isDragging;

			@Override
			public void mousePressed(MouseEvent e) {
				markRenderActivity();
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Track modifier state for multi-selection.
					scene3DOrchestrator.getInputHandler().getInputState().isShiftPressed = e.isShiftDown() || e.isMetaDown();
					// Use Swing's built-in click count to detect double-clicks.
					if (e.getClickCount() == 2) {
						scene3DOrchestrator.getInputHandler().getInputState().doubleClickPoint.set(e.getPoint());
					}
					pressPoint = e.getPoint();
					lastPoint = e.getPoint();
					isDragging = false;
					cameraIsMoving = true; // Start tracking camera movement
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				markRenderActivity();
				if (SwingUtilities.isLeftMouseButton(e)) {
					// We check for !isDragging to differentiate a click from a drag-release.
					// A double-click will also fire this event for the second click.
					if (!isDragging && pressPoint != null) {
						// Update modifier state and capture the click event for selection listeners.
						var inputState = scene3DOrchestrator.getInputHandler().getInputState();
						inputState.isShiftPressed = e.isShiftDown() || e.isMetaDown();
						scene3DOrchestrator.getInputHandler().getInputState().clickPoint.set(pressPoint);
						pendingSelectionClickEvent.set(e);
					}
					pressPoint = null;
					isDragging = false;
					cameraIsMoving = false; // Stop tracking camera movement
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				markRenderActivity();
				if (SwingUtilities.isLeftMouseButton(e) && pressPoint != null) {
					if (!isDragging && pressPoint.distanceSq(e.getPoint()) > CLICK_DRAG_THRESHOLD_SQ) {
						isDragging = true;
						scene3DOrchestrator.getInputHandler().getInputState().dragJustStarted = true;
					}

					if (isDragging) {
						// Check for modifier keys
						boolean isCtrlDown = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
						boolean isAltDown = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;

						// Update input state based on modifiers
						var inputState = scene3DOrchestrator.getInputHandler().getInputState();
						inputState.isPanning = panModeEnabled || isCtrlDown;
						inputState.isLightDragging = isAltDown;

						// Always update the drag delta
						float deltaX = e.getX() - lastPoint.x;
						float deltaY = e.getY() - lastPoint.y;
						inputState.addDrag(deltaX, deltaY);
					}
					lastPoint = e.getPoint();
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				markRenderActivity();
				scene3DOrchestrator.getInputHandler().getInputState().addScroll(e.getWheelRotation() * -1.0f, e.getX(), e.getY());
				hudNeedsUpdate = true; // Mark HUD for update on zoom
			}
		};
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

	private static GLData createGLData() {
		GLData data = new GLData();
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.profile = GLData.Profile.CORE;
		data.doubleBuffer = true;
		// Default-framebuffer MSAA is disabled — see getRequestedSampleCount().
		data.samples = getRequestedSampleCount();
		data.sRGB = true;
		// Disable swap interval to avoid vsync stalls when multiple canvases share a thread.
		data.swapInterval = 0;
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
		// All rendering is done to intermediate FBOs; the default framebuffer's MSAA sample
		// count has no effect on output quality (we use post-processing AA via FXAA).
		// Requesting samples > 0 causes hard context-creation failures on Windows when
		// WGL_ARB_multisample / WGL_EXT_multisample is unavailable (some drivers, VMs).
		return 0;
	}

	/**
	 * Computes framebuffer size without calling AWT native peer methods.
	 * On macOS, querying AWTGLCanvas.getFramebufferWidth/Height off the EDT can crash.
	 */
	private int[] computeFramebufferSize(int windowWidth, int windowHeight) {
		double scaleX = 1.0;
		double scaleY = 1.0;
		GraphicsConfiguration gc = getGraphicsConfiguration();
		if (gc != null) {
			AffineTransform tx = gc.getDefaultTransform();
			scaleX = tx.getScaleX();
			scaleY = tx.getScaleY();
		}
		int fbWidth = (int) Math.round(windowWidth * scaleX);
		int fbHeight = (int) Math.round(windowHeight * scaleY);
		return new int[]{Math.max(1, fbWidth), Math.max(1, fbHeight)};
	}

	private int[] computeFramebufferSize() {
		return computeFramebufferSize(Math.max(1, getWidth()), Math.max(1, getHeight()));
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
		INIT_SEMAPHORE.acquireUninterruptibly();
		try {
			glCapabilities = GL.createCapabilities();
			GLDebug.enableIfRequested("AWT-canvas");
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
			glEnable(GL_FRAMEBUFFER_SRGB);

			int winWidth = Math.max(1, getWidth());
			int winHeight = Math.max(1, getHeight());
			int[] fbSize = computeFramebufferSize(winWidth, winHeight);
			int fbWidth = fbSize[0];
			int fbHeight = fbSize[1];

			scene3DOrchestrator = Scene3DOrchestrator.builder(rocket, winWidth, winHeight, fbWidth, fbHeight)
					.build();
			SceneView scene = scene3DOrchestrator.getScene();

			// Create the scene mesh
			RocketMeshBuilder.buildRocketMesh(scene, rocket, scene3DOrchestrator.getRenderingConfiguration());
			scene3DOrchestrator.applyRocketRotationToScene();
			//RocketMeshBuilder.createOriginAxes(scene, true, true);
			applyThemeBackground(scene);
			installThemeListener();

			// Focus on the rocket
			scene3DOrchestrator.focusOnRocket();
			Consumer<Scene3DOrchestrator> initHook = initializationHook;
			if (initHook != null) {
				initHook.accept(scene3DOrchestrator);
			}
			ImageCaptureRequest pendingCapture = imageCaptureRequest.get();
			if (pendingCapture != null) {
				scene3DOrchestrator.requestExport(pendingCapture.transparent);
			}

			if (this.hudPanel != null) {
				this.hudPanel.setSceneViewController(this.scene3DOrchestrator);
				this.hudPanel.setGLScenePanel(this);
			}

			if (hudEnabled) {
				// --- Initialize HUD rendering objects ---
				hudShader = new Shader("/shaders/ui/hud_vertex.glsl", "/shaders/ui/hud_fragment.glsl");

				// Set initial dimensions
				lastFramebufferWidth = fbWidth;
				lastFramebufferHeight = fbHeight;

				initHudTexture();
				initHudVao();
				requestHudRepaint(true);
			} else {
				hudNeedsUpdate = false;
			}

			addInputListeners();
			DemoFactory.setupDemoKeyboardHandling(this.keyboardHandler, scene3DOrchestrator.getScene(), scene3DOrchestrator);

			// Mark initialization complete - allows resize/render operations to proceed
			glInitialized = true;
			glInitLatch.countDown();
		} catch (Exception e) {
			glInitFailed = true;
			glInitLatch.countDown();
			throw new RuntimeException("Failed to initialize renderer", e);
		} finally {
			INIT_SEMAPHORE.release();
		}
	}

	@Override
	public void render() {
		int count = renderCallCount.incrementAndGet();
		if (glInitFailed) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
		int paintBefore = paintCallCount.get();
		int swapBefore = swapCallCount.get();
		try {
			if (NEEDS_PEER_BOUNDS_SYNC_WORKAROUND) {
				RENDER_LOCK.lock();
			}
			// Delegate to AWTGLCanvas to make the context current and handle buffer swapping.
			super.render();
			if (peerBoundsSyncEnabled && count == 1) {
				// The native NSOpenGLView is created during the first render; schedule another bounds sync
				// afterwards to catch late layout adjustments in Swing.
				requestPeerBoundsSyncNow();
			}
			int paintAfter = paintCallCount.get();
			int swapAfter = swapCallCount.get();
			if (paintAfter == paintBefore && swapAfter == swapBefore) {
				if (peerBoundsSyncEnabled) {
					requestPeerBoundsSyncNow();
				}
				SwingUtilities.invokeLater(() -> {
					revalidate();
					repaint();
				});
			}
		} catch (Throwable t) {
			glInitFailed = true;
			String msg = t.getMessage();
			if ((t instanceof IllegalStateException || t instanceof RuntimeException)
					&& msg != null && (msg.contains("GLX") || msg.contains("glX"))) {
				log.error("3D view disabled: OpenGL/GLX initialization failed. " +
						"On Wayland systems this requires XWayland to be running — " +
						"ensure DISPLAY is set (e.g. DISPLAY=:0) or use a session that " +
						"provides XWayland. Cause: {}", msg);
			} else {
				log.error("3D rendering failed: {}: {}", t.getClass().getSimpleName(), msg, t);
			}
		} finally {
			if (NEEDS_PEER_BOUNDS_SYNC_WORKAROUND) {
				RENDER_LOCK.unlock();
			}
		}
	}

	/**
	 * Cleans up old resources and creates new ones with current framebuffer dimensions.
	 * IMPORTANT: Creates textures directly for THIS context - does not use shared pool.
	 */
	private void initHudTexture() {
		synchronized (hudLock) {
			hudBufferReady.set(false);
			int[] fbSize = computeFramebufferSize();
			int fbWidth = fbSize[0];
			int fbHeight = fbSize[1];

			if (fbWidth <= 0 || fbHeight <= 0) {
				cleanupHudResources();
				return;
			}

			// Check if we can reuse existing resources with padding
			if (hudImage != null && hudImageBuffer != null) {
				int currentCapacity = hudImageBuffer.capacity();
				int requiredCapacity = fbWidth * fbHeight * 4;

				if (currentCapacity >= requiredCapacity && currentCapacity <= requiredCapacity * 1.25) {
					// Cleanup old texture and create new one for THIS context
					if (hudTexture != null) {
						hudTexture.cleanup();
					}
					hudTexture = new Texture(fbWidth, fbHeight, true);

					// Recreate BufferedImage with new size but keep buffer
					if (hudGraphics != null) {
						hudGraphics.dispose();
					}
					hudImage = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
					hudGraphics = hudImage.createGraphics();
					hudGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					hudGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					return;
				}
			}

			// Full cleanup and recreation needed
			cleanupHudResources();

			// Allocate with some padding to reduce future reallocations
			int paddedSize = (int) (fbWidth * fbHeight * 4 * 1.1);    // 10% padding

			hudImage = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
			hudImageBuffer = MemoryUtil.memAlloc(paddedSize);
			hudIntBuffer = hudImageBuffer.asIntBuffer();

			hudGraphics = hudImage.createGraphics();
			hudGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			hudGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// Create texture directly for THIS context - don't use shared pool
			hudTexture = new Texture(fbWidth, fbHeight, true);
		}
	}

	private void cleanupHudResources() {
		if (hudTexture != null) {
			// Cleanup directly - don't return to shared pool
			hudTexture.cleanup();
			hudTexture = null;
		}
		if (hudImageBuffer != null) {
			MemoryUtil.memFree(hudImageBuffer);
			hudImageBuffer = null;
			hudIntBuffer = null;
		}
		if (hudGraphics != null) {
			hudGraphics.dispose();
			hudGraphics = null;
		}
		hudImage = null;
	}

	private void initHudVao() {
		float[] quadVertices = {
				// positions   // texCoords (V coordinates are flipped)
				-1.0f, 1.0f, 0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				-1.0f, -1.0f, 0.0f, 1.0f,  // Bottom-left vertex maps to V=1.0
				1.0f, -1.0f, 1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0

				-1.0f, 1.0f, 0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				1.0f, -1.0f, 1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0
				1.0f, 1.0f, 1.0f, 0.0f   // Top-right vertex maps to V=0.0
		};
		hudVao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, hudVao, "HUD vao");
		hudVbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, hudVbo, "HUD vbo");
		glBindVertexArray(hudVao);
		glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
		glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		glBindVertexArray(0);
	}

	@Override
	public void paintGL() {
		paintCallCount.incrementAndGet();
		if (!glInitialized || !isDisplayable()) {
			return;
		}

		boolean shouldSwap = false;
		try {
			if (scene3DOrchestrator == null) {
				return;
			}

			handlePendingResize();

			Renderer renderer = scene3DOrchestrator.getRenderer();
			SceneView sceneView = scene3DOrchestrator.getScene();

			if (renderer == null || sceneView == null) {
				return;
			}

			// --- 3D Scene Rendering & Export Logic ---
			handleKeyboardEvents();
			int[] fbSize = computeFramebufferSize();
			glViewport(0, 0, fbSize[0], fbSize[1]);
			scene3DOrchestrator.update();

			if (cameraIsMoving) {
				hudNeedsUpdate = true;
			}

			// Check for an export request before the main render
			if (scene3DOrchestrator.isExportRequested()) {
				handleExport(sceneView, renderer);
				shouldSwap = true;
				return;
			}

			// --- Main Display Rendering (if not exporting) --
			renderer.render(sceneView, null, true);
			renderer.presentResolvedToCurrentFramebuffer();

			// --- 2D HUD Rendering - only update texture if needed ---
			if (hudEnabled) {
				boolean hudPanelNeedsRepaint = hudPanel != null && hudPanel.needsRepaint();
				boolean hudRepaintRequested = false;
				if (hudNeedsUpdate || hudPanelNeedsRepaint) {
					hudRepaintRequested = requestHudRepaint(hudPanelNeedsRepaint);
				}
				uploadHudTextureIfReady();
				if (hudRepaintRequested) {
					hudNeedsUpdate = false;
				}

				if (hudTexture != null && hudShader != null) {
					// Set GL state for 2D rendering
					glDisable(GL_DEPTH_TEST);
					glEnable(GL_BLEND);
					glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

					hudShader.use();

					glActiveTexture(GL_TEXTURE0);
					glBindTexture(GL_TEXTURE_2D, hudTexture.getId());

					glBindVertexArray(hudVao);
					glDrawArrays(GL_TRIANGLES, 0, 6);
					glBindVertexArray(0);

					// Restore GL state
					glEnable(GL_DEPTH_TEST);
					glDisable(GL_BLEND);
				}
				// HUD rendering binds textures directly; reset cached state before next 3D frame.
				renderer.resetTextureState();
			}
			shouldSwap = true;
		} catch (Exception ex) {
			log.error("Error during paintGL", ex);
		} finally {
			if (shouldSwap) {
				swapCallCount.incrementAndGet();
				swapBuffers();
			}
		}
	}

	private void handleExport(SceneView sceneView, Renderer renderer) {
		boolean renderBackground = !scene3DOrchestrator.isExportTransparent();
		renderer.render(sceneView, null, renderBackground);

		int exportWidth = renderer.getRenderWidth();
		int exportHeight = renderer.getRenderHeight();

		ByteBuffer exportBuffer = captureResolvedFramebuffer(renderer, exportWidth, exportHeight);
		scene3DOrchestrator.clearExportRequest();

		renderer.presentResolvedToCurrentFramebuffer();

		if (exportBuffer == null) {
			log.warn("Export skipped: no framebuffer data available");
			return;
		}

		ImageCaptureRequest captureRequest = imageCaptureRequest.getAndSet(null);
		if (captureRequest != null) {
			BufferedImage image = bufferToImage(exportBuffer, exportWidth, exportHeight);
			SwingUtilities.invokeLater(() -> captureRequest.callback.accept(image));
			return;
		}

		String filePath = "export_" + System.currentTimeMillis() + ".png";
		EXPORT_EXECUTOR.submit(() -> writePng(exportBuffer, exportWidth, exportHeight, filePath));
	}

	private boolean requestHudRepaint(boolean highPriority) {
		if (hudPanel == null || hudGraphics == null) {
			return false;
		}
		if (!highPriority) {
			long now = System.currentTimeMillis();
			if (now - lastHudPaintTimeMs < MIN_HUD_PAINT_INTERVAL_MS) {
				return false;
			}
		}
		if (!hudPaintScheduled.compareAndSet(false, true)) {
			return true;
		}
		SwingUtilities.invokeLater(() -> {
			try {
				paintHudOnEdt();
			} finally {
				hudPaintScheduled.set(false);
			}
		});
		return true;
	}

	private void paintHudOnEdt() {
		synchronized (hudLock) {
			if (hudImage == null || hudPanel == null || hudGraphics == null || hudImageBuffer == null || hudIntBuffer == null) {
				return;
			}

			hudBufferReady.set(false);

			// The panel works in logical window coordinates
			int windowWidth = Math.max(1, getWidth());
			int windowHeight = Math.max(1, getHeight());
			if (windowHeight == 0) {
				return;
			}
			int[] fbSize = computeFramebufferSize(windowWidth, windowHeight);
			double dpiScale = (double) fbSize[1] / (double) windowHeight;

			hudGraphics.setTransform(hudGraphics.getDeviceConfiguration().getDefaultTransform());
			hudGraphics.scale(dpiScale, dpiScale);

			// Clear with transparent background
			hudGraphics.setBackground(new Color(0, 0, 0, 0));
			hudGraphics.clearRect(0, 0, windowWidth, windowHeight);

			hudPanel.setBounds(0, 0, windowWidth, windowHeight);
			hudPanel.paint(hudGraphics);

			final int[] pixels = ((DataBufferInt) hudImage.getRaster().getDataBuffer()).getData();
			hudIntBuffer.clear();
			hudIntBuffer.put(pixels);
			hudImageBuffer.rewind();
			lastHudPaintTimeMs = System.currentTimeMillis();
			hudBufferReady.set(true);
		}
	}

	private void uploadHudTextureIfReady() {
		if (hudTexture == null || hudShader == null) {
			return;
		}
		if (!hudBufferReady.get()) {
			return;
		}
		synchronized (hudLock) {
			if (!hudBufferReady.get() || hudTexture == null || hudImage == null || hudImageBuffer == null) {
				return;
			}
			hudBufferReady.set(false);

			glBindTexture(GL_TEXTURE_2D, hudTexture.getId());
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, hudImage.getWidth(), hudImage.getHeight(),
					GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, hudImageBuffer);
		}
	}

	private void handlePendingResize() {
		if (!glInitialized || scene3DOrchestrator == null) {
			return;
		}

		int width = pendingWinWidth;
		int height = pendingWinHeight;
		int fbWidth = pendingFbWidth;
		int fbHeight = pendingFbHeight;

		if (!resizeRequested) {
			int currentWidth = Math.max(1, getWidth());
			int currentHeight = Math.max(1, getHeight());
			int[] fbSize = computeFramebufferSize(currentWidth, currentHeight);
			int currentFbWidth = fbSize[0];
			int currentFbHeight = fbSize[1];
			ViewportDimensions viewport = scene3DOrchestrator.getViewport();
			if (viewport.getWindowWidth() == currentWidth
					&& viewport.getWindowHeight() == currentHeight
					&& viewport.getFramebufferWidth() == currentFbWidth
					&& viewport.getFramebufferHeight() == currentFbHeight) {
				return;
			}
			width = currentWidth;
			height = currentHeight;
			fbWidth = currentFbWidth;
			fbHeight = currentFbHeight;
		}

		// If the framebuffer size wasn't captured on resize (EDT), query it now while the context is current
		if (fbWidth <= 0 || fbHeight <= 0) {
			int[] fbSize = computeFramebufferSize(width, height);
			fbWidth = fbSize[0];
			fbHeight = fbSize[1];
		}

		glViewport(0, 0, fbWidth, fbHeight);
		scene3DOrchestrator.resize(width, height, fbWidth, fbHeight);

		if (hudEnabled && (fbWidth != lastFramebufferWidth || fbHeight != lastFramebufferHeight)) {
			initHudTexture();
			lastFramebufferWidth = fbWidth;
			lastFramebufferHeight = fbHeight;
			hudNeedsUpdate = true;
			requestHudRepaint(true);
		}

		resizeRequested = false;
	}

	private ByteBuffer captureResolvedFramebuffer(Renderer renderer, int width, int height) {
		if (width <= 0 || height <= 0) {
			return null;
		}

		int framebufferId = renderer.getResolvedFramebufferId();
		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int previousReadBuffer = glGetInteger(GL_READ_BUFFER);

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		glReadBuffer(framebufferId == 0 ? GL_FRONT : GL_COLOR_ATTACHMENT0);

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
		glReadBuffer(previousReadBuffer);

		buffer.rewind();
		return buffer;
	}

	private BufferedImage bufferToImage(ByteBuffer buffer, int width, int height) {
		buffer.rewind();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = (x + (width * y)) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				image.setRGB(x, height - 1 - y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return image;
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
		if (hudEnabled) {
			hudNeedsUpdate = true;
		}
	}

	public void requestImageCapture(boolean transparent, Consumer<BufferedImage> callback) {
		if (callback == null) {
			return;
		}
		imageCaptureRequest.set(new ImageCaptureRequest(transparent, callback));
		Scene3DOrchestrator orchestrator = scene3DOrchestrator;
		if (orchestrator != null) {
			orchestrator.requestExport(transparent);
		}
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
		// Prevent any further rendering/resize operations
		glInitialized = false;
		startupRecoveryGeneration.incrementAndGet();
		hudPaintScheduled.set(false);
		hudBufferReady.set(false);
		uninstallThemeListener();
		if (scene3DOrchestrator != null) {
			scene3DOrchestrator.shutdown();
		}

		boolean canAttemptContextCleanup = isDisplayable() && getWidth() > 0 && getHeight() > 0;
		if (canAttemptContextCleanup) {
			// Try to clean up GL resources within context.
			// This path can crash on macOS if called after peer teardown has started.
			try {
				runInContext(() -> {
					if (scene3DOrchestrator != null) {
						try {
							SceneView scene = scene3DOrchestrator.getScene();
							Renderer renderer = scene3DOrchestrator.getRenderer();
							if (scene != null) {
								scene.cleanup();
							}
							if (renderer != null) {
								renderer.cleanup();
							}
						} catch (Exception e) {
							log.warn("Error cleaning 3D resources: {}", e.getMessage());
						}
					}
					if (hudVao != 0) {
						GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, hudVao);
						glDeleteVertexArrays(hudVao);
						hudVao = 0;
					}
					if (hudVbo != 0) {
						GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, hudVbo);
						glDeleteBuffers(hudVbo);
						hudVbo = 0;
					}
					if (hudShader != null) {
						hudShader.cleanup();
						hudShader = null;
					}
					if (hudTexture != null) {
						hudTexture.cleanup();
						hudTexture = null;
					}
				});
			} catch (Exception e) {
				log.warn("GL context unavailable during cleanup, some GL resources may leak: {}", e.getMessage());
			}
			// Do not call disposeCanvas() here. Swing/AWTGLCanvas will tear down the native
			// drawing surface during removeNotify(), and forcing disposal earlier can double-free
			// the JAWT surface on macOS during window close.
		} else {
			log.debug("Skipping runInContext cleanup for non-displayable canvas");
		}

		// Always free native memory - this doesn't require GL context
		synchronized (hudLock) {
			if (hudImageBuffer != null) {
				MemoryUtil.memFree(hudImageBuffer);
				hudImageBuffer = null;
				hudIntBuffer = null;
			}
			if (hudGraphics != null) {
				hudGraphics.dispose();
				hudGraphics = null;
			}
			hudImage = null;
			hudBufferReady.set(false);
			hudTexture = null;
			hudShader = null;
		}
		scene3DOrchestrator = null;
		glCapabilities = null;

		GpuResourceTracker.logLiveResources("Swing canvas cleanup (other canvases may still be active)", false);
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
		return paintCallCount.get() > 0 || swapCallCount.get() > 0;
	}

	public boolean isPeerMispositionedForDebug() {
		return peerBoundsSyncEnabled && isShowing() && isPeerMispositioned();
	}

	public String getDebugStateSummary() {
		return "displayable=" + isDisplayable()
				+ ", showing=" + isShowing()
				+ ", visible=" + isVisible()
				+ ", size=" + getWidth() + "x" + getHeight()
				+ ", glInitialized=" + glInitialized
				+ ", glInitFailed=" + glInitFailed
				+ ", renderCalls=" + renderCallCount.get()
				+ ", paintCalls=" + paintCallCount.get()
				+ ", swapCalls=" + swapCallCount.get()
				+ ", peerMispositioned=" + (peerBoundsSyncEnabled && isShowing() ? isPeerMispositioned() : false);
	}

	private void applyThemeBackground(SceneView scene) {
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
		uiThemeListener = () -> {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			orchestrator.enqueueGlTask(() -> applyThemeBackground(orchestrator.getScene()));
		};
		UITheme.Theme.addUIThemeChangeListener(uiThemeListener);
	}

	private void uninstallThemeListener() {
		if (uiThemeListener != null) {
			UITheme.Theme.removeUIThemeChangeListener(uiThemeListener);
			uiThemeListener = null;
		}
	}
}
