package info.openrocket.core.optimization.rocketoptimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.optimization.general.OptimizationException;
import info.openrocket.core.optimization.general.Point;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.unit.Value;
import info.openrocket.core.util.Pair;
import info.openrocket.core.util.BaseTestCase;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestRocketOptimizationFunction extends BaseTestCase {

	@Mock
	OptimizableParameter parameter;
	@Mock
	OptimizationGoal goal;
	@Mock
	SimulationDomain domain;
	@Mock
	SimulationModifier modifier1;
	@Mock
	SimulationModifier modifier2;
	@Mock
	RocketOptimizationListener listener;

	@Test
	public void testNormalEvaluation() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);

		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = -0.43;
		final Value dref = new Value(ddist, Unit.NOUNIT);
		final double pvalue = 9.81;
		final Value pvalueValue = new Value(9.81, Unit.NOUNIT);
		final double gvalue = 8.81;
		final Point point = new Point(p1, p2);

		// Setup stubbing for methods that return values
		when(domain.getDistanceToDomain(simulation)).thenReturn(new Pair<Double, Value>(ddist, dref));
		when(parameter.computeValue(simulation)).thenReturn(pvalue);
		when(parameter.getUnitGroup()).thenReturn(UnitGroup.UNITS_NONE);
		when(goal.getMinimizationParameter(pvalue)).thenReturn(gvalue);
		when(modifier1.getCurrentSIValue(simulation)).thenReturn(0.2);
		when(modifier1.getUnitGroup()).thenReturn(UnitGroup.UNITS_LENGTH);
		when(modifier2.getCurrentSIValue(simulation)).thenReturn(0.3);
		when(modifier2.getUnitGroup()).thenReturn(UnitGroup.UNITS_LENGTH);

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		function.addRocketOptimizationListener(listener);

		double value = function.evaluate(point);
		assertEquals(gvalue, value, 0);

		// Verify the interactions
		verify(modifier1, times(1)).modify(simulation, p1);
		verify(modifier2, times(1)).modify(simulation, p2);
		verify(domain, times(1)).getDistanceToDomain(simulation);
		verify(parameter, times(1)).computeValue(simulation);
		verify(parameter, times(1)).getUnitGroup();
		verify(goal, times(1)).getMinimizationParameter(pvalue);
		verify(modifier1, times(1)).getCurrentSIValue(simulation);
		verify(modifier1, times(1)).getUnitGroup();
		verify(modifier2, times(1)).getCurrentSIValue(simulation);
		verify(modifier2, times(1)).getUnitGroup();
		verify(listener, times(1)).evaluated(eq(point), argThat(new ArgumentMatcher<Value[]>() {
			@Override
			public boolean matches(Value[] argument) {
				// Customize this as necessary to match the expected Value[] array
				return argument[0].getValue() == 0.2 && argument[1].getValue() == 0.3;
			}
		}), eq(dref), eq(pvalueValue), eq(gvalue));

	}

	@Test
	public void testNaNValue() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);

		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = -0.43;
		final Value dref = new Value(0.33, Unit.NOUNIT);
		final double pvalue = 9.81;

		// Setup stubbing for methods that return values
		when(domain.getDistanceToDomain(simulation)).thenReturn(new Pair<>(ddist, dref));
		when(parameter.computeValue(simulation)).thenReturn(pvalue);
		when(parameter.getUnitGroup()).thenReturn(UnitGroup.UNITS_NONE);
		when(goal.getMinimizationParameter(pvalue)).thenReturn(Double.NaN);

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};

		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);

		// Verify that the methods have been called with the specified parameters
		verify(modifier1).modify(simulation, p1);
		verify(modifier2).modify(simulation, p2);
		verify(domain).getDistanceToDomain(simulation);
		verify(parameter).computeValue(simulation);
		verify(parameter).getUnitGroup();
		verify(goal).getMinimizationParameter(pvalue);
	}

	@Test
	public void testOutsideDomain() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);

		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = 0.98;
		final Value dref = new Value(ddist, Unit.NOUNIT);
		final Point point = new Point(p1, p2);

		// Setup stubbing for methods that return values
		when(domain.getDistanceToDomain(simulation)).thenReturn(new Pair<>(ddist, dref));
		when(modifier1.getCurrentSIValue(simulation)).thenReturn(0.2);
		when(modifier1.getUnitGroup()).thenReturn(UnitGroup.UNITS_LENGTH);
		when(modifier2.getCurrentSIValue(simulation)).thenReturn(0.3);
		when(modifier2.getUnitGroup()).thenReturn(UnitGroup.UNITS_LENGTH);

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		function.addRocketOptimizationListener(listener);

		double value = function.evaluate(new Point(p1, p2));
		assertTrue(value > 1e100);

		// Verify the interactions
		verify(modifier1).modify(simulation, p1);
		verify(modifier2).modify(simulation, p2);
		verify(domain).getDistanceToDomain(simulation);
		verify(modifier1).getCurrentSIValue(simulation);
		verify(modifier1).getUnitGroup();
		verify(modifier2).getCurrentSIValue(simulation);
		verify(modifier2).getUnitGroup();

		// For verifying the call to listener.evaluated with complex arguments
		verify(listener).evaluated(eq(point), argThat(new ArgumentMatcher<Value[]>() {
			@Override
			public boolean matches(Value[] argument) {
				// Adjust the logic here based on how specific you need to be about the values
				return argument != null && argument.length == 2
						&& argument[0].getValue() == 0.2 && argument[1].getValue() == 0.3
						&& argument[0].getUnit() == UnitGroup.UNITS_LENGTH.getDefaultUnit()
						&& argument[1].getUnit() == UnitGroup.UNITS_LENGTH.getDefaultUnit();
			}
		}), eq(dref), eq(null), eq(1.98E200));
	}

	@Test
	public void testOutsideDomain2() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);

		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = Double.NaN;
		final Value dref = new Value(0.33, Unit.NOUNIT);

		// Stubbing to return a specific value
		when(domain.getDistanceToDomain(simulation)).thenReturn(new Pair<>(ddist, dref));

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};

		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);

		// Verify the interactions
		verify(modifier1).modify(simulation, p1);
		verify(modifier2).modify(simulation, p2);
		verify(domain).getDistanceToDomain(simulation);
	}

	@Test
	public void testNewSimulationNames() {
		final Rocket rocket = new Rocket();
		rocket.setName("Foobar");
		final Simulation simulation = new Simulation(rocket);
		simulation.setName("MySim");

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2);

		Simulation sim = function.newSimulationInstance(simulation);
		assertFalse(simulation == sim);
		assertEquals(sim.getName(), "MySim");
		assertFalse(rocket == sim.getRocket());
		assertEquals(sim.getRocket().getName(), "Foobar");
	}

}
