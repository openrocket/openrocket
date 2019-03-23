package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class LaunchLugTest extends BaseTestCase {
	protected final double EPSILON = MathUtil.EPSILON;
	
	@Test
	public void testLaunchLugLocationZeroAngle() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		BodyTube body= (BodyTube)rocket.getChild(0).getChild(1);
		LaunchLug lug = (LaunchLug)rocket.getChild(0).getChild(1).getChild(1);
		lug.setInstanceSeparation(0.05);
		lug.setInstanceCount(2);
		
		double expX = 0.111 + body.getLocations()[0].x;
		double expR = body.getOuterRadius()+lug.getOuterRadius();
		Coordinate expPos = new Coordinate( expX, expR, 0, 0);
		Coordinate actPos[] = lug.getLocations();
		assertEquals(" LaunchLug has the wrong x value: ", expPos.x, actPos[0].x, EPSILON);
		assertEquals(" LaunchLug has the wrong y value: ", expPos.y, actPos[0].y, EPSILON);
		assertEquals(" LaunchLug has the wrong z value: ", expPos.z, actPos[0].z, EPSILON);
		assertEquals(" LaunchLug has the wrong weight: ", 0, actPos[0].weight, EPSILON);
		assertEquals(" LaunchLug #1 is in the wrong place: ", expPos, actPos[0]);
		
		expPos = expPos.setX( expX+0.05 );
		assertEquals(" LaunchLug #2 is in the wrong place: ", expPos, actPos[1]);
	}
	
	@Test
	public void testLaunchLugLocationAtAngles() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		BodyTube body= (BodyTube)rocket.getChild(0).getChild(1);
		LaunchLug lug = (LaunchLug)rocket.getChild(0).getChild(1).getChild(1);
		double startAngle = Math.PI/2;
		lug.setAngularOffset( startAngle );
		lug.setInstanceSeparation(0.05);
		lug.setInstanceCount(2);

		double expX = 0.111 +  body.getLocations()[0].x;
		double expR = 0.015;
		double expY = Math.cos(startAngle)*expR ;
		double expZ = Math.sin(startAngle)*expR ;
		Coordinate expPos = new Coordinate( expX, expY, expZ, 0);
		Coordinate actPos[] = lug.getLocations();
		assertEquals(" LaunchLug has the wrong x value: ", expPos.x, actPos[0].x, EPSILON);
		assertEquals(" LaunchLug has the wrong y value: ", expPos.y, actPos[0].y, EPSILON);
		assertEquals(" LaunchLug has the wrong z value: ", expPos.z, actPos[0].z, EPSILON);
		assertEquals(" LaunchLug has the wrong weight: ", 0, actPos[0].weight, EPSILON);
		assertEquals(" LaunchLug is in the wrong place: ", expPos, actPos[0]);

		if( 1 < actPos.length){
			expPos = expPos.setX( expX+0.05 );
			assertEquals(" LaunchLug #2 is in the wrong place: ", expPos, actPos[1]);
		}
	}
	
}
