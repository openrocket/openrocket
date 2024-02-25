package info.openrocket.core.masscalc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class MassCacheTest extends BaseTestCase {

	@Test
	public void testCMCache() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket." + Thread.currentThread().getStackTrace()[1].getMethodName());

		// ant throws in error if it can't find a test case in the *Test.java file.
		// .... soooo we have this waste of space. -DMW
		assertTrue(true);
	}
	//
	// FlightConfiguration config = rocket.getEmptyConfiguration();
	// MassCalculator mc = new MassCalculator();
	//
	// {
	// // validate payload stage
	// AxialStage payloadStage = (AxialStage) rocket.getChild(0);
	// int plNum = payloadStage.getStageNumber();
	// config.setOnlyStage( plNum );
	//
	// MassData calcMass = mc.calculateBurnoutMassData( config );
	//
	// double expMass = 0.116287;
	// double expCMx = 0.278070785749;
	// assertEquals(expMass,
	// calcMass.getCM().weight, EPSILON, "Upper Stage Mass is incorrect: ");
	// assertEquals(expCMx, calcMass.getCM().x,
	// EPSILON, "Upper Stage CM.x is incorrect: ");
	// assertEquals(0.0f, calcMass.getCM().y,
	// EPSILON, "Upper Stage CM.y is incorrect: ");
	// assertEquals(0.0f, calcMass.getCM().z,
	// EPSILON, "Upper Stage CM.z is incorrect: ");
	//
	// MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
	// assertEquals(expMass,
	// rocketLaunchMass.getCM().weight, EPSILON, "Upper Stage Mass (cache) is incorrect: ");
	// assertEquals(expCMx,
	// rocketLaunchMass.getCM().x, EPSILON, "Upper Stage CM.x (cache) is incorrect: ");
	//
	// MassData rocketSpentMass = mc.getRocketSpentMassData( config);
	// assertEquals(expMass,
	// rocketSpentMass.getCM().weight, EPSILON, "Upper Stage Mass (cache) is incorrect: ");
	// assertEquals(expCMx,
	// rocketSpentMass.getCM().x, EPSILON, "Upper Stage CM.x (cache) is incorrect: ");
	// }{
	// ParallelStage boosters = (ParallelStage) rocket.getChild(1).getChild(1);
	// int boostNum = boosters.getStageNumber();
	// config.setOnlyStage( boostNum );
	//
	// MassData boosterMass = mc.calculateBurnoutMassData( config);
	//
	// double expMass = BOOSTER_SET_NO_MOTORS_MASS;
	// double expCMx = BOOSTER_SET_NO_MOTORS_CMX;
	// assertEquals(expMass,
	// boosterMass.getCM().weight, EPSILON, "Heavy Booster Mass is incorrect: ");
	// assertEquals(expCMx,
	// boosterMass.getCM().x, EPSILON, "Heavy Booster CM.x is incorrect: ");
	// assertEquals(0.0f,
	// boosterMass.getCM().y, EPSILON, "Heavy Booster CM.y is incorrect: ");
	// assertEquals(0.0f,
	// boosterMass.getCM().z, EPSILON, "Heavy Booster CM.z is incorrect: ");
	//
	// MassData rocketLaunchMass = mc.getRocketLaunchMassData( config);
	// assertEquals(expMass,
	// rocketLaunchMass.getCM().weight, EPSILON, " Booster Stage Mass (cache) is incorrect: ");
	// assertEquals(expCMx,
	// rocketLaunchMass.getCM().x, EPSILON, " Booster Stage CM.x (cache) is incorrect: ");
	//
	// MassData rocketSpentMass = mc.getRocketSpentMassData( config);
	// assertEquals(expMass,
	// rocketSpentMass.getCM().weight, EPSILON, " Booster Stage Mass (cache) is incorrect: ");
	// assertEquals(expCMx,
	// rocketSpentMass.getCM().x, EPSILON, " Booster Stage CM.x (cache) is incorrect: ");
	// }
	// }
	//

}
