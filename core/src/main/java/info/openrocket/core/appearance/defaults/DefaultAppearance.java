package info.openrocket.core.appearance.defaults;

import java.util.HashMap;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.Decal.EdgeMode;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RadiusRingComponent;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Coordinate;

/**
 * 
 * Class defining the default images of the application
 *
 */
public class DefaultAppearance {
	
	/**
	 * returns a simple appearance with the image in the path with 
	 * default color
	 * no shining	
	 * no offset, origin center and scale 1
	 * 
	 * @param resource	the path file to the resource
	 * @return
	 */
	private static Appearance simple(String resource) {
		return new Appearance(
				new ORColor(1, 1, 1),
				0,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	}

	/**
	 * returns the image with custom color and shine
	 * 
	 * @param base		base color for the image 
	 * @param shine		the custom shine property
	 * @param resource	the file path to the image
	 * @return	The appearance with custom color and shine.
	 */
	private static Appearance simpleAlpha(ORColor base, float shine, String resource) {
		return new Appearance(
				base,
				shine,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	}

	private static final Appearance BALSA = simple("/datafiles/textures/balsa.jpg");
	private static final Appearance WOOD = simple("/datafiles/textures/wood.jpg");
	@SuppressWarnings("unused")
	private static final Appearance CARDBOARD = simple("/datafiles/textures/cardboard.jpg");
	private static final Appearance HARDBOARD = simple("/datafiles/textures/hardboard.jpg");
	private static final Appearance WADDING = simple("/datafiles/textures/wadding.png");
	private static final Appearance CHUTE = simple("/datafiles/textures/chute.jpg");
	
	
	private static final Appearance ESTES_BT = simpleAlpha(new ORColor(212, 185, 145), 0.3f, "/datafiles/textures/spiral-wound-alpha.png");
	private static final Appearance ESTES_IT = simpleAlpha(new ORColor(168, 146, 116), 0.1f, "/datafiles/textures/spiral-wound-alpha.png");
	private static final Appearance WHITE_BT = simpleAlpha(new ORColor(240, 240, 240), 0.3f, "/datafiles/textures/spiral-wound-alpha.png");
	
	private static final Appearance ESTES_MOTOR = simple("/datafiles/textures/motors/estes.jpg");
	private static final Appearance AEROTECH_MOTOR = simple("/datafiles/textures/motors/aerotech.png");
	private static final Appearance KLIMA_MOTOR = simple("/datafiles/textures/motors/klima.jpg");
	private static final Appearance REUSABLE_MOTOR = simpleAlpha(new ORColor(195, 60, 50), 0.6f, "/datafiles/textures/motors/reusable.png");

	private static final HashMap<ORColor, Appearance> plastics = new HashMap<>();
	
	/**
	 * gets the appearance correspondent to the plastic with the given color
	 * also caches the plastics
	 * @param c		the color of the plastics
	 * @return		The plastic appearance with the given color
	 */
	private static Appearance getPlastic(ORColor c) {
		if (!plastics.containsKey(c)) {
			plastics.put(c, new Appearance(c, 0.3));
		}
		return plastics.get(c);
	}
	
	/**
	 * gets the default based on the type of the rocket component
	 * 
	 * @param c	the rocket component
	 * @return	the default appearance for that type of rocket component
	 */
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
			return getPlastic(new ORColor(255, 255, 255));
		if (c instanceof RadiusRingComponent)
			return WOOD;
		if (c instanceof Parachute)
			return CHUTE;
		if (c instanceof EngineBlock)
			return HARDBOARD;
		if (c instanceof MassObject)
			return WADDING;
		if ( c instanceof RailButton )
			return getPlastic(new ORColor(255, 255, 220));
		return Appearance.MISSING;
	}
	
	/**
	 * gets the default motor texture based on the manufacturer
	 * returns reusable motor texture as default
	 * @param m	The motor object
	 * @return	The default appearance for the motor
	 */
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
