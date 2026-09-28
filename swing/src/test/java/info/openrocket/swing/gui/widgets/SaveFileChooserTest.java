package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.util.SystemFileChooser;

import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests the filename validation and target configuration of the save file chooser.
 */
public class SaveFileChooserTest extends BaseTestCase {
	@TempDir
	Path tempDirectory;

	/**
	 * A filename with a character that is illegal on some platform keeps the native dialog open with a warning.
	 */
	@Test
	public void testIllegalFilenameKeepsDialogOpen() {
		SaveFileChooser chooser = new SaveFileChooser();
		RecordingApproveContext context = new RecordingApproveContext();

		int result = chooser.approveSelection(new File[] { new File(tempDirectory.toFile(), "rocket?.ork") }, context);

		assertEquals(SystemFileChooser.CANCEL_OPTION, result);
		assertEquals(JOptionPane.WARNING_MESSAGE, context.messageType);
	}

	/**
	 * A legal filename closes the native dialog without a warning.
	 */
	@Test
	public void testLegalFilenameIsApproved() {
		SaveFileChooser chooser = new SaveFileChooser();
		RecordingApproveContext context = new RecordingApproveContext();

		int result = chooser.approveSelection(new File[] { new File(tempDirectory.toFile(), "rocket.ork") }, context);

		assertEquals(SystemFileChooser.APPROVE_OPTION, result);
		assertNull(context.messageType);
	}

	/**
	 * Exporting a single target saves a file named after that target.
	 */
	@Test
	public void testSingleTargetSavesFile() {
		File directory = tempDirectory.toFile();
		SaveFileChooser chooser = new SaveFileChooser();

		SaveFileChooser.SelectionMode mode = chooser.configureForTargets(List.of("decals/decal.png"), directory);

		assertEquals(SaveFileChooser.SelectionMode.SINGLE_FILE, mode);
		assertEquals(SystemFileChooser.SAVE_DIALOG, chooser.getDialogType());
		assertEquals(SystemFileChooser.FILES_ONLY, chooser.getFileSelectionMode());
		assertEquals(new File(directory, "decal.png"), chooser.getSelectedFile());
	}

	/**
	 * Exporting multiple targets selects a folder, which is not checked for illegal filename characters.
	 */
	@Test
	public void testMultipleTargetsSelectDirectory() {
		// java.io.File accepts the name on Windows, where Path.resolve rejects the illegal character
		File directory = new File(tempDirectory.toFile(), "export?");
		SaveFileChooser chooser = new SaveFileChooser();
		RecordingApproveContext context = new RecordingApproveContext();

		SaveFileChooser.SelectionMode mode = chooser.configureForTargets(List.of("a.png", "b.png"), tempDirectory.toFile());

		assertEquals(SaveFileChooser.SelectionMode.DIRECTORY, mode);
		assertEquals(SystemFileChooser.DIRECTORIES_ONLY, chooser.getFileSelectionMode());
		assertEquals(SystemFileChooser.APPROVE_OPTION, chooser.approveSelection(new File[] { directory }, context));
		assertNull(context.messageType);
	}

	/**
	 * Approve context that records the shown message instead of displaying a native dialog.
	 */
	private static final class RecordingApproveContext extends SystemFileChooser.ApproveContext {
		private Integer messageType;

		@Override
		public int showMessageDialog(int messageType, String primaryText, String secondaryText,
				int defaultButton, String... buttons) {
			this.messageType = messageType;
			return 0;
		}
	}
}
