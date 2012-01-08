package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Rotation2DTest {
	
	@Test
	public void rotationTest() {
		
		double rot60 = 0.5;
		double rot30 = Math.sqrt(3)/2;
		
		Coordinate x = new Coordinate(1,1,0);
		Coordinate y = new Coordinate(0,1,1);
		
		Rotation2D rot = new Rotation2D(Math.PI/3);  // 60 deg
		
		assertEquals(new Coordinate(rot60, 1, -rot30), rot.rotateY(x));
		assertEquals(new Coordinate(rot60, 1, rot30), rot.invRotateY(x));
		
		assertEquals(new Coordinate(1, rot60, rot30), rot.rotateX(x));
		assertEquals(new Coordinate(1, rot60, -rot30), rot.invRotateX(x));
		
		assertEquals(new Coordinate(-rot30, rot60, 1), rot.rotateZ(y));
		assertEquals(new Coordinate(rot30, rot60, 1), rot.invRotateZ(y));
		
	}

}
