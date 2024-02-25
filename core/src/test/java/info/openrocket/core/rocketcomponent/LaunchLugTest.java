package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class LaunchLugTest extends BaseTestCase {
	protected final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testLaunchLugLocationZeroAngle() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();

		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		LaunchLug lug = (LaunchLug) rocket.getChild(0).getChild(1).getChild(1);
		lug.setInstanceSeparation(0.05);
		lug.setInstanceCount(2);

		double expX = 0.111 + body.getLocations()[0].x;
		double expR = -(body.getOuterRadius() + lug.getOuterRadius());
		Coordinate expPos = new Coordinate(expX, expR, 0, 0);
		Coordinate[] actPos = lug.getLocations();
		assertEquals(expPos.x, actPos[0].x, EPSILON, " LaunchLug has the wrong x value: ");
		assertEquals(expPos.y, actPos[0].y, EPSILON, " LaunchLug has the wrong y value: ");
		assertEquals(expPos.z, actPos[0].z, EPSILON, " LaunchLug has the wrong z value: ");
		assertEquals(0, actPos[0].weight, EPSILON, " LaunchLug has the wrong weight: ");
		assertEquals(expPos, actPos[0], " LaunchLug #1 is in the wrong place: ");

		expPos = expPos.setX(expX + 0.05);
		assertEquals(expPos, actPos[1], " LaunchLug #2 is in the wrong place: ");
	}

	@Test
	public void testLaunchLugLocationAtAngles() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();

		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		LaunchLug lug = (LaunchLug) rocket.getChild(0).getChild(1).getChild(1);
		double startAngle = Math.PI / 2;
		lug.setAngleOffset(startAngle);
		lug.setInstanceSeparation(0.05);
		lug.setInstanceCount(2);

		double expX = 0.111 + body.getLocations()[0].x;
		double expR = 0.015;
		double expY = Math.cos(startAngle) * expR;
		double expZ = Math.sin(startAngle) * expR;
		Coordinate expPos = new Coordinate(expX, expY, expZ, 0);
		Coordinate[] actPos = lug.getLocations();
		assertEquals(expPos.x, actPos[0].x, EPSILON, " LaunchLug has the wrong x value: ");
		assertEquals(expPos.y, actPos[0].y, EPSILON, " LaunchLug has the wrong y value: ");
		assertEquals(expPos.z, actPos[0].z, EPSILON, " LaunchLug has the wrong z value: ");
		assertEquals(0, actPos[0].weight, EPSILON, " LaunchLug has the wrong weight: ");
		assertEquals(expPos, actPos[0], " LaunchLug is in the wrong place: ");

		if (1 < actPos.length) {
			expPos = expPos.setX(expX + 0.05);
			assertEquals(expPos, actPos[1], " LaunchLug #2 is in the wrong place: ");
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
		assertEquals(0.05, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.05, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.05, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);

		CG = lug.getCG();
		assertEquals(0.025, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.025, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.025, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");
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
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setOverrideCGX(0.0321);
		lug.setMassOverridden(true);
		lug.setOverrideMass(0.1);

		CG = lug.getCG();
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");
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
		assertEquals(0.25, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.25, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.25, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setInstanceCount(2);
		lug.setInstanceSeparation(0.15);

		CG = lug.getCG();
		assertEquals(0.1, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.1, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.1, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");
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
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0123, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

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
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0321, CG.x, EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.y, EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.z, EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.weight, EPSILON, " LaunchLug CM has the wrong value: ");
	}

}
