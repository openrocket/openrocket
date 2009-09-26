package net.sf.openrocket.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import net.sf.openrocket.util.ComparablePair;
import net.sf.openrocket.util.Prefs;

public class Communication {

	private static final String BUG_REPORT_URL = 
		"http://openrocket.sourceforge.net/actions/reportbug";
	private static final String UPDATE_INFO_URL =
		"http://openrocket.sourceforge.net/actions/updates";

	private static final String VERSION_PARAM = "version";
	
	
	private static final String BUG_REPORT_PARAM = "content";
	private static final int BUG_REPORT_RESPONSE_CODE = HttpURLConnection.HTTP_ACCEPTED;
	private static final int CONNECTION_TIMEOUT = 10000;  // in milliseconds

	private static final int UPDATE_INFO_UPDATE_AVAILABLE = HttpURLConnection.HTTP_OK;
	private static final int UPDATE_INFO_NO_UPDATE_CODE = HttpURLConnection.HTTP_NO_CONTENT;
	private static final String UPDATE_INFO_CONTENT_TYPE = "text/plain";

	
	private static UpdateInfoFetcher fetcher = null;
	

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
		URL url = new URL(BUG_REPORT_URL);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setConnectTimeout(CONNECTION_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setRequestProperty("X-OpenRocket-Version", encode(Prefs.getVersion()));
		
		String post;
		post = (VERSION_PARAM + "=" + encode(Prefs.getVersion())
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
	
	
	
	/**
	 * Start an asynchronous task that will fetch information about the latest
	 * OpenRocket version.  This will overwrite any previous fetching operation.
	 */
	public static void startFetchUpdateInfo() {
		fetcher = new UpdateInfoFetcher();
		fetcher.start();
	}
	
	
	/**
	 * Check whether the update info fetching is still in progress.
	 * 
	 * @return	<code>true</code> if the communication is still in progress.
	 */
	public static boolean isFetchUpdateInfoRunning() {
		if (fetcher == null) {
			throw new IllegalStateException("startFetchUpdateInfo() has not been called");
		}
		return fetcher.isAlive();
	}
	
	
	/**
	 * Retrieve the result of the background update info fetcher.  This method returns 
	 * the result of the previous call to {@link #startFetchUpdateInfo()}. It must be
	 * called before calling this method.
	 * <p>
	 * This method will return <code>null</code> if the info fetcher is still running or
	 * if it encountered a problem in communicating with the server.  The difference can
	 * be checked using {@link #isFetchUpdateInfoRunning()}.
	 * 
	 * @return	the update result, or <code>null</code> if the fetching is still in progress
	 * 			or an error occurred while communicating with the server.
	 * @throws	IllegalStateException	if {@link #startFetchUpdateInfo()} has not been called.
	 */
	public static UpdateInfo getUpdateInfo() {
		if (fetcher == null) {
			throw new IllegalStateException("startFetchUpdateInfo() has not been called");
		}
		return fetcher.info;
	}
	
	
	
	/**
	 * Parse the data received from the server.
	 * 
	 * @param r		the Reader from which to read.
	 * @return		an UpdateInfo construct, or <code>null</code> if the data was invalid.
	 * @throws IOException	if an I/O exception occurs.
	 */
	/* package-private */
	static UpdateInfo parseUpdateInput(Reader r) throws IOException {
		BufferedReader reader;
		if (r instanceof BufferedReader) {
			reader = (BufferedReader)r;
		} else {
			reader = new BufferedReader(r);
		}
		
		
		String version = null;
		ArrayList<ComparablePair<Integer,String>> updates = 
			new ArrayList<ComparablePair<Integer,String>>();
		
		String str = reader.readLine();
		while (str != null) {
			if (str.matches("^Version: *[0-9]+\\.[0-9]+\\.[0-9]+[a-zA-Z0-9.-]* *$")) {
				version = str.substring(8).trim();
			} else if (str.matches("^[0-9]+:\\p{Print}+$")) {
				int index = str.indexOf(':');
				int value = Integer.parseInt(str.substring(0, index));
				String desc = str.substring(index+1).trim();
				if (!desc.equals("")) {
					updates.add(new ComparablePair<Integer,String>(value, desc));
				}
			}
			// Ignore anything else
			str = reader.readLine();
		}
		
		if (version != null) {
			return new UpdateInfo(version, updates);
		} else {
			return null;
		}
	}
	
	
	
	
	private static class UpdateInfoFetcher extends Thread {

		private volatile UpdateInfo info = null;
		
		@Override
		public void run() {
			try {
				doConnection();
			} catch (IOException e) {
				return;
			}
		}
		
		
		private void doConnection() throws IOException {
			URL url;
			url = new URL(UPDATE_INFO_URL + "?" + VERSION_PARAM + "=" + 
					encode(Prefs.getVersion()));
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setRequestProperty("X-OpenRocket-Version", encode(Prefs.getVersion()));
			connection.setRequestProperty("X-OpenRocket-ID", encode(Prefs.getUniqueID()));
			connection.setRequestProperty("X-OpenRocket-OS", encode(
					System.getProperty("os.name") + " " + System.getProperty("os.arch")));
			connection.setRequestProperty("X-OpenRocket-Java", encode(
					System.getProperty("java.vendor") + " " + System.getProperty("java.version")));
			connection.setRequestProperty("X-OpenRocket-Country", encode(
					System.getProperty("user.country")));
			
			InputStream is = null;
			try {
				connection.connect();
				
				if (connection.getResponseCode() == UPDATE_INFO_NO_UPDATE_CODE) {
					// No updates are available
					info = new UpdateInfo();
					return;
				}
				
				if (connection.getResponseCode() != UPDATE_INFO_UPDATE_AVAILABLE) {
					// Error communicating with server
					return;
				}
				
				if (!UPDATE_INFO_CONTENT_TYPE.equalsIgnoreCase(connection.getContentType())) {
					// Unknown response type
					return;
				}
				
				// Update is available, parse input
				is = connection.getInputStream();
				String encoding = connection.getContentEncoding();
				if (encoding == null)
					encoding = "UTF-8";
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
				
				

			} finally {
				if (is != null)
					is.close();
				connection.disconnect();
			}

			
		}
		
	}
	
	
	private static String encode(String str) {
		if (str == null)
			return "null";
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding UTF-8", e);
		}
	}

}
