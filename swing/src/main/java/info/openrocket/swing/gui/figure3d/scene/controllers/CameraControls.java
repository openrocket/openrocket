package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;

/**
 * Abstraction for camera control behaviors used by the orchestrator.
 */
public interface CameraControls {
    void initialize(Rocket rocket, float aspectRatio);
    void focusOnRocket();
    void handleScroll(float scrollDelta);
    void handleOrbit(float dx, float dy);
    void handlePan(float dx, float dy);
    void resize(float newAspectRatio);
    void update();
    Camera getCamera();
}

