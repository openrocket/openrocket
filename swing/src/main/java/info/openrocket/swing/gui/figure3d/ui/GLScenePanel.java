package info.openrocket.swing.gui.figure3d.ui;

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
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.utils.GLDebug;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_READ_BUFFER;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL11.glViewport;
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

	private Scene3DOrchestrator scene3DOrchestrator;
	private final KeyboardHandler keyboardHandler;

	private static final double CLICK_DRAG_THRESHOLD_SQ = 5 * 5;

	private final HUDPanel hudPanel;
	private BufferedImage hudImage;
	private Texture hudTexture;
	private Shader hudShader;
	private int hudVao;
	private int hudVbo; // Store VBO reference for cleanup
	private ByteBuffer hudImageBuffer;
	private IntBuffer hudIntBuffer; // Direct IntBuffer view for efficiency
	private Graphics2D hudGraphics; // Reusable Graphics2D context
	private final CountDownLatch glInitLatch = new CountDownLatch(1);

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

	private final Rocket rocket;
	private static final ExecutorService EXPORT_EXECUTOR;

	static {
		ThreadFactory exportThreadFactory = r -> {
			Thread t = new Thread(r, "gl-export-writer");
			t.setDaemon(true);
			return t;
		};
		EXPORT_EXECUTOR = Executors.newSingleThreadExecutor(exportThreadFactory);
	}

	public GLScenePanel(Rocket rocket, HUDPanel hudPanel) {
		super(createGLData());

		this.rocket = rocket;
		this.hudPanel = hudPanel;
		this.keyboardHandler = new KeyboardHandler();

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (!isDisplayable()) {
					return;
				}
				int width = Math.max(1, getWidth());
				int height = Math.max(1, getHeight());
				pendingWinWidth = width;
				pendingWinHeight = height;
				// Defer framebuffer queries to the GL thread to avoid touching the context on the EDT
				pendingFbWidth = -1;
				pendingFbHeight = -1;
				resizeRequested = true;
				hudNeedsUpdate = true;
			}
		});
	}

	private void addInputListeners() {
		if (scene3DOrchestrator == null) return;

		MouseAdapter mouseAdapter = new MouseAdapter() {
			private Point pressPoint;
			private Point lastPoint;
			private boolean isDragging;

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
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
				if (SwingUtilities.isLeftMouseButton(e)) {
					// We check for !isDragging to differentiate a click from a drag-release.
					// A double-click will also fire this event for the second click.
					if (!isDragging && pressPoint != null) {
						scene3DOrchestrator.getInputHandler().getInputState().clickPoint.set(pressPoint);
					}
					pressPoint = null;
					isDragging = false;
					cameraIsMoving = false; // Stop tracking camera movement
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && pressPoint != null) {
					if (!isDragging && pressPoint.distanceSq(e.getPoint()) > CLICK_DRAG_THRESHOLD_SQ) {
						isDragging = true;
					}

					if (isDragging) {
						// Check for modifier keys
						boolean isCtrlDown = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
						boolean isAltDown = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;

						// Update input state based on modifiers
						var inputState = scene3DOrchestrator.getInputHandler().getInputState();
						inputState.isPanning = isCtrlDown;
						inputState.isLightDragging = isAltDown;

						// Always update the drag delta
						float deltaX = e.getX() - lastPoint.x;
						float deltaY = e.getY() - lastPoint.y;
						inputState.dx += deltaX;
						inputState.dy += deltaY;
					}
					lastPoint = e.getPoint();
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scene3DOrchestrator.getInputHandler().getInputState().scrollDelta += e.getWheelRotation() * -1.0f;
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
				keyboardHandler.handleKeyEvent(e.getKeyCode(), 1);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				keyboardHandler.handleKeyEvent(e.getKeyCode(), 0);
			}
		});
	}

	private static GLData createGLData() {
		GLData data = new GLData();
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.profile = GLData.Profile.CORE;
		data.samples = 4;
		data.sRGB = true;
		// Disable swap interval to avoid vsync stalls when multiple canvases share a thread.
		data.swapInterval = 0;
		return data;
	}

	@Override
	public void initGL() {
		INIT_SEMAPHORE.acquireUninterruptibly();
		try {
			GL.createCapabilities();
			GLDebug.enableIfRequested("AWT-canvas");
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
			glEnable(GL_FRAMEBUFFER_SRGB);

			int winWidth = Math.max(1, getWidth());
			int winHeight = Math.max(1, getHeight());
			int fbWidth = Math.max(1, getFramebufferWidth());
			int fbHeight = Math.max(1, getFramebufferHeight());

			scene3DOrchestrator = Scene3DOrchestrator.builder(rocket, winWidth, winHeight, fbWidth, fbHeight)
					.build();
				SceneView scene = scene3DOrchestrator.getScene();

				// Create the scene mesh
				RocketMeshBuilder.buildRocketMesh(scene, rocket, scene3DOrchestrator.getRenderingConfiguration());
			//RocketMeshBuilder.createOriginAxes(scene, true, true);
			scene.setBackground(new SolidColorBackground(0.4f, 0.4f, 0.4f));

			// Focus on the rocket
			scene3DOrchestrator.focusOnRocket();

			if (this.hudPanel != null) {
				this.hudPanel.setSceneViewController(this.scene3DOrchestrator);
				this.hudPanel.setGLScenePanel(this);
			}

			// --- Initialize HUD rendering objects ---
			hudShader = new Shader("/shaders/ui/hud_vertex.glsl", "/shaders/ui/hud_fragment.glsl");

			// Set initial dimensions
			lastFramebufferWidth = getFramebufferWidth();
			lastFramebufferHeight = getFramebufferHeight();

			initHudTexture();
			initHudVao();

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

	/**
	 * Cleans up old resources and creates new ones with current framebuffer dimensions.
	 * IMPORTANT: Creates textures directly for THIS context - does not use shared pool.
	 */
	private void initHudTexture() {
		int fbWidth = getFramebufferWidth();
		int fbHeight = getFramebufferHeight();

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
		int paddedSize = (int)(fbWidth * fbHeight * 4 * 1.1);	// 10% padding

		hudImage = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
		hudImageBuffer = MemoryUtil.memAlloc(paddedSize);
		hudIntBuffer = hudImageBuffer.asIntBuffer();

		hudGraphics = hudImage.createGraphics();
		hudGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hudGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Create texture directly for THIS context - don't use shared pool
		hudTexture = new Texture(fbWidth, fbHeight, true);
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
				-1.0f,  1.0f,  0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				-1.0f, -1.0f,  0.0f, 1.0f,  // Bottom-left vertex maps to V=1.0
				1.0f, -1.0f,  1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0

				-1.0f,  1.0f,  0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				1.0f, -1.0f,  1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0
				1.0f,  1.0f,  1.0f, 0.0f   // Top-right vertex maps to V=0.0
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
		if (!glInitialized || !isDisplayable()) return;

		try {
			runInContext(() -> {
				// Null safety checks for critical objects
				if (scene3DOrchestrator == null) {
					return;
				}

				handlePendingResize();

				Renderer renderer = scene3DOrchestrator.getRenderer();
				SceneView sceneView = scene3DOrchestrator.getScene();

				if (renderer == null || sceneView == null) {
					log.debug("Skipping render - renderer or scene not available");
					return;
				}

				// --- 3D Scene Rendering & Export Logic ---
				handleKeyboardEvents();
				glViewport(0, 0, getFramebufferWidth(), getFramebufferHeight());
				scene3DOrchestrator.update();

				if (cameraIsMoving) {
					hudNeedsUpdate = true;
				}

				// Check for an export request before the main render
				if (scene3DOrchestrator.isExportRequested()) {
					handleExport(sceneView, renderer);
					return;
				}

				// --- Main Display Rendering (if not exporting) --
				renderer.render(sceneView, null, true);
				renderer.presentResolvedToCurrentFramebuffer();

				// --- 2D HUD Rendering - only update texture if needed ---
				if (hudNeedsUpdate || (hudPanel != null && hudPanel.needsRepaint())) {
					updateHudTexture();
					hudNeedsUpdate = false;
				}

				if (hudTexture != null && hudShader != null) {
					// Set GL state for 2D rendering
					glDisable(GL_DEPTH_TEST);
					glEnable(GL_BLEND);
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

				// --- Final Swap for display ---
				swapBuffers();
			});
		} catch (Exception ex) {
			log.debug("Skipping render - GL context not available");
		}
	}

	private void handleExport(SceneView sceneView, Renderer renderer) {
		boolean renderBackground = !scene3DOrchestrator.isExportTransparent();
		renderer.render(sceneView, null, renderBackground);

		int exportWidth = renderer.getRenderWidth();
		int exportHeight = renderer.getRenderHeight();
		String filePath = "export_" + System.currentTimeMillis() + ".png";

		ByteBuffer exportBuffer = captureResolvedFramebuffer(renderer, exportWidth, exportHeight);
		scene3DOrchestrator.clearExportRequest();

		renderer.presentResolvedToCurrentFramebuffer();
		swapBuffers();

		if (exportBuffer == null) {
			log.warn("Export skipped: no framebuffer data available");
			return;
		}

		EXPORT_EXECUTOR.submit(() -> writePng(exportBuffer, exportWidth, exportHeight, filePath));
	}

	private void updateHudTexture() {
		if (hudImage == null || hudPanel == null || hudGraphics == null) {
			return;
		}

		Runnable hudPainter = () -> {
			// The panel works in logical window coordinates
			int windowWidth = getWidth();
			int windowHeight = getHeight();
			if (windowHeight == 0) {
				return;
			}
			double dpiScale = (double) getFramebufferHeight() / (double) windowHeight;

			hudGraphics.setTransform(hudGraphics.getDeviceConfiguration().getDefaultTransform());
			hudGraphics.scale(dpiScale, dpiScale);

			// Clear with transparent background
			hudGraphics.setBackground(new Color(0, 0, 0, 0));
			hudGraphics.clearRect(0, 0, windowWidth, windowHeight);

			// Paint HUD panel on the EDT for Swing safety
			hudPanel.setBounds(0, 0, windowWidth, windowHeight);
			hudPanel.paint(hudGraphics);

			// Get pixel data directly without creating new arrays
			final int[] pixels = ((DataBufferInt) hudImage.getRaster().getDataBuffer()).getData();

			// Copy directly to IntBuffer view
			hudIntBuffer.clear();
			hudIntBuffer.put(pixels);
			hudImageBuffer.rewind();
		};

		if (SwingUtilities.isEventDispatchThread()) {
			hudPainter.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(hudPainter);
			} catch (Exception e) {
				log.warn("HUD update skipped (EDT busy): {}", e.getMessage());
				return;
			}
		}

		// Update texture
		glBindTexture(GL_TEXTURE_2D, hudTexture.getId());
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, hudImage.getWidth(), hudImage.getHeight(),
				GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, hudImageBuffer);
	}

	private void handlePendingResize() {
		if (!resizeRequested || !glInitialized) {
			return;
		}

		int width = pendingWinWidth;
		int height = pendingWinHeight;
		int fbWidth = pendingFbWidth;
		int fbHeight = pendingFbHeight;

		// If the framebuffer size wasn't captured on resize (EDT), query it now while the context is current
		if (fbWidth <= 0 || fbHeight <= 0) {
			fbWidth = Math.max(1, getFramebufferWidth());
			fbHeight = Math.max(1, getFramebufferHeight());
		}

		glViewport(0, 0, fbWidth, fbHeight);
		scene3DOrchestrator.resize(width, height, fbWidth, fbHeight);

		if (fbWidth != lastFramebufferWidth || fbHeight != lastFramebufferHeight) {
			initHudTexture();
			lastFramebufferWidth = fbWidth;
			lastFramebufferHeight = fbHeight;
			hudNeedsUpdate = true;
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

	private void writePng(ByteBuffer buffer, int width, int height, String filePath) {
		try {
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
		hudNeedsUpdate = true;
	}

	public void cleanup() {
		// Prevent any further rendering/resize operations
		glInitialized = false;

		// Try to clean up GL resources within context
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
		// Dispose the AWTGLCanvas context/resources
		try {
			disposeCanvas();
		} catch (Exception e) {
			log.debug("Canvas dispose failed: {}", e.getMessage());
		}

		// Always free native memory - this doesn't require GL context
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

		GpuResourceTracker.logLiveResources("Swing canvas cleanup (other canvases may still be active)", false);
	}
}
