package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.scene.properties.ViewportDimensions;

/**
 * Abstraction for scene-level input processing used by the orchestrator.
 */
public interface SceneInputProcessor {
	void processInput();
	void updateDimensions(ViewportDimensions viewport);
	InputState getInputState();
}

