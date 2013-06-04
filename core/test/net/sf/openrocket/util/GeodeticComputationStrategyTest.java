package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeodeticComputationStrategyTest {
	
	@Test
	public void testSpericalAddCoordinate() {
		
		double arcmin = (1.0 / 60.0);
		double arcsec = (1.0 / (60.0 * 60.0));
		
		double lat1 = 50.0 + 3 * arcmin + 59 * arcsec;
		double lon1 = -1.0 * (5 + 42 * arcmin + 53 * arcsec); //W 
		
		double lat2 = 58 + 38 * arcmin + 38 * arcsec;
		double lon2 = -1.0 * (3 + 4 * arcmin + 12 * arcsec);
		
		double range = 968.9 * 1000.0;
		double bearing = (9.0 + 7 * arcmin + 11 * arcsec) * (Math.PI / 180.0);
		
		Coordinate coord = new Coordinate(range * Math.sin(bearing), range * Math.cos(bearing), 1000.0);
		WorldCoordinate wc = new WorldCoordinate(lat1, lon1, 0.0);
		wc = GeodeticComputationStrategy.SPHERICAL.addCoordinate(wc, coord);
		
		//System.out.println(wc.getLatitudeDeg());
		//System.out.println(lat2);
		
		//System.out.println(wc.getLongitudeDeg());
		//System.out.println(lon2);
		
		assertEquals(lat2, wc.getLatitudeDeg(), 0.001);
		assertEquals(lon2, wc.getLongitudeDeg(), 0.001);
		assertEquals(1000.0, wc.getAltitude(), 0.0);
	}
	
	
	@Test
	public void testAddCoordinates() {
		
		double min = 1 / 60.0;
		double sec = 1 / 3600.0;
		
		
		// Test zero movement
		//System.out.println("\nTesting zero movement");
		testAddCoordinate(50.0, 20.0, 0, 123, 50.0, 20.0, false);
		
		
		/*
		 * These example values have been computed using the calculator at
		 * http://www.movable-type.co.uk/scripts/latlong.html
		 */
		
		// Long distance NE over England, crosses Greenwich meridian
		// 50 03N  005 42W  to  58 38N  003 04E  is  1109km at 027 16'07"
		//System.out.println("\nTesting 1109km NE over England");
		testAddCoordinate(50 + 3 * min, -5 - 42 * min, 1109000, 27 + 16 * min + 7 * sec, 58 + 38 * min, 3 + 4 * min, false);
		
		// SW over Brazil
		// -10N  -60E  to  -11N  -61E  is  155.9km at 224 25'34"
		//System.out.println("\nTesting 155km SW over Brazil");
		testAddCoordinate(-10, -60, 155900, 224 + 25 * min + 34 * sec, -11, -61, true);
		
		// NW over the 180 meridian
		// 63N  -179E  to  63 01N  179E  is  100.9km at 271 56'34"
		//System.out.println("\nTesting 100km NW over 180 meridian");
		testAddCoordinate(63, -179, 100900, 271 + 56 * min + 34 * sec, 63 + 1 * min, 179, true);
		
		// NE near the north pole
		// 89 50N  0E  to 89 45N  175E  is 46.29 km at 003 00'01"
		//System.out.println("\nTesting 46km NE near north pole");
		testAddCoordinate(89 + 50 * min, 0, 46290, 3 + 0 * min + 1 * sec, 89 + 45 * min, 175, false);
		
		// S directly over south pole
		// -89 50N  12E  to  -89 45N  192E  is  46.33km at 180 00'00"
		//System.out.println("\nTesting 46km directly over south pole ");
		testAddCoordinate(-89 - 50 * min, 12, 46330, 180, -89 - 45 * min, -168, false);
		
	}
	
	private void testAddCoordinate(double initialLatitude, double initialLongitude, double distance, double bearing,
			double finalLatitude, double finalLongitude, boolean testFlat) {
		
		double tolerance;
		
		bearing = Math.toRadians(bearing);
		
		// positive X is EAST, positive Y is NORTH
		double deltaX = distance * Math.sin(bearing);
		double deltaY = distance * Math.cos(bearing);
		
		Coordinate coord = new Coordinate(deltaX, deltaY, 1000.0);
		WorldCoordinate wc = new WorldCoordinate(initialLatitude, initialLongitude, 0.0);
		
		// Test SPHERICAL
		tolerance = 0.0015 * distance / 111325;
		//System.out.println("\nSpherical tolerance: " + tolerance);
		WorldCoordinate result = GeodeticComputationStrategy.SPHERICAL.addCoordinate(wc, coord);
		
		//System.out.println("Difference Lat: " + Math.abs(finalLatitude - result.getLatitudeDeg()));
		//System.out.println("Difference Lon: " + Math.abs(finalLongitude - result.getLongitudeDeg()));
		assertEquals(finalLatitude, result.getLatitudeDeg(), tolerance);
		assertEquals(finalLongitude, result.getLongitudeDeg(), tolerance);
		assertEquals(1000.0, result.getAltitude(), 0.0);
		
		
		// Test WGS84
		/*
		 * Note: Since the example values are computed using a spherical earth approximation,
		 * the WGS84 method will have significantly larger errors.  A tolerance of 1% accommodates
		 * all cases except the NE flight near the north pole, where the ellipsoidal effect is
		 * the greatest.
		 */
		tolerance = 0.04 * distance / 111325;
		//System.out.println("\nWGS84 tolerance: " + tolerance);
		result = GeodeticComputationStrategy.WGS84.addCoordinate(wc, coord);
		
		//System.out.println("Difference Lat: " + Math.abs(finalLatitude - result.getLatitudeDeg()));
		//System.out.println("Difference Lon: " + Math.abs(finalLongitude - result.getLongitudeDeg()));
		assertEquals(finalLatitude, result.getLatitudeDeg(), tolerance);
		assertEquals(finalLongitude, result.getLongitudeDeg(), tolerance);
		assertEquals(1000.0, result.getAltitude(), 0.0);
		
		
		// Test FLAT
		if (testFlat) {
			tolerance = 0.02 * distance / 111325;
			//System.out.println("\nFlat tolerance: " + tolerance);
			result = GeodeticComputationStrategy.FLAT.addCoordinate(wc, coord);
			
			//System.out.println("Difference Lat: " + Math.abs(finalLatitude - result.getLatitudeDeg()));
			//System.out.println("Difference Lon: " + Math.abs(finalLongitude - result.getLongitudeDeg()));
			assertEquals(finalLatitude, result.getLatitudeDeg(), tolerance);
			assertEquals(finalLongitude, result.getLongitudeDeg(), tolerance);
			assertEquals(1000.0, result.getAltitude(), 0.0);
			
		}
		
	}
	
	
	
	@Test
	public void testSpericalGetCoriolisAcceleration() {
		
		// For positive latitude and rotational velocity, a movement due east results in an acceleration due south
		Coordinate velocity = new Coordinate(-1000, 0, 0);
		WorldCoordinate wc = new WorldCoordinate(45, 0, 0);
		double north_accel = GeodeticComputationStrategy.SPHERICAL.getCoriolisAcceleration(wc, velocity).y;
		//System.out.println("North accel " + north_accel);
		assertTrue(north_accel < 0.0);
		
	}
	
}
