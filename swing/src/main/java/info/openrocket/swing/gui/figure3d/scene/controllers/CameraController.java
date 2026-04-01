package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.utils.VectorUtils;
import org.joml.Vector3f;

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
    private float focusedDistance;
    
    /**
     * Constructs a new CameraController with the specified camera and scene references.
     *
	 * @param rocket the rocket instance
     * @param camera the camera instance to control
     * @param scene the scene containing the rocket and other objects to interact with
     */
    public CameraController(Rocket rocket, Camera camera, SceneView scene) {
        this.rocket = rocket;
		this.camera = camera;
        this.scene = scene;
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

        // 1. Center of Interest
        Vector3f rocketCenter = new Vector3f(
				(float) ((minBounds.getX() + maxBounds.getX()) / 2.0),
				(float) ((minBounds.getY() + maxBounds.getY()) / 2.0),
				(float) ((minBounds.getZ() + maxBounds.getZ()) / 2.0)
		);
		camera.setCenterOfInterest(rocketCenter);

        // 2. Calculate distance
        // TODO: this doesn't always work well with fin sets....
		Vector3f dimensions = new Vector3f(
				(float) (maxBounds.getX() - minBounds.getX()),
				(float) (maxBounds.getY() - minBounds.getY()),
				(float) (maxBounds.getZ() - minBounds.getZ())
		);
        camera.fitBounds(dimensions);
        focusedDistance = camera.getDistance();
    }
    
    /**
     * Handle scroll input for camera dolly (zoom).
     * 
     * @param scrollDelta the scroll wheel delta value for zoom amount
     */
    @Override
    public void handleScroll(float scrollDelta) {
        camera.dolly(scrollDelta);
    }
    
    /**
     * Handle mouse drag for camera orbit around the center of interest.
     * 
     * @param dx the horizontal mouse movement delta
     * @param dy the vertical mouse movement delta
     */
    @Override
    public void handleOrbit(float dx, float dy) {
        camera.orbit(dx, dy);
    }
    
    /**
     * Handle mouse drag for camera panning to translate the view.
     * 
     * @param dx the horizontal mouse movement delta
     * @param dy the vertical mouse movement delta
     */
    @Override
    public void handlePan(float dx, float dy) {
        camera.pan(dx, dy);
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
    }

    @Override
    public boolean isZoomFitting() {
        if (focusedDistance <= 0.0f) {
            return true;
        }
        return Math.abs(camera.getDistance() - focusedDistance) <= focusedDistance * 0.01f;
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
}
