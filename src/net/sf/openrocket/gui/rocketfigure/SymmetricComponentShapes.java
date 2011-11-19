package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;


public class SymmetricComponentShapes extends RocketComponentShapes {
	private static final int MINPOINTS = 91;
	private static final double ACCEPTABLE_ANGLE = Math.cos(7.0 * Math.PI / 180.0);
	
	// TODO: HIGH: adaptiveness sucks, remove it.
	
	// TODO: LOW: Uses only first component of cluster (not currently clusterable)

    public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component,
                                        Transformation transformation) {
        return getShapesSide(component, transformation, S);
    }

    public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component,
                                        Transformation transformation, final double scaleFactor) {
		net.sf.openrocket.rocketcomponent.SymmetricComponent c = (net.sf.openrocket.rocketcomponent.SymmetricComponent) component;
		int i;
		
		final double delta = 0.0000001;
		double x;
		
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
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
					points.get(i + 1).x - points.get(i).x < 0.001) { // 1mm
				i++;
				continue;
			}
			
			// Split the longer of the areas
			int n;
			if (points.get(i + 2).x - points.get(i + 1).x > points.get(i + 1).x - points.get(i).x)
				n = i + 1;
			else
				n = i;
			
			x = (points.get(n).x + points.get(n + 1).x) / 2;
			points.add(n + 1, new Coordinate(x, c.getRadius(x), 0));
		}
		

		//System.out.println("Final points: "+points.size());
		
		final int len = points.size();
		
		for (i = 0; i < len; i++) {
			points.set(i, c.toAbsolute(points.get(i))[0]);
		}
		
		/*   Show points:
		Shape[] s = new Shape[len+1];
		final double d=0.001;
		for (i=0; i<len; i++) {
			s[i] = new Ellipse2D.Double(points.get(i).x()-d/2,points.get(i).y()-d/2,d,d);
		}
		*/

		//System.out.println("here");
		
		// TODO: LOW: curved path instead of linear
		Path2D.Double path = new Path2D.Double();
		path.moveTo(points.get(len - 1).x * scaleFactor, points.get(len - 1).y * scaleFactor);
		for (i = len - 2; i >= 0; i--) {
			path.lineTo(points.get(i).x * scaleFactor, points.get(i).y * scaleFactor);
		}
		for (i = 0; i < len; i++) {
			path.lineTo(points.get(i).x * scaleFactor, -points.get(i).y * scaleFactor);
		}
		path.lineTo(points.get(len - 1).x * scaleFactor, points.get(len - 1).y * scaleFactor);
		path.closePath();
		
		//s[len] = path;
		//return s;
		return new Shape[] { path };
	}
	
	private static boolean angleAcceptable(Coordinate v1, Coordinate v2, Coordinate v3) {
		return (cosAngle(v1, v2, v3) > ACCEPTABLE_ANGLE);
	}
	
	/*
	 * cosAngle = v1.v2 / |v1|*|v2| = v1.v2 / sqrt(v1.v1*v2.v2)
	 */
	private static double cosAngle(Coordinate v1, Coordinate v2, Coordinate v3) {
		double cos;
		double len;
		cos = Coordinate.dot(v1.sub(v2), v2.sub(v3));
		len = MathUtil.safeSqrt(v1.sub(v2).length2() * v2.sub(v3).length2());
		return cos / len;
	}
}
