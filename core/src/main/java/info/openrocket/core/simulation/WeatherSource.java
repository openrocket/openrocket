package info.openrocket.core.simulation;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.util.MathUtil;

/** The source of applied weather, with a baseline for detecting subsequent edits. */
public record WeatherSource(String provider, String endpoint, String kind, Instant validAt, Instant fetchedAt,
		String timezone, double latitude, double longitude, double elevation, List<String> groups,
		String baseline) {
	public WeatherSource {
		Objects.requireNonNull(provider);
		Objects.requireNonNull(endpoint);
		Objects.requireNonNull(kind);
		Objects.requireNonNull(validAt);
		Objects.requireNonNull(fetchedAt);
		ZoneId.of(timezone);
		if (!Double.isFinite(latitude) || Math.abs(latitude) > 90
				|| !Double.isFinite(longitude) || Math.abs(longitude) > 180 || !Double.isFinite(elevation)) {
			throw new IllegalArgumentException("Invalid weather site");
		}
		groups = List.copyOf(groups);
		if (!provider.equals("open-meteo") || !endpoint.equals("forecast")
				|| !List.of("current", "forecast", "historical").contains(kind) || groups.isEmpty()
				|| !List.of("latitude", "longitude", "elevation", "temperature", "pressure", "humidity", "wind", "turbulence")
						.containsAll(groups)) {
			throw new IllegalArgumentException("Unsupported weather source");
		}
		Objects.requireNonNull(baseline);
	}

	public boolean isEdited(SimulationOptions options) {
		return !baseline.equals(snapshot(options, groups));
	}

	public boolean isSiteMoved(SimulationOptions options) {
		return !MathUtil.equals(latitude, options.getLaunchLatitude())
				|| !MathUtil.equals(longitude, options.getLaunchLongitude());
	}

	/** Use the persisted units and values so a save/load does not itself count as an edit. */
	public static String snapshot(SimulationOptions options, List<String> groups) {
		StringBuilder result = new StringBuilder();
		for (String group : groups) {
			result.append(group).append(':');
			switch (group) {
				case "latitude" -> result.append(options.getLaunchLatitude());
				case "longitude" -> result.append(options.getLaunchLongitude());
				case "elevation" -> result.append(options.getLaunchAltitude());
				case "temperature" -> result.append(options.isISAAtmosphere()).append(',')
						.append(options.getLaunchTemperature());
				case "pressure" -> result.append(options.isISAAtmosphere()).append(',')
						.append(options.getLaunchPressure());
				case "humidity" -> result.append(options.isISAAtmosphere()).append(',')
						.append(options.getLaunchRelativeHumidity());
				case "wind", "turbulence" -> {
					result.append(options.getWindModelType()).append(',');
					if (options.getWindModelType() == WindModelType.AVERAGE) {
						var wind = options.getAverageWindModel();
						result.append(wind.getAverage()).append(',').append(wind.getDirection()).append(',')
								.append(wind.getStandardDeviation());
					} else {
						result.append(options.getMultiLevelWindModel().getAltitudeReference()).append(',');
						for (var level : options.getMultiLevelWindModel().getLevels()) {
							result.append(level.getAltitude()).append(',').append(level.getSpeed()).append(',')
									.append(level.getDirection()).append(',').append(level.getStandardDeviation()).append(';');
						}
					}
				}
				default -> throw new IllegalArgumentException("Unknown applied weather field: " + group);
			}
			result.append('|');
		}
		return result.toString();
	}
}
