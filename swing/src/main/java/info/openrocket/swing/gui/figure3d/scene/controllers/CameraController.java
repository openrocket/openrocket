package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Controls camera operations and behaviors within the 3D scene management system.
 * This controller handles camera positioning, movement, and user interactions such as orbiting,
 * panning, and zooming. It integrates with the OpenRocket rocket model to provide appropriate
 * camera positioning and focus behaviors for rocket visualization.
 * 
 * <p>Separated from Scene3DOrchestrator to improve separation of concerns and maintain
 * a clean architecture for camera management within the 3D visualization pipeline.</p>
 */
public class CameraController implements CameraControls {
    private final Rocket rocket;
    private final Camera camera;
    private final SceneView scene;
    private final RenderingConfiguration renderingConfiguration;
    private final List<Consumer<Camera>> cameraChangeListeners = new CopyOnWriteArrayList<>();
    private float focusedDistance;
    // Whether the camera should track the fitted distance. This is explicit state
    // rather than "distance ≈ fitted distance" so that a resize-triggered re-fit
    // cannot race with (and overwrite) a manual zoom that was applied in between.
    private volatile boolean zoomFitting = true;
    
    /**
     * Constructs a new CameraController with the specified camera and scene references.
     *
	 * @param rocket the rocket instance
     * @param camera the camera instance to control
     * @param scene the scene containing the rocket and other objects to interact with
     * @param renderingConfiguration the active rendering configuration
     */
    public CameraController(Rocket rocket, Camera camera, SceneView scene,
            RenderingConfiguration renderingConfiguration) {
        this.rocket = rocket;
		this.camera = camera;
        this.scene = scene;
        this.renderingConfiguration = renderingConfiguration;
    }
    
    /**
     * Initialize the camera with default settings for the given rocket.
     * Sets the default side view and establishes the center of interest based on rocket dimensions.
     * 
     * @param rocket the rocket to initialize the camera for
     * @param aspectRatio the aspect ratio of the viewport for camera projection
     */
    @Override
    public void initialize(Rocket rocket, float aspectRatio) {
        camera.setView(CameraConstants.View.SIDE);
        camera.setAspectRatio(aspectRatio);
        
        // Set initial center of interest based on rocket length
        Vector3f centerOfInterest = new Vector3f(
            (float) (rocket.getLength() / 2.0 * RocketMeshBuilder.WORLD_SCALE), 
            0, 
            0
        );
        camera.setCenterOfInterest(centerOfInterest);
        focusedDistance = camera.getDistance();
        scene.updateRocketPivotFromCamera();
    }
    
    /**
     * Focus the camera on the rocket by calculating appropriate bounds and position.
     * Automatically determines the optimal camera distance and center point to frame
     * the entire rocket within the viewport.
     */
	@Override
	public void focusOnRocket() {
		if (rocket == null) {
			return;
		}

        BoundingBox bounds = rocket.getBoundingBox();
		if (bounds == null || bounds.isEmpty()) {
			return;
		}
		CoordinateIF minBounds = bounds.min.multiply(RocketMeshBuilder.WORLD_SCALE);
		CoordinateIF maxBounds = bounds.max.multiply(RocketMeshBuilder.WORLD_SCALE);

		Vector3f minCorner = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vector3f maxCorner = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		updateTransformedBounds(minCorner, maxCorner, minBounds, maxBounds);

        // 1. Center of Interest
        Vector3f rocketCenter = new Vector3f(
				(minCorner.x + maxCorner.x) * 0.5f,
				(minCorner.y + maxCorner.y) * 0.5f,
				(minCorner.z + maxCorner.z) * 0.5f
		);
		camera.setCenterOfInterest(rocketCenter);

        // 2. Calculate distance
		Vector3f dimensions = new Vector3f(
				maxCorner.x - minCorner.x,
				maxCorner.y - minCorner.y,
				maxCorner.z - minCorner.z
		);
        camera.fitBounds(dimensions);
        camera.resetViewOffset();
        focusedDistance = camera.getDistance();
        zoomFitting = true;
        scene.updateRocketPivotFromCamera();
        notifyCameraChanged();
    }

	@Override
	public void resetView() {
		camera.setView(CameraConstants.View.SIDE);
		camera.resetViewOffset();
		camera.update();
		scene.updateRocketPivotFromCamera();
        notifyCameraChanged();
	}
    
    /**
     * Handle scroll input for camera dolly (zoom).
     *
     * @param scrollDelta the scroll wheel delta value for zoom amount
     */
    @Override
    public void handleScroll(float scrollDelta) {
        camera.dolly(scrollDelta);
        zoomFitting = false;
        notifyCameraChanged();
    }

    /**
     * Handle scroll with cursor position for zoom-toward-cursor behaviour.
     * Moves the orbit pivot (centerOfInterest) toward the world point under the cursor
     * so that subsequent orbit drags rotate around what the user zoomed into.
     */
    @Override
    public void handleScroll(float scrollDelta, int mouseX, int mouseY, int viewportWidth, int viewportHeight) {
        float prevDistance = camera.getDistance();
        camera.dolly(scrollDelta);
        zoomFitting = false;
        float newDistance = camera.getDistance();

        // Only update pivot if the distance actually changed (i.e. not already at zoom limit).
        if (viewportWidth > 0 && viewportHeight > 0 && Math.abs(newDistance - prevDistance) > 1e-6f) {
            Vector3f cursorWorld = unprojectToCoIPlane(mouseX, mouseY, viewportWidth, viewportHeight);
            if (cursorWorld != null) {
                // Only adjust centerOfInterest.x — the rocket lies along the world X axis,
                // matching the same constraint applied in camera.pan().
                float factor = 1.0f - (newDistance / prevDistance);
                Vector3f coi = camera.getCenterOfInterest();
                coi.x += factor * (cursorWorld.x - coi.x);
                camera.setCenterOfInterest(coi);
            }
        }

        notifyCameraChanged();
    }

