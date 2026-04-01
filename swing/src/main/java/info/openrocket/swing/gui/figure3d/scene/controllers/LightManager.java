package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.LightVisualizer;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all light sources and their visual representations within the 3D scene.
 * This controller provides comprehensive lighting management for the OpenRocket 3D visualization,
 * including light creation, positioning, and visual representation. It coordinates between
 * the lighting system and the scene management to ensure proper illumination of rocket components
 * and other 3D objects.
 * 
 * <p>The manager handles both the logical light sources used for rendering calculations
 * and their optional visual representations (light visualizers) that appear in the scene
 * to help users understand the lighting setup.</p>
 * 
 * <p>Enforces shader limitations by restricting the maximum number of concurrent lights
 * to maintain rendering performance.</p>
 */
public class LightManager implements LightController {

	private static final Logger log = LoggerFactory.getLogger(LightManager.class);

	private final Scene scene;
	private final List<Light> lights = new ArrayList<>();
	private final Map<Light, List<SceneObject>> lightVisualsMap = new HashMap<>();
	private final LightVisualizer lightVisualizer = new LightVisualizer();
	private boolean visualizersVisible = false;

	/**
	 * Constructs a new LightManager for the specified scene.
	 * 
	 * @param scene the scene that will contain the lights and their visual representations
	 */
	public LightManager(Scene scene) {
		this.scene = scene;
	}

	/**
	 * Adds a light to the manager and optionally creates its corresponding visualizer in the scene.
	 * The light will be included in rendering calculations, and if visualization is enabled,
	 * a visual representation will be added to help users understand the lighting setup.
	 * 
	 * @param light the light to add to the scene
	 * @param addVisualizer if true, creates and adds a visual representation of the light
	 */
    @Override
    public void addLight(Light light, boolean addVisualizer) {
		if (lights.size() >= 10) { // Corresponds to MAX_LIGHTS in the shader
			log.warn("Cannot add more than 10 lights.");
			return;
		}
		this.lights.add(light);

		updateLightVisualizer(light, addVisualizer || visualizersVisible);
	}

	/**
	 * Adds a light to the manager without creating a visual representation.
	 * This is a convenience method that calls addLight(light, false).
	 * 
	 * @param light the light to add to the scene
	 */
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
        return lights;
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
			lightVisualsMap.get(oldLight).forEach(visual -> scene.getObjects().remove(visual));
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
			if (lightVisualsMap.containsKey(light)) {
				return;
			}
			List<SceneObject> visuals = lightVisualizer.createVisualsForLight(light);
			lightVisualsMap.put(light, visuals);
			this.scene.addObjects(visuals);
			return;
		}
		removeLightVisualizer(light);
	}

	private void removeLightVisualizer(Light light) {
		List<SceneObject> visuals = lightVisualsMap.remove(light);
		if (visuals != null) {
			visuals.forEach(visual -> scene.getObjects().remove(visual));
		}
	}
}
