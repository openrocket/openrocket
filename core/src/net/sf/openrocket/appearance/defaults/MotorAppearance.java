package net.sf.openrocket.appearance.defaults;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

public class MotorAppearance extends Appearance {
	
	
	private static MotorAppearance ESTES = new MotorAppearance("/datafiles/textures/motors/estes.png");
	private static MotorAppearance AEROTECH = new MotorAppearance("/datafiles/textures/motors/aerotech.png");
	private static MotorAppearance REUSABLE = new MotorAppearance("/datafiles/textures/motors/reusable.png", new Color(195, 60, 50), .6);
	
	public static Appearance getAppearance(Motor m) {
		if (m instanceof ThrustCurveMotor) {
			ThrustCurveMotor tcm = (ThrustCurveMotor) m;
			if ("Estes".equals(tcm.getManufacturer().getSimpleName())) {
				return ESTES;
			}
			if ("AeroTech".equals(tcm.getManufacturer().getSimpleName())) {
				return AEROTECH;
			}
		}
		return REUSABLE;
	}
	
	protected MotorAppearance(final String resource) {
		super(
				new Color(0, 0, 0),
				.1,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	}
	
	protected MotorAppearance(final String resource, Color c, double shine) {
		super(
				c,
				shine,
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new ResourceDecalImage(resource), EdgeMode.REPEAT));
	}
	
}
