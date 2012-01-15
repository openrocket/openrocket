package net.sf.openrocket.android.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.Coordinate;

abstract class ConversionUtils {

	static double[] stringToDelays( String value ) {
		if (value == null || "".equals(value) ) {
			return new double[0];
		}
		String[] parts = value.split(",");
		double[] values = new double[parts.length];
		for( int i =0; i<parts.length; i++ ) {
			String p = parts[i];
			if ( "P".equals(p) ) {
				values[i] = Motor.PLUGGED;
			} else {
				double d = Double.parseDouble(p);
				values[i] = d;
			}
		}
		return values;
	}
	
	static String delaysToString( double[] delays ) {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for( double d:delays ) {
			if (!first) {
				s .append(",");
			} else {
				first = false;
			}
			if ( d == Motor.PLUGGED ) {
				s.append("P");
			} else {
				s.append(Math.round(d));
			}
		}
		return s.toString();
	}
	
	static double[] deserializeArrayOfDouble( byte[] bytes ) throws Exception {
		double[] data = null;
		if (bytes != null ) {
			ObjectInputStream is = new ObjectInputStream( new ByteArrayInputStream(bytes));
			data = (double[]) is.readObject();
		}
		return data;
	}

	static byte[] serializeArrayOfDouble( double[] data ) throws Exception {

		byte[] serObj = null;
		if ( data != null ) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(b);
			os.writeObject(data);
			os.close();
			serObj = b.toByteArray();
		}
		return serObj;
	}

	static Coordinate[] deserializeArrayOfCoordinate( byte[] bytes ) throws Exception {
		Coordinate[] data = null;
		if (bytes != null ) {
			ObjectInputStream is = new ObjectInputStream( new ByteArrayInputStream(bytes));
			data = (Coordinate[]) is.readObject();
		}
		return data;
	}

	static byte[] serializeArrayOfCoordinate( Coordinate[] data ) throws Exception {

		byte[] serObj = null;
		if ( data != null ) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(b);
			os.writeObject(data);
			os.close();
			serObj = b.toByteArray();
		}
		return serObj;
	}


}