package info.openrocket.core.thrustcurve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.BaseTestCase;

public class SearchResponseParserTest extends BaseTestCase {

	@Test
	public void simpleParseTest() throws Exception {
		InputStream is = SearchResponseParserTest.class.getResourceAsStream("/thrustcurve/SampleSearchResponse.json");
		SearchResponse response = SearchResponseParser.parse(is);
		assertEquals(2, response.getMatches());
		assertEquals(2, response.getResults().size());
		TCMotor motor = response.getResults().get(0);
		assertEquals("507f1f77bcf86cd799439011", motor.getMotor_id());
		assertEquals("AeroTech", motor.getManufacturer());
	}

	@Test
	public void xmlFallbackParseTest() throws Exception {
		String xml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<search-response>" +
				"<criteria><matches>1</matches></criteria>" +
				"<results>" +
				"<result>" +
				"<motor-id>917</motor-id>" +
				"<manufacturer>AeroTech</manufacturer>" +
				"<designation>H123</designation>" +
				"<type>reload</type>" +
				"<diameter>38.0</diameter>" +
				"<length>152.0</length>" +
				"</result>" +
				"</results>" +
				"</search-response>";
		InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		SearchResponse response = SearchResponseParser.parse(is);
		assertEquals(1, response.getMatches());
		assertEquals(1, response.getResults().size());
		assertEquals("917", response.getResults().get(0).getMotor_id());
	}
}
