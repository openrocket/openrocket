package net.sf.openrocket.android.thrustcurve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.sf.openrocket.android.util.AndroidLogWrapper;


public class ThrustCurveAPI {

	private String url_base = "http://www.thrustcurve.org/servlets/";
	
	public SearchResponse doSearch( SearchRequest request ) throws MalformedURLException, IOException {
		
		String requestString = request.toString();
		
		AndroidLogWrapper.d(ThrustCurveAPI.class, "doSearch: " + requestString);
		URL url = new URL(url_base + "search");

        OutputStream  stream;

        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        stream = conn.getOutputStream();

        stream.write(requestString.getBytes());
        
        InputStream is = conn.getInputStream();

        SearchResponse result = SearchResponseParser.parse(is);
        AndroidLogWrapper.d(ThrustCurveAPI.class,result.toString());
        
        return result;
	}

	public MotorBurnFile downloadData( Integer motor_id ) throws MalformedURLException, IOException {

		if ( motor_id == null ) {
			return null;
		}
		DownloadRequest dr = new DownloadRequest();
		dr.add(motor_id);

		String requestString = dr.toString();

		AndroidLogWrapper.d(ThrustCurveAPI.class, "downloadData: " + requestString);
		URL url = new URL(url_base + "download");

		OutputStream  stream;

		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);

		stream = conn.getOutputStream();

		stream.write(requestString.getBytes());

		InputStream is = conn.getInputStream();

		DownloadResponse downloadResponse = DownloadResponseParser.parse(is);
		AndroidLogWrapper.d(ThrustCurveAPI.class,downloadResponse.toString());

		MotorBurnFile mbf = downloadResponse.getData(motor_id);

		return mbf;

    }
}
