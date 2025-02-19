package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
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
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
	// Forward declarations for UI themes and accessibility
	private static final Color TABLE_HEADER_BG = new Color(230, 230, 230);
	private static final Color TABLE_BORDER_COLOR = new Color(200, 200, 200);
	private static final Font HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final int ROW_HEIGHT = 30;
	// Constants
	private static final Translator trans = Application.getTranslator();
	private static final Color EVEN_ROW_COLOR = Color.WHITE;
	private static final Color ODD_ROW_COLOR = new Color(240, 240, 240);
	private static final Color FLASH_COLOR = new Color(255, 255, 153);
	private static final Border DEFAULT_ROW_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
	private static final int FLASH_DURATION_MS = 800;
	private static final int CELL_GAP = 5;
	private static final int CELL_PADDING = 8; // Padding inside cells

	// Table column definitions
	private record ColumnDefinition(String header, int width) { }

	private static final ColumnDefinition[] COLUMNS = {
			new ColumnDefinition(trans.get("simedtdlg.col.Altitude"), 120),
			new ColumnDefinition(trans.get("simedtdlg.col.Speed"), 120),
			new ColumnDefinition(trans.get("simedtdlg.col.Direction"), 120),
			new ColumnDefinition(trans.get("simedtdlg.col.StandardDeviation"), 145),
			new ColumnDefinition(trans.get("simedtdlg.col.Turbulence"), 120),
			new ColumnDefinition(trans.get("simedtdlg.col.Intensity"), 85),
			new ColumnDefinition(trans.get("simedtdlg.col.Delete"), 60)
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

		// Set the preferred size for the rows panel
		rowsPanel.setPreferredSize(new Dimension(totalWidth, rowsPanel.getPreferredSize().height));

		// Populate rows from the wind model
		windModel.getLevels().forEach(lvl -> {
			LevelRow row = new LevelRow(lvl);
			rows.add(row);
			rowsPanel.add(row);
		});

		// Initial sort
		resortRows();
	}

	// UI Creation Methods
	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, TABLE_BORDER_COLOR),
				BorderFactory.createEmptyBorder(2, 0, 2, 0)
		));
		panel.setBackground(TABLE_HEADER_BG);

		for (int i = 0; i < COLUMNS.length; i++) {
			JLabel label = new JLabel(COLUMNS[i].header, SwingConstants.CENTER);
			label.setFont(HEADER_FONT);
			JPanel cell = createFixedCell(label, COLUMNS[i].width);
			cell.setBackground(TABLE_HEADER_BG);
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
		panel.setBorder(BorderFactory.createEmptyBorder(CELL_PADDING, CELL_PADDING, CELL_PADDING, CELL_PADDING));
		panel.add(comp, BorderLayout.CENTER);
		Dimension size = new Dimension(width, comp.getPreferredSize().height + CELL_PADDING * 2);
		panel.setPreferredSize(size);
		panel.setMinimumSize(size);
		panel.setMaximumSize(size);
		return panel;
	}

	private JPanel createGroupCell(JComponent comp1, JComponent comp2, int width) {
		JPanel group = new JPanel();
		group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
		group.setOpaque(false);
		group.add(comp1);
		group.add(Box.createRigidArea(new Dimension(CELL_GAP, 0)));
		group.add(comp2);
		
		// Fixed height based on row height constant to ensure consistent alignment
		Dimension size = new Dimension(width, ROW_HEIGHT - (CELL_PADDING * 2));
		group.setPreferredSize(size);
		group.setMinimumSize(size);
		group.setMaximumSize(size);
		return group;
	}

	private Component createVerticalSeparator() {
		JPanel sep = new JPanel();
		sep.setPreferredSize(new Dimension(1, 0));
		sep.setMaximumSize(new Dimension(1, Integer.MAX_VALUE));
		sep.setBackground(Color.GRAY);
		return sep;
	}

	// Row Management Methods
	public void addRow() {
		double newAltitude = rows.isEmpty()
				? 0
				: rows.stream().mapToDouble(LevelRow::getAltitude).max().orElse(0) + 100;

		// Add to model
		windModel.addWindLevel(newAltitude, 5.0, Math.PI / 2, 0.2);

		// Find the added level
		Optional<LevelWindModel> newLevel = windModel.getLevels().stream()
				.filter(lvl -> Math.abs(lvl.getAltitude() - newAltitude) < 1e-6)
				.findFirst();

		newLevel.ifPresent(lvl -> {
			LevelRow row = new LevelRow(lvl);
			rows.add(row);
			changedRow = row; // Mark the new row to be highlighted
			resortRows();

			// Scroll to make the new row visible
			SwingUtilities.invokeLater(() -> {
				if (rowsPanel.getParent() instanceof JViewport viewport) {
					Rectangle bounds = row.getBounds();
					viewport.scrollRectToVisible(bounds);
				}
			});
		});
	}

	private void removeRow(LevelRow row) {
		rows.remove(row);
		windModel.removeWindLevel(row.getLevel().getAltitude());
		resortRows();
	}

	private void selectRow(LevelRow row) {
		if (selectedRow != null) {
			selectedRow.setSelected(false);
		}
		selectedRow = row;
		if (row != null) {
			row.setSelected(true);
		}
		// Notify listeners
		for (RowSelectionListener listener : selectionListeners) {
			listener.onRowSelected(row == null ? null : row.getLevel());
		}
	}

	private void resortRows() {
		rows.sort(Comparator.comparingDouble(LevelRow::getAltitude));
		rowsPanel.removeAll();

		// Add all rows with alternating background colors
		for (int i = 0; i < rows.size(); i++) {
			LevelRow row = rows.get(i);
			Color bg = (i % 2 == 0) ? EVEN_ROW_COLOR : ODD_ROW_COLOR;
			row.setBaseBackground(bg);
			rowsPanel.add(row);
		}

		// Highlight changed rows
		highlightChangedRows();

		rowsPanel.revalidate();
		rowsPanel.repaint();
		fireChangeEvent();
	}

	private void highlightChangedRows() {
		if (changedRow != null) {
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

	// Accessor methods for embedding in a JScrollPane
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
		private final DoubleModel dmSpeed;
		private final DoubleModel dmDirection;
		private final DoubleModel dmStdDeviation;
		private final DoubleModel dmTurbulence;
		private final JLabel intensityLabel;
		private final List<Component> focusComponents = new ArrayList<>();

		private boolean selected = false;
		
		public LevelRow(LevelWindModel level) {
			this.level = level;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBorder(BorderFactory.createCompoundBorder(
					DEFAULT_ROW_BORDER,
					BorderFactory.createEmptyBorder(2, 0, 2, 0)
			));
			
			// Always use fixed row height for consistent layout
			Dimension rowSize = new Dimension(0, ROW_HEIGHT); // Width will be set later
			setPreferredSize(rowSize);
			setMinimumSize(rowSize);
			setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

			// Create DoubleModels bound to the level
			dmAltitude = new DoubleModel(level, "Altitude", UnitGroup.UNITS_DISTANCE, -100, ExtendedISAModel.getMaximumAllowedAltitude());
			dmSpeed = new DoubleModel(level, "Speed", UnitGroup.UNITS_WINDSPEED, 0, 10.0);
			dmDirection = new DoubleModel(level, "Direction", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
			dmStdDeviation = new DoubleModel(level, "StandardDeviation", UnitGroup.UNITS_WINDSPEED, 0,
					new DoubleModel(level, "Speed", 0.25, UnitGroup.UNITS_COEFFICIENT, 0));
			dmTurbulence = new DoubleModel(level, "TurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0, 1);

			// Add property change listener for altitude
			dmAltitude.addChangeListener(e -> {
				changedRow = this;
				resortRows();
			});

			// Add a single state change listener for efficiency
			StateChangeListener stateListener = e -> MultiLevelWindTable.this.fireChangeEvent();
			dmAltitude.addChangeListener(stateListener);
			dmSpeed.addChangeListener(stateListener);
			dmDirection.addChangeListener(stateListener);
			dmStdDeviation.addChangeListener(stateListener);
			dmTurbulence.addChangeListener(stateListener);

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
			JButton deleteButton = new JButton(Icons.EDIT_DELETE);
			deleteButton.setToolTipText("Delete this row");
			deleteButton.addActionListener(e -> removeRow(this));

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

			// Update width while keeping fixed height
			rowSize.width = totalWidth;
			setPreferredSize(rowSize);
			setMaximumSize(rowSize);

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
			focusComponents.add(component);
			component.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					// Select this row in the table
					MultiLevelWindTable.this.selectRow(LevelRow.this);
					
					SwingUtilities.invokeLater(LevelRow.this::updateHighlight);
				}

				@Override
				public void focusLost(FocusEvent e) {
					SwingUtilities.invokeLater(LevelRow.this::updateHighlight);
				}
			});
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
			updateHighlight();
		}

		// Update highlighting based on selection and edit mode
		private void updateHighlight() {
			if (selected) {
				// Also add a slight background tint
				setBackground(new Color(240, 240, 255));
			} else {
				// Restore original background
				Color bg = (rows.indexOf(this) % 2 == 0) ? EVEN_ROW_COLOR : ODD_ROW_COLOR;
				setBackground(bg);
			}
			
			revalidate();
			repaint();
		}

		// Install context menu for row deletion
		private void installContextMenu() {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem deleteItem = new JMenuItem("Delete this row", Icons.EDIT_DELETE);
			deleteItem.addActionListener(e -> removeRow(this));
			popup.add(deleteItem);

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
			setBackground(FLASH_COLOR);

			Timer timer = new Timer(FLASH_DURATION_MS, e -> {
				setBackground(originalColor);
				((Timer) e.getSource()).stop();
			});

			timer.setRepeats(false);
			timer.start();
		}
	}
}