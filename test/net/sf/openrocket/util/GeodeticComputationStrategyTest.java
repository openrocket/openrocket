package net.sf.openrocket.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeodeticComputationStrategyTest {
	
	@Test
	public void testAddCoordinate() {
		
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
		
		System.out.println(wc.getLatitudeDeg());
		System.out.println(lat2);
		
		System.out.println(wc.getLongitudeDeg());
		System.out.println(lon2);
		
		assertEquals(lat2, wc.getLatitudeDeg(), 0.001);
		assertEquals(lon2, wc.getLongitudeDeg(), 0.001);
		assertEquals(1000.0, wc.getAltitude(), 0.0);
	}
	
	@Test
	public void testGetCoriolisAcceleration1() {
		
		// For positive latitude and rotational velocity, a movement due east results in an acceleration due south
		Coordinate velocity = new Coordinate(-1000, 0, 0);
		WorldCoordinate wc = new WorldCoordinate(45, 0, 0);
		double north_accel = GeodeticComputationStrategy.SPHERICAL.getCoriolisAcceleration(wc, velocity).y;
		System.out.println("North accel " + north_accel);
		assertTrue(north_accel < 0.0);
		
	}
	
}
