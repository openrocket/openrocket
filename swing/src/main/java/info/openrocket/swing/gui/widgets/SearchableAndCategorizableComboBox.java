package info.openrocket.swing.gui.widgets;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A combo box that has a search box for searching the items in the combobox.
 * If no text is entered, the combobox items are displayed in a categorized popup menu, grouped according to their groups.
 * @param <E> The type of the group
 * @param <T> The type of the items
 */
public class SearchableAndCategorizableComboBox<E, T> extends JComboBox<T> {

	private final JPopupMenu categoryPopup;
	private final JPopupMenu searchPopup;
	private final PlaceholderTextField searchFieldCategory;
	private final PlaceholderTextField searchFieldSearch;
	private final JList<T> filteredList;

	private final T[] allItems;
	private final Map<E, T[]> itemGroupMap;

	private int highlightedListIdx = -1;

	private static Color textSelectionBackground;

	static {
		initColors();
	}

	/**
	 * Create a searchable and categorizable combo box.
	 * @param itemGroupMap the map of items and their corresponding groups
	 * @param placeHolderText the placeholder text for the search field (when no text is entered)
	 */
	public SearchableAndCategorizableComboBox(Map<E, T[]> itemGroupMap, String placeHolderText) {
		super();
		setEditable(false);

		this.itemGroupMap = itemGroupMap;
		this.allItems = extractItemsFromMap(itemGroupMap);
		setModel(new DefaultComboBoxModel<>(allItems));

		initColors();

		// Create the search field widget
		searchFieldCategory = new PlaceholderTextField();
		searchFieldCategory.setPlaceholder(placeHolderText);
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
		UITheme.Theme.addUIThemeChangeListener(SearchableAndCategorizableComboBox::updateColors);
	}

	private static void updateColors() {
		textSelectionBackground = GUIUtil.getUITheme().getTextSelectionBackgroundColor();
	}

	private T[] extractItemsFromMap(Map<E, T[]> itemGroupMap) {
		Set<T> uniqueItems = new HashSet<>(); // Use a Set to ensure uniqueness
		for (E group : itemGroupMap.keySet()) {
			uniqueItems.addAll(Arrays.asList(itemGroupMap.get(group)));
		}
		ArrayList<T> items = new ArrayList<>(uniqueItems);
		return items.toArray((T[]) new Object[0]);
	}

	private JPopupMenu createCategoryPopup() {
		final JPopupMenu menu = new JPopupMenu();

		// Add the search field at the top
		menu.add(searchFieldCategory);
		menu.addSeparator(); // Separator between search field and menu items

		// Fill the menu with the groups
		for (E group : itemGroupMap.keySet()) {
			JMenu groupList = new JMenu(group.toString());
			T[] itemsForGroup = itemGroupMap.get(group);

			if (itemsForGroup != null) {
				for (T item : itemsForGroup) {
					JMenuItem itemMenu = new JMenuItem(item.toString());
					itemMenu.addActionListener(e -> {
						setSelectedItem(item);
					});
					groupList.add(itemMenu);
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

	private JList<T> createFilteredList() {
		JList<T> list = new JList<>();		// Don't fill the list with the items yet, this will be done during filtering

		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				T item = (T) value;
				String itemName = item.toString();

				if (itemName.toLowerCase().contains(searchFieldSearch.getText().toLowerCase())) {
					// Use HTML to underline matching text
					itemName = itemName.replaceAll("(?i)(" + searchFieldSearch.getText() + ")", "<u>$1</u>");
					label.setText("<html>" + itemName + "</html>");
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
				T selectedItem = filteredList.getSelectedValue();
				if (selectedItem != null) {
					SearchableAndCategorizableComboBox.this.setSelectedItem(selectedItem);
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
		SortedListModel<T> filteredModel = new SortedListModel<>();

		for (T item : this.allItems) {
			if (item.toString().toLowerCase().contains(searchText)) {
				filteredModel.add(item);
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

	private static class SortedListModel<T> extends AbstractListModel<T> {
		private final SortedSet<T> model;

		public SortedListModel() {
			Comparator<T> alphabeticalComparator = new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					return o1.toString().compareToIgnoreCase(o2.toString());
				}
			};

			model = new TreeSet<>(alphabeticalComparator);
		}

		public int getSize() {
			return model.size();
		}

		public T getElementAt(int index) {
			return (T) model.toArray()[index];
		}

		public void add(T element) {
			if (model.add(element)) {
				fireContentsChanged(this, 0, getSize());
			}
		}
		public void addAll(T[] elements) {
			Collection<T> c = Arrays.asList(elements);
			model.addAll(c);
			fireContentsChanged(this, 0, getSize());
		}

		public void clear() {
			model.clear();
			fireContentsChanged(this, 0, getSize());
		}

		public boolean contains(T element) {
			return model.contains(element);
		}

		public T firstElement() {
			return model.first();
		}

		public Iterator<T> iterator() {
			return model.iterator();
		}

		public T lastElement() {
			return model.last();
		}

		public boolean removeElement(T element) {
			boolean removed = model.remove(element);
			if (removed) {
				fireContentsChanged(this, 0, getSize());
			}
			return removed;
		}
	}

}

