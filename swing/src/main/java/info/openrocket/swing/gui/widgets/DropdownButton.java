package info.openrocket.swing.gui.widgets;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A button that shows a dropdown menu when clicked.
 * Displays a dropdown indicator on the right side.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DropdownButton extends JButton {
	private static final long serialVersionUID = 1L;
	private static final String DROPDOWN_INDICATOR = "  \u007C \u25BC ";
	
	private final JPopupMenu popupMenu;

	/**
	 * Creates a dropdown button with the specified text.
	 *
	 * @param text The text to display on the button
	 */
	public DropdownButton(String text) {
		super(text + DROPDOWN_INDICATOR);
		this.popupMenu = new JPopupMenu();
		
		// Use a small right margin for overall padding
		Insets margin = getMargin();
		if (margin != null) {
			setMargin(new Insets(margin.top, margin.left, margin.bottom, 4));
		} else {
			// Use default margins with small right padding
			setMargin(new Insets(2, 8, 2, 4));
		}
		
		// Show popup menu when button is clicked
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popupMenu.show(DropdownButton.this, 0, DropdownButton.this.getHeight());
			}
		});
	}

	/**
	 * Adds a menu item to the dropdown menu.
	 *
	 * @param item The menu item to add
	 */
	public void addMenuItem(JMenuItem item) {
		popupMenu.add(item);
	}

	/**
	 * Adds a menu item with the specified text and action to the dropdown menu.
	 *
	 * @param text The text for the menu item
	 * @param action The action to perform when the menu item is clicked
	 */
	public void addMenuItem(String text, ActionListener action) {
		JMenuItem item = new JMenuItem(text);
		item.addActionListener(action);
		popupMenu.add(item);
	}

	/**
	 * Removes all menu items from the dropdown menu.
	 */
	public void removeAllMenuItems() {
		popupMenu.removeAll();
	}

	/**
	 * Gets the popup menu for direct manipulation.
	 *
	 * @return The popup menu
	 */
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}
}
