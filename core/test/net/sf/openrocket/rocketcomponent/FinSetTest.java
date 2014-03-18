package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class FinSetTest extends BaseTestCase {
	
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
		// the compuation of CP was incorrect because of numerical instability.
		//
		//     +-----------------+
		//      \                 \
		//       \                 \
		//        +                 \
		//       /                   \
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
		System.out.println(forces);
		assertEquals(0.023409, forces.getCP().x, 0.0001);
	}
	
	@Test
	public void testFreeFormCGWithNegativeY() throws Exception {
		// This particular fin shape is currently not allowed in OR since the y values are negative
		// however, it is possible to convert RockSim files and end up with fins which
		// have negative y values.
		
		// A user submitted an ork file which could not be simulated because the fin
		// was constructed on a tail cone.  It so happened that for one pair of points
		// y_n = - y_(n+1) which caused a divide by zero and resulted in CGx = NaN.
		
		// This Fin set is constructed to have the same problem.  It is a square and rectagle
		// where the two trailing edge corners of the rectangle satisfy y_0 = -y_1
		//
		// +---------+
		// |         |
		// |         |
		// +----+    |
		//      |    |
		//      |    |
		//      +----+
		
		FreeformFinSet fins = new FreeformFinSet();
		fins.setFinCount(1);
		Coordinate[] points = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0, 1),
				new Coordinate(2, 1),
				new Coordinate(2, -1),
				new Coordinate(1, -1),
				new Coordinate(1, 0)
		};
		fins.setPoints(points);
		Coordinate coords = fins.getCG();
		assertEquals(3.0, fins.getFinArea(), 0.001);
		assertEquals(3.5 / 3.0, coords.x, 0.001);
		assertEquals(0.5 / 3.0, coords.y, 0.001);
		
	}
	
	
	@Test
	public void testFreeformConvert() {
		testFreeformConvert(new TrapezoidFinSet());
		testFreeformConvert(new EllipticalFinSet());
		testFreeformConvert(new FreeformFinSet());
	}
	
	
	private void testFreeformConvert(FinSet fin) {
		FreeformFinSet converted;
		Material mat = Material.newMaterial(Type.BULK, "foo", 0.1, true);
		
		fin.setBaseRotation(1.1);
		fin.setCantAngle(0.001);
		fin.setCGOverridden(true);
		fin.setColor(Color.BLACK);
		fin.setComment("cmt");
		fin.setCrossSection(CrossSection.ROUNDED);
		fin.setFinCount(5);
		fin.setFinish(Finish.ROUGH);
		fin.setLineStyle(LineStyle.DASHDOT);
		fin.setMassOverridden(true);
		fin.setMaterial(mat);
		fin.setOverrideCGX(0.012);
		fin.setOverrideMass(0.0123);
		fin.setOverrideSubcomponents(true);
		fin.setPositionValue(0.1);
		fin.setRelativePosition(Position.ABSOLUTE);
		fin.setTabHeight(0.01);
		fin.setTabLength(0.02);
		fin.setTabRelativePosition(TabRelativePosition.END);
		fin.setTabShift(0.015);
		fin.setThickness(0.005);
		
		
		converted = FreeformFinSet.convertFinSet((FinSet) fin.copy());
		
		ComponentCompare.assertSimilarity(fin, converted, true);
		
		assertEquals(converted.getComponentName(), converted.getName());
		
		
		// Create test rocket
		Rocket rocket = new Rocket();
		Stage stage = new Stage();
		BodyTube body = new BodyTube();
		
		rocket.addChild(stage);
		stage.addChild(body);
		body.addChild(fin);
		
		Listener l1 = new Listener("l1");
		rocket.addComponentChangeListener(l1);
		
		fin.setName("Custom name");
		assertTrue(l1.changed);
		assertEquals(ComponentChangeEvent.NONFUNCTIONAL_CHANGE, l1.changetype);
		
		
		// Create copy
		RocketComponent rocketcopy = rocket.copy();
		
		Listener l2 = new Listener("l2");
		rocketcopy.addComponentChangeListener(l2);
		
		FinSet fincopy = (FinSet) rocketcopy.getChild(0).getChild(0).getChild(0);
		FreeformFinSet.convertFinSet(fincopy);
		
		assertTrue(l2.changed);
		assertEquals(ComponentChangeEvent.TREE_CHANGE,
				l2.changetype & ComponentChangeEvent.TREE_CHANGE);
		
	}
	
	
	private static class Listener implements ComponentChangeListener {
		private boolean changed = false;
		private int changetype = 0;
		private final String name;
		
		public Listener(String name) {
			this.name = name;
		}
		
		@Override
		public void componentChanged(ComponentChangeEvent e) {
			assertFalse("Ensuring listener " + name + " has not been called.", changed);
			changed = true;
			changetype = e.getType();
		}
	}
	
}
