package net.sf.openrocket.document;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.file.FileInfo;

public class DecalRegistry {

	private FileInfo fileInfo;
	private boolean isZipFile = false;

	/* FIXME - Caching ?
	private Map<String,byte[]> cache = new HashMap<String,byte[]>();
	 */

	public void setBaseFile(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public void setIsZipFile( boolean isZipFile ) {
		this.isZipFile = isZipFile;
	}

	/**
	 * This function returns an InputStream backed by a byte[] containing the decal pixels.
	 * If it reads in the bytes from an actual file, the underlying file is closed.
	 * 
	 * @param name
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public InputStream getDecal( String name ) throws FileNotFoundException, IOException {
		/* FIXME - Caching?
		byte[] bytes = cache.get(name);
		if ( bytes != null ) {
			return new ByteArrayInputStream(bytes);
		} 
		 */

		InputStream rawIs = null;

		if ( isZipFile ) {
			ZipInputStream zis = new ZipInputStream(fileInfo.fileURL.openStream());
			ZipEntry entry = zis.getNextEntry();
			while ( entry != null ) {
				if ( entry.getName().equals(name) ) {
					rawIs = zis;
					break;
				}
				entry = zis.getNextEntry();
			}
		}

		// Check absolute file name:
		if ( rawIs == null ) {
			File decal = new File(name);
			if ( decal.isAbsolute() ) {
				rawIs = new FileInputStream(decal);
			}
		}

		// Try relative to the model file directory.
		if ( rawIs == null ) {
			if( fileInfo.getDirectory() != null ) {
				File decal = new File(fileInfo.getDirectory(), name);
				rawIs = new FileInputStream(decal);
			}
		}

		if ( rawIs == null ) {
			throw new FileNotFoundException( "Unable to locate decal for name " + name );
		}
		
		try {
			byte[] bytes = readBytes(rawIs);
			// FIXME - update cache;
			return new ByteArrayInputStream(bytes);
		}
		finally {
			rawIs.close();
		}
		
	}

	private static byte[] readBytes( InputStream is ) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

		byte[] buffer = new byte[1024];
		
		if ( ! (is instanceof BufferedInputStream ) ) {
			is = new BufferedInputStream(is);
		}

		int bytesRead = 0;
		while( (bytesRead = is.read(buffer)) > 0 ) {
			bos.write(buffer,0,bytesRead);
		}

		return bos.toByteArray();

	}

}
