package info.openrocket.core.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import info.openrocket.core.util.BuildProperties;

import org.junit.jupiter.api.Test;

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
		assertEquals(connection.getRequestMethod(), "POST");
		assertFalse(connection.getUseCaches());
	}

	@Test
	public void testBugReportSuccess() throws IOException {
		HttpURLConnectionMock connection = setup();
		connection.setResponseCode(Communicator.BUG_REPORT_RESPONSE_CODE);

		String message = "MyMessage\n" +
				"is important\n" +
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

		String message = "MyMessage\n" +
				"is important\n" +
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
