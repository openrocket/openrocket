package info.openrocket.core.models.atmosphere;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import info.openrocket.core.util.ModID;
import static org.junit.jupiter.api.Assertions.*;


public class ExtendedISAModelTest {
	private static final double ISA_GRAVITY = 9.80665;
	private ExtendedISAModel standardModel;
	private ExtendedISAModel customModel;
	private ExtendedISAModel altitudeModel;
	private ExtendedISAModel interpolatedAltitudeModel;

	private static class LinearHumidityInterpolatingModel extends InterpolatingAtmosphericModel {
		@Override
		protected double getMaxAltitude() {
			return 1000;
		}

		@Override
		protected AtmosphericConditions getExactConditions(double altitude) {
			double relativeHumidity = Math.max(0, Math.min(1, altitude / 1000.0));
			return new AtmosphericConditions(288.15, 101325.0, relativeHumidity);
		}

		@Override
		public ModID getModID() {
			return ModID.ZERO;
		}
	}

	@BeforeEach
	void setUp() {
		standardModel = new ExtendedISAModel();
		customModel = new ExtendedISAModel(278.15, 100000.0, 0); // Custom conditions
		// Create model for a launch site at 1000m with specific conditions
		altitudeModel = new ExtendedISAModel(1000.0, 281.15, 89876.0, 0);
		// Create model for a launch site at 2750m (between 2500m and 3000m layers)
		interpolatedAltitudeModel = new ExtendedISAModel(2750.0, 271.15, 72500.0, 0);
	}

	@Test
	@DisplayName("Standard ISA model should return correct sea level conditions")
	void testStandardSeaLevel() {
		AtmosphericConditions conditions = standardModel.getConditions(0);
		assertEquals(288.15, conditions.getTemperature(), 0.01);
		assertEquals(101325.0, conditions.getPressure(), 0.01);
	}

	@ParameterizedTest
	@ValueSource(doubles = {-100.0, -1.0, 0.0}) // Test negative and zero altitudes
	@DisplayName("Model should handle zero and negative altitudes gracefully")
	void testNegativeAltitudes(double altitude) {
		AtmosphericConditions conditions = standardModel.getConditions(altitude);
		// Should return sea level conditions
		assertEquals(288.15, conditions.getTemperature(), 0.01);
		assertEquals(101325.0, conditions.getPressure(), 0.01);
	}

	@Test
	@DisplayName("Custom model should return specified conditions at sea level")
	void testCustomModelSeaLevel() {
		AtmosphericConditions conditions = customModel.getConditions(0);
		assertEquals(278.15, conditions.getTemperature(), 0.01);
		assertEquals(100000.0, conditions.getPressure(), 0.01);
	}

	@Test
	@DisplayName("Temperature should decrease with altitude in troposphere")
	void testTemperatureGradientInTroposphere() {
		AtmosphericConditions cond1 = standardModel.getConditions(0);
		AtmosphericConditions cond2 = standardModel.getConditions(5000);
		AtmosphericConditions cond3 = standardModel.getConditions(10000);

		assertTrue(cond1.getTemperature() > cond2.getTemperature());
		assertTrue(cond2.getTemperature() > cond3.getTemperature());
	}

	@Test
	@DisplayName("Pressure should decrease with altitude")
	void testPressureGradient() {
		AtmosphericConditions cond1 = standardModel.getConditions(0);
		AtmosphericConditions cond2 = standardModel.getConditions(5000);
		AtmosphericConditions cond3 = standardModel.getConditions(10000);

		assertTrue(cond1.getPressure() > cond2.getPressure());
		assertTrue(cond2.getPressure() > cond3.getPressure());
	}

	@Test
	@DisplayName("Model should handle very high altitudes")
	void testHighAltitude() {
		AtmosphericConditions conditions = standardModel.getConditions(80000);
		// Mainly checking that it doesn't throw exceptions
		assertTrue(conditions.getTemperature() > 0);
		assertTrue(conditions.getPressure() > 0);
	}

	@Test
	@DisplayName("Should respect launch site conditions at specified altitude")
	void testLaunchSiteConditions() {
		AtmosphericConditions conditions = altitudeModel.getConditions(1000.0);
		assertEquals(281.15, conditions.getTemperature(), 0.01);
		assertEquals(89876.0, conditions.getPressure(), 0.01);
	}

