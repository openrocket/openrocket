package info.openrocket.core.simulation.montecarlo;

/**
 * Thrown when the nominal trajectory reaches the ground without first deploying a
 * recovery device.
 */
public final class BallisticTrajectoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BallisticTrajectoryException(String message) {
		super(message);
	}
}
