package info.openrocket.swing.gui.figure3d.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Time-based world pose provider for any animatable object.
 * Position/orientation are in the engine's world coordinate system.
 */
public interface PoseProvider {
	Vector3f getPosition(double t);     // world units
	Quaternionf getOrientation(double t);

	default Vector3f getLinearVelocity(double t) { return null; }
	default Vector3f getAngularVelocity(double t) { return null; }

	double getStartTime();
	double getEndTime();
}
