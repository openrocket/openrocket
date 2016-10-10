package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.MathUtil;

public class PointWeightTest {
	
	private static final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testAdd() {

		PointWeight x = new PointWeight(1,2,1,1);
		PointWeight y = new PointWeight(2,1,1,2);
		
		PointWeight sum = x.add(y);
		
		assertEquals( 3.0, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testAverage() {

		PointWeight x = new PointWeight(1,2,1,1);
		PointWeight y = new PointWeight(2,1,1,2);
		
		PointWeight sum = x.average(y);
		
		assertEquals( 1.5, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testSubtract() {
		PointWeight p1 = new PointWeight(1,2,1,1);
		PointWeight p2 = new PointWeight(2,1,1,2);
		
		final PointWeight sum = p1.add(p2);
		
		final PointWeight result = sum.subtract( p2); 
		
		assertEquals( p1.w, result.w, EPSILON);
		assertEquals( p1.x, result.x, EPSILON);
		assertEquals( p1.y, result.y, EPSILON);
		assertEquals( p1.z, result.z, EPSILON);
	}

	
}
