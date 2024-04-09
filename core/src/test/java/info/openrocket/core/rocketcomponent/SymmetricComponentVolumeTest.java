package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import info.openrocket.core.material.Material;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import static info.openrocket.core.util.MathUtil.pow2;

import org.junit.jupiter.api.Test;

public class SymmetricComponentVolumeTest extends BaseTestCase {
	final double EPSILON = MathUtil.EPSILON * 1000;

	// helper functions

	// project thickness onto yz plane to get height of frustrum wall
	private double getHeight(double length, double foreRadius, double aftRadius, double thickness) {
		final double angle = Math.atan((aftRadius - foreRadius) / length);
		return thickness / Math.cos(angle);
	}

	// return Coordinate containing CG and volume of (possibly hollow if thickness <
	// outerR) shoulder
	private Coordinate calculateShoulderCG(double x1, double length, double outerR, double thickness) {
		final double cg = x1 + length / 2.0;

		final double innerR = Math.max(0.0, outerR - thickness);
		final double volume = Math.PI * length * (pow2(outerR) - pow2(innerR));

		return new Coordinate(cg, 0, 0, volume);
	}

	// return Coordinate containing CG and volume of frustum
	// still OK if forward radius is 0 (ie a cone)
	private Coordinate calculateFrustumCG(double length, double foreRadius, double aftRadius) {
		final double moment = Math.PI * pow2(length)
				* (pow2(foreRadius) + 2.0 * foreRadius * aftRadius + 3.0 * pow2(aftRadius)) / 12.0;
		final double volume = Math.PI * length * (pow2(foreRadius) + foreRadius * aftRadius + pow2(aftRadius)) / 3.0;

		return new Coordinate(moment / volume, 0, 0, volume);
	}

	// return Coordinate containing CG and volume of conical transition
	private Coordinate calculateConicalTransitionCG(double length, double foreRadius, double aftRadius,
													double thickness) {
		// get moment and volume of outer frustum
		final Coordinate fullCG = calculateFrustumCG(length, foreRadius, aftRadius);

		final double height = getHeight(length, foreRadius, aftRadius, thickness);

		// if aftRadius <= height the transition is filled and we don't need to mess
		// with
		// the inner frustum
		if (aftRadius <= height) {
			return fullCG;
		}

		double innerLen = length;
		double innerForeRad = foreRadius - height;
		final double innerAftRad = aftRadius - height;

		// if inner forward radius <= 0 the transition is a cone; we
		// need to determine its length (if it's exactly equal the inner length is the
		// same as the outer)
		if (innerForeRad < 0) {
			innerLen = length * (aftRadius - height) / (aftRadius - foreRadius);
			innerForeRad = 0;
		}

		final Coordinate innerCG = calculateFrustumCG(innerLen, innerForeRad, innerAftRad);

		// subtract inner from outer
		final double offset = length - innerLen;
		final double moment = fullCG.x * fullCG.weight - (innerCG.x + offset) * innerCG.weight;
		final double volume = fullCG.weight - innerCG.weight;

		return new Coordinate(moment / volume, 0, 0, volume);
	}

	// combine five CGs (typically forward shoulder cap, forward shoulder,
	// transition, aft shoulder, and aft shoulder cap)
	// Use Coordinate.ZERO as a CG to combine fewer than five
	private Coordinate combineCG(Coordinate cg1, Coordinate cg2, Coordinate cg3, Coordinate cg4, Coordinate cg5) {
		final double moment1 = cg1.x * cg1.weight;
		final double moment2 = cg2.x * cg2.weight;
		final double moment3 = cg3.x * cg3.weight;
		final double moment4 = cg4.x * cg4.weight;
		final double moment5 = cg5.x * cg5.weight;

		final double volume = cg1.weight + cg2.weight + cg3.weight + cg4.weight + cg5.weight;
		return new Coordinate((moment1 + moment2 + moment3 + moment4 + moment5) / volume, 0, 0, volume);
	}

