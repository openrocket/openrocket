package info.openrocket.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public abstract class FileUtils {
	private static final char[] ILLEGAL_CHARS = new char[] { '/', '\\', ':', '*', '?', '"', '<', '>', '|' };

	public static void copy(InputStream is, OutputStream os) throws IOException {
		if (!(os instanceof BufferedOutputStream)) {
			os = new BufferedOutputStream(os);
		}

		if (!(is instanceof BufferedInputStream)) {
			is = new BufferedInputStream(is);
		}

		byte[] buffer = new byte[1024];
		int bytesRead = 0;

		while ((bytesRead = is.read(buffer)) > 0) {
			os.write(buffer, 0, bytesRead);
		}
		os.flush();
	}

	public static byte[] readBytes(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		copy(is, bos);

		return bos.toByteArray();

	}

	/**
	 * Remove the extension from a file name, properly handling paths and edge cases.
	 *
	 * @param fileName the file name or path
	 * @return the file name without extension
	 */
	public static String removeExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return fileName;
		}

		// First get just the filename portion if this is a path
		String name = Paths.get(fileName).getFileName().toString();

		// Handle special case of files starting with a dot
		if (name.startsWith(".")) {
			if (name.length() == 1) { // Just a dot
				return name;
			}
			// Check if there's another dot in the name after the first character
			int secondDot = name.indexOf('.', 1);
			if (secondDot == -1) { // No other dots, it's a dotfile like .gitignore
				return "";
			}
			// Has another extension after the dot prefix
			return name.substring(0, secondDot);
		}

		// Find last occurrence of dot in the filename
		int lastDot = name.lastIndexOf('.');

		if (lastDot == -1) { // No extension found
			return name;
		}

		return name.substring(0, lastDot);
	}

	/**
	 * Get the file name from a path.
	 * 
	 * @param pathString the path (e.g. "my/file/path.txt")
	 * @return the file name (e.g. "path.txt")
	 */
	public static String getFileNameFromPath(String pathString) {
		return Paths.get(pathString).getFileName().toString();
	}

	/**
	 * Returns an illegal character if one is found in the filename, otherwise
	 * returns null.
	 * 
	 * @param filename The filename to check
	 * @return The illegal character, or null if none is found
	 */
	public static Character getIllegalFilenameChar(String filename) {
		if (filename == null || filename.isEmpty()) {
			return null;
		}
		for (char c : ILLEGAL_CHARS) {
			if (filename.indexOf(c) >= 0) {
				return c;
			}
		}
		return null;
	}

}
