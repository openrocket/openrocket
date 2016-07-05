package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FinSetTest extends BaseTestCase {
	final double EPSILON = 0.0001;
	
	@Test
	public void testTrapezoidCGComputation() {
		
		{
			// This is a simple square fin with sides of 1.0.
			TrapezoidFinSet fins = new TrapezoidFinSet();
			fins.setFinCount(1);
			fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
			
			Coordinate coords = fins.getCG();
			assertEquals(1.0, fins.getFinArea(), 0.001);
			assertEquals(0.5, coords.x, 0.001);
			assertEquals(0.5, coords.y, 0.001);
		}
		
		{
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
			assertEquals(0.75, fins.getFinArea(), 0.001);
			assertEquals(0.3889, coords.x, 0.001);
			assertEquals(0.4444, coords.y, 0.001);
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

         final Position[] pos={Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM};
         final double[] expOffs = {1.0, 0.0, 0.4, -0.2};
         final double[] expPos = {1.0, 0.5, 0.9, 0.8};
         for( int caseIndex=0; caseIndex < pos.length; ++caseIndex ){
             fins.setAxialOffset( pos[caseIndex], expOffs[caseIndex]);
             
             final double actOffset = fins.getAxialOffset();
             assertEquals(String.format(" Relative Positioning doesn't match for: (%6.2g via:%s)\n", expOffs[caseIndex], pos[caseIndex].name()),
            		 expOffs[caseIndex], actOffset, EPSILON);
             
             final double actPos = fins.getLocations()[0].x;
             assertEquals(String.format(" Relative Positioning doesn't match for: (%6.2g via:%s)\n", expOffs[caseIndex], pos[caseIndex].name()),
            		 expPos[caseIndex], actPos, EPSILON);
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
         FinSet fins = new TrapezoidFinSet( 1, 0.05, 0.02, 0.03, 0.025);
         fins.setAxialOffset( Position.MIDDLE, 0.0);
         body.addChild(fins);
         // fins.length = 0.05;
         fins.setTabLength(0.01);

         final Position[] pos={Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM};
         final double[] expShift = {0.1, 0.0, 0.01, -0.02};
         final double[] expFront = {0.1, 0.02, 0.03, 0.02};
         for( int caseIndex=0; caseIndex < pos.length; ++caseIndex ){
     	 	fins.setTabRelativePosition( pos[caseIndex]);
     	 	fins.setTabShift( expShift[caseIndex]);
            double actFront= fins.getTabFrontEdge();
    	 	double actShift = fins.getTabShift();
    	 	
    	 	assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
           		 	expFront[caseIndex], actFront, EPSILON);
            
            assertEquals(String.format(" Relative Positioning doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
            		expShift[caseIndex], actShift, EPSILON);
            
            fins.setTabRelativePosition( Position.TOP);
            actShift = fins.getTabShift();
            actFront = fins.getTabFrontEdge();
            
            assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
                 expFront[caseIndex], actFront, EPSILON);
            assertEquals(String.format(" Relative Positioning doesn't match when reshift to top, from "+pos[caseIndex].name()),
           		 expFront[caseIndex], actShift, EPSILON); 
         }
    }
    
    
    @Test 
    public void testGetTabShiftAs() {
        final Position[] pos={Position.TOP, Position.MIDDLE, Position.MIDDLE, Position.BOTTOM};
        final double[] expTop = {0.1, 0.04, 0.07, 0.06};
        final double[] expShift = {0.1, 0.0, 0.03, -0.02};
        final double finLength = 0.10;
        final double tabLength = 0.02;
        
        for( int caseIndex=0; caseIndex < pos.length; ++caseIndex ){
        	double actShift = Position.shiftViaMethod( expTop[caseIndex], pos[caseIndex], finLength, tabLength);
        	assertEquals(String.format("Returned shift doesn't match for: (%6.2g via:%s)\n", expTop[caseIndex], pos[caseIndex].name()),
          		 	expShift[caseIndex], actShift, EPSILON);
        	
        	
        	double actTop = Position.getAsTop( expShift[caseIndex], pos[caseIndex], finLength, tabLength );
        	assertEquals(String.format("Returned front doesn't match for: (%6.2g via:%s)\n", expShift[caseIndex], pos[caseIndex].name()),
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
         TrapezoidFinSet fins = new TrapezoidFinSet( 1, 0.05, 0.02, 0.03, 0.025);
         fins.setAxialOffset( Position.MIDDLE, 0.0);
         body.addChild(fins);
         // fins.length = 0.05;
         fins.setTabLength(0.01);

         
     	fins.setTabRelativePosition( Position.MIDDLE);
 	 	fins.setTabShift( 0.0 );
     
 	 	final double expFrontFirst = 0.02;
 	 	final double actFrontFirst = fins.getTabFrontEdge();
	 	assertEquals(String.format(" Front edge doesn't match for: (%6.2g via:%s)\n", fins.getTabFrontEdge(), Position.MIDDLE.name()),
       		 	expFrontFirst, actFrontFirst, EPSILON);
    
     	fins.setRootChord(0.08);
     	
	 	final double expFrontSecond = 0.035;
 	 	final double actFrontSecond = fins.getTabFrontEdge();
	 	assertEquals(" Front edge doesn't match after adjusting root chord...",expFrontSecond, actFrontSecond, EPSILON);
    }
    
    
}
