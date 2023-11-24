package net.sf.openrocket.gui.simulation;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.UITheme;
import net.sf.openrocket.gui.widgets.PlaceholderTextField;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightDataTypeGroup;
import net.sf.openrocket.startup.Application;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;

public class FlightDataComboBox extends JComboBox<FlightDataType> {
	private static final Translator trans = Application.getTranslator();

	private final JPopupMenu categoryPopup;
	private final JPopupMenu searchPopup;
	private final PlaceholderTextField searchFieldCategory;
	private final PlaceholderTextField searchFieldSearch;
	private final JList<FlightDataType> filteredList;

	private final FlightDataType[] allTypes;
	private final Hashtable<FlightDataTypeGroup, FlightDataType[]> typeGroupMap;

	private int highlightedListIdx = -1;

	private static Color textSelectionBackground;

	static {
		initColors();
	}

	public FlightDataComboBox(FlightDataTypeGroup[] allGroups, FlightDataType[] types) {
		super(types);
		setEditable(false);

		this.allTypes = types;

		initColors();

		// Create the map of flight data group and corresponding flight data types
		typeGroupMap = createFlightDataGroupMap(allGroups, types);

		// Create the search field widget
		searchFieldCategory = new PlaceholderTextField();
		searchFieldCategory.setPlaceholder(trans.get("FlightDataComboBox.placeholder"));
		searchFieldSearch = new PlaceholderTextField();

		// Create the filtered list
		filteredList = createFilteredList();

		// Create the different popups
		categoryPopup = createCategoryPopup();
		searchPopup = createSearchPopup();
		searchPopup.setPreferredSize(categoryPopup.getPreferredSize());

		// Add key listener for the search fields
		searchFieldCategory.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				overrideActionKeys(e);
			}

