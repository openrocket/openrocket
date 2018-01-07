package net.sf.openrocket.rocketcomponent;

//import junit.framework.TestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import junit.framework.Assert;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class ParallelStageTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.000001;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);
	
	
	/* From OpenRocket Technical Documentation
	 *  
	 * 3.1.4 Coordinate systems
	 * During calculation of the aerodynamic properties a coordinate system fixed to the rocket will be used. 
	 * The origin of the coordinates is at the nose cone tip with the positive x-axis directed along the rocket 
	@@ -41,70 +35,302 @@ public class BodyTubeTest extends TestCase {
	 * when discussing the fins. During simulation, however, the y- and z-axes are fixed in relation to the rocket, 
	 * and do not necessarily align with the plane of the pitching moments.
	 */

	
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

	@Test
	public void testSetRocketPositionFail() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
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
	public void testPayload() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		
		// Sustainer Stage
		AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		RocketComponent payloadNose = payloadStage.getChild(0);
		RocketComponent payloadBody = payloadStage.getChild(1);
		assertThat(" createTestRocket failed: is payload stage an ancestor of the payload stage?  ", payloadStage.isAncestor(payloadStage), equalTo(false));
		assertThat(" createTestRocket failed: is payload stage an ancestor of the payload nose? ", payloadStage.isAncestor(payloadNose), equalTo(true));
		assertThat(" createTestRocket failed: is the rocket an ancestor of the sustainer Nose?  ", rocket.isAncestor(payloadNose), equalTo(true));
		assertThat(" createTestRocket failed: is payload Body an ancestor of the payload Nose?  ", payloadBody.isAncestor(payloadNose), equalTo(false));
		
		int relToExpected = -1;
		int relToStage = payloadStage.getRelativeToStage();
		assertThat(" createTestRocket failed: sustainer relative position: ", relToStage, equalTo(relToExpected));
		
		double expectedPayloadLength = 0.564;
		Assert.assertEquals( payloadStage.getLength(), expectedPayloadLength, EPSILON);
		
		double expectedPayloadStageX = 0;
		Assert.assertEquals( payloadStage.getOffset().x, expectedPayloadStageX, EPSILON);
		Assert.assertEquals( payloadStage.getComponentLocations()[0].x, expectedPayloadStageX, EPSILON);
		
		double expectedPayloadNoseX = 0;
		Assert.assertEquals( payloadNose.getOffset().x, expectedPayloadNoseX, EPSILON);
		Assert.assertEquals( payloadNose.getComponentLocations()[0].x, expectedPayloadNoseX, EPSILON);
		
		double expectedPayloadBodyX = payloadNose.getLength();
		Assert.assertEquals( payloadBody.getOffset().x, expectedPayloadBodyX, EPSILON);
		Assert.assertEquals( payloadBody.getComponentLocations()[0].x, expectedPayloadBodyX, EPSILON);	
	}
	
	// WARNING:   this test will not pass unless 'testAddTopStage' is passing as well -- that function tests the dependencies...
	@Test
	public void testCoreStage() {
		// vvvv function under test vvvv  ( which indirectly tests initialization code, and that the test setup creates the preconditions that we expect 		
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		// ^^^^ function under test ^^^^
		
		String rocketTree = rocket.toDebugTree();
		
		// Payload Stage
		AxialStage payloadStage = (AxialStage)rocket.getChild(0);
		final double expectedPayloadLength = 0.564;
		final double payloadLength = payloadStage.getLength();
		Assert.assertEquals( payloadLength, expectedPayloadLength, EPSILON);

		// Core Stage
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		double expectedCoreLength = 0.8;
		assertThat(" createTestRocket failed: Core size: ", coreStage.getLength(), equalTo(expectedCoreLength));
		
		int relToExpected = 0;
		int relToStage = coreStage.getRelativeToStage();
		assertThat(" createTestRocket failed:\n" + rocketTree + " core relative position: ", relToStage, equalTo(relToExpected));
		
		final double expectedCoreStageX = payloadLength;
		Assert.assertEquals( expectedCoreStageX, 0.564, EPSILON);
		Assert.assertEquals( coreStage.getOffset().x, expectedCoreStageX, EPSILON);
		Assert.assertEquals( coreStage.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);
		
		
		RocketComponent coreBody = coreStage.getChild(0);
		Assert.assertEquals( coreBody.getOffset().x, 0.0, EPSILON);
		Assert.assertEquals( coreBody.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);
		
		FinSet coreFins = (FinSet)coreBody.getChild(0);
		
		// default is offset=0, method=BOTTOM
		assertEquals( Position.BOTTOM, coreFins.getRelativePosition() );
		assertEquals( 0.0, coreFins.getAxialOffset(), EPSILON);
		
		assertEquals( 0.480, coreFins.getOffset().x, EPSILON);

		assertEquals( 1.044, coreFins.getComponentLocations()[0].x, EPSILON);

	}


	@Test
	public void testStageAncestry() {
		RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		AxialStage booster = (AxialStage) coreStage.getChild(0).getChild(1);
		
		AxialStage sustainerPrev = sustainer.getUpperStage();
		assertThat("sustainer parent is not found correctly: ", sustainerPrev, equalTo(null));
		
		AxialStage corePrev = coreStage.getUpperStage();
		assertThat("core parent is not found correctly: ", corePrev, equalTo(sustainer));
		
		AxialStage boosterPrev = booster.getUpperStage();
		assertThat("booster parent is not found correctly: ", boosterPrev, equalTo(coreStage));
	}
	
	@Test
	public void testSetStagePosition_topOfStack() {
		// setup
		Rocket rocket = TestRockets.makeFalcon9Heavy();
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
		Coordinate resultantAbsolutePosition = sustainer.getComponentLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed:\n" + rocketTree + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
		
	}
	
	@Test
	public void testBoosterInitializationSimple() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);

		double targetOffset = 0;
		boosterStage .setAxialOffset(Position.BOTTOM, targetOffset);
		
		// vvvv function under test
		boosterStage.setInstanceCount(2);
		boosterStage.setRadialOffset(4.0); 
		boosterStage.setAngularOffset(Math.PI / 2);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		int expectedInstanceCount = 2;
		int instanceCount = boosterStage.getInstanceCount();
		assertThat(" 'setInstancecount(int)' failed: ", instanceCount, equalTo(expectedInstanceCount));
		
		double expectedAbsX = 0.484;
		double resultantX = boosterStage.getComponentLocations()[0].x;
		assertEquals(">>'setAxialOffset()' failed:\n" + treeDump + "  1st Inst absolute position", expectedAbsX, resultantX, EPSILON);
		
		double expectedRadialOffset = 4.0;
		double radialOffset = boosterStage.getRadialOffset();
		assertEquals(" 'setRadialOffset(double)' failed: \n" + treeDump + "  radial offset: ", expectedRadialOffset, radialOffset, EPSILON);
		
		double expectedAngularOffset = Math.PI / 2;
		double angularOffset = boosterStage.getAngularOffset();
		assertEquals(" 'setAngularOffset(double)' failed:\n" + treeDump + "  angular offset: ", expectedAngularOffset, angularOffset, EPSILON);
	}
	
	@Test
	public void testBoosterInitializationAutoRadius() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0;
		boosterStage.setAxialOffset(Position.BOTTOM, targetOffset);
		// vvvv function under test
		boosterStage.setAutoRadialOffset(true);
		boosterStage.setRadialOffset(4.0);  // this call will be overriden by the AutoRadialOffset above
		// ^^^^ function under test
		
		double expectedRadialOffset = 0.077;
		double radialOffset = boosterStage.getRadialOffset();
		assertEquals(" 'setRadialOffset(double)' failed for radial offset: ", expectedRadialOffset, radialOffset, EPSILON);
	}
	


	@Test
	public void testAddStraponAuto() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetXOffset = +1.0;
		boosterStage.setAxialOffset(Position.BOTTOM, targetXOffset);
		double targetRadialOffset = 0.01;
		// vv function under test
		boosterStage.setRadialOffset(targetRadialOffset);
		boosterStage.setAutoRadialOffset(true);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();

		double expectedRadialOffset = coreStage.getOuterRadius() + boosterStage.getOuterRadius();
		double actualRadialOffset = boosterStage.getRadialOffset();
		assertEquals(" 'setAutoRadialOffset()' failed:\n" + treeDump , expectedRadialOffset, actualRadialOffset, EPSILON);
		
