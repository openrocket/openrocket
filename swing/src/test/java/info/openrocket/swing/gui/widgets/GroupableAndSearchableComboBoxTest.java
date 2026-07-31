package info.openrocket.swing.gui.widgets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/**
 * Tests the popup lifecycle of {@link GroupableAndSearchableComboBox}.
 */
public class GroupableAndSearchableComboBoxTest {

	/**
	 * A hover callback queued before the parent popup closes must not reopen its submenu afterward.
	 */
	@Test
	public void testDelayedHoverDoesNotReopenHiddenSubmenu() throws Exception {
		TrackingMenu groupMenu = new TrackingMenu();
		GroupableAndSearchableComboBox.DeselectMenuListener listener =
				new GroupableAndSearchableComboBox.DeselectMenuListener(List.of(groupMenu), groupMenu);

		listener.mouseEntered(new MouseEvent(groupMenu, MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, 0, false));
		SwingUtilities.invokeAndWait(() -> {
			// Flush the hover callback queued by the listener.
		});

		assertFalse(groupMenu.isSelected());
		assertFalse(groupMenu.isPopupMenuVisible());
	}

	/**
	 * Closing a parent popup must also close and deselect all of its group submenus.
	 */
	@Test
	public void testHideGroupSubmenusClearsEveryGroupMenu() {
		JPopupMenu parentPopup = new JPopupMenu();
		TrackingMenu firstGroup = new TrackingMenu();
		TrackingMenu secondGroup = new TrackingMenu();
		parentPopup.add(firstGroup);
		parentPopup.add(secondGroup);
		firstGroup.setSelected(true);
		firstGroup.setPopupMenuVisible(true);
		secondGroup.setSelected(true);
		secondGroup.setPopupMenuVisible(true);

		GroupableAndSearchableComboBox.hideGroupSubmenus(parentPopup);

		assertFalse(firstGroup.isSelected());
		assertFalse(firstGroup.isPopupMenuVisible());
		assertFalse(secondGroup.isSelected());
		assertFalse(secondGroup.isPopupMenuVisible());
	}

	/**
	 * A menu that is still showing should continue to open normally on hover.
	 */
	@Test
	public void testHoverOpensVisibleSubmenu() throws Exception {
		TrackingMenu groupMenu = new TrackingMenu();
		groupMenu.setShowing(true);
		GroupableAndSearchableComboBox.DeselectMenuListener listener =
				new GroupableAndSearchableComboBox.DeselectMenuListener(List.of(groupMenu), groupMenu);

		listener.mouseEntered(new MouseEvent(groupMenu, MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, 0, false));
		SwingUtilities.invokeAndWait(() -> {
			// Flush the hover callback queued by the listener.
		});

		assertTrue(groupMenu.isSelected());
		assertTrue(groupMenu.isPopupMenuVisible());
	}

	/** JMenu test double that does not require a visible native window. */
	private static final class TrackingMenu extends JMenu {
		private boolean popupMenuVisible;
		private boolean showing;

		@Override
		public void setPopupMenuVisible(boolean visible) {
			popupMenuVisible = visible;
		}

		@Override
		public boolean isPopupMenuVisible() {
			return popupMenuVisible;
		}

		@Override
		public boolean isShowing() {
			return showing;
		}

		private void setShowing(boolean showing) {
			this.showing = showing;
		}
	}
}
