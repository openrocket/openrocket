package net.sf.openrocket.communication;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import net.sf.openrocket.util.BugException;

public abstract class Communicator {

	protected static final String BUG_REPORT_URL;

	protected static final String UPDATE_INFO_URL;
	
	static {
		String url;
		url = System.getProperty("openrocket.debug.bugurl");
		if (url == null)
			url = "http://openrocket.sourceforge.net/actions/reportbug";
		BUG_REPORT_URL = url;
		
		url = System.getProperty("openrocket.debug.updateurl");
		if (url == null)
			url = "http://openrocket.sourceforge.net/actions/updates";
		UPDATE_INFO_URL = url;
	}
	

	protected static final String VERSION_PARAM = "version";
	

	protected static final String BUG_REPORT_PARAM = "content";
	protected static final int BUG_REPORT_RESPONSE_CODE = HttpURLConnection.HTTP_ACCEPTED;
	protected static final int CONNECTION_TIMEOUT = 10000;  // in milliseconds

	protected static final int UPDATE_INFO_UPDATE_AVAILABLE = HttpURLConnection.HTTP_OK;
	protected static final int UPDATE_INFO_NO_UPDATE_CODE = HttpURLConnection.HTTP_NO_CONTENT;
	protected static final String UPDATE_INFO_CONTENT_TYPE = "text/plain";

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
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new BugException("Unsupported encoding UTF-8", e);
		}
	}
	
}
