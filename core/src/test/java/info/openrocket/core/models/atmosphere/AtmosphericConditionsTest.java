package info.openrocket.core.models.atmosphere;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;


public class AtmosphericConditionsTest {
	private AtmosphericConditions conditions;

	@BeforeEach
	void setUp() {
		conditions = new AtmosphericConditions();  // Creates with standard conditions
	}

	@Test
	@DisplayName("Constructor should set standard conditions correctly")
	void testStandardConditions() {
		assertEquals(293.15, conditions.getTemperature(), 0.001);
		assertEquals(101325.0, conditions.getPressure(), 0.001);
	}

	@Test
	@DisplayName("Density calculation should be correct for standard conditions")
	void testDensityCalculation() {
		// rho = P/(R*T) where R = 287.053
		double expectedDensity = 101325.0 / (287.053 * 293.15);
		assertEquals(expectedDensity, conditions.getDensity(), 0.001);
	}

	@Test
	@DisplayName("Should not allow negative temperature")
	void testNegativeTemperature() {
		assertThrows(IllegalArgumentException.class, () ->
				conditions.setTemperature(-1.0)
		);
	}

	@ParameterizedTest
	@CsvSource({
			"273.15, 331.3",  // 0 degC
			"293.15, 343.2",  // 20 degC (standard)
			"313.15, 355.1"   // 40 degC
	})
	@DisplayName("Mach speed calculation should be accurate")
	void testMachSpeed(double tempK, double expectedSpeed) {
		conditions.setTemperature(tempK);
		assertEquals(expectedSpeed, conditions.getMachSpeed(), 1.0);
	}

	@Test
	@DisplayName("Gas constant should equal dry-air R when RH is zero")
	void testGasConstantDryAir() {
		AtmosphericConditions conditions = new AtmosphericConditions(288.15, 101325.0, 0.0);
		assertEquals(AtmosphericConditions.R, conditions.getGasConstant(), 0.0);
	}

	@Test
	@DisplayName("Gas constant should increase with relative humidity")
	void testGasConstantIncreasesWithHumidity() {
		AtmosphericConditions dry = new AtmosphericConditions(288.15, 101325.0, 0.0);
		AtmosphericConditions mid = new AtmosphericConditions(288.15, 101325.0, 0.5);
		AtmosphericConditions wet = new AtmosphericConditions(288.15, 101325.0, 1.0);

		assertTrue(mid.getGasConstant() > dry.getGasConstant());
		assertTrue(wet.getGasConstant() > mid.getGasConstant());
	}

	@Test
	@DisplayName("Density should decrease with relative humidity at fixed pressure and temperature")
	void testDensityDecreasesWithHumidity() {
		AtmosphericConditions dry = new AtmosphericConditions(288.15, 101325.0, 0.0);
		AtmosphericConditions wet = new AtmosphericConditions(288.15, 101325.0, 1.0);
		assertTrue(wet.getDensity() < dry.getDensity());
	}

	@Test
	@DisplayName("Saturation vapor pressure should increase with temperature")
	void testVaporPressureSaturationMonotonicWithTemperature() {
		AtmosphericConditions cold = new AtmosphericConditions(260.0, 101325.0, 0.5);
		AtmosphericConditions warm = new AtmosphericConditions(300.0, 101325.0, 0.5);

		assertTrue(cold.vaporPressureSaturation() > 0);
		assertTrue(warm.vaporPressureSaturation() > cold.vaporPressureSaturation());
	}

	@Test
	@DisplayName("Relative humidity should be restricted to [0,1]")
	void testRelativeHumidityBounds() {
		assertThrows(IllegalArgumentException.class, () -> new AtmosphericConditions(288.15, 101325.0, -0.01));
		assertThrows(IllegalArgumentException.class, () -> new AtmosphericConditions(288.15, 101325.0, 1.01));
	}
}
