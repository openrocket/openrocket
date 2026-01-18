package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.ui.HUDPanel;
import info.openrocket.swing.gui.figureelements.RocketInfo;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thin Swing adapter that embeds the new LWJGL-based GLScenePanel into the legacy
 * RocketPanel workflow. It preserves the minimal API expected by callers of the
 * old JOGL RocketFigure3d so RocketPanel can toggle between 2D/3D without broader
 * refactors yet.
 */
public class RocketFigure3d extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(RocketFigure3d.class);
	private static final boolean MAC_OS = System.getProperty("os.name", "").toLowerCase().contains("mac");
	private static final MacEdtRenderScheduler MAC_RENDER_SCHEDULER = MAC_OS ? new MacEdtRenderScheduler() : null;
	private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
	private final int instanceId = INSTANCE_COUNTER.incrementAndGet();

	public static final int TYPE_FIGURE = 2;
	public static final int TYPE_UNFINISHED = 3;
	public static final int TYPE_FINISHED = 4;

	public interface ComponentSelectionListener {
		void componentClicked(RocketComponent[] clicked, MouseEvent event);
	}

	private final OpenRocketDocument document;
	private volatile GLScenePanel glScenePanel;
	private final HUDPanel hudPanel;
	private final RocketInfo rocketInfo;
	private final List<ComponentSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private volatile boolean selectionBridgeInstalled = false;
	private final AtomicInteger canvasGeneration = new AtomicInteger(0);
	private volatile RocketComponent[] pendingSelection;

	private static final int FRAME_INTERVAL_MS = 16;
	private static final int MAX_CONSECUTIVE_RENDER_ERRORS = 5;
	private static final int CANVAS_INIT_RETRY_DELAY_MS = 50;
	// Keep retrying canvas creation for longer when multiple windows are opening/validating.
	private static final int MAX_CANVAS_INIT_ATTEMPTS = 400; // ~20 seconds

	private ScheduledExecutorService renderExecutor;
	private volatile Thread renderThread;
	private int consecutiveRenderErrors = 0;
	private volatile boolean disposed = false;
	private volatile boolean renderingEnabled = false;
	private final AtomicLong lastRenderActivityMs = new AtomicLong(System.currentTimeMillis());
	private volatile long lastRenderFrameSkipDebugMs = 0;

	public RocketFigure3d(OpenRocketDocument document) {
		this.document = document;
		setLayout(new BorderLayout());

		this.rocketInfo = new RocketInfo(document.getRocket().getSelectedConfiguration());
		this.hudPanel = new HUDPanel(document.getRocket(), rocketInfo);
	}

	private void ensureCanvasCreatedOnEdt() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("ensureCanvasCreatedOnEdt must run on EDT");
		}
		if (glScenePanel != null) {
			return;
		}
		GLScenePanel panel = new GLScenePanel(document.getRocket(), hudPanel);
		panel.setRenderActivityCallback(this::markRenderActivity);
		this.glScenePanel = panel;
		add(panel, BorderLayout.CENTER);
		// Ensure heavyweight peer is validated inside its toplevel window before first render.
		var window = SwingUtilities.getWindowAncestor(this);
		if (window != null) {
			window.invalidate();
			window.validate();
		}
		selectionBridgeInstalled = false;
		int generation = canvasGeneration.incrementAndGet();
		hookSelectionBridge(panel, generation);
		panel.requestPeerBoundsSyncNow();
		revalidate();
		repaint();
	}

	private void ensureCanvasCreatedWhenReadyOnEdt(int attempt) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> ensureCanvasCreatedWhenReadyOnEdt(attempt));
			return;
		}
		if (!renderingEnabled || disposed) {
			return;
		}
		if (glScenePanel != null) {
			startRenderLoop();
			return;
		}
		if (attempt >= MAX_CANVAS_INIT_ATTEMPTS) {
			return;
		}
		if (!isShowing() || getWidth() <= 0 || getHeight() <= 0) {
			Timer timer = new Timer(CANVAS_INIT_RETRY_DELAY_MS, e -> ensureCanvasCreatedWhenReadyOnEdt(attempt + 1));
			timer.setRepeats(false);
			timer.start();
			return;
		}
		try {
			getLocationOnScreen();
		} catch (Exception ignored) {
			Timer timer = new Timer(CANVAS_INIT_RETRY_DELAY_MS, e -> ensureCanvasCreatedWhenReadyOnEdt(attempt + 1));
			timer.setRepeats(false);
			timer.start();
			return;
		}

		ensureCanvasCreatedOnEdt();
		startRenderLoop();
	}

	private void renderFrame() {
		GLScenePanel panel = glScenePanel;
		if (panel == null || !renderingEnabled || disposed || panel.glInitFailed) {
			return;
		}
		// Wait until the canvas has a real peer and size.
		if (!panel.isDisplayable() || !panel.isShowing() || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
			return;
		}
		panel.render();
	}

	/**
	 * Start the background render loop. Called when RocketPanel switches to 3D.
	 */
	public void startRendering() {
		renderingEnabled = true;
		markRenderActivity();
		consecutiveRenderErrors = 0;
		// On macOS, heavyweight GL canvases can appear at an incorrect on-screen position
		// immediately after a CardLayout/JSplitPane switch until the window is validated.
		// Defer starting the render loop until after the EDT has processed layout.
		SwingUtilities.invokeLater(() -> {
			if (!renderingEnabled || disposed) {
				return;
			}
			var window = SwingUtilities.getWindowAncestor(this);
			if (window != null) {
				window.invalidate();
				window.validate();
				window.repaint();
			}
			ensureCanvasCreatedWhenReadyOnEdt(0);
		});
	}

	/**
	 * Pause the background render loop. Called when RocketPanel switches to 2D.
	 */
	public void stopRendering() {
		renderingEnabled = false;
		if (MAC_RENDER_SCHEDULER != null) {
			MAC_RENDER_SCHEDULER.unregister(this);
		}
		if (renderExecutor != null) {
			renderExecutor.shutdownNow();
			renderExecutor = null;
		}
	}

	private void startRenderLoop() {
		if (MAC_RENDER_SCHEDULER != null) {
			MAC_RENDER_SCHEDULER.register(this);
			return;
		}
		if (renderExecutor != null && !renderExecutor.isShutdown()) {
			return;
		}
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			// Shouldn't happen; startRendering ensures creation on EDT before calling this.
			return;
		}
		ThreadFactory renderThreadFactory = r -> {
			Thread t = new Thread(r, "gl-render-rocket-" + instanceId);
			t.setDaemon(true);
			return t;
		};
		renderExecutor = Executors.newSingleThreadScheduledExecutor(renderThreadFactory);
		renderExecutor.execute(() -> {
			renderThread = Thread.currentThread();
			try {
				long deadline = System.currentTimeMillis() + 5_000;
				while (System.currentTimeMillis() < deadline) {
					if (disposed || !renderingEnabled) {
						return;
					}
					if (panel.isShowing() && panel.isDisplayable() && panel.getWidth() > 0 && panel.getHeight() > 0) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException ignored) {
						Thread.currentThread().interrupt();
						return;
					}
				}

				if (disposed || !renderingEnabled) {
					return;
				}

				try {
					renderFrame();
				} catch (Exception e) {
					log.warn("Initial render failed", e);
				}

				while (renderingEnabled && !disposed) {
					if (!panel.isDisplayable()) {
						break;
					}
					if (panel.glInitFailed) {
						log.error("GL init failed for RocketFigure3d");
						break;
					}
					try {
						renderFrame();
					} catch (Exception ex) {
						consecutiveRenderErrors++;
						log.warn("Render error ({})", consecutiveRenderErrors, ex);
						if (consecutiveRenderErrors >= MAX_CONSECUTIVE_RENDER_ERRORS) {
							break;
						}
					}
					if (!panel.glInitFailed) {
						consecutiveRenderErrors = 0;
					}
					try {
						Thread.sleep(FRAME_INTERVAL_MS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			} finally {
				renderThread = null;
			}
		});
	}

	private static final class MacEdtRenderScheduler implements ActionListener {
		private static final int MAX_ACTIVE_CANVASES = Math.max(1,
				Integer.getInteger("openrocket.figure3d.mac.maxActiveCanvases", 2));

		private final ArrayList<RocketFigure3d> active = new ArrayList<>();
		private final Timer timer;
		private int index = 0;
		private long lastSelectionDebugMs = 0;

		private MacEdtRenderScheduler() {
			timer = new Timer(FRAME_INTERVAL_MS, this);
			timer.setRepeats(true);
			timer.setCoalesce(true);
		}

		private void register(RocketFigure3d figure) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> register(figure));
				return;
			}
			if (active.contains(figure)) {
				return;
			}
			active.add(figure);
			if (index >= active.size()) {
				index = 0;
			}
			if (!timer.isRunning()) {
				timer.start();
			}
		}

		private void unregister(RocketFigure3d figure) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> unregister(figure));
				return;
			}
			int idx = active.indexOf(figure);
			if (idx < 0) {
				return;
			}
			active.remove(idx);
			if (index > idx) {
				index--;
			}
			if (active.isEmpty()) {
				timer.stop();
				index = 0;
			} else if (index >= active.size()) {
				index = 0;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Prune inactive entries.
			for (int i = active.size() - 1; i >= 0; i--) {
				RocketFigure3d figure = active.get(i);
				if (!figure.renderingEnabled || figure.disposed) {
					active.remove(i);
					if (index > i) {
						index--;
					}
				}
			}
			if (active.isEmpty()) {
				timer.stop();
				index = 0;
				return;
			}

			List<RocketFigure3d> candidates = active;
			if (active.size() > MAX_ACTIVE_CANVASES) {
				candidates = new ArrayList<>(active);
				candidates.sort(Comparator.comparingLong(RocketFigure3d::getLastRenderActivityMs).reversed());
				candidates = candidates.subList(0, MAX_ACTIVE_CANVASES);
			}

			if (index >= candidates.size()) {
				index = 0;
			}
			RocketFigure3d figure = candidates.get(index);
			index = (index + 1) % candidates.size();
			figure.renderFrame();
		}
	}

	private void markRenderActivity() {
		lastRenderActivityMs.set(System.currentTimeMillis());
	}

	private long getLastRenderActivityMs() {
		return lastRenderActivityMs.get();
	}

	private void hookSelectionBridge(GLScenePanel panel, int generation) {
		// Wait for GL initialization on a background thread; the canvas may not init until
		// the user first switches to 3D.
		Thread waiter = new Thread(() -> {
			while (!disposed && canvasGeneration.get() == generation && !panel.glInitFailed) {
				if (panel.awaitInitialized(1000)) {
					SwingUtilities.invokeLater(() -> {
						if (disposed
								|| canvasGeneration.get() != generation
								|| this.glScenePanel != panel
								|| panel.glInitFailed
								|| selectionBridgeInstalled) {
							return;
						}
						panel.addSceneSelectionListener(selection -> {
							MouseEvent event = panel.consumePendingSelectionClickEvent();
							if (event == null) {
								// Ignore non-click selection changes (e.g., programmatic setSelection).
								return;
							}
							RocketComponent[] components = selection.stream()
									.map(obj -> obj.getRocketComponent())
									.filter(rc -> rc != null)
									.toArray(RocketComponent[]::new);
							SwingUtilities.invokeLater(() -> {
								for (ComponentSelectionListener listener : selectionListeners) {
									listener.componentClicked(components, event);
								}
							});
						});
						selectionBridgeInstalled = true;
						RocketComponent[] selection = pendingSelection;
						if (selection != null) {
							pendingSelection = null;
							setSelection(selection);
						}
					});
					return;
				}
			}
		}, "gl-selection-bridge");
		waiter.setDaemon(true);
		waiter.start();
	}

	public void addComponentSelectionListener(ComponentSelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void flushTextureCaches() {
		// The new renderer tracks textures automatically; nothing to flush here.
	}

	public void updateFigure() {
		// RocketSceneSynchronizer in the orchestrator will rebuild meshes on model changes.
		// We just request a repaint/HUD refresh to pick up minor UI updates.
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			return;
		}
		panel.markHudForUpdate();
		panel.repaint();
	}

	// --- Compatibility no-ops / thin wrappers for legacy API expected by RocketPanel ---

	public void setType(int type) {
		// Rendering modes are not currently differentiated; accept call for compatibility.
	}

	public void setDrawCarets(boolean draw) {
		// Carets are drawn via RocketInfo overlay; toggle by showing/hiding HUD.
		hudPanel.setVisible(draw);
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	public void setCG(CoordinateIF cg) {
		if (cg != null) {
			rocketInfo.setCG(cg.getX());
		}
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	public void setCP(CoordinateIF cp) {
		if (cp != null) {
			rocketInfo.setCP(cp.getX());
		}
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	public void clearRelativeExtra() {
		// Extras not yet supported in the new HUD; no-op for now.
	}

	public void addAbsoluteExtra(RocketInfo info) {
		// HUD already renders RocketInfo; ignore additional overlays for now.
	}

	public void setSelection(RocketComponent[] components) {
		RocketComponent[] componentsCopy = components != null ? components.clone() : null;
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			pendingSelection = componentsCopy;
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			pendingSelection = componentsCopy;
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			SceneView scene = orchestrator.getScene();
			if (scene == null) {
				return;
			}
			// Build the list of scene objects that match the requested components.
			List<SceneObject> selectedObjects = scene.getObjects().stream()
					.filter(obj -> {
						RocketComponent rc = obj.getRocketComponent();
						if (rc == null || componentsCopy == null) {
							return false;
						}
						for (RocketComponent c : componentsCopy) {
							if (c == rc) {
								return true;
							}
						}
						return false;
					})
					.collect(Collectors.toList());
			scene.setSelection(selectedObjects);
		});
	}

	public void cleanup() {
		stopRendering();
		disposed = true;
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.cleanup();
		}
	}

	public Scene3DOrchestrator getSceneController() {
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getScene3DOrchestrator() : null;
	}

	@Override
	public void removeNotify() {
		stopRendering();
		disposed = true;
		super.removeNotify();
	}
}
