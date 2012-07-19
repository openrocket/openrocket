package net.sf.openrocket.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DecalRegistry {

	private File baseFile;
	private boolean isZipFile = false;
	
	/* FIXME - Caching ?
	private Map<String,byte[]> cache = new HashMap<String,byte[]>();
	*/
	
	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
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
			ZipInputStream zis = new ZipInputStream(new FileInputStream(baseFile));
			ZipEntry entry = zis.getNextEntry();
			while ( entry != null ) {
				if ( entry.getName().equals(name) ) {
					return zis;
				}
				entry = zis.getNextEntry();
			}
		}
		
		if ( baseFile != null ) {
			File decal = new File(baseFile.getParentFile(), name);
			// FIXME - update cache
			return new FileInputStream(decal);
		}
		throw new FileNotFoundException( "Unable to locate decal for name " + name );
	}
	
}
