package net.sf.openrocket.gui.plot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.EnumSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.FlightDataBranch.Type;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Icons;
import net.sf.openrocket.util.Pair;

public class PlotPanel extends JPanel {
	
	// TODO: LOW: Should these be somewhere else?
	public static final int AUTO = -1;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	
	public static final String AUTO_NAME = "Auto";
	public static final String LEFT_NAME = "Left";
	public static final String RIGHT_NAME = "Right";
	
	private static final String CUSTOM = "Custom";
	
	/** The "Custom" configuration - not to be used for anything other than the title. */
	private static final PlotConfiguration CUSTOM_CONFIGURATION;
	static {
		CUSTOM_CONFIGURATION = new PlotConfiguration(CUSTOM);
	}
	
	/** The array of presets for the combo box. */
	private static final PlotConfiguration[] PRESET_ARRAY;
	static {
		PRESET_ARRAY = Arrays.copyOf(PlotConfiguration.DEFAULT_CONFIGURATIONS, 
				PlotConfiguration.DEFAULT_CONFIGURATIONS.length + 1);
		PRESET_ARRAY[PRESET_ARRAY.length-1] = CUSTOM_CONFIGURATION;
	}
	
	
	
	/** The current default configuration, set each time a plot is made. */
	private static PlotConfiguration defaultConfiguration =
		PlotConfiguration.DEFAULT_CONFIGURATIONS[0].resetUnits();
	
	
	private final Simulation simulation;
	private final FlightDataBranch.Type[] types;
	private PlotConfiguration configuration;
	

	private JComboBox configurationSelector;
	
	private JComboBox domainTypeSelector;
	private UnitSelector domainUnitSelector;
	
	private JPanel typeSelectorPanel;
	private FlightEventTableModel eventTableModel;
	
	
	private int modifying = 0;
	

