package info.openrocket.core.communication;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import jakarta.json.Json;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenMeteoAPI.class);
    private static final ApplicationPreferences PREFERENCES = Application.getPreferences();
    private static final String OPEN_METEO_ELEVATION = "/OpenRocket/open-meteo/elevation";

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
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return a {@link CompletableFuture} that completes with the elevation value in meters,
     *         or {@code Double.NaN} if the request fails or no valid data is available
     */
    public static CompletableFuture<Double> getElevation(double latitude, double longitude) {
        var cached = getCachedElevation(latitude, longitude);

        if (!Double.isNaN(cached)) {
            return CompletableFuture.completedFuture(cached);
        }
        var url = String.format("https://api.open-meteo.com/v1/elevation?latitude=%f&longitude=%f", latitude, longitude);
        var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> response.replace("nan", "null"))
                .thenApply(response -> parseElevationJson(response, latitude, longitude))
                .exceptionally(e -> {
                    LOGGER.atError().setCause(e).log();
                    return Double.NaN;
                })
                .whenComplete((elevation, e) -> {
                    if (e != null) {
                        LOGGER.atError().setCause(e).log();
                    } else if (!Double.isNaN(elevation)) {
                        setCachedElevation(latitude, longitude, elevation);
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
        try (var reader = Json.createReader(new StringReader(Objects.requireNonNull(response)))) {
            var json = reader.readObject();

            if (json.containsKey("elevation")) {
                var array = json.getJsonArray("elevation");

                if (!array.isEmpty()) {
                    var value = array.getJsonNumber(0).doubleValue();
                    LOGGER.atInfo().log("Elevation ({}, {}) = {}", latitude, longitude, value);
                    return value;
                }
            }

            if (json.containsKey("error")) {
                LOGGER.atError().log("An error has occurred: {}", json.getString("reason", "unknown"));
            }
            return Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /**
     * Generates a unique cache key for the given geographic coordinates.
     * <p>
     * This key is used internally to identify entries corresponding to specific latitude and longitude pairs.
     * </p>
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return a unique string key representing the coordinate pair
     */
    private static String getCachedElevationKey(double latitude, double longitude) {
        return String.format("%f,%f", latitude, longitude);
    }

    /**
     * Retrieves a cached elevation value for the specified coordinates, if available.
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @return the cached elevation in meters above sea level, or {@code Double.NaN} if no cached value exists
     */
    private static double getCachedElevation(double latitude, double longitude) {
        return PREFERENCES.getNode(OPEN_METEO_ELEVATION).getDouble(getCachedElevationKey(latitude, longitude), Double.NaN);
    }

    /**
     * Caches the elevation value for the specified geographic coordinates.
     * <p>
     * This method stores the elevation in user preferences to reduce redundant
     * API requests to Open-Meteo for the same latitude/longitude pair.
     * </p>
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     * @param elevation the elevation value in meters above sea level to cache
     */
    private static void setCachedElevation(double latitude, double longitude, double elevation) {
        if (Double.isNaN(getCachedElevation(latitude, longitude))) {
            PREFERENCES.getNode(OPEN_METEO_ELEVATION).putDouble(getCachedElevationKey(latitude, longitude), elevation);
        }
    }
}
