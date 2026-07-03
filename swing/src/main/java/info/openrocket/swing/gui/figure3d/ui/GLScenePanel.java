package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.swing.gui.figure3d.DemoFactory;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.input.KeyboardHandler;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderer;
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.geom.AffineTransform;
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
import java.awt.image.BufferStrategy;
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
import static org.lwjgl.opengl.GL11.GL_BACK;
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
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;
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
	private static final boolean SKIP_DEFAULT_FRAMEBUFFER_FLUSH_ON_MACOS =
			SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS;
	// On macOS the NSOpenGLView layer cannot present frames: its display path runs
	// -[NSOpenGLContext setView:] which aborts in AppKit's main-thread check when
	// triggered from the render thread (via glFlush) or from the EDT's CoreAnimation
	// commits. Instead of presenting through the layer, read the finished frame back
	// from the offscreen FBO and paint it with Java2D, and keep the native GL layer
	// hidden so AppKit never displays it.
	private static final boolean PRESENT_VIA_IMAGE_ON_MACOS = SKIP_DEFAULT_FRAMEBUFFER_FLUSH_ON_MACOS;
	// The peer-bounds nudge workaround repositioned the native GL layer after
	// layout changes. With image presentation the layer is permanently hidden and
	// AWT draws the frame itself, so the nudges (and the resize churn plus flicker
	// they cause during view switches) are no longer needed.
	private static final boolean NEEDS_PEER_BOUNDS_SYNC_WORKAROUND =
			SKIP_DEFAULT_FRAMEBUFFER_FLUSH_ON_MACOS && !PRESENT_VIA_IMAGE_ON_MACOS;
	private final AtomicInteger renderCallCount = new AtomicInteger(0);
	private final AtomicInteger paintCallCount = new AtomicInteger(0);
	private final AtomicInteger swapCallCount = new AtomicInteger(0);

	// Image-presentation state (macOS): the render thread reads the resolved FBO into
	// a staging image and publishes it here; the EDT paints it in paint(). Two images
	// are ping-ponged so the EDT never draws one that is being overwritten.
	private volatile BufferedImage presentedImage;
	private BufferedImage presentedImageA;
	private BufferedImage presentedImageB;
	private ByteBuffer presentReadbackBuffer;
	private volatile boolean presentedImageHasContent = false;
	private volatile boolean nativeGLLayerHidden = false;
	// The strategy's surface outlives the canvas being hidden (macOS heavyweights
	// share the window surface), so it is disposed on hide and recreated on the
	// next presented frame; see handleShowingChanged().
	private volatile BufferStrategy presentStrategy;

	private Scene3DOrchestrator scene3DOrchestrator;
	private final KeyboardHandler keyboardHandler;

	private static final double CLICK_DRAG_THRESHOLD_SQ = 5 * 5;

	private final HUDPanel hudPanel;
	private final boolean hudEnabled;
	private final Object hudLock = new Object();
	private BufferedImage hudImage;
	private Texture hudTexture;
	private GLShader hudShader;
	private int hudVao;
	private int hudVbo; // Store VBO reference for cleanup
	// PBO for async HUD texture uploads. Orphaned + filled on the render thread,
	// then glTexSubImage2D copies from PBO → texture asynchronously instead of
	// stalling on a host-pointer DMA.
	private int hudPbo;
	private int hudPboCapacityBytes;
	private ByteBuffer hudImageBuffer;
	private IntBuffer hudIntBuffer; // Direct IntBuffer view for efficiency
	private Graphics2D hudGraphics; // Reusable Graphics2D context
	private final CountDownLatch glInitLatch = new CountDownLatch(1);
	private final AtomicBoolean hudPaintScheduled = new AtomicBoolean(false);
	private final AtomicBoolean hudBufferReady = new AtomicBoolean(false);
	private volatile long lastHudPaintTimeMs = 0;
	// Default throttle for low-priority HUD repaints. The HUD upload path is the
	// dominant render-thread cost; 15 fps is visually ample for HUD content.
	private static final long MIN_HUD_PAINT_INTERVAL_MS = 66;
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
	private volatile boolean suppressInternalResizeEvents = false;

	// Initialization guard - prevents resize/render operations before GL is fully ready
	private volatile boolean glInitialized = false;
	public volatile boolean glInitFailed = false;
	private volatile boolean visibleFrameRecoveryPending = true;
	// Swap count when the current recovery began; used to tell "no frame completes
	// at all" (escalate to rebuild) apart from "frames complete but look blank"
	// (handled by the framebuffer probes).
	private volatile int recoveryBaselineSwapCount = 0;
	private static final Semaphore INIT_SEMAPHORE = new Semaphore(1, true);
	private static final ReentrantLock RENDER_LOCK = new ReentrantLock(true);
	// How long cleanup() waits for an in-flight render before giving up on
	// in-context GL resource cleanup (leaking is better than a native crash).
	private static final long CLEANUP_RENDER_LOCK_TIMEOUT_MS = 2000;
	// LWJGL capabilities are thread-local; store per-canvas so we can render multiple canvases on one thread.
	private volatile GLCapabilities glCapabilities;

	private final Rocket rocket;
	private final boolean peerBoundsSyncEnabled;
	private static final ExecutorService EXPORT_EXECUTOR;
	// Captures the AWT mouse event that triggered the most recent click-based selection update.
	private final AtomicReference<MouseEvent> pendingSelectionClickEvent = new AtomicReference<>();
	private volatile Runnable renderActivityCallback;
	private volatile Runnable renderRequestCallback;
	private volatile Runnable uiThemeListener;
	private final AtomicReference<ImageCaptureRequest> imageCaptureRequest = new AtomicReference<>();
	private volatile Consumer<Scene3DOrchestrator> initializationHook;
	private volatile boolean panModeEnabled = false;
	private final AtomicBoolean fatalRenderExceptionReported = new AtomicBoolean(false);
	private final AtomicBoolean startupBlankFramebufferRecoveryRequested = new AtomicBoolean(false);
	private volatile int startupBlankFramebufferFrames = 0;
	// Once startup visibility is confirmed, the per-frame glReadPixels probes are
	// disabled — each readback stalls the GPU pipeline and they only exist to
	// drive a one-shot recovery callback.
	private volatile boolean startupFrameDetectionComplete = false;
	private volatile int consecutiveVisibleStartupFrames = 0;
	private static final int STARTUP_VISIBILITY_CONFIRM_FRAMES = 2;
	private volatile Runnable blankDefaultFramebufferCallback;
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
		// With image presentation the frame is painted by AWT via paint(), so
		// repaint events must be processed rather than ignored.
		setIgnoreRepaint(!PRESENT_VIA_IMAGE_ON_MACOS);

		addHierarchyListener(e -> {
			long changeFlags = e.getChangeFlags();
			if ((changeFlags & (HierarchyEvent.PARENT_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED
					| HierarchyEvent.SHOWING_CHANGED)) != 0) {
				updateAncestorWindowListener();
			}
			if ((changeFlags & HierarchyEvent.SHOWING_CHANGED) != 0) {
				handleShowingChanged();
				// lwjgl3-awt's own hierarchy listener un-hides the native GL layer on
				// every showing change; re-hide it afterwards (invokeLater runs after
				// all hierarchy listeners of this event have executed).
				if (PRESENT_VIA_IMAGE_ON_MACOS && nativeGLLayerHidden) {
					SwingUtilities.invokeLater(this::hideNativeGLLayer);
				}
			}
		});

		if (peerBoundsSyncEnabled) {
			// CardLayout/JSplitPane switches can change the on-screen position of heavyweight
			// components without changing their local bounds. On macOS this can leave the
			// native peer at an incorrect location until the next real resize. Force a peer
			// bounds sync when the canvas becomes showing.
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
				if (suppressInternalResizeEvents) {
					return;
				}
				int width = Math.max(1, getWidth());
				int height = Math.max(1, getHeight());
				int[] fbSize = computeFramebufferSize(width, height);
				if (width == pendingWinWidth && height == pendingWinHeight
						&& fbSize[0] == pendingFbWidth && fbSize[1] == pendingFbHeight
						&& resizeRequested) {
					return;
				}
				markRenderActivity();
				pendingWinWidth = width;
				pendingWinHeight = height;
				pendingFbWidth = fbSize[0];
				pendingFbHeight = fbSize[1];
				resizeRequested = true;
				hudNeedsUpdate = true;
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
	 * Registers a callback that is invoked when the user interacts with the canvas (mouse/keyboard/resize).
	 * Used by the macOS render scheduler to prioritize recently active 3D views.
	 */
	public void setRenderActivityCallback(Runnable callback) {
		this.renderActivityCallback = callback;
	}

	public void setRenderRequestCallback(Runnable callback) {
		this.renderRequestCallback = callback;
	}

	// Off by default — when enabled, the renderer skips AO / motion blur /
	// outline passes during drag-rotate / pan / zoom for a fps boost at the
	// cost of a visible quality drop. Toggleable via system property:
	//   -Dopenrocket.figure3d.skipPostFxDuringInteraction=true
	private static final boolean SKIP_POSTFX_DURING_INTERACTION =
			Boolean.getBoolean("openrocket.figure3d.skipPostFxDuringInteraction");

	private void setCameraIsMoving(boolean moving) {
		cameraIsMoving = moving;
		if (!SKIP_POSTFX_DURING_INTERACTION) {
			return;
		}
		Scene3DOrchestrator orchestrator = scene3DOrchestrator;
		if (orchestrator != null) {
			GLRenderer renderer = orchestrator.getRenderer();
			if (renderer != null) {
				renderer.setInteractionMode(moving);
			}
		}
	}

	public void setBlankDefaultFramebufferCallback(Runnable callback) {
		this.blankDefaultFramebufferCallback = callback;
	}

	public void setGlInitFailureCallback(Runnable callback) {
		this.glInitFailureCallback = callback;
	}

	private void markRenderActivity() {
		Runnable callback = renderActivityCallback;
		if (callback != null) {
			callback.run();
		}
	}

	private void beginVisibleFrameRecovery() {
		visibleFrameRecoveryPending = true;
		recoveryBaselineSwapCount = swapCallCount.get();
		startupBlankFramebufferFrames = 0;
		startupBlankFramebufferRecoveryRequested.set(false);
		startupFrameDetectionComplete = false;
		consecutiveVisibleStartupFrames = 0;
		hudNeedsUpdate = true;
		markRenderActivity();
		if (peerBoundsSyncEnabled) {
			requestPeerBoundsSyncNow();
		}
		revalidate();
		repaint();
		scheduleStartupFrameRecoverySequence();
	}

	private void handleWindowDeiconified() {
		if (!isDisplayable()) {
			return;
		}
		peerBoundsSyncAttempts.set(0);
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
		peerBoundsSyncAttempts.set(0);
		if (!isShowing()) {
			startupRecoveryGeneration.incrementAndGet();
			if (PRESENT_VIA_IMAGE_ON_MACOS) {
				// The last presented frame otherwise stays on screen over the view
				// that replaced this canvas (e.g. the 2D card): release the strategy
				// surface and repaint the visible ancestor so the region is redrawn.
				BufferStrategy strategy = presentStrategy;
				presentStrategy = null;
				if (strategy != null) {
					strategy.dispose();
				}
				repaintShowingAncestor();
				Timer repairTimer = new Timer(100, evt -> {
					if (!isShowing()) {
						repaintShowingAncestor();
					}
				});
				repairTimer.setRepeats(false);
				repairTimer.start();
			}
			return;
		}

		beginVisibleFrameRecovery();
	}

	private void repaintShowingAncestor() {
		Container ancestor = getParent();
		while (ancestor != null && !ancestor.isShowing()) {
			ancestor = ancestor.getParent();
		}
		if (ancestor != null) {
			ancestor.repaint();
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();
		updateAncestorWindowListener();
		visibleFrameRecoveryPending = true;
		recoveryBaselineSwapCount = swapCallCount.get();
		startupBlankFramebufferFrames = 0;
		startupBlankFramebufferRecoveryRequested.set(false);
		startupFrameDetectionComplete = false;
		consecutiveVisibleStartupFrames = 0;
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

	@Override
	public void removeNotify() {
		detachAncestorWindowListener();
		// super.removeNotify() disposes the native JAWT drawing surface; that must not
		// overlap a render thread that is mid-frame on the same surface. Bounded wait:
		// the peer teardown cannot be skipped, so proceed either way after the timeout.
		boolean renderLockAcquired = false;
		try {
			renderLockAcquired = RENDER_LOCK.tryLock(CLEANUP_RENDER_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		try {
			super.removeNotify();
		} finally {
			if (renderLockAcquired) {
				RENDER_LOCK.unlock();
			}
		}
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
		if (generation != startupRecoveryGeneration.get() || glInitFailed || !visibleFrameRecoveryPending) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		requestPeerBoundsSyncNow();
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
				&& visibleFrameRecoveryPending) {
			log.warn("No completed 3D frame after startup recovery: {}", getDebugStateSummary());
			// Repaint nudges did not produce any completed frame within the full
			// recovery window. Escalate to a canvas rebuild — the same remedy that
			// works when the user toggles the view type by hand. Frames that complete
			// but look blank are left to the framebuffer probes instead.
			Runnable callback = blankDefaultFramebufferCallback;
			if (swapCallCount.get() == recoveryBaselineSwapCount
					&& callback != null && startupBlankFramebufferRecoveryRequested.compareAndSet(false, true)) {
				log.warn("Requesting 3D canvas rebuild after failed startup recovery");
				callback.run();
				markRenderActivity();
			}
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
			suppressInternalResizeEvents = true;
			try {
				setBounds(x, y, width, height + 1);
				setBounds(x, y, width, height);
			} finally {
				suppressInternalResizeEvents = false;
			}
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
			private int activeDragButton = MouseEvent.NOBUTTON;

			@Override
			public void mousePressed(MouseEvent e) {
				markRenderActivity();
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
					setCameraIsMoving(true); // Start tracking camera movement
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				markRenderActivity();
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
					setCameraIsMoving(false); // Stop tracking camera movement
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				markRenderActivity();
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
				markRenderActivity();
				Scene3DOrchestrator orchestrator = scene3DOrchestrator;
				if (orchestrator == null) {
					return;
				}
				orchestrator.getInputHandler().getInputState().addScroll(e.getWheelRotation() * -1.0f, e.getX(), e.getY());
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
		// The main scene now handles anti-aliasing inside the off-screen pipeline. Keep the
		// heavyweight AWT default framebuffer single-sampled to avoid WGL multisample context
		// creation failures on Windows; the renderer resolves its own MSAA target before present.
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
					hudShader = new GLShader("/shaders/ui/hud_vertex.glsl", "/shaders/ui/hud_fragment.glsl");

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

				// Startup layout may have queued resize events before the GL pipeline existed.
				// The orchestrator was just created with the current canvas size, so those
				// requests are already reflected and must not trigger a second fit on the
				// first interactive frame.
				pendingWinWidth = winWidth;
				pendingWinHeight = winHeight;
				pendingFbWidth = fbWidth;
				pendingFbHeight = fbHeight;
				resizeRequested = false;

				// Mark initialization complete - allows resize/render operations to proceed
				glInitialized = true;
				glInitLatch.countDown();

				if (PRESENT_VIA_IMAGE_ON_MACOS) {
					// The native GL layer must never be displayed; frames are painted
					// via paint() instead. Hide it once the view exists.
					SwingUtilities.invokeLater(this::hideNativeGLLayer);
				}
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
		int renderCount = renderCallCount.incrementAndGet();
		if (glInitFailed) {
			return;
		}
		if (!isDisplayable() || !isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}
		int paintBefore = paintCallCount.get();
		int swapBefore = swapCallCount.get();
		try {
			// Serialize JAWT drawing-surface acquisition across render threads and
			// cleanup: concurrent surface access from two threads crashes natively
			// (SIGBUS/SIGSEGV in JAWT_DrawingSurface_GetDrawingSurfaceInfo).
			RENDER_LOCK.lock();
			// Delegate to AWTGLCanvas to make the context current and handle buffer swapping.
			super.render();
			if (peerBoundsSyncEnabled && renderCount == 1) {
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
				if (isLateTeardownRenderFailure(t)) {
					log.debug("Ignoring 3D render failure during canvas teardown: {}", t.toString());
					return;
				}
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
		} finally {
			RENDER_LOCK.unlock();
		}
	}

	private boolean isLateTeardownRenderFailure(Throwable throwable) {
		if (isDisplayable() && getWidth() > 0 && getHeight() > 0) {
			return false;
		}
		if (!(throwable instanceof NullPointerException)) {
			return false;
		}
		String message = throwable.getMessage();
		if (message != null && message.contains("JAWTDrawingSurface.Unlock()")) {
			return true;
		}
		for (StackTraceElement frame : throwable.getStackTrace()) {
			if (frame.getClassName().contains("PlatformMacOSXGLCanvas")
					&& "unlock".equals(frame.getMethodName())) {
				return true;
			}
		}
		return false;
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

			// Allocate the PBO at the same capacity as the host pixel buffer so
			// glBufferData(orphan) → glBufferSubData(host) → glTexSubImage2D(0L)
			// can stream uploads without ever stalling on prior GPU usage.
			hudPbo = glGenBuffers();
			hudPboCapacityBytes = paddedSize;
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, hudPbo);
			glBufferData(GL_PIXEL_UNPACK_BUFFER, hudPboCapacityBytes, GL_STREAM_DRAW);
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		}
	}

	private void cleanupHudResources() {
		if (hudTexture != null) {
			// Cleanup directly - don't return to shared pool
			hudTexture.cleanup();
			hudTexture = null;
		}
		if (hudPbo != 0) {
			glDeleteBuffers(hudPbo);
			hudPbo = 0;
			hudPboCapacityBytes = 0;
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
		boolean startupFrameVisible = false;
		try {
			if (scene3DOrchestrator == null) {
				return;
			}

			handlePendingResize();

			GLRenderer renderer = scene3DOrchestrator.getRenderer();
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
			renderer.render(sceneView, true);
			if (PRESENT_VIA_IMAGE_ON_MACOS) {
				presentFrameViaImage(renderer);
			} else {
				renderer.presentResolvedToCurrentFramebuffer();
			}
			startupFrameVisible = sampleStartupFrameVisibilityIfNeeded(renderer);

			// --- 2D HUD Rendering - only update texture if needed ---
			if (hudEnabled) {
				boolean hudPanelNeedsRepaint = hudPanel != null && hudPanel.needsRepaint();
				boolean hudRepaintRequested = false;
				if (hudNeedsUpdate || hudPanelNeedsRepaint) {
					// While the camera is being dragged, the orientation gizmo wants
					// to repaint every frame. Force those into the rate-limited path
					// so the HUD upload doesn't block the render thread at 60 fps.
					boolean highPriority = hudPanelNeedsRepaint && !cameraIsMoving;
					hudRepaintRequested = requestHudRepaint(highPriority);
				}
				if (hudRepaintRequested) {
					hudNeedsUpdate = false;
				}

				// With image presentation the HUD image is composited in paint()
				// instead of being uploaded and drawn as a GL overlay.
				if (!PRESENT_VIA_IMAGE_ON_MACOS) {
					uploadHudTextureIfReady();

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
			}
			shouldSwap = true;
		} catch (Exception ex) {
			log.error("Error during paintGL", ex);
		} finally {
			if (shouldSwap) {
				swapCallCount.incrementAndGet();
				// lwjgl3-awt's macOS swapBuffers() is glFlush(). On macOS 26, that can
				// synchronously display the NSOpenGLView backing layer on this render
				// thread and abort inside AppKit's main-thread check for setView:.
				if (!SKIP_DEFAULT_FRAMEBUFFER_FLUSH_ON_MACOS) {
					swapBuffers();
				}
				visibleFrameRecoveryPending = !startupFrameVisible;
			}
		}
	}

	/**
	 * Reads the finished frame back from the renderer's resolved FBO and publishes
	 * it for {@link #paint(Graphics)} to draw. Used on macOS, where presenting via
	 * the NSOpenGLView layer aborts inside AppKit (see PRESENT_VIA_IMAGE_ON_MACOS).
	 * Runs on the render thread with the GL context current.
	 */
	private void presentFrameViaImage(GLRenderer renderer) {
		int width = renderer.getRenderWidth();
		int height = renderer.getRenderHeight();
		if (width <= 0 || height <= 0) {
			return;
		}

		int requiredBytes = width * height * 4;
		if (presentReadbackBuffer == null || presentReadbackBuffer.capacity() < requiredBytes) {
			if (presentReadbackBuffer != null) {
				MemoryUtil.memFree(presentReadbackBuffer);
			}
			presentReadbackBuffer = MemoryUtil.memAlloc(requiredBytes);
		}
		presentReadbackBuffer.clear();

		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int previousReadBuffer = glGetInteger(GL_READ_BUFFER);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, renderer.getResolvedFramebufferId());
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		// BGRA + INT_8_8_8_8_REV packs each pixel as a native-order int that matches
		// the TYPE_INT_* layout, so rows can be bulk-copied without per-pixel swizzling.
		glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, presentReadbackBuffer);
		glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
		glReadBuffer(previousReadBuffer);

		IntBuffer pixels = presentReadbackBuffer.asIntBuffer();
		presentedImageHasContent = pixels.get((height / 2) * width + width / 2) != 0;

		BufferedImage staging = nextPresentStagingImage(width, height);
		int[] target = ((DataBufferInt) staging.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < height; y++) {
			// GL rows are bottom-up; images are top-down
			pixels.get(target, (height - 1 - y) * width, width);
		}

		presentedImage = staging;
		showPresentedFrame();
	}

	/**
	 * Draws the published frame to the screen through a double-buffered
	 * {@link BufferStrategy}, directly from the render thread. Going through
	 * repaint()/paint() instead makes the view flicker during interaction: the
	 * peer erases the damaged region to the background color before paint()
	 * gets to draw the frame.
	 */
	private void showPresentedFrame() {
		if (!isShowing()) {
			// A frame finishing right as the user switches to the 2D view must not
			// blit over the newly shown card.
			return;
		}
		try {
			BufferStrategy strategy = presentStrategy;
			if (strategy == null) {
				if (!isDisplayable()) {
					return;
				}
				createBufferStrategy(2);
				strategy = getBufferStrategy();
				presentStrategy = strategy;
				if (strategy == null) {
					repaint();
					return;
				}
			}
			do {
				do {
					Graphics g = strategy.getDrawGraphics();
					try {
						paintPresentedFrame(g);
					} finally {
						g.dispose();
					}
				} while (strategy.contentsRestored());
				if (!isShowing()) {
					return;
				}
				strategy.show();
			} while (strategy.contentsLost());
		} catch (Exception e) {
			// Peer not ready for a buffer strategy yet — fall back to a plain repaint.
			repaint();
		}
	}

	/**
	 * Returns the ping-pong image the render thread may write to (the one the EDT
	 * is not currently painting), recreating it if the frame size changed.
	 */
	private BufferedImage nextPresentStagingImage(int width, int height) {
		BufferedImage current = presentedImage;
		BufferedImage staging = (current == presentedImageA) ? presentedImageB : presentedImageA;
		if (staging == null || staging.getWidth() != width || staging.getHeight() != height) {
			// INT_RGB so the frame is drawn opaque, ignoring the FBO's alpha channel
			staging = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			if (current == presentedImageA) {
				presentedImageB = staging;
			} else {
				presentedImageA = staging;
			}
		}
		return staging;
	}

	/**
	 * Composites the latest presented frame and the HUD into the given graphics.
	 */
	private void paintPresentedFrame(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		BufferedImage image = presentedImage;
		if (image == null) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// The image is at framebuffer (HiDPI) resolution; drawing at the logical
		// size maps it 1:1 onto the physical pixels of the scaled Graphics.
		g2.drawImage(image, 0, 0, width, height, null);
		if (hudEnabled) {
			synchronized (hudLock) {
				if (hudImage != null) {
					g2.drawImage(hudImage, 0, 0, width, height, null);
				}
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		if (!PRESENT_VIA_IMAGE_ON_MACOS) {
			super.paint(g);
			return;
		}
		// Expose/damage repaint while the render loop is idle: draw the last frame.
		paintPresentedFrame(g);
	}

	@Override
	public void update(Graphics g) {
		if (PRESENT_VIA_IMAGE_ON_MACOS) {
			// Skip AWT's implicit background clear to avoid flicker between frames.
			paint(g);
		} else {
			super.update(g);
		}
	}

	/**
	 * Hides the CALayer of the NSOpenGLView that lwjgl3-awt attaches over this
	 * canvas. The layer never receives frames in image-presentation mode, and any
	 * attempt by AppKit to display it (from the render thread or from the EDT's
	 * CoreAnimation commits) trips a main-thread assertion on recent macOS.
	 * Hiding it both reveals the Java2D-painted frame and prevents those aborts.
	 */
	private void hideNativeGLLayer() {
		if (!PRESENT_VIA_IMAGE_ON_MACOS || platformCanvas == null) {
			return;
		}
		try {
			// lwjgl3-awt installs its own hierarchy listener that un-hides the GL
			// layer whenever the canvas becomes showing again, which flashes the
			// empty (white) layer during 2D/3D view switches. Remove it: the layer
			// must never become visible in image-presentation mode.
			for (java.awt.event.HierarchyListener listener : getHierarchyListeners()) {
				if (listener.getClass().getName().startsWith("org.lwjgl.opengl.awt.")) {
					removeHierarchyListener(listener);
				}
			}

			java.lang.reflect.Field viewField = platformCanvas.getClass().getDeclaredField("view");
			viewField.setAccessible(true);
			long view = ((Number) viewField.get(platformCanvas)).longValue();
			if (view == 0L) {
				return;
			}
			long objcMsgSend = org.lwjgl.system.macosx.ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
			long layer = org.lwjgl.system.JNI.invokePPP(view,
					org.lwjgl.system.macosx.ObjCRuntime.sel_getUid("layer"), objcMsgSend);
			if (layer == 0L) {
				return;
			}
			org.lwjgl.system.JNI.invokePPPV(layer,
					org.lwjgl.system.macosx.ObjCRuntime.sel_getUid("setHidden:"), 1L, objcMsgSend);
			org.lwjgl.awt.MacOSX.caFlush();
			nativeGLLayerHidden = true;
		} catch (Throwable t) {
			log.warn("Could not hide native GL layer; the 3D view may stay blank: {}", t.toString());
		}
	}

	private void handleExport(SceneView sceneView, GLRenderer renderer) {
		boolean renderBackground = !scene3DOrchestrator.isExportTransparent();
		renderer.render(sceneView, renderBackground);

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
		if (hudPanel == null || hudGraphics == null || scene3DOrchestrator == null || !isDisplayable()) {
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
			if (hudImage == null || hudPanel == null || hudGraphics == null || hudImageBuffer == null || hudIntBuffer == null
					|| scene3DOrchestrator == null || !isDisplayable()) {
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
			markRenderActivity();
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

			int byteSize = hudImage.getWidth() * hudImage.getHeight() * 4;

			if (hudPbo != 0) {
				// Async path: orphan the PBO, copy host bytes into pinned memory, then
				// glTexSubImage2D from the bound PBO returns immediately and the GPU
				// performs the upload concurrently with subsequent rendering.
				glBindBuffer(GL_PIXEL_UNPACK_BUFFER, hudPbo);
				glBufferData(GL_PIXEL_UNPACK_BUFFER, hudPboCapacityBytes, GL_STREAM_DRAW);
				int oldLimit = hudImageBuffer.limit();
				hudImageBuffer.limit(byteSize);
				glBufferSubData(GL_PIXEL_UNPACK_BUFFER, 0, hudImageBuffer);
				hudImageBuffer.limit(oldLimit);

				glBindTexture(GL_TEXTURE_2D, hudTexture.getId());
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, hudImage.getWidth(), hudImage.getHeight(),
						GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0L);
				glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
			} else {
				// Fallback if PBO allocation failed.
				glBindTexture(GL_TEXTURE_2D, hudTexture.getId());
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, hudImage.getWidth(), hudImage.getHeight(),
						GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, hudImageBuffer);
			}
		}
	}

	private void handlePendingResize() {
		if (!glInitialized || scene3DOrchestrator == null) {
			return;
		}

		int currentWidth = Math.max(1, getWidth());
		int currentHeight = Math.max(1, getHeight());
		int[] currentFbSize = computeFramebufferSize(currentWidth, currentHeight);
		int currentFbWidth = currentFbSize[0];
		int currentFbHeight = currentFbSize[1];

		int width = pendingWinWidth;
		int height = pendingWinHeight;
		int fbWidth = pendingFbWidth;
		int fbHeight = pendingFbHeight;
		ViewportDimensions viewport = scene3DOrchestrator.getViewport();

		if (!resizeRequested) {
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
		} else if (width != currentWidth || height != currentHeight
				|| fbWidth != currentFbWidth || fbHeight != currentFbHeight) {
			// Resize events can be queued before the canvas reaches its final layout.
			// Always prefer the live canvas dimensions over stale pending values.
			width = currentWidth;
			height = currentHeight;
			fbWidth = currentFbWidth;
			fbHeight = currentFbHeight;
		} else if (viewport.getWindowWidth() == width
				&& viewport.getWindowHeight() == height
				&& viewport.getFramebufferWidth() == fbWidth
				&& viewport.getFramebufferHeight() == fbHeight) {
			resizeRequested = false;
			return;
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

	private ByteBuffer captureResolvedFramebuffer(GLRenderer renderer, int width, int height) {
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

	private boolean sampleStartupFrameVisibilityIfNeeded(GLRenderer renderer) {
		if (startupFrameDetectionComplete) {
			return true;
		}
		// With image presentation, whatever was read back is exactly what paint()
		// shows — no need for (and no meaning in) probing the default framebuffer.
		boolean visible = PRESENT_VIA_IMAGE_ON_MACOS
				? presentedImageHasContent
				: detectStartupFrameVisibility(renderer);
		if (visible) {
			if (++consecutiveVisibleStartupFrames >= STARTUP_VISIBILITY_CONFIRM_FRAMES
					|| startupBlankFramebufferRecoveryRequested.get()) {
				startupFrameDetectionComplete = true;
			}
		} else {
			consecutiveVisibleStartupFrames = 0;
		}
		return visible;
	}

	private boolean detectStartupFrameVisibility(GLRenderer renderer) {
		int[] defaultFbSize = computeFramebufferSize();
		int currentFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int[] resolvedCenter = sampleFramebufferCenterPixelRgba(renderer.getResolvedFramebufferId(), GL_COLOR_ATTACHMENT0,
				renderer.getRenderWidth(), renderer.getRenderHeight());
		int[] presentedBackCenter = sampleFramebufferCenterPixelRgba(currentFramebuffer, GL_BACK,
				defaultFbSize[0], defaultFbSize[1]);
		int[] presentedFrontCenter = sampleFramebufferCenterPixelRgba(currentFramebuffer, GL_FRONT,
				defaultFbSize[0], defaultFbSize[1]);
		boolean resolvedHasContent = hasVisiblePixelContent(resolvedCenter);
		boolean defaultHasContent = hasVisiblePixelContent(presentedBackCenter) || hasVisiblePixelContent(presentedFrontCenter);
		boolean startupFrameVisible = !resolvedHasContent || defaultHasContent;
		// While a resize is in flight the rendered frame and the default framebuffer can
		// disagree on size, making a blank center-pixel read meaningless. Such frames must
		// not count toward the blank-canvas rebuild threshold, or a resize right after
		// startup can trigger a needless (and state-destroying) canvas rebuild.
		boolean sampleReliable = !resizeRequested
				&& renderer.getRenderWidth() == defaultFbSize[0]
				&& renderer.getRenderHeight() == defaultFbSize[1];
		handleBlankDefaultFramebufferDuringStartup(startupFrameVisible, resolvedHasContent && sampleReliable);
		return startupFrameVisible;
	}

	private void handleBlankDefaultFramebufferDuringStartup(boolean startupFrameVisible, boolean resolvedHasContent) {
		if (startupFrameVisible || !visibleFrameRecoveryPending || !resolvedHasContent) {
			startupBlankFramebufferFrames = 0;
			return;
		}

		startupBlankFramebufferFrames++;
		if (startupBlankFramebufferFrames < 2) {
			return;
		}

		Runnable callback = blankDefaultFramebufferCallback;
		if (callback == null || !startupBlankFramebufferRecoveryRequested.compareAndSet(false, true)) {
			return;
		}

		callback.run();
	}

	private int[] sampleFramebufferCenterPixelRgba(int framebufferId, int readBuffer, int width, int height) {
		if (width <= 0 || height <= 0) {
			return null;
		}

		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int previousReadBuffer = glGetInteger(GL_READ_BUFFER);
		ByteBuffer pixel = BufferUtils.createByteBuffer(4);
		int sampleX = Math.max(0, Math.min(width - 1, width / 2));
		int sampleY = Math.max(0, Math.min(height - 1, height / 2));

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		glReadBuffer(framebufferId == 0 ? readBuffer : GL_COLOR_ATTACHMENT0);
		glReadPixels(sampleX, sampleY, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
		glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
		glReadBuffer(previousReadBuffer);

		return new int[]{
				pixel.get(0) & 0xFF,
				pixel.get(1) & 0xFF,
				pixel.get(2) & 0xFF,
				pixel.get(3) & 0xFF
		};
	}

	private boolean hasVisiblePixelContent(int[] rgba) {
		if (rgba == null || rgba.length < 4) {
			return false;
		}
		return rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 0;
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
			// runInContext acquires the native JAWT drawing surface; doing that while a
			// render thread is mid-frame on the same surface crashes natively. Take the
			// shared render lock (bounded, so the EDT can't hang on a stuck render) and
			// skip the in-context cleanup if a render never finishes.
			boolean renderLockAcquired = false;
			try {
				renderLockAcquired = RENDER_LOCK.tryLock(CLEANUP_RENDER_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (renderLockAcquired) {
				try {
					cleanupGLResourcesInContext();
					// Only safe to free while no render is in flight — the render
					// thread reads into this buffer during presentFrameViaImage().
					if (presentReadbackBuffer != null) {
						MemoryUtil.memFree(presentReadbackBuffer);
						presentReadbackBuffer = null;
					}
				} finally {
					RENDER_LOCK.unlock();
				}
			} else {
				log.warn("Render still in progress during cleanup; skipping in-context GL cleanup, " +
						"some GL resources may leak");
			}
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
			if (hudPanel != null) {
				hudPanel.setGLScenePanel(null);
				hudPanel.setSceneViewController(null);
			}
			scene3DOrchestrator = null;
			glCapabilities = null;

		GpuResourceTracker.logLiveResources("Swing canvas cleanup (other canvases may still be active)", false);
	}

	/**
	 * Releases GL-side resources with the context current. Must be called with
	 * {@link #RENDER_LOCK} held. This path can crash on macOS if called after
	 * peer teardown has started, hence the displayability check in {@link #cleanup()}.
	 * Do not call disposeCanvas() here: Swing/AWTGLCanvas tears down the native
	 * drawing surface during removeNotify(), and forcing disposal earlier can
	 * double-free the JAWT surface on macOS during window close.
	 */
	private void cleanupGLResourcesInContext() {
		try {
			runInContext(() -> {
				if (scene3DOrchestrator != null) {
					try {
						SceneView scene = scene3DOrchestrator.getScene();
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
				+ ", visibleFrameRecoveryPending=" + visibleFrameRecoveryPending
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
		uiThemeListener = () -> SwingUtilities.invokeLater(() -> {
			Scene3DOrchestrator orchestrator = scene3DOrchestrator;
			if (orchestrator == null) {
				return;
			}
			orchestrator.enqueueGlTask(() -> applyThemeBackground(orchestrator.getScene()));
			hudNeedsUpdate = true;
			requestPeerBoundsSyncNow();
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
}