	public PlotPanel(final Simulation simulation) {
		super(new MigLayout("fill"));
		
		this.simulation = simulation;
		if (simulation.getSimulatedData() == null  ||
				simulation.getSimulatedData().getBranchCount()==0) {
			throw new IllegalArgumentException("Simulation contains no data.");
		}
		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		types = branch.getTypes();
		
		// TODO: LOW: Revert to custom if data type is not available.
		configuration = defaultConfiguration.clone();
		
		
		////  Configuration selector
		
		// Setup the combo box
		configurationSelector = new JComboBox(PRESET_ARRAY);
		for (PlotConfiguration config: PRESET_ARRAY) {
			if (config.getName().equals(configuration.getName())) {
				configurationSelector.setSelectedItem(config);
			}
		}
		configurationSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				PlotConfiguration conf = (PlotConfiguration)configurationSelector.getSelectedItem();
				if (conf == CUSTOM_CONFIGURATION)
					return;
				modifying++;
				configuration = conf.clone().resetUnits();
				updatePlots();
				modifying--;
			}
		});
		this.add(new JLabel("Preset plot configurations: "), "spanx, split");
		this.add(configurationSelector,"growx, wrap 30lp");

		
		
		//// X axis
		
		
		this.add(new JLabel("X axis type:"), "spanx, split");
		domainTypeSelector = new JComboBox(types);
		domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
		domainTypeSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				FlightDataBranch.Type type = (Type) domainTypeSelector.getSelectedItem();
				configuration.setDomainAxisType(type);
				domainUnitSelector.setUnitGroup(type.getUnitGroup());
				domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
				setToCustom();
			}
		});
		this.add(domainTypeSelector, "gapright para");

		
		this.add(new JLabel("Unit:"));
		domainUnitSelector = new UnitSelector(configuration.getDomainAxisType().getUnitGroup());
		domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
		domainUnitSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (modifying > 0)
					return;
				configuration.setDomainAxisUnit(domainUnitSelector.getSelectedUnit());
			}
		});
		this.add(domainUnitSelector, "width 40lp, gapright para");
		
		
		ResizeLabel desc = new ResizeLabel("<html><p>The data will be plotted in time order " +
				"even if the X axis type is not time.", -2);
		this.add(desc, "width :0px:, growx, wrap para");
		
		
		
		//// Y axis selector panel
		
		this.add(new JLabel("Y axis types:"));
		
		this.add(new JLabel("Flight events:"), "wrap rel");
		
		typeSelectorPanel = new JPanel(new MigLayout("gapy rel"));
		JScrollPane scroll = new JScrollPane(typeSelectorPanel);
		this.add(scroll, "spany 2, height 10px, grow 100, gapright para");
		
		
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
		this.add(new JScrollPane(table), "height 1px, width 200lp, grow 1, wrap rel");
		
		
		////  All + None buttons
		JButton button = new JButton("All");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FlightEvent.Type t: FlightEvent.Type.values())
					configuration.setEvent(t, true);
				eventTableModel.fireTableDataChanged();
			}
		});
		this.add(button, "split 2, gapleft para, gapright para, growx, sizegroup buttons");
		
		button = new JButton("None");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FlightEvent.Type t: FlightEvent.Type.values())
					configuration.setEvent(t, false);
				eventTableModel.fireTableDataChanged();
			}
		});
		this.add(button, "gapleft para, gapright para, growx, sizegroup buttons, wrap para");
		
		
		
		
		button = new JButton("New Y axis plot type");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configuration.getTypeCount() >= 15) {
					JOptionPane.showMessageDialog(PlotPanel.this, 
							"A maximum of 15 plots is allowed.", "Cannot add plot", 
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Select new type smartly
				FlightDataBranch.Type type = null;
				for (FlightDataBranch.Type t: 
					simulation.getSimulatedData().getBranch(0).getTypes()) {
					
					boolean used = false;
					if (configuration.getDomainAxisType().equals(t)) {
						used = true;
					} else {
						for (int i=0; i < configuration.getTypeCount(); i++) {
							if (configuration.getType(i).equals(t)) {
								used = true;
								break;
							}
						}
					}
					
					if (!used) {
						type = t;
						break;
					}
				}
				if (type == null) {
					type = simulation.getSimulatedData().getBranch(0).getTypes()[0];
				}
				
				// Add new type
				configuration.addPlotDataType(type);
				setToCustom();
				updatePlots();
			}
		});
		this.add(button, "spanx, split");
		
		
		this.add(new JPanel(), "growx");
		
		button = new JButton("Plot flight");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defaultConfiguration = configuration.clone();
				PlotDialog.showPlot(SwingUtilities.getWindowAncestor(PlotPanel.this), 
						simulation, configuration);
			}
		});
		this.add(button, "right");

		
		updatePlots();
	}
	
	
	private void setToCustom() {
		modifying++;
		configuration.setName(CUSTOM);
		configurationSelector.setSelectedItem(CUSTOM_CONFIGURATION);
		modifying--;
	}
	
	
	private void updatePlots() {
		domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
		domainUnitSelector.setUnitGroup(configuration.getDomainAxisType().getUnitGroup());
		domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
		
		typeSelectorPanel.removeAll();
		for (int i=0; i < configuration.getTypeCount(); i++) {
			FlightDataBranch.Type type = configuration.getType(i);
			Unit unit = configuration.getUnit(i);
			int axis = configuration.getAxis(i);
			
			typeSelectorPanel.add(new PlotTypeSelector(i, type, unit, axis), "wrap");
		}
		
		typeSelectorPanel.repaint();
		
		eventTableModel.fireTableDataChanged();
	}
	
	
	
	
	/**
	 * A JPanel which configures a single plot of a PlotConfiguration.
	 */
	private class PlotTypeSelector extends JPanel {
		private final String[] POSITIONS = { AUTO_NAME, LEFT_NAME, RIGHT_NAME };
		
		private final int index;
		private JComboBox typeSelector;
		private UnitSelector unitSelector;
		private JComboBox axisSelector;
		
		
		public PlotTypeSelector(int index, FlightDataBranch.Type type) {
			this (index, type, null, -1);
		}
		
		public PlotTypeSelector(int plotIndex, FlightDataBranch.Type type, Unit unit, int position) {
			super(new MigLayout("ins 0"));
			
			this.index = plotIndex;
			
			typeSelector = new JComboBox(types);
			typeSelector.setSelectedItem(type);
			typeSelector.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (modifying > 0)
						return;
					FlightDataBranch.Type type = (Type) typeSelector.getSelectedItem();
					configuration.setPlotDataType(index, type);
					unitSelector.setUnitGroup(type.getUnitGroup());
					unitSelector.setSelectedUnit(configuration.getUnit(index));
					setToCustom();
				}
			});
			this.add(typeSelector, "gapright para");
			
			this.add(new JLabel("Unit:"));
			unitSelector = new UnitSelector(type.getUnitGroup());
			if (unit != null)
				unitSelector.setSelectedUnit(unit);
			unitSelector.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (modifying > 0)
						return;
					Unit unit = (Unit) unitSelector.getSelectedUnit();
					configuration.setPlotDataUnit(index, unit);
				}
			});
			this.add(unitSelector, "width 40lp, gapright para");
			
			this.add(new JLabel("Axis:"));
			axisSelector = new JComboBox(POSITIONS);
			if (position == LEFT)
				axisSelector.setSelectedIndex(1);
			else if (position == RIGHT)
				axisSelector.setSelectedIndex(2);
			else
				axisSelector.setSelectedIndex(0);
			axisSelector.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (modifying > 0)
						return;
					int axis = axisSelector.getSelectedIndex() - 1;
					configuration.setPlotDataAxis(index, axis);
				}
			});
			this.add(axisSelector);
			
			
			JButton button = new JButton(Icons.DELETE);
			button.setToolTipText("Remove this plot");
			button.setBorderPainted(false);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					configuration.removePlotDataType(index);
					setToCustom();
					updatePlots();
				}
			});
			this.add(button, "gapright 0");
		}
	}
	
	
	
	private class FlightEventTableModel extends AbstractTableModel {
		private final FlightEvent.Type[] eventTypes;
		
		public FlightEventTableModel() {
			EnumSet<FlightEvent.Type> set = EnumSet.noneOf(FlightEvent.Type.class);
			for (int i=0; i < simulation.getSimulatedData().getBranchCount(); i++) {
				for (Pair<Double,FlightEvent> e:
					simulation.getSimulatedData().getBranch(i).getEvents()) {
					set.add(e.getV().getType());
				}
			}
			set.remove(FlightEvent.Type.ALTITUDE);
			int count = set.size();
			
			eventTypes = new FlightEvent.Type[count];
			int pos = 0;
			for (FlightEvent.Type t: FlightEvent.Type.values()) {
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
			switch (column) {
			case 0:
				return Boolean.class;
				
			case 1:
				return String.class;
				
			default:
				throw new IndexOutOfBoundsException("column="+column);
			}
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return new Boolean(configuration.isEventActive(eventTypes[row]));
				
			case 1:
				return eventTypes[row].toString();
				
			default:
				throw new IndexOutOfBoundsException("column="+column);
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column != 0 || !(value instanceof Boolean)) {
				throw new IllegalArgumentException("column="+column+", value="+value);
			}
			
			configuration.setEvent(eventTypes[row], (Boolean)value);
			this.fireTableCellUpdated(row, column);
		}
	}
}
