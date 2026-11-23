package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.swing.gui.plot.PlotPanel;
import info.openrocket.swing.gui.plot.PlotTypeSelector;
import info.openrocket.swing.gui.plot.SimulationPlotConfiguration;
import info.openrocket.swing.gui.widgets.GroupableAndSearchableComboBox;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Regression tests for {@link SimulationPlotPanel} selection handling (issue #3493).
 */
public class SimulationPlotPanelTest extends BaseTestCase {

	@Test
	public void removingAllPlotTypesAllowsAddingAndChangingSelection() throws Exception {
		SimulationPlotPanel panel = createPanelWithTypes(
				FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_VELOCITY_Z,
				FlightDataType.TYPE_ACCELERATION_Z,
				FlightDataType.TYPE_THRUST_FORCE);

		while (panel.getConfiguration().getDataCount() > 0) {
			SimulationPlotTypeSelector first = findSelectors(panel).get(0);
			JButton removeButton = getRemoveButton(first);
			assertDoesNotThrow(() -> removeButton.doClick());
		}

		assertEquals(0, panel.getConfiguration().getDataCount());
		assertEquals(0, findSelectors(panel).size());

		JButton newYAxisBtn = findNewYAxisButton(panel);
		assertNotNull(newYAxisBtn);
		newYAxisBtn.doClick();

		List<SimulationPlotTypeSelector> selectors = findSelectors(panel);
		assertEquals(1, selectors.size());
		getTypeSelector(selectors.get(0)).setSelectedItem(FlightDataType.TYPE_THRUST_FORCE);

		assertEquals(FlightDataType.TYPE_THRUST_FORCE, panel.getConfiguration().getType(0));
		assertEquals(FlightDataType.TYPE_THRUST_FORCE.getUnitGroup().getDefaultUnit(),
				selectors.get(0).getSelectedUnit());

		newYAxisBtn.doClick();
		List<SimulationPlotTypeSelector> afterAdd = findSelectors(panel);
		assertEquals(2, afterAdd.size());
		assertEquals(FlightDataType.TYPE_THRUST_FORCE, panel.getConfiguration().getType(0));
		assertEquals(FlightDataType.TYPE_THRUST_FORCE, afterAdd.get(0).getSelectedType());
	}

	@Test
	public void switchingToPresetWithMissingTypesDoesNotBlockFutureSelections() throws Exception {
		// Branch intentionally omits types used by several presets (e.g. Drag coefficients vs. Mach number).
		SimulationPlotPanel panel = createPanelWithTypes(
				FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_VELOCITY_Z,
				FlightDataType.TYPE_THRUST_FORCE);

		JComboBox<?> presetSelector = getConfigurationSelector(panel);
		int emptyPresetIndex = -1;
		for (int i = 0; i < presetSelector.getItemCount(); i++) {
			Object item = presetSelector.getItemAt(i);
			if (item instanceof SimulationPlotConfiguration conf && conf.getDataCount() > 0
					&& conf.getType(0) == FlightDataType.TYPE_DRAG_COEFF) {
				emptyPresetIndex = i;
				break;
			}
		}
		assertNotNull(emptyPresetIndex >= 0 ? emptyPresetIndex : null);

		int targetIndex = emptyPresetIndex;
		assertDoesNotThrow(() -> presetSelector.setSelectedIndex(targetIndex));
		assertEquals(0, panel.getConfiguration().getDataCount());

		JButton newYAxisBtn = findNewYAxisButton(panel);
		assertNotNull(newYAxisBtn);
		newYAxisBtn.doClick();

		List<SimulationPlotTypeSelector> selectors = findSelectors(panel);
		assertEquals(1, selectors.size());
		getTypeSelector(selectors.get(0)).setSelectedItem(FlightDataType.TYPE_THRUST_FORCE);
		assertEquals(FlightDataType.TYPE_THRUST_FORCE, panel.getConfiguration().getType(0));
		assertEquals(FlightDataType.TYPE_THRUST_FORCE.getUnitGroup().getDefaultUnit(),
				selectors.get(0).getSelectedUnit());

		newYAxisBtn.doClick();
		List<SimulationPlotTypeSelector> afterAdd = findSelectors(panel);
		assertEquals(2, afterAdd.size());
		assertEquals(FlightDataType.TYPE_THRUST_FORCE, panel.getConfiguration().getType(0));
		assertEquals(FlightDataType.TYPE_THRUST_FORCE, afterAdd.get(0).getSelectedType());
	}

	private static SimulationPlotPanel createPanelWithTypes(FlightDataType... types) {
		FlightDataBranch branch = new FlightDataBranch("Sustainer", types);
		for (int i = 0; i < 3; i++) {
			branch.addPoint();
			for (FlightDataType type : types) {
				branch.setValue(type, i * 10.0);
			}
		}
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Simulation simulation = new Simulation(document, document.getRocket(), Simulation.Status.UPTODATE, "Test",
				new SimulationOptions(), Collections.emptyList(), new FlightData(branch));
		return SimulationPlotPanel.create(simulation);
	}

	private static List<SimulationPlotTypeSelector> findSelectors(Container container) {
		List<SimulationPlotTypeSelector> selectors = findComponents(container, SimulationPlotTypeSelector.class);
		selectors.sort(Comparator.comparingInt(SimulationPlotTypeSelector::getIndex));
		return selectors;
	}

	private static JButton findNewYAxisButton(Container container) {
		for (JButton button : findComponents(container, JButton.class)) {
			if (button.getText() != null && button.getText().contains("NewYaxisplottype")) {
				return button;
			}
		}
		return null;
	}

	private static JButton getRemoveButton(PlotTypeSelector<?, ?> selector) throws Exception {
		Field field = PlotTypeSelector.class.getDeclaredField("removeButton");
		field.setAccessible(true);
		return (JButton) field.get(selector);
	}

	private static GroupableAndSearchableComboBox<?, ?> getTypeSelector(PlotTypeSelector<?, ?> selector)
			throws Exception {
		Field field = PlotTypeSelector.class.getDeclaredField("typeSelector");
		field.setAccessible(true);
		return (GroupableAndSearchableComboBox<?, ?>) field.get(selector);
	}

	private static JComboBox<?> getConfigurationSelector(PlotPanel<?, ?, ?, ?, ?> panel) throws Exception {
		Field field = PlotPanel.class.getDeclaredField("configurationSelector");
		field.setAccessible(true);
		return (JComboBox<?>) field.get(panel);
	}

	private static <T extends Component> List<T> findComponents(Container container, Class<T> type) {
		List<T> matches = new ArrayList<>();
		for (Component component : container.getComponents()) {
			if (type.isInstance(component)) {
				matches.add(type.cast(component));
			}
			if (component instanceof Container child) {
				matches.addAll(findComponents(child, type));
			}
		}
		return matches;
	}
}
