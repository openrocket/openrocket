package info.openrocket.core.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BaseTestCase;

/**
 * Unit tests for {@link OpenMeteoAPI}.
 */
public class OpenMeteoAPITest extends BaseTestCase {

	private static final double EPSILON = 1e-9;
	private static final double VALID_LATITUDE = 51.5074;
	private static final double VALID_LONGITUDE = -0.1278;

	@AfterEach
	public void cleanupCache() {
		ApplicationPreferences prefs = Application.getPreferences();
		try {
			prefs.getNode("/OpenRocket/open-meteo/elevation").clear();
		} catch (Exception e) {
			// Ignore cleanup errors
		}
	}

	@Test
	public void testInvalidLatitudeTooLow() {
		CompletableFuture<Double> future = OpenMeteoAPI.getElevation(-91.0, VALID_LONGITUDE);
		assertNotNull(future);
		assertTrue(Double.isNaN(future.join()));
	}

	@Test
	public void testInvalidLatitudeTooHigh() {
		CompletableFuture<Double> future = OpenMeteoAPI.getElevation(91.0, VALID_LONGITUDE);
		assertNotNull(future);
		assertTrue(Double.isNaN(future.join()));
	}

	@Test
	public void testValidLatitudeBoundaries() {
		CompletableFuture<Double> future1 = OpenMeteoAPI.getElevation(-90.0, VALID_LONGITUDE);
		assertNotNull(future1);

		CompletableFuture<Double> future2 = OpenMeteoAPI.getElevation(90.0, VALID_LONGITUDE);
		assertNotNull(future2);
	}

	@Test
	public void testInvalidLongitudeTooLow() {
		CompletableFuture<Double> future = OpenMeteoAPI.getElevation(VALID_LATITUDE, -181.0);
		assertNotNull(future);
		assertTrue(Double.isNaN(future.join()));
	}

	@Test
	public void testInvalidLongitudeTooHigh() {
		CompletableFuture<Double> future = OpenMeteoAPI.getElevation(VALID_LATITUDE, 181.0);
		assertNotNull(future);
		assertTrue(Double.isNaN(future.join()));
	}

	@Test
	public void testValidLongitudeBoundaries() {
		CompletableFuture<Double> future1 = OpenMeteoAPI.getElevation(VALID_LATITUDE, -180.0);
		assertNotNull(future1);

		CompletableFuture<Double> future2 = OpenMeteoAPI.getElevation(VALID_LATITUDE, 180.0);
		assertNotNull(future2);
	}

	@Test
	public void testValidCoordinatesReturnsFuture() {
		CompletableFuture<Double> future = OpenMeteoAPI.getElevation(VALID_LATITUDE, VALID_LONGITUDE);
		assertNotNull(future);
	}

	@Test
	public void testCacheStorage() {
		ApplicationPreferences prefs = Application.getPreferences();
		String testKey = "test-51.5074,-0.1278";
		double testElevation = 35.0;

		prefs.getNode("/OpenRocket/open-meteo/elevation").putDouble(testKey, testElevation);

		double retrieved = prefs.getNode("/OpenRocket/open-meteo/elevation")
				.getDouble(testKey, Double.NaN);
		assertEquals(testElevation, retrieved, EPSILON);
	}
}
