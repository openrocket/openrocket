package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.Transformation;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FreeformFinSetTest extends BaseTestCase {

	private static final double EPSILON = 1E-6;

	private FreeformFinSet testFreeformConvert(FinSet sourceSet) {
		sourceSet.setName("test-convert-finset");
		sourceSet.setBaseRotation(1.1);
		sourceSet.setCantAngle(0.001);
		sourceSet.setCGOverridden(true);
		sourceSet.setColor(Color.BLACK);
		sourceSet.setComment("cmt");
		sourceSet.setCrossSection(CrossSection.ROUNDED);
		sourceSet.setFinCount(5);

		if( EllipticalFinSet.class.isAssignableFrom(sourceSet.getClass())){
			((EllipticalFinSet)sourceSet).setLength(0.1);
		}else if( TrapezoidFinSet.class.isAssignableFrom(sourceSet.getClass())){
			((TrapezoidFinSet)sourceSet).setRootChord(0.1);
		}

		sourceSet.setFinish(Finish.ROUGH);
		sourceSet.setLineStyle(LineStyle.DASHDOT);
		sourceSet.setMassOverridden(true);
		sourceSet.setMaterial(Material.newMaterial(Type.BULK, "test-material", 0.1, true));
		sourceSet.setOverrideCGX(0.012);
		sourceSet.setOverrideMass(0.0123);
		sourceSet.setOverrideSubcomponents(true);
		sourceSet.setAxialOffset(0.1);
		sourceSet.setAxialMethod(AxialMethod.ABSOLUTE);
		sourceSet.setTabHeight(0.01);
		sourceSet.setTabLength(0.02);
		sourceSet.setTabOffsetMethod(AxialMethod.BOTTOM);
		sourceSet.setTabOffset(-0.015);
		sourceSet.setThickness(0.005);

		return FreeformFinSet.convertFinSet( sourceSet);
	}

	private Rocket createTemplateRocket(){
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		NoseCone nose = new NoseCone();
		nose.setForeRadius(0.0);
		nose.setLength(1.0);
		nose.setAftRadius(1.0);
		nose.setType( Shape.ELLIPSOID );
		nose.setShapeParameter(0.5);
		nose.setName("Nose Fairing");
		stage.addChild(nose);

		BodyTube body = new BodyTube(2.0,1.0,0.01);
		body.setName("Body Tube");
		stage.addChild(body);

		Transition tail = new Transition();
		tail.setType(Shape.CONICAL);
		tail.setForeRadius(1.0);
		tail.setLength(1.0);
		tail.setAftRadius(0.5);
		// slope = .5/1.0 = 0.5
		tail.setName("Tail Cone");
		stage.addChild(tail);

		// zero-length body tube -- triggers a whole other class of errors
		final BodyTube phantom = new BodyTube(0., 0.5, 0.01);
		phantom.setName("Phantom Body Tube");
		body.setOuterRadiusAutomatic(true);
		stage.addChild(phantom);

		rocket.enableEvents();
		return rocket;
	}

	private FreeformFinSet createFinOnEllipsoidNose(NoseCone nose){
		FreeformFinSet fins = new FreeformFinSet();
		fins.setName("test-freeform-finset");
		fins.setFinCount(1);
		fins.setAxialOffset( AxialMethod.TOP, 0.02);
		final Coordinate[] points = {
			new Coordinate( 0.0, 0.0),
			new Coordinate( 0.4, 1.0),
			new Coordinate( 0.6, 1.0),
			new Coordinate( 0.8, 0.788)   // y-value should be automatically adjusted to snap to body
		};
		fins.setPoints(points);
		nose.addChild(fins);
		
		return fins;
	}

	private FreeformFinSet createFinOnTube(final BodyTube body){
		// This is a trapezoid:
		//   - Height: 1
		//   - Root Chord: 1
		//   - Tip Chord: 1/2
		//   - Sweep: 1/2
		// It can be decomposed into a triangle followed by a rectangle
		//     +--+
		//    /.  |
		//   / .  |
		//  +=====+
		FreeformFinSet fins = new FreeformFinSet();
		fins.setName("TubeBodyFins");
		fins.setFinCount(1);
		fins.setPoints(new Coordinate[]{
				new Coordinate(0, 0),
				new Coordinate(0.5, 1),
				new Coordinate(1, 1),
				new Coordinate(1, 0)
		});
		fins.setAxialOffset( AxialMethod.BOTTOM, 0.0);

		body.addChild(fins);
		
		return fins;
	}

	private FreeformFinSet createFinOnConicalTransition(final Transition body) {
        //            ----+ (1)
		//  (0)  -----    |
		//  ---+          |
		//       -----    |
		//            ----+ (2)
		FreeformFinSet fins = new FreeformFinSet();
		fins.setName("test-freeform-finset");
		fins.setFinCount(1);
		fins.setThickness(0.005);
		fins.setAxialOffset( AxialMethod.TOP, 0.4);
		Coordinate[] initPoints = new Coordinate[] {
				new Coordinate( 0.0, 0.0),
				new Coordinate( 0.4, 0.2),
				new Coordinate( 0.4,-0.2)
		};
		fins.setPoints(initPoints);

		body.addChild(fins);
		
		return fins;
	}

	// ==================== Test Methods ====================
	@Test
	public void testConvertEllipticalToFreeform() {
		final FreeformFinSet finSet = testFreeformConvert(new EllipticalFinSet());

		assertEquals( finSet.getName(), "test-convert-finset");

		assertEquals(1.1, finSet.getBaseRotation(), EPSILON);
		assertEquals(0.001, finSet.getCantAngle(), EPSILON);
		assertTrue(finSet.isCGOverridden());
		assertTrue(finSet.isMassOverridden());
		assertEquals(Color.BLACK, finSet.getColor());
		assertEquals("cmt", finSet.getComment());
		assertEquals(CrossSection.ROUNDED, finSet.getCrossSection());
		assertEquals(5, finSet.getFinCount());
		assertEquals(Finish.ROUGH, finSet.getFinish());
		assertEquals(LineStyle.DASHDOT, finSet.getLineStyle());
		{
			final Material mat = finSet.getMaterial();
			assertEquals(Type.BULK, mat.getType());
			assertEquals("test-material", mat.getName());
			assertEquals(0.1, mat.getDensity(), EPSILON);
			assertTrue(mat.isUserDefined());
		}
		assertEquals(0.012, finSet.getOverrideCGX(), EPSILON);
		assertEquals(0.0123, finSet.getOverrideMass(), EPSILON);
		assertTrue(finSet.getOverrideSubcomponents());

		assertEquals(AxialMethod.ABSOLUTE, finSet.getAxialMethod());
		assertEquals(0.1, finSet.getAxialOffset(), EPSILON);
		assertEquals(0.01, finSet.getTabHeight(), EPSILON);
		assertEquals( 0.02, finSet.getTabLength(), EPSILON);
		assertEquals( AxialMethod.BOTTOM, finSet.getTabOffsetMethod());
		assertEquals( -0.015, finSet.getTabOffset(), EPSILON);
		assertEquals( 0.005, finSet.getThickness(), EPSILON);
	}

	@Test
	public void testConvertTrapezoidToFreeform() {
		final FreeformFinSet finSet = testFreeformConvert(new TrapezoidFinSet());

		assertEquals( finSet.getName(), "test-convert-finset");

		assertEquals(1.1, finSet.getBaseRotation(), EPSILON);
		assertEquals(0.001, finSet.getCantAngle(), EPSILON);
		assertTrue(finSet.isCGOverridden());
		assertTrue(finSet.isMassOverridden());
		assertEquals(Color.BLACK, finSet.getColor());
		assertEquals("cmt", finSet.getComment());
		assertEquals(CrossSection.ROUNDED, finSet.getCrossSection());
		assertEquals(5, finSet.getFinCount());
		assertEquals(Finish.ROUGH, finSet.getFinish());
		assertEquals(LineStyle.DASHDOT, finSet.getLineStyle());
		{
			final Material mat = finSet.getMaterial();
			assertEquals(Type.BULK, mat.getType());
			assertEquals("test-material", mat.getName());
			assertEquals(0.1, mat.getDensity(), EPSILON);
			assertTrue(mat.isUserDefined());
		}
		assertEquals(0.012, finSet.getOverrideCGX(), EPSILON);
		assertEquals(0.0123, finSet.getOverrideMass(), EPSILON);
		assertTrue(finSet.getOverrideSubcomponents());

		assertEquals(AxialMethod.ABSOLUTE, finSet.getAxialMethod());
		assertEquals(0.1, finSet.getAxialOffset(), EPSILON);
		assertEquals(0.01, finSet.getTabHeight(), EPSILON);
		assertEquals( 0.02, finSet.getTabLength(), EPSILON);
		assertEquals( AxialMethod.BOTTOM, finSet.getTabOffsetMethod());
		assertEquals( -0.015, finSet.getTabOffset(), EPSILON);
		assertEquals( 0.005, finSet.getThickness(), EPSILON);

	}

	@Test
	public void testFreeformCMComputation_trapezoidOnTube() {
		final Rocket rkt = createTemplateRocket();
		final BodyTube body = (BodyTube)rkt.getChild(0).getChild(1);
		final FreeformFinSet fins = createFinOnTube(body);

		// assert pre-condition:
		final Coordinate[] finPoints = fins.getFinPoints();
		assertEquals(4, finPoints.length);
		assertEquals(finPoints[0], Coordinate.ZERO);
		assertEquals(finPoints[1], new Coordinate(0.5, 1.0));
		assertEquals(finPoints[2], new Coordinate(1.0, 1.0));
		assertEquals(finPoints[3], new Coordinate(1.0, 0.0));

		final double x0 = fins.getAxialFront();
		assertEquals(1.0, x0, EPSILON);
		assertEquals(1.0, body.getRadius(x0), EPSILON);

		// NOTE: this will be relative to the center of the finset -- which is at the center of it's mounted body
		final Coordinate coords = fins.getCG();
		assertEquals(0.75, fins.getPlanformArea(), EPSILON);
		assertEquals(0.611111, coords.x, EPSILON);
		assertEquals(1.444444, coords.y, EPSILON);
	}

	@Test
	public void testFreeformCMComputation_triangleOnTransition(){
		Rocket rkt = createTemplateRocket();
		final Transition finMount = (Transition)rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = createFinOnConicalTransition(finMount);

		// assert pre-condition:
		final Coordinate[] finPoints = fins.getFinPoints();
		assertEquals(3, finPoints.length);
		assertEquals(finPoints[0], Coordinate.ZERO);
		assertEquals(finPoints[1], new Coordinate(0.4, 0.2));
		assertEquals(finPoints[2], new Coordinate(0.4, -0.2));

		final double x0 = fins.getAxialFront();
		assertEquals(0.4, x0, EPSILON);
		assertEquals(0.8, finMount.getRadius(x0), EPSILON);

		// vv Test target vv
		final Coordinate coords = fins.getCG();
		// ^^ Test target ^^

		// in fin-mount frame coordinates
		final double expectedWettedArea = 0.08;
		assertEquals(expectedWettedArea, fins.getPlanformArea(), EPSILON);
		assertEquals(0.266666, coords.x, EPSILON);
		assertEquals(0.8, coords.y, EPSILON);

		{ // now, add a tab
			fins.setTabOffsetMethod(AxialMethod.TOP);
			fins.setTabOffset(0.1);
			fins.setTabHeight(0.2);
			fins.setTabLength(0.2);

			// fin is a simple trapezoid against a linearly changing body...
			// height is set s.t. the tab trailing edge height == 0
			final double tabLength = fins.getFinFront().x + fins.getTabOffset();
			final double expectedTabArea = (fins.getTabHeight()) * 0.5 * tabLength;
			final double expectedTotalVolume = (expectedWettedArea + expectedTabArea) * fins.getThickness();
			assertEquals("Calculated fin volume is wrong: ", expectedTotalVolume, fins.getComponentVolume(), EPSILON);

			Coordinate tcg = fins.getCG(); // relative to parent.  also includes fin tab CG.
			assertEquals("Calculated fin centroid is wrong! ", 0.238461, tcg.x, EPSILON);
			assertEquals("Calculated fin centroid is wrong! ", 0.714102, tcg.y, EPSILON);
		}
	}


	@Test
	public void testFreeformCMComputation_triangleOnEllipsoid(){
		final Rocket rkt = createTemplateRocket();
		final NoseCone nose = (NoseCone) rkt.getChild(0).getChild(0);
		final FinSet fins = createFinOnEllipsoidNose(nose);

		{  // assert preconditions::Mount
				assertEquals(Shape.ELLIPSOID, nose.getType());
				assertEquals(1.0, nose.getLength(), EPSILON);

		}{ // Assert fin shape
			//              [1]       [2]
			//                 +======+
			//               /          \   [3]
			//            /            ---+----
			//         /       --------
			//  [0] /  --------
			// ---+----
			//
			//  [0]  ( 0.0, 0.0)
			//  [1]  ( 0.4, 1.0)
			//  [2]  ( 0.6, 1.0)
			//  [3]  ( 0.8, 0.7847)

			assertEquals(AxialMethod.TOP, fins.getAxialMethod());
			assertEquals(0.02, fins.getAxialOffset(), EPSILON);
			assertEquals(0.8, fins.getLength(), EPSILON);

			final Coordinate[] finPoints = fins.getFinPoints();
			assertEquals(4, finPoints.length);
			assertEquals(Coordinate.ZERO, finPoints[0]);
			assertEquals(new Coordinate(0.4, 1.0), finPoints[1]);
			assertEquals(new Coordinate(0.6, 1.0), finPoints[2]);
			assertEquals(new Coordinate(0.8, 0.78466912), finPoints[3]);
		}

		final double expectedPlanformArea = 0.13397384;
		final double actualPlanformArea = fins.getPlanformArea();
		assertEquals("Calculated fin planform area is wrong: ", expectedPlanformArea, actualPlanformArea, EPSILON);

		Coordinate wcg = fins.getCG(); // relative to parent
		assertEquals("Calculated fin weight is wrong! ",   0.2733066, wcg.weight, EPSILON);
        assertEquals("Calculated fin centroid is wrong! ", 0.4793588, wcg.x, EPSILON);
        assertEquals("Calculated fin centroid is wrong! ", 0.996741, wcg.y, EPSILON);
	}


	@Test
	public void testFreeformCMComputationTrapezoidExtraPoints() {
		final Rocket rkt = createTemplateRocket();
		final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
		final FreeformFinSet fins = createFinOnTube(body);
		
		// This is the same trapezoid as previous free form, but it has
		// some extra points along the lines.
		Coordinate[] points = new Coordinate[]{
				new Coordinate(0.0, 0.0), // original
				new Coordinate(0.5, 1.0), // original
				new Coordinate(0.6, 1.0),
				new Coordinate(0.8, 1.0),
				new Coordinate(1.0, 1.0), // original
				new Coordinate(1.0, 0.6),
				new Coordinate(1.0, 0.0)  // original
		};
		fins.setPoints(points);

		Coordinate coords = fins.getCG();
		assertEquals(0.75, fins.getPlanformArea(), EPSILON);
		assertEquals(0.611111, coords.x, EPSILON);
		assertEquals(1.444444, coords.y, EPSILON);
	}
	
	@Test
	public void testFreeformCMComputationAdjacentPoints() {
		Rocket rkt = createTemplateRocket();
		final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
		final FreeformFinSet fins = createFinOnTube(body);

		// This is the same trapezoid as previous free form, but it has
		// some extra points which are very close to previous points.
		// in particular for points 0 & 1,
		// y0 + y1 is very small.
		final double PERMUTATION = 1e-15;
		Coordinate[] points = new Coordinate[] {
				new Coordinate(0.0, 0.0), // original
				new Coordinate(0, PERMUTATION),
				new Coordinate(0.5, 1.0), // original
				new Coordinate(0.5 + PERMUTATION, 1.0),
				new Coordinate(1.0, 1.0), // original
				new Coordinate(1.0, PERMUTATION),
				new Coordinate(1.0, 0.0)  // original
		};
		fins.setPoints(points);

		Coordinate coords = fins.getCG();
		assertEquals(0.75, fins.getPlanformArea(), EPSILON);
		assertEquals(0.611111, coords.x, EPSILON);
		assertEquals(1.444444, coords.y, EPSILON);
	}

	@Test
	public void testFreeformFinAddPoint() {
		Rocket rkt = createTemplateRocket();
		final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
		final FreeformFinSet fin = createFinOnTube(body);

		assertEquals(4, fin.getPointCount());
		
		//     +--+
		//    /   |x
		//   /    |
		//  +=====+
		Point2D.Double toAdd = new Point2D.Double(1.01, 0.8);
		fin.addPoint(3, toAdd);
		
        assertEquals(5, fin.getPointCount());
        final Coordinate added = fin.getFinPoints()[3];
        assertEquals(1.1,added.x, 0.1);
        assertEquals(0.8, added.y, 0.1);
	}

    @Test
    public void testSetFirstPoint() {
    	// more transitions trigger more complicated positioning math:  
		final Rocket rkt = createTemplateRocket();
		final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = createFinOnConicalTransition(tailCone);
        final Coordinate[] initialPoints = fins.getFinPoints();
        
		// assert pre-conditions:
		assertEquals(0.4, fins.getLength(), EPSILON);
        assertEquals(initialPoints[0], Coordinate.ZERO);
        assertEquals(initialPoints[1], new Coordinate(0.4, 0.2));
        assertEquals(initialPoints[2], new Coordinate(0.4, -0.2));
        assertEquals(1.0, tailCone.getLength(), EPSILON);
		assertEquals(0.8, tailCone.getRadius(fins.getAxialFront()), EPSILON);

		{ // case 1:
			fins.setAxialOffset( AxialMethod.TOP, 0.1);
			fins.setPoints(initialPoints);
			assertEquals(0.1f, fins.getAxialOffset(), EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( 0, 0.2, 0.1f);
            // ^^^^ function under test ^^^^

			assertEquals(0.3f, fins.getAxialOffset(), EPSILON);
            assertEquals(0.2f, fins.getLength(), EPSILON);
            
	    	assertEquals(0.3,  fins.getFinFront().x, EPSILON);
			assertEquals(0.85, fins.getFinFront().y, EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);
         
            // middle point:
            assertEquals(0.2, postPoints[1].x, EPSILON);
            assertEquals(0.3, postPoints[1].y, EPSILON);
            
            // final point
            assertEquals(0.2, postPoints[2].x, EPSILON);
            assertEquals(-0.1, postPoints[2].y, EPSILON);
            
		}{ // case 2:
			fins.setAxialOffset( AxialMethod.TOP, 0.1);
			fins.setPoints(initialPoints);

			// vvvv function under test vvvv
	    	fins.setPoint( 0, -0.2, 0.1f);
            // ^^^^ function under test ^^^^
	    	
            assertEquals(-0.1, fins.getFinFront().x, EPSILON);
			assertEquals(1.0, fins.getFinFront().y, EPSILON);

			assertEquals(-0.1f, fins.getAxialOffset(), EPSILON);
            assertEquals(0.6f, fins.getLength(), EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            assertEquals(0.6, postPoints[1].x, EPSILON);
            assertEquals(0.15, postPoints[1].y, EPSILON);
            
            assertEquals(0.6, postPoints[2].x, EPSILON);
            assertEquals(-0.25, postPoints[2].y, EPSILON);

		}{ // case 3:
			fins.setAxialOffset( AxialMethod.MIDDLE, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.0, fins.getAxialOffset(), EPSILON);
			assertEquals(0.3, fins.getFinFront().x, EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( 0, 0.1, 0.1f);
            // ^^^^ function under test ^^^^

			assertEquals(0.05, fins.getAxialOffset(), EPSILON);
			assertEquals(0.3, fins.getLength(), EPSILON);

			assertEquals(0.4, fins.getFinFront().x, EPSILON);
			assertEquals(0.8, fins.getFinFront().y, EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid-point
            assertEquals(0.3, postPoints[1].x, EPSILON);
            assertEquals(0.25, postPoints[1].y, EPSILON);
            
            assertEquals(0.3, postPoints[2].x, EPSILON);
            assertEquals(-0.15, postPoints[2].y, EPSILON);
            
		}{ // case 4:
			fins.setAxialOffset( AxialMethod.MIDDLE, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.3, fins.getFinFront().x, EPSILON);
			assertEquals(0.85, fins.getFinFront().y, EPSILON);

			// vvvv function under test vvvv
			fins.setPoint( 0, -0.1, 0.1f);
            // ^^^^ function under test ^^^^

			assertEquals(0.2, fins.getFinFront().x, EPSILON);
			assertEquals(0.9, fins.getFinFront().y, EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid point
            assertEquals(0.5, postPoints[1].x, EPSILON);
            //assertEquals(0.15, postPoints[1].y, EPSILON);
            
            assertEquals(0.5, postPoints[2].x, EPSILON);
            //assertEquals(0.15, postPoints[2].y, EPSILON);

            assertEquals(-0.05f, fins.getAxialOffset(), EPSILON);
            assertEquals(0.5f, fins.getLength(), EPSILON);
		}{ // case 5:
			fins.setAxialOffset( AxialMethod.BOTTOM, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.6, fins.getFinFront().x, EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( 0, 0.1, 0.1f);
            // ^^^^ function under test ^^^^

            assertEquals(0.7, fins.getFinFront().x, EPSILON);
			assertEquals(0.65, fins.getFinFront().y, EPSILON);
			assertEquals(0.0, fins.getAxialOffset(), EPSILON);
			assertEquals(0.3, fins.getLength(), EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid-point
            assertEquals(0.3, postPoints[1].x, EPSILON);
            //assertEquals(0.05, postPoints[1].y, EPSILON);
            
            assertEquals(0.3, postPoints[2].x, EPSILON);
            //assertEquals(0.15, postPoints[2].y, EPSILON);

		}{ // case 6:
			fins.setAxialOffset( AxialMethod.BOTTOM, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.6, fins.getFinFront().x, EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( 0, -0.1, 0.1f);
            // ^^^^ function under test ^^^^

            assertEquals(0.5, fins.getFinFront().x, EPSILON);
			assertEquals(0.75, fins.getFinFront().y, EPSILON);
			assertEquals(0.5, fins.getLength(), EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(3, postPoints.length);

            assertEquals(0.5, postPoints[1].x, EPSILON);
            assertEquals(0.15, postPoints[1].y, EPSILON);
            
            assertEquals(0.5, postPoints[2].x, EPSILON);
            assertEquals(-0.25, postPoints[2].y, EPSILON);
		}
    }

    @Test
    public void testSetLastPoint() {
    	final Rocket rkt = createTemplateRocket();
		final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = createFinOnConicalTransition(tailCone);
		final Coordinate[] initialPoints = fins.getFinPoints();
		final int lastIndex = initialPoints.length - 1;
		final double xf = initialPoints[lastIndex].x;
		final double yf = initialPoints[lastIndex].y;
				
		// assert pre-conditions:
		assertEquals(0.4, fins.getLength(), EPSILON);
        assertEquals(initialPoints[0], Coordinate.ZERO);
        assertEquals(initialPoints[1], new Coordinate(0.4, 0.2));
        assertEquals(initialPoints[2], new Coordinate(0.4, -0.2));
        assertEquals(1.0, tailCone.getLength(), EPSILON);
		assertEquals(0.8, tailCone.getRadius(fins.getAxialFront()), EPSILON);

		{ // case 1:
			fins.setAxialOffset( AxialMethod.TOP, 0.1);
			fins.setPoints(initialPoints);
			
			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf+0.2, yf - 0.3f);
            // ^^^^ function under test ^^^^

            assertEquals(0.1, fins.getFinFront().x, EPSILON);
			assertEquals(0.95, fins.getFinFront().y, EPSILON);
			assertEquals(0.6, fins.getLength(), EPSILON);
	    	
			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // middle point:
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);

            // last point:
            assertEquals(0.6, postPoints[2].x, EPSILON);
            assertEquals(-0.3, postPoints[2].y, EPSILON);

			
		}{ // case 2:
			fins.setAxialOffset( AxialMethod.TOP, 0.1);
			fins.setPoints(initialPoints);

			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf - 0.2, yf + 0.1f);
            // ^^^^ function under test ^^^^

            assertEquals(0.1, fins.getFinFront().x, EPSILON);
			assertEquals(0.95, fins.getFinFront().y, EPSILON);
			assertEquals(0.2, fins.getLength(), EPSILON);

	    	final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // middle point:
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);

            // last point:
            assertEquals(0.2, postPoints[2].x, EPSILON);
            assertEquals(-0.1, postPoints[2].y, EPSILON);
			
		}{ // case 3:
			fins.setAxialOffset( AxialMethod.MIDDLE, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.3, fins.getFinFront().x, EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf + 0.1, yf + 0.1f);
            // ^^^^ function under test ^^^^

			assertEquals(0.3, fins.getFinFront().x, EPSILON);
			assertEquals(0.85, fins.getFinFront().y, EPSILON);
			assertEquals(0.5, fins.getLength(), EPSILON);
			assertEquals(0.05, fins.getAxialOffset(), EPSILON);

	    	final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid-point
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);
            
            // last point
            assertEquals(0.5, postPoints[2].x, EPSILON);
            assertEquals(-0.25, postPoints[2].y, EPSILON);

		}{ // case 4:
			fins.setAxialOffset( AxialMethod.MIDDLE, 0.0);
			fins.setPoints(initialPoints);
			assertEquals(0.3, fins.getFinFront().x, EPSILON);

			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf - 0.1, yf + 0.1f);
            // ^^^^ function under test ^^^^

            assertEquals(0.3, fins.getFinFront().x, EPSILON);
			assertEquals(0.85, fins.getFinFront().y, EPSILON);
			assertEquals(0.3, fins.getLength(), EPSILON);
			assertEquals(-0.05, fins.getAxialOffset(), EPSILON);

	    	final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid point
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);
            
            // last point
            assertEquals(0.3, postPoints[2].x, EPSILON);
            assertEquals(-0.15, postPoints[2].y, EPSILON);

		}{ // case 5:
			fins.setAxialOffset( AxialMethod.BOTTOM, 0.0);
			fins.setPoints(initialPoints);

			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf + 0.1, yf + 0.1f);
            // ^^^^ function under test ^^^^

            assertEquals(0.6, fins.getFinFront().x, EPSILON);
			assertEquals(0.7, fins.getFinFront().y, EPSILON);
            assertEquals(0.1, fins.getAxialOffset(), EPSILON);
            assertEquals(0.5f, fins.getLength(), EPSILON);
            
			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);
			
            // mid-point
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);
            
            // pseudo last point
            assertEquals(0.5, postPoints[2].x, EPSILON);
            assertEquals(-0.2, postPoints[2].y, EPSILON);

		}{ // case 6:
			fins.setAxialOffset( AxialMethod.BOTTOM, 0.0);
			fins.setPoints(initialPoints);

			// vvvv function under test vvvv
	    	fins.setPoint( lastIndex, xf - 0.1, yf + 0.1f);
            // ^^^^ function under test ^^^^
	   
            assertEquals(0.6, fins.getFinFront().x, EPSILON);
			assertEquals(0.7, fins.getFinFront().y, EPSILON);
            assertEquals(-0.1, fins.getAxialOffset(), EPSILON);
            assertEquals(0.3, fins.getLength(), EPSILON);
            
			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // mid-point
            assertEquals(0.4, postPoints[1].x, EPSILON);
            assertEquals(0.2, postPoints[1].y, EPSILON);
            
            // last point
            assertEquals(0.3, postPoints[2].x, EPSILON);
            assertEquals(-0.15, postPoints[2].y, EPSILON);

		}
	}

    @Test
    public void testSetInteriorPoint() {
    	final Rocket rkt = createTemplateRocket();
    	final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);
		
		{ // preconditions // initial points
			final Coordinate[] initialPoints = fins.getFinPoints();
		    assertEquals(initialPoints[0], Coordinate.ZERO);
	        assertEquals(initialPoints[1], new Coordinate(0.4, 0.2));
	        assertEquals(initialPoints[2], new Coordinate(0.4, -0.2));
			assertEquals(0.4, fins.getLength(), EPSILON);
		}{ // preconditions // mount
			Transition finMount = (Transition) rkt.getChild(0).getChild(2);
            assertEquals(0.4, fins.getFinFront().x, EPSILON);
			assertEquals(0.8, fins.getFinFront().y, EPSILON);
			assertEquals(0.8, finMount.getRadius(fins.getAxialFront()), EPSILON);

			assertEquals(AxialMethod.TOP, fins.getAxialMethod());
			assertEquals(0.4, fins.getAxialOffset(), EPSILON);
			
		}{ // test target
			final Coordinate p1 = fins.getFinPoints()[1];
			
			// vvvv function under test vvvv
	    	fins.setPoint( 1, p1.x + 0.1, p1.y + 0.1f);
            // ^^^^ function under test ^^^^
	    	
		}{ // postconditions	    	
			final Coordinate[] postPoints = fins.getFinPoints();
            assertEquals(postPoints.length, 3);

            // middle point:
            assertEquals(0.5, postPoints[1].x, EPSILON);
            assertEquals(0.3, postPoints[1].y, EPSILON);

            // last point:
            assertEquals(0.4, postPoints[2].x, EPSILON);
            assertEquals(-0.2, postPoints[2].y, EPSILON);
            
            assertEquals(0.4, fins.getLength(), EPSILON);
            assertEquals(0.4, fins.getFinFront().x, EPSILON);
			assertEquals(0.8, fins.getFinFront().y, EPSILON);
		}
	}

    @Test
    public void testSetAllPointsOnPhantomBody() {
    	final Rocket rkt = createTemplateRocket();
    	final BodyTube phantomBody = (BodyTube) rkt.getChild(0).getChild(3);

		{ // setup // mount
			assertEquals(0.5, phantomBody.getOuterRadius(), EPSILON);
			assertEquals(0.0, phantomBody.getLength(), EPSILON);
		}{
			//  (1)---------(2)
			//   |    Fin    |
			//   |           |
			//  (0)----+----(3)
			//         |
			//       (body)
			final FreeformFinSet fins = new FreeformFinSet();
			fins.setName("SquareFin");
			phantomBody.addChild(fins);
			fins.setAxialOffset(AxialMethod.MIDDLE, 0.0);
			fins.setPoints(new Coordinate[]{
					new Coordinate(-0.5, 0.0),
					new Coordinate(-0.5, 1.0),
					new Coordinate(0.5, 1.0),
					new Coordinate(0.5, 0.0)
			});

		}{ // postconditions
			FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(3).getChild(0);

			assertEquals(AxialMethod.MIDDLE, fins.getAxialMethod());
			assertEquals(0.0, fins.getAxialOffset(), EPSILON);

			assertEquals(-0.5, fins.getFinFront().x, EPSILON);
			assertEquals(0.5, fins.getFinFront().y, EPSILON);

			final Coordinate[] postPoints = fins.getFinPoints();
			assertEquals(4, postPoints.length);

			// p0
			assertEquals("p0::x", 0.0, postPoints[0].x, EPSILON);
			assertEquals("p0::y", 0.0, postPoints[0].y, EPSILON);

			// p1
			assertEquals("p1::x", 0.0, postPoints[1].x, EPSILON);
			assertEquals("p1::y", 1.0, postPoints[1].y, EPSILON);

			// p2
			assertEquals("p2::x", 1.0, postPoints[2].x, EPSILON);
			assertEquals("p2::y", 1.0, postPoints[2].y, EPSILON);

			// p3 / last
			assertEquals("p3::x", 1.0, postPoints[3].x, EPSILON);
			assertEquals("p3::y", 0.0, postPoints[3].y, EPSILON);

			assertEquals(1.0, fins.getLength(), EPSILON);
		}
	}

    @Test
    public void testSetFirstPoint_clampToLast() {
    	final Rocket rkt = createTemplateRocket();
    	final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);
		
		assertEquals( 1, fins.getFinCount());
		assertEquals( 3, fins.getPointCount());
		assertEquals( AxialMethod.TOP, fins.getAxialMethod());
		assertEquals( 0.4, fins.getAxialOffset(), EPSILON); // pre-condition
		assertEquals( 1.0, tailCone.getLength(), EPSILON);
		
		// fin offset: 0.4 -> 0.59  (just short of prev fin end)
		// fin end:    0.4 ~> min root chord  
		// vv Test Target vv
		fins.setPoint( 0, 0.6, 0);
		// ^^ Test Target ^^

        assertEquals(fins.getFinPoints()[ 0], Coordinate.ZERO);
		
	    // setting the first point actually offsets the whole fin by that amount:
		final double expFinOffset = 0.8;
	    assertEquals("Resultant fin offset does not match!", expFinOffset, fins.getAxialOffset(), EPSILON);
	    
	    assertEquals( 3, fins.getPointCount());
	    Coordinate actualLastPoint = fins.getFinPoints()[2];
		assertEquals(0, actualLastPoint.x, EPSILON);
		assertEquals(0, actualLastPoint.y, EPSILON);
		assertEquals("New fin length is wrong: ", 0.0, fins.getLength(), EPSILON);
    }
    
    @Test
    public void testSetPoint_otherPoint(){
    	// combine the simple case with the complicated to ensure that the simple case is flagged, tested, and debugged before running the more complicated case...
    	{ // setting points on a Tube Body is the simpler case. Test this first: 
	    	final Rocket rkt = createTemplateRocket();
	    	final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
			final FreeformFinSet fins = createFinOnConicalTransition(tailCone);
			
	    	// all points are restricted to be outside the parent body:
		    Coordinate exp_pt = fins.getFinPoints()[0];
		    fins.setPoint(0, -0.6, 0);
		    Coordinate act_pt = fins.getFinPoints()[0];
		    assertEquals( exp_pt.x, act_pt.x, EPSILON);
		    // the last point is already clamped to the body; It should remain so.
		    assertEquals( 0.0, act_pt.y, EPSILON);
    	}
    	{ // more transitions trigger more complicated positioning math:  
    		final Rocket rkt = createTemplateRocket();
			final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
			final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);

			Coordinate act_p_l;
	    	Coordinate exp_p_l;
	    	
	    	final int testIndex = 1;
	    	// move point, and verify that it is coerced to be outside the parent body:
    		exp_p_l = new Coordinate( 0.2, -0.1, 0.0);
    		fins.setPoint( testIndex, 0.2, -0.2); // incorrect y-val.  The function should correct the y-value to above. 
    		
    		act_p_l = fins.getFinPoints()[ testIndex ];
    		assertEquals( exp_p_l.x, act_p_l.x, EPSILON);
    		assertEquals( exp_p_l.y, act_p_l.y, EPSILON);
    	}
    }


	@Test
	public void testSetOffset_triggerLeadingClampCorrection() {
		// test correction of last point due to moving entire fin:
		final Rocket rkt = createTemplateRocket();
		final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);

		final int lastIndex = fins.getPointCount()-1;

		{ // pre-condition
			assertEquals(AxialMethod.TOP, fins.getAxialMethod());
			assertEquals(0.4, fins.getAxialOffset(), EPSILON);
			assertEquals(0.4, fins.getLength(), EPSILON);
		}

		// vv Test Target vv
		fins.setAxialOffset( -0.2);
		// ^^ Test Target ^^

		// fin start: 0.4 => 0.8  [body]
		// fin end:   0.8 => 1.2 [body]
		assertEquals( -0.2, fins.getAxialOffset(), EPSILON);
		assertEquals( 0.4, fins.getLength(), EPSILON);

		// SHOULD DEFINITELY CHANGE
		Coordinate actualLastPoint = fins.getFinPoints()[ lastIndex];
		assertEquals( 0.4, actualLastPoint.x, EPSILON);
		assertEquals( -0.1, actualLastPoint.y, EPSILON);
	}

	@Test
	public void testSetOffset_triggerTrailingClampCorrection() {
		// test correction of last point due to moving entire fin:
		final Rocket rkt = createTemplateRocket();
		final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);

		final int lastIndex = fins.getPointCount()-1;

		{ // pre-condition
			assertEquals(AxialMethod.TOP, fins.getAxialMethod());
			assertEquals(0.4, fins.getAxialOffset(), EPSILON);
			assertEquals(0.4, fins.getLength(), EPSILON);
		}

		// vv Test Target vv
		fins.setAxialOffset( 0.8);
		// ^^ Test Target ^^

		// fin start: 0.4 => 0.8  [body]
		// fin end:   0.8 => 1.2 [body]
		assertEquals( 0.8, fins.getAxialOffset(), EPSILON);
		assertEquals( 0.4, fins.getLength(), EPSILON);

		// SHOULD DEFINITELY CHANGE
		Coordinate actualLastPoint = fins.getFinPoints()[ lastIndex];
		assertEquals( 0.4, actualLastPoint.x, EPSILON);
		assertEquals( -0.1, actualLastPoint.y, EPSILON);
	}

	@Test
	public void testComputeCM_mountlessFin(){
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
			assertEquals(0.75, fins.getPlanformArea(), EPSILON);

			assertEquals(0.388889, coords.x, EPSILON);
			assertEquals(0.444444, coords.y, EPSILON);
	}

    @Test
    public void testTranslatePoints(){
    	final Rocket rkt = createTemplateRocket();
	    final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
		final FreeformFinSet fin = this.createFinOnTube(body);

		assertNotNull(fin.getParent());

	    // Fin length = 1
	    // Body Length = 2
	    //          +--+
	    //         /   |
	    //        /    |
	    //   +---+-----+---+
		final Coordinate[] expectPoints = new Coordinate[]{
				Coordinate.ZERO,
			    new Coordinate(0.5, 1),
			    new Coordinate(1, 1),
			    new Coordinate(1, 0)
		};

		final Coordinate[] finPoints = fin.getFinPoints();
		assertEquals(4, finPoints.length);
		assertEquals(expectPoints[1], finPoints[1]);
		assertEquals(expectPoints[2], finPoints[2]);
		assertEquals(expectPoints[3], finPoints[3]);

		// mounting body:
		assertEquals(body.getLength(), 2.0, EPSILON);

		assertEquals(fin.getAxialMethod(), AxialMethod.BOTTOM);
		assertEquals(fin.getAxialOffset(), 0.0, EPSILON);

		final AxialMethod[] pos={AxialMethod.TOP, AxialMethod.MIDDLE, AxialMethod.MIDDLE, AxialMethod.BOTTOM};
	    final double[] offs = {1.0, 0.0, 0.4, -0.2};
	    final double[] expOffs = {1.0, 0.5, 0.9, 0.8};
	    for( int caseIndex=0; caseIndex < pos.length; ++caseIndex ){
			fin.setAxialOffset( pos[caseIndex], offs[caseIndex]);

			assertEquals(fin.getAxialMethod(), pos[caseIndex]);
			assertEquals(fin.getAxialOffset(), offs[caseIndex], EPSILON);

			final double x_delta = fin.getAxialOffset(AxialMethod.TOP);
	           
	    	final Coordinate[] actualPoints = fin.getFinPoints();

	    	final String rawPointDescr = "\n"+fin.toDebugDetail().toString()+"\n>> axial offset: "+x_delta;
	       
	    	Coordinate[] displayPoints = FinSet.translatePoints( actualPoints, x_delta, 0);	    
	    	for( int index=0; index < displayPoints.length; ++index){
				assertEquals(String.format("Bad Fin Position.x (%6.2g via:%s at point: %d) %s\n",offs[caseIndex], pos[caseIndex].name(), index, rawPointDescr),
	    				     (expectPoints[index].x + expOffs[caseIndex]), displayPoints[index].x, EPSILON);
	    		assertEquals(String.format("Bad Fin Position.y (%6.2g via:%s at point: %d) %s\n",offs[caseIndex], pos[caseIndex].name(), index, rawPointDescr),
						     expectPoints[index].y, displayPoints[index].y, EPSILON);
       		}
    	}
	                   
	}

    @Test
    public void testForIntersection_false() {
    	final Rocket rkt = createTemplateRocket();
    	final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
    	final FreeformFinSet fins = createFinOnTube(body);

    	// Fin length = 1
    	// Body Length = 2
    	//          +--+
    	//         /   |
    	//        /    |
    	//   +---+-----+---+
		final Coordinate[] finPoints = fins.getFinPoints();
		assertEquals(4, finPoints.length);
		assertEquals(finPoints[0], Coordinate.ZERO);
		assertEquals(finPoints[1], new Coordinate(0.5, 1.0));
		assertEquals(finPoints[2], new Coordinate(1.0, 1.0));
		assertEquals(finPoints[3], new Coordinate(1.0, 0.0));
    	
    	assertFalse( " Fin detects false positive intersection in fin points: ", fins.intersects());
    }

    @Test
    public void testForIntersection_true() {
    	final Rocket rkt = createTemplateRocket();
	    final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);
		
    	// An obviously intersecting fin:
    	//   [2] +-----+ [1]
        //        \   /
    	//         \ /
        //          X
    	//         / \
        //    [0] /   \ [3]
    	//   +---+-----+---+
		// = +x =>
    	Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate(0, 0),
	                   new Coordinate(1, 1),
	                   new Coordinate(0, 1),
	                   new Coordinate(1, 0)
    	};
    	// this line throws an exception? 
    	fins.setPoints(initPoints);
	   
    	// this *already* has detected the intersection, and aborted...
    	Coordinate p1 = fins.getFinPoints()[1];
    	// ... which makes a rather hard-to-test functionality...
    	assertThat( "Fin Set failed to detect an intersection! ", p1.x, not(equalTo(initPoints[1].x)));
    	assertThat( "Fin Set failed to detect an intersection! ", p1.y, not(equalTo(initPoints[1].y)));
    }

    @Test
    public void testForIntersectionAtFirstLast() {
    	final Rocket rkt = createTemplateRocket();
	    final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);
		
		// An obviously intersecting fin:
    	//   [2] +---+ [1]
        //       |  /
    	//       | / 
        //    [0]|/ [3]
    	//   +---+-----+---+
		// = +x =>
    	Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate(0, 0),
	                   new Coordinate(0, 1),
	                   new Coordinate(1, 1),
	                   new Coordinate(0, 0)
    	};
    	// this line throws an exception? 
    	fins.setPoints(initPoints);
    	
    	final Coordinate[] finPoints = fins.getFinPoints();
    	
    	// p0
		assertEquals("incorrect body points! ", 0., finPoints[0].x, EPSILON);
		assertEquals("incorrect body points! ", 0., finPoints[0].y, EPSILON);
		
		// p1
		assertEquals("incorrect body points! ", 0., finPoints[1].x, EPSILON);
		assertEquals("incorrect body points! ", 1., finPoints[1].y, EPSILON);
		
		// p2
		assertEquals("incorrect body points! ", 1., finPoints[2].x, EPSILON);
		assertEquals("incorrect body points! ", 1., finPoints[2].y, EPSILON);
		
		// pf
		assertEquals("incorrect body points! ", 0., finPoints[3].x, EPSILON);
		assertEquals("incorrect body points! ", 0., finPoints[3].y, EPSILON);	
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

		assertEquals(0.00130708, fins.getPlanformArea(), EPSILON);

		assertEquals(0.03423168, coords.x, EPSILON);
		assertEquals(0.01427544, coords.y, EPSILON);
		
		BodyTube bt = new BodyTube(0.1, 0.1);
		bt.addChild(fins);
		FinSetCalc calc = new FinSetCalc(fins);
		FlightConditions conditions = new FlightConditions(null);
		Transformation transform = Transformation.IDENTITY;
		AerodynamicForces forces = new AerodynamicForces();
		WarningSet warnings = new WarningSet();
		calc.calculateNonaxialForces(conditions, transform, forces, warnings);
		//System.out.println(forces);
		assertEquals(0.023409, forces.getCP().x, 0.0001);
	}

	@Test
	public void testGenerateRootPointsOnBodyTube(){
		final Rocket rkt = createTemplateRocket();
		final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);
		final FreeformFinSet fins = this.createFinOnTube(body);

		{ // root points (relative to fin-front)
			final Coordinate[] finPoints = fins.getFinPoints();
			final Coordinate[] rootPoints = fins.getRootPoints();

			assertEquals("Method should only generate minimal points for a conical transition fin body! ", 2, rootPoints.length );
			assertEquals("incorrect body point: 0::x ! ", finPoints[0].x, rootPoints[0].x, EPSILON);
			assertEquals("incorrect body point: 0::y ! ", finPoints[0].y, rootPoints[0].y, EPSILON);
			assertEquals("incorrect body point: -1::x !", finPoints[finPoints.length-1].x, rootPoints[1].x, EPSILON);
			assertEquals("incorrect body point: -1::y !", finPoints[finPoints.length-1].y, rootPoints[1].y, EPSILON);
		}
	}

	@Test
	public void testGenerateBodyPointsOnConicalTransition(){
		final Rocket rkt = createTemplateRocket();
		final Transition tailCone = (Transition) rkt.getChild(0).getChild(2);
		final FreeformFinSet fins = this.createFinOnConicalTransition(tailCone);
		
		{ // body points (relative to root)
			final Coordinate[] finPoints = fins.getFinPoints();
			final Coordinate[] rootPoints = fins.getRootPoints();
	
			assertEquals("Method should only generate minimal points for a conical transition fin body! ", 2, rootPoints.length );
			assertEquals("incorrect body points! ", finPoints[0].x, rootPoints[0].x, EPSILON);
			assertEquals("incorrect body points! ", finPoints[0].y, rootPoints[0].y, EPSILON);
			assertEquals("incorrect body points! ", finPoints[finPoints.length-1].x, rootPoints[1].x, EPSILON);
			assertEquals("incorrect body points! ", finPoints[finPoints.length-1].y, rootPoints[1].y, EPSILON);
		}
	}
	
	@Test
	public void testGenerateBodyPointsOnEllipsoidNose(){
		final Rocket rocket = createTemplateRocket();
	    final NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);
		final FreeformFinSet fins = this.createFinOnEllipsoidNose(nose);
		
		final Coordinate finFront = fins.getFinFront();
		final Coordinate[] finPoints = fins.getFinPoints();
		
		{ // fin points (relative to fin) // preconditions
			assertEquals(4, finPoints.length);
			
			assertEquals("incorrect fin points! ", 0f, finPoints[0].x, EPSILON);
			assertEquals("incorrect fin points! ", 0f, finPoints[0].y, EPSILON);

			assertEquals("incorrect fin points! ",  0.8, finPoints[3].x, EPSILON);

			assertEquals(nose.getRadius(0.8+finFront.x) - finFront.y, finPoints[3].y, EPSILON);
			
			assertEquals("incorrect fin points! ",  0.78466912, finPoints[3].y, EPSILON);
			
		}{ // body points (relative to fin)
			final Coordinate[] rootPoints = fins.getRootPoints();
			assertEquals(101, rootPoints.length);
			final int lastIndex = 100;
			
			// trivial, and uninteresting:
			assertEquals("incorrect body points! ", finPoints[0].x, rootPoints[0].x, EPSILON);
			assertEquals("incorrect body points! ", finPoints[0].y, rootPoints[0].y, EPSILON);

			// n.b.: This should match EXACTLY the end point of the fin. (in fin coordinates)
			assertEquals("incorrect body points! ", finPoints[finPoints.length -1].x, rootPoints[lastIndex].x, EPSILON);
			assertEquals("incorrect body points! ", finPoints[finPoints.length -1].y, rootPoints[lastIndex].y, EPSILON);
			
			{// the tests within this scope is are rather fragile, and may break for reasons other than bugs :(
				// the number of points is somewhat arbitrary, but if this test fails, the rest *definitely* will.
				assertEquals("Method is generating how many points? ", 101, rootPoints.length );
	
				final int[] testIndices = {  2,      5,     61,      88};
				final double[] expectedX = { 0.016,  0.04,   0.488,   0.704};
	
				for( int testCase = 0; testCase < testIndices.length; testCase++){
					final int testIndex =  testIndices[testCase];
					assertEquals(String.format("Root points @ %d :: x coordinate mismatch!", testIndex),
							expectedX[testCase], rootPoints[testIndex].x, EPSILON);
					assertEquals(String.format("Root points @ %d :: y coordinate mismatch!", testIndex),
							nose.getRadius(rootPoints[testIndex].x + finFront.x) - finFront.y, rootPoints[testIndex].y, EPSILON);
			    }
			}
		}
	}

	@Test
	public void testFreeFormCMWithNegativeY() throws Exception {
		final Rocket rkt = createTemplateRocket();
	    final BodyTube body = (BodyTube) rkt.getChild(0).getChild(1);

		// A user submitted an ork file which could not be simulated.
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
		body.addChild(fins);
		fins.setAxialOffset( AxialMethod.TOP, 1.0);
		fins.setFinCount(1);

		Coordinate[] points = new Coordinate[] {
				new Coordinate(0.0, 0),
				new Coordinate(0.0, 1),
				new Coordinate(2.0, 1),
				new Coordinate(2.0, -1),
				new Coordinate(1.0001, -1),
				new Coordinate(1.0, 0)
		};
		fins.setPoints(points);

		fins.setPoints( points);
		fins.setFilletRadius( 0.0);
		fins.setTabHeight( 0.0);
		fins.setCrossSection( CrossSection.SQUARE );  // to ensure uniform density
		fins.setMaterial( Material.newMaterial(Type.BULK, "dummy", 1.0, true));

		assertEquals( 3.0, fins.getPlanformArea(), 0.0001);

		final Coordinate cg = fins.getCG();
		assertEquals(1.1666, cg.x, 0.0001);
		assertEquals(1.1666, cg.y, 0.0001);
		assertEquals( 0.0, cg.z, EPSILON);
		assertEquals( 0.009, cg.weight, EPSILON);
	}

	@Test
	public void testFreeFormCMWithTooManyPoints() {
		final Rocket rkt = createTemplateRocket();
		final BodyTube phantomBody = (BodyTube) rkt.getChild(0).getChild(3);
		final FreeformFinSet fins = new FreeformFinSet();

		// fins.setAxialOffset( Position.BOTTOM, 1.0);  // ERROR: no parent!
		final Coordinate[] setPoints = new Coordinate[] {
				new Coordinate(0.006349996571001852, 0.0),
				new Coordinate(0.00635, 0.022224999999999998),
				new Coordinate(0.0067056, 0.02716387422039681),
				new Coordinate(0.007619999999999999, 0.03174998285500926),
				new Coordinate(0.0093472, 0.036159702695982766),
				new Coordinate(0.0110998, 0.03951108977512263),
				new Coordinate(0.028134012585410983, 0.06508746485276898),
				new Coordinate(0.030427066902717206, 0.06843885193190885),
				new Coordinate(0.03298470441048184, 0.07170204461422924),
				new Coordinate(0.0351895643309686, 0.073906904534716),
				new Coordinate(0.03801178502919164, 0.0756707924711054),
				new Coordinate(0.04101039452105363, 0.07672912523293904),
				new Coordinate(0.04409719840973508, 0.07743468040749481),
				new Coordinate(0.04762497428251389, 0.07787565239159215),
				new Coordinate(0.0511527501552927, 0.07797799999999999),
				new Coordinate(0.08021280390730812, 0.07797799999999999),
				new Coordinate(0.08127113666914176, 0.07796384678841163),
				new Coordinate(0.08206488624051698, 0.07787565239159215),
				new Coordinate(0.08281453861348248, 0.07747877760590453),
				new Coordinate(0.08316731620076037, 0.07681731962975852),
				new Coordinate(0.08325551059757984, 0.07584718126474434),
				new Coordinate(0.083312, 0.07487704289973017),
				new Coordinate(0.08329960779598958, 0.033293384799349984),
				new Coordinate(0.08325551059757984, 0.03254373242638449),
				new Coordinate(0.08307912180394089, 0.03174998285500926),
				new Coordinate(0.08263814981984355, 0.031132622077272968),
				new Coordinate(0.08180030305005857, 0.030691650093175617),
				new Coordinate(0.0806978730898152, 0.030479999999999997),
				new Coordinate(0.06178017497203885, 0.030479999999999997),
				new Coordinate(0.05635621956764143, 0.030479999999999997),
				new Coordinate(0.05344580447259892, 0.030225999999999996),
				new Coordinate(0.051461430544160844, 0.0292862),
				new Coordinate(0.050006222996639586, 0.027711399999999997),
				new Coordinate(0.04921247342526435, 0.0261112),
				new Coordinate(0.048683307044347535, 0.024002999999999997),
				new Coordinate(0.048768, 0.022098),
				new Coordinate(0.048768, 0.0)
		};
		fins.setPoints( setPoints);
		phantomBody.addChild(fins);

		{ // fin points (relative to fin) // preconditions
			final Coordinate[] finPoints = fins.getFinPoints();
			assertEquals(37, finPoints.length);

			// fin root length:
			assertEquals(0.04241800, fins.length, EPSILON);
			
			// p_first
			assertEquals(0f, finPoints[0].x, EPSILON);
			assertEquals(0f, finPoints[0].y, EPSILON);

			// p_last
			assertEquals(0.042418, finPoints[36].x, EPSILON);
			assertEquals(0., finPoints[36].y, EPSILON);

		}
	}

}
