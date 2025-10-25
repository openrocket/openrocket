package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Transformation;

import java.awt.geom.Path2D;
import java.util.ArrayList;


public class SymmetricComponentShapes extends RocketComponentShapes {
	private static final int MINPOINTS = 91;
	private static final double ACCEPTABLE_ANGLE = Math.cos(7.0 * Math.PI / 180.0);
	
	// TODO: HIGH: adaptiveness sucks, remove it.
	
	// TODO: LOW: Uses only first component of cluster (not currently clusterable)

	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return SymmetricComponent.class;
	}

	@Override
    public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {
    	return getShapesSide(component, transformation, 1.0d);
    }
    
    public static RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation, final double scaleFactor ) {


		SymmetricComponent c = (SymmetricComponent) component;
		
		int i;
		
		final double delta = 0.0000001;
		double x;
		
		ArrayList<CoordinateIF> points = new ArrayList<>();
		x = delta;
		points.add(new Coordinate(x, c.getRadius(x), 0));
		for (i = 1; i < MINPOINTS - 1; i++) {
			x = c.getLength() * i / (MINPOINTS - 1);
			points.add(new Coordinate(x, c.getRadius(x), 0));
			//System.out.println("Starting with x="+x);
		}
		x = c.getLength() - delta;
		points.add(new Coordinate(x, c.getRadius(x), 0));
		

		i = 0;
		while (i < points.size() - 2) {
			if (angleAcceptable(points.get(i), points.get(i + 1), points.get(i + 2)) ||
					points.get(i + 1).getX() - points.get(i).getX() < 0.001) { // 1mm
				i++;
				continue;
			}
			
			// Split the longer of the areas
			int n;
			if (points.get(i + 2).getX() - points.get(i + 1).getX() > points.get(i + 1).getX() - points.get(i).getX())
				n = i + 1;
			else
				n = i;
			
			x = (points.get(n).getX() + points.get(n + 1).getX()) / 2;
			points.add(n + 1, new Coordinate(x, c.getRadius(x), 0));
		}
		

		//System.out.println("Final points: "+points.size());
		
		
//		for (i = 0; i < len; i++) {
//			points.set(i, c.toAbsolute(points.get(i))[0]);
//		}
		
		/*   Show points:
		Shape[] s = new Shape[len+1];
		final double d=0.001;
		for (i=0; i<len; i++) {
			s[i] = new Ellipse2D.Double(points.get(i).getX()()-d/2,points.get(i).getY()()-d/2,d,d);
		}
		*/

		//System.out.println("here");
		
		final int len = points.size();
		CoordinateIF nose = transformation.transform(Coordinate.ZERO);
		
		// TODO: LOW: curved path instead of linear
		Path2D.Double path = new Path2D.Double();
		path.moveTo((nose.getX() + points.get(len - 1).getX()) * scaleFactor, (nose.getY()+points.get(len - 1).getY()) * scaleFactor);
		for (i = len - 2; i >= 0; i--) {
			path.lineTo((nose.getX()+points.get(i).getX()) * scaleFactor, (nose.getY()+points.get(i).getY()) * scaleFactor);
		}
		for (i = 0; i < len; i++) {
			path.lineTo((nose.getX()+points.get(i).getX()) * scaleFactor, (nose.getY()-points.get(i).getY()) * scaleFactor);
		}
		path.lineTo((nose.getX()+points.get(len - 1).getX()) * scaleFactor , (nose.getY()+points.get(len - 1).getY()) * scaleFactor);
		path.closePath();
		
		//s[len] = path;
		//return s;
		return new RocketComponentShapes[] { new RocketComponentShapes(path, component) };
	}
	
	private static boolean angleAcceptable(CoordinateIF v1, CoordinateIF v2, CoordinateIF v3) {
		return (cosAngle(v1, v2, v3) > ACCEPTABLE_ANGLE);
	}
	
	/*
	 * cosAngle = v1.v2 / |v1|*|v2| = v1.v2 / sqrt(v1.v1*v2.v2)
	 */
	private static double cosAngle(CoordinateIF v1, CoordinateIF v2, CoordinateIF v3) {
		double cos;
		double len;
		cos = CoordinateIF.dot(v1.sub(v2), v2.sub(v3));
		len = MathUtil.safeSqrt(v1.sub(v2).length2() * v2.sub(v3).length2());
		return cos / len;
	}
}
