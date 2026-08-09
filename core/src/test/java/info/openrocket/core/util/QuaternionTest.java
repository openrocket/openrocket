package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class QuaternionTest {

	@Test
	public void oldMainTest() {

		// This is normalized already
		Quaternion q = new Quaternion(0.237188, 0.570190, -0.514542, 0.594872);
		assertEquals(1.0, q.norm(), 0.01);

		q.normalize();
		assertEquals(0.237188, q.getW(), 0.00001);
		assertEquals(0.570190, q.getX(), 0.00001);
		assertEquals(-0.514542, q.getY(), 0.00001);
		assertEquals(0.594872, q.getZ(), 0.00001);
		assertEquals(1.0, q.norm(), 0.01);

		CoordinateIF c = new Coordinate(148578428.914, 8126778.954, -607.741);

		CoordinateIF r = q.rotate(c);

		assertEquals(-42312599.537, r.getX(), 0.001);
		assertEquals(-48162747.551, r.getY(), 0.001);
		assertEquals(134281904.197, r.getZ(), 0.001);

		c = new Coordinate(0, 1, 0);
		CoordinateIF rot = new Coordinate(Math.PI / 4, 0, 0);

		c = Quaternion.rotation(rot).invRotate(c);

		assertEquals(0.0, c.getX(), 0.001);
		assertEquals(0.707, c.getY(), 0.001);
		assertEquals(-0.707, c.getZ(), 0.001);
	}

	@Test
	public void rotationAboutAxisMatchesRotationVector() {
		// A quarter turn about z takes the x axis onto the y axis
		Quaternion q = Quaternion.rotation(Coordinate.Z_UNIT, Math.PI / 2);
		CoordinateIF c = q.rotate(Coordinate.X_UNIT);

		assertEquals(0.0, c.getX(), 1.0e-12);
		assertEquals(1.0, c.getY(), 1.0e-12);
		assertEquals(0.0, c.getZ(), 1.0e-12);

		// and must agree with the rotation vector form of the same rotation
		Quaternion v = Quaternion.rotation(new Coordinate(0, 0, Math.PI / 2));
		assertEquals(v.getW(), q.getW(), 1.0e-12);
		assertEquals(v.getX(), q.getX(), 1.0e-12);
		assertEquals(v.getY(), q.getY(), 1.0e-12);
		assertEquals(v.getZ(), q.getZ(), 1.0e-12);
	}

	@Test
	public void rotationVectorZeroLengthReturnsIdentityQuaternion() {
		Quaternion q = Quaternion.rotation(new Coordinate(0, 0, 0));
		assertEquals(1.0, q.getW(), 1.0e-12);
		assertEquals(0.0, q.getX(), 1.0e-12);
		assertEquals(0.0, q.getY(), 1.0e-12);
		assertEquals(0.0, q.getZ(), 1.0e-12);
	}

	@Test
	public void multiplyWithIdentityReturnsOriginalQuaternion() {
		Quaternion q = new Quaternion(0.5, -0.5, 0.25, 0.75);
		Quaternion identity = new Quaternion();

		Quaternion right = q.multiplyRight(identity);
		Quaternion left = q.multiplyLeft(identity);

		assertEquals(q.getW(), right.getW(), 1.0e-12);
		assertEquals(q.getX(), right.getX(), 1.0e-12);
		assertEquals(q.getY(), right.getY(), 1.0e-12);
		assertEquals(q.getZ(), right.getZ(), 1.0e-12);

		assertEquals(q.getW(), left.getW(), 1.0e-12);
		assertEquals(q.getX(), left.getX(), 1.0e-12);
		assertEquals(q.getY(), left.getY(), 1.0e-12);
		assertEquals(q.getZ(), left.getZ(), 1.0e-12);
	}

	@Test
	public void rotateAndInverseRotateReturnOriginalCoordinate() {
		Quaternion rotation = Quaternion.rotation(new Coordinate(0, 0, Math.PI / 2));
		CoordinateIF original = new Coordinate(1, 0, 0);

		CoordinateIF rotated = rotation.rotate(original);
		CoordinateIF restored = rotation.invRotate(rotated);

		assertEquals(original.getX(), restored.getX(), 1.0e-12);
		assertEquals(original.getY(), restored.getY(), 1.0e-12);
		assertEquals(original.getZ(), restored.getZ(), 1.0e-12);
	}

	@Test
	public void rotateInPlaceMatchesImmutableRotation() {
		Quaternion rotation = Quaternion.rotation(new Coordinate(Math.PI / 3, 0, 0));
		MutableCoordinate mutable = new MutableCoordinate(0, 1, 0);

		CoordinateIF rotated = rotation.rotate(mutable.toImmutable());
		rotation.rotateInPlace(mutable);

		assertEquals(rotated.getX(), mutable.getX(), 1.0e-12);
		assertEquals(rotated.getY(), mutable.getY(), 1.0e-12);
		assertEquals(rotated.getZ(), mutable.getZ(), 1.0e-12);
	}

	@Test
	public void inverseRotateInPlaceRestoresCoordinate() {
		Quaternion rotation = Quaternion.rotation(new Coordinate(0, Math.PI / 6, 0));
		MutableCoordinate mutable = new MutableCoordinate(0, 0, 1);

		rotation.rotateInPlace(mutable);
		rotation.invRotateInPlace(mutable);

		assertEquals(0.0, mutable.getX(), 1.0e-12);
		assertEquals(0.0, mutable.getY(), 1.0e-12);
		assertEquals(1.0, mutable.getZ(), 1.0e-12);
	}

	@Test
	public void normalizeThrowsForZeroQuaternion() {
		Quaternion zero = new Quaternion(0, 0, 0, 0);
		assertThrows(IllegalStateException.class, zero::normalize);
	}

	@Test
	public void normalizeIfNecessaryReturnsSameInstanceWhenAlreadyUnit() {
		double component = 1.0 / Math.sqrt(2);
		Quaternion unit = new Quaternion(component, component, 0, 0);

		Quaternion normalized = unit.normalizeIfNecessary();
		assertSame(unit, normalized);
	}

	@Test
	public void normalizeIfNecessaryNormalizesWhenLengthDeviates() {
		Quaternion scaled = new Quaternion(2, 0, 0, 0);
		Quaternion normalized = scaled.normalizeIfNecessary();

		assertNotSame(scaled, normalized);
		assertEquals(1.0, normalized.norm(), 1.0e-12);
		assertEquals(1.0, normalized.getW(), 1.0e-12);
	}

	@Test
	public void cloneReturnsIndependentInstance() {
		Quaternion q = new Quaternion(0.1, 0.2, 0.3, 0.4);
		Quaternion copy = q.clone();

		assertNotSame(q, copy);
		assertEquals(q.getW(), copy.getW(), 0.0);
		assertEquals(q.getX(), copy.getX(), 0.0);
		assertEquals(q.getY(), copy.getY(), 0.0);
		assertEquals(q.getZ(), copy.getZ(), 0.0);
	}

	@Test
	public void isNaNDetectsAnyNaNComponent() {
		assertTrue(new Quaternion(Double.NaN, 0, 0, 0).isNaN());
		assertTrue(new Quaternion(0, Double.NaN, 0, 0).isNaN());
		assertTrue(new Quaternion(0, 0, Double.NaN, 0).isNaN());
		assertTrue(new Quaternion(0, 0, 0, Double.NaN).isNaN());
		assertFalse(new Quaternion(1, 0, 0, 0).isNaN());
	}
}
