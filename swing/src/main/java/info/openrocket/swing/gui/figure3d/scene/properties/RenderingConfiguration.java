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

	public RenderingConfiguration() {
		this.visualEffects = new VisualEffectsSettings();
		this.quality = new GraphicsQualitySettings();
		this.display = new DisplaySettings();
	}

	public VisualEffectsSettings getVisualEffects() {
		return visualEffects;
	}

	public GraphicsQualitySettings getQuality() {
		return quality;
	}

	public DisplaySettings getDisplay() {
		return display;
	}

	public void addListener(Consumer<RenderingConfiguration> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<RenderingConfiguration> listener) {
		listeners.remove(listener);
	}

	/** Notifies listeners after callers finish a related group of mutations. */
	public void notifyListeners() {
		for (Consumer<RenderingConfiguration> listener : listeners) {
			listener.accept(this);
		}
	}
}
