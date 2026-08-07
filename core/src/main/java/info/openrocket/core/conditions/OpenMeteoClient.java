package info.openrocket.core.conditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Retrieves current surface conditions and a vertical wind profile from Open-Meteo.
 */
public class OpenMeteoClient {
	public static final String ATTRIBUTION_NAME = "Open-Meteo";
	public static final String ATTRIBUTION_URL = "https://open-meteo.com/";

	private static final String API_ENDPOINT = "https://api.open-meteo.com/v1/forecast";
	private static final int[] PRESSURE_LEVELS = {
			1000, 975, 950, 925, 900, 850, 800, 700, 600, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30
	};
	private static final int[] ABOVE_GROUND_LEVELS = { 80, 120, 180 };

	public CurrentConditions fetch(double latitude, double longitude) throws IOException {
		validateCoordinates(latitude, longitude);
		URI uri = URI.create(API_ENDPOINT + "?latitude=" + format(latitude)
				+ "&longitude=" + format(longitude)
				+ "&current=" + currentVariables()
				+ "&wind_speed_unit=ms&timeformat=iso8601&timezone=GMT&forecast_days=1");

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
			return parse(response, latitude, longitude);
		} finally {
			connection.disconnect();
		}
	}

	static CurrentConditions parse(String json, double latitude, double longitude) throws IOException {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			JsonObject current = requiredObject(root, "current");
			double elevation = requiredDouble(root, "elevation");
			double temperature = requiredDouble(current, "temperature_2m") + 273.15;
			double pressure = requiredDouble(current, "surface_pressure") * 100.0;
			double relativeHumidity = requiredDouble(current, "relative_humidity_2m") / 100.0;
			double surfaceSpeed = requiredDouble(current, "wind_speed_10m");
			double surfaceDirection = radians(requiredDouble(current, "wind_direction_10m"));
			double gust = requiredDouble(current, "wind_gusts_10m");
			double turbulenceIntensity = estimateTurbulenceIntensity(surfaceSpeed, gust);

			Map<Double, CurrentConditions.WindLayer> layers = new TreeMap<>();
			addLayer(layers, elevation, surfaceSpeed, surfaceDirection, turbulenceIntensity);
			for (int height : ABOVE_GROUND_LEVELS) {
				addLayer(layers, elevation + height,
						requiredDouble(current, "wind_speed_" + height + "m"),
						radians(requiredDouble(current, "wind_direction_" + height + "m")),
						turbulenceIntensity);
			}

			for (int level : PRESSURE_LEVELS) {
				double altitude = requiredDouble(current, "geopotential_height_" + level + "hPa");
				if (altitude <= elevation) {
					continue;
				}
				addLayer(layers, altitude,
						requiredDouble(current, "wind_speed_" + level + "hPa"),
						radians(requiredDouble(current, "wind_direction_" + level + "hPa")),
						turbulenceIntensity);
			}

			Instant validAt = LocalDateTime.parse(requiredString(current, "time")).toInstant(ZoneOffset.UTC);
			return new CurrentConditions(latitude, longitude, elevation, validAt, temperature, pressure,
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

	private static String currentVariables() {
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

	private static String requiredString(JsonObject object, String name) throws IOException {
		if (!object.has(name) || object.get(name).isJsonNull()) {
			throw new IOException("Weather service response is missing " + name);
		}
		return object.get(name).getAsString();
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
}