	// check CG, volume, mass
	private void checkCG(Coordinate expectedCG, Transition nc) {

		Coordinate cg = nc.getCG();
		assertEquals(expectedCG.x, cg.x, EPSILON, "CG is incorrect");
		assertEquals(expectedCG.weight, nc.getComponentVolume(), EPSILON, "Volume is incorrect");

		final double mass = expectedCG.weight * nc.getMaterial().getDensity();
		assertEquals(mass, nc.getMass(), EPSILON, "Mass is incorrect");
		assertEquals(mass, cg.weight, EPSILON, "Mass (stored in cg.weight) is incorrect");
	}

	// calculate longitudinal moment of inertia of shoulder.
	// axis through center of mass of component
	private double calculateShoulderLongitudinalMOI(double length, double outerRad, double thickness, double cgShift) {
		final double innerRad = Math.max(0.0, outerRad - thickness);
		final double volume = length * Math.PI * (pow2(outerRad) - pow2(innerRad));
		final double moi = volume * (3 * (pow2(outerRad) + pow2(innerRad)) + pow2(length)) / 12.0;

		// parallel axis theorem
		return moi + volume * pow2(cgShift);
	}

	// longitudinal MOI of a cone
	// axis is through point of cone
	private double calculateLongMOICone(double h, double r) {
		final double m = Math.PI * pow2(r) * h / 3.0;
		final double unitIyy = 3.0 * pow2(r) / 20.0 + 3.0 * pow2(h) / 5.0;
		final double Iyy = m * unitIyy;

		return Iyy;
	}

	// calculate the longitudinal moment of inertia of a solid conical frustum
	// computes by calculating for two cones, and subtracting
	// still OK if forward radius is 0 (ie a cone)
	// axis is at forward end of frustum
	private double calculateFrustumLongMOI(double length, double forwardRad, double aftRad) {

		// Find the heights of the two cones.
		final double h2 = length * aftRad / (aftRad - forwardRad);
		final double h1 = h2 * forwardRad / aftRad;

		final double moi1 = calculateLongMOICone(h1, forwardRad);
		final double moi2 = calculateLongMOICone(h2, aftRad);

		// compute MOI relative to tip of cones (they share the same tip, of course)
		double moi = moi2 - moi1;

		// use parallel axis theorem to move MOI to be relative to CG of frustum
		final Coordinate cg = calculateFrustumCG(length, forwardRad, aftRad);
		moi = moi - pow2(cg.x + h1) * cg.weight;

		// move MOI to be relative to back surface of frustum
		moi = moi + pow2(length - cg.x) * cg.weight;

		return moi;
	}

	// calculates the longitudinal moment of of inertia of a (possibly hollow)
	// conical frustum.
	// axis is through CG of frustrum
	private double calculateConicalTransitionLongitudinalMOI(double length, double foreRadius, double aftRadius,
															 double thickness, Coordinate cg, double cgShift) {

		// get MOI of outer frustum, axis through base (aft end) of frustum
		final double fullMOI = calculateFrustumLongMOI(length, foreRadius, aftRadius);

		final double height = getHeight(length, foreRadius, aftRadius, thickness);

		// if frustum is solid, MOI of inner part is 0. If not, we need to calculate it
		double moi;
		if (height >= aftRadius) {
			moi = fullMOI;
		} else {
			double innerLen = length;
			double innerForeRad = foreRadius - height;
			final double innerAftRad = aftRadius - height;

			// if inner forward radius <= 0 the transition is a cone; we
			// need to determine its length (if it's exactly 0 the inner length is equal to
			// the length
			if (innerForeRad < 0) {
				innerLen = length * (aftRadius - height) / (aftRadius - foreRadius);
				innerForeRad = 0;
			}

			final double innerMOI = calculateFrustumLongMOI(innerLen, innerForeRad, innerAftRad);

			moi = fullMOI - innerMOI;
		}

		// use parallel axis theorem to move moi relative to CG
		moi = moi - pow2(length - cg.x) * cg.weight;

		// now axis through component CG
		return moi + cg.weight * pow2(cgShift);
	}

