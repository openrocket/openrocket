package info.openrocket.swing.gui.plot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightDataTypeGroup;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.util.BaseTestCase;
import net.miginfocom.swing.MigLayout;

/**
 * Regression tests for the bounded data type selectors in the plot configuration panel.
 */
public class PlotTypeSelectorTest extends BaseTestCase {

	/**
	 * Long data type names must not widen the X- or Y-axis selectors, and their full text must remain available as a
	 * tooltip after the selection changes.
	 */
	@Test
	public void testDataTypeSelectorsUseFixedWidthAndFullNameTooltips() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			FlightDataType longType = FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC;
			FlightDataType shortType = FlightDataType.TYPE_ALTITUDE;

			PlotTypeSelector<FlightDataType, FlightDataTypeGroup> yAxisSelector = new PlotTypeSelector<>(
					0, longType, longType.getUnitGroup().getDefaultUnit(), -1,
					Arrays.asList(longType, shortType));
			assertEquals(longType.toString(), yAxisSelector.typeSelector.getToolTipText());
			assertFixedDataTypeSelectorWidth(yAxisSelector, yAxisSelector.typeSelector);
			assertCompactRightGap(yAxisSelector, yAxisSelector.typeSelector);
			Component unitSelector = Arrays.stream(yAxisSelector.getComponents())
					.filter(UnitSelector.class::isInstance)
					.findFirst()
					.orElseThrow();
			assertCompactRightGap(yAxisSelector, unitSelector);

			yAxisSelector.typeSelector.setSelectedItem(shortType);
			assertEquals(shortType.toString(), yAxisSelector.typeSelector.getToolTipText());

			SimulationPlotConfiguration customConfiguration = new SimulationPlotConfiguration("Custom", longType);
			SimulationPlotConfiguration defaultConfiguration = new SimulationPlotConfiguration("Default", longType);
			SimulationPlotConfiguration[] presets = { defaultConfiguration, customConfiguration };
			FlightDataType[] availableTypes = { longType, shortType };
			PlotPanel<FlightDataType, FlightDataBranch, FlightDataTypeGroup, SimulationPlotConfiguration,
					PlotTypeSelector<FlightDataType, FlightDataTypeGroup>> plotPanel = new PlotPanel<>(
					availableTypes, availableTypes, customConfiguration, presets, defaultConfiguration,
					null, null);

			assertEquals(longType.toString(), plotPanel.domainTypeSelector.getToolTipText());
			assertFixedDataTypeSelectorWidth(plotPanel, plotPanel.domainTypeSelector);
			assertCompactRightGap(plotPanel, plotPanel.domainTypeSelector);

			plotPanel.domainTypeSelector.setSelectedItem(shortType);
			assertEquals(shortType.toString(), plotPanel.domainTypeSelector.getToolTipText());
		});
	}

	/**
	 * Verifies that MigLayout receives the exact logical width constraint for the selector.
	 */
	private static void assertFixedDataTypeSelectorWidth(JPanel panel, Component selector) {
		MigLayout layout = (MigLayout) panel.getLayout();
		String constraints = assertInstanceOf(String.class, layout.getComponentConstraints(selector));
		assertTrue(constraints.contains("width " + PlotTypeSelector.DATA_TYPE_SELECTOR_WIDTH));
	}

	/**
	 * Verifies that adjacent selector groups retain a small visual separation without paragraph-sized whitespace.
	 */
	private static void assertCompactRightGap(JPanel panel, Component component) {
		MigLayout layout = (MigLayout) panel.getLayout();
		String constraints = assertInstanceOf(String.class, layout.getComponentConstraints(component));
		assertTrue(constraints.contains("gapright unrel"));
	}
}
