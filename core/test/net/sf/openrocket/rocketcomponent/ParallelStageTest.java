package net.sf.openrocket.rocketcomponent;

//import junit.framework.TestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class ParallelStageTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.00001;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);
	
	public void test() {
		//		fail("Not yet implemented");
	}
	
	public Rocket createTestRocket() {
		double tubeRadius = 1.2;
		// setup
		Rocket rocket = new Rocket();
		rocket.setName("Rocket");
		
		AxialStage sustainer = new AxialStage();
		sustainer.setName("Sustainer stage");
		RocketComponent sustainerNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		sustainerNose.setName("Sustainer Nosecone");
		sustainer.addChild(sustainerNose);
		RocketComponent sustainerBody = new BodyTube(3.0, tubeRadius, 0.01);
		sustainerBody.setName("Sustainer Body ");
		sustainer.addChild(sustainerBody);
		rocket.addChild(sustainer);
		
		AxialStage core = new AxialStage();
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

		rocket.enableEvents();
		return rocket;
	}
	
	public ParallelStage createBooster() {
		double tubeRadius = 0.8;
		
		ParallelStage strapon = new ParallelStage();
		strapon.setName("Booster Stage");
		RocketComponent boosterNose = new NoseCone(Transition.Shape.CONICAL, 2.0, tubeRadius);
		boosterNose.setName("Booster Nosecone");
		strapon.addChild(boosterNose);
		RocketComponent boosterBody = new BodyTube(2.0, tubeRadius, 0.01);
		boosterBody.setName("Booster Body ");
		strapon.addChild(boosterBody);
		Transition boosterTail = new Transition();
		boosterTail.setName("Booster Tail");
		boosterTail.setForeRadius(1.0);
		boosterTail.setAftRadius(0.5);
		boosterTail.setLength(1.0);
		strapon.addChild(boosterTail);
		
		strapon.setInstanceCount(3);
		strapon.setRadialOffset(1.8);
		strapon.setAutoRadialOffset(false);
		
		return strapon;
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
		resultPosition = rocket.getOffset();
		assertThat(" Moved the rocket rocket itself-- this should not be enabled.", expectedPosition.x, equalTo(resultPosition.x));
		
	}
	
	@Test
	public void testCreateSustainer() {
		RocketComponent rocket = createTestRocket();
		
		// Sustainer Stage
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		RocketComponent sustainerNose = sustainer.getChild(0);
		RocketComponent sustainerBody = sustainer.getChild(1);
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer stage?  ", sustainer.isAncestor(sustainer), equalTo(false));
		assertThat(" createTestRocket failed: is sustainer stage an ancestor of the sustainer nose? ", sustainer.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is the rocket rocket an ancestor of the sustainer Nose?  ", rocket.isAncestor(sustainerNose), equalTo(true));
		assertThat(" createTestRocket failed: is sustainer Body an ancestor of the sustainer Nose?  ", sustainerBody.isAncestor(sustainerNose), equalTo(false));
		
		String rocketTree = rocket.toDebugTree();
		
		int relToExpected = -1;
		int relToStage = sustainer.getRelativeToStage();
		assertThat(" createTestRocket failed: sustainer relative position: ", relToStage, equalTo(relToExpected));
		
		double expectedSustainerLength = 5.0;
		assertThat(" createTestRocket failed: Sustainer size: ", sustainer.getLength(), equalTo(expectedSustainerLength));
		double expectedSustainerX = 0;
		double sustainerX;
		sustainerX = sustainer.getOffset().x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer Relative position: ", sustainerX, equalTo(expectedSustainerX));
		sustainerX = sustainer.getLocations()[0].x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer Absolute position: ", sustainerX, equalTo(expectedSustainerX));
		
		double expectedSustainerNoseX = 0;
		double sustainerNosePosition = sustainerNose.getOffset().x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer Nose X position: ", sustainerNosePosition, equalTo(expectedSustainerNoseX));
		expectedSustainerNoseX = 0;
		sustainerNosePosition = sustainerNose.getLocations()[0].x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer Nose X position: ", sustainerNosePosition, equalTo(expectedSustainerNoseX));
		
		double expectedSustainerBodyX = 2;
		double sustainerBodyX = sustainerBody.getOffset().x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer body rel X position: ", sustainerBodyX, equalTo(expectedSustainerBodyX));
		expectedSustainerBodyX = 2;
		sustainerBodyX = sustainerBody.getLocations()[0].x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " sustainer body abs X position: ", sustainerBodyX, equalTo(expectedSustainerBodyX));
		
	}
	
	// WARNING:   this test will not pass unless 'testAddTopStage' is passing as well -- that function tests the dependencies...
	@Test
	public void testAddCoreStage() {
		// vvvv function under test vvvv  ( which indirectly tests initialization code, and that the test setup creates the preconditions that we expect 		
		RocketComponent rocket = createTestRocket();
		// ^^^^ function under test ^^^^
		String rocketTree = rocket.toDebugTree();
		
		// Core Stage
		AxialStage core = (AxialStage) rocket.getChild(1);
		double expectedCoreLength = 6.0;
		assertThat(" createTestRocket failed: Core size: ", core.getLength(), equalTo(expectedCoreLength));
		double expectedCoreX = 5;
		double coreX;
		
		int relToExpected = 0;
		int relToStage = core.getRelativeToStage();
		assertThat(" createTestRocket failed:\n" + rocketTree + " core relative position: ", relToStage, equalTo(relToExpected));
		
		coreX = core.getOffset().x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " core Relative position: ", coreX, equalTo(expectedCoreX));
		coreX = core.getLocations()[0].x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " core Absolute position: ", coreX, equalTo(expectedCoreX));
		
		RocketComponent coreUpperBody = core.getChild(0);
		double expectedX = 0;
		double resultantX = coreUpperBody.getOffset().x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " core body rel X: ", resultantX, equalTo(expectedX));
		expectedX = expectedCoreX;
		resultantX = coreUpperBody.getLocations()[0].x;
		assertThat(" createTestRocket failed:\n" + rocketTree + " core body abs X: ", resultantX, equalTo(expectedX));
		
		RocketComponent coreLowerBody = core.getChild(1);
		expectedX = coreUpperBody.getLength();
		resultantX = coreLowerBody.getOffset().x;
		assertEquals(" createTestRocket failed:\n" + rocketTree + " core body rel X: ", expectedX, resultantX, EPSILON);
		expectedX = expectedCoreX + coreUpperBody.getLength();
		resultantX = coreLowerBody.getLocations()[0].x;
		assertEquals(" createTestRocket failed:\n" + rocketTree + " core body abs X: ", expectedX, resultantX, EPSILON);
		
		
		RocketComponent coreFins = coreLowerBody.getChild(0);
		// default is offset=0, method=0
		expectedX = 0.2;
		resultantX = coreFins.getOffset().x;
		assertEquals(" createTestRocket failed:\n" + rocketTree + " core Fins rel X: ", expectedX, resultantX, EPSILON);
		// 5 + 1.8 + 4.2 = 11
		//                 11 - 4 = 7;
		expectedX = 7.0;
		resultantX = coreFins.getLocations()[0].x;
		assertEquals(" createTestRocket failed:\n" + rocketTree + " core Fins abs X: ", expectedX, resultantX, EPSILON);
		
	}


	@Test
	public void testStageAncestry() {
		RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		AxialStage core = (AxialStage) rocket.getChild(1);
		AxialStage booster = (AxialStage) core.getChild(1);
		
		AxialStage sustainerPrev = sustainer.getPreviousStage();
		assertThat("sustainer parent is not found correctly: ", sustainerPrev, equalTo(null));
		
		AxialStage corePrev = core.getPreviousStage();
		assertThat("core parent is not found correctly: ", corePrev, equalTo(sustainer));
		
		AxialStage boosterPrev = booster.getPreviousStage();
		assertThat("booster parent is not found correctly: ", boosterPrev, equalTo(core));
	}
	
	@Test
	public void testSetStagePosition_topOfStack() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		Coordinate expectedPosition = new Coordinate(0, 0., 0.); // i.e. half the tube length
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		
		// without making the rocket 'external' and the Stage should be restricted to AFTER positioning.
		sustainer.setRelativePositionMethod(Position.ABSOLUTE);
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.isAfter(), equalTo(true));
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.getRelativePosition(), equalTo(Position.AFTER));
		
		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		String rocketTree = rocket.toDebugTree();
		
		Coordinate resultantRelativePosition = sustainer.getOffset();
		assertThat(" 'setAxialPosition(double)' failed:\n" + rocketTree + " Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed:\n" + rocketTree + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
		
	}
	
	@Test
	public void testBoosterInitializationSimple() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage set0 = createBooster();
		core.addChild(set0);
		
		double targetOffset = 0;
		set0.setAxialOffset(Position.BOTTOM, targetOffset);
		
		// vvvv function under test
		set0.setInstanceCount(2);
		set0.setRadialOffset(4.0); 
		set0.setAngularOffset(Math.PI / 2);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		int expectedInstanceCount = 2;
		int instanceCount = set0.getInstanceCount();
		assertThat(" 'setInstancecount(int)' failed: ", instanceCount, equalTo(expectedInstanceCount));
		
		double expectedAbsX = 6.0;
		double resultantX = set0.getLocations()[0].x;
		assertEquals(">>'setAxialOffset()' failed:\n" + treeDump + "  1st Inst absolute position", expectedAbsX, resultantX, EPSILON);
		
		double expectedRadialOffset = 4.0;
		double radialOffset = set0.getRadialOffset();
		assertEquals(" 'setRadialOffset(double)' failed: \n" + treeDump + "  radial offset: ", expectedRadialOffset, radialOffset, EPSILON);
		
		double expectedAngularOffset = Math.PI / 2;
		double angularOffset = set0.getAngularOffset();
		assertEquals(" 'setAngularOffset(double)' failed:\n" + treeDump + "  angular offset: ", expectedAngularOffset, angularOffset, EPSILON);
	}
	
	@Test
	public void testBoosterInitializationAutoRadius() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage set0 = createBooster();
		core.addChild(set0);
		
		double targetOffset = 0;
		set0.setAxialOffset(Position.BOTTOM, targetOffset);
		// vvvv function under test
		set0.setAutoRadialOffset(true);
		set0.setRadialOffset(4.0);  // this called will be overriden by the AutoRadialOffset above
		// ^^^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedRadialOffset = 2.2;
		double radialOffset = set0.getRadialOffset();
		assertEquals(" 'setRadialOffset(double)' failed: \n" + treeDump + "  radial offset: ", expectedRadialOffset, radialOffset, EPSILON);
	}
	


	@Test
	public void testAddStraponAuto() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage strapons = createBooster();
		core.addChild( strapons);
		
		double targetXOffset = +1.0;
		strapons.setAxialOffset(Position.BOTTOM, targetXOffset);
		double targetRadialOffset = 0.01;
		// vv function under test
		strapons.setRadialOffset(targetRadialOffset);
		strapons.setAutoRadialOffset(true);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();

		double expectedRadialOffset = core.getOuterRadius() + strapons.getOuterRadius();
		double actualRadialOffset = strapons.getRadialOffset();
		assertEquals(" 'setAutoRadialOffset()' failed:\n" + treeDump , expectedRadialOffset, actualRadialOffset, EPSILON);
		
