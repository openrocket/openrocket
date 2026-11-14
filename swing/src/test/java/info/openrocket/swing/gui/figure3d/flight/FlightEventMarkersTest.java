package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightEventMarkersTest {

	@Test
	void selectsOnlyMarkedEventTypesInOrder() {
		List<FlightEvent> events = List.of(
				new FlightEvent(FlightEvent.Type.LAUNCH, 0.0),
				new FlightEvent(FlightEvent.Type.IGNITION, 0.0),
				new FlightEvent(FlightEvent.Type.BURNOUT, 0.73),
				new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 1.5),
				new FlightEvent(FlightEvent.Type.APOGEE, 3.4),
				new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 3.6),
				new FlightEvent(FlightEvent.Type.GROUND_HIT, 7.4),
				new FlightEvent(FlightEvent.Type.SIMULATION_END, 7.5));

		List<FlightEvent> selected = FlightEventMarkers.selectDisplayEvents(events);

		assertEquals(5, selected.size());
		assertEquals(FlightEvent.Type.BURNOUT, selected.get(0).getType());
		assertEquals(FlightEvent.Type.GROUND_HIT, selected.get(4).getType());
	}

	@Test
	void markedTypesHaveDistinctColors() {
		assertTrue(FlightEventMarkers.hasColor(FlightEvent.Type.APOGEE));
		assertNotEquals(FlightEventMarkers.awtColorOf(FlightEvent.Type.APOGEE),
				FlightEventMarkers.awtColorOf(FlightEvent.Type.BURNOUT));
	}
}