	@Test
	@DisplayName("Should return extrapolated launch site conditions for altitudes below launch site")
	void testBelowLaunchSiteAltitude() {
		AtmosphericConditions conditions500 = altitudeModel.getConditions(500.0);
		AtmosphericConditions conditions0 = altitudeModel.getConditions(0.0);

		double launchTemperature = 281.15;
		double launchPressure = 89876.0;
		double geopotentialLaunch = 999.8427120469674;
		double layer1Alt = 11000.0;
		double layer1Temp = 216.65;
		double tempRate = (layer1Temp - launchTemperature) / (layer1Alt - geopotentialLaunch);
		double seaLevelTemp = launchTemperature - tempRate * geopotentialLaunch;
		double seaLevelPressure = calculatePressure(0, seaLevelTemp, geopotentialLaunch, launchTemperature, launchPressure);

		double geopotential500 = 499.9606749190611;
		double expectedTemp500 = seaLevelTemp + tempRate * geopotential500;
		double expectedPressure500 = calculatePressure(geopotential500, expectedTemp500, 0, seaLevelTemp, seaLevelPressure);

		// Should return launch site conditions for any altitude below launch site
		assertEquals(expectedTemp500, conditions500.getTemperature(), 0.01);
		assertEquals(expectedPressure500, conditions500.getPressure(), 0.01);
		assertEquals(seaLevelTemp, conditions0.getTemperature(), 0.01);
		assertEquals(seaLevelPressure, conditions0.getPressure(), 0.01);
	}

	@Test
	@DisplayName("Should calculate correct conditions above launch site")
	void testAboveLaunchSiteAltitude() {
		AtmosphericConditions conditions2000 = altitudeModel.getConditions(2000.0);
		AtmosphericConditions conditions3000 = altitudeModel.getConditions(3000.0);

		// Temperature and pressure should decrease with altitude
		assertTrue(conditions2000.getTemperature() < 281.15);
		assertTrue(conditions2000.getPressure() < 89876.0);
		assertTrue(conditions3000.getTemperature() < conditions2000.getTemperature());
		assertTrue(conditions3000.getPressure() < conditions2000.getPressure());
	}

	@Test
	@DisplayName("Should handle edge cases in launch site parameters")
	void testEdgeCases() {
		// Test negative altitude
		assertDoesNotThrow(() ->
				new ExtendedISAModel(-100.0, 288.15, 101325.0, 0.0)
		);

		// Test zero pressure
		assertThrows(IllegalArgumentException.class, () ->
				new ExtendedISAModel(1000.0, 288.15, 0.0, 0.0)
		);

		// Test zero temperature
		assertThrows(IllegalArgumentException.class, () ->
				new ExtendedISAModel(1000.0, 0.0, 101325.0, 0.0)
		);

		// Test negative pressure
		assertThrows(IllegalArgumentException.class, () ->
				new ExtendedISAModel(1000.0, 288.15, -1000.0, 0.0)
		);

		// Test negative temperature
		assertThrows(IllegalArgumentException.class, () ->
				new ExtendedISAModel(1000.0, -273.15, 101325.0, 0.0)
		);
	}

	@Test
	@DisplayName("Should throw exception for launch sites above 11km")
	void testTooHighLaunchSite() {
		assertThrows(IllegalArgumentException.class, () ->
				new ExtendedISAModel(12000.0, 220.0, 25000.0, 0.0)
		);
	}

	@Test
	@DisplayName("Should handle launch site between interpolation layers")
	void testInterpolatedLaunchSite() {
		// Test conditions at launch site (2750m)
		AtmosphericConditions conditions = interpolatedAltitudeModel.getConditions(2750.0);
		assertEquals(271.15, conditions.getTemperature(), 0.01);
		assertEquals(72500.0, conditions.getPressure(), 50);

		// Test conditions at interpolation layer boundaries
		AtmosphericConditions lowerConditions = interpolatedAltitudeModel.getConditions(2600.0);
		AtmosphericConditions upperConditions = interpolatedAltitudeModel.getConditions(2900.0);

		// Verify conditions at launch site are between layer boundary conditions
		assertTrue(lowerConditions.getTemperature() >= conditions.getTemperature());
		assertTrue(upperConditions.getTemperature() <= conditions.getTemperature());
		assertTrue(lowerConditions.getPressure() >= conditions.getPressure());
		assertTrue(upperConditions.getPressure() <= conditions.getPressure());
	}

