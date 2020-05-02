package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class TransitionTest extends BaseTestCase {
	protected final double EPSILON = MathUtil.EPSILON*1000;
	
	@Test
	public void testVerifyConicNose(){
		NoseCone nose = new NoseCone(Transition.Shape.CONICAL, 0.06, 0.01);
		assertEquals("nose cone length is wrong ", 0.06, nose.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 0.00, nose.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 0.01, nose.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.CONICAL, equalTo(nose.getType()));
		assertEquals("nose cone shape parameter is wrong ", 0.0, nose.getShapeParameter(), EPSILON );
		
		assertEquals("bad shape - conical forward ", 0.0, nose.getRadius(0.00), EPSILON );
		assertEquals("bad shape - conical forward ", 0.0025, nose.getRadius(0.015), EPSILON );
		assertEquals("bad shape - conical forward ", 0.005, nose.getRadius(0.03), EPSILON );
		assertEquals("bad shape - conical forward ", 0.0075, nose.getRadius(0.045), EPSILON );
		assertEquals("bad shape - conical forward ", 0.01, nose.getRadius(0.06), EPSILON );
		
	}

	@Test
	public void testVerifyForwardConicTransition(){
		Transition nose = new Transition();
		nose.setType( Transition.Shape.CONICAL);
		nose.setForeRadius( 0.5);
		nose.setAftRadius( 1.0);
		nose.setLength( 5.0);
		
		assertEquals("nose cone length is wrong ", 5.0, nose.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 0.5, nose.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 1.0, nose.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.CONICAL, equalTo(nose.getType()));
		assertEquals("nose cone shape parameter is wrong ", 0.0, nose.getShapeParameter(), EPSILON );
		
		assertEquals("bad shape - conical forward transition", 0.5, nose.getRadius(0.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.6, nose.getRadius(1.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.7, nose.getRadius(2.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.8, nose.getRadius(3.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.9, nose.getRadius(4.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 1.0, nose.getRadius(5.0), EPSILON );
		
	}


	@Test
	public void testVerifyBackwardConicTransition(){
		Transition tail = new Transition();
		tail.setType( Transition.Shape.CONICAL);
		tail.setForeRadius( 1.0);
		tail.setAftRadius( 0.5);
		tail.setLength( 5.0);

		assertEquals("nose cone length is wrong ", 5.0, tail.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 1.0, tail.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 0.5, tail.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.CONICAL, equalTo(tail.getType()));
		assertEquals("nose cone shape parameter is wrong ", 0.0, tail.getShapeParameter(), EPSILON );

		assertEquals("bad shape - conical forward transition", 1.0, tail.getRadius(0.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.9, tail.getRadius(1.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.8, tail.getRadius(2.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.7, tail.getRadius(3.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.6, tail.getRadius(4.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.5, tail.getRadius(5.0), EPSILON );
	}

	@Test
	public void testVerifyOgiveNoseCone(){
		Transition nose = new Transition();
		nose.setType( Transition.Shape.OGIVE);
		nose.setForeRadius( 0.0);
		nose.setAftRadius( 1.0);
		nose.setLength( 8.0);

		assertEquals("nose cone length is wrong ", 8.0, nose.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 0.0, nose.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 1.0, nose.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.OGIVE, equalTo(nose.getType()));
		assertEquals("nose cone shape parameter is wrong ", 1.0, nose.getShapeParameter(), EPSILON );

		assertEquals("bad shape - conical forward transition", 0.0, nose.getRadius(0.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.23720214511, nose.getRadius(1.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.44135250736, nose.getRadius(2.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.61308144666, nose.getRadius(3.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.75290684574, nose.getRadius(4.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.86124225056, nose.getRadius(5.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.93840316661, nose.getRadius(6.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.98461174156, nose.getRadius(7.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 1.0, nose.getRadius(8.0), EPSILON );
	}

	@Test
	public void testVerifyForwardOgiveTransition(){
		Transition transition = new Transition();
		transition.setType( Transition.Shape.OGIVE);
		transition.setForeRadius( 0.44135);
		transition.setAftRadius( 1.0);
		transition.setLength( 6.0);

		assertEquals("nose cone length is wrong ", 6.0, transition.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 0.44135, transition.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 1.0, transition.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.OGIVE, equalTo(transition.getType()));
		assertEquals("nose cone shape parameter is wrong ", 1.0, transition.getShapeParameter(), EPSILON );

		assertEquals("bad shape - conical forward transition", 0.44135250736, transition.getRadius(0.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.61308144666, transition.getRadius(1.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.75290684574, transition.getRadius(2.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.86124225056, transition.getRadius(3.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.93840316661, transition.getRadius(4.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.98461174156, transition.getRadius(5.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 1.0, transition.getRadius(6.0), EPSILON );
	}

	@Test
	public void testVerifyBackwardOgiveTransition(){
		Transition transition = new Transition();
		transition.setType( Transition.Shape.OGIVE);
		transition.setForeRadius( 1.0);
		transition.setAftRadius( 0.44135);
		transition.setLength( 6.0);

		assertEquals("nose cone length is wrong ", 6.0, transition.getLength(), EPSILON );
		assertEquals("nose cone fore radius is wrong ", 1.0, transition.getForeRadius(), EPSILON );
		assertEquals("nose cone aft radius is wrong ", 0.44135, transition.getAftRadius(), EPSILON );
		assertThat("nose cone shape type is wrong ", Transition.Shape.OGIVE, equalTo(transition.getType()));
		assertEquals("nose cone shape parameter is wrong ",1.0, transition.getShapeParameter(), EPSILON );

		assertEquals("bad shape - conical forward transition", 1.0, transition.getRadius(0.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.98461174156, transition.getRadius(1.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.93840316661, transition.getRadius(2.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.86124225056, transition.getRadius(3.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.75290684574, transition.getRadius(4.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.61308144666, transition.getRadius(5.0), EPSILON );
		assertEquals("bad shape - conical forward transition", 0.44135250736, transition.getRadius(6.0), EPSILON );
	}

	@Test
	public void testStockIntegration(){
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		NoseCone nose = (NoseCone)rocket.getChild(0).getChild(0);

		assertEquals("Alpha3 nose cone length is wrong ", 0.07, nose.getLength(), EPSILON );
		assertEquals("Alpha3 nose cone fore radius is wrong ", 0.00, nose.getForeRadius(), EPSILON );
		assertEquals("Alpha3 nose cone aft radius is wrong ", 0.012, nose.getAftRadius(), EPSILON );
		assertThat("Alpha3 nose cone shape type is wrong ", Transition.Shape.OGIVE, equalTo(nose.getType()));
		assertEquals("Alpha3 nose cone shape parameter is wrong ", 1.0, nose.getShapeParameter(), EPSILON );

		assertEquals("Alpha3 nose cone aft shoulder length is wrong ", 0.02, nose.getAftShoulderLength(), EPSILON );
		assertEquals("Alpha3 nose cone aft shoulder radius is wrong ", 0.011, nose.getAftShoulderRadius(), EPSILON );
	}
	
}
