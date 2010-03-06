package net.sf.openrocket.gui.main;


import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.ExtendedISAModel;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.MotorConfigurationModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.SimulationExportPanel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.plot.Axis;
import net.sf.openrocket.gui.plot.PlotConfiguration;
import net.sf.openrocket.gui.plot.SimulationPlotPanel;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.RK4Simulator;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationListener;
import net.sf.openrocket.simulation.listeners.CSVSaveListener;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Icons;
import net.sf.openrocket.util.Prefs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class SimulationEditDialog extends JDialog {

	public static final int DEFAULT = -1;
	public static final int EDIT = 1;
	public static final int PLOT = 2;
	
	
	private final Window parentWindow;
	private final Simulation simulation;
	private final SimulationConditions conditions;
	private final Configuration configuration;
	
	
	public SimulationEditDialog(Window parent, Simulation s) {
		this(parent, s, 0);
	}	
	
	public SimulationEditDialog(Window parent, Simulation s, int tab) {
		super(parent, "Edit simulation", JDialog.ModalityType.DOCUMENT_MODAL);
		
		this.parentWindow = parent;
		this.simulation = s;
		this.conditions = simulation.getConditions();
		configuration = simulation.getConfiguration();
		
		JPanel mainPanel = new JPanel(new MigLayout("fill","[grow, fill]"));
		
		mainPanel.add(new JLabel("Simulation name: "), "span, split 2, shrink");
		final JTextField field = new JTextField(simulation.getName());
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				setText();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				setText();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				setText();
			}
			private void setText() {
				String name = field.getText();
				if (name == null || name.equals(""))
					return;
				System.out.println("Setting name:"+name);
				simulation.setName(name);
				
			}
		});
		mainPanel.add(field, "shrinky, growx, wrap");
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		
		tabbedPane.addTab("Launch conditions", flightConditionsTab());
		tabbedPane.addTab("Simulation options", simulationOptionsTab());
		tabbedPane.addTab("Plot data", plotTab());
		tabbedPane.addTab("Export data", exportTab());
		
		// Select the initial tab
		if (tab == EDIT) {
			tabbedPane.setSelectedIndex(0);
		} else if (tab == PLOT) {
			tabbedPane.setSelectedIndex(2);
		} else {
			FlightData data = s.getSimulatedData();
			if (data == null || data.getBranchCount() == 0)
				tabbedPane.setSelectedIndex(0);
			else
				tabbedPane.setSelectedIndex(2);
		}
		
		mainPanel.add(tabbedPane, "spanx, grow, wrap");

		
		// Buttons
		mainPanel.add(new JPanel(), "spanx, split, growx");

		JButton button;
		button = new JButton("Run simulation");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationEditDialog.this.dispose();
				SimulationRunDialog.runSimulations(parentWindow, simulation);
			}
		});
		mainPanel.add(button, "gapright para");
		
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationEditDialog.this.dispose();
			}
		});
		mainPanel.add(close, "");
		
		
		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, button);
	}
	
	
	
	
	
	private JPanel flightConditionsTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		String tip;
		UnitSelector unit;
		BasicSlider slider;
		DoubleModel m;
		JSpinner spin;
		
		//// Motor selector
		JLabel label = new JLabel("Motor configuration:");
		label.setToolTipText("Select the motor configuration to use.");
		panel.add(label, "shrinkx, spanx, split 2");
		
		JComboBox combo = new JComboBox(new MotorConfigurationModel(configuration));
		combo.setToolTipText("Select the motor configuration to use.");
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				conditions.setMotorConfigurationID(configuration.getMotorConfigurationID());
			}
		});
		panel.add(combo, "growx, wrap para");
		
		
		//// Wind settings:  Average wind speed, turbulence intensity, std. deviation
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]",""));
		sub.setBorder(BorderFactory.createTitledBorder("Wind"));
		panel.add(sub, "growx, split 2, aligny 0, flowy, gapright para");


		// Wind average
		label = new JLabel("Average windspeed:");
		tip = "The average windspeed relative to the ground.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"WindSpeedAverage", UnitGroup.UNITS_VELOCITY,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, 10.0));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		


		// Wind std. deviation
		label = new JLabel("Standard deviation:");
		tip = "<html>The standard deviation of the windspeed.<br>" +
				"The windspeed is within twice the standard deviation from the average for " +
				"95% of the time.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"WindSpeedDeviation", UnitGroup.UNITS_VELOCITY,0);
		DoubleModel m2 = new DoubleModel(conditions,"WindSpeedAverage", 0.25, 
				UnitGroup.UNITS_COEFFICIENT,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(new DoubleModel(0), m2));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
				

		// Wind turbulence intensity
		label = new JLabel("Turbulence intensity:");
		tip = "<html>The turbulence intensity is the standard deviation " +
			"divided by the average windspeed.<br>" +
			"Typical values range from "+
			UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.05) +
			" to " +
			UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.20) + ".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"WindTurbulenceIntensity", UnitGroup.UNITS_RELATIVE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		
		final JLabel intensityLabel = new JLabel(
				getIntensityDescription(conditions.getWindTurbulenceIntensity()));
		intensityLabel.setToolTipText(tip);
		sub.add(intensityLabel,"w 75lp, wrap");
		m.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(
						getIntensityDescription(conditions.getWindTurbulenceIntensity()));
			}
		});
		

		
		
		
		//// Temperature and pressure
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]",""));
		sub.setBorder(BorderFactory.createTitledBorder("Atmospheric conditions"));
		panel.add(sub, "growx, aligny 0, gapright para");

		
		BooleanModel isa = new BooleanModel(conditions, "ISAAtmosphere");
		JCheckBox check = new JCheckBox(isa);
		check.setText("Use International Standard Atmosphere");
		check.setToolTipText("<html>Select to use the International Standard Atmosphere model."+
				"<br>This model has a temperature of " + 
				UnitGroup.UNITS_TEMPERATURE.toStringUnit(ExtendedISAModel.STANDARD_TEMPERATURE)+
				" and a pressure of " +
				UnitGroup.UNITS_PRESSURE.toStringUnit(ExtendedISAModel.STANDARD_PRESSURE) +
				" at sea level.");
		sub.add(check, "spanx, wrap unrel");
		
		// Temperature
		label = new JLabel("Temperature:");
		tip = "The temperature at the launch site.";
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchTemperature", UnitGroup.UNITS_TEMPERATURE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(253.15, 308.15));  // -20 ... 35
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider,"w 75lp, wrap");
		

		
		// Pressure
		label = new JLabel("Pressure:");
		tip = "The atmospheric pressure at the launch site.";
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchPressure", UnitGroup.UNITS_PRESSURE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0.950e5, 1.050e5));
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider,"w 75lp, wrap");
		

		
		
		
		//// Launch site conditions
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]",""));
		sub.setBorder(BorderFactory.createTitledBorder("Launch site"));
		panel.add(sub, "growx, split 2, aligny 0, flowy");

		
		// Latitude
		label = new JLabel("Latitude:");
		tip = "<html>The launch site latitude affects the gravitational pull of Earth.<br>" +
			"Positive values are on the Northern hemisphere, negative values on the " +
			"Southern hemisphere.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchLatitude", UnitGroup.UNITS_NONE, -90, 90);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		label = new JLabel(Chars.DEGREE + " N");
		label.setToolTipText(tip);
		sub.add(label,"growx");
		slider = new BasicSlider(m.getSliderModel(-90, 90));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		

		
		// Altitude
		label = new JLabel("Altitude:");
		tip = "<html>The launch altitude above mean sea level.<br>" +
				"This affects the position of the rocket in the atmospheric model.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchAltitude", UnitGroup.UNITS_DISTANCE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, 250, 1000));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		

		

		
		//// Launch rod
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]",""));
		sub.setBorder(BorderFactory.createTitledBorder("Launch rod"));
		panel.add(sub, "growx, aligny 0, wrap");

		
		// Length
		label = new JLabel("Length:");
		tip = "The length of the launch rod.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchRodLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, 1, 5));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		

		
		// Angle
		label = new JLabel("Angle:");
		tip = "The angle of the launch rod from vertical.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchRodAngle", UnitGroup.UNITS_ANGLE,
				0, SimulationConditions.MAX_LAUNCH_ROD_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, Math.PI/9, 
				SimulationConditions.MAX_LAUNCH_ROD_ANGLE));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		

		
		// Direction
		label = new JLabel("Direction:");
		tip = "<html>Direction of the launch rod relative to the wind.<br>" +
				UnitGroup.UNITS_ANGLE.toStringUnit(0) +
				" = towards the wind, "+
				UnitGroup.UNITS_ANGLE.toStringUnit(Math.PI) +
				" = downwind.";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"LaunchRodDirection", UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(-Math.PI, Math.PI));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		
		return panel;
	}

	
	private String getIntensityDescription(double i) {
		if (i < 0.001)
			return "None";
		if (i < 0.05)
			return "Very low";
		if (i < 0.10)
			return "Low";
		if (i < 0.15)
			return "Medium";
		if (i < 0.20)
			return "High";
		if (i < 0.25)
			return "Very high";
		return "Extreme";
	}

	
	
	private JPanel simulationOptionsTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;

		
		//// Simulation options
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]",""));
		sub.setBorder(BorderFactory.createTitledBorder("Simulator options"));
		panel.add(sub, "w 330lp!, growy, aligny 0");

		
		//  Calculation method
		tip = "<html>" +
				"The Extended Barrowman method calculates aerodynamic forces according <br>" +
				"to the Barrowman equations extended to accommodate more components.";

		label = new JLabel("Calculation method:");
		label.setToolTipText(tip);
		sub.add(label, "gaptop unrel, gapright para, spanx, split 2, w 150lp!");
		
		label = new JLabel("Extended Barrowman");
		label.setToolTipText(tip);
		sub.add(label, "growx, wrap para");
		
		
		//  Simulation method
		tip = "<html>" +
				"The six degree-of-freedom simulator allows the rocket total freedom during " +
				"flight.<br>" +
				"Integration is performed using a 4<sup>th</sup> order Runge-Kutta 4 " +
				"numerical integration.";

		label = new JLabel("Simulation method:");
		label.setToolTipText(tip);
		sub.add(label, "gaptop unrel, gapright para, spanx, split 2, w 150lp!");
		
		label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		sub.add(label, "growx, wrap 35lp");
		
		
		// Wind average
		label = new JLabel("Time step:");
		tip = "<html>The time between simulation steps.<br>" +
		"A smaller time step results in a more accurate but slower simulation.<br>" +
				"The 4<sup>th</sup> order simulation method is quite accurate with a time " +
				"step of " +
				UnitGroup.UNITS_TIME_STEP.toStringUnit(RK4Simulator.RECOMMENDED_TIME_STEP) +
				".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"TimeStep", UnitGroup.UNITS_TIME_STEP, 0, 1);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, 0.2));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap");
		


		// Maximum angle step
		/*
		label = new JLabel("Max. angle step:");
		tip = "<html>" +
				"This defines the maximum angle the rocket will turn during one time step.<br>"+
				"Smaller values result in a more accurate but possibly slower simulation.<br>"+
				"A recommended value is " +
				UnitGroup.UNITS_ANGLE.toStringUnit(RK4Simulator.RECOMMENDED_ANGLE_STEP) + ".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions,"MaximumStepAngle", UnitGroup.UNITS_ANGLE, 
				1*Math.PI/180, Math.PI/9);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin,"w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit,"growx");
		slider = new BasicSlider(m.getSliderModel(0, Math.toRadians(10)));
		slider.setToolTipText(tip);
		sub.add(slider,"w 75lp, wrap para");
		*/

		JButton button = new JButton("Reset to default");
		button.setToolTipText("Reset the time step to its default value (" +
				UnitGroup.UNITS_SHORT_TIME.toStringUnit(RK4Simulator.RECOMMENDED_TIME_STEP) +
				").");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				conditions.setTimeStep(RK4Simulator.RECOMMENDED_TIME_STEP);
			}
		});
				
