package info.openrocket.swing.gui.figureelements;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.util.CoordinateIF;

/**
 * Shared helpers for populating {@link RocketInfo} from the current document/configuration context.
 */
public final class RocketInfoContextHelper {

	private RocketInfoContextHelper() {
	}

	public static CoordinateIF calculateCp(
			FlightConfiguration configuration,
			FlightConditions conditions,
			WarningSet warnings,
			AerodynamicCalculator aerodynamicCalculator,
			boolean useWorstCp) {
		warnings.clear();
		if (useWorstCp) {
			return aerodynamicCalculator.getWorstCP(configuration, conditions, warnings);
		}
		return aerodynamicCalculator.getCP(configuration, conditions, warnings);
	}

	public static Simulation findCurrentConfigurationSimulation(
			OpenRocketDocument document,
			FlightConfiguration configuration) {
		for (Simulation simulation : document.getSimulations()) {
			if (simulation.getFlightConfigurationId().compareTo(configuration.getFlightConfigurationID()) == 0) {
				return simulation;
			}
		}
		return null;
	}

	public static boolean shouldShowCalculatingState(
			FlightConfiguration configuration,
			Simulation simulation) {
		if (!configuration.hasMotors()) {
			return false;
		}
		if (simulation == null) {
			return true;
		}
		return !Simulation.isStatusUpToDate(simulation.getStatus());
	}
}
