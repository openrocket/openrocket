package info.openrocket.swing.gui.simulation.currentconditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Retrieves current or forecast surface conditions and a vertical wind profile from Open-Meteo.
 */
public class OpenMeteoClient {
	public static final String ATTRIBUTION_NAME = "Open-Meteo";
	public static final String ATTRIBUTION_URL = "https://open-meteo.com/";
	public static final int MAX_FORECAST_DAYS = 16;

	public static final String ATTRIBUTION_LICENSE_URL = "https://creativecommons.org/licenses/by/4.0/";
	public static final String TERMS_URL = "https://open-meteo.com/en/terms";
	private static final String DEFAULT_API_ENDPOINT = "https://api.open-meteo.com/v1/forecast";
	private static final String DEFAULT_GEOCODING_ENDPOINT = "https://geocoding-api.open-meteo.com/v1/search";
	private static final DateTimeFormatter API_HOUR_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm");
	private static final int MAX_CACHE_ENTRIES = 128;
	// Open-Meteo's finest advertised weather grids are 1 to 2 km, so hundredth-degree cells avoid duplicate
	// requests for locations that resolve to the same practical model area.
	private static final double LOCATION_CACHE_PRECISION = 100.0;
	private static final long CURRENT_INTERVAL_SECONDS = 15 * 60;
	private static final long FORECAST_UPDATE_INTERVAL_SECONDS = 60 * 60;
	private static final long FORECAST_PROPAGATION_SECONDS = 10 * 60;
	private static final long FORCE_REFRESH_INTERVAL_SECONDS = 60;
	private static final double DEFAULT_UPPER_AIR_TURBULENCE_INTENSITY = 0.10;
	private static final Map<WeatherCacheKey, CachedConditions> WEATHER_CACHE = new LinkedHashMap<>(16, 0.75f, true);
	private static final Map<WeatherCacheKey, Instant> LAST_FORCE_REFRESH = new LinkedHashMap<>(16, 0.75f, true);
	private static final Map<LocationCell, ZoneId> TIMEZONE_CACHE = new LinkedHashMap<>(16, 0.75f, true);
	private static final Map<WeatherCacheKey, CompletableFuture<FetchResult>> IN_FLIGHT = new ConcurrentHashMap<>();
	private static final Object REQUEST_RATE_LOCK = new Object();
	private static final Object GEOCODING_RATE_LOCK = new Object();
	private static long nextRequestNanos;
	private static long nextGeocodingRequestNanos;
	private static final long WEATHER_REQUEST_INTERVAL_NANOS = 5_000_000_000L;
	private static final long GEOCODING_REQUEST_INTERVAL_NANOS = 110_000_000L;
	private static final int[] PRESSURE_LEVELS = {
			1000, 975, 950, 925, 900, 850, 800, 700, 600, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30
	};
	private static final int[] ABOVE_GROUND_LEVELS = { 80, 120, 180 };
	private final String apiEndpoint;
	private final String geocodingEndpoint;
	private final String apiKey;

	public OpenMeteoClient() {
		this(System.getProperty("openrocket.openmeteo.apiEndpoint", DEFAULT_API_ENDPOINT),
				System.getProperty("openrocket.openmeteo.geocodingEndpoint", DEFAULT_GEOCODING_ENDPOINT),
				System.getProperty("openrocket.openmeteo.apiKey", ""));
	}

	OpenMeteoClient(String apiEndpoint, String geocodingEndpoint, String apiKey) {
		this.apiEndpoint = apiEndpoint;
		this.geocodingEndpoint = geocodingEndpoint;
		this.apiKey = apiKey == null ? "" : apiKey.trim();
	}

	public boolean usesFreeNonCommercialEndpoint() {
		return apiKey.isEmpty() && apiEndpoint.equals(DEFAULT_API_ENDPOINT);
	}

	public CurrentConditions fetch(double latitude, double longitude) throws IOException {
		return fetchWithCacheInfo(latitude, longitude).conditions();
	}

	public FetchResult fetchWithCacheInfo(double latitude, double longitude) throws IOException {
		return fetchCurrent(latitude, longitude, false);
	}

	public FetchResult forceFetch(double latitude, double longitude) throws IOException {
		return fetchCurrent(latitude, longitude, true);
	}

