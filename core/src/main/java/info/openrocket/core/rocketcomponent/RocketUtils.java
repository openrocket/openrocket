package info.openrocket.core.rocketcomponent;

import java.util.Collection;

import info.openrocket.core.util.Coordinate;

public abstract class RocketUtils {

	public static double getLength(Rocket rocket) {
		double length = 0;
		Collection<Coordinate> bounds = rocket.getSelectedConfiguration().getBounds();
		if (!bounds.isEmpty()) {
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (Coordinate c : bounds) {
				if (c.getX() < minX)
					minX = c.getX();
				if (c.getX() > maxX)
					maxX = c.getX();
			}
			length = maxX - minX;
		}
		return length;
	}

}
