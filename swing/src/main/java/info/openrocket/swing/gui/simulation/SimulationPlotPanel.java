package info.openrocket.swing.gui.simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import info.openrocket.core.document.PlotAppearance;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightDataTypeGroup;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.LineStyle;
import info.openrocket.core.util.ORColor;

import info.openrocket.swing.gui.plot.PlotPanel;
import info.openrocket.swing.gui.plot.SimulationPlotConfiguration;
import info.openrocket.swing.gui.plot.SimulationPlotDialog;
import info.openrocket.swing.gui.plot.Util;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;

/**
 * Panel that displays the simulation plot options to the user.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationPlotPanel extends PlotPanel<FlightDataType, FlightDataBranch, FlightDataTypeGroup,
		SimulationPlotConfiguration, SimulationPlotTypeSelector> {
	@Serial
	private static final long serialVersionUID = -2227129713185477998L;

	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	
	/** The "Custom" configuration - not to be used for anything other than the title. */
	private static final SimulationPlotConfiguration CUSTOM_CONFIGURATION;
	
	/** The array of presets for the combo box. */
	private static final SimulationPlotConfiguration[] PRESET_ARRAY;
	
	
	/** The current default configuration, set each time a plot is made. */
	private static SimulationPlotConfiguration DEFAULT_CONFIGURATION =
			SimulationPlotConfiguration.DEFAULT_CONFIGURATIONS[0].resetUnits();
	
	
	private final Simulation simulation;
	private FlightEventTableModel eventTableModel;
	private Map<Integer, java.awt.Color> defaultPlotColors = Collections.emptyMap();
	private static java.awt.Color darkErrorColor;

	static {
		initColors();

		CUSTOM_CONFIGURATION = new SimulationPlotConfiguration(CUSTOM);

		PRESET_ARRAY = Arrays.copyOf(SimulationPlotConfiguration.DEFAULT_CONFIGURATIONS,
				SimulationPlotConfiguration.DEFAULT_CONFIGURATIONS.length + 1);
		PRESET_ARRAY[PRESET_ARRAY.length - 1] = CUSTOM_CONFIGURATION;
	}
	
	private SimulationPlotPanel(final Simulation simulation, FlightDataType[] types,
							   final DescriptionArea simPlotPanelDesc,
							   Component[] extraWidgetsX, JPanel selectorPanel, Component[] extraWidgetsY) {
		super(types, types, CUSTOM_CONFIGURATION, PRESET_ARRAY, DEFAULT_CONFIGURATION, extraWidgetsX, extraWidgetsY);

		this.simulation = simulation;


		//// X axis listeners
		domainTypeSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				FlightDataType type = (FlightDataType) domainTypeSelector.getSelectedItem();
				if (type == null) {
					return;
				}
				if (type == FlightDataType.TYPE_TIME) {
					simPlotPanelDesc.setVisible(false);
					simPlotPanelDesc.setText("");
				}
				else {
					simPlotPanelDesc.setVisible(true);
					simPlotPanelDesc.setText(trans.get("simplotpanel.Desc"));
				}
			}
		});

		//// Y axis
		addFlightEventsSelectorWidgets(selectorPanel);

		updatePlots();
	}

	public static SimulationPlotPanel create(Simulation simulation) {
		// Check the simulation data
		if (simulation.getSimulatedData() == null ||
				simulation.getSimulatedData().getBranchCount() == 0) {
			throw new IllegalArgumentException("Simulation contains no data.");
		}
		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		FlightDataType[] types = branch.getTypes();

		// Create extra widgets for the X axis
		//// The data will be plotted in time order even if the X axis type is not time.
		DescriptionArea simPlotPanelDesc = new DescriptionArea("", 2, -2.0f, false);
		simPlotPanelDesc.setVisible(false);
		simPlotPanelDesc.setForeground(darkErrorColor);
		simPlotPanelDesc.setViewportBorder(BorderFactory.createEmptyBorder());
		Component[] extraWidgetsX = new Component[] {simPlotPanelDesc};

		// Create extra widgets for the Y axis
		//// Flight events:
		JLabel label = new JLabel(trans.get("simplotpanel.lbl.Flightevents"));
		JPanel selectorPanel = new JPanel(new MigLayout("ins 0"));
		Component[] extraWidgetsY = new Component[] {label, selectorPanel};

		return new SimulationPlotPanel(simulation, types, simPlotPanelDesc, extraWidgetsX, selectorPanel, extraWidgetsY);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationPlotPanel::updateColors);
	}

	public static void updateColors() {
		darkErrorColor = UITheme.getColor(UITheme.Keys.DARK_ERROR);
	}

	@Override
	protected void setDefaultConfiguration(SimulationPlotConfiguration newConfiguration) {
		// Strip per-simulation appearance overrides so they don't leak into other simulations
		// that will start from this default.
		newConfiguration.clearPlotAppearances();
		super.setDefaultConfiguration(newConfiguration);
		DEFAULT_CONFIGURATION = newConfiguration;
	}

	private void addFlightEventsSelectorWidgets(JPanel selectorPanel) {
		//// Flight events
		eventTableModel = new FlightEventTableModel();
		JTable table = new JTable(eventTableModel);
		table.setTableHeader(null);
		table.setShowVerticalLines(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);

		TableColumnModel columnModel = table.getColumnModel();
		TableColumn col0 = columnModel.getColumn(0);
		int w = table.getRowHeight() + 2;
		col0.setMinWidth(w);
		col0.setPreferredWidth(w);
		col0.setMaxWidth(w);
		table.addMouseListener(new GUIUtil.BooleanTableClickListener(table));
		JScrollPane scrollPane = new JScrollPane(table);
		Dimension d = table.getPreferredSize();
		scrollPane.setPreferredSize(new Dimension(d.width, 150));
		selectorPanel.add(scrollPane, "width 200lp, grow 1, wrap rel");

		////  All + None buttons
		JButton button = new JButton(trans.get("simplotpanel.but.All"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FlightEvent.Type t : FlightEvent.Type.values()) {
					SimulationPlotConfiguration configuration = getConfiguration();
					if (configuration != null) {
						configuration.setEvent(t, true);
					}
				}
				eventTableModel.fireTableDataChanged();
			}
		});
		selectorPanel.add(button, "split 2, gapleft para, gapright para, growx, sizegroup buttons");

		//// None
		button = new JButton(trans.get("simplotpanel.but.None"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FlightEvent.Type t : FlightEvent.Type.values()) {
					SimulationPlotConfiguration configuration = (SimulationPlotConfiguration) getConfiguration();
					if (configuration != null) {
						configuration.setEvent(t, false);
					}
				}
				eventTableModel.fireTableDataChanged();
			}
		});
		selectorPanel.add(button, "gapleft para, gapright para, growx, sizegroup buttons, wrap");


		//// Style event marker
		JLabel styleEventMarker = new JLabel(trans.get("simplotpanel.MarkerStyle.lbl.MarkerStyle"));
		JRadioButton radioVerticalMarker = new JRadioButton(trans.get("simplotpanel.MarkerStyle.btn.VerticalMarker"));
		JRadioButton radioIcon = new JRadioButton(trans.get("simplotpanel.MarkerStyle.btn.Icon"));
		ButtonGroup bg = new ButtonGroup();
		bg.add(radioVerticalMarker);
		bg.add(radioIcon);

		boolean useIcon = preferences.getBoolean(ApplicationPreferences.MARKER_STYLE_ICON, false);
		if (useIcon) {
			radioIcon.setSelected(true);
		} else {
			radioVerticalMarker.setSelected(true);
		}

		radioIcon.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				preferences.putBoolean(ApplicationPreferences.MARKER_STYLE_ICON, radioIcon.isSelected());
			}
		});

		domainTypeSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateStyleEventWidgets(styleEventMarker, radioVerticalMarker, radioIcon);
			}
		});
		updateStyleEventWidgets(styleEventMarker, radioVerticalMarker, radioIcon);

		selectorPanel.add(styleEventMarker, "split 3, growx");
		selectorPanel.add(radioVerticalMarker);
		selectorPanel.add(radioIcon, "wrap");
	}

	private void updateStyleEventWidgets(JLabel styleEventMarker, JRadioButton radioVerticalMarker, JRadioButton radioIcon) {
		if (modifying > 0)
			return;
		FlightDataType type = (FlightDataType) domainTypeSelector.getSelectedItem();
		boolean isTime = type == FlightDataType.TYPE_TIME;
		styleEventMarker.setEnabled(isTime);
		radioVerticalMarker.setEnabled(isTime);
		radioIcon.setEnabled(isTime);
		styleEventMarker.setToolTipText(isTime ? trans.get("simplotpanel.MarkerStyle.lbl.MarkerStyle.ttip") : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
		radioVerticalMarker.setToolTipText(isTime ? null : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
		radioIcon.setToolTipText(isTime ? null : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
	}

	@Override
	public JDialog doPlot(Window parent) {
		if (configuration.getDataCount() == 0) {
			JOptionPane.showMessageDialog(SimulationPlotPanel.this,
					trans.get("error.noPlotSelected"),
					trans.get("error.noPlotSelected.title"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		setDefaultConfiguration(configuration.clone());
		return SimulationPlotDialog.getPlot(parent, simulation, configuration);
	}

	@Override
	protected void updatePlots() {
		defaultPlotColors = computeDefaultPlotColors();
		super.updatePlots();
		eventTableModel.fireTableDataChanged();
	}

	@Override
	protected SimulationPlotTypeSelector createSelector(int i, FlightDataType type, Unit unit, int axis) {
		PlotAppearance storedAppearance = getEffectiveAppearance(type);
		if (storedAppearance != null) {
			applyStoredAppearance(i, storedAppearance);
		}

		java.awt.Color color = configuration.getPlotDataColor(i);
		if (color == null) {
			color = defaultPlotColors.getOrDefault(i, Util.getPlotColor(i));
		}
		LineStyle lineStyle = configuration.getPlotDataLineStyle(i);
		return new SimulationPlotTypeSelector(i, type, unit, axis, Arrays.asList(typesY), color, lineStyle);
	}

	@Override
	protected void addSelectionListeners(SimulationPlotTypeSelector selector, final int idx) {
		super.addSelectionListeners(selector, idx);

		selector.addColorSelectionListener(evt -> {
			if (modifying > 0) {
				return;
			}
			java.awt.Color color = selector.getSelectedColor();
			configuration.setPlotDataColor(idx, color);
			updateSimulationAppearance(selector.getSelectedType(), color, null, true, false);
		});

		selector.addLineStyleSelectionListener(e -> {
			if (modifying > 0) {
				return;
			}
			LineStyle lineStyle = selector.getSelectedLineStyle();
			configuration.setPlotDataLineStyle(idx, lineStyle);
			updateSimulationAppearance(selector.getSelectedType(), null, lineStyle, false, true);
		});

		selector.addAxisSelectionListener(e -> {
			if (modifying > 0) {
				return;
			}
			defaultPlotColors = computeDefaultPlotColors();
			if (configuration.getPlotDataColor(idx) == null) {
				java.awt.Color color = defaultPlotColors.getOrDefault(idx, Util.getPlotColor(idx));
				modifying++;
				selector.setSelectedColor(color);
				modifying--;
			}
		});

		selector.addTypeSelectionListener(e -> {
			if (modifying > 0) {
				return;
			}
			FlightDataType selectedType = selector.getSelectedType();
			modifying++;
			defaultPlotColors = computeDefaultPlotColors();
			applyTypeAppearance(idx, selectedType, selector);
			modifying--;
		});

		selector.addSetAsDefaultListener(e -> {
			if (modifying > 0) {
				return;
			}
			FlightDataType selectedType = selector.getSelectedType();
			java.awt.Color explicitColor = configuration.getPlotDataColor(idx);
			LineStyle lineStyle = configuration.getPlotDataLineStyle(idx);
			PlotAppearance appearance = new PlotAppearance(
					explicitColor != null ? ORColor.fromAWTColor(explicitColor) : null,
					lineStyle);
			preferences.setDefaultPlotAppearance(selectedType, appearance);
		});

		selector.addResetToFactoryListener(e -> {
			if (modifying > 0) {
				return;
			}
			FlightDataType selectedType = selector.getSelectedType();
			// Clear the per-simulation override so this type returns to factory defaults.
			simulation.setPlotAppearance(selectedType, null);
			configuration.setPlotDataColor(idx, null);
			configuration.setPlotDataLineStyle(idx, LineStyle.SOLID);
			// Update the UI to reflect the reset without triggering change listeners.
			modifying++;
			selector.setSelectedColor(defaultPlotColors.getOrDefault(idx, Util.getPlotColor(idx)));
			selector.setSelectedLineStyle(LineStyle.SOLID);
			modifying--;
		});
	}

	private void applyTypeAppearance(int index, FlightDataType type, SimulationPlotTypeSelector selector) {
		PlotAppearance storedAppearance = getEffectiveAppearance(type);
		java.awt.Color color = null;
		LineStyle lineStyle = LineStyle.SOLID;
		if (storedAppearance != null) {
			if (storedAppearance.getColor() != null) {
				color = storedAppearance.getColor().toAWTColor();
			}
			if (storedAppearance.getLineStyle() != null) {
				lineStyle = storedAppearance.getLineStyle();
			}
		}

		configuration.setPlotDataColor(index, color);
		configuration.setPlotDataLineStyle(index, lineStyle);

		java.awt.Color selectorColor = color != null
				? color
				: defaultPlotColors.getOrDefault(index, Util.getPlotColor(index));
		selector.setSelectedColor(selectorColor);
		selector.setSelectedLineStyle(lineStyle);
	}

	/**
	 * Returns the appearance to use for a given type.
	 * If the simulation has an explicitly customized appearance (different from the factory default),
	 * that is returned. Otherwise the user's application-level default (from preferences) is used,
	 * falling back to {@code null} when neither exists.
	 */
	private PlotAppearance getEffectiveAppearance(FlightDataType type) {
		PlotAppearance stored = simulation != null ? simulation.getPlotAppearance(type) : null;
		if (stored == null || PlotAppearance.FACTORY_DEFAULT.equals(stored)) {
			return preferences.getDefaultPlotAppearance(type);
		}
		return stored;
	}

	private void applyStoredAppearance(int index, PlotAppearance appearance) {
		if (appearance.getColor() != null && configuration.getPlotDataColor(index) == null) {
			configuration.setPlotDataColor(index, appearance.getColor().toAWTColor());
		}
		if (appearance.getLineStyle() != null) {
			configuration.setPlotDataLineStyle(index, appearance.getLineStyle());
		}
	}

	private void updateSimulationAppearance(FlightDataType type, java.awt.Color color, LineStyle lineStyle,
											boolean updateColor, boolean updateLineStyle) {
		if (simulation == null || type == null) {
			return;
		}
		PlotAppearance appearance = simulation.getPlotAppearance(type);
		ORColor storedColor = appearance != null ? appearance.getColor() : null;
		LineStyle storedLineStyle = appearance != null ? appearance.getLineStyle() : null;

		if (updateColor) {
			storedColor = color != null ? ORColor.fromAWTColor(color) : null;
		}
		if (updateLineStyle) {
			storedLineStyle = lineStyle;
		}
		simulation.setPlotAppearance(type, new PlotAppearance(storedColor, storedLineStyle));
	}

	private Map<Integer, java.awt.Color> computeDefaultPlotColors() {
		if (simulation == null || simulation.getSimulatedData() == null) {
			return Collections.emptyMap();
		}

		// Mirror Plot's axis-based palette ordering so default colors match the rendered series.
		SimulationPlotConfiguration filled = configuration.fillAutoAxes(simulation.getSimulatedData().getBranch(0));
		int branchCount = simulation.getSimulatedData().getBranchCount();
		int safeBranchCount = Math.max(branchCount, 1);

		List<List<Integer>> axisData = new ArrayList<>(2);
		axisData.add(new ArrayList<>());
		axisData.add(new ArrayList<>());

		for (int i = 0; i < filled.getDataCount(); i++) {
			int axis = filled.getAxis(i);
			if (axis < 0 || axis >= axisData.size()) {
				axis = 0;
			}
			axisData.get(axis).add(i);
		}

		Map<Integer, java.awt.Color> colors = new HashMap<>();
		int cumulativeSeriesCount = 0;
		for (int axis = 0; axis < axisData.size(); axis++) {
			List<Integer> indices = axisData.get(axis);
			for (int relativeIndex = 0; relativeIndex < indices.size(); relativeIndex++) {
				int dataIdx = indices.get(relativeIndex);
				int colorIndex = cumulativeSeriesCount + relativeIndex;
				colors.put(dataIdx, Util.getPlotColor(colorIndex));
			}
			cumulativeSeriesCount += indices.size() * safeBranchCount;
		}

		return colors;
	}

	private class FlightEventTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1108240805614567627L;
		private final FlightEvent.Type[] eventTypes;
		
		public FlightEventTableModel() {
			EnumSet<FlightEvent.Type> set = EnumSet.noneOf(FlightEvent.Type.class);
			for (int i = 0; i < simulation.getSimulatedData().getBranchCount(); i++) {
				for (FlightEvent e : simulation.getSimulatedData().getBranch(i).getEvents()) {
					set.add(e.getType());
				}
			}
			set.remove(FlightEvent.Type.ALTITUDE);
			int count = set.size();
			
			eventTypes = new FlightEvent.Type[count];
			int pos = 0;
			for (FlightEvent.Type t : FlightEvent.Type.values()) {
				if (set.contains(t)) {
					eventTypes[pos] = t;
					pos++;
				}
			}
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public int getRowCount() {
			return eventTypes.length;
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			return switch (column) {
				case 0 -> Boolean.class;
				case 1 -> String.class;
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			return switch (column) {
				case 0 -> configuration.isEventActive(eventTypes[row]);
				case 1 -> eventTypes[row].toString();
				default -> throw new IndexOutOfBoundsException("column=" + column);
			};
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column != 0 || !(value instanceof Boolean)) {
				throw new IllegalArgumentException("column=" + column + ", value=" + value);
			}

			((SimulationPlotConfiguration) configuration).setEvent(eventTypes[row], (Boolean) value);
			this.fireTableCellUpdated(row, column);
		}
	}
}