	// calculate rotational moment of inertia of shoulder
	private double calculateShoulderRotationalMOI(double length, double outerRad, double thickness) {
		final double innerRad = Math.max(0.0, outerRad - thickness);

		return length * Math.PI * (Math.pow(outerRad, 4) - Math.pow(innerRad, 4)) / 2.0;
	}

	// calculate rotational moment of inertia of solid frustum
	// still OK if forward radius is 0 (ie a cone)
	private double calculateFrustumRotationalMOI(double length, double foreRadius, double aftRadius) {
		final double unitMOI = 3.0 * (Math.pow(aftRadius, 5) - Math.pow(foreRadius, 5))
				/ (10.0 * (Math.pow(aftRadius, 3) - Math.pow(foreRadius, 3)));
		final double volume = Math.PI * length * (pow2(foreRadius) + foreRadius * aftRadius + pow2(aftRadius)) / 3.0;

		return unitMOI * volume;
	}

	// calculate rotational moment of inertia of transition
	private double calculateConicalTransitionRotationalMOI(double length, double foreRadius, double aftRadius,
														   double thickness) {
		// get MOI of outer frustum
		double fullMOI = calculateFrustumRotationalMOI(length, foreRadius, aftRadius);
		final double height = getHeight(length, foreRadius, aftRadius, thickness);

		// if aftRadius <= height the transition is filled and we don't need to mess
		// with
		// the inner frustum
		if (aftRadius <= height) {
			return fullMOI;
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

		final double innerMOI = calculateFrustumRotationalMOI(innerLen, innerForeRad, innerAftRad);

		return fullMOI - innerMOI;
	}

	// Check surfaceproperties of transition
	private void checkConeSurface(double length, double foreRadius, double aftRadius, Transition trans) {
		final double planformArea = length * (foreRadius + aftRadius);
		final double planformCentroid = 2.0 * pow2(length) * (foreRadius / 6.0 + aftRadius / 3.0) / planformArea;
		final double surfaceArea = Math.PI * (foreRadius + aftRadius)
				* Math.sqrt(pow2(foreRadius - aftRadius) + pow2(length));

		assertEquals(planformArea, trans.getComponentPlanformArea(), EPSILON, "Planform Area is incorrect");
		assertEquals(planformCentroid, trans.getComponentPlanformCenter(), EPSILON, "Planform Centroid is incorrect");
		assertEquals(surfaceArea, trans.getComponentWetArea(), EPSILON, "Surface Area is incorrect");
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

		final double moi = calculateConicalTransitionLongitudinalMOI(length, 0, aftRadius, aftRadius, expectedCG, 0);
		final double expectedLongUnitMOI = moi / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double expectedRotUnitMOI = calculateConicalTransitionRotationalMOI(length, 0, aftRadius, aftRadius)
				/ expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, 0, aftRadius, nc);
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

		double transitionLongMOI = calculateConicalTransitionLongitudinalMOI(length, 0, aftRadius, aftRadius, coneCG,
				expectedCG.x - coneCG.x);
		double shoulderLongMOI = calculateShoulderLongitudinalMOI(length, aftRadius, aftRadius,
				expectedCG.x - shoulderCG.x);

		double moi = (shoulderLongMOI + transitionLongMOI) / expectedCG.weight;
		assertEquals(moi, nc.getLongitudinalUnitInertia(), EPSILON, "Longitudinal unit MOI is incorrect");

		final double coneRotMOI = calculateConicalTransitionRotationalMOI(length, 0, aftRadius, aftRadius);
		final double shoulderRotMOI = calculateShoulderRotationalMOI(length, aftRadius, aftRadius);
		final double expectedRotUnitMOI = (coneRotMOI + shoulderRotMOI) / expectedCG.weight;

		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, 0, aftRadius, nc);
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

