package info.openrocket.swing.gui.figure3d.scene.core;

import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Core camera component for 3D scene visualization in the OpenRocket environment.
 * This class manages camera position, orientation, projection matrices, and user interaction
 * behaviors such as orbiting, panning, and zooming. It provides both perspective and
 * orthographic projection modes to support different visualization needs.
 * 
 * <p>The camera operates around a center of interest point, allowing intuitive 3D navigation
 * for examining rocket components and 3D models. It integrates with the OpenRocket coordinate
 * system and provides appropriate framing and positioning for rocket visualization.</p>
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Orbit controls around a center point</li>
 *   <li>Perspective and orthographic projection modes</li>
 *   <li>Automatic bounds fitting for optimal object framing</li>
 *   <li>Configurable zoom limits and interaction constraints</li>
 *   <li>Predefined view positions (front, side, top, etc.)</li>
 * </ul>
 * 
 * <p>This camera is designed to be controlled externally through camera controllers
 * and can have its projection parameters updated dynamically based on viewport changes.</p>
 */
public class Camera {

	private static final float FIT_MIN_ZOOM_FACTOR = 0.05f;
	private static final float FIT_MAX_ZOOM_FACTOR = 2.0f;

	private final Vector3f position = new Vector3f();
	private boolean fixedCenterOfInterest;
	private final Vector3f centerOfInterest = new Vector3f(0.0f, 0.0f, 0.0f);
	// Shifts the whole camera rig (eye + lookAt) without moving the orbit pivot.
	// Horizontal pan goes to centerOfInterest.x; vertical pan goes here.
	private final Vector3f viewOffset = new Vector3f();
	private final Vector3f scratchLookAt = new Vector3f();
	private float distance;
	private float angleX; // Yaw
	private float angleY; // Pitch
	private boolean pitchClampingEnabled = true;
	// When true, uses a fixed world-up vector (0,1,0) instead of the continuous orbitUp,
	// matching the legacy JOGL photo-studio camera behavior.
	private boolean forceFixedUp = false;

	private float minZoom; // Minimum zoom distance
	private float maxZoom; // Maximum zoom distance

	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix = new Matrix4f();

	// Store original projection values
	private float fov;
	private float aspectRatio;
	private final float zNear;
	private final float zFar;
	private CameraConstants.ProjectionType projectionType;

	/**
	 * Creates a camera with a perspective projection.
	 * @param fov Field of view in radians.
	 * @param aspectRatio Aspect ratio of the window (width / height).
	 * @param zNear Near clipping plane distance.
	 * @param zFar Far clipping plane distance.
	 * @param fixedCenterOfInterest If true, the camera's center of interest cannot be changed by panning or dollying.
	 */
	private Camera(float fov, float aspectRatio, float zNear, float zFar, boolean fixedCenterOfInterest) {
		this.fov = fov;
		this.aspectRatio = aspectRatio;
		this.zNear = zNear;
		this.zFar = zFar;
		this.projectionMatrix = new Matrix4f();
		this.projectionType = CameraConstants.ProjectionType.PERSPECTIVE; // Default to perspective
		this.fixedCenterOfInterest = fixedCenterOfInterest;
		updateProjectionMatrix();
		updateViewMatrix();
	}

	/**
	 * Gets the current horizontal rotation angle (yaw) of the camera.
	 * 
	 * @return the yaw angle in radians
	 */
	public float getAngleX() {
		return angleX;
	}

	/**
	 * Sets the horizontal rotation angle (yaw) of the camera.
	 * 
	 * @param angleX the yaw angle in radians
	 */
	public void setAngleX(float angleX) {
		this.angleX = angleX;
	}

	/**
	 * Gets the current vertical rotation angle (pitch) of the camera.
	 * 
	 * @return the pitch angle in radians
	 */
	public float getAngleY() {
		return angleY;
	}

	/**
	 * Sets the vertical rotation angle (pitch) of the camera.
	 * 
	 * @param angleY the pitch angle in radians
	 */
	public void setAngleY(float angleY) {
		this.angleY = angleY;
	}

