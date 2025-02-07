package info.openrocket.core.models.atmosphere;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class InterpolatingAtmosphericModelTest {
	private ExtendedISAModel model; // Using ExtendedISAModel to test interpolation

	@BeforeEach
	void setUp() {
		model = new ExtendedISAModel();
	}

	@Test
	@DisplayName("Interpolation should be smooth between layers")
	void testSmoothInterpolation() {
		// Test points between 500m layers
		AtmosphericConditions cond1 = model.getConditions(250);
		AtmosphericConditions cond2 = model.getConditions(500);
		AtmosphericConditions cond3 = model.getConditions(750);

		// Check that values are monotonic
		assertTrue(cond1.getPressure() >= cond2.getPressure());
		assertTrue(cond2.getPressure() >= cond3.getPressure());

		// Check for no sudden jumps (testing smoothness)
		double pressureDiff1 = Math.abs(cond1.getPressure() - cond2.getPressure());
		double pressureDiff2 = Math.abs(cond2.getPressure() - cond3.getPressure());
		assertTrue(Math.abs(pressureDiff1 - pressureDiff2) < pressureDiff1 * 0.1); // Within 10%
	}
}
