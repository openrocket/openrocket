package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class SymmetricComponentVolumeTest extends BaseTestCase {
	
	@Test
	public void simpleConeFilled() {
		NoseCone nc = new NoseCone();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setFilled(true);
		nc.setType(Transition.Shape.CONICAL);
		nc.setAftRadius(1.0);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.75, cg.x, epsilonPercent * 0.75);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleConeWithShoulderFilled() {
		NoseCone nc = new NoseCone();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setFilled(true);
		nc.setType(Transition.Shape.CONICAL);
		nc.setAftRadius(1.0);
		nc.setAftShoulderRadius(1.0);
		nc.setAftShoulderLength(1.0);
		nc.setAftShoulderThickness(1.0);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0;
		volume += Math.PI;
		
		double mass = density * volume;
		
		//System.out.println(volume + "\t" + mass);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(1.312, cg.x, epsilonPercent * 1.071);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleConeHollow() {
		NoseCone nc = new NoseCone();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setAftRadius(1.0);
		nc.setThickness(0.5);
		nc.setType(Transition.Shape.CONICAL);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0; // outer volume
		
		// manually projected Thickness of 0.5 on to radius to determine
		// the innerConeDimen.  Since the outer cone is "square" (height = radius),
		// we only need to compute this one dimension in order to compute the
		// volume of the inner cone.
		double innerConeDimen = 1.0 - Math.sqrt(2.0) / 2.0;
		double innerVolume = Math.PI / 3.0 * innerConeDimen * innerConeDimen * innerConeDimen;
		volume -= innerVolume;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.7454, cg.x, epsilonPercent * 0.7454);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleConeWithShoulderHollow() {
		NoseCone nc = new NoseCone();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setType(Transition.Shape.CONICAL);
		nc.setAftRadius(1.0);
		nc.setThickness(0.5);
		nc.setAftShoulderRadius(1.0);
		nc.setAftShoulderLength(1.0);
		nc.setAftShoulderThickness(0.5);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0; // outer volume
		
		// manually projected Thickness of 0.5 on to radius to determine
		// the innerConeDimen.  Since the outer cone is "square" (height = radius),
		// we only need to compute this one dimension in order to compute the
		// volume of the inner cone.
		double innerConeDimen = 1.0 - Math.sqrt(2.0) / 2.0;
		double innerVolume = Math.PI / 3.0 * innerConeDimen * innerConeDimen * innerConeDimen;
		volume -= innerVolume;
		
		volume += Math.PI - Math.PI * 0.5 * 0.5;
		
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(1.2719, cg.x, epsilonPercent * 1.2719);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionFilled() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(4.0);
		nc.setFilled(true);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(1.0);
		nc.setAftRadius(2.0);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0 * (2.0 * 2.0 + 2.0 * 1.0 + 1.0 * 1.0) * 4.0;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(2.4285, cg.x, epsilonPercent * 2.4285);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionWithShouldersFilled() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(4.0);
		nc.setFilled(true);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(1.0);
		nc.setAftRadius(2.0);
		nc.setAftShoulderLength(1.0);
		nc.setAftShoulderRadius(2.0);
		nc.setAftShoulderThickness(2.0);
		nc.setForeShoulderLength(1.0);
		nc.setForeShoulderRadius(1.0);
		nc.setForeShoulderThickness(1.0);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		double volume = Math.PI / 3.0 * (2.0 * 2.0 + 2.0 * 1.0 + 1.0 * 1.0) * 4.0;
		// plus aft shoulder:
		volume += Math.PI * 1.0 * 2.0 * 2.0;
		// plus fore shoulder:
		volume += Math.PI * 1.0 * 1.0 * 1.0;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(2.8023, cg.x, epsilonPercent * 2.8023);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionHollow1() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(0.5);
		nc.setAftRadius(1.0);
		nc.setThickness(0.5);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		// Volume of filled transition = 
		double filledVolume = Math.PI / 3.0 * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5) * 1.0;
		
		// magic 2D cad drawing...
		//
		// Since the thickness >= fore radius, the
		// hollowed out portion of the transition
		// forms a cone.
		// the dimensions of this cone were determined
		// using a 2d cad tool.
		
		double innerConeRadius = 0.441;
		double innerConeLength = 0.882;
		double innerVolume = Math.PI / 3.0 * innerConeLength * innerConeRadius * innerConeRadius;
		
		double volume = filledVolume - innerVolume;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.5884, cg.x, epsilonPercent * 0.5884);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionWithShouldersHollow1() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(0.5);
		nc.setAftRadius(1.0);
		nc.setThickness(0.5);
		nc.setAftShoulderLength(1.0);
		nc.setAftShoulderRadius(1.0);
		nc.setAftShoulderThickness(0.5);
		nc.setForeShoulderLength(1.0);
		nc.setForeShoulderRadius(0.5);
		nc.setForeShoulderThickness(0.5); // note this means fore shoulder is filled.
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		// Volume of filled transition = 
		double filledVolume = Math.PI / 3.0 * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5) * 1.0;
		
		// magic 2D cad drawing...
		//
		// Since the thickness >= fore radius, the
		// hollowed out portion of the transition
		// forms a cone.
		// the dimensions of this cone were determined
		// using a 2d cad tool.
		
		double innerConeRadius = 0.441;
		double innerConeLength = 0.882;
		double innerVolume = Math.PI / 3.0 * innerConeLength * innerConeRadius * innerConeRadius;
		
		double volume = filledVolume - innerVolume;
		
		// Now add aft shoulder
		volume += Math.PI * 1.0 * 1.0 * 1.0 - Math.PI * 1.0 * 0.5 * 0.5;
		// Now add fore shoulder
		volume += Math.PI * 1.0 * 0.5 * 0.5;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.8581, cg.x, epsilonPercent * 0.8581);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionHollow2() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(0.5);
		nc.setAftRadius(1.0);
		nc.setThickness(0.25);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		// Volume of filled transition = 
		double filledVolume = Math.PI / 3.0 * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5) * 1.0;
		
		// magic 2D cad drawing...
		//
		// Since the thickness < fore radius, the
		// hollowed out portion of the transition
		// forms a transition.
		// the dimensions of this transition were determined
		// using a 2d cad tool.
		
		double innerTransitionAftRadius = 0.7205;
		double innerTransitionForeRadius = 0.2205;
		double innerVolume = Math.PI / 3.0
				* (innerTransitionAftRadius * innerTransitionAftRadius + innerTransitionAftRadius * innerTransitionForeRadius + innerTransitionForeRadius * innerTransitionForeRadius);
		
		double volume = filledVolume - innerVolume;
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.56827, cg.x, epsilonPercent * 0.56827);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
	@Test
	public void simpleTransitionWithShouldersHollow2() {
		Transition nc = new Transition();
		
		final double epsilonPercent = 0.001;
		final double density = 2.0;
		
		nc.setLength(1.0);
		nc.setType(Transition.Shape.CONICAL);
		nc.setForeRadius(0.5);
		nc.setAftRadius(1.0);
		nc.setThickness(0.25);
		nc.setAftShoulderLength(1.0);
		nc.setAftShoulderRadius(1.0);
		nc.setAftShoulderThickness(0.25);
		nc.setForeShoulderLength(1.0);
		nc.setForeShoulderRadius(0.5);
		nc.setForeShoulderThickness(0.25);
		
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		Coordinate cg = nc.getCG();
		
		//System.out.println(nc.getComponentVolume() + "\t" + nc.getMass());
		//System.out.println(cg);
		
		// Volume of filled transition = 
		double filledVolume = Math.PI / 3.0 * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5) * 1.0;
		
		// magic 2D cad drawing...
		//
		// Since the thickness < fore radius, the
		// hollowed out portion of the transition
		// forms a transition.
		// the dimensions of this transition were determined
		// using a 2d cad tool.
		
		double innerTransitionAftRadius = 0.7205;
		double innerTransitionForeRadius = 0.2205;
		double innerVolume = Math.PI / 3.0
				* (innerTransitionAftRadius * innerTransitionAftRadius + innerTransitionAftRadius * innerTransitionForeRadius + innerTransitionForeRadius * innerTransitionForeRadius);
		
		double volume = filledVolume - innerVolume;
		
		// now add aft shoulder
		volume += Math.PI * 1.0 * 1.0 * 1.0 - Math.PI * 1.0 * 0.75 * 0.75;
		// now add fore shoulder
		volume += Math.PI * 1.0 * 0.5 * 0.5 - Math.PI * 1.0 * 0.25 * 0.25;
		
		
		double mass = density * volume;
		
		//System.out.println(volume);
		
		assertEquals(volume, nc.getComponentVolume(), epsilonPercent * volume);
		assertEquals(mass, nc.getMass(), epsilonPercent * mass);
		
		assertEquals(0.7829, cg.x, epsilonPercent * 0.7829);
		assertEquals(mass, cg.weight, epsilonPercent * mass);
	}
	
}
