package info.openrocket.swing.gui.figure3d.photo;

import info.openrocket.core.util.ORColor;
import info.openrocket.swing.file.photo.PhotoStudioSetter;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Lake;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PhotoSettingsResetTest {

	@Test
	void resetRestoresAllPhotoStudioDefaults() {
		PhotoSettings settings = new PhotoSettings();
		Map<String, String> defaults = PhotoStudioSetter.getPhotoSettings(new PhotoSettings());

		// Orientation and camera
		settings.setRoll(0.4);
		settings.setYaw(1.1);
		settings.setPitch(0.7);
		settings.setAdvance(0.12);
		settings.setView(-0.7, 1.0, 1.4, Math.toRadians(70));

		// Environment
		settings.setLight(-0.4, 0.9);
		settings.setLightStrength(1.6);
		settings.setSunlight(new ORColor(100, 150, 200));
		settings.setAmbiance(0.4);
		settings.setBackgroundType(PhotoSettings.BackgroundType.GRADIENT);
		settings.setSkyColor(new ORColor(10, 20, 30));
		settings.setSkyColorOpacity(0.5);
		settings.setGradientTopColor(new ORColor(90, 80, 70));
		settings.setGradientBottomColor(new ORColor(20, 30, 40));
		settings.setSky(Lake.instance);

		// Effects
		settings.setMotionBlurred(true);
		settings.setMotionBlurAmount(12);
		settings.setFlame(true);
		settings.setFlameColor(new ORColor(20, 40, 60));
		settings.setSmoke(true);
		settings.setSmokeColor(new ORColor(70, 80, 90));
		settings.setSmokeOpacity(0.75);
		settings.setSparks(true);
		settings.setExhaustScale(7);
		settings.setFlameAspectRatio(2);
		settings.setSparkConcentration(0.9);
		settings.setSparkWeight(0.6);

		assertNotEquals(defaults, PhotoStudioSetter.getPhotoSettings(settings));

		settings.resetToDefaults();

		assertEquals(defaults, PhotoStudioSetter.getPhotoSettings(settings));
		assertEquals(1.0, settings.getSkyColorOpacity(), 0.00001);
	}

	@Test
	void resetNotifiesExistingListenersOnce() {
		PhotoSettings settings = new PhotoSettings();
		settings.setPitch(0.7);
		AtomicInteger notifications = new AtomicInteger();
		settings.addChangeListener(event -> notifications.incrementAndGet());

		settings.resetToDefaults();

		assertEquals(1, notifications.get());
		assertEquals(new PhotoSettings().getPitch(), settings.getPitch(), 0.00001);
	}
}
