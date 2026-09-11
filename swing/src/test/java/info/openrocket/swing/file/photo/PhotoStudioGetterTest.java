package info.openrocket.swing.file.photo;

import info.openrocket.swing.gui.figure3d.photo.PhotoSettings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoStudioGetterTest {

	@Test
	void invalidNumericSettingsFallBackToDefaults() {
		PhotoSettings defaults = new PhotoSettings();
		Map<String, String> settings = Map.ofEntries(
				Map.entry("roll", "null"),
				Map.entry("yaw", "null"),
				Map.entry("pitch", "null"),
				Map.entry("advance", "null"),
				Map.entry("viewAlt", "null"),
				Map.entry("viewAz", "null"),
				Map.entry("viewDistance", "null"),
				Map.entry("fov", "null"),
				Map.entry("lightAlt", "null"),
				Map.entry("lightAz", "null"),
				Map.entry("lightStrength", "null"),
				Map.entry("ambiance", "null"),
				Map.entry("motionBlurAmount", "null"),
				Map.entry("exhaustScale", "null"),
				Map.entry("flameAspectRatio", "null"),
				Map.entry("sparkConcentration", "null"),
				Map.entry("sparkWeight", "null"));

		PhotoSettings loaded = assertDoesNotThrow(() -> new PhotoStudioGetter(settings).getPhotoSettings());

		assertEquals(defaults.getRoll(), loaded.getRoll());
		assertEquals(defaults.getYaw(), loaded.getYaw());
		assertEquals(defaults.getPitch(), loaded.getPitch());
		assertEquals(defaults.getAdvance(), loaded.getAdvance());
		assertEquals(defaults.getViewAlt(), loaded.getViewAlt());
		assertEquals(defaults.getViewAz(), loaded.getViewAz());
		assertEquals(defaults.getViewDistance(), loaded.getViewDistance());
		assertEquals(defaults.getFov(), loaded.getFov());
		assertEquals(defaults.getLightAlt(), loaded.getLightAlt());
		assertEquals(defaults.getLightAz(), loaded.getLightAz());
		assertEquals(defaults.getLightStrength(), loaded.getLightStrength());
		assertEquals(defaults.getAmbiance(), loaded.getAmbiance());
		assertEquals(defaults.getMotionBlurAmount(), loaded.getMotionBlurAmount());
		assertEquals(defaults.getExhaustScale(), loaded.getExhaustScale());
		assertEquals(defaults.getFlameAspectRatio(), loaded.getFlameAspectRatio());
		assertEquals(defaults.getSparkConcentration(), loaded.getSparkConcentration());
		assertEquals(defaults.getSparkWeight(), loaded.getSparkWeight());
	}

	@Test
	void malformedColorsAndNonFiniteNumbersFallBackToDefaults() {
		PhotoSettings defaults = new PhotoSettings();
		Map<String, String> settings = Map.of(
				"roll", "NaN",
				"viewDistance", "Infinity",
				"sunlight", "255 255 broken 255",
				"skyColor", "256 0 0 255");

		PhotoSettings loaded = assertDoesNotThrow(() -> new PhotoStudioGetter(settings).getPhotoSettings());

		assertEquals(defaults.getRoll(), loaded.getRoll());
		assertEquals(defaults.getViewDistance(), loaded.getViewDistance());
		assertEquals(defaults.getSunlight(), loaded.getSunlight());
		assertEquals(defaults.getSkyColor(), loaded.getSkyColor());
	}
}
