package net.sf.openrocket.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.file.rocksim.export.RocksimSaver;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class GeneralRocketSaver {

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
	public void save(File dest, OpenRocketDocument document, StorageOptions options) throws IOException {
		OutputStream s = new BufferedOutputStream(new FileOutputStream(dest));
		try {
			save(dest.getName(), s, document, options);
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
	public final void save(String fileName, OutputStream dest, OpenRocketDocument doc) throws IOException {
		save(fileName, dest, doc, doc.getDefaultStorageOptions());
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

	public void save(String fileName, OutputStream output, OpenRocketDocument document, StorageOptions options)	throws IOException {

		// If we don't include decals, just write the simple file.
		if (!options.isIncludeDecals()) {
			saveInternal(output,document,options);
			return;
		}

		// We're saving decals, so the result will be a zip file.  There's no
		// need to gzip the rocket model file in the archive.
		options.setCompressionEnabled(false);

		// Open a zip stream to write to.
		ZipOutputStream zos = new ZipOutputStream(output);
		zos.setLevel(9);

		/* if we want a directory ...
		String path = fileName;
		int dotlocation = fileName.lastIndexOf('.');
		if ( dotlocation > 1 ) {
			path = fileName.substring(dotlocation);
		}
		 */

		// big try block to close the zos.
		try {
			
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
			for( RocketComponent c : document.getRocket() ) {

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

			// Fixme - should probably be the same name?  Should we put everything in a directory?
			ZipEntry mainFile = new ZipEntry("rocket.ork");
			zos.putNextEntry(mainFile);
			saveInternal(zos,document,options);
			zos.closeEntry();

			// Now we write out all the decal images files.

			for( Map.Entry<String, InputStream> image : decals.entrySet() ) {

				String newName = decalNameNormalization.get(image.getKey());
				ZipEntry decal = new ZipEntry(newName);
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
}
