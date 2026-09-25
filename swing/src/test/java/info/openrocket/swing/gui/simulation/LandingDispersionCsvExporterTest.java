package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.MonteCarloSimulationRunner;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;

public class LandingDispersionCsvExporterTest extends BaseTestCase {
	@Test
	public void testExportIncludesConfigurationSamplesMetricsAndEveryRunBranchPair() throws IOException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setLaunchRodLength(3.0);
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(1234)
				.uncertainty(MonteCarloParameter.WIND_DIRECTION, MonteCarloDistribution.UNIFORM,
						Math.toRadians(5))
				.build();
		MonteCarloResult result = new MonteCarloSimulationRunner().run(simulation, settings);

		StringWriter writer = new StringWriter();
		LandingDispersionCsvExporter.write(writer, result);
		String csv = writer.toString();

		assertTrue(csv.contains("\"analysis_seed\""));
		assertTrue(csv.contains("\"wind_direction_delta_rad\""));
		assertTrue(csv.contains("\"apogee_altitude_m\""));
		assertTrue(csv.contains("\"landing_velocity_m_per_s\""));
		assertTrue(csv.contains("WIND_DIRECTION:UNIFORM:"));
		assertTrue(csv.contains("\"nominal\""));
		assertEquals(1L + 3L * result.getFlightBranches().size(), csv.lines().count());
	}
}
