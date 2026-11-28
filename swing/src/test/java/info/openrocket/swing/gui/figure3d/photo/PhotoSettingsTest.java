package info.openrocket.swing.gui.figure3d.photo;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ORColor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoSettingsTest {
	@Test
	void defaultCameraUsesFiftyDegreeFieldOfViewAtMatchingDistance() {
		PhotoSettings settings = new PhotoSettings();

		assertEquals(Math.toRadians(50.0), settings.getFov(), 1.0e-12);
		assertEquals(0.80, settings.getViewDistance(), 1.0e-12);
	}

	@Test
	void lightingPresetsSetAllLightingProperties() {
		PhotoSettings settings = new PhotoSettings();
		assertPreset(settings, PhotoSettings.LightingPreset.STUDIO, 255, 255, 255, 0.9, 0.45, 35, -45);
		assertPreset(settings, PhotoSettings.LightingPreset.DAYLIGHT, 255, 248, 230, 1.25, 0.35, 55, -45);
		assertPreset(settings, PhotoSettings.LightingPreset.SUNSET, 255, 155, 75, 1.1, 0.18, 12, -60);
		assertPreset(settings, PhotoSettings.LightingPreset.NIGHT, 130, 110, 255, 1.15, 0.07, 20, 60);
	}

	@Test
	void lightingPresetPreservesOtherSettingsAndNotifiesOnce() {
		PhotoSettings settings = new PhotoSettings();
		settings.setView(-0.4, 1.2, 1.5, 0.9);
		settings.setRoll(0.6);
		settings.setPitch(0.8);
		settings.setYaw(1.1);
		settings.setAdvance(0.2);
		settings.setBackgroundType(PhotoSettings.BackgroundType.GRADIENT);
		ORColor background = new ORColor(10, 20, 30);
		settings.setGradientTopColor(background);
		settings.setSmoke(true);
		settings.setFlame(true);
		AtomicInteger notifications = new AtomicInteger();
		settings.addChangeListener(e -> notifications.incrementAndGet());

		settings.applyLightingPreset(PhotoSettings.LightingPreset.NIGHT);

		assertEquals(1, notifications.get());
		assertEquals(-0.4, settings.getViewAlt(), 1e-12);
		assertEquals(1.2, settings.getViewAz(), 1e-12);
		assertEquals(1.5, settings.getViewDistance(), 1e-12);
		assertEquals(0.9, settings.getFov(), 1e-12);
		assertEquals(0.6, settings.getRoll(), 1e-12);
		assertEquals(0.8, settings.getPitch(), 1e-12);
		assertEquals(1.1, settings.getYaw(), 1e-12);
		assertEquals(0.2, settings.getAdvance(), 1e-12);
		assertEquals(PhotoSettings.BackgroundType.GRADIENT, settings.getBackgroundType());
		assertSame(background, settings.getGradientTopColor());
		assertTrue(settings.isSmoke());
		assertTrue(settings.isFlame());
	}

	private static void assertPreset(PhotoSettings settings, PhotoSettings.LightingPreset preset,
			int red, int green, int blue, double strength, double ambient, double altitude, double azimuth) {
		settings.applyLightingPreset(preset);
		assertEquals(red, settings.getSunlight().getRed());
		assertEquals(green, settings.getSunlight().getGreen());
		assertEquals(blue, settings.getSunlight().getBlue());
		assertEquals(strength, settings.getLightStrength(), 1e-12);
		assertEquals(ambient, settings.getAmbiance(), 1e-12);
		assertEquals(Math.toRadians(altitude), settings.getLightAlt(), 1e-12);
		assertEquals(Math.toRadians(azimuth), settings.getLightAz(), 1e-12);
	}
}
