package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Rotation2DTest {

	@Test
	public void rotationTest() {

		double rot60 = 0.5;
		double rot30 = Math.sqrt(3) / 2;

		Coordinate x = new ImmutableCoordinate(1, 1, 0);
		Coordinate y = new ImmutableCoordinate(0, 1, 1);

		Rotation2D rot = new Rotation2D(Math.PI / 3); // 60 deg

		assertEquals(new ImmutableCoordinate(rot60, 1, -rot30), rot.rotateY(x));
		assertEquals(new ImmutableCoordinate(rot60, 1, rot30), rot.invRotateY(x));

		assertEquals(new ImmutableCoordinate(1, rot60, rot30), rot.rotateX(x));
		assertEquals(new ImmutableCoordinate(1, rot60, -rot30), rot.invRotateX(x));

		assertEquals(new ImmutableCoordinate(-rot30, rot60, 1), rot.rotateZ(y));
		assertEquals(new ImmutableCoordinate(rot30, rot60, 1), rot.invRotateZ(y));

	}

}
