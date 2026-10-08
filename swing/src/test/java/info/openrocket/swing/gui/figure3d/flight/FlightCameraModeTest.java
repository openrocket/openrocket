package info.openrocket.swing.gui.figure3d.flight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class FlightCameraModeTest {
	@Test
	void replayCameraModesExcludeOnboardView() {
		assertArrayEquals(new FlightCameraMode[] {
				FlightCameraMode.OVERVIEW, FlightCameraMode.FOLLOW, FlightCameraMode.PAD
		}, FlightCameraMode.values());
	}
}
