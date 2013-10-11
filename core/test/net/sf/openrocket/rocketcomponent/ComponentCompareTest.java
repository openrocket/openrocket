package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class ComponentCompareTest extends BaseTestCase {
	
	@Test
	public void testComponentEquality() {
		
		//System.out.println("TEST CLASSPATH: " + System.getProperty("java.class.path"));
		
		Rocket r1 = net.sf.openrocket.util.TestRockets.makeBigBlue();
		Rocket r2 = net.sf.openrocket.util.TestRockets.makeBigBlue();
		
		Iterator<RocketComponent> i1 = r1.iterator(true);
		Iterator<RocketComponent> i2 = r2.iterator(true);
		while (i1.hasNext()) {
			assertTrue(i2.hasNext());
			
			RocketComponent c1 = i1.next();
			RocketComponent c2 = i2.next();
			
			ComponentCompare.assertEquality(c1, c2);
			ComponentCompare.assertSimilarity(c1, c2);
		}
		assertFalse(i2.hasNext());
		
		
		ComponentCompare.assertDeepEquality(r1, r2);
		ComponentCompare.assertDeepSimilarity(r1, r2, false);
		
		
		r1.setColor(Color.BLACK);
		try {
			ComponentCompare.assertEquality(r1, r2);
			fail();
		} catch (AssertionError e) {
			// Correct behavior
		}
		
		
		i1 = r1.iterator(true);
		i2 = r2.iterator(true);
		boolean finsetfound = false;
		while (i1.hasNext()) {
			RocketComponent c1 = i1.next();
			RocketComponent c2 = i2.next();
			
			if (c1 instanceof FinSet) {
				finsetfound = true;
				FinSet f1 = (FinSet) c1;
				f1.setTabHeight(0.001);
				
				try {
					ComponentCompare.assertEquality(c1, c2);
					fail();
				} catch (AssertionError e) {
					// Correct behavior
				}
			}
		}
		assertTrue(finsetfound);
	}
	
	
	@Test
	public void testComponentSimilarity() throws IllegalFinPointException {
		FinSet trap = new TrapezoidFinSet(
				5, // fins
				5.0, // root
				3.0, // tip
				0.0, // sweep
				2.0); // height
		FinSet free = new FreeformFinSet(new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0, 2),
				new Coordinate(3, 2),
				new Coordinate(5, 0)
		});
		free.setFinCount(5);
		
		ComponentCompare.assertSimilarity(trap, free, true);
		
		try {
			ComponentCompare.assertSimilarity(trap, free);
			fail();
		} catch (AssertionError e) {
			// Correct behavior
		}
		
		free.setName(trap.getName());
		ComponentCompare.assertSimilarity(trap, free);
		
		try {
			ComponentCompare.assertEquality(trap, free);
			fail();
		} catch (AssertionError e) {
			// Correct behavior
		}
		
		
		BodyTube t1 = new BodyTube();
		BodyTube t2 = new BodyTube();
		t1.addChild(free);
		t2.addChild(trap);
		
		ComponentCompare.assertDeepSimilarity(t1, t2, false);
		
		try {
			ComponentCompare.assertDeepEquality(t1, t2);
			fail();
		} catch (AssertionError e) {
			// Correct behavior
		}
		
		t1.addChild(new TrapezoidFinSet());
		
		try {
			ComponentCompare.assertDeepSimilarity(t1, t2, true);
			fail();
		} catch (AssertionError e) {
			// Correct behavior
		}
		
	}
	
}