//		Coordinate[] instanceAbsoluteCoords = set0.getLocations();
//		//		Coordinate[] instanceRelativeCoords = new Coordinate[] { componentAbsolutePosition };
//		//		instanceRelativeCoords = boosterSet.shiftCoordinates(instanceRelativeCoords);
//		
//		int inst = 0;
//		Coordinate expectedPosition0 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
//		Coordinate resultantPosition0 = instanceAbsoluteCoords[inst];
//		assertEquals(" 'setAngularOffset(double)' failed:\n" + treeDump + "  angular offset: ", resultantPosition0, equalTo(expectedPosition0));
//		
//		inst = 1;
//		Coordinate expectedPosition1 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
//		Coordinate resultantPosition1 = instanceAbsoluteCoords[inst];
//		assertThat(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", resultantPosition1, equalTo(expectedPosition1));
//		
//		inst = 2;
//		Coordinate expectedPosition2 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
//		Coordinate resultantPosition2 = instanceAbsoluteCoords[inst];
//		assertThat(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", resultantPosition2, equalTo(expectedPosition2));
//		
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testBoosterInstanceLocation_BOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage set0 = createBooster();
		core.addChild(set0);
		
		double targetOffset = 0;
		set0.setAxialOffset(Position.BOTTOM, targetOffset);
		int targetInstanceCount = 3;
		double targetRadialOffset = 1.8;
		// vv function under test
		set0.setInstanceCount(targetInstanceCount);
		set0.setRadialOffset(targetRadialOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedX = 6;
		double angle = Math.PI * 2 / targetInstanceCount;
		double radius = targetRadialOffset;
		
		Coordinate[] instanceAbsoluteCoords = set0.getLocations();
		//		Coordinate[] instanceRelativeCoords = new Coordinate[] { componentAbsolutePosition };
		//		instanceRelativeCoords = boosterSet.shiftCoordinates(instanceRelativeCoords);
		
		int inst = 0;
		Coordinate expectedPosition0 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition0 = instanceAbsoluteCoords[inst];
		assertThat(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", resultantPosition0, equalTo(expectedPosition0));
		
		inst = 1;
		Coordinate expectedPosition1 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition1 = instanceAbsoluteCoords[inst];
		assertThat(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", resultantPosition1, equalTo(expectedPosition1));
		
		inst = 2;
		Coordinate expectedPosition2 = new Coordinate(expectedX, radius * Math.cos(angle * inst), radius * Math.sin(angle * inst));
		Coordinate resultantPosition2 = instanceAbsoluteCoords[inst];
		assertThat(treeDump + "\n>> Failed to generate Parallel Stage instances correctly: ", resultantPosition2, equalTo(expectedPosition2));
		
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideABSOLUTE() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetX = +17.0;
		double expectedX = targetX - core.getLocations()[0].x;
		
		// when subStages should be freely movable		
		// vv function under test
		booster.setAxialOffset(Position.ABSOLUTE, targetX);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		Coordinate resultantRelativePosition = booster.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " PositionValue: ", resultantPositionValue, equalTo(targetX));
		double resultantAxialPosition = booster.getAxialOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantAxialPosition, equalTo(targetX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Absolute position: ", resultantAbsolutePosition.x, equalTo(targetX));
	}
	
	// WARNING:
	// Because even though this is an "outside" stage, it's relative to itself -- i.e. an error-condition  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideTopOfStack() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		int expectedRelativeIndex = -1;
		int resultantRelativeIndex = sustainer.getRelativeToStage();
		assertThat(" 'setRelativeToStage(int)' failed. Relative stage index:", expectedRelativeIndex, equalTo(resultantRelativeIndex));
		
		// vv function under test
		// when 'external' the stage should be freely movable		
		sustainer.setAxialOffset(Position.TOP, targetPosition.x);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedX = 0;
		Coordinate resultantRelativePosition = sustainer.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Sustainer Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		double expectedPositionValue = 0;
		double resultantPositionValue = sustainer.getPositionValue();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Sustainer Position Value: ", resultantPositionValue, equalTo(expectedPositionValue));
		
		double expectedAxialOffset = 0;
		double resultantAxialOffset = sustainer.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed: \n" + treeDump + " Relative position: ", resultantAxialOffset, equalTo(expectedAxialOffset));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getLocations()[0];
		assertThat(" 'setAbsolutePositionVector()' failed: \n" + treeDump + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
	}
	
	@Test
	public void testSetStagePosition_outsideTOP() {
		Rocket rocket = this.createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +2.0;
		
		// vv function under test
		booster.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = 2;
		double expectedAbsoluteX = 7;
		Coordinate resultantRelativePosition = booster.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + "  Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + "  Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed: \n" + treeDump + "  Axial Offset: ", resultantAxialOffset, equalTo(targetOffset));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Position Value: ", resultantPositionValue, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideMIDDLE() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		// when 'external' the stage should be freely movable
		// vv function under test
		double targetOffset = +2.0;
		booster.setAxialOffset(Position.MIDDLE, targetOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = 2.5;
		double expectedAbsoluteX = 7.5;
		Coordinate resultantRelativePosition = booster.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Position Value: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed:\n" + treeDump + " Axial Offset: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testSetStagePosition_outsideBOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		// vv function under test
		double targetOffset = +4.0;
		booster.setAxialOffset(Position.BOTTOM, targetOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = 5;
		double expectedAbsoluteX = +10;
		Coordinate resultantRelativePosition = booster.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = booster.getLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantPositionValue = booster.getPositionValue();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Position Value: ", resultantPositionValue, equalTo(targetOffset));
		
		double resultantAxialOffset = booster.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed: \n" + treeDump + " Axial Offset: ", resultantAxialOffset, equalTo(targetOffset));
	}
	
	@Test
	public void testAxial_setTOP_getABSOLUTE() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativePositionX = targetOffset;
		Coordinate resultantRelativePosition = booster.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativePositionX));
		
		// vv function under test
		double resultantAxialPosition = booster.asPositionValue(Position.ABSOLUTE);
		// ^^ function under test
		
		double expectedAbsoluteX = 9.5;
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " asPositionValue: ", resultantAxialPosition, equalTo(expectedAbsoluteX));
	}
	
	@Test
	public void testAxial_setTOP_getAFTER() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = targetOffset;
		double resultantX = booster.getOffset().x;
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantX, equalTo(expectedRelativeX));
		
		// vv function under test
		resultantX = booster.asPositionValue(Position.AFTER);
		// ^^ function under test
		
		double expectedAfterX = -1.5;
		assertEquals(" 'setPositionValue()' failed: \n" + treeDump + " asPosition: ", expectedAfterX, resultantX, EPSILON);
	}
	
	@Test
	public void testAxial_setTOP_getMIDDLE() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = targetOffset;
		double resultantX = booster.getOffset().x;
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantX, equalTo(expectedRelativeX));
		
		double resultantAxialPosition;
		double expectedAxialPosition = +4.0;
		// vv function under test
		resultantAxialPosition = booster.asPositionValue(Position.MIDDLE);
		// ^^ function under test
		
		assertEquals(" 'setPositionValue()' failed: \n" + treeDump + " Relative position: ", expectedAxialPosition, resultantAxialPosition, EPSILON);
	}
	
	@Test
	public void testAxial_setTOP_getBOTTOM() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.TOP, targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = targetOffset;
		double resultantX = booster.getOffset().x;
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", resultantX, equalTo(expectedRelativeX));
		
		// vv function under test
		double resultantAxialOffset = booster.asPositionValue(Position.BOTTOM);
		// ^^ function under test
		double expectedAxialOffset = +3.5;
		assertEquals(" 'setPositionValue()' failed: \n" + treeDump + " Relative position: ", expectedAxialOffset, resultantAxialOffset, EPSILON);
	}
	
	
	@Test
	public void testAxial_setBOTTOM_getTOP() {
		// setup
		RocketComponent rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage booster = createBooster();
		core.addChild(booster);
		
		double targetOffset = +4.50;
		booster.setAxialOffset(Position.BOTTOM, targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = +5.5;
		double resultantX = booster.getOffset().x;
		assertEquals(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", expectedRelativeX, resultantX, EPSILON);
		
		// vv function under test
		double resultantAxialOffset = booster.asPositionValue(Position.TOP);
		// ^^ function under test
		double expectedAxialOffset = expectedRelativeX;
		assertEquals(" 'setPositionValue()' failed: \n" + treeDump + " Relative position: ", expectedAxialOffset, resultantAxialOffset, EPSILON);
	}
	
	@Test
	public void testOutsideStageRepositionTOPAfterAdd() {
		final double boosterRadius = 0.8;
		Rocket rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		
		ParallelStage booster = new ParallelStage();
		booster.setName("Booster Stage");
		core.addChild(booster);
		final double targetOffset = +2.50;
		final Position targetMethod = Position.TOP;
		booster.setAxialOffset(targetMethod, targetOffset);
		String treeDumpBefore = rocket.toDebugTree();
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     a booster should retain it's positioning method and offset while adding on children
		double expectedRelativeX = 2.5;
		double resultantOffset = booster.getOffset().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " initial relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		double expectedAxialOffset = targetOffset;
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " Initial axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
		
		// Body Component 2
		RocketComponent boosterBody = new BodyTube(4.0, boosterRadius, 0.01);
		boosterBody.setName("Booster Body ");
		booster.addChild(boosterBody);
		
		String treeDumpAfter = rocket.toDebugTree();
		
		expectedRelativeX = 2.5; // no change
		resultantOffset = booster.getOffset().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		expectedAxialOffset = targetOffset; // again, no change
		resultantOffset = booster.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
	}
	
	@Test
	public void testStageInitializationMethodValueOrder() {
		Rocket rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage boosterA = createBooster();
		boosterA.setName("Booster A Stage");
		core.addChild(boosterA);
		ParallelStage boosterB = createBooster();
		boosterB.setName("Booster B Stage");
		core.addChild(boosterB);
		
		double targetOffset = +4.5;
		double expectedOffset = +4.5;
		// requirement:  regardless of initialization order (which we cannot control) 
		//     two boosters with identical initialization commands should end up at the same place. 
		
		boosterA.setAxialOffset(Position.TOP, targetOffset);
		
		boosterB.setRelativePositionMethod(Position.TOP);
		boosterB.setAxialOffset(targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double resultantOffsetA = boosterA.getOffset().x;
		double resultantOffsetB = boosterB.getOffset().x;
		
		assertEquals(" init order error: " + treeDump + " Booster A: resultant positions: ", expectedOffset, resultantOffsetA, EPSILON);
		assertEquals(" init order error: " + treeDump + " Booster B: resultant positions: ", expectedOffset, resultantOffsetB, EPSILON);
	}
	
	@Test
	public void testStageNumbering() {
		Rocket rocket = createTestRocket();
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		AxialStage core = (AxialStage) rocket.getChild(1);
		ParallelStage boosterA = createBooster();
		boosterA.setName("Booster A Stage");
		core.addChild(boosterA);
		boosterA.setAxialOffset(Position.BOTTOM, 0.0);
		ParallelStage boosterB = createBooster();
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
		
		// remove Booster A 
		core.removeChild(2);
		
		String treedump = rocket.toDebugTree();
		int expectedStageCount = 3;
		int actualStageCount = rocket.getStageCount();
		
		assertEquals(" Stage tracking error:  removed booster A, but count not updated: " + treedump, expectedStageCount, actualStageCount);
		actualStageCount = rocket.getSelectedConfiguration().getStageCount();
		assertEquals(" Stage tracking error:  removed booster A, but configuration not updated: " + treedump, expectedStageCount, actualStageCount);
		
		ParallelStage boosterC = createBooster();
		boosterC.setName("Booster C Stage");
		core.addChild(boosterC);
		boosterC.setAxialOffset(Position.BOTTOM, 0);
		
		expectedStageNumber = 2;
		actualStageNumber = boosterC.getStageNumber();
		assertEquals(" init order error: Booster B: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		//rocket.getDefaultConfiguration().dumpConfig();
	}
	
	@Test
	public void testToAbsolute() {
		Rocket rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		String treeDump = rocket.toDebugTree();
		
		Coordinate input = new Coordinate(3, 0, 0);
		Coordinate[] actual = core.toAbsolute(input);
		
		double expectedX = 8;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual[0].x, EPSILON);
	}
	
	@Test
	public void testToRelative() {
		Rocket rocket = createTestRocket();
		AxialStage core = (AxialStage) rocket.getChild(1);
		RocketComponent ubody = core.getChild(0);
		RocketComponent lbody = core.getChild(1);
		
		String treeDump = rocket.toDebugTree();
		
		Coordinate input = new Coordinate(1, 0, 0);
		Coordinate actual = core.toAbsolute(input)[0];
		
		double expectedX = 6;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual.x, EPSILON);
		
		input = new Coordinate(1, 0, 0);
		actual = ubody.toRelative(input, lbody)[0];
		
		expectedX = -0.8;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual.x, EPSILON);
		
		
		
	}
	
	
	
}
