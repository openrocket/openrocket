package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class MultiLevelWindTable extends JPanel implements ChangeSource {
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private static final double ALTITUDE_INCREASE = 100;		// Default altitude increase when adding a new row
	private static final Font HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final int FLASH_DURATION_MS = 800;
	private static final int CELL_GAP = 1;
	private static final int CELL_PADDING = 8; // Padding inside cells

	// Table column definitions
	private record ColumnDefinition(String header, int width) { }

	private static final ColumnDefinition[] COLUMNS = {
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Altitude"), 110),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Speed"), 120),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Direction"), 100),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.StandardDeviation"), 120),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Turbulence"), 100),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Intensity"), 85),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Delete"), 60)
	};

	public interface RowSelectionListener {
		void onRowSelected(LevelWindModel level);
	}

	// Instance fields
	private final JPanel rowsPanel;
	private final JPanel headerPanel;
	private final MultiLevelPinkNoiseWindModel windModel;
	private final List<LevelRow> rows;
	private LevelRow changedRow = null;

	private LevelRow selectedRow = null;
	private final List<RowSelectionListener> selectionListeners = new ArrayList<>();
	private final List<StateChangeListener> listeners = new ArrayList<>();

	private static Color tableHeaderBg;
	private static Color tableBorderColor;
	private static Color evenRowColor;
	private static Color oddRowColor;
	private static Color flashColor;
	private static Color selectedRowColor;

	static {
		initColors();
	}

	public MultiLevelWindTable(MultiLevelPinkNoiseWindModel windModel) {
		this.windModel = windModel;
		this.rows = new ArrayList<>();
		setLayout(new BorderLayout());

		// Build header panel
		headerPanel = createHeaderPanel();

		// Build rows panel
		rowsPanel = new JPanel();
		rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

		// Calculate total width based on column definitions
		int totalWidth = 0;
		for (ColumnDefinition col : COLUMNS) {
			totalWidth += col.width();
		}
		// Add some extra width for separators
		totalWidth += (COLUMNS.length - 1); // 1px for each separator
		
		// Ensure rows panel has its preferred width set but allows vertical scrolling
		rowsPanel.setMinimumSize(new Dimension(totalWidth, 10));
		
		// Set a small preferred height initially to ensure vertical scrolling works
		// This will be updated when rows are added to match actual content height
		rowsPanel.setPreferredSize(new Dimension(totalWidth, 10));

		// Populate rows from the wind model
		windModel.getLevels().forEach(lvl -> {
			LevelRow row = new LevelRow(lvl);
			rows.add(row);
			rowsPanel.add(row);
		});

		// Initial sort
		resortRows(null);
		
		// Initialize delete buttons state
		updateDeleteButtonsState();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(MultiLevelWindTable::updateColors);
	}

	public static void updateColors() {
		UITheme.Theme currentTheme = GUIUtil.getUITheme();
		tableHeaderBg = currentTheme.getRowBackgroundDarkerColor();
		tableBorderColor = currentTheme.getBorderColor();
		evenRowColor = currentTheme.getRowBackgroundLighterColor();
		oddRowColor = currentTheme.getRowBackgroundDarkerColor();
		flashColor = currentTheme.getTableRowFlashColor();
		selectedRowColor = currentTheme.getTableRowSelectionColor();
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 1, 0, tableBorderColor),
				BorderFactory.createEmptyBorder(1, 0, 1, 0)
		));
		panel.setBackground(tableHeaderBg);

		for (int i = 0; i < COLUMNS.length; i++) {
			JLabel label = new JLabel(COLUMNS[i].header, SwingConstants.CENTER);
			label.setFont(HEADER_FONT);
			JPanel cell = createFixedCell(label, COLUMNS[i].width);
			cell.setBackground(tableHeaderBg);
			panel.add(cell);

			if (i < COLUMNS.length - 1) {
				panel.add(createVerticalSeparator());
			}
		}

		return panel;
	}

	private JPanel createFixedCell(JComponent comp, int width) {
		JPanel panel = new JPanel(new BorderLayout());
		// Add padding around the component
		panel.setBorder(BorderFactory.createEmptyBorder(CELL_PADDING / 2, CELL_PADDING, CELL_PADDING / 2, CELL_PADDING));
		panel.add(comp, BorderLayout.CENTER);
		Dimension size = new Dimension(width, comp.getPreferredSize().height + CELL_PADDING);
		panel.setPreferredSize(size);
		panel.setMinimumSize(size);
		panel.setMaximumSize(size);
		return panel;
	}

	private JPanel createGroupCell(JComponent comp1, JComponent comp2, int width) {
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
		group.setOpaque(false);
		
		// Add left padding
		group.add(Box.createRigidArea(new Dimension(CELL_PADDING, 0)));
		
		// Add components with spacing between them
		group.add(comp1);
		group.add(Box.createRigidArea(new Dimension(CELL_GAP, 0)));
		group.add(comp2);
		
		// Add right padding
		group.add(Box.createRigidArea(new Dimension(CELL_PADDING, 0)));
		
		// Fixed height based on row height constant to ensure consistent alignment
		Dimension currentSize = group.getPreferredSize();
		Dimension size = new Dimension(width, currentSize.height);
		group.setPreferredSize(size);
		group.setMinimumSize(size);
		group.setMaximumSize(size);
		return group;
	}

	private Component createVerticalSeparator() {
		JPanel sep = new JPanel();
		sep.setPreferredSize(new Dimension(1, 0));
		sep.setMaximumSize(new Dimension(1, Integer.MAX_VALUE));
		sep.setBackground(tableBorderColor);
		return sep;
	}

	public void addRow() {
		double newAltitude = rows.isEmpty() ? 0
				: rows.stream().mapToDouble(LevelRow::getAltitude).max().orElse(0) + ALTITUDE_INCREASE;

		LevelRow prevRow = rows.isEmpty() ? null : rows.get(rows.size() - 1);
		double newSpeed = prevRow != null ? prevRow.getLevel().getSpeed() : prefs.getAverageWindModel().getAverage();
		double newDirection = prevRow != null ? prevRow.getLevel().getDirection() : 0;
		double newStdDeviation = prevRow != null ? prevRow.getLevel().getStandardDeviation() : 0;

		// Add to model
		windModel.addWindLevel(newAltitude, newSpeed, newDirection, newStdDeviation);

		// Find the added level
		Optional<LevelWindModel> newLevel = windModel.getLevels().stream()
				.filter(lvl -> Math.abs(lvl.getAltitude() - newAltitude) < 1e-6)
				.findFirst();

		newLevel.ifPresent(lvl -> {
			List<LevelRow> originalOrder = new ArrayList<>(rows);
			LevelRow row = new LevelRow(lvl);
			rows.add(row);
			changedRow = row; // Mark the new row to be highlighted
			int thisIdx = rows.indexOf(row);
			resortRows(originalOrder, thisIdx, thisIdx);

			// Scroll to make the new row visible
			SwingUtilities.invokeLater(() -> {
				if (rowsPanel.getParent() instanceof JViewport viewport) {
					Rectangle bounds = row.getBounds();
					viewport.scrollRectToVisible(bounds);
				}
			});

			updateDeleteButtonsState();
		});
	}

	private void deleteRow(LevelRow row) {
		// Don't allow deleting the last row
		if (rows.size() <= 1) {
			return;
		}
		
		List<LevelRow> originalOrder = new ArrayList<>(rows);
		int prevIdx = Math.max(0, rows.indexOf(row) - 1);
		int thisIdx = rows.indexOf(row);
		rows.remove(row);
		thisIdx = Math.min(thisIdx, rows.size() - 1);
		windModel.removeWindLevel(row.getLevel().getAltitude());
		resortRows(originalOrder, prevIdx, thisIdx);
		selectRow(null);

		updateDeleteButtonsState();
	}

	public void deleteSelectedRow() {
		if (selectedRow != null) {
			deleteRow(selectedRow);
		}
	}
	
	/**
	 * Updates the enabled state of all delete buttons based on the number of rows.
	 * Disables delete functionality when there's only one row left.
	 */
	private void updateDeleteButtonsState() {
		boolean enabled = rows.size() > 1;
		for (LevelRow row : rows) {
			row.setDeleteEnabled(enabled);
		}
	}

	/**
	 * Select a row in the table.
	 * @param row the row to select, or null to deselect all rows
	 */
	private void selectRow(LevelRow row) {
		// Deselect the currently selected row
		if (selectedRow != null) {
			selectedRow.setSelected(false);
		}

		// Select the new row
		selectedRow = row;
		if (row != null) {
			row.setSelected(true);
		}

		// Notify listeners
		for (RowSelectionListener listener : selectionListeners) {
			listener.onRowSelected(row == null ? null : row.getLevel());
		}
	}
	
	/**
	 * Clears the current row selection.
	 */
	public void clearSelection() {
		selectRow(null);
	}

	/**
	 * Resort the rows based on altitude and update the UI.
	 * @param originalOrder the original order of rows before adding/removing a row
	 */
	private void resortRows(List<LevelRow> originalOrder, int highlightStartIdx, int highlightEndIdx) {
		// Perform the sort
		rows.sort(Comparator.comparingDouble(LevelRow::getAltitude));

		// Check if order changed
		boolean orderChanged = false;
		if (originalOrder != null) {
			if (originalOrder.size() != rows.size()) {
				orderChanged = true;
			} else if (!rows.isEmpty()) {
				for (int i = 0; i < rows.size(); i++) {
					if (originalOrder.get(i) != rows.get(i)) {
						orderChanged = true;
						break;
					}
				}
			}
		} else {
			orderChanged = true;
		}

		// Don't do anything if the order didn't change
		if (!orderChanged) {
			// Still update the UI
			rowsPanel.revalidate();
			rowsPanel.repaint();
			fireChangeEvent();

			return;
		}

		rowsPanel.removeAll();

		// Add all rows with alternating background colors
		for (int i = 0; i < rows.size(); i++) {
			LevelRow row = rows.get(i);
			Color bg = (i % 2 == 0) ? evenRowColor : oddRowColor;
			row.setBaseBackground(bg);
			rowsPanel.add(row);
		}
		
		// Update the rows panel preferred size based on row count
		Dimension size = rowsPanel.getPreferredSize();
		if (!rows.isEmpty()) {
			// Set height based on the total height of all rows
			size.height = rows.stream()
					.mapToInt(row -> row.getPreferredSize().height)
					.sum();
			rowsPanel.setPreferredSize(size);
		}

		// Highlight changed rows
		highlightChangedRows(highlightStartIdx, highlightEndIdx);

		rowsPanel.revalidate();
		rowsPanel.repaint();
		fireChangeEvent();
	}

	private void resortRows(List<LevelRow> originalOrder) {
		resortRows(originalOrder, -1, -1);
	}

	/**
	 * Synchronize the rows with the wind model.
	 */
	private void syncRowsFromModel() {
		rows.clear();
		windModel.getLevels().forEach(lvl -> {
			LevelRow row = new LevelRow(lvl);
			rows.add(row);
		});
		resortRows(null);
	}

	public void importLevels(File file, String separator) {
		windModel.importLevelsFromCSV(file, separator);
		syncRowsFromModel();
	}

	public void resetLevels() {
		windModel.resetLevels();
		syncRowsFromModel();
	}

	private void highlightChangedRows(int highlightStartIdx, int highlightEndIdx) {
		// If a range is specified, highlight the range
		if (highlightStartIdx != -1 && highlightEndIdx != -1) {
			for (int i = highlightStartIdx; i <= highlightEndIdx; i++) {
				rows.get(i).flashMovement();
			}
			changedRow = null;
		}
		// Otherwise, highlight the changed row and its neighbors
		else if (changedRow != null) {
			int idx = rows.indexOf(changedRow);
			Set<LevelRow> highlightSet = new HashSet<>();
			highlightSet.add(changedRow);

			if (idx > 0) {
				highlightSet.add(rows.get(idx - 1));
			}
			if (idx < rows.size() - 1) {
				highlightSet.add(rows.get(idx + 1));
			}

			highlightSet.forEach(LevelRow::flashMovement);
			changedRow = null;
		}
	}

	public JPanel getRowsPanel() {
		return rowsPanel;
	}

	public JPanel getHeaderPanel() {
		return headerPanel;
	}

	public void addSelectionListener(RowSelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(RowSelectionListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeEvent() {
		EventObject event = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}


	// Inner class representing one wind level row
	private class LevelRow extends JPanel {
		private final LevelWindModel level;
		private final DoubleModel dmAltitude;
		private final JLabel intensityLabel;
		private final JButton deleteButton;
		private JMenuItem deleteItem;

		private boolean selected = false;
		
		public LevelRow(LevelWindModel level) {
			this.level = level;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			// Create DoubleModels bound to the level
			dmAltitude = new DoubleModel(level, "Altitude", UnitGroup.UNITS_DISTANCE, -100, ExtendedISAModel.getMaximumAllowedAltitude());
			DoubleModel dmSpeed = new DoubleModel(level, "Speed", UnitGroup.UNITS_WINDSPEED, 0, 10.0);
			DoubleModel dmDirection = new DoubleModel(level, "Direction", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
			DoubleModel dmStdDeviation = new DoubleModel(level, "StandardDeviation", UnitGroup.UNITS_WINDSPEED, 0, dmSpeed);
			DoubleModel dmTurbulence = new DoubleModel(level, "TurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0, 1);

			// Add property change listener for altitude
			dmAltitude.addChangeListener(e -> {
				changedRow = this;
				List<LevelRow> originalOrder = new ArrayList<>(rows);
				resortRows(originalOrder);
			});

			// Add a single state change listener for efficiency
			StateChangeListener stateListener = e -> MultiLevelWindTable.this.fireChangeEvent();
			dmAltitude.addChangeListener(stateListener);
			dmSpeed.addChangeListener(stateListener);
			dmDirection.addChangeListener(stateListener);
			dmStdDeviation.addChangeListener(stateListener);
			dmStdDeviation.addChangeListener(dmTurbulence);
			dmTurbulence.addChangeListener(stateListener);
			dmTurbulence.addChangeListener(dmStdDeviation);

			// Create UI components for each column
			JPanel altitudeGroup = createSpinnerWithUnitSelector(dmAltitude, COLUMNS[0].width);
			JPanel speedGroup = createSpinnerWithUnitSelector(dmSpeed, COLUMNS[1].width);
			JPanel directionGroup = createSpinnerWithUnitSelector(dmDirection, COLUMNS[2].width);
			JPanel stdDeviationGroup = createSpinnerWithUnitSelector(dmStdDeviation, COLUMNS[3].width);
			JPanel turbulenceGroup = createSpinnerWithUnitSelector(dmTurbulence, COLUMNS[4].width);

			// Intensity description label
			intensityLabel = new JLabel(level.getIntensityDescription());
			JPanel intensityCell = createFixedCell(intensityLabel, COLUMNS[5].width);
			intensityCell.setOpaque(false);

			// Update intensity description when turbulence changes
			dmTurbulence.addChangeListener(e -> intensityLabel.setText(level.getIntensityDescription()));

			// Delete button with improved styling
			deleteButton = new JButton(Icons.EDIT_DELETE);
			deleteButton.setToolTipText(trans.get("MultiLevelWindTable.but.deleteWindLevel.ttip"));
			deleteButton.addActionListener(e -> deleteRow(this));
			// Button state will be set by updateDeleteButtonsState()

			JPanel deleteCell = createFixedCell(deleteButton, COLUMNS[6].width);
			deleteCell.setOpaque(false);

			// Add all cells to row with separators
			add(altitudeGroup);
			add(createVerticalSeparator());
			add(speedGroup);
			add(createVerticalSeparator());
			add(directionGroup);
			add(createVerticalSeparator());
			add(stdDeviationGroup);
			add(createVerticalSeparator());
			add(turbulenceGroup);
			add(createVerticalSeparator());
			add(intensityCell);
			add(createVerticalSeparator());
			add(deleteCell);

			// Calculate the total width
			int totalWidth = 0;
			for (ColumnDefinition col : COLUMNS) {
				totalWidth += col.width();
			}
			// Add some extra width for separators
			totalWidth += (COLUMNS.length - 1); // 1px for each separator

			// Update dimensions with fixed width and height 
			Dimension currentSize = getPreferredSize();
			Dimension size = new Dimension(totalWidth, currentSize.height);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size); // Fixed height to ensure consistent row heights

			// Install right-click context menu
			installContextMenu();
		}

		// Helper method to create spinner with unit selector
		private JPanel createSpinnerWithUnitSelector(DoubleModel model, int width) {
			JSpinner spinner = new JSpinner(model.getSpinnerModel());
			spinner.setEditor(new SpinnerEditor(spinner));
			UnitSelector unitSelector = new UnitSelector(model);

			// Get editor component for focus tracking
			Component editorComponent = getEditorComponent(spinner);

			// Add both components to focus tracking
			addToFocusTracking(editorComponent);
			addToFocusTracking(unitSelector);

			return createGroupCell(spinner, unitSelector, width);
		}

		// Helper method to get editor component from spinner
		private Component getEditorComponent(JSpinner spinner) {
			Component editor = spinner.getEditor();
			if (editor instanceof JSpinner.DefaultEditor) {
				return ((JSpinner.DefaultEditor) editor).getTextField();
			}
			return editor;
		}

		// Add component to focus tracking system
		private void addToFocusTracking(Component component) {
			component.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					// Select this row in the table
					MultiLevelWindTable.this.selectRow(LevelRow.this);
					
					SwingUtilities.invokeLater(LevelRow.this::updateHighlight);
				}

				@Override
				public void focusLost(FocusEvent e) {
					if (selectedRow == LevelRow.this) {
						MultiLevelWindTable.this.selectRow(null);
					}
					SwingUtilities.invokeLater(LevelRow.this::updateHighlight);
				}
			});
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			updateHighlight();
		}
		
		/**
		 * Set the delete button and menu item enabled/disabled.
		 *
		 * @param enabled true to enable, false to disable
		 */
		public void setDeleteEnabled(boolean enabled) {
			deleteButton.setEnabled(enabled);
			if (deleteItem != null) {
				deleteItem.setEnabled(enabled);
			}
		}

		// Update highlighting based on selection and edit mode
		private void updateHighlight() {
			if (selected) {
				// Also add a slight background tint
				setBackground(selectedRowColor);
			} else {
				// Restore original background
				Color bg = (rows.indexOf(this) % 2 == 0) ? evenRowColor : oddRowColor;
				setBackground(bg);
			}
			
			revalidate();
			repaint();
		}

		// Install context menu for row deletion
		private void installContextMenu() {
			JPopupMenu popup = new JPopupMenu();
			deleteItem = new JMenuItem(trans.get("MultiLevelWindTable.popupmenu.Delete"), Icons.EDIT_DELETE);
			deleteItem.addActionListener(e -> deleteRow(this));
			popup.add(deleteItem);
			popup.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					MultiLevelWindTable.this.selectRow(LevelRow.this);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					MultiLevelWindTable.this.selectRow(null);
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					MultiLevelWindTable.this.selectRow(null);
				}
			});

			// Apply popup menu to all components recursively
			applyToAllComponents(this, component -> {
				if (component instanceof JComponent) {
					((JComponent) component).setComponentPopupMenu(popup);
				}
			});
		}

		// Apply an action to all components in a container recursively
		private void applyToAllComponents(Component component, Consumer<Component> action) {
			action.accept(component);
			if (component instanceof Container) {
				for (Component child : ((Container) component).getComponents()) {
					applyToAllComponents(child, action);
				}
			}
		}

		// Getters and utility methods
		public double getAltitude() {
			return dmAltitude.getValue();
		}

		public LevelWindModel getLevel() {
			return level;
		}

		public void setBaseBackground(Color bg) {
			setBackground(bg);
		}

		// Flash the row with a highlight color temporarily
		public void flashMovement() {
			final Color originalColor = getBackground();
			setBackground(flashColor);

			Timer timer = new Timer(FLASH_DURATION_MS, e -> {
				setBackground(originalColor);
				((Timer) e.getSource()).stop();
			});

			timer.setRepeats(false);
			timer.start();
		}
	}
}