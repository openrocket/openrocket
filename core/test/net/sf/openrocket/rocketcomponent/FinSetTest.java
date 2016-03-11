package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FinSetTest extends BaseTestCase {
	
	@Test
	public void testTrapezoidCGComputation() {
		
		{
			// This is a simple square fin with sides of 1.0.
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(1.0, fins.getFinArea(), 0.001);
			assertEquals(0.5, coords.x, 0.001);
			assertEquals(0.5, coords.y, 0.001);
		}
		
		{
			// This is a trapezoid.  Height 1, root 1, tip 1/2 no sweep.
			// It can be decomposed into a rectangle followed by a triangle
			//  +---+
			//  |    \
			//  |     \
			//  +------+
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 0.5, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
	}
	
}
