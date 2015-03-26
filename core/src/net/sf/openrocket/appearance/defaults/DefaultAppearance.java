package net.sf.openrocket.appearance.defaults;

import java.util.HashMap;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

public class DefaultAppearance {
	
	private static Appearance simple(String resource) {
		return new Appearance(
				new Color(1, 1, 1),
				0,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	};
	
	private static Appearance simpleAlpha(Color base, float shine, String resource) {
		return new Appearance(
				base,
				shine,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	};
	
	private static Appearance BALSA = simple("/datafiles/textures/balsa.jpg");
	private static Appearance WOOD = simple("/datafiles/textures/wood.jpg");
	@SuppressWarnings("unused")
	private static Appearance CARDBOARD = simple("/datafiles/textures/cardboard.jpg");
	private static Appearance HARDBOARD = simple("/datafiles/textures/hardboard.jpg");
	private static Appearance WADDING = simple("/datafiles/textures/wadding.png");
	private static Appearance CHUTE = simple("/datafiles/textures/chute.jpg");
	
	
	private static final Appearance ESTES_BT = simpleAlpha(new Color(212, 185, 145), .3f, "/datafiles/textures/spiral-wound-alpha.png");
	private static final Appearance ESTES_IT = simpleAlpha(new Color(168, 146, 116), .1f, "/datafiles/textures/spiral-wound-alpha.png");
	private static final Appearance WHITE_BT = simpleAlpha(new Color(240, 240, 240), .3f, "/datafiles/textures/spiral-wound-alpha.png");
	
	private static Appearance ESTES_MOTOR = simple("/datafiles/textures/motors/estes.jpg");
	private static Appearance AEROTECH_MOTOR = simple("/datafiles/textures/motors/aerotech.png");
	private static Appearance KLIMA_MOTOR = simple("/datafiles/textures/motors/klima.jpg");
	private static Appearance REUSABLE_MOTOR = simpleAlpha(new Color(195, 60, 50), .6f, "/datafiles/textures/motors/reusable.png");
	
	private static HashMap<Color, Appearance> plastics = new HashMap<Color, Appearance>();
	
	private static Appearance getPlastic(Color c) {
		if (!plastics.containsKey(c)) {
			plastics.put(c, new Appearance(c, .3));
		}
		return plastics.get(c);
	}
	
	public static Appearance getDefaultAppearance(RocketComponent c) {
		if (c instanceof BodyTube)
			return ESTES_BT;
		if (c instanceof InnerTube || c instanceof TubeCoupler || c instanceof TubeFinSet)
			return ESTES_IT;
		if (c instanceof FinSet)
			return BALSA;
		if (c instanceof LaunchLug)
			return WHITE_BT;
		if (c instanceof Transition)
			return getPlastic(new Color(255, 255, 255));
		if (c instanceof RadiusRingComponent)
			return WOOD;
		if (c instanceof Parachute)
			return CHUTE;
		if (c instanceof EngineBlock)
			return HARDBOARD;
		if (c instanceof MassObject)
			return WADDING;
		
		return Appearance.MISSING;
	}
	
	public static Appearance getDefaultAppearance(Motor m) {
		if (m instanceof ThrustCurveMotor) {
			ThrustCurveMotor tcm = (ThrustCurveMotor) m;
			if ("Estes".equals(tcm.getManufacturer().getSimpleName())) {
				return ESTES_MOTOR;
			}
			if ("AeroTech".equals(tcm.getManufacturer().getSimpleName())) {
				return AEROTECH_MOTOR;
			}
			if ("Klima".equals(tcm.getManufacturer().getSimpleName())) {
				return KLIMA_MOTOR;
			}
		}
		return REUSABLE_MOTOR;
	}
}
