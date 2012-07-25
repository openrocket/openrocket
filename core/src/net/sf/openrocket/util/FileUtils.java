package net.sf.openrocket.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class FileUtils {

	public static void copy( InputStream is, OutputStream os ) throws IOException {
		
		if ( ! (os instanceof BufferedOutputStream ) ) {
			os = new BufferedOutputStream(os);
		}
		
		if ( ! (is instanceof BufferedInputStream ) ) {
			is = new BufferedInputStream(is);
		}
		
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		
		while( (bytesRead = is.read(buffer)) > 0 ) {
			os.write(buffer,0,bytesRead);
		}
		os.flush();
	}
	
	public static byte[] readBytes( InputStream is ) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

		copy( is, bos );

		return bos.toByteArray();

	}

}
