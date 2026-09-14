package info.openrocket.core.simulation.montecarlo;

/**
 * A simulation abort associated with one independently simulated landing body.
 *
 * @param bodyId stable source-component identity across trajectory simulations
 * @param branchIndex branch index in this trajectory's simulation data
 * @param branchName display name of the simulated body
 * @param message abort description
 */
public record LandingBodyFailure(String bodyId, int branchIndex, String branchName, String message) {
}
