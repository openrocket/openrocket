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
}
