package info.openrocket.swing.gui.figure3d.scene.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Combined display, quality, and visual-effects settings for a figure3d view.
 */
public class RenderingConfiguration {

	private final VisualEffectsSettings visualEffects;
	private final GraphicsQualitySettings quality;
	private final DisplaySettings display;

	private final List<Consumer<RenderingConfiguration>> listeners = new ArrayList<>();

	/**
	 * Creates a new rendering configuration with default settings.
	 */
	public RenderingConfiguration() {
		this.visualEffects = new VisualEffectsSettings();
		this.quality = new GraphicsQualitySettings();
		this.display = new DisplaySettings();
	}

	/**
	 * Gets the visual effects settings (particles, motion blur, display elements).
	 *
	 * @return The visual effects configuration
	 */
	public VisualEffectsSettings getVisualEffects() {
		return visualEffects;
	}

	/**
	 * Gets the graphics quality settings (render quality, anti-aliasing, surface effects).
	 *
	 * @return The graphics quality configuration
	 */
	public GraphicsQualitySettings getQuality() {
		return quality;
	}

	/**
	 * Gets the display settings (render modes, wireframe, transparency).
	 *
	 * @return The display configuration
	 */
	public DisplaySettings getDisplay() {
		return display;
	}

	// Change Notification System

	/**
	 * Adds a listener that will be notified when any configuration changes.
	 *
	 * @param listener The listener to add
	 */
	public void addListener(Consumer<RenderingConfiguration> listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a previously added listener.
	 *
	 * @param listener The listener to remove
	 */
	public void removeListener(Consumer<RenderingConfiguration> listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies listeners after the configuration has changed.
	 */
	public void notifyListeners() {
		for (Consumer<RenderingConfiguration> listener : listeners) {
			listener.accept(this);
		}
	}

}
