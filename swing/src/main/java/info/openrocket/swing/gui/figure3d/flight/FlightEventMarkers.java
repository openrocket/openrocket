package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightEvent;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which flight events the replay displays, and the color each one gets. The same colors are
 * used for the trajectory markers and the scrub-slider ticks so the two stay coherent.
 */
final class FlightEventMarkers {

	private static final Map<FlightEvent.Type, Vector3f> COLORS = new LinkedHashMap<>();

	static {
		COLORS.put(FlightEvent.Type.BURNOUT, new Vector3f(1.0f, 0.45f, 0.15f));
		COLORS.put(FlightEvent.Type.STAGE_SEPARATION, new Vector3f(1.0f, 0.8f, 0.25f));
		COLORS.put(FlightEvent.Type.APOGEE, new Vector3f(0.35f, 0.65f, 1.0f));
		COLORS.put(FlightEvent.Type.EJECTION_CHARGE, new Vector3f(0.95f, 0.95f, 0.95f));
		COLORS.put(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, new Vector3f(0.95f, 0.35f, 0.8f));
		COLORS.put(FlightEvent.Type.GROUND_HIT, new Vector3f(0.6f, 0.45f, 0.3f));
	}

	private FlightEventMarkers() {
	}

	/** Returns the events worth marking in the replay, in flight order. */
	static List<FlightEvent> selectDisplayEvents(List<FlightEvent> events) {
		List<FlightEvent> selected = new ArrayList<>();
		for (FlightEvent event : events) {
			if (COLORS.containsKey(event.getType())) {
				selected.add(event);
			}
		}
		return selected;
	}

	static Vector3f colorOf(FlightEvent.Type type) {
		Vector3f color = COLORS.get(type);
		return color != null ? new Vector3f(color) : new Vector3f(1.0f, 1.0f, 1.0f);
	}

	/** Whether the type has a dedicated display color (is one of the marked event types). */
	static boolean hasColor(FlightEvent.Type type) {
		return COLORS.containsKey(type);
	}

	static Color awtColorOf(FlightEvent.Type type) {
		Vector3f color = colorOf(type);
		return new Color(color.x, color.y, color.z);
	}
}
