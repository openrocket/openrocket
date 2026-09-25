package info.openrocket.core.util;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MutableCoordinateTest {

	@Test
	void basicSetAndAddOperationsWork() {
		MutableCoordinate mutable = new MutableCoordinate().set(1, 2, 3, 0.5);
		mutable.add(1, -1, 0.5);

		assertEquals(2.0, mutable.getX(), 1e-9);
		assertEquals(1.0, mutable.getY(), 1e-9);
		assertEquals(3.5, mutable.getZ(), 1e-9);
		assertEquals(0.5, mutable.getWeight(), 1e-9);
	}

	@Test
	void addScaledMatchesExpectations() {
		MutableCoordinate mutable = new MutableCoordinate().set(0, 0, 0);
		CoordinateIF coord = new Coordinate(2, 4, -1, 0.25);

		mutable.addScaled(coord, 0.5).addScaled(coord, 1.5);

		assertEquals(4.0, mutable.getX(), 1e-9);
		assertEquals(8.0, mutable.getY(), 1e-9);
		assertEquals(-2.0, mutable.getZ(), 1e-9);
		assertEquals(0.5, mutable.getWeight(), 1e-9);
	}

	@Test
	void subtractCoordinateMatchesImmutableResultAndPreservesWeight() {
		CoordinateIF other = new Coordinate(1, 2, 3, 4);
		CoordinateIF expected = new Coordinate(5, 7, 9, 11).sub(other);
		MutableCoordinate mutable = new MutableCoordinate(5, 7, 9, 11);

		CoordinateIF actual = mutable.sub(other);

		assertSame(mutable, actual);
		assertEquals(expected.getX(), actual.getX(), 1e-12);
		assertEquals(expected.getY(), actual.getY(), 1e-12);
		assertEquals(expected.getZ(), actual.getZ(), 1e-12);
		assertEquals(expected.getWeight(), actual.getWeight(), 1e-12);
		assertEquals(11.0, actual.getWeight(), 1e-12);
	}

	@Test
	void toCoordinateCreatesIndependentImmutableInstance() {
		MutableCoordinate mutable = new MutableCoordinate().set(1, 1, 1);
		CoordinateIF first = mutable.toImmutable();

		mutable.add(1, 0, 0);
		CoordinateIF second = mutable.toImmutable();

		assertNotSame(first, second);
		assertEquals(1.0, first.getX(), 1e-9);
		assertEquals(2.0, second.getX(), 1e-9);
	}

	@Test
	void normalizeThrowsForZeroVector() {
		MutableCoordinate zero = new MutableCoordinate();
		assertThrows(IllegalStateException.class, zero::normalize);
	}

	@Test
	void normalizeScalesComponentsToUnitLength() {
		MutableCoordinate coord = new MutableCoordinate(3, 4, 0, 2.0);
		coord.normalize();

		assertEquals(0.6, coord.getX(), 1e-12);
		assertEquals(0.8, coord.getY(), 1e-12);
		assertEquals(0.0, coord.getZ(), 1e-12);
		assertEquals(2.0, coord.getWeight(), 1e-12);
	}

	@Test
	void crossProductMutatesReceiverAndClearsWeight() {
		MutableCoordinate a = new MutableCoordinate(1, 0, 0, 2);
		MutableCoordinate b = new MutableCoordinate(0, 1, 0, 3);

		CoordinateIF result = a.cross(b);

		assertSame(a, result);
		assertEquals(0.0, a.getX(), 1e-12);
		assertEquals(0.0, a.getY(), 1e-12);
		assertEquals(1.0, a.getZ(), 1e-12);
		assertEquals(0.0, a.getWeight(), 1e-12);
	}

	@Test
	void staticCrossProducesNewMutableCoordinate() {
		CoordinateIF result = MutableCoordinate.cross(new Coordinate(0, 1, 0), new Coordinate(1, 0, 0));
		assertEquals(0.0, result.getX(), 1e-12);
		assertEquals(0.0, result.getY(), 1e-12);
		assertEquals(-1.0, result.getZ(), 1e-12);
		assertEquals(0.0, result.getWeight(), 1e-12);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("coordinateOperations")
	void operationsMatchImmutableCoordinate(String name, boolean returnsReceiver, CoordinateOperation operation) {
		CoordinateIF immutable = new Coordinate(1, 2, 3, 4);
		MutableCoordinate mutable = new MutableCoordinate(1, 2, 3, 4);

		CoordinateIF expected = operation.apply(immutable);
		CoordinateIF actual = operation.apply(mutable);

		assertCoordinateEquals(expected, actual);
		if (returnsReceiver) {
			assertSame(mutable, actual, name + " should return the mutable receiver");
		} else {
			assertNotSame(mutable, actual, name + " should return an independent value");
		}
	}

	@Test
	void scalarOperationsMatchImmutableCoordinate() {
		CoordinateIF immutable = new Coordinate(1, -2, 3, 4);
		CoordinateIF mutable = new MutableCoordinate(1, -2, 3, 4);
		CoordinateIF other = new Coordinate(-2, 5, 7, 3);

		assertEquals(immutable.isWeighted(), mutable.isWeighted());
		assertEquals(immutable.isNaN(), mutable.isNaN());
		assertEquals(immutable.length(), mutable.length(), 1e-12);
		assertEquals(immutable.length2(), mutable.length2(), 1e-12);
		assertEquals(immutable.dot(other), mutable.dot(other), 1e-12);
		assertEquals(immutable.max(), mutable.max(), 1e-12);
		assertEquals(immutable.toPreciseString(), mutable.toPreciseString());
	}

	@Test
	void averageUsesWeightsWhenPresent() {
		MutableCoordinate coord = new MutableCoordinate(1, 1, 1, 2.0);
		CoordinateIF other = new Coordinate(3, 3, 3, 2.0);

		coord.average(other);

		assertEquals(2.0, coord.getX(), 1e-12);
		assertEquals(2.0, coord.getY(), 1e-12);
		assertEquals(2.0, coord.getZ(), 1e-12);
		assertEquals(4.0, coord.getWeight(), 1e-12);
	}

	@Test
	void averageFallsBackToUnweightedWhenWeightsCancelOut() {
		MutableCoordinate coord = new MutableCoordinate(4, 0, 0, 0.1);
		CoordinateIF other = new Coordinate(0, 4, 0, -0.1);

		coord.average(other);

		assertEquals(2.0, coord.getX(), 1e-12);
		assertEquals(2.0, coord.getY(), 1e-12);
		assertEquals(0.0, coord.getZ(), 1e-12);
		assertEquals(0.0, coord.getWeight(), 1e-12);
	}

	@Test
	void interpolateBlendsComponentsLinearly() {
		MutableCoordinate coord = new MutableCoordinate(0, 0, 0, 0);
		CoordinateIF target = new Coordinate(10, -10, 5, 2.0);

		coord.interpolate(target, 0.25);

		assertEquals(2.5, coord.getX(), 1e-12);
		assertEquals(-2.5, coord.getY(), 1e-12);
		assertEquals(1.25, coord.getZ(), 1e-12);
		assertEquals(0.5, coord.getWeight(), 1e-12);
	}

	@Test
	void equalsUsesToleranceComparison() {
		MutableCoordinate base = new MutableCoordinate(1, 2, 3, 4);
		MutableCoordinate near = new MutableCoordinate(1 + MathUtil.EPSILON / 4,
				2 - MathUtil.EPSILON / 4,
				3 + MathUtil.EPSILON / 4,
				4 - MathUtil.EPSILON / 4);

		assertTrue(base.equals(near));
		assertTrue(near.equals(base));

		MutableCoordinate far = new MutableCoordinate(1 + MathUtil.EPSILON * 10, 2, 3, 4);
		assertFalse(base.equals(far));
		assertFalse(base.equals("not a coordinate"));
	}

	@Test
	void hashCodeVariesWithComponents() {
		MutableCoordinate first = new MutableCoordinate(1, 2, 3, 4);
		MutableCoordinate second = new MutableCoordinate(2, 2, 3, 4);
		assertNotEquals(first.hashCode(), second.hashCode());
	}

	@Test
	void cloneProducesIndependentInstance() {
		MutableCoordinate coord = new MutableCoordinate(1, 2, 3, 4);
		CoordinateIF copy = coord.clone();

		assertNotSame(coord, copy);
		assertEquals(1.0, copy.getX(), 1e-12);
		assertEquals(2.0, copy.getY(), 1e-12);
		assertEquals(3.0, copy.getZ(), 1e-12);
		assertEquals(4.0, copy.getWeight(), 1e-12);
	}

	/**
	 * Supply every coordinate-returning interface operation with inputs that make
	 * weight-handling differences visible.
	 */
	private static Stream<Arguments> coordinateOperations() {
		CoordinateIF other = new Coordinate(-2, 5, 7, 3);
		return Stream.of(
				operation("setX", true, coordinate -> coordinate.setX(9)),
				operation("setY", true, coordinate -> coordinate.setY(9)),
				operation("setZ", true, coordinate -> coordinate.setZ(9)),
				operation("setWeight", true, coordinate -> coordinate.setWeight(9)),
				operation("setXYZ", true, coordinate -> coordinate.setXYZ(other)),
				operation("add coordinate", true, coordinate -> coordinate.add(other)),
				operation("add xyz", true, coordinate -> coordinate.add(5, 6, 7)),
				operation("add xyz and weight", true, coordinate -> coordinate.add(5, 6, 7, 8)),
				operation("addScaled", true, coordinate -> coordinate.addScaled(other, -0.25)),
				operation("subtract coordinate", true, coordinate -> coordinate.sub(other)),
				operation("subtract xyz", true, coordinate -> coordinate.sub(5, 6, 7)),
				operation("multiply scalar", true, coordinate -> coordinate.multiply(-2)),
				operation("multiply coordinate", true, coordinate -> coordinate.multiply(other)),
				operation("cross", true, coordinate -> coordinate.cross(other)),
				operation("normalize", true, CoordinateIF::normalize),
				operation("average", true, coordinate -> coordinate.average(other)),
				operation("average null", true, coordinate -> coordinate.average(null)),
				operation("interpolate", true, coordinate -> coordinate.interpolate(other, 0.25)),
				operation("toImmutable", false, CoordinateIF::toImmutable),
				operation("toMutable", false, CoordinateIF::toMutable),
				operation("clone", false, CoordinateIF::clone));
	}

	/** Wrap a named coordinate operation as parameterized-test arguments. */
	private static Arguments operation(String name, boolean returnsReceiver, CoordinateOperation operation) {
		return Arguments.of(name, returnsReceiver, operation);
	}

	/** Assert that two coordinate implementations contain the same numeric value. */
	private static void assertCoordinateEquals(CoordinateIF expected, CoordinateIF actual) {
		assertEquals(expected.getX(), actual.getX(), 1e-12);
		assertEquals(expected.getY(), actual.getY(), 1e-12);
		assertEquals(expected.getZ(), actual.getZ(), 1e-12);
		assertEquals(expected.getWeight(), actual.getWeight(), 1e-12);
	}

	/** Operation shared by the immutable and mutable differential tests. */
	@FunctionalInterface
	private interface CoordinateOperation {
		CoordinateIF apply(CoordinateIF coordinate);
	}
}
