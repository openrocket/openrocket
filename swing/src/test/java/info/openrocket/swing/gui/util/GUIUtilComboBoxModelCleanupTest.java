package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.junit.jupiter.api.Test;

/**
 * Regression test for a Swing NPE where a BasicComboBoxUI handler can remain attached to an old model
 * after cleanup, and later receive model events after the UI has been uninstalled.
 */
public class GUIUtilComboBoxModelCleanupTest {

	private static final class TrackingComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
		private static final long serialVersionUID = 1L;

		private final List<String> items = Arrays.asList("A", "B");
		private final CopyOnWriteArrayList<ListDataListener> listeners = new CopyOnWriteArrayList<>();
		private Object selectedItem = items.get(0);

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public String getElementAt(int index) {
			return items.get(index);
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selectedItem = anItem;
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.addIfAbsent(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		int getListenerCount() {
			return listeners.size();
		}

		void fireTestChangeEvent() {
			for (ListDataListener l : listeners) {
				l.contentsChanged(new javax.swing.event.ListDataEvent(this, javax.swing.event.ListDataEvent.CONTENTS_CHANGED, 0, 0));
			}
		}
	}

	@Test
	public void testSetNullModelsDetachesComboBoxUIHandlerFromOldModel() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			TrackingComboBoxModel oldModel = new TrackingComboBoxModel();
			JComboBox<String> combo = new JComboBox<>(oldModel);

			// Force a BasicComboBoxUI instance so we can reproduce the original failure mode.
			combo.setUI(new BasicComboBoxUI());
			assertTrue(oldModel.getListenerCount() > 0, "Expected combo box UI to register ListDataListener(s) on the model");

			// This should swap out the model while property-change listeners are still intact,
			// so the UI can detach from the old model.
			GUIUtil.setNullModels(combo);

			// Uninstall the old UI. In the original bug, the old UI's Handler would still be
			// attached to the old model, but now have comboBox == null, causing an NPE on change.
			combo.setUI(new BasicComboBoxUI());

			assertDoesNotThrow(oldModel::fireTestChangeEvent, "Old model should not retain UI handlers after cleanup");
		});
	}
}

