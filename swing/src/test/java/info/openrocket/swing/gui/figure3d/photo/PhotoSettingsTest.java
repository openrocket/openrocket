package info.openrocket.swing.gui.figure3d.photo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoSettingsTest {
	@Test
	void defaultCameraUsesFiftyDegreeFieldOfViewAtMatchingDistance() {
		PhotoSettings settings = new PhotoSettings();

		assertEquals(Math.toRadians(50.0), settings.getFov(), 1.0e-12);
		assertEquals(0.80, settings.getViewDistance(), 1.0e-12);
	}
}
