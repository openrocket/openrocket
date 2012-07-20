package net.sf.openrocket.document;

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

	public InputStream getDecal( String name ) throws FileNotFoundException, IOException {
		/* FIXME - Caching?
		byte[] bytes = cache.get(name);
		if ( bytes != null ) {
			return new ByteArrayInputStream(bytes);
		} 
		 */
		if ( isZipFile ) {
			ZipInputStream zis = new ZipInputStream(fileInfo.fileURL.openStream());
			ZipEntry entry = zis.getNextEntry();
			while ( entry != null ) {
				if ( entry.getName().equals(name) ) {
					return zis;
				}
				entry = zis.getNextEntry();
			}
		}

		// Check absolute file name:
		{
			File decal = new File(name);
			if ( decal.isAbsolute() ) {
				return new FileInputStream(decal);
			}
		}
		
		// Try relative to the model file directory.
		if( fileInfo.getDirectory() != null ) {
			File decal = new File(fileInfo.getDirectory(), name);
			// FIXME - update cache
			return new FileInputStream(decal);
		}
		
		throw new FileNotFoundException( "Unable to locate decal for name " + name );
	}

}
