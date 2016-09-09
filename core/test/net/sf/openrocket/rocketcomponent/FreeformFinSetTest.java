package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FreeformFinSetTest extends BaseTestCase {
	final double EPSILON = 0.0001;
	
    public Rocket createFinsOnTube() {
    	Rocket rkt = new Rocket();
        AxialStage stg = new AxialStage();
	    rkt.addChild(stg);
	    BodyTube body = new BodyTube(1.0, 0.6);
	    body.setLength(1.0);
	    body.setOuterRadius(0.6);
	    stg.addChild(body);
	   
	    // Fin length = 0.4
	    // Body Length = 1.0
	    //          +--+
	    //         /   |
	    //        /    |
	    //   +---+-----+---+
	    //
	    FreeformFinSet fins = new FreeformFinSet();
	    fins.setFinCount(1);
	    Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate(0.0, 0.0),
	                   new Coordinate(0.2, 0.4),
	                   new Coordinate(0.4, 0.4),
	                   new Coordinate(0.4, 0.0)
	    };
	    fins.setPoints(initPoints);
	    body.addChild(fins);
	    fins.setAxialOffset( Position.TOP, 0.4);
	    
	    rkt.enableEvents();
	    return rkt;
    }
    
    public Rocket createFinsOnTransition() {
    	Rocket rkt = new Rocket();
        AxialStage stg = new AxialStage();
	    rkt.addChild(stg);
	    Transition body = new Transition();
	    body.setType(Shape.CONICAL);
	    body.setForeRadius(1.0);
	    body.setLength(1.0);
	    body.setAftRadius(0.5);
	    // slope = .5/1.0 = 0.5
	    body.setName("Transition Body");
	    stg.addChild(body);
	   
	    // Fin length = 0.4
	    // Body Length = 1.0
	    //         +------+
	    //       /       +
	    //     /        +
	    // +---+       +
	    //     +---+  +
	    //         +---+
	    FreeformFinSet fins = new FreeformFinSet();
	    body.addChild(fins);
	    fins.setName("test-freeform-finset");
	    fins.setFinCount(1);
	    fins.setAxialOffset( Position.TOP, 0.4);
	    Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate( 0.0, 0.0),
	                   new Coordinate( 0.3, 0.2),
	                   new Coordinate( 0.6, 0.2),
	                   new Coordinate( 0.4,-0.2)
	    };
	    fins.setPoints(initPoints);
	    fins.setAxialOffset( Position.TOP, 0.4);
	    
	    rkt.enableEvents();
	    return rkt;
    }
    
    public Rocket createFinsOnBoattail(){
    	Rocket rkt = new Rocket();
        AxialStage stg = new AxialStage();
	    rkt.addChild(stg);
	    Transition body = new Transition();
	    body.setForeRadius(0.1);
	    body.setLength(0.1);
	    body.setAftRadius(0.04);
	    body.setType( Shape.ELLIPSOID );
	    body.setShapeParameter(0.5);
	    body.setName("Transition Body");
	    stg.addChild(body);
	   
	    FreeformFinSet fins = new FreeformFinSet();
	    body.addChild(fins);
	    fins.setName("test-freeform-finset");
	    fins.setFinCount(1);
	    fins.setAxialOffset( Position.TOP, 0.02);
	    fins.setPoints(new Coordinate[] {
                new Coordinate( 0.0,   0.0),
                new Coordinate( 0.06,  0.04),
                new Coordinate( 0.08,  0.04),
                new Coordinate( 0.06, -0.03029),
	    });
	    
	    rkt.enableEvents();
	    return rkt;
    }
    
    
    @Test
    public void testSetPoint_firstPoint_boundsCheck() {
    	// more transitions trigger more complicated positioning math:  
		Rocket rkt = createFinsOnTransition();
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
		assertEquals( 1, fins.getFinCount());
		final int startIndex = 0;
		final int lastIndex = fins.getPointCount()-1;
		assertEquals( 3, lastIndex);

    	Coordinate act_p_0;
    	{ // first point x is restricted to the front of the parent body:
		    fins.setPoint( startIndex, -1, 0);
		    act_p_0 = fins.getFinPoints()[0];
		    assertEquals( 0.0, act_p_0.x, EPSILON);
		    assertEquals( 0.0, fins.getAxialOffset(), EPSILON);
    	}
    	
    	{// first point y is restricted to the body
		    fins.setPoint( startIndex, 0, -1);
		    act_p_0 = fins.getFinPoints()[0];
			assertEquals( 0.0, act_p_0.y, EPSILON);
    	}
    }
    
    @Test
    public void testSetPoint_firstPoint_normal() {
    	// more transitions trigger more complicated positioning math:  
		Rocket rkt = createFinsOnTransition();
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
		assertEquals( 1, fins.getFinCount());
		final int startIndex = 0;
		final int lastIndex = fins.getPointCount()-1;
		assertEquals( 3, lastIndex);
		final double initXOffset = fins.getAxialOffset();
		assertEquals( 0.4, initXOffset, EPSILON); // pre-condition
		
		final double xDelta = 0.2;
		final double expXDelta = xDelta;
		Coordinate expectedLastPoint = fins.getFinPoints()[ lastIndex].add( -0.2, 0.1, 0);
		fins.setPoint( startIndex, xDelta, 0);
    	
	    // setting the first point actually offsets the whole fin by that amount:
		Coordinate act_p_0 = fins.getFinPoints()[ startIndex];
		assertEquals( 0.0, act_p_0.x, EPSILON);
		assertEquals( 0.0, act_p_0.y, EPSILON);
	    final double expFinOffset = initXOffset + expXDelta;
	    assertEquals( expFinOffset, fins.getAxialOffset(), EPSILON);
	    
	    // SHOULD NOT CHANGE (in this case):
	    Coordinate actualLastPoint = fins.getFinPoints()[ lastIndex];
		assertEquals( expectedLastPoint.x, actualLastPoint.x, EPSILON);
		assertEquals( expectedLastPoint.y, actualLastPoint.y, EPSILON); // magic number
		assertEquals("New fin length is wrong: ", 0.2, fins.getLength(), EPSILON);
    }

    @Test
    public void testSetFirstPoint_testNonIntersection() {
    	// more transitions trigger more complicated positioning math:  
		Rocket rkt = createFinsOnTransition();
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
		assertEquals( 1, fins.getFinCount());
		final int startIndex = 0;
		final int lastIndex = fins.getPointCount()-1;
		assertEquals( 3, lastIndex);
		final double initXOffset = fins.getAxialOffset();
		assertEquals( 0.4, initXOffset, EPSILON); // pre-condition
		
		final double attemptedDelta = 0.6;
		fins.setPoint( startIndex, attemptedDelta, 0);
		// fin offset: 0.4 -> 0.59  (just short of prev fin end)
		// fin end:    0.4 ~> min root chord  
    
		Coordinate act_p_0 = fins.getFinPoints()[ startIndex];
		assertEquals( 0.0, act_p_0.x, EPSILON);
		assertEquals( 0.0, act_p_0.y, EPSILON);
		
	    // setting the first point actually offsets the whole fin by that amount:
		final double expFinOffset = 0.79;
	    assertEquals("Resultant fin offset does not match!", expFinOffset, fins.getAxialOffset(), EPSILON);
	    
	    // SHOULD NOT CHANGE (in this case):
	    Coordinate actualLastPoint = fins.getFinPoints()[ lastIndex];
		assertEquals("last point did not adjust correctly: ", FreeformFinSet.MIN_ROOT_CHORD, actualLastPoint.x, EPSILON);
		assertEquals("last point did not adjust correctly: ", -0.005, actualLastPoint.y, EPSILON); // magic number
		assertEquals("New fin length is wrong: ", FreeformFinSet.MIN_ROOT_CHORD, fins.getLength(), EPSILON);
    }
    

    @Test
    public void testSetLastPoint_TubeBody() {
    	// combine the simple case with the complicated to ensure that the simple case is flagged, tested, and debugged before running the more complicated case...
    	{ // setting points on a Tube Body is the simpler case. Test this first:
    		Rocket rkt = createFinsOnTube();
    		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
    		
    		// last point is restricted to the body
    		final int lastIndex = fins.getPointCount()-1; 
    		Coordinate exp_p_l = new Coordinate( 0.6, 0.0, 0.0);
    		fins.setPoint(lastIndex, 1, -1);
    		
    		Coordinate act_p_l = fins.getFinPoints()[lastIndex];
    		assertEquals( exp_p_l.x, act_p_l.x, EPSILON);
    		assertEquals( exp_p_l.y, act_p_l.y, EPSILON);
		}
    	{ // more transitions trigger more complicated positioning math:  
			Rocket rkt = createFinsOnTransition();
			FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
			
			assertEquals( 1, fins.getFinCount());
			final int lastIndex = fins.getPointCount()-1;
			assertEquals( 3, lastIndex);
    	
	    	Coordinate act_p_l;
	    	Coordinate exp_p_l;
	    	{ // this is where the point starts off at: 
	    		act_p_l = fins.getFinPoints()[lastIndex];
	    		assertEquals( 0.4, act_p_l.x, EPSILON);
	    		assertEquals( -0.2, act_p_l.y, EPSILON);
	    	}
	    	
	    	{ // (1):  move point within bounds
	    		// move last point, and verify that its y-value is still clamped to the body ( at the new location) 
	    		exp_p_l = new Coordinate( 0.6, -0.3, 0.0);
	    		fins.setPoint(lastIndex, 0.6, 0.0); // w/ incorrect y-val.  The function should correct the y-value as above. 
	    		
	    		act_p_l = fins.getFinPoints()[lastIndex];
	    		assertEquals( exp_p_l.x, act_p_l.x, EPSILON);
	    		assertEquals( exp_p_l.y, act_p_l.y, EPSILON);
	    	}
		}
	}
    
    @Test
    public void testSetPoint_otherPoint() {
    	// combine the simple case with the complicated to ensure that the simple case is flagged, tested, and debugged before running the more complicated case...
    	{ // setting points on a Tube Body is the simpler case. Test this first: 
	    	Rocket rkt = createFinsOnTube();
	    	FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
	    	
	    	// all points are restricted to be outside the parent body:
		    Coordinate exp_pt = fins.getFinPoints()[0];
		    fins.setPoint(0, -0.6, 0);
		    Coordinate act_pt = fins.getFinPoints()[0];
		    assertEquals( exp_pt.x, act_pt.x, EPSILON);
		    // the last point is already clamped to the body; It should remain so.
		    assertEquals( 0.0, act_pt.y, EPSILON);
    	}
    	{ // more transitions trigger more complicated positioning math:  
    		Rocket rkt = createFinsOnTransition();
			FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
			assertEquals( 1, fins.getFinCount());
			
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
    public void testSetOffset_triggerClampCorrection() {
		// test correction of last point due to moving entire fin:
		Rocket rkt = createFinsOnTransition();
		Transition body = (Transition) rkt.getChild(0).getChild(0);
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
		assertEquals( 1, fins.getFinCount());
		final int lastIndex = fins.getPointCount()-1;
		assertEquals( 3, lastIndex);
		
		final double initXOffset = fins.getAxialOffset();
		assertEquals( 0.4, initXOffset, EPSILON); // pre-condition
		final double newXTop = 0.85;
		final double expFinOffset = 0.6;
		final double expLength = body.getLength() - expFinOffset;
		fins.setAxialOffset( Position.TOP, newXTop);
		// fin start: 0.4 => 0.8  [body]
		// fin end:   0.8 => 0.99 [body]  
	    assertEquals( expFinOffset, fins.getAxialOffset(), EPSILON);
	    assertEquals( expLength, fins.getLength(), EPSILON);
	    
	    // SHOULD DEFINITELY CHANGE
	    Coordinate actualLastPoint = fins.getFinPoints()[ lastIndex];
		assertEquals( 0.4, actualLastPoint.x, EPSILON);
		assertEquals( -0.2, actualLastPoint.y, EPSILON);
    }
    
	@Test
	public void testComputeCM_ZeroSweepSimpleTrapezoid() throws Exception {
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
			assertEquals(0.75, fins.getFinWettedArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
	}
	

	@Test
	public void testComputeCM_ZeroSweepComplicatedTrapezoid() throws Exception {
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
			assertEquals(0.75, fins.getFinWettedArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
	}
		
	@Test
	public void testComputeCM_CoincidentPoints() throws Exception {
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
			assertEquals(0.75, fins.getFinWettedArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
		
		
	}
	
    @Test
    public void testTranslatePoints() throws IllegalFinPointException {
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
	    //
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
	    
	    final Position[] pos={Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM};
	    final double[] offs = {1.0, 0.0, 0.4, -0.2};
	    final double[] expOffs = {1.0, 0.5, 0.9, 0.8};
	    for( int caseIndex=0; caseIndex < pos.length; ++caseIndex ){
	    	fins.setAxialOffset( pos[caseIndex], offs[caseIndex]);
	    	final double x_delta = fins.asPositionValue(Position.TOP);
	           
	    	Coordinate actualPoints[] = fins.getFinPoints();
	                   
	    	final String rawPointDescr = "\n"+fins.toDebugDetail().toString()+"\n>> axial offset: "+x_delta;
	       
	    	Coordinate[] displayPoints = FinSet.translatePoints( actualPoints, x_delta, 0);	    
	    	for( int index=0; index < displayPoints.length; ++index){
	    		assertEquals(String.format("Bad Fin Position.x (%6.2g via:%s at point: %d) %s\n",offs[caseIndex], pos[caseIndex].name(), index, rawPointDescr),
	    				(initPoints[index].x + expOffs[caseIndex]), displayPoints[index].x, EPSILON);
	    		assertEquals(String.format("Bad Fin Position.y (%6.2g via:%s at point: %d) %s\n",offs[caseIndex], pos[caseIndex].name(), index, rawPointDescr),
	    				initPoints[index].y, displayPoints[index].y, EPSILON);
       		}
    	}
	                   
	}

	
    @Test
    public void testForNonIntersection() throws IllegalFinPointException {
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
               //
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
               
               assertFalse( " Fin detects false positive intersection in fin points: ", fins.intersects());
       }
    
    @Test
    public void testForIntersection() throws IllegalFinPointException {
    	final Rocket rkt = new Rocket();
    	final AxialStage stg = new AxialStage();
    	rkt.addChild(stg);
    	BodyTube body = new BodyTube(2.0, 0.01);
    	stg.addChild(body);
	   
    	// An obviously intersecting fin:
    	//        +---+
    	//         \ /   
    	//         / \   
    	//   +----+---+---+
    	//
    	FreeformFinSet fins = new FreeformFinSet();
    	fins.setFinCount(1);
    	Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate(0, 0),
	                   new Coordinate(1, 1),
	                   new Coordinate(0, 1),
	                   new Coordinate(1, 0)
    	};
    	// this line throws an exception? 
    	fins.setPoints(initPoints);
    	body.addChild(fins);
	   
    	// this *already* has detected the intersection, and aborted...
    	Coordinate p1 = fins.getFinPoints()[1];
    	// ... which makes a rather hard-to-test functionality...
    	assertThat( "Fin Set failed to detect an intersection! ", p1.x, not(equalTo(initPoints[1].x)));
    	assertThat( "Fin Set failed to detect an intersection! ", p1.y, not(equalTo(initPoints[1].y)));
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
		assertEquals(0.00130, fins.getFinWettedArea(), 0.00001);
		assertEquals(0.03423, coords.x, 0.00001);
		assertEquals(0.01427, coords.y, 0.00001);
		
		BodyTube bt = new BodyTube();
		bt.addChild(fins);
		FinSetCalc calc = new FinSetCalc(fins);
		FlightConditions conditions = new FlightConditions(null);
		AerodynamicForces forces = new AerodynamicForces();
		WarningSet warnings = new WarningSet();
		calc.calculateNonaxialForces(conditions, forces, warnings);
		//System.out.println(forces);
		assertEquals(0.023409, forces.getCP().x, 0.0001);
	}
	
	@Test
	public void testGenerateBodyPoints_conicalTransition_fromFin(){
		Rocket rkt = createFinsOnTransition();
		FinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 

		Coordinate[] finPoints = FinSet.translatePoints( fins.getFinPoints_fromFin(),  0.0, fins.getBodyRadius());
		final Coordinate exp_startp = finPoints[0];
		final Coordinate exp_endp = finPoints[ finPoints.length-1];
		
		final Coordinate[] bodyPoints = fins.getBodyPoints();

		assertEquals("Method should only generate minimal points for a conical transition fin body! ", 2, bodyPoints.length );
		assertEquals("incorrect body points! ", exp_startp.x, bodyPoints[0].x, EPSILON);
		assertEquals("incorrect body points! ", exp_startp.y, bodyPoints[0].y, EPSILON);
		assertEquals("incorrect body points! ", exp_endp.x, bodyPoints[1].x, EPSILON);
		assertEquals("incorrect body points! ", exp_endp.y, bodyPoints[1].y, EPSILON);
	}
	
	@Test
	public void testGenerateBodyPoints_ConicalTransition_fromParent(){
		Rocket rkt = createFinsOnTransition();
		FinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 

		final double yFinFront = fins.getBodyRadius();
		final Coordinate[] finPoints = FinSet.translatePoints( fins.getFinPoints(), 0.0, yFinFront );
		final Coordinate exp_startp = finPoints[0];
		final Coordinate exp_endp = finPoints[ finPoints.length-1];
		
		final Coordinate[] bodyPoints = fins.getBodyPoints();
		
		assertEquals("Method should only generate minimal points for a conical transition fin body! ", 2, bodyPoints.length );
		assertEquals("incorrect body points! ", exp_startp.x, bodyPoints[0].x, EPSILON);
		assertEquals("incorrect body points! ", exp_startp.y, bodyPoints[0].y, EPSILON);
		assertEquals("incorrect body points! ", exp_endp.x, bodyPoints[1].x, EPSILON);
		assertEquals("incorrect body points! ", exp_endp.y, bodyPoints[1].y, EPSILON);
	}
	
	

	@Test
	public void testGenerateBodyPoints_GenericBase(){
		{
			Rocket rkt = createFinsOnBoattail();
			FinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 

			final double yFinFront = fins.getBodyRadius();
			final Coordinate[] finPoints = FinSet.translatePoints( fins.getFinPoints(), 0.0, yFinFront );
			
			final Coordinate exp_startp = finPoints[0];
			final Coordinate exp_endp = finPoints[ finPoints.length-1];
			
			final Coordinate[] bodyPoints = fins.getBodyPoints();
			
			// trivial, and uninteresting:
			assertEquals("incorrect body points! ", exp_startp.x, bodyPoints[0].x, EPSILON);
			assertEquals("incorrect body points! ", exp_startp.y, bodyPoints[0].y, EPSILON);
			
			// n.b.: This should match EXACTLY the end point of the fin. (in fin coordinates)
			assertEquals("incorrect body points! ",  exp_endp.x, bodyPoints[bodyPoints.length-1].x, EPSILON);
			assertEquals("incorrect body points! ",  exp_endp.y, bodyPoints[bodyPoints.length-1].y, EPSILON);
			
			{// the tests within this scope is are rather fragile, and may break for reasons other than bugs :(
				// the number of points is somewhat arbitrary, but if this test fails, the rest *definitely* will.
				assertEquals("Method is generating how many points, in general? ", 13, bodyPoints.length );

				assertEquals("incorrect body points! ", 0.01, bodyPoints[2].x, EPSILON);
				assertEquals("incorrect body points! ", 0.09615, bodyPoints[2].y, EPSILON);
				
				assertEquals("incorrect body points! ", 0.04, bodyPoints[8].x, EPSILON);
				assertEquals("incorrect body points! ", 0.08353, bodyPoints[8].y, EPSILON);
				
				assertEquals("incorrect body points! ", 0.055, bodyPoints[11].x, EPSILON);
				assertEquals("incorrect body points! ", 0.07264, bodyPoints[11].y, EPSILON);
			}
			
		}
	}

	@Test
	public void testFinCentroid_simpleBase(){
		// Start with a simple fin, on a simple body.  
		final Rocket rkt = createFinsOnTube();
		assertTrue(" Expected a 'straight' BodyTube as parent!: ", ( rkt.getChild(0).getChild(0) instanceof BodyTube));
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
		assertEquals("Calculated fin count is wrong: ", 1, fins.getFinCount() );
		
		// fin is a simple trapezoid against a "flat" parent body:
	    //          +--+        Fin length = 0.4
	    //         /   |        Fin Height = 0.4
	    //        /    |        Fin Sweep =  0.2
	    //   +---+-----+---+
	    final double avgChord = 0.3;
		final double expFinArea = 0.4*avgChord;
		assertEquals("Calculated fin area is wrong: ", expFinArea, fins.getFinWettedArea(), EPSILON);
		
	}

	@Test
	public void testFinCentroid_conicBase(){
		// next, calculate with a more complicated fin, and with a linearly-varying body:  
		final Rocket rkt = createFinsOnTransition();
		Transition body = (Transition) rkt.getChild(0).getChild(0); 
		FinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
					
		assertEquals("Calculated fin count is wrong: ", 1, fins.getFinCount() );
		assertEquals("Transition does not have a conical shape! ", Shape.CONICAL, body.getType());
		
	    // Fin length = 0.4
	    // Body Length = 1.0       
	    //           +----+        
	    //        /      +		   
	    //     /        +           
	    // +---+       +           
	    //     +---+  +            
	    //         +---+           
		//
		// Coordinate( 0.0, 0.0),
		// Coordinate( 0.3, 0.2),
		// Coordinate( 0.6, 0.2),
		// Coordinate( 0.4,-0.2)

		
		// fin is a simple trapezoid against a linearly changing body...
		final double expectedWettedArea = 0.13;
		final double actualWettedArea = fins.getFinWettedArea();
		Coordinate wcg = fins.getCG(); // relative to parent
		assertEquals("Calculated fin area is wrong: ", expectedWettedArea, actualWettedArea, EPSILON);
		assertEquals("Calculated fin centroid is wrong! ", 0.3256, wcg.x, EPSILON);
		assertEquals("Calculated fin centroid is wrong! ", 0.830769, wcg.y, EPSILON);
	
		{
			fins.setTabHeight(0.1);
			fins.setTabLength(0.1);
			fins.setTabPositionMethod(Position.TOP);
			fins.setTabShift(0.2);
			
			// fin is a simple trapezoid against a linearly changing body...
			// height is set s.t. the tab trailing edge height == 0 
			final double expectedTabArea = (fins.getTabHeight())*3/4 * fins.getTabLength();
			final double expectedTotalArea = (expectedWettedArea + expectedTabArea)*fins.getThickness();
			final double actualTotalArea = fins.getComponentVolume();
			assertEquals("Calculated fin area is wrong: ", expectedTotalArea, actualTotalArea, EPSILON);
			
			Coordinate tcg = fins.getCG(); // relative to parent.  also includes fin tab CG.
			assertEquals("Calculated fin centroid is wrong! ", 0.32121212, tcg.x, EPSILON);
			assertEquals("Calculated fin centroid is wrong! ", 0.820303, tcg.y, EPSILON);
		}
	}

	@Test
	public void testFinCentroid_genericBase (){
		Rocket rkt = createFinsOnBoattail();
		Transition body = (Transition) rkt.getChild(0).getChild(0);
		FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
					
		assertEquals("Calculated fin count is wrong: ", 1, fins.getFinCount() );
		assertEquals("Transition does not have an ellipsoidal shape! ", Shape.ELLIPSOID, body.getType());
		assertEquals("Transition has unexpected shape parameter: ", 0.5, body.getShapeParameter(), EPSILON);
		
		assertEquals("Fin Offset doesn't match! ", 0.02, fins.getAxialOffset(), EPSILON);
		assertThat(" Fin Offset Method doesn't match!", Position.TOP, equalTo(fins.getRelativePositionMethod()));
		
	    //           +----+        
	    //        /      +
	    //     /        +           
	    // +------     +           
	    //        --- +            
	    //           --+           
		//body.setForeRadius(0.1);
		//body.setLength(0.1);
		//body.setAftRadius(0.04);
		//
		//fins.setAxialOffset( Position.TOP, 0.02);
		//fins.setPoints(new Coordinate[] {
		//        new Coordinate( 0.0,   0.0),
		//        new Coordinate( 0.06,  0.04),
		//        new Coordinate( 0.08,  0.04),
		//        new Coordinate( 0.06, -0.03029),
		
		final double expectedFinArea = 0.0025813603; // estimate
		final double actualFinArea = fins.getFinWettedArea();
		Coordinate centroid = fins.getCG(); // relative to parent
		assertEquals("Calculated fin area is wrong: ", expectedFinArea, actualFinArea, EPSILON);
		assertEquals("Calculated fin centroid is wrong! ", 0.04806, centroid.x, EPSILON);
		assertEquals("Calculated fin centroid is wrong! ", 0.1066543, centroid.y, EPSILON);
	}
	
	@Test
	public void testFreeFormCGWithNegativeY() throws Exception {
		// A user submitted an ork file which could not be simulated because the fin
		// was constructed on a tail cone.  It so happened that for one pair of points
		// y_n = - y_(n+1) which caused a divide by zero and resulted in CGx = NaN.
		
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
		fins.setCrossSection( CrossSection.SQUARE );  // to ensure uniform density
		fins.setFinCount(1);
		// fins.setAxialOffset( Position.BOTTOM, 1.0);  // ERROR: no parent!
		Coordinate[] points = new Coordinate[] {
				new Coordinate(0, 0),
				new Coordinate(0, 1),
				new Coordinate(2, 1),
				new Coordinate(2, -1),
				new Coordinate(1, -1),
				new Coordinate(1, 0)
		};
		fins.setPoints( points);
		fins.setFilletRadius( 0.0);
		fins.setTabHeight( 0.0);
		fins.setMaterial( Material.newMaterial(Type.BULK, "dummy", 1.0, true));

//		assertEquals( 3.0, fins.getFinWettedArea(), EPSILON);		
//		
//		Coordinate cg = fins.getCG();
//		assertEquals( 1.1666, cg.x, EPSILON);
//		assertEquals( 0.1666, cg.y, EPSILON);
//		assertEquals( 0.0, cg.z, EPSILON);
//		assertEquals( 0.009, cg.weight, EPSILON);
	}
	
	
	@Test
	public void testConvertTrapezoidToFreeform() {
		final FinSet sourceSet = new EllipticalFinSet();
		sourceSet.setName("fins-to-convert");
		Material mat = Material.newMaterial(Type.BULK, "foo", 0.1, true);
		sourceSet.setBaseRotation(1.1);
		sourceSet.setCantAngle(0.001);
		sourceSet.setCGOverridden(true);
		sourceSet.setColor(Color.BLACK);
		sourceSet.setComment("cmt");
		sourceSet.setCrossSection(CrossSection.ROUNDED);
		sourceSet.setFinCount(5);
		sourceSet.setFinish(Finish.ROUGH);
		sourceSet.setLineStyle(LineStyle.DASHDOT);
		sourceSet.setMassOverridden(true);
		sourceSet.setMaterial(mat);
		sourceSet.setOverrideCGX(0.012);
		sourceSet.setOverrideMass(0.0123);
		sourceSet.setOverrideSubcomponents(true);
		sourceSet.setAxialOffset(Position.ABSOLUTE, 0.1);
		
		sourceSet.setTabHeight(0.01);
		sourceSet.setTabLength(0.02);
		sourceSet.setTabPositionMethod( Position.BOTTOM);
		sourceSet.setTabShift(0.015);
		sourceSet.setThickness(0.005);
		
		BodyTube dummyTube = new BodyTube( 0.2, 0.01, 0.001 );
		dummyTube.addChild( sourceSet);
		
		FreeformFinSet destSet = FreeformFinSet.convertFinSet( sourceSet);
		
		assertEquals( sourceSet.getName(), destSet.getName());
		
	}
	
	@Test
	public void testConvertEllipticaToFreeform() {
		final FinSet sourceSet = new EllipticalFinSet();
		sourceSet.setName("fins-to-convert");
	
		Material mat = Material.newMaterial(Type.BULK, "foo", 0.1, true);
		
		sourceSet.setBaseRotation(1.1);
		sourceSet.setCantAngle(0.001);
		sourceSet.setCGOverridden(true);
		sourceSet.setColor(Color.BLACK);
		sourceSet.setComment("cmt");
		sourceSet.setCrossSection(CrossSection.ROUNDED);
		sourceSet.setFinCount(5);
		sourceSet.setFinish(Finish.ROUGH);
		sourceSet.setLineStyle(LineStyle.DASHDOT);
		sourceSet.setMassOverridden(true);
		sourceSet.setMaterial(mat);
		sourceSet.setOverrideCGX(0.012);
		sourceSet.setOverrideMass(0.0123);
		sourceSet.setOverrideSubcomponents(true);
		sourceSet.setAxialOffset(Position.ABSOLUTE, 0.1);
		
		sourceSet.setTabHeight(0.01);
		sourceSet.setTabLength(0.02);
		sourceSet.setTabPositionMethod( Position.BOTTOM);
		sourceSet.setTabShift(0.015);
		sourceSet.setThickness(0.005);
		
		FreeformFinSet destSet= FreeformFinSet.convertFinSet((FinSet) sourceSet.copy());
		
		assertEquals( sourceSet.getName(), destSet.getName());

		
		
	}
	

	
}
