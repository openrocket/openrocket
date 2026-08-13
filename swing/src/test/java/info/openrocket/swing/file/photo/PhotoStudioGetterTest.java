package info.openrocket.swing.file.photo;

import info.openrocket.swing.gui.figure3d.photo.PhotoSettings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoStudioGetterTest {

	@Test
	void settingsPoisonedByAnOlderSaveFallBackToDefaults() {
		PhotoSettings defaults = new PhotoSettings();
		Map<String, String> settings = Map.of(
				"lightStrength", "null",
				"motionBlurAmount", "null");

		PhotoSettings loaded = assertDoesNotThrow(() -> new PhotoStudioGetter(settings).getPhotoSettings());

		assertEquals(defaults.getLightStrength(), loaded.getLightStrength());
		assertEquals(defaults.getMotionBlurAmount(), loaded.getMotionBlurAmount());
	}
}
