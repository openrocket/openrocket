package net.sf.openrocket.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;


public abstract class RocketSaver {
	
	/**
	 * Save the document to the specified file using the default storage options.
	 * 
	 * @param dest			the destination file.
	 * @param document		the document to save.
	 * @throws IOException	in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument document) throws IOException {
		save(dest, document, document.getDefaultStorageOptions());
	}

	
	/**
	 * Save the document to the specified file using the given storage options.
	 * 
	 * @param dest			the destination file.
	 * @param document		the document to save.
	 * @param options		the storage options.
	 * @throws IOException	in case of an I/O error.
	 */
	public void save(File dest, OpenRocketDocument document, StorageOptions options) 
	throws IOException {
		OutputStream s = new BufferedOutputStream(new FileOutputStream(dest));
		try {
			save(s, document, options);
		} finally {
			s.close();
		}
	}
	
	
	/**
	 * Save the document to the specified output stream using the default storage options.
	 * 
	 * @param dest			the destination stream.
	 * @param doc			the document to save.
	 * @throws IOException	in case of an I/O error.
	 */
	public final void save(OutputStream dest, OpenRocketDocument doc) throws IOException {
		save(dest, doc, doc.getDefaultStorageOptions());
	}
	
	
	/**
	 * Save the document to the specified output stream using the given storage options.
	 * 
	 * @param dest			the destination stream.
	 * @param doc			the document to save.
	 * @param options		the storage options.
	 * @throws IOException	in case of an I/O error.
	 */
	public abstract void save(OutputStream dest, OpenRocketDocument doc, 
			StorageOptions options) throws IOException;
	
	
	
	
	
	
	
	public static String escapeXML(String s) {

		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"","&quot;");
		s = s.replace("'", "&apos;");
		
		for (int i=0; i < s.length(); i++) {
			char n = s.charAt(i);
			if (((n < 32) && (n != 9) && (n != 10) && (n != 13)) || (n == 127)) {
				s = s.substring(0,i) + "&#" + ((int)n) + ";" + s.substring(i+1);
			}
		}
		
		return s;
	}
}
