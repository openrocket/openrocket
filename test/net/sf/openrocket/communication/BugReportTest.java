package net.sf.openrocket.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import net.sf.openrocket.util.BuildProperties;

import org.junit.Test;


public class BugReportTest {
	
	private HttpURLConnectionMock setup() {
		HttpURLConnectionMock connection = new HttpURLConnectionMock();
		Communicator.setConnectionSource(new ConnectionSourceStub(connection));
		
		connection.setUseCaches(true);
		return connection;
	}
	
	private void check(HttpURLConnectionMock connection) {
		assertEquals(Communicator.BUG_REPORT_URL, connection.getTrueUrl());
		assertTrue(connection.getConnectTimeout() > 0);
		assertEquals(BuildProperties.getVersion(), connection.getRequestProperty("X-OpenRocket-Version"));
		assertTrue(connection.getInstanceFollowRedirects());
		assertEquals("POST", connection.getRequestMethod());
		assertFalse(connection.getUseCaches());
	}
	

	@Test
	public void testBugReportSuccess() throws IOException {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(Communicator.BUG_REPORT_RESPONSE_CODE);
		
		String message = 
			"MyMessage\n"+
			"is important\n"+
			"h\u00e4h?";
		
		BugReporter.sendBugReport(message);

		check(connection);
		
		String msg = connection.getOutputStreamString();
		assertTrue(msg.indexOf("version=" + BuildProperties.getVersion()) >= 0);
		assertTrue(msg.indexOf(Communicator.encode(message)) >= 0);
	}
	

	@Test
	public void testBugReportFailure() throws IOException {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(200);
		
		String message = 
			"MyMessage\n"+
			"is important\n"+
			"h\u00e4h?";
		
		try {
			BugReporter.sendBugReport(message);
			fail("Exception did not occur");
		} catch (IOException e) {
			// Success
		}

		check(connection);
	}
	
}
