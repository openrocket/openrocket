package net.sf.openrocket.communication;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import net.sf.openrocket.util.BuildProperties;

public class BugReporter extends Communicator {
	
	// Inhibit instantiation
	private BugReporter() {
	}

	
	/**
	 * Send the provided report to the OpenRocket bug report URL.  If the connection
	 * fails or the server does not respond with the correct response code, an
	 * exception is thrown.
	 * 
	 * @param report		the report to send.
	 * @throws IOException	if an error occurs while connecting to the server or
	 * 						the server responds with a wrong response code.
	 */
	public static void sendBugReport(String report) throws IOException {
		
		HttpURLConnection connection = connectionSource.getConnection(BUG_REPORT_URL);
		
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setRequestProperty("X-OpenRocket-Version", encode(BuildProperties.getVersion()));
		
		String post;
		post = (VERSION_PARAM + "=" + encode(BuildProperties.getVersion())
				+ "&" + BUG_REPORT_PARAM + "=" + encode(report));
		
		OutputStreamWriter wr = null;
		try {
			// Send post information
			connection.setDoOutput(true);
			wr = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			wr.write(post);
			wr.flush();
			
			if (connection.getResponseCode() != BUG_REPORT_RESPONSE_CODE) {
				throw new IOException("Server responded with code " + 
						connection.getResponseCode() + ", expecting " + BUG_REPORT_RESPONSE_CODE);
			}
		} finally {
			if (wr != null)
				wr.close();
			connection.disconnect();
		}
	}
	
}
