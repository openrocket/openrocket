package net.sf.openrocket.appearance.defaults;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class DefaultAppearance {
	public static Appearance getDefaultAppearance(RocketComponent c) {
		if (c instanceof BodyTube)
			return SpiralWound.ESTES_BT;
		if (c instanceof FinSet)
			return Balsa.INSTANCE;
		if (c instanceof LaunchLug)
			return SpiralWound.WHITE;
		return Appearance.MISSING;
	}
}