	@Test
	@DisplayName("Should properly interpolate around non-standard launch altitude")
	void testInterpolationAroundLaunchSite() {
		// Test points around the 2750m launch site (temperature should decrease with altitude in the troposphere)
		double[] testAltitudes = {2600.0, 2700.0, 2750.0, 2800.0, 2900.0};
		AtmosphericConditions[] conditions = new AtmosphericConditions[testAltitudes.length];

		// Get conditions at each test altitude
		for (int i = 0; i < testAltitudes.length; i++) {
			conditions[i] = interpolatedAltitudeModel.getConditions(testAltitudes[i]);
		}

		// Verify monotonic decrease in temperature and pressure
		for (int i = 1; i < conditions.length; i++) {
			assertTrue(conditions[i].getTemperature() <= conditions[i-1].getTemperature(),
					"Temperature should decrease or remain constant with altitude");
			assertTrue(conditions[i].getPressure() <= conditions[i-1].getPressure(),
					"Pressure should decrease or remain constant with altitude");
		}

		// Check for smooth transitions (no sudden jumps)
		for (int i = 1; i < conditions.length - 1; i++) {
			double currTemp = conditions[i].getTemperature();
			double prevTemp = conditions[i-1].getTemperature();
			double nextTemp = conditions[i+1].getTemperature();

			// Differences between consecutive points should be similar (within 20%)
			assertTrue(currTemp < prevTemp, "Temperature should decrease with altitude in the troposphere");
			assertTrue(nextTemp < currTemp, "Temperature should decrease with altitude in the troposphere");
		}
	}

	@ParameterizedTest
	@CsvSource({
			"1000.0, 281.15, 89876.0",   // Mountain launch site
			"500.0, 285.15, 95461.0",    // Hill launch site
			"2000.0, 275.15, 79501.0"    // High altitude launch site
	})
	@DisplayName("Should handle various launch site conditions correctly")
	void testVariousLaunchSites(double altitude, double temperature, double pressure) {
		ExtendedISAModel model = new ExtendedISAModel(altitude, temperature, pressure, 0);
		AtmosphericConditions conditions = model.getConditions(altitude);

		assertEquals(temperature, conditions.getTemperature(), 0.01);
		assertEquals(pressure, conditions.getPressure(), 0.01);

		// Test that conditions change appropriately above launch site
		AtmosphericConditions higherConditions = model.getConditions(altitude + 1000);
		assertTrue(higherConditions.getTemperature() < temperature);
		assertTrue(higherConditions.getPressure() < pressure);
	}

	@Test
	@DisplayName("Should maintain realistic atmosphere model above launch site")
	void testAtmosphericModelContinuity() {
		// Create two measurement points above launch site
		AtmosphericConditions conditions2000 = altitudeModel.getConditions(2000.0);
		AtmosphericConditions conditions2500 = altitudeModel.getConditions(2500.0);

		// Calculate approximate lapse rate (temperature change with altitude)
		double lapseRate = (conditions2500.getTemperature() - conditions2000.getTemperature()) / 500.0;

		// Standard tropospheric lapse rate is approximately -0.0065 K/m
		// Check if our model produces reasonable results
		assertTrue(lapseRate < 0, "Temperature should decrease with altitude");
		assertTrue(lapseRate > -0.01, "Temperature shouldn't decrease too rapidly");

		// Check pressure ratio is reasonable
		double pressureRatio = conditions2500.getPressure() / conditions2000.getPressure();
		assertTrue(pressureRatio < 1.0, "Pressure should decrease with altitude");
		assertTrue(pressureRatio > 0.9, "Pressure shouldn't decrease too rapidly over 500m");
	}

	@Test
	@DisplayName("Model should return provided non-zero humidity at sea level")
	void testNonZeroHumidityAtSeaLevel() {
		ExtendedISAModel humidModel = new ExtendedISAModel(288.15, 101325.0, 0.65);
		AtmosphericConditions conditions = humidModel.getConditions(0);
		assertEquals(0.65, conditions.getRelativeHumidity(), 1e-12);
	}

