package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;


public class FinSetShapes extends RocketComponentShape {


	public static RocketComponentShape[] getShapesSide( final RocketComponent component,
													    final Transformation transformation){
		final FinSet finset = (FinSet) component;
		
        // this supplied transformation includes: 
        //  - baseRotationTransformation
        //  - mount-radius transformation
        //  - component-center offset transformation
        //  - component-instance offset transformation        

		/**
		 *   this supplied location contains the *instance* location... but is expected to contain the *component* location. (?)
		 *   also, this requires changing machinery beyond this class. :(
		 */
		final Transformation cantRotation = finset.getCantRotation();

        final Transformation compositeTransform = transformation.applyTransformation(cantRotation);
		
		Coordinate[] finPoints = finset.getFinPoints();
        Coordinate[] tabPoints = finset.getTabPoints();
        Coordinate[] rootPoints = finset.getRootPoints();

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
		double tabHeight = finset.getTabHeight();
		
		// Generate base coordinates for a single fin
		Coordinate[] c = new Coordinate[4];
		c[0]=new Coordinate(0, 0,-thickness/2);
        c[1]=new Coordinate(0, 0,thickness/2);
        c[2]=new Coordinate(0,height,thickness/2);
        c[3]=new Coordinate(0,height,-thickness/2);

		// Generate base coordinates for a single fin tab
		Coordinate[] cTab = new Coordinate[4];
		cTab[0]=new Coordinate(0, 0,-thickness/2);
		cTab[1]=new Coordinate(0, 0,thickness/2);
		cTab[2]=new Coordinate(0, -tabHeight,thickness/2);
		cTab[3]=new Coordinate(0, -tabHeight,-thickness/2);

		// y translate the back view (if there is a fin point with non-zero y value)
		Coordinate[] points = finset.getFinPoints();
		double yOffset = Double.MAX_VALUE;
		for (Coordinate point : points) {
			yOffset = MathUtil.min(yOffset, point.y);
		}
		final Transformation translateOffsetY = new Transformation(0, yOffset, 0);
		final Transformation compositeTransform = transformation.applyTransformation(translateOffsetY);

		// Make polygon
		Shape p = makePolygonBack(c, compositeTransform);

		if (tabHeight != 0 && finset.getTabLength() != 0) {
			Shape pTab = makePolygonBack(cTab, compositeTransform);
			return new Shape[]{p, pTab};
		}
		else {
			return new Shape[]{p};
		}
	}

	private static Shape[] cantedShapesBack(FinSet finset,
												Transformation transformation) {
		if (finset.getTabHeight() == 0 || finset.getTabLength() == 0) {
			return cantedShapesBackFins(finset, transformation);
		}

		Shape[] toReturn;
		Shape[] shapesFin = cantedShapesBackFins(finset, transformation);
		Shape[] shapesTab = cantedShapesBackTabs(finset, transformation);

		toReturn = Arrays.copyOf(shapesFin, shapesFin.length + shapesTab.length);
		System.arraycopy(shapesTab, 0, toReturn, shapesFin.length, shapesTab.length);

		return toReturn;
	}

	private static Shape[] cantedShapesBackFins(FinSet finset,
												Transformation transformation) {
		double thickness = finset.getThickness();
		
		Coordinate[] sidePoints;
		Coordinate[] backPoints;
		int maxIndex;

		Coordinate[] points = finset.getFinPoints();
		
		// this loop finds the index @ max-y, as visible from the back
		for (maxIndex = points.length-1; maxIndex > 0; maxIndex--) {
			if (points[maxIndex-1].y < points[maxIndex].y)
				break;
		}
		 
		Transformation cantTransform = finset.getCantRotation();
		final Transformation compositeTransform = transformation.applyTransformation(cantTransform);
		
		sidePoints = new Coordinate[points.length];
		backPoints = new Coordinate[2*(points.length-maxIndex)];
		double sign = Math.copySign(1.0, finset.getCantAngle());

		// Calculate points for the visible side panel
		for (int i=0; i < points.length; i++) {
			sidePoints[i] = points[i].add(0,0,sign*thickness/2);
		}

		// Calculate points for the back portion
		int i=0;
		for (int j=points.length-1; j >= maxIndex; j--, i++) {
			backPoints[i] = points[j].add(0,0,sign*thickness/2);
		}
		for (int j=maxIndex; j <= points.length-1; j++, i++) {
			backPoints[i] = points[j].add(0,0,-sign*thickness/2);
		}
		
		// Generate shapes
		Shape[] s;
		if (thickness > 0.0005) {
			s = new Shape[2];
			s[0] = makePolygonBack(sidePoints,compositeTransform);
			s[1] = makePolygonBack(backPoints,compositeTransform);
		} else {
			s = new Shape[1];
			s[0] = makePolygonBack(sidePoints,compositeTransform);
		}
		
		return s;
	}

	private static Shape[] cantedShapesBackTabs(FinSet finset,
											Transformation transformation) {
		double thickness = finset.getThickness();

		Coordinate[] sidePoints;
		Coordinate[] backPoints;
		int minIndex;

		Coordinate[] points = finset.getTabPoints();

		// this loop finds the index @ min-y, as visible from the back
		for (minIndex = points.length-1; minIndex > 0; minIndex--) {
			if (points[minIndex-1].y > points[minIndex].y)
				break;
		}

		Transformation cantTransform = finset.getCantRotation();
		final Transformation compositeTransform = transformation.applyTransformation(cantTransform);

		sidePoints = new Coordinate[points.length];
		backPoints = new Coordinate[2*(points.length-minIndex)];
		double sign = Math.copySign(1.0, finset.getCantAngle());

		// Calculate points for the visible side panel
		for (int i=0; i < points.length; i++) {
			sidePoints[i] = points[i].add(0,0,sign*thickness/2);
		}

		// Calculate points for the back portion
		int i=0;
		for (int j=points.length-1; j >= minIndex; j--, i++) {
			backPoints[i] = points[j].add(0,0,sign*thickness/2);
		}
		for (int j=minIndex; j <= points.length-1; j++, i++) {
			backPoints[i] = points[j].add(0,0,-sign*thickness/2);
		}

		// Generate shapes
		Shape[] s;
		if (thickness > 0.0005) {
			s = new Shape[2];
			s[0] = makePolygonBack(sidePoints,compositeTransform);
			s[1] = makePolygonBack(backPoints,compositeTransform);
		} else {
			s = new Shape[1];
			s[0] = makePolygonBack(sidePoints,compositeTransform);
		}

		return s;
	}
	
	private static Shape makePolygonBack(Coordinate[] array, final Transformation t) {
		Path2D.Float p;

		// Make polygon
		p = new Path2D.Float();
		for (int i=0; i < array.length; i++) {
			Coordinate a = t.transform(array[i] );
			if (i==0)
				p.moveTo(a.z, a.y);
			else
				p.lineTo(a.z, a.y);			
		}
		p.closePath();
		return p;
	}

}
