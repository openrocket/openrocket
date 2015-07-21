package net.sf.openrocket.rocketcomponent;

//import junit.framework.TestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class StageTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.001;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);
	
	public void test() {
		//		fail("Not yet implemented");
	}
	
	public Rocket createTestRocket() {
		double tubeRadius = 1;
		// setup
		Rocket root = new Rocket();
		root.setName("Rocket");
		
		Stage sustainer = new Stage();
		sustainer.setName("Sustainer stage");
		RocketComponent sustainerNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		sustainerNose.setName("Sustainer Nosecone");
		sustainer.addChild(sustainerNose);
		RocketComponent sustainerBody = new BodyTube(3.0, tubeRadius, 0.01);
		sustainerBody.setName("Sustainer Body ");
		sustainer.addChild(sustainerBody);
		root.addChild(sustainer);
		
		Stage core = new Stage();
		core.setName("Core stage");
		BodyTube coreBody = new BodyTube(6.0, tubeRadius, 0.01);
		coreBody.setName("Core Body ");
		core.addChild(coreBody);
		root.addChild(core);
		
		return root;
	}
	
	public Stage createBooster() {
		double tubeRadius = 0.8;
		
		Stage booster = new Stage();
		booster.setName("Booster Stage A");
		booster.setOutside(true);
		RocketComponent boosterNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		boosterNose.setName("Booster A Nosecone");
		booster.addChild(boosterNose);
		RocketComponent boosterBody = new BodyTube(2.0, tubeRadius, 0.01);
		boosterBody.setName("Booster A Body ");
		booster.addChild(boosterBody);
		Transition boosterTail = new Transition();
		boosterTail.setName("Booster A Tail");
		boosterTail.setForeRadius(1.0);
		boosterTail.setAftRadius(0.5);
		boosterTail.setLength(1.0);
		booster.addChild(boosterTail);
		
		return booster;
	}
	
	//	// instantiate a rocket with realistic numbers, matching a file that exhibited errors
	//	public Rocket createDeltaIIRocket() {
	//		
	//		// setup
	//		Rocket root = new Rocket();
	//		root.setName("Rocket");
	//		
	//		Stage payloadFairing = new Stage();
	//		root.addChild(payloadFairing);
	//		payloadFairing.setName("Payload Fairing");
	//		NoseCone payloadNose = new NoseCone(Transition.Shape.POWER, 0.0535, 0.03);
	//		payloadNose.setShapeParameter(0.55);
	//		payloadNose.setName("Payload Nosecone");
	//		payloadNose.setAftRadius(0.3);
	//		payloadNose.setThickness(0.001);
	//		payloadFairing.addChild(payloadNose);
	//		BodyTube payloadBody = new BodyTube(0.0833, 0.03, 0.001);
	//		payloadBody.setName("Payload Body ");
	//		payloadFairing.addChild(payloadBody);
	//		Transition payloadTransition = new Transition();
	//		payloadTransition.setName("Payload Aft Transition");
	//		payloadTransition.setForeRadius(0.03);
	//		payloadTransition.setAftRadius(0.024);
	//		payloadTransition.setLength(0.04);
	//		payloadFairing.addChild(payloadTransition);
	//		
	//		Stage core = new Stage();
	//		core.setName("Delta Core stage");
	//		BodyTube coreBody = new BodyTube(0.0833, 0.3, 0.001);
	//		coreBody.setName("Delta Core Body ");
	//		core.addChild(coreBody);
	//		root.addChild(core);
	//		
	//		return root;
	//	}
	
	/* From OpenRocket Technical Documentation
	 *  
	 * 3.1.4 Coordinate systems
	 * During calculation of the aerodynamic properties a coordinate system fixed to the rocket will be used. 
	 * The origin of the coordinates is at the nose cone tip with the positive x-axis directed along the rocket 
	@@ -41,70 +35,302 @@ public class BodyTubeTest extends TestCase {
	 * when discussing the fins. During simulation, however, the y- and z-axes are fixed in relation to the rocket, 
	 * and do not necessarily align with the plane of the pitching moments.
	 */
	
	@Test
	public void testSetRocketPositionFail() {
		RocketComponent root = createTestRocket();
		Coordinate expectedPosition;
		Coordinate targetPosition;
		Coordinate resultPosition;
		
		// case 1: the Root Rocket should be stationary
		expectedPosition = ZERO;
		targetPosition = new Coordinate(+4.0, 0.0, 0.0);
		root.setAxialOffset(targetPosition.x);
		resultPosition = root.getRelativePositionVector();
		assertThat(" Moved the rocket root itself-- this should not be enabled.", expectedPosition.x, equalTo(resultPosition.x));
		
	}
	
	@Test
	public void testAddTopStage() {
		RocketComponent root = createTestRocket();
		
		// Sustainer Stage
		Stage sustainer = (Stage) root.getChild(0);
		RocketComponent sustainerNose = sustainer.getChild(0);
		RocketComponent sustainerBody = sustainer.getChild(1);
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer stage?  ", sustainer.isAncestor(sustainer), equalTo(false));
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer nose? ", sustainer.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is the root rocket an ancestor of the sustainer Nose?  ", root.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is sustainer Body an ancestor of the sustainer Nose?  ", sustainerBody.isAncestor(sustainerNose), equalTo(false));
		
		double expectedSustainerLength = 5.0;
		assertThat(" createTestRocket failed: Sustainer size: ", sustainer.getLength(), equalTo(expectedSustainerLength));
		double expectedSustainerX = +2.5;
		double sustainerX;
		sustainerX = sustainer.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: Relative position: ", sustainerX, equalTo(expectedSustainerX));
		sustainerX = sustainer.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: Absolute position: ", sustainerX, equalTo(expectedSustainerX));
		
		double expectedSustainerNoseX = -1.5;
		double sustainerNosePosition = sustainerNose.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: sustainer Nose X position: ", sustainerNosePosition, equalTo(expectedSustainerNoseX));
		expectedSustainerNoseX = +1;
		sustainerNosePosition = sustainerNose.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: sustainer Nose X position: ", sustainerNosePosition, equalTo(expectedSustainerNoseX));
		
		double expectedSustainerBodyX = +1;
		double sustainerBodyX = sustainerBody.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: sustainer body rel X position: ", sustainerBodyX, equalTo(expectedSustainerBodyX));
		expectedSustainerBodyX = +3.5;
		sustainerBodyX = sustainerBody.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: sustainer body abs X position: ", sustainerBodyX, equalTo(expectedSustainerBodyX));
		
	}
	
	// WARNING:   this test will not pass unless 'testAddTopStage' is passing as well -- that function tests the dependencies...
	@Test
	public void testAddMiddleStage() {
		RocketComponent root = createTestRocket();
		
		// Core Stage
		Stage core = (Stage) root.getChild(1);
		double expectedCoreLength = 6.0;
		assertThat(" createTestRocket failed: Core size: ", core.getLength(), equalTo(expectedCoreLength));
		double expectedCoreX = +8.0;
		double coreX;
		core.setRelativePosition(Position.AFTER);
		
		coreX = core.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: Relative position: ", coreX, equalTo(expectedCoreX));
		coreX = core.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: Absolute position: ", coreX, equalTo(expectedCoreX));
		
		RocketComponent coreBody = core.getChild(0);
		double expectedCoreBodyX = 0.0;
		double coreBodyX = coreBody.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: core body rel X: ", coreBodyX, equalTo(expectedCoreBodyX));
		expectedCoreBodyX = expectedCoreX;
		coreBodyX = coreBody.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: core body abs X: ", coreBodyX, equalTo(expectedCoreBodyX));
		
	}
	
	@Test
	public void testSetStagePosition_topOfStack() {
		// setup
		RocketComponent root = createTestRocket();
		Stage sustainer = (Stage) root.getChild(0);
		Coordinate expectedPosition = new Coordinate(+2.5, 0., 0.); // i.e. half the tube length
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		// without making the rocket 'external' and the Stage should be restricted to AFTER positioning.
		sustainer.setOutside(false);
		sustainer.setRelativePositionMethod(Position.ABSOLUTE);
		assertThat("Setting a stage's position method to anything other than AFTER flags it off-center", sustainer.getOutside(), equalTo(true));
		
		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		
		Coordinate resultantRelativePosition = sustainer.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
		
	}
	
	@Test
	public void testFindPrevAxialStage() {
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		Stage booster2 = new Stage();
		booster2.setOutside(true);
		booster2.setName("Booster Set 2");
		RocketComponent booster2Body = new BodyTube(2.0, 1.0, 0.01);
		booster2Body.setName("Booster Body 2");
		booster2.addChild(booster2Body);
		root.addChild(booster2);
		
		Stage booster3 = new Stage();
		booster3.setOutside(true);
		booster3.setName("Booster Set 3");
		RocketComponent booster3Body = new BodyTube(4.0, 1.0, 0.01);
		booster3Body.setName("Booster Body 3");
		booster3.addChild(booster3Body);
		root.addChild(booster3);
		
		Stage tail = new Stage();
		tail.setName("Tail");
		RocketComponent tailBody = new BodyTube(4.0, 1.0, 0.01);
		tailBody.setName("TailBody");
		root.addChild(tail);
		tail.addChild(tailBody);
		
		Stage prevAxialStage;
		prevAxialStage = booster3.updatePrevAxialStage();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", prevAxialStage, equalTo(core));
		
		prevAxialStage = tail.updatePrevAxialStage();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", prevAxialStage, equalTo(core));
		
	}
	
	
	@Test
	public void testSetStagePosition_inStack() {
		// setup
		RocketComponent root = createTestRocket();
		Stage sustainer = (Stage) root.getChild(0);
		Stage core = (Stage) root.getChild(1);
		Coordinate expectedSustainerPosition = new Coordinate(+2.5, 0., 0.); // i.e. half the tube length
		Coordinate expectedCorePosition = new Coordinate(+8.0, 0., 0.);
		Coordinate targetPosition = new Coordinate(+17.0, 0., 0.);
		
		sustainer.setAxialOffset(targetPosition.x);
		Coordinate sustainerPosition = sustainer.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", sustainerPosition.x, equalTo(expectedSustainerPosition.x));
		
		core.setAxialOffset(targetPosition.x);
		Coordinate resultantCorePosition = core.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantCorePosition.x, equalTo(expectedCorePosition.x));
		
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideABSOLUTE() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		Coordinate targetPosition = new Coordinate(+17.0, 0., 0.);
		double expectedX = targetPosition.x;
		
		// when 'external' the stage should be freely movable		
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.ABSOLUTE);
		booster.setRelativeToStage(1);
		// vv function under test
		booster.setAxialOffset(targetPosition.x);
		// ^^ function under test
		
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setAxialPosition(double)' failed. PositionValue: ", resultantPositionValue, equalTo(expectedX));
		double resultantAxialPosition = booster.getAxialOffset();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantAxialPosition, equalTo(expectedX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
	}
	
	// WARNING:
	// Because even though this is an "outside" stage, it's relative to itself -- i.e. an error-condition  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideTopOfStack() {
		// setup
		RocketComponent root = createTestRocket();
		Stage sustainer = (Stage) root.getChild(0);
		Coordinate expectedPosition = new Coordinate(+2.5, 0., 0.);
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		// when 'external' the stage should be freely movable		
		sustainer.setOutside(true);
		sustainer.setRelativePositionMethod(Position.TOP);
		sustainer.setRelativeToStage(0);
		int expectedRelativeIndex = -1;
		assertThat(" 'setRelativeToStage(int)' failed. Relative stage index:", sustainer.getRelativeToStage(), equalTo(expectedRelativeIndex));
		
		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		
		Coordinate resultantRelativePosition = sustainer.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		double expectedPositionValue = 0;
		double resultantPositionValue = sustainer.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(expectedPositionValue));
		
		double expectedAxialPosition = 0;
		double resultantAxialPosition = sustainer.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialPosition));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getAbsolutePositionVector();
		assertThat(" 'setAbsolutePositionVector()' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
	}
	
	@Test
	public void testSetStagePosition_outsideTOP() {
		Rocket root = this.createTestRocket();
		Stage booster = createBooster();
		root.addChild(booster);
		
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.TOP);
		booster.setRelativeToStage(1);
		// vv function under test
		double targetOffset = +2.0;
		booster.setAxialOffset(targetOffset);
		// ^^ function under test
		
		double expectedX = +9.5;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideMIDDLE() {
		// setup
		RocketComponent root = createTestRocket();
		Stage booster = createBooster();
		root.addChild(booster);
		
		// when 'external' the stage should be freely movable
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.MIDDLE);
		booster.setRelativeToStage(1);
		// vv function under test
		double targetOffset = +2.0;
		booster.setAxialOffset(targetOffset);
		// ^^ function under test
		
		double expectedX = +10.0;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
		
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideBOTTOM() {
		// setup
		RocketComponent root = createTestRocket();
		Stage booster = createBooster();
		root.addChild(booster);
		
		
		// when 'external' the stage should be freely movable
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.BOTTOM);
		booster.setRelativeToStage(1);
		// vv function under test
		double targetOffset = +4.0;
		booster.setAxialOffset(targetOffset);
		// ^^ function under test
		
		double expectedX = +12.5;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the root component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testAxial_setTOP_getABSOLUTE() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.TOP);
		booster.setRelativeToStage(1);
		booster.setAxialOffset(targetOffset);
		
		double expectedAxialOffset = +12.0;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedAxialOffset));
		
		Stage refStage = core;
		// vv function under test
		double resultantAxialPosition = booster.asPositionValue(Position.ABSOLUTE, refStage);
		// ^^ function under test
		
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialOffset));
	}
	
	@Test
	public void testAxial_setTOP_getAFTER() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.TOP);
		booster.setRelativeToStage(1);
		booster.setAxialOffset(targetOffset);
		
		Coordinate expectedPosition = new Coordinate(+12.0, 0., 0.);
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		
		Stage refStage = core;
		double resultantAxialPosition;
		double expectedAxialPosition;
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.AFTER, refStage);
		// ^^ function under test
		expectedAxialPosition = -1.5;
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialPosition));
	}
	
	@Test
	public void testAxial_setTOP_getMIDDLE() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		
		double targetOffset = +4.50;
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.TOP);
		booster.setRelativeToStage(1);
		booster.setAxialOffset(targetOffset);
		
		Coordinate expectedPosition = new Coordinate(+12.0, 0., 0.);
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		
		Stage refStage = core;
		double resultantAxialPosition;
		double expectedAxialPosition = +4.0;
		
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.MIDDLE, refStage);
		// ^^ function under test
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialPosition));
	}
	
	@Test
	public void testAxial_setTOP_getBOTTOM() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		
		double targetOffset = +4.50;
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.TOP);
		booster.setRelativeToStage(1);
		booster.setAxialOffset(targetOffset);
		
		Coordinate expectedPosition = new Coordinate(+12.0, 0., 0.);
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		
		Stage refStage = core;
		double resultantAxialPosition;
		double expectedAxialPosition;
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.BOTTOM, refStage);
		// ^^ function under test
		expectedAxialPosition = +3.5;
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialPosition));
	}
	
	
	@Test
	public void testAxial_setBOTTOM_getTOP() {
		// setup
		RocketComponent root = createTestRocket();
		Stage core = (Stage) root.getChild(1);
		Stage booster = createBooster();
		root.addChild(booster);
		
		
		double targetOffset = +4.50;
		booster.setOutside(true);
		booster.setRelativePositionMethod(Position.BOTTOM);
		booster.setRelativeToStage(1);
		booster.setAxialOffset(targetOffset);
		
		Coordinate expectedPosition = new Coordinate(+13.0, 0., 0.);
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		
		Stage refStage = core;
		double resultantAxialPosition;
		double expectedAxialPosition;
		
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.TOP, refStage);
		// ^^ function under test
		expectedAxialPosition = 5.5;
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAxialPosition));
	}
	
	@Test
	public void testInitializationOrder() {
		Rocket root = createTestRocket();
		Stage boosterA = createBooster();
		root.addChild(boosterA);
		Stage boosterB = createBooster();
		root.addChild(boosterB);
		
		double targetOffset = +4.50;
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     two boosters with identical initialization commands should end up at the same place. 
		
		boosterA.setOutside(true);
		boosterA.setRelativePositionMethod(Position.BOTTOM);
		boosterA.setRelativeToStage(1);
		boosterA.setAxialOffset(targetOffset);
		
		boosterB.dumpDetail();
		boosterB.setRelativePositionMethod(Position.TOP);
		System.err.println("  B: setMeth: " + boosterB.getRelativePositionVector().x);
		boosterB.setRelativeToStage(1);
		System.err.println("  B: setRelTo: " + boosterB.getRelativePositionVector().x);
		boosterB.setAxialOffset(targetOffset);
		System.err.println("  B: setOffs: " + boosterB.getRelativePositionVector().x);
		boosterB.setRelativePositionMethod(Position.BOTTOM);
		System.err.println("  B: setMeth: " + boosterB.getRelativePositionVector().x);
		boosterB.setOutside(true);
		System.err.println("  B: setOutside:" + boosterB.getRelativePositionVector().x);
		
		root.dumpTree(true, "");
		
		double offsetA = boosterA.getAxialOffset();
		double offsetB = boosterB.getAxialOffset();
		
		assertThat(" init order error: Booster A: resultant positions: ", offsetA, equalTo(targetOffset));
		assertThat(" init order error: Booster B: resultant positions: ", offsetB, equalTo(targetOffset));
		
		
	}
	
	
}
