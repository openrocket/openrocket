package info.openrocket.swing.gui.figure3d.photo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Validates a user-selected image before passing its path to the renderer. */
final class CustomPhotoBackground {
	private static final int MAX_DIMENSION = 4096;
	private static final long MAX_FILE_BYTES = 32L * 1024 * 1024;

	private CustomPhotoBackground() {
	}

	static File validate(File file) throws IOException {
		File absolute = file.getAbsoluteFile();
		if (!absolute.isFile() || absolute.length() > MAX_FILE_BYTES) {
			throw new IOException("Missing or oversized background image");
		}
		try (ImageInputStream input = ImageIO.createImageInputStream(absolute)) {
			if (input == null) {
				throw new IOException("Cannot open background image");
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IOException("Unsupported background image");
			}
			ImageReader reader = readers.next();
			try {
				String format = reader.getFormatName();
				if (!"png".equalsIgnoreCase(format) && !"jpeg".equalsIgnoreCase(format)) {
					throw new IOException("Only PNG and JPEG images are supported");
				}
				reader.setInput(input, true, true);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION) {
					throw new IOException("Background image dimensions exceed the supported limit");
				}
				// Decode once to reject unreadable images before changing the selection.
				if (reader.read(0) == null) {
					throw new IOException("Cannot decode background image");
				}
			} finally {
				reader.dispose();
			}
		}
		return absolute;
	}
}
