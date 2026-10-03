package info.openrocket.swing.gui.figure3d.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * An animated object's pose over time in the engine's world coordinates. The pose places the
 * object's local origin: a local point {@code p} is at {@code getPosition(t) + getOrientation(t) * p}.
 * Every call returns a new vector or quaternion that the caller may modify.
 */
public interface PoseProvider {
	/** Position of the local origin in world units. */
	Vector3f getPosition(double t);

	/** Rotation from the object's local axes to world axes. */
	Quaternionf getOrientation(double t);

	/** Velocity in world units per second, or null when unknown. */
	default Vector3f getLinearVelocity(double t) {
		return null;
	}

	/** Angular velocity in radians per second about world axes, or null when unknown. */
	default Vector3f getAngularVelocity(double t) {
		return null;
	}

	/** First time with data; earlier times hold the first pose. */
	double getStartTime();

	/** Last time with data; later times hold the last pose. */
	double getEndTime();
}
