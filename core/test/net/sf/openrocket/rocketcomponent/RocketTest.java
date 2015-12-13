package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class RocketTest extends BaseTestCase {
	
	@Test
	public void testCopyFrom() {
//		Rocket r1 = net.sf.openrocket.util.TestRockets.makeIsoHaisu();
//		Rocket r2 = net.sf.openrocket.util.TestRockets.makeBigBlue();
//		
//		Rocket copy = (Rocket) r2.copy();
//		
//		ComponentCompare.assertDeepEquality(r2, copy);
//		
//		r1.copyFrom(copy);
//		
//		ComponentCompare.assertDeepEquality(r1, r2);
		fail("NYI");
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
