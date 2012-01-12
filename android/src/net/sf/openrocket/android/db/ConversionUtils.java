package net.sf.openrocket.android.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.openrocket.util.Coordinate;

abstract class ConversionUtils {

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