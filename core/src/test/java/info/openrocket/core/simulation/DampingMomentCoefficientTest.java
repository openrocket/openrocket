package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

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

		List<Double> total = branch.getView(FlightDataType.TYPE_DAMPING_MOMENT_COEFF);
		List<Double> aerodynamic = branch.getView(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC);
		List<Double> propulsive = branch.getView(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE);

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
			assertTrue(p >= 0.0, "Propulsive damping moment coefficient should not be negative");
			foundNonZeroAerodynamic |= Math.abs(a) > NON_ZERO_THRESHOLD;
			foundNonZeroPropulsive |= Math.abs(p) > NON_ZERO_THRESHOLD;
		}

		assertTrue(foundNonZeroAerodynamic, "Expected at least one non-zero aerodynamic contribution");
		assertTrue(foundNonZeroPropulsive, "Expected at least one non-zero propulsive contribution");
	}

	/**
	 * The propulsive part is the jet damping coefficient, the sum over every motor of mdot * (x_nozzle - x_cg)^2,
	 * where x_nozzle is the absolute position of that motor's nozzle exit. Integrated over time, this equals the
	 * expelled mass of each motor times its squared lever arm. Compare the time integral of the stored values
	 * against that sum, using the motor mass curves and the nozzle exit positions from the mount geometry.
	 */
	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	public void testPropulsiveDampingMatchesExpelledMassAndNozzlePosition(boolean withMotorPods)
			throws SimulationException {
		Rocket rocket = withMotorPods ? TestRockets.makeEstesAlphaIIIWithMotorPods() : TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.001);
		simulation.getOptions().setRandomSeed(0xC0FFEE);

		simulation.simulate();

		// Every motor ignites at launch, so the motor time equals the simulation time.
		List<Motor> motors = new ArrayList<>();
		List<Double> nozzlePositions = new ArrayList<>();
		for (RocketComponent component : rocket) {
			if (!(component instanceof MotorMount mount) || !mount.isMotorMount() ||
					mount.getMotorConfig(TestRockets.TEST_FCID_0).isEmpty()) {
				continue;
			}
			CoordinateIF nozzleExit = new Coordinate(mount.getLength() + mount.getMotorOverhang());
			for (CoordinateIF location : component.toAbsolute(nozzleExit)) {
				motors.add(mount.getMotorConfig(TestRockets.TEST_FCID_0).getMotor());
				nozzlePositions.add(location.getX());
			}
		}
		assertEquals(withMotorPods ? 3 : 1, motors.size());

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		List<Double> time = branch.getView(FlightDataType.TYPE_TIME);
		List<Double> cg = branch.getView(FlightDataType.TYPE_CG_LOCATION);
		List<Double> propulsive = branch.getView(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE);

		// Integrate from launch rod clearance, before which the coefficient is not computed
		int first = 0;
		while (first < propulsive.size() && !(propulsive.get(first) > NON_ZERO_THRESHOLD)) {
			first++;
		}
		assertTrue(first < propulsive.size(), "Expected propulsive damping samples during the burn");

		double actual = 0;
		double expected = 0;
		for (int k = first; k + 1 < time.size(); k++) {
			double p0 = propulsive.get(k);
			double p1 = propulsive.get(k + 1);
			if (Double.isNaN(p0) || Double.isNaN(p1)) {
				break;
			}
			double t0 = time.get(k);
			double t1 = time.get(k + 1);
			actual += 0.5 * (p0 + p1) * (t1 - t0);
			for (int i = 0; i < motors.size(); i++) {
				double expelledMass = motors.get(i).getTotalMass(t0) - motors.get(i).getTotalMass(t1);
				double arm0 = MathUtil.pow2(nozzlePositions.get(i) - cg.get(k));
				double arm1 = MathUtil.pow2(nozzlePositions.get(i) - cg.get(k + 1));
				expected += expelledMass * 0.5 * (arm0 + arm1);
			}
		}

		assertTrue(expected > 0, "Expected mass to be expelled after launch rod clearance");
		assertEquals(expected, actual, 0.01 * expected);
	}
}
