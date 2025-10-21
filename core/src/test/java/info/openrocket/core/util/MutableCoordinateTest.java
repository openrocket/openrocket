package info.openrocket.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

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
	void toCoordinateCreatesIndependentImmutableInstance() {
		MutableCoordinate mutable = new MutableCoordinate().set(1, 1, 1);
		CoordinateIF first = mutable.toImmutable();

		mutable.add(1, 0, 0);
		CoordinateIF second = mutable.toImmutable();

		assertNotSame(first, second);
		assertEquals(1.0, first.getX(), 1e-9);
		assertEquals(2.0, second.getX(), 1e-9);
	}
}
