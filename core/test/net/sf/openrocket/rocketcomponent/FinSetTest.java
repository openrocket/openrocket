package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Mass;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FinSetTest extends BaseTestCase {
	final double EPSILON = 1E-8;
	
	public Rocket createSimpleFin() {
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
		fins.setAxialOffset(Position.MIDDLE, 0.0);
		fins.setMaterial(Material.newMaterial(Material.Type.BULK, "Fin-Test-Material", 1.0, true));
		fins.setThickness(0.005); // == 5 mm
		
		body.addChild(fins);
		
		fins.setTabLength(0.00);
		
		fins.setFilletRadius(0.0);
		
		rkt.enableEvents();
		return rkt;
	}

	@Test
	public void testCGCalculation_simpleFin() {
		// test simple square fin.
		final Rocket rkt = new Rocket();
		final AxialStage stg = new AxialStage();
		rkt.addChild(stg);
		BodyTube body = new BodyTube(0.5, 0.1);
		stg.addChild(body);
		rkt.enableEvents();
		
		// This is a simple square fin with sides of 1.0.
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(1); // verify that this method return numbers for a single fin w/in a multi-fin set.
		fins.setFinShape(0.1, 0.1, 0.0, 0.1, .005);
		body.addChild(fins);
		fins.setMaterial(Material.newMaterial(Material.Type.BULK, "testMaterial", 1.0, true));
		
		assertEquals(0.01, fins.getFinWettedArea(), 0.00001);
		Coordinate coords = fins.getCG();
		assertEquals("Centroid x coordinate is wrong! ", 0.05, coords.x, EPSILON);
		assertEquals("Centroid y coordinate is wrong! ", 0.15, coords.y, EPSILON);
		
		// should still return a single-fin-wetted area
		assertEquals(0.00005, fins.getComponentVolume(), 0.0000001);
		
		{
			// this should also trigger a recalculation
			fins.setFinCount(2);

			// should still return a single-fin-wetted area
			assertEquals(0.01, fins.getFinWettedArea(), 0.00001);
			
			Coordinate newCG = fins.getCG();
			assertEquals(0.05, newCG.x, EPSILON);
			assertEquals(0.0, newCG.y, EPSILON);
		}
	}
	
	
	@Test
	public void testCGCalculations_finWithTab() {
		final Rocket rkt = createSimpleFin();
		FinSet fins = (FinSet) rkt.getChild(0).getChild(0).getChild(0);
		
		fins.setTabLength(0.02);
		fins.setTabHeight(0.02);
		fins.setTabPositionMethod(Position.MIDDLE);
		fins.setTabShift(0.0);

		assertEquals("Wetted Area does not match!", 0.0020, fins.getFinWettedArea(), EPSILON);
		
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
		final Rocket rkt = createSimpleFin();
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
		assertEquals("Body radius doesn't match: ", 0.1, body.getOuterRadius(), EPSILON);
		
		final Mass actVolume = fins.calculateFilletVolumeCentroid();
		
		assertEquals("Line volume doesn't match: ", 5.973e-07, actVolume.w, EPSILON);
		
		assertEquals("Line mass center.x doesn't match: ", 0.03, actVolume.x, EPSILON);
		assertEquals("Line mass center.y doesn't match: ", 0.101, actVolume.y, EPSILON);
		
		
		{ // and then, check that the fillet volume feeds into a correct overall CG: 
			Coordinate actCentroid = fins.getCG();
			assertEquals("Complete centroid x doesn't match: ", 0.03000, actCentroid.x, EPSILON);
			assertEquals("Complete centroid y doesn't match: ", 0.11971548, actCentroid.y, EPSILON);
		}
	}
	
	
	@Test
	public void testRelativeLocation() throws IllegalFinPointException {
		final Rocket rkt = new Rocket();
		final AxialStage stg = new AxialStage();
		rkt.addChild(stg);
		BodyTube body = new BodyTube(2.0, 0.01);
		stg.addChild(body);
		
		// Fin length = 1
		// Body Length = 2
		//          +--+
		//         /   |
		//        /    |
		//   +---+-----+---+
		FreeformFinSet fins = new FreeformFinSet();
		fins.setFinCount(1);
		Coordinate[] initPoints = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0.5, 1),
				new Coordinate(1, 1),
				new Coordinate(1, 0)
		};
		fins.setPoints(initPoints);
		body.addChild(fins);
		
		assertEquals("fin body length doesn't match: ", body.getLength(), 2.0, EPSILON);
		assertEquals("fin length doesn't match: ", fins.getLength(), 1.0, EPSILON);
		
		final Position[] pos = { Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM };
		final double[] expOffs = { 1.0, 0.0, 0.4, -0.2 };
		final double[] expPos = { 1.0, 0.5, 0.9, 0.8 };
		for (int caseIndex = 0; caseIndex < pos.length; ++caseIndex) {
			fins.setAxialOffset(pos[caseIndex], expOffs[caseIndex]);
			
			
			final double actOffset = fins.getAxialOffset();
			assertEquals(String.format(" Relative Positioning doesn't match for: (%6.2g via:%s)\n", expOffs[caseIndex], pos[caseIndex].name()),
					expOffs[caseIndex], actOffset, EPSILON);
			
			final double actXLoc = fins.getLocations()[0].x;
			assertEquals(String.format(" Top Positioning doesn't match for: (%6.2g via:%s)\n", expOffs[caseIndex], pos[caseIndex].name()),
					expPos[caseIndex], actXLoc, EPSILON);
			
			
			final double actTop = fins.asPositionValue(Position.TOP);
			assertEquals(String.format(" Top Positioning doesn't match for: (%6.2g via:%s)\n", expOffs[caseIndex], pos[caseIndex].name()),
					expPos[caseIndex], actTop, EPSILON);
		}
	}
	
	
	
	@Test
	public void testTabLocation() throws IllegalFinPointException {
		final Rocket rkt = new Rocket();
		final AxialStage stg = new AxialStage();
		rkt.addChild(stg);
		BodyTube body = new BodyTube(0.2, 0.001);
		stg.addChild(body);
		
		// Fin length = 0.025
		// Tab Length = 0.01
		//          +--+
		//         /   |
		//        /    |
		//   +---+-----+---+
		//   
		FinSet fins = new TrapezoidFinSet(1, 0.05, 0.02, 0.03, 0.025);
		fins.setName("test fins");
		fins.setAxialOffset(Position.MIDDLE, 0.0);
		body.addChild(fins);
		// length = 0.05
		assertEquals("incorrect fin length:", 0.05, fins.getLength(), EPSILON);
		fins.setTabLength(0.01);
		assertEquals("incorrect fin tab length:", 0.01, fins.getTabLength(), EPSILON);
		
		final Position[] pos = { Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM };
		final double[] expShift = { 0.01, 0.0, 0.01, -0.02 };
		final double[] expFront = { 0.01, 0.02, 0.03, 0.02 };
		final double[] expMiddle = { -0.01, 0.0, 0.01, 0.0 };
		final double[] expBottom = { -0.03, -0.02, -0.01, -0.02 };
		for (int caseIndex = 0; caseIndex < pos.length; ++caseIndex) {
			fins.setTabPositionMethod(pos[caseIndex]);
			fins.setTabShift(expShift[caseIndex]);
			double actFront = fins.getTabFrontEdge();
			double actShift = fins.getTabShift();
			
			assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
					expFront[caseIndex], actFront, EPSILON);
			
			assertEquals(String.format(" Relative Positioning doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
					expShift[caseIndex], actShift, EPSILON);
			
			fins.setTabPositionMethod(Position.TOP);
			actFront = fins.getTabFrontEdge();
			
			assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
					expFront[caseIndex], actFront, EPSILON);
			assertEquals(String.format(" Relative Positioning doesn't match when reshift to top, from " + pos[caseIndex].name()),
					expFront[caseIndex], fins.getTabShift(), EPSILON);
			
			fins.setTabPositionMethod(Position.MIDDLE);
			actShift = fins.getTabShift();
			assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
					expFront[caseIndex], actFront, EPSILON);
			assertEquals(String.format(" Relative Positioning doesn't match when reshift to middle, from " + pos[caseIndex].name()),
					expMiddle[caseIndex], fins.getTabShift(), EPSILON);
			
			fins.setTabPositionMethod(Position.BOTTOM);
			actShift = fins.getTabShift();
			assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
					expFront[caseIndex], actFront, EPSILON);
			assertEquals(String.format(" Relative Positioning doesn't match when reshift to bottom, from " + pos[caseIndex].name()),
					expBottom[caseIndex], fins.getTabShift(), EPSILON);
		}
	}
	
	
	@Test
	public void testGetTabShiftAs() {
		final Position[] method = { Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM };
		final double[] expTop = { 0.1, 0.04, 0.07, 0.06 };
		final double[] expShift = { 0.1, 0.0, 0.03, -0.02 };
		final double finLength = 0.10;
		final double tabLength = 0.02;
		
		for (int caseIndex = 0; caseIndex < method.length; ++caseIndex) {
			double actShift = Position.getShift(method[caseIndex], expTop[caseIndex], finLength, tabLength);
			assertEquals(String.format("Returned shift doesn't match for: (%6.2g via:%s)\n", expTop[caseIndex], method[caseIndex].name()),
					expShift[caseIndex], actShift, EPSILON);
			
			
			double actTop = Position.getTop(expShift[caseIndex], method[caseIndex], finLength, tabLength);
			assertEquals(String.format("Returned front doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], method[caseIndex].name()),
					expTop[caseIndex], actTop, EPSILON);
		}
	}
	
	
	@Test
	public void testTabLocationUpdate() throws IllegalFinPointException {
		final Rocket rkt = new Rocket();
		final AxialStage stg = new AxialStage();
		rkt.addChild(stg);
		BodyTube body = new BodyTube(0.2, 0.001);
		stg.addChild(body);
		
		// Fin length = 0.025
		// Tab Length = 0.01
		//          +--+
		//         /   |
		//        /    |
		//   +---+-----+---+
		//
		TrapezoidFinSet fins = new TrapezoidFinSet(1, 0.05, 0.02, 0.03, 0.025);
		fins.setAxialOffset(Position.MIDDLE, 0.0);
		body.addChild(fins);
		// fins.length = 0.05;
		fins.setTabLength(0.01);
		
		fins.setTabPositionMethod(Position.MIDDLE);
		fins.setTabShift(0.0);
		
		final double expFrontFirst = 0.02;
		final double actFrontFirst = fins.getTabFrontEdge();
		assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", fins.getTabFrontEdge(), Position.MIDDLE.name()),
				expFrontFirst, actFrontFirst, EPSILON);
		
		fins.setRootChord(0.08);
		
		final double expFrontSecond = 0.035;
		final double actFrontSecond = fins.getTabFrontEdge();
		assertEquals(" Front edge doesn't match after adjusting root chord...", expFrontSecond, actFrontSecond, EPSILON);
	}
	
	
	// arguably, this should be in a test class for TrapezoidalFinSet
	@Test
	public void testGenerateTrapezoidalPoints() {
		final Rocket rkt = createSimpleFin();
		BodyTube body = (BodyTube) rkt.getChild(0).getChild(0);
		
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
	
	public void testAreaCalculations() {
		Coordinate[] basicPoints = { new Coordinate(0.00, 0.0),
				new Coordinate(0.02, 0.05),
				new Coordinate(0.04, 0.05),
				new Coordinate(0.06, 0.0),
				new Coordinate(0.00, 0.0) };
		//
		//      [1] +--+ [2]
		//         /    \
		//        /      \
		//   [0] +--------+ [3]
		//       [4]
		//
		
		final double expArea = 0.04 * 0.05;
		final Mass actCentroid = FinSet.calculateCurveIntegral(basicPoints);
		assertEquals(" basic area doesn't match...", expArea, actCentroid.w, EPSILON);
		assertEquals(" basic centroid x doesn't match: ", 0.03000, actCentroid.x, EPSILON);
		assertEquals(" basic centroid y doesn't match: ", 0.02083, actCentroid.y, EPSILON);
	}
	
	
	
	
}
