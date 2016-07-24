package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

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
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FreeformFinSetTest extends BaseTestCase {
	final double EPSILON = 0.0001;
	
    public Rocket createFinsOnTube() {
    	Rocket rkt = new Rocket();
        AxialStage stg = new AxialStage();
	    rkt.addChild(stg);
	    BodyTube body = new BodyTube(1.0, 0.01);
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
	                   new Coordinate(0.0, 0.0),
	                   new Coordinate(0.2, 0.4),
	                   new Coordinate(0.4, 0.4),
	                   new Coordinate(0.4, 0.0)
	    };
	    fins.setPoints(initPoints);
	    body.addChild(fins);
	    fins.setAxialOffset( Position.TOP, 0.4);
	    
	    return rkt;
    }
    
    
    

    public Rocket createFinsOnTransition() {
    	Rocket rkt = new Rocket();
        AxialStage stg = new AxialStage();
	    rkt.addChild(stg);
	    Transition body = new Transition();
	    body.setForeRadius(1.0);
	    body.setLength(1.0);
	    body.setAftRadius(0.5);
	    // slope = .5/1.0 = 0.5
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
	    fins.setAxialOffset( Position.TOP, 0.4);
	    Coordinate[] initPoints = new Coordinate[] {
	                   new Coordinate( 0.0, 0.0),
	                   new Coordinate( 0.2, 0.4),
	                   new Coordinate( 0.4, 0.4),
	                   new Coordinate( 0.4,-0.2)
	    };
	    fins.setPoints(initPoints);
	    body.addChild(fins);
	    fins.setAxialOffset( Position.TOP, 0.4);
	    
	    return rkt;
    }
    
    
    
    @Test
    public void testSetPoint_firstPoint() {
    	// combine the simple case with the complicated to ensure that the simple case is flagged, tested, and debugged before running the more complicated case...
    	{ // setting points on a Tube Body is the simpler case. Test this first:
	    	Rocket rkt = createFinsOnTube();
	    	FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0); 
	    	
	    	Coordinate act_p_0;
	    	// first point x is restricted to the front of the parent body:
	    	fins.setPoint(0, -1, 0);
		    act_p_0 = fins.getFinPoints()[0];
		    assertEquals( 0.0, act_p_0.x, EPSILON);
		    assertEquals( 0.0, fins.getAxialOffset(), EPSILON);
			
	        // first point y is restricted to the body
		    fins.setPoint(0, 0, -1);
		    act_p_0 = fins.getFinPoints()[0];
			assertEquals( 0.0, act_p_0.y, EPSILON);
			
			// moving the first point relocates the rest of the points:
			// i.e. the points don't move relative to p0
			Coordinate exp_p_1 = fins.getFinPoints()[1].add( -0.2, 0, 0);
			fins.setPoint(0,  0.2, 0);
			Coordinate act_p_1 = fins.getFinPoints()[1];
			assertEquals("moving fin start point doesn't move other fin point: x: ", exp_p_1.x, act_p_1.x, EPSILON);
			assertEquals("moving fin start point doesn't move other fin point: y: ", exp_p_1.y, act_p_1.y, EPSILON);
		}
		{ // more transitions trigger more complicated positioning math:  
			Rocket rkt = createFinsOnTransition();
			FreeformFinSet fins = (FreeformFinSet) rkt.getChild(0).getChild(0).getChild(0);
			
	    	Coordinate act_p_0;
	    	// first point x is restricted to the front of the parent body:
		    fins.setPoint(0, -1, 0);
		    act_p_0 = fins.getFinPoints()[0];
		    assertEquals( 0.0, act_p_0.x, EPSILON);
		    assertEquals( 0.0, fins.getAxialOffset(), EPSILON);
			
	        // first point y is restricted to the body
		    fins.setPoint(0, 0, -1);
		    act_p_0 = fins.getFinPoints()[0];
			assertEquals( 0.0, act_p_0.y, EPSILON);
		}
    }
    
    
    @Test
    public void testSetPoint_lastPoint_TubeBody() {
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
	    	
    		// move last point, and verify that its y-value is still clamped to the body ( at the new location) 
    		exp_p_l = new Coordinate( 0.6, -0.3, 0.0);
    		fins.setPoint(lastIndex, 0.6, 0.0); // w/ incorrect y-val.  The function should correct the y-value as above. 
    		
    		act_p_l = fins.getFinPoints()[lastIndex];
    		assertEquals( exp_p_l.x, act_p_l.x, EPSILON);
    		assertEquals( exp_p_l.y, act_p_l.y, EPSILON);
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
			assertEquals(0.75, fins.getFinArea(), 0.001);
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
			assertEquals(0.75, fins.getFinArea(), 0.001);
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
			assertEquals(0.75, fins.getFinArea(), 0.001);
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
		assertEquals(0.00130, fins.getFinArea(), 0.00001);
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
	public void testFinArea(){
		Rocket rkt = TestRockets.makeV2();
		FreeformFinSet fins = (FreeformFinSet)rkt.getChild(0).getChild(2).getChild(0);
		fins.setFinCount(1);
		assertThat( fins.getFinCount(), equalTo(1));
		
		// under development

		assertEquals("Calculated fin area is wrong: ", 0.006805, fins.getFinArea(), EPSILON);
		
	}
	
	@Test
	public void testFinCM(){
		Rocket rkt = TestRockets.makeV2();
		FreeformFinSet fins = (FreeformFinSet)rkt.getChild(0).getChild(2).getChild(0);
		fins.setFinCount(1);
		assertThat( fins.getFinCount(), equalTo(1));
		
		// under development
		Coordinate cg = fins.getCG();
		assertEquals("Calculated fin CM.x is wrong: ", 0.0949629, cg.x, EPSILON);
		assertEquals("Calculated fin CM.y is wrong: ", 0.0, cg.y, EPSILON);
		assertEquals("Calculated fin CM.z is wrong: ", 0.0, cg.z, EPSILON);
		
	}
	
	
	@Test
	public void testFinCG(){
		Rocket rkt = TestRockets.makeV2();
		FreeformFinSet fins = (FreeformFinSet)rkt.getChild(0).getChild(2).getChild(0);
		fins.setFinCount(1);
		assertThat( fins.getFinCount(), equalTo(1));
		
		final double calcMass = fins.getMass();
		assertEquals( 0.0127693, calcMass, EPSILON);
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

		assertEquals( 3.0, fins.getFinArea(), EPSILON);		
		
		Coordinate cg = fins.getCG();
		assertEquals( 1.1666, cg.x, EPSILON);
		assertEquals( 0.1666, cg.y, EPSILON);
		assertEquals( 0.0, cg.z, EPSILON);
		assertEquals( 0.009, cg.weight, EPSILON);
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
		sourceSet.setTabRelativePosition( Position.BOTTOM);
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
		sourceSet.setTabRelativePosition( Position.BOTTOM);
		sourceSet.setTabShift(0.015);
		sourceSet.setThickness(0.005);
		
		FreeformFinSet destSet= FreeformFinSet.convertFinSet((FinSet) sourceSet.copy());
		
		assertEquals( sourceSet.getName(), destSet.getName());

		
		
	}
	

	
}
