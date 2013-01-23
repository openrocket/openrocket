package net.sf.openrocket.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;

import org.junit.Test;

public class ThrustCurveMotorSetTest {
	
	
	private static final ThrustCurveMotor motor1 = new ThrustCurveMotor(
			Manufacturer.getManufacturer("A"),
			"F12X", "Desc", Motor.Type.UNKNOWN, new double[] {},
			0.024, 0.07, new double[] { 0, 1, 2 }, new double[] { 0, 1, 0 },
			new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL }, "digestA");
	
	private static final ThrustCurveMotor motor2 = new ThrustCurveMotor(
			Manufacturer.getManufacturer("A"),
			"F12H", "Desc", Motor.Type.SINGLE, new double[] { 5 },
			0.024, 0.07, new double[] { 0, 1, 2 }, new double[] { 0, 1, 0 },
			new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL }, "digestB");
	
	private static final ThrustCurveMotor motor3 = new ThrustCurveMotor(
			Manufacturer.getManufacturer("A"),
			"F12", "Desc", Motor.Type.UNKNOWN, new double[] { 0, Motor.PLUGGED },
			0.024, 0.07, new double[] { 0, 1, 2 }, new double[] { 0, 2, 0 },
			new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL }, "digestC");
	
	private static final ThrustCurveMotor motor4 = new ThrustCurveMotor(
			Manufacturer.getManufacturer("A"),
			"F12", "Desc", Motor.Type.HYBRID, new double[] { 0 },
			0.024, 0.07, new double[] { 0, 1, 2 }, new double[] { 0, 2, 0 },
			new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL }, "digestD");
	
	
	@Test
	public void testSimplifyDesignation() {
		assertEquals("J115", ThrustCurveMotorSet.simplifyDesignation("J115"));
		assertEquals("J115", ThrustCurveMotorSet.simplifyDesignation(" J115  "));
		assertEquals("H115", ThrustCurveMotorSet.simplifyDesignation("241H115-KS"));
		assertEquals("J115", ThrustCurveMotorSet.simplifyDesignation("384  J115"));
		assertEquals("J115", ThrustCurveMotorSet.simplifyDesignation("384-J115"));
		assertEquals("A2", ThrustCurveMotorSet.simplifyDesignation("A2T"));
		assertEquals("1/2A2T", ThrustCurveMotorSet.simplifyDesignation("1/2A2T"));
		assertEquals("MicroMaxxII", ThrustCurveMotorSet.simplifyDesignation("Micro Maxx II"));
	}
	
	@Test
	public void testAdding() {
		ThrustCurveMotorSet set = new ThrustCurveMotorSet();
		
		// Test empty set
		assertNull(set.getManufacturer());
		assertEquals(0, set.getMotors().size());
		
		// Add motor1
		assertTrue(set.matches(motor1));
		set.addMotor(motor1);
		assertEquals(motor1.getManufacturer(), set.getManufacturer());
		assertEquals(motor1.getDesignation(), set.getDesignation());
		assertEquals(Motor.Type.UNKNOWN, set.getType());
		assertEquals(motor1.getDiameter(), set.getDiameter(), 0.00001);
		assertEquals(motor1.getLength(), set.getLength(), 0.00001);
		assertEquals(1, set.getMotors().size());
		assertEquals(motor1, set.getMotors().get(0));
		assertEquals(Collections.emptyList(), set.getDelays());
		
		// Add motor1 again
		assertTrue(set.matches(motor1));
		set.addMotor(motor1);
		assertEquals(motor1.getManufacturer(), set.getManufacturer());
		assertEquals(motor1.getDesignation(), set.getDesignation());
		assertEquals(Motor.Type.UNKNOWN, set.getType());
		assertEquals(motor1.getDiameter(), set.getDiameter(), 0.00001);
		assertEquals(motor1.getLength(), set.getLength(), 0.00001);
		assertEquals(1, set.getMotors().size());
		assertEquals(motor1, set.getMotors().get(0));
		assertEquals(Collections.emptyList(), set.getDelays());
		
		// Add motor2
		assertTrue(set.matches(motor2));
		set.addMotor(motor2);
		assertEquals(motor1.getManufacturer(), set.getManufacturer());
		assertEquals(motor3.getDesignation(), set.getDesignation());
		assertEquals(Motor.Type.SINGLE, set.getType());
		assertEquals(motor1.getDiameter(), set.getDiameter(), 0.00001);
		assertEquals(motor1.getLength(), set.getLength(), 0.00001);
		assertEquals(2, set.getMotors().size());
		assertEquals(motor2, set.getMotors().get(0));
		assertEquals(motor1, set.getMotors().get(1));
		assertEquals(Arrays.asList(5.0), set.getDelays());
		
		// Add motor3
		assertTrue(set.matches(motor3));
		set.addMotor(motor3);
		assertEquals(motor1.getManufacturer(), set.getManufacturer());
		assertEquals(motor3.getDesignation(), set.getDesignation());
		assertEquals(Motor.Type.SINGLE, set.getType());
		assertEquals(motor1.getDiameter(), set.getDiameter(), 0.00001);
		assertEquals(motor1.getLength(), set.getLength(), 0.00001);
		assertEquals(3, set.getMotors().size());
		assertEquals(motor3, set.getMotors().get(0));
		assertEquals(motor2, set.getMotors().get(1));
		assertEquals(motor1, set.getMotors().get(2));
		assertEquals(Arrays.asList(0.0, 5.0, Motor.PLUGGED), set.getDelays());
		
		// Test that adding motor4 fails
		assertFalse(set.matches(motor4));
		try {
			set.addMotor(motor4);
			fail("Did not throw exception");
		} catch (IllegalArgumentException e) {
		}
	}
	
}
