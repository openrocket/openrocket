package info.openrocket.core.thrustcurve;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public abstract class ThrustCurveAPI {

	private static final Gson GSON = new Gson();

	public static SearchResponse doSearch(SearchRequest request) throws IOException, SAXException {

		String requestString = request.toString();
		String[] paths = new String[]{"/api/v1/search", "/api/v1/search.json"};

		SearchResponse lastResponse = null;
		for (String path : paths) {
			SearchResponse response = postSearch(path, requestString);
			lastResponse = response;
			if (response.getError() == null) {
				return response;
			}
		}

		return lastResponse == null ? new SearchResponse() : lastResponse;
	}

	private static SearchResponse postSearch(String path, String requestString) throws IOException {
		URL url = buildApiUrl(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json, application/xml");
		conn.setRequestProperty("User-Agent", "OpenRocket");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		// Write JSON Request
		try (OutputStream stream = conn.getOutputStream()) {
			stream.write(requestString.getBytes(StandardCharsets.UTF_8));
		}

		int status = conn.getResponseCode();
		String contentType = conn.getContentType();
		InputStream responseStream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
		if (responseStream == null) {
			SearchResponse response = new SearchResponse();
			response.setError("Empty response from ThrustCurve API (status " + status + ")");
			return response;
		}

		String body = readStream(responseStream);
		if (body.isEmpty()) {
			SearchResponse response = new SearchResponse();
			response.setError("Empty response body from ThrustCurve API (status " + status + ")");
			return response;
		}

		String lowered = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
		String trimmed = body.trim();
		boolean looksLikeHtml = trimmed.startsWith("<") &&
				(trimmed.toLowerCase(Locale.ROOT).contains("<html") || trimmed.toLowerCase(Locale.ROOT).contains("<!doctype"));
		if (lowered.contains("text/html") || looksLikeHtml) {
			String snippet = trimmed.substring(0, Math.min(trimmed.length(), 200));
			SearchResponse response = new SearchResponse();
			response.setError("HTML response from ThrustCurve API (" + url + ", status " + status + ", content-type " + contentType + "): " + snippet);
			return response;
		}

		SearchResponse response = SearchResponseParser.parse(
				new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
		if (response.getError() != null) {
			response.setError("ThrustCurve API error (" + url + ", status " + status + ", content-type " + contentType + "): " + response.getError());
		}
		return response;
	}

	/**
	 * Utilises the ThrustCurveAPI to get the Manufacturer abbreviations, for the purpose of being used to obtain the
	 * rest of the Motor Data per manufacturer.
	 * @return Array of Motor Manufacturer abbreviations.
	 */
	public static String[] downloadManufacturers() throws IOException {
		URL url = buildApiUrl("/api/v1/metadata.json");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent", "OpenRocket");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		} finally {
			conn.disconnect();
		}

		String jsonString = response.toString();
		return parseManufacturerAbbreviations(jsonString);
	}

	/**
	 * Parses the manufacturer abbreviations from the metadata JSON of the ThrustCurveAPI.
	 * @param jsonString The String representation of the ThrustCurveAPI metadata.
	 * @return Array of Motor Abbreviations.
	 */
	private static String[] parseManufacturerAbbreviations(String jsonString){
		try {
			JsonElement root = JsonParser.parseString(jsonString);
			if (!root.isJsonObject()) {
				return new String[0];
			}
			JsonObject obj = root.getAsJsonObject();
			JsonArray manufacturers = obj.getAsJsonArray("manufacturers");
			if (manufacturers == null) {
				return new String[0];
			}
			List<String> names = new ArrayList<>();
			for (JsonElement element : manufacturers) {
				if (!element.isJsonObject()) {
					continue;
				}
				JsonObject entry = element.getAsJsonObject();
				JsonElement abbrevElement = entry.get("abbrev");
				if (abbrevElement == null || abbrevElement.isJsonNull()) {
					continue;
				}
				names.add(abbrevElement.getAsString());
			}
			return names.toArray(new String[0]);
		} catch (JsonParseException ex) {
			return new String[0];
		}
	}


	public static List<MotorBurnFile> downloadData(String motor_id, String format) throws IOException {
		if (motor_id == null) {
			return null;
		}
		List<String> formats = new ArrayList<>();
		if (format != null) {
			formats.add(format);
		}
		String[] paths = new String[]{"/api/v1/download.json"};
		IOException lastError = null;
		for (String path : paths) {
			if (formats.isEmpty()) {
				try {
					List<MotorBurnFile> data = postDownload(path, motor_id, null);
					if (data != null && !data.isEmpty()) {
						return data;
					}
				} catch (IOException ex) {
					lastError = ex;
				}
				continue;
			}
			for (String fmt : formats) {
				try {
					List<MotorBurnFile> data = postDownload(path, motor_id, fmt);
					if (data != null && !data.isEmpty()) {
						return data;
					}
				} catch (IOException ex) {
					lastError = ex;
				}
			}
		}
		if (lastError != null) {
			throw lastError;
		}
		return Collections.emptyList();
	}

	private static List<MotorBurnFile> postDownload(String path, String motorId, String format) throws IOException {

		// Prepare JSON Body for /api/v1/download
		// Payload: { "motorIds": ["id"], "format": "rocksim" }
		JsonObject jsonBody = new JsonObject();
		JsonArray motorIds = new JsonArray();
		motorIds.add(motorId);
		jsonBody.add("motorIds", motorIds);
		if (format != null) {
			jsonBody.addProperty("format", format);
		}

		URL url = buildApiUrl(path);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent", "OpenRocket");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		try (OutputStream stream = conn.getOutputStream()) {
			stream.write(GSON.toJson(jsonBody).getBytes(StandardCharsets.UTF_8));
		}

		int status = conn.getResponseCode();
		InputStream responseStream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
		if (responseStream == null) {
			throw new IOException("Empty response from ThrustCurve API (" + url + ", status " + status + ")");
		}

		String body = readStream(responseStream);
		if (body.isEmpty()) {
			throw new IOException("Empty response body from ThrustCurve API (" + url + ", status " + status + ")");
		}

		String contentType = conn.getContentType();
		String lowered = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
		String trimmed = body.trim();
		boolean looksLikeHtml = trimmed.startsWith("<") &&
				(trimmed.toLowerCase(Locale.ROOT).contains("<html") || trimmed.toLowerCase(Locale.ROOT).contains("<!doctype"));
		if (lowered.contains("text/html") || looksLikeHtml) {
			String snippet = trimmed.substring(0, Math.min(trimmed.length(), 200));
			throw new IOException("HTML response from ThrustCurve API (" + url + ", status " + status + ", content-type " + contentType + "): " + snippet);
		}

		DownloadResponse downloadResponse;
		try {
			downloadResponse = DownloadResponseParser.parse(
					new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
		} catch (RuntimeException ex) {
			throw new IOException("Unable to parse download response (" + url + ", status " + status + "): " + ex.getMessage(), ex);
		}
		if (downloadResponse.getError() != null) {
			throw new IOException("ThrustCurve API error (" + url + ", status " + status + "): " + downloadResponse.getError());
		}
		List<MotorBurnFile> data = downloadResponse.getData(motorId);
		return data == null ? Collections.emptyList() : data;
	}

	private static String readStream(InputStream in) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(StandardCharsets.UTF_8);
	}

	private static URL buildApiUrl(String path) throws IOException {
		String base = getApiBase();
		URL baseUrl = new URL(base);
		return new URL(baseUrl, path);
	}

	private static String getApiBase() {
		String property = System.getProperty("thrustcurve.api.base");
		if (property != null) {
			String trimmed = property.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}
		String env = System.getenv("THRUSTCURVE_API_BASE");
		if (env != null) {
			String trimmed = env.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}
		return "https://www.thrustcurve.org";
	}

}