//		button.setToolTipText("<html>Reset the step value to its default:<br>" +
//				"Time step " +
//				UnitGroup.UNITS_SHORT_TIME.toStringUnit(RK4Simulator.RECOMMENDED_TIME_STEP) +
//				"; maximum angle step " +
//				UnitGroup.UNITS_ANGLE.toStringUnit(RK4Simulator.RECOMMENDED_ANGLE_STEP) + ".");
		sub.add(button, "spanx, tag right, wrap para");
		
		
		
		
		//// Simulation listeners
		sub = new JPanel(new MigLayout("fill, gap 0 0"));
		sub.setBorder(BorderFactory.createTitledBorder("Simulator listeners"));
		panel.add(sub, "growx, growy");
		
		
		DescriptionArea desc = new DescriptionArea(5);
		desc.setText("<html>" +
				"<i>Simulation listeners</i> is an advanced feature that allows "+
				"user-written code to listen to and interact with the simulation.  " +
				"For details on writing simulation listeners, see the OpenRocket " +
				"technical documentation.");
		sub.add(desc, "aligny 0, growx, wrap para");
		
		
		label = new JLabel("Current listeners:");
		sub.add(label, "spanx, wrap rel");

		final ListenerListModel listenerModel = new ListenerListModel();
		final JList list = new JList(listenerModel);
		list.setCellRenderer(new ListenerCellRenderer());
		JScrollPane scroll = new JScrollPane(list);
