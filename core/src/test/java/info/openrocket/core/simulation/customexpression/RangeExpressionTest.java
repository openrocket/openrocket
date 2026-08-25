package info.openrocket.core.simulation.customexpression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

import de.congrace.exp4j.Variable;

import org.junit.jupiter.api.Test;

public class RangeExpressionTest extends BaseTestCase {

	/**
	 * Build a status carrying a short stretch of flight data, enough for a range
	 * expression to be evaluated against.
	 */
	private SimulationStatus statusWithTimeData() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0, false);

		Simulation simulation = new Simulation(OpenRocketDocumentFactory.createDocumentFromRocket(rocket), rocket);
		simulation.getOptions().setTimeStep(0.05);

		SimulationConditions conditions = new SimulationConditions();
		conditions.setSimulation(simulation);

		SimulationStatus status = new SimulationStatus(config, conditions);

		FlightDataBranch branch = new FlightDataBranch("test", FlightDataType.TYPE_TIME);
		for (int i = 0; i <= 20; i++) {
			branch.addPoint();
			branch.setValue(FlightDataType.TYPE_TIME, i * 0.05);
			branch.setValue(FlightDataType.TYPE_ALTITUDE, i);
		}
		status.setFlightDataBranch(branch);

		return status;
	}

	@Test
	public void reversedRangeIsUnknown() {
		OpenRocketDocument doc = OpenRocketDocumentFactory.createNewRocket();
		SimulationStatus status = statusWithTimeData();

		// Start after the end: this selects no data, so it must not quietly come back
		// as the single sample at the end of the range
		RangeExpression reversed = new RangeExpression(doc, "0.8", "0.4", "h");
		Variable result = reversed.evaluate(status);
		assertEquals("Unknown", result.getName());

		// A range the right way round still resolves
		RangeExpression forward = new RangeExpression(doc, "0.4", "0.8", "h");
		assertTrue(forward.evaluate(status).getArrayValue().length > 1);
	}
}
