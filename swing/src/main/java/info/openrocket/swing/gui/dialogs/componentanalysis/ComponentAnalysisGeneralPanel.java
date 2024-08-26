package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.componentanalysis.CAParameters;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.CMAnalysisEntry;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.adaptors.Column;
import info.openrocket.swing.gui.adaptors.ColumnTable;
import info.openrocket.swing.gui.adaptors.ColumnTableModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.ConfigurationComboBox;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.components.StageSelector;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static info.openrocket.core.unit.Unit.NOUNIT;
import static info.openrocket.core.util.Chars.ALPHA;

public class ComponentAnalysisGeneralPanel extends JPanel implements StateChangeListener {
	private static final Logger log = LoggerFactory.getLogger(ComponentAnalysisDialog.class);
	private static final Translator trans = Application.getTranslator();


	private final FlightConditions conditions;
	private final Rocket rocket;
	private final DoubleModel theta, aoa, mach, roll;
	private final JToggleButton worstToggle;
	private boolean fakeChange = false;
	private final AerodynamicCalculator aerodynamicCalculator;
	private final CAParameters parameters;

	private final ColumnTableModel longitudeStabilityTableModel;
	private final ColumnTableModel dragTableModel;
	private final ColumnTableModel rollTableModel;

	private final JList<Object> warningList;


	private final List<LongitudinalStabilityRow> stabData = new ArrayList<>();
	private final List<AerodynamicForces> dragData = new ArrayList<>();
	private final List<AerodynamicForces> rollData = new ArrayList<>();