	@Test
	@DisplayName("Humidity should affect derived properties (gas constant and density)")
	void testHumidityAffectsDerivedProperties() {
		ExtendedISAModel humidModel = new ExtendedISAModel(288.15, 101325.0, 0.80);

		AtmosphericConditions dry = standardModel.getConditions(0);
		AtmosphericConditions humid = humidModel.getConditions(0);

		assertEquals(dry.getTemperature(), humid.getTemperature(), 1e-9);
		assertEquals(dry.getPressure(), humid.getPressure(), 1e-6);
		assertTrue(humid.getGasConstant() > dry.getGasConstant(), "Humid air should have higher gas constant");
		assertTrue(humid.getDensity() < dry.getDensity(), "Humid air should have lower density at same P/T");
	}

	@Test
	@DisplayName("Should validate relative humidity bounds in constructor")
	void testRelativeHumidityBounds() {
		assertThrows(IllegalArgumentException.class, () -> new ExtendedISAModel(288.15, 101325.0, -0.01));
		assertThrows(IllegalArgumentException.class, () -> new ExtendedISAModel(288.15, 101325.0, 1.01));
		assertDoesNotThrow(() -> new ExtendedISAModel(288.15, 101325.0, 0.0));
		assertDoesNotThrow(() -> new ExtendedISAModel(288.15, 101325.0, 1.0));
	}

	@Test
	@DisplayName("InterpolatingAtmosphericModel should interpolate relative humidity")
	void testInterpolatedRelativeHumidity() {
		InterpolatingAtmosphericModel model = new LinearHumidityInterpolatingModel();

		assertEquals(0.0, model.getConditions(0).getRelativeHumidity(), 1e-12);
		assertEquals(0.25, model.getConditions(250).getRelativeHumidity(), 1e-12);
		assertEquals(0.5, model.getConditions(500).getRelativeHumidity(), 1e-12);
	}

	@Test
	@DisplayName("Geometric altitude should be converted to geopotential for ISA temperature")
	void testGeopotentialConversionForTemperature() {
		double geometricAltitude = 32000.0;
		double geopotentialAltitude = 31839.71865615363;
		double expectedTemp = expectedISATemperature(geopotentialAltitude);

		AtmosphericConditions conditions = standardModel.getConditions(geometricAltitude);
		assertEquals(expectedTemp, conditions.getTemperature(), 0.03);
	}

	@Test
	@DisplayName("Maximum allowed altitude should be based on geometric altitude")
	void testMaximumAllowedAltitudeUsesGeometric() {
		double expected = 11018.064362274883;
		assertEquals(expected, ExtendedISAModel.getMaximumAllowedAltitude(), 1e-6);
	}

	private static double expectedISATemperature(double geopotentialAltitude) {
		if (geopotentialAltitude <= 11000.0) {
			return 288.15 - 0.0065 * geopotentialAltitude;
		}
		if (geopotentialAltitude <= 20000.0) {
			return 216.65;
		}
		if (geopotentialAltitude <= 32000.0) {
			return 216.65 + 0.001 * (geopotentialAltitude - 20000.0);
		}
		if (geopotentialAltitude <= 47000.0) {
			return 228.65 + 0.0028 * (geopotentialAltitude - 32000.0);
		}
		if (geopotentialAltitude <= 51000.0) {
			return 270.65;
		}
		if (geopotentialAltitude <= 71000.0) {
			return 270.65 - 0.0028 * (geopotentialAltitude - 51000.0);
		}
		if (geopotentialAltitude <= 84852.0) {
			return 214.65 - 0.0020 * (geopotentialAltitude - 71000.0);
		}
		return 186.95;
	}

	private static double calculatePressure(double alt1, double temp1, double alt2, double temp2, double press2) {
		double tempRate = (temp2 - temp1) / (alt2 - alt1);
		if (Math.abs(tempRate) > 0.000001) {
			return press2 / Math.pow(1 + (alt2 - alt1) * tempRate / temp1, -ISA_GRAVITY / (tempRate * AtmosphericConditions.R));
		}
		return press2 / Math.exp(-(alt2 - alt1) * ISA_GRAVITY / (AtmosphericConditions.R * temp1));
	}
}
