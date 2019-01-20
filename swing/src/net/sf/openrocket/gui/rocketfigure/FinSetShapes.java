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


	public static RocketComponentShape[] getShapesSide( final RocketComponent component,
													    final Transformation transformation){
		final FinSet finset = (FinSet) component;
		
        // this supplied transformation includes: 
        //  - baseRotationTransformation
        //  - mount-radius transformtion
        //  - component-center offset transformation
        //  - component-instance offset transformation        

		/**
		 *   this supplied location contains the *instance* location... but is expected to contain the *component* location. (?)
		 *   also, this requires changing machinery beyond this class. :(
		 */
		final Transformation cantRotation = finset.getCantRotation();

        final Transformation compositeTransform = cantRotation.applyTransformation( transformation);
		
		Coordinate finPoints[] = finset.getFinPoints();
        Coordinate tabPoints[] = finset.getTabPoints();
        Coordinate rootPoints[] = finset.getRootPoints();

		// Translate & rotate points into place
        finPoints = compositeTransform.transform( finPoints );
        tabPoints = compositeTransform.transform( tabPoints);
        rootPoints = compositeTransform.transform( rootPoints );
        
		// Generate shapes
		ArrayList<RocketComponentShape> shapeList = new ArrayList<>();
		
		// Make fin polygon
		shapeList.add(new RocketComponentShape(generatePath(finPoints), finset));

        // Make fin polygon
        shapeList.add(new RocketComponentShape(generatePath(tabPoints), finset));

        // Make fin polygon
        shapeList.add(new RocketComponentShape(generatePath(rootPoints), finset));

		return shapeList.toArray(new RocketComponentShape[0]);
	}

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {

		FinSet finset = (FinSet) component;
		
		Shape[] toReturn;

		if (MathUtil.equals(finset.getCantAngle(), 0)) {
			toReturn = uncantedShapesBack(finset, transformation);
		} else {
			toReturn = cantedShapesBack(finset, transformation);
		}


		return RocketComponentShape.toArray(toReturn, finset);
	}

	private static Path2D.Float generatePath(final Coordinate[] points){
		Path2D.Float finShape = new Path2D.Float();
		for( int i = 0; i < points.length; i++){
			Coordinate curPoint = points[i];
			if (i == 0)
				finShape.moveTo(curPoint.x, curPoint.y);
			else
				finShape.lineTo(curPoint.x, curPoint.y);
		}
		return finShape;
	}
	
	private static Shape[] uncantedShapesBack(FinSet finset,
			Transformation transformation) {
		
		double thickness = finset.getThickness();
		double height = finset.getSpan();
		
		// Generate base coordinates for a single fin
		Coordinate c[] = new Coordinate[4];
		c[0]=new Coordinate(0, 0,-thickness/2);
        c[1]=new Coordinate(0, 0,thickness/2);
        c[2]=new Coordinate(0,height,thickness/2);
        c[3]=new Coordinate(0,height,-thickness/2);

		// Apply base rotation
		c = transformation.transform(c);
          
		// Make polygon
		Coordinate a;
		Path2D.Double p = new Path2D.Double();
		
	    a = c[0];
		p.moveTo(a.z, a.y);
		a = c[1];
		p.lineTo(a.z, a.y);			
		a = c[2];
		p.lineTo(a.z, a.y);		
		a = c[3];
		p.lineTo(a.z, a.y);
		p.closePath();
		
		return new Shape[]{p};
	}
	
	
	// TODO: LOW:  Jagged shapes from back draw incorrectly.
	private static Shape[] cantedShapesBack(FinSet finset,
			Transformation transformation) {
		int i;
		int fins = finset.getFinCount();
		double thickness = finset.getThickness();
		
		Transformation cantRotation = finset.getCantRotation();

		Coordinate[] sidePoints;
		Coordinate[] backPoints;
		int maxIndex;

		Coordinate[] points = finset.getFinPoints();
		for (maxIndex = points.length-1; maxIndex > 0; maxIndex--) {
			if (points[maxIndex-1].y < points[maxIndex].y)
				break;
		}
		
		points = cantRotation.transform( points );
//		transformPoints(points,new Transformation(0,radius,0));
		points = transformation.transform( points );
		
		
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
				
				s[2*fin] = makePolygonBack(sidePoints,finset,transformation);
				s[2*fin+1] = makePolygonBack(backPoints,finset,transformation);
			}
			
		} else {
			
			s = new Shape[fins];
			for (int fin=0; fin<fins; fin++) {
				s[fin] = makePolygonBack(sidePoints,finset,transformation);
			}
			
		}
		
		return s;
	}
	
	private static Shape makePolygonBack(Coordinate[] array, FinSet finset, final Transformation t) {
		Path2D.Float p;

		Coordinate compCenter = t.transform(Coordinate.ZERO);
		
		// Make polygon
		p = new Path2D.Float();
		for (int i=0; i < array.length; i++) {
			Coordinate a = t.transform(compCenter.add( array[i]) );
			if (i==0)
				p.moveTo(a.z, a.y);
			else
				p.lineTo(a.z, a.y);			
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
