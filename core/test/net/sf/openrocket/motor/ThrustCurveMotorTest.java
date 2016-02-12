package net.sf.openrocket.motor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;

public class ThrustCurveMotorTest {
	
	// private final double EPSILON = 0.000001;

	private final double radius = 0.025;
	private final double length = 0.10;
	
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
	public void testTimeIndexingNegative(){
		// attempt to retrieve for a time before the motor ignites
		assertEquals( 0.0, motor.getPseudoIndex( -1 ),  0.001 );
	}

	@Test
	public void testTimeRetrieval(){
		// attempt to retrieve an integer index: 
		assertEquals( 0.0, motor.getMotorTimeAtIndex( 0 ),  0.001 );
		assertEquals( 1.0, motor.getMotorTimeAtIndex( 1 ),  0.001 );
		assertEquals( 4.0, motor.getMotorTimeAtIndex( 3 ),  0.001 );

		final double searchTime = 0.2;
		assertEquals( searchTime, motor.getMotorTimeAtIndex( motor.getPseudoIndex(searchTime)),  0.001 );	
	}
	
	@Test
	public void testThrustRetrieval(){
		// attempt to retrieve an integer index: 
		assertEquals( 2.0, motor.getThrustAtIndex( 1 ),  0.001 );
		assertEquals( 3.0, motor.getThrustAtIndex( 2 ),  0.001 );
		assertEquals( 0.0, motor.getThrustAtIndex( 3 ),  0.001 );
	}
	
	// using better interface
//	@Test
//	public void testCGRetrievalByDouble(){
//		final double actCGx0 = motor.getCGxAtIndex( 0.0 );
//		assertEquals( 0.02, actCGx0,  0.001 );
//		final double actMass0 = motor.getMassAtIndex( 0.0 );
//		assertEquals( 0.05, actMass0,  0.001 );
//		 
//		final double actCGx25 = motor.getCGxAtIndex( 2.5 );
//		assertEquals( 0.025, actCGx25,  0.001 );
//		final double actMass25 = motor.getMassAtIndex( 2.5 );
//		assertEquals( 0.04, actMass25,  0.001 );
//	}
	
	// deprecated version. 
	// delete this method upon change to new function signatures
	@Test
	public void testCGRetrieval(){
		final double actCGx0 = motor.getCGAtIndex( 0.0 ).x;
		assertEquals( 0.02, actCGx0,  0.001 );
		final double actMass0 = motor.getCGAtIndex( 0.0 ).weight;
		assertEquals( 0.05, actMass0,  0.001 );
		 
		final double actCGx25 = motor.getCGAtIndex( 2.5 ).x;
		assertEquals( 0.025, actCGx25,  0.001 );
		final double actMass25 = motor.getCGAtIndex( 2.5 ).weight;
		assertEquals( 0.04, actMass25,  0.001 );
	}
	
	@Test
	public void testTimeIndexingPastEnd(){
		// attempt to retrieve for a time after motor cutoff
		assertEquals( 3.0, motor.getPseudoIndex( 5.0),  0.001 );
	}
	@Test
	public void testTimeIndexingAtEnd(){
		// attempt to retrieve for a time just at motor cutoff
		assertEquals( 3.0, motor.getPseudoIndex( 4.0),  0.001 );
	}
	@Test
	public void testTimeIndexingDuring(){
		// attempt to retrieve for a generic time during the motor's burn
		assertEquals( 1.6, motor.getPseudoIndex( 2.2), 0.001 );
	}
	@Test
	public void testTimeIndexingSnapUp(){
		// attempt to retrieve for a generic time during the motor's burn
		assertEquals( 3.0, motor.getPseudoIndex( 3.9999), 0.001 );
	}
	@Test
	public void testTimeIndexingSnapDown(){
		// attempt to retrieve for a generic time during the motor's burn
		assertEquals( 2.0, motor.getPseudoIndex( 3.0001), 0.001 );
	}
	
//	@Test
//	public void testInstance() {
//		ThrustCurveMotorState instance = motor.getNewInstance();
//		
//		verify(instance, 0, 0.05, 0.02);
//		instance.step(0.0, null);
//		verify(instance, 0, 0.05, 0.02);
//		instance.step(0.5, null);
//		verify(instance, 0.5, 0.05, 0.02);
//		instance.step(1.5, null);
//		verify(instance, (1.5 + 2.125)/2, 0.05, 0.02);
//		instance.step(2.5, null);
//		verify(instance, (2.125 + 2.875)/2, 0.05, 0.02);
//		instance.step(3.0, null);
//		verify(instance, (2+3.0/4 + 3)/2, 0.05, 0.02);
//		instance.step(3.5, null);
//		verify(instance, (1.5 + 3)/2, 0.045, 0.0225);
//		instance.step(4.5, null);
//		// mass and cg is simply average of the end points
//		verify(instance, 1.5/4, 0.035, 0.0275);
//		instance.step(5.0, null);
//		verify(instance, 0, 0.03, 0.03);
//	}
//	
//	private void verify(ThrustCurveMotorState instance, double thrust, double mass, double cgx) {
//		assertEquals("Testing thrust", thrust, instance.getThrust(), EPS);
//		assertEquals("Testing mass", mass, instance.getCG().weight, EPS);
//		assertEquals("Testing cg x", cgx, instance.getCG().x, EPS);
//		assertEquals("Testing longitudinal inertia", mass*longitudinal, instance.getMotor().getLongitudinalInertia(), EPS);
//		assertEquals("Testing rotational inertia", mass*rotational, instance.getMotor().getRotationalInertia(), EPS);
//	}
			
	
}
