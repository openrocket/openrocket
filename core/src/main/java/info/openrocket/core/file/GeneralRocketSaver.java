package info.openrocket.core.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.file.rasaero.export.RASAeroSaver;
import info.openrocket.core.file.rocksim.export.RockSimSaver;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.InsideColorComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.MathUtil;

public class GeneralRocketSaver {
	protected final WarningSet warnings = new WarningSet();
	protected final ErrorSet errors = new ErrorSet();

	/**
	 * Interface which can be implemented by the caller to receive progress
	 * information.
	 * 
	 */
	public interface SavingProgress {

		/**
		 * Inform the callback of the current progress.
		 * It is guaranteed that the value will be an integer between 0 and 100
		 * representing
		 * percent complete. The SavingProgress object might not be notified the through
		 * setProgress when the save is complete. When called with the value 100, the
		 * saving process
		 * may not be complete, do not use this as an indication of completion.
		 * 
		 * @param progress int value between 0 and 100 representing percent complete.
		 */
		public void setProgress(int progress);

	}

	/**
	 * Save the document to the specified file using the default storage options.
	 * 
	 * @param dest     the destination file.
	 * @param document the document to save.
	 * @throws IOException in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument document) throws IOException, DecalNotFoundException {
		save(dest, document, document.getDefaultStorageOptions());
	}

	/**
	 * Save the document to the specified file using the given storage options.
	 * 
	 * @param dest     the destination file.
	 * @param document the document to save.
	 * @param options  the storage options.
	 * @throws IOException in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument document, StorageOptions options)
			throws IOException, DecalNotFoundException {
		save(dest, document, options, null);
	}

	/**
	 * Save the document to a file with default StorageOptions and a SavingProgress
	 * callback object.
	 * 
	 * @param dest     the destination stream.
	 * @param doc      the document to save.
	 * @param progress a SavingProgress object used to provide progress information
	 * @throws IOException in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument doc, SavingProgress progress)
			throws IOException, DecalNotFoundException {
		save(dest, doc, doc.getDefaultStorageOptions(), progress);
	}

	/**
	 * Save the document to a file with the given StorageOptions and a
	 * SavingProgress callback object.
	 * 
	 * @param dest     the destination stream.
	 * @param doc      the document to save.
	 * @param opts     the storage options.
	 * @param progress a SavingProgress object used to provide progress information
	 * @throws IOException in case of an I/O error.
	 */
	public final void save(File dest, OpenRocketDocument doc, StorageOptions opts, SavingProgress progress)
			throws IOException, DecalNotFoundException {

		// This method is the core operational method. It saves the document into a new
		// (hopefully unique)
		// file, then if the save is successful, it will copy the file over the old one.

		// Write to a temporary file in the same directory as the specified file.
		File temporaryNewFile = File.createTempFile("ORSave", ".tmp", dest.getParentFile());

		OutputStream s = new BufferedOutputStream(new FileOutputStream(temporaryNewFile));

		if (progress != null) {
			long estimatedSize = this.estimateFileSize(doc, opts);
			s = new ProgressOutputStream(s, estimatedSize, progress);
		}
		try {
			save(dest.getName(), s, doc, opts);
		} catch (DecalNotFoundException decex) {
			temporaryNewFile.delete();
			throw decex;
		} finally {
			s.close();
		}

		// Move the temporary new file over the specified file.

		boolean destExists = dest.exists();
		File oldBackupFile = new File(dest.getParentFile(), dest.getName() + "-bak");

		if (destExists) {
			dest.renameTo(oldBackupFile);
		}
		// since we created the temporary new file in the same directory as the dest
		// file,
		// it is on the same filesystem, so File.renameTo will work just fine.
		boolean success = temporaryNewFile.renameTo(dest);

		if (success) {
			dest.setLastModified(System.currentTimeMillis());
			if (destExists) {
				oldBackupFile.delete();
			}
		} else {
			temporaryNewFile.delete();
			if (destExists) {
				oldBackupFile.renameTo(dest);
			}
			throw new IOException("Unable to move temporary file to destination");
		}
	}

