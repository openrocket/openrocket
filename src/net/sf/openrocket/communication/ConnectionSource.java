package net.sf.openrocket.communication;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * A source for network connections.  This interface exists to enable unit testing.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ConnectionSource {

	/**
	 * Return a connection to the specified url.
	 * @param url	the URL to connect to.
	 * @return		the corresponding HttpURLConnection
	 * @throws IOException	if an IOException occurs
	 */
	public HttpURLConnection getConnection(String url) throws IOException;
	
}
