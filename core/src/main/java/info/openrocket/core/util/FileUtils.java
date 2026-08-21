package info.openrocket.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
		return fileName == null || fileName.isEmpty() ? fileName :
				fileName.replaceFirst("[.][^.]+$", "");
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

	public static File makeDirectoryIfNotExists(File dir) throws IOException {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Could not create directory: " + dir.getAbsolutePath());
			}
		} else if (!dir.isDirectory()) {
			throw new IOException("File exists and is not a directory: " + dir.getAbsolutePath());
		} else if (!dir.canRead()) {
			throw new IOException("Directory is not readable: " + dir.getAbsolutePath());
		}
		return dir;
	}

	/**
	 * Replace targetFile with sourceFile atomically if possible.
	 * @param sourceFile the file to move
	 * @param targetFile the target file to replace
	 * @throws IOException if an I/O error occurs
	 */
	public static void replaceFile(File sourceFile, File targetFile) throws IOException {
		if (sourceFile == null || !sourceFile.isFile()) {
			throw new IOException("Temporary file missing: " + sourceFile);
		}
		try {
			Files.move(sourceFile.toPath(), targetFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException ignored) {
			Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
