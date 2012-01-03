package net.sf.openrocket.android.thrustcurve;

import java.io.InputStream;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class SearchResponseParser {

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
	
	public static SearchResponse parse( InputStream in ) {
		
		final SearchResponse ret = new SearchResponse();
		final TCMotor currentMotor = new TCMotor();

		RootElement rootEl = new RootElement(thrustcurveURI, root_tag);
		Element criteriaEl = rootEl.getChild( thrustcurveURI, criteria);
		
		criteriaEl.getChild(thrustcurveURI,matches).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						ret.setMatches(Integer.parseInt(arg0));
					}
				}
		);
		Element resultsEl = rootEl.getChild(thrustcurveURI,results);
		Element resultEl = resultsEl.getChild(thrustcurveURI,result);

		resultEl.setStartElementListener(
				new StartElementListener() {
					@Override
					public void start(Attributes arg0) {
						currentMotor.init();
					}
				}
		);
		
		resultEl.setEndElementListener(
				new EndElementListener() {
					@Override
					public void end() {
						ret.getResults().add((TCMotor)currentMotor.clone());
					}
				}
		);
		
		resultEl.getChild(thrustcurveURI,motor_id).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setMotor_id(Integer.parseInt(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,manufacturer).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						if ( arg0 != null ) {
							currentMotor.setManufacturer(arg0);
						}
					}
				}
		);
		resultEl.getChild(thrustcurveURI,manufacturer_abbr).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						if ( arg0 != null ) {
							currentMotor.setManufacturer_abbr(arg0);
						}
					}
				}
		);
		resultEl.getChild(thrustcurveURI,designation).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setDesignation(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,brand_name).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setBrand_name(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,common_name).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setCommon_name(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,impulse_class).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setImpulse_class(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,diameter).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setDiameter(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,length).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setLength(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,type).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setType(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,cert_org).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setCert_org(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,avg_thrust_n).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setAvg_thrust_n(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,max_thrust_n).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setMax_thrust_n(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,tot_impulse_ns).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setTot_impulse_ns(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,burn_time_s).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setBurn_time_s(Float.parseFloat(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,data_files).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setData_files(Integer.parseInt(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,info_url).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setInfo_url(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,total_weight_g).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setTot_mass_g(Double.parseDouble(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,prop_weight_g).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setProp_mass_g(Double.parseDouble(arg0));
					}
				}
		);
		resultEl.getChild(thrustcurveURI,delays).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setDelays(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,case_info).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setCase_info(arg0);
					}
				}
		);
		resultEl.getChild(thrustcurveURI,prop_info).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setProp_info(arg0);
					}
				}
		);


        try {
            Xml.parse(in, Xml.Encoding.UTF_8,  rootEl.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

		return ret;
	}
	
}
