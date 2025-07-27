package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

import info.openrocket.core.util.BaseTestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoundingBoxTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testEstesAlphaIIIBoundingBox() {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();

		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final BoundingBox bounds = config.getBoundingBoxAerodynamic();

		assertEquals(0.000000000, bounds.min.x, EPSILON, "bounds max x");
		assertEquals(0.270000000, bounds.max.x, EPSILON, "bounds max x");
		assertEquals(-0.032385640, bounds.min.y, EPSILON, "bounds min y");
		assertEquals(0.062000000, bounds.max.y, EPSILON, "bounds max y");
		assertEquals(-0.054493575, bounds.min.z, EPSILON, "bounds min z");
		assertEquals(0.052893575, bounds.max.z, EPSILON, "bounds max z");
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

			assertEquals(0.000000000, bounds.min.x, EPSILON, "bounds min x");
			assertEquals(0.335000000, bounds.max.x, EPSILON, "bounds max x");
			assertEquals(-0.032385640, bounds.min.y, EPSILON, "bounds min y");
			assertEquals(0.062000000, bounds.max.y, EPSILON, "bounds max y");
			assertEquals(-0.054493575, bounds.min.z, EPSILON, "bounds min z");
			assertEquals(0.052893575, bounds.max.z, EPSILON, "bounds max z");
		}
		{ // case B: Sustainer Only
			config.setOnlyStage(0);

			// DEBUG
			System.err.println("==== Case B: Sustainer Only ====");

			final BoundingBox bounds = config.getBoundingBoxAerodynamic();

			assertEquals(0.000000000, bounds.min.x, EPSILON, "bounds min x");
			assertEquals(0.270000000, bounds.max.x, EPSILON, "bounds max x");
			assertEquals(-0.032385640, bounds.min.y, EPSILON, "bounds min y");
			assertEquals(0.062000000, bounds.max.y, EPSILON, "bounds max y");
			assertEquals(-0.054493575, bounds.min.z, EPSILON, "bounds min z");
			assertEquals(0.052893575, bounds.max.z, EPSILON, "bounds max z");
		}
		{ // case C: Booster Only
			config.setOnlyStage(1);

			// DEBUG
			System.err.println("==== Case C: Booster Only ====");
			System.err.println(rocket.toDebugTree());

			final BoundingBox bounds = config.getBoundingBoxAerodynamic();

			assertEquals(0.270000000, bounds.min.x, EPSILON, "bounds min x");
			assertEquals(0.335000000, bounds.max.x, EPSILON, "bounds max x");
			assertEquals(-0.032385640, bounds.min.y, EPSILON, "bounds min y");
			assertEquals(0.062000000, bounds.max.y, EPSILON, "bounds max y");
			assertEquals(-0.054493575, bounds.min.z, EPSILON, "bounds min z");
			assertEquals(0.052893575, bounds.max.z, EPSILON, "bounds max z");
		}
	}

	@Test
	public void testFalcon9HBoundingBox() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();

		// DEBUG
		System.err.println(rocket.toDebugTree());

		final BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(0.0, bounds.min.x, EPSILON);
		assertEquals(1.364, bounds.max.x, EPSILON);

		assertEquals(-0.215500, bounds.min.y, EPSILON);
		assertEquals(0.215500, bounds.max.y, EPSILON);

		assertEquals(-0.12069451, bounds.min.z, EPSILON);
		assertEquals(0.12069451, bounds.max.z, EPSILON);
	}

	@Test
	public void testPodsBoundingBox() {
		Rocket rocket = TestRockets.makeEndPlateRocket();

		// DEBUG
		System.err.println(rocket.toDebugTree());

		BoundingBox bounds = rocket.getBoundingBox();
		assertEquals(0.0, bounds.min.x, EPSILON);
		assertEquals(0.304, bounds.max.x, EPSILON);

		assertEquals(-0.0365, bounds.min.y, EPSILON);
		assertEquals(0.0365, bounds.max.y, EPSILON);

		assertEquals(-0.0365, bounds.min.z, EPSILON);
		assertEquals(0.0365, bounds.max.z, EPSILON);

		// Add a mass component to the pod set (to test GitHub issue #1849)
		PodSet podSet = (PodSet) rocket.getChild(0).getChild(1).getChild(1);
		BodyTube tube = (BodyTube) podSet.getChild(0);
		tube.addChild(new MassComponent());

		bounds = rocket.getBoundingBox();
		assertEquals(0.0, bounds.min.x, EPSILON);
		assertEquals(0.304, bounds.max.x, EPSILON);

		assertEquals(-0.0365, bounds.min.y, EPSILON);
		assertEquals(0.0365, bounds.max.y, EPSILON);

		assertEquals(-0.0365, bounds.min.z, EPSILON);
		assertEquals(0.0365, bounds.max.z, EPSILON);
	}

}
