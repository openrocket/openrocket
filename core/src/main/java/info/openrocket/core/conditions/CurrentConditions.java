package info.openrocket.core.conditions;

import java.time.Instant;
import java.util.List;

/**
 * Weather-model conditions for a launch site at a specific time.
 */
public record CurrentConditions(
		double latitude,
		double longitude,
		double elevation,
		Instant validAt,
		double temperature,
		double pressure,
		double relativeHumidity,
		double windGust,
		List<WindLayer> windLayers) {

	public CurrentConditions {
		windLayers = List.copyOf(windLayers);
		if (windLayers.isEmpty()) {
			throw new IllegalArgumentException("At least one wind layer is required");
		}
	}

	/**
	 * One wind sample at an altitude above mean sea level.
	 */
	public record WindLayer(double altitude, double speed, double direction, double standardDeviation) {
	}
}
