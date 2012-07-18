package net.sf.openrocket.android.thrustcurve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;


public abstract class ThrustCurveAPI {

	public static SearchResponse doSearch( SearchRequest request ) throws MalformedURLException, IOException {
		
		String requestString = request.toString();
		
		AndroidLogWrapper.d(ThrustCurveAPI.class, "doSearch: " + requestString);
		// Froyo has troubles resolving URLS constructed with protocols.  Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/search");

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

	public static List<MotorBurnFile> downloadData( Integer motor_id ) throws MalformedURLException, IOException {

		if ( motor_id == null ) {
			return null;
		}
		DownloadRequest dr = new DownloadRequest();
		dr.add(motor_id);

		String requestString = dr.toString();

		AndroidLogWrapper.d(ThrustCurveAPI.class, "downloadData: " + requestString);
		// Froyo has troubles resolving URLS constructed with protocols.  Because of this
		// we need to do it in parts.
		URL url = new URL("http", "www.thrustcurve.org", "/servlets/download");

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

		return downloadResponse.getData(motor_id);

    }
	
	/**
	 * look through the listOfMotors to find the one which best matches the motor requested.
	 * 
	 * The algorithm uses a score based method.  Each entry in listOfMotors is assigned a score
	 * and the element with the highest score is returned.  The score is computed as follows:
	 * 
	 * 1) if the element matches the digest of the requested motor eactly, score += 1000
	 * 1) if the element matches the designation in the requested motor exactly, score = 100
	 * 2) if the element is a RockSim file score += 10
	 * 
	 * @param motor
	 * @param listOfMotors
	 * @return
	 */
	public static ThrustCurveMotor findBestMatch( ThrustCurveMotorPlaceholder motor, List<MotorBurnFile> listOfMotors ) {
		
		ThrustCurveMotor bestMatch = null;
		int bestScore = -1;
		
		final String wantedDesignation = motor.getDesignation();
		final String wantedDigest = motor.getDigest();
		
		for ( MotorBurnFile entry : listOfMotors ) {
			int entryScore = 0;
			ThrustCurveMotor entryMotor = entry.getThrustCurveMotor();
			
			if ("RockSim".equals(entry.getFiletype()) ) {
				entryScore += 10;
			}

			if ( wantedDigest != null && wantedDigest.equals( entryMotor.getDigest() ) ) {
				entryScore += 1000;
			}
			
			if ( wantedDesignation != null && wantedDesignation.equals(entryMotor.getDesignation())) {
				entryScore += 100;
			}
			
			if ( entryScore > bestScore ) {
				bestScore = entryScore;
				bestMatch = entry.getThrustCurveMotor();
			}
			
		}
		
		return bestMatch;
	}
	
	/**
	 * Extract all unique motors from the list based on designation.
	 * @param listOfMotors
	 * @return
	 */
	public static List<ThrustCurveMotor> extractAllMotors( List<MotorBurnFile> listOfMotors ) {
		
		Map<String, ThrustCurveMotor> motorsByDesignation = new HashMap<String,ThrustCurveMotor>();
		
		for( MotorBurnFile entry : listOfMotors ) {
			ThrustCurveMotor motor = entry.getThrustCurveMotor();
			if ( motor != null ) {
				motorsByDesignation.put( motor.getDesignation(), motor);
			}
		}
		
		return new ArrayList<ThrustCurveMotor>(motorsByDesignation.values());
		
	}
}
