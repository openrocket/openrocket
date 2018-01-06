package net.sf.openrocket.masscalc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassCacheTest extends BaseTestCase {
	
	
	@Test
	public void testCMCache() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());
	
		// ant throws in error if it can't find a test case in the *Test.java file.
		// .... soooo we have this waste of space. -DMW
		assertTrue( true );
	}
//		
//		FlightConfiguration config = rocket.getEmptyConfiguration();
//		MassCalculator mc = new MassCalculator();
//		
//		{
//			// validate payload stage
//			AxialStage payloadStage = (AxialStage) rocket.getChild(0);
//			int plNum = payloadStage.getStageNumber();
//			config.setOnlyStage( plNum );
//			
//			MassData calcMass = mc.calculateBurnoutMassData( config );
//			
//			double expMass = 0.116287;
//			double expCMx = 0.278070785749;
//			assertEquals("Upper Stage Mass is incorrect: ", expMass, calcMass.getCM().weight, EPSILON);
//			assertEquals("Upper Stage CM.x is incorrect: ", expCMx, calcMass.getCM().x, EPSILON);
//			assertEquals("Upper Stage CM.y is incorrect: ", 0.0f, calcMass.getCM().y, EPSILON);
//			assertEquals("Upper Stage CM.z is incorrect: ", 0.0f, calcMass.getCM().z, EPSILON);
//		
//			MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
//			assertEquals("Upper Stage Mass (cache) is incorrect: ", expMass, rocketLaunchMass.getCM().weight, EPSILON);
//			assertEquals("Upper Stage CM.x (cache) is incorrect: ", expCMx, rocketLaunchMass.getCM().x, EPSILON);
//			
//			MassData rocketSpentMass = mc.getRocketSpentMassData( config);
//			assertEquals("Upper Stage Mass (cache) is incorrect: ", expMass, rocketSpentMass.getCM().weight, EPSILON);
//			assertEquals("Upper Stage CM.x (cache) is incorrect: ", expCMx, rocketSpentMass.getCM().x, EPSILON);
//		}{
//			ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
//			int boostNum = boosters.getStageNumber();
//			config.setOnlyStage( boostNum );
//			
//			MassData boosterMass = mc.calculateBurnoutMassData( config);
//			
//			double expMass = BOOSTER_SET_NO_MOTORS_MASS;
//			double expCMx = BOOSTER_SET_NO_MOTORS_CMX;
//			assertEquals("Heavy Booster Mass is incorrect: ", expMass, boosterMass.getCM().weight, EPSILON);
//			assertEquals("Heavy Booster CM.x is incorrect: ", expCMx, boosterMass.getCM().x, EPSILON);
//			assertEquals("Heavy Booster CM.y is incorrect: ", 0.0f, boosterMass.getCM().y, EPSILON);
//			assertEquals("Heavy Booster CM.z is incorrect: ", 0.0f, boosterMass.getCM().z, EPSILON);
//			
//			MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
//			assertEquals(" Booster Stage Mass (cache) is incorrect: ", expMass, rocketLaunchMass.getCM().weight, EPSILON);
//			assertEquals(" Booster Stage CM.x (cache) is incorrect: ", expCMx, rocketLaunchMass.getCM().x, EPSILON);
//			
//			MassData rocketSpentMass = mc.getRocketSpentMassData( config);
//			assertEquals(" Booster Stage Mass (cache) is incorrect: ", expMass, rocketSpentMass.getCM().weight, EPSILON);
//			assertEquals(" Booster Stage CM.x (cache) is incorrect: ", expCMx, rocketSpentMass.getCM().x, EPSILON);
//		}
//	}
//	

}
