// thzero
package net.sf.openrocket.thrustcurve;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

// thzero - begin
import net.sf.openrocket.util.BaseTestCase;
// thzero - end

public class SearchResponseParserTest extends BaseTestCase {
	
	@Test
	public void simpleParseTest() throws Exception {
// thzero - begin
		InputStream is = SearchResponseParserTest.class.getResourceAsStream("/thrustcurve/SampleSearchResponse.xml");
// thzero - end
		SearchResponse response = SearchResponseParser.parse(is);
		assertEquals(252, response.getMatches());
	}
}
