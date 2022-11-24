package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import org.junit.Test;


import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BoundingBoxTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testEstesAlphaIIIBoundingBox(){
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final BoundingBox bounds = config.getBoundingBoxAerodynamic();
		
		assertEquals("bounds max x",  0.000000000, bounds.min.x, EPSILON);
		assertEquals("bounds max x",  0.270000000, bounds.max.x, EPSILON);
		assertEquals("bounds min y", -0.032385640, bounds.min.y, EPSILON);
		assertEquals("bounds max y",  0.062000000, bounds.max.y, EPSILON);
		assertEquals("bounds min z", -0.054493575, bounds.min.z, EPSILON);
		assertEquals("bounds max z",  0.052893575, bounds.max.z, EPSILON);
	}


	@Test
	public void testBetaBoundingBox() {
		Rocket rocket = TestRockets.makeBeta();
		
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		
		{ // case A: All Stages
			config.setAllStages();

			// DEBUG
			System.err.println("==== Case A: All Stages ====");
			
			final BoundingBox bounds = config.getBoundingBoxAerodynamic();
			
			assertEquals("bounds min x",  0.000000000, bounds.min.x, EPSILON);
			assertEquals("bounds max x",  0.335000000, bounds.max.x, EPSILON);
			assertEquals("bounds min y", -0.032385640, bounds.min.y, EPSILON);
			assertEquals("bounds max y",  0.062000000, bounds.max.y, EPSILON);
			assertEquals("bounds min z", -0.054493575, bounds.min.z, EPSILON);
			assertEquals("bounds max z",  0.052893575, bounds.max.z, EPSILON);
		}
		{ // case B: Sustainer Only
			config.setOnlyStage(0);
			
			// DEBUG
			System.err.println("==== Case B: Sustainer Only ====");
			
			final BoundingBox bounds = config.getBoundingBoxAerodynamic();
			
			assertEquals("bounds min x",  0.000000000, bounds.min.x, EPSILON);
			assertEquals("bounds max x",  0.270000000, bounds.max.x, EPSILON);
			assertEquals("bounds min y", -0.032385640, bounds.min.y, EPSILON);
			assertEquals("bounds max y",  0.062000000, bounds.max.y, EPSILON);
			assertEquals("bounds min z", -0.054493575, bounds.min.z, EPSILON);
			assertEquals("bounds max z",  0.052893575, bounds.max.z, EPSILON);
		}
		{ // case C: Booster Only
			config.setOnlyStage(1);
			
			// DEBUG
			System.err.println("==== Case C: Booster Only ====");
			System.err.println(rocket.toDebugTree());
			
			final BoundingBox bounds = config.getBoundingBoxAerodynamic();
			
			assertEquals("bounds min x",  0.270000000, bounds.min.x, EPSILON);
			assertEquals("bounds max x",  0.335000000, bounds.max.x, EPSILON);
			assertEquals("bounds min y", -0.032385640, bounds.min.y, EPSILON);
			assertEquals("bounds max y",  0.062000000, bounds.max.y, EPSILON);
			assertEquals("bounds min z", -0.054493575, bounds.min.z, EPSILON);
			assertEquals("bounds max z",  0.052893575, bounds.max.z, EPSILON);
		}
	}
	
	@Test
	public void testFalcon9HBoundingBox() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();

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

	@Test
	public void testPodsBoundingBox() {
		Rocket rocket = TestRockets.makeEndPlateRocket();

		// DEBUG
		System.err.println(rocket.toDebugTree());

		BoundingBox bounds = rocket.getBoundingBox();
		assertEquals( 0.0,   bounds.min.x,  EPSILON);
		assertEquals( 0.304, bounds.max.x, EPSILON);

		assertEquals( -0.0365, bounds.min.y, EPSILON);
		assertEquals(  0.0365, bounds.max.y, EPSILON);

		assertEquals( -0.0365, bounds.min.z, EPSILON);
		assertEquals(  0.0365, bounds.max.z, EPSILON);

		// Add a mass component to the pod set (to test GitHub issue #1849)
		PodSet podSet = (PodSet) rocket.getChild(0).getChild(1).getChild(1);
		BodyTube tube = (BodyTube) podSet.getChild(0);
		tube.addChild(new MassComponent());

		bounds = rocket.getBoundingBox();
		assertEquals( 0.0,   bounds.min.x,  EPSILON);
		assertEquals( 0.304, bounds.max.x, EPSILON);

		assertEquals( -0.0365, bounds.min.y, EPSILON);
		assertEquals(  0.0365, bounds.max.y, EPSILON);

		assertEquals( -0.0365, bounds.min.z, EPSILON);
		assertEquals(  0.0365, bounds.max.z, EPSILON);
	}

}
