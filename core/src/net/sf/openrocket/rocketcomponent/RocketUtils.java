// this class is only used by the Android application
package net.sf.openrocket.rocketcomponent;

import java.util.Collection;

import net.sf.openrocket.util.Coordinate;

public abstract class RocketUtils {
	
	public static double getLength(Rocket rocket) {
		return rocket.getSelectedConfiguration().getLength();
	}
}
