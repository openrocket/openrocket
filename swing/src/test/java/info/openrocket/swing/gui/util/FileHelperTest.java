package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;
import com.formdev.flatlaf.util.SystemFileChooser.PatternFilter;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests the native file chooser helpers in {@link FileHelper}.
 */
public class FileHelperTest extends BaseTestCase {
	@TempDir
	Path tempDirectory;

	@Test
	public void testSingleExtensionsCreateExtensionFilter() {
		FileFilter filter = FileHelper.createFilter("CSV", "csv", "txt");

		FileNameExtensionFilter extensionFilter = assertInstanceOf(FileNameExtensionFilter.class, filter);
		assertArrayEquals(new String[] { "csv", "txt" }, extensionFilter.getExtensions());
	}

	/**
	 * Multipart extensions need a pattern filter, which macOS does not support.
	 */
	@Test
	public void testMultipartExtensions() {
		FileFilter filter = FileHelper.createFilter("OpenRocket", "ork", "ork.gz");

		if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
			FileNameExtensionFilter extensionFilter = assertInstanceOf(FileNameExtensionFilter.class, filter);
			assertArrayEquals(new String[] { "ork" }, extensionFilter.getExtensions());
		} else {
			PatternFilter patternFilter = assertInstanceOf(PatternFilter.class, filter);
			assertArrayEquals(new String[] { "*.ork", "*.ork.gz" }, patternFilter.getPatterns());
		}
	}

	/**
	 * The save dialog already asked about overwriting the selected file, so no second confirmation is needed.
	 * A confirmation dialog would block until it is answered, which the timeout turns into a failure.
	 */
	@Test
	public void testSelectedExistingFileIsNotConfirmedAgain() throws IOException {
		File file = Files.createFile(tempDirectory.resolve("rocket.csv")).toFile();

		assertTimeoutPreemptively(Duration.ofSeconds(10),
				() -> assertTrue(FileHelper.confirmWrite(file, new File(file.getPath()), null)));
	}

	@Test
	public void testNewFileDoesNotNeedConfirmation() {
		File selected = new File(tempDirectory.toFile(), "rocket");
		File file = FileHelper.forceExtension(selected, "csv");

		assertTrue(FileHelper.confirmWrite(file, selected, null));
	}
}
