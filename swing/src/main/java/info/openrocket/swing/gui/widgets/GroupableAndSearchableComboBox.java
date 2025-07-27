package info.openrocket.swing.gui.widgets;

import info.openrocket.core.util.Group;
import info.openrocket.core.util.Groupable;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * A combo box that has a search box for searching the items in the combobox.
 * If no text is entered, the combobox items are displayed in a grouped popup menu, grouped according to their groups.
 * @param <G> The type of the group
 * @param <T> The type of the groupable items
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class GroupableAndSearchableComboBox<G extends Group, T extends Groupable<G>> extends JComboBox<T> {
	private static final String CHECKMARK = "\u2713";
	private static final int CHECKMARK_X_OFFSET = 5;
	private static final int CHECKMARK_Y_OFFSET = 5;

	private String placeHolderText;
	private JPopupMenu groupsPopup;
	private JPopupMenu searchPopup;
	private PlaceholderTextField searchFieldGroups;
	private PlaceholderTextField searchFieldSearch;
	private Component[] extraGroupPopupWidgets;
	private JList<T> filteredList;

	private Map<G, List<T>> itemGroupMap;
	private List<T> allItems;

	private int highlightedListIdx = -1;

	private static Color textSelectionBackground;

	static {
		initColors();
	}

	/**
	 * Create a searchable and groupable combo box.
	 * @param placeHolderText the placeholder text for the search field (when no text is entered)
	 * @param extraGroupPopupWidgets extra widgets to add to the groups popup. Each widget will be added as a separate menu item.
	 */
	public GroupableAndSearchableComboBox(ComboBoxModel<T> model, String placeHolderText,
										  Component... extraGroupPopupWidgets) {
		super(model != null ? model : new DefaultComboBoxModel<>());
		List<T> items = new ArrayList<>();
		for (int i = 0; i < Objects.requireNonNull(model).getSize(); i++) {
			items.add(model.getElementAt(i));
		}

		init(model, constructItemGroupMapFromList(items), placeHolderText, extraGroupPopupWidgets);
	}

	public GroupableAndSearchableComboBox(List<T> allItems, String placeHolderText, Component... extraGroupPopupWidgets) {
		super();

		init(null, constructItemGroupMapFromList(allItems), placeHolderText, extraGroupPopupWidgets);
	}

	private void init(ComboBoxModel<T> model, Map<G, List<T>> itemGroupMap, String placeHolderText, Component... extraGroupsPopupWidgets) {
		setEditable(false);

		initColors();

		this.extraGroupPopupWidgets = extraGroupsPopupWidgets;
		this.placeHolderText = placeHolderText;
		this.itemGroupMap = itemGroupMap;
		updateItems(itemGroupMap);
		setupMainRenderer();

		setupModelListener(model);
		setupSearchFieldListeners();
		addMouseListeners();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(GroupableAndSearchableComboBox::updateColors);
	}

	public static void updateColors() {
		textSelectionBackground = GUIUtil.getUITheme().getTextSelectionBackgroundColor();
	}

	private Map<G, List<T>> constructItemGroupMapFromList(List<T> items) {
		Map<G, List<T>> itemGroupMap = new TreeMap<>(Comparator.comparing(Group::getPriority));

		for (T item : items) {
			G group = item.getGroup();
			itemGroupMap.computeIfAbsent(group, k -> new ArrayList<>()).add(item);
		}

		// Sort items within each group
		for (List<T> groupItems : itemGroupMap.values()) {
			groupItems.sort(new ItemComparator());
		}

		return itemGroupMap;
	}

	private class ItemComparator implements Comparator<T> {
		@Override
		@SuppressWarnings("unchecked")
		public int compare(T item1, T item2) {
			if (item1 instanceof Comparable && item2 instanceof Comparable) {
				return ((Comparable<T>) item1).compareTo(item2);
			} else {
				// Fall back to alphabetical sorting using the display string
				return getDisplayString(item1).compareToIgnoreCase(getDisplayString(item2));
			}
		}
	}

	public void setupMainRenderer() {
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					label.setText(getDisplayString((T) value));
				} else {
					// Handle the case when no item is selected
					label.setText("");
				}
				return label;
			}
		});
	}

	public void updateItems(Map<G, List<T>> itemGroupMap) {
		this.itemGroupMap = new LinkedHashMap<>(itemGroupMap);  // Create a copy to avoid external modifications
		this.allItems = extractItemsFromMap(itemGroupMap);

		// Update the model
		if (getModel() instanceof DefaultComboBoxModel<T>) {
			T selectedItem = (T) getModel().getSelectedItem();
			ComboBoxModel<T> model = new DefaultComboBoxModel<>(new Vector<>(allItems));
			model.setSelectedItem(selectedItem);
			setModel(model);
			setupModelListener(model);
		}

		// Recreate the search fields only if they don't exist
		if (this.searchFieldGroups == null) {
			this.searchFieldGroups = new PlaceholderTextField();
			this.searchFieldGroups.setPlaceholder(this.placeHolderText);
		}
		if (this.searchFieldSearch == null) {
			this.searchFieldSearch = new PlaceholderTextField();
		}

		// Recreate the filtered list and popups
		this.filteredList = createFilteredList();
		this.groupsPopup = createGroupsPopup();
		this.searchPopup = createSearchPopup();
		this.searchPopup.setPreferredSize(this.groupsPopup.getPreferredSize());

		revalidate();
		repaint();
	}

	public void updateItemsFromModel() {
		ComboBoxModel<T> model = getModel();
		if (model == null) {
			return;
		}
		List<T> items = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			T item = model.getElementAt(i);
			if (item != null) {
				items.add(item);
			}
		}
		updateItems(constructItemGroupMapFromList(items));
	}

	private List<T> extractItemsFromMap(Map<G, List<T>> itemGroupMap) {
		Set<T> uniqueItems = new HashSet<>(); // Use a Set to ensure uniqueness
		for (G group : itemGroupMap.keySet()) {
			uniqueItems.addAll(itemGroupMap.get(group));
		}
		return new ArrayList<>(uniqueItems);
	}

	static class DeselectMenuListener extends MouseAdapter {
		final List<JMenu> groupMenus;
		final JMenu ownMenu;

		DeselectMenuListener(List<JMenu> groupMenus, JMenu ownMenu) {
			this.groupMenus = groupMenus;
			this.ownMenu = ownMenu;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			SwingUtilities.invokeLater(() -> {
				for (JMenu groupMenu : groupMenus) {
					if (groupMenu != ownMenu) {
						groupMenu.setSelected(false);
						groupMenu.setPopupMenuVisible(false);
					}
				}
				if (ownMenu != null) {
					ownMenu.setSelected(true);
					ownMenu.setPopupMenuVisible(true);
				}
			});
		}
	}

	private JPopupMenu createGroupsPopup() {
		final JPopupMenu menu = new JPopupMenu();
		final List<JMenu> groupMenus = new ArrayList<>();

		// Add the search field at the top
		menu.add(searchFieldGroups);
		searchFieldGroups.addMouseListener(new DeselectMenuListener(groupMenus, null));
		menu.addSeparator();

		// Fill the menu with the groups
		for (G group : itemGroupMap.keySet()) {
			JMenu groupMenu = new JMenu(group.toString()) {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					// If the group contains the selected item, draw a checkbox
					if (containsSelectedItem(group, (T) GroupableAndSearchableComboBox.this.getSelectedItem())) {
						g.drawString(CHECKMARK, CHECKMARK_X_OFFSET, getHeight() - CHECKMARK_Y_OFFSET);
					}
				}
			};
			List<T> itemsForGroup = itemGroupMap.get(group);

			if (itemsForGroup != null) {
				for (T item : itemsForGroup) {
					JCheckBoxMenuItem itemMenu = new JCheckBoxMenuItem(getDisplayString(item));
					itemMenu.setSelected(item == GroupableAndSearchableComboBox.this.getSelectedItem());
					itemMenu.addActionListener(e -> {
						setSelectedItem(item);
					});
					groupMenu.add(itemMenu);
				}
			}

			groupMenus.add(groupMenu);
			menu.add(groupMenu);
			groupMenu.addMouseListener(new DeselectMenuListener(groupMenus, groupMenu));
		}

		// Extra widgets
		if (extraGroupPopupWidgets != null) {
			for (Component widget : extraGroupPopupWidgets) {
				menu.add(widget);
				widget.addMouseListener(new DeselectMenuListener(groupMenus, null));
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
					GroupableAndSearchableComboBox.this.setSelectedItem(selectedItem);
				}
			}
		});
	}

	public void hidePopups() {
		hideGroupsPopup();
		hideSearchPopup();
	}

	private void showGroupsPopup() {
		groupsPopup.show(this, 0, getHeight());
		searchFieldSearch.setText("");
		searchFieldGroups.setText("");
	}

	private void hideGroupsPopup() {
		groupsPopup.setVisible(false);
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

	@Override
	public void setSelectedItem(Object anObject) {
		// Hide the popups after selection
		hidePopups();
		getModel().setSelectedItem(anObject);
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
		return groupsPopup.isVisible() || searchPopup.isVisible();
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

	private void setupModelListener(ComboBoxModel<T> model) {
		/*if (model == null) {
			return;
		}
		model.addListDataListener(this);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setSelectedItem(GroupableAndSearchableComboBox.this.getSelectedItem());
			}
		});*/
	}

	private void setupSearchFieldListeners() {
		searchFieldGroups.addKeyListener(new SearchFieldKeyAdapter(searchFieldGroups, searchFieldSearch, true));
		searchFieldSearch.addKeyListener(new SearchFieldKeyAdapter(searchFieldSearch, searchFieldGroups, false));

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
							showGroupsPopup();
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
								showGroupsPopup();
							}
						}
					});
				}
			});
		}
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		super.intervalAdded(e);
		updateItemsFromModel();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		super.intervalRemoved(e);
		updateItemsFromModel();
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		super.contentsChanged(e);
		updateItemsFromModel();
	}

	private class SearchFieldKeyAdapter extends KeyAdapter {
		private final PlaceholderTextField primaryField;
		private final PlaceholderTextField secondaryField;
		private final boolean isGroupsPopup;

		SearchFieldKeyAdapter(PlaceholderTextField primary, PlaceholderTextField secondary, boolean isGroupsPopup) {
			this.primaryField = primary;
			this.secondaryField = secondary;
			this.isGroupsPopup = isGroupsPopup;
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
				if (isGroupsPopup) {
					handleGroupsPopupSearch(text);
				} else {
					handleSearchPopupSearch(text);
				}
				filter(text);
			});
		}

		private void handleGroupsPopupSearch(String text) {
			if (!text.isEmpty() && !searchPopup.isVisible()) {
				hideGroupsPopup();
				showSearchPopup();
			}
		}

		private void handleSearchPopupSearch(String text) {
			if (text.isEmpty() && !groupsPopup.isVisible()) {
				hideSearchPopup();
				showGroupsPopup();
			}
		}

		/**
		 * Override the default action keys (escape, enter, arrow keys) to do our own actions.
		 * @param e the key event
		 */
		private void overrideActionKeys(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE -> hidePopups();
				case KeyEvent.VK_ENTER -> selectHighlightedItemInFilteredList();
				case KeyEvent.VK_DOWN -> highlightNextItemInFilteredList();
				case KeyEvent.VK_UP -> highlightPreviousItemInFilteredList();
				default -> {
					return;
				}
			}
			e.consume();
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
				itemName = itemName.replaceAll("(?i)(" + Pattern.quote(searchFieldSearch.getText()) + ")", "<u>$1</u>");
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

