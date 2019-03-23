package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.position.*;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class TrapezoidFinSetTest extends BaseTestCase {

	private static final double EPSILON = 1E-8;

	private Rocket createSimpleTrapezoidalFin() {
		final Rocket rkt = new Rocket();
		final AxialStage stg = new AxialStage();
		rkt.addChild(stg);
		BodyTube body = new BodyTube(0.2, 0.1);
		stg.addChild(body);
		TrapezoidFinSet fins = new TrapezoidFinSet(1, 0.06, 0.02, 0.02, 0.05);
		//
		//     sweep= 0.02 | tipChord = 0.02
		//            |    |      |
		//            |    +------+  ----------
		//            |   /        \
		//            |  /          \     height = 0.05
		//            | /            \
		//             /              \
		//  __________/________________\_____   length == rootChord == 0.06
		//                |        |
		//                |        |      tab height = 0.02
		//                |        |
		//                +--------+      tab length = 0.02
		//                    position = 0.0 via middle
		//
		//         Fin Area = 0.05 * ( (0.2 + 0.06)/2) = 0.0
		//
		fins.setAxialOffset(AxialMethod.MIDDLE, 0.0);
		fins.setMaterial(Material.newMaterial(Material.Type.BULK, "Fin-Test-Material", 1.0, true));
		fins.setThickness(0.005); // == 5 mm

		body.addChild(fins);

		fins.setTabLength(0.00);

		fins.setFilletRadius(0.0);

		rkt.enableEvents();
		return rkt;
	}

	@Test
	public void testMultiplicity() {
		final TrapezoidFinSet trapFins = new TrapezoidFinSet();
		assertEquals(1, trapFins.getFinCount());
	}

	@Test
	public void testGenerateTrapezoidalPoints() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		FinSet fins = (FinSet) rkt.getChild(0).getChild(0).getChild(0);

		// Fin length = 0.05
		// Tab Length = 0.01
		//          +--+
		//         /    \
		//        /      \
		//   +---+--------+---+
		//
		Coordinate[] actPoints = fins.getFinPoints();

		Coordinate[] expPoints = { new Coordinate(0.00, 0.0),
				new Coordinate(0.02, 0.05),
				new Coordinate(0.04, 0.05),
				new Coordinate(0.06, 0.0),
				new Coordinate(0.00, 0.0) };

		for (int index = 0; index < actPoints.length; ++index) {
			assertEquals(" generated fin point [" + index + "] doesn't match! ", expPoints[index].x, actPoints[index].x, EPSILON);
			assertEquals(" generated fin point [" + index + "] doesn't match!", expPoints[index].x, actPoints[index].x, EPSILON);
			assertEquals(" generated fin point [" + index + "] doesn't match!", expPoints[index].x, actPoints[index].x, EPSILON);
		}
	}

	@Test
	public void testCGCalculation_simpleSquareFin() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		final TrapezoidFinSet fins = (TrapezoidFinSet)rkt.getChild(0).getChild(0).getChild(0);

		// This is a simple square fin with sides of 0.1.
		fins.setFinShape(0.1, 0.1, 0.0, 0.1, .005);

		// should return a single-fin-planform area
		assertEquals("area calculation doesn't match: ", 0.01, fins.getPlanformArea(), 0.00001);

		final double expSingleMass = 0.00005;
		final Coordinate singleCG = fins.getComponentCG();
		assertEquals("Fin mass is wrong! ", expSingleMass, singleCG.weight, EPSILON);
		assertEquals("Centroid x coordinate is wrong! ", 0.05, singleCG.x, EPSILON);
		assertEquals("Centroid y coordinate is wrong! ", 0.15, singleCG.y, EPSILON);

		// should still return a single-fin-wetted area
		assertEquals(0.00005, fins.getComponentVolume(), 0.0000001);

		{ // test instancing code
			// this should also trigger a recalculation
			fins.setFinCount(2);

			// should still return a single-fin-planform area
			assertEquals(0.01, fins.getPlanformArea(), 0.00001);

			Coordinate doubleCG = fins.getComponentCG();
			final double expDoubleMass = expSingleMass*2;
			assertEquals("Fin x2 mass does not change from single fin instance! ", expDoubleMass, doubleCG.weight, EPSILON);
			assertEquals(0.05, doubleCG.x, EPSILON);
			assertEquals(0.0, doubleCG.y, EPSILON);
		}
	}


	@Test
	public void testCGCalculations_finWithTab() throws IllegalFinPointException {
		final Rocket rkt = createSimpleTrapezoidalFin();
		FinSet fins = (FinSet) rkt.getChild(0).getChild(0).getChild(0);

		fins.setTabLength(0.02);
		fins.setTabHeight(0.02);
		fins.setTabOffsetMethod(AxialMethod.MIDDLE);
		fins.setTabOffset(0.0);

		assertEquals("Wetted Area does not match!", 0.0020, fins.getPlanformArea(), EPSILON);

		final double expVol1 = 0.00001200;
		final double actVol1 = fins.getComponentVolume();
		assertEquals(" fin volume is incorrect", expVol1, actVol1, EPSILON);

		Coordinate actCentroid1 = fins.getCG();
		assertEquals(" basic centroid x doesn't match: ", 0.03000, actCentroid1.x, EPSILON);
		assertEquals(" basic centroid y doesn't match: ", 0.11569444, actCentroid1.y, EPSILON);

		{
			fins.setFinCount(2);
			final double expVol2 = expVol1 * 2;
			final double actVol2 = fins.getComponentVolume();
			assertEquals(" fin volume is incorrect", expVol2, actVol2, EPSILON);

			Coordinate actCentroid2 = fins.getCG();
			// x coordinate will be the same....
			assertEquals(" basic centroid y doesn't match: ", 0.0, actCentroid2.y, EPSILON);
		}
	}


	@Test
	public void testFilletCalculations() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		BodyTube body = (BodyTube) rkt.getChild(0).getChild(0);
		FinSet fins = (FinSet) rkt.getChild(0).getChild(0).getChild(0);

		fins.setFilletRadius(0.005);
		fins.setFilletMaterial(Material.newMaterial(Material.Type.BULK, "Fillet-Test-Material", 1.0, true));

		// used for fillet and edge calculations:
		//
		//      [1] +--+ [2]
		//         /    \
		//        /      \
		//   [0] +--------+ [3]
		//
		assertEquals(0.06, fins.getLength(), EPSILON);
		assertEquals("Body radius doesn't match: ", 0.1, body.getOuterRadius(), EPSILON);

		final Coordinate actVolume = fins.calculateFilletVolumeCentroid();

		assertEquals("Fin volume doesn't match: ", 5.973e-07, actVolume.weight, EPSILON);
		assertEquals("Fin mass center.x doesn't match: ", 0.03, actVolume.x, EPSILON);
		assertEquals("Fin mass center.y doesn't match: ", 0.101, actVolume.y, EPSILON);


		{ // and then, check that the fillet volume feeds into a correct overall CG:
			Coordinate actCentroid = fins.getCG();
			assertEquals("Complete centroid x doesn't match: ", 0.03000, actCentroid.x, EPSILON);
			assertEquals("Complete centroid y doesn't match: ", 0.11971548, actCentroid.y, EPSILON);
		}
	}

	@Test
	public void testTrapezoidCGComputation() {
		{
			// This is a simple square fin with sides of 1.0.
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(1.0, fins.getPlanformArea(), 0.001);
			assertEquals(0.5, coords.x, 0.001);
			assertEquals(0.5, coords.y, 0.001);
		}{
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
			assertEquals(0.75, fins.getPlanformArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		}
	}
	
	@Test
	public void testGetBodyPoints_phantomMount() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		
		// set mount to have zero-dimensions:
		final BodyTube mount = (BodyTube)rkt.getChild(0).getChild(0);
		mount.setLength(0.0);
		mount.setOuterRadius(0.0);
		assertEquals( 0, mount.getLength(), 0.00001);
		assertEquals( 0, mount.getOuterRadius(), 0.00001);
		assertEquals( 0, mount.getInnerRadius(), 0.00001);
		
		final TrapezoidFinSet fins = (TrapezoidFinSet)mount.getChild(0);
		final Coordinate[] mountPoints = fins.getMountPoints();
		
		assertEquals(2, mountPoints.length );
		assertEquals( 0.00, mountPoints[0].x, 0.00001);
		assertEquals( 0.00, mountPoints[0].y, 0.00001);
		assertEquals( 0.00, mountPoints[1].x, 0.00001);
		assertEquals( 0.00, mountPoints[1].y, 0.00001);
	}
	
	@Test
	public void testGetBodyPoints_zeroLengthMount() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		
		// set mount to have zero-dimensions:
		final BodyTube mount = (BodyTube)rkt.getChild(0).getChild(0);
		mount.setLength(0.0);
		mount.setOuterRadius(0.1);
		mount.setInnerRadius(0.08);
		assertEquals( 0, mount.getLength(), 0.00001);
		assertEquals( 0.1, mount.getOuterRadius(), 0.00001);
		assertEquals( 0.08, mount.getInnerRadius(), 0.00001);
		
		final TrapezoidFinSet fins = (TrapezoidFinSet)mount.getChild(0);
		final Coordinate[] mountPoints = fins.getMountPoints();
				
		assertEquals(2, mountPoints.length );
		assertEquals( 0.0, mountPoints[0].x, 0.00001);
		assertEquals( 0.1, mountPoints[0].y, 0.00001);
		assertEquals( 0.0, mountPoints[1].x, 0.00001);
		assertEquals( 0.1, mountPoints[1].y, 0.00001);
	}
	
	@Test
	public void testTrapezoidCGComputation_phantomMount() {
		final Rocket rkt = createSimpleTrapezoidalFin();
		
		// set mount to have zero-dimensions:
		final BodyTube mount = (BodyTube)rkt.getChild(0).getChild(0);
		mount.setLength(0.0);
		mount.setOuterRadius(0.0);

		assertEquals( 0, mount.getLength(), 0.00001);
		assertEquals( 0, mount.getOuterRadius(), 0.00001);
		assertEquals( 0, mount.getInnerRadius(), 0.00001);
		
		final TrapezoidFinSet fins = (TrapezoidFinSet)mount.getChild(0);
		
		assertEquals( 0.06, fins.getLength(), 0.00001);
		assertEquals( 0.05, fins.getHeight(), 0.00001);
		assertEquals( 0.06, fins.getRootChord(), 0.00001);
		assertEquals( 0.02, fins.getTipChord(), 0.00001);
		
		final Coordinate coords = fins.getCG();
		assertEquals(0.002, fins.getPlanformArea(), 0.001);
		assertEquals(0.03, coords.x, 0.001);
		assertEquals(0.02, coords.y, 0.001);
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
