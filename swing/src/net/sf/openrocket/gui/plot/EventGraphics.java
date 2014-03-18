package net.sf.openrocket.gui.plot;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sf.openrocket.simulation.FlightEvent;

public class EventGraphics {

	static Color getEventColor(FlightEvent.Type type) {
		Color c = EVENT_COLORS.get(type);
		if (c != null)
			return c;
		return DEFAULT_EVENT_COLOR;
	}

	static Image getEventImage(FlightEvent.Type type ) {
		Image i = EVENT_IMAGES.get(type);
		return i;
	}
	
	private static final Color DEFAULT_EVENT_COLOR = new Color(0, 0, 0);
	private static final Map<FlightEvent.Type, Color> EVENT_COLORS = new HashMap<FlightEvent.Type, Color>();
	static {
		EVENT_COLORS.put(FlightEvent.Type.LAUNCH, new Color(255, 0, 0));
		EVENT_COLORS.put(FlightEvent.Type.LIFTOFF, new Color(0, 80, 196));
		EVENT_COLORS.put(FlightEvent.Type.LAUNCHROD, new Color(0, 100, 80));
		EVENT_COLORS.put(FlightEvent.Type.IGNITION, new Color(230, 130, 15));
		EVENT_COLORS.put(FlightEvent.Type.BURNOUT, new Color(80, 55, 40));
		EVENT_COLORS.put(FlightEvent.Type.EJECTION_CHARGE, new Color(80, 55, 40));
		EVENT_COLORS.put(FlightEvent.Type.STAGE_SEPARATION, new Color(80, 55, 40));
		EVENT_COLORS.put(FlightEvent.Type.APOGEE, new Color(15, 120, 15));
		EVENT_COLORS.put(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, new Color(0, 0, 128));
		EVENT_COLORS.put(FlightEvent.Type.GROUND_HIT, new Color(0, 0, 0));
		EVENT_COLORS.put(FlightEvent.Type.SIMULATION_END, new Color(128, 0, 0));
		EVENT_COLORS.put(FlightEvent.Type.TUMBLE, new Color(196, 0, 255));
	}

	private static final Map<FlightEvent.Type, Image> EVENT_IMAGES = new HashMap<FlightEvent.Type, Image>();
	static {
		loadImage(FlightEvent.Type.LAUNCH, "pix/eventicons/event-launch.png");
		loadImage(FlightEvent.Type.LIFTOFF, "pix/eventicons/event-liftoff.png");
		loadImage(FlightEvent.Type.LAUNCHROD, "pix/eventicons/event-launchrod.png");
		loadImage(FlightEvent.Type.IGNITION, "pix/eventicons/event-ignition.png");
		loadImage(FlightEvent.Type.BURNOUT, "pix/eventicons/event-burnout.png");
		loadImage(FlightEvent.Type.EJECTION_CHARGE, "pix/eventicons/event-ejection-charge.png");
		loadImage(FlightEvent.Type.STAGE_SEPARATION,
				"pix/eventicons/event-stage-separation.png");
		loadImage(FlightEvent.Type.APOGEE, "pix/eventicons/event-apogee.png");
		loadImage(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
				"pix/eventicons/event-recovery-device-deployment.png");
		loadImage(FlightEvent.Type.GROUND_HIT, "pix/eventicons/event-ground-hit.png");
		loadImage(FlightEvent.Type.SIMULATION_END, "pix/eventicons/event-simulation-end.png");
	}

	private static void loadImage(FlightEvent.Type type, String file) {
		InputStream is;

		is = ClassLoader.getSystemResourceAsStream(file);
		if (is == null) {
			//System.out.println("ERROR: File " + file + " not found!");
			return;
		}

		try {
			Image image = ImageIO.read(is);
			EVENT_IMAGES.put(type, image);
		} catch (IOException ignore) {
			ignore.printStackTrace();
		}
	}

}
