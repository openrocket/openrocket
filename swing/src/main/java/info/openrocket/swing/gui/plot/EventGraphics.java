package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import info.openrocket.core.logging.MessagePriority;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.simulation.FlightEvent;

public class EventGraphics {

	static Color getEventColor(FlightEvent event) {
		FlightEvent.Type type = event.getType();
		Color c = EVENT_COLORS.get(type);
		if (c != null)
			return c;
		return DEFAULT_EVENT_COLOR;
	}

	static Image getEventImage(FlightEvent event) {
		FlightEvent.Type type = event.getType();
		if (type == FlightEvent.Type.SIM_WARN) {
			return MESSAGE_IMAGES.get(((Warning) event.getData()).getPriority());
		} else {
			return EVENT_IMAGES.get(type);
		}
	}
	
	private static final Color DEFAULT_EVENT_COLOR = new Color(0, 0, 0);
	private static final Map<FlightEvent.Type, Color> EVENT_COLORS = new HashMap<>();
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
		EVENT_COLORS.put(FlightEvent.Type.EXCEPTION, new Color(255, 0, 0));
		EVENT_COLORS.put(FlightEvent.Type.SIM_WARN, new Color(127, 127, 0));
		EVENT_COLORS.put(FlightEvent.Type.SIM_ABORT, new Color(255, 0, 0));
	}

	private static final Map<FlightEvent.Type, Image> EVENT_IMAGES = new HashMap<>();
	static {
		loadImage(EVENT_IMAGES, FlightEvent.Type.LAUNCH, "pix/eventicons/event-launch.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.LIFTOFF, "pix/eventicons/event-liftoff.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.LAUNCHROD, "pix/eventicons/event-launchrod.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.IGNITION, "pix/eventicons/event-ignition.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.BURNOUT, "pix/eventicons/event-burnout.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.EJECTION_CHARGE, "pix/eventicons/event-ejection-charge.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.STAGE_SEPARATION,
				"pix/eventicons/event-stage-separation.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.APOGEE, "pix/eventicons/event-apogee.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
				"pix/eventicons/event-recovery-device-deployment.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.GROUND_HIT, "pix/eventicons/event-ground-hit.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.SIMULATION_END, "pix/eventicons/event-simulation-end.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.TUMBLE, "pix/eventicons/event-tumble.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.EXCEPTION, "pix/eventicons/event-exception.png");
		loadImage(EVENT_IMAGES, FlightEvent.Type.SIM_ABORT, "pix/eventicons/event-exception.png");
	}
	
	// Messages can happen at several priority levels, requiring different icons
	private static final Map<MessagePriority, Image> MESSAGE_IMAGES = new HashMap<MessagePriority, Image>();
	static {
		loadImage(MESSAGE_IMAGES, MessagePriority.LOW, "pix/icons/warning_low.png");
		loadImage(MESSAGE_IMAGES, MessagePriority.NORMAL, "pix/icons/warning_normal.png");
		loadImage(MESSAGE_IMAGES, MessagePriority.HIGH, "pix/icons/warning_high.png");
	}

	private static <KeyType> void loadImage(Map<KeyType, Image> imageMap, KeyType type, String file) {
		InputStream is;

		is = ClassLoader.getSystemResourceAsStream(file);
		if (is == null) {
			//System.out.println("ERROR: File " + file + " not found!");
			return;
		}

		try {
			Image image = ImageIO.read(is);
			imageMap.put(type, image);
		} catch (IOException ignore) {
			ignore.printStackTrace();
		}
	}
}