	public ComponentAnalysisGeneralPanel(Window parent, final RocketPanel rocketPanel) {
		super(new MigLayout("fill", "[120lp][70lp][50lp][]"));

		this.rocket = rocketPanel.getDocument().getRocket();
		this.aerodynamicCalculator = rocketPanel.getAerodynamicCalculator().newInstance();

		this.conditions = new FlightConditions(rocket.getSelectedConfiguration());

		// Create CAParameters
		this.parameters = new CAParameters(rocket, rocketPanel.getFigure().getRotation());
		this.parameters.addListener(rocketPanel);

		this.aoa = new DoubleModel(parameters, "AOA", UnitGroup.UNITS_ANGLE, 0, Math.PI);
		this.mach = new DoubleModel(parameters, "Mach", UnitGroup.UNITS_COEFFICIENT, 0);
		this.theta = new DoubleModel(parameters, "Theta", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
		this.roll = new DoubleModel(parameters, "RollRate", UnitGroup.UNITS_ROLL);

		JTable table;

		//// Wind direction:
		this.add(new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.winddir")));
		EditableSpinner spinner = new EditableSpinner(theta.getSpinnerModel());
		this.add(spinner, "growx");
		final UnitSelector unitSelectorTheta = new UnitSelector(theta);
		unitSelectorTheta.addItemListener(e -> setParametersThetaUnit(unitSelectorTheta));
		setParametersThetaUnit(unitSelectorTheta);
		this.add(unitSelectorTheta);
		BasicSlider slider = new BasicSlider(theta.getSliderModel(0, 2 * Math.PI));
		this.add(slider, "growx, split 2");
		//// Worst button
		this.worstToggle = new JToggleButton(trans.get("ComponentAnalysisGeneralTab.ToggleBut.worst"));
		this.worstToggle.setSelected(true);
		this.worstToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stateChanged(null);
			}
		});
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!fakeChange)
					worstToggle.setSelected(false);
			}
		});
		this.add(worstToggle);


		this.warningList = new JList<>();
		JScrollPane scrollPane = new JScrollPane(warningList);
		////Warnings:
		scrollPane.setBorder(BorderFactory.createTitledBorder(trans.get("ComponentAnalysisGeneralTab.TitledBorder.warnings")));
		this.add(scrollPane, "gap paragraph, spany 4, w 300lp, grow, height :100lp:, wrap");

		////Angle of attack:
		this.add(new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.angleofattack")));
		this.add(new EditableSpinner(aoa.getSpinnerModel()), "growx");
		final UnitSelector unitSelectorAOA = new UnitSelector(aoa);
		unitSelectorAOA.addItemListener(e -> setParametersAOAUnit(unitSelectorAOA));
		setParametersAOAUnit(unitSelectorAOA);
		this.add(unitSelectorAOA);
		this.add(new BasicSlider(aoa.getSliderModel(0, Math.PI)), "growx, wrap");

		//// Mach number:
		this.add(new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.machnumber")));
		this.add(new EditableSpinner(mach.getSpinnerModel()));
		final UnitSelector unitSelectorMach = new UnitSelector(mach);
		unitSelectorMach.addItemListener(e -> setParametersMachUnit(unitSelectorMach));
		setParametersMachUnit(unitSelectorMach);
		this.add(unitSelectorMach);
		this.add(new BasicSlider(mach.getSliderModel(0, 3)), "growx, wrap");

		//// Roll rate:
		this.add(new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.rollrate")));
		this.add(new EditableSpinner(roll.getSpinnerModel()), "growx");
		final UnitSelector unitSelectorRoll = new UnitSelector(roll);
		unitSelectorRoll.addItemListener(e -> setParametersRollUnit(unitSelectorRoll));
		setParametersRollUnit(unitSelectorRoll);
		this.add(unitSelectorRoll);
		this.add(new BasicSlider(roll.getSliderModel(-20 * 2 * Math.PI, 20 * 2 * Math.PI)),
				"growx, wrap");

		// Stage and motor selection:
		//// Active stages:
		StageSelector stageSelector = new StageSelector(rocket);
		this.rocket.addChangeListener(stageSelector);
		this.add(new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.activestages")), "spanx, split, gapafter rel");
		this.add(stageSelector, "gapafter paragraph");

		//// Motor configuration:
		JLabel label = new JLabel(trans.get("ComponentAnalysisGeneralTab.lbl.motorconf"));
		label.setHorizontalAlignment(JLabel.RIGHT);
		this.add(label, "growx, right");

		final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rocket);
		this.add( configComboBox, "wrap");


		// Tabbed pane

		JTabbedPane tabbedPane = new JTabbedPane();
		this.add(tabbedPane, "spanx, growx, growy, pushy");


		// Create the Longitudinal Stability (CM vs CP) data table
		this.longitudeStabilityTableModel = new ColumnTableModel(

				//// Component
				new Column(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.Component")) {
					@Override
					public Object getValueAt(int row) {
						Object c = stabData.get(row).name;

						return c.toString();
					}

					@Override
					public int getDefaultWidth() {
						return 200;
					}
				},
				// would be per-instance mass
				new Column(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.EachMass") + " (" + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).eachMass);
					}
				},
				new Column(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.AllMass") + " (" + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).cm.weight);
					}
				},
				new Column(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.CG") + " (" + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).cm.x);
					}
				},
				new Column(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.CP") + " (" + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).cpx);
					}
				},
				new Column("<html>C<sub>N<sub>" + ALPHA + "</sub></sub>") {
					@Override
					public Object getValueAt(int row) {
						return NOUNIT.toUnit(stabData.get(row).cna);
					}
				}

		) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public int getRowCount() {
				return stabData.size();
			}
		};

		table = new ColumnTable(longitudeStabilityTableModel);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.longitudeStabilityTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new StabilityCellRenderer());

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Stability and Stability information
		tabbedPane.addTab(trans.get("ComponentAnalysisGeneralTab.TabStability"),
				null, scrollpane, trans.get("ComponentAnalysisGeneralTab.TabStability.ttip"));



		// Create the drag data table
		this.dragTableModel = new ColumnTableModel(
				//// Component
				new Column(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.Component")) {
					@Override
					public Object getValueAt(int row) {
						RocketComponent c = dragData.get(row).getComponent();
						if (c instanceof Rocket) {
							return trans.get("ComponentAnalysisGeneralTab.TOTAL");
						}
						return c.toString();
					}

					@Override
					public int getDefaultWidth() {
						return 200;
					}
				},
				//// <html>Pressure C<sub>D</sub>
				new Column(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.Pressure")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getPressureCD();
					}
				},
				//// <html>Base C<sub>D</sub>
				new Column(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.Base")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getBaseCD();
					}
				},
				//// <html>Friction C<sub>D</sub>
				new Column(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.friction")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getFrictionCD();
					}
				},
				//// <html>Total C<sub>D</sub>
				new Column(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.total")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getCD();
					}
				}
		) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int getRowCount() {
				return dragData.size();
			}
		};


		table = new JTable(dragTableModel);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.dragTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new DragCellRenderer());

		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Drag characteristics and Drag characteristics tooltip
		tabbedPane.addTab(trans.get("ComponentAnalysisGeneralTab.dragTabchar"), null, scrollpane,
				trans.get("ComponentAnalysisGeneralTab.dragTabchar.ttip"));




		// Create the roll data table
		this.rollTableModel = new ColumnTableModel(
				//// Component
				new Column(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.component")) {
					@Override
					public Object getValueAt(int row) {
						RocketComponent c = rollData.get(row).getComponent();
						if (c instanceof Rocket) {
							return trans.get("ComponentAnalysisGeneralTab.TOTAL");
						}
						return c.toString();
					}
				},
				//// Roll forcing coefficient
				new Column(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.rollforc")) {
					@Override
					public Object getValueAt(int row) {
						return rollData.get(row).getCrollForce();
					}
				},
				//// Roll damping coefficient
				new Column(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.rolldamp")) {
					@Override
					public Object getValueAt(int row) {
						return rollData.get(row).getCrollDamp();
					}
				},
				//// <html>Total C<sub>l</sub>
				new Column(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.total")) {
					@Override
					public Object getValueAt(int row) {
						return rollData.get(row).getCroll();
					}
				}
		) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int getRowCount() {
				return rollData.size();
			}
		};


		table = new JTable(rollTableModel);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setDefaultRenderer(Object.class, new RollDynamicsCellRenderer());

		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Roll dynamics and Roll dynamics tooltip
		tabbedPane.addTab(trans.get("ComponentAnalysisGeneralTab.rollTableModel"), null, scrollpane,
				trans.get("ComponentAnalysisGeneralTab.rollTableModel.ttip"));


		//// Reference length:
		this.add(new StyledLabel(trans.get("ComponentAnalysisGeneralTab.lbl.reflenght"), -1),
				"span, split, gapleft para, gapright rel");
		DoubleModel dm = new DoubleModel(conditions, "RefLength", UnitGroup.UNITS_LENGTH);
		UnitSelector sel = new UnitSelector(dm, true);
		sel.resizeFont(-1);
		this.add(sel, "gapright para");

		//// Reference area:
		this.add(new StyledLabel(trans.get("ComponentAnalysisGeneralTab.lbl.refarea"), -1), "gapright rel");
		dm = new DoubleModel(conditions, "RefArea", UnitGroup.UNITS_AREA);
		sel = new UnitSelector(dm, true);
		sel.resizeFont(-1);
		this.add(sel, "wrap");



		// TODO: LOW: printing
		//		button = new JButton("Print");
		//		button.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				try {
		//					table.print();
		//				} catch (PrinterException e1) {
		//					JOptionPane.showMessageDialog(ComponentAnalysisDialog.this,
		//							"An error occurred while printing.", "Print error",
		//							JOptionPane.ERROR_MESSAGE);
		//				}
		//			}
		//		});
		//		this.add(button,"tag ok");


		// Add the data updater to listen to changes
		this.rocket.addChangeListener(this);
		this.mach.addChangeListener(this);
		this.theta.addChangeListener(this);
		this.aoa.addChangeListener(this);
		this.roll.addChangeListener(this);
		this.stateChanged(null);

		// Remove listeners when closing window
		parent.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				theta.setValue(parameters.getInitialTheta());

				//System.out.println("Closing method called: " + this);
				rocket.removeChangeListener(ComponentAnalysisGeneralPanel.this);
				mach.removeChangeListener(ComponentAnalysisGeneralPanel.this);
				theta.removeChangeListener(ComponentAnalysisGeneralPanel.this);
				aoa.removeChangeListener(ComponentAnalysisGeneralPanel.this);
				roll.removeChangeListener(ComponentAnalysisGeneralPanel.this);
				//System.out.println("SETTING NAN VALUES");
				rocketPanel.setCPAOA(Double.NaN);
				rocketPanel.setCPTheta(Double.NaN);
				rocketPanel.setCPMach(Double.NaN);
				rocketPanel.setCPRoll(Double.NaN);
			}
		});
	}

	private void setParametersThetaUnit(UnitSelector unitSelector) {
		parameters.setThetaUnit(unitSelector.getSelectedUnit());
	}

	private void setParametersAOAUnit(UnitSelector unitSelector) {
		parameters.setAOAUnit(unitSelector.getSelectedUnit());
	}

	private void setParametersMachUnit(UnitSelector unitSelector) {
		parameters.setMachUnit(unitSelector.getSelectedUnit());
	}

	private void setParametersRollUnit(UnitSelector unitSelector) {
		parameters.setRollRateUnit(unitSelector.getSelectedUnit());
	}


	public CAParameters getParameters() {
		return parameters;
	}

	public AerodynamicCalculator getAerodynamicCalculator() {
		return aerodynamicCalculator;
	}

	public Rocket getRocket() {
		return rocket;
	}

	/**
	 * Updates the data in the table and fires a table data change event.
	 */
	@Override
	public void stateChanged(EventObject e) {
		final FlightConfiguration configuration = rocket.getSelectedConfiguration();
		AerodynamicForces forces;
		WarningSet set = new WarningSet();
		conditions.setAOA(aoa.getValue());
		conditions.setTheta(theta.getValue());
		conditions.setMach(mach.getValue());
		conditions.setRollRate(roll.getValue());
		conditions.setReference(configuration);

		if (worstToggle.isSelected()) {
			aerodynamicCalculator.getWorstCP(configuration, conditions, null);
			if (!MathUtil.equals(conditions.getTheta(), theta.getValue())) {
				fakeChange = true;
				theta.setValue(conditions.getTheta()); // Fires a stateChanged event
				fakeChange = false;
				return;
			}
		}

		// key is the comp.hashCode() or motor.getDesignation().hashCode()
		Map<Integer, CMAnalysisEntry> cmMap= MassCalculator.getCMAnalysis(configuration);

		Map<RocketComponent, AerodynamicForces> aeroData = aerodynamicCalculator.getForceAnalysis(configuration, conditions, set);

		stabData.clear();
		dragData.clear();
		rollData.clear();

		for (final RocketComponent comp: configuration.getAllComponents()) {
			CMAnalysisEntry cmEntry = cmMap.get(comp.hashCode());
			if (null == cmEntry) {
				log.warn("Could not find massData entry for component: " + comp.getName());
				continue;
			}

			if ((comp instanceof ComponentAssembly) && !(comp instanceof Rocket)){
				continue;
			}

			String name = cmEntry.name;
			if (cmEntry.source instanceof Rocket) {
				name = trans.get("ComponentAnalysisGeneralTab.TOTAL");
			}
			LongitudinalStabilityRow row = new LongitudinalStabilityRow(name, cmEntry.source);
			stabData.add(row);

			row.source = cmEntry.source;
			row.eachMass = cmEntry.eachMass;
			row.cm = cmEntry.totalCM;

			forces = aeroData.get(comp);
			if (forces == null) {
				row.cpx = 0.0;
				row.cna = 0.0;
				continue;
			}

			if (forces.getCP() != null) {
				if ((comp instanceof Rocket) &&
						(forces.getCP().weight < MathUtil.EPSILON)) {
					row.cpx = Double.NaN;
				} else {
					row.cpx = forces.getCP().x;
				}
				row.cna = forces.getCNa();
			}

			if (!Double.isNaN(forces.getCD())) {
				dragData.add(forces);
			}

			if (comp instanceof FinSet) {
				rollData.add(forces);
			}
			// // We _would_ check this, except TubeFinSet doesn't implement cant angles... so they can't impart any roll torque
			// // If this is ever implemented, uncomment this block:
			// else if(comp instanceof TubeFinSet){
			// 	rollData.add(forces)
			// }
		}

		for (final MotorConfiguration config: configuration.getActiveMotors()) {
			CMAnalysisEntry cmEntry = cmMap.get(config.getMotor().getDesignation().hashCode());
			if (null == cmEntry) {
				continue;
			}

			LongitudinalStabilityRow row = new LongitudinalStabilityRow(cmEntry.name, cmEntry.source);
			stabData.add(row);

			row.source = cmEntry.source;
			row.eachMass = cmEntry.eachMass;
			row.cm = cmEntry.totalCM;
			row.cpx = 0.0;
			row.cna = 0.0;
		}

		// Set warnings
		if (set.isEmpty()) {
			warningList.setListData(new String[] {trans.get("ComponentAnalysisGeneralTab.noWarnings")
			});
		} else {
			warningList.setListData(new Vector<>(set));
		}

		longitudeStabilityTableModel.fireTableDataChanged();
		dragTableModel.fireTableDataChanged();
		rollTableModel.fireTableDataChanged();
	}

	/**
	 * Default cell renderer for the tables.
	 */
	private class CustomCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		protected final Font normalFont;
		protected final Font boldFont;

		private final List<?> data;
		protected final int decimalPlaces;

		private static Color backgroundColor;

		static {
			initColors();
		}

		public CustomCellRenderer(List<?> data, int decimalPlaces) {
			super();
			this.decimalPlaces = decimalPlaces;
			this.data = data;
			this.normalFont = getFont();
			this.boldFont = normalFont.deriveFont(Font.BOLD);
		}

		private static void initColors() {
			updateColors();
			UITheme.Theme.addUIThemeChangeListener(CustomCellRenderer::updateColors);
		}

		private static void updateColors() {
			backgroundColor = GUIUtil.getUITheme().getBackgroundColor();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (value instanceof Double) {
				label.setText(formatDouble((Double) value));
			} else {
				// Other
				label.setText(value != null ? value.toString() : null);
			}

			label.setOpaque(true);
			label.setBackground(backgroundColor);
			label.setHorizontalAlignment(SwingConstants.LEFT);

			if ((row < 0) || (row >= data.size()))
				return label;

			// Set selected color
			if (isSelected) {
				label.setBackground(table.getSelectionBackground());
				label.setForeground((Color) UIManager.get("Table.selectionForeground"));
			} else {
				label.setForeground(table.getForeground());
			}

			return label;
		}

		protected String formatDouble(Double value) {
			DecimalFormat df = new DecimalFormat("0." + "#".repeat(Math.max(0, decimalPlaces)));
			return df.format(value);
		}
	}


	private class StabilityCellRenderer extends CustomCellRenderer {
		public StabilityCellRenderer() {
			super(stabData, 3);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (row == 0) {
				this.setFont(boldFont);
			} else {
				this.setFont(normalFont);
			}

			return label;
		}
	}

	private class DragCellRenderer extends CustomCellRenderer {
		private static final long serialVersionUID = 1L;

		public DragCellRenderer() {
			super(dragData, 3);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (!isSelected && (value instanceof Double)) {
				double cd = (Double) value;
				float r = (float) (cd / 1.5);

				float hue = MathUtil.clamp(0.3333f * (1 - 2.0f * r), 0, 0.3333f);
				float sat = MathUtil.clamp(0.8f * r + 0.1f * (1 - r), 0, 1);
				float val = 1.0f;

				label.setBackground(Color.getHSBColor(hue, sat, val));
				label.setForeground(Color.BLACK);
			}

			if ((row < 0) || (row >= dragData.size()))
				return label;

			if ((dragData.get(row).getComponent() instanceof Rocket) || (column == 4)) {
				label.setFont(boldFont);
			} else {
				label.setFont(normalFont);
			}

			return label;
		}

		@Override
		protected String formatDouble(Double cd) {
			final double totalCD = dragData.get(0).getCD();

			DecimalFormat df = new DecimalFormat("0." + "#".repeat(Math.max(0, decimalPlaces)));
			String cdFormatted = df.format(cd);
			return String.format(cdFormatted + "  (%.0f%%)", 100 * cd / totalCD);
		}
	}

	private class RollDynamicsCellRenderer extends CustomCellRenderer {
		public RollDynamicsCellRenderer() {
			super(rollData, 3);
		}
	}

	private class LongitudinalStabilityRow {

		public String name;
		public Object source;
		public double eachMass;
		public Coordinate cm;
		public double cpx;
		public double cna;

		public LongitudinalStabilityRow(final String _name, final Object _source){
			name = _name;
			source = _source;
			eachMass = Double.NaN;
			cm = Coordinate.NaN;
			cpx = Double.NaN;
			cna = Double.NaN;
		}

	}
}
