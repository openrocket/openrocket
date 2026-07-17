package info.openrocket.swing.gui.figure3d.core;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.DemoFactory;
import info.openrocket.swing.gui.figure3d.export.ImageExporter;
import info.openrocket.swing.gui.figure3d.export.PngExporter;
import info.openrocket.swing.gui.figure3d.input.KeyboardHandler;
import info.openrocket.swing.gui.figure3d.input.MouseInputHandler;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.window.CursorQuery;
import info.openrocket.swing.gui.figure3d.window.FramebufferAware;
import info.openrocket.swing.gui.figure3d.window.GLFWWindowManager;
import info.openrocket.swing.gui.figure3d.window.KeyboardEventSource;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderer;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;

public class Standalone3DEngine implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Standalone3DEngine.class);

	private WindowManager windowManager;
	private Scene3DOrchestrator scene3DOrchestrator;
	private KeyboardHandler keyboardHandler;
	private double lastTime;
	private int frames;

	@Override
	public void run() {
		try {
			init();
			loop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}
	}

	private void init() throws Exception {
		windowManager = new GLFWWindowManager();
		windowManager.createWindow(1280, 720, "LWJGL Engine");
		windowManager.makeContextCurrent();

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_FRAMEBUFFER_SRGB);
		glClearColor(0.1f, 0.1f, 0.12f, 1.0f);

        int[] winSize = windowManager.getWindowSize();
        int[] fbSize = ((FramebufferAware) windowManager).getFramebufferSize();

		Rocket rocket  = DemoFactory.createTestRocket();
		scene3DOrchestrator = Scene3DOrchestrator.builder(rocket, winSize[0], winSize[1], fbSize[0], fbSize[1])
				.withQualityMode()
				.build();
		SceneView scene = scene3DOrchestrator.getScene();
		scene3DOrchestrator.rebuildRocketScene(false);
		DemoFactory.setupBackground(scene, 1);
		scene3DOrchestrator.focusOnRocket();

		keyboardHandler = new KeyboardHandler();
        MouseInputHandler mouseInputHandler = new MouseInputHandler(
                scene3DOrchestrator.getInputHandler().getInputState(),
                (CursorQuery) windowManager,
                keyboardHandler);
        setupCallbacks(keyboardHandler, mouseInputHandler);

		DemoFactory.setupDemoKeyboardHandling(keyboardHandler, scene3DOrchestrator.getScene(), scene3DOrchestrator);

		windowManager.showWindow();
		lastTime = glfwGetTime();
	}

	private void setupCallbacks(KeyboardHandler keyboardHandler, MouseInputHandler mouseInputHandler) {
        windowManager.setupInputCallbacks(mouseInputHandler);
        if (windowManager instanceof KeyboardEventSource kes) {
            kes.setupKeyboardCallbacks(keyboardHandler);
        }

		// The resize callback now handles all resize logic, including redrawing.
        ((FramebufferAware) windowManager).setupFramebufferSizeCallback((width, height) -> {
            if (width > 0 && height > 0 && scene3DOrchestrator != null) {
                // 1. Update the viewport to the new dimensions.
                glViewport(0, 0, width, height);

                // 2. Update the camera's projection matrix and any renderer framebuffers.
                int[] newWinSize = windowManager.getWindowSize();
                scene3DOrchestrator.resize(newWinSize[0], newWinSize[1], width, height);

				// 3. Redraw the scene immediately within the callback.
				// This is critical because the main loop is paused during the resize.
				scene3DOrchestrator.update();
				scene3DOrchestrator.getRenderer().render(scene3DOrchestrator.getScene(), true);
				windowManager.swapBuffers();
			}
		});
	}

	private void loop() {
		while (!windowManager.shouldClose()) {
			// This will now block during a resize, and the callback will handle drawing.
			// When not resizing, it will process events and then proceed to the render logic below.
			windowManager.pollEvents();

			// Process keyboard actions each frame
			if (keyboardHandler != null) {
				keyboardHandler.handleQueuedEvents();
			}

			// Handle PNG Export Request
			GLRenderer renderer = scene3DOrchestrator.getRenderer();
			if (scene3DOrchestrator.isExportRequested()) {
        int[] fbSize = ((FramebufferAware) windowManager).getFramebufferSize();
        glViewport(0, 0, fbSize[0], fbSize[1]);

				// Render the scene for export
				renderer.render(scene3DOrchestrator.getScene(), !scene3DOrchestrator.isExportTransparent());

				// Export from the resolved off-screen buffer
				try {
					String filePath = "export_" + System.currentTimeMillis() + ".png";
					ImageExporter exporter = new PngExporter();
					exporter.export(renderer.getResolvedFramebufferId(), renderer.getRenderWidth(), renderer.getRenderHeight(), filePath);
				} catch (IOException e) {
					log.error("Failed to export screenshot: {}", e.getMessage());
				}

				renderer.presentResolvedToCurrentFramebuffer();
				windowManager.swapBuffers();

				// Clear the request
				scene3DOrchestrator.clearExportRequest();
			}

			// The main loop's rendering is now for when the window is NOT being resized.
			int[] fbSize = ((FramebufferAware) windowManager).getFramebufferSize();
			glViewport(0, 0, fbSize[0], fbSize[1]);

			scene3DOrchestrator.update();
			renderer.render(scene3DOrchestrator.getScene(), true);
			renderer.presentResolvedToCurrentFramebuffer();
			windowManager.swapBuffers();
			updateFpsCounter();
		}
	}

	private void updateFpsCounter() {
		frames++;
		double currentTime = glfwGetTime();
		if (currentTime - lastTime >= 1.0) {
			String title = String.format("LWJGL Engine - %.2f FPS", (frames / (currentTime - lastTime)));
			windowManager.setWindowTitle(title);
			frames = 0;
			lastTime = currentTime;
		}
	}

	private void cleanup() {
		if (scene3DOrchestrator != null) {
			scene3DOrchestrator.shutdown();
			if (scene3DOrchestrator.getScene() != null) scene3DOrchestrator.getScene().cleanup();
			if (scene3DOrchestrator.getRenderer() != null) scene3DOrchestrator.getRenderer().cleanup();
		}
		if (windowManager != null) {
			windowManager.cleanup();
		}
		GpuResourceTracker.logLiveResources("Standalone3DEngine cleanup");
	}

	public static void main(String[] args) {
		info.openrocket.core.startup.OpenRocketCore.initialize();

		Standalone3DEngine engine = new Standalone3DEngine();
		engine.run();
	}
}
