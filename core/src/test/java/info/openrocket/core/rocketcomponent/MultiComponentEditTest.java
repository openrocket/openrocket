package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for editing multiple selected components at once, which works by registering
 * the other selected components as config listeners of the edited component.
 */
public class MultiComponentEditTest extends BaseTestCase {

	@Test
	public void testBodyTubeLengthAppliedToAllSelectedBodyTubes() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube tube1 = new BodyTube();
		tube1.setLength(0.1);
		stage.addChild(tube1);
		BodyTube tube2 = new BodyTube();
		tube2.setLength(0.2);
		stage.addChild(tube2);

		// Select both tubes: tube1 is edited, tube2 follows its changes
		assertTrue(tube1.addConfigListener(tube2));

		tube1.setLength(0.5);

		assertEquals(0.5, tube1.getLength(), MathUtil.EPSILON);
		assertEquals(0.5, tube2.getLength(), MathUtil.EPSILON);
	}

	@Test
	public void testBodyTubeOuterRadiusAppliedToAllSelectedBodyTubes() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube tube1 = new BodyTube();
		tube1.setOuterRadius(0.02);
		stage.addChild(tube1);
		BodyTube tube2 = new BodyTube();
		tube2.setOuterRadius(0.03);
		stage.addChild(tube2);

		assertTrue(tube1.addConfigListener(tube2));

		tube1.setOuterRadius(0.05);

		assertEquals(0.05, tube1.getOuterRadius(), MathUtil.EPSILON);
		assertEquals(0.05, tube2.getOuterRadius(), MathUtil.EPSILON);
	}

	@Test
	public void testDeselectedBodyTubeNoLongerFollowsEdits() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube tube1 = new BodyTube();
		tube1.setLength(0.1);
		stage.addChild(tube1);
		BodyTube tube2 = new BodyTube();
		tube2.setLength(0.2);
		stage.addChild(tube2);

		assertTrue(tube1.addConfigListener(tube2));
		tube1.setLength(0.5);
		assertEquals(0.5, tube2.getLength(), MathUtil.EPSILON);

		// Deselect tube2: later edits to tube1 must not change it
		tube1.removeConfigListener(tube2);
		tube1.setLength(0.7);

		assertEquals(0.7, tube1.getLength(), MathUtil.EPSILON);
		assertEquals(0.5, tube2.getLength(), MathUtil.EPSILON);
	}

	@Test
	public void testLengthAppliedToMoreThanTwoSelectedBodyTubes() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube tube1 = new BodyTube();
		tube1.setLength(0.1);
		stage.addChild(tube1);
		BodyTube tube2 = new BodyTube();
		tube2.setLength(0.2);
		stage.addChild(tube2);
		BodyTube tube3 = new BodyTube();
		tube3.setLength(0.3);
		stage.addChild(tube3);

		assertTrue(tube1.addConfigListener(tube2));
		assertTrue(tube1.addConfigListener(tube3));

		tube1.setLength(0.5);

		assertEquals(0.5, tube1.getLength(), MathUtil.EPSILON);
		assertEquals(0.5, tube2.getLength(), MathUtil.EPSILON);
		assertEquals(0.5, tube3.getLength(), MathUtil.EPSILON);
	}

	@Test
	public void testLengthAppliedToSelectedComponentOfDifferentType() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.1);
		stage.addChild(noseCone);
		BodyTube tube = new BodyTube();
		tube.setLength(0.2);
		stage.addChild(tube);

		// Edit the nose cone with the body tube also selected
		assertTrue(noseCone.addConfigListener(tube));

		noseCone.setLength(0.5);

		assertEquals(0.5, noseCone.getLength(), MathUtil.EPSILON);
		assertEquals(0.5, tube.getLength(), MathUtil.EPSILON);
	}
}
