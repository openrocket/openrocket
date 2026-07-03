package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.figure3d.animation.PlaybackClock;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.core.math.DefaultRaycaster;
import info.openrocket.swing.gui.figure3d.core.math.Raycaster;
import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.rendering.RealisticRenderer;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderer;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraController;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraControls;
import info.openrocket.swing.gui.figure3d.scene.controllers.DefaultSceneInputProcessor;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.controllers.SceneInputProcessor;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.events.ExportListener;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.Figure3DPreferences;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.ViewportDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates scene state, input, synchronization, and rendering for a figure3d view.
 */
public class Scene3DOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(Scene3DOrchestrator.class);

    public final SceneView scene;
    private final GLRenderer renderer;
	private final RenderingConfiguration renderingConfiguration;
	private final ViewportDimensions viewport;
    private final CameraControls cameraController;
    private final SceneInputProcessor inputHandler;
	private final ConcurrentLinkedQueue<Runnable> glTaskQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	private final RocketSceneSynchronizer rocketSynchronizer;
	private final AppearanceFactory.DecalTextureCache decalTextureCache = AppearanceFactory.createDecalTextureCache();

    private volatile boolean exportRequested = false;
    private volatile boolean exportTransparent = false;
    private final List<ExportListener> exportListeners = new ArrayList<>();

	private long lastFrameTime;
	private volatile PlaybackClock playbackClock = null;

	/**
	 * Updates the orchestrator's knowledge of the window and framebuffer dimensions.
	 * 
	 * <p>This method should be called whenever the viewport is resized to ensure all
	 * components are properly updated with the new dimensions. It handles the distinction
	 * between logical window coordinates and physical framebuffer pixels, which is
	 * important for high-DPI displays.</p>
	 * 
	 * @param newWinWidth The new logical window width in pixels
	 * @param newWinHeight The new logical window height in pixels
	 * @param newFbWidth The new framebuffer width in pixels
	 * @param newFbHeight The new framebuffer height in pixels
	 */
	public void resize(int newWinWidth, int newWinHeight, int newFbWidth, int newFbHeight) {
		boolean wasZoomFitting = cameraController.isZoomFitting();

		// Update viewport dimensions
		viewport.update(newWinWidth, newWinHeight, newFbWidth, newFbHeight);
		
		// Propagate resize event to relevant components
		cameraController.resize(viewport.getAspectRatio());
		if (wasZoomFitting) {
			cameraController.focusOnRocket();
		}
		inputHandler.updateDimensions(viewport);
		if (renderer != null) {
			renderer.resize(viewport.getFramebufferWidth(), viewport.getFramebufferHeight());
		}
	}

	/**
	 * Automatically positions and orients the camera to frame the entire rocket.
	 * 
	 * <p>This method calculates the rocket's bounding box and adjusts the camera
	 * distance and center of interest to provide an optimal view of the complete
	 * rocket geometry.</p>
	 */
	public void focusOnRocket() {
		cameraController.focusOnRocket();
	}

	/**
	 * Restores the default side view, clears any drag-applied rocket rotation, and fits the rocket.
	 */
	public void resetViewAndFocusOnRocket() {
		if (scene instanceof Scene actualScene) {
			actualScene.resetRocketRotation();
		}
		cameraController.resetView();
		cameraController.focusOnRocket();
	}

	/**
	 * Reapplies the persisted rocket drag rotation after the rocket mesh is rebuilt.
	 */
	public void applyRocketRotationToScene() {
		if (scene instanceof Scene actualScene) {
			actualScene.applyRocketRotationToRocketObjects();
		}
	}

	/**
	 * Rebuilds the rocket scene geometry using the current rendering configuration.
	 */
	public void rebuildRocketScene() {
		rebuildRocketScene(true);
	}

	/**
	 * Rebuilds the rocket scene geometry and optionally refocuses the camera.
	 */
	public void rebuildRocketScene(boolean refocusCamera) {
		rocketSynchronizer.rebuildRocketScene(refocusCamera);
	}

	/**
	 * Runs one frame of non-render updates before the caller renders the scene.
	 */
	public void update() {
		runPendingGlTasks();
		long currentFrameTime = System.nanoTime();
		float deltaTime = (currentFrameTime - lastFrameTime) / 1e9f;
		lastFrameTime = currentFrameTime;

		// Process all input events
		inputHandler.processInput();

		// Update camera and scene
		cameraController.update();
		scene.updateParticles(deltaTime);

		// --- Simulation playback (if bound) ---
		if (playbackClock != null) {
				playbackClock.update(deltaTime);
				double t = playbackClock.getTime();
				for (var obj : scene.getObjects()) {
					if (obj.hasPoseProvider()) {
						obj.applyPoseAtTime(t);
					}
				}
		}
	}

	/**
	 * Gets the managed 3D scene containing all rocket geometry and effects.
	 * 
	 * @return The scene instance managed by this orchestrator
	 */
	public SceneView getScene() {
		return scene;
	}

	/**
	 * Requests an export of the current rendered frame to a PNG file.
	 * 
	 * <p>The export will be processed on the next render cycle. The file will be
	 * saved with a timestamp-based filename in the current working directory.</p>
	 * 
	 * @param transparent If true, exports with a transparent background; 
	 *                   if false, includes the scene background
	 */
    public void requestExport(boolean transparent) {
        this.exportRequested = true;
        this.exportTransparent = transparent;
        notifyExportRequested(transparent);
    }

	/**
	 * Checks if a PNG export has been requested and is pending.
	 * 
	 * @return true if an export is waiting to be processed
	 */
	public boolean isExportRequested() {
		return exportRequested;
	}

	/**
	 * Checks if the pending export should use a transparent background.
	 * 
	 * @return true if the export should have a transparent background
	 */
	public boolean isExportTransparent() {
		return exportTransparent;
	}

	/**
	 * Clears any pending export request.
	 * 
	 * <p>This method should be called after an export has been processed
	 * to prevent duplicate exports.</p>
	 */
    public void clearExportRequest() {
        this.exportRequested = false;
    }

    // --- Export listeners ---
    public void addExportListener(ExportListener listener) {
        if (listener != null) exportListeners.add(listener);
    }

    public void removeExportListener(ExportListener listener) {
        exportListeners.remove(listener);
    }

    private void notifyExportRequested(boolean transparent) {
        for (ExportListener l : exportListeners) {
            l.onExportRequested(transparent);
        }
    }

	/**
	 * Gets the unified rendering configuration containing all rendering settings.
	 * 
	 * @return The rendering configuration instance
	 */
    public RenderingConfiguration getRenderingConfiguration() {
        return renderingConfiguration;
    }

    /**
     * Exposes the renderer via its interface to reduce coupling.
     */
    public GLRenderer getRenderer() {
        return renderer;
    }

	public AppearanceFactory.DecalTextureCache getDecalTextureCache() {
		return decalTextureCache;
	}

	/**
	 * Enqueues work that must run on the GL/render thread.
	 */
	public void enqueueGlTask(Runnable task) {
		if (task == null || shutdown.get()) {
			return;
		}
		glTaskQueue.add(task);
	}

	private void runPendingGlTasks() {
		Runnable task;
		while ((task = glTaskQueue.poll()) != null) {
			try {
				task.run();
			} catch (Exception e) {
				log.warn("GL task failed", e);
			}
		}
	}

	/**
	 * Detaches listeners and stops accepting new tasks.
	 */
	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}
		glTaskQueue.clear();
		if (rocketSynchronizer != null) {
			rocketSynchronizer.dispose();
		}
	}

	/**
	 * Gets the camera controller for external access to camera operations.
	 * 
	 * @return The camera controller instance
	 */
    public CameraControls getCameraController() {
        return cameraController;
    }
	
	/**
	 * Gets the input handler for external access to input state and processing.
	 * 
	 * @return The input handler instance
	 */
    public SceneInputProcessor getInputHandler() {
        return inputHandler;
    }
	
	/**
	 * Gets the viewport dimensions for external access to coordinate conversion.
	 * 
	 * @return The viewport dimensions instance
	 */
	public ViewportDimensions getViewport() {
		return viewport;
	}
	
	/**
	 * Builder for {@link Scene3DOrchestrator}.
	 */
	public static class Builder {
		private final Rocket rocket;
		private final ViewportDimensions viewport;
		private float fieldOfView = (float) Math.toRadians(10);
		private float nearPlane = 0.1f;
		private float farPlane = 100f;
		private DisplaySettings.RenderMode renderMode = DisplaySettings.RenderMode.FINISHED;
		private boolean performanceMode = false;
		private boolean qualityMode = false;
		private GLRenderer rendererOverride;
		
		private Builder(Rocket rocket, ViewportDimensions viewport) {
			this.rocket = rocket;
			this.viewport = viewport;
		}
		
		/**
		 * Sets the camera field of view.
		 * @param fieldOfView The field of view in radians (default: 10 degrees)
		 * @return This builder instance
		 */
		public Builder withFieldOfView(double fieldOfView) {
			this.fieldOfView = (float) fieldOfView;
			return this;
		}
		
		/**
		 * Sets the camera near and far clipping planes.
		 * @param nearPlane The near clipping plane distance (default: 0.1)
		 * @param farPlane The far clipping plane distance (default: 100.0)
		 * @return This builder instance
		 */
		public Builder withClippingPlanes(float nearPlane, float farPlane) {
			this.nearPlane = nearPlane;
			this.farPlane = farPlane;
			return this;
		}
		
		/**
		 * Sets the initial render mode.
		 * @param renderMode The render mode to use (default: FINISHED)
		 * @return This builder instance
		 */
		public Builder withRenderMode(DisplaySettings.RenderMode renderMode) {
			this.renderMode = renderMode;
			return this;
		}
		
		/**
		 * Configures the orchestrator for optimal performance (lower quality).
		 * @return This builder instance
		 */
		public Builder withPerformanceMode() {
			this.performanceMode = true;
			this.qualityMode = false;
			return this;
		}
		
		/**
		 * Configures the orchestrator for maximum visual quality.
		 * @return This builder instance
		 */
		public Builder withQualityMode() {
			this.performanceMode = false;
			this.qualityMode = true;
			return this;
		}
		
		/**
		 * Overrides the renderer instance. Primarily intended for testing.
		 */
		public Builder withRenderer(GLRenderer renderer) {
			this.rendererOverride = renderer;
			return this;
		}
		
		/**
		 * Builds the Scene3DOrchestrator with the configured settings.
		 * @return A new Scene3DOrchestrator instance
		 * @throws Exception If any component fails to initialize
		 */
		public Scene3DOrchestrator build() throws Exception {
			// Create camera with custom settings
			Camera camera = Camera.builder()
				.withFieldOfView(fieldOfView)
				.withAspectRatio(viewport.getAspectRatio())
				.withClippingPlanes(nearPlane, farPlane)
				.withFixedCenterOfInterest(false)
				.build();
				
			// Create rendering configuration
			RenderingConfiguration config = RenderingConfiguration.builder()
					.withParticleEffects(false)
					//.withParticleTiming(false, 0f)
					.build();
			Figure3DPreferences.applyDefaults(config, rocket.getDocument().getDocumentPreferences(),
					Application.getPreferences());
			
			// Create scene
			Scene scene = Scene.builder(rocket, camera, config).build();
			
			// Create the orchestrator
			Scene3DOrchestrator orchestrator = new Scene3DOrchestrator(rocket, viewport, camera, scene, config, rendererOverride);
			
			// Apply render mode
			orchestrator.getRenderingConfiguration().getDisplay().setMode(renderMode);
			
			// Apply performance/quality settings
			if (performanceMode) {
				orchestrator.getRenderingConfiguration().setPerformanceMode();
			} else if (qualityMode) {
				orchestrator.getRenderingConfiguration().setQualityMode();
			}
			
			return orchestrator;
		}
	}
	
	/**
	 * Creates a builder for constructing Scene3DOrchestrator with custom configuration.
	 * @param rocket The rocket model to visualize
	 * @param viewport The viewport dimensions
	 * @return A new builder instance
	 */
	public static Builder builder(Rocket rocket, ViewportDimensions viewport) {
		return new Builder(rocket, viewport);
	}
	
	/**
	 * Creates a builder for constructing Scene3DOrchestrator with custom configuration.
	 * @param rocket The rocket model to visualize
	 * @param width The width in pixels (both window and framebuffer)
	 * @param height The height in pixels (both window and framebuffer)
	 * @return A new builder instance
	 */
	public static Builder builder(Rocket rocket, int width, int height) {
		return new Builder(rocket, new ViewportDimensions(width, height));
	}
	
	/**
	 * Creates a builder for constructing Scene3DOrchestrator with custom configuration.
	 * @param rocket The rocket model to visualize
	 * @param winWidth The logical window width in pixels
	 * @param winHeight The logical window height in pixels
	 * @param fbWidth The framebuffer width in pixels
	 * @param fbHeight The framebuffer height in pixels
	 * @return A new builder instance
	 */
	public static Builder builder(Rocket rocket, int winWidth, int winHeight, int fbWidth, int fbHeight) {
		return new Builder(rocket, new ViewportDimensions(winWidth, winHeight, fbWidth, fbHeight));
	}
	
	/**
	 * Internal constructor used by the builder to create a fully configured orchestrator.
	 * This constructor initializes all components of the 3D visualization system in the correct
	 * order and establishes the necessary connections between them.
	 * 
	 * <p>Initialization sequence:</p>
	 * <ol>
	 *   <li>Core components (viewport, scene, configuration)</li>
	 *   <li>GLRenderer with GPU resources</li>
	 *   <li>Controllers for camera and input handling</li>
	 *   <li>Synchronizer for rocket model integration</li>
	 * </ol>
	 * 
	 * @param rocket the OpenRocket model to visualize
	 * @param viewport the viewport dimensions for rendering
	 * @param camera the pre-configured camera instance
	 * @param scene the pre-configured scene instance
	 * @param config the rendering configuration settings
	 * @throws Exception if any component fails to initialize properly
	 */
	private Scene3DOrchestrator(Rocket rocket, ViewportDimensions viewport, Camera camera, SceneView scene, RenderingConfiguration config, GLRenderer rendererOverride) throws Exception {
		// 1. Initialize core components
		this.viewport = viewport;
		this.scene = scene;
		this.renderingConfiguration = config;
        Raycaster raycaster = new DefaultRaycaster();
		InputState inputState = new InputState();

		// 2. Initialize renderer
		if (rendererOverride != null) {
			this.renderer = rendererOverride;
		} else {
			this.renderer = new RealisticRenderer(renderingConfiguration, rocket, viewport.getFramebufferWidth(), viewport.getFramebufferHeight());
		}

		// 3. Initialize controllers
		this.cameraController = new CameraController(rocket, camera, scene, renderingConfiguration);
		this.cameraController.initialize(rocket, viewport.getAspectRatio());
		this.cameraController.addCameraChangeListener(ignored -> {
			LightController lightController = this.scene.getLightController();
			if (!lightController.areVisualizersVisible()) {
				return;
			}
			for (Light light : lightController.getLights()) {
				lightController.refreshVisualizer(light);
			}
		});
		
        this.inputHandler = new DefaultSceneInputProcessor(inputState, raycaster, scene, cameraController,
				renderingConfiguration);
        this.inputHandler.updateDimensions(viewport);

		this.rocketSynchronizer = new RocketSceneSynchronizer(this, this.scene, rocket);
		this.lastFrameTime = System.nanoTime();
	}

	// ---------- Simulation control helpers ----------

	/** Bind a pose to all rocket component objects and initialize the playback clock. */
	public void bindFlightPoseToRocket(PoseProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("pose provider null");
		}
		enqueueGlTask(() -> {
			for (var obj : scene.getObjects()) {
				if (obj.getRocketComponent() != null) {
					obj.setPoseProvider(provider);
				}
			}
		});
		this.playbackClock = new PlaybackClock(provider.getStartTime(), provider.getEndTime());
	}

	public PlaybackClock getPlaybackClock() {
		return playbackClock;
	}
}
