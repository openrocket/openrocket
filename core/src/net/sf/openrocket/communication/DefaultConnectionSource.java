package net.sf.openrocket.communication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Default implementation of ConnectionSource, which simply opens a new
 * HttpURLConnection from a URL object.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DefaultConnectionSource implements ConnectionSource {

	@Override
	public HttpURLConnection getConnection(String urlString) throws IOException {
		URL url = new URL(urlString);
		return (HttpURLConnection) url.openConnection();
	}

}
