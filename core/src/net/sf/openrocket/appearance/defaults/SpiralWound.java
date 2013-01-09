package net.sf.openrocket.appearance.defaults;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

class SpiralWound extends Appearance {
	
	public static final SpiralWound ESTES_BT = new SpiralWound(new Color(212, 185, 145), .3, 45);
	public static final SpiralWound BLUE = new SpiralWound(new Color(212, 185, 145), .3, 45);
	public static final SpiralWound WHITE = new SpiralWound(new Color(240, 240, 240), .3, 45);
	
	public SpiralWound(Color paint, double shine, double angle) {
		super(
				paint,
				shine,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage("/datafiles/textures/spiral-wound-alpha.png"), EdgeMode.REPEAT));
	}
	
}
