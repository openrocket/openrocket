package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


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
		
		Coordinate c = new Coordinate(148578428.914, 8126778.954, -607.741);
		
		Coordinate r = q.rotate(c);
		
		//System.out.println("Rotated: " + q.rotate(c));
		
		assertEquals(-42312599.537, r.x, 0.001);
		assertEquals(-48162747.551, r.y, 0.001);
		assertEquals(134281904.197, r.z, 0.001);
		
		c = new Coordinate(0, 1, 0);
		Coordinate rot = new Coordinate(Math.PI / 4, 0, 0);
		
		//System.out.println("Before: "+c);
		c = Quaternion.rotation(rot).invRotate(c);
		//System.out.println("After: "+c);
		
		assertEquals(0.0, c.x, 0.001);
		assertEquals(0.707, c.y, 0.001);
		assertEquals(-0.707, c.z, 0.001);
		
		
	}
	
	
}
