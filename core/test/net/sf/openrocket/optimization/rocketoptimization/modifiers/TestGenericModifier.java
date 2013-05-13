package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import static net.sf.openrocket.util.MathUtil.EPSILON;
import static org.junit.Assert.assertEquals;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGenericModifier extends BaseTestCase {
	
	private TestValue value;
	private GenericModifier<TestValue> gm;
	private Simulation sim;
	
	@Before
	public void setup() {
		value = new TestValue();
		sim = new Simulation(new Rocket());
		
		Object related = new Object();
		
		gm = new GenericModifier<TestGenericModifier.TestValue>("Test modifier", "Description", related,
				UnitGroup.UNITS_NONE, 2.0, TestValue.class, "value") {
			@Override
			protected TestValue getModifiedObject(Simulation simulation) {
				Assert.assertTrue(simulation == sim);
				return value;
			}
		};
		
		gm.setMinValue(0.5);
		gm.setMaxValue(5.5);
	}
	
	@Test
	public void testGetCurrentValue() throws OptimizationException {
		value.d = 1.0;
		assertEquals(2.0, gm.getCurrentSIValue(sim), EPSILON);
		value.d = 2.0;
		assertEquals(4.0, gm.getCurrentSIValue(sim), EPSILON);
	}
	
	@Test
	public void testGetCurrentScaledValue() throws OptimizationException {
		value.d = 0.0;
		assertEquals(-0.1, gm.getCurrentScaledValue(sim), EPSILON);
		value.d = 1.0;
		assertEquals(0.3, gm.getCurrentScaledValue(sim), EPSILON);
		value.d = 2.0;
		assertEquals(0.7, gm.getCurrentScaledValue(sim), EPSILON);
		value.d = 3.0;
		assertEquals(1.1, gm.getCurrentScaledValue(sim), EPSILON);
	}
	
	@Test
	public void testModify() throws OptimizationException {
		value.d = 0.0;
		gm.modify(sim, -0.5);
		assertEquals(-1.0, value.d, EPSILON);
		
		gm.modify(sim, 0.0);
		assertEquals(0.25, value.d, EPSILON);
		
		gm.modify(sim, 0.5);
		assertEquals(1.5, value.d, EPSILON);
		
		gm.modify(sim, 1.0);
		assertEquals(2.75, value.d, EPSILON);
		
		gm.modify(sim, 1.5);
		assertEquals(4.0, value.d, EPSILON);
	}
	
	public void testSingularRange() throws OptimizationException {
		gm.setMinValue(1.0);
		gm.setMaxValue(1.0);
		value.d = 0.5;
		assertEquals(0.0, gm.getCurrentScaledValue(sim), EPSILON);
		value.d = 1.0;
		assertEquals(0.5, gm.getCurrentScaledValue(sim), EPSILON);
		value.d = 1.00001;
		assertEquals(1.0, gm.getCurrentScaledValue(sim), EPSILON);
	}
	
	public class TestValue {
		private double d;
		
		public double getValue() {
			return d;
		}
		
		public void setValue(double value) {
			this.d = value;
		}
	}
	
}
