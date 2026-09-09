package info.openrocket.core.file.openrocket.importt;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.UncertaintySpec;

/** Loads the optional per-simulation landing-dispersion configuration. */
class LandingDispersionSettingsHandler extends AbstractElementHandler {
	private final Integer runCount;
	private final Integer seed;
	private final Map<MonteCarloParameter, UncertaintySpec> uncertainties =
			new EnumMap<>(MonteCarloParameter.class);
	private MonteCarloSettings settings;

	LandingDispersionSettingsHandler(HashMap<String, String> attributes, WarningSet warnings) {
		runCount = parseInteger(attributes.get("runs"), "run count", warnings);
		seed = parseInteger(attributes.get("seed"), "seed", warnings);
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		if (element.equals("uncertainty")) {
			return PlainTextHandler.INSTANCE;
		}
		warnings.add("Unknown landing-dispersion element '" + element + "', ignoring.");
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		if (!element.equals("uncertainty")) {
			return;
		}

		MonteCarloParameter parameter = (MonteCarloParameter) DocumentConfig.findEnum(
				attributes.get("parameter"), MonteCarloParameter.class);
		MonteCarloDistribution distribution = (MonteCarloDistribution) DocumentConfig.findEnum(
				attributes.get("distribution"), MonteCarloDistribution.class);
		if (parameter == null || distribution == null) {
			warnings.add("Invalid landing-dispersion uncertainty parameter or distribution, ignoring.");
			return;
		}

		double spread;
		try {
			spread = DocumentConfig.stringToDouble(attributes.get("spread"));
		} catch (NumberFormatException exception) {
			warnings.add("Invalid landing-dispersion uncertainty spread, ignoring.");
			return;
		}

		try {
			UncertaintySpec uncertainty = new UncertaintySpec(distribution, spread);
			if (distribution.requiresRelativeParameter() && !parameter.isRelative()) {
				throw new IllegalArgumentException("Distribution does not apply to parameter");
			}
			if (uncertainty.spread() == 0) {
				uncertainties.remove(parameter);
			} else {
				uncertainties.put(parameter, uncertainty);
			}
		} catch (IllegalArgumentException exception) {
			warnings.add("Invalid landing-dispersion uncertainty, ignoring.");
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		if (runCount == null || seed == null) {
			return;
		}

		try {
			MonteCarloSettings.Builder builder = MonteCarloSettings.builder()
					.runCount(runCount)
					.seed(seed);
			for (Map.Entry<MonteCarloParameter, UncertaintySpec> entry : uncertainties.entrySet()) {
				builder.uncertainty(entry.getKey(), entry.getValue());
			}
			settings = builder.build();
		} catch (IllegalArgumentException exception) {
			warnings.add("Invalid landing-dispersion settings, ignoring.");
		}
	}

	MonteCarloSettings getSettings() {
		return settings;
	}

	private static Integer parseInteger(String value, String description, WarningSet warnings) {
		if (value == null) {
			warnings.add("Missing landing-dispersion " + description + ", ignoring settings.");
			return null;
		}
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException exception) {
			warnings.add("Invalid landing-dispersion " + description + ", ignoring settings.");
			return null;
		}
	}
}
