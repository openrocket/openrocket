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
}