	/**
	 * Provide an estimate of the file size when saving the document with the
	 * specified options. This is used as an indication to the user and when
	 * estimating
	 * file save progress.
	 * 
	 * @param doc     the document.
	 * @param options the save options, compression must be taken into account.
	 * @return the estimated number of bytes the storage would take.
	 */
	public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
		if (options.getFileType() == FileType.ROCKSIM) {
			return new RockSimSaver().estimateFileSize(doc, options);
		} else if (options.getFileType() == FileType.RASAERO) {
			return new RASAeroSaver().estimateFileSize(doc, options);
		} else {
			return new OpenRocketSaver().estimateFileSize(doc, options);
		}
	}

	private void save(String fileName, OutputStream output, OpenRocketDocument document, StorageOptions options)
			throws IOException, DecalNotFoundException {

		// For now, we don't save decal information in ROCKSIM/RASAero files, so don't
		// do anything
		// which follows.
		// TODO - add support for decals in ROCKSIM files?
		if (options.getFileType() == FileType.ROCKSIM || options.getFileType() == FileType.RASAERO) {
			saveInternal(output, document, options);
			output.close();
			return;
		}

		Set<DecalImage> usedDecals = new TreeSet<>();

		// Look for all decals used in the rocket.
		for (RocketComponent c : document.getRocket()) {
			Appearance ap = c.getAppearance();
			Appearance ap_in = null;
			if (c instanceof InsideColorComponent)
				ap_in = ((InsideColorComponent) c).getInsideColorComponentHandler().getInsideAppearance();

			if ((ap == null) && (ap_in == null))
				continue;
			if (ap != null) {
				Decal decal = ap.getTexture();
				if (decal != null)
					usedDecals.add(decal.getImage());
			}
			if (ap_in != null) {
				Decal decal = ap_in.getTexture();
				if (decal != null)
					usedDecals.add(decal.getImage());
			}
		}

		saveAllPartsZipFile(output, document, options, usedDecals);
	}

	public void saveAllPartsZipFile(OutputStream output, OpenRocketDocument document, StorageOptions options,
			Set<DecalImage> decals) throws IOException, DecalNotFoundException {

		// Open a zip stream to write to.
		ZipOutputStream zos = new ZipOutputStream(output);
		// big try block to close the zos.
		try (zos) {
			zos.setLevel(9);

			ZipEntry mainFile = new ZipEntry("rocket.ork");
			zos.putNextEntry(mainFile);
			saveInternal(zos, document, options);
			zos.closeEntry();

			// Now we write out all the decal images files.
			for (DecalImage image : decals) {
				if (image.isIgnored()) {
					image.setIgnored(false);
					continue;
				}

				String name = image.getName();
				ZipEntry decal = new ZipEntry(name);
				zos.putNextEntry(decal);

				InputStream is = image.getBytes();
				int bytesRead = 0;
				byte[] buffer = new byte[2048];
				while ((bytesRead = is.read(buffer)) > 0) {
					zos.write(buffer, 0, bytesRead);
				}
				zos.closeEntry();
			}

			zos.flush();
		}

	}

	// package scope for testing.

	private void saveInternal(OutputStream output, OpenRocketDocument document, StorageOptions options)
			throws IOException {
		warnings.clear();
		errors.clear();

		if (options.getFileType() == FileType.ROCKSIM) {
			new RockSimSaver().save(output, document, options, warnings, errors);
		} else if (options.getFileType() == FileType.RASAERO) {
			new RASAeroSaver().save(output, document, options, warnings, errors);
		} else {
			new OpenRocketSaver().save(output, document, options, warnings, errors);
		}
	}

	/**
	 * Return a list of warnings generated during the saving process.
	 * 
	 * @return a list of warnings generated during the saving process
	 */
	public WarningSet getWarnings() {
		return warnings;
	}

	/**
	 * Return a list of errors generated during the saving process.
	 * 
	 * @return a list of errors generated during the saving process
	 */
	public ErrorSet getErrors() {
		return errors;
	}

	private static class ProgressOutputStream extends FilterOutputStream {

		private final long estimatedSize;
		private long bytesWritten = 0;
		private final SavingProgress progressCallback;

		ProgressOutputStream(OutputStream ostream, long estimatedSize, SavingProgress progressCallback) {
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
				if (estimatedSize > 0) {
					p = (int) Math.floor(bytesWritten * 100.0 / estimatedSize);
					p = MathUtil.clamp(p, 0, 100);
				}
				progressCallback.setProgress(p);
			}
		}

	}
}
