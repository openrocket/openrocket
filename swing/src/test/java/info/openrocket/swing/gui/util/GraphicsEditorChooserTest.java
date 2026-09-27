package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.FlatLightLaf;

class GraphicsEditorChooserTest {

	private LookAndFeel originalLookAndFeel;

	@BeforeEach
	void installFlatLaf() {
		originalLookAndFeel = UIManager.getLookAndFeel();
		FlatLightLaf.setup();
	}

	@AfterEach
	void restoreLookAndFeel() throws UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(originalLookAndFeel);
	}

	@Test
	void macApplicationBundlesAreSelectableInsteadOfTraversable(@TempDir Path temporaryDirectory) throws IOException {
		File applicationBundle = Files.createDirectory(temporaryDirectory.resolve("Editor.app")).toFile();
		File uppercaseApplicationBundle = Files.createDirectory(temporaryDirectory.resolve("Alternate.APP")).toFile();
		File ordinaryDirectory = Files.createDirectory(temporaryDirectory.resolve("editors")).toFile();
		File ordinaryFile = Files.createFile(temporaryDirectory.resolve("editor")).toFile();

		FileView fileView = GraphicsEditorChooser.createMacApplicationFileView();
		assertFalse(fileView.isTraversable(applicationBundle));
		assertFalse(fileView.isTraversable(uppercaseApplicationBundle));
		assertNull(fileView.isTraversable(ordinaryDirectory));
		assertNull(fileView.isTraversable(ordinaryFile));

		JFileChooser chooser = new JFileChooser();
		chooser.setFileView(fileView);
		assertFalse(chooser.isTraversable(applicationBundle));
		assertTrue(chooser.isTraversable(ordinaryDirectory));
	}

	@Test
	void openButtonApprovesSelectedMacApplicationBundle(@TempDir Path temporaryDirectory) throws IOException {
		File applicationBundle = Files.createDirectory(temporaryDirectory.resolve("Editor.app")).toFile();

		JFileChooser chooser = GraphicsEditorChooser.createMacApplicationChooser();
		chooser.setCurrentDirectory(temporaryDirectory.toFile());
		AtomicReference<String> command = new AtomicReference<>();
		chooser.addActionListener(e -> command.set(e.getActionCommand()));

		chooser.setSelectedFile(applicationBundle);
		clickOpen(chooser);

		assertEquals(JFileChooser.APPROVE_SELECTION, command.get());
		assertEquals(applicationBundle, chooser.getSelectedFile());
	}

	@Test
	void openButtonOpensSelectedOrdinaryDirectory(@TempDir Path temporaryDirectory) throws IOException {
		File ordinaryDirectory = Files.createDirectory(temporaryDirectory.resolve("editors")).toFile();

		JFileChooser chooser = GraphicsEditorChooser.createMacApplicationChooser();
		chooser.setCurrentDirectory(temporaryDirectory.toFile());
		AtomicReference<String> command = new AtomicReference<>();
		chooser.addActionListener(e -> command.set(e.getActionCommand()));

		chooser.setSelectedFile(ordinaryDirectory);
		clickOpen(chooser);

		assertNull(command.get());
		// Compare canonical files because Windows may report the temp directory as an 8.3 short path
		assertEquals(ordinaryDirectory.getCanonicalFile(), chooser.getCurrentDirectory().getCanonicalFile());
	}

	private static void clickOpen(JFileChooser chooser) {
		((BasicFileChooserUI) chooser.getUI()).getApproveSelectionAction().actionPerformed(null);
	}
}
