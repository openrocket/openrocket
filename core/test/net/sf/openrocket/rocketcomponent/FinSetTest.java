package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class FinSetTest extends BaseTestCase {

	@Test
	public void testCGComputation() throws Exception {

		{
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);

			Coordinate coords = fins.getCG();
			assertEquals(1.0, fins.getFinArea(), 0.001);
			assertEquals(0.5, coords.x, 0.001);
			assertEquals(0.5, coords.y, 0.001);
		}

		{
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 0.5, 0.0, 1.0, .005);

			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
		
		{
			// This is the same shape as the previous TrapezoidFinSet.
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(0,1),
				new Coordinate(.5,1),
				new Coordinate(1,0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}

		{
			// Add some superfluous points which are on the outline but "far apart"
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(0,.5),
				new Coordinate(0,1),
				new Coordinate(.25,1),
				new Coordinate(.5,1),
				new Coordinate(.75,.5),
				new Coordinate(1,0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}

		{
			// add some points which are close
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(0,1E-15),
				new Coordinate(0,1),
				new Coordinate(1E-15,1),
				new Coordinate(.5,1),
				new Coordinate(.5,1-1E-15),
				new Coordinate(1,1E-15),
				new Coordinate(1,0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}

		{
			// Different shaped figure.  Two rectangles crafted so two pairs of points y_0 = - y_1
			FreeformFinSet fins = new FreeformFinSet();
			fins.setFinCount(1);
			Coordinate[] points = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(0,1),
				new Coordinate(2,1),
				new Coordinate(2,-1),
				new Coordinate(1,-1),
				new Coordinate(1,0)
			};
			fins.setPoints(points);
			Coordinate coords = fins.getCG();
			assertEquals(3.0, fins.getFinArea(), 0.001);
			assertEquals(3.5/3.0, coords.x, 0.001);
			assertEquals(0.5/3.0, coords.y, 0.001);
		}

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
		fin.setColor(ColorConversion.fromAwtColor(Color.YELLOW));
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
