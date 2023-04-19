package net.sf.openrocket.communication;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class Communicator {

	protected static final String BUG_REPORT_URL;

	protected static final String UPDATE_URL;
	protected static final String UPDATE_URL_LATEST;	// Extra URL needed for the latest GitHub release
	
	static {
		String url;
		url = System.getProperty("openrocket.debug.bugurl");
		if (url == null)
			url = "http://openrocket.sourceforge.net/actions/reportbug";
		BUG_REPORT_URL = url;
		
		url = System.getProperty("openrocket.debug.updateurl");
		if (url == null) {
			url = "https://api.github.com/repos/openrocket/openrocket/releases";
			UPDATE_URL_LATEST = "https://api.github.com/repos/openrocket/openrocket/releases/latest";
		}
		else {
			UPDATE_URL_LATEST = null;
		}
		UPDATE_URL = url;
	}
	

	protected static final String VERSION_PARAM = "version";
	

	protected static final String BUG_REPORT_PARAM = "content";
	protected static final int BUG_REPORT_RESPONSE_CODE = HttpURLConnection.HTTP_ACCEPTED;
	protected static final int CONNECTION_TIMEOUT = 10000;  // in milliseconds

	// Limit the number of bytes that can be read from the server
	protected static final int MAX_INPUT_BYTES = 20000;

	
	protected static ConnectionSource connectionSource = new DefaultConnectionSource();
	
	
	/**
	 * Set the source of the network connections.  This can be used for unit testing.
	 * By default the source is a DefaultConnectionSource.
	 * 
	 * @param source	the source of the connections.
	 */
	public static void setConnectionSource(ConnectionSource source) {
		connectionSource = source;
	}
	

	/**
	 * URL-encode the specified string in UTF-8 encoding.
	 * 
	 * @param str	the string to encode (null ok)
	 * @return		the encoded string or "null"
	 */
	public static String encode(String str) {
		if (str == null)
			return "null";
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
	
}
