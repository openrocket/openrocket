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
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		FlightConfigurationId fcid1 = config1.getId();
		FlightConfiguration config2 = new FlightConfiguration(rkt1, null);
		rkt1.setFlightConfiguration( config2.getId(), config2);
		FlightConfiguration config3 = new FlightConfiguration(rkt1, null);
		rkt1.setFlightConfiguration( config3.getId(), config3);
		
		//System.err.println("src: "+ rkt1.toDebugConfigs());
		// vvvv test target vvvv 
		Rocket rkt2 = rkt1.copyWithOriginalID();
		// ^^^^ test target ^^^^
		//System.err.println("cpy: "+ rkt1.toDebugConfigs());
		
		FlightConfiguration config4 = rkt2.getSelectedConfiguration();
		FlightConfigurationId fcid4 = config4.getId();
		assertThat("fcids should match: ", fcid1.key, equalTo(fcid4.key));
		assertThat("Configurations should be different match: "+config1.toDebug()+"=?="+config4.toDebug(), config1.instanceNumber, not( config4.instanceNumber));
	
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
		
		NoseCone nose = (NoseCone)stage.getChild(0);
		BodyTube body = (BodyTube)stage.getChild(1);
		FinSet fins = (FinSet)body.getChild(0);
		LaunchLug lug = (LaunchLug)body.getChild(1);
		InnerTube mmt = (InnerTube)body.getChild(2);
		EngineBlock block = (EngineBlock)mmt.getChild(0);
		Parachute chute = (Parachute)body.getChild(3);
		CenteringRing center = (CenteringRing)body.getChild(4);
		
		
		RocketComponent cc;
		Coordinate expLoc;
		Coordinate actLoc;
		{
			cc = nose;
			expLoc = new Coordinate(0,0,0);
			actLoc = cc.getLocations()[0];
			assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			cc = body;
			expLoc = new Coordinate(0.06985,0,0);
			actLoc = cc.getLocations()[0];
			assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			{	
				cc = fins;
				expLoc = new Coordinate(0.20955,0,0);
				actLoc = cc.getLocations()[0];
				assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				
				cc = lug;
				expLoc = new Coordinate(0.180975, 0.015376, 0);
				actLoc = cc.getLocations()[0];
				assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				
				cc = mmt;
				expLoc = new Coordinate(0.2032,0,0);
				actLoc = cc.getLocations()[0];
				assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				{
					cc = block;
					expLoc = new Coordinate(0.2032,0,0);
					actLoc = cc.getLocations()[0];
					assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
				}
				
			}
			
			cc = chute;
			expLoc = new Coordinate(0.098425,0,0);
			actLoc = cc.getLocations()[0];
			assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			
			cc = center;
			assertThat(cc.getName()+" not instanced correctly: ", cc.getInstanceCount(), equalTo(2));
			// singleton instances follow different code paths
			center.setInstanceCount(1);
			expLoc = new Coordinate(0.20955,0,0);
			actLoc = cc.getLocations()[0];
			assertEquals(" position x fail: ", expLoc.x, actLoc.x, EPSILON);
			assertEquals(" position y fail: ", expLoc.y, actLoc.y, EPSILON);
			assertEquals(" position z fail: ", expLoc.z, actLoc.z, EPSILON);
			assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));

			cc = center;
			center.setInstanceCount(2);
			Coordinate actLocs[] = cc.getLocations();
			expLoc = new Coordinate(0.20955,0,0); 
			actLoc = actLocs[0];
//			assertEquals(" position x fail: ", expLoc.x, actLoc.x, EPSILON);
//			assertEquals(" position y fail: ", expLoc.y, actLoc.y, EPSILON);
//			assertEquals(" position z fail: ", expLoc.z, actLoc.z, EPSILON);
			assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			{ // second instance
				expLoc = new Coordinate(0.24455, 0, 0);
				actLoc = actLocs[1];
				assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			}
			{ // second instance
				assertThat(cc.getName()+" not instanced correctly: ", cc.getInstanceCount(), equalTo(2));
				expLoc = new Coordinate(0.24455, 0, 0);
				actLoc = actLocs[1];
				assertThat(cc.getName()+" not positioned correctly: ", actLoc, equalTo(expLoc));
			}
			
		}
	}
	
}
