package info.openrocket.core.thrustcurve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class SearchResponseParser {

	public static SearchResponse parse(InputStream in) throws IOException {
		SearchResponse response = new SearchResponse();
		String jsonString = readStream(in);
		String trimmed = jsonString.trim();
		if (trimmed.isEmpty()) {
			return response;
		}

		// Check if response is XML
		if (trimmed.charAt(0) == '<') {
			String lowered = trimmed.toLowerCase(Locale.ROOT);
			if (lowered.contains("<!doctype") || lowered.contains("<html")) {
				response.setError("Unexpected HTML/DTD response from ThrustCurve API");
				return response;
			}
			return parseXmlResponse(jsonString, response);
		}

		JsonObject rootObj;
		try {
			JsonElement root = JsonParser.parseString(jsonString);
			if (!root.isJsonObject()) {
				return response;
			}
			rootObj = root.getAsJsonObject();
		} catch (JsonParseException ex) {
			response.setError("Unable to parse JSON response: " + ex.getMessage());
			return response;
		}

		Integer matches = getInt(rootObj, "matches");
		if (matches == null) {
			matches = getInt(rootObj, "totalResults");
		}
		if (matches != null) {
			response.setMatches(matches);
		}

		String error = getString(rootObj, "error");
		if (error != null) {
			response.setError(error);
		}

		// V1 uses "results" array
		JsonArray results = rootObj.getAsJsonArray("results");
		if (results != null) {
			for (JsonElement item : results) {
				if (item.isJsonObject()) {
					JsonObject map = item.getAsJsonObject();
					TCMotor motor = new TCMotor();
					motor.init();

					// ID is now String
					motor.setMotor_id(getString(map, "motorId"));

					motor.setManufacturer(getString(map, "manufacturer"));
					motor.setManufacturer_abbr(getString(map, "manufacturerAbbrev"));
					motor.setDesignation(getString(map, "designation"));
					motor.setBrand_name(getString(map, "brandName"));
					motor.setCommon_name(getString(map, "commonName"));
					motor.setImpulse_class(getString(map, "impulseClass"));

					motor.setDiameter(getFloat(map, "diameter"));
					motor.setLength(getFloat(map, "length"));
					motor.setType(getString(map, "type"));
					motor.setCert_org(getString(map, "certOrg"));

					motor.setAvg_thrust_n(getFloat(map, "avgThrustN"));
					motor.setMax_thrust_n(getFloat(map, "maxThrustN"));
					motor.setTot_impulse_ns(getFloat(map, "totImpulseNs"));
					motor.setBurn_time_s(getFloat(map, "burnTimeS"));

					motor.setData_files(getInt(map, "dataFiles"));
					motor.setInfo_url(getString(map, "infoUrl"));

					motor.setTot_mass_g(getDouble(map, "totalWeightG"));
					motor.setProp_mass_g(getDouble(map, "propWeightG"));

					motor.setDelays(getString(map, "delays"));
					motor.setCase_info(getString(map, "caseInfo"));
					motor.setProp_info(getString(map, "propInfo"));
					motor.setAvailability(getString(map, "availability"));

					parseUpdatedOn(motor, getString(map, "updatedOn"));
					applyLegacyDefaults(motor);

					response.addMotor(motor);
				}
			}

			if (matches == null) {
				response.setMatches(response.getResults().size());
			}
		}

		return response;
	}

	private static SearchResponse parseXmlResponse(String xmlString, SearchResponse response) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setExpandEntityReferences(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

			NodeList matchNodes = doc.getElementsByTagName("matches");
			if (matchNodes.getLength() > 0) {
				String matchesText = matchNodes.item(matchNodes.getLength() - 1).getTextContent();
				Integer matches = parseInt(matchesText);
				if (matches != null) {
					response.setMatches(matches);
				}
			}

			NodeList results = doc.getElementsByTagName("result");
			for (int i = 0; i < results.getLength(); i++) {
				Node node = results.item(i);
				if (!(node instanceof Element)) continue;
				Element element = (Element) node;

				TCMotor motor = new TCMotor();
				motor.init();

				motor.setMotor_id(getChildText(element, "motor-id"));
				motor.setManufacturer(getChildText(element, "manufacturer"));
				motor.setManufacturer_abbr(getChildText(element, "manufacturer-abbrev"));
				motor.setDesignation(getChildText(element, "designation"));
				motor.setBrand_name(getChildText(element, "brand-name"));
				motor.setCommon_name(getChildText(element, "common-name"));
				motor.setImpulse_class(getChildText(element, "impulse-class"));

				motor.setDiameter(parseFloat(getChildText(element, "diameter")));
				motor.setLength(parseFloat(getChildText(element, "length")));
				motor.setType(getChildText(element, "type"));
				motor.setCert_org(getChildText(element, "cert-org"));

				motor.setAvg_thrust_n(parseFloat(getChildText(element, "avg-thrust-n")));
				motor.setMax_thrust_n(parseFloat(getChildText(element, "max-thrust-n")));
				motor.setTot_impulse_ns(parseFloat(getChildText(element, "tot-impulse-ns")));
				motor.setBurn_time_s(parseFloat(getChildText(element, "burn-time-s")));

				motor.setData_files(parseInt(getChildText(element, "data-files")));
				motor.setInfo_url(getChildText(element, "info-url"));

				motor.setTot_mass_g(parseDouble(getChildText(element, "total-weight-g")));
				motor.setProp_mass_g(parseDouble(getChildText(element, "prop-weight-g")));

				motor.setDelays(getChildText(element, "delays"));
				motor.setCase_info(getChildText(element, "case-info"));
				motor.setProp_info(getChildText(element, "prop-info"));
				motor.setAvailability(getChildText(element, "availability"));

				parseUpdatedOn(motor, getChildText(element, "updated-on"));
				applyLegacyDefaults(motor);

				response.addMotor(motor);
			}

			if (response.getMatches() == 0 && !response.getResults().isEmpty()) {
				response.setMatches(response.getResults().size());
			}

			return response;
		} catch (Exception ex) {
			response.setError("Unable to parse XML response: " + ex.getMessage());
			return response;
		}
	}

	private static String getChildText(Element parent, String tagName) {
		NodeList nodes = parent.getElementsByTagName(tagName);
		if (nodes.getLength() == 0) {
			return null;
		}
		return nodes.item(0).getTextContent().trim();
	}

	// Helpers to safely extract types from the JSON
	private static String getString(JsonObject obj, String key) {
		JsonElement val = getElement(obj, key);
		if (val == null) return null;
		if (val.isJsonPrimitive()) {
			return val.getAsString();
		}
		return val.toString();
	}

	private static Float getFloat(JsonObject obj, String key) {
		JsonElement val = getElement(obj, key);
		if (val == null || !val.isJsonPrimitive()) return null;
		JsonPrimitive prim = val.getAsJsonPrimitive();
		if (prim.isNumber()) return prim.getAsFloat();
		if (prim.isString()) return parseFloat(prim.getAsString());
		return null;
	}

	private static Double getDouble(JsonObject obj, String key) {
		JsonElement val = getElement(obj, key);
		if (val == null || !val.isJsonPrimitive()) return null;
		JsonPrimitive prim = val.getAsJsonPrimitive();
		if (prim.isNumber()) return prim.getAsDouble();
		if (prim.isString()) return parseDouble(prim.getAsString());
		return null;
	}

	private static Integer getInt(JsonObject obj, String key) {
		JsonElement val = getElement(obj, key);
		if (val == null || !val.isJsonPrimitive()) return null;
		JsonPrimitive prim = val.getAsJsonPrimitive();
		if (prim.isNumber()) return prim.getAsInt();
		if (prim.isString()) return parseInt(prim.getAsString());
		return null;
	}

	private static JsonElement getElement(JsonObject obj, String key) {
		if (obj == null || key == null) return null;
		JsonElement val = obj.get(key);
		if (val == null || val.isJsonNull()) return null;
		return val;
	}

	private static Float parseFloat(String value) {
		if (value == null || value.isEmpty()) return null;
		try {
			return Float.valueOf(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static Double parseDouble(String value) {
		if (value == null || value.isEmpty()) return null;
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static Integer parseInt(String value) {
		if (value == null || value.isEmpty()) return null;
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static void parseUpdatedOn(TCMotor motor, String dateStr) {
		if (dateStr == null) {
			return;
		}
		try {
			SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
			motor.setUpdated_on(iso.parse(dateStr));
		} catch (ParseException e) {
			// Ignore
		}
	}

	private static void applyLegacyDefaults(TCMotor motor) {
		if ("A".equalsIgnoreCase(motor.getImpulse_class()) && motor.getCommon_name() != null) {
			if (motor.getCommon_name().startsWith("1/2A")) motor.setImpulse_class("1/2A");
			else if (motor.getCommon_name().startsWith("1/4A")) motor.setImpulse_class("1/4A");
			else if (motor.getCommon_name().startsWith("Micro")) motor.setImpulse_class("1/8A");
		}

		if (motor.getCase_info() == null || motor.getCase_info().equalsIgnoreCase("single use")) {
			motor.setCase_info(motor.getType() + " " + motor.getDiameter() + "x" + motor.getLength());
		}
	}

	private static String readStream(InputStream in) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}
}
