package info.openrocket.swing.gui.figure3d.photo;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import info.openrocket.swing.file.photo.PhotoStudioSetter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomPhotoBackgroundTest {
	@TempDir
	Path directory;

	@Test
	void importsReplacesAndRemovesImageWithoutChangingOtherSettings() throws IOException {
		PhotoSettings settings = new PhotoSettings();
		var originalSky = settings.getSky();
		settings.setLightStrength(0.5);
		AtomicInteger events = new AtomicInteger();
		settings.addChangeListener(e -> events.incrementAndGet());
		File png = createImage("background.png", "png", 12, 8);
		File jpeg = createImage("background.jpg", "jpeg", 16, 9);

		settings.importCustomBackground(png);
		assertEquals(png, settings.getCustomBackground());
		assertEquals(PhotoSettings.BackgroundType.CUSTOM_IMAGE, settings.getBackgroundType());
		assertEquals(1, events.get());
		settings.importCustomBackground(jpeg);
		assertEquals(jpeg, settings.getCustomBackground());
		settings.removeCustomBackground();
		assertNull(settings.getCustomBackground());
		assertEquals(PhotoSettings.BackgroundType.TEXTURE, settings.getBackgroundType());
		assertSame(originalSky, settings.getSky());
		assertEquals(0.5, settings.getLightStrength(), 1e-12);
		assertEquals(3, events.get());
	}

	@Test
	void invalidImageLeavesPreviousSelectionUnchanged() throws IOException {
		PhotoSettings settings = new PhotoSettings();
		File valid = createImage("valid.png", "png", 12, 8);
		settings.importCustomBackground(valid);
		File invalid = Files.writeString(directory.resolve("invalid.png"), "not an image").toFile();
		AtomicInteger events = new AtomicInteger();
		settings.addChangeListener(e -> events.incrementAndGet());
		assertThrows(IOException.class, () -> settings.importCustomBackground(invalid));
		assertEquals(valid, settings.getCustomBackground());
		assertEquals(PhotoSettings.BackgroundType.CUSTOM_IMAGE, settings.getBackgroundType());
		assertEquals(0, events.get());
	}

	@Test
	void rejectsUnsupportedFormatAndOversizedDimensions() throws IOException {
		File gif = createImage("background.gif", "gif", 12, 8);
		File wide = createImage("wide.png", "png", 4097, 1);
		assertThrows(IOException.class, () -> CustomPhotoBackground.validate(gif));
		assertThrows(IOException.class, () -> CustomPhotoBackground.validate(wide));
	}

	@Test
	void savesBuiltInBackgroundInsteadOfLocalImagePath() throws IOException {
		PhotoSettings settings = new PhotoSettings();
		File image = createImage("private.png", "png", 12, 8);
		settings.importCustomBackground(image);
		var saved = PhotoStudioSetter.getPhotoSettings(settings);
		assertEquals("TEXTURE", saved.get("backgroundType"));
		assertTrue(saved.values().stream().noneMatch(value -> value.contains(image.getAbsolutePath())));
	}

	private File createImage(String name, String format, int width, int height) throws IOException {
		File file = directory.resolve(name).toFile();
		assertTrue(ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, file));
		return file;
	}
}