		final double expectedLongUnitMOI = calculateConicalTransitionLongitudinalMOI(length, 0, aftRadius, thickness,
				expectedCG, 0.0) / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double expectedRotUnitMOI = calculateConicalTransitionRotationalMOI(length, 0, aftRadius, thickness)
				/ expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, 0, aftRadius, nc);
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

		final Coordinate coneCG = calculateConicalTransitionCG(length, 0, aftRadius, thickness);
		final Coordinate shoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);
		final Coordinate expectedCG = coneCG.average(shoulderCG);

		checkCG(expectedCG, nc);

		final double coneLongMOI = calculateConicalTransitionLongitudinalMOI(length, 0.0, aftRadius, thickness, coneCG,
				coneCG.x - expectedCG.x);
		final double shoulderLongMOI = calculateShoulderLongitudinalMOI(length, aftRadius, thickness,
				shoulderCG.x - expectedCG.x);

		final double longUnitMOI = (coneLongMOI + shoulderLongMOI) / expectedCG.weight;
		assertEquals(longUnitMOI, nc.getLongitudinalUnitInertia(), EPSILON, "Longitudinal unit MOI is incorrect");

		final double coneRotMOI = calculateConicalTransitionRotationalMOI(length, 0, aftRadius, thickness);
		final double shoulderRotMOI = calculateShoulderRotationalMOI(length, aftRadius, thickness);
		final double expectedRotUnitMOI = (coneRotMOI + shoulderRotMOI) / expectedCG.weight;

		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, 0, aftRadius, nc);
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

		final Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, aftRadius);

		checkCG(expectedCG, nc);

		final double expectedLongUnitMOI = calculateConicalTransitionLongitudinalMOI(length, foreRadius, aftRadius,
				aftRadius, expectedCG, 0.0) / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double expectedRotUnitMOI = calculateConicalTransitionRotationalMOI(length, foreRadius, aftRadius,
				aftRadius) / expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, foreRadius, aftRadius, nc);
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

		final Coordinate foreShoulderCG = calculateShoulderCG(-foreShoulderLength, foreShoulderLength, foreRadius,
				foreRadius);
		final Coordinate transCG = calculateConicalTransitionCG(transLength, foreRadius, aftRadius, aftRadius);
		final Coordinate aftShoulderCG = calculateShoulderCG(transLength, aftShoulderLength, aftRadius, aftRadius);
		final Coordinate expectedCG = combineCG(Coordinate.ZERO, foreShoulderCG, transCG, aftShoulderCG,
				Coordinate.ZERO);

		checkCG(expectedCG, nc);

		final double foreShoulderLongMOI = calculateShoulderLongitudinalMOI(foreShoulderLength, foreRadius, foreRadius,
				expectedCG.x - foreShoulderCG.x);
		final double transLongMOI = calculateConicalTransitionLongitudinalMOI(transLength, foreRadius, aftRadius,
				aftRadius, transCG, expectedCG.x - transCG.x);
		final double aftShoulderLongMOI = calculateShoulderLongitudinalMOI(aftShoulderLength, aftRadius, aftRadius,
				expectedCG.x - aftShoulderCG.x);

		final double expectedLongUnitMOI = (foreShoulderLongMOI + transLongMOI + aftShoulderLongMOI)
				/ expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double foreShoulderRotMOI = calculateShoulderRotationalMOI(foreShoulderLength, foreRadius, foreRadius);
		final double coneRotMOI = calculateConicalTransitionRotationalMOI(transLength, foreRadius, aftRadius,
				aftRadius);
		final double aftShoulderRotMOI = calculateShoulderRotationalMOI(aftShoulderLength, aftRadius, aftRadius);

		final double expectedRotUnitMOI = (foreShoulderRotMOI + coneRotMOI + aftShoulderRotMOI) / expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(transLength, foreRadius, aftRadius, nc);
	}

	@Test
	public void testVolumeSimpleTransitionHollow1() {
		final double length = 1.0;
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
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);

		checkCG(expectedCG, nc);

		final double expectedLongUnitMOI = calculateConicalTransitionLongitudinalMOI(length, foreRadius, aftRadius,
				thickness, expectedCG, 0.0) / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double expectedRotUnitMOI = calculateConicalTransitionRotationalMOI(length, foreRadius, aftRadius,
				thickness) / expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, foreRadius, aftRadius, nc);
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
		final Coordinate coneCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);
		final Coordinate aftShoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);

		final Coordinate expectedCG = combineCG(Coordinate.ZERO, foreShoulderCG, coneCG, aftShoulderCG,
				Coordinate.ZERO);
		checkCG(expectedCG, nc);

		final double foreShoulderLongMOI = calculateShoulderLongitudinalMOI(length, foreRadius, thickness,
				foreShoulderCG.x - expectedCG.x);
		final double coneLongMOI = calculateConicalTransitionLongitudinalMOI(length, foreRadius, aftRadius, thickness,
				coneCG, coneCG.x - expectedCG.x);
		final double aftShoulderLongMOI = calculateShoulderLongitudinalMOI(length, aftRadius, thickness,
				aftShoulderCG.x - expectedCG.x);

		final double longUnitMOI = (foreShoulderLongMOI + coneLongMOI + aftShoulderLongMOI) / expectedCG.weight;
		assertEquals(longUnitMOI, nc.getLongitudinalUnitInertia(), EPSILON, "Longitudinal unit MOI is incorrect");

		final double foreShoulderRotMOI = calculateShoulderRotationalMOI(length, foreRadius, thickness);
		final double coneRotMOI = calculateConicalTransitionRotationalMOI(length, foreRadius, aftRadius, thickness);
		final double aftShoulderRotMOI = calculateShoulderRotationalMOI(length, aftRadius, thickness);

		final double expectedRotUnitMOI = (foreShoulderRotMOI + coneRotMOI + aftShoulderRotMOI) / expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, foreRadius, aftRadius, nc);
	}

	@Test
	public void testVolumeSimpleTransitionHollow2() {

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
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate expectedCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);
		checkCG(expectedCG, nc);

		final double expectedLongUnitMOI = calculateConicalTransitionLongitudinalMOI(length, foreRadius, aftRadius,
				thickness, expectedCG, 0.0) / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal unit MOI is incorrect");

		final double expectedRotUnitMOI = calculateConicalTransitionRotationalMOI(length, foreRadius, aftRadius,
				thickness) / expectedCG.weight;
		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, foreRadius, aftRadius, nc);
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
		nc.setAftShoulderCapped(true);
		nc.setForeShoulderLength(length);
		nc.setForeShoulderRadius(foreRadius);
		nc.setForeShoulderThickness(thickness);
		nc.setForeShoulderCapped(true);
		nc.setMaterial(Material.newMaterial(Material.Type.BULK, "test", density, true));

		final Coordinate foreCapCG = calculateShoulderCG(-length, thickness, foreRadius - thickness, foreRadius);
		final Coordinate foreShoulderCG = calculateShoulderCG(-length, length, foreRadius, thickness);
		final Coordinate coneCG = calculateConicalTransitionCG(length, foreRadius, aftRadius, thickness);
		final Coordinate aftShoulderCG = calculateShoulderCG(length, length, aftRadius, thickness);
		final Coordinate aftCapCG = calculateShoulderCG(2.0 * length - thickness, thickness, aftRadius - thickness,
				aftRadius);

		final Coordinate expectedCG = combineCG(foreCapCG, foreShoulderCG, coneCG, aftShoulderCG, aftCapCG);
		checkCG(expectedCG, nc);

		final double foreCapLongMOI = calculateShoulderLongitudinalMOI(thickness, foreRadius - thickness, foreRadius,
				foreCapCG.x - expectedCG.x);
		final double foreShoulderLongMOI = calculateShoulderLongitudinalMOI(length, foreRadius, thickness,
				foreShoulderCG.x - expectedCG.x);
		final double coneLongMOI = calculateConicalTransitionLongitudinalMOI(length, foreRadius, aftRadius, thickness,
				coneCG, coneCG.x - expectedCG.x);
		final double aftShoulderLongMOI = calculateShoulderLongitudinalMOI(length, aftRadius, thickness,
				aftShoulderCG.x - expectedCG.x);
		final double aftCapLongMOI = calculateShoulderLongitudinalMOI(thickness, aftRadius - thickness, aftRadius,
				aftCapCG.x - expectedCG.x);

		final double expectedLongUnitMOI = (foreCapLongMOI + foreShoulderLongMOI + coneLongMOI + aftShoulderLongMOI
				+ aftCapLongMOI) / expectedCG.weight;
		assertEquals(expectedLongUnitMOI, nc.getLongitudinalUnitInertia(),
				EPSILON, "Longitudinal Unit MOI is incorrect");

		final double foreCapRotMOI = calculateShoulderRotationalMOI(thickness, foreRadius - thickness, foreRadius);
		final double foreShoulderRotMOI = calculateShoulderRotationalMOI(length, foreRadius, thickness);
		final double coneRotMOI = calculateConicalTransitionRotationalMOI(length, foreRadius, aftRadius, thickness);
		final double aftShoulderRotMOI = calculateShoulderRotationalMOI(length, aftRadius, thickness);
		final double aftCapRotMOI = calculateShoulderRotationalMOI(thickness, aftRadius - thickness, aftRadius);

		final double expectedRotUnitMOI = (foreCapRotMOI + foreShoulderRotMOI + coneRotMOI + aftShoulderRotMOI
				+ aftCapRotMOI) / expectedCG.weight;

		assertEquals(expectedRotUnitMOI, nc.getRotationalUnitInertia(), EPSILON, "Rotational unit MOI is incorrect");

		checkConeSurface(length, foreRadius, aftRadius, nc);
	}

	@Test
	public void testTransitionVsTubeFilled() {
		// BodyTubes use closed form solutions for mass properties, while Transitions
		// use
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

		assertEquals(bt1.getLength(), trans1.getLength(), EPSILON, "Length is incorrect");
		assertEquals(bt1.getRadius(0), trans1.getRadius(0), EPSILON, "Forward radius is incorrect");
		assertEquals(bt1.getRadius(bt1.getLength()), trans1.getRadius(trans1.getLength()),
				EPSILON, "Aft radius is incorrect");

		assertEquals(bt1.getComponentVolume(), trans1.getComponentVolume(), EPSILON, "Volume is incorrect");
		assertEquals(bt1.getComponentCG().x, trans1.getComponentCG().x, EPSILON, "CG is incorrect");

		assertEquals(bt1.getLongitudinalUnitInertia(),
				trans1.getLongitudinalUnitInertia(), EPSILON, "Longitudinal moment of inertia is incorrect");
		assertEquals(bt1.getRotationalUnitInertia(),
				trans1.getRotationalUnitInertia(), EPSILON, "Rotational unit moment of inertia is incorrect");

		assertEquals(bt1.getComponentWetArea(), trans1.getComponentWetArea(), EPSILON, "Wetted area is incorrect");
		assertEquals(bt1.getComponentPlanformArea(), trans1.getComponentPlanformArea(),
				EPSILON, "Planform area is incorrect");
		assertEquals(bt1.getComponentPlanformCenter(),
				trans1.getComponentPlanformCenter(), EPSILON, "Planform centroid is incorrect");
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

		assertEquals(bt2.getComponentVolume(), trans2.getComponentVolume(), EPSILON, "Volume is incorrect");
		assertEquals(bt2.getComponentCG().x, trans2.getComponentCG().x, EPSILON, "CG is incorrect");
		assertEquals(bt2.getLongitudinalUnitInertia(),
				trans2.getLongitudinalUnitInertia(), EPSILON, "Longitudinal unit moment of inertia is incorrect");
		assertEquals(bt2.getRotationalUnitInertia(),
				trans2.getRotationalUnitInertia(), EPSILON, "Rotational unit moment of inertia is incorrect");
		assertEquals(bt2.getComponentWetArea(), trans2.getComponentWetArea(), EPSILON, "Wetted area is incorrect");

		assertEquals(bt2.getComponentPlanformArea(), trans2.getComponentPlanformArea(),
				EPSILON, "Planform area is incorrect");
		assertEquals(bt2.getComponentPlanformCenter(),
				trans2.getComponentPlanformCenter(), EPSILON, "Planform centroid is incorrect");
	}
}
