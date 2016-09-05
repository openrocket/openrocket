package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParticleTest {
	
	private static final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testAdd() {

		WeightVector x = new WeightVector(1,2,1,1);
		WeightVector y = new WeightVector(2,1,1,2);
		
		WeightVector sum = x.add(y);
		
		assertEquals( 3.0, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testAverage() {

		WeightVector x = new WeightVector(1,2,1,1);
		WeightVector y = new WeightVector(2,1,1,2);
		
		WeightVector sum = x.average(y);
		
		assertEquals( 1.5, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testSubtract() {
		WeightVector p1 = new WeightVector(1,2,1,1);
		WeightVector p2 = new WeightVector(2,1,1,2);
		
		final WeightVector sum = p1.add(p2);
		
		final WeightVector result = sum.subtract( p2); 
		
		assertEquals( p1.w, result.w, EPSILON);
		assertEquals( p1.x, result.x, EPSILON);
		assertEquals( p1.y, result.y, EPSILON);
		assertEquals( p1.z, result.z, EPSILON);
	}

	
}
