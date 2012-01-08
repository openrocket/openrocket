package net.sf.openrocket.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoordinateTest {
	
	private static final double EPS = 0.0000000001;

	@Test
	public void coordinateTest() {
		
		Coordinate x = new Coordinate(1,1,1,1);
		Coordinate y = new Coordinate(1,2,3,4);
		
		assertCoordinateEquals(new Coordinate(2,1,1,1), x.setX(2));
		assertCoordinateEquals(new Coordinate(1,2,1,1), x.setY(2));
		assertCoordinateEquals(new Coordinate(1,1,2,1), x.setZ(2));
		assertCoordinateEquals(new Coordinate(1,1,1,2), x.setWeight(2));
		assertCoordinateEquals(new Coordinate(2,3,4,1), x.setXYZ(y).add(1,1,1));
		
		assertFalse(x.isNaN());
		assertTrue(x.setX(Double.NaN).isNaN());
		assertTrue(Coordinate.NaN.isNaN());
		
		assertTrue(x.isWeighted());
		assertFalse(x.setWeight(0).isWeighted());
		
		
		assertCoordinateEquals(x, x.add(Coordinate.NUL));
		assertCoordinateEquals(new Coordinate(2,3,4,5), x.add(y));
		assertCoordinateEquals(new Coordinate(2,3,4,1), x.add(1,2,3));
		assertCoordinateEquals(new Coordinate(2,3,4,5), x.add(1,2,3,4));

		assertCoordinateEquals(new Coordinate(0,-1,-2,1), x.sub(y));
		assertCoordinateEquals(new Coordinate(0,-1,-2,1), x.sub(1,2,3));

		assertCoordinateEquals(new Coordinate(2,4,6,8), y.multiply(2));
		
		assertEquals(1+2+3, y.dot(x), EPS);
		assertEquals(1+2+3, x.dot(y), EPS);
		assertEquals(1+2+3, Coordinate.dot(x,y), EPS);
		assertEquals(x.dot(x), x.length2(), EPS);
		assertEquals(y.dot(y), y.length2(), EPS);
		assertEquals(Math.sqrt(1+4+9), y.length(), EPS);
		assertEquals(1, y.normalize().length(), EPS);
		
		assertCoordinateEquals(new Coordinate(1.75,1.75,1.75,4), 
				new Coordinate(1,1,1,1).average(new Coordinate(2,2,2,3)));
		assertCoordinateEquals(new Coordinate(1,1,1,1), 
				new Coordinate(1,1,1,1).average(new Coordinate(2,2,2,0)));
		assertCoordinateEquals(new Coordinate(1.5,1.5,1.5,0), 
				new Coordinate(1,1,1,0).average(new Coordinate(2,2,2,0)));
		
	}
	
	
	private void assertCoordinateEquals(Coordinate a, Coordinate b) {
		assertEquals(a, b);
		assertEquals(a.weight, b.weight, EPS);
	}
	
}
