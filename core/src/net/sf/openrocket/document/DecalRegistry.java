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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.file.FileInfo;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.FileUtils;

public class DecalRegistry {
	private static LogHelper log = Application.getLogger();

	private FileInfo fileInfo;
	private boolean isZipFile = false;

	private Map<String,DecalImageImpl> registeredDecals = new HashMap<String,DecalImageImpl>();

	public void setBaseFile(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public void setIsZipFile( boolean isZipFile ) {
		this.isZipFile = isZipFile;
	}

	public DecalImage getDecalImage( String decalName ) {
		DecalImageImpl d = registeredDecals.get(decalName);
		if ( d == null ) {
			d = new DecalImageImpl(decalName);
			registeredDecals.put(decalName, d);
		}
		return d;
	}

	public DecalImage getDecalImage( File file ) {

		// See if this file is being used already
		DecalImageImpl decal = findDecalForFile( file );

		if ( decal != null ) {
			return decal;
		}

		// It's a new file, generate a name for it.
		String decalName = makeUniqueName( file.getName() );

		decal = new DecalImageImpl( decalName );
		decal.setFileSystemLocation( file );

		registeredDecals.put(decalName, decal);
		return decal;

	}

	public Set<DecalImage> getDecalList( ) {

		Set<DecalImage> decals = new TreeSet<DecalImage>();

		decals.addAll(registeredDecals.values());

		return decals;
	}

	public Set<DecalImage> getExportableDecalsList() {

		Set<DecalImage> exportableDecals = new HashSet<DecalImage>();

		for( DecalImage d : registeredDecals.values() ) {
			if ( isExportable(d.getName())) {
				exportableDecals.add(d);
			}
		}

		return exportableDecals;

	}

	public class DecalImageImpl implements DecalImage, Comparable {

		private final String name;

		private File fileSystemLocation;

		private DecalImageImpl( String name ) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public InputStream getBytes() throws FileNotFoundException, IOException {
			return DecalRegistry.this.getDecal(this);
		}

		@Override
		public void exportImage(File file, boolean watchForChanges) throws IOException {
			this.fileSystemLocation = file;
			DecalRegistry.this.exportDecal(this, file);
		}

		File getFileSystemLocation() {
			return fileSystemLocation;
		}

		void setFileSystemLocation( File fileSystemLocation ) {
			this.fileSystemLocation = fileSystemLocation;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int compareTo(Object o) {
			if ( ! (o instanceof DecalImageImpl ) ) {
				return -1;
			}
			return this.name.compareTo( ((DecalImageImpl)o).name );
		}

	}

	/**
	 * Returns true if the named decal is exportable - that is, it is currently stored in
	 * the zip file.
	 * 
	 * @param name
	 * @return
	 */
	private boolean isExportable( String name ) {
		if ( !isZipFile ) {
			return false;
		}
		try {
			InputStream is = findInZipContainer(name);
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
	private InputStream getDecal( DecalImageImpl decal ) throws FileNotFoundException, IOException {

		// This is the InputStream to be returned.
		InputStream rawIs = null;


		// First check if the decal is located on the file system
		File exportedFile= decal.getFileSystemLocation();
		if ( exportedFile != null  ) {
			rawIs = new FileInputStream(exportedFile);
		}

		String name = decal.getName();

		if ( rawIs == null && isZipFile ) {
			rawIs = findInZipContainer(name);
		}

		// Try relative to the model file directory.  This is so we can support unzipped container format.
		if ( rawIs == null ) {
			if( fileInfo != null && fileInfo.getDirectory() != null ) {
				File decalFile = new File(fileInfo.getDirectory(), name);
				rawIs = new FileInputStream(decalFile);
			}
		}

		if ( rawIs == null ) {
			throw new FileNotFoundException( "Unable to locate decal for name " + name );
		}

		try {
			byte[] bytes = FileUtils.readBytes(rawIs);
			return new ByteArrayInputStream(bytes);
		}
		finally {
			rawIs.close();
		}

	}

	private void exportDecal( DecalImageImpl decal, File selectedFile ) throws IOException {

		try {
			InputStream is = decal.getBytes();
			OutputStream os = new BufferedOutputStream( new FileOutputStream(selectedFile));

			FileUtils.copy(is, os);

			is.close();
			os.close();

		}
		catch (IOException iex) {
			throw new BugException(iex);
		}

	}


	private ZipInputStream findInZipContainer( String name ) {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(fileInfo.fileURL.openStream());
		} catch( IOException ex ) {
			return null;
		}
		try {
			ZipEntry entry = zis.getNextEntry();
			while ( entry != null ) {
				if ( entry.getName().equals(name) ) {
					return zis;
				}
				entry = zis.getNextEntry();
			}
			zis.close();
			return null;
		}
		catch ( IOException ioex ) {
			try {
				zis.close();
			} catch ( IOException ex ) {
				// why does close throw?  it's maddening
			}
			return null;
		}
	}

	private DecalImageImpl findDecalForFile( File file ) {

		for( DecalImageImpl d : registeredDecals.values() ) {
			if ( file.equals( d.getFileSystemLocation() ) ) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Regular expression for parsing file names with numerical identifiers.
	 * For examples:
	 * 
	 * decals/an image (3).png
	 * 
	 * group(0) = "decals/an image (3).png"
	 * group(1) = "decals/an image"
	 * group(2) = " (3)"
	 * group(3) = "3"
	 * group(4) = "png"
	 * 
	 * decals/an image.png
	 * 
	 * group(0) = "decals/an image.png"
	 * group(1) = "decals/an image"
	 * group(2) = "null"
	 * group(3) = "null"
	 * group(4) = "png"
	 */
	private static final Pattern fileNamePattern = Pattern.compile("(.*?)( \\((\\d+)\\)\\)?+)?\\.(\\w*)");
	private static final int BASE_NAME_INDEX = 1;
	private static final int NUMBER_INDEX = 3;
	private static final int EXTENSION_INDEX = 4;

	private String makeUniqueName( String name ) {

		String newName = "decals/" + name;
		String basename = "";
		String extension = "";
		Matcher nameMatcher = fileNamePattern.matcher(newName);
		if ( nameMatcher.matches() ) {
			basename = nameMatcher.group(BASE_NAME_INDEX);
			extension = nameMatcher.group(EXTENSION_INDEX);
		}

		Set<Integer> counts = new TreeSet<Integer>();

		boolean needsRewrite = false; 

		for ( DecalImageImpl d: registeredDecals.values() ) {
			Matcher m = fileNamePattern.matcher( d.getName() );
			if ( m.matches() ) {
				if ( basename.equals(m.group(BASE_NAME_INDEX)) && extension.equals(m.group(EXTENSION_INDEX))) {
					String intString = m.group(NUMBER_INDEX);
					if ( intString != null ) {
						Integer i = Integer.parseInt(intString);
						counts.add(i);
					}
					needsRewrite = true;
				}
			} else if ( newName.equals(d.getName() ) ) {
				needsRewrite = true;
			}
		}

		if ( !needsRewrite ) {
			return newName;
		}

		// find a missing integer;
		Integer newIndex = 1;
		while( counts.contains(newIndex)  ) {
			newIndex++;
		}

		return MessageFormat.format("{0} ({1}).{2}", basename,newIndex,extension);
	}

}
