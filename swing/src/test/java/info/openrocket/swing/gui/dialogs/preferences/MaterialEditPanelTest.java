package info.openrocket.swing.gui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.material.Material;
import info.openrocket.core.material.MaterialGroup;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests synchronization between the material databases and the preferences table.
 */
public class MaterialEditPanelTest extends BaseTestCase {

	/**
	 * A material edit temporarily removes a database row before adding its replacement. The table
	 * must keep the old snapshot readable until it receives the completed update, because its row
	 * sorter still maps view rows to the old model indices during that interval.
	 */
	@Test
	public void materialRowsRemainReadableWhileDatabaseChanges() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			Material material = Material.newMaterial(Material.Type.LINE, "Issue 3166 material", 0.001,
					MaterialGroup.CUSTOM, true, true);
			document.getDocumentPreferences().addMaterial(material);

			MaterialEditPanel panel = new MaterialEditPanel(document);
			JTable table = findTable(panel);
			assertNotNull(table);

			table.getRowSorter().toggleSortOrder(0);
			int materialModelRow = table.getModel().getRowCount() - 1;
			assertEquals(material.getName(), table.getModel().getValueAt(materialModelRow, 0));
			int materialViewRow = table.convertRowIndexToView(materialModelRow);

			// Simulate the remove phase of editing the material before the table change is announced.
			document.getDocumentPreferences().removeMaterial(material);

			assertEquals(materialModelRow + 1, table.getModel().getRowCount());
			Object displayedName = assertDoesNotThrow(() -> table.getValueAt(materialViewRow, 0));
			assertEquals(material.getName(), displayedName);
		});
	}

	/**
	 * Locate the table without exposing a production accessor solely for testing.
	 */
	private static JTable findTable(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JTable table) {
				return table;
			}
			if (component instanceof Container childContainer) {
				JTable table = findTable(childContainer);
				if (table != null) {
					return table;
				}
			}
		}
		return null;
	}
}
