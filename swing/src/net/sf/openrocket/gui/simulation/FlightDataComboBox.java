package net.sf.openrocket.gui.simulation;

import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightDataTypeGroup;

import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;

public class FlightDataComboBox extends JComboBox<FlightDataType> {
	private final JPopupMenu mainMenu;
	private final Hashtable<FlightDataTypeGroup, FlightDataType[]> subItems = new Hashtable<>();

	public FlightDataComboBox(FlightDataType[] types) {
		super(types);
		setEditable(false);

		for (FlightDataTypeGroup group : FlightDataTypeGroup.ALL_GROUPS) {
			ArrayList<FlightDataType> listForGroup = new ArrayList<>();
			for (FlightDataType type : types) {
				if (type.getGroup().equals(group)) {
					listForGroup.add(type);
				}
			}
			subItems.put(group, listForGroup.toArray(new FlightDataType[0]));
		}

		mainMenu = createMainMenu();

		// Override the mouse listeners to use our custom popup
		for (MouseListener mouseListener : getMouseListeners()) {
			removeMouseListener(mouseListener);
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showCustomPopup();
			}
		});

		Component arrowButton = getArrowButton();
		if (arrowButton != null) {
			for (MouseListener mouseListener : arrowButton.getMouseListeners()) {
				arrowButton.removeMouseListener(mouseListener);
			}
			arrowButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showCustomPopup();
				}
			});
		}
	}

	private Component getArrowButton() {
		for (Component child : getComponents()) {
			if (child instanceof BasicArrowButton) {
				return child;
			}
		}
		return null;
	}

	@Override
	public void showPopup() {
		// Override the default JComboBox showPopup() to do nothing
		// Our custom popup will be shown by the MouseListener
	}

	private JPopupMenu createMainMenu() {
		JPopupMenu menu = new JPopupMenu();

		for (FlightDataTypeGroup group : FlightDataTypeGroup.ALL_GROUPS) {
			JMenu groupMenu = new JMenu(group.getName());
			FlightDataType[] typesForGroup = subItems.get(group);

			if (typesForGroup != null) {
				for (FlightDataType type : typesForGroup) {
					JMenuItem typeItem = new JMenuItem(type.getName());
					typeItem.addActionListener(e -> {
						setSelectedItem(type);
					});
					groupMenu.add(typeItem);
				}
			}

			menu.add(groupMenu);
		}

		return menu;
	}

	private void showCustomPopup() {
		mainMenu.show(this, 0, getHeight());
	}
}
