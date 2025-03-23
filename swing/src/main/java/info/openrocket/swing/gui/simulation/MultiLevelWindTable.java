package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.atmosphere.ExtendedISAModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MultiLevelWindTable extends JPanel implements ChangeSource {
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private static final double ALTITUDE_INCREASE = 100;		// Default altitude increase when adding a new row
	private static final Font HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final int FLASH_DURATION_MS = 800;
	private static final int CELL_PADDING = 8; // Padding inside cells

	private static final ColumnDefinition[] COLUMNS = {
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.AltitudeMSL"), trans.get("MultiLevelWindTable.col.AltitudeMSL.ttip"),
					100, UnitGroup.UNITS_DISTANCE),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Speed"), 100, UnitGroup.UNITS_WINDSPEED),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Direction"), 90, UnitGroup.UNITS_ANGLE),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.StandardDeviation"), 100, UnitGroup.UNITS_WINDSPEED),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Turbulence"), 90, UnitGroup.UNITS_RELATIVE),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Intensity"), 85, null),
			new ColumnDefinition(trans.get("MultiLevelWindTable.col.Delete"), 60, null)
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
	private JLabel altitudeHeaderLabel;

	private LevelRow selectedRow = null;
	private final List<RowSelectionListener> selectionListeners = new ArrayList<>();
	private final List<StateChangeListener> listeners = new ArrayList<>();
	
	// Shared unit models
	private final DoubleModel unitAltitudeModel;
	private final DoubleModel unitSpeedModel;
	private final DoubleModel unitDirectionModel;
	private final DoubleModel unitStdDeviationModel;
	private final DoubleModel unitTurbulenceModel;
	
	// Unit selectors in header
	private UnitSelector altitudeUnitSelector;
	private UnitSelector speedUnitSelector;
	private UnitSelector directionUnitSelector;
	private UnitSelector stdDeviationUnitSelector;
	private UnitSelector turbulenceUnitSelector;

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
		
		// Initialize shared unit models with dummy values (will be updated when units change)
		// We use 1.0 as a placeholder value since we're only interested in the unit, not the value
		this.unitAltitudeModel = new DoubleModel(1.0, UnitGroup.UNITS_DISTANCE);
		this.unitSpeedModel = new DoubleModel(1.0, UnitGroup.UNITS_WINDSPEED);
		this.unitDirectionModel = new DoubleModel(1.0, UnitGroup.UNITS_ANGLE);
		this.unitStdDeviationModel = new DoubleModel(1.0, UnitGroup.UNITS_WINDSPEED);
		this.unitTurbulenceModel = new DoubleModel(1.0, UnitGroup.UNITS_RELATIVE);
		
		// Build header panel
		headerPanel = createHeaderPanel();

		// Build rows panel
		rowsPanel = new JPanel();
		rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

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

		updateAltitudeHeader(windModel.getAltitudeReference());
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
			ColumnDefinition column = COLUMNS[i];
			
			// Create header cell with label and optional unit selector
			JPanel cell;
			if (column.unitGroup() != null) {
				// Create a panel with vertical layout for label and unit selector
				JPanel headerContent = new JPanel();
				headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.Y_AXIS));
				headerContent.setOpaque(false);
				
				// Add label at the top
				JLabel label = new JLabel(column.header(), SwingConstants.CENTER);
				String tooltip = column.headerTooltip();
				if (tooltip != null) {
					label.setToolTipText(tooltip);
				}
				label.setFont(HEADER_FONT);
				label.setAlignmentX(CENTER_ALIGNMENT);
				headerContent.add(label);

				if (i == 0) {
					altitudeHeaderLabel = label;
				}
				
				// Add small gap
				headerContent.add(Box.createVerticalStrut(3));
				
				// Add unit selector at the bottom
				UnitSelector unitSelector = createUnitSelector(i, column.unitGroup());
				unitSelector.setAlignmentX(CENTER_ALIGNMENT);
				headerContent.add(unitSelector);
				
				// Create the cell with the header content
				cell = createFixedCell(headerContent, column.width());
			} else {
				// For columns without units, just add the label
				JLabel label = new JLabel(column.header(), SwingConstants.CENTER);
				label.setFont(HEADER_FONT);
				cell = createFixedCell(label, column.width());
			}
			
			cell.setBackground(tableHeaderBg);
			panel.add(cell);

			if (i < COLUMNS.length - 1) {
				panel.add(createVerticalSeparator());
			}
		}

		return panel;
	}
	
	/**
	 * Creates a unit selector for a specific column and registers it
	 */
	private UnitSelector createUnitSelector(int columnIndex, UnitGroup unitGroup) {
		UnitSelector selector;
		
		switch (columnIndex) {
			case 0: // Altitude
				selector = new UnitSelector(unitAltitudeModel);
				altitudeUnitSelector = selector;
				selector.addItemListener(e -> updateAltitudeUnits(selector.getSelectedUnit()));
				break;
			case 1: // Speed
				selector = new UnitSelector(unitSpeedModel);
				speedUnitSelector = selector;
				selector.addItemListener(e -> updateSpeedUnits(selector.getSelectedUnit()));
				break;
			case 2: // Direction
				selector = new UnitSelector(unitDirectionModel);
				directionUnitSelector = selector;
				selector.addItemListener(e -> updateDirectionUnits(selector.getSelectedUnit()));
				break;
			case 3: // Standard Deviation
				selector = new UnitSelector(unitStdDeviationModel);
				stdDeviationUnitSelector = selector;
				selector.addItemListener(e -> updateStdDeviationUnits(selector.getSelectedUnit()));
				break;
			case 4: // Turbulence
				selector = new UnitSelector(unitTurbulenceModel);
				turbulenceUnitSelector = selector;
				selector.addItemListener(e -> updateTurbulenceUnits(selector.getSelectedUnit()));
				break;
			default:
				selector = new UnitSelector(unitGroup);
		}
		
		return selector;
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
			resortRows(originalOrder);
			highlightChangedRows(thisIdx, thisIdx);

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
		int thisIdx = rows.indexOf(row);
		rows.remove(row);
		row.invalidateModels();
		windModel.removeWindLevel(row.getLevel().getAltitude());
		resortRows(originalOrder);
		selectRow(null);
		if (thisIdx < rows.size()) {
			highlightChangedRows(thisIdx, thisIdx);
		}

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
	 * @return true if the order changed, false if it stayed the same
	 */
	private boolean resortRows(List<LevelRow> originalOrder) {
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

			return false;
		}

		rowsPanel.removeAll();

		// Add all rows with alternating background colors
		for (int i = 0; i < rows.size(); i++) {
			LevelRow row = rows.get(i);
			Color bg = (i % 2 == 0) ? evenRowColor : oddRowColor;
			row.setBaseBackground(bg);
			rowsPanel.add(row);
		}

		rowsPanel.revalidate();
		rowsPanel.repaint();
		fireChangeEvent();

		return true;
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

	/**
	 * Import wind levels from a CSV file with the specified settings.
	 *
	 * @param file The CSV file to import
	 * @param separator The field separator used in the CSV file
	 * @param altitudeColumn The name or index of the altitude column
	 * @param speedColumn The name or index of the speed column
	 * @param directionColumn The name or index of the direction column
	 * @param stdDeviationColumn The name or index of the standard deviation column (can be empty)
	 * @param altitudeUnit The unit used for altitude values in the CSV
	 * @param speedUnit The unit used for speed values in the CSV
	 * @param directionUnit The unit used for direction values in the CSV
	 * @param stdDeviationUnit The unit used for standard deviation values in the CSV
	 * @param hasHeaders Whether the CSV file has headers
	 */
	public void importLevels(File file, String separator,
							 String altitudeColumn, String speedColumn,
							 String directionColumn, String stdDeviationColumn,
							 Unit altitudeUnit, Unit speedUnit,
							 Unit directionUnit, Unit stdDeviationUnit,
							 boolean hasHeaders) {
		try {
			windModel.importLevelsFromCSV(file, separator,
					altitudeColumn, speedColumn,
					directionColumn, stdDeviationColumn,
					altitudeUnit, speedUnit,
					directionUnit, stdDeviationUnit,
					hasHeaders);
			syncRowsFromModel();
		} catch (Exception e) {
			// Ensure we reset if any error occurs
			resetLevels();
			throw e;
		}
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
		}
		changedRow = null;
	}

	public JPanel getRowsPanel() {
		return rowsPanel;
	}

	public JPanel getHeaderPanel() {
		return headerPanel;
	}

	public void updateAltitudeHeader(WindModel.AltitudeReference reference) {
		String key;
		switch (reference) {
			case MSL -> key = "AltitudeMSL";
			case AGL -> key = "AltitudeAGL";
			default -> key = "Altitude";
		}
		altitudeHeaderLabel.setText(trans.get("MultiLevelWindTable.col." + key));
		altitudeHeaderLabel.setToolTipText(trans.get("MultiLevelWindTable.col." + key + ".ttip"));
	}

	public void addSelectionListener(RowSelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(RowSelectionListener listener) {
		selectionListeners.remove(listener);
	}
	
	/**
	 * Removes all selection listeners from the table.
	 * This should be called when the dialog is being disposed to prevent memory leaks.
	 */
	public void removeAllSelectionListeners() {
		selectionListeners.clear();
	}
	
	/**
	 * Invalidates all DoubleModels in the table rows.
	 * This should be called when the dialog is being disposed to prevent memory leaks.
	 */
	public void invalidateModels() {
		for (LevelRow row : rows) {
			row.invalidateModels();
		}
		
		// Also invalidate the shared unit models
		unitAltitudeModel.invalidateMe();
		unitSpeedModel.invalidateMe();
		unitDirectionModel.invalidateMe();
		unitStdDeviationModel.invalidateMe();
		unitTurbulenceModel.invalidateMe();
	}
	
	/**
	 * Update the altitude unit for all rows
	 */
	private void updateAltitudeUnits(Unit unit) {
		// Create a copy of the rows to avoid ConcurrentModificationException
		List<LevelRow> rowsCopy = new ArrayList<>(rows);
		for (LevelRow row : rowsCopy) {
			row.setAltitudeUnit(unit);
		}
		
		// Notify listeners for plot update
		fireChangeEvent(unit);
	}
	
	/**
	 * Update the speed unit for all rows
	 */
	private void updateSpeedUnits(Unit unit) {
		// Create a copy of the rows to avoid ConcurrentModificationException
		List<LevelRow> rowsCopy = new ArrayList<>(rows);
		for (LevelRow row : rowsCopy) {
			row.setSpeedUnit(unit);
		}
		
		// Notify listeners for plot update
		fireChangeEvent(unit);
	}
	
	/**
	 * Update the direction unit for all rows
	 */
	private void updateDirectionUnits(Unit unit) {
		// Create a copy of the rows to avoid ConcurrentModificationException
		List<LevelRow> rowsCopy = new ArrayList<>(rows);
		for (LevelRow row : rowsCopy) {
			row.setDirectionUnit(unit);
		}
		
		// Notify listeners for plot update
		fireChangeEvent(unit);
	}
	
	/**
	 * Update the standard deviation unit for all rows
	 */
	private void updateStdDeviationUnits(Unit unit) {
		// Create a copy of the rows to avoid ConcurrentModificationException
		List<LevelRow> rowsCopy = new ArrayList<>(rows);
		for (LevelRow row : rowsCopy) {
			row.setStdDeviationUnit(unit);
		}
		
		// Notify listeners for plot update
		fireChangeEvent(unit);
	}
	
	/**
	 * Update the turbulence unit for all rows
	 */
	private void updateTurbulenceUnits(Unit unit) {
		// Create a copy of the rows to avoid ConcurrentModificationException
		List<LevelRow> rowsCopy = new ArrayList<>(rows);
		for (LevelRow row : rowsCopy) {
			row.setTurbulenceUnit(unit);
		}
		
		// Notify listeners for plot update
		fireChangeEvent(unit);
	}
	
	/**
	 * Get the selected altitude unit
	 */
	public Unit getAltitudeUnit() {
		return altitudeUnitSelector.getSelectedUnit();
	}

	/**
	 * Set the selected altitude unit
	 * @param unit the unit to set
	 */
	public void setAltitudeUnit(Unit unit) {
		altitudeUnitSelector.setSelectedUnit(unit);
	}
	
	/**
	 * Get the selected speed unit
	 */
	public Unit getSpeedUnit() {
		return speedUnitSelector.getSelectedUnit();
	}

	/**
	 * Set the selected speed unit
	 * @param unit the unit to set
	 */
	public void setSpeedUnit(Unit unit) {
		speedUnitSelector.setSelectedUnit(unit);
	}

	/**
	 * Get the selected direction unit
	 */
	public Unit getDirectionUnit() {
		return directionUnitSelector.getSelectedUnit();
	}

	/**
	 * Set the selected direction unit
	 * @param unit the unit to set
	 */
	public void setDirectionUnit(Unit unit) {
		directionUnitSelector.setSelectedUnit(unit);
	}

	/**
	 * Get the selected standard deviation unit
	 */
	public Unit getStdDeviationUnit() {
		return stdDeviationUnitSelector.getSelectedUnit();
	}

	/**
	 * Set the selected standard deviation unit
	 * @param unit the unit to set
	 */
	public void setStdDeviationUnit(Unit unit) {
		stdDeviationUnitSelector.setSelectedUnit(unit);
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeEvent(Object source) {
		EventObject event = new EventObject(source);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}

	public void fireChangeEvent() {
		fireChangeEvent(this);
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
		private final JButton deleteButton;
		private JMenuItem deleteItem;

		private boolean selected = false;
		
		public LevelRow(LevelWindModel level) {
			this.level = level;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			// Create DoubleModels bound to the level
			dmAltitude = new DoubleModel(level, "Altitude", UnitGroup.UNITS_DISTANCE, -100, ExtendedISAModel.getMaximumAllowedAltitude());
			dmSpeed = new DoubleModel(level, "Speed", UnitGroup.UNITS_WINDSPEED, 0, 10.0);
			dmDirection = new DoubleModel(level, "Direction", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
			dmStdDeviation = new DoubleModel(level, "StandardDeviation", UnitGroup.UNITS_WINDSPEED, 0, dmSpeed);
			dmTurbulence = new DoubleModel(level, "TurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0, 1);
			
			// Initialize with current shared units if available
			if (altitudeUnitSelector != null) {
				dmAltitude.setCurrentUnit(altitudeUnitSelector.getSelectedUnit());
			}
			if (speedUnitSelector != null) {
				dmSpeed.setCurrentUnit(speedUnitSelector.getSelectedUnit());
			}
			if (directionUnitSelector != null) {
				dmDirection.setCurrentUnit(directionUnitSelector.getSelectedUnit());
			}
			if (stdDeviationUnitSelector != null) {
				dmStdDeviation.setCurrentUnit(stdDeviationUnitSelector.getSelectedUnit());
			}
			if (turbulenceUnitSelector != null) {
				dmTurbulence.setCurrentUnit(turbulenceUnitSelector.getSelectedUnit());
			}

			// Add property change listener for altitude
			dmAltitude.addChangeListener(e -> {
				changedRow = this;
				List<LevelRow> originalOrder = new ArrayList<>(rows);
				boolean changed = resortRows(originalOrder);
				if (changed) {
					int thisIdx = rows.indexOf(this);
					int startIdx = Math.max(0, thisIdx - 1);
					int endIdx = Math.min(rows.size() - 1, thisIdx + 1);
					SwingUtilities.invokeLater(() -> {
						highlightChangedRows(startIdx, endIdx);
					});
				}
			});

			// Add a single state change listener for efficiency
			StateChangeListener stateListener = e -> MultiLevelWindTable.this.fireChangeEvent();
			dmAltitude.addChangeListener(stateListener);
			dmSpeed.addChangeListener(stateListener);
			dmSpeed.addChangeListener(dmStdDeviation);
			dmDirection.addChangeListener(stateListener);
			dmStdDeviation.addChangeListener(stateListener);
			dmStdDeviation.addChangeListener(dmTurbulence);
			dmTurbulence.addChangeListener(stateListener);
			dmTurbulence.addChangeListener(dmStdDeviation);

			// Create UI components for each column
			JPanel altitudeGroup = createSpinnerOnly(dmAltitude, COLUMNS[0].width);
			JPanel speedGroup = createSpinnerOnly(dmSpeed, COLUMNS[1].width);
			JPanel directionGroup = createSpinnerOnly(dmDirection, COLUMNS[2].width);
			JPanel stdDeviationGroup = createSpinnerOnly(dmStdDeviation, COLUMNS[3].width);
			JPanel turbulenceGroup = createSpinnerOnly(dmTurbulence, COLUMNS[4].width);

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

		// Helper method to create spinner without unit selector
		private JPanel createSpinnerOnly(DoubleModel model, int width) {
			JSpinner spinner = new JSpinner(model.getSpinnerModel());
			spinner.setEditor(new SpinnerEditor(spinner));

			// Get editor component for focus tracking
			Component editorComponent = getEditorComponent(spinner);
			addToFocusTracking(editorComponent);

			// Create a centered panel for the spinner
			JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(false);
			panel.add(spinner, BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createEmptyBorder(0, CELL_PADDING, 0, CELL_PADDING));
			
			// Fixed width
			Dimension size = new Dimension(width, spinner.getPreferredSize().height);
			panel.setPreferredSize(size);
			panel.setMinimumSize(size);
			panel.setMaximumSize(size);
			
			return panel;
		}
		
		/**
		 * Sets the altitude unit for this row
		 */
		public void setAltitudeUnit(Unit unit) {
			dmAltitude.setCurrentUnit(unit);
		}
		
		/**
		 * Sets the speed unit for this row
		 */
		public void setSpeedUnit(Unit unit) {
			dmSpeed.setCurrentUnit(unit);
		}
		
		/**
		 * Sets the direction unit for this row
		 */
		public void setDirectionUnit(Unit unit) {
			dmDirection.setCurrentUnit(unit);
		}
		
		/**
		 * Sets the standard deviation unit for this row
		 */
		public void setStdDeviationUnit(Unit unit) {
			dmStdDeviation.setCurrentUnit(unit);
		}
		
		/**
		 * Sets the turbulence unit for this row
		 */
		public void setTurbulenceUnit(Unit unit) {
			dmTurbulence.setCurrentUnit(unit);
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
			final LevelRow thisRow = this;

			// Force a complete repaint before changing the color
			SwingUtilities.invokeLater(() -> {
				// Set the flash color and ensure it's visible
				setBackground(flashColor);
				setOpaque(true);

				// Schedule restoration of the correct background color after delay
				Timer timer = new Timer(FLASH_DURATION_MS, e -> {
					// Calculate the correct background color based on current position
					Color bg = (rows.indexOf(thisRow) % 2 == 0) ? evenRowColor : oddRowColor;
					setBackground(bg);
					((Timer) e.getSource()).stop();
				});

				timer.setRepeats(false);
				timer.start();
			});
		}
		
		/**
		 * Invalidates all DoubleModels in this row.
		 * This should be called when the dialog is being disposed to prevent memory leaks.
		 */
		public void invalidateModels() {
			dmAltitude.invalidateMe();
			dmSpeed.invalidateMe();
			dmDirection.invalidateMe();
			dmStdDeviation.invalidateMe();
			dmTurbulence.invalidateMe();
		}
	}

	private static class ColumnDefinition {
		private final String header;
		private final String headerTooltip;
		private final int width;
		private final UnitGroup unitGroup;

		ColumnDefinition(String header, String headerTooltip, int width, UnitGroup unitGroup) {
			this.header = header;
			this.headerTooltip = headerTooltip;
			this.width = width;
			this.unitGroup = unitGroup;
		}

		ColumnDefinition(String header, int width, UnitGroup unitGroup) {
			this(header, null, width, unitGroup);
		}

		String header() {
			return header;
		}

		String headerTooltip() {
			return headerTooltip;
		}

		int width() {
			return width;
		}

		UnitGroup unitGroup() {
			return unitGroup;
		}
	}
}