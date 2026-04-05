package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.swing.gui.figure3d.scene.core.Light;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstraction for managing scene lights and optional visualizers.
 */
public interface LightController {
    void addLight(Light light, boolean addVisualizer);
    void addLight(Light light);
    List<Light> getLights();
    Light getLight(int index);
    void setLight(Light light, int index);
    void removeLight(Light light);
    boolean areVisualizersVisible();
    void setVisualizersVisible(boolean visible);
    void refreshVisualizer(Light light);
    void cleanup();
    void addLightChangeListener(Consumer<Light> listener);
    void removeLightChangeListener(Consumer<Light> listener);
    void fireLightChanged(Light light);
}
