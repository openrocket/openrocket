package net.sf.openrocket.appearance.defaults;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

class Balsa extends Appearance {
	public static final Balsa INSTANCE = new Balsa();
	
	private Balsa() {
		super(
				new Color(1, 1, 1),
				0,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage("/datafiles/textures/balsa.png"), EdgeMode.REPEAT));
	}
}
