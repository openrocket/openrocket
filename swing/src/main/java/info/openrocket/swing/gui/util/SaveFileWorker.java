package info.openrocket.swing.gui.util;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.file.GeneralRocketSaver.SavingProgress;

public class SaveFileWorker extends SwingWorker<Void, Void> {
	private static final Logger log = LoggerFactory.getLogger(SaveFileWorker.class);

	private final OpenRocketDocument document;
	private final File file;
	private final GeneralRocketSaver saver;
	private final Supplier<byte[]> previewRenderer;
	private final Supplier<byte[]> thumbnailRenderer;

	public SaveFileWorker(OpenRocketDocument document, File file, GeneralRocketSaver saver) {
		this(document, file, saver, null, null);
	}

	public SaveFileWorker(OpenRocketDocument document, File file, GeneralRocketSaver saver,
			Supplier<byte[]> previewRenderer, Supplier<byte[]> thumbnailRenderer) {
		this.document = document;
		this.file = file;
		this.saver = saver;
		this.previewRenderer = previewRenderer;
		this.thumbnailRenderer = thumbnailRenderer;
	}

	@Override
	protected Void doInBackground() throws Exception {
		StorageOptions options = document.getDefaultStorageOptions();

		// Render the file preview and thumbnail off the EDT, at the same time: their 3D renders take turns on the 3D
		// view's render thread, but processing and encoding the images overlaps. A design that cannot be rendered is
		// still saved, just without the image.
		CompletableFuture<byte[]> preview = previewRenderer == null ? null
				: CompletableFuture.supplyAsync(previewRenderer);
		if (thumbnailRenderer != null) {
			try {
				options.setThumbnailImage(thumbnailRenderer.get());
			} catch (RuntimeException e) {
				log.warn("Could not render the file thumbnail", e);
				options.clearThumbnailImage();
			}
		}
		if (preview != null) {
			try {
				options.setPreviewImage(preview.join());
			} catch (CompletionException e) {
				log.warn("Could not render the file preview", e.getCause());
				options.clearPreviewImage();
			}
		}

		// Create the ProgressOutputStream that provides progress estimates
		SavingProgress progressCallback = new GeneralRocketSaver.SavingProgress() {

			@Override
			public void setProgress(int progress) {
				SaveFileWorker.this.setProgress(progress);

			}
		};

		saver.save(file, document, progressCallback);
		return null;
	}

}
