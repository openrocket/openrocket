package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.MathUtil;
import static net.sf.openrocket.util.MathUtil.pow2;

import org.junit.Test;

public class SymmetricComponentVolumeTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON*1000;

	// helper functions

	// return Coordinate containing CG and volume of (possibly hollow if thickness < outerR) shoulder
	private Coordinate calculateShoulderCG(double x1, double length, double outerR, double thickness) {
		final double cg = x1 + length/2.0;

		final double innerR = Math.max(0.0, outerR - thickness);
		final double volume = Math.PI * length * (pow2(outerR) - pow2(innerR));

		return new Coordinate(cg, 0, 0, volume);
	}

	// return Coordinate containing CG and volume of frustum
	// still OK if foreward radius is 0 (ie a cone)
	private Coordinate calculateFrustumCG(double length, double foreRadius, double aftRadius) {
		final double moment = Math.PI * pow2(length) * (pow2(foreRadius) + 2.0 * foreRadius * aftRadius + 3.0 * pow2(aftRadius))/12.0;
		final double volume = Math.PI * length * (pow2(foreRadius) + foreRadius * aftRadius + pow2(aftRadius)) / 3.0;

		return new Coordinate(moment/volume, 0, 0, volume);
	}
	
	// return Coordinate containing CG and volume of conical transition
	private Coordinate calculateConicalTransitionCG(double length, double foreRadius, double aftRadius, double thickness) {
		// get moment and volume of outer frustum
		final Coordinate fullCG = calculateFrustumCG(length, foreRadius, aftRadius);

		// project thickness onto yz plane to get height
		final double angle = Math.atan((aftRadius - foreRadius)/length);
		final double height = thickness/Math.cos(angle);
		
		// if aftRadius <= height the transition is filled and we don't need to mess with
		// the inner frustum
		if (aftRadius <= height) {
			return fullCG;
		}
		
		 double innerLen = length;
		 double innerForeRad = foreRadius - height;
		 final double innerAftRad = aftRadius - height;
			
		 // if forward radius <= height the transition is a cone; we
		 // need to determine its length
		 if (foreRadius < height) {
			 innerLen = length * (aftRadius - height) / (aftRadius - foreRadius);
			 innerForeRad = 0;
		 }
		 
		 final Coordinate innerCG = calculateFrustumCG(innerLen, innerForeRad, innerAftRad);

		 // subtract inner from outer
		 final double offset = length - innerLen;
		 final double moment = fullCG.x * fullCG.weight - (innerCG.x + offset) * innerCG.weight;
		 final double volume = fullCG.weight - innerCG.weight;

		 return new Coordinate(moment/volume, 0, 0, volume);
	}

	// combine three CGs (typically forward shoulder, transition, and aft shoulder)
	private Coordinate combineCG(Coordinate cg1, Coordinate cg2, Coordinate cg3) {
		final double moment1 = cg1.x * cg1.weight;
		final double moment2 = cg2.x * cg2.weight;
		final double moment3 = cg3.x * cg3.weight;

		final double volume = cg1.weight + cg2.weight + cg3.weight;
		return new Coordinate((moment1 + moment2 + moment3) / volume, 0, 0, volume);
	}

	// check CG, volume, mass
	private void checkCG(Coordinate expectedCG, Transition nc) {

        Coordinate cg = nc.getCG();
        assertEquals("CG is incorrect", expectedCG.x, cg.x, EPSILON);
        assertEquals("Volume is incorrect", expectedCG.weight, nc.getComponentVolume(), EPSILON);

		final double mass = expectedCG.weight * nc.getMaterial().getDensity();
        assertEquals("Mass is incorrect", mass, nc.getMass(), EPSILON);
        assertEquals("Mass (stored in cg.weight) is incorrect", mass, cg.weight, EPSILON);
	}
	
    @Test
    public void testVolumeSimpleConeFilled() {

		final double length = 1.0;
		final double aftRadius = 1.0;
        final double density = 2.0;

        NoseCone nc = new NoseCone();
        nc.setLength(length);
        nc.setFilled(true);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setAftRadius(aftRadius);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		Coordinate expectedCG = calculateConicalTransitionCG(length, 0, aftRadius, aftRadius);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleConeWithShoulderFilled() {

		final double length = 1.0;
		final double aftRadius = 1.0;
		final double thickness = 1.0;
        final double density = 2.0;
		
        NoseCone nc = new NoseCone();
        nc.setLength(length);
        nc.setFilled(true);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setAftRadius(aftRadius);
        nc.setAftShoulderRadius(aftRadius);
        nc.setAftShoulderLength(length);
        nc.setAftShoulderThickness(aftRadius);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate coneCG = calculateConicalTransitionCG(length, 0, aftRadius, aftRadius);
		final Coordinate shoulderCG = calculateShoulderCG(length, length, aftRadius, aftRadius);
		final Coordinate expectedCG = coneCG.average(shoulderCG);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleConeHollow() {

		final double length = 1.0;
		final double aftRadius = 1.0;
		final double thickness = 0.5;
        final double density = 2.0;

        NoseCone nc = new NoseCone();
        nc.setLength(length);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		Coordinate expectedCG = calculateConicalTransitionCG(length, 0.0, aftRadius, thickness);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleConeWithShoulderHollow() {

		final double aftRadius = 1.0;
		final double length = 1.0;
		final double thickness = 0.5;
        final double density = 2.0;
		
        NoseCone nc = new NoseCone();
        nc.setLength(length);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setAftShoulderRadius(aftRadius);
        nc.setAftShoulderLength(length);
        nc.setAftShoulderThickness(thickness);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate expectedConeCG = calculateConicalTransitionCG(length, 0, aftRadius, thickness);
		final Coordinate expectedShoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);
		final Coordinate expectedCG = expectedConeCG.average(expectedShoulderCG);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionFilled() {
		final double length = 4.0;
		final double foreRadius = 1.0;
		final double aftRadius = 2.0;
        final double density = 2.0;
		
        Transition nc = new Transition();
        nc.setLength(length);
        nc.setFilled(true);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, aftRadius);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionWithShouldersFilled() {

		final double transLength = 4.0;
		final double foreRadius = 1.0;
		final double foreShoulderLength = 1.0;
		final double aftRadius = 2.0;
		final double aftShoulderLength = 1.0;
        final double density = 2.0;

        Transition nc = new Transition();
        nc.setLength(transLength);
        nc.setFilled(true);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setAftShoulderLength(aftShoulderLength);
        nc.setAftShoulderRadius(aftRadius);
        nc.setAftShoulderThickness(aftRadius);
        nc.setForeShoulderLength(foreShoulderLength);
        nc.setForeShoulderRadius(foreRadius);
        nc.setForeShoulderThickness(foreRadius);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate foreShoulderCG = calculateShoulderCG(-foreShoulderLength, foreShoulderLength, foreRadius, foreRadius);
		final Coordinate transCG = calculateConicalTransitionCG(transLength, foreRadius, aftRadius, aftRadius);
		final Coordinate aftShoulderCG = calculateShoulderCG(transLength, aftShoulderLength, aftRadius, aftRadius);
		final Coordinate expectedCG = combineCG(foreShoulderCG, transCG, aftShoulderCG);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionHollow1() {

		final double length = 1.0;
		final double foreRadius = 0.5;
		final double aftRadius = 0.5;
		final double thickness = 0.5;
        final double density = 2.0;

        Transition nc = new Transition();
        nc.setLength(length);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));
		
		final Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionWithShouldersHollow1() {
		final double length = 1.0; // length of transition itself and each shoulder
		final double foreRadius = 0.5;
		final double aftRadius = 1.0;
		final double thickness = 0.5;
        final double density = 2.0;
		
        Transition nc = new Transition();
        nc.setLength(length);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setAftShoulderLength(length);
        nc.setAftShoulderRadius(aftRadius);
        nc.setAftShoulderThickness(thickness);
        nc.setForeShoulderLength(length);
        nc.setForeShoulderRadius(foreRadius);
        nc.setForeShoulderThickness(thickness); // note this means fore shoulder is filled.
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate foreShoulderCG = calculateShoulderCG(-length, length, foreRadius, thickness);
		final Coordinate transitionCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);
		final Coordinate aftShoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);
		final Coordinate expectedCG = combineCG(foreShoulderCG, transitionCG, aftShoulderCG);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionHollow2() {
		final double length = 1.0; // length of transition itself and each shoulder
		final double foreRadius = 0.5;
		final double aftRadius = 1.0;
		final double thickness = 0.25;
        final double density = 2.0;

        Transition nc = new Transition();
        nc.setLength(length);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);

		checkCG(expectedCG, nc);
    }

    @Test
    public void testVolumeSimpleTransitionWithShouldersHollow2() {
		final double length = 1.0;
		final double foreRadius = 0.5;
		final double aftRadius = 1.0;
		final double thickness = 0.25;
        final double density = 2.0;

        Transition nc = new Transition();
        nc.setLength(length);
        nc.setShapeType(Transition.Shape.CONICAL);
        nc.setForeRadius(foreRadius);
        nc.setAftRadius(aftRadius);
        nc.setThickness(thickness);
        nc.setAftShoulderLength(length);
        nc.setAftShoulderRadius(aftRadius);
		nc.setAftShoulderThickness(thickness);
        nc.setForeShoulderLength(length);
        nc.setForeShoulderRadius(foreRadius);
		nc.setForeShoulderThickness(thickness);
        nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate foreShoulderCG = calculateShoulderCG(-length, length, foreRadius, thickness);
		final Coordinate transitionCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);
		final Coordinate aftShoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);
		final Coordinate expectedCG = combineCG(foreShoulderCG, transitionCG, aftShoulderCG);

		checkCG(expectedCG, nc);
    }

	@Test
	public void testTransitionVsTubeFilled() {
		// BodyTubes use closed form solutions for mass properties, while Transitions use
		// numerical integration from SymmetricComponent. Properties should agree.
		final double radius = 1.0;
		final double length = 10.0;
		
		BodyTube bt1 = new BodyTube(length, radius, true);
		
		Transition trans1 = new Transition();
		trans1.setFilled(true);
		trans1.setLength(length);
		trans1.setForeRadius(radius, true);
		trans1.setAftRadius(radius, true);
		trans1.setShapeType(Transition.Shape.CONICAL);

		assertEquals("Length is incorrect", bt1.getLength(), trans1.getLength(), EPSILON);
		assertEquals("Forward radius is incorrect", bt1.getRadius(0), trans1.getRadius(0), EPSILON);
		assertEquals("Aft radius is incorrect", bt1.getRadius(bt1.getLength()), trans1.getRadius(trans1.getLength()), EPSILON);
		assertEquals("Volume is incorrect", bt1.getComponentVolume(), trans1.getComponentVolume(), EPSILON);
		assertEquals("CG is incorrect", bt1.getComponentCG().x, trans1.getComponentCG().x, EPSILON);
		assertEquals("Longitudinal moment of inertia is incorrect", bt1.getLongitudinalUnitInertia(), trans1.getLongitudinalUnitInertia(), EPSILON);
		assertEquals("Rotational moment of inertia is incorrect", bt1.getRotationalUnitInertia(), trans1.getRotationalUnitInertia(), EPSILON);
		assertEquals("Wetted area is incorrect", bt1.getComponentWetArea(), trans1.getComponentWetArea(), EPSILON);
		assertEquals("Planform area is incorrect", bt1.getComponentPlanformArea(), trans1.getComponentPlanformArea(), EPSILON);
		assertEquals("Planform centroid is incorrect", bt1.getComponentPlanformCenter(), trans1.getComponentPlanformCenter(), EPSILON);

	}
	
	@Test
	public void testTransitionVsTubeHollow() {
		final double radius = 1.0;
		final double innerRadius = 0.1;
		final double length = 10.0;
		
		BodyTube bt2 = new BodyTube(length, radius, false);
		bt2.setInnerRadius(innerRadius);

		Transition trans2 = new Transition();
		trans2.setFilled(false);
		trans2.setLength(length);
		trans2.setForeRadius(radius, true);
		trans2.setAftRadius(radius, true);
		trans2.setShapeType(Transition.Shape.CONICAL);
		trans2.setThickness(radius - innerRadius, true);		
		
		assertEquals("Volume is incorrect", bt2.getComponentVolume(), trans2.getComponentVolume(), EPSILON);
		assertEquals("CG is incorrect", bt2.getComponentCG().x, trans2.getComponentCG().x, EPSILON);
		assertEquals("Longitudinal unit moment of inertia is incorrect", bt2.getLongitudinalUnitInertia(), trans2.getLongitudinalUnitInertia(), EPSILON);
		assertEquals("Rotational unit moment of inertia is incorrect", bt2.getRotationalUnitInertia(), trans2.getRotationalUnitInertia(), EPSILON);
		assertEquals("Wetted area is incorrect", bt2.getComponentWetArea(), trans2.getComponentWetArea(), EPSILON);
		assertEquals("Planform area is incorrect", bt2.getComponentPlanformArea(), trans2.getComponentPlanformArea(), EPSILON);
		assertEquals("Planform centroid is incorrect", bt2.getComponentPlanformCenter(), trans2.getComponentPlanformCenter(), EPSILON);
	}
}
