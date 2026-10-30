package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Poses a flying body so it rotates about its own geometric center instead of the rocket's
 * nose, which is the local origin of every component. The simulation integrates one point per
 * branch; the body's current center follows that point. When a separation changes which stages
 * the body is attached to, the offset between the old and new centers is frozen in world space
 * at that moment, so the body stays continuous and a separated booster tumbles about itself
 * rather than swinging around a point far above it.
 */
final class BodyCenteredPoseProvider implements PoseProvider {
	private final PoseProvider delegate;
	private final double[] switchTimes;
	private final Vector3f[] centers;
	private final Vector3f[] frozenOffsets;

	/**
	 * @param switchTimes ascending times at which the attached group changes
	 * @param centers the group's rocket-local center before the first switch and after each one
	 *                (one more entry than {@code switchTimes})
	 */
	BodyCenteredPoseProvider(PoseProvider delegate, List<Double> switchTimes, List<Vector3f> centers) {
		if (centers.size() != switchTimes.size() + 1) {
			throw new IllegalArgumentException("Need one center per segment between switches");
		}
		this.delegate = delegate;
		this.switchTimes = switchTimes.stream().mapToDouble(Double::doubleValue).toArray();
		this.centers = centers.stream().map(Vector3f::new).toArray(Vector3f[]::new);
		this.frozenOffsets = new Vector3f[this.centers.length];
		frozenOffsets[0] = new Vector3f();
		for (int i = 0; i < this.switchTimes.length; i++) {
			Vector3f shift = new Vector3f(this.centers[i + 1]).sub(this.centers[i]);
			frozenOffsets[i + 1] = delegate.getOrientation(this.switchTimes[i]).transform(shift).add(frozenOffsets[i]);
		}
	}

	/** The body's own rocket-local center: its group after the last separation. */
	Vector3f getBodyCenter() {
		return new Vector3f(centers[centers.length - 1]);
	}

	@Override
	public Vector3f getPosition(double time) {
		int segment = segmentAt(time);
		Quaternionf orientation = delegate.getOrientation(time);
		return new Vector3f(delegate.getPosition(time))
				.add(frozenOffsets[segment])
				.sub(orientation.transform(new Vector3f(centers[segment])));
	}

	private int segmentAt(double time) {
		int segment = 0;
		while (segment < switchTimes.length && time >= switchTimes[segment]) {
			segment++;
		}
		return segment;
	}

	@Override
	public Quaternionf getOrientation(double time) {
		return delegate.getOrientation(time);
	}

	@Override
	public Vector3f getLinearVelocity(double time) {
		return delegate.getLinearVelocity(time);
	}

	@Override
	public Vector3f getAngularVelocity(double time) {
		return delegate.getAngularVelocity(time);
	}

	@Override
	public double getStartTime() {
		return delegate.getStartTime();
	}

	@Override
	public double getEndTime() {
		return delegate.getEndTime();
	}
}
