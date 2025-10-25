package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CAParameterSweepTest extends ComponentAnalysisTestBase {

	private static final double EPSILON = 1e-9;

	@Mock
	private AerodynamicCalculator aerodynamicCalculator;

	private Rocket rocket;
	private CAParameters parameters;
	private FinSet finSet;

	@BeforeEach
	void setUp() {
		rocket = createRocket();
		parameters = new CAParameters(rocket, 0.15);
		parameters.setAOA(0.2);
		parameters.setMach(0.3);
		parameters.setRollRate(0.4);

		finSet = new TrapezoidFinSet();
	}

	@Test
	void sweepCollectsDomainAndComponentDataAcrossMultipleSteps() {
		CAParameterSweep sweep = new CAParameterSweep(parameters, aerodynamicCalculator, rocket);

		Map<RocketComponent, AerodynamicForces> firstSweep = new LinkedHashMap<>();
		firstSweep.put(rocket, createRocketForces(rocket, 1.2, 4.0, 0.11, 0.02, 0.31, 0.4));
		firstSweep.put(finSet, createFinSetForces(finSet, 0.6, 2.5, 0.05, 0.04, 0.03, 0.07, 0.5, -0.1));

		Map<RocketComponent, AerodynamicForces> secondSweep = new LinkedHashMap<>();
		secondSweep.put(rocket, createRocketForces(rocket, 1.4, 5.5, 0.12, 0.03, 0.29, 0.45));
		secondSweep.put(finSet, createFinSetForces(finSet, 0.7, 3.0, 0.06, 0.05, 0.02, 0.09, 0.6, -0.15));

		when(aerodynamicCalculator.getForceAnalysis(any(), any(), any()))
				.thenReturn(firstSweep, secondSweep);

		double initialMach = parameters.getMach();
		CADataBranch branch = sweep.sweep(CADomainDataType.MACH, 0.5, 0.6, 0.1, initialMach);

		List<Double> machDomain = branch.get(CADomainDataType.MACH);
		assertNotNull(machDomain);
		assertEquals(List.of(0.5, 0.6), machDomain);

		assertEquals(1.2, branch.getByIndex(CADataType.CP_X, rocket, 0), EPSILON);
		assertEquals(4.0, branch.getByIndex(CADataType.CNa, rocket, 0), EPSILON);
		assertEquals(0.4, branch.getByIndex(CADataType.PER_INSTANCE_CD, rocket, 0), EPSILON);
		assertEquals(1.4, branch.getByIndex(CADataType.CP_X, rocket, 1), EPSILON);
		assertEquals(5.5, branch.getByIndex(CADataType.CNa, rocket, 1), EPSILON);
		assertEquals(0.45, branch.getByIndex(CADataType.PER_INSTANCE_CD, rocket, 1), EPSILON);

		assertEquals(0.6, branch.getByIndex(CADataType.CP_X, finSet, 0), EPSILON);
		assertEquals(2.5, branch.getByIndex(CADataType.CNa, finSet, 0), EPSILON);
		assertEquals(0.07, branch.getByIndex(CADataType.PER_INSTANCE_CD, finSet, 0), EPSILON);
		assertEquals(0.7, branch.getByIndex(CADataType.CP_X, finSet, 1), EPSILON);
		assertEquals(3.0, branch.getByIndex(CADataType.CNa, finSet, 1), EPSILON);
		assertEquals(0.09, branch.getByIndex(CADataType.PER_INSTANCE_CD, finSet, 1), EPSILON);

		assertEquals(0.5, branch.getByIndex(CADataType.ROLL_FORCING_COEFFICIENT, rocket, 0), EPSILON);
		assertEquals(-0.1, branch.getByIndex(CADataType.ROLL_DAMPING_COEFFICIENT, rocket, 0), EPSILON);
		assertEquals(0.4, branch.getByIndex(CADataType.TOTAL_ROLL_COEFFICIENT, rocket, 0), EPSILON);
		assertEquals(0.6, branch.getByIndex(CADataType.ROLL_FORCING_COEFFICIENT, rocket, 1), EPSILON);
		assertEquals(-0.15, branch.getByIndex(CADataType.ROLL_DAMPING_COEFFICIENT, rocket, 1), EPSILON);
		assertEquals(0.45, branch.getByIndex(CADataType.TOTAL_ROLL_COEFFICIENT, rocket, 1), EPSILON);

		assertEquals(initialMach, parameters.getMach(), EPSILON);

		ArgumentCaptor<FlightConditions> conditionsCaptor = ArgumentCaptor.forClass(FlightConditions.class);
		verify(aerodynamicCalculator, times(2))
				.getForceAnalysis(any(), conditionsCaptor.capture(), any(WarningSet.class));

		List<FlightConditions> captured = conditionsCaptor.getAllValues();
		assertEquals(2, captured.size());
		assertEquals(0.5, captured.get(0).getMach(), EPSILON);
		assertEquals(0.6, captured.get(1).getMach(), EPSILON);
		assertEquals(0.2, captured.get(0).getAOA(), EPSILON);
		assertEquals(0.2, captured.get(1).getAOA(), EPSILON);
		assertEquals(0.15, captured.get(0).getTheta(), EPSILON);
		assertEquals(0.15, captured.get(1).getTheta(), EPSILON);
		assertEquals(0.4, captured.get(0).getRollRate(), EPSILON);
		assertEquals(0.4, captured.get(1).getRollRate(), EPSILON);

		assertEquals(2, branch.getLength());
	}

	private AerodynamicForces createRocketForces(RocketComponent component, double cpX, double cna, double pressureCd,
			double baseCd, double frictionCd, double perInstanceCd) {
		AerodynamicForces forces = new AerodynamicForces();
		forces.setComponent(component);
		forces.setCP(new Coordinate(cpX, 0, 0, cna));
		forces.setPressureCD(pressureCd);
		forces.setBaseCD(baseCd);
		forces.setFrictionCD(frictionCd);
		forces.setCD(perInstanceCd);
		return forces;
	}

	private AerodynamicForces createFinSetForces(RocketComponent component, double cpX, double cna, double pressureCd,
			double baseCd, double frictionCd, double perInstanceCd, double rollForce, double rollDamp) {
		AerodynamicForces forces = createRocketForces(component, cpX, cna, pressureCd, baseCd, frictionCd, perInstanceCd);
		forces.setCrollForce(rollForce);
		forces.setCrollDamp(rollDamp);
		return forces;
	}
}
