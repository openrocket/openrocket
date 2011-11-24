package net.sf.openrocket.models.gravity;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.util.WorldCoordinate;

import org.junit.Test;


public class WGSGravityModelTest {
	
	private WGSGravityModel model = new WGSGravityModel();
	
	@Test
	public void testSurfaceGravity() {
		// Equator
		test(0, 0, 0, 9.780);
		// Mid-latitude
		test(45, 0, 0, 9.806);
		// Mid-latitude
		test(45, 99, 0, 9.806);
		// South pole
		test(-90, 0, 0, 9.832);
	}
	
	@Test
	public void testAltitudeEffect() {
		test(45, 0, -100, 9.806);
		test(45, 0, 0, 9.806);
		test(45, 0, 10, 9.806);
		test(45, 0, 100, 9.806);
		test(45, 0, 1000, 9.803);
		test(45, 0, 10000, 9.775);
		test(45, 0, 100000, 9.505);
	}
	
	private void test(double lat, double lon, double alt, double g) {
		WorldCoordinate wc = new WorldCoordinate(lat, lon, alt);
		assertEquals(g, model.getGravity(wc), 0.001);
		assertEquals(g, model.getGravity(wc), 0.001);
	}
	
}
