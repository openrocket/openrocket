package info.openrocket.swing.gui.figure3d.constants;

/** Camera movement, limits, and projection constants. */
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
}
