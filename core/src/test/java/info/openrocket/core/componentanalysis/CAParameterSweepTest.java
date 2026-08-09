package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.TestRockets;
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
	private AxialStage stage;
	private FinSet finSet;

	@BeforeEach
	void setUp() {
		rocket = createRocket();
		parameters = new CAParameters(rocket, 0.15);
		parameters.setAOA(0.2);
		parameters.setMach(0.3);
		parameters.setRollRate(0.4);

		stage = new AxialStage();
		finSet = new TrapezoidFinSet();
	}

	@Test
	void sweepCollectsDomainAndComponentDataAcrossMultipleSteps() {
		CAParameterSweep sweep = new CAParameterSweep(parameters, aerodynamicCalculator, rocket);

		Map<RocketComponent, AerodynamicForces> firstSweep = new LinkedHashMap<>();
		firstSweep.put(rocket, createRocketForces(rocket, 1.2, 4.0, 0.11, 0.02, 0.31, 0.4));
		firstSweep.put(stage, createFinSetForces(stage, 1.0, 3.5, 0.09, 0.01, 0.20, 0.3, 0.4, -0.08));
		firstSweep.put(finSet, createFinSetForces(finSet, 0.6, 2.5, 0.05, 0.04, 0.03, 0.07, 0.5, -0.1));

		Map<RocketComponent, AerodynamicForces> secondSweep = new LinkedHashMap<>();
		secondSweep.put(rocket, createRocketForces(rocket, 1.4, 5.5, 0.12, 0.03, 0.29, 0.45));
		secondSweep.put(stage, createFinSetForces(stage, 1.1, 4.7, 0.10, 0.02, 0.21, 0.35, 0.5, -0.1));
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

		assertEquals(1.0, branch.getByIndex(CADataType.CP_X, stage, 0), EPSILON);
		assertEquals(3.5, branch.getByIndex(CADataType.CNa, stage, 0), EPSILON);
		assertEquals(0.32, branch.getByIndex(CADataType.TOTAL_ROLL_COEFFICIENT, stage, 0), EPSILON);
		assertEquals(1.1, branch.getByIndex(CADataType.CP_X, stage, 1), EPSILON);
		assertEquals(4.7, branch.getByIndex(CADataType.CNa, stage, 1), EPSILON);
		assertEquals(0.4, branch.getByIndex(CADataType.TOTAL_ROLL_COEFFICIENT, stage, 1), EPSILON);

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

	@Test
	void sweepWithAFineStepKeepsTheDomainValuesMonotonic() {
		CAParameterSweep sweep = new CAParameterSweep(parameters, aerodynamicCalculator, rocket);

		when(aerodynamicCalculator.getForceAnalysis(any(), any(), any()))
				.thenReturn(new LinkedHashMap<>());

		// The smallest wind direction step the GUI allows.  Its plain string form has
		// enough decimals that scaling the domain values by 10^decimals used to run
		// past the range of a long.
		double delta = Math.PI / 1800;
		CADataBranch branch = sweep.sweep(CADomainDataType.WIND_DIRECTION, 6.0, 2 * Math.PI, delta, 0.15);

		List<Double> direction = branch.get(CADomainDataType.WIND_DIRECTION);
		assertEquals(branch.getLength(), direction.size());
		for (int i = 1; i < direction.size(); i++) {
			assertEquals(delta, direction.get(i) - direction.get(i - 1), 1e-6,
					"domain values are not evenly spaced at index " + i);
		}
	}

	@Test
	void sweepStoresCalculatedStageAndRocketTotals() {
		Rocket actualRocket = TestRockets.makeEstesAlphaIII();
		AxialStage actualStage = actualRocket.getStage(0);
		CAParameters actualParameters = new CAParameters(actualRocket, 0);
		actualParameters.setRollRate(2.0);
		CAParameterSweep sweep = new CAParameterSweep(actualParameters, new BarrowmanCalculator(), actualRocket);

		CADataBranch branch = sweep.sweep(CADomainDataType.MACH, 0.3, 0.3, 0.1, actualParameters.getMach());

		assertFalse(branch.get(CADataType.CNa, actualStage).isEmpty());
		assertEquals(branch.getLast(CADataType.CP_X, actualRocket),
				branch.getLast(CADataType.CP_X, actualStage), EPSILON);
		assertEquals(branch.getLast(CADataType.CNa, actualRocket),
				branch.getLast(CADataType.CNa, actualStage), EPSILON);
		double expectedStageCD = 0;
		for (RocketComponent component : actualRocket.getSelectedConfiguration().getAllActiveComponents()) {
			if (component.isAerodynamic() && actualStage.isAncestor(component)) {
				expectedStageCD += branch.getLast(CADataType.TOTAL_CD, component);
			}
		}
		assertTrue(expectedStageCD > 0);
		assertEquals(expectedStageCD, branch.getLast(CADataType.TOTAL_CD, actualStage), EPSILON);
		assertEquals(branch.getLast(CADataType.TOTAL_ROLL_COEFFICIENT, actualRocket),
				branch.getLast(CADataType.TOTAL_ROLL_COEFFICIENT, actualStage), EPSILON);
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
