package info.openrocket.core.thrustcurve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.BaseTestCase;

public class SearchResponseParserTest extends BaseTestCase {

	@Test
	public void simpleParseTest() throws Exception {
		InputStream is = SearchResponseParserTest.class.getResourceAsStream("/thrustcurve/SampleSearchResponse.xml");
		SearchResponse response = SearchResponseParser.parse(is);
		assertEquals(252, response.getMatches());
	}
}
