package net.sf.openrocket.rocketcomponent;

import java.util.Collection;

import net.sf.openrocket.util.Coordinate;

public abstract class RocketUtils {
	
	public static double getLength(Rocket rocket) {
		double length = 0;
		Collection<Coordinate> bounds = rocket.getSelectedConfiguration().getBounds();
		if (!bounds.isEmpty()) {
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (Coordinate c : bounds) {
				if (c.x < minX)
					minX = c.x;
				if (c.x > maxX)
					maxX = c.x;
			}
			length = maxX - minX;
		}
		return length;
	}
	
	
}
