package net.sf.openrocket.android.thrustcurve;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;


class RaspBurnFile{

	private final static String TAG = "RaspBurnFile";
	
	private final static int HEADER = 0;
	private final static int DATA = 1;
	private final static Pattern headerPattern = Pattern.compile("(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)");
	private final static Pattern dataPattern = Pattern.compile("(\\S*)\\s+(\\S*)");
	
	static void parse( MotorBurnFile that, String filecontents ) {
		
		int state = HEADER;
		
		LineNumberReader reader = new LineNumberReader( new StringReader(filecontents));
		
		Vector<Double> datapoints = new Vector<Double>();
		
		String line;
		Matcher m;
		try {
		while ( (line = reader.readLine()) != null ) {
			line = line.trim();
			Log.d("RASP",line);
			if ( line.startsWith(";")) {
				continue;
			}
			switch (state) {
			
			case HEADER:
				Log.d("RASP","header");
				m = headerPattern.matcher(line);
				if ( m.matches() ) {
					Log.d("RASP","header matches");
					
					/*motorName = m.group(1);*/
					/*diameter = Integer.decode(m.group(2));*/
					that.setLength(Float.parseFloat(m.group(3)) );
					String delays = m.group(4);
					if ( delays != null ) {
						delays = delays.replace("-", ",");
						that.setDelays(delays);
					}
					that.setPropWeightG(Double.parseDouble(m.group(5))*1000.0);
					that.setTotWeightG(Double.parseDouble(m.group(6))*1000.0);
					/*manufacturer = m.group(7);*/
					
				}
				state = DATA;
				break;
				
			case DATA:
				Log.d("RASP","data");
				m = dataPattern.matcher(line);
				if ( m.matches() ) {
					Log.d("RASP","data matches");
					Double x = Double.parseDouble(m.group(1));
					Double y = Double.parseDouble(m.group(2));
					Log.d("RASP","data matches ("+x+","+y+")");
					datapoints.add(x);
					datapoints.add(y);
				}
				break;
			}
			that.setDatapoints(datapoints);
		}
		} catch (IOException ex ) {
			Log.d(TAG,"Unable to parse Rasp file: " + ex);
		}
		
	}
	

}
