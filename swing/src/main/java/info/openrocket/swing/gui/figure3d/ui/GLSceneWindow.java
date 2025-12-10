package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.DemoFactory;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Helper that encapsulates the boilerplate required to host a {@link GLScenePanel}
 * inside a Swing {@link JFrame}. Creates the rocket, HUD panel, canvas, and a refresh
 * timer so callers can instantiate new windows with a single line of code.
 *
 * <p>Supports multiple simultaneous windows - each window has its own GL context
 * and resources.</p>
 */
public final class GLSceneWindow {

	private static final Logger log = LoggerFactory.getLogger(GLSceneWindow.class);

	private static final int MAX_CONSECUTIVE_ERRORS = 5;
	private static final long FRAME_INTERVAL_MS = 16;

	private final JFrame frame;
	private final GLScenePanel canvas;
	private final ScheduledExecutorService renderExecutor;
	private volatile boolean disposed = false;
	private int consecutiveRenderErrors = 0;

	private GLSceneWindow(String title, int width, int height, int x, int y, Rocket rocket) {
		this.frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setPreferredSize(new Dimension(width, height));

		// Handle window close via X button - ensures proper cleanup
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		RocketInfo rocketInfo = new RocketInfo(rocket.getSelectedConfiguration());
		HUDPanel hudPanel = new HUDPanel(rocket, rocketInfo);

		this.canvas = new GLScenePanel(rocket, hudPanel);
		frame.add(canvas);
		frame.pack();
		frame.setLocation(x, y);
		frame.setVisible(true);

		ThreadFactory renderThreadFactory = r -> {
			Thread t = new Thread(r, "gl-render-" + title);
			t.setDaemon(true);
			return t;
		};
		this.renderExecutor = Executors.newSingleThreadScheduledExecutor(renderThreadFactory);
		startRenderLoop(title);
	}

	/**
	 * Creates and shows a new GL scene window with a default demo rocket.
	 */
	public static GLSceneWindow create(String title, int width, int height, int x, int y) {
		Rocket rocket = DemoFactory.createTestRocket();
		GLSceneWindow window = new GLSceneWindow(title, width, height, x, y, rocket);
		// Wait briefly for GL initialization on a background thread so we don't block the EDT
		Thread initWatcher = new Thread(() -> {
			boolean ready = window.getCanvas().awaitInitialized(2000);
			if (!ready) {
				log.warn("GL context for {} did not initialize within timeout", title);
			}
		}, "gl-init-wait-" + title);
		initWatcher.setDaemon(true);
		initWatcher.start();
		return window;
	}

	private void startRenderLoop(String title) {
		renderExecutor.execute(() -> {
			// Wait for the AWT peer to exist and have a real size before the first render
			long deadline = System.currentTimeMillis() + 5_000;
			while (System.currentTimeMillis() < deadline) {
				if (disposed) {
					return;
				}
				if (canvas.isDisplayable() && canvas.getWidth() > 0 && canvas.getHeight() > 0) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
					return;
				}
			}

			if (disposed) {
				return;
			}

			// Kick off initialization; this triggers initGL once the context is available.
			try {
				canvas.render();
			} catch (Exception e) {
				log.error("Initial render failed for {}", title, e);
			}

			boolean initialized = canvas.awaitInitialized(3_000) && !canvas.glInitFailed;
			if (!initialized) {
				log.error("GL context did not initialize for {} within timeout", title);
				renderExecutor.shutdownNow();
				dispose();
				return;
			}

			while (!disposed) {
				if (!canvas.isDisplayable()) {
					renderExecutor.shutdownNow();
					return;
				}
				if (canvas.glInitFailed) {
					log.error("GL init failed for {}", title);
					renderExecutor.shutdownNow();
					dispose();
					return;
				}
				try {
					canvas.render();
					consecutiveRenderErrors = 0; // Reset on successful render
				} catch (Exception ex) {
					consecutiveRenderErrors++;
					log.warn("Render error ({})", consecutiveRenderErrors, ex);
					if (consecutiveRenderErrors >= MAX_CONSECUTIVE_ERRORS) {
						log.error("Too many consecutive render errors, disposing window");
						renderExecutor.shutdownNow();
						dispose();
						return;
					}
				}

				try {
					Thread.sleep(FRAME_INTERVAL_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		});
	}

	public JFrame getFrame() {
		return frame;
	}

	public GLScenePanel getCanvas() {
		return canvas;
	}

	/**
	 * Returns whether this window has been disposed.
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Disposes of all resources associated with this window.
	 * This method is safe to call multiple times.
	 */
	public void dispose() {
		if (disposed) {
			return;
		}
		disposed = true;

		log.debug("Disposing GLSceneWindow: {}", frame.getTitle());

		// Stop render thread first to prevent further GL operations
		renderExecutor.shutdownNow();

		// Clean up GL resources
		try {
			canvas.cleanup();
		} catch (Exception e) {
			log.warn("Error during canvas cleanup: {}", e.getMessage());
		}

		// Finally dispose the frame
		frame.dispose();
	}
}
