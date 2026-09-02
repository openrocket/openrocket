package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SystemLocationProviderTest {
	@Test
	void parsesNativeTabSeparatedLocation() throws Exception {
		DeviceLocation location = SystemLocationProvider.parse("33.25\t-117.27\t42.5\t12.0", "Test Provider");

		assertEquals(33.25, location.latitude());
		assertEquals(-117.27, location.longitude());
		assertEquals(42.5, location.altitude());
		assertEquals(12.0, location.horizontalAccuracy());
	}

	@Test
	void rejectsOutOfRangeCoordinates() {
		assertThrows(LocationException.class,
				() -> SystemLocationProvider.parse("95\t-117.27\t42.5\t12.0", "Test Provider"));
	}
}
