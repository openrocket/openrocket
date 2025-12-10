package info.openrocket.swing.gui.figure3d;

import info.openrocket.swing.gui.figure3d.ui.GLSceneWindow;
import javax.swing.SwingUtilities;

/**
 * Main application entry point for the OpenRocket 3D visualization engine.
 * Sets up Swing window with OpenGL rendering and HUD overlay.
 *
 * <p>Uses a thread-safe rendering pipeline that prevents deadlocks and race conditions
 * when running multiple GL windows simultaneously.</p>
 */
public class Main {

	/**
	 * Application entry point that initializes OpenRocket and starts the 3D engine.
	 * @param args command line arguments (unused)
	 */
	public static void main(String[] args) {
		info.openrocket.core.startup.OpenRocketCore.initialize();

		SwingUtilities.invokeLater(() -> {
			GLSceneWindow.create("LWJGL3 Engine in Swing", 1280, 720, 200, 200);
			GLSceneWindow.create("LWJGL3 Engine in Swing 2", 1280, 720, 500, 260);
		});
	}
}
