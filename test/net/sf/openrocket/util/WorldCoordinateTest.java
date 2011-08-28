package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WorldCoordinateTest {
	
	private static final double EPS = 1e-10;
	
	@Test
	public void testConstructor() {
		WorldCoordinate wc;
		
		wc = new WorldCoordinate(10, 15, 130);
		assertEquals(10, wc.getLatitudeDeg(), EPS);
		assertEquals(15, wc.getLongitudeDeg(), EPS);
		assertEquals(130, wc.getAltitude(), 0);
		
		wc = new WorldCoordinate(100, 190, 13000);
		assertEquals(90, wc.getLatitudeDeg(), EPS);
		assertEquals(-170, wc.getLongitudeDeg(), EPS);
		assertEquals(13000, wc.getAltitude(), 0);
		
		wc = new WorldCoordinate(-100, -200, -13000);
		assertEquals(-90, wc.getLatitudeDeg(), EPS);
		assertEquals(160, wc.getLongitudeDeg(), EPS);
		assertEquals(-13000, wc.getAltitude(), 0);
	}
	
	@Test
	public void testGetLatitude() {
		WorldCoordinate wc;
		wc = new WorldCoordinate(10, 15, 130);
		assertEquals(10, wc.getLatitudeDeg(), EPS);
		assertEquals(Math.toRadians(10), wc.getLatitudeRad(), EPS);
	}
	
	@Test
	public void testGetLongitude() {
		WorldCoordinate wc;
		wc = new WorldCoordinate(10, 15, 130);
		assertEquals(15, wc.getLongitudeDeg(), EPS);
		assertEquals(Math.toRadians(15), wc.getLongitudeRad(), EPS);
	}
	
}
