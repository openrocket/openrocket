package net.sf.openrocket.communication;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ConnectionSourceStub implements ConnectionSource {
	
	private final HttpURLConnection connection;

	public ConnectionSourceStub(HttpURLConnection connection) {
		this.connection = connection;
	}
	
	@Override
	public HttpURLConnection getConnection(String url) throws IOException {
		if (connection instanceof HttpURLConnectionMock) {
			((HttpURLConnectionMock)connection).setTrueUrl(url);
		}
		return connection;
	}

}
