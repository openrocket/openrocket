package info.openrocket.swing.gui.simulation.currentconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import info.openrocket.core.startup.Application;

/** Persists choices explicitly saved from the weather customization dialogs. */
final class WeatherCustomizationPreferences {
	private static final String NODE_NAME = "weatherCustomization";
	private static final String FIELDS_SAVED = "fieldsSaved";
	private static final String EXCLUDED_WIND_LEVELS = "excludedWindLevelIndices";
	private static final String TURBULENCE_INTENSITY = "turbulenceIntensity";

	private final Preferences preferences;

	WeatherCustomizationPreferences() {
		this(Application.getPreferences().getNode(NODE_NAME));
	}

	WeatherCustomizationPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	Optional<FieldSettings> loadFieldSettings() {
		if (!preferences.getBoolean(FIELDS_SAVED, false)) {
			return Optional.empty();
		}
		return Optional.of(new FieldSettings(
				preferences.getBoolean("latitude", true),
				preferences.getBoolean("longitude", true),
				preferences.getBoolean("elevation", true),
				preferences.getBoolean("temperature", true),
				preferences.getBoolean("pressure", true),
				preferences.getBoolean("humidity", true),
				preferences.getBoolean("wind", true),
				preferences.getBoolean("turbulence", true)));
	}

	void saveFieldSettings(FieldSettings settings) {
		preferences.putBoolean("latitude", settings.latitude());
		preferences.putBoolean("longitude", settings.longitude());
		preferences.putBoolean("elevation", settings.elevation());
		preferences.putBoolean("temperature", settings.temperature());
		preferences.putBoolean("pressure", settings.pressure());
		preferences.putBoolean("humidity", settings.humidity());
		preferences.putBoolean("wind", settings.wind());
		preferences.putBoolean("turbulence", settings.turbulence());
		preferences.putBoolean(FIELDS_SAVED, true);
	}

	OptionalDouble loadTurbulenceIntensity() {
		if (!preferences.getBoolean(FIELDS_SAVED, false)) {
			return OptionalDouble.empty();
		}
		double value = preferences.getDouble(TURBULENCE_INTENSITY, Double.NaN);
		return Double.isFinite(value) && value >= 0 && value <= 1
				? OptionalDouble.of(value) : OptionalDouble.empty();
	}

	void saveTurbulenceIntensity(double value) {
		if (!Double.isFinite(value) || value < 0 || value > 1) {
			throw new IllegalArgumentException("Turbulence intensity must be between zero and one");
		}
		preferences.putDouble(TURBULENCE_INTENSITY, value);
	}

	void clearTurbulenceIntensity() {
		preferences.remove(TURBULENCE_INTENSITY);
	}

	Set<Integer> loadExcludedWindLevelIndices() {
		String saved = preferences.get(EXCLUDED_WIND_LEVELS, "").trim();
		if (saved.isEmpty()) {
			return Collections.emptySet();
		}
		return Arrays.stream(saved.split(","))
				.map(String::trim)
				.map(WeatherCustomizationPreferences::parseNonNegativeInteger)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toUnmodifiableSet());
	}

	void saveExcludedWindLevelIndices(Set<Integer> excludedIndices) {
		String encoded = excludedIndices.stream()
				.filter(index -> index != null && index >= 0)
				.collect(Collectors.toCollection(TreeSet::new)).stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		preferences.put(EXCLUDED_WIND_LEVELS, encoded);
	}

	private static Optional<Integer> parseNonNegativeInteger(String value) {
		try {
			int parsed = Integer.parseInt(value);
			return parsed >= 0 ? Optional.of(parsed) : Optional.empty();
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
	}

	record FieldSettings(boolean latitude, boolean longitude, boolean elevation, boolean temperature,
			boolean pressure, boolean humidity, boolean wind, boolean turbulence) {
	}
}
