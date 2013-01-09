package net.sf.openrocket.appearance.defaults;

import java.util.HashMap;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

public class DefaultAppearance {
	
	private static Appearance BALSA = new
			Appearance(
					new Color(1, 1, 1),
					0,
					new Decal(
							new Coordinate(0, 0),
							new Coordinate(0, 0),
							new Coordinate(1, 1),
							0,
							new ResourceDecalImage("/datafiles/textures/balsa.png"), EdgeMode.REPEAT));
	
	private static Appearance WOOD = new
			Appearance(
					new Color(1, 1, 1),
					0,
					new Decal(
							new Coordinate(0, 0),
							new Coordinate(0, 0),
							new Coordinate(1, 1),
							0,
							new ResourceDecalImage("/datafiles/textures/wood.png"), EdgeMode.REPEAT));
	
	public static final Appearance ESTES_BT = new Appearance(
			new Color(212, 185, 145),
			.3,
			new Decal(
					new Coordinate(0, 0),
					new Coordinate(0, 0),
					new Coordinate(1, 3),
					0,
					new ResourceDecalImage("/datafiles/textures/spiral-wound-alpha.png"), EdgeMode.REPEAT));
	
	public static final Appearance WHITE_BT = new Appearance(
			new Color(240, 240, 240),
			.3,
			new Decal(
					new Coordinate(0, 0),
					new Coordinate(0, 0),
					new Coordinate(1, 3),
					0,
					new ResourceDecalImage("/datafiles/textures/spiral-wound-alpha.png"), EdgeMode.REPEAT));
	
	
	private static HashMap<Color, Appearance> plastics = new HashMap<Color, Appearance>();
	
	public static Appearance getPlastic(Color c) {
		if (!plastics.containsKey(c)) {
			plastics.put(c, new Appearance(c, .3));
		}
		return plastics.get(c);
	}
	
	public static Appearance getDefaultAppearance(RocketComponent c) {
		if (c instanceof BodyTube || c instanceof InnerTube || c instanceof TubeCoupler)
			return ESTES_BT;
		if (c instanceof FinSet)
			return BALSA;
		if (c instanceof LaunchLug)
			return WHITE_BT;
		if (c instanceof Transition)
			return getPlastic(new Color(255, 255, 255));
		if (c instanceof RadiusRingComponent)
			return WOOD;
		return Appearance.MISSING;
	}
}
