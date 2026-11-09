package info.openrocket.swing.gui.scalefigure;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;

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
	void thumbnailPngIsEmbeddedNextToPreview() throws Exception {
		byte[] previewBytes = { 1, 2, 3 };
		byte[] thumbnailBytes = { 4, 5, 6, 7 };

		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		StorageOptions options = document.getDefaultStorageOptions();
		options.setPreviewImage(previewBytes.clone());
		options.setThumbnailImage(thumbnailBytes.clone());

		File tempFile = Files.createTempFile("thumbnail-test", ".ork").toFile();
		tempFile.deleteOnExit();

		new GeneralRocketSaver().save(tempFile, document);

		try (ZipFile zip = new ZipFile(tempFile)) {
			ZipEntry previewEntry = zip.getEntry("preview.png");
			ZipEntry thumbnailEntry = zip.getEntry("thumbnail.png");
			assertNotNull(previewEntry, "Saved ORK must contain preview.png");
			assertNotNull(thumbnailEntry, "Saved ORK must contain thumbnail.png");

			try (InputStream previewStream = zip.getInputStream(previewEntry);
				 InputStream thumbnailStream = zip.getInputStream(thumbnailEntry)) {
				assertArrayEquals(previewBytes, previewStream.readAllBytes());
				assertArrayEquals(thumbnailBytes, thumbnailStream.readAllBytes());
			}
		}
	}

	@Test
	void previewAndThumbnailRenderedWithThe3DViewArePersisted() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Rendering needs a window with an OpenGL canvas; skip in headless environments");
		Assumptions.assumeTrue(RocketFigure3d.is3dEnabled(), "3D rendering is disabled");

		String previousFlag = System.getProperty("openrocket.unittest");
		System.setProperty("openrocket.unittest", "true");
		AtomicReference<JFrame> frameRef = new AtomicReference<>();

		try {
			// The images are 3D renders of the rocket, so the rocket needs components to draw.
			OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(
					TestRockets.makeEstesAlphaIII());

			// The design window shows the 2D view, so the 3D view renders offscreen.
			AtomicReference<RocketPanel> panelRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() -> {
				RocketPanel panel = new RocketPanel(document);
				JFrame frame = new JFrame();
				frame.add(panel);
				frame.setSize(800, 600);
				frame.setVisible(true);
				panelRef.set(panel);
				frameRef.set(frame);
			});
			assertFalse(panelRef.get().getCurrentViewType().is3d, "Test should save from the 2D view");

			AtomicReference<Supplier<byte[]>> previewRef = new AtomicReference<>();
			AtomicReference<Supplier<byte[]>> thumbnailRef = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() -> {
				previewRef.set(panelRef.get().prepareFilePreview(640, 480));
				thumbnailRef.set(panelRef.get().prepareFileThumbnail(256));
			});
			assertNotNull(previewRef.get(), "Preview should be prepared");
			assertNotNull(thumbnailRef.get(), "Thumbnail should be prepared");

			// Render off the EDT, as the save worker does.
			byte[] preview = previewRef.get().get();
			byte[] thumbnail = thumbnailRef.get().get();
			assertNotNull(preview, "Preview should be rendered");
			assertNotNull(thumbnail, "Thumbnail should be rendered");

			BufferedImage previewImage = ImageIO.read(new ByteArrayInputStream(preview));
			assertEquals(640, previewImage.getWidth());
			assertEquals(480, previewImage.getHeight());
			assertTrue(colorCount(previewImage) > 10, "Preview should show the rocket, not a blank image");
			BufferedImage thumbnailImage = ImageIO.read(new ByteArrayInputStream(thumbnail));
			assertEquals(256, thumbnailImage.getWidth());
			assertEquals(256, thumbnailImage.getHeight());
			assertTrue(colorCount(thumbnailImage) > 10, "Thumbnail should show the rocket, not a blank image");

			StorageOptions options = document.getDefaultStorageOptions();
			options.setPreviewImage(preview);
			options.setThumbnailImage(thumbnail);
			File tempFile = Files.createTempFile("preview-auto", ".ork").toFile();
			tempFile.deleteOnExit();
			new GeneralRocketSaver().save(tempFile, document);

			try (ZipFile zip = new ZipFile(tempFile)) {
				ZipEntry previewEntry = zip.getEntry("preview.png");
				ZipEntry thumbnailEntry = zip.getEntry("thumbnail.png");
				assertNotNull(previewEntry, "Saved ORK must contain preview.png");
				assertNotNull(thumbnailEntry, "Saved ORK must contain thumbnail.png");
				try (InputStream previewStream = zip.getInputStream(previewEntry);
					 InputStream thumbnailStream = zip.getInputStream(thumbnailEntry)) {
					assertArrayEquals(preview, previewStream.readAllBytes());
					assertArrayEquals(thumbnail, thumbnailStream.readAllBytes());
				}
			}
		} finally {
			JFrame frame = frameRef.get();
			if (frame != null) {
				SwingUtilities.invokeAndWait(frame::dispose);
			}
			if (previousFlag == null) {
				System.clearProperty("openrocket.unittest");
			} else {
				System.setProperty("openrocket.unittest", previousFlag);
			}
		}
	}

	private static long colorCount(BufferedImage image) {
		java.util.Set<Integer> colors = new java.util.HashSet<>();
		for (int y = 0; y < image.getHeight(); y += 2) {
			for (int x = 0; x < image.getWidth(); x += 2) {
				colors.add(image.getRGB(x, y));
			}
		}
		return colors.size();
	}
}
