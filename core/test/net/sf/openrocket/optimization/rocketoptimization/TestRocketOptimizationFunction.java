package net.sf.openrocket.optimization.rocketoptimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class TestRocketOptimizationFunction extends BaseTestCase {
	Mockery context = new JUnit4Mockery();
	
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
		
		// @formatter:off
		context.checking(new Expectations() {
			{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation);
				will(returnValue(new Pair<Double, Value>(ddist, dref)));
				oneOf(parameter).computeValue(simulation);
				will(returnValue(pvalue));
				oneOf(parameter).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_NONE));
				oneOf(goal).getMinimizationParameter(pvalue);
				will(returnValue(gvalue));
				oneOf(modifier1).getCurrentSIValue(simulation);
				will(returnValue(0.2));
				oneOf(modifier1).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_LENGTH));
				oneOf(modifier2).getCurrentSIValue(simulation);
				will(returnValue(0.3));
				oneOf(modifier2).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_LENGTH));
				oneOf(listener).evaluated(point, new Value[] {
						new Value(0.2, UnitGroup.UNITS_LENGTH.getDefaultUnit()),
						new Value(0.3, UnitGroup.UNITS_LENGTH.getDefaultUnit())
				}, dref, pvalueValue, gvalue);
			}
		});
		// @formatter:on
		
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
		
		// @formatter:off
		context.checking(new Expectations() {
			{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation);
				will(returnValue(new Pair<Double, Value>(ddist, dref)));
				oneOf(parameter).computeValue(simulation);
				will(returnValue(pvalue));
				oneOf(parameter).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_NONE));
				oneOf(goal).getMinimizationParameter(pvalue);
				will(returnValue(Double.NaN));
			}
		});
		// @formatter:on
		
		
		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		
		
		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);
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
		
		// @formatter:off
		context.checking(new Expectations() {
			{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation);
				will(returnValue(new Pair<Double, Value>(ddist, dref)));
				oneOf(modifier1).getCurrentSIValue(simulation);
				will(returnValue(0.2));
				oneOf(modifier1).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_LENGTH));
				oneOf(modifier2).getCurrentSIValue(simulation);
				will(returnValue(0.3));
				oneOf(modifier2).getUnitGroup();
				will(returnValue(UnitGroup.UNITS_LENGTH));
				oneOf(listener).evaluated(point, new Value[] {
						new Value(0.2, UnitGroup.UNITS_LENGTH.getDefaultUnit()),
						new Value(0.3, UnitGroup.UNITS_LENGTH.getDefaultUnit())
				}, dref, null, 1.98E200);
			}
		});
		// @formatter:on
		
		
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
	}
	
	@Test
	public void testOutsideDomain2() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);
		
		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = Double.NaN;
		final Value dref = new Value(0.33, Unit.NOUNIT);
		
		// @formatter:off
		context.checking(new Expectations() {
			{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation);
				will(returnValue(new Pair<Double, Value>(ddist, dref)));
			}
		});
		// @formatter:on
		
		
		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		
		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);
	}
	
	
	@Test
	public void testNewSimulationInstance() {
		final Rocket rocket = new Rocket();
		rocket.setName("Foobar");
		final Simulation simulation = new Simulation(rocket);
		simulation.setName("MySim");
		
		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2);
		
		Simulation sim = function.newSimulationInstance(simulation);
		assertFalse(simulation == sim);
		assertEquals("MySim", sim.getName());
		assertFalse(rocket == sim.getRocket());
		assertEquals("Foobar", sim.getRocket().getName());
	}
	
}
