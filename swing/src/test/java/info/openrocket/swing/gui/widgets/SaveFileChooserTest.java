package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests validation of filesystem-backed save destinations.
 */
public class SaveFileChooserTest {
	@TempDir
	Path tempDirectory;

	/**
	 * A normal directory accepted by the platform file-system view is a valid destination.
	 */
	@Test
	public void testAcceptsFileSystemDirectory() {
		File directory = tempDirectory.toFile();
		FileSystemView fileSystemView = FileSystemView.getFileSystemView();

		assertTrue(SaveFileChooser.isFileSystemDirectory(directory, fileSystemView));
	}

	/**
	 * Virtual Windows shell nodes, such as "This PC", must not be used as save directories.
	 */
	@Test
	public void testRejectsVirtualShellDirectory() {
		File virtualDirectory = tempDirectory.toFile();
		FileSystemView fileSystemView = mock(FileSystemView.class);
		when(fileSystemView.isFileSystem(virtualDirectory)).thenReturn(false);

		assertFalse(SaveFileChooser.isFileSystemDirectory(virtualDirectory, fileSystemView));
	}

	/**
	 * A filesystem path must also exist as a directory before it can contain a saved file.
	 */
	@Test
	public void testRejectsMissingDirectory() {
		File directory = tempDirectory.resolve("missing").toFile();
		FileSystemView fileSystemView = FileSystemView.getFileSystemView();

		assertFalse(SaveFileChooser.isFileSystemDirectory(directory, fileSystemView));
	}

	/**
	 * Approval must remain inside the chooser when the current location is a virtual shell node.
	 */
	@Test
	public void testVirtualShellDirectoryPreventsApproval() {
		File directory = tempDirectory.toFile();
		FileSystemView fileSystemView = mock(FileSystemView.class);
		when(fileSystemView.isFileSystem(directory)).thenReturn(false);
		TestSaveFileChooser chooser = new TestSaveFileChooser();
		chooser.setCurrentDirectory(directory);
		chooser.setSelectedFile(new File(directory, "rocket.obj"));
		chooser.setTestFileSystemView(fileSystemView);
		AtomicBoolean approved = new AtomicBoolean(false);
		chooser.addActionListener(event -> approved.set(
				JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())));

		chooser.approveSelection();

		assertTrue(chooser.isInvalidLocationWarningShown());
		assertFalse(approved.get());
	}

	/**
	 * Approval must proceed normally when the current location is a filesystem directory.
	 */
	@Test
	public void testFileSystemDirectoryAllowsApproval() {
		File directory = tempDirectory.toFile();
		TestSaveFileChooser chooser = new TestSaveFileChooser();
		chooser.setCurrentDirectory(directory);
		chooser.setSelectedFile(new File(directory, "rocket.obj"));
		AtomicBoolean approved = new AtomicBoolean(false);
		chooser.addActionListener(event -> approved.set(
				JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())));

		chooser.approveSelection();

		assertFalse(chooser.isInvalidLocationWarningShown());
		assertTrue(approved.get());
	}

	/**
	 * Test chooser that records warnings without displaying a modal dialog.
	 */
	private static final class TestSaveFileChooser extends SaveFileChooser {
		private FileSystemView testFileSystemView;
		private boolean invalidLocationWarningShown;

		@Override
		public FileSystemView getFileSystemView() {
			return testFileSystemView != null ? testFileSystemView : super.getFileSystemView();
		}

		@Override
		protected void showInvalidSaveLocationWarning() {
			invalidLocationWarningShown = true;
		}

		private void setTestFileSystemView(FileSystemView fileSystemView) {
			testFileSystemView = fileSystemView;
		}

		private boolean isInvalidLocationWarningShown() {
			return invalidLocationWarningShown;
		}
	}
}
