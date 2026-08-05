package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import info.openrocket.swing.gui.figure3d.scene.graph.LightVisualizer;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static info.openrocket.swing.gui.figure3d.constants.RenderingConstants.MAX_LIGHTS;

/** Owns scene lights and their optional interactive visualizers. */
public class LightManager implements LightController {

	private static final Logger log = LoggerFactory.getLogger(LightManager.class);

	private final Scene scene;
	private final List<Light> lights = new ArrayList<>();
	private final Map<Light, List<SceneObject>> lightVisualsMap = new HashMap<>();
	private final LightVisualizer lightVisualizer = new LightVisualizer();
	private boolean visualizersVisible = false;
	private final List<Consumer<Light>> lightChangeListeners = new CopyOnWriteArrayList<>();

	/** @param scene scene that owns the visualizer objects */
	public LightManager(Scene scene) {
		this.scene = scene;
	}

	/**
	 * @param light the light to add to the scene
	 * @param addVisualizer if true, creates and adds a visual representation of the light
	 */
	@Override
	public void addLight(Light light, boolean addVisualizer) {
		if (lights.size() >= MAX_LIGHTS) {
			log.warn("Cannot add more than {} lights.", MAX_LIGHTS);
			return;
		}
		this.lights.add(light);

		updateLightVisualizer(light, addVisualizer || visualizersVisible);
	}

	@Override
	public void addLight(Light light) {
		addLight(light, visualizersVisible);
	}

	/**
	 * Gets the list of all active lights in the scene.
	 * 
	 * @return an unmodifiable view of all lights managed by this controller
	 */
	@Override
	public List<Light> getLights() {
		return Collections.unmodifiableList(lights);
	}

	/**
	 * Gets a specific light by its index in the light list.
	 * 
	 * @param index the zero-based index of the light to retrieve
	 * @return the light at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	@Override
	public Light getLight(int index) {
		if (index < 0 || index >= lights.size()) {
			throw new IndexOutOfBoundsException("Invalid light index: " + index);
		}
		return lights.get(index);
	}

	/**
	 * Replaces an existing light at the specified index with a new light.
	 * Automatically handles removal of old light visuals and creation of new ones.
	 * 
	 * @param light the new light to set at the specified index
	 * @param index the zero-based index where the light should be placed
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	@Override
	public void setLight(Light light, int index) {
		if (index < 0 || index >= lights.size()) {
			throw new IndexOutOfBoundsException("Invalid light index: " + index);
		}
		// Remove the old light's visuals first
		Light oldLight = lights.get(index);
		if (lightVisualsMap.containsKey(oldLight)) {
			lightVisualsMap.get(oldLight).forEach(scene::removeObject);
			lightVisualsMap.remove(oldLight);
		}

		// Set the new light
		lights.set(index, light);

		// Add the new light's visuals when enabled
		updateLightVisualizer(light, visualizersVisible);
	}

	/**
	 * Removes a light from the manager and its visual representation from the scene.
	 * Both the logical light and any associated visual objects will be removed.
	 * 
	 * @param light the light to remove from the scene
	 */
	@Override
	public void removeLight(Light light) {
		if (lights.remove(light)) {
			removeLightVisualizer(light);
		}
	}

	@Override
	public boolean areVisualizersVisible() {
		return visualizersVisible;
	}

	@Override
	public void setVisualizersVisible(boolean visible) {
		if (visualizersVisible == visible) {
			return;
		}
		visualizersVisible = visible;
		for (Light light : lights) {
			updateLightVisualizer(light, visible);
		}
	}

	@Override
	public void refreshVisualizer(Light light) {
		if (!visualizersVisible) {
			return;
		}
		List<SceneObject> visuals = lightVisualsMap.get(light);
		if (visuals == null) {
			return;
		}
		lightVisualizer.updateVisualsForLight(light, visuals, scene);
	}

	@Override
	public void addLightChangeListener(Consumer<Light> listener) {
		lightChangeListeners.add(listener);
	}

	@Override
	public void removeLightChangeListener(Consumer<Light> listener) {
		lightChangeListeners.remove(listener);
	}

	@Override
	public void fireLightChanged(Light light) {
		for (Consumer<Light> listener : lightChangeListeners) {
			listener.accept(light);
		}
	}

	/**
	 * Cleans up all resources associated with the light manager.
	 * This method handles cleanup of light visualizers and prepares the manager
	 * for disposal. Light objects themselves are lightweight and don't require
	 * explicit cleanup, but their visual representations are managed by the scene.
	 * 
	 * <p>Note: Visual objects (SceneObjects) are cleaned up by the main scene
	 * cleanup process. This method is provided for consistency and future extensions.</p>
	 */
	@Override
	public void cleanup() {
		// Light objects themselves don't have resources to clean up.  
		// The visualizer SceneObjects will be cleaned up by the Scene's main cleanup method.
	}

	private void updateLightVisualizer(Light light, boolean shouldShow) {
		if (shouldShow) {
			List<SceneObject> existingVisuals = lightVisualsMap.get(light);
			if (existingVisuals != null) {
				lightVisualizer.updateVisualsForLight(light, existingVisuals, scene);
				return;
			}
			List<SceneObject> visuals = lightVisualizer.createVisualsForLight(light, scene);
			lightVisualsMap.put(light, visuals);
			this.scene.addObjects(visuals);
			return;
		}
		removeLightVisualizer(light);
	}

	private void removeLightVisualizer(Light light) {
		List<SceneObject> visuals = lightVisualsMap.remove(light);
		if (visuals != null) {
			visuals.forEach(scene::removeObject);
		}
	}
}
