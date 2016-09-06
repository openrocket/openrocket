package net.sf.openrocket.masscalc;

//import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Mass;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassDataTest extends BaseTestCase {
	
	// tolerance for compared double test results
	protected final double EPSILON = MathUtil.EPSILON;
	
	protected final Coordinate ZERO = new Coordinate(0., 0., 0.);

	@Test
	public void testTwoPointInline() {
		double m1 = 2.5;
		Mass r1 = new Mass(0,-40, 0, m1);
		double I1ax=28.7;
		double I1t = I1ax/2;
		MassData body1 = new MassData( r1, I1ax, I1t, I1t);
		
		double m2 = 5.7;
		Mass r2 = new Mass(0, 32, 0, m2);
		double I2ax=20;
		double I2t = I2ax/2;
		MassData body2 = new MassData( r2, I2ax, I2t, I2t);
		
		// point 3 is defined as the CM of bodies 1 and 2 combined.
		MassData asbly3 = body1.add(body2);
		
		Mass cm3_actual = new Mass( asbly3.getCM());
		Mass cm3_expected = r1.add(r2);
		assertEquals(" Center of Mass calculated incorrectly: ", cm3_expected, cm3_actual );
				
		// these are a bit of a hack, and depend upon all the bodies being along the y=0, z=0 line.
		Coordinate delta13 = asbly3.getCM().sub( r1.toCoordinate());
		Coordinate delta23 = asbly3.getCM().sub( r2.toCoordinate());
		
		double y13 = delta13.y;
		double dy_13_2 = MathUtil.pow2( y13); // hack
		double I13ax = I1ax + m1*dy_13_2;
		double I13zz = I1t + m1*dy_13_2;
		
		double y23 = delta23.y;
		double dy_23_2 = MathUtil.pow2( y23); // hack
		double I23ax = I2ax + m2*dy_23_2 ;
		double I23zz = I2t + m2*dy_23_2 ;
		
		double expI3xx = I13ax+I23ax;
		double expI3yy = I1t+I2t;
		double expI3zz = I13zz+I23zz;
		
		assertEquals("x-axis MOI don't match: ", asbly3.getIxx(), expI3xx, EPSILON*10);
		
		assertEquals("y-axis MOI don't match: ", asbly3.getIyy(), expI3yy, EPSILON*10);
		
		assertEquals("z-axis MOI don't match: ", asbly3.getIzz(), expI3zz, EPSILON*10);
	}
	
	
	@Test
	public void testTwoPointGeneral() {
		boolean debug=false;
		double m1 = 2.5;
		Mass r1 = new Mass(0,-40, -10, m1);
		double I1xx=28.7;
		double I1t = I1xx/2;
		MassData body1 = new MassData(r1, I1xx, I1t, I1t);
		
		double m2 = 5.7;
		Mass r2 = new Mass(0, 32, 15, m2);
		double I2xx=20;
		double I2t = I2xx/2;
		MassData body2 = new MassData(r2, I2xx, I2t, I2t);
		
		// point 3 is defined as the CM of bodies 1 and 2 combined.
		MassData asbly3 = body1.add(body2);

		Mass cm3_actual = new Mass( asbly3.getCM());
		Mass cm3_expected = r1.add(r2);
//		System.err.println("     @(1):     "+ body1.toDebug());
//		System.err.println("     @(2):     "+ body2.toDebug());
//		System.err.println("     @(3):     "+ asbly3.toDebug());
//		System.err.println(" Center of Mass: (3) expected:  "+ cm3_expected);
		assertEquals(" Center of Mass calculated incorrectly: ", cm3_expected, cm3_actual );
		
		
		if(debug){
			System.err.println(" Body 1:     "+ body1.toDebug() );
			System.err.println(" Body 2:     "+ body2.toDebug() );
			System.err.println(" Body 3:     "+ asbly3.toDebug() );
		}

		
		// these are a bit of a hack, and depend upon all the bodies being along the y=0, z=0 line.
		Coordinate delta13 = asbly3.getCM().sub( r1.toCoordinate());
		Coordinate delta23 = asbly3.getCM().sub( r2.toCoordinate());
		double x2, y2, z2;
		
		x2 = MathUtil.pow2( delta13.x);
		y2 = MathUtil.pow2( delta13.y);
		z2 = MathUtil.pow2( delta13.z);
		double I13xx = I1xx + m1*(y2+z2);
		double I13yy = I1t + m1*(x2+z2);
		double I13zz = I1t + m1*(x2+y2);
		
		x2 = MathUtil.pow2( delta23.x);
		y2 = MathUtil.pow2( delta23.y);
		z2 = MathUtil.pow2( delta23.z);
		double I23xx = I2xx + m2*(y2+z2);
		double I23yy = I2t + m2*(x2+z2);
		double I23zz = I2t + m2*(x2+y2);
		
		double expI3xx = I13xx + I23xx;
		assertEquals("x-axis MOI don't match: ", asbly3.getIxx(), expI3xx, EPSILON*10);
		
		double expI3yy = I13yy + I23yy;
		assertEquals("y-axis MOI don't match: ", asbly3.getIyy(), expI3yy, EPSILON*10);
		
		double expI3zz = I13zz + I23zz;
		assertEquals("z-axis MOI don't match: ", asbly3.getIzz(), expI3zz, EPSILON*10);
	}
	

	@Test
	public void testMassDataCompoundCalculations() {
		double m1 = 2.5;
		Mass r1 = new Mass(0,-40, 0, m1);
		double I1ax=28.7;
		double I1t = I1ax/2;
		MassData body1 = new MassData(r1, I1ax, I1t, I1t);
		
		double m2 = m1;
		Mass r2 = new Mass(0, -2, 0, m2);
		double I2ax=28.7;
		double I2t = I2ax/2;
		MassData body2 = new MassData(r2, I2ax, I2t, I2t);
		
		double m5 = 5.7;
		Mass r5 = new Mass(0, 32, 0, m5);
		double I5ax=20;
		double I5t = I5ax/2;
		MassData body5 = new MassData(r5, I5ax, I5t, I5t);
		
		// point 3 is defined as the CM of bodies 1 and 2 combined.
		MassData asbly3 = body1.add(body2);
		
		// point 4 is defined as the CM of bodies 1, 2 and 5 combined.
		MassData asbly4_indirect = asbly3.add(body5);
		Mass cm4_expected = r1.add(r2).add(r5);
		Mass cm4_actual = new Mass( 0, 7.233644859813085, 0, m1+m2+m5 );
		
		//System.err.println(" Center of Mass: (3):     "+ asbly3.toCMDebug() );
		//System.err.println("           MOI:  (3):     "+ asbly3.toIMDebug() );
		//System.err.println(" Center of Mass: indirect:"+ asbly4_indirect.getCM() );
		//System.err.println(" Center of Mass: (4) direct:  "+ cm4_expected);
		assertEquals(" Center of Mass calculated incorrectly: ", cm4_expected, cm4_actual);
		
		// these are a bit of a hack, and depend upon all the bodies being along the y=0, z=0 line.
		double y4 = cm4_expected.y;
		double I14ax = I1ax + m1*MathUtil.pow2( Math.abs(body1.getCM().y - y4) );
		double I24ax = I2ax + m2*MathUtil.pow2( Math.abs(body2.getCM().y - y4) );
		double I54ax = I5ax + m5*MathUtil.pow2( Math.abs(body5.getCM().y - y4) );
		
		double I14zz = I1t + m1*MathUtil.pow2( Math.abs(body1.getCM().y - y4) );
		double I24zz = I2t + m2*MathUtil.pow2( Math.abs(body2.getCM().y - y4) );
//		System.err.println(String.format(" I24yy: %8g = %6g + %3g*%g", I24zz,  I2t, m2, MathUtil.pow2( Math.abs(body2.getCM().y - y4)) ));
//		System.err.println(String.format("      : delta y24: %8g = ||%g - %g||", Math.abs(body2.getCM().y - y4), body2.getCM().y, y4 ));
		double I54zz = I5t + m5*MathUtil.pow2( Math.abs(body5.getCM().y - y4) );
		
		double I4xx = I14ax+I24ax+I54ax;
		double I4yy = I1t+I2t+I5t;
		double I4zz = I14zz+I24zz+I54zz;
		MassData asbly4_expected = new MassData( cm4_expected, I4xx, I4yy, I4zz);
		//System.err.println(String.format(" Ixx: direct:   %12g", I4xx ));
		assertEquals("x-axis MOI don't match: ", asbly4_indirect.getIxx(), asbly4_expected.getIxx(), EPSILON*10);
		
		//System.err.println(String.format(" Iyy: direct:   %12g", I4yy ));
		assertEquals("y-axis MOI don't match: ", asbly4_indirect.getIyy(), asbly4_expected.getIyy(), EPSILON*10);
		
		//System.err.println(String.format(" Izz: direct: %12g", I4zz));
		assertEquals("z-axis MOI don't match: ", asbly4_indirect.getIzz(), asbly4_expected.getIzz(), EPSILON*10);
	}
	
	
}
