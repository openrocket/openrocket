package net.sf.openrocket.util;

import static java.lang.Double.NaN;
import static java.lang.Math.PI;
import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilTest {
	
	public static final double EPS = 0.00000000001;

	@Test
	public void miscMathTest() {
		
		assertEquals(PI*PI, MathUtil.pow2(PI), EPS);
		assertEquals(PI*PI*PI, MathUtil.pow3(PI), EPS);
		assertEquals(PI*PI*PI*PI, MathUtil.pow4(PI), EPS);
		
		assertEquals(1.0, MathUtil.clamp(0.9999, 1.0, 2.0), 0);
		assertEquals(1.23, MathUtil.clamp(1.23, 1.0, 2.0), 0);
		assertEquals(2.0, MathUtil.clamp(2 + EPS/100, 1.0, 2.0), 0);
		
		assertEquals(1.0f, MathUtil.clamp(0.9999f, 1.0f, 2.0f), 0);
		assertEquals(1.23f, MathUtil.clamp(1.23f, 1.0f, 2.0f), 0);
		assertEquals(2.0f, MathUtil.clamp(2.0001f, 1.0f, 2.0f), 0);
		
		assertEquals(1, MathUtil.clamp(-3, 1, 5));
		assertEquals(3, MathUtil.clamp(3, 1, 5));
		assertEquals(5, MathUtil.clamp(6, 1, 5));
		
		assertEquals(-1.0, MathUtil.sign(Double.NEGATIVE_INFINITY), EPS);
		assertEquals(-1.0, MathUtil.sign(-100), EPS);
		assertEquals(-1.0, MathUtil.sign(Math.nextAfter(0.0, -1.0)), EPS);
		assertEquals( 1.0, MathUtil.sign(Math.nextUp(0.0)), EPS);
		assertEquals( 1.0, MathUtil.sign(100), EPS);
		assertEquals( 1.0, MathUtil.sign(Double.POSITIVE_INFINITY), EPS);
	}
	
	@Test
	public void hypotTest() {
		
		for (int i=0; i<10000; i++) {
			double x = Math.random()*100 - 50;
			double y = Math.random()*i - i/2;
			double z = Math.hypot(x, y);
			assertEquals(z, MathUtil.hypot(x, y), EPS);
		}
		
	}
	
	@Test
	public void reduceTest() {
		
		for (int i=-1000; i<1000; i++) {
			double angle = Math.random() * 2*PI;
			double shift = angle + i*2*PI;
			assertEquals(angle, MathUtil.reduce360(shift), EPS);
		}
		
		for (int i=-1000; i<1000; i++) {
			double angle = Math.random() * 2*PI - PI;
			double shift = angle + i*2*PI;
			assertEquals(angle, MathUtil.reduce180(shift), EPS);
		}
		
	}
	
	@Test
	public void minmaxTest() {
		assertEquals(1.0, MathUtil.min(1.0, Math.nextUp(1.0)), 0);
		assertEquals(1.0, MathUtil.min(1.0, Double.POSITIVE_INFINITY), 0);
		assertEquals(1.0, MathUtil.min(NaN, 1.0), 0);
		assertEquals(1.0, MathUtil.min(1.0, NaN), 0);
		assertEquals(NaN, MathUtil.min(NaN, NaN), 0);

		assertEquals(Math.nextUp(1.0), MathUtil.max(1.0, Math.nextUp(1.0)), 0);
		assertEquals(Double.POSITIVE_INFINITY, MathUtil.max(1.0, Double.POSITIVE_INFINITY), 0);
		assertEquals(1.0, MathUtil.max(NaN, 1.0), 0);
		assertEquals(1.0, MathUtil.max(1.0, NaN), 0);
		assertEquals(NaN, MathUtil.max(NaN, NaN), 0);
		
		assertEquals(1.0, MathUtil.min(1.0, 2.0, 3.0), 0);
		assertEquals(1.0, MathUtil.min(1.0, NaN, NaN), 0);
		assertEquals(1.0, MathUtil.min(NaN, 1.0, NaN), 0);
		assertEquals(1.0, MathUtil.min(NaN, NaN, 1.0), 0);
		assertEquals(1.0, MathUtil.min(2.0, NaN, 1.0), 0);
		assertEquals(1.0, MathUtil.min(1.0, 2.0, NaN), 0);
		assertEquals(1.0, MathUtil.min(NaN, 2.0, 1.0), 0);
		
		assertEquals(3.0, MathUtil.max(1.0, 3.0, 2.0), 0);
		assertEquals(1.0, MathUtil.max(1.0, NaN, NaN), 0);
		assertEquals(1.0, MathUtil.max(NaN, 1.0, NaN), 0);
		assertEquals(1.0, MathUtil.max(NaN, NaN, 1.0), 0);
		assertEquals(2.0, MathUtil.max(2.0, NaN, 1.0), 0);
		assertEquals(2.0, MathUtil.max(1.0, 2.0, NaN), 0);
		assertEquals(2.0, MathUtil.max(NaN, 2.0, 1.0), 0);
	}
	
	@Test
	public void mapTest() {
		assertEquals(1.0, MathUtil.map(1.0, 0.0, 5.0, -1.0, 9.0), EPS);
		assertEquals(7.0, MathUtil.map(1.0, 5.0, 0.0, -1.0, 9.0), EPS);
		assertEquals(7.0, MathUtil.map(1.0, 0.0, 5.0, 9.0, -1.0), EPS);
		assertEquals(6.0, MathUtil.map(6.0, 0.0, 5.0, Math.nextUp(6.0), 6.0), EPS);
		assertEquals(6.0, MathUtil.map(6.0, 0.0, 0.0, Math.nextUp(6.0), 6.0), EPS);
		try {
			MathUtil.map(6.0, 1.0, Math.nextUp(1.0), 1.0, 2.0);
			fail("Should not be reached.");
		} catch (IllegalArgumentException normal) { }

		assertEquals(7.0, MathUtil.map(Math.nextUp(1.0), 0.0, 5.0, 9.0, -1.0), EPS);
	}
	
	
	@Test
	public void equalsTest() {
		assertTrue(MathUtil.equals(1.0, 1.0 + MathUtil.EPSILON/3));
		assertFalse(MathUtil.equals(1.0, 1.0 + MathUtil.EPSILON*2));
		assertTrue(MathUtil.equals(-1.0, -1.0 + MathUtil.EPSILON/3));
		assertFalse(MathUtil.equals(-1.0, -1.0 + MathUtil.EPSILON*2));
		
		for (double zero: new double[] { 0.0, MathUtil.EPSILON/10, -MathUtil.EPSILON/10 }) {

			assertTrue(MathUtil.equals(zero, MathUtil.EPSILON/3));
			assertTrue(MathUtil.equals(zero, -MathUtil.EPSILON/3));
			assertFalse(MathUtil.equals(zero, MathUtil.EPSILON*2));
			assertFalse(MathUtil.equals(zero, -MathUtil.EPSILON*2));

			assertTrue(MathUtil.equals(MathUtil.EPSILON/3, zero));
			assertTrue(MathUtil.equals(-MathUtil.EPSILON/3, zero));
			assertFalse(MathUtil.equals(MathUtil.EPSILON*2, zero));
			assertFalse(MathUtil.equals(-MathUtil.EPSILON*2, zero));

		}
		
		for (double value: new double[] { PI*1e20, -PI*1e20 }) {
			assertTrue("value=" + value, MathUtil.equals(value, value + 1));
			assertTrue("value=" + value, MathUtil.equals(value, Math.nextUp(value)));
			assertTrue("value=" + value, MathUtil.equals(value, value * (1+MathUtil.EPSILON)));
		}
		
		assertFalse(MathUtil.equals(NaN, 0.0));
		assertFalse(MathUtil.equals(0.0, NaN));
		assertFalse(MathUtil.equals(NaN, NaN));
	}
	
}
