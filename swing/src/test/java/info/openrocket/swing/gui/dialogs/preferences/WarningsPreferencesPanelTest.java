package info.openrocket.swing.gui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import info.openrocket.swing.gui.simulation.RecoveryWarningsPanel;
import info.openrocket.swing.util.BaseTestCase;

/** Tests placement of application-wide warning threshold preferences. */
public class WarningsPreferencesPanelTest extends BaseTestCase {

	@Test
	public void testRecoveryThresholdsHaveDedicatedPreferencesTab() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			SimulationPreferencesPanel simulationPanel = new SimulationPreferencesPanel();
			WarningsPreferencesPanel warningsPanel = new WarningsPreferencesPanel();

			assertTrue(findComponents(simulationPanel, RecoveryWarningsPanel.class).isEmpty());
			assertEquals(1, findComponents(warningsPanel, RecoveryWarningsPanel.class).size());
		});
	}

	private static <T extends Component> List<T> findComponents(Component root, Class<T> type) {
		List<T> matches = new ArrayList<>();
		if (type.isInstance(root)) {
			matches.add(type.cast(root));
		}
		if (root instanceof Container container) {
			for (Component child : container.getComponents()) {
				matches.addAll(findComponents(child, type));
			}
		}
		return matches;
	}
}
