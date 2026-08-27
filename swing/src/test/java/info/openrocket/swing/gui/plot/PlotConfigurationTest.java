package info.openrocket.swing.gui.plot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Regression tests for plot configuration auto-axis selection.
 */
public class PlotConfigurationTest extends BaseTestCase {

	/**
	 * A custom expression can yield only non-finite values for the auto-axis scorer.
	 * The recursive chooser must still return a configuration instead of bubbling up
	 * a null result that later throws a NullPointerException.
	 */
	@Test
	public void testFillAutoAxesFallsBackWhenGoodnessIsNaN() {
		FlightDataBranch branch = new FlightDataBranch("test", FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 10.0);

		NaNGoodnessSimulationPlotConfiguration configuration =
				new NaNGoodnessSimulationPlotConfiguration("NaN goodness test");
		configuration.addPlotDataType(FlightDataType.TYPE_ALTITUDE);

		SimulationPlotConfiguration filled = assertDoesNotThrow(
				() -> configuration.fillAutoAxes(branch),
				"Auto-axis selection should not fail when every candidate scores as NaN"
		);

		assertNotNull(filled, "Auto-axis selection must always return a configuration");
		assertTrue(filled.getAxis(0) >= 0, "The plotted type should be assigned to a concrete axis");
	}

	/**
	 * Test helper that simulates a custom expression producing only non-finite
	 * auto-axis goodness scores.
	 */
	private static final class NaNGoodnessSimulationPlotConfiguration extends SimulationPlotConfiguration {
		private NaNGoodnessSimulationPlotConfiguration(String name) {
			super(name);
		}

		@Override
		protected double getGoodnessValue(FlightDataBranch data) {
			return Double.NaN;
		}
	}
}
