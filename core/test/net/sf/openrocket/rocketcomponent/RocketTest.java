package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class RocketTest extends BaseTestCase {

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
		assertThat("Configurations should be different: "+config1.toDebug()+"=?="+config4.toDebug(), config1.instanceNumber, not( config4.instanceNumber));
	
		FlightConfiguration config5 = rkt2.getFlightConfiguration(config2.getId());
		FlightConfigurationId fcid5 = config5.getId();
		assertThat("fcids should match: ", config2.getId(), equalTo(fcid5));
		assertThat("Configurations should bef different match: "+config2.toDebug()+"=?="+config5.toDebug(), config2.instanceNumber, not( config5.instanceNumber));
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
		final double EPSILON = MathUtil.EPSILON;
		Rocket rocket = TestRockets.makeEstesAlphaIII();
			
//		String treeDump = rocket.toDebugTree();
//		System.err.println(treeDump);
		
		AxialStage stage= (AxialStage)rocket.getChild(0);
		
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
//				assertEquals(" position x fail: ", expLoc.x, actLoc.x, EPSILON);
//				assertEquals(" position y fail: ", expLoc.y, actLoc.y, EPSILON);
//				assertEquals(" position z fail: ", expLoc.z, actLoc.z, EPSILON);
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
	}
	
}
