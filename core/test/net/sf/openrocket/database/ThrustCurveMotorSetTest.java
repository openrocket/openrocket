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
	
	
	private static final ThrustCurveMotor motor1 = new ThrustCurveMotor.Builder()
			.setManufacturer(Manufacturer.getManufacturer("A"))
			.setCommonName("F12")
			.setDesignation("F12")
			.setDescription("Desc")
			.setMotorType(Motor.Type.UNKNOWN)
			.setStandardDelays(new double[] {})
			.setDiameter(0.024)
			.setLength(0.07)
			.setTimePoints(new double[] { 0, 1, 2 })
			.setThrustPoints(new double[] { 0, 1, 0 })
			.setCGPoints(new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL })
			.setDigest("digestA")
			.build();
	
	private static final ThrustCurveMotor motor2 = new ThrustCurveMotor.Builder()
			.setManufacturer(Manufacturer.getManufacturer("A"))
			.setCommonName("F12")
			.setDesignation("F12")
			.setDescription("Desc")
			.setMotorType(Motor.Type.SINGLE)
			.setStandardDelays(new double[] { 5 })
			.setDiameter(0.024)
			.setLength(0.07)
			.setTimePoints(new double[] { 0, 1, 2 })
			.setThrustPoints(new double[] { 0, 1, 0 })
			.setCGPoints(new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL })
			.setDigest("digestB")
			.build();
	
	private static final ThrustCurveMotor motor3 = new ThrustCurveMotor.Builder()
			.setManufacturer(Manufacturer.getManufacturer("A"))
			.setDesignation("F12")
			.setDescription("Desc")
			.setMotorType(Motor.Type.HYBRID)
			.setStandardDelays(new double[] { 0 })
			.setDiameter(0.024)
			.setLength(0.07)
			.setTimePoints(new double[] { 0, 1, 2 })
			.setThrustPoints(new double[] { 0, 2, 0 })
			.setCGPoints(new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL })
			.setDigest("digestD")
			.build();
		
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
		assertEquals(motor2.getManufacturer(), set.getManufacturer());
		assertEquals(motor2.getCommonName(), set.getCommonName());
		assertEquals(Motor.Type.SINGLE, set.getType());
		assertEquals(motor2.getDiameter(), set.getDiameter(), 0.00001);
		assertEquals(motor2.getLength(), set.getLength(), 0.00001);
		assertEquals(2, set.getMotors().size());
		assertEquals(motor1, set.getMotors().get(0));
		assertEquals(motor2, set.getMotors().get(1));
		assertEquals(Arrays.asList(5.0), set.getDelays());

		// Test that adding motor3 fails
		assertFalse(set.matches(motor3));
		try {
			set.addMotor(motor3);
			fail("Did not throw exception");
		} catch (IllegalArgumentException e) {
		}
	}
	
}
