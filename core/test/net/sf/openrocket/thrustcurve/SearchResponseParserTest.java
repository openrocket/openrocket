package net.sf.openrocket.thrustcurve;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class SearchResponseParserTest extends BaseTestCase {
	
	@Test
	public void simpleParseTest() throws Exception {
		InputStream is = SearchResponseParserTest.class.getResourceAsStream("SampleSearchResponse.xml");
		SearchResponse response = SearchResponseParser.parse(is);
		assertEquals(252, response.getMatches());
	}
}
