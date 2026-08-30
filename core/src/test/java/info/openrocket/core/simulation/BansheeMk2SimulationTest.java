package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

/**
 * Regression coverage for GitHub issue #3161, reported with the Banshee Mk2
 * design by GitHub user Enderdyls (Dylan Cole).
 */
public class BansheeMk2SimulationTest extends BaseTestCase {

	/**
	 * Verifies that zero-volume integration slices in a short Power-series tail
	 * cone do not contaminate the complete rocket's center of mass with NaN.
	 */
	@Test
	public void testSimulationWithZeroVolumeTailConeSlices() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube bodyTube = (BodyTube) rocket.getStage(0).getChild(1);

		PodSet podSet = new PodSet();
		podSet.setInstanceCount(1);
		bodyTube.addChild(podSet);

		NoseCone podNoseCone = new NoseCone(Transition.Shape.HAACK, 0.15, 0.011);
		podNoseCone.setShapeParameter(0.0);
		podSet.addChild(podNoseCone);

		// These are the dimensions and shape settings that triggered the original failure.
		NoseCone tailCone = new NoseCone(Transition.Shape.POWER, 0.001, 0.011);
		tailCone.setShapeParameter(0.0);
		tailCone.setThickness(0.002);
		tailCone.setFlipped(true);
		podSet.addChild(tailCone);

		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().setRandomSeed(0x3161);

		assertDoesNotThrow(() -> {
			simulation.simulate();
		});
		assertFalse(simulation.hasErrors(), simulation::getStatusDescription);
	}
}
