package net.sf.openrocket.util;

import static java.lang.Double.NaN;
import static java.lang.Math.PI;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MathUtilTest {
	
	public static final double EPS = 0.00000000001;
	
	/*
	@Test 
	public void rangeTest() {
		double[] a;
		
		a = MathUtil.range(0, 10, 2);
		assertEquals(0, a[0], 0);
		assertEquals(10, a[5], 0);
		assertEquals(6, a.length, 0);
		
		a = MathUtil.range(1, 2, 2);
		assertEquals(1, a[0], 0);
		assertEquals(1, a.length, 0);
		
	}
	*/
	
	@Test
	public void miscMathTest() {
		
		assertEquals(PI * PI, MathUtil.pow2(PI), EPS);
		assertEquals(PI * PI * PI, MathUtil.pow3(PI), EPS);
		assertEquals(PI * PI * PI * PI, MathUtil.pow4(PI), EPS);
		
		assertEquals(1.0, MathUtil.clamp(0.9999, 1.0, 2.0), 0);
		assertEquals(1.23, MathUtil.clamp(1.23, 1.0, 2.0), 0);
		assertEquals(2.0, MathUtil.clamp(2 + EPS / 100, 1.0, 2.0), 0);
		
		assertEquals(1.0f, MathUtil.clamp(0.9999f, 1.0f, 2.0f), 0);
		assertEquals(1.23f, MathUtil.clamp(1.23f, 1.0f, 2.0f), 0);
		assertEquals(2.0f, MathUtil.clamp(2.0001f, 1.0f, 2.0f), 0);
		
		assertEquals(1, MathUtil.clamp(-3, 1, 5));
		assertEquals(3, MathUtil.clamp(3, 1, 5));
		assertEquals(5, MathUtil.clamp(6, 1, 5));
		
		assertEquals(-1.0, MathUtil.sign(Double.NEGATIVE_INFINITY), EPS);
		assertEquals(-1.0, MathUtil.sign(-100), EPS);
		assertEquals(-1.0, MathUtil.sign(Math.nextAfter(0.0, -1.0)), EPS);
		assertEquals(1.0, MathUtil.sign(Math.nextUp(0.0)), EPS);
		assertEquals(1.0, MathUtil.sign(100), EPS);
		assertEquals(1.0, MathUtil.sign(Double.POSITIVE_INFINITY), EPS);
	}
	
	@Test
	public void hypotTest() {
		
		for (int i = 0; i < 10000; i++) {
			double x = Math.random() * 100 - 50;
			double y = Math.random() * i - i / 2;
			double z = Math.hypot(x, y);
			assertEquals(z, MathUtil.hypot(x, y), EPS);
		}
		
	}
	
	@Test
	public void reduceTest() {
		
		for (int i = -1000; i < 1000; i++) {
			double angle = Math.random() * 2 * PI;
			double shift = angle + i * 2 * PI;
			assertEquals(angle, MathUtil.reduce360(shift), EPS);
		}
		
		for (int i = -1000; i < 1000; i++) {
			double angle = Math.random() * 2 * PI - PI;
			double shift = angle + i * 2 * PI;
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
		
		assertEquals(1.0, MathUtil.min(1.0, 2.0, 3.0, 4.0), 0);
		assertEquals(1.0, MathUtil.min(1.0, NaN, NaN, NaN), 0);
		assertEquals(1.0, MathUtil.min(NaN, 1.0, NaN, NaN), 0);
		assertEquals(1.0, MathUtil.min(NaN, NaN, 1.0, NaN), 0);
		assertEquals(1.0, MathUtil.min(2.0, NaN, 1.0, NaN), 0);
		assertEquals(1.0, MathUtil.min(2.0, NaN, NaN, 1.0), 0);
		assertEquals(1.0, MathUtil.min(1.0, 2.0, NaN, 3.0), 0);
		assertEquals(1.0, MathUtil.min(NaN, 2.0, 3.0, 1.0), 0);
		
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
		} catch (IllegalArgumentException normal) {
		}
		
		assertEquals(7.0, MathUtil.map(Math.nextUp(1.0), 0.0, 5.0, 9.0, -1.0), EPS);
	}
	
	
	@Test
	public void mapCoordinateTest() {
		assertEquals(new Coordinate(0.8, 2.0, 1.6, 4.0),
				MathUtil.map(1.0, 0.0, 5.0, new Coordinate(0, 1, 2, 3), new Coordinate(4, 6, 0, 8)));
	}
	
	
	@Test
	public void equalsTest() {
		assertTrue(MathUtil.equals(1.0, 1.0 + MathUtil.EPSILON / 3));
		assertFalse(MathUtil.equals(1.0, 1.0 + MathUtil.EPSILON * 2));
		assertTrue(MathUtil.equals(-1.0, -1.0 + MathUtil.EPSILON / 3));
		assertFalse(MathUtil.equals(-1.0, -1.0 + MathUtil.EPSILON * 2));
		
		for (double zero : new double[] { 0.0, MathUtil.EPSILON / 10, -MathUtil.EPSILON / 10 }) {
			
			assertTrue(MathUtil.equals(zero, MathUtil.EPSILON / 3));
			assertTrue(MathUtil.equals(zero, -MathUtil.EPSILON / 3));
			assertFalse(MathUtil.equals(zero, MathUtil.EPSILON * 2));
			assertFalse(MathUtil.equals(zero, -MathUtil.EPSILON * 2));
			
			assertTrue(MathUtil.equals(MathUtil.EPSILON / 3, zero));
			assertTrue(MathUtil.equals(-MathUtil.EPSILON / 3, zero));
			assertFalse(MathUtil.equals(MathUtil.EPSILON * 2, zero));
			assertFalse(MathUtil.equals(-MathUtil.EPSILON * 2, zero));
			
		}
		
		for (double value : new double[] { PI * 1e20, -PI * 1e20 }) {
			assertTrue("value=" + value, MathUtil.equals(value, value + 1));
			assertTrue("value=" + value, MathUtil.equals(value, Math.nextUp(value)));
			assertTrue("value=" + value, MathUtil.equals(value, value * (1 + MathUtil.EPSILON)));
		}
		
		assertFalse(MathUtil.equals(NaN, 0.0));
		assertFalse(MathUtil.equals(0.0, NaN));
		assertFalse(MathUtil.equals(NaN, NaN));
	}
	
	@Test
	public void testAverageStddev() {
		List<Integer> ints = new ArrayList<Integer>();
		List<Double> doubles = new ArrayList<Double>();
		
		ints.add(3);
		ints.add(4);
		ints.add(7);
		ints.add(5);
		
		doubles.add(3.4);
		doubles.add(2.9);
		doubles.add(7.5);
		doubles.add(5.43);
		doubles.add(2.8);
		doubles.add(6.6);
		
		assertEquals(4.75, MathUtil.average(ints), EPS);
		assertEquals(1.707825127659933, MathUtil.stddev(ints), EPS);
		assertEquals(4.771666666666667, MathUtil.average(doubles), EPS);
		assertEquals(2.024454659078999, MathUtil.stddev(doubles), EPS);
	}
	
	@Test
	public void testMedian() {
		List<Integer> ints = new ArrayList<Integer>();
		List<Double> doubles = new ArrayList<Double>();
		
		ints.add(3);
		ints.add(4);
		ints.add(7);
		ints.add(5);
		
		doubles.add(3.4);
		doubles.add(2.9);
		doubles.add(7.5);
		doubles.add(5.43);
		doubles.add(2.8);
		doubles.add(6.6);
		
		assertEquals(4.5, MathUtil.median(ints), EPS);
		assertEquals(4.415, MathUtil.median(doubles), EPS);
		
		ints.add(9);
		doubles.add(10.0);
		
		assertEquals(5, MathUtil.median(ints), EPS);
		assertEquals(5.43, MathUtil.median(doubles), EPS);
	}
	
	@Test
	public void testInterpolate() {
		double v;
		List<Double> x;
		List<Double> y;

		x = new ArrayList<Double>();
		y = new ArrayList<Double>();
		y.add(1.0);
		
		v= MathUtil.interpolate(null, y, 0.0);
		assertEquals("Failed to test for domain null", Double.NaN, v, EPS);
		
		v = MathUtil.interpolate(x, y, 0.0);
		assertEquals("Failed to test for empty domain", Double.NaN, v, EPS);
		
		x = new ArrayList<Double>();
		x.add(1.0);
		y = new ArrayList<Double>();
		
		v = MathUtil.interpolate(x, null, 0.0);
		assertEquals("Failed to test for range null", Double.NaN, v, EPS);
		
		v = MathUtil.interpolate(x, y, 0.0);
		assertEquals("Failed to test for empty range", Double.NaN, v, EPS);
		
		x = new ArrayList<Double>();
		x.add(1.0);
		x.add(2.0);
		y = new ArrayList<Double>();
		y.add(15.0);
		y.add(17.0);
		
		v = MathUtil.interpolate(x,y,0.0);
		assertEquals("Failed to test t out of domain", Double.NaN, v, EPS);
		
		v = MathUtil.interpolate(x,y,5.0);
		assertEquals("Failed to test t out of domain", Double.NaN, v, EPS);
		
		v = MathUtil.interpolate(x,y,1.0);
		assertEquals("Failed to calculate left endpoint", 15.0, v, EPS);
		v = MathUtil.interpolate(x,y,2.0);
		assertEquals("Failed to calculate right endpoint", 17.0, v, EPS);
		v = MathUtil.interpolate(x,y,1.5);
		assertEquals("Failed to calculate center", 16.0, v, EPS);
		
		x = new ArrayList<Double>();
		x.add(0.25);
		x.add(0.5);
		x.add(1.0);
		x.add(2.0);
		y = new ArrayList<Double>();
		y.add(0.0);
		y.add(0.0);
		y.add(15.0);
		y.add(17.0);
		v = MathUtil.interpolate(x,y,1.5);
		assertEquals("Failed to calculate center with longer list", 16.0, v, EPS);
		
	}
}
