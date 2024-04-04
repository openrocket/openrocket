package info.openrocket.swing.gui.dialogs;

import static info.openrocket.core.unit.Unit.NOUNIT;
import static info.openrocket.core.util.Chars.ALPHA;
import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.masscalc.CMAnalysisEntry;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.*;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.adaptors.Column;
import info.openrocket.swing.gui.adaptors.ColumnTable;
import info.openrocket.swing.gui.adaptors.ColumnTableModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.ConfigurationComboBox;
import info.openrocket.swing.gui.components.StageSelector;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.widgets.SelectColorToggleButton;
import info.openrocket.swing.gui.widgets.SelectColorButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentAnalysisDialog extends JDialog implements StateChangeListener {
	private static final Logger log = LoggerFactory.getLogger(ComponentAnalysisDialog.class);

	private static final long serialVersionUID = 9131240570600307935L;
	private static ComponentAnalysisDialog singletonDialog = null;
	private static final Translator trans = Application.getTranslator();


	private final FlightConditions conditions;
	private final Rocket rkt;
	private final DoubleModel theta, aoa, mach, roll;
	private final JToggleButton worstToggle;
	private boolean fakeChange = false;
	private AerodynamicCalculator aerodynamicCalculator;
	private double initTheta;

	private final ColumnTableModel longitudeStabilityTableModel;
	private final ColumnTableModel dragTableModel;
	private final ColumnTableModel rollTableModel;

	private final JList<Object> warningList;


	private final List<LongitudinalStabilityRow> stabData = new ArrayList<>();
	private final List<AerodynamicForces> dragData = new ArrayList<AerodynamicForces>();
	private final List<AerodynamicForces> rollData = new ArrayList<AerodynamicForces>();


	public ComponentAnalysisDialog(final RocketPanel rocketPanel) {
		////Component analysis
		super(SwingUtilities.getWindowAncestor(rocketPanel),
				trans.get("componentanalysisdlg.componentanalysis"));

		JTable table;

		JPanel panel = new JPanel(new MigLayout("fill", "[120lp][70lp][50lp][]"));
		add(panel);

		rkt = rocketPanel.getDocument().getRocket();
		this.aerodynamicCalculator = rocketPanel.getAerodynamicCalculator().newInstance();


		conditions = new FlightConditions(rkt.getSelectedConfiguration());

		rocketPanel.setCPAOA(0);
		aoa = new DoubleModel(rocketPanel, "CPAOA", UnitGroup.UNITS_ANGLE, 0, Math.PI);
		rocketPanel.setCPMach(Application.getPreferences().getDefaultMach());
		mach = new DoubleModel(rocketPanel, "CPMach", UnitGroup.UNITS_COEFFICIENT, 0);
		initTheta = rocketPanel.getFigure().getRotation();
		rocketPanel.setCPTheta(rocketPanel.getFigure().getRotation());
		theta = new DoubleModel(rocketPanel, "CPTheta", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
		rocketPanel.setCPRoll(0);
		roll = new DoubleModel(rocketPanel, "CPRoll", UnitGroup.UNITS_ROLL);

		//// Wind direction:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.winddir")));
		EditableSpinner spinner = new EditableSpinner(theta.getSpinnerModel());
		panel.add(spinner, "growx");
		panel.add(new UnitSelector(theta));
		BasicSlider slider = new BasicSlider(theta.getSliderModel(0, 2 * Math.PI));
		panel.add(slider, "growx, split 2");
		//// Worst button
		worstToggle = new SelectColorToggleButton(trans.get("componentanalysisdlg.ToggleBut.worst"));
		worstToggle.setSelected(true);
		worstToggle.addActionListener(new ActionListener() {
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
		panel.add(worstToggle);


		warningList = new JList<>();
		JScrollPane scrollPane = new JScrollPane(warningList);
		////Warnings:
		scrollPane.setBorder(BorderFactory.createTitledBorder(trans.get("componentanalysisdlg.TitledBorder.warnings")));
		panel.add(scrollPane, "gap paragraph, spany 4, w 300lp, grow, height :100lp:, wrap");

		////Angle of attack:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.angleofattack")));
		panel.add(new EditableSpinner(aoa.getSpinnerModel()), "growx");
		panel.add(new UnitSelector(aoa));
		panel.add(new BasicSlider(aoa.getSliderModel(0, Math.PI)), "growx, wrap");

		//// Mach number:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.machnumber")));
		panel.add(new EditableSpinner(mach.getSpinnerModel()));
		panel.add(new UnitSelector(mach));
		panel.add(new BasicSlider(mach.getSliderModel(0, 3)), "growx, wrap");

		//// Roll rate:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.rollrate")));
		panel.add(new EditableSpinner(roll.getSpinnerModel()), "growx");
		panel.add(new UnitSelector(roll));
		panel.add(new BasicSlider(roll.getSliderModel(-20 * 2 * Math.PI, 20 * 2 * Math.PI)),
				"growx, wrap");

		// Stage and motor selection:
		//// Active stages:
		StageSelector stageSelector = new StageSelector(rkt);
		rkt.addChangeListener(stageSelector);
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.activestages")), "spanx, split, gapafter rel");
		panel.add(stageSelector, "gapafter paragraph");

		//// Motor configuration:
		JLabel label = new JLabel(trans.get("componentanalysisdlg.lbl.motorconf"));
		label.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(label, "growx, right");

		final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rkt);
		panel.add( configComboBox, "wrap");


		// Tabbed pane

		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "spanx, growx, growy, pushy");


		// Create the Longitudinal Stability (CM vs CP) data table
		longitudeStabilityTableModel = new ColumnTableModel(

				//// Component
				new Column(trans.get("componentanalysisdlg.TabStability.Col.Component")) {
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
				new Column(trans.get("componentanalysisdlg.TabStability.Col.EachMass") + " (" + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).eachMass);
					}
				},
				new Column(trans.get("componentanalysisdlg.TabStability.Col.AllMass") + " (" + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();

					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).cm.weight);
					}
				},
				new Column(trans.get("componentanalysisdlg.TabStability.Col.CG") + " (" + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit() + ")") {
					final private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
					
					@Override
					public Object getValueAt(int row) {
						return unit.toUnit(stabData.get(row).cm.x);
					}
				},
				new Column(trans.get("componentanalysisdlg.TabStability.Col.CP") + " (" + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit() + ")") {
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
		longitudeStabilityTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new StabilityCellRenderer());

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Stability and Stability information
		tabbedPane.addTab(trans.get("componentanalysisdlg.TabStability"),
				null, scrollpane, trans.get("componentanalysisdlg.TabStability.ttip"));



		// Create the drag data table
		dragTableModel = new ColumnTableModel(
				//// Component
				new Column(trans.get("componentanalysisdlg.dragTableModel.Col.Component")) {
					@Override
					public Object getValueAt(int row) {
						RocketComponent c = dragData.get(row).getComponent();
						if (c instanceof Rocket) {
							return trans.get("componentanalysisdlg.TOTAL");
						}
						return c.toString();
					}

					@Override
					public int getDefaultWidth() {
						return 200;
					}
				},
				//// <html>Pressure C<sub>D</sub>
				new Column(trans.get("componentanalysisdlg.dragTableModel.Col.Pressure")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getPressureCD();
					}
				},
				//// <html>Base C<sub>D</sub>
				new Column(trans.get("componentanalysisdlg.dragTableModel.Col.Base")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getBaseCD();
					}
				},
				//// <html>Friction C<sub>D</sub>
				new Column(trans.get("componentanalysisdlg.dragTableModel.Col.friction")) {
					@Override
					public Object getValueAt(int row) {
						return dragData.get(row).getFrictionCD();
					}
				},
				//// <html>Total C<sub>D</sub>
				new Column(trans.get("componentanalysisdlg.dragTableModel.Col.total")) {
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
		dragTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new DragCellRenderer());

		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Drag characteristics and Drag characteristics tooltip
		tabbedPane.addTab(trans.get("componentanalysisdlg.dragTabchar"), null, scrollpane,
				trans.get("componentanalysisdlg.dragTabchar.ttip"));




		// Create the roll data table
		rollTableModel = new ColumnTableModel(
				//// Component
				new Column(trans.get("componentanalysisdlg.rollTableModel.Col.component")) {
					@Override
					public Object getValueAt(int row) {
						RocketComponent c = rollData.get(row).getComponent();
						if (c instanceof Rocket) {
							return trans.get("componentanalysisdlg.TOTAL");
						}
						return c.toString();
					}
				},
				//// Roll forcing coefficient
				new Column(trans.get("componentanalysisdlg.rollTableModel.Col.rollforc")) {
					@Override
					public Object getValueAt(int row) {
						return rollData.get(row).getCrollForce();
					}
				},
				//// Roll damping coefficient
				new Column(trans.get("componentanalysisdlg.rollTableModel.Col.rolldamp")) {
					@Override
					public Object getValueAt(int row) {
						return rollData.get(row).getCrollDamp();
					}
				},
				//// <html>Total C<sub>l</sub>
				new Column(trans.get("componentanalysisdlg.rollTableModel.Col.total")) {
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
		tabbedPane.addTab(trans.get("componentanalysisdlg.rollTableModel"), null, scrollpane,
				trans.get("componentanalysisdlg.rollTableModel.ttip"));



		// Add the data updater to listen to changes
		rkt.addChangeListener(this);
		mach.addChangeListener(this);
		theta.addChangeListener(this);
		aoa.addChangeListener(this);
		roll.addChangeListener(this);
		this.stateChanged(null);



		// Remove listeners when closing window
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				theta.setValue(initTheta);

				//System.out.println("Closing method called: " + this);
				rkt.removeChangeListener(ComponentAnalysisDialog.this);
				mach.removeChangeListener(ComponentAnalysisDialog.this);
				theta.removeChangeListener(ComponentAnalysisDialog.this);
				aoa.removeChangeListener(ComponentAnalysisDialog.this);
				roll.removeChangeListener(ComponentAnalysisDialog.this);
				//System.out.println("SETTING NAN VALUES");
				rocketPanel.setCPAOA(Double.NaN);
				rocketPanel.setCPTheta(Double.NaN);
				rocketPanel.setCPMach(Double.NaN);
				rocketPanel.setCPRoll(Double.NaN);
				singletonDialog = null;
			}
		});

		//// Reference length:
		panel.add(new StyledLabel(trans.get("componentanalysisdlg.lbl.reflenght"), -1),
				"span, split, gapleft para, gapright rel");
		DoubleModel dm = new DoubleModel(conditions, "RefLength", UnitGroup.UNITS_LENGTH);
		UnitSelector sel = new UnitSelector(dm, true);
		sel.resizeFont(-1);
		panel.add(sel, "gapright para");

		//// Reference area: 
		panel.add(new StyledLabel(trans.get("componentanalysisdlg.lbl.refarea"), -1), "gapright rel");
		dm = new DoubleModel(conditions, "RefArea", UnitGroup.UNITS_AREA);
		sel = new UnitSelector(dm, true);
		sel.resizeFont(-1);
		panel.add(sel, "wrap");



		// Buttons
		JButton button;

		// TODO: LOW: printing
		//		button = new SelectColorButton("Print");
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
		//		panel.add(button,"tag ok");

		//button = new SelectColorButton("Close");
		//Close button
		button = new SelectColorButton(trans.get("dlg.but.close"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentAnalysisDialog.this.dispose();
			}
		});
		panel.add(button, "span, tag cancel");


		this.setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();

		GUIUtil.setDisposableDialogOptions(this, null);
	}



	/**
	 * Updates the data in the table and fires a table data change event.
	 */
	@Override
	public void stateChanged(EventObject e) {
		final FlightConfiguration configuration = rkt.getSelectedConfiguration();
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

		for(final RocketComponent comp: configuration.getAllComponents()) {
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
				name = trans.get("componentanalysisdlg.TOTAL");
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

		for(final MotorConfiguration config: configuration.getActiveMotors()) {
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
			warningList.setListData(new String[] {trans.get("componentanalysisdlg.noWarnings")
			});
		} else {
			warningList.setListData(new Vector<Warning>(set));
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

	/////////  Singleton implementation

	public static void showDialog(RocketPanel rocketpanel) {
		if (singletonDialog != null)
			singletonDialog.dispose();
		singletonDialog = new ComponentAnalysisDialog(rocketpanel);
		singletonDialog.setVisible(true);
	}

	public static void hideDialog() {
		if (singletonDialog != null)
			singletonDialog.dispose();
	}

}
