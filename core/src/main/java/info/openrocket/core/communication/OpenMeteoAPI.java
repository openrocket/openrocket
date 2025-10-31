package info.openrocket.core.communication;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for interacting with the Open-Meteo APIs.
 */
public final class OpenMeteoAPI {
    private static final Logger log = LoggerFactory.getLogger(OpenMeteoAPI.class);
    private static final ApplicationPreferences prefs = Application.getPreferences();
    private static final String OPEN_METEO_ELEVATION = "/OpenRocket/open-meteo/elevation";
    
    // API configuration constants
    private static final String ELEVATION_API_BASE_URL = "https://api.open-meteo.com/v1/elevation";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    
    // Coordinate validation constants
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    
    // Cache precision: round to 4 decimal places (~11 meters precision)
    private static final int CACHE_PRECISION_DECIMALS = 4;
    
    // Reusable HTTP client instance
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    private OpenMeteoAPI() {}

    /**
     * Retrieves the elevation (in meters) for the specified latitude and longitude.
     * <p>
     * This method first checks a local cache for a previously retrieved value.
     * If no cached result exists, it asynchronously queries the Open-Meteo Elevation API.
     * Results are cached for future requests. If the API request fails or returns
     * invalid data, the returned {@code CompletableFuture} completes with {@code Double.NaN}.
     * </p>
     * <p>
     * Usage example:
     * <pre>{@code
     * CompletableFuture<Double> elevationFuture = OpenMeteoAPI.getElevation(51.5074, -0.1278);
     * elevationFuture.thenAccept(elevation -> {
     *     if (!Double.isNaN(elevation)) {
     *         System.out.println("Elevation: " + elevation + " meters");
     *     }
     * });
     * }</pre>
     * </p>
     * @param latitude  the latitude in decimal degrees (must be between -90 and 90)
     * @param longitude the longitude in decimal degrees (must be between -180 and 180)
     * @return a {@link CompletableFuture} that completes with the elevation value in meters,
     *         or {@code Double.NaN} if the request fails or no valid data is available
     */
    public static CompletableFuture<Double> getElevation(double latitude, double longitude) {
        // Validate input coordinates
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            log.atWarn().log("Invalid latitude: {}. Must be between {} and {}",
                    latitude, MIN_LATITUDE, MAX_LATITUDE);
            return CompletableFuture.completedFuture(Double.NaN);
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            log.atWarn().log("Invalid longitude: {}. Must be between {} and {}",
                    longitude, MIN_LONGITUDE, MAX_LONGITUDE);
            return CompletableFuture.completedFuture(Double.NaN);
        }
        
        // Round coordinates for cache lookup to improve cache hit rate
        double roundedLat = roundToPrecision(latitude, CACHE_PRECISION_DECIMALS);
        double roundedLon = roundToPrecision(longitude, CACHE_PRECISION_DECIMALS);
        
        double cached = getCachedElevation(roundedLat, roundedLon);

        if (!Double.isNaN(cached)) {
            log.atDebug().log("Using cached elevation for ({}, {}): {} m",
                    roundedLat, roundedLon, cached);
            return CompletableFuture.completedFuture(cached);
        }
        
