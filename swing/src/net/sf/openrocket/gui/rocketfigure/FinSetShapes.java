package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class FinSetShapes extends RocketComponentShape {


    
	public static RocketComponentShape[] getShapesSide(
			RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
		FinSet finset = (FinSet)component;
		
		int finCount = finset.getFinCount();
        // TODO: MEDIUM: sloping radius
        double radius = finset.getBodyRadius();
        
        Transformation cantRotation = finset.getCantRotation();
		Transformation baseRotation = finset.getBaseRotationTransformation(); // rotation about x-axis
		Transformation radialTranslation = new Transformation( 0, radius, 0);
		Transformation finRotation = finset.getFinRotationTransformation();
		Transformation scale = Transformation.scale(S);
		Transformation compositeTransform = baseRotation
		                                        .applyTransformation( radialTranslation)
		                                        .applyTransformation( cantRotation)
		                                        .applyTransformation( transformation);
		                                        

		Coordinate finSetFront = componentAbsoluteLocation;
		Coordinate finPoints[] = finset.getFinPoints();
        Coordinate tabPoints[] = finset.getTabPoints();
        Coordinate basePoints[] = finset.getRootPoints();
		
		// Translate & rotate points into place
        finPoints = compositeTransform.transform( finPoints );
        tabPoints = compositeTransform.transform( tabPoints);
        basePoints = compositeTransform.transform( basePoints );
        
		// Generate shapes
        ArrayList<RocketComponentShape> shapeList = new ArrayList<>();
        for (int finNum=0; finNum<finCount; finNum++) {
            Coordinate curPoint;
            
            // Make fin polygon
            Path2D.Float finShape = new Path2D.Float();
            for (int i=0; i<finPoints.length; i++) {
                curPoint = scale.transform( finSetFront.add(finPoints[i]));
                
                if (i==0)
                    finShape.moveTo(curPoint.x, curPoint.y);
                else
                    finShape.lineTo(curPoint.x, curPoint.y);         
            }
            shapeList.add( new RocketComponentShape( finShape, finset));

            // draw fin-body intersection line
            double angle_rad = finset.getBaseRotation() + ((double)finNum) / ((double)finCount) *2*Math.PI;
            // only draw body-root intersection line if it's not hidden-- i.e. is not at {0,PI/2,PI,3/2*PI} angles
            final boolean drawRoot= (0.05 < Math.abs( angle_rad % (Math.PI/2.0)));
            boolean simpleRoot = finset.isRootStraight( );
            if( drawRoot){
                if( simpleRoot){
                    // draws a straight-line connection from the end back to the start
                    finShape.closePath();
                }else{
                    // this implies a curved fin-body intersection 
                    // ... which is more complicated.
                    Path2D.Float rootShape = new Path2D.Float();
                    for (int i=0; i< basePoints.length; i++) {
                        curPoint = scale.transform( finSetFront.add( basePoints[i]));
                        
                        if (i==0)
                            rootShape.moveTo(curPoint.x, curPoint.y);
                        else
                            rootShape.lineTo(curPoint.x, curPoint.y);   
                    }
                    
                    shapeList.add( new RocketComponentShape( rootShape, finset));
                }
            }
            
            // Make tab polygon
            Path2D.Float tabShape = new Path2D.Float();
            if( 0 < tabPoints.length ){
                for (int i=0; i<tabPoints.length; i++) {
                    curPoint = scale.transform( finSetFront.add(tabPoints[i]));
                    
                    if (i==0)
                        tabShape.moveTo(curPoint.x, curPoint.y);
                    else
                        tabShape.lineTo(curPoint.x, curPoint.y);         
                }
                
                // the fin tab / body surface line should lay on the fin-root line above 
            
                shapeList.add( new RocketComponentShape( tabShape, finset));
            }

            // Rotate fin, tab coordinates
            finPoints = finRotation.transform(finPoints);
            tabPoints = finRotation.transform(tabPoints);
            basePoints = finRotation.transform( basePoints);
        }
        
		return shapeList.toArray(new RocketComponentShape[0]);
	}
	
	public static RocketComponentShape[] getShapesBack(
			RocketComponent component, 
			Transformation transformation,
			Coordinate location) {
	
		FinSet finset = (FinSet)component; 
		
		Shape[] toReturn;

		if (MathUtil.equals(finset.getCantAngle(),0)){
			toReturn = uncantedShapesBack(finset, transformation, location);
		}else{
			toReturn = cantedShapesBack(finset, transformation, location);
		}
		
		
		return RocketComponentShape.toArray( toReturn, finset);
	}
	
	
	private static Shape[] uncantedShapesBack(FinSet finset,
			Transformation transformation,
			Coordinate location) {
		
		int fins = finset.getFinCount();
		double radius = finset.getBodyRadius();
		double thickness = finset.getThickness();
		double height = finset.getSpan();
		Coordinate compCenter = location;
		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();
		

		// Generate base coordinates for a single fin
		Coordinate c[] = new Coordinate[4];
		c[0]=new Coordinate(0,radius,-thickness/2);
		c[1]=new Coordinate(0,radius,thickness/2);
		c[2]=new Coordinate(0,height+radius,thickness/2);
		c[3]=new Coordinate(0,height+radius,-thickness/2);

		// Apply base rotation
		transformPoints(c,baseRotation);
		
		// Generate shapes
		Shape[] s = new Shape[fins];
		for (int fin=0; fin<fins; fin++) {
			Coordinate a;
			Path2D.Double p;

			// Make polygon
			p = new Path2D.Double();
			a = transformation.transform(compCenter.add( c[0] ));
			p.moveTo(a.z*S, a.y*S);
			a = transformation.transform(compCenter.add( c[1] ));
			p.lineTo(a.z*S, a.y*S);			
			a = transformation.transform(compCenter.add( c[2] ));
			p.lineTo(a.z*S, a.y*S);			
			a = transformation.transform(compCenter.add( c[3] ));
			p.lineTo(a.z*S, a.y*S);	
			p.closePath();
			s[fin] = p;

			// Rotate fin coordinates
			transformPoints(c,finRotation);
		}
		
		return s;
	}
	
	
	// TODO: LOW:  Jagged shapes from back draw incorrectly.
	private static Shape[] cantedShapesBack(FinSet finset,
			Transformation transformation,
			Coordinate location) {
		int i;
		int fins = finset.getFinCount();
		double radius = finset.getBodyRadius();
		double thickness = finset.getThickness();
		
		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();
		Transformation cantRotation = finset.getCantRotation();

		Coordinate[] sidePoints;
		Coordinate[] backPoints;
		int maxIndex;

		Coordinate[] points = finset.getFinPoints();
		for (maxIndex = points.length-1; maxIndex > 0; maxIndex--) {
			if (points[maxIndex-1].y < points[maxIndex].y)
				break;
		}
		
		transformPoints(points,cantRotation);
		transformPoints(points,new Transformation(0,radius,0));
		transformPoints(points,baseRotation);
		
		
		sidePoints = new Coordinate[points.length];
		backPoints = new Coordinate[2*(points.length-maxIndex)];
		double sign;
		if (finset.getCantAngle() > 0) {
			sign = 1.0;
		} else {
			sign = -1.0;
		}			
			
		// Calculate points for the side panel
		for (i=0; i < points.length; i++) {
			sidePoints[i] = points[i].add(0,0,sign*thickness/2);
		}

		// Calculate points for the back portion
		i=0;
		for (int j=points.length-1; j >= maxIndex; j--, i++) {
			backPoints[i] = points[j].add(0,0,sign*thickness/2);
		}
		for (int j=maxIndex; j <= points.length-1; j++, i++) {
			backPoints[i] = points[j].add(0,0,-sign*thickness/2);
		}
		
		// Generate shapes
		Shape[] s;
		if (thickness > 0.0005) {
			
			s = new Shape[fins*2];
			for (int fin=0; fin<fins; fin++) {
				
				s[2*fin] = makePolygonBack(sidePoints,finset,transformation, location);
				s[2*fin+1] = makePolygonBack(backPoints,finset,transformation, location);
				
				// Rotate fin coordinates
				transformPoints(sidePoints,finRotation);
				transformPoints(backPoints,finRotation);
			}
			
		} else {
			
			s = new Shape[fins];
			for (int fin=0; fin<fins; fin++) {
				s[fin] = makePolygonBack(sidePoints,finset,transformation, location);
				transformPoints(sidePoints,finRotation);
			}
			
		}
		
		return s;
	}
	
	
	
	private static void transformPoints(Coordinate[] array, Transformation t) {
		for (int i=0; i < array.length; i++) {
			array[i] = t.transform(array[i]);
		}
	}
	
	private static Shape makePolygonBack(Coordinate[] array, FinSet finset, 
			Transformation t, Coordinate location) {
		Path2D.Float p;

		Coordinate compCenter = location;
		// Make polygon
		p = new Path2D.Float();
		for (int i=0; i < array.length; i++) {
			Coordinate a = t.transform(compCenter.add( array[i]) );
			if (i==0)
				p.moveTo(a.z*S, a.y*S);
			else
				p.lineTo(a.z*S, a.y*S);			
		}
		p.closePath();
		return p;
	}
	
	
	/*  Side painting with thickness:

		Coordinate c[] = new Coordinate[8];
		
		c[0]=new Coordinate(0-position*rootChord,radius,thickness/2);
		c[1]=new Coordinate(rootChord-position*rootChord,radius,thickness/2);
		c[2]=new Coordinate(sweep+tipChord-position*rootChord,height+radius,thickness/2);
		c[3]=new Coordinate(sweep-position*rootChord,height+radius,thickness/2);
		
		c[4]=new Coordinate(0-position*rootChord,radius,-thickness/2);
		c[5]=new Coordinate(rootChord-position*rootChord,radius,-thickness/2);
		c[6]=new Coordinate(sweep+tipChord-position*rootChord,height+radius,-thickness/2);
		c[7]=new Coordinate(sweep-position*rootChord,height+radius,-thickness/2);
		
		if (rotation != 0) {
			rot = Transformation.rotate_x(rotation);
			for (int i=0; i<8; i++)
				c[i] = rot.transform(c[i]);
		}
		
		Shape[] s = new Shape[fins*6];
		rot = Transformation.rotate_x(2*Math.PI/fins);
		
		for (int fin=0; fin<fins; fin++) {
			Coordinate a,b;
			Path2D.Float p;

			// First polygon
			p = new Path2D.Float();
			a = finset.toAbsolute(c[0]);
			p.moveTo(a.x(), a.y());
			a = finset.toAbsolute(c[1]);
			p.lineTo(a.x(), a.y());			
			a = finset.toAbsolute(c[2]);
			p.lineTo(a.x(), a.y());			
			a = finset.toAbsolute(c[3]);
			p.lineTo(a.x(), a.y());	
			p.closePath();
			s[fin*6] = p;
			
			// Second polygon
			p = new Path2D.Float();
			a = finset.toAbsolute(c[4]);
			p.moveTo(a.x(), a.y());
			a = finset.toAbsolute(c[5]);
			p.lineTo(a.x(), a.y());			
			a = finset.toAbsolute(c[6]);
			p.lineTo(a.x(), a.y());			
			a = finset.toAbsolute(c[7]);
			p.lineTo(a.x(), a.y());	
			p.closePath();
			s[fin*6+1] = p;
			
			// Single lines
			for (int i=0; i<4; i++) {
				a = finset.toAbsolute(c[i]);
				b = finset.toAbsolute(c[i+4]);
				s[fin*6+2+i] = new Line2D.Float((float)a.x(),(float)a.y(),(float)b.x(),(float)b.y());
			}

			// Rotate fin coordinates
			for (int i=0; i<8; i++)
				c[i] = rot.transform(c[i]);
		}
		
	 */
}
