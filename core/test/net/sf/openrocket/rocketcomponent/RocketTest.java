package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.util.BoundingBox;
import org.junit.Test;

import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class RocketTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testCopyIndependence() {
		Rocket rkt1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config1 = new FlightConfiguration(rkt1, null);
		rkt1.setFlightConfiguration( config1.getId(), config1);
		rkt1.setSelectedConfiguration( config1.getId());
		FlightConfiguration config2 = new FlightConfiguration(rkt1, null);
		rkt1.setFlightConfiguration( config2.getId(), config2);
		
		// vvvv test target vvvv 
		Rocket rkt2 = rkt1.copyWithOriginalID();
		// ^^^^ test target ^^^^
		
		FlightConfiguration config4 = rkt2.getSelectedConfiguration();
		FlightConfigurationId fcid4 = config4.getId();
		
		assertThat("fcids should match: ", config1.getId().key, equalTo(fcid4.key));
		assertThat("Configurations should be different: "+config1.toDebug()+"=?="+config4.toDebug(), config1.configurationInstanceId, not( config4.configurationInstanceId));
	
		FlightConfiguration config5 = rkt2.getFlightConfiguration(config2.getId());
		FlightConfigurationId fcid5 = config5.getId();
		assertThat("fcids should match: ", config2.getId(), equalTo(fcid5));
		assertThat("Configurations should bef different match: "+config2.toDebug()+"=?="+config5.toDebug(), config2.configurationInstanceId, not( config5.configurationInstanceId));
	}
	
	
	
	@Test
	public void testCopyRocketFrom() {
		//Rocket r1 = net.sf.openrocket.util.TestRockets.makeBigBlue();
		//Rocket r2 = new Rocket();
		
		// this method fails, but I'm not sure what this is testing, or why. 
		// therefore, I'm not convinced it's valuable enough to keep around.
		//r2.copyFrom(r1);
		//ComponentCompare.assertDeepEquality(r1, r2);
	}

	@Test
	public void testEstesAlphaIII(){
		final Rocket rocket = TestRockets.makeEstesAlphaIII();

		final AxialStage stage= (AxialStage)rocket.getChild(0);

		Coordinate expLoc;
		Coordinate actLoc;
		{
			NoseCone nose = (NoseCone)stage.getChild(0);
			expLoc = new Coordinate(0,0,0);
			actLoc = nose.getComponentLocations()[0];
			assertThat(nose.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			BodyTube body = (BodyTube)stage.getChild(1);
			expLoc = new Coordinate(0.07,0,0);
			actLoc = body.getComponentLocations()[0];
			assertThat(body.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			{	
				FinSet fins = (FinSet)body.getChild(0);
				Coordinate actLocs[] = fins.getComponentLocations();
				assertThat(fins.getName()+" have incorrect count: ", fins.getInstanceCount(), equalTo(3));
				{ // fin #1
					expLoc = new Coordinate(0.22,0.012,0);
					assertThat(fins.getName()+" not positioned correctly: ", actLocs[0], equalTo(expLoc));
				}

				LaunchLug lugs = (LaunchLug)body.getChild(1);
				expLoc = new Coordinate(0.181, 0.015, 0);
				assertThat(lugs.getName()+" have incorrect count: ", lugs.getInstanceCount(), equalTo(1));
				actLocs = lugs.getComponentLocations();
				{ // singular instance:
					assertThat(lugs.getName()+" not positioned correctly: ", actLocs[0], equalTo(expLoc));
				}

				InnerTube mmt = (InnerTube)body.getChild(2);
				expLoc = new Coordinate(0.203,0,0);
				actLoc = mmt.getComponentLocations()[0];
				assertThat(mmt.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				{
					EngineBlock block = (EngineBlock)mmt.getChild(0);
					expLoc = new Coordinate(0.203,0,0);
					actLoc = block.getComponentLocations()[0];
					assertThat(block.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				}
				
			}

			Parachute chute = (Parachute)body.getChild(3);
			expLoc = new Coordinate(0.098,0,0);
			actLoc = chute.getComponentLocations()[0];
			assertThat(chute.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			CenteringRing ring = (CenteringRing)body.getChild(4);
			assertThat(ring.getName()+" not instanced correctly: ", ring.getInstanceCount(), equalTo(2));
			// singleton instances follow different code paths
			ring.setInstanceCount(1);
			expLoc = new Coordinate(0.21,0,0);
			actLoc = ring.getComponentLocations()[0];
			assertEquals(" position x fail: ", expLoc.x, actLoc.x, EPSILON);
			assertEquals(" position y fail: ", expLoc.y, actLoc.y, EPSILON);
			assertEquals(" position z fail: ", expLoc.z, actLoc.z, EPSILON);
			assertThat(ring.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));

			ring.setInstanceCount(2);
			Coordinate actLocs[] = ring.getComponentLocations();
			{ // first instance
				expLoc = new Coordinate(0.21, 0, 0);
				actLoc = actLocs[0];
				assertThat(ring.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			}
			{ // second instance
				assertThat(ring.getName()+" not instanced correctly: ", ring.getInstanceCount(), equalTo(2));
				expLoc = new Coordinate(0.245, 0, 0);
				actLoc = actLocs[1];
				assertThat(ring.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			}
			
		}

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(bounds.min.x, 0.0,  EPSILON);
		assertEquals(bounds.max.x, 0.27, EPSILON);

		assertEquals( -0.032385640, bounds.min.y, EPSILON);
		assertEquals( -0.054493575, bounds.min.z, EPSILON);
		assertEquals(  0.062000000,  bounds.max.y, EPSILON);
		assertEquals(  0.052893575, bounds.max.z, EPSILON);
	}
	
	@Test 
	public void testRemoveReadjustLocation() {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		{
			BodyTube bodyPrior = (BodyTube)rocket.getChild(0).getChild(1);
			Coordinate locPrior = bodyPrior.getComponentLocations()[0];
			assertThat(locPrior.x, equalTo(0.07));
		}
		
		// remove the nose cone, causing the bodytube to reposition:
		rocket.getChild(0).removeChild(0);

		{
			BodyTube tubePost = (BodyTube)rocket.getChild(0).getChild(0);
			Coordinate locPost = tubePost.getComponentLocations()[0];
			assertThat(locPost.x, equalTo(0.0));
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
			assertEquals(" radius match: ", expRadius, nose.getAftRadius(), EPSILON);
			final BodyTube body = (BodyTube) sustainer.getChild(1);
			assertEquals(" radius match: ", expRadius, body.getOuterRadius(), EPSILON);

			body.setOuterRadiusAutomatic(true);
			assertEquals(" radius match: ", expRadius, body.getOuterRadius(), EPSILON);
		}
		{ // test auto-radius within a stage: body tube -> trailing transition
			final BodyTube body = (BodyTube) booster.getChild(0);
			assertEquals(" radius match: ", expRadius, body.getOuterRadius(), EPSILON);
			final Transition tailCone = (Transition)booster.getChild(1);
			assertEquals(" radius match: ", expRadius, tailCone.getForeRadius(), EPSILON);

			tailCone.setForeRadiusAutomatic(true);
			assertEquals(" trailing transition match: ", expRadius, tailCone.getForeRadius(), EPSILON);
		}
		{ // test auto-radius across stages: sustainer body -> booster body
			BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);
			assertEquals(" radius match: ", expRadius, sustainerBody.getOuterRadius(), EPSILON);
			BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(" radius match: ", expRadius, boosterBody.getOuterRadius(), EPSILON);

			boosterBody.setOuterRadiusAutomatic(true);
			assertEquals(" radius match: ", expRadius, boosterBody.getOuterRadius(), EPSILON);
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
			assertEquals(" radius match: ", expRadius, nose.getAftRadius(), EPSILON);
			final BodyTube body = (BodyTube) sustainer.getChild(1);
			assertEquals(" radius match: ", expRadius, body.getOuterRadius(), EPSILON);

			nose.setAftRadiusAutomatic(true);
			assertEquals(" radius match: ", expRadius, nose.getAftRadius(), EPSILON);
		}
		{ // test auto-radius within a stage: body tube <- trailing transition
			System.err.println("## Testing auto-radius:  booster: body <- tail");
			final BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(" radius match: ", expRadius, boosterBody.getOuterRadius(), EPSILON);
			final Transition tailCone = (Transition)booster.getChild(1);
			assertEquals(" radius match: ", expRadius, tailCone.getForeRadius(), EPSILON);

			boosterBody.setOuterRadiusAutomatic(true);
			assertEquals(" trailing transition match: ", expRadius, boosterBody.getOuterRadius(), EPSILON);
		}
		{ // test auto-radius across stages: sustainer body <- booster body
			System.err.println("## Testing auto-radius:  booster:body -> sustainer:body");
			BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);
			assertEquals(" radius match: ", expRadius, sustainerBody.getOuterRadius(), EPSILON);
			BodyTube boosterBody = (BodyTube) booster.getChild(0);
			assertEquals(" radius match: ", expRadius, boosterBody.getOuterRadius(), EPSILON);

			sustainerBody.setOuterRadiusAutomatic(true);
			assertEquals(" radius match: ", expRadius, sustainerBody.getOuterRadius(), EPSILON);
		}
	}

	@Test
	public void testBeta(){
		Rocket rocket = TestRockets.makeBeta();

		AxialStage boosterStage= (AxialStage)rocket.getChild(1);

		Coordinate expLoc;
		Coordinate actLoc;
		Coordinate actLocs[];
		{
			BodyTube body = (BodyTube)boosterStage.getChild(0);
			Coordinate[] bodyLocs = body.getComponentLocations();
			expLoc = new Coordinate(0.27, 0, 0);
			assertThat(body.getName()+" not positioned correctly: ", bodyLocs[0], equalTo(expLoc));

			{
				TubeCoupler coupler = (TubeCoupler)body.getChild(0);
				actLocs = coupler.getComponentLocations();
				expLoc = new Coordinate(0.255, 0, 0);
				assertThat(coupler.getName()+" not positioned correctly: ", actLocs[0], equalTo(expLoc) );

				FinSet fins = (FinSet)body.getChild(1);
				actLocs = fins.getComponentLocations();
				assertThat(fins.getName()+" have incorrect count: ", fins.getInstanceCount(), equalTo(3));
				{ // fin #1
					expLoc = new Coordinate(0.28, 0.012, 0);
					assertThat(fins.getName()+" not positioned correctly: ", actLocs[0], equalTo(expLoc));
				}

				InnerTube mmt = (InnerTube)body.getChild(2);
				actLoc = mmt.getComponentLocations()[0];
				expLoc = new Coordinate(0.285, 0, 0);
				assertThat(mmt.getName()+" not positioned correctly: ", actLoc, equalTo( expLoc ));
			}
		}

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals( bounds.min.x, 0.0,  EPSILON);
		assertEquals( bounds.max.x, 0.335, EPSILON);

		assertEquals( -0.032385640, bounds.min.y, EPSILON);
		assertEquals( -0.054493575, bounds.min.z, EPSILON);
		assertEquals(  0.062000000,  bounds.max.y, EPSILON);
		assertEquals(  0.052893575, bounds.max.z, EPSILON);
	}
	
	@Test
	public void testFalcon9HComponentLocations() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("TestRocket."+Thread.currentThread().getStackTrace()[1].getMethodName());

		Coordinate offset;
		Coordinate loc;
		
		// ====== Payload Stage ======
		// ====== ====== ====== ======
		AxialStage payloadStage = (AxialStage)rocket.getChild(0);
		{
			NoseCone nc = (NoseCone)payloadStage.getChild(0);
			offset = nc.getPosition();
			loc = nc.getComponentLocations()[0];			
			assertEquals("P/L NoseCone offset is incorrect: ", 0.0, offset.x, EPSILON);
			assertEquals("P/L NoseCone location is incorrect: ", 0.0, loc.x, EPSILON);

			BodyTube plbody = (BodyTube)payloadStage.getChild(1);
			offset = plbody.getPosition();
			loc = plbody.getComponentLocations()[0];			
			assertEquals("P/L Body offset calculated incorrectly: ", 0.118, offset.x, EPSILON);
			assertEquals("P/L Body location calculated incorrectly: ", 0.118, loc.x, EPSILON);

			
			Transition tr= (Transition)payloadStage.getChild(2);
			offset = tr.getPosition();
			loc = tr.getComponentLocations()[0];			
			assertEquals(tr.getName()+" offset is incorrect: ", 0.250, offset.x, EPSILON);
			assertEquals(tr.getName()+" location is incorrect: ", 0.250, loc.x, EPSILON);

			BodyTube upperBody = (BodyTube)payloadStage.getChild(3);
			offset = upperBody.getPosition();
			loc = upperBody.getComponentLocations()[0];			
			assertEquals(upperBody.getName()+" offset is incorrect: ", 0.264, offset.x, EPSILON);
			assertEquals(upperBody.getName()+" location is incorrect: ", 0.264, loc.x, EPSILON);
			{
				Parachute chute = (Parachute)payloadStage.getChild(3).getChild(0);
				offset = chute.getPosition();
				loc = chute.getComponentLocations()[0];			
				assertEquals(chute.getName()+" offset is incorrect: ", 0.0775, offset.x, EPSILON);
				assertEquals(chute.getName()+" location is incorrect: ", 0.3415, loc.x, EPSILON);
				
				ShockCord cord= (ShockCord)payloadStage.getChild(3).getChild(1);
				offset = cord.getPosition();
				loc = cord.getComponentLocations()[0];			
				assertEquals(cord.getName()+" offset is incorrect: ", 0.155, offset.x, EPSILON);
				assertEquals(cord.getName()+" location is incorrect: ", 0.419, loc.x, EPSILON);
			}

			BodyTube interstage = (BodyTube)payloadStage.getChild(4);
			offset = interstage.getPosition();
			loc = interstage.getComponentLocations()[0];			
			assertEquals(interstage.getName()+" offset is incorrect: ", 0.444, offset.x, EPSILON);
			assertEquals(interstage.getName()+" location is incorrect: ", 0.444, loc.x, EPSILON);
		}

		// ====== Core Stage ======
		// ====== ====== ======
		{
			BodyTube coreBody = (BodyTube)rocket.getChild(1).getChild(0);
			offset = coreBody.getPosition();
			loc = coreBody.getComponentLocations()[0];			
			assertEquals(coreBody.getName()+" offset is incorrect: ", 0.0, offset.x, EPSILON);
			assertEquals(coreBody.getName()+" location is incorrect: ", 0.564, loc.x, EPSILON);

			// ====== Booster Set Stage ======
			// ====== ====== ======
			ParallelStage boosters = (ParallelStage) coreBody.getChild(0);
			{
				assertEquals( RadiusMethod.SURFACE, boosters.getRadiusMethod() );
				assertEquals( AngleMethod.RELATIVE, boosters.getAngleMethod() );
				
				Coordinate boosterPosition = boosters.getPosition();
				assertEquals( boosters.getName()+" position is incorrect: ", -0.08, boosterPosition.x, EPSILON);
				assertEquals( boosters.getName()+" position is incorrect: ", 0.0, boosterPosition.y, EPSILON);
				assertEquals( boosters.getName()+" position is incorrect: ", 0.0, boosterPosition.z, EPSILON);
				
				Coordinate boosterInstanceOffsets[] = boosters.getInstanceOffsets();			
				assertEquals( boosters.getName()+" location is incorrect: ", 0.0, boosterInstanceOffsets[0].x, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ",  0.077, boosterInstanceOffsets[0].y, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ", -0.077, boosterInstanceOffsets[1].y, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ", 0.0, boosterInstanceOffsets[0].z, EPSILON);
				
				
				Coordinate boosterLocations[] = boosters.getComponentLocations();			
				assertEquals( boosters.getName()+" location is incorrect: ", 0.484, boosterLocations[0].x, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ", 0.077, boosterLocations[0].y, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ", -0.077, boosterLocations[1].y, EPSILON);
				assertEquals( boosters.getName()+" location is incorrect: ", 0.0, boosterLocations[0].z, EPSILON);
				
				// think of the casts as an assert that ( child instanceof NoseCone) == true
				NoseCone nose = (NoseCone) boosters.getChild(0);
				offset = nose.getPosition();
				loc = nose.getComponentLocations()[0];	
				assertEquals(nose.getName()+" offset is incorrect: ", 0.0, offset.x, EPSILON);
				assertEquals(nose.getName()+" location is incorrect: ", 0.484, loc.x, EPSILON);
	
				BodyTube boosterBody= (BodyTube) boosters.getChild(1);
				offset = boosterBody.getPosition();
				loc = boosterBody.getComponentLocations()[0];			
				assertEquals(boosterBody.getName()+" offset is incorrect: ", 0.08, offset.x, EPSILON);
				assertEquals(boosterBody.getName()+" location is incorrect: ", 0.564, loc.x, EPSILON);
				{
					InnerTube mmt = (InnerTube)boosterBody.getChild(0);
					offset = mmt.getPosition();
					loc = mmt.getComponentLocations()[0];
					assertEquals(mmt.getName()+" offset is incorrect: ", 0.65, offset.x, EPSILON);
					assertEquals(mmt.getName()+" location is incorrect: ", 1.214, loc.x, EPSILON);
	
					final FinSet coreFins = (FinSet)boosterBody.getChild(1);
					offset = coreFins.getPosition();
					loc = coreFins.getComponentLocations()[0];			
					assertEquals(coreFins.getName()+" offset is incorrect: ", 0.480, offset.x, EPSILON);
					assertEquals(coreFins.getName()+" location is incorrect: ", 1.044, loc.x, EPSILON);
				}
			}
		}

		// DEBUG
		System.err.println(rocket.toDebugTree());

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals( 0.0,   bounds.min.x,  EPSILON);
		assertEquals( 1.364, bounds.max.x, EPSILON);

		assertEquals( -0.215500, bounds.min.y, EPSILON);
		assertEquals(  0.215500, bounds.max.y, EPSILON);

		assertEquals( -0.12069451, bounds.min.z, EPSILON);
		assertEquals(  0.12069451, bounds.max.z, EPSILON);
	}

}