//		Coordinate[] instanceAbsoluteCoords = set0.getComponentLocations;
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
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0;
		boosterStage.setAxialOffset(Position.BOTTOM, targetOffset);
		int targetInstanceCount = 3;
		double targetRadialOffset = 1.8;
		// vv function under test
		boosterStage.setInstanceCount(targetInstanceCount);
		boosterStage.setRadialOffset(targetRadialOffset);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedX = 0.484;
		double angle = Math.PI * 2 / targetInstanceCount;
		double radius = targetRadialOffset;
		
		Coordinate[] instanceAbsoluteCoords = boosterStage.getComponentLocations();
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
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final BodyTube coreBody= (BodyTube) rocket.getChild(1).getChild(0);
		final ParallelStage boosterStage = (ParallelStage)coreBody.getChild(1);
		
		double targetAbsoluteX = 0.8;
		double expectedRelativeX = 0.236;
		double expectedAbsoluteX = 0.8;
		
		// when subStages should be freely movable		
		// vv function under test
		boosterStage.setAxialOffset(Position.ABSOLUTE, targetAbsoluteX);
		// ^^ function under test
		
		String treeDump = rocket.toDebugTree();
		
		double actualAxialOffset = boosterStage.getAxialOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Absolute position: ", actualAxialOffset, equalTo(expectedAbsoluteX));
		
		double actualRelativeX = boosterStage.asPositionValue(Position.TOP);
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Relative position: ", actualRelativeX, equalTo(expectedRelativeX));

		double actualAbsoluteX = boosterStage.getComponentLocations()[0].x;
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Absolute position: ", actualAbsoluteX, equalTo(expectedAbsoluteX));
	}
	
	// WARNING:
	// Because even though this is an "outside" stage, it's relative to itself -- i.e. an error-condition  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideTopOfStack() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);
