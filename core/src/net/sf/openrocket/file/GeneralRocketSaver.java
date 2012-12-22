package net.sf.openrocket.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.document.StorageOptions.FileType;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.file.rocksim.export.RocksimSaver;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;

public class GeneralRocketSaver {

	/**
	 * Interface which can be implemented by the caller to receive progress information.
	 * 
	 */
	public interface SavingProgress {

		/**
		 * Inform the callback of the current progress.
		 * It is guaranteed that the value will be an integer between 0 and 100 representing
		 * percent complete.  The SavingProgress object might not be notified the through
		 * setProgress when the save is complete.  When called with the value 100, the saving process
		 * may not be complete, do not use this as an indication of completion.
		 * 
		 * @param progress  int value between 0 and 100 representing percent complete.
		 */
		public void setProgress( int progress );

	}

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
	public final void save(File dest, OpenRocketDocument document, StorageOptions options) throws IOException {
		save( dest, document, options, null );
	}

	/**
	 * Save the document to a file with default StorageOptions and a SavingProgress callback object.
	 * 
	 * @param dest			the destination stream.
	 * @param doc			the document to save.
	 * @param progress      a SavingProgress object used to provide progress information
	 * @throws IOException	in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument doc, SavingProgress progress ) throws IOException {
		save( dest, doc, doc.getDefaultStorageOptions(), progress );
	}

	/**
	 * Save the document to a file with the given StorageOptions and a SavingProgress callback object.
	 * 
	 * @param dest			the destination stream.
	 * @param doc			the document to save.
	 * @param options		the storage options.
	 * @param progress      a SavingProgress object used to provide progress information
	 * @throws IOException	in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument doc, StorageOptions opts, SavingProgress progress ) throws IOException {

		// This method is the core operational method.  It saves the document into a new (hopefully unique)
		// file, then if the save is successful, it will copy the file over the old one.
		
		// Write to a temporary file in the same directory as the specified file.
		File temporaryNewFile = File.createTempFile("ORSave", ".tmp", dest.getParentFile() );
		
		OutputStream s = new BufferedOutputStream( new FileOutputStream(temporaryNewFile));
		
		if ( progress != null ) {
			long estimatedSize = this.estimateFileSize(doc, opts);
			s = new ProgressOutputStream( s, estimatedSize, progress );
		}
		try {
			save(dest.getName(), s, doc, opts);
		} finally {
			s.close();
		}

		// Move the temporary new file over the specified file.
		
		boolean destExists = dest.exists();
		File oldBackupFile = new File( dest.getParentFile(), dest.getName() + "-bak");
		
		if ( destExists ) {
			dest.renameTo(oldBackupFile);
		}
		// since we created the temporary new file in the same directory as the dest file,
		// it is on the same filesystem, so File.renameTo will work just fine.
		boolean success = temporaryNewFile.renameTo(dest);
		
		if ( success ) {
			if ( destExists ) {
				oldBackupFile.delete();
			}
		}
	}
	
	/**
	 * Provide an estimate of the file size when saving the document with the
	 * specified options.  This is used as an indication to the user and when estimating
	 * file save progress.
	 * 
	 * @param doc		the document.
	 * @param options	the save options, compression must be taken into account.
	 * @return			the estimated number of bytes the storage would take.
	 */
	public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
		if ( options.getFileType() == StorageOptions.FileType.ROCKSIM ) {
			return new RocksimSaver().estimateFileSize(doc, options);
		} else {
			return new OpenRocketSaver().estimateFileSize(doc,options);
		}
	}

	private void save(String fileName, OutputStream output, OpenRocketDocument document, StorageOptions options)	throws IOException {

		// For now, we don't save decal inforamtion in ROCKSIM files, so don't do anything
		// which follows.
		// TODO - add support for decals in ROCKSIM files?
		if ( options.getFileType() == FileType.ROCKSIM ) {
			saveInternal(output, document, options);
			output.close();
			return;
		}

		// grab the set of decal images.  We do this up front
		// so we can fail early if some resource is missing.

		// decalNameNormalization maps the current decal file name
		// to the name used in the zip file.
		Map<String,String> decalNameNormalization = new HashMap<String,String>();

		// decals maintains a mapping from decal file name to an input stream.
		Map<String,InputStream> decals = new HashMap<String,InputStream>();
		// try block to close streams held in decals if something goes wrong.
		try {

			// Look for all decals used in the rocket.
			for( RocketComponent c : document.getRocket() ) {
				if ( c.getAppearance() == null ) {
					continue;
				}
				Appearance ap = c.getAppearance();
				if ( ap.getTexture() == null ) {
					continue;
				}

				Decal decal = ap.getTexture();

				String decalName = decal.getImage();

				// If the decal name is already in the decals map, we've already
				// seen it attached to another component.
				if ( decals.containsKey(decalName) ) {
					continue;
				}

				// Use the DecalRegistry to get the input stream.
				InputStream is = document.getDecalRegistry().getDecal(decalName);

				// Add it to the decals map.
				decals.put(decalName, is);

				// Normalize the name:
				File fname = new File(decalName);
				String newName = "decals/" + fname.getName();

				// If the normalized name is already used, it represents a different
				// decal name.  We need to change the name slightly to find one which works.
				if ( decalNameNormalization.values().contains(newName) ) {
					// We'll append integers to the names until we get something which works.
					// so if newName is "decals/foo.jpg", we will try "decals/foo (1).jpg"
					// "decals/foo (2).jpg", etc.
					String newNameTemplate = buildFilenameTemplate(newName);
					int i=1;
					while( true ) {
						newName = MessageFormat.format(newNameTemplate, i);
						if ( ! decalNameNormalization.values().contains(newName) ) {
							break;
						}
						i++;
					}
				}

				decalNameNormalization.put(decalName, newName);

			}
		}
		catch (IOException ex) {
			for ( InputStream is: decals.values() ) {
				try {
					is.close();
				}
				catch ( Throwable t ) {
				}
			}
			throw ex;
		}

		// Now we have to loop through all the components and update their names.
		
		// First we copy the OpenRocketDocument so we can modify the decal file names
		// without changing the ui's copy.  This is so the ui will continue to
		// use the exported decals.
		OpenRocketDocument rocketDocCopy = document.copy();
		for( RocketComponent c : rocketDocCopy.getRocket() ) {

			if ( c.getAppearance() == null ) {
				continue;
			}

			Appearance ap = c.getAppearance();

			if ( ap.getTexture() == null ) {
				continue;
			}

			AppearanceBuilder builder = new AppearanceBuilder(ap);

			builder.setImage( decalNameNormalization.get(ap.getTexture().getImage()));

			c.setAppearance(builder.getAppearance());

		}
		
		Map<String,InputStream> decalMap = new HashMap<String,InputStream>();
		for( Map.Entry<String, InputStream> image : decals.entrySet() ) {

			String newName = decalNameNormalization.get(image.getKey());
			decalMap.put(newName, image.getValue());
		}

		saveAllPartsZipFile(fileName, output, rocketDocCopy, options, decalMap);
	}
	
	public void saveAllPartsZipFile(String fileName, OutputStream output, OpenRocketDocument document, StorageOptions options, Map<String,InputStream> decals) throws IOException {
		
		// Open a zip stream to write to.
		ZipOutputStream zos = new ZipOutputStream(output);
		zos.setLevel(9);
		// big try block to close the zos.
		try {


			ZipEntry mainFile = new ZipEntry(fileName);
			zos.putNextEntry(mainFile);
			saveInternal(zos,document,options);
			zos.closeEntry();

			// Now we write out all the decal images files.

			for( Map.Entry<String, InputStream> image : decals.entrySet() ) {

				String name = image.getKey();
				ZipEntry decal = new ZipEntry(name);
				zos.putNextEntry(decal);

				InputStream is = image.getValue();
				int bytesRead = 0;
				byte[] buffer = new byte[2048];
				while( (bytesRead = is.read(buffer)) > 0  ) {
					zos.write(buffer, 0, bytesRead);
				}
				zos.closeEntry();
			}

			zos.flush();
		} finally {
			zos.close();
		}


	}

	// package scope for testing.
	static String buildFilenameTemplate( String fileName ) {
		// We're going to use MessageTemplate for this.  If we don't have a dot
		// just append "(5)"
		String nameTemplate = fileName + " ({0})";

		// split up the newName.  Look for extension.
		int lastDot = fileName.lastIndexOf('.');
		if ( lastDot > 0 ) {
			String firstPart = fileName.substring(0, lastDot );
			String lastPart = fileName.substring(lastDot);
			nameTemplate = firstPart + " ({0})" + lastPart;
		}

		return nameTemplate;
	}

	private void saveInternal(OutputStream output, OpenRocketDocument document, StorageOptions options)
			throws IOException {

		if ( options.getFileType() == StorageOptions.FileType.ROCKSIM ) {
			new RocksimSaver().save(output, document, options);
		} else {
			new OpenRocketSaver().save(output, document, options);
		}
	}

	private static class ProgressOutputStream extends FilterOutputStream {

		private long estimatedSize;
		private long bytesWritten = 0;
		private SavingProgress progressCallback;

		ProgressOutputStream( OutputStream ostream, long estimatedSize, SavingProgress progressCallback ) {
			super(ostream);
			this.estimatedSize = estimatedSize;
			this.progressCallback = progressCallback;
		}

		@Override
		public void write(int b) throws IOException {
			super.write(b);
			bytesWritten++;
			updateProgress();
		}

		@Override
		public void write(byte[] b) throws IOException {
			super.write(b);
			bytesWritten += b.length;
			updateProgress();
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			super.write(b, off, len);
			bytesWritten += len;
			updateProgress();
		}

		private void updateProgress() {
			if (progressCallback != null) {
				int p = 50;
				if ( estimatedSize > 0 ) {
					p = (int) Math.floor( bytesWritten * 100.0 / estimatedSize );
					p = MathUtil.clamp(p, 0, 100);
				}
				progressCallback.setProgress(p);
			}
		}

	}
}
