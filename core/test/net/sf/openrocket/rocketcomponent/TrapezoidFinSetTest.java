package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class TrapezoidFinSetTest extends BaseTestCase {
	
	@Test
	public void testTrapezoidCGComputation() {
		
		{
			// This is a simple square fin with sides of 1.0.
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(1.0, fins.getFinArea(), 0.001);
			assertEquals(0.5, coords.x, 0.001);
			assertEquals(0.5, coords.y, 0.001);
		}
		
		{
			// This is a trapezoid.  Height 1, root 1, tip 1/2 no sweep.
			// It can be decomposed into a rectangle followed by a triangle
			//  +---+
			//  |    \
			//  |     \
			//  +------+
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 0.5, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
	}
	
	@Test
	public void testInstancePoints_PI_2_BaseRotation() {
		// This is a simple square fin with sides of 1.0.
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(4);
		fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		fins.setBaseRotation( Math.PI/2 );
		
		BodyTube body = new BodyTube(1.0, 0.05 );
		body.addChild( fins );
		
		Coordinate[] points = fins.getInstanceOffsets();
		
		assertEquals( 0, points[0].x, 0.00001);
		assertEquals( 0, points[0].y, 0.00001);
		assertEquals( 0.05, points[0].z, 0.00001);
		
		assertEquals( 0, points[1].x, 0.00001);
		assertEquals( -0.05, points[1].y, 0.00001);
		assertEquals( 0, points[1].z, 0.00001);		
	}
			
	@Test
	public void testInstancePoints_PI_4_BaseRotation() {
		// This is a simple square fin with sides of 1.0.
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(4);
		fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		fins.setBaseRotation( Math.PI/4 );
		
		BodyTube body = new BodyTube(1.0, 0.05 );
		body.addChild( fins );
		
		Coordinate[] points = fins.getInstanceOffsets();
		
		assertEquals( 0, points[0].x, 0.0001);
		assertEquals( 0.03535, points[0].y, 0.0001);
		assertEquals( 0.03535, points[0].z, 0.0001);
		
		assertEquals( 0, points[1].x, 0.0001);
		assertEquals( -0.03535, points[1].y, 0.0001);
		assertEquals( 0.03535, points[1].z, 0.0001);	
	}
			
	
	@Test
	public void testInstanceAngles_zeroBaseRotation() {
		// This is a simple square fin with sides of 1.0.
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(4);
		fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		fins.setBaseRotation( 0.0 );

		double[] angles = fins.getInstanceAngles();
			
		assertEquals( angles[0], 0, 0.000001 );
		assertEquals( angles[1], Math.PI/2, 0.000001 );
		assertEquals( angles[2], Math.PI, 0.000001 );
		assertEquals( angles[3], 1.5*Math.PI, 0.000001 );
	}
	
	@Test
	public void testInstanceAngles_90_BaseRotation() {
		// This is a simple square fin with sides of 1.0.
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(4);
		fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		fins.setBaseRotation( Math.PI/2 );

		double[] angles = fins.getInstanceAngles();
			
		assertEquals( angles[0], Math.PI/2, 0.000001 );
		assertEquals( angles[1], Math.PI, 0.000001 );
		assertEquals( angles[2], 1.5*Math.PI, 0.000001 );
		assertEquals( angles[3], 0, 0.000001 );
	}
	
}
