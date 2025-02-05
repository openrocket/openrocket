package info.openrocket.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {
	@TempDir
	Path tempDir;

	private File testFile;
	private static final String TEST_CONTENT = "Hello, World!";

	@BeforeEach
	void setUp() throws IOException {
		testFile = tempDir.resolve("test.txt").toFile();
		try (FileWriter writer = new FileWriter(testFile)) {
			writer.write(TEST_CONTENT);
		}
	}

	@Test
	void testCopy_WithBufferedStreams() throws IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(testFile));
			 ByteArrayOutputStream os = new ByteArrayOutputStream()) {

			FileUtils.copy(is, os);

			assertEquals(TEST_CONTENT, os.toString());
		}
	}

	@Test
	void testCopy_WithUnbufferedStreams() throws IOException {
		try (InputStream is = new FileInputStream(testFile);
			 ByteArrayOutputStream os = new ByteArrayOutputStream()) {

			FileUtils.copy(is, os);

			assertEquals(TEST_CONTENT, os.toString());
		}
	}

	@Test
	void testCopy_WithEmptyFile() throws IOException {
		File emptyFile = tempDir.resolve("empty.txt").toFile();
		// Create the empty file
		assertTrue(emptyFile.createNewFile());

		try (InputStream is = new FileInputStream(emptyFile);
			 ByteArrayOutputStream os = new ByteArrayOutputStream()) {

			FileUtils.copy(is, os);

			assertEquals(0, os.size());
		}
	}

	@Test
	void testReadBytes() throws IOException {
		try (InputStream is = new FileInputStream(testFile)) {
			byte[] bytes = FileUtils.readBytes(is);

			assertEquals(TEST_CONTENT, new String(bytes));
		}
	}

	@Test
	void testReadBytes_EmptyFile() throws IOException {
		File emptyFile = tempDir.resolve("empty.txt").toFile();
		// Create the empty file
		assertTrue(emptyFile.createNewFile());

		try (InputStream is = new FileInputStream(emptyFile)) {
			byte[] bytes = FileUtils.readBytes(is);

			assertEquals(0, bytes.length);
		}
	}

	@Test
	void testRemoveExtension_NormalFile() {
		assertEquals("filename", FileUtils.removeExtension("filename.txt"));
	}

	@Test
	void testRemoveExtension_NoExtension() {
		assertEquals("filename", FileUtils.removeExtension("filename"));
	}

	@Test
	void testRemoveExtension_DotFile() {
		assertEquals("", FileUtils.removeExtension(".gitignore"));
		// Test periods in path
		assertEquals("/path/with.dots/test", FileUtils.removeExtension("/path/with.dots/test.txt"));
		assertEquals("C:/my.folder/sub.folder/test", FileUtils.removeExtension("C:/my.folder/sub.folder/test.txt"));
		assertEquals("C:/my.folder/sub.folder/test.1.2", FileUtils.removeExtension("C:/my.folder/sub.folder/test.1.2.txt"));
		assertEquals("folder.name/test", FileUtils.removeExtension("folder.name/test.txt"));
	}

	@Test
	void testGetFileNameFromPath() {
		// Test absolute paths
		assertEquals("file.txt", FileUtils.getFileNameFromPath("/path/to/file.txt"));
		assertEquals("document.pdf", FileUtils.getFileNameFromPath("C:/Documents/document.pdf"));

		// Test relative paths
		assertEquals("image.jpg", FileUtils.getFileNameFromPath("../folder/image.jpg"));
		assertEquals("script.sh", FileUtils.getFileNameFromPath("./script.sh"));

		// Test just filename
		assertEquals("simple.txt", FileUtils.getFileNameFromPath("simple.txt"));

		// Test paths with spaces and special characters
		assertEquals("my file.txt", FileUtils.getFileNameFromPath("/path/to/my file.txt"));
		assertEquals("file with spaces.doc", FileUtils.getFileNameFromPath("folder/file with spaces.doc"));
	}

	@Test
	void testGetIllegalFilenameChar_ValidFilename() {
		assertNull(FileUtils.getIllegalFilenameChar("valid-filename.txt"));
	}

	@Test
	void testGetIllegalFilenameChar_IllegalChars() {
		assertEquals('/', FileUtils.getIllegalFilenameChar("illegal/filename"));
		assertEquals('\\', FileUtils.getIllegalFilenameChar("illegal\\filename"));
		assertEquals(':', FileUtils.getIllegalFilenameChar("illegal:filename"));
		assertEquals('*', FileUtils.getIllegalFilenameChar("illegal*filename"));
		assertEquals('?', FileUtils.getIllegalFilenameChar("illegal?filename"));
		assertEquals('"', FileUtils.getIllegalFilenameChar("illegal\"filename"));
		assertEquals('<', FileUtils.getIllegalFilenameChar("illegal<filename"));
		assertEquals('>', FileUtils.getIllegalFilenameChar("illegal>filename"));
		assertEquals('|', FileUtils.getIllegalFilenameChar("illegal|filename"));
	}

	@Test
	void testGetIllegalFilenameChar_EmptyString() {
		assertNull(FileUtils.getIllegalFilenameChar(""));
	}

	@Test
	void testGetIllegalFilenameChar_NullInput() {
		assertNull(FileUtils.getIllegalFilenameChar(null));
	}

	@Test
	void testCopy_WithClosedInputStream() {
		assertThrows(IOException.class, () -> {
			InputStream is = new FileInputStream(testFile);
			is.close();

			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				FileUtils.copy(is, os);
			}
		});
	}

	@Test
	void testCopy_WithClosedOutputStream() {
		File outputFile = tempDir.resolve("output.txt").toFile();
		assertThrows(IOException.class, () -> {
			try (InputStream is = new FileInputStream(testFile)) {
				FileOutputStream os = new FileOutputStream(outputFile);
				os.close();

				FileUtils.copy(is, os);
			}
		});
	}
}
