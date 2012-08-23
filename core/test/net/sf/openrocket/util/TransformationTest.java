package net.sf.openrocket.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class TransformationTest {
	@Test
	public void oldMainTest() {
		Transformation t;

		t = new Transformation();
		{
			Coordinate a = t.transform( new Coordinate(1,0,0) );
			assertEquals( new Coordinate(1,0,0), a );
			a = t.transform( new Coordinate(0,1,0) );
			assertEquals( new Coordinate(0,1,0), a );
			a = t.transform( new Coordinate(0,0,1) );
			assertEquals( new Coordinate(0,0,1), a );
		}

		t = new Transformation(1,2,3);
		{
			Coordinate a = t.transform( new Coordinate(1,0,0) );
			assertEquals( new Coordinate(2,2,3), a );
			a = t.transform( new Coordinate(0,1,0) );
			assertEquals( new Coordinate(1,3,3), a );
			a = t.transform( new Coordinate(0,0,1) );
			assertEquals( new Coordinate(1,2,4), a );
		}
		
		
		t = new Transformation(new Coordinate(2,3,4));
		{
			Coordinate a = t.transform( new Coordinate(1,0,0) );
			assertEquals( new Coordinate(3,3,4), a );
			a = t.transform( new Coordinate(0,1,0) );
			assertEquals( new Coordinate(2,4,4), a );
			a = t.transform( new Coordinate(0,0,1) );
			assertEquals( new Coordinate(2,3,5), a );
		}

		t = Transformation.rotate_y(0.01);
		{
			Coordinate a = t.transform( new Coordinate(1,0,0) );
			// we need to test individual coordinates due to error.
			assertEquals( 1, a.x, .001);
			assertEquals( 0, a.y, .001);
			assertEquals( -.01, a.z, .001);
			a = t.transform( new Coordinate(0,1,0) );
			assertEquals( new Coordinate(0,1,0), a );
			a = t.transform( new Coordinate(0,0,1) );
			// we need to test individual coordinates due to error.
			assertEquals( .01, a.x, .001);
			assertEquals( 0, a.y, .001);
			assertEquals( 1, a.z, .001);
		}

		t = new Transformation(-1,0,0);
		t = t.applyTransformation(Transformation.rotate_y(0.01));
		t = t.applyTransformation(new Transformation(1,0,0));
		{
			Coordinate a = t.transform( new Coordinate(1,0,0) );
			// we need to test individual coordinates due to error.
			assertEquals( 1, a.x, .001);
			assertEquals( 0, a.y, .001);
			assertEquals( -.02, a.z, .001);
			a = t.transform( new Coordinate(0,1,0) );
			assertEquals( 0, a.x, .001);
			assertEquals( 1, a.y, .001);
			assertEquals( -.01, a.z, .001);
			a = t.transform( new Coordinate(0,0,1) );
			// we need to test individual coordinates due to error.
			assertEquals( .01, a.x, .001);
			assertEquals( 0, a.y, .001);
			assertEquals( .99, a.z, .001);
		}
	}


}
