package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FreeformFinSetTest extends BaseTestCase {
	final double EPSILON = 0.0001;
	
	@Test
	public void testFreeformCGComputation() throws Exception {
		
		{
			// This is a trapezoid.  Height 1, root 1, tip 1/2 no sweep.
			// It can be decomposed into a rectangle followed by a triangle
			//  +---+
			//  |    \
			//  |     \
			//  +------+
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
					new Coordinate(0, 0),
					new Coordinate(0, 1),
					new Coordinate(.5, 1),
					new Coordinate(1, 0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
		{
			// This is the same trapezoid as previous free form, but it has
			// some extra points along the lines.
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
					new Coordinate(0, 0),
					new Coordinate(0, .5),
					new Coordinate(0, 1),
					new Coordinate(.25, 1),
					new Coordinate(.5, 1),
					new Coordinate(.75, .5),
					new Coordinate(1, 0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
		{
			// This is the same trapezoid as previous free form, but it has
			// some extra points which are very close to previous points.
			// in particular for points 0 & 1,
			// y0 + y1 is very small.
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
					new Coordinate(0, 0),
					new Coordinate(0, 1E-15),
					new Coordinate(0, 1),
					new Coordinate(1E-15, 1),
					new Coordinate(.5, 1),
					new Coordinate(.5, 1 - 1E-15),
					new Coordinate(1, 1E-15),
					new Coordinate(1, 0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
	}
	
	@Test
	public void testWildmanVindicatorShape() throws Exception {
		// This fin shape is similar to the aft fins on the Wildman Vindicator.
		// A user noticed that if the y values are similar but not equal,
		// the computation of CP was incorrect because of numerical instability.
		//
		//     +-----------------+
		//      \                 \
		//       \                 \
		//        +                 \         +x
		//       /                   \        <=+
		//      +---------------------+
		//
		FreeformFinSet fins = new FreeformFinSet();
		fins.setFinCount(1);
		Coordinate[] points = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0.02143125, 0.01143),
				new Coordinate(0.009524999999999999, 0.032543749999999996),
				new Coordinate(0.041275, 0.032537399999999994),
				new Coordinate(0.066675, 0)
		};
		fins.setPoints(points);
		Coordinate coords = fins.getCG();
		assertEquals(0.00130, fins.getFinArea(), 0.00001);
		assertEquals(0.03423, coords.x, 0.00001);
		assertEquals(0.01427, coords.y, 0.00001);
		
		BodyTube bt = new BodyTube();
		bt.addChild(fins);
		FinSetCalc calc = new FinSetCalc(fins);
		FlightConditions conditions = new FlightConditions(null);
		AerodynamicForces forces = new AerodynamicForces();
		WarningSet warnings = new WarningSet();
		calc.calculateNonaxialForces(conditions, forces, warnings);
		//System.out.println(forces);
		assertEquals(0.023409, forces.getCP().x, 0.0001);
	}

	// this is a useful test case because it combines several odd features, and is likely to indicate faults
	@Test
	public void testFinsOnTransition(){
		Rocket rkt = TestRockets.makeV2();
		FreeformFinSet fins = (FreeformFinSet)rkt.getChild(0).getChild(2).getChild(0);
		assertThat( fins.getFinCount(), equalTo(4));
		
		// under development
//		System.err.println("  >> TESTING TRANSITION FINS");
		Coordinate cg = fins.getCG();
//		System.err.println("  << TESTING TRANSITION FINS");
		
		assertEquals("Calculated fin area is wrong: ", 0.006805, fins.getFinArea(), EPSILON);
		assertEquals("Calculated fin CGx is wrong: ",  0.0949629, cg.x, EPSILON);
		assertEquals( 0.0, cg.y, EPSILON);
		assertEquals( 0.0, cg.z, EPSILON);
		
		final double calcMass = fins.getMass()/fins.getFinCount();
		assertEquals( 0.0127693, calcMass, EPSILON);
	}

	@Test
	public void testFreeFormCGWithNegativeY() throws Exception {
		// A user submitted an ork file which could not be simulated because the fin
		// was constructed on a tail cone.  It so happened that for one pair of points
		// y_n = - y_(n+1) which caused a divide by zero and resulted in CGx = NaN.
		
		// This Fin set is constructed to have the same problem.  It is a square and rectangle
		// where the two trailing edge corners of the rectangle satisfy y_0 = -y_1
		//
		//    +=> +x
		//    0    1    2    3  
		//    |    |    |    |
		//     
		//    +---------+      - +1
		//    |         |             ^ 
		//    |         |             | +y    
		//----+----+    |      -  0   +  
		//         |    |
		//         |    |
		//         +----+      - -1
		//         |
		//         |		
		FreeformFinSet fins = new FreeformFinSet();
		fins.setCrossSection( CrossSection.SQUARE );  // to ensure uniform density
		fins.setFinCount(1);
		// fins.setAxialOffset( Position.BOTTOM, 1.0);  // ERROR: no parent!
		Coordinate[] points = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0, 1),
				new Coordinate(2, 1),
				new Coordinate(2, -1),
				new Coordinate(1, -1),
				new Coordinate(1, 0)
		};
		fins.setPoints( points);
		fins.setFilletRadius( 0.0);
		fins.setTabHeight( 0.0);
		fins.setMaterial( Material.newMaterial(Type.BULK, "dummy", 1.0, true));

		assertEquals( 3.0, fins.getFinArea(), EPSILON);		
		
		Coordinate cg = fins.getCG();
		assertEquals( 1.1666, cg.x, EPSILON);
		assertEquals( 0.1666, cg.y, EPSILON);
		assertEquals( 0.0, cg.z, EPSILON);
		assertEquals( 0.009, cg.weight, EPSILON);
	}
	
	
	@Test
	public void testConvertTrapezoidToFreeform() {
		final FinSet sourceSet = new EllipticalFinSet();
		sourceSet.setName("fins-to-convert");
		Material mat = Material.newMaterial(Type.BULK, "foo", 0.1, true);
		sourceSet.setBaseRotation(1.1);
		sourceSet.setCantAngle(0.001);
		sourceSet.setCGOverridden(true);
		sourceSet.setColor(Color.BLACK);
		sourceSet.setComment("cmt");
		sourceSet.setCrossSection(CrossSection.ROUNDED);
		sourceSet.setFinCount(5);
		sourceSet.setFinish(Finish.ROUGH);
		sourceSet.setLineStyle(LineStyle.DASHDOT);
		sourceSet.setMassOverridden(true);
		sourceSet.setMaterial(mat);
		sourceSet.setOverrideCGX(0.012);
		sourceSet.setOverrideMass(0.0123);
		sourceSet.setOverrideSubcomponents(true);
		sourceSet.setPositionValue(0.1);
		sourceSet.setRelativePosition(Position.ABSOLUTE);
		sourceSet.setTabHeight(0.01);
		sourceSet.setTabLength(0.02);
		sourceSet.setTabRelativePosition( Position.BOTTOM);
		sourceSet.setTabShift(0.015);
		sourceSet.setThickness(0.005);
		
		BodyTube dummyTube = new BodyTube( 0.2, 0.01, 0.001 );
		dummyTube.addChild( sourceSet);
		
		FreeformFinSet destSet = FreeformFinSet.convertFinSet( sourceSet);
		
		assertEquals( sourceSet.getName(), destSet.getName());
		
	}
	
	@Test
	public void testConvertEllipticaToFreeform() {
		final FinSet sourceSet = new EllipticalFinSet();
		sourceSet.setName("fins-to-convert");
	
		Material mat = Material.newMaterial(Type.BULK, "foo", 0.1, true);
		
		sourceSet.setBaseRotation(1.1);
		sourceSet.setCantAngle(0.001);
		sourceSet.setCGOverridden(true);
		sourceSet.setColor(Color.BLACK);
		sourceSet.setComment("cmt");
		sourceSet.setCrossSection(CrossSection.ROUNDED);
		sourceSet.setFinCount(5);
		sourceSet.setFinish(Finish.ROUGH);
		sourceSet.setLineStyle(LineStyle.DASHDOT);
		sourceSet.setMassOverridden(true);
		sourceSet.setMaterial(mat);
		sourceSet.setOverrideCGX(0.012);
		sourceSet.setOverrideMass(0.0123);
		sourceSet.setOverrideSubcomponents(true);
		sourceSet.setPositionValue(0.1);
		sourceSet.setRelativePosition(Position.ABSOLUTE);
		sourceSet.setTabHeight(0.01);
		sourceSet.setTabLength(0.02);
		sourceSet.setTabRelativePosition( Position.BOTTOM);
		sourceSet.setTabShift(0.015);
		sourceSet.setThickness(0.005);
		
		FreeformFinSet destSet= FreeformFinSet.convertFinSet((FinSet) sourceSet.copy());
		
		assertEquals( sourceSet.getName(), destSet.getName());

		
		
	}
	

	
}
