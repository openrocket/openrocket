package info.openrocket.swing.gui.figure3d.flight;

import com.google.inject.Guice;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport;
import info.openrocket.swing.gui.figure3d.animation.SimToWorld;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the wind direction convention against the simulator: a parachute drifts downwind. */
class WindFieldSimulationTest {
	@Test
	void recordedWindPointsTheWayTheRocketDriftsUnderItsParachute() throws Exception {
		Application.setInjector(Guice.createInjector(new ServicesForTesting(), new PluginModule()));
		OpenRocketDocument document = GoldenImageTestSupport.createStyledRocketDocument();
		Simulation simulation = new Simulation(document, document.getRocket());
		simulation.getOptions().setWindSpeedAverage(6.0);
		simulation.getOptions().setWindDirection(Math.toRadians(60.0));
		simulation.simulate();
		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		double deployment = eventTime(branch, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT);
		double landing = eventTime(branch, FlightEvent.Type.GROUND_HIT);

		Vector3f drift = position(branch, landing).sub(position(branch, deployment));
		WindField wind = WindField.fromBranch(branch);
		Vector3f meanWind = new Vector3f();
		for (int i = 0; i <= 20; i++) {
			meanWind.add(wind.velocityAt(deployment + (landing - deployment) * i / 20.0));
		}

		drift.y = 0.0f;
		assertTrue(drift.length() > 0.0f && meanWind.length() > 0.0f);
		assertTrue(drift.normalize().dot(meanWind.normalize()) > 0.9f,
				"Descent drift " + drift + " should follow the recorded wind " + meanWind);
	}

	private static double eventTime(FlightDataBranch branch, FlightEvent.Type type) {
		return branch.getEvents().stream().filter(event -> event.getType() == type)
				.mapToDouble(FlightEvent::getTime).min().orElseThrow();
	}

	private static Vector3f position(FlightDataBranch branch, double time) {
		List<Double> times = branch.get(FlightDataType.TYPE_TIME);
		int index = 0;
		while (index < times.size() - 1 && times.get(index) < time) {
			index++;
		}
		return SimToWorld.toEngine(branch.get(FlightDataType.TYPE_POSITION_X).get(index).floatValue(),
				branch.get(FlightDataType.TYPE_POSITION_Y).get(index).floatValue(), 0.0f);
	}
}
