package net.sf.openrocket.file;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;


public abstract class RocketSaver {
	
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
	public abstract void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options) throws IOException;
	
	/**
	 * Provide an estimate of the file size when saving the document with the
	 * specified options.  This is used as an indication to the user and when estimating
	 * file save progress.
	 * 
	 * @param doc		the document.
	 * @param options	the save options, compression must be taken into account.
	 * @return			the estimated number of bytes the storage would take.
	 */
	public abstract long estimateFileSize(OpenRocketDocument doc, StorageOptions options);
}
