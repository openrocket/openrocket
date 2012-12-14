package net.sf.openrocket.document;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.file.FileInfo;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.FileUtils;

public class DecalRegistry {
	private static LogHelper log = Application.getLogger();

	private FileInfo fileInfo;
	private boolean isZipFile = false;

	private Map<String,File> exportedDecalMap = new HashMap<String,File>();
	
	/* TODO - should we implement caching?
	private Map<String,byte[]> cache = new HashMap<String,byte[]>();
	 */

	public void setBaseFile(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public void setIsZipFile( boolean isZipFile ) {
		this.isZipFile = isZipFile;
	}

	/**
	 * Returns true if the named decal is exportable - that is, it is currently stored in
	 * the zip file.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isExportable( String name ) {
		if ( !isZipFile ) {
			return false;
		}
		try {
			InputStream is = forwardToEntry(name);
			if ( is != null ) {
				is.close();
				return true;
			} else {
				return false;
			}
		} catch ( IOException iex ) {
			return false;
		}
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
		/* TODO
		// if the decal has already been cached return it.
		byte[] bytes = cache.get(name);
		if ( bytes != null ) {
			return new ByteArrayInputStream(bytes);
		} 
		 */

		// This is the InputStream to be returned.
		InputStream rawIs = null;

		
		// First check if the decal had been exported
		{
			File exportedFile= exportedDecalMap.get(name);
			if ( exportedFile != null  ) {
				try {
					rawIs = new FileInputStream(exportedFile);
				} catch (FileNotFoundException ex) {
					// If we can no longer find the file, we'll try to resort to using a different loading
					// strategy.
					exportedDecalMap.remove(name);
				}
			}
		}

		if ( rawIs == null && isZipFile ) {
			rawIs = forwardToEntry(name);
		}

		// Check absolute file name:
		if ( rawIs == null ) {
			File decal = new File(name);
			if ( decal.isAbsolute() ) {
				try {
					rawIs = new FileInputStream(decal);
				} catch ( FileNotFoundException e ){
					name = decal.getName();
					log.debug("Unable to find absolute file" + decal + ", falling back to " + name);
				}
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
			byte[] bytes = FileUtils.readBytes(rawIs);
			// TODO - here we would update the cache.
			return new ByteArrayInputStream(bytes);
		}
		finally {
			rawIs.close();
		}

	}

	public void exportDecal( String decalName, File selectedFile ) throws IOException {
	
		try {
			InputStream is = getDecal(decalName);
			OutputStream os = new BufferedOutputStream( new FileOutputStream(selectedFile));

			FileUtils.copy(is, os);

			is.close();
			os.close();
			
			exportedDecalMap.put(decalName, selectedFile );
			
		}
		catch (IOException iex) {
			throw new BugException(iex);
		}

		
		
	}
	
	
	private ZipInputStream forwardToEntry( String name ) throws IOException {
		ZipInputStream zis = new ZipInputStream(fileInfo.fileURL.openStream());
		try {
			ZipEntry entry = zis.getNextEntry();
			while ( entry != null ) {
				if ( entry.getName().equals(name) ) {
					return zis;
				}
				entry = zis.getNextEntry();
			}
		}
		catch ( IOException ioex ) {
			zis.close();
			throw ioex;
		}
		zis.close();
		return null;
	}
}
