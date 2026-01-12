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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Thin Swing adapter that embeds the new LWJGL-based GLScenePanel into the legacy
 * RocketPanel workflow. It preserves the minimal API expected by callers of the
 * old JOGL RocketFigure3d so RocketPanel can toggle between 2D/3D without broader
 * refactors yet.
 */
public class RocketFigure3d extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(RocketFigure3d.class);

	public static final int TYPE_FIGURE = 2;
	public static final int TYPE_UNFINISHED = 3;
	public static final int TYPE_FINISHED = 4;

	public interface ComponentSelectionListener {
		void componentClicked(RocketComponent[] clicked, MouseEvent event);
	}

	private final GLScenePanel glScenePanel;
	private final HUDPanel hudPanel;
	private final RocketInfo rocketInfo;
	private final List<ComponentSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private volatile boolean selectionBridgeInstalled = false;

	private static final int FRAME_INTERVAL_MS = 16;
	private static final int MAX_CONSECUTIVE_RENDER_ERRORS = 5;
	private ScheduledExecutorService renderExecutor;
	private volatile Thread renderThread;
	private int consecutiveRenderErrors = 0;
	private volatile boolean disposed = false;
	private volatile boolean renderingEnabled = false;

	public RocketFigure3d(OpenRocketDocument document) {
		setLayout(new BorderLayout());

		this.rocketInfo = new RocketInfo(document.getRocket().getSelectedConfiguration());
		this.hudPanel = new HUDPanel(document.getRocket(), rocketInfo);
		this.glScenePanel = new GLScenePanel(document.getRocket(), hudPanel);
		add(glScenePanel, BorderLayout.CENTER);

		hookSelectionBridge();
	}

	private void renderFrame() {
		if (!renderingEnabled || disposed || glScenePanel.glInitFailed) {
			return;
		}
		// Wait until the canvas has a real peer and size.
		if (!glScenePanel.isDisplayable() || glScenePanel.getWidth() <= 0 || glScenePanel.getHeight() <= 0) {
			return;
		}
		glScenePanel.render();
	}

	/**
	 * Start the background render loop. Called when RocketPanel switches to 3D.
	 */
	public void startRendering() {
		renderingEnabled = true;
		consecutiveRenderErrors = 0;
		startRenderLoop();
	}

	/**
	 * Pause the background render loop. Called when RocketPanel switches to 2D.
	 */
	public void stopRendering() {
		renderingEnabled = false;
		if (renderExecutor != null) {
			renderExecutor.shutdownNow();
			renderExecutor = null;
		}
	}

	private void startRenderLoop() {
		if (renderExecutor != null && !renderExecutor.isShutdown()) {
			return;
		}
		ThreadFactory renderThreadFactory = r -> {
			Thread t = new Thread(r, "gl-render-rocket");
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
					if (glScenePanel.isDisplayable() && glScenePanel.getWidth() > 0 && glScenePanel.getHeight() > 0) {
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
					glScenePanel.render();
				} catch (Exception e) {
					log.warn("Initial render failed", e);
				}

				boolean initialized = glScenePanel.awaitInitialized(3_000) && !glScenePanel.glInitFailed;
				if (!initialized) {
					log.error("GL context did not initialize for RocketFigure3d");
					return;
				}

				while (renderingEnabled && !disposed) {
					if (!glScenePanel.isDisplayable()) {
						break;
					}
					if (glScenePanel.glInitFailed) {
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
					if (!glScenePanel.glInitFailed) {
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

	private void hookSelectionBridge() {
		// Wait for GL initialization on a background thread; the canvas may not init until
		// the user first switches to 3D.
		Thread waiter = new Thread(() -> {
			while (!disposed && !glScenePanel.glInitFailed) {
				if (glScenePanel.awaitInitialized(1000)) {
					SwingUtilities.invokeLater(() -> {
						if (disposed || glScenePanel.glInitFailed || selectionBridgeInstalled) {
							return;
						}
						glScenePanel.addSceneSelectionListener(selection -> {
							MouseEvent event = glScenePanel.consumePendingSelectionClickEvent();
							if (event == null) {
								// Ignore non-click selection changes (e.g., programmatic setSelection).
								return;
							}
							RocketComponent[] components = selection.stream()
									.map(obj -> obj.getRocketComponent())
									.filter(rc -> rc != null)
									.toArray(RocketComponent[]::new);
							for (ComponentSelectionListener listener : selectionListeners) {
								listener.componentClicked(components, event);
							}
						});
						selectionBridgeInstalled = true;
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
		glScenePanel.markHudForUpdate();
		glScenePanel.repaint();
	}

	// --- Compatibility no-ops / thin wrappers for legacy API expected by RocketPanel ---

	public void setType(int type) {
		// Rendering modes are not currently differentiated; accept call for compatibility.
	}

	public void setDrawCarets(boolean draw) {
		// Carets are drawn via RocketInfo overlay; toggle by showing/hiding HUD.
		hudPanel.setVisible(draw);
		glScenePanel.markHudForUpdate();
	}

	public void setCG(CoordinateIF cg) {
		if (cg != null) {
			rocketInfo.setCG(cg.getX());
		}
		glScenePanel.markHudForUpdate();
	}

	public void setCP(CoordinateIF cp) {
		if (cp != null) {
			rocketInfo.setCP(cp.getX());
		}
		glScenePanel.markHudForUpdate();
	}

	public void clearRelativeExtra() {
		// Extras not yet supported in the new HUD; no-op for now.
	}

	public void addAbsoluteExtra(RocketInfo info) {
		// HUD already renders RocketInfo; ignore additional overlays for now.
	}

	public void setSelection(RocketComponent[] components) {
		Scene3DOrchestrator orchestrator = glScenePanel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		RocketComponent[] componentsCopy = components != null ? components.clone() : null;
		orchestrator.enqueueGlTask(() -> {
			SceneView scene = orchestrator.getScene();
			if (scene == null) {
				return;
			}
			// Build the list of scene objects that match the requested components.
			List<SceneObject> selectedObjects = scene.getObjects().stream()
					.filter(obj -> {
						RocketComponent rc = obj.getRocketComponent();
						if (rc == null || componentsCopy == null) return false;
						for (RocketComponent c : componentsCopy) {
							if (c == rc) return true;
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
		glScenePanel.cleanup();
	}

	public Scene3DOrchestrator getSceneController() {
		return glScenePanel.getScene3DOrchestrator();
	}

	@Override
	public void removeNotify() {
		stopRendering();
		disposed = true;
		super.removeNotify();
	}
}
