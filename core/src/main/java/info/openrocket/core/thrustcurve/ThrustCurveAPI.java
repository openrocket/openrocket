package info.openrocket.core.thrustcurve;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.SAXException;

public abstract class ThrustCurveAPI {

	public static SearchResponse doSearch(SearchRequest request) throws IOException, SAXException {

		String requestString = request.toString();

		// Froyo has troubles resolving URLS constructed with protocols. Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/search");

		OutputStream stream;

		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(2000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		stream = conn.getOutputStream();

		stream.write(requestString.getBytes());

		InputStream is = conn.getInputStream();

        return SearchResponseParser.parse(is);
	}

	//TODO might be ideal to have a fallback, incase there are issues with the ThrustCurveAPI metadata?
	/**
	 * Utilises the ThrustCurveAPI to get the Manufacturer abbreviations, for the purpose of being used to obtain the
	 * rest of the Motor Data per manufacturer.
	 * @return Array of Motor Manufacturer abbreviations.
	 */
	public static String[] downloadManufacturers() throws IOException {
		URL url = new URL("https", "www.thrustcurve.org", "/api/v1/metadata.json");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

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
		int start = jsonString.indexOf("\"manufacturers\":");
		if (start == -1) return new String[0];

		start = jsonString.indexOf("[", start);
		int end = jsonString.indexOf("]", start);
		if (start == -1 || end == -1) return new String[0];

		String manufacturersArray = jsonString.substring(start + 1, end);

		List<String> names = new ArrayList<>();
		for (String entry : manufacturersArray.split("\\{")) {
			int nameIndex = entry.indexOf("\"abbrev\":");
			if (nameIndex == -1) continue;
			// Developer Note (Jordan Senft): Added the "9" as its own declared value to avoid confusion if others_
			// _wish to contribute to this class in the future. This could be subject to change in future versions of_
			// _the ThrustCurveAPI.
			int literalStringLength = 9;
			int quoteStart = entry.indexOf("\"", literalStringLength + 9);
			int quoteEnd = entry.indexOf("\"", quoteStart + 1);
			if (quoteStart != -1 && quoteEnd != -1) {
				String name = entry.substring(quoteStart + 1, quoteEnd);
				names.add(name);
			}
		}

		return names.toArray(new String[0]);
	}


	public static List<MotorBurnFile> downloadData(Integer motor_id, String format) throws IOException, SAXException {

		if (motor_id == null) {
			return null;
		}
		DownloadRequest dr = new DownloadRequest();
		dr.add(motor_id);
		dr.setFormat(format);

		String requestString = dr.toString();

		// Froyo has troubles resolving URLS constructed with protocols. Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/download");

		OutputStream stream;

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.connect();

		stream = conn.getOutputStream();

		stream.write(requestString.getBytes());

		if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
			return Collections.emptyList();
		}
		InputStream is = conn.getInputStream();

		DownloadResponse downloadResponse = DownloadResponseParser.parse(is);

		return downloadResponse.getData(motor_id);

	}

}