	private FetchResult fetchCurrent(double latitude, double longitude, boolean forceRefresh) throws IOException {
		validateCoordinates(latitude, longitude);
		WeatherCacheKey cacheKey = WeatherCacheKey.current(latitude, longitude);
		CachedConditions cached = forceRefresh ? null : getCached(cacheKey);
		if (cached != null) {
			return new FetchResult(atRequestedLocation(cached.conditions(), latitude, longitude), true,
					cached.expiresAt(), forceRefreshAvailableAt(cacheKey));
		}
		if (forceRefresh) {
			checkForceRefresh(cacheKey);
		}
		return coalesced(cacheKey, forceRefresh, () -> {
			URI uri = weatherUri("latitude=" + format(latitude)
					+ "&longitude=" + format(longitude)
					+ "&current=" + currentVariables()
					+ "&wind_speed_unit=ms&timeformat=iso8601&timezone=GMT&forecast_days=1");
			CurrentConditions conditions = fetch(uri, latitude, longitude, false);
			Instant expiresAt = currentExpiration(Instant.now());
			cache(cacheKey, conditions, expiresAt);
			return new FetchResult(conditions, false, expiresAt, forceRefreshAvailableAt(cacheKey));
		});
	}

	public CurrentConditions fetchForecast(double latitude, double longitude, Instant requestedAt) throws IOException {
		return fetchForecastWithCacheInfo(latitude, longitude, requestedAt).conditions();
	}

	public FetchResult fetchForecastWithCacheInfo(double latitude, double longitude, Instant requestedAt)
			throws IOException {
		return fetchForecast(latitude, longitude, requestedAt, false);
	}

	public FetchResult forceFetchForecast(double latitude, double longitude, Instant requestedAt) throws IOException {
		return fetchForecast(latitude, longitude, requestedAt, true);
	}

	private FetchResult fetchForecast(double latitude, double longitude, Instant requestedAt, boolean forceRefresh)
			throws IOException {
		validateCoordinates(latitude, longitude);
		if (requestedAt == null) {
			throw new IllegalArgumentException("Forecast time is required");
		}
		Instant forecastHour = requestedAt.truncatedTo(ChronoUnit.HOURS);
		WeatherCacheKey cacheKey = WeatherCacheKey.forecast(latitude, longitude, forecastHour);
		CachedConditions cached = forceRefresh ? null : getCached(cacheKey);
		if (cached != null) {
			return new FetchResult(atRequestedLocation(cached.conditions(), latitude, longitude), true,
					cached.expiresAt(), forceRefreshAvailableAt(cacheKey));
		}
		if (forceRefresh) {
			checkForceRefresh(cacheKey);
		}
		String hour = API_HOUR_FORMAT.format(
				LocalDateTime.ofInstant(forecastHour, ZoneOffset.UTC));
		return coalesced(cacheKey, forceRefresh, () -> {
			URI uri = weatherUri("latitude=" + format(latitude)
					+ "&longitude=" + format(longitude)
					+ "&hourly=" + currentVariables()
					+ "&start_hour=" + hour + "&end_hour=" + hour
					+ "&wind_speed_unit=ms&timeformat=iso8601&timezone=GMT");
			CurrentConditions conditions = fetch(uri, latitude, longitude, true);
			Instant expiresAt = forecastExpiration(Instant.now());
			cache(cacheKey, conditions, expiresAt);
			return new FetchResult(conditions, false, expiresAt, forceRefreshAvailableAt(cacheKey));
		});
	}

	private FetchResult coalesced(WeatherCacheKey key, boolean forceRefresh, WeatherFetch operation)
			throws IOException {
		CompletableFuture<FetchResult> mine = new CompletableFuture<>();
		CompletableFuture<FetchResult> existing = IN_FLIGHT.putIfAbsent(key, mine);
		if (existing != null) {
			return await(existing);
		}
		try {
			throttleWeatherRequests();
			FetchResult result = operation.fetch();
			if (forceRefresh) {
				markForceRefreshSucceeded(key);
			}
			mine.complete(result);
			return result;
		} catch (IOException | RuntimeException e) {
			mine.completeExceptionally(e);
			throw e;
		} finally {
			IN_FLIGHT.remove(key, mine);
		}
	}

