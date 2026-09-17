package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphicsEditorChooserTest {

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
}
