package info.openrocket.core.simulation.extension.example;

import java.util.List;

import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;

/**
 * Deprecated simulation extension kept for backwards compatibility.
 * <p>
 * The damping moment coefficient (Cdm) is now computed and stored by default by the core simulation.
 */
@Deprecated
public class DampingMoment extends AbstractSimulationExtension {

	@Override
	public List<FlightDataType> getFlightDataTypes() {
		// Report the type so older documents that enable this extension still “see” the same output.
		return List.of(FlightDataType.TYPE_DAMPING_MOMENT_COEFF);
	}

	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		// No-op: Cdm is now computed as a built-in flight data output.
	}

	@Override
	public String getName() {
		return "Damping moment coefficient (Cdm) (built-in)";
	}

	@Override
	public String getDescription() {
		return "Deprecated simulation extension: Cdm is now computed as a built-in flight data type.";
	}
}
