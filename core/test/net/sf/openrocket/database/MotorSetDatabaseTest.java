package net.sf.openrocket.database;

import static org.junit.Assert.*;

import java.util.List;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;

import org.junit.Test;


public class MotorSetDatabaseTest {

	@Test
	public void testMotorLoading() {
		
		ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase(true) {
			@Override
			protected void loadMotors() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				this.addMotor(new ThrustCurveMotor(Manufacturer.getManufacturer("A"),
						"Foo", "Desc", Motor.Type.SINGLE, new double[] { 0 },
						0.024, 0.07, new double[] { 0, 1, 2 }, new double[] {0, 1, 0},
						new Coordinate[] {Coordinate.NUL, Coordinate.NUL, Coordinate.NUL}, "digestA"));
				this.addMotor(new ThrustCurveMotor(Manufacturer.getManufacturer("A"),
						"Bar", "Desc", Motor.Type.SINGLE, new double[] { 0 },
						0.024, 0.07, new double[] { 0, 1, 2 }, new double[] {0, 1, 0},
						new Coordinate[] {Coordinate.NUL, Coordinate.NUL, Coordinate.NUL}, "digestB"));
				this.addMotor(new ThrustCurveMotor(Manufacturer.getManufacturer("A"),
						"Foo", "Desc", Motor.Type.UNKNOWN, new double[] { 0 },
						0.024, 0.07, new double[] { 0, 1, 2 }, new double[] {0, 1, 0},
						new Coordinate[] {Coordinate.NUL, Coordinate.NUL, Coordinate.NUL}, "digestA"));
			}
		};
		
		assertFalse(db.isLoaded());
		db.startLoading();
		assertFalse(db.isLoaded());
		List<ThrustCurveMotorSet> list = db.getMotorSets();
		assertTrue(db.isLoaded());
		
		assertEquals(2, list.size());
		assertEquals(1, list.get(0).getMotors().size());
		assertEquals(1, list.get(1).getMotors().size());
		assertEquals("Bar", list.get(0).getMotors().get(0).getDesignation());
		assertEquals("Foo", list.get(1).getMotors().get(0).getDesignation());
	}
	
}
