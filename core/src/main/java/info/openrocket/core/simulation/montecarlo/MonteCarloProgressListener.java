package info.openrocket.core.simulation.montecarlo;

/**
 * Receives coarse progress updates after complete trajectories. The total includes
 * the nominal baseline trajectory.
 */
@FunctionalInterface
public interface MonteCarloProgressListener {
	void onProgress(int completedTrajectories, int totalTrajectories);
}
