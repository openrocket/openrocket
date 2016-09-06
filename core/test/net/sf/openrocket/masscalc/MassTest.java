package net.sf.openrocket.masscalc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.Mass;
import net.sf.openrocket.util.MathUtil;

public class MassTest {
	
	private static final double EPSILON = MathUtil.EPSILON;

	@Test
	public void testAdd() {

		Mass x = new Mass(1,2,1,1);
		Mass y = new Mass(2,1,1,2);
		
		Mass sum = x.add(y);
		
		assertEquals( 3.0, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testAverage() {

		Mass x = new Mass(1,2,1,1);
		Mass y = new Mass(2,1,1,2);
		
		Mass sum = x.average(y);
		
		assertEquals( 1.5, sum.w, EPSILON);
		assertEquals( 1.66666667, sum.x, EPSILON);
		assertEquals( 1.33333333, sum.y, EPSILON);
		assertEquals( 1.0, sum.z, EPSILON);
	}

	@Test
	public void testSubtract() {
		Mass p1 = new Mass(1,2,1,1);
		Mass p2 = new Mass(2,1,1,2);
		
		final Mass sum = p1.add(p2);
		
		final Mass result = sum.subtract( p2); 
		
		assertEquals( p1.w, result.w, EPSILON);
		assertEquals( p1.x, result.x, EPSILON);
		assertEquals( p1.y, result.y, EPSILON);
		assertEquals( p1.z, result.z, EPSILON);
	}

	
}
