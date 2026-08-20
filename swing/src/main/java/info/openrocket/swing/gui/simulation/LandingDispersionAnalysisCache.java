package info.openrocket.swing.gui.simulation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.UncertaintySpec;
import info.openrocket.core.util.Config;
import info.openrocket.core.util.ModID;

/**
 * Process-local cache for the most recent completed landing-dispersion analysis of
 * each simulation. Simulation keys are weak and compared by identity, so cached
 * results neither merge distinct simulations nor retain closed documents.
 */
final class LandingDispersionAnalysisCache {
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private LandingDispersionAnalysisCache() {
	}

	/** Return a result only while every simulation input still matches. */
	static synchronized MonteCarloResult get(Simulation simulation) {
		Objects.requireNonNull(simulation, "simulation");
		Iterator<Entry> iterator = ENTRIES.iterator();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			Simulation cachedSimulation = entry.simulation.get();
			if (cachedSimulation == null) {
				iterator.remove();
				continue;
			}
			if (cachedSimulation != simulation) {
				continue;
			}
			if (!entry.inputState.matches(simulation)) {
				iterator.remove();
				return null;
			}
			return entry.result;
		}
		return null;
	}

	/** Return a valid result only when the visible analysis definition also matches. */
	static synchronized MonteCarloResult get(Simulation simulation, MonteCarloSettings settings) {
		MonteCarloResult result = get(simulation);
		return result != null && settingsMatch(result.getSettings(), settings) ? result : null;
	}

	/** Replace the cached result for this exact simulation. */
	static synchronized void put(Simulation simulation, MonteCarloResult result) {
		Objects.requireNonNull(simulation, "simulation");
		Objects.requireNonNull(result, "result");
		Iterator<Entry> iterator = ENTRIES.iterator();
		while (iterator.hasNext()) {
			Simulation cachedSimulation = iterator.next().simulation.get();
			if (cachedSimulation == null || cachedSimulation == simulation) {
				iterator.remove();
			}
		}
		ENTRIES.add(new Entry(new WeakReference<>(simulation),
				SimulationInputState.capture(simulation), result));
	}

	/**
	 * Worker count affects execution only, not the requested ensemble definition, so it
	 * deliberately does not invalidate a reusable result.
	 */
	static boolean settingsMatch(MonteCarloSettings first, MonteCarloSettings second) {
		if (first == null || second == null
				|| first.getRunCount() != second.getRunCount()
				|| first.getSeed() != second.getSeed()) {
			return false;
		}
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			UncertaintySpec firstSpec = first.getUncertainty(parameter);
			UncertaintySpec secondSpec = second.getUncertainty(parameter);
			if (firstSpec.distribution() != secondSpec.distribution()
					|| !sameSpread(firstSpec.spread(), secondSpec.spread())) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameSpread(double first, double second) {
		if (Double.compare(first, second) == 0) {
			return true;
		}
		double tolerance = 4 * Math.max(Math.ulp(first), Math.ulp(second));
		return Math.abs(first - second) <= tolerance;
	}

	/** Visible for deterministic package tests; production entries expire weakly. */
	static synchronized void clear() {
		ENTRIES.clear();
	}

	private record Entry(WeakReference<Simulation> simulation,
			SimulationInputState inputState, MonteCarloResult result) {
	}

	private static final class SimulationInputState {
		private final ModID rocketModId;
		private final FlightConfigurationId configurationId;
		private final ModID configurationModId;
		private final OptionsState options;
		private final List<ExtensionState> extensions;

		private SimulationInputState(Simulation simulation) {
			this.rocketModId = simulation.getRocket().getModID();
			this.configurationId = simulation.getFlightConfigurationId();
			this.configurationModId = simulation.getActiveConfiguration().getModID();
			this.options = new OptionsState(simulation.getOptions());
			this.extensions = simulation.getSimulationExtensions().stream()
					.map(ExtensionState::new)
					.toList();
		}

		private static SimulationInputState capture(Simulation simulation) {
			return new SimulationInputState(simulation);
		}

		private boolean matches(Simulation simulation) {
			return rocketModId == simulation.getRocket().getModID()
					&& Objects.equals(configurationId, simulation.getFlightConfigurationId())
					&& configurationModId == simulation.getActiveConfiguration().getModID()
					&& options.matches(simulation.getOptions())
					&& extensionsMatch(simulation.getSimulationExtensions());
		}

		private boolean extensionsMatch(List<SimulationExtension> current) {
			if (extensions.size() != current.size()) {
				return false;
			}
			for (int index = 0; index < extensions.size(); index++) {
				if (!extensions.get(index).matches(current.get(index))) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * SimulationOptions.equals omits several trajectory inputs, so retain those
	 * explicitly alongside its deep clone.
	 */
	private static final class OptionsState {
		private final SimulationOptions options;

		private OptionsState(SimulationOptions options) {
			this.options = options.clone();
		}

		private boolean matches(SimulationOptions current) {
			return options.equals(current)
					&& options.getLaunchIntoWind() == current.getLaunchIntoWind()
					&& options.getGeodeticComputation() == current.getGeodeticComputation()
					&& options.isISAAtmosphere() == current.isISAAtmosphere()
					&& Objects.equals(options.getDragLookupCsvPath(), current.getDragLookupCsvPath())
					&& options.getDragLookupTable() == current.getDragLookupTable()
					&& Objects.equals(options.getDragLookupCsvRows(), current.getDragLookupCsvRows())
					&& Objects.equals(options.getStabilityLookupCsvPath(), current.getStabilityLookupCsvPath())
					&& options.getStabilityLookupTable() == current.getStabilityLookupTable()
					&& Objects.equals(options.getStabilityLookupCsvRows(), current.getStabilityLookupCsvRows());
		}
	}

	private static final class ExtensionState {
		private final String id;
		private final Config config;

		private ExtensionState(SimulationExtension extension) {
			this.id = extension.getId();
			this.config = extension.getConfig();
		}

		private boolean matches(SimulationExtension extension) {
			return Objects.equals(id, extension.getId()) && configMatches(config, extension.getConfig());
		}

		private static boolean configMatches(Config first, Config second) {
			if (!first.keySet().equals(second.keySet())) {
				return false;
			}
			for (String key : first.keySet()) {
				if (!Objects.equals(first.get(key, null), second.get(key, null))) {
					return false;
				}
			}
			return true;
		}
	}
}
