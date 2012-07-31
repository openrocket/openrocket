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
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class FinSetTest extends BaseTestCase {
	

	@Test
	public void testFreeformConvert() {
		testFreeformConvert(new TrapezoidFinSet());
		testFreeformConvert(new EllipticalFinSet());
		testFreeformConvert(new FreeformFinSet());
	}
	
	
	private void testFreeformConvert(FinSet fin) {
		FreeformFinSet converted;
		Material mat = Material.newUserMaterial(Type.BULK, "foo", 0.1);
		
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
