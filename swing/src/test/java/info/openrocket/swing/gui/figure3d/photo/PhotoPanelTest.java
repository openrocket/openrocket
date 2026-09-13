package info.openrocket.swing.gui.figure3d.photo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoPanelTest {
	@Test
	void lightDirectionRoundTripsToTheOriginalAzimuthInEveryQuadrant() {
		double[] azimuths = { 0.0, Math.PI / 2.0, Math.PI, Math.PI * 3.0 / 2.0 };

		for (double azimuth : azimuths) {
			float dx = (float) Math.cos(azimuth);
			float dz = (float) Math.sin(azimuth);

			assertEquals(azimuth, PhotoPanel.lightAzimuthFromDirection(dx, dz), 1.0e-6);
		}
	}
}
