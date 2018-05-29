package net.sf.openrocket.gui.dialogs;

import static net.sf.openrocket.unit.Unit.NOUNIT;
import static net.sf.openrocket.util.Chars.ALPHA;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTable;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ConfigurationComboBox;
import net.sf.openrocket.gui.components.StageSelector;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

public class ComponentAnalysisDialog extends JDialog implements StateChangeListener {
	private static final long serialVersionUID = 9131240570600307935L;
	private static ComponentAnalysisDialog singletonDialog = null;
	private static final Translator trans = Application.getTranslator();


	private final FlightConditions conditions;
	private final Rocket rkt;
	private final DoubleModel theta, aoa, mach, roll;
	private final JToggleButton worstToggle;
	private boolean fakeChange = false;
	private AerodynamicCalculator aerodynamicCalculator;
	private final MassCalculator massCalculator = new MassCalculator();

	private final ColumnTableModel cpTableModel;
	private final ColumnTableModel dragTableModel;
	private final ColumnTableModel rollTableModel;

	private final JList<Object> warningList;


	private final List<Object[]> cgData = new ArrayList<Object[]>();
	private final List<AerodynamicForces> dragData = new ArrayList<AerodynamicForces>();
	private double totalCD = 0;
	private final List<AerodynamicForces> rollData = new ArrayList<AerodynamicForces>();


	public ComponentAnalysisDialog(final RocketPanel rocketPanel) {
		////Component analysis
		super(SwingUtilities.getWindowAncestor(rocketPanel),
				trans.get("componentanalysisdlg.componentanalysis"));

		JTable table;

		JPanel panel = new JPanel(new MigLayout("fill"));
		add(panel);

		rkt = rocketPanel.getDocument().getRocket();
		this.aerodynamicCalculator = rocketPanel.getAerodynamicCalculator().newInstance();


		conditions = new FlightConditions(rkt.getSelectedConfiguration());

		rocketPanel.setCPAOA(0);
		aoa = new DoubleModel(rocketPanel, "CPAOA", UnitGroup.UNITS_ANGLE, 0, Math.PI);
		rocketPanel.setCPMach(Application.getPreferences().getDefaultMach());
		mach = new DoubleModel(rocketPanel, "CPMach", UnitGroup.UNITS_COEFFICIENT, 0);
		rocketPanel.setCPTheta(rocketPanel.getFigure().getRotation());
		theta = new DoubleModel(rocketPanel, "CPTheta", UnitGroup.UNITS_ANGLE, 0, 2 * Math.PI);
		rocketPanel.setCPRoll(0);
		roll = new DoubleModel(rocketPanel, "CPRoll", UnitGroup.UNITS_ROLL);

		//// Wind direction:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.winddir")), "width 120lp!");
		panel.add(new UnitSelector(theta, true), "width 50lp!");
		BasicSlider slider = new BasicSlider(theta.getSliderModel(0, 2 * Math.PI));
		panel.add(slider, "growx, split 2");
		//// Worst button
		worstToggle = new JToggleButton(trans.get("componentanalysisdlg.ToggleBut.worst"));
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
		panel.add(worstToggle, "");


		warningList = new JList<>();
		JScrollPane scrollPane = new JScrollPane(warningList);
		////Warnings:
		scrollPane.setBorder(BorderFactory.createTitledBorder(trans.get("componentanalysisdlg.TitledBorder.warnings")));
		panel.add(scrollPane, "gap paragraph, spany 4, width 300lp!, growy 1, height :100lp:, wrap");

		////Angle of attack:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.angleofattack")), "width 120lp!");
		panel.add(new UnitSelector(aoa, true), "width 50lp!");
		panel.add(new BasicSlider(aoa.getSliderModel(0, Math.PI)), "growx, wrap");

		//// Mach number:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.machnumber")), "width 120lp!");
		panel.add(new UnitSelector(mach, true), "width 50lp!");
		panel.add(new BasicSlider(mach.getSliderModel(0, 3)), "growx, wrap");

		//// Roll rate:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.rollrate")), "width 120lp!");
		panel.add(new UnitSelector(roll, true), "width 50lp!");
		panel.add(new BasicSlider(roll.getSliderModel(-20 * 2 * Math.PI, 20 * 2 * Math.PI)),
				"growx, wrap");

		// Stage and motor selection:
		//// Active stages:
		panel.add(new JLabel(trans.get("componentanalysisdlg.lbl.activestages")), "spanx, split, gapafter rel");
		panel.add(new StageSelector( rkt), "gapafter paragraph");

		//// Motor configuration:
		JLabel label = new JLabel(trans.get("componentanalysisdlg.lbl.motorconf"));
		label.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(label, "growx, right");

		final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rkt);
		panel.add( configComboBox, "wrap");


