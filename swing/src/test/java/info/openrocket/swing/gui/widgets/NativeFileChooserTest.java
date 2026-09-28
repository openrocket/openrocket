package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;
import com.formdev.flatlaf.util.SystemFileChooser.PatternFilter;

import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests the preparation done before the native file dialog is shown.
 */
public class NativeFileChooserTest extends BaseTestCase {
	@TempDir
	Path tempDirectory;

	/**
	 * Cancelling the options dialog must cancel the file chooser without opening the native dialog.
	 */
	@Test
	public void testCancelledOptionsCancelFileChooser() {
		TestNativeFileChooser chooser = new TestNativeFileChooser(false);
		chooser.setOptionsPanel(new JPanel());

		assertEquals(SystemFileChooser.CANCEL_OPTION, chooser.showSaveDialog(null));
		assertEquals(1, chooser.optionsDialogCount);
	}

	/**
	 * Without an options panel, no options dialog is shown.
	 */
	@Test
	public void testNoOptionsDialogWithoutOptionsPanel() {
		TestNativeFileChooser chooser = new TestNativeFileChooser(false);

		assertTrue(chooser.prepareDialog(null, SystemFileChooser.OPEN_DIALOG));
		assertEquals(0, chooser.optionsDialogCount);
	}

	/**
	 * A selected file without a folder is placed in the current directory, which the native dialogs would otherwise lose.
	 */
	@Test
	public void testRelativeSelectedFileIsResolvedAgainstCurrentDirectory() {
		File directory = tempDirectory.toFile();
		NativeFileChooser chooser = new NativeFileChooser();
		chooser.setCurrentDirectory(directory);
		chooser.setSelectedFile(new File("rocket.csv"));

		assertTrue(chooser.prepareDialog(null, SystemFileChooser.SAVE_DIALOG));

		assertEquals(new File(directory, "rocket.csv"), chooser.getSelectedFile());
	}

	/**
	 * Save dialogs get the extension of the selected filter as default extension.
	 */
	@Test
	public void testSaveDialogUsesFilterExtensionAsDefault() {
		NativeFileChooser chooser = new NativeFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv", "txt"));

		assertTrue(chooser.prepareDialog(null, SystemFileChooser.SAVE_DIALOG));

		assertEquals("csv", chooser.getPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_EXTENSION));
	}

	/**
	 * Open dialogs do not append extensions.
	 */
	@Test
	public void testOpenDialogHasNoDefaultExtension() {
		NativeFileChooser chooser = new NativeFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));

		assertTrue(chooser.prepareDialog(null, SystemFileChooser.OPEN_DIALOG));

		assertNull(chooser.getPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_EXTENSION));
	}

	/**
	 * An explicitly set default extension is kept.
	 */
	@Test
	public void testExplicitDefaultExtensionIsKept() {
		NativeFileChooser chooser = new NativeFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
		chooser.putPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_EXTENSION, "txt");

		assertTrue(chooser.prepareDialog(null, SystemFileChooser.SAVE_DIALOG));

		assertEquals("txt", chooser.getPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_EXTENSION));
	}

	@Test
	public void testDefaultExtensionOfPatternFilter() {
		assertEquals("ork", NativeFileChooser.getDefaultExtension(new PatternFilter("OpenRocket", "*.ork", "*.ork.gz")));
		assertNull(NativeFileChooser.getDefaultExtension(new PatternFilter("Copies", "*_copy.*")));
		assertNull(NativeFileChooser.getDefaultExtension(new PatternFilter("Named", "rocket.ork")));
	}

	@Test
	public void testDefaultExtensionOfAcceptAllFilter() {
		NativeFileChooser chooser = new NativeFileChooser();
		assertNull(NativeFileChooser.getDefaultExtension(chooser.getAcceptAllFileFilter()));
		assertNull(NativeFileChooser.getDefaultExtension(null));
	}

	/**
	 * Test chooser that answers the options dialog without displaying it.
	 */
	private static final class TestNativeFileChooser extends NativeFileChooser {
		private final boolean optionsResult;
		private int optionsDialogCount;

		private TestNativeFileChooser(boolean optionsResult) {
			this.optionsResult = optionsResult;
		}

		@Override
		protected boolean showOptionsDialog(Component parent) {
			optionsDialogCount++;
			return optionsResult;
		}
	}
}
