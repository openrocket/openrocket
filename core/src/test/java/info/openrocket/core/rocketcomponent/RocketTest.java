package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.ImmutableCoordinate;
import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.AngleMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class RocketTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testCopyIndependence() {
		Rocket rkt1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config1 = new FlightConfiguration(rkt1, null);
		config1.setName("Test config 1");
		rkt1.setFlightConfiguration(config1.getId(), config1);
		rkt1.setSelectedConfiguration(config1.getId());
		FlightConfiguration config2 = new FlightConfiguration(rkt1, null);
		rkt1.setFlightConfiguration(config2.getId(), config2);

		// vvvv test target vvvv
		Rocket rkt2 = rkt1.copyWithOriginalID();
		// ^^^^ test target ^^^^

		FlightConfiguration config4 = rkt2.getSelectedConfiguration();
		FlightConfigurationId fcid4 = config4.getId();

		assertEquals(config1.getId().key, fcid4.key, "fcids should match: ");
		assertEquals(config1.getName(), config4.getName(), "names should match: ");
		assertEquals("Test config 1", config4.getName(), "name not right: ");
		assertNotEquals(config1.configurationInstanceId, config4.configurationInstanceId,
				"Configurations should be different: " + config1.toDebug() + "=?=" + config4.toDebug());

		FlightConfiguration config5 = rkt2.getFlightConfiguration(config2.getId());
		FlightConfigurationId fcid5 = config5.getId();
		assertEquals(config2.getId(), fcid5, "fcids should match: ");
		assertNotEquals(config2.configurationInstanceId, config5.configurationInstanceId,
				"Configurations should bef different match: " + config2.toDebug() + "=?=" + config5.toDebug());
	}

	@Test
	public void testCopyRocketFrom() {
		// Rocket r1 = info.openrocket.core.util.TestRockets.makeBigBlue();
		// Rocket r2 = new Rocket();

		// this method fails, but I'm not sure what this is testing, or why.
		// therefore, I'm not convinced it's valuable enough to keep around.
		// r2.copyFrom(r1);
		// ComponentCompare.assertDeepEquality(r1, r2);
	}

	@Test
	public void testEstesAlphaIII() {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();

		final AxialStage stage = (AxialStage) rocket.getChild(0);

		Coordinate expLoc;
		Coordinate actLoc;
		{
			NoseCone nose = (NoseCone) stage.getChild(0);
			expLoc = new ImmutableCoordinate(0, 0, 0);
			actLoc = nose.getComponentLocations()[0];
			assertEquals(actLoc, expLoc, nose.getName() + " not positioned correctly: ");

			BodyTube body = (BodyTube) stage.getChild(1);
			expLoc = new ImmutableCoordinate(0.07, 0, 0);
			actLoc = body.getComponentLocations()[0];
			assertEquals(actLoc, expLoc, body.getName() + " not positioned correctly: ");

			{
				FinSet fins = (FinSet) body.getChild(0);
				Coordinate[] actLocs = fins.getComponentLocations();
				assertEquals(fins.getInstanceCount(), 3, fins.getName() + " have incorrect count: ");
				{ // fin #1
					expLoc = new ImmutableCoordinate(0.22, 0.012, 0);
					assertEquals(actLocs[0], expLoc, fins.getName() + " not positioned correctly: ");
				}

				LaunchLug lugs = (LaunchLug) body.getChild(1);
				expLoc = new ImmutableCoordinate(0.181, -0.015, 0);
				assertEquals(lugs.getInstanceCount(), 1, lugs.getName() + " have incorrect count: ");
				actLocs = lugs.getComponentLocations();
				{ // singular instance:
					assertEquals(actLocs[0], expLoc, lugs.getName() + " not positioned correctly: ");
				}

				InnerTube mmt = (InnerTube) body.getChild(2);
				expLoc = new ImmutableCoordinate(0.203, 0, 0);
				actLoc = mmt.getComponentLocations()[0];
				assertEquals(actLoc, expLoc, mmt.getName() + " not positioned correctly: ");
				{
					EngineBlock block = (EngineBlock) mmt.getChild(0);
					expLoc = new ImmutableCoordinate(0.203, 0, 0);
					actLoc = block.getComponentLocations()[0];
					assertEquals(actLoc, expLoc, block.getName() + " not positioned correctly: ");
				}

			}

			Parachute chute = (Parachute) body.getChild(3);
			expLoc = new ImmutableCoordinate(0.098, 0, 0);
			actLoc = chute.getComponentLocations()[0];
			assertEquals(actLoc, expLoc, chute.getName() + " not positioned correctly: ");

			CenteringRing ring = (CenteringRing) body.getChild(4);
			assertEquals(ring.getInstanceCount(), 2, ring.getName() + " not instanced correctly: ");
			// singleton instances follow different code paths
			ring.setInstanceCount(1);
			expLoc = new ImmutableCoordinate(0.21, 0, 0);
			actLoc = ring.getComponentLocations()[0];
			assertEquals(expLoc.getX(), actLoc.getX(), EPSILON, " position x fail: ");
			assertEquals(expLoc.getY(), actLoc.getY(), EPSILON, " position y fail: ");
			assertEquals(expLoc.getZ(), actLoc.getZ(), EPSILON, " position z fail: ");
			assertEquals(actLoc, expLoc, ring.getName() + " not positioned correctly: ");

			ring.setInstanceCount(2);
			Coordinate[] actLocs = ring.getComponentLocations();
			{ // first instance
				expLoc = new ImmutableCoordinate(0.21, 0, 0);
				actLoc = actLocs[0];
				assertEquals(actLoc, expLoc, ring.getName() + " not positioned correctly: ");
			}
			{ // second instance
				assertEquals(ring.getInstanceCount(), 2, ring.getName() + " not instanced correctly: ");
				expLoc = new ImmutableCoordinate(0.245, 0, 0);
				actLoc = actLocs[1];
				assertEquals(actLoc, expLoc, ring.getName() + " not positioned correctly: ");
			}

		}

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(bounds.min.getX(), 0.0, EPSILON);
		assertEquals(bounds.max.getX(), 0.27, EPSILON);

		assertEquals(-0.032385640, bounds.min.getY(), EPSILON);
		assertEquals(-0.054493575, bounds.min.getZ(), EPSILON);
		assertEquals(0.062000000, bounds.max.getY(), EPSILON);
		assertEquals(0.052893575, bounds.max.getZ(), EPSILON);
	}

	@Test
	public void testChangeAxialMethod() {
		class AxialPositionTestCase {
			final public AxialMethod beginMethod;
			final public double beginOffset;
			final public AxialMethod endMethod;
			final public double endOffset;
			final public double endPosition;

			public AxialPositionTestCase(AxialMethod _begMeth, double _begOffs, AxialMethod _endMeth, double _endOffs,
					double _endPos) {
				beginMethod = _begMeth;
				beginOffset = _begOffs;
				endMethod = _endMeth;
				endOffset = _endOffs;
				endPosition = _endPos;
			}
		}

		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final AxialStage stage = (AxialStage) rocket.getChild(0);
		final BodyTube body = (BodyTube) stage.getChild(1);
		final FinSet fins = (FinSet) body.getChild(0);

		{ // verify construction:
			assertEquals(0.20, body.getLength(), EPSILON, "incorrect body length:");
			assertEquals(0.05, fins.getLength(), EPSILON, "incorrect fin length:");
			{ // fin #1
				final Coordinate expLoc = new ImmutableCoordinate(0.22, 0.012, 0);
				final Coordinate[] actLocs = fins.getComponentLocations();
				assertEquals(actLocs[0], expLoc, fins.getName() + " not positioned correctly: ");
			}
		}

		final ArrayList<AxialPositionTestCase> allTestCases = new ArrayList<>(10);
		allTestCases.add(0, new AxialPositionTestCase(AxialMethod.BOTTOM, 0.0, AxialMethod.TOP, 0.15, 0.15));
		allTestCases.add(1, new AxialPositionTestCase(AxialMethod.TOP, 0.0, AxialMethod.BOTTOM, -0.15, 0.0));
		allTestCases.add(2, new AxialPositionTestCase(AxialMethod.BOTTOM, -0.03, AxialMethod.TOP, 0.12, 0.12));
		allTestCases.add(3, new AxialPositionTestCase(AxialMethod.BOTTOM, 0.03, AxialMethod.TOP, 0.18, 0.18));
		allTestCases.add(4, new AxialPositionTestCase(AxialMethod.BOTTOM, 0.03, AxialMethod.MIDDLE, 0.105, 0.18));
		allTestCases.add(5, new AxialPositionTestCase(AxialMethod.MIDDLE, 0.0, AxialMethod.TOP, 0.075, 0.075));
		allTestCases.add(6, new AxialPositionTestCase(AxialMethod.MIDDLE, 0.0, AxialMethod.BOTTOM, -0.075, 0.075));
		allTestCases.add(7, new AxialPositionTestCase(AxialMethod.MIDDLE, 0.005, AxialMethod.TOP, 0.08, 0.08));

		for (int caseIndex = 0; caseIndex < allTestCases.size(); ++caseIndex) {
			final AxialPositionTestCase cur = allTestCases.get(caseIndex);
			// test repositioning
			fins.setAxialOffset(cur.beginMethod, cur.beginOffset);
			assertEquals(fins.getAxialMethod(),
					cur.beginMethod, fins.getName() + " incorrect start axial-position-method: ");
			assertEquals(cur.beginOffset,
					fins.getAxialOffset(), EPSILON, fins.getName() + " incorrect start axial-position-value: ");

			{
				// System.err.println(String.format("## Running Test case # %d :", caseIndex));
				fins.setAxialMethod(cur.endMethod);
				assertEquals(cur.endOffset, fins.getAxialOffset(), EPSILON,
						String.format(" Test Case # %d // offset doesn't match!", caseIndex));
				assertEquals(cur.endPosition, fins.getPosition().getX(), EPSILON,
						String.format(" Test Case # %d // position doesn't match!", caseIndex));
			}
		}

	}

	@Test
	public void testRemoveReadjustLocation() {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();

		{
			BodyTube bodyPrior = (BodyTube) rocket.getChild(0).getChild(1);
			Coordinate locPrior = bodyPrior.getComponentLocations()[0];
			assertEquals(locPrior.getX(), 0.07, EPSILON);
		}

		// remove the nose cone, causing the bodytube to reposition:
		rocket.getChild(0).removeChild(0);

		{
			BodyTube tubePost = (BodyTube) rocket.getChild(0).getChild(0);
			Coordinate locPost = tubePost.getComponentLocations()[0];
			assertEquals(locPost.getX(), 0.0, EPSILON);
		}
	}

	@Test
	public void testAutoSizePreviousComponent() {
		Rocket rocket = TestRockets.makeBeta();

		final AxialStage sustainer = (AxialStage) rocket.getChild(0);
		final AxialStage booster = (AxialStage) rocket.getChild(1);
		final double expRadius = 0.012;

		{ // test auto-radius within a stage: nose -> body tube
			final NoseCone nose = (NoseCone) sustainer.getChild(0);
			assertEquals(expRadius, nose.getAftRadius(), EPSILON, " radius match: ");
			final BodyTube body = (BodyTube) sustainer.getChild(1);
			assertEquals(expRadius, body.getOuterRadius(), EPSILON, " radius match: ");

			body.setOuterRadiusAutomatic(true);
			assertEquals(expRadius, body.getOuterRadius(), EPSILON, " radius match: ");
		}
		{ // test auto-radius within a stage: tail cone -> body tube
			final BodyTube body = (BodyTube) booster.getChild(0);
			assertEquals(expRadius, body.getOuterRadius(), EPSILON, " radius match: ");
			final Transition tailCone = (Transition) booster.getChild(1);
			assertEquals(expRadius, tailCone.getForeRadius(), EPSILON, " radius match: ");

			tailCone.setForeRadiusAutomatic(true);
			assertEquals(expRadius, tailCone.getForeRadius(), EPSILON, " trailing transition match: ");
		}
		{ // test auto-radius across stages: sustainer body -> booster body
			BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);
			assertEquals(expRadius, sustainerBody.getOuterRadius(), EPSILON, " radius match: ");
			BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(expRadius, boosterBody.getOuterRadius(), EPSILON, " radius match: ");

			boosterBody.setOuterRadiusAutomatic(true);
			assertEquals(expRadius, boosterBody.getOuterRadius(), EPSILON, " radius match: ");
		}
	}

	@Test
	public void testAutoSizeNextComponent() {
		Rocket rocket = TestRockets.makeBeta();

		final AxialStage sustainer = (AxialStage) rocket.getChild(0);
		final AxialStage booster = (AxialStage) rocket.getChild(1);
		final double expRadius = 0.012;

		{ // test auto-radius within a stage: nose <- body tube
			System.err.println("## Testing auto-radius:  sustainer:  nose <- body");
			final NoseCone nose = (NoseCone) sustainer.getChild(0);
			assertEquals(expRadius, nose.getAftRadius(), EPSILON, " radius match: ");
			final BodyTube body = (BodyTube) sustainer.getChild(1);
			assertEquals(expRadius, body.getOuterRadius(), EPSILON, " radius match: ");

			nose.setAftRadiusAutomatic(true);
			assertEquals(expRadius, nose.getAftRadius(), EPSILON, " radius match: ");
		}
		{ // test auto-radius within a stage: body tube <- trailing transition
			System.err.println("## Testing auto-radius:  booster: body <- tail");
			final BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(expRadius, boosterBody.getOuterRadius(), EPSILON, " radius match: ");
			final Transition tailCone = (Transition) booster.getChild(1);
			assertEquals(expRadius, tailCone.getForeRadius(), EPSILON, " radius match: ");

			boosterBody.setOuterRadiusAutomatic(true);
			assertEquals(expRadius, boosterBody.getOuterRadius(), EPSILON, " trailing transition match: ");
		}
		{ // test auto-radius across stages: sustainer body <- booster body
			System.err.println("## Testing auto-radius:  booster:body -> sustainer:body");
			BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);
			assertEquals(expRadius, sustainerBody.getOuterRadius(), EPSILON, " radius match: ");
			BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(expRadius, boosterBody.getOuterRadius(), EPSILON, " radius match: ");

			sustainerBody.setOuterRadiusAutomatic(true);
			assertEquals(expRadius, sustainerBody.getOuterRadius(), EPSILON, " radius match: ");
		}
	}

	@Test
	public void testBeta() {
		Rocket rocket = TestRockets.makeBeta();

		AxialStage boosterStage = (AxialStage) rocket.getChild(1);

		Coordinate expLoc;
		Coordinate actLoc;
		Coordinate[] actLocs;
		{
			BodyTube body = (BodyTube) boosterStage.getChild(0);
			Coordinate[] bodyLocs = body.getComponentLocations();
			expLoc = new ImmutableCoordinate(0.27, 0, 0);
			assertEquals(bodyLocs[0], expLoc, body.getName() + " not positioned correctly: ");

			{
				TubeCoupler coupler = (TubeCoupler) body.getChild(0);
				actLocs = coupler.getComponentLocations();
				expLoc = new ImmutableCoordinate(0.255, 0, 0);
				assertEquals(actLocs[0], expLoc, coupler.getName() + " not positioned correctly: ");

				FinSet fins = (FinSet) body.getChild(1);
				actLocs = fins.getComponentLocations();
				assertEquals(fins.getInstanceCount(), 3, fins.getName() + " have incorrect count: ");
				{ // fin #1
					expLoc = new ImmutableCoordinate(0.28, 0.012, 0);
					assertEquals(actLocs[0], expLoc, fins.getName() + " not positioned correctly: ");
				}

				InnerTube mmt = (InnerTube) body.getChild(2);
				actLoc = mmt.getComponentLocations()[0];
				expLoc = new ImmutableCoordinate(0.285, 0, 0);
				assertEquals(actLoc, expLoc, mmt.getName() + " not positioned correctly: ");
			}
		}

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(bounds.min.getX(), 0.0, EPSILON);
		assertEquals(bounds.max.getX(), 0.335, EPSILON);

		assertEquals(-0.032385640, bounds.min.getY(), EPSILON);
		assertEquals(-0.054493575, bounds.min.getZ(), EPSILON);
		assertEquals(0.062000000, bounds.max.getY(), EPSILON);
		assertEquals(0.052893575, bounds.max.getZ(), EPSILON);
	}

	@Test
	public void testFalcon9HComponentLocations() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket." + Thread.currentThread().getStackTrace()[1].getMethodName());

		Coordinate offset;
		Coordinate loc;

		// ====== Payload Stage ======
		// ====== ====== ====== ======
		AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		{
			NoseCone nc = (NoseCone) payloadStage.getChild(0);
			offset = nc.getPosition();
			loc = nc.getComponentLocations()[0];
			assertEquals(0.0, offset.getX(), EPSILON, "P/L NoseCone offset is incorrect: ");
			assertEquals(0.0, loc.getX(), EPSILON, "P/L NoseCone location is incorrect: ");

			BodyTube plbody = (BodyTube) payloadStage.getChild(1);
			offset = plbody.getPosition();
			loc = plbody.getComponentLocations()[0];
			assertEquals(0.118, offset.getX(), EPSILON, "P/L Body offset calculated incorrectly: ");
			assertEquals(0.118, loc.getX(), EPSILON, "P/L Body location calculated incorrectly: ");

			Transition tr = (Transition) payloadStage.getChild(2);
			offset = tr.getPosition();
			loc = tr.getComponentLocations()[0];
			assertEquals(0.250, offset.getX(), EPSILON, tr.getName() + " offset is incorrect: ");
			assertEquals(0.250, loc.getX(), EPSILON, tr.getName() + " location is incorrect: ");

			BodyTube upperBody = (BodyTube) payloadStage.getChild(3);
			offset = upperBody.getPosition();
			loc = upperBody.getComponentLocations()[0];
			assertEquals(0.264, offset.getX(), EPSILON, upperBody.getName() + " offset is incorrect: ");
			assertEquals(0.264, loc.getX(), EPSILON, upperBody.getName() + " location is incorrect: ");
			{
				Parachute chute = (Parachute) payloadStage.getChild(3).getChild(0);
				offset = chute.getPosition();
				loc = chute.getComponentLocations()[0];
				assertEquals(0.0775, offset.getX(), EPSILON, chute.getName() + " offset is incorrect: ");
				assertEquals(0.3415, loc.getX(), EPSILON, chute.getName() + " location is incorrect: ");

				ShockCord cord = (ShockCord) payloadStage.getChild(3).getChild(1);
				offset = cord.getPosition();
				loc = cord.getComponentLocations()[0];
				assertEquals(0.155, offset.getX(), EPSILON, cord.getName() + " offset is incorrect: ");
				assertEquals(0.419, loc.getX(), EPSILON, cord.getName() + " location is incorrect: ");
			}

			BodyTube interstage = (BodyTube) payloadStage.getChild(4);
			offset = interstage.getPosition();
			loc = interstage.getComponentLocations()[0];
			assertEquals(0.444, offset.getX(), EPSILON, interstage.getName() + " offset is incorrect: ");
			assertEquals(0.444, loc.getX(), EPSILON, interstage.getName() + " location is incorrect: ");
		}

		// ====== Core Stage ======
		// ====== ====== ======
		{
			BodyTube coreBody = (BodyTube) rocket.getChild(1).getChild(0);
			offset = coreBody.getPosition();
			loc = coreBody.getComponentLocations()[0];
			assertEquals(0.0, offset.getX(), EPSILON, coreBody.getName() + " offset is incorrect: ");
			assertEquals(0.564, loc.getX(), EPSILON, coreBody.getName() + " location is incorrect: ");

			// ====== Booster Set Stage ======
			// ====== ====== ======
			ParallelStage boosters = (ParallelStage) coreBody.getChild(0);
			{
				assertEquals(RadiusMethod.SURFACE, boosters.getRadiusMethod());
				assertEquals(AngleMethod.RELATIVE, boosters.getAngleMethod());

				Coordinate boosterPosition = boosters.getPosition();
				assertEquals(-0.08, boosterPosition.getX(), EPSILON, boosters.getName() + " position is incorrect: ");
				assertEquals(0.0, boosterPosition.getY(), EPSILON, boosters.getName() + " position is incorrect: ");
				assertEquals(0.0, boosterPosition.getZ(), EPSILON, boosters.getName() + " position is incorrect: ");

				Coordinate[] boosterInstanceOffsets = boosters.getInstanceOffsets();
				assertEquals(0.0, boosterInstanceOffsets[0].getX(),
						EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(0.077, boosterInstanceOffsets[0].getY(),
						EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(-0.077, boosterInstanceOffsets[1].getY(),
						EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(0.0, boosterInstanceOffsets[0].getZ(),
						EPSILON, boosters.getName() + " location is incorrect: ");

				Coordinate[] boosterLocations = boosters.getComponentLocations();
				assertEquals(0.484, boosterLocations[0].getX(), EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(0.077, boosterLocations[0].getY(), EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(-0.077, boosterLocations[1].getY(), EPSILON, boosters.getName() + " location is incorrect: ");
				assertEquals(0.0, boosterLocations[0].getZ(), EPSILON, boosters.getName() + " location is incorrect: ");

				// think of the casts as an assert that ( child instanceof NoseCone) == true
				NoseCone nose = (NoseCone) boosters.getChild(0);
				offset = nose.getPosition();
				loc = nose.getComponentLocations()[0];
				assertEquals(0.0, offset.getX(), EPSILON, nose.getName() + " offset is incorrect: ");
				assertEquals(0.484, loc.getX(), EPSILON, nose.getName() + " location is incorrect: ");

				BodyTube boosterBody = (BodyTube) boosters.getChild(1);
				offset = boosterBody.getPosition();
				loc = boosterBody.getComponentLocations()[0];
				assertEquals(0.08, offset.getX(), EPSILON, boosterBody.getName() + " offset is incorrect: ");
				assertEquals(0.564, loc.getX(), EPSILON, boosterBody.getName() + " location is incorrect: ");
				{
					InnerTube mmt = (InnerTube) boosterBody.getChild(0);
					offset = mmt.getPosition();
					loc = mmt.getComponentLocations()[0];
					assertEquals(0.65, offset.getX(), EPSILON, mmt.getName() + " offset is incorrect: ");
					assertEquals(1.214, loc.getX(), EPSILON, mmt.getName() + " location is incorrect: ");

					final FinSet coreFins = (FinSet) boosterBody.getChild(1);
					offset = coreFins.getPosition();
					loc = coreFins.getComponentLocations()[0];
					assertEquals(0.480, offset.getX(), EPSILON, coreFins.getName() + " offset is incorrect: ");
					assertEquals(1.044, loc.getX(), EPSILON, coreFins.getName() + " location is incorrect: ");
				}
			}
		}

		// DEBUG
		System.err.println(rocket.toDebugTree());

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(0.0, bounds.min.getX(), EPSILON);
		assertEquals(1.364, bounds.max.getX(), EPSILON);

		assertEquals(-0.215500, bounds.min.getY(), EPSILON);
		assertEquals(0.215500, bounds.max.getY(), EPSILON);

		assertEquals(-0.12069451, bounds.min.getZ(), EPSILON);
		assertEquals(0.12069451, bounds.max.getZ(), EPSILON);
	}

}
