package info.openrocket.swing.gui.figure3d.scene.events;

/**
 * Listener for orchestrator export requests.
 */
@FunctionalInterface
public interface ExportListener {
	void onExportRequested(boolean transparentBackground);
}

