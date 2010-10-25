package net.sf.openrocket.optimization.rocketoptimization;

import static org.junit.Assert.*;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.rocketcomponent.Rocket;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class TestRocketOptimizationFunction {
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
	
	
	@Test
	public void testNormalEvaluation() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);
		
		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = -0.43;
		final double pvalue = 9.81;
		final double gvalue = 8.81;
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation); will(returnValue(ddist));
				oneOf(parameter).computeValue(simulation); will(returnValue(pvalue));
				oneOf(goal).getMinimizationParameter(pvalue); will(returnValue(gvalue));
		}});
		// @formatter:on
		

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		

		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		double value = function.evaluate(new Point(p1, p2));
		assertEquals(gvalue, value, 0);
		
		assertEquals(pvalue, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		// Re-evaluate the point to verify parameter is not recomputed
		value = function.evaluate(new Point(p1, p2));
		assertEquals(gvalue, value, 0);
	}
	
	
	@Test
	public void testNaNValue() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);
		
		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = -0.43;
		final double pvalue = 9.81;
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation); will(returnValue(ddist));
				oneOf(parameter).computeValue(simulation); will(returnValue(pvalue));
				oneOf(goal).getMinimizationParameter(pvalue); will(returnValue(Double.NaN));
		}});
		// @formatter:on
		

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		

		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);
		
		assertEquals(pvalue, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);
	}
	
	
	@Test
	public void testOutsideDomain() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);
		
		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = 0.98;
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation); will(returnValue(ddist));
		}});
		// @formatter:on
		

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		

		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		double value = function.evaluate(new Point(p1, p2));
		assertTrue(value > 1e100);
		
		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		value = function.evaluate(new Point(p1, p2));
		assertTrue(value > 1e100);
	}
	
	@Test
	public void testOutsideDomain2() throws InterruptedException, OptimizationException {
		final Rocket rocket = new Rocket();
		final Simulation simulation = new Simulation(rocket);
		
		final double p1 = 0.4;
		final double p2 = 0.7;
		final double ddist = Double.NaN;
		
		// @formatter:off
		context.checking(new Expectations() {{
				oneOf(modifier1).modify(simulation, p1);
				oneOf(modifier2).modify(simulation, p2);
				oneOf(domain).getDistanceToDomain(simulation); will(returnValue(ddist));
		}});
		// @formatter:on
		

		RocketOptimizationFunction function = new RocketOptimizationFunction(simulation,
				parameter, goal, domain, modifier1, modifier2) {
			@Override
			Simulation newSimulationInstance(Simulation sim) {
				return sim;
			}
		};
		

		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		double value = function.evaluate(new Point(p1, p2));
		assertEquals(Double.MAX_VALUE, value, 0);
		
		assertEquals(Double.NaN, function.getComputedParameterValue(new Point(p1, p2)), 0);
		
		value = function.evaluate(new Point(p1, p2));
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
