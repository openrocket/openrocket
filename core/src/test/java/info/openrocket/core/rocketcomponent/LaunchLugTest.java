package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.Test;

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

		double expX = 0.111 + body.getComponentLocations()[0].getX();
		double expR = -(body.getOuterRadius() + lug.getOuterRadius());
		CoordinateIF expPos = new Coordinate(expX, expR, 0, 0);
		CoordinateIF[] actPos = lug.getComponentLocations();
		assertEquals(expPos.getX(), actPos[0].getX(), EPSILON, " LaunchLug has the wrong x value: ");
		assertEquals(expPos.getY(), actPos[0].getY(), EPSILON, " LaunchLug has the wrong y value: ");
		assertEquals(expPos.getZ(), actPos[0].getZ(), EPSILON, " LaunchLug has the wrong z value: ");
		assertEquals(0, actPos[0].getWeight(), EPSILON, " LaunchLug has the wrong weight: ");
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

		double expX = 0.111 + body.getComponentLocations()[0].getX();
		double expR = 0.015;
		double expY = Math.cos(startAngle) * expR;
		double expZ = Math.sin(startAngle) * expR;
		CoordinateIF expPos = new Coordinate(expX, expY, expZ, 0);
		CoordinateIF[] actPos = lug.getComponentLocations();
		assertEquals(expPos.getX(), actPos[0].getX(), EPSILON, " LaunchLug has the wrong x value: ");
		assertEquals(expPos.getY(), actPos[0].getY(), EPSILON, " LaunchLug has the wrong y value: ");
		assertEquals(expPos.getZ(), actPos[0].getZ(), EPSILON, " LaunchLug has the wrong z value: ");
		assertEquals(0, actPos[0].getWeight(), EPSILON, " LaunchLug has the wrong weight: ");
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
		CoordinateIF CG = lug.getCG();
		assertEquals(0.05, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.05, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.05, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);

		CG = lug.getCG();
		assertEquals(0.025, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.025, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.025, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00309761, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");
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
		CoordinateIF CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.008331504, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setOverrideCGX(0.0321);
		lug.setMassOverridden(true);
		lug.setOverrideMass(0.1);

		CG = lug.getCG();
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.1, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");
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
		CoordinateIF CG = lug.getCG();
		assertEquals(0.25, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.25, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.25, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Change dimensions
		lug.setLength(0.05);
		lug.setOuterRadius(0.015);
		lug.setAngleOffset(0);
		lug.setInstanceCount(2);
		lug.setInstanceSeparation(0.15);

		CG = lug.getCG();
		assertEquals(0.1, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.1, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.1, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.00619522, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");
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
		CoordinateIF CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(-0.045, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.045, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0123, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.0225, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.03897114, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.024994512, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

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
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.04, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		// Test rotated CG
		lug.setAngleOffset(Math.PI / 2);
		CG = lug.getCG();
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(0.04, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");

		lug.setAngleOffset(-Math.PI / 3);
		CG = lug.getCG();
		assertEquals(0.0321, CG.getX(), EPSILON, " LaunchLug CG has the wrong x value: ");
		assertEquals(0.02, CG.getY(), EPSILON, " LaunchLug CG has the wrong y value: ");
		assertEquals(-0.034641016, CG.getZ(), EPSILON, " LaunchLug CG has the wrong z value: ");
		assertEquals(0.2, CG.getWeight(), EPSILON, " LaunchLug CM has the wrong value: ");
	}

}
