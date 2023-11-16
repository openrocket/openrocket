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
		double expR = -(body.getOuterRadius()+lug.getOuterRadius());
		Coordinate expPos = new Coordinate( expX, expR, 0, 0);
		Coordinate[] actPos = lug.getLocations();
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
		
		BodyTube body = (BodyTube)rocket.getChild(0).getChild(1);
		LaunchLug lug = (LaunchLug)rocket.getChild(0).getChild(1).getChild(1);
		double startAngle = Math.PI/2;
		lug.setAngleOffset( startAngle );
		lug.setInstanceSeparation(0.05);
		lug.setInstanceCount(2);

		double expX = 0.111 +  body.getLocations()[0].x;
		double expR = 0.015;
		double expY = Math.cos(startAngle)*expR ;
		double expZ = Math.sin(startAngle)*expR ;
		Coordinate expPos = new Coordinate( expX, expY, expZ, 0);
		Coordinate[] actPos = lug.getLocations();
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

	@Test
	public void testCMSingleInstance() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setOuterRadius(0.025);
		LaunchLug lug = new LaunchLug();
		lug.setLength(0.1);
		lug.setOuterRadius(0.02);
		bodyTube.addChild(lug);

		// Test normal CG
		Coordinate CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.05, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", -0.045, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.05, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.045, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.05, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.0225, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.03897114, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);


		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);

		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.025, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.04, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00309761, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.025, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.04, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00309761, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.025, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.02, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.034641016, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00309761, CG.weight, EPSILON);
	}

	@Test
	public void testCMSingleInstanceOverride() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setOuterRadius(0.025);
		LaunchLug lug = new LaunchLug();
		lug.setLength(0.1);
		lug.setOuterRadius(0.02);
		lug.setCGOverridden(true);
		lug.setOverrideCGX(0.0123);
		bodyTube.addChild(lug);

		// Test normal CG
		Coordinate CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", -0.045, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.045, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.0225, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.03897114, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.008331504, CG.weight, EPSILON);


		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setOverrideCGX(0.0321);
		lug.setMassOverridden(true);
		lug.setOverrideMass(0.1);

		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.04, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.1, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.04, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.1, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.02, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.034641016, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.1, CG.weight, EPSILON);
	}

	@Test
	public void testCMMultipleInstances() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setOuterRadius(0.025);
		LaunchLug lug = new LaunchLug();
		lug.setLength(0.1);
		lug.setOuterRadius(0.02);
		lug.setInstanceCount(3);
		lug.setInstanceSeparation(0.2);
		bodyTube.addChild(lug);

		// Test normal CG
		Coordinate CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.25, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", -0.045, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.25, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.045, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.25, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.0225, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.03897114, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);


		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setInstanceCount(2);
		lug.setInstanceSeparation(0.15);

		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.1, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.04, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00619522, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.1, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.04, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00619522, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.1, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.02, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.034641016, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.00619522, CG.weight, EPSILON);
	}

	@Test
	public void testCMMultipleInstancesOverride() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setOuterRadius(0.025);
		LaunchLug lug = new LaunchLug();
		lug.setLength(0.1);
		lug.setOuterRadius(0.02);
		lug.setInstanceCount(3);
		lug.setInstanceSeparation(0.2);
		lug.setCGOverridden(true);
		lug.setOverrideCGX(0.0123);
		bodyTube.addChild(lug);

		// Test normal CG
		Coordinate CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", -0.045, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.045, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.0225, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.03897114, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.024994512, CG.weight, EPSILON);


		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setInstanceCount(2);
		lug.setInstanceSeparation(0.15);
		lug.setOverrideCGX(0.0321);
		lug.setMassOverridden(true);
		lug.setOverrideMass(0.2);

		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.04, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.2, CG.weight, EPSILON);

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", 0.04, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.2, CG.weight, EPSILON);

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(" LaunchLug CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
		assertEquals(" LaunchLug CG has the wrong y value: ", 0.02, CG.y, EPSILON);
		assertEquals(" LaunchLug CG has the wrong z value: ", -0.034641016, CG.z, EPSILON);
		assertEquals(" LaunchLug CM has the wrong value: ", 0.2, CG.weight, EPSILON);
	}
	
}
