package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
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
		FileSystemView fileSystemView = mock(FileSystemView.class);
		when(fileSystemView.isFileSystem(directory)).thenReturn(true);

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
		FileSystemView fileSystemView = mock(FileSystemView.class);
		when(fileSystemView.isFileSystem(directory)).thenReturn(true);

		assertFalse(SaveFileChooser.isFileSystemDirectory(directory, fileSystemView));
	}

	/**
	 * Approval must remain inside the chooser when the current location is a virtual shell node.
	 */
	@Test
	public void testVirtualShellDirectoryPreventsApproval() throws Exception {
		File directory = tempDirectory.toFile();
		FileSystemView fileSystemView = mock(FileSystemView.class);

		SwingUtilities.invokeAndWait(() -> {
			TestSaveFileChooser chooser = new TestSaveFileChooser();
			try {
				chooser.setCurrentDirectory(directory);
				chooser.setSelectedFile(new File(directory, "rocket.obj"));
				// Windows may normalize the selected directory to a different File representation.
				when(fileSystemView.isFileSystem(chooser.getCurrentDirectory())).thenReturn(false);
				chooser.setTestFileSystemView(fileSystemView);
				AtomicBoolean approved = new AtomicBoolean(false);
				chooser.addActionListener(event -> approved.set(
						JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())));

				chooser.approveSelection();

				assertTrue(chooser.isInvalidLocationWarningShown());
				assertFalse(approved.get());
			} finally {
				// Uninstall the UI to stop any platform directory-model work started by JFileChooser.
				chooser.releaseUiResources();
			}
		});
	}

	/**
	 * Approval must proceed normally when the current location is a filesystem directory.
	 */
	@Test
	public void testFileSystemDirectoryAllowsApproval() throws Exception {
		File directory = tempDirectory.toFile();
		FileSystemView fileSystemView = mock(FileSystemView.class);

		SwingUtilities.invokeAndWait(() -> {
			TestSaveFileChooser chooser = new TestSaveFileChooser();
			try {
				chooser.setCurrentDirectory(directory);
				chooser.setSelectedFile(new File(directory, "rocket.obj"));
				// Stub the platform-normalized directory that approveSelection actually validates.
				when(fileSystemView.isFileSystem(chooser.getCurrentDirectory())).thenReturn(true);
				chooser.setTestFileSystemView(fileSystemView);
				AtomicBoolean approved = new AtomicBoolean(false);
				chooser.addActionListener(event -> approved.set(
						JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())));

				chooser.approveSelection();

				assertFalse(chooser.isInvalidLocationWarningShown());
				assertTrue(approved.get());
			} finally {
				// Uninstall the UI to stop any platform directory-model work started by JFileChooser.
				chooser.releaseUiResources();
			}
		});
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

		/**
		 * Uninstalls the test chooser UI so its asynchronous directory model cannot outlive the test.
		 */
		private void releaseUiResources() {
			setUI(null);
		}
	}
}
