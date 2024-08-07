package info.openrocket.swing.gui.widgets;

import info.openrocket.core.util.Group;
import info.openrocket.core.util.Groupable;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * A combo box that has a search box for searching the items in the combobox.
 * If no text is entered, the combobox items are displayed in a categorized popup menu, grouped according to their groups.
 * @param <G> The type of the group
 * @param <T> The type of the groupable items
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SearchableAndCategorizableComboBox<G extends Group, T extends Groupable<G>> extends JComboBox<T> {
	private static final String CHECKMARK = "\u2713";
	private static final int CHECKMARK_X_OFFSET = 5;
	private static final int CHECKMARK_Y_OFFSET = 5;

	private final String placeHolderText;
	private JPopupMenu categoryPopup;
	private JPopupMenu searchPopup;
	private PlaceholderTextField searchFieldCategory;
	private PlaceholderTextField searchFieldSearch;
	private final Component[] extraCategoryWidgets;
	private JList<T> filteredList;

	private Map<G, List<T>> itemGroupMap;
	private List<T> allItems;

	private int highlightedListIdx = -1;

	private static Color textSelectionBackground;

	static {
		initColors();
	}

	/**
	 * Create a searchable and categorizable combo box.
	 * @param itemGroupMap the map of items and their corresponding groups
	 * @param placeHolderText the placeholder text for the search field (when no text is entered)
	 * @param extraCategoryWidgets extra widgets to add to the category popup. Each widget will be added as a separate menu item.
	 */
	public SearchableAndCategorizableComboBox(ComboBoxModel<T> model, Map<G, List<T>> itemGroupMap, String placeHolderText,
											  Component... extraCategoryWidgets) {
		super(model != null ? model : new DefaultComboBoxModel<>());
		setEditable(false);

		initColors();

		this.extraCategoryWidgets = extraCategoryWidgets;
		this.placeHolderText = placeHolderText;
		updateItems(itemGroupMap);
		setupMainRenderer();

		setupModelListener(model);
		setupSearchFieldListeners();
		addMouseListeners();
	}

	public SearchableAndCategorizableComboBox(Map<G, List<T>> itemGroupMap, String placeHolderText, Component... extraCategoryWidgets) {
		this(null, itemGroupMap, placeHolderText, extraCategoryWidgets);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SearchableAndCategorizableComboBox::updateColors);
	}

	private static void updateColors() {
		textSelectionBackground = GUIUtil.getUITheme().getTextSelectionBackgroundColor();
	}

	public void setupMainRenderer() {
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					label.setText(getDisplayString((T) value));
				}
				return label;
			}
		});
	}

	public void updateItems(Map<G, List<T>> itemGroupMap) {
		this.itemGroupMap = new LinkedHashMap<>(itemGroupMap);  // Create a copy to avoid external modifications
		this.allItems = extractItemsFromMap(itemGroupMap);

		// Update the existing model instead of creating a new one
		ComboBoxModel<T> model = getModel();
		if (model instanceof MutableComboBoxModel<T> mutableModel) {

			// Remove all existing elements
			while (mutableModel.getSize() > 0) {
				mutableModel.removeElementAt(0);
			}

			// Add new elements
			for (T item : allItems) {
				mutableModel.addElement(item);
			}
		} else {
			// If the model is not mutable, we need to set a new model
			// This should be a rare case, as DefaultComboBoxModel is mutable
			setModel(new DefaultComboBoxModel<>(new Vector<>(allItems)));
		}

		// Recreate the search fields only if they don't exist
		if (this.searchFieldCategory == null) {
			this.searchFieldCategory = new PlaceholderTextField();
			this.searchFieldCategory.setPlaceholder(this.placeHolderText);
		}
		if (this.searchFieldSearch == null) {
			this.searchFieldSearch = new PlaceholderTextField();
		}

		// Recreate the filtered list and popups
		this.filteredList = createFilteredList();
		this.categoryPopup = createCategoryPopup();
		this.searchPopup = createSearchPopup();
		this.searchPopup.setPreferredSize(this.categoryPopup.getPreferredSize());

		revalidate();
		repaint();
	}

	private void updateItemsFromModel() {
		ComboBoxModel<T> model = getModel();
		Map<G, List<T>> newGroupMap = new HashMap<>();

		for (int i = 0; i < model.getSize(); i++) {
			T item = model.getElementAt(i);
			G group = item.getGroup();
			newGroupMap.computeIfAbsent(group, k -> new ArrayList<>()).add(item);
		}

		Map<G, List<T>> newItemGroupMap = new HashMap<>(newGroupMap);
		updateItems(newItemGroupMap);
	}

	private List<T> extractItemsFromMap(Map<G, List<T>> itemGroupMap) {
		Set<T> uniqueItems = new HashSet<>(); // Use a Set to ensure uniqueness
		for (G group : itemGroupMap.keySet()) {
			uniqueItems.addAll(itemGroupMap.get(group));
		}
		return new ArrayList<>(uniqueItems);
	}

	private JPopupMenu createCategoryPopup() {
		final JPopupMenu menu = new JPopupMenu();

		// Add the search field at the top
		menu.add(searchFieldCategory);
		menu.addSeparator(); // Separator between search field and menu items

		// Fill the menu with the groups
		for (G group : itemGroupMap.keySet()) {
			JMenu groupMenu = new JMenu(group.toString()) {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					// If the group contains the selected item, draw a checkbox
					if (containsSelectedItem(group, (T) SearchableAndCategorizableComboBox.this.getSelectedItem())) {
						g.drawString(CHECKMARK, CHECKMARK_X_OFFSET, getHeight() - CHECKMARK_Y_OFFSET); // Unicode for checked checkbox
					}
				}
			};
			List<T> itemsForGroup = itemGroupMap.get(group);

			if (itemsForGroup != null) {
				for (T item : itemsForGroup) {
					JMenuItem itemMenu = new JMenuItem(getDisplayString(item)) {
						@Override
						public void paintComponent(Graphics g) {
							super.paintComponent(g);
							// If the item is currently selected, draw a checkmark before it
							if (item == SearchableAndCategorizableComboBox.this.getSelectedItem()) {
								g.drawString(CHECKMARK + " ", CHECKMARK_X_OFFSET, getHeight() - CHECKMARK_Y_OFFSET);
							}
						}
					};
					itemMenu.addActionListener(e -> {
						setSelectedItem(item);
					});
					groupMenu.add(itemMenu);
				}
			}

			menu.add(groupMenu);
		}

		// Extra widgets
		if (extraCategoryWidgets != null) {
			for (Component widget : extraCategoryWidgets) {
				menu.add(widget);
			}
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

	public String getDisplayString(T item) {
		return item.toString();
	}

	private JList<T> createFilteredList() {
		JList<T> list = new JList<>();

		list.setCellRenderer(new FilteredListCellRenderer());
		list.addMouseMotionListener(new FilteredListMouseMotionAdapter());
		list.addListSelectionListener(this::onFilteredListSelectionChanged);

		return list;
	}

	private void onFilteredListSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			selectComboBoxItemFromFilteredList();
		}
	}

	private void selectComboBoxItemFromFilteredList() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				T selectedItem = filteredList.getSelectedValue();
				if (selectedItem != null) {
					SearchableAndCategorizableComboBox.this.setSelectedItem(selectedItem);
					// Hide the popups after selection
					hidePopups();
				}
			}
		});
	}

	private void hidePopups() {
		hideCategoryPopup();
		hideSearchPopup();
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

	private boolean containsSelectedItem(G group, T targetItem) {
		return targetItem != null && targetItem.getGroup().equals(group);
	}

	private void filter(String text) {
		filteredList.removeAll();
		String searchText = text.toLowerCase();
		SortedListModel<T> filteredModel = new SortedListModel<>();

		for (T item : this.allItems) {
			if (getDisplayString(item).toLowerCase().contains(searchText)) {
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
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE -> hidePopups();
			case KeyEvent.VK_ENTER -> selectHighlightedItemInFilteredList();
			case KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT -> highlightNextItemInFilteredList();
			case KeyEvent.VK_UP, KeyEvent.VK_LEFT -> highlightPreviousItemInFilteredList();
		}
	}

	/**
	 * Select the highlighted item in the filtered list and hide the popups.
	 */
	private void selectHighlightedItemInFilteredList() {
		if (highlightedListIdx >= 0 && highlightedListIdx < filteredList.getModel().getSize() && searchPopup.isVisible()) {
			filteredList.setSelectedIndex(highlightedListIdx);
			selectComboBoxItemFromFilteredList();
		}
	}

	/**
	 * Highlight the next item in the filtered list.
	 */
	private void highlightNextItemInFilteredList() {
		if (highlightedListIdx + 1 >= filteredList.getModel().getSize() || !searchPopup.isVisible()) {
			highlightedListIdx++;
			filteredList.ensureIndexIsVisible(highlightedListIdx);
			filteredList.repaint();
		}
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

	private void setupModelListener(ComboBoxModel<T> model) {
		if (model == null) {
			return;
		}
		model.addListDataListener(new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
				updateItemsFromModel();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateItemsFromModel();
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				updateItemsFromModel();
			}
		});
	}

	private void setupSearchFieldListeners() {
		searchFieldCategory.addKeyListener(new SearchFieldKeyAdapter(searchFieldCategory, searchFieldSearch, true));
		searchFieldSearch.addKeyListener(new SearchFieldKeyAdapter(searchFieldSearch, searchFieldCategory, false));

		// Fix a bug where the first character would get selected when the search field gets focus (thus deleting it on
		// the next key press)
		searchFieldSearch.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					searchFieldSearch.setCaretPosition(searchFieldSearch.getText().length());
				});
			}
		});
	}

	/**
	 * Add mouse listener to widgets of the combobox to open our custom popup menu.
	 */
	private void addMouseListeners() {
		// Override the mouse listeners to use our custom popup
		for (MouseListener mouseListener : getMouseListeners()) {
			removeMouseListener(mouseListener);
		}

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

	private class SearchFieldKeyAdapter extends KeyAdapter {
		private final PlaceholderTextField primaryField;
		private final PlaceholderTextField secondaryField;
		private final boolean isCategory;

		SearchFieldKeyAdapter(PlaceholderTextField primary, PlaceholderTextField secondary, boolean isCategory) {
			this.primaryField = primary;
			this.secondaryField = secondary;
			this.isCategory = isCategory;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			overrideActionKeys(e);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			EventQueue.invokeLater(() -> {
				String text = primaryField.getText();
				highlightedListIdx = 0;
				secondaryField.setText(text);
				if (isCategory) {
					handleCategorySearch(text);
				} else {
					handleGeneralSearch(text);
				}
				filter(text);
			});
		}

		private void handleCategorySearch(String text) {
			if (!text.isEmpty() && !searchPopup.isVisible()) {
				hideCategoryPopup();
				showSearchPopup();
			}
		}

		private void handleGeneralSearch(String text) {
			if (text.isEmpty() && !categoryPopup.isVisible()) {
				hideSearchPopup();
				showCategoryPopup();
			}
		}
	}

	private class FilteredListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			T item = (T) value;
			String itemName = getDisplayString(item);

			if (item == getSelectedItem()) {
				itemName = CHECKMARK + " " + itemName;
			}

			if (itemName.toLowerCase().contains(searchFieldSearch.getText().toLowerCase())) {
				itemName = itemName.replaceAll("(?i)(" + searchFieldSearch.getText() + ")", "<u>$1</u>");
				label.setText("<html>" + itemName + "</html>");
			}

			if (highlightedListIdx == index || isSelected) {
				label.setBackground(textSelectionBackground);
				label.setOpaque(true);
			} else {
				label.setOpaque(false);
			}

			return label;
		}
	}

	private class FilteredListMouseMotionAdapter extends MouseAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			Point p = new Point(e.getX(), e.getY());
			int index = filteredList.locationToIndex(p);
			if (index != highlightedListIdx) {
				highlightedListIdx = index;
				filteredList.repaint();
			}
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

