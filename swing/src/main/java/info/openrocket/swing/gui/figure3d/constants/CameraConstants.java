package info.openrocket.swing.gui.figure3d.constants;

import org.joml.Vector3f;

/**
 * Contains camera-related constants for movement, limits, projection settings,
 * and predefined views.
 */
public abstract class CameraConstants {

	// --- Camera Movement Constants ---
	public static final float BASE_ROCKET_ROTATION_SENSITIVITY = 0.01f; // radians per pixel
	public static final float BASE_VIEW_ROTATION_SENSITIVITY = BASE_ROCKET_ROTATION_SENSITIVITY;
	public static final float DEFAULT_ROTATION_SENSITIVITY_FACTOR = 1.0f;
	public static final float ZOOM_PADDING_FACTOR = 1.2f;

	// --- Camera Limits ---
	public static final float DEFAULT_MIN_ZOOM = 0.5f;
	public static final float DEFAULT_MAX_ZOOM = 30.0f;
	public static final float DEFAULT_DISTANCE = 8.0f;
	public static final float MIN_DISTANCE = 0.1f;
	public static final float MAX_DISTANCE = 1000.0f;

	// --- Default Camera Angles ---
	public static final float DEFAULT_ANGLE_X = (float) Math.toRadians(25.0); // Yaw
	public static final float DEFAULT_ANGLE_Y = (float) Math.toRadians(30.0); // Pitch
	public static final float MIN_PITCH_ANGLE = (float) Math.toRadians(-89.0);
	public static final float MAX_PITCH_ANGLE = (float) Math.toRadians(89.0);

	// --- Projection Settings ---
	public static final float DEFAULT_FIELD_OF_VIEW = (float) Math.toRadians(45); // 45 degrees
	public static final float DEFAULT_Z_NEAR = 0.1f;
	public static final float DEFAULT_Z_FAR = 100.0f;
	public static final float MIN_DYNAMIC_Z_NEAR = 0.002f;
	public static final float DYNAMIC_Z_NEAR_DISTANCE_FACTOR = 0.02f;
	public static final float DYNAMIC_Z_FAR_DISTANCE_FACTOR = 25.0f;

	// --- Focus and Framing ---
	public static final float MIN_FOCUS_DISTANCE = 0.01f;
	public static final float FOCUS_MARGIN = 0.1f; // Extra space around object when focusing
	public static final float AUTO_FOCUS_PADDING = 1.5f; // Multiplier for automatic focus distance

	/**
	 * Enum representing the standard camera views.
	 */
	public enum View {
		// --- Side View ---
		// The camera looks from the side (+Z axis), with the rocket's length (+X) to the right
		// and its radial "up" (+Y) pointing to the top of the screen.
		SIDE(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0)),

		// --- Top View ---
		// The camera looks from the top (+Y axis) down at the rocket. The rocket's length (+X)
		// points to the right, and the "up" vector is set to keep the side view's "into the screen"
		// direction pointing up, preventing a disorienting roll.
		TOP(new Vector3f(0, 1, 0), new Vector3f(0, 0, -1)),

		// --- Back View ---
		// The camera looks from the tail of the rocket (-X axis) towards the nose.
		// The rocket's radial "up" (+Y) points to the top of the screen.
		BACK(new Vector3f(-1, 0, 0), new Vector3f(0, 1, 0));

		private final Vector3f position;
		private final Vector3f up;

		View(Vector3f position, Vector3f up) {
			this.position = position;
			this.up = up;
		}

		public Vector3f getPosition() {
			return position;
		}

		public Vector3f getUp() {
			return up;
		}
	}

	// --- Common View Directions ---
	public static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);
	public static final Vector3f WORLD_RIGHT = new Vector3f(1, 0, 0);
	public static final Vector3f WORLD_FORWARD = new Vector3f(0, 0, -1);

	// --- Camera Projection Types ---
	public enum ProjectionType {
		PERSPECTIVE,
		ORTHOGRAPHIC
	}
}
