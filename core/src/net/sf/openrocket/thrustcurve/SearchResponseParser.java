package net.sf.openrocket.thrustcurve;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.SimpleSAX;

public class SearchResponseParser implements ElementHandler {
	
	private static final String thrustcurveURI = "http://www.thrustcurve.org/2008/SearchResponse";
	/*
	 * XML Tags in SearchResult xsd
	 */
	private static final String root_tag = "search-response";
	private static final String criteria = "criteria";
	private static final String criterion = "criterion";
	private static final String name = "name";
	private static final String value = "value";
	private static final String matches = "matches";
	private static final String results = "results";
	private static final String result = "result";
	
	private static final String motor_id = "motor-id";
	private static final String manufacturer = "manufacturer";
	private static final String manufacturer_abbr = "manufacturer-abbrev";
	private static final String designation = "designation";
	private static final String brand_name = "brand-name";
	private static final String common_name = "common-name";
	private static final String impulse_class = "impulse-class";
	private static final String diameter = "diameter";
	private static final String length = "length";
	private static final String type = "type";
	private static final String cert_org = "cert-org";
	private static final String avg_thrust_n = "avg-thrust-n";
	private static final String max_thrust_n = "max-thrust-n";
	private static final String tot_impulse_ns = "tot-impulse-ns";
	private static final String burn_time_s = "burn-time-s";
	private static final String data_files = "data-files";
	private static final String info_url = "info-url";
	private static final String total_weight_g = "total-weight-g";
	private static final String prop_weight_g = "prop-weight-g";
	private static final String delays = "delays";
	private static final String case_info = "case-info";
	private static final String prop_info = "prop-info";
	private static final String updated_on = "updated-on";
	private static final String availability = "availability";
	
	private final SearchResponse response = new SearchResponse();
	
	private TCMotor currentMotor;
	
	private SearchResponseParser() {
	}
	
	public static SearchResponse parse(InputStream in) throws IOException, SAXException {
		
		SearchResponseParser handler = new SearchResponseParser();
		WarningSet warnings = new WarningSet();
		SimpleSAX.readXML(new InputSource(in), handler, warnings);
		
		return handler.response;
		
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
		if (result.equals(element)) {
			currentMotor = new TCMotor();
		}
		return this;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		
		switch (element) {
		case result:
			// Convert impulse class.  ThrustCurve puts mmx, 1/4a and 1/2a as A.
			if ("a".equalsIgnoreCase(currentMotor.getImpulse_class())) {
				if (currentMotor.getCommon_name().startsWith("1/2A")) {
					currentMotor.setImpulse_class("1/2A");
				} else if (currentMotor.getCommon_name().startsWith("1/4A")) {
					currentMotor.setImpulse_class("1/4A");
				} else if (currentMotor.getCommon_name().startsWith("Micro")) {
					currentMotor.setImpulse_class("1/8A");
				}
			}
			
			// Convert Case Info.
			if (currentMotor.getCase_info() == null
					|| "single use".equalsIgnoreCase(currentMotor.getCase_info())
					|| "single-use".equalsIgnoreCase(currentMotor.getCase_info())) {
				currentMotor.setCase_info(currentMotor.getType() + " " + currentMotor.getDiameter() + "x" + currentMotor.getLength());
			}
			response.addMotor(currentMotor);
			break;
		case matches:
			this.response.setMatches(Integer.parseInt(content));
			break;
		case motor_id:
			currentMotor.setMotor_id(Integer.parseInt(content));
			break;
		case manufacturer:
			currentMotor.setManufacturer(content);
			break;
		case manufacturer_abbr:
			currentMotor.setManufacturer_abbr(content);
			break;
		case designation:
			currentMotor.setDesignation(content);
			break;
		case brand_name:
			currentMotor.setBrand_name(content);
			break;
		case common_name:
			currentMotor.setCommon_name(content);
			break;
		case impulse_class:
			currentMotor.setImpulse_class(content);
			break;
		case diameter:
			currentMotor.setDiameter(Float.parseFloat(content));
			break;
		case length:
			currentMotor.setLength(Float.parseFloat(content));
			break;
		case type:
			currentMotor.setType(content);
			break;
		case cert_org:
			currentMotor.setCert_org(content);
			break;
		case avg_thrust_n:
			currentMotor.setAvg_thrust_n(Float.parseFloat(content));
			break;
		case max_thrust_n:
			currentMotor.setMax_thrust_n(Float.parseFloat(content));
			break;
		case tot_impulse_ns:
			currentMotor.setTot_impulse_ns(Float.parseFloat(content));
			break;
		case burn_time_s:
			currentMotor.setBurn_time_s(Float.parseFloat(content));
			break;
		case data_files:
			currentMotor.setData_files(Integer.parseInt(content));
			break;
		case info_url:
			currentMotor.setInfo_url(content);
			break;
		case total_weight_g:
			currentMotor.setTot_mass_g(Double.parseDouble(content));
			break;
		case prop_weight_g:
			currentMotor.setProp_mass_g(Double.parseDouble(content));
			break;
		case delays:
			currentMotor.setDelays(content);
			break;
		case case_info:
			currentMotor.setCase_info(content);
			break;
		case prop_info:
			currentMotor.setProp_info(content);
			break;
		case availability:
			currentMotor.setAvailability(content);
			break;
		case updated_on:
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			try {
				currentMotor.setUpdated_on(formatter.parse(content));
			} catch (ParseException ignored) { }
			break;
		}
		
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
	}
	
}