        // Use original (unrounded) coordinates for API request for maximum accuracy
        String url = String.format("%s?latitude=%.6f&longitude=%.6f", 
                ELEVATION_API_BASE_URL, latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        
        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> parseElevationJson(response, latitude, longitude))
                .whenComplete((elevation, throwable) -> {
                    if (throwable != null) {
                        log.atError()
                                .setCause(throwable)
                                .addKeyValue("latitude", latitude)
                                .addKeyValue("longitude", longitude)
                                .log("Failed to retrieve elevation from Open-Meteo API");
                    } else if (!Double.isNaN(elevation)) {
                        // Cache using rounded coordinates for better cache efficiency
                        setCachedElevation(roundedLat, roundedLon, elevation);
                    }
                });
    }

    /**
     * Parses an Open-Meteo elevation API JSON response and extracts the elevation value.
     * <p>
     * This method reads the JSON response, handles missing or malformed data,
     * and logs errors if applicable. If the response does not contain
     * a valid elevation array or an API error is reported, {@code Double.NaN} is returned.
     * </p>
     *
     * @param response  the raw JSON response body from the Open-Meteo API
     * @param latitude  the latitude used in the request, for logging and context
     * @param longitude the longitude used in the request, for logging and context
     * @return the parsed elevation value in meters, or {@code Double.NaN} if unavailable
     */
    private static double parseElevationJson(String response, double latitude, double longitude) {
        Objects.requireNonNull(response, "Response cannot be null");
        
        try (JsonReader reader = Json.createReader(new StringReader(response))) {
            JsonObject json = reader.readObject();

            if (json.containsKey("elevation")) {
                JsonArray array = json.getJsonArray("elevation");

                if (!array.isEmpty()) {
                    double value = array.getJsonNumber(0).doubleValue();
                    // Check for NaN or Infinity values
                    if (Double.isFinite(value)) {
                        log.atInfo().log("Elevation ({}, {}) = {} m", latitude, longitude, value);
                        return value;
                    } else {
                        log.atWarn().log("Received non-finite elevation value: {} for coordinates ({}, {})",
                                value, latitude, longitude);
                        return Double.NaN;
                    }
                } else {
                    log.atWarn().log("Elevation array is empty for coordinates ({}, {})",
                            latitude, longitude);
                }
            }

            if (json.containsKey("error")) {
                String reason = json.getString("reason", "unknown");
                log.atError().log("Open-Meteo API error for coordinates ({}, {}): {}",
                        latitude, longitude, reason);
            } else {
                log.atWarn().log("No elevation field found in API response for coordinates ({}, {})",
                        latitude, longitude);
            }
            return Double.NaN;
        } catch (jakarta.json.JsonException e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("latitude", latitude)
                    .addKeyValue("longitude", longitude)
                    .log("Failed to parse JSON response from Open-Meteo API");
            return Double.NaN;
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("latitude", latitude)
                    .addKeyValue("longitude", longitude)
                    .log("Unexpected error while parsing elevation response");
            return Double.NaN;
        }
    }

    /**
     * Rounds a double value to the specified number of decimal places.
     *
     * @param value    the value to round
     * @param decimals the number of decimal places
     * @return the rounded value
     */
    private static double roundToPrecision(double value, int decimals) {
        double multiplier = Math.pow(10.0, decimals);
        return Math.round(value * multiplier) / multiplier;
    }

    /**
     * Generates a unique cache key for the given geographic coordinates.
     * <p>
     * This key is used internally to identify entries corresponding to specific latitude and longitude pairs.
     * Coordinates are rounded to improve cache hit rate for nearby locations.
     * </p>
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return a unique string key representing the coordinate pair
     */
    private static String getCachedElevationKey(double latitude, double longitude) {
        // Use consistent formatting to ensure cache key stability
        return String.format("%.4f,%.4f", latitude, longitude);
    }

    /**
     * Retrieves a cached elevation value for the specified coordinates, if available.
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the cached elevation in meters above sea level, or {@code Double.NaN} if no cached value exists
     */
    private static double getCachedElevation(double latitude, double longitude) {
        return prefs.getNode(OPEN_METEO_ELEVATION).getDouble(getCachedElevationKey(latitude, longitude), Double.NaN);
    }

    /**
     * Caches the elevation value for the specified geographic coordinates.
     * <p>
     * This method stores the elevation in user preferences to reduce redundant
     * API requests to Open-Meteo for the same latitude/longitude pair.
     * Only caches if the value is finite (not NaN or Infinity) and not already cached.
     * </p>
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param elevation the elevation value in meters above sea level to cache
     */
    private static void setCachedElevation(double latitude, double longitude, double elevation) {
        // Only cache finite values
        if (!Double.isFinite(elevation)) {
            log.atDebug().log("Skipping cache for non-finite elevation: {} at ({}, {})",
                    elevation, latitude, longitude);
            return;
        }
        
        // Only cache if not already present
        if (Double.isNaN(getCachedElevation(latitude, longitude))) {
            prefs.getNode(OPEN_METEO_ELEVATION)
                    .putDouble(getCachedElevationKey(latitude, longitude), elevation);
            log.atDebug().log("Cached elevation {} m for coordinates ({}, {})",
                    elevation, latitude, longitude);
        }
    }
}
