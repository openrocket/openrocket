package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.swing.util.BaseTestCase;

class MultiLevelWindTableTest extends BaseTestCase {
	@Test
	void weatherImportRowsStartCheckedByPositionAndCanBeIncludedAgain() throws Exception {
		MultiLevelWindTable[] table = new MultiLevelWindTable[1];
		SwingUtilities.invokeAndWait(() -> table[0] = new MultiLevelWindTable(windModel(6), Set.of(1, 4)));

		assertEquals(Set.of(1, 4), table[0].getExcludedRowIndices());
		List<JCheckBox> importCheckboxes = componentsOfType(table[0].getRowsPanel(), JCheckBox.class);
		assertEquals(6, importCheckboxes.size());
		assertFalse(importCheckboxes.get(1).isSelected());
		assertFalse(componentsOfType(table[0].getRowsPanel().getComponent(1), JSpinner.class).get(0).isEnabled());

		SwingUtilities.invokeAndWait(importCheckboxes.get(1)::doClick);

		assertEquals(Set.of(4), table[0].getExcludedRowIndices());
		assertTrue(componentsOfType(table[0].getRowsPanel().getComponent(1), JSpinner.class).get(0).isEnabled());
	}

	@Test
	void excludedPositionTracksTheFinalSortedRowOrder() throws Exception {
		MultiLevelWindTable[] table = new MultiLevelWindTable[1];
		SwingUtilities.invokeAndWait(() -> table[0] = new MultiLevelWindTable(windModel(6), Set.of(4)));
		JSpinner firstAltitude = componentsOfType(table[0].getRowsPanel().getComponent(0), JSpinner.class).get(0);

		SwingUtilities.invokeAndWait(() -> firstAltitude.setValue(1000.0));

		assertEquals(Set.of(3), table[0].getExcludedRowIndices());
	}

	@Test
	void allowsEveryWindLevelToBeUnchecked() throws Exception {
		MultiLevelWindTable[] table = new MultiLevelWindTable[1];
		SwingUtilities.invokeAndWait(() -> table[0] = new MultiLevelWindTable(windModel(3), Set.of()));
		List<JCheckBox> importCheckboxes = componentsOfType(table[0].getRowsPanel(), JCheckBox.class);

		SwingUtilities.invokeAndWait(() -> importCheckboxes.forEach(JCheckBox::doClick));

		assertEquals(Set.of(0, 1, 2), table[0].getExcludedRowIndices());
	}

	private static MultiLevelPinkNoiseWindModel windModel(int levelCount) {
		MultiLevelPinkNoiseWindModel model = new MultiLevelPinkNoiseWindModel();
		model.clearLevels();
		for (int index = 0; index < levelCount; index++) {
			model.addWindLevel(index * 100.0, 5.0, 0.0, 0.5);
		}
		return model;
	}

	private static <T extends Component> List<T> componentsOfType(Component root, Class<T> type) {
		List<T> result = new ArrayList<>();
		if (type.isInstance(root)) {
			result.add(type.cast(root));
		}
		if (root instanceof Container container) {
			for (Component child : container.getComponents()) {
				result.addAll(componentsOfType(child, type));
			}
		}
		return result;
	}
}
