package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * Abstraction for camera control behaviors used by the orchestrator.
 */
public interface CameraControls {
	void initialize(Rocket rocket, float aspectRatio);
	void focusOnRocket();
	void focusOnBounds(Vector3f center, Vector3f dimensions);
	void focusOnBounds(Vector3f center, Vector3f dimensions,
			float closestDistanceFactor, float farthestDistanceFactor);
	void refitOnRocketBoundsChange();
	void resetView();
	void handleScroll(float scrollDelta);

	/** Scroll with cursor position for zoom-toward-cursor behaviour. */
	default void handleScroll(float scrollDelta, int mouseX, int mouseY, int viewportWidth, int viewportHeight) {
		handleScroll(scrollDelta);
	}
	void handleOrbit(float dx, float dy);
	void handlePan(float dx, float dy, int viewportWidth, int viewportHeight);
	void setPanEnabled(boolean enabled);
	void resize(float newAspectRatio);
	void update();
	double getZoomScale();
	void setZoomScale(double scale);
	void setZoomFitting(boolean zoomFitting);

	/**
	 * Returns a multiplier in (0, 1] that slows orbit/rotate sensitivity as the
	 * user zooms in. At default zoom the factor is 1.0; deeper zooms shrink it
	 * proportionally, floored at 0.25 so motion never feels completely stuck.
	 * Without this compensation, drag-rotation feels hyper-sensitive at high
	 * zoom because each camera-degree maps to many more on-screen pixels.
	 */
	default float computeZoomSensitivityFactor() {
		double zoomScale = getZoomScale();
		if (zoomScale <= 1.0 || Double.isNaN(zoomScale) || Double.isInfinite(zoomScale)) {
			return 1.0f;
		}
		return (float) Math.max(0.25, 1.0 / zoomScale);
	}
	boolean isZoomFitting();

	/** Rocket geometric center in world coordinates, or null if empty. */
	Vector3f computeRocketCenter();

	/** Rocket model-space bounding-box size, or null if empty. */
	Vector3f computeRocketSize();

	Camera getCamera();
	void addCameraChangeListener(Consumer<Camera> listener);
	void removeCameraChangeListener(Consumer<Camera> listener);
}
