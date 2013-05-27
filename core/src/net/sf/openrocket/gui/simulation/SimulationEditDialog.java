package net.sf.openrocket.gui.simulation;


import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.FlightConfigurationModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.dialogs.flightconfiguration.FlightConfigurationDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.models.atmosphere.ExtendedISAModel;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.DefaultSimulationOptionFactory;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.simulation.listeners.example.CSVSaveListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.GeodeticComputationStrategy;


public class SimulationEditDialog extends JDialog {
	
	private final Window parentWindow;
	private final Simulation[] simulation;
	private final OpenRocketDocument document;
	private final SimulationOptions conditions;
	private final Configuration configuration;
	private static final Translator trans = Application.getTranslator();
	
	
	public SimulationEditDialog(Window parent, OpenRocketDocument document, Simulation... sims) {
		//// Edit simulation
		super(parent, trans.get("simedtdlg.title.Editsim"), JDialog.ModalityType.DOCUMENT_MODAL);
		this.document = document;
		this.parentWindow = parent;
		this.simulation = sims;
		this.conditions = simulation[0].getOptions();
		configuration = simulation[0].getConfiguration();
		
		JPanel mainPanel = new JPanel(new MigLayout(""));
		
		if (sims.length == 1) {
			//// Simulation name:
			mainPanel.add(new JLabel(trans.get("simedtdlg.lbl.Simname") + " "), "shrink");
			final JTextField field = new JTextField(simulation[0].getName());
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
					//System.out.println("Setting name:" + name);
					simulation[0].setName(name);
					
				}
			});
			mainPanel.add(field, "shrinky, growx, wrap");
			
			//// Flight selector
			//// Flight configuration:
			JLabel label = new JLabel(trans.get("simedtdlg.lbl.Flightcfg"));
			//// Select the motor configuration to use.
			label.setToolTipText(trans.get("simedtdlg.lbl.ttip.Flightcfg"));
			mainPanel.add(label, "shrink");
			
			JComboBox combo = new JComboBox(new FlightConfigurationModel(configuration));
			//// Select the motor configuration to use.
			combo.setToolTipText(trans.get("simedtdlg.combo.ttip.Flightcfg"));
			combo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					conditions.setMotorConfigurationID(configuration.getFlightConfigurationID());
				}
			});
			mainPanel.add(combo, "split 2, shrink");
			
			//// Edit button
			JButton button = new JButton(trans.get("simedtdlg.but.FlightcfgEdit"));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JDialog configDialog = new FlightConfigurationDialog(SimulationEditDialog.this.document.getRocket(), SwingUtilities.windowForComponent(SimulationEditDialog.this));
					configDialog.setVisible(true);
				}
			});
			mainPanel.add(button, "shrink, wrap");
		}
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//// Launch conditions
		tabbedPane.addTab(trans.get("simedtdlg.tab.Launchcond"), flightConditionsTab());
		//// Simulation options
		tabbedPane.addTab(trans.get("simedtdlg.tab.Simopt"), simulationOptionsTab());
		
		tabbedPane.setSelectedIndex(0);
		
		mainPanel.add(tabbedPane, "spanx, grow, wrap");
		
		
		//// Open Plot button
		JButton button = new JButton("<<Plot");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationEditDialog.this.dispose();
				SimulationPlotExportDialog plot = new SimulationPlotExportDialog(SimulationEditDialog.this.parentWindow,
						SimulationEditDialog.this.document, SimulationEditDialog.this.simulation[0]);
				plot.setVisible(true);
			}
			
		});
		mainPanel.add(button, "spanx, split 3, align left");
		if (sims.length == 1 && sims[0].hasSimulationData()) {
			button.setVisible(true);
		} else {
			button.setVisible(false);
		}
		
		//// Run simulation button
		button = new JButton(trans.get("simedtdlg.but.runsimulation"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationEditDialog.this.dispose();
				SimulationRunDialog.runSimulations(parentWindow, SimulationEditDialog.this.document, simulation);
			}
		});
		mainPanel.add(button, " align right, tag ok");
		
		//// Close button 
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationEditDialog.this.dispose();
			}
		});
		mainPanel.add(close, "tag ok");
		
		
		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, button);
	}
	
	private void copyChangesToAllSims() {
		if (simulation.length > 1) {
			for (int i = 1; i < simulation.length; i++) {
				simulation[i].getOptions().copyConditionsFrom(simulation[0].getOptions());
				simulation[i].getSimulationListeners().clear();
				simulation[i].getSimulationListeners().addAll(simulation[0].getSimulationListeners());
			}
		}
	}
	
	
	
	
	private JPanel flightConditionsTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		String tip;
		UnitSelector unit;
		BasicSlider slider;
		DoubleModel m;
		JSpinner spin;
		
		//// Wind settings:  Average wind speed, turbulence intensity, std. deviation
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Wind
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Wind")));
		panel.add(sub, "growx, split 2, aligny 0, flowy, gapright para");
		
		
		// Wind average
		//// Average windspeed:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Averwindspeed"));
		//// The average windspeed relative to the ground.
		tip = trans.get("simedtdlg.lbl.ttip.Averwindspeed");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "WindSpeedAverage", UnitGroup.UNITS_WINDSPEED, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 10.0));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		// Wind std. deviation
		//// Standard deviation:
		label = new JLabel(trans.get("simedtdlg.lbl.Stddeviation"));
		//// <html>The standard deviation of the windspeed.<br>
		//// The windspeed is within twice the standard deviation from the average for 95% of the time.
		tip = trans.get("simedtdlg.lbl.ttip.Stddeviation");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "WindSpeedDeviation", UnitGroup.UNITS_WINDSPEED, 0);
		DoubleModel m2 = new DoubleModel(conditions, "WindSpeedAverage", 0.25,
				UnitGroup.UNITS_COEFFICIENT, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(new DoubleModel(0), m2));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		// Wind turbulence intensity
		//// Turbulence intensity:
		label = new JLabel(trans.get("simedtdlg.lbl.Turbulenceintensity"));
		//// <html>The turbulence intensity is the standard deviation divided by the average windspeed.<br>
		//// Typical values range from 
		//// to
		tip = trans.get("simedtdlg.lbl.ttip.Turbulenceintensity1") +
				trans.get("simedtdlg.lbl.ttip.Turbulenceintensity2") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.05) +
				" " + trans.get("simedtdlg.lbl.ttip.Turbulenceintensity3") + " " +
				UnitGroup.UNITS_RELATIVE.getDefaultUnit().toStringUnit(0.20) + ".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "WindTurbulenceIntensity", UnitGroup.UNITS_RELATIVE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		
		final JLabel intensityLabel = new JLabel(
				getIntensityDescription(conditions.getWindTurbulenceIntensity()));
		intensityLabel.setToolTipText(tip);
		sub.add(intensityLabel, "w 75lp, wrap");
		m.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				intensityLabel.setText(
						getIntensityDescription(conditions.getWindTurbulenceIntensity()));
			}
		});
		
		
		
		
		
		//// Temperature and pressure
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Atmospheric conditions
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Atmoscond")));
		panel.add(sub, "growx, aligny 0, gapright para");
		
		
		BooleanModel isa = new BooleanModel(conditions, "ISAAtmosphere");
		JCheckBox check = new JCheckBox(isa);
		//// Use International Standard Atmosphere
		check.setText(trans.get("simedtdlg.checkbox.InterStdAtmosphere"));
		//// <html>Select to use the International Standard Atmosphere model.
		//// <br>This model has a temperature of
		//// and a pressure of
		//// at sea level.
		check.setToolTipText(trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere1") + " " +
				UnitGroup.UNITS_TEMPERATURE.toStringUnit(ExtendedISAModel.STANDARD_TEMPERATURE) +
				" " + trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere2") + " " +
				UnitGroup.UNITS_PRESSURE.toStringUnit(ExtendedISAModel.STANDARD_PRESSURE) +
				" " + trans.get("simedtdlg.checkbox.ttip.InterStdAtmosphere3"));
		sub.add(check, "spanx, wrap unrel");
		
		// Temperature:
		label = new JLabel(trans.get("simedtdlg.lbl.Temperature"));
		//// The temperature at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Temperature");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchTemperature", UnitGroup.UNITS_TEMPERATURE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(253.15, 308.15)); // -20 ... 35
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		// Pressure:
		label = new JLabel(trans.get("simedtdlg.lbl.Pressure"));
		//// The atmospheric pressure at the launch site.
		tip = trans.get("simedtdlg.lbl.ttip.Pressure");
		label.setToolTipText(tip);
		isa.addEnableComponent(label, false);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchPressure", UnitGroup.UNITS_PRESSURE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		isa.addEnableComponent(spin, false);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		isa.addEnableComponent(unit, false);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0.950e5, 1.050e5));
		slider.setToolTipText(tip);
		isa.addEnableComponent(slider, false);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		
		
		//// Launch site conditions
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Launch site
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.lbl.Launchsite")));
		panel.add(sub, "growx, split 2, aligny 0, flowy");
		
		
		// Latitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Latitude"));
		//// <html>The launch site latitude affects the gravitational pull of Earth.<br>
		//// Positive values are on the Northern hemisphere, negative values on the Southern hemisphere.
		tip = trans.get("simedtdlg.lbl.ttip.Latitude");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchLatitude", UnitGroup.UNITS_NONE, -90, 90);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		label = new JLabel(Chars.DEGREE + " N");
		label.setToolTipText(tip);
		sub.add(label, "growx");
		slider = new BasicSlider(m.getSliderModel(-90, 90));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		// Longitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Longitude"));
		tip = trans.get("simedtdlg.lbl.ttip.Longitude");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchLongitude", UnitGroup.UNITS_NONE, -180, 180);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		label = new JLabel(Chars.DEGREE + " E");
		label.setToolTipText(tip);
		sub.add(label, "growx");
		slider = new BasicSlider(m.getSliderModel(-180, 180));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		// Altitude:
		label = new JLabel(trans.get("simedtdlg.lbl.Altitude"));
		//// <html>The launch altitude above mean sea level.<br> 
		//// This affects the position of the rocket in the atmospheric model.
		tip = trans.get("simedtdlg.lbl.ttip.Altitude");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 250, 1000));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		
		
		//// Launch rod
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Launch rod
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Launchrod")));
		panel.add(sub, "growx, aligny 0, wrap");
		
		
		// Length:
		label = new JLabel(trans.get("simedtdlg.lbl.Length"));
		//// The length of the launch rod.
		tip = trans.get("simedtdlg.lbl.ttip.Length");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, 1, 5));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		// Angle:
		label = new JLabel(trans.get("simedtdlg.lbl.Angle"));
		//// The angle of the launch rod from vertical.
		tip = trans.get("simedtdlg.lbl.ttip.Angle");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodAngle", UnitGroup.UNITS_ANGLE,
				0, SimulationOptions.MAX_LAUNCH_ROD_ANGLE);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(0, Math.PI / 9,
				SimulationOptions.MAX_LAUNCH_ROD_ANGLE));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		
		
		// Direction:
		label = new JLabel(trans.get("simedtdlg.lbl.Direction"));
		//// <html>Direction of the launch rod relative to the wind.<br>
		////  = towards the wind, 
		////  = downwind.
		tip = trans.get("simedtdlg.lbl.ttip.Direction1") +
				UnitGroup.UNITS_ANGLE.toStringUnit(0) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction2") + " " +
				UnitGroup.UNITS_ANGLE.toStringUnit(Math.PI) +
				" " + trans.get("simedtdlg.lbl.ttip.Direction3");
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "LaunchRodDirection", UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "growx");
		slider = new BasicSlider(m.getSliderModel(-Math.PI, Math.PI));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		
		JButton restoreDefaults = new JButton(trans.get("simedtdlg.but.resettodefault"));
		restoreDefaults.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				SimulationOptions defaults = f.getDefault();
				conditions.copyConditionsFrom(defaults);
				
			}
			
		});
		panel.add(restoreDefaults, "span, split 3, skip, gapbottom para, gapright para, right");
		
		JButton saveDefaults = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveDefaults.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				DefaultSimulationOptionFactory f = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
				f.saveDefault(conditions);
				
			}
			
		});
		
		panel.add(saveDefaults, "gapbottom para, gapright para, right");
		return panel;
	}
	
	
	private String getIntensityDescription(double i) {
		if (i < 0.001)
			//// None
			return trans.get("simedtdlg.IntensityDesc.None");
		if (i < 0.05)
			//// Very low
			return trans.get("simedtdlg.IntensityDesc.Verylow");
		if (i < 0.10)
			//// Low
			return trans.get("simedtdlg.IntensityDesc.Low");
		if (i < 0.15)
			//// Medium
			return trans.get("simedtdlg.IntensityDesc.Medium");
		if (i < 0.20)
			//// High
			return trans.get("simedtdlg.IntensityDesc.High");
		if (i < 0.25)
			//// Very high
			return trans.get("simedtdlg.IntensityDesc.Veryhigh");
		//// Extreme
		return trans.get("simedtdlg.IntensityDesc.Extreme");
	}
	
	
	
	private JPanel simulationOptionsTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub, subsub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;
		
		
		//// Simulation options
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Simulator options
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Simopt")));
		panel.add(sub, "growx, growy, aligny 0");
		
		
		// Separate panel for computation methods, as they use a different layout
		subsub = new JPanel(new MigLayout("insets 0, fill"));
		
		
		//// Calculation method:
		tip = trans.get("simedtdlg.lbl.ttip.Calcmethod");
		label = new JLabel(trans.get("simedtdlg.lbl.Calcmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		//// Extended Barrowman
		label = new JLabel(trans.get("simedtdlg.lbl.ExtBarrowman"));
		label.setToolTipText(tip);
		subsub.add(label, "growx, wrap para");
		
		
		//  Simulation method
		tip = trans.get("simedtdlg.lbl.ttip.Simmethod1") +
				trans.get("simedtdlg.lbl.ttip.Simmethod2");
		label = new JLabel(trans.get("simedtdlg.lbl.Simmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		subsub.add(label, "growx, wrap para");
		
		
		//// Geodetic calculation method:
		label = new JLabel(trans.get("simedtdlg.lbl.GeodeticMethod"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GeodeticMethodTip"));
		subsub.add(label, "gapright para");
		
		EnumModel<GeodeticComputationStrategy> gcsModel = new EnumModel<GeodeticComputationStrategy>(conditions, "GeodeticComputation");
		final JComboBox gcsCombo = new JComboBox(gcsModel);
		ActionListener gcsTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) gcsCombo.getSelectedItem();
				gcsCombo.setToolTipText(gcs.getDescription());
			}
		};
		gcsCombo.addActionListener(gcsTTipListener);
		gcsTTipListener.actionPerformed(null);
		subsub.add(gcsCombo, "growx, wrap para");
		
		sub.add(subsub, "spanx, wrap para");
		
		
		//// Time step:
		label = new JLabel(trans.get("simedtdlg.lbl.Timestep"));
		tip = trans.get("simedtdlg.lbl.ttip.Timestep1") +
				trans.get("simedtdlg.lbl.ttip.Timestep2") + " " +
				UnitGroup.UNITS_TIME_STEP.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP) +
				".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "TimeStep", UnitGroup.UNITS_TIME_STEP, 0, 1);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		//sub.add(spin, "nogrid");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "w 25");
		//sub.add(unit, "nogrid");
		slider = new BasicSlider(m.getSliderModel(0, 0.2));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		//sub.add(slider,"wrap");
		
		
		
		
		//// Reset to default button
		JButton button = new JButton(trans.get("simedtdlg.but.resettodefault"));
		//// Reset the time step to its default value (
		button.setToolTipText(trans.get("simedtdlg.but.ttip.resettodefault") +
				UnitGroup.UNITS_SHORT_TIME.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP) +
				").");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				conditions.setTimeStep(RK4SimulationStepper.RECOMMENDED_TIME_STEP);
				conditions.setGeodeticComputation(GeodeticComputationStrategy.SPHERICAL);
			}
		});
		
		sub.add(button, "align left");
		
		
		
		
		//// Simulation listeners
		sub = new JPanel(new MigLayout("fill, gap 0 0"));
		//// Simulator listeners
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Simlist")));
		panel.add(sub, "growx, growy");
		
		
		DescriptionArea desc = new DescriptionArea(5);
		//// <html><i>Simulation listeners</i> is an advanced feature that allows user-written code to listen to and interact with the simulation.  
		//// For details on writing simulation listeners, see the OpenRocket technical documentation.
		desc.setText(trans.get("simedtdlg.txt.longA1") +
				trans.get("simedtdlg.txt.longA2"));
		sub.add(desc, "aligny 0, growx, wrap para");
		
		//// Current listeners:
		label = new JLabel(trans.get("simedtdlg.lbl.Curlist"));
		sub.add(label, "spanx, wrap rel");
		
		final ListenerListModel listenerModel = new ListenerListModel();
		final JList list = new JList(listenerModel);
		list.setCellRenderer(new ListenerCellRenderer());
		JScrollPane scroll = new JScrollPane(list);
		//		scroll.setPreferredSize(new Dimension(1,1));
		sub.add(scroll, "height 1px, grow, wrap rel");
		
		//// Add button
		button = new JButton(trans.get("simedtdlg.but.add"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String previous = Application.getPreferences().getString("previousListenerName", "");
				String input = (String) JOptionPane.showInputDialog(SimulationEditDialog.this,
						new Object[] {
								//// Type the full Java class name of the simulation listener, for example:
								"Type the full Java class name of the simulation listener, for example:",
								"<html><tt>" + CSVSaveListener.class.getName() + "</tt>" },
						//// Add simulation listener
						trans.get("simedtdlg.lbl.Addsimlist"),
						JOptionPane.QUESTION_MESSAGE,
						null, null,
						previous
						);
				if (input == null || input.equals(""))
					return;
				
				Application.getPreferences().putString("previousListenerName", input);
				simulation[0].getSimulationListeners().add(input);
				listenerModel.fireContentsChanged();
			}
		});
		sub.add(button, "split 2, sizegroup buttons, alignx 50%, gapright para");
		
		//// Remove button
		button = new JButton(trans.get("simedtdlg.but.remove"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selected = list.getSelectedIndices();
				Arrays.sort(selected);
				for (int i = selected.length - 1; i >= 0; i--) {
					simulation[0].getSimulationListeners().remove(selected[i]);
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
			return simulation[0].getSimulationListeners().get(index);
		}
		
		@Override
		public int getSize() {
			return simulation[0].getSimulationListeners().size();
		}
		
		public void fireContentsChanged() {
			super.fireContentsChanged(this, 0, getSize());
		}
	}
	
	
	
	
	
	/**
	 * Return a panel stating that there is no data available, and that the user
	 * should run the simulation first.
	 */
	public static JPanel noDataPanel() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// No data available
		//// No flight data available.
		panel.add(new JLabel(trans.get("simedtdlg.lbl.Noflightdata")),
				"alignx 50%, aligny 100%, wrap para");
		//// Please run the simulation first.
		panel.add(new JLabel(trans.get("simedtdlg.lbl.runsimfirst")),
				"alignx 50%, aligny 0%, wrap");
		return panel;
	}
	
	
	
	private class ListenerCellRenderer extends JLabel implements ListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String s = value.toString();
			setText(s);
			
			// Attempt instantiating, catch any exceptions
			Exception ex = null;
			try {
				Class<?> c = Class.forName(s);
				@SuppressWarnings("unused")
				SimulationListener l = (SimulationListener) c.newInstance();
			} catch (Exception e) {
				ex = e;
			}
			
			if (ex == null) {
				setIcon(Icons.SIMULATION_LISTENER_OK);
				//// Listener instantiated successfully.
				setToolTipText("Listener instantiated successfully.");
			} else {
				setIcon(Icons.SIMULATION_LISTENER_ERROR);
				//// <html>Unable to instantiate listener due to exception:<br>
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
