package info.openrocket.core.simulation;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import info.openrocket.core.util.MathUtil;

/** The source of applied weather and whether the user subsequently changed it. */
public record WeatherSource(String provider, String endpoint, String kind, Instant validAt, Instant fetchedAt, Instant expiresAt,
		String timezone, double latitude, double longitude, double elevation, List<String> groups,
		boolean changedByUser) {
	public WeatherSource {
		Objects.requireNonNull(provider);
		Objects.requireNonNull(endpoint);
		Objects.requireNonNull(kind);
		Objects.requireNonNull(validAt);
		Objects.requireNonNull(fetchedAt);
		// Current conditions cover the 15-minute interval starting at their valid time.
		// Recompute this on load too, correcting older files with a delayed expiration.
		if (kind.equals("current")) expiresAt = validAt.plusSeconds(900);
		else if (expiresAt == null) expiresAt = validAt;
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
	}

	public WeatherSource withUserChanges() {
		return new WeatherSource(provider, endpoint, kind, validAt, fetchedAt, expiresAt, timezone,
				latitude, longitude, elevation, groups, true);
	}

	public boolean isExpired(Instant now) {
		return !now.isBefore(expiresAt);
	}

	public boolean isSiteMoved(SimulationOptions options) {
		return !MathUtil.equals(latitude, options.getLaunchLatitude())
				|| !MathUtil.equals(longitude, options.getLaunchLongitude());
	}

}
