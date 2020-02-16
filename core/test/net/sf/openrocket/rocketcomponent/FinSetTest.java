package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import net.sf.openrocket.material.Material;
import org.junit.Test;

import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FinSetTest extends BaseTestCase {

	private static final double EPSILON = 1E-8;

	@Test
	public void testMultiplicity() {
		final EllipticalFinSet fins = new EllipticalFinSet();
		assertEquals(3, fins.getFinCount());
	}

    /**
	 *     sweep= 0.02 | tipChord = 0.02
	 *            |    |      |
	 *            |    +------+  ----------
	 *            |   /        \
	 *            |  /          \     height = 0.05
	 *            | /            \
	 *             /              \
	 *  __________/________________\_____   length == rootChord == 0.06
	 *                |        |
	 *                |        |      tab height = 0.02
	 *                |        |
	 *                +--------+      tab length = 0.02
	 *                    position = 0.0 via middle
	 *
	 *         Fin Area = 0.05 * ( (0.2 + 0.06)/2) = 0.0
	 */
    private static FinSet createSimpleFin() {

		TrapezoidFinSet fins = new TrapezoidFinSet(1, 0.06, 0.02, 0.02, 0.05);
        fins.setName("test fins");
        fins.setAxialOffset(AxialMethod.MIDDLE, 0.0);
		fins.setMaterial(Material.newMaterial(Material.Type.BULK, "Fin-Test-Material", 1.0, true));
		fins.setThickness(0.005); // == 5 mm

		fins.setTabLength(0.02);
		fins.setTabOffsetMethod(AxialMethod.TOP);
		fins.setTabOffset(0.02);

        fins.setFilletRadius(0.0);

        fins.setAngleMethod(AngleMethod.FIXED);
        fins.setAngleOffset(Math.toRadians(90.0));

        fins.setCantAngle(Math.toRadians(3.0));

	    return fins;
    }

    @Test
    public void testAngleOffset() {
        final FinSet fins = FinSetTest.createSimpleFin();

        assertEquals("Angle Offset Doesn't match!", Math.PI/2, fins.getAngleOffset(), EPSILON);
        assertEquals("Angle Offset Doesn't match!", 90.0, Math.toDegrees(fins.getAngleOffset()), EPSILON);
        
        assertEquals("Cant angle doesn't match!", Math.PI/60, fins.getCantAngle(), EPSILON);
        assertEquals("Cant angle doesn't match!", 3.0, Math.toDegrees(fins.getCantAngle()), EPSILON);
   }

	@Test
	public void testTabLocation() {
	    final FinSet fins = FinSetTest.createSimpleFin();
		assertEquals("incorrect fin length:", 0.06, fins.getLength(), EPSILON);
		assertEquals("incorrect fin tab length:", 0.02, fins.getTabLength(), EPSILON);

		final double expFront = 0.02;
		final AxialMethod[] methods = AxialMethod.axialOffsetMethods;
		final double[] expShift = {0.02, 0.0, -0.02};
		for( int caseIndex=0; caseIndex < methods.length; ++caseIndex ){
			double actFront = fins.getTabFrontEdge();
			assertEquals(" Front edge doesn't match!", expFront, actFront, EPSILON);

			// update
			fins.setTabOffsetMethod( methods[caseIndex]);

			//query
			double actShift = fins.getTabOffset();
			assertEquals(String.format("Offset doesn't match for: %s \n", methods[caseIndex].name()), expShift[caseIndex], actShift, EPSILON);
		}
	}

    @Test
    public void testTabGetAs(){
		final FinSet fins = FinSetTest.createSimpleFin();
		assertEquals("incorrect fin length:", 0.06, fins.getLength(), EPSILON);
		assertEquals("incorrect fin tab length:", 0.02, fins.getTabLength(), EPSILON);

		// TOP -> native(TOP)
		fins.setTabOffsetMethod(AxialMethod.TOP);
		fins.setTabOffset(0.0);

		assertEquals("Setting by TOP method failed!", 0.0, fins.getTabFrontEdge(), EPSILON);
		assertEquals("Setting by TOP method failed!", 0.0, fins.getTabOffset(), EPSILON);

		// MIDDLE -> native
		fins.setTabOffsetMethod(AxialMethod.MIDDLE);
		fins.setTabOffset(0.0);
		assertEquals("Setting by TOP method failed!", 0.02, fins.getTabFrontEdge(), EPSILON);
		assertEquals("Setting by TOP method failed!", 0.0, fins.getTabOffset(), EPSILON);

		// BOTTOM -> native
		fins.setTabOffsetMethod(AxialMethod.BOTTOM);
		fins.setTabOffset(0.0);

		assertEquals("Setting by TOP method failed!", 0.04, fins.getTabFrontEdge(), EPSILON);
		assertEquals("Setting by TOP method failed!", 0.0, fins.getTabOffset(), EPSILON);
	}

	@Test
	public void testTabLocationUpdate() {
		final FinSet fins = FinSetTest.createSimpleFin();
		assertEquals("incorrect fin length:", 0.06, fins.getLength(), EPSILON);
		assertEquals("incorrect fin tab length:", 0.02, fins.getTabLength(), EPSILON);

		// TOP -> native(TOP)
		fins.setTabOffsetMethod(AxialMethod.MIDDLE);
		fins.setTabOffset(0.0);

		assertEquals("Setting by TOP method failed!", 0.0, fins.getTabOffset(), EPSILON);
		assertEquals("Setting by TOP method failed!", 0.02, fins.getTabFrontEdge(), EPSILON);

		((TrapezoidFinSet)fins).setRootChord(0.08);

		assertEquals("Front edge doesn't match after adjusting root chord...", 0.03, fins.getTabFrontEdge(), EPSILON);
		assertEquals("Offset doesn't match after adjusting root chord....", 0.0, fins.getTabOffset(), EPSILON);
	}

	@Test
	public void testAreaCalculationsSingleIncrement() {
		Coordinate[] basicPoints = {
				new Coordinate(0.00, 0.0),
				new Coordinate(0.06, 0.06),
				new Coordinate(0.06, 0.0),
				new Coordinate(0.00, 0.0) };
		//
		//      [1] +
		//         /|
		//        / |
		//   [0] +--+ [2]
		//     [3]
		//

		final double expArea = 0.06 * 0.06 * 0.5;
		final Coordinate actCentroid = FinSet.calculateCurveIntegral(basicPoints);
		assertEquals(" basic area doesn't match...", expArea, actCentroid.weight, EPSILON);
		assertEquals(" basic centroid x doesn't match: ", 0.04, actCentroid.x, 1e-8);
		assertEquals(" basic centroid y doesn't match: ", 0.02, actCentroid.y, 1e-8);
	}

	@Test
	public void testAreaCalculationsDoubleIncrement() {
		Coordinate[] basicPoints = {
				new Coordinate(0.00, 0.0),
				new Coordinate(0.06, 0.06),
				new Coordinate(0.12, 0.0),
				new Coordinate(0.00, 0.0) };
		//
		//      [1] +
		//         / \
		//        /   \
		//   [0] +-----+ [2]
		//     [3]
		//

		final double expArea = 0.06 * 0.12 * 0.5;
		final Coordinate actCentroid = FinSet.calculateCurveIntegral(basicPoints);
		assertEquals(" basic area doesn't match...", expArea, actCentroid.weight, EPSILON);
		assertEquals(" basic centroid x doesn't match: ", 0.06, actCentroid.x, 1e-8);
		assertEquals(" basic centroid y doesn't match: ", 0.02, actCentroid.y, 1e-8);
	}


	@Test
	public void testAreaCalculations() {
		Coordinate[] basicPoints = {
				new Coordinate(0.00, 0.0),
				new Coordinate(0.02, 0.05),
				new Coordinate(0.04, 0.05),
				new Coordinate(0.06, 0.0),
				new Coordinate(0.00, 0.0) };
		/*
		 *      [1] +--+ [2]
		 *         /    \
		 *        /      \
		 *   [0] +--------+ [3]
		 *       [4]
		 */
		final double expArea = 0.04 * 0.05;
		final Coordinate actCentroid = FinSet.calculateCurveIntegral(basicPoints);
		assertEquals(" basic area doesn't match...", expArea, actCentroid.weight, EPSILON);
		assertEquals(" basic centroid x doesn't match: ", 0.03000, actCentroid.x, 1e-8);
		assertEquals(" basic centroid y doesn't match: ", 0.020833333, actCentroid.y, 1e-8);
	}

	@Test
	public void testFinInstanceAngles() {
		FinSet fins = createSimpleFin();
	    fins.setBaseRotation(Math.PI/6);  // == 30d
		fins.setInstanceCount(3);         // == 120d between each fin
                                          // => [ 30, 150, 270 ] 
		                                  // => PI*[ 1/6, 5/6, 9/6 ]
		                                  // => [ .523, 2.61, 4.71 ]

		final double[] instanceAngles = fins.getInstanceAngles();
		assertEquals( (1./6.)* Math.PI, fins.getBaseRotation(), EPSILON);
		assertEquals( (1./6.)* Math.PI, fins.getAngleOffset(), EPSILON);
		
		assertEquals((1./6.)* Math.PI, instanceAngles[0], EPSILON);
		assertEquals((5./6.)* Math.PI, instanceAngles[1], EPSILON);
		assertEquals((9./6.)* Math.PI, instanceAngles[2], EPSILON);
	}


}
