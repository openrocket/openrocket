package net.sf.openrocket.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.DoubleBuffer;

public class TestTransformation {
	static final Coordinate x_unit = Coordinate.X_UNIT;
	static final Coordinate y_unit = Coordinate.Y_UNIT;
	static final Coordinate z_unit = Coordinate.Z_UNIT;
	
	static final double M_PI = Math.PI;
	static final double M_2PI = 2*Math.PI;
	static final double M_PI_2 = Math.PI/2.0;
	
	
	@Test
	public void testTransformIdentity() {
		Transformation t = Transformation.IDENTITY;
		assertEquals( x_unit,  t.transform(x_unit) );
		assertEquals( y_unit,  t.transform(y_unit) );
		assertEquals( z_unit,  t.transform(z_unit) );
	}
	
	@Test
	public void testTransformIdentityToOpenGL() {
		Transformation t = Transformation.IDENTITY;
		DoubleBuffer buf = t.getGLMatrix();
		
		assertEquals( 1.0, buf.get(0), 1e-6);
		assertEquals( 0.0, buf.get(1), 1e-6);
		assertEquals( 0.0, buf.get(2), 1e-6);
		assertEquals( 0.0, buf.get(3), 1e-6);
		
		assertEquals( 0.0, buf.get(4), 1e-6);
		assertEquals( 1.0, buf.get(5), 1e-6);
		assertEquals( 0.0, buf.get(6), 1e-6);
		assertEquals( 0.0, buf.get(7), 1e-6);
		
		assertEquals( 0.0, buf.get( 8), 1e-6);
		assertEquals( 0.0, buf.get( 9), 1e-6);
		assertEquals( 1.0, buf.get(10), 1e-6);
		assertEquals( 0.0, buf.get(11), 1e-6);
		
		assertEquals( 0.0, buf.get(12), 1e-6);
		assertEquals( 0.0, buf.get(13), 1e-6);
		assertEquals( 0.0, buf.get(14), 1e-6);
		assertEquals( 1.0, buf.get(15), 1e-6);		
	}
	
	@Test
	public void testTransformTranslationToOpenGL() {
		Transformation translate = new Transformation( 1,2,3 );
		DoubleBuffer buf = translate.getGLMatrix();
		
		assertEquals( 1.0, buf.get(0), 1e-6);
		assertEquals( 0.0, buf.get(1), 1e-6);
		assertEquals( 0.0, buf.get(2), 1e-6);
		assertEquals( 0.0, buf.get(3), 1e-6);
		
		assertEquals( 0.0, buf.get(4), 1e-6);
		assertEquals( 1.0, buf.get(5), 1e-6);
		assertEquals( 0.0, buf.get(6), 1e-6);
		assertEquals( 0.0, buf.get(7), 1e-6);
		
		assertEquals( 0.0, buf.get( 8), 1e-6);
		assertEquals( 0.0, buf.get( 9), 1e-6);
		assertEquals( 1.0, buf.get(10), 1e-6);
		assertEquals( 0.0, buf.get(11), 1e-6);
		
		assertEquals( 1.0, buf.get(12), 1e-6);
		assertEquals( 2.0, buf.get(13), 1e-6);
		assertEquals( 3.0, buf.get(14), 1e-6);
		assertEquals( 1.0, buf.get(15), 1e-6);		
	}
	
	
	@Test
	public void testTransformRotateByPI2ToOpenGL() {
		Transformation translate = Transformation.getAxialRotation(M_PI_2);
		DoubleBuffer buf = translate.getGLMatrix();
		
		assertEquals( 1.0, buf.get(0), 1e-6);
		assertEquals( 0.0, buf.get(1), 1e-6);
		assertEquals( 0.0, buf.get(2), 1e-6);
		assertEquals( 0.0, buf.get(3), 1e-6);
		
		assertEquals( 0.0, buf.get(4), 1e-6);
		assertEquals( 0.0, buf.get(5), 1e-6);
		assertEquals( 1.0, buf.get(6), 1e-6);
		assertEquals( 0.0, buf.get(7), 1e-6);
		
		assertEquals( 0.0, buf.get( 8), 1e-6);
		assertEquals( -1.0, buf.get( 9), 1e-6);
		assertEquals( 0.0, buf.get(10), 1e-6);
		assertEquals( 0.0, buf.get(11), 1e-6);
		
		assertEquals( 0.0, buf.get(12), 1e-6);
		assertEquals( 0.0, buf.get(13), 1e-6);
		assertEquals( 0.0, buf.get(14), 1e-6);
		assertEquals( 1.0, buf.get(15), 1e-6);		
	}
	
