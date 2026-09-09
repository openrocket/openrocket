package info.openrocket.core.simulation.montecarlo;

import java.util.List;

/**
 * Thrown when an analysis would execute extensions that have not declared
 * themselves safe for repeated, concurrent Monte Carlo simulation.
 */
public class UnsafeSimulationExtensionException extends IllegalArgumentException {
	private final List<String> extensionNames;

	public UnsafeSimulationExtensionException(List<String> extensionNames) {
		super("Landing-dispersion analysis cannot run extensions with possible external side effects: "
				+ String.join(", ", extensionNames) + ". Disable them before starting the analysis.");
		this.extensionNames = List.copyOf(extensionNames);
	}

	public List<String> getExtensionNames() {
		return extensionNames;
	}
}
