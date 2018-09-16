package net.sf.openrocket.rocketcomponent;

//import junit.framework.TestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

import junit.framework.Assert;
import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class ParallelStageTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = 0.000001;
	
	/* From OpenRocket Technical Documentation
	 *  
	 * 3.1.4 Coordinate systems
	 * During calculation of the aerodynamic properties a coordinate system fixed to the rocket will be used. 
	 * The origin of the coordinates is at the nose cone tip with the positive x-axis directed along the rocket 
	 * when discussing the fins. During simulation, however, the y- and z-axes are fixed in relation to the rocket,
	 * and do not necessarily align with the plane of the pitching moments.
	 */

	public ParallelStage createExtraBooster() {
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
       strapon.setRadiusMethod( RadiusMethod.FREE );
       strapon.setRadiusOffset( 0.18 );
       
       return strapon;
   }

	@Test
	public void testSetRocketPositionFail() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();

		// case 1: the rocket Rocket should be stationary
		rocket.setAxialOffset( +4.8 );
		
		assertEquals( AxialMethod.ABSOLUTE, rocket.getAxialMethod() );
		assertEquals( 0, rocket.getAxialOffset(), EPSILON);
		assertEquals( 0, rocket.getPosition().x, EPSILON);		
	}
	
	@Test
	public void testCreatePayloadStage() {
		// vvvv function under test vvvv
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		// ^^^^ function under test ^^^^
		
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
		Assert.assertEquals( payloadStage.getPosition().x, expectedPayloadStageX, EPSILON);
		Assert.assertEquals( payloadStage.getComponentLocations()[0].x, expectedPayloadStageX, EPSILON);
		
		Assert.assertEquals( 0, payloadNose.getPosition().x, EPSILON);
		Assert.assertEquals( 0, payloadNose.getComponentLocations()[0].x, EPSILON);
		
		double expectedPayloadBodyX = payloadNose.getLength();
		Assert.assertEquals( payloadBody.getPosition().x, expectedPayloadBodyX, EPSILON);
		Assert.assertEquals( payloadBody.getComponentLocations()[0].x, expectedPayloadBodyX, EPSILON);	
	}
	
	// WARNING:   this test will not pass unless 'testAddTopStage' is passing as well -- that function tests the dependencies...
	@Test
	public void testCreateCoreStage() {
		// vvvv function under test vvvv
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		// ^^^^ function under test ^^^^

		// Payload Stage
		AxialStage payloadStage = (AxialStage)rocket.getChild(0);
		final double expectedPayloadLength = 0.564;
		final double payloadLength = payloadStage.getLength();
		Assert.assertEquals( payloadLength, expectedPayloadLength, EPSILON);

		// Core Stage
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		double expectedCoreLength = 0.8;
		assertThat(" createTestRocket failed: @ Core size: ", coreStage.getLength(), equalTo(expectedCoreLength));
		
		int relToExpected = 0;
		int relToStage = coreStage.getRelativeToStage();
		assertThat(" createTestRocket failed! @ core relative position: ", relToStage, equalTo(relToExpected));
		
		final double expectedCoreStageX = payloadLength;
		Assert.assertEquals( expectedCoreStageX, 0.564, EPSILON);
		Assert.assertEquals( coreStage.getPosition().x, expectedCoreStageX, EPSILON);
		Assert.assertEquals( coreStage.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);
		
		RocketComponent coreBody = coreStage.getChild(0);
		Assert.assertEquals( coreBody.getPosition().x, 0.0, EPSILON);
		Assert.assertEquals( coreBody.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);

	}


	@Test
	public void testStageAncestry() {
		RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		
		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		AxialStage booster = (AxialStage) coreStage.getChild(0).getChild(0);
		
		AxialStage sustainerPrev = sustainer.getUpperStage();
		assertThat("sustainer parent is not found correctly: ", sustainerPrev, equalTo(null));
		
		AxialStage corePrev = coreStage.getUpperStage();
		assertThat("core parent is not found correctly: ", corePrev, equalTo(sustainer));
		
		AxialStage boosterPrev = booster.getUpperStage();
		assertThat("booster parent is not found correctly: ", boosterPrev, equalTo(coreStage));
	}
	
	@Test
	public void testSetStagePosition_topOfStack() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();

		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		Coordinate expectedPosition = new Coordinate(0, 0., 0.); // i.e. half the tube length
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);
		
		
		// without making the rocket 'external' and the Stage should be restricted to AFTER positioning.
		sustainer.setAxialMethod(AxialMethod.ABSOLUTE);
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.isAfter(), equalTo(true));
		assertThat("Setting a centerline stage to anything other than AFTER is ignored.", sustainer.getAxialMethod(), equalTo(AxialMethod.AFTER));
		
		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		String rocketTree = rocket.toDebugTree();
		
		Coordinate resultantRelativePosition = sustainer.getPosition();
		assertThat(" 'setAxialPosition(double)' failed:\n" + rocketTree + " Relative position: ", resultantRelativePosition.x, equalTo(expectedPosition.x));
		// for all stages, the absolute position should equal the relative, because the direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getComponentLocations()[0];
		assertThat(" 'setAxialPosition(double)' failed:\n" + rocketTree + " Absolute position: ", resultantAbsolutePosition.x, equalTo(expectedPosition.x));
		
	}
	
	@Test
	public void testBoosterInitializationFREERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterSet = (ParallelStage)coreStage.getChild(0).getChild(0);

		// vvvv function under test
		parallelBoosterSet.setRadiusMethod( RadiusMethod.FREE );
		parallelBoosterSet.setRadiusOffset(2.0);
		// ^^ function under test
		
		assertThat(" 'setInstancecount(int)' failed: ", 2, equalTo(parallelBoosterSet.getInstanceCount()));
		
		assertFalse( RadiusMethod.FREE.clampToZero());
		assertEquals(" error while setting radius method: ", RadiusMethod.FREE, parallelBoosterSet.getRadiusMethod() );
		assertEquals(" error while setting radius offset: ", 2.0, parallelBoosterSet.getRadiusOffset(), EPSILON);

		assertEquals(" error while setting radius offset: ", 2.0, parallelBoosterSet.getInstanceLocations()[0].y, EPSILON);
	}
	
	@Test
	public void testBoosterInitializationSURFACERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);

		final BodyTube coreBody = (BodyTube)coreStage.getChild(0);
		final BodyTube boosterBody = (BodyTube)parallelBoosterStage.getChild(1);
		
		// vvvv function under test
		parallelBoosterStage.setRadiusMethod( RadiusMethod.SURFACE );
		
	    // for the 'SURFACE' method, above, this call should have no effect.
		parallelBoosterStage.setRadiusOffset(4.0);
		// ^^^^ function under test
		
		assertThat(" 'setInstancecount(int)' failed: ", 2, equalTo(parallelBoosterStage.getInstanceCount()));
		
		assertTrue( RadiusMethod.SURFACE.clampToZero());
		assertEquals(" error while setting radius method: ", RadiusMethod.SURFACE, parallelBoosterStage.getRadiusMethod() );
		assertEquals(" error while setting radius offset: ", 0.0, parallelBoosterStage.getRadiusOffset(), EPSILON);
		
		final double expectedRadius = coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		{
			final Coordinate actualInstanceOffsets[] = parallelBoosterStage.getInstanceOffsets();
			
			assertEquals(" error while setting radius offset: ", 0, actualInstanceOffsets[0].x, EPSILON);
			assertEquals(" error while setting radius offset: ", expectedRadius, actualInstanceOffsets[0].y, EPSILON);
			
			assertEquals(" error while setting radius offset: ", 0, actualInstanceOffsets[1].x, EPSILON);
			assertEquals(" error while setting radius offset: ", -expectedRadius, actualInstanceOffsets[1].y, EPSILON);
		}{
			final Coordinate actualLocations[] = parallelBoosterStage.getComponentLocations();
			
			assertEquals(" error while setting radius offset: ", 0.484, actualLocations[0].x, EPSILON);
			assertEquals(" error while setting radius offset: ", expectedRadius, actualLocations[0].y, EPSILON);
			
			assertEquals(" error while setting radius offset: ", 0.484, actualLocations[1].x, EPSILON);
			assertEquals(" error while setting radius offset: ", -expectedRadius, actualLocations[1].y, EPSILON);
		}
	}
	

	@Test
	public void testBoosterInitializationRELATIVERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);

		final BodyTube coreBody = (BodyTube)coreStage.getChild(0);
		final BodyTube boosterBody = (BodyTube)parallelBoosterStage.getChild(1);
		
		// vv function under test
		parallelBoosterStage.setAxialOffset( AxialMethod.BOTTOM, 0.0 );
		final double targetRadiusOffset = 0.01;
		parallelBoosterStage.setRadius( RadiusMethod.RELATIVE, targetRadiusOffset );
		// ^^ function under test

		assertFalse(RadiusMethod.RELATIVE.clampToZero());
		assertEquals(" error while setting radius method: ", RadiusMethod.RELATIVE, parallelBoosterStage.getRadiusMethod() );
		assertEquals(" error while setting radius offset: ", targetRadiusOffset, parallelBoosterStage.getRadiusOffset() , EPSILON);
		
		final double expectedRadius = targetRadiusOffset + coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		{
			final Coordinate actualInstanceOffsets[] = parallelBoosterStage.getInstanceOffsets();
			
			assertEquals(" error while setting radius offset: ", 0, actualInstanceOffsets[0].x, EPSILON);
			assertEquals(" error while setting radius offset: ", expectedRadius, actualInstanceOffsets[0].y, EPSILON);
			
			assertEquals(" error while setting radius offset: ", 0, actualInstanceOffsets[1].x, EPSILON);
			assertEquals(" error while setting radius offset: ", -expectedRadius, actualInstanceOffsets[1].y, EPSILON);
		}{
			final Coordinate actualLocations[] = parallelBoosterStage.getComponentLocations();
			
			assertEquals(" error while setting radius offset: ", 0.484, actualLocations[0].x, EPSILON);
			assertEquals(" error while setting radius offset: ", expectedRadius, actualLocations[0].y, EPSILON);
			
			assertEquals(" error while setting radius offset: ", 0.484, actualLocations[1].x, EPSILON);
			assertEquals(" error while setting radius offset: ", -expectedRadius, actualLocations[1].y, EPSILON);
		}
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testBoosterInstanceLocation_BOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube)coreStage.getChild(0);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		final BodyTube boosterBody = (BodyTube)boosterStage.getChild(1);

		// vv function under test
		int targetInstanceCount = 3;
		boosterStage.setInstanceCount(targetInstanceCount);
		boosterStage.setRadiusMethod( RadiusMethod.SURFACE );
		// ^^ function under test
		
		assertEquals( targetInstanceCount, boosterStage.getInstanceCount() );
		
		final double expectedX = 0.484;
		final double expectedRadiusOffset = coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		final double angleIncr = Math.PI * 2 / targetInstanceCount;
		
		Coordinate[] instanceAbsoluteCoords = boosterStage.getComponentLocations();

		for( int index = 0; index < targetInstanceCount; ++index ) {
			final Coordinate actualPosition = instanceAbsoluteCoords[index];
			assertEquals(String.format("At index=%d, radius=%.6g, angle=%.6g",index, expectedRadiusOffset, angleIncr*index), expectedX, actualPosition.x, EPSILON );
			
			final double expectedY = expectedRadiusOffset * Math.cos(angleIncr * index);
			assertEquals(String.format("At index=%d, radius=%.6g, angle=%.6g",index, expectedRadiusOffset, angleIncr*index), expectedY, actualPosition.y, EPSILON );
			
			final double expectedZ = expectedRadiusOffset * Math.sin(angleIncr * index);
			assertEquals(String.format("At index=%d, radius=%.6g, angle=%.6g",index, expectedRadiusOffset, angleIncr*index), expectedZ, actualPosition.z, EPSILON );
		}
		
	}
	
	// because even though this is an "outside" stage, it's relative to itself -- i.e. an error.  
	// also an error with a well-defined failure result (i.e. just failover to AFTER placement as the first stage of a rocket. 
	@Test
	public void testSetStagePosition_outsideABSOLUTE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final BodyTube coreBody= (BodyTube) rocket.getChild(1).getChild(0);
		final ParallelStage boosterStage = (ParallelStage)coreBody.getChild(0);
		
		double targetAbsoluteX = 0.8;
		double expectedRelativeX = 0.236;
		double expectedAbsoluteX = 0.8;

		// when substages should be freely movable
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.ABSOLUTE, targetAbsoluteX);
		// ^^ function under test

		assertEquals("setAxialOffset( method, double) failed: ", AxialMethod.ABSOLUTE, boosterStage.getAxialMethod() );
		assertEquals("setAxialOffset( method, double) failed: ", targetAbsoluteX, boosterStage.getAxialOffset(), EPSILON );
				
		double actualRelativeX = boosterStage.getAxialOffset(AxialMethod.TOP);
		assertEquals(" 'setAxialPosition(double)' failed: Relative position: ", expectedRelativeX, actualRelativeX, EPSILON );

		double actualAbsoluteX = boosterStage.getComponentLocations()[0].x;
		assertEquals(" 'setAxialPosition(double)' failed: Absolute position: ", expectedAbsoluteX, actualAbsoluteX, EPSILON);

	}
	
	@Test
	public void testSetStagePosition_centerline() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);

		int expectedRelativeIndex = -1;
		int resultantRelativeIndex = payloadStage.getRelativeToStage();
		assertThat(" 'setRelativeToStage(int)' failed. Relative stage index:", expectedRelativeIndex, equalTo(resultantRelativeIndex));
		
		// vv function under test
		// a centerline stage is not freely movable		
		payloadStage.setAxialOffset(AxialMethod.TOP, 4.0 );
		// ^^ function under test
			
		assertEquals("setAxialPosition( Method, double) ", AxialMethod.AFTER, payloadStage.getAxialMethod() );
		assertEquals("setAxialPosition( Method, double) ", 0.0, payloadStage.getAxialOffset(), EPSILON );

		assertEquals("setAxialPosition( Method, double) ", 0.0, payloadStage.getPosition().x, EPSILON );
		
		assertEquals("setAxialPosition( Method, double) ", RadiusMethod.COAXIAL, payloadStage.getRadiusMethod() );
		assertEquals("setAxialPosition( Method, double) ", 0.0, payloadStage.getRadiusOffset(), EPSILON );

		assertEquals("setAxialPosition( Method, double) ", 0.0, payloadStage.getComponentLocations()[0].x, EPSILON );
	}
	
	@Test
	public void testSetStagePosition_outsideTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test
		
		String treeDump = rocket.toDebugTree();
		
		double expectedRelativeX = 0.2;
		double expectedAbsoluteX = 0.764;
		Coordinate resultantRelativePosition = boosterStage.getPosition();
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
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);

		// when 'external' the stage should be freely movable
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.MIDDLE, targetOffset);
		// ^^ function under test

		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );

		Assert.assertEquals( 0.16, boosterStage.getPosition().x, EPSILON );

		Assert.assertEquals( 0.724, boosterStage.getComponentLocations()[0].x, EPSILON );
	}
	
	@Test
	public void testSetBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.BOTTOM, targetOffset);
		// ^^ function under test

		Assert.assertEquals( 0.120, boosterStage.getPosition().x, EPSILON);

		Assert.assertEquals( 0.684,  boosterStage.getComponentLocations()[0].x, EPSILON);
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON);
	}
	
	@Test
	public void testSetTOP_getABSOLUTE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( targetOffset, boosterStage.getPosition().x, EPSILON );
		
		final double expectedRelativePositionX = 0.2;
		final double resultantRelativePosition = boosterStage.getPosition().x;
		Assert.assertEquals(expectedRelativePositionX, resultantRelativePosition, EPSILON);
				
		// vv function under test
		final double actualAbsoluteX = boosterStage.getAxialOffset(AxialMethod.ABSOLUTE);
		// ^^ function under test
		
		Assert.assertEquals( 0.764, actualAbsoluteX, EPSILON );
	}
	
	@Test
	public void testSetTOP_getAFTER() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getPosition().x, EPSILON );
	
		
		// vv function under test
		double actualPositionXAfter = boosterStage.getAxialOffset(AxialMethod.AFTER);
		// ^^ function under test
		
		Assert.assertEquals( -0.6, actualPositionXAfter, EPSILON );
	}
	
	@Test
	public void testSetTOP_getMIDDLE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getPosition().x, EPSILON );
			
		// vv function under test
		final double actualAxialPosition = boosterStage.getAxialOffset(AxialMethod.MIDDLE);
		// ^^ function under test
		
		Assert.assertEquals( 0.24, actualAxialPosition, EPSILON );
	}
	
	@Test
	public void testSetTOP_getBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		double targetOffset = 0.2;
		
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test
		
		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON );
		Assert.assertEquals( 0.2, boosterStage.getPosition().x, EPSILON );
		
		// vv function under test
		double actualAxialBottomOffset = boosterStage.getAxialOffset(AxialMethod.BOTTOM);
		// ^^ function under test
		
		Assert.assertEquals( 0.28, actualAxialBottomOffset, EPSILON );
	}
	
	
	@Test
	public void testSetBOTTOM_getTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.BOTTOM, targetOffset);
		// ^^ function under test

		Assert.assertEquals( targetOffset, boosterStage.getAxialOffset(), EPSILON);
		Assert.assertEquals( 0.120, boosterStage.getPosition().x, EPSILON);
		
		// vv function under test
		double actualAxialTopOffset = boosterStage.getAxialOffset(AxialMethod.TOP);
		// ^^ function under test
				
		Assert.assertEquals( 0.12, actualAxialTopOffset, EPSILON);
	}
	
	@Test
	public void testOutsideStageRepositionTOPAfterAdd() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage)coreStage.getChild(0).getChild(0);
		
		final double targetOffset = +2.50;
		final AxialMethod targetMethod = AxialMethod.TOP;
		boosterStage.setAxialOffset(targetMethod, targetOffset);
		String treeDumpBefore = rocket.toDebugTree();
		
		// requirement:  regardless of initialization order (which we cannot control) 
		//     a booster should retain it's positioning method and offset while adding on children
		double expectedRelativeX = 2.5;
		double resultantOffset = boosterStage.getPosition().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " initial relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		double expectedAxialOffset = targetOffset;
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " Initial axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
				
		String treeDumpAfter = rocket.toDebugTree();
		
		expectedRelativeX = 2.5; // no change
		resultantOffset = boosterStage.getPosition().x;
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated relative X: ", expectedRelativeX, resultantOffset, EPSILON);
		expectedAxialOffset = targetOffset; // again, no change
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(" init order error: Booster: " + treeDumpBefore + " =======> " + treeDumpAfter + " populated axial offset: ", expectedAxialOffset, resultantOffset, EPSILON);
	}
	
	@Test
	public void testStageInitializationMethodValueOrder() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube)coreStage.getChild(0);
		
		
		ParallelStage boosterA = createExtraBooster();
		boosterA.setName("Booster A Stage");
		coreBody.addChild(boosterA);
		ParallelStage boosterB = createExtraBooster();
		boosterB.setName("Booster B Stage");
		coreBody.addChild(boosterB);
		
		double targetOffset = +4.5;
		double expectedOffset = +4.5;
		// requirement:  regardless of initialization order (which we cannot control) 
		//     two boosters with identical initialization commands should end up at the same place. 
		
		boosterA.setAxialOffset(AxialMethod.TOP, targetOffset);
		
		boosterB.setAxialMethod(AxialMethod.TOP);
		boosterB.setAxialOffset(targetOffset);
		String treeDump = rocket.toDebugTree();
		
		double resultantOffsetA = boosterA.getPosition().x;
		double resultantOffsetB = boosterB.getPosition().x;
		
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
		
		ParallelStage boosterA = (ParallelStage)coreBody.getChild(0);
		
		ParallelStage boosterB = createExtraBooster();
		boosterB.setName("Booster A Stage");
		coreBody.addChild(boosterB);
		boosterB.setAxialOffset(AxialMethod.BOTTOM, 0.0);
		
		ParallelStage boosterC = createExtraBooster();
		boosterC.setName("Booster B Stage");
		coreBody.addChild(boosterC);
		boosterC.setAxialOffset(AxialMethod.BOTTOM, 0);
		
		
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
		
		// remove Booster B
		coreBody.removeChild(1);
		
		String treedump = rocket.toDebugTree();
		int expectedStageCount = 4;
		int actualStageCount = config.getStageCount();
		
		assertEquals(" Stage tracking error:  removed booster A, but count not updated: " + treedump, expectedStageCount, actualStageCount);
		actualStageCount = rocket.getSelectedConfiguration().getStageCount();
		assertEquals(" Stage tracking error:  removed booster A, but configuration not updated: " + treedump, expectedStageCount, actualStageCount);
		
		ParallelStage boosterD = createExtraBooster();
		boosterC.setName("Booster D Stage");
		coreBody.addChild(boosterD);
		boosterC.setAxialOffset(AxialMethod.BOTTOM, 0);
		
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