	private static FetchResult await(CompletableFuture<FetchResult> future) throws IOException {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Weather request was interrupted", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException ioException) {
				throw ioException;
			}
			if (cause instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			throw new IOException("Weather request failed", cause);
		}
	}

	private static void throttleWeatherRequests() throws IOException {
		throttle(REQUEST_RATE_LOCK, true);
	}

	private static void throttleGeocodingRequests() throws IOException {
		throttle(GEOCODING_RATE_LOCK, false);
	}

	private static void throttle(Object lock, boolean weatherRequest) throws IOException {
		long delayNanos;
		synchronized (lock) {
			long now = System.nanoTime();
			long next = weatherRequest ? nextRequestNanos : nextGeocodingRequestNanos;
			long slot = Math.max(now, next);
			delayNanos = slot - now;
			// A weather request contains 69 variables and therefore counts as multiple API calls under Open-Meteo's
			// published accounting rules. Five-second spacing keeps sustained weighted usage below the hourly ceiling.
			if (weatherRequest) {
				nextRequestNanos = slot + WEATHER_REQUEST_INTERVAL_NANOS;
			} else {
				nextGeocodingRequestNanos = slot + GEOCODING_REQUEST_INTERVAL_NANOS;
			}
		}
		if (delayNanos <= 0) {
			return;
		}
		try {
			long millis = delayNanos / 1_000_000L;
			int nanos = (int) (delayNanos % 1_000_000L);
			Thread.sleep(millis, nanos);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Weather request was interrupted", e);
		}
	}

	private static synchronized CachedConditions getCached(WeatherCacheKey key) {
		CachedConditions cached = WEATHER_CACHE.get(key);
		if (cached == null) {
			return null;
		}
		if (!Instant.now().isBefore(cached.expiresAt())) {
			WEATHER_CACHE.remove(key);
			return null;
		}
		return cached;
	}

	private static synchronized void cache(WeatherCacheKey key, CurrentConditions conditions, Instant expiresAt) {
		WEATHER_CACHE.entrySet().removeIf(entry -> !Instant.now().isBefore(entry.getValue().expiresAt()));
		if (WEATHER_CACHE.size() >= MAX_CACHE_ENTRIES) {
			WEATHER_CACHE.remove(WEATHER_CACHE.keySet().iterator().next());
		}
		WEATHER_CACHE.put(key, new CachedConditions(conditions, expiresAt));
	}

	private static synchronized void checkForceRefresh(WeatherCacheKey key) throws RefreshRateLimitException {
		Instant now = Instant.now();
		Instant availableAt = forceRefreshAvailableAt(key);
		if (now.isBefore(availableAt)) {
			throw new RefreshRateLimitException(availableAt);
		}
	}

	private static synchronized void markForceRefreshSucceeded(WeatherCacheKey key) {
		Instant now = Instant.now();
		if (LAST_FORCE_REFRESH.size() >= MAX_CACHE_ENTRIES) {
			LAST_FORCE_REFRESH.remove(LAST_FORCE_REFRESH.keySet().iterator().next());
		}
		LAST_FORCE_REFRESH.put(key, now);
	}

	private static synchronized Instant forceRefreshAvailableAt(WeatherCacheKey key) {
		Instant lastRefresh = LAST_FORCE_REFRESH.get(key);
		return lastRefresh == null ? Instant.EPOCH : lastRefresh.plusSeconds(FORCE_REFRESH_INTERVAL_SECONDS);
	}

	static synchronized void clearCachesForTesting() {
		WEATHER_CACHE.clear();
		LAST_FORCE_REFRESH.clear();
		TIMEZONE_CACHE.clear();
		IN_FLIGHT.clear();
		nextRequestNanos = 0;
		nextGeocodingRequestNanos = 0;
	}

	private static CurrentConditions atRequestedLocation(CurrentConditions conditions, double latitude,
			double longitude) {
		return new CurrentConditions(latitude, longitude, conditions.modelLatitude(), conditions.modelLongitude(),
				conditions.elevation(), conditions.validAt(),
				conditions.temperature(), conditions.pressure(), conditions.relativeHumidity(), conditions.windGust(),
				conditions.windLayers());
	}

	private static Instant currentExpiration(Instant now) {
		long nextInterval = (Math.floorDiv(now.getEpochSecond(), CURRENT_INTERVAL_SECONDS) + 1)
				* CURRENT_INTERVAL_SECONDS;
		return Instant.ofEpochSecond(nextInterval + FORECAST_PROPAGATION_SECONDS);
	}

	private static Instant forecastExpiration(Instant now) {
		long currentHour = Math.floorDiv(now.getEpochSecond(), FORECAST_UPDATE_INTERVAL_SECONDS)
				* FORECAST_UPDATE_INTERVAL_SECONDS;
		long expiration = currentHour + FORECAST_PROPAGATION_SECONDS;
		if (expiration <= now.getEpochSecond()) {
			expiration += FORECAST_UPDATE_INTERVAL_SECONDS;
		}
		return Instant.ofEpochSecond(expiration);
	}

	public List<LocationSearchResult> searchLocations(String query) throws IOException {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		URI uri = URI.create(geocodingEndpoint + "?name="
				+ URLEncoder.encode(query.trim(), StandardCharsets.UTF_8)
				+ "&count=10&language=en&format=json");
		throttleGeocodingRequests();
		HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
		connection.setConnectTimeout(10_000);
		connection.setReadTimeout(20_000);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "OpenRocket weather location picker (https://openrocket.info/)");
		try {
			int status = connection.getResponseCode();
			InputStream stream = status >= 200 && status < 300
					? connection.getInputStream() : connection.getErrorStream();
			String response = readResponse(stream);
			if (status < 200 || status >= 300) {
				throw new IOException("Location search returned HTTP " + status + responseReason(response));
			}
			return parseLocations(response);
		} finally {
			connection.disconnect();
		}
	}

	public ZoneId resolveTimezone(double latitude, double longitude) throws IOException {
		validateCoordinates(latitude, longitude);
		LocationCell cell = LocationCell.of(latitude, longitude);
		synchronized (OpenMeteoClient.class) {
			ZoneId cached = TIMEZONE_CACHE.get(cell);
			if (cached != null) {
				return cached;
			}
		}
		URI uri = weatherUri("latitude=" + format(latitude) + "&longitude=" + format(longitude)
				+ "&timezone=auto&forecast_days=1");
		throttleWeatherRequests();
		HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
		connection.setConnectTimeout(10_000);
		connection.setReadTimeout(20_000);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "OpenRocket weather location picker (https://openrocket.info/)");
		try {
			int status = connection.getResponseCode();
			InputStream stream = status >= 200 && status < 300
					? connection.getInputStream() : connection.getErrorStream();
			String response = readResponse(stream);
			if (status < 200 || status >= 300) {
				throw new IOException("Timezone lookup returned HTTP " + status + responseReason(response));
			}
			ZoneId timezone = parseTimezone(response);
			synchronized (OpenMeteoClient.class) {
				if (TIMEZONE_CACHE.size() >= MAX_CACHE_ENTRIES) {
					TIMEZONE_CACHE.remove(TIMEZONE_CACHE.keySet().iterator().next());
				}
				TIMEZONE_CACHE.put(cell, timezone);
			}
			return timezone;
		} finally {
			connection.disconnect();
		}
	}

	static ZoneId parseTimezone(String json) throws IOException {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			return ZoneId.of(requiredString(root, "timezone"));
		} catch (IllegalStateException | JsonParseException | NullPointerException | java.time.DateTimeException e) {
			throw new IOException("Timezone lookup returned an invalid response", e);
		}
	}

	static List<LocationSearchResult> parseLocations(String json) throws IOException {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (!root.has("results") || root.get("results").isJsonNull()) {
				return List.of();
			}
			List<LocationSearchResult> results = new ArrayList<>();
			for (var element : root.getAsJsonArray("results")) {
				JsonObject result = element.getAsJsonObject();
				results.add(new LocationSearchResult(requiredString(result, "name"),
						optionalString(result, "admin1"), optionalString(result, "country"),
						requiredDouble(result, "latitude"), requiredDouble(result, "longitude"),
						optionalString(result, "timezone")));
			}
			return results;
		} catch (IllegalStateException | JsonParseException | NullPointerException e) {
			throw new IOException("Location search returned an invalid response", e);
		}
	}

	private CurrentConditions fetch(URI uri, double latitude, double longitude, boolean forecast) throws IOException {

		HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
		connection.setConnectTimeout(10_000);
		connection.setReadTimeout(20_000);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("User-Agent", "OpenRocket current-conditions feature (https://openrocket.info/)");

		try {
			int status = connection.getResponseCode();
			InputStream stream = status >= 200 && status < 300
					? connection.getInputStream() : connection.getErrorStream();
			String response = readResponse(stream);
			if (status < 200 || status >= 300) {
				throw new IOException("Weather service returned HTTP " + status + responseReason(response));
			}
			return forecast ? parseForecast(response, latitude, longitude) : parse(response, latitude, longitude);
		} finally {
			connection.disconnect();
		}
	}

	private URI weatherUri(String query) {
		String keyParameter = apiKey.isEmpty() ? "" : "&apikey="
				+ URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
		return URI.create(apiEndpoint + "?" + query + keyParameter);
	}

	static CurrentConditions parse(String json, double latitude, double longitude) throws IOException {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			return parse(root, requiredObject(root, "current"), latitude, longitude);
		} catch (IllegalStateException | JsonParseException | NullPointerException e) {
			throw new IOException("Weather service returned an invalid response", e);
		}
	}

	static CurrentConditions parseForecast(String json, double latitude, double longitude) throws IOException {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			JsonObject hourly = requiredObject(root, "hourly");
			JsonObject selectedHour = new JsonObject();
			for (Map.Entry<String, com.google.gson.JsonElement> entry : hourly.entrySet()) {
				if (!entry.getValue().isJsonArray() || entry.getValue().getAsJsonArray().isEmpty()) {
					throw new IOException("Weather service returned no forecast for the selected time");
				}
				selectedHour.add(entry.getKey(), entry.getValue().getAsJsonArray().get(0));
			}
			return parse(root, selectedHour, latitude, longitude);
		} catch (IllegalStateException | JsonParseException | NullPointerException e) {
			throw new IOException("Weather service returned an invalid response", e);
		}
	}

	private static CurrentConditions parse(JsonObject root, JsonObject values, double latitude, double longitude)
			throws IOException {
		try {
			double modelLatitude = requiredDouble(root, "latitude");
			double modelLongitude = requiredDouble(root, "longitude");
			double elevation = requiredDouble(root, "elevation");
			double temperature = requiredDouble(values, "temperature_2m") + 273.15;
			double pressure = requiredDouble(values, "surface_pressure") * 100.0;
			double relativeHumidity = requiredDouble(values, "relative_humidity_2m") / 100.0;
			double surfaceSpeed = requiredDouble(values, "wind_speed_10m");
			double surfaceDirection = radians(requiredDouble(values, "wind_direction_10m"));
			double gust = requiredDouble(values, "wind_gusts_10m");
			double turbulenceIntensity = estimateTurbulenceIntensity(surfaceSpeed, gust);

			Map<Double, CurrentConditions.WindLayer> layers = new TreeMap<>();
			// Open-Meteo's surface wind is sampled 10 metres above terrain, not at terrain elevation.
			addLayer(layers, elevation + 10, surfaceSpeed, surfaceDirection, turbulenceIntensity);
			for (int height : ABOVE_GROUND_LEVELS) {
				Double speed = optionalDouble(values, "wind_speed_" + height + "m");
				Double direction = optionalDouble(values, "wind_direction_" + height + "m");
				if (speed != null && direction != null) {
					addLayer(layers, elevation + height, speed, radians(direction),
							DEFAULT_UPPER_AIR_TURBULENCE_INTENSITY);
				}
			}

			for (int level : PRESSURE_LEVELS) {
				Double altitude = optionalDouble(values, "geopotential_height_" + level + "hPa");
				Double speed = optionalDouble(values, "wind_speed_" + level + "hPa");
				Double direction = optionalDouble(values, "wind_direction_" + level + "hPa");
				if (altitude == null || speed == null || direction == null) {
					continue;
				}
				if (altitude <= elevation) {
					continue;
				}
				addLayer(layers, altitude, speed, radians(direction), DEFAULT_UPPER_AIR_TURBULENCE_INTENSITY);
			}

			Instant validAt = LocalDateTime.parse(requiredString(values, "time")).toInstant(ZoneOffset.UTC);
			return new CurrentConditions(latitude, longitude, modelLatitude, modelLongitude, elevation, validAt,
					temperature, pressure,
					relativeHumidity, gust, new ArrayList<>(layers.values()));
		} catch (IllegalStateException | JsonParseException | NullPointerException e) {
			throw new IOException("Weather service returned an invalid response", e);
		}
	}

	private static void addLayer(Map<Double, CurrentConditions.WindLayer> layers, double altitude, double speed,
			double direction, double turbulenceIntensity) throws IOException {
		if (!Double.isFinite(altitude) || !Double.isFinite(speed) || !Double.isFinite(direction) || speed < 0) {
			throw new IOException("Weather service returned an invalid wind layer");
		}
		double standardDeviation = Math.min(speed * turbulenceIntensity, speed * 0.40);
		layers.put(altitude, new CurrentConditions.WindLayer(altitude, speed, direction, standardDeviation));
	}

	private static double estimateTurbulenceIntensity(double speed, double gust) {
		if (speed <= 0.1 || gust <= speed) {
			return 0.10;
		}
		return Math.max(0.05, Math.min(0.35, (gust - speed) / (3.0 * speed)));
	}

	static String currentVariables() {
		List<String> variables = new ArrayList<>(List.of(
				"temperature_2m", "relative_humidity_2m", "surface_pressure",
				"wind_speed_10m", "wind_direction_10m", "wind_gusts_10m"));
		for (int height : ABOVE_GROUND_LEVELS) {
			variables.add("wind_speed_" + height + "m");
			variables.add("wind_direction_" + height + "m");
		}
		for (int level : PRESSURE_LEVELS) {
			variables.add("wind_speed_" + level + "hPa");
			variables.add("wind_direction_" + level + "hPa");
			variables.add("geopotential_height_" + level + "hPa");
		}
		return String.join(",", variables);
	}

	private static void validateCoordinates(double latitude, double longitude) {
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
				|| !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException("Invalid latitude or longitude");
		}
	}

	private static JsonObject requiredObject(JsonObject object, String name) throws IOException {
		if (!object.has(name) || !object.get(name).isJsonObject()) {
			throw new IOException("Weather service response is missing " + name);
		}
		return object.getAsJsonObject(name);
	}

	private static double requiredDouble(JsonObject object, String name) throws IOException {
		if (!object.has(name) || object.get(name).isJsonNull()) {
			throw new IOException("Weather service response is missing " + name);
		}
		double value = object.get(name).getAsDouble();
		if (!Double.isFinite(value)) {
			throw new IOException("Weather service returned invalid " + name);
		}
		return value;
	}

	private static Double optionalDouble(JsonObject object, String name) {
		if (!object.has(name) || object.get(name).isJsonNull()) {
			return null;
		}
		try {
			double value = object.get(name).getAsDouble();
			return Double.isFinite(value) ? value : null;
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	private static String requiredString(JsonObject object, String name) throws IOException {
		if (!object.has(name) || object.get(name).isJsonNull()) {
			throw new IOException("Weather service response is missing " + name);
		}
		return object.get(name).getAsString();
	}

	private static String optionalString(JsonObject object, String name) {
		return object.has(name) && !object.get(name).isJsonNull() ? object.get(name).getAsString() : "";
	}

	private static String readResponse(InputStream stream) throws IOException {
		if (stream == null) {
			return "";
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			return response.toString();
		}
	}

	private static String responseReason(String response) {
		try {
			JsonObject object = JsonParser.parseString(response).getAsJsonObject();
			return object.has("reason") ? ": " + object.get("reason").getAsString() : "";
		} catch (RuntimeException ignored) {
			return "";
		}
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.6f", value);
	}

	private static double radians(double degrees) {
		return Math.toRadians((degrees % 360.0 + 360.0) % 360.0);
	}

	private record WeatherCacheKey(long latitude, long longitude, Instant forecastAt) {
		private static WeatherCacheKey current(double latitude, double longitude) {
			return new WeatherCacheKey(locationCell(latitude), locationCell(longitude), null);
		}

		private static WeatherCacheKey forecast(double latitude, double longitude, Instant forecastAt) {
			return new WeatherCacheKey(locationCell(latitude), locationCell(longitude), forecastAt);
		}

		private static long locationCell(double coordinate) {
			return Math.round(coordinate * LOCATION_CACHE_PRECISION);
		}
	}

	private record LocationCell(long latitude, long longitude) {
		private static LocationCell of(double latitude, double longitude) {
			return new LocationCell(WeatherCacheKey.locationCell(latitude), WeatherCacheKey.locationCell(longitude));
		}
	}

	private record CachedConditions(CurrentConditions conditions, Instant expiresAt) {
	}

	@FunctionalInterface
	private interface WeatherFetch {
		FetchResult fetch() throws IOException;
	}

	public record FetchResult(CurrentConditions conditions, boolean cached, Instant refreshAvailableAt,
			Instant forceRefreshAvailableAt) {
	}

	public static class RefreshRateLimitException extends IOException {
		private final Instant availableAt;

		private RefreshRateLimitException(Instant availableAt) {
			super("Force refresh is available after " + availableAt);
			this.availableAt = availableAt;
		}

		public Instant getAvailableAt() {
			return availableAt;
		}
	}

	public record LocationSearchResult(String name, String region, String country, double latitude,
			double longitude, String timezoneId) {
		@Override
		public String toString() {
			return String.join(", ", java.util.stream.Stream.of(name, region, country)
					.filter(value -> value != null && !value.isBlank()).toList());
		}
	}
}