		// Tabbed pane

		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "spanx, growx, growy");


		// Create the CP data table
		cpTableModel = new ColumnTableModel(

				//// Component
				new Column(trans.get("componentanalysisdlg.TabStability.Col.Component")) {
					@Override
					public Object getValueAt(int row) {
						Object c = cgData.get(row)[0];
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
				new Column(trans.get("componentanalysisdlg.TabStability.Col.CG") + " / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
					
					@Override
					public Object getValueAt(int row) {
						Coordinate cg = (Coordinate) cgData.get(row)[1];
						if ( cg == null ) {
							return null;
						}
						return unit.toString(cg.x);
					}
				},
				new Column(trans.get("componentanalysisdlg.TabStability.Col.Mass") + " / " + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();
					
					@Override
					public Object getValueAt(int row) {
						Coordinate cg = (Coordinate) cgData.get(row)[1];
						if ( cg == null ) {
							return null;
						}
						return unit.toString(cg.weight);
					}
				},
				new Column(trans.get("componentanalysisdlg.TabStability.Col.CP") + " / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
					
					@Override
					public Object getValueAt(int row) {
						AerodynamicForces forces = (AerodynamicForces) cgData.get(row)[2];
						if ( forces == null ) {
							return null;
						}
						return unit.toString(forces.getCP().x);
					}
				},
				new Column("<html>C<sub>N<sub>" + ALPHA + "</sub></sub>") {
					@Override
					public Object getValueAt(int row) {
						AerodynamicForces forces = (AerodynamicForces) cgData.get(row)[2];
						if ( forces == null ) {
							return null;
						}
						return NOUNIT.toString(forces.getCP().weight);
					}
				}
	
				) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public int getRowCount() {
				return cgData.size();
			}
		};

		table = new ColumnTable(cpTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		cpTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new CustomCellRenderer());
		//		table.setShowHorizontalLines(false);
		//		table.setShowVerticalLines(true);

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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		dragTableModel.setColumnWidths(table.getColumnModel());

		table.setDefaultRenderer(Object.class, new DragCellRenderer(new Color(0.5f, 1.0f, 0.5f)));
		//		table.setShowHorizontalLines(false);
		//		table.setShowVerticalLines(true);

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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		table.setDefaultRenderer(Object.class, new CustomCellRenderer());

		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600, 200));

		//// Roll dynamics and Roll dynamics tooltip
		tabbedPane.addTab(trans.get("componentanalysisdlg.rollTableModel"), null, scrollpane,
				trans.get("componentanalysisdlg.rollTableModel.ttip"));





		// Add the data updater to listen to changes in aoa and theta
		mach.addChangeListener(this);
		theta.addChangeListener(this);
		aoa.addChangeListener(this);
		roll.addChangeListener(this);
		this.stateChanged(null);



		// Remove listeners when closing window
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				//System.out.println("Closing method called: " + this);
				theta.removeChangeListener(ComponentAnalysisDialog.this);
				aoa.removeChangeListener(ComponentAnalysisDialog.this);
				mach.removeChangeListener(ComponentAnalysisDialog.this);
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
		//		panel.add(button,"tag ok");

		//button = new JButton("Close");
		//Close button
		button = new JButton(trans.get("dlg.but.close"));
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

		Map<RocketComponent, AerodynamicForces> aeroData =
				aerodynamicCalculator.getForceAnalysis(configuration, conditions, set);
		Map<RocketComponent, Coordinate> massData =
				massCalculator.getCGAnalysis(configuration);


		cgData.clear();
		dragData.clear();
		rollData.clear();
		for (RocketComponent c : configuration.getActiveComponents()) {
			if ( c instanceof AxialStage ) {
				continue;
			}
			Object[] data = new Object[3];
			cgData.add(data);
			data[0] = c;

			Coordinate cg = massData.get(c);
			data[1] = cg;
			
			forces = aeroData.get(c);
			if (forces == null) {
				continue;
			}
			if (forces.getCP() != null) {
				data[2] = forces;
			}
			
			if (!Double.isNaN(forces.getCD())) {
				dragData.add(forces);
			}
			if (c instanceof FinSet) {
				rollData.add(forces);
			}
		}

		for ( MotorConfiguration motorConfig : configuration.getActiveMotors()) {
		
			Object [] data = new Object[3];
			cgData.add(data);
			
			data[0] = motorConfig.getMotor().getDesignation();
			data[1] = motorConfig.getMotor().getLaunchMass(); 
		}
		
		forces = aeroData.get(rkt);
		if (forces != null) {
			Object[] data = new Object[3];
			cgData.add(data);
			data[0] = rkt;
			data[1] = massData.get(rkt);
			data[2] = forces;
			dragData.add(forces);
			rollData.add(forces);
			totalCD = forces.getCD();
		} else {
			totalCD = 0;
		}

		// Set warnings
		if (set.isEmpty()) {
			warningList.setListData(new String[] {
					trans.get("componentanalysisdlg.noWarnings")
			});
		} else {
			warningList.setListData(new Vector<Warning>(set));
		}

		cpTableModel.fireTableDataChanged();
		dragTableModel.fireTableDataChanged();
		rollTableModel.fireTableDataChanged();
	}


	private class CustomCellRenderer extends JLabel implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Font normalFont;
		private final Font boldFont;

		public CustomCellRenderer() {
			super();
			normalFont = getFont();
			boldFont = normalFont.deriveFont(Font.BOLD);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			this.setText(value == null ? null : value.toString());

			if ((row < 0) || (row >= cgData.size()))
				return this;

			if (cgData.get(row)[0] instanceof Rocket) {
				this.setFont(boldFont);
			} else {
				this.setFont(normalFont);
			}
			return this;
		}
	}



	private class DragCellRenderer extends JLabel implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Font normalFont;
		private final Font boldFont;


		public DragCellRenderer(Color baseColor) {
			super();
			normalFont = getFont();
			boldFont = normalFont.deriveFont(Font.BOLD);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			if (value instanceof Double) {

				// A drag coefficient
				double cd = (Double) value;
				this.setText(String.format("%.2f (%.0f%%)", cd, 100 * cd / totalCD));

				float r = (float) (cd / 1.5);

				float hue = MathUtil.clamp(0.3333f * (1 - 2.0f * r), 0, 0.3333f);
				float sat = MathUtil.clamp(0.8f * r + 0.1f * (1 - r), 0, 1);
				float val = 1.0f;

				this.setBackground(Color.getHSBColor(hue, sat, val));
				this.setOpaque(true);
				this.setHorizontalAlignment(SwingConstants.CENTER);

			} else {

				// Other
				this.setText(value.toString());
				this.setOpaque(false);
				this.setHorizontalAlignment(SwingConstants.LEFT);

			}

			if ((row < 0) || (row >= dragData.size()))
				return this;

			if ((dragData.get(row).getComponent() instanceof Rocket) || (column == 4)) {
				this.setFont(boldFont);
			} else {
				this.setFont(normalFont);
			}
			return this;
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
