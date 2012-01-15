package net.sf.openrocket.motor;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;

import org.junit.Test;

public class ThrustCurveMotorTest {
	
	private final double EPS = 0.000001;

	private final double radius = 0.025;
	private final double length = 0.10;
	private final double longitudinal = Inertia.filledCylinderLongitudinal(radius, length);
	private final double rotational = Inertia.filledCylinderRotational(radius);
	
	private final ThrustCurveMotor motor = 
		new ThrustCurveMotor(Manufacturer.getManufacturer("foo"),
				"X6", "Description of X6", Motor.Type.RELOAD, 
				new double[] {0, 2, Motor.PLUGGED}, radius*2, length,
				new double[] {0, 1, 3, 4},  // time
				new double[] {0, 2, 3, 0},  // thrust
				new Coordinate[] {
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.03,0,0,0.03)
		}, "digestA");
	
	@Test 
	public void testMotorData() {
		
		assertEquals("X6", motor.getDesignation());
		assertEquals("X6-5", motor.getDesignation(5.0));
		assertEquals("Description of X6", motor.getDescription());
		assertEquals(Motor.Type.RELOAD, motor.getMotorType());
		
	}
	
	@Test
	public void testInstance() {
		MotorInstance instance = motor.getInstance();
		
		verify(instance, 0, 0.05, 0.02);
		instance.step(0.0, 0, null);
		verify(instance, 0, 0.05, 0.02);
		instance.step(0.5, 0, null);
		verify(instance, 0.5, 0.05, 0.02);
		instance.step(1.5, 0, null);
		verify(instance, (1.5 + 2.125)/2, 0.05, 0.02);
		instance.step(2.5, 0, null);
		verify(instance, (2.125 + 2.875)/2, 0.05, 0.02);
		instance.step(3.0, 0, null);
		verify(instance, (2+3.0/4 + 3)/2, 0.05, 0.02);
		instance.step(3.5, 0, null);
		verify(instance, (1.5 + 3)/2, 0.045, 0.0225);
		instance.step(4.5, 0, null);
		// mass and cg is simply average of the end points
		verify(instance, 1.5/4, 0.035, 0.0275);
		instance.step(5.0, 0, null);
		verify(instance, 0, 0.03, 0.03);
	}
	
	private void verify(MotorInstance instance, double thrust, double mass, double cgx) {
		assertEquals("Testing thrust", thrust, instance.getThrust(), EPS);
		assertEquals("Testing mass", mass, instance.getCG().weight, EPS);
		assertEquals("Testing cg x", cgx, instance.getCG().x, EPS);
		assertEquals("Testing longitudinal inertia", mass*longitudinal, instance.getLongitudinalInertia(), EPS);
		assertEquals("Testing rotational inertia", mass*rotational, instance.getRotationalInertia(), EPS);
	}
			
	
}
