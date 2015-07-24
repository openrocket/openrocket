package net.sf.openrocket.rocketcomponent;

//import junit.framework.TestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class StageTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.00001;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);
	
	public void test() {
		//		fail("Not yet implemented");
	}
	
	public Rocket createTestRocket() {
		double tubeRadius = 1;
		// setup
		Rocket rocket = new Rocket();
		rocket.setName("Rocket");
		
		Stage sustainer = new Stage();
		sustainer.setName("Sustainer stage");
		RocketComponent sustainerNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		sustainerNose.setName("Sustainer Nosecone");
		sustainer.addChild(sustainerNose);
		RocketComponent sustainerBody = new BodyTube(3.0, tubeRadius, 0.01);
		sustainerBody.setName("Sustainer Body ");
		sustainer.addChild(sustainerBody);
		rocket.addChild(sustainer);
		
		Stage core = new Stage();
		core.setName("Core stage");
		rocket.addChild(core);
		BodyTube coreUpperBody = new BodyTube(1.8, tubeRadius, 0.01);
		coreUpperBody.setName("Core UpBody ");
		core.addChild(coreUpperBody);
		BodyTube coreLowerBody = new BodyTube(4.2, tubeRadius, 0.01);
		coreLowerBody.setName("Core LoBody ");
		core.addChild(coreLowerBody);
		FinSet coreFins = new TrapezoidFinSet(4, 4, 2, 2, 4);
		coreFins.setName("Core Fins");
		coreLowerBody.addChild(coreFins);
		return rocket;
	}
	
	public Stage createBooster() {
		double tubeRadius = 0.8;
		
		Stage booster = new Stage();
		booster.setName("Booster Stage");
		booster.setOutside(true);
		RocketComponent boosterNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		boosterNose.setName("Booster Nosecone");
		booster.addChild(boosterNose);
		RocketComponent boosterBody = new BodyTube(2.0, tubeRadius, 0.01);
		boosterBody.setName("Booster Body ");
		booster.addChild(boosterBody);
		Transition boosterTail = new Transition();
		boosterTail.setName("Booster Tail");
		boosterTail.setForeRadius(1.0);
		boosterTail.setAftRadius(0.5);
		boosterTail.setLength(1.0);
		booster.addChild(boosterTail);
		
		booster.setInstanceCount(3);
		booster.setRadialOffset(1.8);
		
		return booster;
	}
	
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
		RocketComponent rocket = createTestRocket();
		Coordinate expectedPosition;
		Coordinate targetPosition;
		Coordinate resultPosition;
		
		// case 1: the rocket Rocket should be stationary
		expectedPosition = ZERO;
		targetPosition = new Coordinate(+4.0, 0.0, 0.0);
		rocket.setAxialOffset(targetPosition.x);
		resultPosition = rocket.getRelativePositionVector();
		assertThat(" Moved the rocket rocket itself-- this should not be enabled.", expectedPosition.x, equalTo(resultPosition.x));
		
	}
	
	@Test
	public void testAddSustainerStage() {
		RocketComponent rocket = createTestRocket();
		
		// Sustainer Stage
		Stage sustainer = (Stage) rocket.getChild(0);
		RocketComponent sustainerNose = sustainer.getChild(0);
		RocketComponent sustainerBody = sustainer.getChild(1);
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer stage?  ", sustainer.isAncestor(sustainer), equalTo(false));
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer nose? ", sustainer.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is the rocket rocket an ancestor of the sustainer Nose?  ", rocket.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is sustainer Body an ancestor of the sustainer Nose?  ", sustainerBody.isAncestor(sustainerNose), equalTo(false));
		
		int relToExpected = -1;
		int relToStage = sustainer.getRelativeToStage();
		assertThat(" createTestRocket failed: sustainer relative position: ", relToStage, equalTo(relToExpected));
		
		double expectedSustainerLength = 5.0;
		assertThat(" createTestRocket failed: Sustainer size: ", sustainer.getLength(), equalTo(expectedSustainerLength));
		double expectedSustainerX = +2.5;
		double sustainerX;
		sustainerX = sustainer.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: sustainer Relative position: ", sustainerX, equalTo(expectedSustainerX));
		sustainerX = sustainer.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: sustainer Absolute position: ", sustainerX, equalTo(expectedSustainerX));
		
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
	public void testAddCoreStage() {
		// vvvv function under test vvvv  ( which indirectly tests initialization code, and that the test setup creates the preconditions that we expect 		
		RocketComponent rocket = createTestRocket();
		// ^^^^ function under test ^^^^
		
		// Core Stage
		Stage core = (Stage) rocket.getChild(1);
		double expectedCoreLength = 6.0;
		assertThat(" createTestRocket failed: Core size: ", core.getLength(), equalTo(expectedCoreLength));
		double expectedCoreX = +8.0;
		double coreX;
		
		int relToExpected = 0;
		int relToStage = core.getRelativeToStage();
		assertThat(" createTestRocket failed: corerelative position: ", relToStage, equalTo(relToExpected));
		
		coreX = core.getRelativePositionVector().x;
		assertThat(" createTestRocket failed: core Relative position: ", coreX, equalTo(expectedCoreX));
		coreX = core.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: core Absolute position: ", coreX, equalTo(expectedCoreX));
		
		RocketComponent coreUpBody = core.getChild(0);
		double expectedX = -2.1;
		double resultantX = coreUpBody.getRelativePositionVector().x;
		
		assertThat(" createTestRocket failed: core body rel X: ", resultantX, equalTo(expectedX));
		expectedX = 5.9;
		resultantX = coreUpBody.getAbsolutePositionVector().x;
		assertThat(" createTestRocket failed: core body abs X: ", resultantX, equalTo(expectedX));
		
		RocketComponent coreLoBody = core.getChild(1);
		expectedX = 0.9;
		resultantX = coreLoBody.getRelativePositionVector().x;
		assertEquals(" createTestRocket failed: core body rel X: ", expectedX, resultantX, EPSILON);
		expectedX = 8.9;
		resultantX = coreLoBody.getAbsolutePositionVector().x;
		assertEquals(" createTestRocket failed: core body abs X: ", expectedX, resultantX, EPSILON);
		
		RocketComponent coreFins = coreLoBody.getChild(0);
		expectedX = 0.1;
		resultantX = coreFins.getRelativePositionVector().x;
		assertEquals(" createTestRocket failed: core Fins rel X: ", expectedX, resultantX, EPSILON);
		expectedX = 9.0;
		resultantX = coreFins.getAbsolutePositionVector().x;
		assertEquals(" createTestRocket failed: core Fins abs X: ", expectedX, resultantX, EPSILON);
		
	}
	
	@Test
	public void testSetStagePosition_topOfStack() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage sustainer = (Stage) rocket.getChild(0);
		Coordinate expectedPosition = new Coordinate(+2.5, 0., 0.); // i.e. half the tube length
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		
		// without making the rocket 'external' and the Stage should be restricted to AFTER positioning.
		sustainer.setRelativePositionMethod(Position.ABSOLUTE);
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.getOutside(), equalTo(false));
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.getRelativePosition(), equalTo(Position.AFTER));
		
		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		
		Coordinate resultantRelativePosition = sustainer.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
		
	}
	
	@Test
	public void testBoosterInitialization() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage boosterSet = createBooster();
		core.addChild(boosterSet);
		
		double targetOffset = 0;
		boosterSet.setAxialOffset(Position.BOTTOM, targetOffset);
		// vv function under test
		boosterSet.setInstanceCount(2);
		boosterSet.setRadialOffset(4.0);
		boosterSet.setAngularOffset(Math.PI / 2);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		int expectedInstanceCount = 2;
		int instanceCount = boosterSet.getInstanceCount();
		assertThat(" 'setInstancecount(int)' failed: ", instanceCount, equalTo(expectedInstanceCount));
		
		double expectedAbsX = 8.5;
		Coordinate resultantCenter = boosterSet.getAbsolutePositionVector();
		assertEquals(treeDump + "\n>>'setAxialOffset()' failed: ", expectedAbsX, resultantCenter.x, EPSILON);
		
		double expectedRadialOffset = 4.0;
		double radialOffset = boosterSet.getRadialOffset();
		assertEquals(" 'setRadialOffset(double)' failed. offset: ", expectedRadialOffset, radialOffset, EPSILON);
		
		double expectedAngularOffset = Math.PI / 2;
		double angularOffset = boosterSet.getAngularOffset();
		assertEquals(" 'setAngularOffset(double)' failed. offset: ", expectedAngularOffset, angularOffset, EPSILON);
	}
	
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testBoosterInstanceLocation_BOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage boosterSet = createBooster();
		core.addChild(boosterSet);
		
		double targetOffset = 0;
		boosterSet.setAxialOffset(Position.BOTTOM, targetOffset);
		int targetInstanceCount = 3;
		double targetRadialOffset = 1.8;
		// vv function under test
		boosterSet.setInstanceCount(targetInstanceCount);
		boosterSet.setRadialOffset(targetRadialOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedX = 8.5;
		double angle = Math.PI * 2 / targetInstanceCount;
		double radius = targetRadialOffset;
		
		Coordinate componentAbsolutePosition = boosterSet.getAbsolutePositionVector();
		Coordinate[] instanceCoords = new Coordinate[] { componentAbsolutePosition };
		instanceCoords = boosterSet.shiftCoordinates(instanceCoords);
		
		int inst = 0;
		Coordinate expectedPosition0 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition0 = instanceCoords[0];
		assertEquals(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", expectedPosition0, resultantPosition0);
		
		inst = 1;
		Coordinate expectedPosition1 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition1 = instanceCoords[1];
		assertEquals(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", expectedPosition1, resultantPosition1);
		
		inst = 2;
		Coordinate expectedPosition2 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition2 = instanceCoords[2];
		assertEquals(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", expectedPosition2, resultantPosition2);
		
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideABSOLUTE() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetX = +17.0;
		double expectedX = targetX - core.getAbsolutePositionVector().x;
		
		// when subStages should be freely movable		
		// vv function under test
		booster.setAxialOffset(Position.ABSOLUTE, targetX);
		// ^^ function under test
		
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setAxialPosition(double)' failed. PositionValue: ", resultantPositionValue, equalTo(targetX));
		double resultantAxialPosition = booster.getAxialOffset();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantAxialPosition, equalTo(targetX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(targetX));
	}
	
	// WARNING:
	// Because even though this is an "outside" stage, it's relative to itself -- i.e. an error-condition  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideTopOfStack() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage sustainer = (Stage) rocket.getChild(0);
		Coordinate expectedPosition = new Coordinate(+2.5, 0., 0.);
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		// when 'external' the stage should be freely movable		
		sustainer.setRelativePositionMethod(Position.TOP);
		
		int expectedRelativeIndex = -1;
		int resultantRelativeIndex = sustainer.getRelativeToStage();
		assertThat(" 'setRelativeToStage(int)' failed. Relative stage index:", expectedRelativeIndex, equalTo(resultantRelativeIndex));
		
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
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getAbsolutePositionVector();
		assertThat(" 'setAbsolutePositionVector()' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
	}
	
	@Test
	public void testSetStagePosition_outsideTOP() {
		Rocket rocket = this.createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +2.0;
		
		// vv function under test
		booster.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		double expectedAbsoluteX = +9.5;
		double expectedRelativeX = 1.5;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideMIDDLE() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		// when 'external' the stage should be freely movable
		// vv function under test
		double targetOffset = +2.0;
		booster.setAxialOffset(Position.MIDDLE, targetOffset);
		// ^^ function under test
		
		double expectedRelativeX = +2.0;
		double expectedAbsoluteX = +10.0;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideBOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		// vv function under test
		double targetOffset = +4.0;
		booster.setAxialOffset(Position.BOTTOM, targetOffset);
		// ^^ function under test
		
		double expectedRelativeX = +4.5;
		double expectedAbsoluteX = +12.5;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getAbsolutePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed. Relative position: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testAxial_setTOP_getABSOLUTE() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		
		double expectedAxialOffset = +4.0;
		Coordinate resultantRelativePosition = booster.getRelativePositionVector();
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantRelativePosition.x, equalTo(expectedAxialOffset));
		
		// vv function under test
		double resultantAxialPosition = booster.asPositionValue(Position.ABSOLUTE);
		// ^^ function under test
		
		double expectedAbsoluteX = +12.0;
		assertThat(" 'setPositionValue()' failed. Relative position: ", resultantAxialPosition, equalTo(expectedAbsoluteX));
	}
	
	@Test
	public void testAxial_setTOP_getAFTER() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		
		double expectedRelativeX = +4.0;
		double resultantX = booster.getRelativePositionVector().x;
		
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantX, equalTo(expectedRelativeX));
		
		// vv function under test
		// because this component is not initalized to 
		resultantX = booster.asPositionValue(Position.AFTER);
		// ^^ function under test
		
		double expectedAfterX = 4.5;
		assertEquals(" 'setPositionValue()' failed. Relative position: ", expectedAfterX, resultantX, EPSILON);
	}
	
	@Test
	public void testAxial_setTOP_getMIDDLE() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		
		double expectedRelativeX = +4.0;
		double resultantX = booster.getRelativePositionVector().x;
		assertThat(" 'setAxialPosition(double)' failed. Relative position: ", resultantX, equalTo(expectedRelativeX));
		
		double resultantAxialPosition;
		double expectedAxialPosition = +4.0;
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.MIDDLE);
		// ^^ function under test
		
		assertEquals(" 'setPositionValue()' failed. Relative position: ", expectedAxialPosition, resultantAxialPosition, EPSILON);
	}
	
	@Test
	public void testAxial_setTOP_getBOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		
		double expectedRelativeX = +4.0;
		double resultantX = booster.getRelativePositionVector().x;
		assertEquals(" 'setAxialPosition(double)' failed. Relative position: ", expectedRelativeX, resultantX, EPSILON);
		
		// vv function under test
		double resultantAxialOffset = booster.asPositionValue(Position.BOTTOM);
		// ^^ function under test
		double expectedAxialOffset = +3.5;
		assertEquals(" 'setPositionValue()' failed. Relative position: ", expectedAxialOffset, resultantAxialOffset, EPSILON);
	}
	
	
	@Test
	public void testAxial_setBOTTOM_getTOP() {
		// setup
		RocketComponent rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.BOTTOM, targetOffset);
		
		double expectedRelativeX = +5.0;
		double resultantX = booster.getRelativePositionVector().x;
		assertEquals(" 'setAxialPosition(double)' failed. Relative position: ", expectedRelativeX, resultantX, EPSILON);
		
		// vv function under test
		double resultantAxialOffset = booster.asPositionValue(Position.TOP);
		// ^^ function under test
		double expectedAxialOffset = 5.5;
		assertEquals(" 'setPositionValue()' failed. Relative position: ", expectedAxialOffset, resultantAxialOffset, EPSILON);
	}
	
	@Test
	public void testOutsideStageRepositionTOPAfterAdd() {
		final double boosterRadius = 0.8;
		final double targetOffset = +2.50;
		final Position targetMethod = Position.TOP;
		Rocket rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		
		Stage booster = new Stage();
		booster.setName("Booster Stage");
		core.addChild(booster);
		booster.setAxialOffset(targetMethod, targetOffset);
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     a booster should retain it's positioning method and offset while adding on children
		double expectedRelativeX = -0.5;
		double resultantOffset = booster.getRelativePositionVector().x;
		assertEquals(" init order error: Booster: initial relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		double expectedAxialOffset = targetOffset;
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: initial axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
		
		// Body Component 2
		RocketComponent boosterBody = new BodyTube(4.0, boosterRadius, 0.01);
		boosterBody.setName("Booster Body ");
		booster.addChild(boosterBody);
		
		expectedAxialOffset = targetOffset;
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: populated axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
		expectedRelativeX = 1.5;
		resultantOffset = booster.getRelativePositionVector().x;
		assertEquals(" init order error: Booster: populated relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		expectedAxialOffset = targetOffset;
		
	}
	
	@Test
	public void testOutsideStageRepositionBOTTOMAfterAdd() {
		final double boosterRadius = 0.8;
		final double targetOffset = +2.50;
		final Position targetMethod = Position.BOTTOM;
		Rocket rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		
		Stage booster = new Stage();
		booster.setName("Booster Stage");
		core.addChild(booster);
		booster.setAxialOffset(targetMethod, targetOffset);
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     a booster should retain it's positioning method and offset while adding on children
		double expectedRelativeX = 5.5;
		double resultantOffset = booster.getRelativePositionVector().x;
		assertEquals(" init order error: Booster: initial relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		double expectedAxialOffset = targetOffset;
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: initial axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
		
		// Body Component 2
		RocketComponent boosterBody = new BodyTube(4.0, boosterRadius, 0.01);
		boosterBody.setName("Booster Body ");
		booster.addChild(boosterBody);
		
		expectedAxialOffset = targetOffset;
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: populated axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
		expectedRelativeX = 3.5;
		resultantOffset = booster.getRelativePositionVector().x;
		assertEquals(" init order error: Booster: populated relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		expectedAxialOffset = targetOffset;
		
	}
	
	
	@Test
	public void testStageInitializationMethodValueOrder() {
		Rocket rocket = createTestRocket();
		Stage core = (Stage) rocket.getChild(1);
		Stage boosterA = createBooster();
		boosterA.setName("Booster A Stage");
		core.addChild(boosterA);
		Stage boosterB = createBooster();
		boosterB.setName("Booster B Stage");
		core.addChild(boosterB);
		
		double targetOffset = +4.50;
		double expectedOffset = +4.0;
		// requirement:  regardless of initialization order (which we cannot control) 
		//     two boosters with identical initialization commands should end up at the same place. 
		
		boosterA.setAxialOffset(Position.TOP, targetOffset);
		
		boosterB.setRelativePositionMethod(Position.TOP);
		boosterB.setAxialOffset(targetOffset);
		
		double resultantOffsetA = boosterA.getRelativePositionVector().x;
		double resultantOffsetB = boosterB.getRelativePositionVector().x;
		
		assertEquals(" init order error: Booster A: resultant positions: ", expectedOffset, resultantOffsetA, EPSILON);
		assertEquals(" init order error: Booster B: resultant positions: ", expectedOffset, resultantOffsetB, EPSILON);
		
	}
	
	
	@Test
	public void testStageNumbering() {
		Rocket rocket = createTestRocket();
		Stage sustainer = (Stage) rocket.getChild(0);
		Stage core = (Stage) rocket.getChild(1);
		Stage boosterA = createBooster();
		boosterA.setName("Booster A Stage");
		core.addChild(boosterA);
		boosterA.setAxialOffset(Position.BOTTOM, 0.0);
		Stage boosterB = createBooster();
		boosterB.setName("Booster B Stage");
		core.addChild(boosterB);
		boosterB.setAxialOffset(Position.BOTTOM, 0);
		
		
		int expectedStageNumber = 0;
		int actualStageNumber = sustainer.getStageNumber();
		assertEquals(" init order error: sustainer: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		expectedStageNumber = 1;
		actualStageNumber = core.getStageNumber();
		assertEquals(" init order error: core: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		expectedStageNumber = 2;
		actualStageNumber = boosterA.getStageNumber();
		assertEquals(" init order error: Booster A: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		expectedStageNumber = 3;
		actualStageNumber = boosterB.getStageNumber();
		assertEquals(" init order error: Booster B: resultant positions: ", expectedStageNumber, actualStageNumber);
		
	}
	
	
	
	
}
