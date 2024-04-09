package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.position.*;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class ParallelStageTest extends BaseTestCase {

	// tolerance for compared double test results
	protected final double EPSILON = 0.000001;

	/*
	 * From OpenRocket Technical Documentation
	 * 
	 * 3.1.4 Coordinate systems
	 * During calculation of the aerodynamic properties a coordinate system fixed to
	 * the rocket will be used.
	 * The origin of the coordinates is at the nose cone tip with the positive
	 * x-axis directed along the rocket
	 * when discussing the fins. During simulation, however, the y- and z-axes are
	 * fixed in relation to the rocket,
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
		strapon.setRadiusMethod(RadiusMethod.FREE);
		strapon.setRadiusOffset(0.18);

		return strapon;
	}

	@Test
	public void testSetRocketPositionFail() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();

		// case 1: the rocket Rocket should be stationary
		rocket.setAxialOffset(+4.8);

		assertEquals(AxialMethod.ABSOLUTE, rocket.getAxialMethod());
		assertEquals(0, rocket.getAxialOffset(), EPSILON);
		assertEquals(0, rocket.getPosition().x, EPSILON);
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
		assertFalse(payloadStage.isAncestor(payloadStage), " createTestRocket failed: is payload stage an ancestor of the payload stage?  ");
		assertTrue(payloadStage.isAncestor(payloadNose), " createTestRocket failed: is payload stage an ancestor of the payload nose? ");
		assertTrue(rocket.isAncestor(payloadNose), " createTestRocket failed: is the rocket an ancestor of the sustainer Nose?  ");
		assertFalse(payloadBody.isAncestor(payloadNose), " createTestRocket failed: is payload Body an ancestor of the payload Nose?  ");

		int relToExpected = -1;
		int relToStage = payloadStage.getRelativeToStage();
		assertEquals(relToStage, relToExpected, " createTestRocket failed: sustainer relative position: ");

		double expectedPayloadLength = 0.564;
		assertEquals(payloadStage.getLength(), expectedPayloadLength, EPSILON);

		double expectedPayloadStageX = 0;
		assertEquals(payloadStage.getPosition().x, expectedPayloadStageX, EPSILON);
		assertEquals(payloadStage.getComponentLocations()[0].x, expectedPayloadStageX, EPSILON);

		assertEquals(0, payloadNose.getPosition().x, EPSILON);
		assertEquals(0, payloadNose.getComponentLocations()[0].x, EPSILON);

		double expectedPayloadBodyX = payloadNose.getLength();
		assertEquals(payloadBody.getPosition().x, expectedPayloadBodyX, EPSILON);
		assertEquals(payloadBody.getComponentLocations()[0].x, expectedPayloadBodyX, EPSILON);
	}

	// WARNING: this test will not pass unless 'testAddTopStage' is passing as well
	// -- that function tests the dependencies...
	@Test
	public void testCreateCoreStage() {
		// vvvv function under test vvvv
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		// ^^^^ function under test ^^^^

		// Payload Stage
		AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		final double expectedPayloadLength = 0.564;
		final double payloadLength = payloadStage.getLength();
		assertEquals(payloadLength, expectedPayloadLength, EPSILON);

		// Core Stage
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		double expectedCoreLength = 0.8;
		assertEquals(coreStage.getLength(), expectedCoreLength, " createTestRocket failed: @ Core size: ");

		int relToExpected = 0;
		int relToStage = coreStage.getRelativeToStage();
		assertEquals(relToStage, relToExpected, " createTestRocket failed! @ core relative position: ");

		final double expectedCoreStageX = payloadLength;
		assertEquals(expectedCoreStageX, 0.564, EPSILON);
		assertEquals(coreStage.getPosition().x, expectedCoreStageX, EPSILON);
		assertEquals(coreStage.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);

		RocketComponent coreBody = coreStage.getChild(0);
		assertEquals(coreBody.getPosition().x, 0.0, EPSILON);
		assertEquals(coreBody.getComponentLocations()[0].x, expectedCoreStageX, EPSILON);

	}

	@Test
	public void testStageAncestry() {
		RocketComponent rocket = TestRockets.makeFalcon9Heavy();

		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		AxialStage booster = (AxialStage) coreStage.getChild(0).getChild(0);

		AxialStage sustainerPrev = sustainer.getUpperStage();
		assertNull(sustainerPrev, "sustainer parent is not found correctly: ");

		AxialStage corePrev = coreStage.getUpperStage();
		assertEquals(corePrev, sustainer, "core parent is not found correctly: ");

		AxialStage boosterPrev = booster.getUpperStage();
		assertEquals(boosterPrev, coreStage, "booster parent is not found correctly: ");
	}

	@Test
	public void testSetStagePosition_topOfStack() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();

		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		Coordinate expectedPosition = new Coordinate(0, 0., 0.); // i.e. half the tube length
		Coordinate targetPosition = new Coordinate(+4.0, 0., 0.);

		// without making the rocket 'external' and the Stage should be restricted to
		// AFTER positioning.
		sustainer.setAxialMethod(AxialMethod.ABSOLUTE);
		assertTrue(sustainer.isAfter(), "Setting a centerline stage to anything other than AFTER is ignored.");
		assertEquals(sustainer.getAxialMethod(),
				AxialMethod.AFTER, "Setting a centerline stage to anything other than AFTER is ignored.");

		// vv function under test
		sustainer.setAxialOffset(targetPosition.x);
		// ^^ function under test
		String rocketTree = rocket.toDebugTree();

		Coordinate resultantRelativePosition = sustainer.getPosition();
		assertEquals(resultantRelativePosition.x, expectedPosition.x, EPSILON,
				" 'setAxialPosition(double)' failed:\n" + rocketTree + " Relative position: ");
		// for all stages, the absolute position should equal the relative, because the
		// direct parent is the rocket component (i.e. the Rocket)
		Coordinate resultantAbsolutePosition = sustainer.getComponentLocations()[0];
		assertEquals(resultantAbsolutePosition.x, expectedPosition.x, EPSILON,
				" 'setAxialPosition(double)' failed:\n" + rocketTree + " Absolute position: ");

	}

	@Test
	public void testBoosterInitializationFREERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterSet = (ParallelStage) coreStage.getChild(0).getChild(0);

		// vvvv function under test
		parallelBoosterSet.setRadiusMethod(RadiusMethod.FREE);
		parallelBoosterSet.setRadiusOffset(2.0);
		// ^^ function under test

		assertEquals(2, parallelBoosterSet.getInstanceCount(), " 'setInstancecount(int)' failed: ");

		assertFalse(RadiusMethod.FREE.clampToZero());
		assertEquals(RadiusMethod.FREE, parallelBoosterSet.getRadiusMethod(), " error while setting radius method: ");
		assertEquals(2.0, parallelBoosterSet.getRadiusOffset(), EPSILON, " error while setting radius offset: ");

		assertEquals(2.0, parallelBoosterSet.getInstanceLocations()[0].y,
				EPSILON, " error while setting radius offset: ");
	}

	@Test
	public void testBoosterInitializationSURFACERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
		final BodyTube boosterBody = (BodyTube) parallelBoosterStage.getChild(1);

		// vvvv function under test
		parallelBoosterStage.setRadiusMethod(RadiusMethod.SURFACE);

		// for the 'SURFACE' method, above, this call should have no effect.
		parallelBoosterStage.setRadiusOffset(4.0);
		// ^^^^ function under test

		assertEquals(2, parallelBoosterStage.getInstanceCount(), " 'setInstancecount(int)' failed: ");

		assertTrue(RadiusMethod.SURFACE.clampToZero());
		assertEquals(RadiusMethod.SURFACE,
				parallelBoosterStage.getRadiusMethod(), " error while setting radius method: ");
		assertEquals(0.0, parallelBoosterStage.getRadiusOffset(), EPSILON, " error while setting radius offset: ");

		final double expectedRadius = coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		{
			final Coordinate[] actualInstanceOffsets = parallelBoosterStage.getInstanceOffsets();

			assertEquals(0, actualInstanceOffsets[0].x, EPSILON, " error while setting radius offset: ");
			assertEquals(expectedRadius, actualInstanceOffsets[0].y, EPSILON, " error while setting radius offset: ");

			assertEquals(0, actualInstanceOffsets[1].x, EPSILON, " error while setting radius offset: ");
			assertEquals(-expectedRadius, actualInstanceOffsets[1].y, EPSILON, " error while setting radius offset: ");
		}
		{
			final Coordinate[] actualLocations = parallelBoosterStage.getComponentLocations();

			assertEquals(0.484, actualLocations[0].x, EPSILON, " error while setting radius offset: ");
			assertEquals(expectedRadius, actualLocations[0].y, EPSILON, " error while setting radius offset: ");

			assertEquals(0.484, actualLocations[1].x, EPSILON, " error while setting radius offset: ");
			assertEquals(-expectedRadius, actualLocations[1].y, EPSILON, " error while setting radius offset: ");
		}
	}

	@Test
	public void testBoosterInitializationRELATIVERadius() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage parallelBoosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
		final BodyTube boosterBody = (BodyTube) parallelBoosterStage.getChild(1);

		// vv function under test
		parallelBoosterStage.setAxialOffset(AxialMethod.BOTTOM, 0.0);
		final double targetRadiusOffset = 0.01;
		parallelBoosterStage.setRadius(RadiusMethod.RELATIVE, RadiusMethod.RELATIVE
				.getRadius(parallelBoosterStage.getParent(), parallelBoosterStage, targetRadiusOffset));
		// ^^ function under test

		assertFalse(RadiusMethod.RELATIVE.clampToZero());
		assertEquals(RadiusMethod.RELATIVE,
				parallelBoosterStage.getRadiusMethod(), " error while setting radius method: ");
		assertEquals(targetRadiusOffset, parallelBoosterStage.getRadiusOffset(),
				EPSILON, " error while setting radius offset: ");

		final double expectedRadius = targetRadiusOffset + coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		{
			final Coordinate[] actualInstanceOffsets = parallelBoosterStage.getInstanceOffsets();

			assertEquals(0, actualInstanceOffsets[0].x, EPSILON, " error while setting radius offset: ");
			assertEquals(expectedRadius, actualInstanceOffsets[0].y, EPSILON, " error while setting radius offset: ");

			assertEquals(0, actualInstanceOffsets[1].x, EPSILON, " error while setting radius offset: ");
			assertEquals(-expectedRadius, actualInstanceOffsets[1].y, EPSILON, " error while setting radius offset: ");
		}
		{
			final Coordinate[] actualLocations = parallelBoosterStage.getComponentLocations();

			assertEquals(0.484, actualLocations[0].x, EPSILON, " error while setting radius offset: ");
			assertEquals(expectedRadius, actualLocations[0].y, EPSILON, " error while setting radius offset: ");

			assertEquals(0.484, actualLocations[1].x, EPSILON, " error while setting radius offset: ");
			assertEquals(-expectedRadius, actualLocations[1].y, EPSILON, " error while setting radius offset: ");
		}
	}

	// because even though this is an "outside" stage, it's relative to itself --
	// i.e. an error.
	// also an error with a well-defined failure result (i.e. just failover to AFTER
	// placement as the first stage of a rocket.
	@Test
	public void testBoosterInstanceLocation_BOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);
		final BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);

		// vv function under test
		int targetInstanceCount = 3;
		boosterStage.setInstanceCount(targetInstanceCount);
		boosterStage.setRadiusMethod(RadiusMethod.SURFACE);
		// ^^ function under test

		assertEquals(targetInstanceCount, boosterStage.getInstanceCount());

		final double expectedX = 0.484;
		final double expectedRadiusOffset = coreBody.getOuterRadius() + boosterBody.getOuterRadius();
		final double angleIncr = Math.PI * 2 / targetInstanceCount;

		Coordinate[] instanceAbsoluteCoords = boosterStage.getComponentLocations();

		for (int index = 0; index < targetInstanceCount; ++index) {
			final Coordinate actualPosition = instanceAbsoluteCoords[index];
			String txt = String.format("At index=%d, radius=%.6g, angle=%.6g", index, expectedRadiusOffset,
					angleIncr * index);
			assertEquals(expectedX, actualPosition.x, EPSILON, txt);

			final double expectedY = expectedRadiusOffset * Math.cos(angleIncr * index);
			assertEquals(expectedY, actualPosition.y, EPSILON, txt);

			final double expectedZ = expectedRadiusOffset * Math.sin(angleIncr * index);
			assertEquals(expectedZ, actualPosition.z, EPSILON, txt);
		}

	}

	// because even though this is an "outside" stage, it's relative to itself --
	// i.e. an error.
	// also an error with a well-defined failure result (i.e. just failover to AFTER
	// placement as the first stage of a rocket.
	@Test
	public void testSetStagePosition_outsideABSOLUTE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final BodyTube coreBody = (BodyTube) rocket.getChild(1).getChild(0);
		final ParallelStage boosterStage = (ParallelStage) coreBody.getChild(0);

		double targetAbsoluteX = 0.8;
		double expectedRelativeX = 0.236;
		double expectedAbsoluteX = 0.8;

		// when substages should be freely movable
		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.ABSOLUTE, targetAbsoluteX);
		// ^^ function under test

		assertEquals(AxialMethod.ABSOLUTE, boosterStage.getAxialMethod(), "setAxialOffset( method, double) failed: ");
		assertEquals(targetAbsoluteX, boosterStage.getAxialOffset(),
				EPSILON, "setAxialOffset( method, double) failed: ");

		double actualRelativeX = boosterStage.getAxialOffset(AxialMethod.TOP);
		assertEquals(expectedRelativeX, actualRelativeX,
				EPSILON, " 'setAxialPosition(double)' failed: Relative position: ");

		double actualAbsoluteX = boosterStage.getComponentLocations()[0].x;
		assertEquals(expectedAbsoluteX, actualAbsoluteX,
				EPSILON, " 'setAxialPosition(double)' failed: Absolute position: ");

	}

	@Test
	public void testSetStagePosition_centerline() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);

		int expectedRelativeIndex = -1;
		int resultantRelativeIndex = payloadStage.getRelativeToStage();
		assertEquals(expectedRelativeIndex, resultantRelativeIndex, " 'setRelativeToStage(int)' failed. Relative stage index:");

		// vv function under test
		// a centerline stage is not freely movable
		payloadStage.setAxialOffset(AxialMethod.TOP, 4.0);
		// ^^ function under test

		assertEquals(AxialMethod.AFTER, payloadStage.getAxialMethod(), "setAxialPosition( Method, double) ");
		assertEquals(0.0, payloadStage.getAxialOffset(), EPSILON, "setAxialPosition( Method, double) ");

		assertEquals(0.0, payloadStage.getPosition().x, EPSILON, "setAxialPosition( Method, double) ");

		assertEquals(RadiusMethod.COAXIAL, payloadStage.getRadiusMethod(), "setAxialPosition( Method, double) ");
		assertEquals(0.0, payloadStage.getRadiusOffset(), EPSILON, "setAxialPosition( Method, double) ");

		assertEquals(0.0, payloadStage.getComponentLocations()[0].x, EPSILON, "setAxialPosition( Method, double) ");
	}

	@Test
	public void testSetStagePosition_outsideTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		double targetOffset = 0.2;

		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test

		String treeDump = rocket.toDebugTree();

		double expectedRelativeX = 0.2;
		double expectedAbsoluteX = 0.764;
		Coordinate resultantRelativePosition = boosterStage.getPosition();
		assertEquals(resultantRelativePosition.x, expectedRelativeX, EPSILON,
				" 'setAxialPosition(double)' failed: \n" + treeDump + "  Relative position: ");
		// for all stages, the absolute position should equal the relative, because the
		// direct parent is the rocket component (i.e. the Rocket)

		Coordinate resultantAbsolutePosition = boosterStage.getComponentLocations()[0];

		assertEquals(resultantAbsolutePosition.x, expectedAbsoluteX, EPSILON,
				" 'setAxialPosition(double)' failed: \n" + treeDump + "  Absolute position: ");

		double resultantAxialOffset = boosterStage.getAxialOffset();
		assertEquals(resultantAxialOffset, targetOffset, EPSILON,
				" 'getAxialPosition()' failed: \n" + treeDump + "  Axial Offset: ");

		double resultantPositionValue = boosterStage.getAxialOffset();
		assertEquals(resultantPositionValue, targetOffset, EPSILON,
				" 'setPositionValue()' failed: \n" + treeDump + " Position Value: ");
	}

	@Test
	public void testSetMIDDLE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		// when 'external' the stage should be freely movable
		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.MIDDLE, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);

		assertEquals(0.16, boosterStage.getPosition().x, EPSILON);

		assertEquals(0.724, boosterStage.getComponentLocations()[0].x, EPSILON);
	}

	@Test
	public void testSetBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.BOTTOM, targetOffset);
		// ^^ function under test

		assertEquals(0.120, boosterStage.getPosition().x, EPSILON);

		assertEquals(0.684, boosterStage.getComponentLocations()[0].x, EPSILON);

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
	}

	@Test
	public void testSetTOP_getABSOLUTE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		double targetOffset = 0.2;

		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
		assertEquals(targetOffset, boosterStage.getPosition().x, EPSILON);

		final double expectedRelativePositionX = 0.2;
		final double resultantRelativePosition = boosterStage.getPosition().x;
		assertEquals(expectedRelativePositionX, resultantRelativePosition, EPSILON);

		// vv function under test
		final double actualAbsoluteX = boosterStage.getAxialOffset(AxialMethod.ABSOLUTE);
		// ^^ function under test

		assertEquals(0.764, actualAbsoluteX, EPSILON);
	}

	@Test
	public void testSetTOP_getAFTER() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		double targetOffset = 0.2;

		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
		assertEquals(0.2, boosterStage.getPosition().x, EPSILON);

		// vv function under test
		double actualPositionXAfter = boosterStage.getAxialOffset(AxialMethod.AFTER);
		// ^^ function under test

		assertEquals(-0.6, actualPositionXAfter, EPSILON);
	}

	@Test
	public void testSetTOP_getMIDDLE() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		double targetOffset = 0.2;

		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
		assertEquals(0.2, boosterStage.getPosition().x, EPSILON);

		// vv function under test
		final double actualAxialPosition = boosterStage.getAxialOffset(AxialMethod.MIDDLE);
		// ^^ function under test

		assertEquals(0.24, actualAxialPosition, EPSILON);
	}

	@Test
	public void testSetTOP_getBOTTOM() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		double targetOffset = 0.2;

		// vv function under test
		boosterStage.setAxialOffset(AxialMethod.TOP, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
		assertEquals(0.2, boosterStage.getPosition().x, EPSILON);

		// vv function under test
		double actualAxialBottomOffset = boosterStage.getAxialOffset(AxialMethod.BOTTOM);
		// ^^ function under test

		assertEquals(0.28, actualAxialBottomOffset, EPSILON);
	}

	@Test
	public void testSetBOTTOM_getTOP() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		// vv function under test
		double targetOffset = 0.2;
		boosterStage.setAxialOffset(AxialMethod.BOTTOM, targetOffset);
		// ^^ function under test

		assertEquals(targetOffset, boosterStage.getAxialOffset(), EPSILON);
		assertEquals(0.120, boosterStage.getPosition().x, EPSILON);

		// vv function under test
		double actualAxialTopOffset = boosterStage.getAxialOffset(AxialMethod.TOP);
		// ^^ function under test

		assertEquals(0.12, actualAxialTopOffset, EPSILON);
	}

	@Test
	public void testOutsideStageRepositionTOPAfterAdd() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);

		final double targetOffset = +2.50;
		final AxialMethod targetMethod = AxialMethod.TOP;
		boosterStage.setAxialOffset(targetMethod, targetOffset);
		String treeDumpBefore = rocket.toDebugTree();

		// requirement: regardless of initialization order (which we cannot control)
		// a booster should retain it's positioning method and offset while adding on
		// children
		double expectedRelativeX = 2.5;
		double resultantOffset = boosterStage.getPosition().x;
		assertEquals(expectedRelativeX, resultantOffset, EPSILON,
				" init order error: Booster: " + treeDumpBefore + " initial relative X: ");
		double expectedAxialOffset = targetOffset;
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(expectedAxialOffset, resultantOffset, EPSILON,
				" init order error: Booster: " + treeDumpBefore + " Initial axial offset: ");

		String treeDumpAfter = rocket.toDebugTree();

		expectedRelativeX = 2.5; // no change
		resultantOffset = boosterStage.getPosition().x;
		assertEquals(expectedRelativeX, resultantOffset, EPSILON, " init order error: Booster: " + treeDumpBefore +
				" =======> " + treeDumpAfter + " populated relative X: ");
		expectedAxialOffset = targetOffset; // again, no change
		resultantOffset = boosterStage.getAxialOffset();
		assertEquals(expectedAxialOffset, resultantOffset, EPSILON, " init order error: Booster: " + treeDumpBefore +
				" =======> " + treeDumpAfter + " populated axial offset: ");
	}

	@Test
	public void testStageInitializationMethodValueOrder() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		ParallelStage boosterA = createExtraBooster();
		boosterA.setName("Booster A Stage");
		coreBody.addChild(boosterA);
		ParallelStage boosterB = createExtraBooster();
		boosterB.setName("Booster B Stage");
		coreBody.addChild(boosterB);

		double targetOffset = +4.5;
		double expectedOffset = +4.5;
		// requirement: regardless of initialization order (which we cannot control)
		// two boosters with identical initialization commands should end up at the same
		// place.

		boosterA.setAxialOffset(AxialMethod.TOP, targetOffset);

		boosterB.setAxialMethod(AxialMethod.TOP);
		boosterB.setAxialOffset(targetOffset);
		String treeDump = rocket.toDebugTree();

		double resultantOffsetA = boosterA.getPosition().x;
		double resultantOffsetB = boosterB.getPosition().x;

		assertEquals(expectedOffset, resultantOffsetA, EPSILON,
				" init order error: " + treeDump + " Booster A: resultant positions: ");
		assertEquals(expectedOffset, resultantOffsetB, EPSILON,
				" init order error: " + treeDump + " Booster B: resultant positions: ");
	}

	@Test
	public void testStageNumbering() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		ParallelStage boosterA = (ParallelStage) coreBody.getChild(0);

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
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: sustainer: resultant positions: ");

		expectedStageNumber = 1;
		actualStageNumber = coreStage.getStageNumber();
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: core: resultant positions: ");

		expectedStageNumber = 2;
		actualStageNumber = boosterA.getStageNumber();
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: Booster A: resultant positions: ");

		expectedStageNumber = 3;
		actualStageNumber = boosterB.getStageNumber();
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: Booster B: resultant positions: ");

		expectedStageNumber = 4;
		actualStageNumber = boosterC.getStageNumber();
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: Booster C: resultant positions: ");

		// remove Booster B
		coreBody.removeChild(1);

		String treedump = rocket.toDebugTree();
		int expectedStageCount = 4;
		int actualStageCount = config.getStageCount();

		assertEquals(expectedStageCount, actualStageCount,
				" Stage tracking error:  removed booster A, but count not updated: " + treedump);
		actualStageCount = rocket.getSelectedConfiguration().getStageCount();
		assertEquals(expectedStageCount, actualStageCount,
				" Stage tracking error:  removed booster A, but configuration not updated: " + treedump);

		ParallelStage boosterD = createExtraBooster();
		boosterD.setName("Booster D Stage");
		coreBody.addChild(boosterD);
		boosterD.setAxialOffset(AxialMethod.BOTTOM, 0);

		expectedStageNumber = 4;
		actualStageNumber = boosterD.getStageNumber();
		assertEquals(expectedStageNumber, actualStageNumber, " init order error: Booster D: resultant positions: ");

		// rocket.getDefaultConfiguration().dumpConfig();
	}

	@Test
	public void testToAbsolute() {
		final RocketComponent rocket = TestRockets.makeFalcon9Heavy();
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		String treeDump = rocket.toDebugTree();

		Coordinate input = new Coordinate(3, 0, 0);
		Coordinate[] actual = coreStage.toAbsolute(input);

		double expectedX = 3.564;
		assertEquals(expectedX, actual[0].x, EPSILON,
				treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ");
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
		assertEquals(expectedX, actual.x, EPSILON,
				treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ");

		input = new Coordinate(1, 0, 0);
		actual = payloadNose.toRelative(input, payloadBody)[0];

		expectedX = 0.853999;
		assertEquals(expectedX, actual.x, EPSILON,
				treeDump + " coordinate transform through 'core.toAbsolute(c)' failed: ");

	}

}
