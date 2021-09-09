package net.sf.openrocket.thrustcurve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import org.xml.sax.SAXException;


public abstract class ThrustCurveAPI {
	
	public static SearchResponse doSearch(SearchRequest request) throws IOException, SAXException {
		
		String requestString = request.toString();
		
		// Froyo has troubles resolving URLS constructed with protocols.  Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/search");
		
		OutputStream stream;
		
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(2000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		
		stream = conn.getOutputStream();
		
		stream.write(requestString.getBytes());
		
		InputStream is = conn.getInputStream();
		
		SearchResponse result = SearchResponseParser.parse(is);
		
		return result;
	}
	
	public static List<MotorBurnFile> downloadData(Integer motor_id, String format) throws IOException, SAXException {
		
		if (motor_id == null) {
			return null;
		}
		DownloadRequest dr = new DownloadRequest();
		dr.add(motor_id);
		dr.setFormat(format);
		
		String requestString = dr.toString();
		
		// Froyo has troubles resolving URLS constructed with protocols.  Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/download");
		
		OutputStream stream;
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.connect();
		
		stream = conn.getOutputStream();
		
		stream.write(requestString.getBytes());
		
		if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
			return Collections.emptyList();
		}
		InputStream is = conn.getInputStream();
		
		DownloadResponse downloadResponse = DownloadResponseParser.parse(is);
		
		return downloadResponse.getData(motor_id);
		
	}
	
}
