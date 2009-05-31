package net.sf.openrocket.gui;

import static net.sf.openrocket.unit.Unit.NOUNIT2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.MotorConfigurationModel;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Prefs;

public class ComponentAnalysisDialog extends JDialog implements ChangeListener {

	private static ComponentAnalysisDialog singletonDialog = null;

	
	private final FlightConditions conditions;
	private final Configuration configuration;
	private final DoubleModel theta, aoa, mach, roll;
	private final JToggleButton worstToggle;
	private boolean fakeChange = false;
	private AerodynamicCalculator calculator;
	
	private final ColumnTableModel cpTableModel;
	private final ColumnTableModel dragTableModel;
	private final ColumnTableModel rollTableModel;
	
	private final JList warningList;
	
	
	private final List<AerodynamicForces> cpData = new ArrayList<AerodynamicForces>();
	private final List<AerodynamicForces> dragData = new ArrayList<AerodynamicForces>();
	private double totalCD = 0;
	private final List<AerodynamicForces> rollData = new ArrayList<AerodynamicForces>();
	
	
	public ComponentAnalysisDialog(final RocketPanel rocketPanel) {
		super(SwingUtilities.getWindowAncestor(rocketPanel), "Component analysis");

		JTable table;

		JPanel panel = new JPanel(new MigLayout("fill","[][35lp::][fill][fill]"));
		add(panel);
		
		this.configuration = rocketPanel.getConfiguration();
		this.calculator = rocketPanel.getCalculator().newInstance();
		this.calculator.setConfiguration(configuration);

		
		conditions = new FlightConditions(configuration);
		
		rocketPanel.setCPAOA(0);
		aoa = new DoubleModel(rocketPanel, "CPAOA", UnitGroup.UNITS_ANGLE, 0, Math.PI);
		rocketPanel.setCPMach(Prefs.getDefaultMach());
		mach = new DoubleModel(rocketPanel, "CPMach", UnitGroup.UNITS_COEFFICIENT, 0);
		rocketPanel.setCPTheta(rocketPanel.getFigure().getRotation());
		theta = new DoubleModel(rocketPanel, "CPTheta", UnitGroup.UNITS_ANGLE, 0, 2*Math.PI);
		rocketPanel.setCPRoll(0);
		roll = new DoubleModel(rocketPanel, "CPRoll", UnitGroup.UNITS_ROLL);
		
		
		panel.add(new JLabel("Wind direction:"),"width 100lp!");
		panel.add(new UnitSelector(theta,true),"width 50lp!");
		BasicSlider slider = new BasicSlider(theta.getSliderModel(0, 2*Math.PI));
		panel.add(slider,"growx, split 2");
		worstToggle = new JToggleButton("Worst");
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
		panel.add(worstToggle,"");
		
		
		warningList = new JList();
		JScrollPane scrollPane = new JScrollPane(warningList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Warnings:"));
		panel.add(scrollPane,"gap paragraph, spany 4, width 300lp!, growy 1, height :100lp:, wrap");
		
		
		panel.add(new JLabel("Angle of attack:"),"width 100lp!");
		panel.add(new UnitSelector(aoa,true),"width 50lp!");
		panel.add(new BasicSlider(aoa.getSliderModel(0, Math.PI)),"growx, wrap");
		
		panel.add(new JLabel("Mach number:"),"width 100lp!");
		panel.add(new UnitSelector(mach,true),"width 50lp!");
		panel.add(new BasicSlider(mach.getSliderModel(0, 3)),"growx, wrap");
		
		panel.add(new JLabel("Roll rate:"), "width 100lp!");
		panel.add(new UnitSelector(roll,true),"width 50lp!");
		panel.add(new BasicSlider(roll.getSliderModel(-20*2*Math.PI, 20*2*Math.PI)),
				"growx, wrap paragraph");
		
		
		// Stage and motor selection:
		
		panel.add(new JLabel("Active stages:"),"spanx, split, gapafter rel");
		panel.add(new StageSelector(configuration),"gapafter paragraph");
				
		JLabel label = new JLabel("Motor configuration:");
		label.setHorizontalAlignment(JLabel.RIGHT);
		panel.add(label,"growx, right");
		panel.add(new JComboBox(new MotorConfigurationModel(configuration)),"wrap");

		
		
		// Tabbed pane
		
		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "spanx, growx, growy");
		
		
		// Create the CP data table
		cpTableModel = new ColumnTableModel(
				
				new Column("Component") {
					@Override public Object getValueAt(int row) {
						RocketComponent c = cpData.get(row).component;
						if (c instanceof Rocket) {
							return "Total";
						}
						return c.toString();
					}
					@Override public int getDefaultWidth() {
						return 200;
					}
				},
				new Column("CG / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
					@Override public Object getValueAt(int row) {
						return unit.toString(cpData.get(row).cg.x);
					}
				},
				new Column("Mass / " + UnitGroup.UNITS_MASS.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_MASS.getDefaultUnit();
					@Override
					public Object getValueAt(int row) {
						return unit.toString(cpData.get(row).cg.weight);
					}
				},
				new Column("CP / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().getUnit()) {
					private Unit unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
					@Override public Object getValueAt(int row) {
						return unit.toString(cpData.get(row).cp.x);
					}
				},
				new Column("<html>C<sub>N<sub>\u03b1</sub></sub>") {
					@Override public Object getValueAt(int row) {
						return NOUNIT2.toString(cpData.get(row).cp.weight);
					}
				}
				
		) {
			@Override public int getRowCount() {
				return cpData.size();
			}
		};
		
		table = new JTable(cpTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		cpTableModel.setColumnWidths(table.getColumnModel());
		
		table.setDefaultRenderer(Object.class, new CustomCellRenderer());
//		table.setShowHorizontalLines(false);
//		table.setShowVerticalLines(true);
		
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,200));
		
