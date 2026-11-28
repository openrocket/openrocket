package info.openrocket.swing.gui.figure3d.photo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoSettingsTest {
	@Test
	void explosionAmountIsBoundedAndDoesNotChangeOtherSettings() {
		PhotoSettings settings = new PhotoSettings();
		assertEquals(0, settings.getExplosionAmount(), 1e-12);
		settings.setViewDistance(1.7);
		settings.setFlame(true);
		settings.setExplosionAmount(0.5);
		assertEquals(0.5, settings.getExplosionAmount(), 1e-12);
		settings.setExplosionAmount(2);
		assertEquals(1, settings.getExplosionAmount(), 1e-12);
		assertThrows(IllegalArgumentException.class, () -> settings.setExplosionAmount(Double.NaN));
		settings.setExplosionAmount(-1);
		assertEquals(0, settings.getExplosionAmount(), 1e-12);
		assertEquals(1.7, settings.getViewDistance(), 1e-12);
		assertTrue(settings.isFlame());
	}

	@Test
	void defaultCameraUsesFiftyDegreeFieldOfViewAtMatchingDistance() {
		PhotoSettings settings = new PhotoSettings();

		assertEquals(Math.toRadians(50.0), settings.getFov(), 1.0e-12);
		assertEquals(0.80, settings.getViewDistance(), 1.0e-12);
	}
}
