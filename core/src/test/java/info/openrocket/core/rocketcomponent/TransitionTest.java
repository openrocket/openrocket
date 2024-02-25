package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class TransitionTest extends BaseTestCase {
	protected final double EPSILON = MathUtil.EPSILON * 1000;

	@Test
	public void testVerifyConicNose() {
		NoseCone nose = new NoseCone(Transition.Shape.CONICAL, 0.06, 0.01);
		assertEquals(0.06, nose.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(0.00, nose.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(0.01, nose.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.CONICAL, nose.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(0.0, nose.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(0.0, nose.getRadius(0.00), EPSILON, "bad shape - conical forward ");
		assertEquals(0.0025, nose.getRadius(0.015), EPSILON, "bad shape - conical forward ");
		assertEquals(0.005, nose.getRadius(0.03), EPSILON, "bad shape - conical forward ");
		assertEquals(0.0075, nose.getRadius(0.045), EPSILON, "bad shape - conical forward ");
		assertEquals(0.01, nose.getRadius(0.06), EPSILON, "bad shape - conical forward ");

	}

	@Test
	public void testVerifyForwardConicTransition() {
		Transition nose = new Transition();
		nose.setShapeType(Transition.Shape.CONICAL);
		nose.setForeRadius(0.5);
		nose.setAftRadius(1.0);
		nose.setLength(5.0);

		assertEquals(5.0, nose.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(0.5, nose.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(1.0, nose.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.CONICAL, nose.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(0.0, nose.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(0.5, nose.getRadius(0.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.6, nose.getRadius(1.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.7, nose.getRadius(2.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.8, nose.getRadius(3.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.9, nose.getRadius(4.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(1.0, nose.getRadius(5.0), EPSILON, "bad shape - conical forward transition");

	}

	@Test
	public void testVerifyBackwardConicTransition() {
		Transition tail = new Transition();
		tail.setShapeType(Transition.Shape.CONICAL);
		tail.setForeRadius(1.0);
		tail.setAftRadius(0.5);
		tail.setLength(5.0);

		assertEquals(5.0, tail.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(1.0, tail.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(0.5, tail.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.CONICAL, tail.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(0.0, tail.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(1.0, tail.getRadius(0.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.9, tail.getRadius(1.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.8, tail.getRadius(2.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.7, tail.getRadius(3.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.6, tail.getRadius(4.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.5, tail.getRadius(5.0), EPSILON, "bad shape - conical forward transition");
	}

	@Test
	public void testVerifyOgiveNoseCone() {
		Transition nose = new Transition();
		nose.setShapeType(Transition.Shape.OGIVE);
		nose.setForeRadius(0.0);
		nose.setAftRadius(1.0);
		nose.setLength(8.0);

		assertEquals(8.0, nose.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(0.0, nose.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(1.0, nose.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.OGIVE, nose.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(1.0, nose.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(0.0, nose.getRadius(0.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.23720214511, nose.getRadius(1.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.44135250736, nose.getRadius(2.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.61308144666, nose.getRadius(3.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.75290684574, nose.getRadius(4.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.86124225056, nose.getRadius(5.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.93840316661, nose.getRadius(6.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.98461174156, nose.getRadius(7.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(1.0, nose.getRadius(8.0), EPSILON, "bad shape - conical forward transition");
	}

	@Test
	public void testVerifyForwardOgiveTransition() {
		Transition transition = new Transition();
		transition.setShapeType(Transition.Shape.OGIVE);
		transition.setForeRadius(0.44135);
		transition.setAftRadius(1.0);
		transition.setLength(6.0);

		assertEquals(6.0, transition.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(0.44135, transition.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(1.0, transition.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.OGIVE, transition.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(1.0, transition.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(0.44135250736, transition.getRadius(0.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.61308144666, transition.getRadius(1.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.75290684574, transition.getRadius(2.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.86124225056, transition.getRadius(3.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.93840316661, transition.getRadius(4.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.98461174156, transition.getRadius(5.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(1.0, transition.getRadius(6.0), EPSILON, "bad shape - conical forward transition");
	}

	@Test
	public void testVerifyBackwardOgiveTransition() {
		Transition transition = new Transition();
		transition.setShapeType(Transition.Shape.OGIVE);
		transition.setForeRadius(1.0);
		transition.setAftRadius(0.44135);
		transition.setLength(6.0);

		assertEquals(6.0, transition.getLength(), EPSILON, "nose cone length is wrong ");
		assertEquals(1.0, transition.getForeRadius(), EPSILON, "nose cone fore radius is wrong ");
		assertEquals(0.44135, transition.getAftRadius(), EPSILON, "nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.OGIVE, transition.getShapeType(), "nose cone shape type is wrong ");
		assertEquals(1.0, transition.getShapeParameter(), EPSILON, "nose cone shape parameter is wrong ");

		assertEquals(1.0, transition.getRadius(0.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.98461174156, transition.getRadius(1.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.93840316661, transition.getRadius(2.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.86124225056, transition.getRadius(3.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.75290684574, transition.getRadius(4.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.61308144666, transition.getRadius(5.0), EPSILON, "bad shape - conical forward transition");
		assertEquals(0.44135250736, transition.getRadius(6.0), EPSILON, "bad shape - conical forward transition");
	}

	@Test
	public void testStockIntegration() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);

		assertEquals(0.07, nose.getLength(), EPSILON, "Alpha3 nose cone length is wrong ");
		assertEquals(0.00, nose.getForeRadius(), EPSILON, "Alpha3 nose cone fore radius is wrong ");
		assertEquals(0.012, nose.getAftRadius(), EPSILON, "Alpha3 nose cone aft radius is wrong ");
		assertEquals(Transition.Shape.OGIVE, nose.getShapeType(), "Alpha3 nose cone shape type is wrong ");
		assertEquals(1.0, nose.getShapeParameter(), EPSILON, "Alpha3 nose cone shape parameter is wrong ");

		assertEquals(0.02, nose.getAftShoulderLength(), EPSILON, "Alpha3 nose cone aft shoulder length is wrong ");
		assertEquals(0.011, nose.getAftShoulderRadius(), EPSILON, "Alpha3 nose cone aft shoulder radius is wrong ");
	}

}
