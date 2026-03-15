package info.openrocket.core.thrustcurve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class DownloadResponseParser {

	public static DownloadResponse parse(InputStream in) throws IOException {
		DownloadResponse response = new DownloadResponse();

		// 1. Read InputStream to String
		String jsonString = readStream(in);

		// 2. Parse JSON
		JsonObject rootObj;
		try {
			JsonElement root = JsonParser.parseString(jsonString);
			if (!root.isJsonObject()) {
				return response;
			}
			rootObj = root.getAsJsonObject();
		} catch (JsonParseException ex) {
			throw new RuntimeException("Unable to parse JSON response: " + ex.getMessage(), ex);
		}

		// 3. Extract Results
		JsonArray results = rootObj.getAsJsonArray("results");
		if (results != null) {
			for (JsonElement item : results) {
				if (item.isJsonObject()) {
					JsonObject resultObj = item.getAsJsonObject();

					MotorBurnFile mbf = new MotorBurnFile();
					mbf.init();

					// Parse String ID
					String motorId = getString(resultObj, "motorId");
					if (motorId != null) {
						mbf.setMotorId(motorId);
					}

					String simfileId = getString(resultObj, "simfileId");
					if (simfileId != null) {
						mbf.setSimfileId(simfileId);
					}

					String fileType = getString(resultObj, "format");
					if (fileType != null) {
						mbf.setFiletype(fileType);
					}

					// Handle Data (Usually Base64 in V1 Download response)
					String dataContent = getString(resultObj, "data");
					if (dataContent != null) {
						// MotorBurnFile.decodeFile expects Base64
						mbf.decodeFile(dataContent);
					}

					response.add(mbf);
				}
			}
		}

		// 4. Handle Error (if any)
		String error = getString(rootObj, "error");
		if (error != null) {
			response.setError(error);
		}

		return response;
	}

	private static String getString(JsonObject obj, String key) {
		JsonElement val = getElement(obj, key);
		if (val == null) return null;
		if (val.isJsonPrimitive()) {
			return val.getAsString();
		}
		return val.toString();
	}

	private static JsonElement getElement(JsonObject obj, String key) {
		if (obj == null || key == null) return null;
		JsonElement val = obj.get(key);
		if (val == null || val.isJsonNull()) return null;
		return val;
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
}