    /**
     * Unprojects the screen-space cursor position onto the plane passing through
     * the current centerOfInterest, perpendicular to the camera's view direction.
     * Returns the world-space intersection point, or null if the ray is parallel to the plane.
     */
    private Vector3f unprojectToCoIPlane(int mouseX, int mouseY, int viewportWidth, int viewportHeight) {
        float ndcX = (2.0f * mouseX / viewportWidth) - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY / viewportHeight); // flip Y: screen down → NDC up

        Matrix4f invVP = new Matrix4f(camera.getProjectionMatrix())
                .mul(camera.getViewMatrix())
                .invert();

        Vector4f nearH = invVP.transform(new Vector4f(ndcX, ndcY, -1.0f, 1.0f));
        Vector4f farH  = invVP.transform(new Vector4f(ndcX, ndcY,  1.0f, 1.0f));
        nearH.div(nearH.w);
        farH.div(farH.w);

        Vector3f rayOrigin = new Vector3f(nearH.x, nearH.y, nearH.z);
        Vector3f rayDir = new Vector3f(farH.x - nearH.x, farH.y - nearH.y, farH.z - nearH.z).normalize();

        // Plane through the visual look-at target (orbit pivot + viewOffset), normal = camera forward.
        Vector3f effectiveLookAt = camera.getEffectiveLookAt();
        Vector3f camPos = camera.getPosition();
        Vector3f planeNormal = new Vector3f(effectiveLookAt).sub(camPos).normalize();

        float denom = planeNormal.dot(rayDir);
        if (Math.abs(denom) < 1e-6f) return null; // ray nearly parallel to plane

        float t = planeNormal.dot(new Vector3f(effectiveLookAt).sub(rayOrigin)) / denom;
        if (t < 0.0f) return null; // intersection behind camera

        return new Vector3f(rayDir).mul(t).add(rayOrigin);
    }
    
    /**
     * Handle mouse drag for camera orbit around the center of interest.
     * 
     * @param dx the horizontal mouse movement delta
     * @param dy the vertical mouse movement delta
     */
    @Override
    public void handleOrbit(float dx, float dy) {
        float sensitivity = CameraConstants.BASE_VIEW_ROTATION_SENSITIVITY *
                renderingConfiguration.getVisualEffects().getDragRotationSensitivity() *
                computeZoomSensitivityFactor();
        camera.orbit(dx, dy, sensitivity);
        notifyCameraChanged();
    }
    
    /**
     * Handle mouse drag for camera panning to translate the view.
     * 
     * @param dx the horizontal mouse movement delta
     * @param dy the vertical mouse movement delta
     */
    @Override
    public void handlePan(float dx, float dy, int viewportWidth, int viewportHeight) {
        camera.pan(dx, dy, viewportWidth, viewportHeight);
        // Keep the rocket rotation pivot aligned with the horizontally panned focus point.
        scene.updateRocketPivotFromCamera();
        notifyCameraChanged();
    }
    
    /**
     * Update camera state and internal matrices.
     * This method should be called every frame to ensure camera transformations
     * are properly applied and synchronized with the rendering pipeline.
     */
    @Override
    public void update() {
        camera.update();
    }

    @Override
    public double getZoomScale() {
        if (focusedDistance <= 0.0f) {
            return 1.0;
        }
        return focusedDistance / camera.getDistance();
    }

    @Override
    public void setZoomScale(double scale) {
        if (focusedDistance <= 0.0f || Double.isNaN(scale) || Double.isInfinite(scale) || scale <= 0.0) {
            return;
        }
        camera.setDistance((float) (focusedDistance / scale));
        zoomFitting = Math.abs(scale - 1.0) < 0.01;
        notifyCameraChanged();
    }

    @Override
    public boolean isZoomFitting() {
        if (focusedDistance <= 0.0f) {
            return true;
        }
        return zoomFitting;
    }
    
    /**
     * Handle window/framebuffer resize by updating the camera's aspect ratio.
     * 
     * @param newAspectRatio the new aspect ratio after window resize
     */
    @Override
    public void resize(float newAspectRatio) {
        camera.setAspectRatio(newAspectRatio);
    }
    
    /**
     * Get the underlying camera instance for direct access to camera properties.
     * 
     * @return the camera instance managed by this controller
     */
    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public void addCameraChangeListener(Consumer<Camera> listener) {
        if (listener != null) {
            cameraChangeListeners.add(listener);
        }
    }

    @Override
    public void removeCameraChangeListener(Consumer<Camera> listener) {
        cameraChangeListeners.remove(listener);
    }

    private void notifyCameraChanged() {
        for (Consumer<Camera> listener : cameraChangeListeners) {
            listener.accept(camera);
        }
    }

	private void updateTransformedBounds(Vector3f minCorner, Vector3f maxCorner,
			CoordinateIF minBounds, CoordinateIF maxBounds) {
		float[] xValues = {(float) minBounds.getX(), (float) maxBounds.getX()};
		float[] yValues = {(float) minBounds.getY(), (float) maxBounds.getY()};
		float[] zValues = {(float) minBounds.getZ(), (float) maxBounds.getZ()};
		Vector3f corner = new Vector3f();
		Vector3f transformed = new Vector3f();

		for (float x : xValues) {
			for (float y : yValues) {
				for (float z : zValues) {
					corner.set(x, y, z);
					scene.transformRocketPoint(corner, transformed);
					minCorner.min(transformed);
					maxCorner.max(transformed);
				}
			}
		}
	}
}
