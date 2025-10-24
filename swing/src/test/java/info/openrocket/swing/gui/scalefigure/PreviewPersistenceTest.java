package info.openrocket.swing.gui.scalefigure;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketSaver;

/**
 * Validates that the generated preview bytes wind up in the saved ORK archive.
 * <p>
 * This test does not exercise the Swing rendering pipeline directly (which is hard to
 * drive in a headless CI environment). Instead it feeds a known PNG payload into the
 * storage options and verifies that {@link GeneralRocketSaver} persists it as the
 * {@code preview.png} entry.
 */
class PreviewPersistenceTest extends BaseTestCase {

	@Test
	void previewPngIsEmbeddedInSavedArchive() throws Exception {
		// Build a vivid 32x32 PNG so we can recognise the exact bytes that should be written.
		BufferedImage stubPreview = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = stubPreview.createGraphics();
		g2.setColor(new Color(0x3366FF));
		g2.fillRect(0, 0, 32, 32);
		g2.setColor(new Color(0xFFCC00));
		g2.fillOval(4, 4, 24, 24);
		g2.dispose();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(stubPreview, "png", buffer), "PNG encoding should succeed");
		byte[] previewBytes = buffer.toByteArray();
		assertTrue(previewBytes.length > 0, "Encoded PNG must contain data");

		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		StorageOptions options = document.getDefaultStorageOptions();
		options.setPreviewImage(previewBytes.clone());

		File tempFile = Files.createTempFile("preview-test", ".ork").toFile();
		tempFile.deleteOnExit();

		new GeneralRocketSaver().save(tempFile, document);

		try (ZipFile zip = new ZipFile(tempFile)) {
			ZipEntry previewEntry = zip.getEntry("preview.png");
			assertNotNull(previewEntry, "Saved ORK must contain preview.png");

			try (InputStream previewStream = zip.getInputStream(previewEntry)) {
				byte[] savedBytes = previewStream.readAllBytes();
				assertArrayEquals(previewBytes, savedBytes, "Saved preview should match the source PNG exactly");
			}
		}
	}

	@Test
	void previewGeneratedThroughRocketPanelIsPersisted() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"RocketPanel relies on Swing painting; skip in headless environments");

		String previousFlag = System.getProperty("openrocket.unittest");
		System.setProperty("openrocket.unittest", "true");

		try {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			AtomicReference<RocketPanel> panelRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() -> panelRef.set(new RocketPanel(document)));

			RocketPanel panel = panelRef.get();
			assertNotNull(panel, "RocketPanel should be created");

			AtomicReference<Boolean> attachedRef = new AtomicReference<>(Boolean.FALSE);
			SwingUtilities.invokeAndWait(() -> attachedRef.set(panel.attachPreviewToDocument(
					document, RocketPanel.VIEW_TYPE.SideView, 640, 480, 720)));

			assertTrue(attachedRef.get(), "Preview image should be attached to the document");
			byte[] storedPreview = document.getDefaultStorageOptions().getPreviewImage();
			assertNotNull(storedPreview, "Document storage options should now contain preview bytes");
			assertTrue(storedPreview.length > 0, "Stored preview data should be non-empty");

			File tempFile = Files.createTempFile("preview-auto", ".ork").toFile();
			tempFile.deleteOnExit();

			new GeneralRocketSaver().save(tempFile, document);

			try (ZipFile zip = new ZipFile(tempFile)) {
				ZipEntry previewEntry = zip.getEntry("preview.png");
				assertNotNull(previewEntry, "Saved ORK must contain preview.png generated via panel");

				try (InputStream previewStream = zip.getInputStream(previewEntry)) {
					byte[] savedBytes = previewStream.readAllBytes();
					assertArrayEquals(storedPreview, savedBytes, "Saved preview should match the panel-generated PNG");
				}
			}
		} finally {
			if (previousFlag == null) {
				System.clearProperty("openrocket.unittest");
			} else {
				System.setProperty("openrocket.unittest", previousFlag);
			}
		}
	}
}