	@Test
	public void testTransformTranslationIndividual() {
		Transformation translate = new Transformation( 1,2,3 );
		
		assertEquals( new Coordinate(2,2,3), translate.transform( x_unit ));
		assertEquals( new Coordinate(1,3,3), translate.transform( y_unit ));
		assertEquals( new Coordinate(1,2,4), translate.transform( z_unit ));
	}
	
	@Test
	public void testTransformTranslationCoordinate() {
		Transformation translate = new Transformation( new Coordinate( 2,3,4));
		
		assertEquals( new Coordinate(3,3,4), translate.transform( x_unit ));
		assertEquals( new Coordinate(2,4,4), translate.transform( y_unit ));
		assertEquals( new Coordinate(2,3,5), translate.transform( z_unit ));
	}
	
	@Test
	public void testTransformTranslationConvenience() {
		Transformation translate = Transformation.getTranslationTransform( 2,3,4);
		
		assertEquals( new Coordinate(3,3,4), translate.transform( x_unit ));
		assertEquals( new Coordinate(2,4,4), translate.transform( y_unit ));
		assertEquals( new Coordinate(2,3,5), translate.transform( z_unit ));
	}

	@Test
	public void testTransformSmallYRotation() {
		Transformation t = Transformation.rotate_y(0.01);

		Coordinate v1 = t.transform( x_unit );
		// we need to test individual coordinates due to error.
		assertEquals( 1, v1.x, .001);
		assertEquals( 0, v1.y, .001);
		assertEquals( -.01, v1.z, .001);
		
		assertEquals( y_unit, t.transform( y_unit ));
	
		Coordinate v2 = t.transform( z_unit );
		// we need to test individual coordinates due to error.
		assertEquals( .01, v2.x, .001);
		assertEquals( 0, v2.y, .001);
		assertEquals( 1, v2.z, .001);
	}

	@Test
	public void testTransformRotateXByPI2() {
		Transformation t = Transformation.getAxialRotation(M_PI_2);
		
		assertEquals( x_unit, t.transform(x_unit));
		assertEquals( z_unit, t.transform( y_unit ));
		assertEquals( y_unit.multiply(-1), t.transform( z_unit ));
	}
	

	@Test
	public void testTransformEuler313Transform() {
		{
			Transformation r313 = Transformation.getEulerAngle313Transform(0.0, 0.0, M_PI_2);
			assertEquals( y_unit, r313.transform( x_unit ));
			assertEquals( x_unit.multiply(-1), r313.transform( y_unit ));
			assertEquals( z_unit, r313.transform( z_unit ));
		}{
			Transformation r313 = Transformation.getEulerAngle313Transform(M_PI/4.0, 0.0, M_PI/4.0);
			// precision = 8 decimal places
			assertEquals( y_unit, r313.transform( x_unit ));
			assertEquals( x_unit.multiply(-1), r313.transform( y_unit ));
			assertEquals( z_unit, r313.transform( z_unit ));
		}{
			Transformation r313 = Transformation.getEulerAngle313Transform(M_PI/4.0, M_PI_2, M_PI/4.0);
			// precision = 8 decimal places
			assertEquals( new Coordinate(+0.500000,    +0.500000,    0.707106781), r313.transform( x_unit ));
			assertEquals( new Coordinate(-0.500000,    -0.5000000,   0.707106781), r313.transform( y_unit ));
			assertEquals( new Coordinate(+0.707106781, -0.707106781, 0.0), r313.transform( z_unit ));			
		}
	}

	@Test
	public void testTransformEuler121Transform() {
		Transformation r123 = Transformation.rotate_x(-1.0)
				.applyTransformation(Transformation.rotate_y(0.01))
				.applyTransformation(Transformation.rotate_z(1.0));
		
		
		assertEquals( new Coordinate(+0.540275291, +0.450102302, -0.710992634), r123.transform( x_unit ));
		assertEquals( new Coordinate(-0.841428912, +0.299007198, -0.450102302), r123.transform( y_unit ));
		assertEquals( new Coordinate(+0.009999833334, +0.841428911609, +0.540275290977), r123.transform( z_unit ));

	}

	@Test
	public void testTransformRotateTranslate() {
		Transformation r = Transformation.getTranslationTransform( 2,3,4)  
				.applyTransformation(Transformation.getAxialRotation( M_PI_2 ));
				
		assertEquals( new Coordinate( 3,3,4), r.transform( x_unit ));
		assertEquals( new Coordinate( 2,3,5), r.transform( y_unit ));
		assertEquals( new Coordinate( 2,2,4), r.transform( z_unit ));
	}	
	
}
