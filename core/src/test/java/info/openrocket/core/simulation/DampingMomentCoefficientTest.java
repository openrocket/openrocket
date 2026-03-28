package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class DampingMomentCoefficientTest extends BaseTestCase {

	private static final double SUM_TOLERANCE = 1.0e-12;
	private static final double NON_ZERO_THRESHOLD = 1.0e-9;

	@Test
	public void testDampingMomentCoefficientTypesAreDefined() {
		assertSame(UnitGroup.UNITS_ANGULAR_MOMENTUM, FlightDataType.TYPE_DAMPING_MOMENT_COEFF.getUnitGroup());
		assertSame(UnitGroup.UNITS_ANGULAR_MOMENTUM, FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC.getUnitGroup());
		assertSame(UnitGroup.UNITS_ANGULAR_MOMENTUM, FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE.getUnitGroup());

		assertEquals("Cdm", FlightDataType.TYPE_DAMPING_MOMENT_COEFF.getSymbol());
		assertEquals("Cdm_aero", FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC.getSymbol());
		assertEquals("Cdm_prop", FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE.getSymbol());

		List<FlightDataType> allTypes = Arrays.asList(FlightDataType.ALL_TYPES);
		assertTrue(allTypes.contains(FlightDataType.TYPE_DAMPING_MOMENT_COEFF));
		assertTrue(allTypes.contains(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC));
		assertTrue(allTypes.contains(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE));

		assertFalse(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC.getName().isBlank());
		assertFalse(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE.getName().isBlank());
	}

	@ParameterizedTest
	@EnumSource(SimulationStepperMethod.class)
	public void testDampingMomentCoefficientStoredAndConsistent(SimulationStepperMethod stepperMethod)
			throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().setRandomSeed(0xC0FFEE);
		simulation.getOptions().setSimulationStepperMethodChoice(stepperMethod);

		simulation.simulate();

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		assertNotNull(branch);

		List<Double> total = branch.get(FlightDataType.TYPE_DAMPING_MOMENT_COEFF);
		List<Double> aerodynamic = branch.get(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC);
		List<Double> propulsive = branch.get(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE);

		assertNotNull(total);
		assertNotNull(aerodynamic);
		assertNotNull(propulsive);
		assertEquals(total.size(), aerodynamic.size());
		assertEquals(total.size(), propulsive.size());
		assertFalse(total.isEmpty());

		boolean foundNonZeroAerodynamic = false;
		boolean foundNonZeroPropulsive = false;

		for (int i = 0; i < total.size(); i++) {
			double t = total.get(i);
			double a = aerodynamic.get(i);
			double p = propulsive.get(i);

			if (Double.isNaN(t)) {
				assertTrue(Double.isNaN(a));
				assertTrue(Double.isNaN(p));
				continue;
			}

			assertEquals(t, a + p, SUM_TOLERANCE);
			foundNonZeroAerodynamic |= Math.abs(a) > NON_ZERO_THRESHOLD;
			foundNonZeroPropulsive |= Math.abs(p) > NON_ZERO_THRESHOLD;
		}

		assertTrue(foundNonZeroAerodynamic, "Expected at least one non-zero aerodynamic contribution");
		assertTrue(foundNonZeroPropulsive, "Expected at least one non-zero propulsive contribution");
	}
}

