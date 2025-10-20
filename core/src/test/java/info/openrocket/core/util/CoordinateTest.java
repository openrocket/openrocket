package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CoordinateTest {

	private static final double EPS = 0.0000000001;

	@Test
	public void testConstructors() {
		Coordinate c1 = new ImmutableCoordinate();
		assertCoordinateEquals(new ImmutableCoordinate(0, 0, 0, 0), c1);

		Coordinate c2 = new ImmutableCoordinate(1);
		assertCoordinateEquals(new ImmutableCoordinate(1, 0, 0, 0), c2);

		Coordinate c3 = new ImmutableCoordinate(1, 2);
		assertCoordinateEquals(new ImmutableCoordinate(1, 2, 0, 0), c3);

		Coordinate c4 = new ImmutableCoordinate(1, 2, 3);
		assertCoordinateEquals(new ImmutableCoordinate(1, 2, 3, 0), c4);

		Coordinate c5 = new ImmutableCoordinate(1, 2, 3, 4);
		assertCoordinateEquals(new ImmutableCoordinate(1, 2, 3, 4), c5);
	}

	@Test
	public void testSetters() {
		Coordinate x = new ImmutableCoordinate(1, 1, 1, 1);

		assertCoordinateEquals(new ImmutableCoordinate(2, 1, 1, 1), x.setX(2));
		assertCoordinateEquals(new ImmutableCoordinate(1, 2, 1, 1), x.setY(2));
		assertCoordinateEquals(new ImmutableCoordinate(1, 1, 2, 1), x.setZ(2));
		assertCoordinateEquals(new ImmutableCoordinate(1, 1, 1, 2), x.setWeight(2));

		Coordinate y = new ImmutableCoordinate(1, 2, 3, 4);
		assertCoordinateEquals(new ImmutableCoordinate(1, 2, 3, 1), x.setXYZ(y));
	}

	@Test
	public void testIsWeighted() {
		assertTrue(new ImmutableCoordinate(1, 1, 1, 1).isWeighted());
		assertFalse(new ImmutableCoordinate(1, 1, 1, 0).isWeighted());
	}

	@Test
	public void testIsNaN() {
		assertFalse(new ImmutableCoordinate(1, 1, 1, 1).isNaN());
		assertTrue(new ImmutableCoordinate(Double.NaN, 1, 1, 1).isNaN());
		assertTrue(new ImmutableCoordinate(1, Double.NaN, 1, 1).isNaN());
		assertTrue(new ImmutableCoordinate(1, 1, Double.NaN, 1).isNaN());
		assertTrue(new ImmutableCoordinate(1, 1, 1, Double.NaN).isNaN());
		assertTrue(ImmutableCoordinate.NaN.isNaN());
	}

	@Test
	public void testAdd() {
		Coordinate x = new ImmutableCoordinate(1, 1, 1, 1);
		Coordinate y = new ImmutableCoordinate(1, 2, 3, 4);

		assertCoordinateEquals(new ImmutableCoordinate(2, 3, 4, 5), x.add(y));
		assertCoordinateEquals(new ImmutableCoordinate(2, 3, 4, 1), x.add(1, 2, 3));
		assertCoordinateEquals(new ImmutableCoordinate(2, 3, 4, 5), x.add(1, 2, 3, 4));
	}

	@Test
	public void testSub() {
		Coordinate x = new ImmutableCoordinate(1, 1, 1, 1);
		Coordinate y = new ImmutableCoordinate(1, 2, 3, 4);

		assertCoordinateEquals(new ImmutableCoordinate(0, -1, -2, 1), x.sub(y));
		assertCoordinateEquals(new ImmutableCoordinate(0, -1, -2, 1), x.sub(1, 2, 3));
	}

	@Test
	public void testMultiply() {
		Coordinate x = new ImmutableCoordinate(1, 2, 3, 4);

		assertCoordinateEquals(new ImmutableCoordinate(2, 4, 6, 8), x.multiply(2));
		assertCoordinateEquals(new ImmutableCoordinate(1, 4, 9, 16), x.multiply(x));
	}

	@Test
	public void testDot() {
		Coordinate x = new ImmutableCoordinate(1, 1, 1, 1);
		Coordinate y = new ImmutableCoordinate(1, 2, 3, 4);

		assertEquals(6, x.dot(y), EPS);
		assertEquals(6, y.dot(x), EPS);
		assertEquals(6, Coordinate.dot(x, y), EPS);
	}

	@Test
	public void testLength() {
		Coordinate x = new ImmutableCoordinate(3, 4, 0, 1);

		assertEquals(5, x.length(), EPS);
		assertEquals(25, x.length2(), EPS);
	}

	@Test
	public void testMax() {
		assertEquals(3, new ImmutableCoordinate(1, -2, 3, 4).max(), EPS);
	}

	@Test
	public void testNormalize() {
		Coordinate x = new ImmutableCoordinate(3, 4, 0, 2);
		Coordinate normalized = x.normalize();

		assertEquals(1, normalized.length(), EPS);
		assertEquals(2, normalized.getWeight(), EPS);
	}

	@Test
	public void testCross() {
		Coordinate x = new ImmutableCoordinate(1, 0, 0);
		Coordinate y = new ImmutableCoordinate(0, 1, 0);

		assertCoordinateEquals(new ImmutableCoordinate(0, 0, 1), x.cross(y));
		assertCoordinateEquals(new ImmutableCoordinate(0, 0, 1), ImmutableCoordinate.cross(x, y));
	}

	@Test
	public void testAverage() {
		Coordinate x = new ImmutableCoordinate(1, 2, 4, 1);
		Coordinate y = new ImmutableCoordinate(3, 5, 9, 1);

		assertCoordinateEquals(new ImmutableCoordinate(2, 3.5, 6.5, 2), x.average(y));

		y = new ImmutableCoordinate(3, 5, 9, 3);

		assertCoordinateEquals(new ImmutableCoordinate(2.5, 4.25, 7.75, 4), x.average(y));
	}

	@Test
	public void testInterpolate() {
		Coordinate x = new ImmutableCoordinate(0, 0, 0, 0);
		Coordinate y = new ImmutableCoordinate(10, 10, 10, 10);

		assertCoordinateEquals(new ImmutableCoordinate(5, 5, 5, 5), x.interpolate(y, 0.5));
	}

	private void assertCoordinateEquals(Coordinate expected, Coordinate actual) {
		assertEquals(expected.getX(), actual.getX(), EPS);
		assertEquals(expected.getY(), actual.getY(), EPS);
		assertEquals(expected.getZ(), actual.getZ(), EPS);
		assertEquals(expected.getWeight(), actual.getWeight(), EPS);
	}
}