		tabbedPane.addTab("Stability", null, scrollpane, "Stability information");
		
		
		
		// Create the drag data table
		dragTableModel = new ColumnTableModel(
				new Column("Component") {
					@Override public Object getValueAt(int row) {
						RocketComponent c = dragData.get(row).component;
						if (c instanceof Rocket) {
							return "Total";
						}
						return c.toString();
					}
					@Override public int getDefaultWidth() {
						return 200;
					}
				},
				new Column("<html>Pressure C<sub>D</sub>") {
					@Override public Object getValueAt(int row) {
						return dragData.get(row).pressureCD;
					}
				},
				new Column("<html>Base C<sub>D</sub>") {
					@Override public Object getValueAt(int row) {
						return dragData.get(row).baseCD;
					}
				},
				new Column("<html>Friction C<sub>D</sub>") {
					@Override public Object getValueAt(int row) {
						return dragData.get(row).frictionCD;
					}
				},
				new Column("<html>Total C<sub>D</sub>") {
					@Override public Object getValueAt(int row) {
						return dragData.get(row).CD;
					}
				}
		) {
			@Override public int getRowCount() {
				return dragData.size();
			}			
		};
		

		table = new JTable(dragTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		dragTableModel.setColumnWidths(table.getColumnModel());
		
		table.setDefaultRenderer(Object.class, new DragCellRenderer(new Color(0.5f,1.0f,0.5f)));
//		table.setShowHorizontalLines(false);
//		table.setShowVerticalLines(true);
		
		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,200));
		
		tabbedPane.addTab("Drag characteristics", null, scrollpane, "Drag characteristics");
		
		
		
		
		// Create the roll data table
		rollTableModel = new ColumnTableModel(
				new Column("Component") {
					@Override public Object getValueAt(int row) {
						RocketComponent c = rollData.get(row).component;
						if (c instanceof Rocket) {
							return "Total";
						}
						return c.toString();
					}
				},
				new Column("Roll forcing coefficient") {
					@Override public Object getValueAt(int row) {
						return rollData.get(row).CrollForce;
					}
				},
				new Column("Roll damping coefficient") {
					@Override public Object getValueAt(int row) {
						return rollData.get(row).CrollDamp;
					}
				},
				new Column("<html>Total C<sub>l</sub>") {
					@Override public Object getValueAt(int row) {
						return rollData.get(row).Croll;
					}
				}
		) {
			@Override public int getRowCount() {
				return rollData.size();
			}			
		};
		

		table = new JTable(rollTableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setSelectionForeground(Color.BLACK);
		rollTableModel.setColumnWidths(table.getColumnModel());
		
		scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,200));
		
		tabbedPane.addTab("Roll dynamics", null, scrollpane, "Roll dynamics");
		
		
		
		
		
		
		// Add the data updater to listen to changes in aoa and theta
		mach.addChangeListener(this);
		theta.addChangeListener(this);
		aoa.addChangeListener(this);
		roll.addChangeListener(this);
		configuration.addChangeListener(this);
		this.stateChanged(null);
		
		
		
		// Remove listeners when closing window
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Closing method called: "+this);
				theta.removeChangeListener(ComponentAnalysisDialog.this);
				aoa.removeChangeListener(ComponentAnalysisDialog.this);
				mach.removeChangeListener(ComponentAnalysisDialog.this);
				roll.removeChangeListener(ComponentAnalysisDialog.this);
				configuration.removeChangeListener(ComponentAnalysisDialog.this);
				System.out.println("SETTING NAN VALUES");
				rocketPanel.setCPAOA(Double.NaN);
				rocketPanel.setCPTheta(Double.NaN);
				rocketPanel.setCPMach(Double.NaN);
				rocketPanel.setCPRoll(Double.NaN);
				singletonDialog = null;
			}
		});
		

		panel.add(new ResizeLabel("Reference length: ", -1), 
				"span, split, gapleft para, gapright rel");
		DoubleModel dm = new DoubleModel(conditions, "RefLength", UnitGroup.UNITS_LENGTH);
		UnitSelector sel = new UnitSelector(dm, true);
		sel.resizeFont(-1);
		panel.add(sel, "gapright para");
		
		panel.add(new ResizeLabel("Reference area: ", -1), "gapright rel");
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
		
		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComponentAnalysisDialog.this.dispose();
			}
		});
		panel.add(button,"span, split, tag cancel");
		

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GUIUtil.installEscapeCloseOperation(this);
		pack();
	}
	
	
	
	/**
	 * Updates the data in the table and fires a table data change event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		AerodynamicForces forces;
		WarningSet set = new WarningSet();
		conditions.setAOA(aoa.getValue());
		conditions.setTheta(theta.getValue());
		conditions.setMach(mach.getValue());
		conditions.setRollRate(roll.getValue());
		conditions.setReference(configuration);
		
		if (worstToggle.isSelected()) {
			calculator.getWorstCP(conditions, null);
			if (!MathUtil.equals(conditions.getTheta(), theta.getValue())) {
				fakeChange = true;
				theta.setValue(conditions.getTheta());  // Fires a stateChanged event
				fakeChange = false;
				return;
			}
		}
		
		Map<RocketComponent, AerodynamicForces> data = calculator.getForceAnalysis(conditions, set);
		
		cpData.clear();
		dragData.clear();
		rollData.clear();
		for (RocketComponent c: configuration) {
			forces = data.get(c);
			if (forces == null)
				continue;
			if (forces.cp != null) {
				cpData.add(forces);
			}
			if (!Double.isNaN(forces.CD)) {
				dragData.add(forces);
			}
			if (c instanceof FinSet) {
				rollData.add(forces);
			}
		}
		forces = data.get(configuration.getRocket());
		if (forces != null) {
			cpData.add(forces);
			dragData.add(forces);
			rollData.add(forces);
			totalCD = forces.CD;
		} else {
			totalCD = 0;
		}
		
		// Set warnings
		if (set.isEmpty()) {
			warningList.setListData(new String[] {
					"<html><i><font color=\"gray\">No warnings.</font></i>"
			});
		} else {
			warningList.setListData(new Vector<Warning>(set));
		}
		
		cpTableModel.fireTableDataChanged();
		dragTableModel.fireTableDataChanged();
		rollTableModel.fireTableDataChanged();
	}
	
	
	private class CustomCellRenderer extends JLabel implements TableCellRenderer {
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
			
			this.setText(value.toString());
			
			if ((row < 0) || (row >= cpData.size()))
					return this;
			
			if (cpData.get(row).component instanceof Rocket) {
				this.setFont(boldFont);
			} else {
				this.setFont(normalFont);
			}
			return this;
		}
	}
	

	
	private class DragCellRenderer extends JLabel implements TableCellRenderer {
		private final Font normalFont;
		private final Font boldFont;
		
		private final float[] start = { 0.3333f, 0.2f, 1.0f };
		private final float[] end = { 0.0f, 0.8f, 1.0f };
		
		
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
				double cd = (Double)value;
				this.setText(String.format("%.2f (%.0f%%)", cd, 100*cd/totalCD));

				float r = (float)(cd/1.5);
				
				float hue = MathUtil.clamp(0.3333f * (1-2.0f*r), 0, 0.3333f);
				float sat = MathUtil.clamp(0.8f*r + 0.1f*(1-r), 0, 1);
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
			
			if ((dragData.get(row).component instanceof Rocket) || (column == 4)){
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
