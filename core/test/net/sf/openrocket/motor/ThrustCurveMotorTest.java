package net.sf.openrocket.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Pair;


public class ThrustCurveMotorTest {
	
	// private final double EPSILON = 0.000001;

	private final double radius = 0.025;
	private final double length = 0.10;
	
	private final ThrustCurveMotor motorX6 = new ThrustCurveMotor.Builder()
		.setManufacturer(Manufacturer.getManufacturer("foo"))
		.setDesignation("X6")
		.setDescription("Description of X6")
		.setMotorType(Motor.Type.RELOAD)
		.setStandardDelays(new double[] {0, 2, Motor.PLUGGED_DELAY})
		.setDiameter(radius*2)
		.setLength(length)
		.setTimePoints(new double[] {0, 1, 3, 4})
		.setThrustPoints(new double[] {0, 2, 3, 0})
		.setCGPoints(new Coordinate[] {
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.02,0,0,0.05),
					new Coordinate(0.03,0,0,0.03)})
		.setDigest("digestA")
		.build();
	
	
	private final double radiusA8 = 0.018;
	private final double lengthA8 = 0.10;
	private final ThrustCurveMotor motorEstesA8_3 = new ThrustCurveMotor.Builder()
			.setManufacturer(Manufacturer.getManufacturer("Estes"))
			.setDesignation("A8-3")
			.setDescription("A8 Test Motor")
			.setMotorType(Motor.Type.SINGLE)
			.setStandardDelays(new double[] {0, 2, Motor.PLUGGED_DELAY})
			.setDiameter(radiusA8*2)
			.setLength(lengthA8)
			.setTimePoints(new double[] { 
						0,     0.041, 0.084, 0.127,
						0.166, 0.192, 0.206, 0.226,
						0.236, 0.247, 0.261, 0.277,
						0.306, 0.351, 0.405, 0.467,
						0.532, 0.589, 0.632, 0.652,
						0.668, 0.684, 0.703, 0.73})
			.setThrustPoints(new double[] {
						0,     0.512, 2.115, 4.358,
						6.794, 8.588, 9.294, 9.73,
						8.845, 7.179, 5.063, 3.717,
						3.205, 2.884, 2.499, 2.371,
						2.307, 2.371, 2.371, 2.243, 
						1.794, 1.153, 0.448, 0})
			.setCGPoints(new Coordinate[] {
						new Coordinate(0.0350, 0, 0, 0.016350),new Coordinate(0.0352, 0, 0, 0.016335),new Coordinate(0.0354, 0, 0, 0.016255),new Coordinate(0.0356, 0, 0, 0.016057),
						new Coordinate(0.0358, 0, 0, 0.015748),new Coordinate(0.0360, 0, 0, 0.015463),new Coordinate(0.0362, 0, 0, 0.015285),new Coordinate(0.0364, 0, 0, 0.015014),
						new Coordinate(0.0366, 0, 0, 0.014882),new Coordinate(0.0368, 0, 0, 0.014757),new Coordinate(0.0370, 0, 0, 0.014635),new Coordinate(0.0372, 0, 0, 0.014535),
						new Coordinate(0.0374, 0, 0, 0.014393),new Coordinate(0.0376, 0, 0, 0.014198),new Coordinate(0.0378, 0, 0, 0.013991),new Coordinate(0.0380, 0, 0, 0.013776),
						new Coordinate(0.0382, 0, 0, 0.013560),new Coordinate(0.0384, 0, 0, 0.013370),new Coordinate(0.0386, 0, 0, 0.013225),new Coordinate(0.0388, 0, 0, 0.013160),
						new Coordinate(0.0390, 0, 0, 0.013114),new Coordinate(0.0392, 0, 0, 0.013080),new Coordinate(0.0394, 0, 0, 0.013059),new Coordinate(0.0396, 0, 0, 0.013050)})
			.setDigest("digestA8-3")
			.build();


	@Test
	public void testVerifyMotorA8_3Times(){
		final ThrustCurveMotor mtr = motorEstesA8_3;
		
		assertEquals( 0.041, mtr.getTime( 0.041),  0.001 );
		
		assertEquals( 0.206, mtr.getTime( 0.206),  0.001 );
	}

	@Test
	public void testVerifyMotorA8_3Thrusts(){
		final ThrustCurveMotor mtr = motorEstesA8_3;
		
		assertEquals( 0.512, mtr.getThrust( 0.041),  0.001 );
		
		assertEquals( 9.294, mtr.getThrust( 0.206),  0.001 );
	}
	

	@Test
	public void testVerifyMotorA8_3CG(){
		final ThrustCurveMotor mtr = motorEstesA8_3;

		final double actCGx0p041 = mtr.getCMx(0.041);
		assertEquals( 0.0352, actCGx0p041,  0.001 );
		final double actMass0p041 = mtr.getTotalMass(  0.041 );
		assertEquals( 0.016335, actMass0p041,  0.001 );

		final double actCGx0p206 = mtr.getCMx( 0.206 );
		assertEquals( 0.0362, actCGx0p206,  0.001 );
		final double actMass0p206 = mtr.getTotalMass( 0.206 );
		assertEquals( 0.015285, actMass0p206,  0.001 );
	}
	
	private class TestPair extends Pair<Double,Double>{
		private TestPair(){ super( 0., 0.);}
		
		public TestPair( Double u, Double v){
			super(u,v);
		}
	}
	
	@Test
	public void testThrustInterpolation(){
		final ThrustCurveMotor mtr = motorEstesA8_3;
		
		Pair<Double, Double> testPairs[] = new TestPair[]{
				new TestPair(0.512, 0.041),
				new TestPair(2.115, 0.084),
				new TestPair( 1.220, 0.060),
				new TestPair( 1.593, 0.070),
				new TestPair( 1.965, 0.080),	
				new TestPair( 2.428, 0.090),	
	    };
		
		for( Pair<Double,Double> testCase : testPairs ){
			final double motorTime = testCase.getV();
			final double expThrust = testCase.getU();
			final double actThrust = mtr.getThrust(motorTime); 
					
			assertEquals( "Error in interpolating thrust: ", expThrust, actThrust,  0.001 );
		}
	}
	
	@Test 
	public void testMotorData() {
		assertEquals("X6", motorX6.getDesignation());
		assertEquals("X6-5", motorX6.getDesignation(5.0));
		assertEquals("Description of X6", motorX6.getDescription());
		assertEquals(Motor.Type.RELOAD, motorX6.getMotorType());
	}

	@Test
	public void testTimeIndexingNegative(){
		final ThrustCurveMotor mtr = motorX6;
		// attempt to retrieve for a time before the motor ignites
		assertTrue( "Fault in negative time indexing: ", Double.isNaN( mtr.getTime( -1 )) );
	}

	@Test
	public void testTimeIndexingPastBurnout(){
		final ThrustCurveMotor mtr = motorX6;
		
		// attempt to retrieve for a time after the motor finishes
		// should retrieve the last time value.  In this case: 4.0
		assertEquals( 4.0, mtr.getTime( Double.MAX_VALUE ),  0.00000001 );
		assertEquals( 4.0, mtr.getTime( 20.0 ),  0.00000001 );
	}
	

	@Test
	public void testTimeIndexingAtBurnout(){
		// attempt to retrieve for a time after motor cutoff
		assertEquals( 4.0, motorX6.getTime( 4.0),  0.00001 );
	}

	@Test
	public void testTimeRetrieval(){
		final ThrustCurveMotor mtr = motorX6;

		final double[] timeList = { 0.2, 0.441, 0.512, 1., 2., 3};
		
		for( double searchTime : timeList ){
			assertEquals( searchTime, mtr.getTime(searchTime), 0.00001);
		}
	}
	
	@Test
	public void testThrustRetrieval(){
		// attempt to retrieve an integer index: 
		assertEquals( 2.0, motorX6.getThrust( 1 ),  0.001 );
		assertEquals( 2.5, motorX6.getThrust( 2 ),  0.001 );
		assertEquals( 3.0, motorX6.getThrust( 3 ),  0.001 );
	}
			
	
}
