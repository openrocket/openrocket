package info.openrocket.core.simulation.montecarlo;

/**
 * Thrown when the nominal trajectory has a ballistic ground hit and provides neither
 * a recovered landing body nor an independently landing separated body.
 */
public final class BallisticTrajectoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BallisticTrajectoryException(String message) {
		super(message);
	}
}