			public void keyTyped(KeyEvent e) {
				EventQueue.invokeLater(() -> {
					String text = searchFieldCategory.getText();
					highlightedListIdx = 0;		// Start with the first item selected
					searchFieldSearch.setText(text);
					if (!text.isEmpty() && !searchPopup.isVisible()) {
						hideCategoryPopup();
						showSearchPopup();
						filter(text);
					}
				});
			}
		});
		searchFieldSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				overrideActionKeys(e);
			}

			@Override
			public void keyTyped(KeyEvent e) {
				EventQueue.invokeLater(() -> {
					String text = searchFieldSearch.getText();
					highlightedListIdx = 0;		// Start with the first item selected
					searchFieldCategory.setText(text);
					if (text.isEmpty() && !categoryPopup.isVisible()) {
						hideSearchPopup();
						showCategoryPopup();
					}
					filter(text);
				});
			}
		});

		// Override the mouse listeners to use our custom popup
		for (MouseListener mouseListener : getMouseListeners()) {
			removeMouseListener(mouseListener);
		}

		addMouseListeners();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(FlightDataComboBox::updateColors);
	}

	private static void updateColors() {
		textSelectionBackground = GUIUtil.getUITheme().getTextSelectionBackgroundColor();
	}

	/**
	 * Create a map of flight data group and corresponding flight data types.
	 * @param groups the groups
	 * @param types the types
	 * @return the map linking the types to their groups
	 */
	private Hashtable<FlightDataTypeGroup, FlightDataType[]> createFlightDataGroupMap(FlightDataTypeGroup[] groups, FlightDataType[] types) {
		Hashtable<FlightDataTypeGroup, FlightDataType[]> map = new Hashtable<>();
		for (FlightDataTypeGroup group : groups) {
			ArrayList<FlightDataType> listForGroup = new ArrayList<>();
			for (FlightDataType type : types) {
				if (type.getGroup().equals(group)) {
					listForGroup.add(type);
				}
			}
			map.put(group, listForGroup.toArray(new FlightDataType[0]));
		}

		return map;
	}

	private JPopupMenu createCategoryPopup() {
		final JPopupMenu menu = new JPopupMenu();

		// Add the search field at the top
		menu.add(searchFieldCategory);
		menu.addSeparator(); // Separator between search field and menu items

		// Fill the menu with the groups
		for (FlightDataTypeGroup group : typeGroupMap.keySet()) {
			JMenu groupList = new JMenu(group.getName());
			FlightDataType[] typesForGroup = typeGroupMap.get(group);

			if (typesForGroup != null) {
				for (FlightDataType type : typesForGroup) {
					JMenuItem typeItem = new JMenuItem(type.getName());
					typeItem.addActionListener(e -> {
						setSelectedItem(type);
					});
					groupList.add(typeItem);
				}
			}

			menu.add(groupList);
		}

		return menu;
	}

	private JPopupMenu createSearchPopup() {
		final JPopupMenu menu = new JPopupMenu();
		menu.setLayout(new BorderLayout());

		// Add the search field at the top
		menu.add(searchFieldSearch, BorderLayout.NORTH);
		menu.addSeparator();

		menu.add(new JScrollPane(filteredList));

		return menu;
	}

	private JList<FlightDataType> createFilteredList() {
		JList<FlightDataType> list = new JList<>();
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				FlightDataType type = (FlightDataType) value;
				String typeName = type.toString();

				if (typeName.toLowerCase().contains(searchFieldSearch.getText().toLowerCase())) {
					// Use HTML to underline matching text
					typeName = typeName.replaceAll("(?i)(" + searchFieldSearch.getText() + ")", "<u>$1</u>");
					label.setText("<html>" + typeName + "</html>");
				}

				// Set the hover color
				if (highlightedListIdx == index || isSelected) {
					label.setBackground(textSelectionBackground);
					label.setOpaque(true);
				} else {
					label.setOpaque(false);
				}

				return label;
			}
		});

		list.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = new Point(e.getX(),e.getY());
				int index = list.locationToIndex(p);
				if (index != highlightedListIdx) {
					highlightedListIdx = index;
					list.repaint();
				}
			}
		});

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Check if the event is in the final phase of change
				if (!e.getValueIsAdjusting()) {
					selectComboBoxItemFromFilteredList();
				}
			}
		});

		return list;
	}

	private void selectComboBoxItemFromFilteredList() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FlightDataType selectedType = filteredList.getSelectedValue();
				if (selectedType != null) {
					FlightDataComboBox.this.setSelectedItem(selectedType);
					// Hide the popups after selection
					hideCategoryPopup();
					hideSearchPopup();
				}
			}
		});
	}

	private void showCategoryPopup() {
		categoryPopup.show(this, 0, getHeight());
		searchFieldSearch.setText("");
		searchFieldCategory.setText("");
	}

	private void hideCategoryPopup() {
		categoryPopup.setVisible(false);
	}

	private void showSearchPopup() {
		searchPopup.show(this, 0, getHeight());
	}

	private void hideSearchPopup() {
		searchPopup.setVisible(false);
	}

	private void filter(String text) {
		filteredList.removeAll();
		String searchText = text.toLowerCase();
		DefaultListModel<FlightDataType> filteredModel = new DefaultListModel<>();

		for (FlightDataType item : this.allTypes) {
			if (item.toString().toLowerCase().contains(searchText)) {
				filteredModel.addElement(item);
			}
		}

		filteredList.setModel(filteredModel);
		filteredList.revalidate();
		filteredList.repaint();
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

	@Override
	public boolean isPopupVisible() {
		return categoryPopup.isVisible() || searchPopup.isVisible();
	}

	/**
	 * Override the default action keys (escape, enter, arrow keys) to do our own actions.
	 * @param e the key event
	 */
	private void overrideActionKeys(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			hideCategoryPopup();
			hideSearchPopup();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			selectHighlightedItemInFilteredList();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_RIGHT) {
			highlightNextItemInFilteredList();
		} else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
			highlightPreviousItemInFilteredList();
		}
	}

	/**
	 * Select the highlighted item in the filtered list and hide the popups.
	 */
	private void selectHighlightedItemInFilteredList() {
		if (highlightedListIdx >= filteredList.getModel().getSize() || highlightedListIdx < 0 || !searchPopup.isVisible()) {
			return;
		}
		filteredList.setSelectedIndex(highlightedListIdx);
		selectComboBoxItemFromFilteredList();
	}

	/**
	 * Highlight the next item in the filtered list.
	 */
	private void highlightNextItemInFilteredList() {
		if (highlightedListIdx + 1 >= filteredList.getModel().getSize() || !searchPopup.isVisible()) {
			return;
		}
		highlightedListIdx++;
		filteredList.ensureIndexIsVisible(highlightedListIdx);
		filteredList.repaint();
	}

	/**
	 * Highlight the previous item in the filtered list.
	 */
	private void highlightPreviousItemInFilteredList() {
		if (highlightedListIdx <= 0 || !searchPopup.isVisible()) {
			return;
		}
		highlightedListIdx--;
		filteredList.ensureIndexIsVisible(highlightedListIdx);
		filteredList.repaint();
	}



	/**
	 * Add mouse listener to widgets of the combobox to open our custom popup menu.
	 */
	private void addMouseListeners() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!isPopupVisible()) {
							showCategoryPopup();
						}
					}
				});
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
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (!isPopupVisible()) {
								showCategoryPopup();
							}
						}
					});
				}
			});
		}
	}
}