	/**
	 * Recalculates the projection matrix with a new aspect ratio.
	 * @param aspectRatio The new aspect ratio of the window (width / height).
	 */
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
		updateProjectionMatrix();
	}

	/**
	 * Gets the current field of view in radians.
	 *
	 * @return the field of view in radians
	 */
	public float getFieldOfView() {
		return fov;
	}

	/**
	 * Sets the field of view in radians.
	 *
	 * @param fov the field of view in radians
	 */
	public void setFieldOfView(double fov) {
		this.fov = (float) fov;
		updateProjectionMatrix();
	}
	
	/**
	 * Sets the projection type for the camera.
	 * @param projectionType The projection type (PERSPECTIVE or ORTHOGRAPHIC).
	 */
	public void setProjectionType(CameraConstants.ProjectionType projectionType) {
		this.projectionType = projectionType;
		updateProjectionMatrix();
	}
	
	/**
	 * Gets the current projection type.
	 * @return The current projection type.
	 */
	public CameraConstants.ProjectionType getProjectionType() {
		return projectionType;
	}
	
	/**
	 * Switches to perspective projection.
	 */
	public void setPerspectiveProjection() {
		setProjectionType(CameraConstants.ProjectionType.PERSPECTIVE);
	}
	
	/**
	 * Switches to orthographic projection.
	 */
	public void setOrthographicProjection() {
		setProjectionType(CameraConstants.ProjectionType.ORTHOGRAPHIC);
	}
	
	/**
	 * Toggles between perspective and orthographic projection.
	 */
	public void toggleProjection() {
		if (projectionType == CameraConstants.ProjectionType.PERSPECTIVE) {
			setProjectionType(CameraConstants.ProjectionType.ORTHOGRAPHIC);
		} else {
			setProjectionType(CameraConstants.ProjectionType.PERSPECTIVE);
		}
	}
	
	/**
	 * Updates the projection matrix based on the current projection type and parameters.
	 */
	private void updateProjectionMatrix() {
		projectionMatrix.identity();
		float nearPlane = getEffectiveNearPlane();
		float farPlane = getEffectiveFarPlane();
		
		switch (projectionType) {
			case PERSPECTIVE -> {
				projectionMatrix.perspective(fov, aspectRatio, nearPlane, farPlane);
			}
			case ORTHOGRAPHIC -> {
				// For orthographic projection, we need to calculate appropriate bounds
				// based on the current distance to maintain similar framing
				// Use a fallback distance if distance is 0 or not initialized
				float effectiveDistance = distance > 0 ? distance : CameraConstants.DEFAULT_DISTANCE;
				float halfHeight = effectiveDistance * (float) Math.tan(fov / 2.0f);
				float halfWidth = halfHeight * aspectRatio;
				projectionMatrix.ortho(-halfWidth, halfWidth, -halfHeight, halfHeight, nearPlane, farPlane);
			}
		}
	}

	// Scale clipping planes with the current orbit distance so close inspection does not clip the model.
	private float getEffectiveNearPlane() {
		float effectiveDistance = distance > 0 ? distance : CameraConstants.DEFAULT_DISTANCE;
		float dynamicNear = effectiveDistance * CameraConstants.DYNAMIC_Z_NEAR_DISTANCE_FACTOR;
		return Math.max(CameraConstants.MIN_DYNAMIC_Z_NEAR, Math.min(zNear, dynamicNear));
	}

	private float getEffectiveFarPlane() {
		float effectiveDistance = distance > 0 ? distance : CameraConstants.DEFAULT_DISTANCE;
		return Math.max(zFar, effectiveDistance * CameraConstants.DYNAMIC_Z_FAR_DISTANCE_FACTOR);
	}
	/**
	 * Sets the camera to a predefined view.
	 * This method calculates the required yaw (angleX) and pitch (angleY)
	 * to position the camera according to the selected view, while maintaining the current zoom distance.
	 *
	 * @param view The predefined view to snap to.
	 */
	public void setView(CameraConstants.View view) {
		Vector3f viewPos = view.getPosition();
		// We use atan2 to correctly calculate the angle in all quadrants.
		// Yaw (angleX) is the angle in the XZ plane.
		this.angleX = (float) Math.atan2(viewPos.x, viewPos.z);
		// Pitch (angleY) is the angle up from the XZ plane.
		this.angleY = (float) Math.asin(viewPos.y / viewPos.length());
	}

	/**
	 * Sets the center of interest for the camera.
	 * This is the point the camera looks at.
	 *
	 * @param centerOfInterest The new center of interest.
	 */
	public void setCenterOfInterest(Vector3f centerOfInterest) {
		if (fixedCenterOfInterest) {
			return; // Do not allow changes if fixed
		}
		this.centerOfInterest.set(centerOfInterest);
		updateViewMatrix();
	}

	/**
	 * Automatically positions and zooms the camera to fit the specified 3D bounds.
	 * Calculates the appropriate distance and framing to ensure the entire object
	 * is visible within the camera's viewport, accounting for the current view angle.
	 * 
	 * @param boxDimensions the dimensions of the bounding box to fit in the view
	 */
	public void fitBounds(Vector3f boxDimensions) {
		Vector3f right = new Vector3f();
		Vector3f up = new Vector3f();
		viewMatrix.positiveX(right);
		viewMatrix.positiveY(up);

		// Take absolute values of the camera's basis vectors
		right.absolute();
		up.absolute();

		// Project the full bounding box dimensions onto the camera's right and up vectors
		// to find the width and height of the projected 2D bounding box on the screen.
		float projWidth = boxDimensions.dot(right);
		float projHeight = boxDimensions.dot(up);

		// Distance required to fit the projected height in the vertical FOV
		float distanceForHeight = (float) ((projHeight / 2.0f) / Math.tan(fov / 2.0f));

		// Distance required to fit the projected width in the horizontal FOV
		float distanceForWidth = (float) ((projWidth / 2.0f) / (Math.tan(fov / 2.0f) * this.aspectRatio));

		// Use the greater of the two distances to ensure the object is fully visible.
		float newDistance = Math.max(distanceForHeight, distanceForWidth);
		newDistance *= CameraConstants.ZOOM_PADDING_FACTOR; // Add padding

		float fittedDistance = Math.max(CameraConstants.MIN_DISTANCE, newDistance);
		this.minZoom = Math.max(CameraConstants.MIN_DISTANCE, fittedDistance * FIT_MIN_ZOOM_FACTOR);
		this.maxZoom = Math.max(this.minZoom * FIT_MAX_ZOOM_FACTOR, fittedDistance * FIT_MAX_ZOOM_FACTOR);
		this.distance = Math.max(minZoom, Math.min(maxZoom, fittedDistance)); // Clamp zoom

		updateProjectionMatrix();
		updateViewMatrix();
	}

	/**
	 * Orbits the camera around the center of interest based on mouse movement.
	 * Updates both yaw and pitch angles while constraining pitch to prevent gimbal lock.
	 * 
	 * @param dx horizontal mouse movement delta for yaw rotation
	 * @param dy vertical mouse movement delta for pitch rotation
	 */
	public void orbit(float dx, float dy, float sensitivity) {
		// Invert orbit drag so the rocket follows the cursor direction.
		angleX -= dx * sensitivity;
		angleY += dy * sensitivity;
		if (pitchClampingEnabled) {
			angleY = Math.max(CameraConstants.MIN_PITCH_ANGLE, Math.min(CameraConstants.MAX_PITCH_ANGLE, angleY)); // Clamp pitch
		}
	}


	/**
	 * Moves the camera and its target point parallel to the view plane.
	 *
	 * @param dx The change in the horizontal screen direction in viewport pixels.
	 * @param dy The change in the vertical screen direction in viewport pixels.
	 * @param viewportWidth The viewport width in logical pixels.
	 * @param viewportHeight The viewport height in logical pixels.
	 */
	public void pan(float dx, float dy, int viewportWidth, int viewportHeight) {
		if (fixedCenterOfInterest) return;
		if (viewportWidth <= 0 || viewportHeight <= 0) return;
		Vector3f right = new Vector3f();
		viewMatrix.positiveX(right);
		Vector3f up = new Vector3f();
		viewMatrix.positiveY(up);

		float effectiveDistance = distance > 0 ? distance : CameraConstants.DEFAULT_DISTANCE;
		float halfHeight = effectiveDistance * (float) Math.tan(fov / 2.0f);
		float halfWidth = halfHeight * aspectRatio;
		float worldUnitsPerPixelX = (halfWidth * 2.0f) / viewportWidth;
		float worldUnitsPerPixelY = (halfHeight * 2.0f) / viewportHeight;

		// Compute full 3D world-space pan movement.
		float moveX = right.x * (-dx * worldUnitsPerPixelX) + up.x * (dy * worldUnitsPerPixelY);
		float moveY = right.y * (-dx * worldUnitsPerPixelX) + up.y * (dy * worldUnitsPerPixelY);
		float moveZ = right.z * (-dx * worldUnitsPerPixelX) + up.z * (dy * worldUnitsPerPixelY);

		// Horizontal component updates the orbit pivot (rocket lies along world X).
		centerOfInterest.x += moveX;
		// Vertical component shifts the view offset without touching the orbit pivot.
		viewOffset.y += moveY;
		viewOffset.z += moveZ;
	}

	/**
	 * Resets the view offset accumulated by vertical panning, re-centering the view
	 * on the orbit pivot without changing the camera angles or orbit distance.
	 */
	public void resetViewOffset() {
		viewOffset.zero();
	}

	/**
	 * Returns the effective world-space look-at target (orbit pivot + view offset).
	 *
	 * @return a new Vector3f with the effective look-at position
	 */
	public Vector3f getEffectiveLookAt() {
		return new Vector3f(centerOfInterest).add(viewOffset);
	}


	/**
	 * Moves the camera and its target point forward or backward.
	 * In perspective mode, this changes the distance from the center of interest.
	 * In orthographic mode, this scales the view frustum to simulate zoom.
	 *
	 * @param scrollAmount The distance to move. Positive is forward, negative is backward.
	 */
	public void dolly(float scrollAmount) {
		distance -= scrollAmount;
		distance = Math.max(minZoom, Math.min(maxZoom, distance)); // Clamp zoom

		updateProjectionMatrix();
	}

	/**
	 * Sets the camera distance from the center of interest.
	 *
	 * @param distance the new distance
	 */
	public void setDistance(float distance) {
		this.distance = Math.max(minZoom, Math.min(maxZoom, distance));
		updateProjectionMatrix();
		updateViewMatrix();
	}

	/**
	 * Gets the camera distance from the center of interest.
	 *
	 * @return the current distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Sets zoom distance constraints for dolly/setDistance operations.
	 *
	 * @param minZoom minimum allowed distance (must be >= 0)
	 * @param maxZoom maximum allowed distance (must be > minZoom)
	 */
	public void setZoomLimits(float minZoom, float maxZoom) {
		if (minZoom < 0.0f) {
			throw new IllegalArgumentException("minZoom must be >= 0");
		}
		if (maxZoom <= minZoom) {
			throw new IllegalArgumentException("maxZoom must be > minZoom");
		}
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.distance = Math.max(minZoom, Math.min(maxZoom, distance));
		updateProjectionMatrix();
		updateViewMatrix();
	}

	/**
	 * Enables or disables pitch clamping during orbit controls.
	 *
	 * @param enabled true to clamp pitch, false for unrestricted pitch
	 */
	public void setPitchClampingEnabled(boolean enabled) {
		this.pitchClampingEnabled = enabled;
	}

	/**
	 * When true the view matrix uses a fixed world-up vector (0,1,0) instead of the
	 * continuous orbit-up, reproducing the legacy JOGL gluLookAt behaviour used by
	 * the photo studio.
	 */
	public void setForceFixedUp(boolean forceFixedUp) {
		this.forceFixedUp = forceFixedUp;
	}

	/**
	 * Moves the camera and its target point left or right.
	 *
	 * @param amount The distance to move. Positive is right, negative is left.
	 */
	public void truck(float amount) {
		if (fixedCenterOfInterest) {
			return;
		}
		Vector3f right = new Vector3f();
		viewMatrix.positiveX(right);
		centerOfInterest.add(right.mul(amount));
	}

	/**
	 * Moves the camera and its target point straight up or down along the world's Y-axis.
	 * @param amount The distance to move. Positive is up, negative is down.
	 */
	public void pedestal(float amount) {
		if (fixedCenterOfInterest) {
			return;
		}
		// Directly modify the Y component of the center of interest.
		centerOfInterest.add(0, amount, 0);
	}

	/**
	 * Orbits the camera horizontally around the center of interest by a given angle.
	 * @param angle The angle to rotate in radians. Positive is right, negative is left.
	 */
	public void orbitYaw(float angle) {
		this.angleX += angle;
	}

	/**
	 * Updates the camera's internal state and view matrix.
	 * This method should be called every frame to ensure camera transformations
	 * are properly calculated and applied to the rendering pipeline.
	 */
	public void update() {
		updateViewMatrix();
	}

	private void updateViewMatrix() {
		float sinYaw = (float) Math.sin(angleX);
		float cosYaw = (float) Math.cos(angleX);
		float sinPitch = (float) Math.sin(angleY);
		float cosPitch = (float) Math.cos(angleY);

		float camX = distance * sinYaw * cosPitch;
		float camY = distance * sinPitch;
		float camZ = distance * cosYaw * cosPitch;
		// Eye position is shifted by viewOffset so vertical pan moves the whole rig.
		position.set(
			centerOfInterest.x + viewOffset.x + camX,
			centerOfInterest.y + viewOffset.y + camY,
			centerOfInterest.z + viewOffset.z + camZ
		);

		// Choose up vector: legacy photo-studio mode uses a fixed world-up (0,1,0) to match
		// the old JOGL gluLookAt behaviour exactly; normal interactive mode uses an orbit-up
		// computed from yaw/pitch so crossing ±90° pitch stays continuous without snapping.
		Vector3f up;
		if (forceFixedUp) {
			up = new Vector3f(0, 1, 0);
		} else {
			up = new Vector3f(-sinYaw * sinPitch, cosPitch, -cosYaw * sinPitch).normalize();
		}

		// LookAt target is the orbit pivot shifted by the same viewOffset.
		scratchLookAt.set(centerOfInterest).add(viewOffset);
		viewMatrix.identity().lookAt(position, scratchLookAt, up);
	}

	/**
	 * Gets the camera's view transformation matrix.
	 * 
	 * @return the 4x4 view matrix for transforming world coordinates to view space
	 */
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	/**
	 * Gets the current center-of-interest (look-at target) in world space.
	 *
	 * @return a copy of the center-of-interest vector
	 */
	public Vector3f getCenterOfInterest() {
		return new Vector3f(centerOfInterest);
	}

	/**
	 * Gets the camera's projection matrix.
	 * 
	 * @return the 4x4 projection matrix for transforming view coordinates to clip space
	 */
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	/**
	 * Gets the camera's current position in world space.
	 * 
	 * @return the camera's 3D position vector
	 */
	public Vector3f getPosition() {
		return new Vector3f(position);
	}
	
	/**
	 * Builder class for creating Camera instances with flexible configuration.
	 * 
	 * <p>This builder provides a fluent API for creating cameras with custom settings,
	 * making it easier to configure complex camera setups.</p>
	 * 
	 * <h3>Usage Example:</h3>
	 * <pre>
	 * Camera camera = Camera.builder()
	 *     .withFieldOfView(Math.toRadians(15))
	 *     .withAspectRatio(16.0f / 9.0f)
	 *     .withClippingPlanes(0.1f, 200.0f)
	 *     .withInitialDistance(10.0f)
	 *     .withZoomLimits(1.0f, 50.0f)
	 *     .withInitialAngles((float) Math.toRadians(45.0), (float) Math.toRadians(30.0))
	 *     .withCenterOfInterest(0.0f, 0.0f, 0.0f)
	 *     .withFixedCenterOfInterest(false)
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private float fov = CameraConstants.DEFAULT_FIELD_OF_VIEW; // Default FOV in radians
		private float aspectRatio = 1.0f;
		private float zNear = CameraConstants.DEFAULT_Z_NEAR;
		private float zFar = CameraConstants.DEFAULT_Z_FAR;
		private boolean fixedCenterOfInterest = true;
		private float initialDistance = CameraConstants.DEFAULT_DISTANCE;
		private float minZoom = CameraConstants.DEFAULT_MIN_ZOOM;
		private float maxZoom = CameraConstants.DEFAULT_MAX_ZOOM;
		private float initialAngleX = CameraConstants.DEFAULT_ANGLE_X;
		private float initialAngleY = CameraConstants.DEFAULT_ANGLE_Y;
		private Vector3f initialCenterOfInterest = new Vector3f(0.0f, 0.0f, 0.0f);
		private CameraConstants.ProjectionType projectionType = CameraConstants.ProjectionType.PERSPECTIVE; // Default to perspective projection
		
		/**
		 * Sets the camera's field of view.
		 * @param fov The field of view in radians (default: 45 degrees)
		 * @return This builder instance
		 */
		public Builder withFieldOfView(double fov) {
			this.fov = (float) fov;
			return this;
		}
		
		/**
		 * Sets the camera's aspect ratio.
		 * @param aspectRatio The aspect ratio (width / height, default: 1.0)
		 * @return This builder instance
		 */
		public Builder withAspectRatio(float aspectRatio) {
			this.aspectRatio = aspectRatio;
			return this;
		}
		
		/**
		 * Sets the camera's near and far clipping planes.
		 * @param zNear The near clipping plane distance (default: 0.1)
		 * @param zFar The far clipping plane distance (default: 100.0)
		 * @return This builder instance
		 */
		public Builder withClippingPlanes(float zNear, float zFar) {
			this.zNear = zNear;
			this.zFar = zFar;
			return this;
		}
		
		/**
		 * Sets whether the camera's center of interest is fixed.
		 * @param fixed If true, the center of interest cannot be changed by panning (default: true)
		 * @return This builder instance
		 */
		public Builder withFixedCenterOfInterest(boolean fixed) {
			this.fixedCenterOfInterest = fixed;
			return this;
		}
		
		/**
		 * Sets the initial distance from the center of interest.
		 * @param distance The initial distance (default: 8.0)
		 * @return This builder instance
		 */
		public Builder withInitialDistance(float distance) {
			this.initialDistance = distance;
			return this;
		}
		
		/**
		 * Sets the zoom limits for the camera.
		 * @param minZoom The minimum zoom distance (default: 0.5)
		 * @param maxZoom The maximum zoom distance (default: 30.0)
		 * @return This builder instance
		 */
		public Builder withZoomLimits(float minZoom, float maxZoom) {
			this.minZoom = minZoom;
			this.maxZoom = maxZoom;
			return this;
		}
		
		/**
		 * Sets the initial camera angles.
		 * @param angleX The initial yaw angle in radians (default: 25 degrees)
		 * @param angleY The initial pitch angle in radians (default: 30 degrees)
		 * @return This builder instance
		 */
		public Builder withInitialAngles(float angleX, float angleY) {
			this.initialAngleX = angleX;
			this.initialAngleY = angleY;
			return this;
		}
		
		/**
		 * Sets the initial center of interest.
		 * @param x The X coordinate of the center of interest
		 * @param y The Y coordinate of the center of interest
		 * @param z The Z coordinate of the center of interest
		 * @return This builder instance
		 */
		public Builder withCenterOfInterest(float x, float y, float z) {
			this.initialCenterOfInterest = new Vector3f(x, y, z);
			return this;
		}
		
		/**
		 * Sets the initial center of interest.
		 * @param center The center of interest as a Vector3f
		 * @return This builder instance
		 */
		public Builder withCenterOfInterest(Vector3f center) {
			this.initialCenterOfInterest = new Vector3f(center);
			return this;
		}
		
		/**
		 * Configures the camera for wide-angle viewing.
		 * @return This builder instance
		 */
		public Builder withWideAngle() {
			this.fov = (float) Math.toRadians(60);
			return this;
		}
		
		/**
		 * Configures the camera for telephoto viewing.
		 * @return This builder instance
		 */
		public Builder withTelephoto() {
			this.fov = (float) Math.toRadians(15);
			return this;
		}

		/**
		 * Sets the projection type for the camera.
		 * 
		 * @param projectionType the projection type (PERSPECTIVE or ORTHOGRAPHIC)
		 * @return this builder instance
		 */
		public Builder withProjectionType(CameraConstants.ProjectionType projectionType) {
			this.projectionType = projectionType;
			return this;
		}
		
		/**
		 * Configures the camera for architectural viewing (minimal perspective distortion).
		 * @return This builder instance
		 */
		public Builder withArchitecturalView() {
			this.fov = (float) Math.toRadians(30);
			this.initialAngleY = 0.0f; // Horizontal view
			return this;
		}
		
		/**
		 * Builds the Camera with the configured settings.
		 * @return A new Camera instance
		 */
		public Camera build() {
			Camera camera = new Camera(fov, aspectRatio, zNear, zFar, fixedCenterOfInterest);
			
			// Apply initial settings that aren't handled by the constructor
			camera.distance = initialDistance;
			camera.minZoom = minZoom;
			camera.maxZoom = maxZoom;
			camera.angleX = initialAngleX;
			camera.angleY = initialAngleY;
			camera.centerOfInterest.set(initialCenterOfInterest);
			camera.projectionType = projectionType;
			
			// Update matrices in correct order - projection needs distance first
			camera.updateProjectionMatrix();
			camera.updateViewMatrix();
			
			return camera;
		}
	}
	
	/**
	 * Creates a new camera builder.
	 * @return A new Camera.Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}
}