//		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
//		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
	    Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);

		int expectedRelativeIndex = -1;
		int resultantRelativeIndex = payloadStage.getRelativeToStage();
		assertThat(" 'setRelativeToStage(int)' failed. Relative stage index:", expectedRelativeIndex, equalTo(resultantRelativeIndex));
		
		// vv function under test
		// when 'external' the stage should be freely movable		
		payloadStage.setAxialOffset(Position.TOP, targetPosition.x);
		// ^^ function under test
		String treeDump = rocket.toDebugTree();
		
		double expectedX = 0;
		Coordinate resultantRelativePosition = payloadStage.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + " Sustainer Relative position: ", resultantRelativePosition.x, equalTo(expectedX));
		double expectedPositionValue = 0;
		double resultantPositionValue = payloadStage.getAxialOffset();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Sustainer Position Value: ", resultantPositionValue, equalTo(expectedPositionValue));
		
		double expectedAxialOffset = 0;
		double resultantAxialOffset = payloadStage.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed: \n" + treeDump + " Relative position: ", resultantAxialOffset, equalTo(expectedAxialOffset));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = payloadStage.getComponentLocations()[0];
		assertThat(" 'setAbsolutePositionVector()' failed: \n" + treeDump + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedX));
	}
	
	@Test
	public void testSetStagePosition_outsideTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = 0.2;
		double expectedAbsoluteX = 0.764;
		Coordinate resultantRelativePosition = boosterStage.getOffset();
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + "  Relative position: ", resultantRelativePosition.x, equalTo(expectedRelativeX));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = boosterStage.getComponentLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed: \n" + treeDump + "  Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedAbsoluteX));
		
		double resultantAxialOffset = boosterStage.getAxialOffset();
		assertThat(" 'getAxialPosition()' failed: \n" + treeDump + "  Axial Offset: ", resultantAxialOffset, equalTo(targetOffset));
		
		double resultantPositionValue = boosterStage.getAxialOffset();
		assertThat(" 'setPositionValue()' failed: \n" + treeDump + " Position Value: ", resultantPositionValue, equalTo(targetOffset));
	}
	
	@Test
	public void testSetMIDDLE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		// when 'external' the stage should be freely movable
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(Position.MIDDLE, targetOffset);
		// ^^ function under test

		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		
		Assert.assertEquals( 0.16, boosterStage.getOffset().x, EPSILON );
		
		Assert.assertEquals( 0.724, boosterStage.getComponentLocations()[0].x, EPSILON );
		
	}
	
	@Test
	public void testSetBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final ParallelStage boosterStage = (ParallelStage)rocket.getChild(1).getChild(0).getChild(1);
		
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(Position.BOTTOM, targetOffset);
		// ^^ function under test

		Assert.assertEquals( 0.120, boosterStage.getOffset().x, EPSILON);

		Assert.assertEquals( 0.684,  boosterStage.getComponentLocations()[0].x, EPSILON);
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON);
	}
	
	@Test
	public void testSetTOP_getABSOLUTE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getOffset().x, EPSILON );
		
		final double expectedRelativePositionX = targetOffset;
		final double resultantRelativePosition = boosterStage.getOffset().x;
		Assert.assertEquals(expectedRelativePositionX, resultantRelativePosition, EPSILON);
				
		// vv function under test
		final double actualAbsoluteX = boosterStage.asPositionValue(Position.ABSOLUTE);
		// ^^ function under test
		
		Assert.assertEquals( 0.764, actualAbsoluteX, EPSILON );
	}
	
	@Test
	public void testSetTOP_getAFTER() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getOffset().x, EPSILON );
	
		
		// vv function under test
		double actualPositionXAfter = boosterStage.asPositionValue(Position.AFTER);
		// ^^ function under test
		
		Assert.assertEquals( -0.6, actualPositionXAfter, EPSILON );
	}
	
	@Test
	public void testSetTOP_getMIDDLE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getOffset().x, EPSILON );
			
		// vv function under test
		final double actualAxialPosition = boosterStage.asPositionValue(Position.MIDDLE);
		// ^^ function under test
		
		Assert.assertEquals( 0.24, actualAxialPosition, EPSILON );
	}
	
	@Test
	public void testSetTOP_getBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(Position.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getOffset().x, EPSILON );
		
		// vv function under test
		double actualAxialBottomOffset = boosterStage.asPositionValue(Position.BOTTOM);
		// ^^ function under test
		
		Assert.assertEquals( 0.28, actualAxialBottomOffset, EPSILON );
	}
	
	
	@Test
	public void testSetBOTTOM_getTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final ParallelStage boosterStage = (ParallelStage)rocket.getChild(1).getChild(0).getChild(1);
		
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(Position.BOTTOM, targetOffset);
		// ^^ function under test

		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON);
		Assert.assertEquals( 0.120, boosterStage.getOffset().x, EPSILON);
		
		// vv function under test
		double actualAxialTopOffset = boosterStage.asPositionValue(Position.TOP);
		// ^^ function under test
				
		Assert.assertEquals( 0.12, actualAxialTopOffset, EPSILON);
	}
	
	@Test
	public void testOutsideStageRepositionTOPAfterAdd() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(1);
		
		final double targetOffset = +2.50;
		final Position targetMethod = Position.TOP;
		boosterStage.setAxialOffset(targetMethod, targetOffset);
		String treeDumpBefore = rocket.toDebugTree();
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     a booster should retain it's positioning method and offset while adding on children
		double expectedRelativeX = 2.5;
		double resultantOffset = boosterStage.getOffset().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " initial relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		double expectedAxialOffset = targetOffset;
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " Initial axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
				
		String treeDumpAfter = rocket.toDebugTree();
		
		expectedRelativeX = 2.5; // no change
		resultantOffset = boosterStage.getOffset().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		expectedAxialOffset = targetOffset; // again, no change
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
	}
	
	@Test
	public void testStageInitializationMethodValueOrder() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final BodyTube coreBody = (BodyTube) rocket.getChild(1).getChild(0);
		
		ParallelStage boosterA = createBooster();
		boosterA.setName("Booster A Stage");
		coreBody.addChild(boosterA);
		ParallelStage boosterB = createBooster();
		boosterB.setName("Booster B Stage");
		coreBody.addChild(boosterB);
		
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
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
		
		ParallelStage boosterA = (ParallelStage)coreBody.getChild(1);
		
		ParallelStage boosterB = createBooster();
		boosterB.setName("Booster A Stage");
		coreBody.addChild(boosterB);
		boosterB.setAxialOffset(Position.BOTTOM, 0.0);
		
		ParallelStage boosterC = createBooster();
		boosterC.setName("Booster B Stage");
		coreBody.addChild(boosterC);
		boosterC.setAxialOffset(Position.BOTTOM, 0);
		
		
		int expectedStageNumber = 0;
		int actualStageNumber = payloadStage.getStageNumber();
		assertEquals(" init order error: sustainer: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		expectedStageNumber = 1;
		actualStageNumber = coreStage.getStageNumber();
		assertEquals(" init order error: core: resultant positions: ", expectedStageNumber, actualStageNumber);

		expectedStageNumber = 2;
		actualStageNumber = boosterA.getStageNumber();
		assertEquals(" init order error: core: resultant positions: ", expectedStageNumber, actualStageNumber);

		expectedStageNumber = 3;
		actualStageNumber = boosterB.getStageNumber();
		assertEquals(" init order error: Booster A: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		expectedStageNumber = 4;
		actualStageNumber = boosterC.getStageNumber();
		assertEquals(" init order error: Booster B: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		// remove Booster A 
		coreBody.removeChild(2);
		
		String treedump = rocket.toDebugTree();
		int expectedStageCount = 4;
		int actualStageCount = config.getStageCount();
		
		assertEquals(" Stage tracking error:  removed booster A, but count not updated: " + treedump, expectedStageCount, actualStageCount);
		actualStageCount = rocket.getSelectedConfiguration().getStageCount();
		assertEquals(" Stage tracking error:  removed booster A, but configuration not updated: " + treedump, expectedStageCount, actualStageCount);
		
		ParallelStage boosterD = createBooster();
		boosterC.setName("Booster D Stage");
		coreBody.addChild(boosterD);
		boosterC.setAxialOffset(Position.BOTTOM, 0);
		
		expectedStageNumber = 3;
		actualStageNumber = boosterD.getStageNumber();
		assertEquals(" init order error: Booster D: resultant positions: ", expectedStageNumber, actualStageNumber);
		
		//rocket.getDefaultConfiguration().dumpConfig();
	}
	
	@Test
	public void testToAbsolute() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		String treeDump = rocket.toDebugTree();
		
		Coordinate input = new Coordinate(3, 0, 0);
		Coordinate[] actual = coreStage.toAbsolute(input);
		
		double expectedX = 3.564;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual[0].x, EPSILON);
	}
	
	@Test
	public void testToRelative() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		
		RocketComponent payloadNose = payloadStage.getChild(1);
		RocketComponent payloadBody = payloadStage.getChild(3);
		
		String treeDump = rocket.toDebugTree();
		
		Coordinate input = new Coordinate(1, 0, 0);
		Coordinate actual = payloadStage.toAbsolute(input)[0];
		
		double expectedX = 1.0;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual.x, EPSILON);
		
		input = new Coordinate(1, 0, 0);
		actual = payloadNose.toRelative(input, payloadBody)[0];
		
		expectedX = 0.853999;
		assertEquals(treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ", expectedX, actual.x, EPSILON);
		
		
		
	}
	
	
	
}
