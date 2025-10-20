package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

		Coordinate c = new ImmutableCoordinate(148578428.914, 8126778.954, -607.741);

		Coordinate r = q.rotate(c);

		// System.out.println("Rotated: " + q.rotate(c));

		assertEquals(-42312599.537, r.getX(), 0.001);
		assertEquals(-48162747.551, r.getY(), 0.001);
		assertEquals(134281904.197, r.getZ(), 0.001);

		c = new ImmutableCoordinate(0, 1, 0);
		Coordinate rot = new ImmutableCoordinate(Math.PI / 4, 0, 0);

		// System.out.println("Before: "+c);
		c = Quaternion.rotation(rot).invRotate(c);
		// System.out.println("After: "+c);

		assertEquals(0.0, c.getX(), 0.001);
		assertEquals(0.707, c.getY(), 0.001);
		assertEquals(-0.707, c.getZ(), 0.001);

	}

}