//		scroll.setPreferredSize(new Dimension(1,1));
		sub.add(scroll, "height 1px, grow, wrap rel");
		
		
		button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String previous = Prefs.NODE.get("previousListenerName", "");
				String input = (String)JOptionPane.showInputDialog(SimulationEditDialog.this,
						new Object[] {
						"Type the full Java class name of the simulation listener, for example:",
						"<html><tt>" + CSVSaveListener.class.getName() + "</tt>" },
						"Add simulation listener",
						JOptionPane.QUESTION_MESSAGE,
						null, null,
						previous
				);
				if (input == null || input.equals(""))
					return;

				Prefs.NODE.put("previousListenerName", input);
				simulation.getSimulationListeners().add(input);
				listenerModel.fireContentsChanged();
			}
		});
		sub.add(button, "split 2, sizegroup buttons, alignx 50%, gapright para");
		
		button = new JButton("Remove");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selected = list.getSelectedIndices();
				Arrays.sort(selected);
				for (int i=selected.length-1; i>=0; i--) {
					simulation.getSimulationListeners().remove(selected[i]);
				}
				listenerModel.fireContentsChanged();
			}
		});
		sub.add(button, "sizegroup buttons, alignx 50%");
		

		return panel;
	}


	private class ListenerListModel extends AbstractListModel {
		@Override
		public String getElementAt(int index) {
			if (index < 0 || index >= getSize())
				return null;
			return simulation.getSimulationListeners().get(index);
		}
		@Override
		public int getSize() {
			return simulation.getSimulationListeners().size();
		}
		public void fireContentsChanged() {
			super.fireContentsChanged(this, 0, getSize());
		}
	}
	
	
	
	
	/**
	 * A panel for plotting the previously calculated data.
	 */
	private JPanel plotTab() {

		// Check that data exists
		if (simulation.getSimulatedData() == null  ||
				simulation.getSimulatedData().getBranchCount() == 0) {
			return noDataPanel();
		}
		
		return new SimulationPlotPanel(simulation);
	}
	
	
	
	/**
	 * A panel for exporting the data.
	 */
	private JPanel exportTab() {
		FlightData data = simulation.getSimulatedData();

		// Check that data exists
		if (data == null  || data.getBranchCount() == 0 ||
				data.getBranch(0).getTypes().length == 0) {
			return noDataPanel();
		}
		
		return new SimulationExportPanel(simulation);
	}
	
	
	
	

	/**
	 * Return a panel stating that there is no data available, and that the user
	 * should run the simulation first.
	 */
	public static JPanel noDataPanel() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// No data available
		panel.add(new JLabel("No flight data available."),
		"alignx 50%, aligny 100%, wrap para");
		panel.add(new JLabel("Please run the simulation first."),
		"alignx 50%, aligny 0%, wrap");
		return panel;
	}
	

	private void performPlot(PlotConfiguration config) {

		// Fill the auto-selections
		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		PlotConfiguration filled = config.fillAutoAxes(branch);
		List<Axis> axes = filled.getAllAxes();


		// Create the data series for both axes
		XYSeriesCollection[] data = new XYSeriesCollection[2];
		data[0] = new XYSeriesCollection();
		data[1] = new XYSeriesCollection();
		
		
		// Get the domain axis type
		final FlightDataBranch.Type domainType = filled.getDomainAxisType();
		final Unit domainUnit = filled.getDomainAxisUnit();
		if (domainType == null) {
			throw new IllegalArgumentException("Domain axis type not specified.");
		}
		List<Double> x = branch.get(domainType);

		
		// Create the XYSeries objects from the flight data and store into the collections
		int length = filled.getTypeCount();
		String[] axisLabel = new String[2];
		for (int i = 0; i < length; i++) {
			// Get info
			FlightDataBranch.Type type = filled.getType(i);
			Unit unit = filled.getUnit(i);
			int axis = filled.getAxis(i);
			String name = getLabel(type, unit);
			
			// Store data in provided units
			List<Double> y = branch.get(type);
			XYSeries series = new XYSeries(name, false, true);
			for (int j=0; j<x.size(); j++) {
				series.add(domainUnit.toUnit(x.get(j)), unit.toUnit(y.get(j)));
			}
			data[axis].addSeries(series);

			// Update axis label
			if (axisLabel[axis] == null)
				axisLabel[axis] = type.getName();
			else
				axisLabel[axis] += "; " + type.getName();
		}
		
		
		// Create the chart using the factory to get all default settings
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Simulated flight",
            null, 
            null, 
            null,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
		
        
		// Add the data and formatting to the plot
		XYPlot plot = chart.getXYPlot();
		int axisno = 0;
		for (int i=0; i<2; i++) {
			// Check whether axis has any data
			if (data[i].getSeriesCount() > 0) {
				// Create and set axis
				double min = axes.get(i).getMinValue();
				double max = axes.get(i).getMaxValue();
				NumberAxis axis = new PresetNumberAxis(min, max);
				axis.setLabel(axisLabel[i]);
//				axis.setRange(axes.get(i).getMinValue(), axes.get(i).getMaxValue());
				plot.setRangeAxis(axisno, axis);
				
				// Add data and map to the axis
				plot.setDataset(axisno, data[i]);
				plot.setRenderer(axisno, new StandardXYItemRenderer());
				plot.mapDatasetToRangeAxis(axisno, axisno);
				axisno++;
			}
		}
		
		plot.getDomainAxis().setLabel(getLabel(domainType,domainUnit));
		plot.addDomainMarker(new ValueMarker(0));
		plot.addRangeMarker(new ValueMarker(0));
		
		
		// Create the dialog
		final JDialog dialog = new JDialog(this, "Simulation results");
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		dialog.add(panel);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true,  // save
				false, // print
				true,  // zoom
				true); // tooltips
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setEnforceFileExtensions(true);
		chartPanel.setInitialDelay(500);
		
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		
		panel.add(chartPanel, "grow, wrap 20lp");

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		panel.add(button, "right");

		dialog.setLocationByPlatform(true);
		dialog.pack();
		
		GUIUtil.setDisposableDialogOptions(dialog, button);

		dialog.setVisible(true);
	}

	
	private class PresetNumberAxis extends NumberAxis {
		private final double min;
		private final double max;
		
		public PresetNumberAxis(double min, double max) {
			this.min = min;
			this.max = max;
			autoAdjustRange();
		}
		
		@Override
		protected void autoAdjustRange() {
			this.setRange(min, max);
		}
	}
	
	
	private String getLabel(FlightDataBranch.Type type, Unit unit) {
		String name = type.getName();
		if (unit != null  &&  !UnitGroup.UNITS_NONE.contains(unit)  &&
				!UnitGroup.UNITS_COEFFICIENT.contains(unit) && unit.getUnit().length() > 0)
			name += " ("+unit.getUnit() + ")";
		return name;
	}
	


	private class ListenerCellRenderer extends JLabel implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String s = value.toString();
			setText(s);

			// Attempt instantiating, catch any exceptions
			Exception ex = null;
			try {
				Class<?> c = Class.forName(s);
				@SuppressWarnings("unused")
				SimulationListener l = (SimulationListener)c.newInstance();
			} catch (Exception e) {
				ex = e;
			}

			if (ex == null) {
				setIcon(Icons.SIMULATION_LISTENER_OK);
				setToolTipText("Listener instantiated successfully.");
			} else {
				setIcon(Icons.SIMULATION_LISTENER_ERROR);
				setToolTipText("<html>Unable to instantiate listener due to exception:<br>" +
						ex.toString());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setOpaque(true);
			return this;
		}
	}
}
